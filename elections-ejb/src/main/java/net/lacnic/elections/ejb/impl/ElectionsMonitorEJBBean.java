package net.lacnic.elections.ejb.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.dao.ReportDao;
import net.lacnic.elections.data.ElectionReport;
import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participation;
import net.lacnic.elections.data.TablesReportData;
import net.lacnic.elections.data.TableReportStringData;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Commissioner;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionLight;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.EmailHistory;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.domain.services.ActivityReportTable;
import net.lacnic.elections.domain.services.AuditorReportTable;
import net.lacnic.elections.domain.services.CandidateReportTable;
import net.lacnic.elections.domain.services.CommissionerReportTable;
import net.lacnic.elections.domain.services.ElectionReportTable;
import net.lacnic.elections.domain.services.EmailReportTable;
import net.lacnic.elections.domain.services.UserAdminReportTable;
import net.lacnic.elections.domain.services.UserVoterReportTable;
import net.lacnic.elections.domain.services.VoteReportTable;
import net.lacnic.elections.ejb.ElectionsMonitorEJB;
import net.lacnic.elections.ejb.commons.impl.AutomaticProcesses;
import net.lacnic.elections.utils.Constants;
import net.ripe.ipresource.IpResourceSet;


@Stateless
@Remote(ElectionsMonitorEJB.class)
public class ElectionsMonitorEJBBean implements ElectionsMonitorEJB {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	private static HealthCheck healthCheck;


	public ElectionsMonitorEJBBean() { }

	/**
	 * Get the health check data.
	 * 
	 * @return returns a health check entity containing the system's health information.
	 */
	@Override
	public HealthCheck getHealthCheckData() {
		if (healthCheck == null)
			healthCheck = updateHealthCheckData();
		return healthCheck;
	}

	/**
	 * Creates a updated health check report.
	 * 
	 * @return returns a health check entity containing the health check information from the system. 
	 */
	@Override
	public HealthCheck updateHealthCheckData() {
		ReportDao reportDao = ElectionsDaoFactory.createReportDao(em);

		int sendAttempts = AutomaticProcesses.getAttempts();
		long failedAccessIps = reportDao.getFailedIpAccesesAmount();
		long failedAccessSum = reportDao.getFailedIpAccesesSum();
		long mailsTotal = reportDao.getEmailsAmount();
		long mailsPending = reportDao.getPendingSendEmailsAmount();
		long mailsSent = reportDao.getSentEmailsAmount();

		List<ElectionReport> elections = electionReport();

		return new HealthCheck(sendAttempts, failedAccessIps, failedAccessSum, mailsTotal, mailsPending, mailsSent, elections);
	}

	/**
	 * Gets a list of reports from all the elections on the system
	 * 
	 * @return returns a collection of election report entity containing the information
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
	 * @param org
	 * 			String containing the organization looked for.
	 * 
	 * @return returns a collection of participation entity with the information.
	 */
	@Override
	public List<Participation> getOrganizationParticipations(String org) {
		List<Participation> participations = new ArrayList<>();
		List<Election> elections = ElectionsDaoFactory.createElectionDao(em).getElectionsAllOrderStartDateDesc();
		for (Election election : elections) {
			UserVoter userVoter = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVotersByOrganization(org, election.getElectionId());
			Participation participation = new Participation();
			participation.setCategory(election.getCategory().toString());
			if (userVoter != null) {
				participation.setEmail(userVoter.getMail());
				participation.setName(userVoter.getName());
				participation.setCountry(userVoter.getCountry());
				participation.setVoted(userVoter.isVoted());
				if (election.isEnabledToVote()) {
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
			};			
			participation.setElectionEndDate(election.getEndDate());
			participation.setElectionStartDate(election.getStartDate());
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

	/**
	 * Gets a list with the basic information of all the elections on the system orderer by date in descending order.
	 * 
	 * @return returns a collection of election light entity with the information.
	 */
	@Override
	public List<ElectionLight> getElectionsLightAllOrderStartDateDesc() {
		return ElectionsDaoFactory.createElectionDao(em).getElectionsLightAllOrderStartDateDesc();
	}

	/**
	 * Gets the authentication token to validate services invocation. It is withdrawn from the parameter WS_AUTH_TOKEN
	 * 
	 * @return returns a string with the token.
	 */
	@Override
	public String getWsAuthToken() {
		try {
			return ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_AUTH_TOKEN).getValue();
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Gets the authentication method used in web services. It is specified with the WS_AUTH_METHOD parameter
	 * 
	 * @return returns a string with the auth method.
	 */
	public String getWsAuthMethod() {
		try {
			return ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_AUTH_METHOD).getValue();
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Gets the authentication method used in web services. It is specified with the WS_AUTH_METHOD parameter
	 * 
	 * @return returns a string with the auth method.
	 */
	public String getWsLacnicAuthUrl() {
		try {
			return ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_LACNIC_AUTH_URL).getValue();
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Gets and parses the list of authorized ips from which the web services can be invoked, the are withdrawn from the parameter WS_AUTHORIZED_IPS.
	 *  
	 * @return returns a ip resource set entity with the information.
	 */
	@Override
	public IpResourceSet getWsAuthorizedIps() {
		try {
			return IpResourceSet.parse(ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.WS_AUTHORIZED_IPS).getValue());
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}


	/**
	 * Returns a list with id and description for all Activities in the system
	 * 
	 * @return List of Activities id and description
	 */
	@Override
	public List<TablesReportData> getActivitiesBasicData() {
		try {
			List<TablesReportData> activitiesData = new ArrayList<>();
			List<Object[]> activitiesDataList = ElectionsDaoFactory.createActivityDao(em).getActivitiesAllIdAndDescription();

			for(int i = 0; i < activitiesDataList.size(); i++) {
				activitiesData.add(new TablesReportData((long)activitiesDataList.get(i)[0], activitiesDataList.get(i)[1].toString()));
			}

			return activitiesData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns information about the Activity with the given id
	 * 
	 * @param activityId
	 * 			Identifier of the Activity
	 * 
	 * @return An ActivityReportTable instance containing the information
	 */
	@Override
	public ActivityReportTable getActivityTableReport(Long activityId) {
		try {
			Activity activity = ElectionsDaoFactory.createActivityDao(em).getActivity(activityId);
			if(activity != null) {
				return new ActivityReportTable(activity);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Auditors in the system
	 * 
	 * @return List of Auditors id and description
	 */
	@Override
	public List<TablesReportData> getAuditorsBasicData() {
		try {
			List<TablesReportData> auditorsData = new ArrayList<>();
			List<Object[]> auditorsDataList = ElectionsDaoFactory.createAuditorDao(em).getAuditorsAllIdAndDescription();

			for(int i = 0; i < auditorsDataList.size(); i++) {
				auditorsData.add(new TablesReportData((long)auditorsDataList.get(i)[0], auditorsDataList.get(i)[1].toString()));
			}

			return auditorsData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns information about the Auditor with the given id
	 * 
	 * @param auditorId
	 * 			Identifier of the Auditor
	 * 
	 * @return An AuditorReportTable instance containing the information
	 */
	@Override
	public AuditorReportTable getAuditorTableReport(Long auditorId) {
		try {
			Auditor auditor = ElectionsDaoFactory.createAuditorDao(em).getAuditor(auditorId);
			if(auditor != null) {
				return new AuditorReportTable(auditor);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Candidates in the system
	 * 
	 * @return List of Candidates id and description
	 */
	@Override
	public List<TablesReportData> getCandidatesBasicData() {
		try {
			List<TablesReportData> candidatesData = new ArrayList<>();
			List<Object[]> candidatesDataList = ElectionsDaoFactory.createCandidateDao(em).getCandidatesAllIdAndDescription();

			for(int i = 0; i < candidatesDataList.size(); i++) {
				candidatesData.add(new TablesReportData((long)candidatesDataList.get(i)[0], candidatesDataList.get(i)[1].toString()));
			}

			return candidatesData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns information about the Candidate with the given id
	 * 
	 * @param candidateId
	 * 			Identifier of the Candidate
	 * 
	 * @return A CandidateReportTable instance containing the information
	 */
	@Override
	public CandidateReportTable getCandidateTableReport(Long candidateId) {
		try {
			Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
			if(candidate != null) {
				return new CandidateReportTable(candidate);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Commissioners in the system
	 * 
	 * @return List of Commissioners id and description
	 */
	@Override
	public List<TablesReportData> getCommissionersBasicData() {
		try {
			List<TablesReportData> commissionersData = new ArrayList<>();
			List<Object[]> commissionersDataList = ElectionsDaoFactory.createCommissionerDao(em).getCommissionersAllIdAndDescription();

			for(int i = 0; i < commissionersDataList.size(); i++) {
				commissionersData.add(new TablesReportData((long)commissionersDataList.get(i)[0], commissionersDataList.get(i)[1].toString()));
			}

			return commissionersData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns information about the Commissioner with the given id
	 * 
	 * @param commissionerId
	 * 			Identifier of the Commissioner
	 * 
	 * @return A CommissionerReportTable instance containing the information
	 */
	@Override
	public CommissionerReportTable getCommissionerTableReport(Long commissionerId) {
		try {
			Commissioner commissioner = ElectionsDaoFactory.createCommissionerDao(em).getCommissioner(commissionerId);
			if(commissioner != null) {
				return new CommissionerReportTable(commissioner);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Elections in the system
	 * 
	 * @return List of Elections id and description
	 */
	@Override
	public List<TablesReportData> getElectionsBasicData() {
		try {
			List<TablesReportData> electionsData = new ArrayList<>();
			List<Object[]> electionsDataList = ElectionsDaoFactory.createElectionDao(em).getElectionsAllIdAndTitle();

			for(int i = 0; i < electionsDataList.size(); i++) {
				electionsData.add(new TablesReportData((long)electionsDataList.get(i)[0], electionsDataList.get(i)[1].toString()));
			}

			return electionsData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns information about the Election with the given id
	 * 
	 * @param electionId
	 * 			Identifier of the Election
	 * 
	 * @return An ElectionReportTable instance containing the information
	 */
	@Override
	public ElectionReportTable getElectionTableReport(Long electionId) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			if(election != null) {
				return new ElectionReportTable(election);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all Emails in the system
	 * 
	 * @return List of Emails id and description
	 */
	@Override
	public List<TablesReportData> getEmailsBasicData() {
		try {
			List<TablesReportData> emailsData = new ArrayList<>();
			List<Object[]> emailsDataList = ElectionsDaoFactory.createEmailDao(em).getEmailsAllIdAndDescription();

			for(int i = 0; i < emailsDataList.size(); i++) {
				emailsData.add(new TablesReportData((long)emailsDataList.get(i)[0], emailsDataList.get(i)[1].toString()));
			}

			return emailsData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns information about the Email with the given id
	 * 
	 * @param emailId
	 * 			Identifier of the Email
	 * 
	 * @return An EmailReportTable instance containing the information
	 */
	@Override
	public EmailReportTable getEmailTableReport(Long emailId) {
		try {
			Email email = ElectionsDaoFactory.createEmailDao(em).getEmail(emailId);
			if(email != null) {
				return new EmailReportTable(email);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all EmailsHistory in the system
	 * 
	 * @return List of EmailsHistory id and description
	 */
	@Override
	public List<TablesReportData> getEmailsHistoryBasicData() {
		try {
			List<TablesReportData> emailsHistoryData = new ArrayList<>();
			List<Object[]> emailsHistoryDataList = ElectionsDaoFactory.createEmailDao(em).getEmailsHistoryAllIdAndDescription();

			for(int i = 0; i < emailsHistoryDataList.size(); i++) {
				emailsHistoryData.add(new TablesReportData((long)emailsHistoryDataList.get(i)[0], emailsHistoryDataList.get(i)[1].toString()));
			}

			return emailsHistoryData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns information about the EmailHistory with the given id
	 * 
	 * @param emailHistoryId
	 * 			Identifier of the EmailHistory
	 * 
	 * @return An EmailReportTable instance containing the information
	 */
	@Override
	public EmailReportTable getEmailHistoryTableReport(Long emailHistoryId) {
		try {
			EmailHistory emailHistory = ElectionsDaoFactory.createEmailDao(em).getEmailHistory(emailHistoryId);
			if(emailHistory != null) {
				return new EmailReportTable(emailHistory);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns a list with id and description for all IpAccesses in the system
	 * 
	 * @return List of IpAccesses id and description
	 */
	@Override
	public List<TablesReportData> getIpAccessesBasicData() {
		try {
			List<TablesReportData> ipAccessesData = new ArrayList<>();
			List<Object[]> ipAccessesDataList = ElectionsDaoFactory.createIpAccessDao(em).getIpAccessesAllIdAndDescription();

			for(int i = 0; i < ipAccessesDataList.size(); i++) {
				ipAccessesData.add(new TablesReportData((long)ipAccessesDataList.get(i)[0], ipAccessesDataList.get(i)[1].toString()));
			}

			return ipAccessesData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns information about the IpAccess with the given id
	 * 
	 * @param ipAccessId
	 * 			Identifier of the IpAccess
	 * 
	 * @return An IpAccess instance containing the information
	 */
	@Override
	public IpAccess getIpAccessTableReport(Long ipAccessId) {
		try {
			IpAccess ipAccess = ElectionsDaoFactory.createIpAccessDao(em).getIpAccess(ipAccessId);
			return ipAccess;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	/**
	 * Returns a list with id and description for all votes in the system
	 * 
	 * @return List of votes id and dadte
	 */
	@Override
	public List<TablesReportData> getVotesBasicData() {
		try {
			List<TablesReportData> votesData = new ArrayList<>();
			List<Object[]> votesDataList = ElectionsDaoFactory.createVoteDao(em).getVotesAllIdAndDate();

			for(int i = 0; i < votesDataList.size(); i++) {
				votesData.add(new TablesReportData((long)votesDataList.get(i)[0], votesDataList.get(i)[1].toString()));
			}

			return votesData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns information about the Vote with the given id
	 * 
	 * @param voteId
	 * 			Identifier of the vote
	 * 
	 * @return An vote instance containing the information
	 */
	@Override
	public VoteReportTable getVoteTableReport(Long voteId) {
		try {
			Vote vote = ElectionsDaoFactory.createVoteDao(em).getVote(voteId);
			if(vote != null) {
				return new VoteReportTable(vote);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	/**
	 * Returns a list with id and description for all user voters in the system
	 * 
	 * @return List of voters id and name
	 */
	@Override
	public List<TablesReportData> getUserVoterBasicData() {
		try {
			List<TablesReportData> userVotesData = new ArrayList<>();
			List<Object[]> userVotersDataList = ElectionsDaoFactory.createUserVoterDao(em).getUserVotersAllIdAndName();

			for(int i = 0; i < userVotersDataList.size(); i++) {
				userVotesData.add(new TablesReportData((long)userVotersDataList.get(i)[0], userVotersDataList.get(i)[1].toString()));
			}

			return userVotesData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns information about the voter with the given id
	 * 
	 * @param userVoteId
	 * 			Identifier of the voter
	 * 
	 * @return An user voter instance containing the information
	 */
	@Override
	public UserVoterReportTable getUserVoterReportTable(Long userVoterId) {
		try {
			UserVoter userVoter = ElectionsDaoFactory.createUserVoterDao(em).getUserVoter(userVoterId);
			if(userVoter != null) {
				return new UserVoterReportTable(userVoter);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	/**
	 * Returns a list with id and description for all user admins in the system
	 * 
	 * @return List of admins id and name
	 */
	@Override
	public List<TableReportStringData> getUserAdminBasicData() {
		try {
			List<TableReportStringData> userAdminsData = new ArrayList<>();
			List<Object[]> userAdminssDataList = ElectionsDaoFactory.createUserAdminDao(em).getUserAdminsAllIdAndName();

			for(int i = 0; i < userAdminssDataList.size(); i++) {
				userAdminsData.add(new TableReportStringData(userAdminssDataList.get(i)[0].toString(), userAdminssDataList.get(i)[1].toString()));
			}

			return userAdminsData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	/**
	 * Returns information about the user admin with the given id
	 * 
	 * @param userAdminId
	 * 			Identifier of the admin
	 * 
	 * @return An user admin instance containing the information
	 */
	@Override
	public UserAdminReportTable getUserAdminReportTable(String userAdminId) {
		try {
			UserAdmin userAdmin = ElectionsDaoFactory.createUserAdminDao(em).getUserAdmin(userAdminId);
			if(userAdmin != null) {
				return new UserAdminReportTable(userAdmin);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}
	
	/**
	 * Returns a list with id and description for all user parameters in the system
	 * 
	 * @return List of parameters key and value
	 */
	@Override
	public List<TableReportStringData> getParametersBasicData() {
		try {
			List<TableReportStringData> parameterData = new ArrayList<>();
			List<Object[]> parameterDataList = ElectionsDaoFactory.createParameterDao(em).getParameterReportTable();

			for(int i = 0; i < parameterDataList.size(); i++) {
				parameterData.add(new TableReportStringData(parameterDataList.get(i)[0].toString(), parameterDataList.get(i)[1].toString()));
			}

			return parameterData;
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	/**
	 * Returns information about the parameter with the given key
	 * 
	 * @param key
	 * 			Identifier of the parameter
	 * 
	 * @return An parameter instance containing the information
	 */
	@Override
	public Parameter getParameterReport(String key) {
		try {
			Parameter parameter = ElectionsDaoFactory.createParameterDao(em).getParameterReport(key);
			if(parameter != null) {
				return parameter;
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

}
