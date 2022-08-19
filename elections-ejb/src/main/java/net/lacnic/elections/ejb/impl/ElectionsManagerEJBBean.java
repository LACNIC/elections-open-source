package net.lacnic.elections.ejb.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Commissioner;
import net.lacnic.elections.domain.Customization;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.RecipientType;
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.ejb.ElectionsManagerEJB;
import net.lacnic.elections.exception.CensusValidationException;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.ExcelUtils;
import net.lacnic.elections.utils.FilesUtils;
import net.lacnic.elections.utils.LinksUtils;
import net.lacnic.elections.utils.StringUtils;
import net.lacnic.portal.auth.client.LoginData;
import net.lacnic.portal.auth.client.UtilsLogin;

@Stateless
@Remote(ElectionsManagerEJB.class)
public class ElectionsManagerEJBBean implements ElectionsManagerEJB {

	private static final String TEXT_ELECCION = " para la elección ";

	private Random rand = new SecureRandom();

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	public ElectionsManagerEJBBean() {
	}

	/**
	 * Logs an user to the application and persists audit information in the
	 * activity table
	 * 
	 * @param userAdminId Login id
	 * @param password    User password
	 * @param ip          Ip of the user login in
	 * 
	 * @return returns a UserAdmin entity if the id and password exists and null
	 *         otherwise.
	 */
	@Override
	public UserAdmin userAdminLogin(String userAdminId, String password, String ip) {
		UserAdmin a = ElectionsDaoFactory.createUserAdminDao(em).verifyUserLogin(userAdminId, password);
		try {
			if (a != null) {
				String description = userAdminId.toUpperCase() + " se ha logueado exitosamente";
				EJBFactory.getInstance().getElectionsManagerEJB().persistActivity(userAdminId, ActivityType.LOGIN_SUCCESSFUL, description, ip, null);
			} else {
				String description = "Intento fallido de login de usuario " + userAdminId.toUpperCase();
				EJBFactory.getInstance().getElectionsManagerEJB().persistActivity(userAdminId, ActivityType.LOGIN_FAILED, description, ip, null);
			}
		} catch (Exception e) {
			appLogger.error(e);
		}
		return a;
	}

	/**
	 * Gets a list of all the users in the system
	 * 
	 * @return returns a list of UserAdmin entity.
	 */
	@Override
	public List<UserAdmin> getUserAdminsAll() {
		return ElectionsDaoFactory.createUserAdminDao(em).getUserAdminsAll();
	}

	/**
	 * Finds a user using the id as a parameter
	 * 
	 * @param userAdminId The id of the user needed
	 * 
	 * @return returns an UserAdmin entity containing the information of the user id
	 *         if found or null if it does not exists
	 */
	@Override
	public UserAdmin getUserAdmin(String userAdminId) {
		return em.find(UserAdmin.class, userAdminId);
	}

	/**
	 * Validates the captcha
	 * 
	 * @param reCaptchaResponse The captcha string response
	 * 
	 * @return returns true if the captcha is valid, false if it is not
	 */
	@Override
	public boolean isValidCaptchaResponse(String reCaptchaResponse) {

		try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
			appLogger.info("start verifying ...");
			appLogger.info("reCAPTCHA response value : " + reCaptchaResponse);

			String skGoogleApiReCaptcha = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.SkGoogleApiReCaptcha);
			String checkURL = "https://www.google.com/recaptcha/api/siteverify" + "?secret=" + skGoogleApiReCaptcha + "&response=" + reCaptchaResponse;

			appLogger.info("check URL : " + checkURL);

			HttpPost post = new HttpPost("https://www.google.com/recaptcha/api/siteverify");

			// add headerype
			post.setHeader("User-Agent", "Mozilla/5.0");

			List<NameValuePair> urlParameters = new ArrayList<>();
			urlParameters.add(new BasicNameValuePair("secret", skGoogleApiReCaptcha));
			urlParameters.add(new BasicNameValuePair("response", reCaptchaResponse));

			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			HttpResponse httpClientResponse = httpClient.execute(post);

			appLogger.info("\nSending 'POST' request to URL : https://www.google.com/recaptcha/api/siteverify");
			appLogger.info("Post parameters : " + post.getEntity());
			appLogger.info("Response Code : " + httpClientResponse.getStatusLine().getStatusCode());

			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(httpClientResponse.getEntity().getContent()));

			StringBuilder result = new StringBuilder();
			String line = "";
			while ((line = bufferReader.readLine()) != null) {
				result.append(line);
			}

			JsonReader jsonReader = Json.createReader(new StringReader(result.toString()));
			try {
				JsonObject jsonObject = jsonReader.readObject();
				appLogger.info(jsonObject.getBoolean("success"));
				return jsonObject.getBoolean("success");
			} catch (Exception e) {
				appLogger.error(e);
				return false;
			} finally {
				jsonReader.close();
			}
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	/**
	 * Validates if it is Prod environment and captcha is configured
	 * 
	 * @return returns true if captcha should be shown
	 */
	@Override
	public boolean isShowCaptcha() {
		try {
			return EJBFactory.getInstance().getElectionsParametersEJB().isProd() && !getDataSiteKey().isEmpty() && !getSkGoogleApiReCaptcha().isEmpty();
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	private String getSkGoogleApiReCaptcha() {
		return EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.SkGoogleApiReCaptcha);
	}

	/**
	 * Gets an election by its identifier
	 * 
	 * @param electionId identifier of the election
	 * 
	 * @return returns an entity with the election information
	 */
	@Override
	public Election getElection(long electionId) {
		return ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
	}

	/**
	 * Gets a list of the candidates of an election sorted
	 * 
	 * @param electionId identifier of the election
	 * 
	 * @return returns a list of candidate entity sorted according to their order.
	 */
	@Override
	public List<Candidate> getElectionCandidatesOrdered(long electionId) {
		List<Candidate> candidates = ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(electionId);
		Election election = getElection(electionId);
		Collections.sort(candidates, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate candidate1, Candidate candidate2) {
				int result;

				if (candidate1.getCandidateOrder() == Constants.MIN_ORDER)
					result = Constants.MIN_ORDER;
				else if (candidate2.getCandidateOrder() == Constants.MAX_ORDER)
					result = Constants.MAX_ORDER;
				else if (election.isRandomOrderCandidates()) {
					result = Integer.valueOf(rand.nextInt(30)).compareTo(Integer.valueOf(rand.nextInt(30)));
				} else
					result = (Integer.valueOf(candidate2.getCandidateOrder()).compareTo(Integer.valueOf(candidate1.getCandidateOrder())));
				return result;
			}
		});
		return candidates;
	}

	/**
	 * Deletes an election removing all its related resources (admins, auditors,
	 * candidates, emails, votes and user voters)
	 * 
	 * @param electionId    identifier of the election
	 * @param electionTitle title of the election used for logging purposes
	 * @param userAdminId   id of the user deleting the election, used for logging
	 *                      purposes
	 * @param ip            ip of the user deleting the election, used for logging
	 *                      purposes
	 */
	@Override
	public void removeElection(long electionId, String electionTitle, String userAdminId, String ip) throws Exception {
		try {
			List<UserAdmin> userAdmins = ElectionsDaoFactory.createUserAdminDao(em).getElectionUserAdmins(electionId);
			List<Auditor> auditors = ElectionsDaoFactory.createAuditorDao(em).getElectionAuditors(electionId);
			List<Candidate> candidates = ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(electionId);
			List<Email> emails = ElectionsDaoFactory.createEmailDao(em).getElectionEmails(electionId);
			List<ElectionEmailTemplate> emailTemplates = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplates(electionId);
			List<UserVoter> userVoters = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionId);
			List<Vote> votes = ElectionsDaoFactory.createVoteDao(em).getElectionVotes(electionId);
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			for (UserAdmin userAdmin : userAdmins)
				em.remove(userAdmin);
			for (Auditor auditor : auditors)
				em.remove(auditor);
			for (Candidate candidate : candidates)
				em.remove(candidate);
			for (Email email : emails)
				em.remove(email);
			for (ElectionEmailTemplate emailTemplate : emailTemplates)
				em.remove(emailTemplate);
			for (UserVoter userVoter : userVoters)
				em.remove(userVoter);
			for (Vote vote : votes)
				em.remove(vote);
			em.remove(election);
			String description = userAdminId.toUpperCase() + " removed election " + electionTitle;
			persistActivity(userAdminId, ActivityType.DELETE_ELECTION, description, ip, electionId);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Get a list of the auditors related to an election
	 * 
	 * @param electionId identifier of the election
	 * 
	 * @return returns a list of auditor entity related the election
	 */
	@Override
	public List<Auditor> getElectionAuditors(long electionId) throws Exception {
		return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditors(electionId);
	}

	/**
	 * Gets a list of all the elections on the application sorted by creation date
	 * 
	 * @return returns a list of the election entity sorted by creation date.
	 */
	@Override
	public List<Election> getElectionsAllOrderCreationDate() {
		return ElectionsDaoFactory.createElectionDao(em).getElectionsAllOrderCreationDate();
	}

	/**
	 * Get a voter identified by the id
	 * 
	 * @param userVoterId identifier of the voter
	 * 
	 * @return returns a entity with the voter information
	 */
	@Override
	public UserVoter getUserVoter(long userVoterId) {
		return em.find(UserVoter.class, userVoterId);
	}

	/**
	 * Updates or creates (if it does not exists) an election
	 * 
	 * @param election    An election entity with the information to update the
	 *                    election.
	 * @param userAdminId Id of the user, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 * 
	 * @return returns the election entity of the updated election
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Election updateElection(Election election, String userAdminId, String ip) throws Exception {
		try {
			String description;
			if (election.getElectionId() == 0) {
				em.persist(election);
				description = userAdminId.toUpperCase() + " creó la elección" + " (" + election.getTitleSpanish() + ")" + " correctamente";
				persistActivity(userAdminId, ActivityType.CREATE_ELECTION, description, ip, election.getElectionId());
				createElectionEmailTemplates(election); // new
				List<Commissioner> commisioners = ElectionsDaoFactory.createCommissionerDao(em).getCommissionersAll();
				for (Commissioner commisioner : commisioners) {
					em.persist(new Auditor(election, commisioner));
				}
			} else
				em.merge(election);
			description = userAdminId.toUpperCase() + " actualizó la elección " + " (" + election.getTitleSpanish() + ")" + " correctamente";
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, election.getElectionId());
		} catch (Exception e) {
			appLogger.error(e);
		}

		return election;
	}

	/**
	 * Gets then result html link of an election
	 * 
	 * @param election Entity of the election
	 * 
	 * @return returns a string with the result link
	 */
	@Override
	public String getResultsLink(Election election) throws Exception {
		return LinksUtils.buildResultsLink(election.getResultToken());
	}

	/**
	 * Updates the election result link, by enabling/disabling it
	 * 
	 * @param electionId  Identifier of the election
	 * @param status      New value for the result link
	 * @param userAdminId Id of the user, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 * 
	 */
	@Override
	public void setResultsLinkStatus(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setResultLinkAvailable(status);
			em.persist(election);
			String message = "deshabilitó el link de resultado para la elección ";
			if (Boolean.TRUE.equals(status)) {
				message = "habilitó el link de resultado para la elección ";
			}
			String description = userAdminId.toUpperCase() + " " + message + "(" + election.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.ENABLE_RESULTS_LINK, description, ip, election.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	/**
	 * Updates the election audit link, by enabling/disabling it
	 * 
	 * @param electionId  Identifier of the election
	 * @param status      New value for the audit link
	 * @param userAdminId Id of the user, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 * 
	 */
	@Override
	public void setAuditLinkStatus(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setAuditorLinkAvailable(status);
			em.persist(election);
			String message = "deshabilitó el link de auditoria para la elección ";
			if (Boolean.TRUE.equals(status)) {
				message = "habilitó el link de auditoria para la elección ";
			}
			String description = userAdminId.toUpperCase() + " " + message + "(" + election.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.ENABLE_AUDIT_LINK, description, ip, election.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	/**
	 * Gets all the commissioners from the system.
	 * 
	 * @return returns a list of commissioner entity containing all the
	 *         commissioners in the system.
	 */

	@Override
	public List<Commissioner> getCommissionersAll() {
		return ElectionsDaoFactory.createCommissionerDao(em).getCommissionersAll();
	}

	/**
	 * Removes a voter from the election census.
	 * 
	 * @param userVoter     Entity containing the information of the voter
	 * @param electionTitle Title of the election used for logging purposes
	 * @param userAdminId   Identifier of the user performing the action used for
	 *                      logging purposes
	 * @param ip            Ip of the user performing the action, used for logging
	 *                      purposes
	 */
	@Override
	public void removeUserVoter(UserVoter userVoter, String electionTitle, String userAdminId, String ip) {
		UserVoter userVoterDB = em.find(UserVoter.class, userVoter.getUserVoterId());
		Long electionId = userVoterDB.getElection().getElectionId();
		em.remove(userVoterDB);
		String description = userAdminId.toUpperCase() + " eliminó a " + userVoterDB.getName() + " del listado de usuario padrón en la elección " + electionTitle;
		persistActivity(userAdminId, ActivityType.REMOVE_USER_CENSUS, description, ip, electionId);
		long candidateAmount = ElectionsDaoFactory.createUserVoterDao(em).getElectionCensusSize(userVoterDB.getElection().getElectionId());
		if (candidateAmount < 1) {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(userVoterDB.getElection().getElectionId());
			election.setElectorsSet(false);
			em.persist(election);
		}
	}

	/**
	 * Gets all the activity log of the system.
	 * 
	 * @return returns a list of activity entity which contains all the activities
	 *         on the system
	 */
	@Override
	public List<Activity> getActivitiesAll() {
		return ElectionsDaoFactory.createActivityDao(em).getActivitiesAll();
	}

	/**
	 * Gets a list of the activity for a particular election
	 * 
	 * @param electionId Identifier of the election
	 * 
	 * @return returns a list of activity entity which contains all the activities
	 *         of the election.
	 */
	@Override
	public List<Activity> getElectionActivities(long electionId) {
		return ElectionsDaoFactory.createActivityDao(em).getElectionActivities(electionId);
	}

	/**
	 * Get the voters of a particular election
	 * 
	 * @param electionId Identifier of the election
	 * 
	 * @return returns a list of uservoter entity which contains the voters of the
	 *         election.
	 */
	@Override
	public List<UserVoter> getElectionUserVoters(long electionId) {
		return ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionId);
	}

	/**
	 * Updates a particular voter's link of an election
	 * 
	 * @param userVoterId   Identifier of the voter
	 * 
	 * @param electionTitle Title of the election used for logging purposes
	 * @param userAdminId   Identifier of the user performing the action used for
	 *                      logging purposes
	 * @param ip            Ip of the user performing the action, used for logging
	 *                      purposes
	 * 
	 * 
	 */
	@Override
	public void updateUserVoterToken(long userVoterId, String name, String electionTitle, String userAdminId, String ip) {
		UserVoter userVoter = em.find(UserVoter.class, userVoterId);
		userVoter.setVoteToken(StringUtils.createSecureToken());
		em.merge(userVoter);
		String description = userAdminId.toUpperCase() + " actualizó el link de votación para el usuario " + name + " en la elección " + electionTitle;
		persistActivity(userAdminId, ActivityType.UPDATE_TOKEN_USER_CENSUS, description, ip, userVoter.getElection().getElectionId());
	}

	/**
	 * Deletes a user admin from the system.
	 * 
	 * @param userAdminToDeleteId Identifier of the user to be deleted
	 * @param userAdminId         Identifier of the user performing the action used
	 *                            for logging purposes
	 * @param ip                  Ip of the user performing the action, used for
	 *                            logging purposes
	 */
	@Override
	public void removeUserAdmin(String userAdminToDeleteId, String userAdminId, String ip) {
		UserAdmin userAdmin = ElectionsDaoFactory.createUserAdminDao(em).getUserAdmin(userAdminToDeleteId);
		em.remove(userAdmin);
		String description = userAdminId.toUpperCase() + " eliminó a " + userAdminToDeleteId.toUpperCase() + " de listado de admin";
		persistActivity(userAdminId, ActivityType.REMOVE_ADMIN, description, ip, null);
	}

	/**
	 * Upload the census of a particular election from an excel sheet.
	 * 
	 * @param electionId  Identifier of the election
	 * @param content     Excel file containing the voters to upload
	 * @param userAdminId Identifier of the user performing the action used for
	 *                    logging purposes
	 * @param ip          Ip of the user performing the action, used for logging
	 *                    purposes
	 */
	@Override
	public void updateElectionCensus(String contentType, long electionId, byte[] content, String userAdminId, String ip) throws CensusValidationException, Exception {
		try {
			List<UserVoter> userVoters = ExcelUtils.processCensusExcel(contentType, content);
			Election election = em.find(Election.class, electionId);
			election.setElectorsSet(true);
			ElectionsDaoFactory.createVoteDao(em).deleteElectionVotes(electionId);
			ElectionsDaoFactory.createUserVoterDao(em).deleteElectionCensus(electionId);

			for (UserVoter userVoter : userVoters) {
				userVoter.setElection(election);
				userVoter.setVoteToken(StringUtils.createSecureToken());
				em.persist(userVoter);
			}
			em.persist(election);

			String description = userAdminId.toUpperCase() + " agregó el padrón para la elección  " + "(" + election.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.ADD_CENSUS, description, ip, electionId);

		} catch (CensusValidationException e) {
			appLogger.error(e);
			throw e;
		} catch (Exception e1) {
			appLogger.error(e1);
			throw e1;
		}
	}

	/**
	 * Get a list of the elections from this year
	 * 
	 * @return returns a list election light entity containing the information
	 */
	@Override
	public List<Election> getElectionsLightThisYear() {
		return ElectionsDaoFactory.createElectionDao(em).getElectionsLightThisYear();
	}

	/**
	 * Adds a voter to a particular election
	 * 
	 * @param electionId  Identifier of the election
	 * @param userVoter   Entity containing the voter information.
	 * @param userAdminId Identifier of the user performing the action used for
	 *                    logging purposes
	 * @param ip          Ip of the user performing the action, used for logging
	 *                    purposes
	 * 
	 * @return returns true if the operation succeeds
	 * 
	 */
	@Override
	public boolean addUserVoter(long electionId, UserVoter userVoter, String userAdminId, String ip) throws CensusValidationException {
		try {
			// Bug-Fix: Now the e-mail can be repeated because the same person can vote per
			// different organizations

//			UserVoter userVoterDB = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoterByMail(electionId,
//					userVoter.getMail());
//			if (userVoterDB == null) {

			Election election = em.find(Election.class, electionId);
			userVoter.setElection(election);
			userVoter.setVoteToken(StringUtils.createSecureToken());
			em.persist(userVoter);
			election.setElectorsSet(true);
			String description = userAdminId.toUpperCase() + " agregó a " + userVoter.getName() + " como usuario padrón para la elección " + election.getTitleSpanish();
			persistActivity(userAdminId, ActivityType.ADD_VOTE_USER, description, ip, electionId);
			return true;
//			} else {
//				throw new CensusValidationException("censusManagementDuplicateEmail", null, null);
//			}
//		} catch (CensusValidationException censusValidationException) {
//			appLogger.error(censusValidationException);
//			throw censusValidationException;
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	/**
	 * Updates the information of a voter.
	 * 
	 * @param userVoter   entity with the voter information
	 * @param userAdminId Identifier of the user performing the action used for
	 *                    logging purposes
	 * @param ip          Ip of the user performing the action, used for logging
	 *                    purposes
	 * 
	 */
	@Override
	public void editUserVoter(UserVoter userVoter, String userAdminId, String ip) throws CensusValidationException {
		try {
			// Bug-Fix: Now the e-mail can be repeated because the same person can vote per
			// different organizations

//			UserVoter userVoterDB = ElectionsDaoFactory.createUserVoterDao(em)
//					.getElectionUserVoterByMail(userVoter.getElection().getElectionId(), userVoter.getMail());
//			if (userVoterDB == null || userVoterDB.getUserVoterId() == userVoter.getUserVoterId()) {
			// If found user is the same as the one we're updating, it's ok
			em.merge(userVoter);
			String description = userAdminId.toUpperCase() + " actualizó los datos de un usuario padrón para la elección  " + userVoter.getElection().getTitleSpanish();
			persistActivity(userAdminId, ActivityType.EDIT_VOTE_USER, description, ip, userVoter.getElection().getElectionId());
//			} else {
//				throw new CensusValidationException("censusManagementDuplicateEmail", null, null);
//			}
//		} catch (CensusValidationException censusValidationException) {
//			appLogger.error(censusValidationException);
//			throw censusValidationException;

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Gets all the email templates linked to a particular election.
	 * 
	 * @param electionId Identifier of the election
	 * 
	 * @return returns a list of election email template entity containing the
	 *         information.
	 */
	@Override
	public List<ElectionEmailTemplate> getElectionEmailTemplates(long electionId) {
		if (electionId == 0)
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplates();
		else
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplates(electionId);
	}

	/**
	 * Updates an email template information.
	 * 
	 * @param electionEmailTemplate Entity with the information to update.
	 */
	@Override
	public void modifyElectionEmailTemplate(ElectionEmailTemplate electionEmailTemplate) {
		Election election = electionEmailTemplate.getElection();
		if (election != null)
			em.merge(election);
		em.merge(electionEmailTemplate);
	}

	/**
	 * Updates the election vote link, by enabling/disabling it
	 * 
	 * @param electionId  Identifier of the election
	 * @param status      New value for the vote link
	 * @param userAdminId Id of the user, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void setVoteLinkStatus(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setVotingLinkAvailable(status);
			em.persist(election);
			String message = "deshabilitó el link de votación para la elección ";
			if (Boolean.TRUE.equals(status)) {
				message = "habilitó el link de votación para la elección ";
			}
			String description = userAdminId.toUpperCase() + " " + message + "(" + election.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.ENABLE_VOTE_LINK, description, ip, election.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	/**
	 * Gets the default email templates
	 * 
	 * @return returns a list of email template entity with the information.
	 */
	@Override
	public List<ElectionEmailTemplate> getBaseEmailTemplates() {
		return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplates();
	}

	/**
	 * Get a particular email template of a particular election
	 * 
	 * @param templateType Type of the template to get
	 * @param electionId   Identifier of the election
	 * 
	 * @return returns an email template entity with the information
	 */
	@Override
	public ElectionEmailTemplate getEmailTemplate(String templateType, long electionId) {
		if (electionId == 0)
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplate(templateType);
		else
			return ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(templateType, electionId);
	}

	/**
	 * Updates the information of an admin user, including giving him/her privileges
	 * over a particular election
	 * 
	 * @param userAdmin            Entity with the information of the user.
	 * @param email                Original email of the user
	 * @param authorizedElectionId Identifier of the election that user will have
	 *                             authorization
	 * @param userAdminId          Id of the user, used for logging purposes
	 * @param ip                   Ip of the user, used for logging purposes
	 */
	@Override
	public void editUserAdmin(UserAdmin userAdmin, String email, Long authorizedElectionId, String userAdminId, String ip) {
		try {
			userAdmin.setAuthorizedElectionId(authorizedElectionId);
			em.merge(userAdmin);
			String description;
			if (userAdminId.equalsIgnoreCase(userAdmin.getUserAdminId())) {
				description = userAdminId.toUpperCase() + " editó  su email de " + email + " a " + userAdmin.getEmail() + " y su elección autorizada a la de id " + authorizedElectionId;
			} else {
				description = userAdminId.toUpperCase() + " editó el email del usuario " + userAdmin.getUserAdminId().toUpperCase() + " de " + email + " a " + userAdmin.getEmail() + " y la elección autorizada a la de id " + authorizedElectionId;
			}
			persistActivity(userAdminId, ActivityType.EDIT_ADMIN, description, ip, authorizedElectionId);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Generates an excel file with the census of an election.
	 * 
	 * @param electionId Identifier of the election.
	 * 
	 * @return returns an excel file containing the information.
	 */
	@Override
	public File exportCensus(long electionId) {
		String fileName = "/padron_electoral_" + electionId + ".xlsx";
		return ExcelUtils.exportToExcel(getElectionUserVoters(electionId), fileName);
	}

	/**
	 * Updates the password of an admin user
	 * 
	 * @param userAdminToUpdateId Identifier of the user
	 * @param password            New passwrod to set
	 * @param userAdminId         Id of the user, used for logging purposes
	 * @param ip                  Ip of the user, used for logging purposes
	 */
	@Override
	public void editAdminUserPassword(String userAdminToUpdateId, String password, String userAdminId, String ip) {
		try {
			UserAdmin a = ElectionsDaoFactory.createUserAdminDao(em).getUserAdmin(userAdminToUpdateId);
			a.setPassword(password);

			em.persist(a);
			String description;
			if (userAdminId.equalsIgnoreCase(userAdminToUpdateId)) {
				description = userAdminId.toUpperCase() + " cambió su contraseña";
			} else {
				description = userAdminId.toUpperCase() + " cambió la contraseña de " + userAdminToUpdateId.toUpperCase();
			}
			persistActivity(userAdminId, ActivityType.EDIT_ADMIN, description, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Creates a new admin user
	 * 
	 * @param userAdmin   Entity with the information of the new user.
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public boolean addUserAdmin(UserAdmin userAdmin, String userAdminId, String ip) {
		try {
			if (ElectionsDaoFactory.createUserAdminDao(em).getUserAdmin(userAdmin.getUserAdminId()) == null) {
				UserAdmin userAdminAux = new UserAdmin();
				userAdminAux.setUserAdminId(userAdmin.getUserAdminId().toLowerCase());
				userAdminAux.setPassword(userAdmin.getPassword().toUpperCase());
				userAdminAux.setEmail(userAdmin.getEmail().toLowerCase());
				userAdminAux.setAuthorizedElectionId(userAdmin.getAuthorizedElectionId());
				em.persist(userAdminAux);
				String description = userAdminId.toUpperCase() + " agregó a " + userAdmin.getUserAdminId().toUpperCase() + " como admin";
				persistActivity(userAdminId, ActivityType.ADD_ADMIN, description, ip, userAdmin.getAuthorizedElectionId());
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	/**
	 * Creates a new candidate
	 * 
	 * @param electionId  Identifier of the election
	 * @param candidate   Entity with the information of the new candidate
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void addCandidate(long electionId, Candidate candidate, String userAdminId, String ip) {
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		candidate.setElection(election);
		int order = ElectionsDaoFactory.createCandidateDao(em).getLastNonFixedCandidateOrder(electionId);
		candidate.setCandidateOrder(order + 1);
		em.persist(candidate);
		election.setCandidatesSet(true);
		em.persist(election);
		String description = userAdminId.toUpperCase() + " agregó un candidato para la elección  " + election.getTitleSpanish();
		persistActivity(userAdminId, ActivityType.ADD_CANDIDATE, description, ip, election.getElectionId());
	}

	/**
	 * Deletes a candidate
	 * 
	 * @param candidateId Identifier of the candidate
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void removeCandidate(long candidateId, String userAdminId, String ip) {
		Candidate candidate = em.find(Candidate.class, candidateId);
		em.remove(candidate);
		String description = userAdminId.toUpperCase() + " eliminó al candidato " + candidate.getName() + TEXT_ELECCION + candidate.getElection().getTitleSpanish();
		persistActivity(userAdminId, ActivityType.REMOVE_CANDIDATE, description, ip, candidate.getElection().getElectionId());

		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(candidate.getElection().getElectionId());
		election.setCandidatesSet(election.getCandidates().size() > 1);
		em.persist(election);
	}

	/**
	 * Gets the information about a candidate
	 * 
	 * @param candidateId Identifier of the candidate
	 * 
	 * @return returns a candidate entity with the information
	 */
	@Override
	public Candidate getCandidate(long candidateId) {
		return ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
	}

	/**
	 * Updates the information of a candidate
	 * 
	 * @param candidateId Identifier of the candidate
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void editCandidate(Candidate candidate, String userAdminId, String ip) {
		try {
			em.merge(candidate);
			String description = userAdminId.toUpperCase() + " actualizó los datos de un candidato para la elección " + candidate.getElection().getTitleSpanish();
			persistActivity(userAdminId, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Creates a new auditor and associates it to an election
	 * 
	 * @param electionId    Identifier of the election
	 * @param auditor       Entity containing the auditor information
	 * @param electionTitle Title of the election where the auditor will be added,
	 *                      used for logging purposes
	 * @param userAdminId   Id of the user performing the action, used for logging
	 *                      purposes
	 * @param ip            Ip of the user, used for logging purposes
	 */
	@Override
	public void addAuditor(long electionId, Auditor auditor, String electionTitle, String userAdminId, String ip) {
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		auditor.setElection(election);
		em.persist(auditor);
		String description = userAdminId.toUpperCase() + " agregó un auditor para la elección " + electionTitle;
		persistActivity(userAdminId, ActivityType.ADD_AUDITOR, description, ip, electionId);
	}

	/**
	 * Deletes an auditor
	 * 
	 * @param auditorId   identifier of the auditor
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void removeAuditor(long auditorId, String userAdminId, String ip) {
		Auditor auditor = em.find(Auditor.class, auditorId);
		em.remove(auditor);
		String description = userAdminId.toUpperCase() + " eliminó al auditor " + auditor.getName() + TEXT_ELECCION + auditor.getElection().getTitleSpanish();
		persistActivity(userAdminId, ActivityType.REMOVE_AUDITOR, description, ip, auditor.getElection().getElectionId());
	}

	/**
	 * Gets information about an auditor
	 * 
	 * @param auditorId Identifier of the auditor
	 * 
	 * @return returns an auditor entity with the information.
	 */
	@Override
	public Auditor getAuditor(long auditorId) {
		return ElectionsDaoFactory.createAuditorDao(em).getAuditor(auditorId);
	}

	/**
	 * Updates an auditor information
	 * 
	 * @param auditor     Entity containing the auditor information
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void editAuditor(Auditor auditor, String userAdminId, String ip) {
		try {
			em.merge(auditor);
			String description = userAdminId.toUpperCase() + " actualizó los datos de un auditor para la elección " + auditor.getElection().getTitleSpanish();
			persistActivity(userAdminId, ActivityType.EDIT_AUDITOR, description, ip, auditor.getElection().getElectionId());
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Updates the election flag for set auditors
	 * 
	 * @param electionId    Identifier of the election
	 * @param electionTitle Title of the election, used for logging purposes
	 * @param userAdminId   Id of the user performing the action, used for logging
	 *                      purposes
	 * @param ip            Ip of the user, used for logging purposes
	 */
	@Override
	public void persistElectionAuditorsSet(long electionId, String electionTitle, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setAuditorsSet(true);
			em.persist(election);
			String description = userAdminId.toUpperCase() + " agregó auditores para la elección " + electionTitle;
			persistActivity(userAdminId, ActivityType.ADD_AUDITORS, description, ip, electionId);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Gets a list with all the parameters
	 * 
	 * @return returns a list of parameter entity with all the parameters on the
	 *         system.
	 */
	@Override
	public List<Parameter> getParametersAll() {
		return EJBFactory.getInstance().getElectionsParametersEJB().getParametersAll();
	}

	/**
	 * Get a list of all the disabled Ips
	 * 
	 * @return returns a list of ip access entity with the information.
	 */
	@Override
	public List<IpAccess> getAllDisabledIPs() {
		return ElectionsDaoFactory.createIpAccessDao(em).getAllDisabledIPs();
	}

	/**
	 * Add a new parameter to the system.
	 * 
	 * @param key         Parameter key.
	 * @param value       Value of the parameter.
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public boolean addParameter(String key, String value, String userAdminId, String ip) {
		if (EJBFactory.getInstance().getElectionsParametersEJB().addParameter(key, value)) {
			String description = userAdminId.toUpperCase() + " creó el parámetro " + key;
			persistActivity(userAdminId, ActivityType.ADD_PARAMETER, description, ip, null);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Edit the value of a parameter
	 * 
	 * @param parameter   entity with the parameter to update and the new value
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void editParameter(Parameter parameter, String userAdminId, String ip) {
		try {
			EJBFactory.getInstance().getElectionsParametersEJB().editParameter(parameter);
			String description = userAdminId.toUpperCase() + " actualizó el parámetro " + parameter.getKey();
			persistActivity(userAdminId, ActivityType.EDIT_PARAMETER, description, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Deletes a parameter from the system.
	 * 
	 * @param key         Key of the parameter to delete
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void removeParameter(String key, String userAdminId, String ip) {
		try {
			String description;
			EJBFactory.getInstance().getElectionsParametersEJB().deleteParameter(key);
			description = userAdminId.toUpperCase() + " eliminó el parámetro " + key + " del sistema";
			persistActivity(userAdminId, ActivityType.DELETE_PARAMETER, description, ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Get list of the recipients an email template. Depending on the template it
	 * can be a list of voters or auditors
	 * 
	 * @param electionEmailTemplate Entity of the email template with the
	 *                              information of the email template to get the
	 *                              recipients
	 * 
	 * @return returns a list, it may be of voters entity or auditors entity
	 *         depending on the email template.
	 */
	@Override
	public List getRecipientsByRecipientType(ElectionEmailTemplate electionEmailTemplate) throws Exception {
		RecipientType recipientType = electionEmailTemplate.getRecipientType();
		List<UserVoter> result;
		List<UserVoter> unifiedList = new ArrayList<>();
		boolean exists = false;

		if (recipientType.equals(RecipientType.VOTERS)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.VOTERS_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionCensusByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_MX)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionCensusByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersNotVotedYet(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_MX)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
		} else if (recipientType.equals(RecipientType.VOTERS_ALREADY_VOTED)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersVoted(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.VOTERS_ALREADY_VOTED_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersVotedByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_ALREADY_VOTED_MX)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersVotedByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_TWO_ELECTIONS)) {
			result = ElectionsDaoFactory.createUserVoterDao(em).getJointElectionUserVotersNotVotedYet(electionEmailTemplate.getElection().getElectionId());
			unifiedList = new ArrayList<>();
			exists = false;
			if (!result.isEmpty())
				unifiedList.add(result.get(0));
			for (UserVoter userVoter : result) {
				exists = false;
				for (UserVoter unifiedUserVoter : unifiedList) {
					if (unifiedUserVoter.getMail().equalsIgnoreCase(userVoter.getMail())) {
						exists = true;
						break;
					}
				}
				if (!exists)
					unifiedList.add(userVoter);
			}
			return unifiedList;
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_TWO_ELECTIONS_BR)) {
			result = ElectionsDaoFactory.createUserVoterDao(em).getJointElectionUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
			unifiedList = new ArrayList<>();
			exists = false;
			if (!result.isEmpty())
				unifiedList.add(result.get(0));
			for (UserVoter userVoter : result) {
				exists = false;
				for (UserVoter unifiedUserVoter : unifiedList) {
					if (unifiedUserVoter.getMail().equalsIgnoreCase(userVoter.getMail())) {
						exists = true;
						break;
					}
				}
				if (!exists)
					unifiedList.add(userVoter);
			}
			return unifiedList;
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_TWO_ELECTIONS_MX)) {
			result = ElectionsDaoFactory.createUserVoterDao(em).getJointElectionUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
			unifiedList = new ArrayList<>();
			exists = false;
			if (!result.isEmpty())
				unifiedList.add(result.get(0));
			for (UserVoter userVoter : result) {
				exists = false;
				for (UserVoter unifiedUserVoter : unifiedList) {
					if (unifiedUserVoter.getMail().equalsIgnoreCase(userVoter.getMail())) {
						exists = true;
						break;
					}
				}
				if (!exists)
					unifiedList.add(userVoter);
			}
			return unifiedList;
		} else if (recipientType.equals(RecipientType.AUDITORS)) {
			return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditors(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.AUDITORS_NOT_AGREED_CONFORMITY_YET)) {
			return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditorsNotAgreedConformity(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.AUDITORS_ALREADY_AGREED_CONFORMITY)) {
			return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditorsAgreedConformity(electionEmailTemplate.getElection().getElectionId());
		} else {
			throw new Exception("Should have never got here.");
		}
	}

	/**
	 * Creates email templates for all the elections. It iterates the election and
	 * for every election it iterated the templates and for each template it creates
	 * an email template linked to the election
	 * 
	 * @return returns the number of template created.
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Integer createMissingEmailTemplates() {
		int amount = 0;
		List<ElectionEmailTemplate> bases = getBaseEmailTemplates();
		List<Election> noTemplateElections = getElectionsAllOrderCreationDate();
		for (int j = 0; j < noTemplateElections.size(); j++) {
			Election electionJ = noTemplateElections.get(j);
			for (int i = 0; i < bases.size(); i++) {
				ElectionEmailTemplate templateI = bases.get(i);
				if (getEmailTemplate(templateI.getTemplateType(), electionJ.getElectionId()) == null) {
					em.persist(new ElectionEmailTemplate(electionJ, templateI));
					amount++;
				}
			}
		}
		return amount;
	}

	/**
	 * Sends emails to the list of voters/auditor using an email template, also
	 * creates the information on the mail table.
	 * 
	 * @param users                 List of voter or auditor entity with the
	 *                              recipients of the email
	 * @param electionEmailTemplate Entity with the information of the election and
	 *                              email template.
	 */
	@Override
	public void queueMassiveSending(List users, ElectionEmailTemplate electionEmailTemplate) {
		EJBFactory.getInstance().getMailsSendingEJB().queueMassiveSending(users, electionEmailTemplate);
	}

	/**
	 * Sets the maximum order to a candidate in order to appear first.
	 * 
	 * @param candidateId Identifier of the candidate.
	 */
	@Override
	public void fixCandidateToTop(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		Candidate currentFirstCandidate = ElectionsDaoFactory.createCandidateDao(em).getElectionFirstCandidate(candidate.getElection().getElectionId());
		if (currentFirstCandidate != null) {
			currentFirstCandidate.setCandidateOrder(candidate.getCandidateOrder());
		}
		candidate.setCandidateOrder(Constants.MAX_ORDER);
		em.persist(candidate);
		em.persist(currentFirstCandidate);
	}

	/**
	 * Set the order to a candidate to appear first on a non fixed election.
	 * 
	 * @param candidateId Identifier of the candidate.
	 */
	@Override
	public void fixCandidateToFirstNonFixed(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		int lastNonFixedOrder = ElectionsDaoFactory.createCandidateDao(em).getLastNonFixedCandidateOrder(candidate.getElection().getElectionId());
		candidate.setCandidateOrder(lastNonFixedOrder + 1);
		em.persist(candidate);
	}

	/**
	 * Set the minimum order to a candidate.
	 * 
	 * @param candidateId Identifier of the candidate.
	 * 
	 */
	@Override
	public void fixCandidateToBottom(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		Candidate currentLastCandidate = ElectionsDaoFactory.createCandidateDao(em).getElectionLastCandidate(candidate.getElection().getElectionId());
		if (currentLastCandidate != null) {
			currentLastCandidate.setCandidateOrder(candidate.getCandidateOrder());
		}
		candidate.setCandidateOrder(Constants.MIN_ORDER);
		em.persist(candidate);
		em.persist(currentLastCandidate);
	}

	/**
	 * Moves a candidate up in the order, by switching the order with the candidate
	 * immediate above.
	 * 
	 * @param candidateId Identifier of the candidate.
	 */
	@Override
	public void moveCandidateUp(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		Candidate nextAboveCandidate = ElectionsDaoFactory.createCandidateDao(em).getNextAboveCandidate(candidate.getElection().getElectionId(), candidate.getCandidateOrder());
		if (nextAboveCandidate != null && nextAboveCandidate.getCandidateOrder() != Constants.MAX_ORDER) {
			appLogger.info(candidate.getCandidateOrder() + " - " + nextAboveCandidate.getCandidateOrder());

			int aux = candidate.getCandidateOrder();
			candidate.setCandidateOrder(nextAboveCandidate.getCandidateOrder());
			nextAboveCandidate.setCandidateOrder(aux);
			em.persist(candidate);
			em.persist(nextAboveCandidate);
		}
	}

	/**
	 * Moves a candidate down in the order, by switching the order with the
	 * candidate immediate below.
	 * 
	 * @param candidateId Identifier of the candidate.
	 */
	@Override
	public void moveCandidateDown(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		Candidate nextBelowCandidate = ElectionsDaoFactory.createCandidateDao(em).getNextBelowCandidate(candidate.getElection().getElectionId(), candidate.getCandidateOrder());
		if (nextBelowCandidate != null && nextBelowCandidate.getCandidateOrder() != Constants.MIN_ORDER) {
			int aux = candidate.getCandidateOrder();
			appLogger.info(candidate.getCandidateOrder() + " - " + nextBelowCandidate.getCandidateOrder());

			candidate.setCandidateOrder(nextBelowCandidate.getCandidateOrder());
			nextBelowCandidate.setCandidateOrder(aux);
			em.persist(candidate);
			em.persist(nextBelowCandidate);
		}
	}

	/**
	 * Enables/Disables the random order attribute so as to order the candidates
	 * randomly or not
	 * 
	 * @param electionId Identifier of the election
	 * @param value      Boolean value to be set, true for the order to be random,
	 *                   false to leave the order defined.
	 */
	@Override
	public void setSortCandidatesRandomly(Long electionId, Boolean value) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setRandomOrderCandidates(value);
			em.persist(election);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Gets all the email that have not been sent.
	 * 
	 * @return returns a list of email entity containing the information
	 */
	@Override
	public List<Email> getPendingSendEmails() {
		return ElectionsDaoFactory.createEmailDao(em).getPendingSendEmails();
	}

	/**
	 * Gets all the email on the system.
	 * 
	 * @return returns a list of email entity containing the information
	 */
	@Override
	public List<Email> getEmailsAll() {
		return ElectionsDaoFactory.createEmailDao(em).getEmailsAll();
	}

	/**
	 * Gets all the email related to an election
	 * 
	 * @param electionId Identifier of the election
	 * 
	 * @return returns a list of email entity containing the information
	 */
	@Override
	public List<Email> getElectionEmails(Long electionId) {
		return ElectionsDaoFactory.createEmailDao(em).getElectionEmails(electionId);
	}

	/**
	 * Gets all the email related to an election that have not been sent.
	 * 
	 * @param electionId Identifier of the election
	 * 
	 * @return returns a list of email entity containing the information
	 */
	@Override
	public List<Email> getElectionPendingSendEmails(Long electionId) {
		return ElectionsDaoFactory.createEmailDao(em).getElectionPendingSendEmails(electionId);
	}

	/**
	 * Get a commissioner
	 * 
	 * @param commissionerId Identifier of the commissioner.
	 * 
	 * @return returns a commissioner entity with the information.
	 */
	@Override
	public Commissioner getCommissioner(long commissionerId) {
		return ElectionsDaoFactory.createCommissionerDao(em).getCommissioner(commissionerId);
	}

	/**
	 * Adds a commissioner to the system.
	 * 
	 * @param name        Name of the commissioner
	 * @param mail        Mail of the commissioner
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 * 
	 * @return returns true if the commissioner was correctly added, false if the
	 *         mail was null or an exception is thrown.
	 */
	@Override
	public boolean addCommissioner(String name, String mail, String userAdminId, String ip) {
		try {
			if (ElectionsDaoFactory.createCommissionerDao(em).getCommissionerByMail(mail) == null) {
				Commissioner a = new Commissioner();
				a.setName(name);
				a.setMail(mail);
				em.persist(a);
				String description = userAdminId.toUpperCase() + " agregó al comisionado " + name;
				persistActivity(userAdminId, ActivityType.ADD_COMMISSIONER, description, ip, null);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	/**
	 * Remove a commissioner from the system.
	 * 
	 * @param commissionerId Identifier of the commissioner.
	 * @param name           Name of the commissioner, used for logging purposes
	 * @param userAdminId    Id of the user performing the action, used for logging
	 *                       purposes
	 * @param ip             Ip of the user, used for logging purposes
	 */
	@Override
	public void removeCommissioner(long commissionerId, String name, String userAdminId, String ip) {
		Commissioner a = ElectionsDaoFactory.createCommissionerDao(em).getCommissioner(commissionerId);
		em.remove(a);
		String description = userAdminId.toUpperCase() + " eliminó al comisionado " + name + " del sistema";
		persistActivity(userAdminId, ActivityType.REMOVE_COMMISSIONER, description, ip, null);
	}

	/**
	 * Updated the information of a commissioner
	 * 
	 * @param commissioner Entity containing the information of the commissioner to
	 *                     be updated.
	 * @param userAdminId  Id of the user performing the action, used for logging
	 *                     purposes
	 * @param ip           Ip of the user, used for logging purposes
	 */
	@Override
	public void editCommissioner(Commissioner commissioner, String userAdminId, String ip) {
		try {
			em.merge(commissioner);
			persistActivity(userAdminId, ActivityType.EDIT_COMMISSIONER, userAdminId + " ha editado el comidionado " + commissioner.getName(), ip, null);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Gets the census example excel file.
	 * 
	 * @param filePath Path where to get the file.
	 * 
	 * @return returns the excel file.
	 */
	@Override
	public File exportCensusExample(String filePath) {
		return FilesUtils.getCensusExample(filePath);
	}

	/**
	 * Creates a set of email templates for an election, based on the default ones.
	 * 
	 * @param election Entity with the election information
	 */
	@Override
	public void createElectionEmailTemplates(Election election) {
		List<ElectionEmailTemplate> bases = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplates();
		for (int i = 0; i < bases.size(); i++) {
			ElectionEmailTemplate templateI = bases.get(i);
			if (getEmailTemplate(templateI.getTemplateType(), election.getElectionId()) == null) {
				em.persist(new ElectionEmailTemplate(election, templateI));
			}
		}
	}

	/**
	 * Resends a voter the election started and election created mails using the
	 * templates defined for the election.
	 * 
	 * @param userVoter   Entity with the voter information
	 * @param election    Entity with the election information
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void resendUserVoterElectionMail(UserVoter userVoter, Election election, String userAdminId, String ip) {
		ElectionEmailTemplate noticeTemplate = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(Constants.TemplateTypeELECTION_NOTICE, election.getElectionId());
		ElectionEmailTemplate startedTemplate = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(Constants.TemplateTypeELECTION_START, election.getElectionId());
		if (noticeTemplate != null || startedTemplate != null) {
			Date now = new Date();
			if (((now.after(election.getCreationDate())) && (now.before(election.getStartDate()))))
				EJBFactory.getInstance().getMailsSendingEJB().queueSingleSending(noticeTemplate, userVoter, null, election, new ArrayList<>());
			if ((election.isVotingLinkAvailable()) && ((now.after(election.getStartDate())) && (now.before(election.getEndDate()))))
				EJBFactory.getInstance().getMailsSendingEJB().queueSingleSending(startedTemplate, userVoter, null, election, new ArrayList<>());
			String description = userAdminId.toUpperCase() + " reénvio el email al usuario padrón " + userVoter.getName().toUpperCase() + " de la elección " + "(" + election.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.RESEND_EMAIL_ELECTION_USER_CENSUS, description, ip, election.getElectionId());
		}
	}

	/**
	 * Gets the identifier of the first election for which the user is authorized
	 * 
	 * @param userAdminId Identifier of the user.
	 * 
	 * @return returns the identifier of the election.
	 */
	@Override
	public long getUserAuthorizedElectionId(String userAdminId) {
		return ElectionsDaoFactory.createUserAdminDao(em).getUserAuthorizedElectionId(userAdminId);
	}

	/**
	 * Gets a parameter filtering by its key
	 * 
	 * @param key A string with the parameter key to search
	 * 
	 * @return returns a parameter entity with the information.
	 */
	@Override
	public Parameter getParameter(String key) {
		return ElectionsDaoFactory.createParameterDao(em).getParameter(key);
	}

	/**
	 * Enables/Disables the revision flag for an election, also, if the revision is
	 * disabled, it updates the elections auditor to disable the revision.
	 * 
	 * @param electionId  Identifier of the election
	 * @param status      The value of the revision status, true - enabled, false -
	 *                    disabled
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void requestElectionRevision(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			setAuditLinkStatus(electionId, status, userAdminId, ip);

			if (!(Boolean.TRUE.equals(status))) {
				List<Auditor> auditors = election.getAuditors();
				for (Auditor auditor : auditors) {
					auditor.setRevisionAvailable(false);
					em.persist(auditor);
					String description = userAdminId.toUpperCase() + " cerró el proceso de revisión y se revocó la autorizacion de la revisión del auditor: " + auditor.getAuditorId() + " - " + auditor.getName() + TEXT_ELECCION + "(" + election.getTitleSpanish() + ")";
					persistActivity(userAdminId, ActivityType.ELECTION_REVISION_NO, description, ip, election.getElectionId());
				}
			}
			election.setRevisionRequest(status);
			em.persist(election);
			String strValor = " revocó la solicitud de ";
			if (Boolean.TRUE.equals(status)) {
				strValor = " solicitó la ";
			}
			String description = userAdminId.toUpperCase() + strValor + "revisión para la elección " + "(" + election.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.ELECTION_REVISION, description, ip, election.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	/**
	 * Gets all the votes of an election.
	 * 
	 * @param electionId Identifier of the election
	 * 
	 * @return returns a collection of vote entity with the information.
	 */
	@Override
	public List<Vote> getElectionVotes(Long electionId) {
		return ElectionsDaoFactory.createVoteDao(em).getElectionVotes(electionId);
	}

	/**
	 * Validates if an election has the revision active
	 * 
	 * @param electionId  Identifier of the election
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 * 
	 * @return returns true if the election has the revision active and all the
	 *         auditors too, false otherwise.
	 */
	@Override
	public boolean isRevisionActive(long electionId, String userAdminId, String ip) {
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		List<Auditor> auditors = election.getAuditors();
		if (!election.isRevisionRequest())
			return false;

		for (Auditor auditor : auditors) {
			if (auditor.isCommissioner() && !auditor.isRevisionAvailable())
				return false;
		}

		String description = userAdminId.toUpperCase() + " ingresó a la revisión de votos para la elección " + "(" + election.getTitleSpanish() + ")";
		persistActivity(userAdminId, ActivityType.ENTER_TO_REVISION, description, ip, election.getElectionId());
		return true;
	}

	/**
	 * Returns the candidate orderer immediately above the parameter
	 * 
	 * @param candidate Entity with the candidate from which the immediately above
	 *                  will be looked.
	 * 
	 * @return returns a candidate entity with the information with the candidate
	 *         ordered immediately above to the one passed, null if there's none.
	 */
	@Override
	public Candidate getNextAboveCandidate(Candidate candidate) {
		return ElectionsDaoFactory.createCandidateDao(em).getNextAboveCandidate(candidate.getElection().getElectionId(), candidate.getCandidateOrder());
	}

	/**
	 * Returns the candidate orderer immediately below the parameter
	 * 
	 * @param candidate Entity with the candidate from which the immediately below
	 *                  will be looked.
	 * 
	 * @return returns a candidate entity with the information with the candidate
	 *         ordered immediately below to the one passed, null if there's none.
	 */
	@Override
	public Candidate getNextBelowCandidate(Candidate candidate) {
		return ElectionsDaoFactory.createCandidateDao(em).getNextBelowCandidate(candidate.getElection().getElectionId(), candidate.getCandidateOrder());
	}

	/**
	 * Validates if a commissioner exists.
	 * 
	 * @param name Name of the searched commissioner
	 * @param mail Mail of the searched commissioner
	 * 
	 * @return returns true if a commissioner with the name and mail exists, false
	 *         if not
	 */
	@Override
	public boolean commissionerExists(String name, String mail) {
		return ElectionsDaoFactory.createCommissionerDao(em).commissionerExists(name, mail);
	}

	/**
	 * Validates if an auditor exists on an election
	 * 
	 * @param electionId Identifier of the election
	 * @param name       Name of the searched auditor
	 * @param mail       Mail of the searched auditor
	 * 
	 * @return returns true if there is an auditor with the name and mail associated
	 *         to the election, false if not.
	 */
	@Override
	public boolean auditorExists(long electionId, String name, String mail) {
		return ElectionsDaoFactory.createAuditorDao(em).auditorExists(electionId, name, mail);
	}

	/**
	 * Gets the default sender defined in the parameter DEFAULT_SENDER constant.
	 * 
	 * @return returns a string with the parameter value
	 */
	@Override
	public String getDefaultSender() {
		return EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.DEFAULT_SENDER);
	}

	/**
	 * Gets the default website defined in the parameter WEBSITE_DEFAULT constant.
	 * 
	 * @return returns a string with the parameter value
	 */
	@Override
	public String getDefaultWebsite() {
		return EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.WEBSITE_DEFAULT);
	}

	/**
	 * Adds or updates a base email template
	 * 
	 * @param electionEmailTemplate Entity with the email template
	 * @param userAdminId           Id of the user performing the action, used for
	 *                              logging purposes
	 * @param ip                    Ip of the user, used for logging purposes
	 * 
	 * @return returns true if la operation succeeds or false if there's an error
	 */
	@Override
	public boolean createBaseEmailTemplate(ElectionEmailTemplate electionEmailTemplate, String userAdminId, String ip) {
		try {
			electionEmailTemplate.setTemplateType(electionEmailTemplate.getTemplateType().toUpperCase());
			em.persist(electionEmailTemplate);
			String description = userAdminId.toUpperCase() + " creó un nuevo template base " + electionEmailTemplate.getTemplateType();
			persistActivity(userAdminId, ActivityType.ADD_BASE_TEMPLATE, description, ip, null);
			return true;
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	/**
	 * Get a list of the the joint elections on the system.
	 * 
	 * @return returns a collection of joint election entity containing the
	 *         information.
	 */
	@Override
	public List<JointElection> getJointElectionsAll() {
		return ElectionsDaoFactory.createElectionDao(em).getJointElectionsAll();
	}

	/**
	 * Gets a joint election information, searching by a election id
	 * 
	 * @param electionId Identifier of an election searched
	 * 
	 * @return returns a joint election entity on which one of the elections is the
	 *         one searched by or null if it does not find it.
	 */
	@Override
	public JointElection getJointElectionForElection(long electionId) {
		return ElectionsDaoFactory.createElectionDao(em).getJointElectionForElection(electionId);
	}

	/**
	 * Creates or update (an existing) joint election.
	 * 
	 * @param jointElection Entity containing the joint election information to be
	 *                      add/updated
	 */
	@Override
	public void updateJointElection(JointElection jointElection) {
		em.merge(jointElection);
	}

	/**
	 * Deletes a joint election from the system.
	 * 
	 * @param jointElection An entity containing the joint election information
	 * 
	 */
	@Override
	public void removeJointElection(JointElection jointElection) {
		em.remove(em.contains(jointElection) ? jointElection : em.merge(jointElection));
	}

	/**
	 * Validates if an election is involved in a joint election process
	 * 
	 * @param electionId Identifier of the election searched.
	 * 
	 * @return returns true if the election searched is joint with another, false if
	 *         not.
	 */
	@Override
	public boolean isJointElection(long electionId) {
		return !ElectionsDaoFactory.createElectionDao(em).electionIsSimple(electionId);
	}

	/**
	 * Validates if the census of both elections in a joint election contains the
	 * same voters
	 * 
	 * @param jointElection Entity with the joint election information (contains two
	 *                      elections)
	 * 
	 * @return returns true if both census are the same, false if they differ.
	 */
	@Override
	public boolean electionsCensusEqual(JointElection jointElection) {
		return ElectionsDaoFactory.createUserVoterDao(em).electionsCensusEqual(jointElection.getIdElectionA(), jointElection.getIdElectionB());
	}

	/**
	 * Gets a list with the identifier and title of all the system's elections
	 * 
	 * @return reutns a collection of string, on each string containing the id and
	 *         title of the elections.
	 */
	@Override
	public List<String> getElectionsAllIdAndTitle() {
		List<Object[]> electionsIdTitleList = ElectionsDaoFactory.createElectionDao(em).getElectionsAllIdAndTitle();
		List<String> resultList = new ArrayList<>();

		for (int i = 0; i < electionsIdTitleList.size(); i++) {
			resultList.add(electionsIdTitleList.get(i)[0].toString() + "-" + electionsIdTitleList.get(i)[1].toString());
		}
		return resultList;
	}

	/**
	 * Gets the customization of the system.
	 * 
	 * @return returns a entity containing all the customizacion information.
	 */
	@Override
	public Customization getCustomization() {
		return ElectionsDaoFactory.createCustomizationDao(em).getCustomization();
	}

	/**
	 * Update the customization of the system.
	 * 
	 * @param customization Entity containing all the customization information.
	 * 
	 * @return returns true if the update was successfull, false if not.
	 */
	@Override
	public boolean updateCustomization(Customization customization) {
		try {
			em.merge(customization);
			return true;
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	/**
	 * Saves an Activity log entry
	 * 
	 * @param userAdminId  Identifier of the user who performs the activity
	 * @param activityType Enumerate containing the type of the activity
	 * @param description  A string with the description of the activity
	 * @param ip           A string with the ip address of the user performing the
	 *                     activity
	 * @param electionId   Identifier of the election.
	 */
	@Override
	public void persistActivity(String userAdminId, ActivityType activityType, String description, String ip, Long electionId) {
		Activity activity = new Activity();
		activity.setIp(ip);
		activity.setElectionId(electionId);
		activity.setTimestamp(new Date());
		activity.setUserName(userAdminId);
		activity.setActivityType(activityType);
		activity.setDescription(description);
		em.persist(activity);
	}

	/**
	 * Returns the data site key
	 */
	@Override
	public String getDataSiteKey() {
		try {
			return EJBFactory.getInstance().getElectionsParametersEJB().getDataSiteKey();
		} catch (Exception e) {
			appLogger.error(e);
			return "";
		}
	}

	/**
	 * Validates if an election can be closed
	 * 
	 * @param electionId Identifier of the election.
	 * 
	 * @return returns true if the election can be closed
	 */
	@Override
	public boolean electionCanBeClosed(long electionId) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			boolean closed = election.isClosed();
			Date endDate = election.getEndDate();
			Date currentDate = DateTime.now().toDate();

			if ((!closed) && (endDate.before(currentDate))) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	/**
	 * Closes an election
	 * 
	 * @param electionId  Identifier of the election searched.
	 * 
	 * @param userAdminId Id of the user performing the action, used for logging
	 *                    purposes
	 * @param ip          Ip of the user, used for logging purposes
	 * 
	 * @return returns true if the operation succeeds or false if there's an error
	 * 
	 */
	@Override
	public boolean closeElection(long electionId, String userAdminId, String ip) {
		try {
			// Set election as closed and date of closure
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setClosed(true);
			election.setClosedDate(new Date());

			// Disable voting link
			election.setVotingLinkAvailable(false);

			// Remove the voter info from votes
			List<Vote> votes = election.getVotes();
			for (Vote vote : votes) {
				vote.setUserVoter(null);
				em.persist(vote);
			}
			em.persist(election);

			persistActivity(userAdminId, ActivityType.CLOSE_ELECTION, userAdminId + " ha cerrado la elección " + electionId, ip, null);
			return true;
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	@Override
	public UserAdmin login(String username, String password, String ip) {
		try {

			LoginData dataLDAP = UtilsLogin.login(username, password);
			if (dataLDAP.getAuthenticated() && dataLDAP.getRoles().contains(Constants.elections_admin)) {
				String description = username.toUpperCase() + " se ha logueado exitosamente";
				EJBFactory.getInstance().getElectionsManagerEJB().persistActivity(username, ActivityType.LOGIN_SUCCESSFUL, description, ip, null);

				return crearActualizarAdminUser(dataLDAP);
			} else {
				String description = "Intento fallido de login de usuario " + username.toUpperCase();
				EJBFactory.getInstance().getElectionsManagerEJB().persistActivity(username, ActivityType.LOGIN_FAILED, description, ip, null);
			}
			return null;
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	private UserAdmin crearActualizarAdminUser(LoginData paiUser) {
		try {
			UserAdmin userAdmin = ElectionsDaoFactory.createUserAdminDao(em).getUserAdmin(paiUser.getUsername());

			if (userAdmin != null) {
				boolean isUpdated = userAdmin.getUserAdminId().equals(userAdmin.getEmail());
				if (isUpdated) {
					return userAdmin;
				} else {
					userAdmin.setEmail(paiUser.getUsername());
					em.merge(userAdmin);
				}

			} else {
				UserAdmin updatedUserAdmin = new UserAdmin();
				updatedUserAdmin.setUserAdminId(paiUser.getUsername());
				updatedUserAdmin.setEmail(paiUser.getUsername());
				updatedUserAdmin.setPassword("-creado en pai.lacnic.net-");
				updatedUserAdmin.setAuthorizedElectionId(0L);
				em.persist(updatedUserAdmin);
				return updatedUserAdmin;
			}

		} catch (Exception e) {
			appLogger.error(e);
		}
		return null;
	}

	@Override
	public String getAuditReportURL() {
		return getParameter("AUDIT_REPORT_LINK").getValue();
	}

}
