package net.lacnic.elections.ejb.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.dao.ReportDao;
import net.lacnic.elections.data.ElectionReport;
import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participation;
import net.lacnic.elections.data.ParticipationStatusV2;
import net.lacnic.elections.data.ParticipationTypeV2;
import net.lacnic.elections.data.ParticipationV2;
import net.lacnic.elections.data.TableReportDataStringId;
import net.lacnic.elections.data.TablesReportDataLongId;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Commissioner;
import net.lacnic.elections.domain.Customization;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.ElectionLight;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.EmailHistory;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;
import net.lacnic.elections.domain.services.dbtables.ActivityTableReport;
import net.lacnic.elections.domain.services.dbtables.AuditorTableReport;
import net.lacnic.elections.domain.services.dbtables.CandidateTableReport;
import net.lacnic.elections.domain.services.dbtables.CommissionerTableReport;
import net.lacnic.elections.domain.services.dbtables.CustomizationTableReport;
import net.lacnic.elections.domain.services.dbtables.ElectionEmailTemplateTableReport;
import net.lacnic.elections.domain.services.dbtables.ElectionTableReport;
import net.lacnic.elections.domain.services.dbtables.EmailTableReport;
import net.lacnic.elections.domain.services.dbtables.UserAdminTableReport;
import net.lacnic.elections.domain.services.dbtables.UserVoterTableReport;
import net.lacnic.elections.domain.services.dbtables.VoteTableReport;
import net.lacnic.elections.domain.services.detail.ElectionDetailReport;
import net.lacnic.elections.domain.services.detail.ElectionParticipationDetailReport;
import net.lacnic.elections.domain.services.detail.OrganizationVoterDetailReport;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCoreSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionOfficialResultSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionPhotoSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionRollSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionsSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionsSnapshotMetadata;
import net.lacnic.elections.ejb.ElectionsMonitorEJB;
import net.lacnic.elections.publicelection.PublicElectionSnapshotBuilder;
import net.lacnic.elections.publicelection.PublicElectionSnapshotBundle;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.ElectionsCaches;
import net.lacnic.elections.utils.ElectionsProperties;
import net.lacnic.elections.utils.VotingPeriodResolver;
import net.ripe.ipresource.IpResourceSet;

@Stateless
@Remote(ElectionsMonitorEJB.class)
public class ElectionsMonitorEJBBean implements ElectionsMonitorEJB {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	public ElectionsMonitorEJBBean() {
		// Intencionalmente vacio: el contenedor EJB inyecta dependencias y gestiona el
		// ciclo de vida.
	}

	/**
	 * Get the health check data.
	 * 
	 * @return returns a health check entity containing the system's health
	 *         information.
	 */
	@Override
	public HealthCheck getHealthCheckData() {
		HealthCheck healthCheck = ElectionsCaches.getHealthCheck();
		if (healthCheck == null)
			return updateHealthCheckData();
		return healthCheck;
	}

	/**
	 * Creates a updated health check report.
	 * 
	 * @return returns a health check entity containing the health check information
	 *         from the system.
	 */
	@Override
	public HealthCheck updateHealthCheckData() {
		try {
			boolean baseOk = isDatabaseAvailable();
			if (!baseOk) {
				HealthCheck unavailableHealthCheck = new HealthCheck("database unavailable");
				unavailableHealthCheck.setBaseOk(false);
				return unavailableHealthCheck;
			}

			ReportDao reportDao = ElectionsDaoFactory.createReportDao(em);

			int sendAttempts = 0;
			long failedAccessIps = reportDao.getFailedIpAccesesAmount();
			long failedAccessSum = reportDao.getFailedIpAccesesSum();
			long mailsTotal = reportDao.getEmailsAmount();
			long mailsPending = reportDao.getPendingSendEmailsAmount();
			long mailsSent = reportDao.getSentEmailsAmount();

			List<ElectionReport> elections = electionReport();

			HealthCheck updatedHealthCheck = new HealthCheck(sendAttempts, failedAccessIps, failedAccessSum, mailsTotal, mailsPending, mailsSent, elections);
			updatedHealthCheck.setBaseOk(true);
			ElectionsCaches.putHealthCheck(updatedHealthCheck);
			return updatedHealthCheck;
		} catch (Exception e) {
			return new HealthCheck("Health check metrics unavailable");
		}
	}

	private boolean isDatabaseAvailable() {
		if (em == null) {
			return false;
		}
		try {
			em.createNativeQuery("SELECT 1").getSingleResult();
			return true;
		} catch (Exception e) {
			appLogger.warn("Could not verify database connection for health check", e);
			return false;
		}
	}

	/**
	 * Gets a list of reports from all the elections on the system
	 * 
	 * @return returns a collection of election report entity containing the
	 *         information
	 */
	private List<ElectionReport> electionReport() {
		ReportDao reportDao = ElectionsDaoFactory.createReportDao(em);
		List<ElectionReport> election = new ArrayList<>();

		for (Object[] nameAndId : reportDao.getElectionsAllIdName()) {
			Long id = (Long) nameAndId[0];
			String electionName = (String) nameAndId[1];
			long usersVoted = reportDao.getElectionAlreadyVotedAmount(id);
			long usersNotVoted = reportDao.getElectionNotVotedYetAmount(id);
			long usersTotal = reportDao.getElectionCensusSize(id);
			long pendingMails = reportDao.getElectionPendingSendEmailsAmount(id);
			ElectionReport eleccionReport = new ElectionReport(electionName, usersVoted, usersNotVoted, usersTotal, pendingMails);
			election.add(eleccionReport);
		}

		return election;
	}

	/**
	 * Get a list of all the participations of an organization in all the elections.
	 * 
	 * @param org String containing the organization looked for.
	 * 
	 * @return returns a collection of participation entity with the information.
	 */
	@Override
	public List<Participation> getOrganizationParticipations(String org) {
		List<Participation> participations = new ArrayList<>();
		List<Election> elections = ElectionsDaoFactory.createElectionDao(em).getElectionsAllOrderStartDateDesc();
		VotingPeriodResolver.applyVotingWindow(em, elections);
		Date now = new Date();
		for (Election election : elections) {
			UserVoter userVoter = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoterByOrganization(org, election.getElectionId());
			Participation participation = new Participation();
			participation.setCategory(election.getCategory().toString());
			Date votingStartDate = election.getVotingPeriodStartDate();
			Date votingEndDate = election.getVotingPeriodEndDate();
			boolean votingEnabled = election.isVotingLinkAvailable() && votingStartDate != null && votingEndDate != null && now.after(votingStartDate) && now.before(votingEndDate);
			if (userVoter != null) {
				participation.setEmail(userVoter.getMail());
				participation.setName(userVoter.getName());
				participation.setCountry(userVoter.getCountry());
				participation.setVoted(userVoter.isVoted());
				if (votingEnabled) {
					participation.setVoteLink(userVoter.getVoteLink());
				} else {
					participation.setVoteLink("");
				}
			} else {
				participation.setEmail("");
				participation.setName("");
				participation.setCountry("");
				participation.setVoted(false);
				participation.setVoteLink("");
			}
			participation.setElectionEndDate(election.getVotingPeriodEndDate());
			participation.setElectionStartDate(election.getVotingPeriodStartDate());
			participation.setOrgId(org);
			participation.setElectionTitleEN(election.getTitleEnglish());
			participation.setElectionTitleSP(election.getTitleSpanish());
			participation.setElectionTitlePT(election.getTitlePortuguese());
			participation.setElectionLinkSP(election.getLinkSpanish());
			participation.setElectionLinkEN(election.getLinkEnglish());
			participation.setElectionLinkPT(election.getLinkPortuguese());

			participations.add(participation);
		}
		return participations;
	}

	@Override
	public List<ParticipationV2> getOrganizationParticipationsV2(String org) {
		List<ParticipationV2> participations = new ArrayList<>();
		List<Election> elections = ElectionsDaoFactory.createElectionDao(em).getElectionsAllOrderStartDateDesc();
		VotingPeriodResolver.applyVotingWindow(em, elections);
		Date now = new Date();

		for (Election election : elections) {
			participations.add(buildVoteParticipationV2(org, election, now));

			Organization organization = ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByElectionAndOrgId(election.getElectionId(), org);
			if (organization == null) {
				continue;
			}

			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getLatestNominationByElectionAndOrganizationId(election.getElectionId(), organization.getId());
			participations.add(buildNominationParticipationV2(org, election, organization, nomination, now));
			List<SupportNomination> supportNominations = ElectionsDaoFactory.createSupportNominationDao(em).getElectionSupportNominationsBySupportingOrganizationId(election.getElectionId(), organization.getId());
			for (SupportNomination supportNomination : supportNominations) {
				participations.add(buildSupportParticipationV2(org, election, supportNomination, now));
			}
		}

		return participations;
	}

	private ParticipationV2 buildVoteParticipationV2(String org, Election election, Date now) {
		ParticipationV2 participation = buildParticipationV2Base(org, election);
		participation.setParticipationType(ParticipationTypeV2.VOTE);

		UserVoter userVoter = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoterByOrganization(org, election.getElectionId());
		boolean availableByWindow = isPublicLinkAvailableForCalendarWindow(election, ElectionCalendarKey.N_16_PERIODO_VOTING, election.isVotingLinkAvailable(), now);

		if (userVoter == null) {
			participation.setEmail("");
			participation.setName("");
			participation.setCountry("");
			participation.setLink("");
			participation.setStatus(ParticipationStatusV2.BLOCKED);
			return participation;
		}

		participation.setEmail(defaultText(userVoter.getMail()));
		participation.setName(defaultText(userVoter.getName()));
		participation.setCountry(defaultText(userVoter.getCountry()));

		if (userVoter.isVoted()) {
			participation.setLink("");
			participation.setStatus(ParticipationStatusV2.USED);
		} else if (availableByWindow && hasText(userVoter.getVoteToken())) {
			participation.setLink(defaultText(userVoter.getVoteLink()));
			participation.setStatus(ParticipationStatusV2.AVAILABLE);
		} else {
			participation.setLink("");
			participation.setStatus(ParticipationStatusV2.BLOCKED);
		}

		return participation;
	}

	private ParticipationV2 buildNominationParticipationV2(String org, Election election, Organization organization, Nomination nomination, Date now) {
		ParticipationV2 participation = buildParticipationV2Base(org, election);
		participation.setParticipationType(ParticipationTypeV2.NOMINATION);

		boolean used = hasBlockingNomination(election, organization);
		boolean availableByWindow = isPublicLinkAvailableForCalendarWindow(election, ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES, election.isDoNominationLinkAvailable(), now);
		participation.setEmail(resolveNominationParticipationEmail(organization, nomination, used));
		participation.setName(resolveNominationParticipationName(organization, nomination, used));
		participation.setCountry(defaultText(organization != null ? organization.getCountry() : null));

		if (used) {
			participation.setLink("");
			participation.setStatus(ParticipationStatusV2.USED);
		} else if (canExposeNominationLink(organization, availableByWindow)) {
			participation.setLink(defaultText(organization.getDoNominationLink()));
			participation.setStatus(ParticipationStatusV2.AVAILABLE);
		} else {
			participation.setLink("");
			participation.setStatus(ParticipationStatusV2.BLOCKED);
		}

		return participation;
	}

	static boolean canExposeNominationLink(Organization organization, boolean availableByWindow) {
		return availableByWindow && organization != null && !organization.isDeudor()
				&& organization.getDoNominationToken() != null && !organization.getDoNominationToken().trim().isEmpty();
	}

	private boolean hasBlockingNomination(Election election, Organization organization) {
		if (election == null || organization == null) {
			return false;
		}
		return ElectionsDaoFactory.createNominationDao(em).existsElectionNominationByOrganizationIdAndStatuses(election.getElectionId(), organization.getId(), NominationStatus.blockingForNewNomination());
	}

	private ParticipationV2 buildSupportParticipationV2(String org, Election election, SupportNomination supportNomination, Date now) {
		ParticipationV2 participation = buildParticipationV2Base(org, election);
		participation.setParticipationType(ParticipationTypeV2.SUPPORT);

		Organization supportingOrganization = supportNomination != null ? supportNomination.getSupportingOrganization() : null;
		Nomination nomination = supportNomination != null ? supportNomination.getNomination() : null;
		String supportEmail = resolveNominationCandidateEmail(nomination);
		String supportName = resolveNominationCandidateName(nomination);

		participation.setEmail(defaultText(supportEmail));
		participation.setName(defaultText(supportName));
		participation.setCountry(defaultText(supportingOrganization != null ? supportingOrganization.getCountry() : null));

		SupportStatus supportStatus = supportNomination != null ? supportNomination.getSupportStatus() : null;
		boolean used = supportStatus != null && supportStatus != SupportStatus.PROPOSED;
		boolean availableByWindow = isPublicLinkAvailableForCalendarWindow(election, ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES, election.isNominationSupportLinkAvailable(), now);

		if (used) {
			participation.setLink("");
			participation.setStatus(ParticipationStatusV2.USED);
		} else if (availableByWindow && supportNomination != null && hasText(supportNomination.getToken())) {
			participation.setLink(defaultText(supportNomination.getSupportNominationLink()));
			participation.setStatus(ParticipationStatusV2.AVAILABLE);
		} else {
			participation.setLink("");
			participation.setStatus(ParticipationStatusV2.BLOCKED);
		}

		return participation;
	}

	private String resolveNominationCandidateEmail(Nomination nomination) {
		Candidate candidate = nomination != null ? nomination.getCandidate() : null;
		if (candidate != null && hasText(candidate.getMail())) {
			return candidate.getMail();
		}
		return nomination != null ? nomination.getNominationEmail() : "";
	}

	private String resolveNominationCandidateName(Nomination nomination) {
		Candidate candidate = nomination != null ? nomination.getCandidate() : null;
		if (candidate != null && hasText(candidate.getName())) {
			return candidate.getName();
		}
		return nomination != null ? nomination.getNominationName() : "";
	}

	private String resolveNominationParticipationEmail(Organization organization, Nomination nomination, boolean useNominationData) {
		String nominationEmail = useNominationData ? resolveNominationCandidateEmail(nomination) : "";
		if (hasText(nominationEmail)) {
			return nominationEmail;
		}
		return organization != null ? defaultText(organization.getMembershipContactEmail()) : "";
	}

	private String resolveNominationParticipationName(Organization organization, Nomination nomination, boolean useNominationData) {
		String nominationName = useNominationData ? resolveNominationCandidateName(nomination) : "";
		if (hasText(nominationName)) {
			return nominationName;
		}
		return organization != null ? defaultText(organization.getMembershipContactName()) : "";
	}

	private ParticipationV2 buildParticipationV2Base(String org, Election election) {
		ParticipationV2 participation = new ParticipationV2();
		participation.setOrgId(defaultText(org));
		participation.setElectionEndDate(election.getVotingPeriodEndDate());
		participation.setElectionStartDate(election.getVotingPeriodStartDate());
		participation.setCategory(election.getCategory().toString());
		participation.setElectionTitleEN(defaultText(election.getTitleEnglish()));
		participation.setElectionTitleSP(defaultText(election.getTitleSpanish()));
		participation.setElectionTitlePT(defaultText(election.getTitlePortuguese()));
		participation.setElectionLinkSP(defaultText(election.getLinkSpanish()));
		participation.setElectionLinkEN(defaultText(election.getLinkEnglish()));
		participation.setElectionLinkPT(defaultText(election.getLinkPortuguese()));
		participation.setEmail("");
		participation.setName("");
		participation.setCountry("");
		participation.setLink("");
		participation.setStatus(ParticipationStatusV2.BLOCKED);
		return participation;
	}

	private boolean isPublicLinkAvailableForCalendarWindow(Election election, ElectionCalendarKey calendarKey, boolean linkAvailable, Date referenceDate) {
		if (election == null || !linkAvailable || calendarKey == null || referenceDate == null) {
			return false;
		}

		ElectionCalendar calendar = ElectionsDaoFactory.createElectionCalendarDao(em).getElectionCalendarByKey(election.getElectionId(), calendarKey);
		if (calendar == null) {
			return false;
		}

		Date startDate = calendar.getStartDate();
		Date endDate = calendar.getEndDate();

		if (startDate == null || referenceDate.before(startDate)) {
			return false;
		}
		return endDate == null || !referenceDate.after(endDate);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String defaultText(String value) {
		return hasText(value) ? value : "";
	}

	/**
	 * Gets a list with the basic information of all the elections on the system
	 * orderer by date in descending order.
	 * 
	 * @return returns a collection of election light entity with the information.
	 */
	@Override
	public List<ElectionLight> getElectionsLightAllOrderStartDateDesc() {
		List<ElectionLight> elections = ElectionsDaoFactory.createElectionDao(em).getElectionsLightAllOrderStartDateDesc();
		VotingPeriodResolver.applyVotingWindowToLight(em, elections);
		VotingPeriodResolver.applyNominationWindowToLight(em, elections);
		return elections;
	}

	@Override
	public PublicElectionsSnapshot getPublicElectionsSnapshot() {
		try {
			PublicElectionsSnapshot cachedSnapshot = ElectionsCaches.getPublicElectionsSnapshot();
			if (cachedSnapshot != null) {
				refreshSnapshotStaleness(cachedSnapshot);
				return cachedSnapshot;
			}
			PublicElectionsSnapshot snapshot = buildPublicElectionsSnapshot();
			if (snapshot == null) {
				return null;
			}
			ElectionsCaches.putPublicElectionsSnapshot(snapshot);
			return snapshot;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Gets the authentication method used in web services. It is specified with the
	 * WS_AUTH_METHOD property from elections.properties
	 * 
	 * @return returns a string with the auth method.
	 */
	public String getWsAuthMethod() {
		return ElectionsProperties.get(Constants.WS_AUTH_METHOD);
	}

	@Override
	public String getWsAuthToken() {
		return ElectionsProperties.get(Constants.WS_AUTH_TOKEN);
	}

	/**
	 * Gets the authentication method used in web services. It is specified with the
	 * WS_LACNIC_AUTH_URL property from elections.properties
	 * 
	 * @return returns a string with the auth method.
	 */
	public String getWsLacnicAuthUrl() {
		return ElectionsProperties.get(Constants.WS_LACNIC_AUTH_URL);
	}

	/**
	 * Gets and parses the list of authorized ips from which the web services can be
	 * invoked, they are read from the WS_AUTHORIZED_IPS property.
	 * 
	 * @return returns a ip resource set entity with the information.
	 */
	@Override
	public IpResourceSet getWsAuthorizedIps() {
		try {
			return IpResourceSet.parse(ElectionsProperties.get(Constants.WS_AUTHORIZED_IPS));
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Gets the maximum page size for services. It is specified with the
	 * WS_MAX_PAGE_SIZE parameter
	 * 
	 * @return the max page size
	 */
	public Integer getWsMaxPageSize() {
		try {
			String value = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.WS_MAX_PAGE_SIZE);
			return Integer.parseInt(value);
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Activities in the system
	 * 
	 * @return List of Activities id and description
	 */
	@Override
	public List<TablesReportDataLongId> getActivitiesBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> activitiesData = new ArrayList<>();
			List<Object[]> activitiesDataList = ElectionsDaoFactory.createActivityDao(em).getActivitiesAllIdAndDescription(pageSize, offset);

			for (int i = 0; i < activitiesDataList.size(); i++) {
				activitiesData.add(new TablesReportDataLongId((Long) activitiesDataList.get(i)[0], activitiesDataList.get(i)[1].toString()));
			}

			return activitiesData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the Activity with the given id
	 * 
	 * @param activityId Identifier of the Activity
	 * 
	 * @return An ActivityReportTable instance containing the information
	 */
	@Override
	public ActivityTableReport getActivityTableReport(Long activityId) {
		try {
			Activity activity = ElectionsDaoFactory.createActivityDao(em).getActivity(activityId);
			if (activity != null) {
				return new ActivityTableReport(activity);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Auditors in the system
	 * 
	 * @return List of Auditors id and description
	 */
	@Override
	public List<TablesReportDataLongId> getAuditorsBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> auditorsData = new ArrayList<>();
			List<Object[]> auditorsDataList = ElectionsDaoFactory.createAuditorDao(em).getAuditorsAllIdAndDescription(pageSize, offset);

			for (int i = 0; i < auditorsDataList.size(); i++) {
				auditorsData.add(new TablesReportDataLongId((Long) auditorsDataList.get(i)[0], auditorsDataList.get(i)[1].toString()));
			}

			return auditorsData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the Auditor with the given id
	 * 
	 * @param auditorId Identifier of the Auditor
	 * 
	 * @return An AuditorReportTable instance containing the information
	 */
	@Override
	public AuditorTableReport getAuditorTableReport(Long auditorId) {
		try {
			Auditor auditor = ElectionsDaoFactory.createAuditorDao(em).getAuditor(auditorId);
			if (auditor != null) {
				return new AuditorTableReport(auditor);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Candidates in the system
	 * 
	 * @return List of Candidates id and description
	 */
	@Override
	public List<TablesReportDataLongId> getCandidatesBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> candidatesData = new ArrayList<>();
			List<Object[]> candidatesDataList = ElectionsDaoFactory.createCandidateDao(em).getCandidatesAllIdAndDescription(pageSize, offset);

			for (int i = 0; i < candidatesDataList.size(); i++) {
				candidatesData.add(new TablesReportDataLongId((Long) candidatesDataList.get(i)[0], candidatesDataList.get(i)[1].toString()));
			}

			return candidatesData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the Candidate with the given id
	 * 
	 * @param candidateId Identifier of the Candidate
	 * 
	 * @return A CandidateReportTable instance containing the information
	 */
	@Override
	public CandidateTableReport getCandidateTableReport(Long candidateId) {
		try {
			Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
			if (candidate != null) {
				return new CandidateTableReport(candidate);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Commissioners in the system
	 * 
	 * @return List of Commissioners id and description
	 */
	@Override
	public List<TablesReportDataLongId> getCommissionersBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> commissionersData = new ArrayList<>();
			List<Object[]> commissionersDataList = ElectionsDaoFactory.createCommissionerDao(em).getCommissionersAllIdAndDescription(pageSize, offset);

			for (int i = 0; i < commissionersDataList.size(); i++) {
				commissionersData.add(new TablesReportDataLongId((Long) commissionersDataList.get(i)[0], commissionersDataList.get(i)[1].toString()));
			}

			return commissionersData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the Commissioner with the given id
	 * 
	 * @param commissionerId Identifier of the Commissioner
	 * 
	 * @return A CommissionerReportTable instance containing the information
	 */
	@Override
	public CommissionerTableReport getCommissionerTableReport(Long commissionerId) {
		try {
			Commissioner commissioner = ElectionsDaoFactory.createCommissionerDao(em).getCommissioner(commissionerId);
			if (commissioner != null) {
				return new CommissionerTableReport(commissioner);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Customizations in the system
	 * 
	 * @return List of Customizations id and description
	 */
	@Override
	public List<TablesReportDataLongId> getCustomizationsBasicData() {
		try {
			List<TablesReportDataLongId> customizationData = new ArrayList<>();
			List<Object[]> customizationDataList = ElectionsDaoFactory.createCustomizationDao(em).getCustomizationsAllIdAndDescription();

			for (int i = 0; i < customizationDataList.size(); i++) {
				customizationData.add(new TablesReportDataLongId((Long) customizationDataList.get(i)[0], customizationDataList.get(i)[1].toString()));
			}

			return customizationData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the Customization with the given id
	 * 
	 * @param customizationId Identifier of the Customization
	 * 
	 * @return A CustomizationReportTable instance containing the information
	 */
	@Override
	public CustomizationTableReport getCustomizationTableReport(Long customizationId) {
		try {
			Customization customization = ElectionsDaoFactory.createCustomizationDao(em).getCustomizationById(customizationId);
			if (customization != null) {
				return new CustomizationTableReport(customization);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Elections in the system
	 * 
	 * @return List of Elections id and description
	 */
	@Override
	public List<TablesReportDataLongId> getElectionsBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> electionsData = new ArrayList<>();
			List<Object[]> electionsDataList = ElectionsDaoFactory.createElectionDao(em).getElectionsAllIdAndDescription(pageSize, offset);

			for (int i = 0; i < electionsDataList.size(); i++) {
				electionsData.add(new TablesReportDataLongId((Long) electionsDataList.get(i)[0], electionsDataList.get(i)[1].toString()));
			}

			return electionsData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the Election with the given id
	 * 
	 * @param electionId Identifier of the Election
	 * 
	 * @return An ElectionReportTable instance containing the information
	 */
	@Override
	public ElectionTableReport getElectionTableReport(Long electionId) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			if (election != null) {
				VotingPeriodResolver.applyVotingWindow(em, election);
				return new ElectionTableReport(election);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all ElectionEmailTemplates in the
	 * system
	 * 
	 * @return List of ElectionEmailTemplates id and description
	 */
	@Override
	public List<TablesReportDataLongId> getElectionEmailTemplatesBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> electionEmailsDataList = new ArrayList<>();
			List<Object[]> electionEmailsData = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionEmailTemplatesAllIdAndDescription(pageSize, offset);

			for (int i = 0; i < electionEmailsData.size(); i++) {
				electionEmailsDataList.add(new TablesReportDataLongId((Long) electionEmailsData.get(i)[0], electionEmailsData.get(i)[1].toString()));
			}

			return electionEmailsDataList;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the ElectionEmailTemplate with the given id
	 * 
	 * @param electionId Identifier of the ElectionEmailTemplate
	 * 
	 * @return An ElectionEmailTemplate instance containing the information
	 */
	@Override
	public ElectionEmailTemplateTableReport getElectionEmailTemplateTableReport(Long electionEmailTemplateId) {
		try {
			ElectionEmailTemplate electionEmailTemplate = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionEmailTemplate(electionEmailTemplateId);
			if (electionEmailTemplate != null) {
				return new ElectionEmailTemplateTableReport(electionEmailTemplate);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Emails in the system
	 * 
	 * @return List of Emails id and description
	 */
	@Override
	public List<TablesReportDataLongId> getEmailsBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> emailsData = new ArrayList<>();
			List<Object[]> emailsDataList = ElectionsDaoFactory.createEmailDao(em).getEmailsAllIdAndDescription(pageSize, offset);

			for (int i = 0; i < emailsDataList.size(); i++) {
				emailsData.add(new TablesReportDataLongId((Long) emailsDataList.get(i)[0], emailsDataList.get(i)[1].toString()));
			}

			return emailsData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the Email with the given id
	 * 
	 * @param emailId Identifier of the Email
	 * 
	 * @return An EmailReportTable instance containing the information
	 */
	@Override
	public EmailTableReport getEmailTableReport(Long emailId) {
		try {
			Email email = ElectionsDaoFactory.createEmailDao(em).getEmail(emailId);
			if (email != null) {
				return new EmailTableReport(email);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all EmailsHistory in the system
	 * 
	 * @return List of EmailsHistory id and description
	 */
	@Override
	public List<TablesReportDataLongId> getEmailsHistoryBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> emailsHistoryData = new ArrayList<>();
			List<Object[]> emailsHistoryDataList = ElectionsDaoFactory.createEmailDao(em).getEmailsHistoryAllIdAndDescription(pageSize, offset);

			for (int i = 0; i < emailsHistoryDataList.size(); i++) {
				emailsHistoryData.add(new TablesReportDataLongId((Long) emailsHistoryDataList.get(i)[0], emailsHistoryDataList.get(i)[1].toString()));
			}

			return emailsHistoryData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the EmailHistory with the given id
	 * 
	 * @param emailHistoryId Identifier of the EmailHistory
	 * 
	 * @return An EmailReportTable instance containing the information
	 */
	@Override
	public EmailTableReport getEmailHistoryTableReport(Long emailHistoryId) {
		try {
			EmailHistory emailHistory = ElectionsDaoFactory.createEmailDao(em).getEmailHistory(emailHistoryId);
			if (emailHistory != null) {
				return new EmailTableReport(emailHistory);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all IpAccesses in the system
	 * 
	 * @return List of IpAccesses id and description
	 */
	@Override
	public List<TablesReportDataLongId> getIpAccessesBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> ipAccessesData = new ArrayList<>();
			List<Object[]> ipAccessesDataList = ElectionsDaoFactory.createIpAccessDao(em).getIpAccessesAllIdAndDescription(pageSize, offset);

			for (int i = 0; i < ipAccessesDataList.size(); i++) {
				ipAccessesData.add(new TablesReportDataLongId((Long) ipAccessesDataList.get(i)[0], ipAccessesDataList.get(i)[1].toString()));
			}

			return ipAccessesData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the IpAccess with the given id
	 * 
	 * @param ipAccessId Identifier of the IpAccess
	 * 
	 * @return An IpAccess instance containing the information
	 */
	@Override
	public IpAccess getIpAccessTableReport(Long ipAccessId) {
		try {
			IpAccess ipAccess = ElectionsDaoFactory.createIpAccessDao(em).getIpAccess(ipAccessId);
			return ipAccess;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all JointElections in the system
	 * 
	 * @return List of JointElections id and description
	 */
	@Override
	public List<TablesReportDataLongId> getJointElectionsBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> jointElectionsData = new ArrayList<>();
			List<Long> jointElectionsDataList = ElectionsDaoFactory.createJointElectionDao(em).getJointElectionsIds(pageSize, offset);

			for (int i = 0; i < jointElectionsDataList.size(); i++) {
				jointElectionsData.add(new TablesReportDataLongId((Long) jointElectionsDataList.get(i), ""));
			}

			return jointElectionsData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the JointElection with the given id
	 * 
	 * @param jointElectionId Identifier of the JointElection
	 * 
	 * @return A JointElection instance containing the information
	 */
	@Override
	public JointElection getJointElectionTableReport(Long jointElectionId) {
		try {
			JointElection jointelection = ElectionsDaoFactory.createJointElectionDao(em).getJointElection(jointElectionId);
			return jointelection;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Parameters in the system
	 * 
	 * @return List of Parameters key and value
	 */
	@Override
	public List<TableReportDataStringId> getParametersBasicData() {
		try {
			List<TableReportDataStringId> parameterData = new ArrayList<>();
			List<Parameter> parameterDataList = ElectionsDaoFactory.createParameterDao(em).getParametersAll();

			for (int i = 0; i < parameterDataList.size(); i++) {
				String paramKey = parameterDataList.get(i).getKey();
				if (isSensitiveParameter(paramKey)) {
					parameterData.add(new TableReportDataStringId(paramKey, "**********"));
				} else {
					parameterData.add(new TableReportDataStringId(paramKey, parameterDataList.get(i).getValue()));
				}
			}

			return parameterData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the Parameter with the given key
	 * 
	 * @param key Identifier of the Parameter
	 * 
	 * @return A Parameter instance containing the information
	 */
	@Override
	public Parameter getParameterReport(String key) {
		try {
			Parameter parameter = ElectionsDaoFactory.createParameterDao(em).getParameter(key);
			if (parameter != null) {
				if (isSensitiveParameter(parameter.getKey())) {
					parameter.setValue("**********");
				}
				return parameter;
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	private boolean isSensitiveParameter(String key) {
		return Constants.EMAIL_HOST.equals(key)
				|| Constants.EMAIL_USER.equals(key)
				|| Constants.EMAIL_PASSWORD.equals(key)
				|| Constants.PORTAL_APIKEY.equals(key)
				|| Constants.WS_AUTH_TOKEN.equals(key)
				|| Constants.WS_AUTHORIZED_IPS.equals(key)
				|| Constants.CAMPUS_TOKEN.equals(key)
				|| Constants.MILACNIC_SYNC_API_TOKEN.equals(key)
				|| Constants.OPENAI_API_KEY.equals(key)
				|| Constants.SkGoogleApiReCaptcha.equals(key);
	}

	/**
	 * Returns a list with id and description for all UserAdmins in the system
	 * 
	 * @return List of UserAdmins id and name
	 */
	@Override
	public List<TableReportDataStringId> getUserAdminBasicData(int pageSize, int offset) {
		try {
			List<TableReportDataStringId> userAdminsData = new ArrayList<>();
			List<Object[]> userAdminsDataList = ElectionsDaoFactory.createUserAdminDao(em).getUserAdminsAllIdAndName(pageSize, offset);

			for (int i = 0; i < userAdminsDataList.size(); i++) {
				userAdminsData.add(new TableReportDataStringId(userAdminsDataList.get(i)[0].toString(), userAdminsDataList.get(i)[1].toString()));
			}

			return userAdminsData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the user admin with the given id
	 * 
	 * @param userAdminId Identifier of the admin
	 * 
	 * @return An user admin instance containing the information
	 */
	@Override
	public UserAdminTableReport getUserAdminReportTable(String userAdminId) {
		try {
			UserAdmin userAdmin = ElectionsDaoFactory.createUserAdminDao(em).getUserAdmin(userAdminId);
			if (userAdmin != null) {
				return new UserAdminTableReport(userAdmin);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all UserVoters in the system
	 * 
	 * @return List of UserVoters id and name
	 */
	@Override
	public List<TablesReportDataLongId> getUserVotersBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> userVotersData = new ArrayList<>();
			List<Object[]> userVotersDataList = ElectionsDaoFactory.createUserVoterDao(em).getUserVotersAllIdAndName(pageSize, offset);

			for (int i = 0; i < userVotersDataList.size(); i++) {
				userVotersData.add(new TablesReportDataLongId((Long) userVotersDataList.get(i)[0], userVotersDataList.get(i)[1].toString()));
			}

			return userVotersData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the UserVoter with the given id
	 * 
	 * @param userVoterId Identifier of the UserVoter
	 * 
	 * @return An UserVoter instance containing the information
	 */
	@Override
	public UserVoterTableReport getUserVoterReportTable(Long userVoterId) {
		try {
			UserVoter userVoter = ElectionsDaoFactory.createUserVoterDao(em).getUserVoter(userVoterId);
			if (userVoter != null) {
				return new UserVoterTableReport(userVoter);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Votes in the system
	 * 
	 * @return List of Votes id and date
	 */
	@Override
	public List<TablesReportDataLongId> getVotesBasicData(int pageSize, int offset) {
		try {
			List<TablesReportDataLongId> votesData = new ArrayList<>();
			List<Object[]> votesDataList = ElectionsDaoFactory.createVoteDao(em).getVotesAllIdAndDate(pageSize, offset);

			for (int i = 0; i < votesDataList.size(); i++) {
				votesData.add(new TablesReportDataLongId((Long) votesDataList.get(i)[0], votesDataList.get(i)[1].toString()));
			}

			return votesData;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns information about the Vote with the given id
	 * 
	 * @param voteId Identifier of the Vote
	 * 
	 * @return A VoteReportTable instance containing the information
	 */
	@Override
	public VoteTableReport getVoteTableReport(Long voteId) {
		try {
			Vote vote = ElectionsDaoFactory.createVoteDao(em).getVote(voteId);
			if (vote != null) {
				return new VoteTableReport(vote);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns detailed information about the Elections in the system
	 * 
	 * @return A List of ElectionDetailReport containing the information
	 */
	@Override
	public List<ElectionDetailReport> getElectionsDetailReport(int pageSize, int offset) {
		try {
			List<Election> elections = ElectionsDaoFactory.createElectionDao(em).getElections(pageSize, offset);
			VotingPeriodResolver.applyVotingWindow(em, elections);
			List<ElectionDetailReport> electionsDetailList = new ArrayList<ElectionDetailReport>();
			for (Election election : elections) {
				electionsDetailList.add(new ElectionDetailReport(election));
			}

			return electionsDetailList;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns detailed information about the Election with the given id
	 * 
	 * @param electionId Identifier of the election
	 * 
	 * @return An ElectionDetailReport instance containing the information
	 */
	@Override
	public ElectionDetailReport getElectionDetailReport(Long electionId) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			if (election != null) {
				VotingPeriodResolver.applyVotingWindow(em, election);
				return new ElectionDetailReport(election);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns detailed information about the participations of the given email in
	 * different elections
	 * 
	 * @param email The email to search for
	 * 
	 * @return A list of ElectionParticipationDetailReport instances containing the
	 *         information
	 */
	@Override
	public List<ElectionParticipationDetailReport> getElectionsParticipationsByEmail(String email, int pageSize, int offset) {
		try {
			List<Auditor> auditors = ElectionsDaoFactory.createAuditorDao(em).getAuditorsByEmail(email, pageSize, offset);
			List<UserVoter> userVoters = ElectionsDaoFactory.createUserVoterDao(em).getUserVotersByEmail(email, pageSize, offset);
			List<Candidate> candidates = ElectionsDaoFactory.createCandidateDao(em).getCandidatesByEmail(email, pageSize, offset);
			applyVotingWindowForRelatedElections(auditors, userVoters, candidates);

			List<ElectionParticipationDetailReport> participations = new ArrayList<>();
			for (Auditor auditor : auditors) {
				participations.add(new ElectionParticipationDetailReport(auditor));
			}
			for (UserVoter userVoter : userVoters) {
				participations.add(new ElectionParticipationDetailReport(userVoter));
			}
			for (Candidate candidate : candidates) {
				participations.add(new ElectionParticipationDetailReport(candidate));
			}

			return participations;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Returns detailed information about the participations of the given
	 * organization in different elections
	 * 
	 * @param orgID The organization id to search for
	 * 
	 * @return A list of OrganizationVoterDetailReport instances containing the
	 *         information
	 */
	@Override
	public List<OrganizationVoterDetailReport> getElectionsParticipationsByOrgId(String orgID, int pageSize, int offset) {
		try {
			List<UserVoter> userVoters = ElectionsDaoFactory.createUserVoterDao(em).getUserVotersByOrganization(orgID, pageSize, offset);
			applyVotingWindowForRelatedElections(userVoters);
			List<OrganizationVoterDetailReport> orgVoterDetailList = new ArrayList<>();
			for (UserVoter userVoter : userVoters) {
				orgVoterDetailList.add(new OrganizationVoterDetailReport(userVoter));
			}

			return orgVoterDetailList;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	private void applyVotingWindowForRelatedElections(List<Auditor> auditors, List<UserVoter> userVoters, List<Candidate> candidates) {
		Map<Long, Election> electionsById = new HashMap<>();
		for (Auditor auditor : auditors) {
			addElectionToHydrationMap(electionsById, auditor != null ? auditor.getElection() : null);
		}
		for (UserVoter userVoter : userVoters) {
			addElectionToHydrationMap(electionsById, userVoter != null ? userVoter.getElection() : null);
		}
		for (Candidate candidate : candidates) {
			addElectionToHydrationMap(electionsById, candidate != null ? candidate.getElection() : null);
		}
		VotingPeriodResolver.applyVotingWindow(em, electionsById.values());
	}

	private void applyVotingWindowForRelatedElections(List<UserVoter> userVoters) {
		Map<Long, Election> electionsById = new HashMap<>();
		for (UserVoter userVoter : userVoters) {
			addElectionToHydrationMap(electionsById, userVoter != null ? userVoter.getElection() : null);
		}
		VotingPeriodResolver.applyVotingWindow(em, electionsById.values());
	}

	private void addElectionToHydrationMap(Map<Long, Election> electionsById, Election election) {
		if (election == null || election.getElectionId() <= 0) {
			return;
		}
		electionsById.putIfAbsent(election.getElectionId(), election);
	}

	@Override
	public PublicElectionCoreSnapshot getPublicElectionCoreSnapshot(Long electionId) {
		try {
			if (electionId == null || electionId.longValue() <= 0L) {
				return null;
			}
			PublicElectionCoreSnapshot cachedSnapshot = ElectionsCaches.getPublicElectionCoreSnapshot(electionId);
			if (cachedSnapshot != null) {
				refreshSnapshotStaleness(cachedSnapshot);
				return cachedSnapshot;
			}
			PublicElectionSnapshotBundle bundle = buildPublicElectionSnapshots(electionId.longValue());
			if (bundle == null || bundle.getCoreSnapshot() == null) {
				return null;
			}
			cachePublicElectionSnapshots(electionId, bundle);
			return bundle.getCoreSnapshot();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public PublicElectionRollSnapshot getPublicElectionRollSnapshot(Long electionId) {
		try {
			if (electionId == null || electionId.longValue() <= 0L) {
				return null;
			}
			PublicElectionRollSnapshot cachedSnapshot = ElectionsCaches.getPublicElectionRollSnapshot(electionId);
			if (cachedSnapshot != null) {
				refreshSnapshotStaleness(cachedSnapshot);
				return cachedSnapshot;
			}
			PublicElectionSnapshotBundle bundle = buildPublicElectionSnapshots(electionId.longValue());
			if (bundle == null || bundle.getRollSnapshot() == null) {
				return null;
			}
			cachePublicElectionSnapshots(electionId, bundle);
			return bundle.getRollSnapshot();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public PublicElectionPhotoSnapshot getPublicElectionPhotoSnapshot(Long electionId) {
		try {
			if (electionId == null || electionId.longValue() <= 0L) {
				return null;
			}
			PublicElectionPhotoSnapshot cachedSnapshot = ElectionsCaches.getPublicElectionPhotoSnapshot(electionId);
			if (cachedSnapshot != null) {
				refreshSnapshotStaleness(cachedSnapshot);
				return cachedSnapshot;
			}
			PublicElectionSnapshotBundle bundle = buildPublicElectionSnapshots(electionId.longValue());
			if (bundle == null || bundle.getPhotoSnapshot() == null) {
				return null;
			}
			cachePublicElectionSnapshots(electionId, bundle);
			return bundle.getPhotoSnapshot();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public PublicElectionOfficialResultSnapshot getPublicElectionOfficialResultSnapshot(Long electionId) {
		try {
			if (electionId == null || electionId.longValue() <= 0L) {
				return null;
			}
			PublicElectionOfficialResultSnapshot cachedSnapshot = ElectionsCaches.getPublicElectionOfficialResultSnapshot(electionId);
			if (cachedSnapshot != null) {
				refreshSnapshotStaleness(cachedSnapshot);
				return cachedSnapshot;
			}
			PublicElectionSnapshotBundle bundle = buildPublicElectionSnapshots(electionId.longValue());
			if (bundle == null || bundle.getOfficialResultSnapshot() == null) {
				return null;
			}
			cachePublicElectionSnapshots(electionId, bundle);
			return bundle.getOfficialResultSnapshot();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	@TransactionTimeout(35000)
	public void refreshOpenPublicElectionSnapshotCache() {
		try {
			List<Election> elections = ElectionsDaoFactory.createElectionDao(em).getElectionsAllOrderStartDateDesc();
			for (Election election : elections) {
				if (election == null || election.isClosed()) {
					continue;
				}
				PublicElectionSnapshotBundle bundle = buildPublicElectionSnapshots(election.getElectionId());
				if (bundle == null) {
					continue;
				}
				cachePublicElectionSnapshots(election.getElectionId(), bundle);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

	@Override
	@TransactionTimeout(35000)
	public void refreshPublicElectionsSnapshotCache() {
		try {
			PublicElectionsSnapshot snapshot = buildPublicElectionsSnapshot();
			if (snapshot != null) {
				ElectionsCaches.putPublicElectionsSnapshot(snapshot);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

	private PublicElectionSnapshotBundle buildPublicElectionSnapshots(long electionId) {
		return new PublicElectionSnapshotBuilder(em).build(electionId);
	}

	private PublicElectionsSnapshot buildPublicElectionsSnapshot() {
		List<ElectionLight> elections = getElectionsLightAllOrderStartDateDesc();
		if (elections == null) {
			return null;
		}
		PublicElectionsSnapshot snapshot = new PublicElectionsSnapshot();
		snapshot.setMetadata(buildPublicElectionsSnapshotMetadata());
		for (ElectionLight election : elections) {
			if (election == null || election.getCategory() == ElectionCategory.TEST) {
				continue;
			}
			snapshot.getElections().add(election);
		}
		return snapshot;
	}

	private PublicElectionsSnapshotMetadata buildPublicElectionsSnapshotMetadata() {
		Date generatedAt = new Date();
		PublicElectionsSnapshotMetadata metadata = new PublicElectionsSnapshotMetadata();
		metadata.setLastUpdatedUtc(generatedAt);
		metadata.setNextRefreshUtc(new Date(generatedAt.getTime() + (5L * 60L * 1000L)));
		metadata.setStale(Boolean.FALSE);
		metadata.setRefreshStatus("READY");
		metadata.setRefreshMessage(null);
		return metadata;
	}

	private void cachePublicElectionSnapshots(Long electionId, PublicElectionSnapshotBundle bundle) {
		if (electionId == null || bundle == null) {
			return;
		}
		if (bundle.getCoreSnapshot() != null) {
			ElectionsCaches.putPublicElectionCoreSnapshot(electionId, bundle.getCoreSnapshot());
		}
		if (bundle.getRollSnapshot() != null) {
			ElectionsCaches.putPublicElectionRollSnapshot(electionId, bundle.getRollSnapshot());
		}
		if (bundle.getPhotoSnapshot() != null) {
			ElectionsCaches.putPublicElectionPhotoSnapshot(electionId, bundle.getPhotoSnapshot());
		}
		if (bundle.getOfficialResultSnapshot() != null) {
			ElectionsCaches.putPublicElectionOfficialResultSnapshot(electionId, bundle.getOfficialResultSnapshot());
		}
	}

	private void refreshSnapshotStaleness(PublicElectionCoreSnapshot snapshot) {
		if (snapshot == null || snapshot.getMetadata() == null) {
			return;
		}
		boolean stale = snapshot.getMetadata().getNextRefreshUtc() != null && snapshot.getMetadata().getNextRefreshUtc().before(new Date());
		snapshot.getMetadata().setStale(Boolean.valueOf(stale));
	}

	private void refreshSnapshotStaleness(PublicElectionRollSnapshot snapshot) {
		if (snapshot == null || snapshot.getMetadata() == null) {
			return;
		}
		boolean stale = snapshot.getMetadata().getNextRefreshUtc() != null && snapshot.getMetadata().getNextRefreshUtc().before(new Date());
		snapshot.getMetadata().setStale(Boolean.valueOf(stale));
	}

	private void refreshSnapshotStaleness(PublicElectionPhotoSnapshot snapshot) {
		if (snapshot == null || snapshot.getMetadata() == null) {
			return;
		}
		boolean stale = snapshot.getMetadata().getNextRefreshUtc() != null && snapshot.getMetadata().getNextRefreshUtc().before(new Date());
		snapshot.getMetadata().setStale(Boolean.valueOf(stale));
	}

	private void refreshSnapshotStaleness(PublicElectionOfficialResultSnapshot snapshot) {
		if (snapshot == null || snapshot.getMetadata() == null) {
			return;
		}
		boolean stale = snapshot.getMetadata().getNextRefreshUtc() != null && snapshot.getMetadata().getNextRefreshUtc().before(new Date());
		snapshot.getMetadata().setStale(Boolean.valueOf(stale));
	}

	private void refreshSnapshotStaleness(PublicElectionsSnapshot snapshot) {
		if (snapshot == null || snapshot.getMetadata() == null) {
			return;
		}
		boolean stale = snapshot.getMetadata().getNextRefreshUtc() != null && snapshot.getMetadata().getNextRefreshUtc().before(new Date());
		snapshot.getMetadata().setStale(Boolean.valueOf(stale));
	}

}
