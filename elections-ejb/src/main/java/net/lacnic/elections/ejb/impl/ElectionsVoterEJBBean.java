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
import javax.ejb.*;
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

	/**
	 * Gets a list of candidate name and vote code for an election
	 * 
	 * @param electionId
	 * 			Identifier of the election
	 * 
	 * @return returns a collection of array of objects, where each array contains the candidate name and vote code for all the votes in the election. 
	 */
	@Override
	public List<Object[]> getElectionVotesCandidateAndCode(long electionId) {
		return ElectionsDaoFactory.createVoteDao(em).getElectionVotesCandidateAndCode(electionId);
	}

	/**
	 * Create a new vote or votes from a voter and the candidates chosen
	 * 
	 * @param candidates
	 * 			List of candidate entity containing the ones the voter voted
	 * @param userVoter
	 * 			The information of the voter
	 * @param ip 
	 * 			The ip of the voter
	 */
	@Override
	public void vote(List<Candidate> candidates, UserVoter userVoter, String ip) throws OperationNotPermittedException {
		try {
			UserVoter userVoterDB = ElectionsDaoFactory.createUserVoterDao(em).getUserVoter(userVoter.getUserVoterId());
			Election election = userVoterDB.getElection();
			ArrayList<Vote> votes = EJBFactory.getInstance().getElectionsVoterEJB().doVotes(candidates, userVoter, ip);
			ElectionEmailTemplate t = EJBFactory.getInstance().getElectionsManagerEJB().getEmailTemplate(Constants.TemplateTypeVOTE_CODES, election.getElectionId());
			EJBFactory.getInstance().getMailsSendingEJB().queueSingleSending(t, userVoterDB, null, election, votes);
		} catch (Exception e) {
			e.printStackTrace();
			context.setRollbackOnly();
			throw new OperationNotPermittedException("Operación no permitida");
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ArrayList<Vote> doVotes(List<Candidate> candidates, UserVoter userVoter, String ip) throws OperationNotPermittedException {
		UserVoter userVoterDB = ElectionsDaoFactory.createUserVoterDao(em).getUserVoter(userVoter.getUserVoterId());
		if (userVoterDB.isVoted() || candidates.size() > userVoterDB.getElection().getMaxCandidates() || !userVoterDB.getElection().isEnabledToVote())
			throw new OperationNotPermittedException("Operación no permitida");

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
		em.persist(userVoterDB);
		return votes;
	}


	/**
	 * Gets the voter from a the vote token
	 * 
	 * @param voteToken
	 * 			A string containing the unique vote token
	 * 
	 * @return returns a entity containing the voter associated to the token
	 */
	@Override
	public UserVoter verifyUserVoterAccess(String voteToken) {
		try {
			return ElectionsDaoFactory.createUserVoterDao(em).getUserVoterByToken(voteToken);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Gets a voters of a joint election searching the the token from one and binding by the mail
	 * 
	 * @param voteToken
	 * 			A string containing the unique vote token
	 * 
	 * @return returns an array containing two user voter entities, one corresponding to the voter associated to the token and the other is the user
	 * voter from the same voter but associated to the other election.
	 */
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

	/**
	 * Gets the election associated to a result token
	 * 
	 * @param resultToken
	 * 			A string containing the unique result token
	 * 
	 * @return returns an election entity associated to the token or null if it  is not found
	 */
	@Override
	public Election verifyResultAccess(String resultToken) {
		try {
			return ElectionsDaoFactory.createElectionDao(em).getElectionByResultToken(resultToken);
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}
	
	/**
	 * Gets the auditor associated to a result token
	 * 
	 * @param resultToken
	 * 			A string containing the unique token
	 * 
	 * @return returns an auditor entity associated to the token or null if it  is not found
	 */
	@Override
	public Auditor verifyAuditorResultAccess(String resultToken) {
		try {
			return ElectionsDaoFactory.createAuditorDao(em).getAuditorByResultToken(resultToken);
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	/**
	 * Creates an invalid ip access
	 * 
	 * @param remoteAddress
	 * 			A string containing the ip address that failed to access
	 * 
	 */
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

	/**
	 * Get a list of candidates from the election in the order defined
	 * 
	 * @param electionId
	 * 			Identifier of the election
	 * 
	 * @return a collection of candidate entity containing all the candidates in the election inserted in the order defined.
	 */
	@Override
	public List<Candidate> getElectionCandidatesOrdered(long electionId) throws Exception {
		return EJBFactory.getInstance().getElectionsManagerEJB().getElectionCandidatesOrdered(electionId);
	}
	
	/**
	 * Gets the list of candidates of an election
	 * 
	 * @param electionId
	 * 			Identifier of the election
	 * 
	 * @return returns a collection of candidate entity with the candidates of the election
	 */
	@Override
	public List<Candidate> getElectionCandidates(long electionId) throws Exception {
		return ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(electionId);
	}

	/**
	 * Gets the amount of voters who voter for a candidate 
	 * 
	 * @param candidateId
	 * 				Identifier of the candidate
	 * 
	 * @return returns the amount of voters who voted for the candidate
	 */
	@Override
	public long getCandidateVotesAmount(long candidateId) throws Exception {
		return ElectionsDaoFactory.createCandidateDao(em).getCandidateVotesAmount(candidateId);
	}

	/**
	 * Gets the amount of voters who did vote in an election
	 * 
	 * @param electionId
	 * 				Identifier of the election
	 * 
	 * @return returns the amount of voters who did vote in the election
	 */
	@Override
	public long getElectionUserVotersVotedAmount(long electionId) throws Exception {
		UserVoterDao userVoterDao = ElectionsDaoFactory.createUserVoterDao(em);
		return userVoterDao.getElectionUserVotersVotedAmount(electionId);
	}

	/**
	 * Updates the conformity of an auditor, by setting the agreed conformity to true.
	 * 
	 * @param auditorId
	 * 			Identifier of the auditor
	 */
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
	
	/**
	 * Enables the auditor to be able to review an election.
	 * 
	 * @param auditorId
	 * 				Identifier of the auditor
	 * @param ip
	 * 				Ip address of the user performing the action for loggin purposes.
	 * 
	 */
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
	
	/**
	 * Validates if a user has already voted
	 * 
	 * @param userVoterId
	 * 			Identifier of the user voter
	 * 
	 * @return returns true if the voter has voted on the election, false otherwise
	 */
	@Override
	public boolean userAlreadyVoted(long userVoterId) throws Exception {
		UserVoter userVoter = ElectionsDaoFactory.createUserVoterDao(em).getUserVoter(userVoterId);
		return userVoter.isVoted();
	}

	/**
	 * Get the amount of votes from an election
	 * 
	 * @param electionId
	 * 			Identifier of the election
	 * 
	 * @return returns the amount of votes in an election
	 */
	@Override
	public long getElectionVotesAmount(long electionId) throws Exception {
		return ElectionsDaoFactory.createVoteDao(em).getElectionVotesAmount(electionId);
	}

	/**
	 * Gets the sistem info document.
	 * 
	 * @return returns a byte array with the document info form the template path
	 */
	@Override
	public byte[] getAquaItAudit() {
		try {
			return FilesUtils.getBytesFromFile(templatesPath + "AQUAIT-LACNIC-sistema-votacion.pdf");
		} catch (Exception e) {
			appLogger.error(e);
		}
		return "".getBytes();
	}

	/**
	 * Gets the requirements document.
	 * 
	 * @return returns a byte array with the document info form the template path
	 */
	@Override
	public byte[] getV1Requirements() {
		try {
			return FilesUtils.getBytesFromFile(templatesPath + "RequerimientosSistemaEleccionesV10.pdf");
		} catch (Exception e) {
			appLogger.error(e);
		}
		return "".getBytes();
	}
	
	/**
	 * Gets the election revision document
	 * 
	 * @param filePath
	 * 			A string containing the file path of the document
	 * 
	 * @return returns a file containing the document from the file path.
	 */
	@Override
	public File getElectionRolesRevisionDocument(String filePath) {					
		return FilesUtils.getElectionRolesRevisionDocument(filePath);			
	}

	/**
	 * Gets the result of an election, including all the votes of each candidate.
	 * 
	 * @param electionId
	 * 			Identifier of the election
	 * 
	 * @return returns an election result data object containing the result of an election.
	 */
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

	/**
	 * Gets a list containing the  votes of a voter on an election
	 * 
	 * @param userVoterId
	 * 			Identifier of the voter
	 * @param electionId
	 * 			Identifier of the election
	 * 
	 * @return returns a list containing the candidate name, vote code and candidate picture of all the votes of a voter in an election
	 */
	@Override
	public List<Object[]> getElectionVotesCandidateForUserVoter(long userVoterId, long electionId) {
		return ElectionsDaoFactory.createVoteDao(em).getElectionVotesCandidateForUserVoter(userVoterId, electionId);
	}
	
	/**
	 * Calculates the evolution of votes by day of an election, including for each day, the votes of the day and the sum of votes up to that day
	 * 
	 * @param electionId
	 * 			Identifier of the election
	 * 
	 * @return returns a collection of object containing three lists, a list of days, a list of votes by day and a list with the sum of votes up to each day.
	 */
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
	
	/**
	 * Gets a list of days in the range between two dates
	 *  
	 * @param startDate
	 * 			Start date
	 * @param endDate
	 * 			End date
	 * 
	 * @return returns an array of string with the days between the dates.
	 */
	public List<String> getDatesBetween(Date startDate, Date endDate) {
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
	
	/**
	 * Gets an election.
	 * 
	 * @param electionId
	 * 			Identifier of the election searched
	 * 
	 * @return returns a election entity
	 */
	@Override
	public Election getElection(long electionId) {
		return ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
	}

	/**
	 * Validates if an election is not a contained in a joint election
	 * 
	 * @param electionId
	 * 			Identifier of the election searched
	 * 
	 * @return returns true if the election is not a cointained in a joint election, false if it is.
	 */
	@Override
	public boolean electionIsSimple(long electionId) {
		return ElectionsDaoFactory.createElectionDao(em).electionIsSimple(electionId);
	}
	
	/**
	 * Gets a  joint election filtering by an election
	 * 
	 * @param electionId
	 * 			Identifier of the election searched
	 * 
	 * @return returns a joint election containing the election searched or null if it does not exists
	 */
	@Override
	public JointElection getJointElection(long electionId) {
		return ElectionsDaoFactory.createElectionDao(em).getJointElectionForElection(electionId);
	}

	/**
	 * Calculates the amount of votes in an election with an specific amount each voter had the possibility to vote (no filtering whether the voter voted or no)
	 * 
	 * @param electionId
	 * 			Identifier of the election
	 * @param voteAmount
	 * 			Amount of votes per voter searched 
	 * @return returns the count of all the voter who had the chance to vote the amount of votes passed as a parameter in the election
	 */
	private Long getElectionUserVotersAmountByVoteAmount(long electionId, Integer voteAmount) {
		UserVoterDao padronDao = ElectionsDaoFactory.createUserVoterDao(em);
		return padronDao.getElectionUserVotersAmountByVoteAmount(electionId, voteAmount);
	}

	/**
	 * Calculates the amount of votes in an election with an specific amount each voter voted
	 * 
	 * @param electionId
	 * 			Identifier of the election
	 * @param voteAmount
	 * 			Amount of votes per voter searched 
	 * @return returns the count of all the voter who voted the amount of votes passed as a parameter in the election
	 */
	private Long getElectionUserVotersAmountByVoteAmountAndVoted(long electionId, Integer voteAmount) {
		UserVoterDao padronDao = ElectionsDaoFactory.createUserVoterDao(em);
		return padronDao.getElectionUserVotersAmountByVoteAmountAndVoted(electionId, voteAmount);
	}

	/**
	 * Creates a new activity log entry.
	 * 
	 * @param userAdminId
	 * 			Identifier of the user performing the activity
	 * @param activityType
	 * 			Type of the activity
	 * @param description
	 * 			Description of the activity
	 * @param ip
	 * 			Ip of the user performing the activity
	 * @param electionId
	 * 			Identifier of the election on which the activity took place
	 */
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
