package net.lacnic.elections.ejb.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.dao.UserVoterDao;
import net.lacnic.elections.data.ElectionsResultsData;
import net.lacnic.elections.data.ResultDetailData;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.ejb.ElectionsVoterEJB;
import net.lacnic.elections.exception.OperationNotPermittedException;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.StringUtils;
import net.lacnic.elections.utils.FilesUtils;


@Stateless
@Remote(ElectionsVoterEJB.class)
public class ElectionsVoterEJBBean implements ElectionsVoterEJB {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	String templatesPath = System.getProperty("jboss.server.config.dir").concat("/templates/");

	@Resource
	private SessionContext context;


	public ElectionsVoterEJBBean() { }


	@Override
	public List<Object[]> getElectionVotesCandidateAndCode(long electionId) {
		return ElectionsDaoFactory.createVoteDao(em).getElectionVotesCandidateAndCode(electionId);
	}

	@Override
	public void vote(List<Candidate> candidates, UserVoter userVoter, String ip) throws OperationNotPermittedException {
		UserVoter userVoterDB = ElectionsDaoFactory.createUserVoterDao(em).getUserVoter(userVoter.getUserVoterId());
		if (userVoterDB.isVoted()) {
			throw new OperationNotPermittedException("Operación no permitida");
		}
		if (candidates.size() > userVoterDB.getElection().getMaxCandidates() || userVoterDB.isVoted() || !userVoterDB.getElection().isEnabledToVote()) {
			throw new OperationNotPermittedException("Operación no permitida");
		}

		ArrayList<Vote> votes = new ArrayList<>();
		Date voteDate = new Date();
		for (Candidate candidate : candidates) {
			for (int i = 0; i < userVoterDB.getVoteAmount(); i++) {
				Vote vote = new Vote();
				vote.setCode(StringUtils.createSmallToken());
				vote.setIp(ip);
				vote.setCandidate(candidate);
				vote.setVoteDate(voteDate);
				vote.setUserVoter(userVoterDB);
				vote.setElection(userVoterDB.getElection());
				em.persist(vote);
				votes.add(vote);
			}
		}
		userVoterDB.setVoted(true);
		userVoterDB.setVoteDate(voteDate);
		ElectionEmailTemplate t = EJBFactory.getInstance().getElectionsManagerEJB().getEmailTemplate(Constants.TemplateTypeVOTE_CODES, userVoterDB.getElection().getElectionId());
		EJBFactory.getInstance().getMailsSendingEJB().queueSingleSending(t, userVoterDB, null, userVoterDB.getElection(), votes);
		em.persist(userVoterDB);
	}

	@Override
	public UserVoter verifyUserVoterAccess(String voteToken) {
		try {
			return ElectionsDaoFactory.createUserVoterDao(em).getUserVoterByToken(voteToken);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public UserVoter[] verifyUserVoterAccessJointElection(String voteToken) {
		try {
			UserVoter userVoter1 = ElectionsDaoFactory.createUserVoterDao(em).getUserVoterByToken(voteToken);
			UserVoter userVoter2 = null;
			JointElection jointElection = ElectionsDaoFactory.createElectionDao(em).getJointElectionForElection(userVoter1.getElection().getElectionId());
			long singleElection = 0;

			if (userVoter1.getElection().getElectionId() == jointElection.getIdElectionB())
				singleElection = jointElection.getIdElectionA();
			else
				singleElection = jointElection.getIdElectionB();

			for (UserVoter userVoter : ElectionsDaoFactory.createElectionDao(em).getElection(singleElection).getUserVoters()) {
				if (userVoter.getMail().toUpperCase().equalsIgnoreCase(userVoter1.getMail())) {
					userVoter2 = userVoter;
					break;
				}
			}

			return new UserVoter[] { userVoter1, userVoter2 };
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	@Override
	public Election verifyResultAccess(String resultToken) {
		try {
			return ElectionsDaoFactory.createElectionDao(em).getElectionByResultToken(resultToken);
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	@Override
	public Auditor verifyAuditorResultAccess(String resultToken) {
		try {
			return ElectionsDaoFactory.createAuditorDao(em).getAuditorByResultToken(resultToken);
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	@Override
	public void saveFailedAccessIp(String remoteAddress) {
		IpAccess ip = ElectionsDaoFactory.createIpAccessDao(em).getIP(remoteAddress);
		if (ip == null) {
			ip = new IpAccess();
			ip.setLastAttemptDate(new Date());
			ip.setFirstAttemptDate(new Date());
			ip.setIp(remoteAddress);
			ip.setAttemptCount(1);
			em.persist(ip);
		} else {
			ip.setLastAttemptDate(new Date());
			ip.setAttemptCount(ip.getAttemptCount() + 1);
			em.merge(ip);
		}
	}

	@Override
	public List<Candidate> getElectionCandidatesOrdered(long electionId) throws Exception {
		return EJBFactory.getInstance().getElectionsManagerEJB().getElectionCandidatesOrdered(electionId);
	}

	@Override
	public List<Candidate> getElectionCandidates(long electionId) throws Exception {
		return ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(electionId);
	}

	@Override
	public long getCandidateVotesAmount(long candidateId) throws Exception {
		return ElectionsDaoFactory.createCandidateDao(em).getCandidateVotesAmount(candidateId);
	}

	@Override
	public long getElectionUserVotersVotedAmount(long electionId) throws Exception {
		UserVoterDao userVoterDao = ElectionsDaoFactory.createUserVoterDao(em);
		return userVoterDao.getElectionUserVotersVotedAmount(electionId);
	}

	@Override
	public void confirmAuditorAgreedConformity(long auditorId) {
		try {
			Auditor auditor = em.find(Auditor.class, auditorId);
			auditor.setAgreedConformity(true);
			em.persist(auditor);
			ElectionEmailTemplate mailTemplate = EJBFactory.getInstance().getElectionsManagerEJB().getEmailTemplate(Constants.TemplateTypeAUDITOR_AGREEMENT, auditor.getElection().getElectionId());
			EJBFactory.getInstance().getMailsSendingEJB().queueSingleSending(mailTemplate, null, auditor, auditor.getElection(), new ArrayList<>());
		} catch (Exception e) {
			appLogger.error(e);
		}

	}

	@Override
	public void enableAuditorElectionRevision(long auditorId, String ip) {
		try {
			Auditor auditor = em.find(Auditor.class, auditorId);
			auditor.setRevisionAvailable(true);
			em.persist(auditor);
			ElectionEmailTemplate mailTemplate = EJBFactory.getInstance().getElectionsManagerEJB().getEmailTemplate(Constants.TemplateTypeAUDITOR_REVISION, auditor.getElection().getElectionId());
			EJBFactory.getInstance().getMailsSendingEJB().queueSingleSending(mailTemplate, null, auditor, auditor.getElection(), new ArrayList<>());

			String description = auditor.getAuditorId() + " - " + auditor.getName() + " autorizó la revisión de la elección: " + auditor.getElection().getTitleSpanish();
			persistActivity(auditor.getName(), ActivityType.ELECTION_REVISION_YES, description, ip, auditor.getElection().getElectionId());
		} catch (Exception e) {
			appLogger.error(e);
		}

	}

	@Override
	public boolean userAlreadyVoted(long userVoterId) throws Exception {
		UserVoter userVoter = ElectionsDaoFactory.createUserVoterDao(em).getUserVoter(userVoterId);
		return userVoter.isVoted();
	}

	@Override
	public long getElectionVotesAmount(long electionId) throws Exception {
		return ElectionsDaoFactory.createVoteDao(em).getElectionVotesAmount(electionId);
	}

	@Override
	public byte[] getAquaItAudit() {
		try {
			return FilesUtils.getBytesFromFile(templatesPath + "AQUAIT-LACNIC-sistema-votacion.pdf");
		} catch (Exception e) {
			appLogger.error(e);
		}
		return "".getBytes();
	}

	@Override
	public byte[] getV1Requirements() {
		try {
			return FilesUtils.getBytesFromFile(templatesPath + "RequerimientosSistemaEleccionesV10.pdf");
		} catch (Exception e) {
			appLogger.error(e);
		}
		return "".getBytes();
	}

	@Override
	public File getElectionRolesRevisionDocument(String filePath) {					
		return FilesUtils.getElectionRolesRevisionDocument(filePath);			
	}

	@Override
	public ElectionsResultsData getElectionsResultsData(long electionId) throws Exception {
		Election election = em.find(Election.class, electionId);
		int max = election.getMaxCandidates();

		ElectionsResultsData resultsData = new ElectionsResultsData(max);
		ArrayList<ResultDetailData> listResultDetailData = new ArrayList<>();
		List<Integer> distinctVoteAmounts = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVotersDistinctVoteAmounts(electionId);

		for (Integer voteAmount : distinctVoteAmounts) {
			Long enabled = getElectionUserVotersAmountByVoteAmount(electionId, voteAmount);
			Long participants = getElectionUserVotersAmountByVoteAmountAndVoted(electionId, voteAmount);
			ResultDetailData detail = new ResultDetailData(enabled, participants, voteAmount);
			listResultDetailData.add(detail);
		}
		resultsData.setResultDetailData(listResultDetailData);
		resultsData.calculateTotals();
		return resultsData;
	}

	@Override
	public List<Object[]> getElectionVotesCandidateForUserVoter(long userVoterId, long electionId) {
		return ElectionsDaoFactory.createVoteDao(em).getElectionVotesCandidateForUserVoter(userVoterId, electionId);
	}

	@Override
	public List<Object> getElectionVoteEvolutionData(long electionId) {
		List<UserVoter> votersList = ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersVoted(electionId);

		List<String> days = new ArrayList<>();
		List<Integer> votesByDay = new ArrayList<>();
		List<Integer> votesSum = new ArrayList<>();
		List<Object> data = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		try {

			Date day1 = votersList.get(0).getVoteDate();
			Date lastDay = votersList.get(votersList.size() - 1).getVoteDate();

			List<String> allDates = getDatesBetween(day1, lastDay);
			List<String> voteDates = new ArrayList<>();

			for (UserVoter voter : votersList) {
				voteDates.add(sdf.format(voter.getVoteDate()));
			}

			for (String date : allDates) {
				days.add(date);
				votesByDay.add(Collections.frequency(voteDates, date));
			}

			int sum = 0;
			for (int votesDay : votesByDay) {
				sum = sum + votesDay;
				votesSum.add(sum);
			}

			data.add(days);
			data.add(votesByDay);
			data.add(votesSum);

			return data;
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public static List<String> getDatesBetween(Date startDate, Date endDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		List<String> datesInRange = new ArrayList<>();

		Calendar startCalendar = new GregorianCalendar();
		startCalendar.setTime(startDate);

		Calendar endCalendar = new GregorianCalendar();
		endCalendar.setTime(endDate);

		while (startCalendar.before(endCalendar)) {
			Date result = startCalendar.getTime();
			String day = sdf.format(result);
			if (!datesInRange.contains(day))
				datesInRange.add(day);
			startCalendar.add(Calendar.DATE, 1);
		}
		String day = sdf.format(endDate);
		if (!datesInRange.contains(day))
			datesInRange.add(day);

		return datesInRange;
	}

	@Override
	public Election getElection(long electionId) {
		return ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
	}

	@Override
	public boolean electionIsSimple(long electionId) {
		return ElectionsDaoFactory.createElectionDao(em).electionIsSimple(electionId);
	}

	@Override
	public JointElection getJointElection(long electionId) {
		return ElectionsDaoFactory.createElectionDao(em).getJointElectionForElection(electionId);
	}


	private Long getElectionUserVotersAmountByVoteAmount(long electionId, Integer voteAmount) {
		UserVoterDao padronDao = ElectionsDaoFactory.createUserVoterDao(em);
		return padronDao.getElectionUserVotersAmountByVoteAmount(electionId, voteAmount);
	}

	private Long getElectionUserVotersAmountByVoteAmountAndVoted(long electionId, Integer voteAmount) {
		UserVoterDao padronDao = ElectionsDaoFactory.createUserVoterDao(em);
		return padronDao.getElectionUserVotersAmountByVoteAmountAndVoted(electionId, voteAmount);
	}

	private void persistActivity(String userAdminId, ActivityType activityType, String description, String ip, Long electionId) {
		Activity activity = new Activity();
		activity.setIp(ip);
		activity.setElectionId(electionId);
		activity.setTimestamp(new Date());
		activity.setUserName(userAdminId);
		activity.setActivityType(activityType);
		activity.setDescription(description);
		em.persist(activity);
	}

}
