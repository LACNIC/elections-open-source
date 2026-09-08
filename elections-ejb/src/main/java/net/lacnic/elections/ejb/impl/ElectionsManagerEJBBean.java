package net.lacnic.elections.ejb.impl;

import static net.lacnic.elections.utils.Constants.CENSUS_SYNC_MESSAGE_PREFIX;
import static net.lacnic.elections.utils.Constants.SYNC_AUDIT_FIELD_CENSUS;
import static net.lacnic.elections.utils.Constants.SYNC_AUDIT_FIELD_ORGANIZATION;
import static net.lacnic.elections.utils.Constants.SYNC_STATUS_ERROR;
import static net.lacnic.elections.utils.Constants.SYNC_STATUS_NO_DATA;
import static net.lacnic.elections.utils.Constants.SYNC_STATUS_SUCCESS;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ejb.Asynchronous;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.campus.CampusClient;
import net.lacnic.elections.dao.CandidateElectionTaskProgressDao;
import net.lacnic.elections.dao.ElectionCalendarDao;
import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.dao.QueryParameterNames;
import net.lacnic.elections.dao.SupportNominationDao;
import net.lacnic.elections.dao.UserVoterDao;
import net.lacnic.elections.data.AdminLoginResult;
import net.lacnic.elections.data.AsyncProcessingError;
import net.lacnic.elections.data.AsyncProcessingProgress;
import net.lacnic.elections.data.AsyncProcessingType;
import net.lacnic.elections.data.CandidateBioMigrationBatchResult;
import net.lacnic.elections.data.CandidatePhotoBatchResult;
import net.lacnic.elections.data.CensusUpsertResult;
import net.lacnic.elections.data.MilacnicFetchResult;
import net.lacnic.elections.data.MilacnicOrganizationRecord;
import net.lacnic.elections.data.MilacnicSyncResult;
import net.lacnic.elections.data.OrganizationBulkImportResult;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.CandidateType;
import net.lacnic.elections.domain.Commissioner;
import net.lacnic.elections.domain.Customization;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionAuditorResult;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.RecipientType;
import net.lacnic.elections.domain.ReminderFrequency;
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.UserVoterLite;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStage;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateCampusCourseStatus;
import net.lacnic.elections.domain.pre.CandidateCountryLink;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateEvaluationStatus;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import net.lacnic.elections.domain.pre.CandidateQuestionOwner;
import net.lacnic.elections.domain.pre.CandidateQuestionStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.CandidateWorkOrganization;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.ElectionPresetConfigurations;
import net.lacnic.elections.domain.pre.ElectionPresetConfigurations.ElectionPresetConfiguration;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;
import net.lacnic.elections.domain.pre.SyncAudit;
import net.lacnic.elections.domain.pre.SyncRun;
import net.lacnic.elections.domain.pre.TaskDependencyLevel;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateCountryLinkRow;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateWorkOrganizationRow;
import net.lacnic.elections.domain.services.publicelection.PublicElectionVoteCountRow;
import net.lacnic.elections.domain.services.detail.OrganizationDebtorImportResult;
import net.lacnic.elections.ejb.ElectionsManagerEJB;
import net.lacnic.elections.exception.CensusValidationException;
import net.lacnic.elections.utils.AuditorCandidateDecisionUtils;
import net.lacnic.elections.utils.AuditorCandidateDecisionWindow;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.CriticalOperationsLoggerUtils;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.ElectionsCaches;
import net.lacnic.elections.utils.ElectionsRoles;
import net.lacnic.elections.utils.EmailTemplateType;
import net.lacnic.elections.utils.ExcelUtils;
import net.lacnic.elections.utils.ExcelUtils.OrganizationsUpsertExcelData;
import net.lacnic.elections.utils.FilesUtils;
import net.lacnic.elections.utils.HtmlSanitizerUtils;
import net.lacnic.elections.utils.LinksUtils;
import net.lacnic.elections.utils.PublicPhotoResizeProcessor;
import net.lacnic.elections.utils.ReminderCalendarKeys;
import net.lacnic.elections.utils.StringUtils;
import net.lacnic.evra.registro.CategoriasEnum;
import net.lacnic.elections.utils.VotingPeriodResolver;
import net.lacnic.portal.auth.client.LoginData;
import net.lacnic.portal.auth.client.UtilsLogin;

@Stateless
@Remote(ElectionsManagerEJB.class)
public class ElectionsManagerEJBBean implements ElectionsManagerEJB {

	private static final String TEXT_ELECCION = " para la elección ";
	private static final String SYSTEM_ACTOR = "SYSTEM";
	private static final String SYSTEM_CRON_ACTOR = "SYSTEM_CRON";
	private static final String SYSTEM_MANUAL_ACTOR = "SYSTEM_MANUAL";
	private static final String KEY_CENSUS_ASYNC_PROCESSING_ERROR = "censusManagementAsyncProcessingError";
	private static final String KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR = "organizationsManagementAsyncProcessingError";
	private static final String ACTIVITY_CANDIDATE_ID_FRAGMENT = ", candidateId=";
	private static final String ACTIVITY_UPDATED_ROWS_FRAGMENT = ", actualizadas=";
	private static final String ACTIVITY_NOMINATION_ID_FRAGMENT = ", nominationId=";
	private static final String ACTIVITY_NOMINATION_EMAIL_FRAGMENT = ", nominationEmail=";
	private static final String MILACNIC_ENDPOINT_RESOLUTION_ERROR = "Could not resolve MiLACNIC WS endpoint";
	private static final String LOG_CENSUS_PROCESSING_FLAG_RELEASED = "Census processing flag released at async end. electionId={}, context={}";
	private static final String LOG_ORGANIZATIONS_PROCESSING_FLAG_RELEASED = "Organizations processing flag released at async end. electionId={}, context={}";
	private static final String MILACNIC_WS_RETURNED_PREFIX = "MiLACNIC WS returned ";
	private static final String JSON_FIELD_ORGANIZATIONS = "organizations";
	private static final String ACTIVITY_SOURCE_ADMIN_PANEL = "ADMIN_PANEL";
	private static final String ACTIVITY_SOURCE_CANDIDATE_LINK = "CANDIDATE_LINK";
	private static final String CANDIDATE_ACTIVITY_PREFIX = "CANDIDATE_";
	private static final int CENSUS_UPLOAD_PROGRESS_LOG_EVERY_ROWS = 1000;
	private static final int CENSUS_DELETE_BATCH_SIZE = 2000;
	private static final int CANDIDATE_PHOTO_OPTIMIZATION_BATCH_SIZE = 50;
	private static final int CANDIDATE_BIO_MIGRATION_BATCH_SIZE = 100;
	private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");
	private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&(?:[a-zA-Z]{2,10}|#\\d{1,7}|#x[0-9a-fA-F]{1,6});");
	private static final Pattern HTML_SCRIPT_STYLE_PATTERN = Pattern.compile("(?is)<(script|style)\\b[^>]*>.*?</\\1>");
	private static final Pattern HTML_BREAK_PATTERN = Pattern.compile("(?is)<br\\s*/?>");
	private static final Pattern HTML_LIST_ITEM_PATTERN = Pattern.compile("(?is)<li\\b[^>]*>");
	private static final Pattern HTML_BLOCK_PATTERN = Pattern.compile("(?is)</?(p|div|section|article|header|footer|aside|blockquote|pre|ul|ol|table|tr|td|th|h[1-6])\\b[^>]*>");
	private static final Pattern HTML_CLOSING_LI_PATTERN = Pattern.compile("(?is)</li\\b[^>]*>");
	private static final Pattern HTML_NUMERIC_ENTITY_PATTERN = Pattern.compile("&#(x?[0-9a-fA-F]+);");
	private static final Set<ElectionCalendarKey> REMINDER_CRON_ALLOWED_CALENDAR_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES)));
	private static final Set<ElectionCalendarKey> CANDIDATE_REMINDER_EVALUATION_EXTENSION_CALENDAR_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ElectionCalendarKey.N_6_PERIODO_ADDITIONAL_EVALUATION)));
	private static final String CAMPUS_PROGRESS_CHECK_TRACKING_PREFIX = "CAMPUS_PROGRESS_CHECK";
	private static final int DEFAULT_CAMPUS_PROGRESS_CHECK_MAX_ATTEMPTS = 30;
	private static final int DEFAULT_LOGIN_CAPTCHA_MAX_ATTEMPTS = 5;
	private static final String SYNC_AUDIT_FIELD_DEUDOR = "DEUDOR";
	private static final Random RANDOM = new SecureRandom();
	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	public ElectionsManagerEJBBean() {
		// Intencionalmente vacio: el contenedor EJB inyecta dependencias y gestiona el ciclo de vida.
	}

	private ElectionsManagerEJB getManagerProxy() {
		return EJBFactory.getInstance().getElectionsManagerEJB();
	}

	/**
	 * Logs an user to the application and persists audit information in the activity table
	 *
	 * @param userAdminId Login id
	 * @param password    User password
	 * @param ip          Ip of the user login in
	 *
	 * @return returns a UserAdmin entity if the id and password exists and null otherwise.
	 */
	@Override
	public UserAdmin userAdminLogin(String userAdminId, String password, String ip) {
		UserAdmin a = ElectionsDaoFactory.createUserAdminDao(em).verifyUserLogin(userAdminId, password);
		try {
			if (a != null) {
				String description = userAdminId.toUpperCase() + " se ha logueado exitosamente";
				getManagerProxy().persistActivity(userAdminId, ActivityType.LOGIN_SUCCESSFUL, description, ip, null);
			} else {
				String description = "Intento fallido de login de usuario " + userAdminId.toUpperCase();
				appLogger.warn("Failed APP login attempt. user={}, ip={}", userAdminId, ip);
				ElectionsCaches.incrementLoginCaptchaAttempt(ip);
				getManagerProxy().persistActivity(userAdminId, ActivityType.LOGIN_FAILED, description, ip, null);
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
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
	 * @return returns an UserAdmin entity containing the information of the user id if found or null if it does not exists
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

		if (reCaptchaResponse == null || reCaptchaResponse.isBlank()) {
			appLogger.warn("reCAPTCHA response is null or empty");
			return false;
		}

		String secretKey = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.SkGoogleApiReCaptcha);

		if (secretKey == null || secretKey.isBlank()) {
			appLogger.error("reCAPTCHA secret key is not configured");
			return false;
		}

		String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			HttpPost post = new HttpPost(verifyUrl);
			post.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0");
			post.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

			List<NameValuePair> params = List.of(new BasicNameValuePair("secret", secretKey), new BasicNameValuePair("response", reCaptchaResponse));

			post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

			try (CloseableHttpResponse response = httpClient.execute(post); InputStream is = response.getEntity().getContent(); JsonReader jsonReader = Json.createReader(is)) {

				int statusCode = response.getCode();
				appLogger.info("reCAPTCHA HTTP response code: {}", statusCode);

				if (statusCode != HttpStatus.SC_OK) {
					appLogger.warn("Non-OK response from reCAPTCHA: {}", statusCode);
					return false;
				}

				JsonObject jsonObject = jsonReader.readObject();
				boolean success = jsonObject.getBoolean("success", false);

				appLogger.info("reCAPTCHA validation result: {}", success);
				return success;
			}

		} catch (Exception e) {
			appLogger.error("Error validating reCAPTCHA", e);
			return false;
		}
	}

	/**
	 * Validates if CAPTCHA is enabled and its keys are configured.
	 *
	 * @return returns true if captcha should be shown
	 */
	@Override
	public boolean isShowCaptcha() {
		try {
			return isLoginCaptchaEnabled() && !getDataSiteKey().isEmpty() && !getSkGoogleApiReCaptcha().isEmpty();
		} catch (Exception e) {
			appLogger.error(e.getMessage());
			return false;
		}
	}

	@Override
	public boolean shouldShowLoginCaptcha(String userName, String ip) {
		if (!isShowCaptcha() || ip == null || ip.isBlank()) {
			return false;
		}

		int failedAttempts = ElectionsCaches.getLoginCaptchaAttempts(ip);
		int maxAttempts = resolveLoginCaptchaMaxAttempts();
		appLogger.debug("Login captcha check. user={}, ip={}, failedAttempts={}, maxAttempts={}", userName, ip, failedAttempts, maxAttempts);
		return failedAttempts >= maxAttempts;
	}

	@Override
	public boolean sendLinkRecoveryEmail(long electionId, String recipientEmail, String linkTypeKey, String linkUrl, String userAdminId, String ip) {
		boolean queued = EJBFactory.getInstance().getMailsSendingEJB().queueLinkRecoveryEmail(electionId, recipientEmail, linkTypeKey, linkUrl);
		if (queued) {
			String actor = userAdminId != null ? userAdminId : SYSTEM_ACTOR;
			String description = actor.toUpperCase() + " solicitó recuperación de link " + linkTypeKey + TEXT_ELECCION + electionId;
			persistActivity(actor, ActivityType.EDIT_ELECTION, description, ip, electionId);
		}
		return queued;
	}

	private String getSkGoogleApiReCaptcha() {
		return EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.SkGoogleApiReCaptcha);
	}

	private boolean isLoginCaptchaEnabled() {
		String value = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.LOGIN_CAPTCHA_ENABLED);
		if (value == null || value.isBlank()) {
			return true;
		}
		if (value.equalsIgnoreCase(Boolean.TRUE.toString())) {
			return true;
		}
		if (value.equalsIgnoreCase(Boolean.FALSE.toString())) {
			return false;
		}
		appLogger.warn("Invalid {} value '{}'. Using secure default: true", Constants.LOGIN_CAPTCHA_ENABLED, value);
		return true;
	}

	private int resolveLoginCaptchaMaxAttempts() {
		try {
			String value = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.LOGIN_CAPTCHA_MAX_ATTEMPTS);
			if (value == null || value.isBlank()) {
				return DEFAULT_LOGIN_CAPTCHA_MAX_ATTEMPTS;
			}
			int parsed = Integer.parseInt(value.trim());
			return parsed > 0 ? parsed : DEFAULT_LOGIN_CAPTCHA_MAX_ATTEMPTS;
		} catch (Exception e) {
			appLogger.warn("Unable to resolve login captcha max attempts. Using default {}", DEFAULT_LOGIN_CAPTCHA_MAX_ATTEMPTS, e);
			return DEFAULT_LOGIN_CAPTCHA_MAX_ATTEMPTS;
		}
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
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		if (election != null && !hasText(election.getPublicElectionToken())) {
			election.setPublicElectionToken(StringUtils.createSecureToken());
			em.merge(election);
		}
		VotingPeriodResolver.applyVotingWindow(em, election);
		return election;
	}

	@Override
	public ElectionAuditorResult getElectionAuditorResult(long electionId) {
		if (electionId <= 0L) {
			return null;
		}
		return ElectionsDaoFactory.createElectionAuditorResultDao(em).getElectionAuditorResult(electionId);
	}

	@Override
	public Election getElectionWithRestrictedCountries(long electionId) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			if (election.getRestrictedCountries() != null) {
				election.getRestrictedCountries().size();
			}
			VotingPeriodResolver.applyVotingWindow(em, election);
			return election;
		} catch (NoResultException e) {
			throw new IllegalArgumentException("Election not found for electionId=" + electionId, e);
		}
	}

	@Override
	public Election getElectionByQuestionToken(String questionToken) {
		if (!hasText(questionToken)) {
			return null;
		}
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElectionByQuestionToken(questionToken);
			VotingPeriodResolver.applyVotingWindow(em, election);
			return election;
		} catch (NoResultException e) {
			return null;
		}
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
					result = Integer.valueOf(RANDOM.nextInt(30)).compareTo(Integer.valueOf(RANDOM.nextInt(30)));
				} else
					result = (Integer.valueOf(candidate2.getCandidateOrder()).compareTo(Integer.valueOf(candidate1.getCandidateOrder())));
				return result;
			}
		});
		return candidates;
	}

	@Override
	public List<Candidate> getElectionBallotCandidates(long electionId) {
		Election election = getElection(electionId);
		List<Candidate> orderedPublishedCandidates = new ArrayList<>();
		Candidate abstentionCandidate = null;
		int abstentionIndex = -1;
		for (Candidate candidate : ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(electionId)) {
			if (candidate == null || candidate.getStatus() != CandidateStatus.CONFIRMED_AND_PUBLISHED) {
				continue;
			}
			if (candidate.isAbstention()) {
				abstentionCandidate = candidate;
				abstentionIndex = orderedPublishedCandidates.size();
				continue;
			}
			orderedPublishedCandidates.add(candidate);
		}

		if (election == null || !election.isRandomOrderCandidates()) {
			if (abstentionCandidate != null) {
				int targetIndex = Math.min(Math.max(abstentionIndex, 0), orderedPublishedCandidates.size());
				orderedPublishedCandidates.add(targetIndex, abstentionCandidate);
			}
			return orderedPublishedCandidates;
		}

		Collections.shuffle(orderedPublishedCandidates, RANDOM);
		if (abstentionCandidate != null) {
			int targetIndex = Math.min(Math.max(abstentionIndex, 0), orderedPublishedCandidates.size());
			orderedPublishedCandidates.add(targetIndex, abstentionCandidate);
		}
		return orderedPublishedCandidates;
	}

	@Override
	public List<Candidate> getElectionPublishedCandidates(long electionId) {
		return ElectionsDaoFactory.createCandidateDao(em).getElectionPublishedCandidates(electionId);
	}

	@Override
	public Candidate getElectionAbstentionCandidate(long electionId) {
		return ElectionsDaoFactory.createCandidateDao(em).getElectionAbstentionCandidate(electionId);
	}

	@Override
	public List<CandidateQuestion> getElectionCandidateQuestions(long electionId) {
		return ElectionsDaoFactory.createCandidateQuestionDao(em).getElectionCandidateQuestions(electionId);
	}

	@Override
	public List<CandidateQuestion> getElectionCandidateQuestionsForPublicElectionPage(long electionId) {
		return ElectionsDaoFactory.createCandidateQuestionDao(em).getElectionCandidateQuestionsForPublicElectionPage(electionId);
	}

	@Override
	public CandidateQuestion getCandidateQuestion(long candidateQuestionId) {
		try {
			return ElectionsDaoFactory.createCandidateQuestionDao(em).getCandidateQuestion(candidateQuestionId);
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public CandidateQuestion saveCandidateQuestion(CandidateQuestion candidateQuestionData, String userAdminId, String ip) {
		return saveCandidateQuestion(candidateQuestionData, true, userAdminId, ip);
	}

	@Override
	public CandidateQuestion saveCandidateQuestion(CandidateQuestion candidateQuestionData, boolean sendStatusNotificationEmail, String userAdminId, String ip) {
		if (candidateQuestionData == null || candidateQuestionData.getCandidate() == null || candidateQuestionData.getElection() == null) {
			return null;
		}
		long electionId = candidateQuestionData.getElection().getElectionId();
		long candidateId = candidateQuestionData.getCandidate().getCandidateId();
		if (electionId <= 0 || candidateId <= 0) {
			return null;
		}

		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
			if (election == null || candidate == null || candidate.isAbstention() || candidate.getElection() == null || candidate.getElection().getElectionId() != election.getElectionId()) {
				return null;
			}

			CandidateQuestion managedQuestion;
			boolean isNewQuestion = candidateQuestionData.getCandidateQuestionId() <= 0;
			CandidateQuestionStatus previousStatus = null;
			if (isNewQuestion) {
				managedQuestion = new CandidateQuestion();
				managedQuestion.setCreationDate(new Date());
			} else {
				managedQuestion = ElectionsDaoFactory.createCandidateQuestionDao(em).getCandidateQuestion(candidateQuestionData.getCandidateQuestionId());
				if (managedQuestion == null || managedQuestion.getElection() == null || managedQuestion.getElection().getElectionId() != electionId) {
					return null;
				}
				previousStatus = managedQuestion.getStatus();
			}

			managedQuestion.setElection(election);
			managedQuestion.setCandidate(candidate);
			managedQuestion.setAskedByName(normalizeShortText(candidateQuestionData.getAskedByName(), 500));
			managedQuestion.setAskedByEmail(normalizeEmailText(candidateQuestionData.getAskedByEmail(), 320));
			managedQuestion.setQuestionLanguage(candidateQuestionData.getQuestionLanguage() != null ? candidateQuestionData.getQuestionLanguage() : LanguageCode.SP);
			managedQuestion.setQuestionSpanish(normalizeQuestionText(candidateQuestionData.getQuestionSpanish()));
			managedQuestion.setQuestionEnglish(normalizeQuestionText(candidateQuestionData.getQuestionEnglish()));
			managedQuestion.setQuestionPortuguese(normalizeQuestionText(candidateQuestionData.getQuestionPortuguese()));
			managedQuestion.setAnswerSpanish(normalizeLongText(candidateQuestionData.getAnswerSpanish()));
			managedQuestion.setAnswerEnglish(normalizeLongText(candidateQuestionData.getAnswerEnglish()));
			managedQuestion.setAnswerPortuguese(normalizeLongText(candidateQuestionData.getAnswerPortuguese()));

			CandidateQuestionStatus status = candidateQuestionData.getStatus() != null ? candidateQuestionData.getStatus() : CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC;
			managedQuestion.setStatus(status);
			managedQuestion.setCurrentOwner(resolveCurrentOwner(status));
			managedQuestion.setUpdateDate(new Date());
			if (status == CandidateQuestionStatus.PUBLISHED) {
				if (managedQuestion.getPublishedDate() == null) {
					managedQuestion.setPublishedDate(new Date());
				}
			} else {
				managedQuestion.setPublishedDate(null);
			}

			if (isNewQuestion) {
				em.persist(managedQuestion);
			} else {
				managedQuestion = em.merge(managedQuestion);
			}

			if (sendStatusNotificationEmail) {
				queueCandidateQuestionStatusNotificationIfNeeded(previousStatus, status, managedQuestion);
			}
			return managedQuestion;
		} catch (Exception e) {
			appLogger.error("Error saving candidate question", e);
			return null;
		}
	}

	@Override
	public boolean createCandidateQuestionFromPublicToken(String questionToken, long candidateId, String askedByName, String askedByEmail, LanguageCode questionLanguage, String questionText, String ip) {
		if (!hasText(questionToken) || candidateId <= 0 || !hasText(questionText) || !hasText(askedByName) || !hasText(askedByEmail)) {
			return false;
		}

		try {
			Election election = getElectionByQuestionToken(questionToken);
			if (election == null) {
				return false;
			}

			Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
			if (candidate == null || candidate.getElection() == null || candidate.getElection().getElectionId() != election.getElectionId()) {
				return false;
			}
			if (candidate.getStatus() != CandidateStatus.CONFIRMED_AND_PUBLISHED) {
				return false;
			}
			if (candidate.isAbstention()) {
				return false;
			}

			CandidateQuestion question = new CandidateQuestion();
			LanguageCode resolvedLanguage = questionLanguage != null ? questionLanguage : LanguageCode.SP;
			String normalizedQuestionText = normalizeQuestionText(questionText);
			question.setElection(election);
			question.setCandidate(candidate);
			question.setAskedByName(normalizeShortText(askedByName, 500));
			question.setAskedByEmail(normalizeEmailText(askedByEmail, 320));
			question.setQuestionLanguage(resolvedLanguage);
			question.setQuestionSpanish(normalizedQuestionText);
			question.setQuestionEnglish(normalizedQuestionText);
			question.setQuestionPortuguese(normalizedQuestionText);
			question.setStatus(CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC);
			question.setCurrentOwner(resolveCurrentOwner(question.getStatus()));
			question.setCreationDate(new Date());
			question.setUpdateDate(new Date());
			em.persist(question);
			queueCandidateQuestionNotificationToStandardRecipient(
					election,
					question,
					null,
					CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC);
			return true;
		} catch (Exception e) {
			appLogger.error("Error creating public candidate question", e);
			return false;
		}
	}

	private void queueCandidateQuestionStatusNotificationIfNeeded(CandidateQuestionStatus previousStatus, CandidateQuestionStatus newStatus, CandidateQuestion question) {
		if (question == null || question.getElection() == null || newStatus == null || previousStatus == newStatus) {
			return;
		}

		switch (newStatus) {
		case QUESTION_READY_FOR_CANDIDATE:
			queueCandidateQuestionNotificationToCandidate(question.getElection(), question, previousStatus, newStatus);
			break;
		case ANSWER_SUBMITTED_BY_CANDIDATE:
			queueCandidateQuestionNotificationToStandardRecipient(question.getElection(), question, previousStatus, newStatus);
			break;
		case PUBLISHED:
			queueCandidateQuestionNotificationToAskedBy(question.getElection(), question, previousStatus, newStatus);
			break;
		case QUESTION_RECEIVED_LACNIC:
		case REJECTED:
		default:
			break;
		}
	}

	private void queueCandidateQuestionNotificationToStandardRecipient(Election election, CandidateQuestion question, CandidateQuestionStatus previousStatus, CandidateQuestionStatus newStatus) {
		String recipient = resolveCandidateQuestionStandardRecipient(election);
		if (!hasText(recipient)) {
			appLogger.warn("Candidate question email skipped because standard recipient is not configured. electionId={}, questionId={}, status={}",
					election != null ? election.getElectionId() : null,
					question != null ? question.getCandidateQuestionId() : null,
					newStatus);
			return;
		}
		queueCandidateQuestionStatusNotification(election, question, previousStatus, newStatus, recipient, resolveCandidateQuestionNotificationLanguage(question));
	}

	private void queueCandidateQuestionNotificationToCandidate(Election election, CandidateQuestion question, CandidateQuestionStatus previousStatus, CandidateQuestionStatus newStatus) {
		Candidate candidate = question != null ? question.getCandidate() : null;
		String recipient = candidate != null ? candidate.getMail() : null;
		if (!hasText(recipient)) {
			appLogger.warn("Candidate question email skipped because candidate recipient is missing. electionId={}, questionId={}, candidateId={}, status={}",
					election != null ? election.getElectionId() : null,
					question != null ? question.getCandidateQuestionId() : null,
					candidate != null ? candidate.getCandidateId() : null,
					newStatus);
			return;
		}
		queueCandidateQuestionStatusNotification(election, question, previousStatus, newStatus, recipient, resolveCandidateQuestionNotificationLanguage(question));
	}

	private void queueCandidateQuestionNotificationToAskedBy(Election election, CandidateQuestion question, CandidateQuestionStatus previousStatus, CandidateQuestionStatus newStatus) {
		String recipient = question != null ? question.getAskedByEmail() : null;
		if (!hasText(recipient)) {
			appLogger.warn("Candidate question email skipped because askedBy recipient is missing. electionId={}, questionId={}, status={}",
					election != null ? election.getElectionId() : null,
					question != null ? question.getCandidateQuestionId() : null,
					newStatus);
			return;
		}
		queueCandidateQuestionStatusNotification(election, question, previousStatus, newStatus, recipient, resolveCandidateQuestionNotificationLanguage(question));
	}

	private void queueCandidateQuestionStatusNotification(Election election, CandidateQuestion question, CandidateQuestionStatus previousStatus, CandidateQuestionStatus newStatus, String recipient, LanguageCode language) {
		if (election == null || question == null || newStatus == null || !hasText(recipient)) {
			return;
		}

		LanguageCode resolvedLanguage = language != null ? language : Constants.DEFAULT_EMAIL_LANGUAGE;
		Date eventDate = resolveCandidateQuestionEventDate(question, newStatus);
		String questionText = resolveTemplateValue(resolveCandidateQuestionText(question, resolvedLanguage));
		String answerText = resolveTemplateValue(resolveCandidateQuestionAnswer(question, resolvedLanguage));
		String candidateName = resolveTemplateValue(question.getCandidate() != null ? question.getCandidate().getName() : null);
		String candidateEmail = resolveTemplateValue(question.getCandidate() != null ? question.getCandidate().getMail() : null);
		String previousStatusKey = previousStatus != null ? previousStatus.name() : "-";
		String newStatusKey = newStatus.name();
		String linkCta = resolveCandidateQuestionLinkCta(election, question, newStatus);

		Map<String, Object> variables = new HashMap<>();
		variables.put("election", election);
		variables.put("candidate", question.getCandidate());
		variables.put("candidateQuestion", question);
		variables.put("candidateQuestionId", question.getCandidateQuestionId());
		variables.put("questionId", question.getCandidateQuestionId());
		variables.put("candidateQuestionLanguage", resolvedLanguage.name());
		variables.put("candidateQuestionPreviousStatus", previousStatusKey);
		variables.put("candidateQuestionNewStatus", newStatusKey);
		variables.put("candidateQuestionPreviousStatusLabel", resolveCandidateQuestionPreviousStatusLabel(previousStatus, resolvedLanguage));
		variables.put("candidateQuestionNewStatusLabel", newStatus.resolveLabel(resolvedLanguage));
		variables.put("candidateQuestionPreviousStatusLabelSpanish", resolveCandidateQuestionPreviousStatusLabel(previousStatus, LanguageCode.SP));
		variables.put("candidateQuestionPreviousStatusLabelEnglish", resolveCandidateQuestionPreviousStatusLabel(previousStatus, LanguageCode.EN));
		variables.put("candidateQuestionPreviousStatusLabelPortuguese", resolveCandidateQuestionPreviousStatusLabel(previousStatus, LanguageCode.PT));
		variables.put("candidateQuestionNewStatusLabelSpanish", newStatus.resolveLabel(LanguageCode.SP));
		variables.put("candidateQuestionNewStatusLabelEnglish", newStatus.resolveLabel(LanguageCode.EN));
		variables.put("candidateQuestionNewStatusLabelPortuguese", newStatus.resolveLabel(LanguageCode.PT));
		variables.put("candidateQuestionText", questionText);
		variables.put("candidateQuestionAnswer", answerText);
		variables.put("candidateQuestionQuestionSpanish", resolveTemplateValue(question.getQuestionSpanish()));
		variables.put("candidateQuestionQuestionEnglish", resolveTemplateValue(question.getQuestionEnglish()));
		variables.put("candidateQuestionQuestionPortuguese", resolveTemplateValue(question.getQuestionPortuguese()));
		variables.put("candidateQuestionAnswerSpanish", resolveTemplateValue(question.getAnswerSpanish()));
		variables.put("candidateQuestionAnswerEnglish", resolveTemplateValue(question.getAnswerEnglish()));
		variables.put("candidateQuestionAnswerPortuguese", resolveTemplateValue(question.getAnswerPortuguese()));
		variables.put("candidateQuestionEventDate", eventDate);
		variables.put("candidateQuestionEventDateUtc", formatDateUtc(eventDate));
		variables.put("candidateQuestionActionRequired", newStatus.resolveActionRequired(resolvedLanguage));
		variables.put("candidateQuestionActionRequiredSpanish", newStatus.resolveActionRequired(LanguageCode.SP));
		variables.put("candidateQuestionActionRequiredEnglish", newStatus.resolveActionRequired(LanguageCode.EN));
		variables.put("candidateQuestionActionRequiredPortuguese", newStatus.resolveActionRequired(LanguageCode.PT));
		variables.put("candidateQuestionAskedByInitialName", resolveCandidateQuestionAskedByInitialName(question.getAskedByName()));
		variables.put("candidateQuestionCandidateName", candidateName);
		variables.put("candidateQuestionCandidateEmail", candidateEmail);
		variables.put("previousStatus", previousStatusKey);
		variables.put("newStatus", newStatusKey);
		variables.put("question", questionText);
		variables.put("answer", answerText);
		variables.put("date", formatDateUtc(eventDate));
		variables.put("actionRequired", newStatus.resolveActionRequired(resolvedLanguage));
		variables.put("linkCTA", linkCta);
		variables.put("candidateQuestionLinkCTA", linkCta);

		try {
			EJBFactory.getInstance().getMailsSendingEJB().queueTemplateEmail(
					election,
					EmailTemplateType.CANDIDATE_QUESTION_STATUS_NOTIFICATION,
					recipient.trim(),
					null,
					resolvedLanguage,
					variables);
		} catch (Exception e) {
			appLogger.error("Error queueing candidate question status email. electionId={}, questionId={}, previousStatus={}, newStatus={}, recipient={}",
					election.getElectionId(),
					question.getCandidateQuestionId(),
					previousStatus,
					newStatus,
					recipient,
					e);
		}
	}

	private String resolveCandidateQuestionLinkCta(Election election, CandidateQuestion question, CandidateQuestionStatus newStatus) {
		String adminLink = resolveCandidateQuestionAdminManagementLink(election);
		if (newStatus == null) {
			return adminLink;
		}
		switch (newStatus) {
		case QUESTION_READY_FOR_CANDIDATE:
			String candidateTasksLink = resolveCandidateQuestionCandidateTasksLink(question);
			return hasText(candidateTasksLink) ? candidateTasksLink : adminLink;
		case PUBLISHED:
		case REJECTED:
			String publicCandidateLink = resolveCandidateQuestionPublicCandidateLink(election, question);
			return hasText(publicCandidateLink) ? publicCandidateLink : adminLink;
		case QUESTION_RECEIVED_LACNIC:
		case ANSWER_SUBMITTED_BY_CANDIDATE:
		default:
			return adminLink;
		}
	}

	private String resolveCandidateQuestionAdminManagementLink(Election election) {
		if (election == null || election.getElectionId() <= 0) {
			return "";
		}
		String baseUrl = resolveOptionalTextParameter(Constants.URL);
		return buildApplicationUrl(baseUrl, "elections/admin/election/questions?id=" + election.getElectionId());
	}

	private String resolveCandidateQuestionCandidateTasksLink(CandidateQuestion question) {
		if (question == null || question.getCandidate() == null || question.getCandidate().getCandidateId() <= 0) {
			return null;
		}
		Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByCandidateId(question.getCandidate().getCandidateId());
		if (nomination == null || !hasText(nomination.getAcceptNominationToken())) {
			return null;
		}
		return nomination.getAcceptNominationLink();
	}

	private String resolveCandidateQuestionPublicCandidateLink(Election election, CandidateQuestion question) {
		if (election == null || question == null || question.getCandidate() == null || question.getCandidate().getCandidateId() <= 0) {
			return null;
		}
		String publicElectionToken = election.getPublicElectionToken();
		if (!hasText(publicElectionToken)) {
			return null;
		}
		return LinksUtils.buildPublicCandidateProfileLink(publicElectionToken, question.getCandidate().getCandidateId());
	}

	private String buildApplicationUrl(String baseUrl, String pathAndQuery) {
		if (!hasText(baseUrl) || !hasText(pathAndQuery)) {
			return "";
		}
		String normalizedBase = baseUrl.trim();
		if (!normalizedBase.endsWith("/")) {
			normalizedBase = normalizedBase + "/";
		}
		String normalizedPath = pathAndQuery.startsWith("/") ? pathAndQuery.substring(1) : pathAndQuery;
		return normalizedBase + normalizedPath;
	}

	private String resolveCandidateQuestionStandardRecipient(Election election) {
		if (election != null && hasText(election.getDefaultRecipient())) {
			return election.getDefaultRecipient().trim();
		}
		String systemDefaultRecipient = getDefaultRecipient();
		if (hasText(systemDefaultRecipient)) {
			return systemDefaultRecipient.trim();
		}
		if (election != null && hasText(election.getDefaultSender())) {
			return election.getDefaultSender().trim();
		}
		String systemDefaultSender = getDefaultSender();
		return hasText(systemDefaultSender) ? systemDefaultSender.trim() : null;
	}

	private LanguageCode resolveCandidateQuestionNotificationLanguage(CandidateQuestion question) {
		LanguageCode questionLanguage = question != null ? question.getQuestionLanguage() : null;
		return questionLanguage != null ? questionLanguage : Constants.DEFAULT_EMAIL_LANGUAGE;
	}

	private Date resolveCandidateQuestionEventDate(CandidateQuestion question, CandidateQuestionStatus newStatus) {
		if (question == null) {
			return new Date();
		}
		if (newStatus == CandidateQuestionStatus.PUBLISHED && question.getPublishedDate() != null) {
			return question.getPublishedDate();
		}
		if (question.getUpdateDate() != null) {
			return question.getUpdateDate();
		}
		if (question.getCreationDate() != null) {
			return question.getCreationDate();
		}
		return new Date();
	}

	private String resolveCandidateQuestionText(CandidateQuestion question, LanguageCode language) {
		if (question == null) {
			return null;
		}
		return resolveCandidateQuestionLocalizedText(
				language,
				question.getQuestionSpanish(),
				question.getQuestionEnglish(),
				question.getQuestionPortuguese());
	}

	private String resolveCandidateQuestionAnswer(CandidateQuestion question, LanguageCode language) {
		if (question == null) {
			return null;
		}
		return resolveCandidateQuestionLocalizedText(
				language,
				question.getAnswerSpanish(),
				question.getAnswerEnglish(),
				question.getAnswerPortuguese());
	}

	private String resolveCandidateQuestionLocalizedText(LanguageCode language, String spanish, String english, String portuguese) {
		LanguageCode resolvedLanguage = language != null ? language : Constants.DEFAULT_EMAIL_LANGUAGE;
		switch (resolvedLanguage) {
		case EN:
			if (hasText(english)) {
				return english.trim();
			}
			if (hasText(spanish)) {
				return spanish.trim();
			}
			return hasText(portuguese) ? portuguese.trim() : null;
		case PT:
			if (hasText(portuguese)) {
				return portuguese.trim();
			}
			if (hasText(spanish)) {
				return spanish.trim();
			}
			return hasText(english) ? english.trim() : null;
		case SP:
		default:
			if (hasText(spanish)) {
				return spanish.trim();
			}
			if (hasText(english)) {
				return english.trim();
			}
			return hasText(portuguese) ? portuguese.trim() : null;
		}
	}

	private String resolveCandidateQuestionPreviousStatusLabel(CandidateQuestionStatus previousStatus, LanguageCode language) {
		if (previousStatus != null) {
			return previousStatus.resolveLabel(language);
		}
		LanguageCode resolvedLanguage = language != null ? language : Constants.DEFAULT_EMAIL_LANGUAGE;
		switch (resolvedLanguage) {
		case EN:
			return "No previous status";
		case PT:
			return "Sem status anterior";
		case SP:
		default:
			return "Sin estado previo";
		}
	}

	private String resolveCandidateQuestionAskedByInitialName(String askedByName) {
		if (!hasText(askedByName)) {
			return "?";
		}
		String[] parts = askedByName.trim().split("\\s+");
		Character first = null;
		Character last = null;
		int count = 0;
		for (String part : parts) {
			Character initial = extractCandidateQuestionInitial(part);
			if (initial == null) {
				continue;
			}
			if (first == null) {
				first = initial;
			}
			last = initial;
			count++;
		}
		if (first == null) {
			return "?";
		}
		if (count <= 1 || last == null) {
			return String.valueOf(first);
		}
		return String.valueOf(first) + last;
	}

	private Character extractCandidateQuestionInitial(String value) {
		if (!hasText(value)) {
			return null;
		}
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				return Character.toUpperCase(c);
			}
		}
		return null;
	}

	private String resolveTemplateValue(String value) {
		return hasText(value) ? value.trim() : "-";
	}

	private String formatDateUtc(Date date) {
		if (date == null) {
			return "-";
		}
		return new DateTime(date, DateTimeZone.UTC).toString("yyyy-MM-dd HH:mm:ss 'UTC'");
	}

	/**
	 * Deletes an election removing all its related resources (admins, auditors, candidates, emails, votes and user voters)
	 *
	 * @param electionId    identifier of the election
	 * @param electionTitle title of the election used for logging purposes
	 * @param userAdminId   id of the user deleting the election, used for logging purposes
	 * @param ip            ip of the user deleting the election, used for logging purposes
	 */
	@Override
	public void removeElection(long electionId, String electionTitle, String userAdminId, String ip) throws Exception {
		try {
			List<Auditor> auditors = ElectionsDaoFactory.createAuditorDao(em).getElectionAuditors(electionId);
			List<Candidate> candidates = ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(electionId);
			List<Email> emails = ElectionsDaoFactory.createEmailDao(em).getElectionEmails(electionId);
			List<ElectionEmailTemplate> emailTemplates = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplates(electionId);
			List<UserVoter> userVoters = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionId);
			List<Vote> votes = ElectionsDaoFactory.createVoteDao(em).getElectionVotes(electionId);
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
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
			appLogger.error(e.getMessage());
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
		List<Election> elections = ElectionsDaoFactory.createElectionDao(em).getElectionsAllOrderCreationDate();
		for (Election election : elections) {
			VotingPeriodResolver.applyVotingWindow(em, election);
		}
		return elections;
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
	 * @param election    An election entity with the information to update the election.
	 * @param userAdminId Id of the user, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 *
	 * @return returns the election entity of the updated election
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Election updateElection(Election election, String userAdminId, String ip) throws Exception {
		try {
			normalizeElectionRichTextFields(election);
			boolean isNewElection = election.getElectionId() == 0;
			return isNewElection ? createElection(election, userAdminId, ip) : updateExistingElection(election, userAdminId, ip);
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ElectionAuditorResult saveElectionAuditorResult(ElectionAuditorResult electionAuditorResult, String userAdminId, String ip) throws Exception {
		try {
			if (electionAuditorResult == null || electionAuditorResult.getElectionId() <= 0L) {
				throw new IllegalArgumentException("Election auditor result requires a valid electionId");
			}

			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionAuditorResult.getElectionId());
			if (election == null) {
				throw new IllegalArgumentException("Election not found for electionId=" + electionAuditorResult.getElectionId());
			}

			ElectionAuditorResult existingResult = ElectionsDaoFactory.createElectionAuditorResultDao(em)
					.getElectionAuditorResult(electionAuditorResult.getElectionId());
			ElectionAuditorResult managedResult = existingResult != null ? existingResult : new ElectionAuditorResult();
			managedResult.setElectionId(electionAuditorResult.getElectionId());
			copyElectionAuditorResultFields(electionAuditorResult, managedResult);
			normalizeElectionAuditorResultRichTextFields(managedResult);

			if (existingResult == null) {
				em.persist(managedResult);
			} else {
				managedResult = em.merge(managedResult);
			}

			String actor = hasText(userAdminId) ? userAdminId.toUpperCase() : SYSTEM_ACTOR;
			String description = actor + " actualizó los resultados oficiales de la elección " + safeValue(election.getTitleSpanish())
					+ ". electionId=" + election.getElectionId()
					+ ", resultEsChars=" + safeLength(managedResult.getResultSpanish())
					+ ", resultEnChars=" + safeLength(managedResult.getResultEnglish())
					+ ", resultPtChars=" + safeLength(managedResult.getResultPortuguese())
					+ ", cartaEs=" + booleanText(hasBinary(managedResult.getResultLetterSpanish()))
					+ ", cartaEn=" + booleanText(hasBinary(managedResult.getResultLetterEnglish()))
					+ ", cartaPt=" + booleanText(hasBinary(managedResult.getResultLetterPortuguese()));
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, election.getElectionId());
			appLogger.info("saveElectionAuditorResult: electionId={}, userAdminId={}", election.getElectionId(), userAdminId);
			return managedResult;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			throw e;
		}
	}

	private void normalizeElectionRichTextFields(Election election) {
		if (election == null) {
			return;
		}

		election.setCallSpanish(normalizeHtmlContentToNullIfEmpty(election.getCallSpanish()));
		election.setCallEnglish(normalizeHtmlContentToNullIfEmpty(election.getCallEnglish()));
		election.setCallPortuguese(normalizeHtmlContentToNullIfEmpty(election.getCallPortuguese()));
	}

	private void normalizeElectionAuditorResultRichTextFields(ElectionAuditorResult electionAuditorResult) {
		if (electionAuditorResult == null) {
			return;
		}
		electionAuditorResult.setResultSpanish(normalizeHtmlContentToNullIfEmpty(electionAuditorResult.getResultSpanish()));
		electionAuditorResult.setResultEnglish(normalizeHtmlContentToNullIfEmpty(electionAuditorResult.getResultEnglish()));
		electionAuditorResult.setResultPortuguese(normalizeHtmlContentToNullIfEmpty(electionAuditorResult.getResultPortuguese()));
	}

	private void copyElectionAuditorResultFields(ElectionAuditorResult source, ElectionAuditorResult target) {
		if (source == null || target == null) {
			return;
		}
		target.setResultSpanish(source.getResultSpanish());
		target.setResultEnglish(source.getResultEnglish());
		target.setResultPortuguese(source.getResultPortuguese());
		target.setResultLetterSpanish(source.getResultLetterSpanish());
		target.setResultLetterEnglish(source.getResultLetterEnglish());
		target.setResultLetterPortuguese(source.getResultLetterPortuguese());
	}

	private String normalizeHtmlContentToNullIfEmpty(String value) {
		if (!hasText(value)) {
			return null;
		}

		return hasText(htmlToRenderedPlainText(value)) ? value.trim() : null;
	}

	private Election createElection(Election election, String userAdminId, String ip) {
		if (!hasText(election.getDefaultSender())) {
			election.setDefaultSender(getDefaultSender());
		}
		if (!hasText(election.getDefaultRecipient())) {
			election.setDefaultRecipient(getDefaultRecipient());
		}
		em.persist(election);

		String description = userAdminId.toUpperCase() + " creó la elección" + " (" + election.getTitleSpanish() + ")" + " correctamente";
		persistActivity(userAdminId, ActivityType.CREATE_ELECTION, description, ip, election.getElectionId());
		appLogger.info("createElection: electionId={}, userAdminId={}", election.getElectionId(), userAdminId);

		createElectionEmailTemplates(election);
		List<Commissioner> commisioners = ElectionsDaoFactory.createCommissionerDao(em).getCommissionersAll();
		for (Commissioner commisioner : commisioners) {
			em.persist(new Auditor(election, commisioner));
		}

		applyElectionSetupByElectionType(election);
		return election;
	}

	private Election updateExistingElection(Election election, String userAdminId, String ip) {
		Election managedElection = em.merge(election);
		String description = userAdminId.toUpperCase() + " actualizó la elección " + " (" + managedElection.getTitleSpanish() + ")" + " correctamente";
		persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, managedElection.getElectionId());
		appLogger.info("updateElection: electionId={}, userAdminId={}", managedElection.getElectionId(), userAdminId);
		return managedElection;
	}

	private void applyElectionSetupByElectionType(Election election) {
		if (election == null || election.getElectionId() <= 0) {
			return;
		}

		ElectionType electionType = election.getEffectiveElectionType();
		if (electionType == null) {
			return;
		}

		if (ElectionType.BOARD == electionType) {
			applyBoardScopeSetup(election);
		} else if (ElectionType.ELECTORAL_COMMISSION == electionType) {
			applyElectoralCommissionScopeSetup(election);
		} else if (ElectionType.FISCAL_COMMISSION == electionType) {
			applyFiscalCommissionScopeSetup(election);
		} else if (ElectionType.MODERATORS == electionType) {
			applyModeratorsScopeSetup(election);
		} else if (ElectionType.IANA == electionType) {
			applyIanaScopeSetup(election);
		} else if (ElectionType.ASO == electionType) {
			applyAsoScopeSetup(election);
		} else if (ElectionType.OTHER == electionType) {
			applyOtherScopeSetup(election);
		}
	}

	private void applyBoardScopeSetup(Election election) {
		applyPresetScopeSetup(election, ElectionPresetConfigurations.get(ElectionType.BOARD));
	}

	private void applyElectoralCommissionScopeSetup(Election election) {
		applyPresetScopeSetup(election, ElectionPresetConfigurations.get(ElectionType.ELECTORAL_COMMISSION));
	}

	private void applyFiscalCommissionScopeSetup(Election election) {
		applyPresetScopeSetup(election, ElectionPresetConfigurations.get(ElectionType.FISCAL_COMMISSION));
	}

	private void applyModeratorsScopeSetup(Election election) {
		applyPresetScopeSetup(election, ElectionPresetConfigurations.get(ElectionType.MODERATORS));
	}

	private void applyIanaScopeSetup(Election election) {
		applyPresetScopeSetup(election, ElectionPresetConfigurations.get(ElectionType.IANA));
	}

	private void applyAsoScopeSetup(Election election) {
		applyPresetScopeSetup(election, ElectionPresetConfigurations.get(ElectionType.ASO));
	}

	private void applyOtherScopeSetup(Election election) {
		applyPresetScopeSetup(election, ElectionPresetConfigurations.get(ElectionType.OTHER));
	}

	private void applyPresetScopeSetup(Election election, ElectionPresetConfiguration presetConfiguration) {
		if (presetConfiguration == null) {
			return;
		}

		election.setElectionType(presetConfiguration.getElectionType());
		election.setManageOrganizationsManual(presetConfiguration.isManageOrganizationsManual());
		election.setManageVotersManual(presetConfiguration.isManageVotersManual());

		long electionId = election.getElectionId();
		if (electionId <= 0) {
			return;
		}

		boolean hasCalendars = !ElectionsDaoFactory.createElectionCalendarDao(em).getElectionCalendars(electionId).isEmpty();
		boolean hasTasks = ElectionsDaoFactory.createElectionTaskDao(em).countElectionTasks(electionId) > 0;
		if (hasCalendars || hasTasks) {
			return;
		}

		Date defaultCalendarDateUtc = ElectionPresetConfigurations.buildDefaultCalendarDateUtc();

		Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey = new HashMap<>();
		for (ElectionCalendarKey calendarKey : ElectionCalendarKey.values()) {
			boolean shouldSetDate = presetConfiguration.getCalendarsWithDefaultDate().contains(calendarKey);
			Date startDate = shouldSetDate ? defaultCalendarDateUtc : null;
			Date endDate = shouldSetDate ? defaultCalendarDateUtc : null;
			boolean publicable = ElectionPresetConfigurations.isDefaultPublicCalendar(calendarKey);
			ElectionCalendar calendar = new ElectionCalendar(election, calendarKey, startDate, endDate, publicable);
			em.persist(calendar);
			calendarsByKey.put(calendarKey, calendar);
		}

		ElectionCalendar fallbackCalendar = !calendarsByKey.isEmpty() ? calendarsByKey.values().iterator().next() : null;
		boolean campusConfigured = CampusClient.isCampusIntegrationEnabled();
		for (ElectionTaskKey taskKey : presetConfiguration.getPresetTasks()) {
			if (!ElectionPresetConfigurations.isPresetTaskEnabled(taskKey, campusConfigured)) {
				continue;
			}
			ElectionCalendarKey targetKey = ElectionPresetConfigurations.resolveTaskStage(taskKey);
			ElectionCalendar taskCalendar = calendarsByKey.get(targetKey);
			if (taskCalendar == null) {
				taskCalendar = fallbackCalendar;
			}
			if (taskCalendar == null) {
				continue;
			}
			TaskDependencyLevel dependencyLevel = resolveDefaultDependencyLevel(taskKey);
			Integer displayOrder = resolveDefaultTaskDisplayOrder(taskKey);
			boolean publicable = ElectionPresetConfigurations.isDefaultPublicTask(taskKey);
			ElectionTask electionTask = new ElectionTask(election, taskCalendar, taskKey, dependencyLevel, displayOrder, publicable);
			em.persist(electionTask);
		}

		election.setOrganizationsSet(true);
		election.setElectorsSet(true);
		election.setCalendarSet(false);
		election.setTasksSet(true);
		election.setCandidatesSet(false);
		election.setAuditorsSet(false);
	}

	private TaskDependencyLevel resolveDefaultDependencyLevel(ElectionTaskKey taskKey) {
		if (taskKey == null) {
			return TaskDependencyLevel.LEVEL_1;
		}
		return taskKey.getDefaultDependencyLevel();
	}

	private Integer resolveDefaultTaskDisplayOrder(ElectionTaskKey taskKey) {
		if (taskKey == null) {
			return null;
		}
		return taskKey.getDefaultDisplayOrder();
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
			boolean enabled = Boolean.TRUE.equals(status);
			String message = enabled ? "habilitó el link de resultado para la elección " : "deshabilitó el link de resultado para la elección ";
			String description = userAdminId.toUpperCase() + " " + message + "(" + election.getTitleSpanish() + ")";
			ActivityType activityType = enabled ? ActivityType.ENABLE_RESULTS_LINK : ActivityType.DISABLE_RESULTS_LINK;
			persistActivity(userAdminId, activityType, description, ip, election.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1.getMessage());
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
			boolean enabled = Boolean.TRUE.equals(status);
			String message = enabled ? "habilitó el link de auditoria para la elección " : "deshabilitó el link de auditoria para la elección ";
			String description = userAdminId.toUpperCase() + " " + message + "(" + election.getTitleSpanish() + ")";
			ActivityType activityType = enabled ? ActivityType.ENABLE_AUDIT_LINK : ActivityType.DISABLE_AUDIT_LINK;
			persistActivity(userAdminId, activityType, description, ip, election.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1.getMessage());
		}
	}

	@Override
	public void setDoNominationLinkStatus(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setDoNominationLinkAvailable(status);
			em.persist(election);
			boolean enabled = Boolean.TRUE.equals(status);
			String message = enabled ? "habilitó el link de nominación para la elección " : "deshabilitó el link de nominación para la elección ";
			String description = userAdminId.toUpperCase() + " " + message + "(" + election.getTitleSpanish() + ")";
			ActivityType activityType = enabled ? ActivityType.ENABLE_DO_NOMINATION_LINK : ActivityType.DISABLE_DO_NOMINATION_LINK;
			persistActivity(userAdminId, activityType, description, ip, election.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1.getMessage());
		}
	}

	@Override
	public void setNominationTasksLinkStatus(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setNominationTasksLinkAvailable(status);
			em.persist(election);
			boolean enabled = Boolean.TRUE.equals(status);
			String message = enabled ? "habilitó el link de tareas de nominación para la elección " : "deshabilitó el link de tareas de nominación para la elección ";
			String description = userAdminId.toUpperCase() + " " + message + "(" + election.getTitleSpanish() + ")";
			ActivityType activityType = enabled ? ActivityType.ENABLE_NOMINATION_TASKS_LINK : ActivityType.DISABLE_NOMINATION_TASKS_LINK;
			persistActivity(userAdminId, activityType, description, ip, election.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1.getMessage());
		}
	}

	@Override
	public void setNominationSupportLinkStatus(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setNominationSupportLinkAvailable(status);
			em.persist(election);
			boolean enabled = Boolean.TRUE.equals(status);
			String message = enabled ? "habilitó el link de apoyo a nominación para la elección " : "deshabilitó el link de apoyo a nominación para la elección ";
			String description = userAdminId.toUpperCase() + " " + message + "(" + election.getTitleSpanish() + ")";
			ActivityType activityType = enabled ? ActivityType.ENABLE_NOMINATION_SUPPORT_LINK : ActivityType.DISABLE_NOMINATION_SUPPORT_LINK;
			persistActivity(userAdminId, activityType, description, ip, election.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1.getMessage());
		}
	}

	@Override
	public void setPublicElectionLinkStatus(Long electionId, Boolean status, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setPublicElectionLinkAvailable(status);
			em.persist(election);
			boolean enabled = Boolean.TRUE.equals(status);
			String message = enabled ? "habilitó el link de portal público para la elección " : "deshabilitó el link de portal público para la elección ";
			String description = userAdminId.toUpperCase() + " " + message + "(" + election.getTitleSpanish() + ")";
			ActivityType activityType = enabled ? ActivityType.ENABLE_PUBLIC_ELECTION_LINK : ActivityType.DISABLE_PUBLIC_ELECTION_LINK;
			persistActivity(userAdminId, activityType, description, ip, election.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1.getMessage());
		}
	}

	/**
	 * Gets all the commissioners from the system.
	 *
	 * @return returns a list of commissioner entity containing all the commissioners in the system.
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
	 * @param userAdminId   Identifier of the user performing the action used for logging purposes
	 * @param ip            Ip of the user performing the action, used for logging purposes
	 */
	@Override
	public void removeUserVoter(UserVoter userVoter, String electionTitle, String userAdminId, String ip) throws CensusValidationException {
		if (userVoter == null || userVoter.getElection() == null) {
			throw new CensusValidationException("censusManagementUserNotFound", null, null);
		}
		validateUserVoterCanBeRemoved(userVoter.getElection().getElectionId(), userVoter.getUserVoterId());
		UserVoter userVoterDB = em.find(UserVoter.class, userVoter.getUserVoterId());
		Long electionId = userVoterDB.getElection().getElectionId();
		em.remove(userVoterDB);
		String description = userAdminId.toUpperCase() + " eliminó a " + userVoterDB.getName() + " del listado de usuario padrón en la elección " + electionTitle;
		persistActivity(userAdminId, ActivityType.REMOVE_VOTE_USER_MANUAL, description, ip, electionId);
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
	 * @return returns a list of activity entity which contains all the activities on the system
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
	 * @return returns a list of activity entity which contains all the activities of the election.
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
	 * @return returns a list of uservoter entity which contains the voters of the election.
	 */
	@Override
	public List<UserVoter> getElectionUserVoters(long electionId) {
		return ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionId);
	}

	@Override
	public List<UserVoter> getElectionUserVotersByEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter) {
		String safeEmail = normalizeEmailText(normalizedEmail, 320);
		if (electionId <= 0 || !hasText(safeEmail)) {
			return Collections.emptyList();
		}
		int safeMaxResults = maxResults > 0 ? maxResults : 10;
		String safeCountryCodeFilter = normalizeCountryCodeFilter(countryCodeFilter);
		return ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVotersByNormalizedEmail(
				electionId,
				safeEmail,
				safeMaxResults,
				safeCountryCodeFilter);
	}

	@Override
	public List<Organization> getElectionOrganizationsByMembershipContactEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter) {
		String safeEmail = normalizeEmailText(normalizedEmail, 320);
		if (electionId <= 0 || !hasText(safeEmail)) {
			return Collections.emptyList();
		}
		int safeMaxResults = maxResults > 0 ? maxResults : 10;
		String safeCountryCodeFilter = normalizeCountryCodeFilter(countryCodeFilter);
		return ElectionsDaoFactory.createOrganizationDao(em).getElectionOrganizationsByNormalizedMembershipContactEmail(
				electionId,
				safeEmail,
				safeMaxResults,
				safeCountryCodeFilter);
	}

	@Override
	public List<UserVoterLite> getElectionUserVotersLite(long electionId) {
		return ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVotersLite(electionId);
	}

	/**
	 * Updates a particular voter's link of an election
	 *
	 * @param userVoterId   Identifier of the voter
	 *
	 * @param electionTitle Title of the election used for logging purposes
	 * @param userAdminId   Identifier of the user performing the action used for logging purposes
	 * @param ip            Ip of the user performing the action, used for logging purposes
	 *
	 *
	 */
	@Override
	public void updateUserVoterToken(long userVoterId, String name, String electionTitle, String userAdminId, String ip) {
		UserVoter userVoter = em.find(UserVoter.class, userVoterId);
		userVoter.setVoteToken(StringUtils.createSecureToken());
		em.merge(userVoter);
		String description = userAdminId.toUpperCase() + " renovó el link de votación para el usuario " + name + " en la elección " + electionTitle;
		persistActivity(userAdminId, ActivityType.UPDATE_TOKEN_USER_CENSUS_MANUAL, description, ip, userVoter.getElection().getElectionId());
	}

	/**
	 * Deletes a user admin from the system.
	 *
	 * @param userAdminToDeleteId Identifier of the user to be deleted
	 * @param userAdminId         Identifier of the user performing the action used for logging purposes
	 * @param ip                  Ip of the user performing the action, used for logging purposes
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
	 * @param electionId          Identifier of the election
	 * @param content             Excel file containing the voters to upload
	 * @param regenerateVoteLinks When true, regenerates voting links for updated voters. When false, keeps existing links.
	 * @param userAdminId         Identifier of the user performing the action used for logging purposes
	 * @param ip                  Ip of the user performing the action, used for logging purposes
	 */
	@Override
	public boolean queueElectionCensusUpdate(String contentType, long electionId, byte[] content, boolean regenerateVoteLinks, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startCensusProcessing(electionId);
		if (!queued) {
			appLogger.info("Census update already in progress. electionId={}", electionId);
			return false;
		}
			try {
				getManagerProxy().processElectionCensusUpdateAsync(contentType, electionId, content, regenerateVoteLinks, userAdminId, ip);
				return true;
			} catch (Exception e) {
				ElectionsCaches.finishCensusProcessing(electionId);
				ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
				appLogger.error("Unable to queue census async update. electionId={}", electionId, e);
				throw new IllegalStateException("Unable to queue census async update", e);
			}
		}

	@Override
	@Asynchronous
	@TransactionTimeout(7200)
	public void processElectionCensusUpdateAsync(String contentType, long electionId, byte[] content, boolean regenerateVoteLinks, String userAdminId, String ip) {
		boolean requestCensusPurge = false;
		try {
			ElectionsCaches.markCensusProcessingPreparing(electionId);
			getManagerProxy().updateElectionCensus(contentType, electionId, content, regenerateVoteLinks, userAdminId, ip);
			ElectionsCaches.clearCensusProcessingError(electionId);
			requestCensusPurge = true;
			appLogger.info("Census async persistence finished. electionId={}", electionId);
		} catch (CensusValidationException e) {
			ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Async census update failed with validation error. electionId={}, messageKey={}", electionId, e.getMessage(), e);
		} catch (Exception e) {
			ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Async census update failed. electionId={}", electionId, e);
		} finally {
			ElectionsCaches.finishCensusProcessing(electionId);
			appLogger.info("Census processing flag released. electionId={}", electionId);
			if (requestCensusPurge) {
				try {
					EJBFactory.getInstance().getMailsSendingEJB().purgeCensusTablesAsync(electionId);
					appLogger.info("Census purge requested after successful async census update. electionId={}", electionId);
				} catch (Exception e) {
					appLogger.error("Unable to request census purge after async census update. electionId={}", electionId, e);
				}
			}
		}
	}

		/**
		 * Upload the census of a particular election from an excel sheet.
		 *
		 * @param electionId          Identifier of the election
		 * @param content             Excel file containing the voters to upload
		 * @param regenerateVoteLinks When true, regenerates voting links for updated voters. When false, keeps existing links.
		 * @param userAdminId         Identifier of the user performing the action used for logging purposes
		 * @param ip                  Ip of the user performing the action, used for logging purposes
		 */
	@Override
	@TransactionTimeout(7200)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateElectionCensus(String contentType, long electionId, byte[] content, boolean regenerateVoteLinks, String userAdminId, String ip) throws CensusValidationException, Exception {
		long startedAt = System.currentTimeMillis();
		boolean regenerate = regenerateVoteLinks;
		try {
			List<UserVoter> importedUserVoters = ExcelUtils.processCensusExcel(contentType, content);
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			appLogger.info("Census upload started. electionId={}, censusIdentity=ORG_ID, regenerateVoteLinks={}, importedRows={}, admin={}", electionId, regenerate, importedUserVoters.size(), userAdminId);
			CensusUpsertResult upsertResult = applyElectionCensusSnapshot(election, importedUserVoters, regenerate, "upload", true, null, null);

			String linksPolicy = regenerate ? " [regenera links]" : " [mantiene links]";
			String description = userAdminId.toUpperCase() + " actualizó el padrón completo para la elección  " + "(" + election.getTitleSpanish() + ")" + linksPolicy;
			ActivityType activityType = regenerate ? ActivityType.ADD_CENSUS_REGENERATE_LINKS : ActivityType.ADD_CENSUS_KEEP_LINKS;
			persistActivity(userAdminId, activityType, description, ip, electionId);
			long durationMs = System.currentTimeMillis() - startedAt;
			appLogger.info("Census upload completed. electionId={}, regenerateVoteLinks={}, importedRows={}, createdRows={}, updatedRows={}, deletedRows={}, regeneratedTokenRows={}, keptTokenRows={}, generatedMissingTokenRows={}, durationMs={}",
					electionId,
					regenerate,
					upsertResult.totalImportedRows,
					upsertResult.createdRows,
					upsertResult.updatedRows,
					upsertResult.deletedRows,
					upsertResult.regeneratedTokenRows,
					upsertResult.keptTokenRows,
					upsertResult.generatedMissingTokenRows,
					durationMs);

		} catch (CensusValidationException e) {
			long durationMs = System.currentTimeMillis() - startedAt;
			appLogger.error("Census upload validation failed. electionId={}, regenerateVoteLinks={}, durationMs={}, errorKey={}", electionId, regenerate, durationMs, e.getMessage(), e);
			throw e;
		} catch (Exception e1) {
			long durationMs = System.currentTimeMillis() - startedAt;
			appLogger.error("Census upload failed unexpectedly. electionId={}, regenerateVoteLinks={}, durationMs={}", electionId, regenerate, durationMs, e1);
			throw e1;
		}
	}

	private CensusUpsertResult applyElectionCensusSnapshot(Election election, List<UserVoter> importedUserVoters, boolean regenerateVoteLinks, String operationLabel, boolean updateProgressCache,
			String syncRunId, Date syncTimestamp)
			throws CensusValidationException {
		if (election == null || election.getElectionId() <= 0) {
			throw new IllegalArgumentException("Election is required to reconcile census snapshot");
		}

		List<UserVoter> safeImportedUserVoters = importedUserVoters == null ? Collections.emptyList() : importedUserVoters;
		CensusUpsertResult summary = new CensusUpsertResult();
		summary.totalImportedRows = safeImportedUserVoters.size();
		String source = hasText(operationLabel) ? operationLabel.trim() : "snapshot";
		long electionId = election.getElectionId();

		election.setElectorsSet(true);
		List<UserVoterLite> existingUserVoters = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVotersLite(electionId);
		Set<String> importedOrgIds = prepareImportedCensusOrgIds(safeImportedUserVoters);
		List<UserVoterLite> userVotersToDelete = resolveCensusVotersToDeleteForSnapshotLite(existingUserVoters, importedOrgIds);
		validateCensusVotersCanBeDeletedLite(userVotersToDelete);

		summary.existingRows = existingUserVoters.size();
		summary.toDeleteRows = userVotersToDelete.size();
		appLogger.info("Census {} reconciliation plan. electionId={}, censusIdentity=ORG_ID, existingRows={}, importedRows={}, toDeleteRows={}", source, electionId, summary.existingRows, summary.totalImportedRows, summary.toDeleteRows);

		Set<Long> userVotersToDeleteIds = new HashSet<>();
		for (UserVoterLite userVoterToDelete : userVotersToDelete) {
			userVotersToDeleteIds.add(userVoterToDelete.getUserVoterId());
		}

		Map<String, UserVoterLite> existingByOrgId = new HashMap<>();
		for (UserVoterLite existingUserVoter : existingUserVoters) {
			if (!userVotersToDeleteIds.contains(existingUserVoter.getUserVoterId())) {
				String normalizedOrgId = normalizeCensusOrgId(existingUserVoter.getOrgID());
				if (normalizedOrgId != null) {
					existingByOrgId.putIfAbsent(normalizedOrgId, existingUserVoter);
				}
			}
		}

		int totalCreatedRows = 0;
		int totalUpdatedRows = 0;
		for (UserVoter importedUserVoter : safeImportedUserVoters) {
			String normalizedOrgId = normalizeCensusOrgId(importedUserVoter.getOrgID());
			UserVoterLite existingUserVoter = normalizedOrgId == null ? null : existingByOrgId.get(normalizedOrgId);
			if (existingUserVoter == null) {
				totalCreatedRows++;
			} else if (shouldUpdateExistingCensusUserVoter(importedUserVoter, existingUserVoter, regenerateVoteLinks)) {
				totalUpdatedRows++;
			}
		}

		int totalDeletedRows = userVotersToDelete.size();
		int totalPersistenceRows = summary.totalImportedRows + totalDeletedRows;
		int persistedRows = 0;
		if (updateProgressCache) {
			ElectionsCaches.updateCensusProcessingProgress(electionId, persistedRows, totalPersistenceRows, summary.createdRows, summary.updatedRows, summary.deletedRows, totalCreatedRows, totalUpdatedRows,
					totalDeletedRows);
		}

		int processedRows = 0;
		for (UserVoter importedUserVoter : safeImportedUserVoters) {
			String normalizedOrgId = normalizeCensusOrgId(importedUserVoter.getOrgID());
			importedUserVoter.setOrgID(normalizedOrgId);
			importedUserVoter.setMail(normalizeCensusMailForStorage(importedUserVoter.getMail()));
			importedUserVoter.setCountry(normalizeCountryForStorage(importedUserVoter.getCountry()));
			importedUserVoter.setOrgName(normalizeOptionalText(importedUserVoter.getOrgName()));
			UserVoterLite existingUserVoter = normalizedOrgId == null ? null : existingByOrgId.get(normalizedOrgId);

			if (existingUserVoter != null) {
				boolean fieldChanges = hasCensusUserVoterFieldChanges(importedUserVoter, existingUserVoter);
				if (shouldUpdateExistingCensusUserVoter(importedUserVoter, existingUserVoter, regenerateVoteLinks)) {
					summary.updatedRows++;
					String beforeSnapshot = fieldChanges ? buildCensusSyncSnapshot(existingUserVoter) : null;
					String afterSnapshot = fieldChanges ? buildCensusSyncSnapshot(importedUserVoter) : null;
					existingUserVoter.setName(importedUserVoter.getName());
					existingUserVoter.setMail(importedUserVoter.getMail());
					existingUserVoter.setVoteAmount(importedUserVoter.getVoteAmount());
					existingUserVoter.setCountry(importedUserVoter.getCountry());
					existingUserVoter.setLanguage(importedUserVoter.getLanguage());
					existingUserVoter.setOrgID(importedUserVoter.getOrgID());
					existingUserVoter.setOrgName(importedUserVoter.getOrgName());
					if (regenerateVoteLinks) {
						existingUserVoter.setVoteToken(StringUtils.createSecureToken());
						summary.regeneratedTokenRows++;
					} else if (existingUserVoter.getVoteToken() == null || existingUserVoter.getVoteToken().trim().isEmpty()) {
						existingUserVoter.setVoteToken(StringUtils.createSecureToken());
						summary.generatedMissingTokenRows++;
					} else {
						summary.keptTokenRows++;
					}
					if (fieldChanges) {
						registerSyncAuditText(electionId, importedUserVoter.getOrgID(), SYNC_AUDIT_FIELD_CENSUS, beforeSnapshot, afterSnapshot, syncRunId, syncTimestamp);
					}
					em.merge(existingUserVoter);
				} else {
					summary.keptTokenRows++;
				}
			} else {
				summary.createdRows++;
				UserVoterLite newUserVoter = new UserVoterLite();
				newUserVoter.setElectionId(electionId);
				newUserVoter.setVoted(false);
				newUserVoter.setVoteDate(null);
				newUserVoter.setName(importedUserVoter.getName());
				newUserVoter.setMail(importedUserVoter.getMail());
				newUserVoter.setVoteAmount(importedUserVoter.getVoteAmount());
				newUserVoter.setCountry(importedUserVoter.getCountry());
				newUserVoter.setLanguage(importedUserVoter.getLanguage());
				newUserVoter.setOrgID(importedUserVoter.getOrgID());
				newUserVoter.setOrgName(importedUserVoter.getOrgName());
				newUserVoter.setVoteToken(StringUtils.createSecureToken());
				summary.regeneratedTokenRows++;
				em.persist(newUserVoter);
				if (normalizedOrgId != null) {
					existingByOrgId.put(normalizedOrgId, newUserVoter);
				}
			}
			processedRows++;
			persistedRows++;
			if (processedRows % CENSUS_UPLOAD_PROGRESS_LOG_EVERY_ROWS == 0 || processedRows == summary.totalImportedRows) {
				appLogger.info("Census {} progress. electionId={}, processedRows={}, totalRows={}, createdRows={}, updatedRows={}", source, electionId, processedRows, summary.totalImportedRows, summary.createdRows, summary.updatedRows);
				if (updateProgressCache) {
					ElectionsCaches.updateCensusProcessingProgress(electionId, persistedRows, totalPersistenceRows, summary.createdRows, summary.updatedRows, summary.deletedRows, totalCreatedRows, totalUpdatedRows,
							totalDeletedRows);
				}
			}
		}

		int deletedProgressRows = 0;
		UserVoterDao userVoterDao = ElectionsDaoFactory.createUserVoterDao(em);
		List<Long> deleteIds = new ArrayList<>();
		for (UserVoterLite userVoterToDelete : userVotersToDelete) {
			if (userVoterToDelete != null) {
				deleteIds.add(userVoterToDelete.getUserVoterId());
			}
		}
		for (int offset = 0; offset < deleteIds.size(); offset += CENSUS_DELETE_BATCH_SIZE) {
			int end = Math.min(offset + CENSUS_DELETE_BATCH_SIZE, deleteIds.size());
			List<Long> deleteBatchIds = deleteIds.subList(offset, end);
			int deletedBatchRows = userVoterDao.deleteUserVotersByIdsBatch(deleteBatchIds);
			summary.deletedRows += deletedBatchRows;
			deletedProgressRows += deletedBatchRows;
			persistedRows += deletedBatchRows;
			if (deletedProgressRows % CENSUS_UPLOAD_PROGRESS_LOG_EVERY_ROWS == 0 || deletedProgressRows == userVotersToDelete.size() || end == deleteIds.size()) {
				appLogger.info("Census {} delete progress. electionId={}, deletedRows={}, totalToDelete={}", source, electionId, summary.deletedRows, userVotersToDelete.size());
				if (updateProgressCache) {
					ElectionsCaches.updateCensusProcessingProgress(electionId, persistedRows, totalPersistenceRows, summary.createdRows, summary.updatedRows, summary.deletedRows, totalCreatedRows, totalUpdatedRows,
							totalDeletedRows);
				}
			}
		}

		if (updateProgressCache) {
			ElectionsCaches.updateCensusProcessingProgress(electionId, totalPersistenceRows, totalPersistenceRows, summary.createdRows, summary.updatedRows, summary.deletedRows, totalCreatedRows, totalUpdatedRows,
					totalDeletedRows);
		}
		em.persist(election);
		return summary;
	}

	private boolean shouldUpdateExistingCensusUserVoter(UserVoter importedUserVoter, UserVoterLite existingUserVoter, boolean regenerateVoteLinks) {
		if (importedUserVoter == null || existingUserVoter == null) {
			return false;
		}
		if (regenerateVoteLinks || !hasText(existingUserVoter.getVoteToken())) {
			return true;
		}
		return hasCensusUserVoterFieldChanges(importedUserVoter, existingUserVoter);
	}

	private boolean hasCensusUserVoterFieldChanges(UserVoter importedUserVoter, UserVoterLite existingUserVoter) {
		return !Objects.equals(normalizeOptionalText(importedUserVoter.getName()), normalizeOptionalText(existingUserVoter.getName()))
				|| !Objects.equals(normalizeOptionalText(importedUserVoter.getOrgName()), normalizeOptionalText(existingUserVoter.getOrgName()))
				|| !Objects.equals(normalizeCensusMailForStorage(importedUserVoter.getMail()), normalizeCensusMailForStorage(existingUserVoter.getMail()))
				|| !Objects.equals(importedUserVoter.getVoteAmount(), existingUserVoter.getVoteAmount())
				|| !Objects.equals(normalizeCountryForStorage(importedUserVoter.getCountry()), normalizeCountryForStorage(existingUserVoter.getCountry()))
				|| !Objects.equals(normalizeLanguageForStorage(importedUserVoter.getLanguage()), normalizeLanguageForStorage(existingUserVoter.getLanguage()))
				|| !Objects.equals(normalizeCensusOrgId(importedUserVoter.getOrgID()), normalizeCensusOrgId(existingUserVoter.getOrgID()));
	}

	@Override
	public boolean isElectionCensusProcessing(long electionId) {
		boolean processing = ElectionsCaches.isElectionProcessing(electionId);
		AsyncProcessingType processingType = ElectionsCaches.getElectionProcessingType(electionId);
		AsyncProcessingProgress progress = resolveProcessingProgressByType(electionId, processingType);
		appLogger.info("Census processing state requested. electionId={}, processing={}, processingType={}, progressProcessedRows={}, progressTotalRows={}",
				electionId,
				processing,
				processingType,
				progress != null ? progress.getProcessedRows() : null,
				progress != null ? progress.getTotalRows() : null);
		return processing;
	}

	@Override
	public boolean isElectionOrganizationsProcessing(long electionId) {
		boolean processing = ElectionsCaches.isElectionProcessing(electionId);
		AsyncProcessingType processingType = ElectionsCaches.getElectionProcessingType(electionId);
		AsyncProcessingProgress progress = resolveProcessingProgressByType(electionId, processingType);
		appLogger.info("Organizations processing state requested. electionId={}, processing={}, processingType={}, progressProcessedRows={}, progressTotalRows={}",
				electionId,
				processing,
				processingType,
				progress != null ? progress.getProcessedRows() : null,
				progress != null ? progress.getTotalRows() : null);
		return processing;
	}

	@Override
	public AsyncProcessingType getElectionProcessingType(long electionId) {
		return ElectionsCaches.getElectionProcessingType(electionId);
	}

	@Override
	public AsyncProcessingError getElectionCensusProcessingError(long electionId) {
		return ElectionsCaches.getCensusProcessingError(electionId);
	}

	@Override
	public AsyncProcessingProgress getElectionCensusProcessingProgress(long electionId) {
		return ElectionsCaches.getCensusProcessingProgress(electionId);
	}

	@Override
	public AsyncProcessingError getElectionOrganizationsProcessingError(long electionId) {
		return ElectionsCaches.getOrganizationsProcessingError(electionId);
	}

	@Override
	public AsyncProcessingProgress getElectionOrganizationsProcessingProgress(long electionId) {
		return ElectionsCaches.getOrganizationsProcessingProgress(electionId);
	}

	private AsyncProcessingProgress resolveProcessingProgressByType(long electionId, AsyncProcessingType processingType) {
		if (processingType == null) {
			return null;
		}
		if (processingType == AsyncProcessingType.CENSUS) {
			return ElectionsCaches.getCensusProcessingProgress(electionId);
		}
		if (processingType == AsyncProcessingType.ORGANIZATIONS) {
			return ElectionsCaches.getOrganizationsProcessingProgress(electionId);
		}
		return null;
	}

	@Override
	public boolean queueOrganizationsDebtorsUpdate(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startOrganizationsProcessing(electionId);
		if (!queued) {
			appLogger.info("Organizations debtors update already in progress. electionId={}", electionId);
			return false;
		}
			try {
				getManagerProxy().processOrganizationsDebtorsUpdateAsync(contentType, electionId, content, overwriteAll, userAdminId, ip);
				return true;
			} catch (Exception e) {
				ElectionsCaches.finishOrganizationsProcessing(electionId);
				ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
				appLogger.error("Unable to queue organizations debtors async update. electionId={}", electionId, e);
				throw new IllegalStateException("Unable to queue organizations debtors async update", e);
			}
		}

	@Override
	@Asynchronous
	@TransactionTimeout(9000)
	public void processOrganizationsDebtorsUpdateAsync(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip) {
		try {
			ElectionsCaches.markOrganizationsProcessingPreparing(electionId);
			getManagerProxy().updateOrganizationsDebtors(contentType, electionId, content, overwriteAll, userAdminId, ip);
			ElectionsCaches.clearOrganizationsProcessingError(electionId);
			appLogger.info("Organizations debtors async persistence finished. electionId={}", electionId);
		} catch (CensusValidationException e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Async organizations debtors update failed with validation error. electionId={}, messageKey={}", electionId, e.getMessage(), e);
		} catch (Exception e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Async organizations debtors update failed. electionId={}", electionId, e);
		} finally {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			appLogger.info("Organizations processing flag released (debtors). electionId={}", electionId);
		}
	}

	@Override
	@TransactionTimeout(9000)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public OrganizationDebtorImportResult updateOrganizationsDebtors(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip) throws CensusValidationException {
		OrganizationDebtorImportResult result = ExcelUtils.processOrganizationsDebtorsExcel(contentType, content);
		List<Organization> organizations = ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
		java.util.HashMap<String, Organization> organizationsByOrgId = indexOrganizationsByOrgId(organizations);
		validateOrganizationsDebtorsOrgIdsExist(result.getOrgIds(), organizationsByOrgId);
		int totalRows = result.getOrgIds() == null ? 0 : result.getOrgIds().size();
		ElectionsCaches.updateOrganizationsProcessingProgress(electionId, 0, totalRows, 0, 0, 0, 0, totalRows, 0);

		if (overwriteAll) {
			for (Organization organization : organizations) {
				if (organization.isDeudor()) {
					organization.setDeudor(false);
				}
			}
		}

		int updatedRows = 0;
		for (String orgId : result.getOrgIds()) {
			Organization organization = organizationsByOrgId.get(normalizeOrgId(orgId));
			organization.setDeudor(true);
			updatedRows++;
			ElectionsCaches.updateOrganizationsProcessingProgress(electionId, updatedRows, totalRows, 0, updatedRows, 0, 0, totalRows, 0);
		}

		result.setUpdatedRows(updatedRows);
		result.setMissingOrgIds(new ArrayList<>());
		String description = userAdminId.toUpperCase() + " actualizó el listado de organizaciones deudoras. Procesadas: " + result.getProcessedRows() + ", actualizadas: " + updatedRows;
		persistActivity(userAdminId, ActivityType.EDIT_CENSUS, description, ip, electionId);
		return result;
	}

	private java.util.HashMap<String, Organization> indexOrganizationsByOrgId(List<Organization> organizations) {
		java.util.HashMap<String, Organization> organizationsByOrgId = new java.util.HashMap<>();
		for (Organization organization : organizations) {
			organizationsByOrgId.put(normalizeOrgId(organization.getOrgId()), organization);
		}
		return organizationsByOrgId;
	}

	private void validateOrganizationsDebtorsOrgIdsExist(List<String> orgIds, java.util.HashMap<String, Organization> organizationsByOrgId) throws CensusValidationException {
		List<String> missingOrgIds = new ArrayList<>();
		for (String orgId : orgIds) {
			if (!organizationsByOrgId.containsKey(normalizeOrgId(orgId))) {
				missingOrgIds.add(orgId);
			}
		}
		if (!missingOrgIds.isEmpty()) {
			throw new CensusValidationException("organizationsManagementMissingOrgIds", null, String.join(", ", missingOrgIds));
		}
	}

	/**
	 * Get a list of the elections from this year
	 *
	 * @return returns a list election light entity containing the information
	 */
	@Override
	public List<Election> getElectionsLightThisYear() {
		List<Election> elections = ElectionsDaoFactory.createElectionDao(em).getElectionsLightThisYear();
		for (Election election : elections) {
			VotingPeriodResolver.applyVotingWindow(em, election);
		}
		return elections;
	}

	/**
	 * Adds a voter to a particular election
	 *
	 * @param electionId  Identifier of the election
	 * @param userVoter   Entity containing the voter information.
	 * @param userAdminId Identifier of the user performing the action used for logging purposes
	 * @param ip          Ip of the user performing the action, used for logging purposes
	 *
	 * @return returns true if the operation succeeds
	 *
	 */
	@Override
	public boolean addUserVoter(long electionId, UserVoter userVoter, String userAdminId, String ip) throws CensusValidationException {
		userVoter.setMail(normalizeCensusMailForStorage(userVoter.getMail()));
		userVoter.setOrgID(normalizeCensusOrgId(userVoter.getOrgID()));
		userVoter.setCountry(normalizeCountryForStorage(userVoter.getCountry()));
		validateUserVoterCanBeAdded(electionId, userVoter);
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		userVoter.setElection(election);
		userVoter.setVoteToken(StringUtils.createSecureToken());
		em.persist(userVoter);
		election.setElectorsSet(true);
		String description = userAdminId.toUpperCase() + " agregó manualmente a " + userVoter.getName() + " como usuario padrón para la elección " + election.getTitleSpanish();
		persistActivity(userAdminId, ActivityType.ADD_VOTE_USER_MANUAL, description, ip, electionId);
		return true;
	}

	/**
	 * Updates the information of a voter.
	 *
	 * @param userVoter   entity with the voter information
	 * @param userAdminId Identifier of the user performing the action used for logging purposes
	 * @param ip          Ip of the user performing the action, used for logging purposes
	 *
	 */
	@Override
	public void editUserVoter(UserVoter userVoter, String userAdminId, String ip) throws CensusValidationException {
		userVoter.setMail(normalizeCensusMailForStorage(userVoter.getMail()));
		userVoter.setOrgID(normalizeCensusOrgId(userVoter.getOrgID()));
		userVoter.setCountry(normalizeCountryForStorage(userVoter.getCountry()));
		validateUserVoterCanBeEdited(userVoter);
		em.merge(userVoter);
		String description = userAdminId.toUpperCase() + " editó manualmente los datos de un usuario padrón para la elección  " + userVoter.getElection().getTitleSpanish();
		persistActivity(userAdminId, ActivityType.EDIT_VOTE_USER_MANUAL, description, ip, userVoter.getElection().getElectionId());
	}

	/**
	 * Gets all the email templates linked to a particular election.
	 *
	 * @param electionId Identifier of the election
	 *
	 * @return returns a list of election email template entity containing the information.
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
			boolean enabled = Boolean.TRUE.equals(status);
			String message = enabled ? "habilitó el link de votación para la elección " : "deshabilitó el link de votación para la elección ";
			String description = userAdminId.toUpperCase() + " " + message + "(" + election.getTitleSpanish() + ")";
			ActivityType activityType = enabled ? ActivityType.ENABLE_VOTE_LINK : ActivityType.DISABLE_VOTE_LINK;
			persistActivity(userAdminId, activityType, description, ip, election.getElectionId());
		} catch (Exception e1) {
			appLogger.error(e1.getMessage());
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
	 * Updates the information of an admin user.
	 *
	 * @param userAdmin            Entity with the information of the user.
	 * @param email       Original email of the user
	 * @param userAdminId Id of the user, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void editUserAdmin(UserAdmin userAdmin, String email, String userAdminId, String ip) {
		try {
			em.merge(userAdmin);
			String description;
			if (userAdminId.equalsIgnoreCase(userAdmin.getUserAdminId())) {
				description = userAdminId.toUpperCase() + " editó su email de " + email + " a " + userAdmin.getEmail();
			} else {
				description = userAdminId.toUpperCase() + " editó el email del usuario " + userAdmin.getUserAdminId().toUpperCase() + " de " + email + " a " + userAdmin.getEmail();
			}
			persistActivity(userAdminId, ActivityType.EDIT_ADMIN, description, ip, null);
		} catch (Exception e) {
			appLogger.error(e.getMessage());
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
		return ExcelUtils.exportToExcel(getElectionUserVoters(electionId), fileName, true);
	}

	@Override
	public File exportOrganizationsUpsert(long electionId) {
		String fileName = "/organizaciones_alta_actualizacion_" + electionId + ".xlsx";
		return ExcelUtils.exportOrganizationsUpsertToExcel(getOrganizations(electionId), fileName);
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
			appLogger.error(e.getMessage());
		}
	}

	/**
	 * Creates a new admin user
	 *
	 * @param userAdmin   Entity with the information of the new user.
	 * @param userAdminId Id of the user performing the action, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public boolean addUserAdmin(UserAdmin userAdmin, String userAdminId, String ip) {
		try {
			if (ElectionsDaoFactory.createUserAdminDao(em).getUserAdmin(userAdmin.getUserAdminId()) == null) {
				UserAdmin userAdminAux = new UserAdmin(userAdmin.getUserAdminId(), userAdmin.getPassword(), userAdmin.getEmail());
				em.persist(userAdminAux);
				String description = userAdminId.toUpperCase() + " agregó a " + userAdmin.getUserAdminId().toUpperCase() + " como admin";
				persistActivity(userAdminId, ActivityType.ADD_ADMIN, description, ip, null);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Creates a new candidate
	 *
	 * @param electionId  Identifier of the election
	 * @param candidate   Entity with the information of the new candidate
	 * @param userAdminId Id of the user performing the action, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void addCandidate(long electionId, Candidate candidate, String userAdminId, String ip) {
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		candidate.setElection(election);
		candidate.setCandidateType(CandidateType.NORMAL);
		int order = ElectionsDaoFactory.createCandidateDao(em).getLastNonFixedCandidateOrder(electionId);
		candidate.setCandidateOrder(order + 1);
		em.persist(candidate);
		List<ElectionTask> electionTasks = ElectionsDaoFactory.createElectionTaskDao(em).getElectionTasks(electionId);
		for (ElectionTask electionTask : electionTasks) {
			CandidateElectionTaskProgress candidateTaskProgress = new CandidateElectionTaskProgress(candidate, electionTask, CandidateElectionTaskStatus.NOT_STARTED);
			em.persist(candidateTaskProgress);
		}
		election.setCandidatesSet(hasRegularCandidates(electionId));
		em.persist(election);
		String description = userAdminId.toUpperCase() + " agregó un candidato para la elección  " + election.getTitleSpanish();
		persistActivity(userAdminId, ActivityType.ADD_CANDIDATE, description, ip, election.getElectionId());
	}

	@Override
	public void addAbstentionCandidate(long electionId, String userAdminId, String ip) {
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		if (election == null || ElectionsDaoFactory.createCandidateDao(em).getElectionAbstentionCandidate(electionId) != null) {
			return;
		}

		Candidate candidate = new Candidate();
		candidate.setElection(election);
		candidate.setCandidateType(CandidateType.ABSTENTION);
		candidate.setStatus(CandidateStatus.CONFIRMED_AND_PUBLISHED);
		candidate.setReminderFrequency(ReminderFrequency.DISABLED);
		candidate.setCandidateOrder(Constants.MAX_ORDER);
		candidate.setOnlySp(false);
		candidate.setName("Abstención / Abstention / Abstenção");
		String abstentionText = resolveAbstentionDefaultText();
		candidate.setBioSpanish(abstentionText);
		candidate.setBioEnglish(abstentionText);
		candidate.setBioPortuguese(abstentionText);

		try {
			Object[] defaultPhoto = FilesUtils.getDefaultAbstentionPhoto(null);
			candidate.setPictureInfo((byte[]) defaultPhoto[0]);
			candidate.setPictureName((String) defaultPhoto[1]);
			candidate.setPictureExtension((String) defaultPhoto[2]);
		} catch (IOException e) {
			appLogger.error("Could not load default candidate picture for abstention candidate. electionId={}", electionId, e);
			candidate.setPictureInfo(new byte[0]);
			candidate.setPictureName("default_abstention_photo.jpg");
			candidate.setPictureExtension("jpg");
		}

		Candidate currentFirstCandidate = ElectionsDaoFactory.createCandidateDao(em).getElectionFirstCandidate(electionId);
		if (currentFirstCandidate != null) {
			int order = ElectionsDaoFactory.createCandidateDao(em).getLastNonFixedCandidateOrder(electionId) + 1;
			currentFirstCandidate.setCandidateOrder(order);
			em.persist(currentFirstCandidate);
		}

		em.persist(candidate);
		election.setCandidatesSet(hasRegularCandidates(electionId));
		em.persist(election);

		String actor = userAdminId != null && !userAdminId.trim().isEmpty() ? userAdminId.toUpperCase() : SYSTEM_ACTOR;
		String description = actor + " agregó la opción de abstención para la elección " + election.getTitleSpanish();
		persistActivity(actor, ActivityType.ADD_CANDIDATE, description, ip, election.getElectionId());
	}

	private String resolveAbstentionDefaultText() {
		String configuredValue = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.ABSTENTION_DEFAULT_TEXT);
		if (configuredValue == null) {
			return Constants.DEFAULT_ABSTENTION_TEXT;
		}
		String normalizedValue = configuredValue.trim();
		return normalizedValue.isEmpty() ? Constants.DEFAULT_ABSTENTION_TEXT : normalizedValue;
	}

	/**
	 * Deletes a candidate
	 *
	 * @param candidateId Identifier of the candidate
	 * @param userAdminId Id of the user performing the action, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void removeCandidate(long candidateId, String userAdminId, String ip) {
		Candidate candidate = em.find(Candidate.class, candidateId);
		em.remove(candidate);
		String description = userAdminId.toUpperCase() + " eliminó al candidato " + candidate.getName() + TEXT_ELECCION + candidate.getElection().getTitleSpanish();
		persistActivity(userAdminId, ActivityType.REMOVE_CANDIDATE, description, ip, candidate.getElection().getElectionId());

		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(candidate.getElection().getElectionId());
		election.setCandidatesSet(hasRegularCandidatesAfterRemoval(candidate));
		em.persist(election);
	}

	private boolean hasRegularCandidates(long electionId) {
		return ElectionsDaoFactory.createCandidateDao(em).countElectionCandidatesExcludingAbstention(electionId) > 0;
	}

	private boolean hasRegularCandidatesAfterRemoval(Candidate candidate) {
		if (candidate == null || candidate.getElection() == null) {
			return false;
		}
		long currentCount = ElectionsDaoFactory.createCandidateDao(em).countElectionCandidatesExcludingAbstention(candidate.getElection().getElectionId());
		return currentCount > 0;
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

	@Override
	public Map<ElectionTaskKey, CandidateElectionTaskStatus> getCandidateSupportTaskStatuses(long candidateId) {
		Map<ElectionTaskKey, CandidateElectionTaskStatus> statuses = new EnumMap<>(ElectionTaskKey.class);
		if (candidateId <= 0L) {
			return statuses;
		}
		for (CandidateElectionTaskProgress progress : ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).getByCandidateId(candidateId)) {
			if (progress == null || progress.getElectionTask() == null || progress.getElectionTask().getTaskKey() == null || progress.getStatus() == null) {
				continue;
			}
			ElectionTaskKey taskKey = progress.getElectionTask().getTaskKey();
			if (taskKey == ElectionTaskKey.ORG_SUPPORTS || taskKey == ElectionTaskKey.USER_SUPPORTS_2 || taskKey == ElectionTaskKey.USER_SUPPORTS_5) {
				statuses.put(taskKey, progress.getStatus());
			}
		}
		return statuses;
	}

	@Override
	public Nomination getNominationByCandidateId(long candidateId) {
		return ElectionsDaoFactory.createNominationDao(em).getNominationByCandidateId(candidateId);
	}

	@Override
	public List<SupportNomination> getCandidateSupportNominations(long candidateId) {
		if (candidateId <= 0L) {
			return Collections.emptyList();
		}
		return ElectionsDaoFactory.createSupportNominationDao(em).getCandidateSupportNominations(candidateId);
	}

	@Override
	public boolean rejectCandidateSupport(long candidateId, long supportNominationId, String comment, String userAdminId, String ip) {
		return transitionCandidateSupport(candidateId, supportNominationId, SupportStatus.REJECTED, comment, userAdminId, ip);
	}

	@Override
	public boolean approveRejectedCandidateSupport(long candidateId, long supportNominationId, String comment, String userAdminId, String ip) {
		return transitionCandidateSupport(candidateId, supportNominationId, SupportStatus.ACCEPTED, comment, userAdminId, ip);
	}

	@Override
	public boolean returnRejectedCandidateSupportToProposal(long candidateId, long supportNominationId, String comment, String userAdminId, String ip) {
		return transitionCandidateSupport(candidateId, supportNominationId, SupportStatus.PROPOSED, comment, userAdminId, ip);
	}

	private boolean transitionCandidateSupport(long candidateId, long supportNominationId, SupportStatus targetStatus,
			String comment, String userAdminId, String ip) {
		String normalizedComment = normalizeAdministrativeSupportComment(comment);
		if (candidateId <= 0L || supportNominationId <= 0L || normalizedComment == null
				|| !isAdministrativeSupportTargetStatus(targetStatus)) {
			return false;
		}

		SupportNominationDao supportNominationDao = ElectionsDaoFactory.createSupportNominationDao(em);
		SupportNomination supportSnapshot = supportNominationDao.getSupportNomination(supportNominationId);
		if (!belongsToCandidate(supportSnapshot, candidateId)) {
			return false;
		}
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidateForUpdate(candidateId);
		if (candidate == null) {
			return false;
		}
		SupportNomination supportNomination = supportNominationDao.getSupportNominationForUpdate(supportNominationId);
		if (!belongsToCandidate(supportNomination, candidateId) || supportNomination.getSupportStatus() == null
				|| supportNomination.getSupportStatus() == targetStatus) {
			return false;
		}

		Nomination nomination = supportNomination.getNomination();
		if (nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || isTerminalCandidateStatus(candidate.getStatus())) {
			return false;
		}
		if (targetStatus == SupportStatus.ACCEPTED && !isOrganizationSupportEligibleForAdministrativeApproval(supportNomination, supportNominationDao)) {
			return false;
		}

		List<SupportNomination> candidateSupports = supportNominationDao.getCandidateSupportNominations(candidateId);
		CandidateElectionTaskProgress organizationTask = null;
		CandidateElectionTaskProgress userTwoTask = null;
		CandidateElectionTaskProgress userFiveTask = null;
		if (targetStatus != SupportStatus.REJECTED) {
			CandidateElectionTaskProgressDao taskProgressDao = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em);
			organizationTask = taskProgressDao.getByCandidateIdAndTaskKeyForUpdate(candidateId, ElectionTaskKey.ORG_SUPPORTS);
			userTwoTask = taskProgressDao.getByCandidateIdAndTaskKeyForUpdate(candidateId, ElectionTaskKey.USER_SUPPORTS_2);
			userFiveTask = taskProgressDao.getByCandidateIdAndTaskKeyForUpdate(candidateId, ElectionTaskKey.USER_SUPPORTS_5);
			if (!hasAdministrativeSupportCapacity(supportNomination, candidateSupports, userFiveTask)) {
				return false;
			}
		}

		SupportStatus previousStatus = supportNomination.getSupportStatus();
		Date now = new Date();
		supportNomination.setSupportStatus(targetStatus);
		supportNomination.setSupportResponseInstant(targetStatus == SupportStatus.PROPOSED ? null : now);
		em.merge(supportNomination);
		if (targetStatus == SupportStatus.REJECTED) {
			reopenCandidateSupportTasksWhenThresholdIsNotMet(candidate, candidateSupports);
		} else {
			reconcileCandidateSupportTasks(candidateSupports, organizationTask, userTwoTask, userFiveTask, supportNomination, now);
		}

		String actor = hasText(userAdminId) ? userAdminId.trim().toUpperCase(Locale.ROOT) : SYSTEM_ACTOR;
		String actionDescription = administrativeSupportActionDescription(targetStatus);
		String description = actor + actionDescription + "supportNominationId=" + supportNominationId
				+ ACTIVITY_CANDIDATE_ID_FRAGMENT + candidateId
				+ ", previousStatus=" + previousStatus.name()
				+ ", newStatus=" + targetStatus.name()
				+ ", comment=" + normalizedComment;
		persistActivity(actor, ActivityType.EDIT_CANDIDATES, description, ip, nomination.getElection().getElectionId());
		return true;
	}

	private boolean isAdministrativeSupportTargetStatus(SupportStatus targetStatus) {
		return targetStatus == SupportStatus.REJECTED || targetStatus == SupportStatus.ACCEPTED || targetStatus == SupportStatus.PROPOSED;
	}

	private String administrativeSupportActionDescription(SupportStatus targetStatus) {
		if (targetStatus == SupportStatus.REJECTED) {
			return " rechazó administrativamente un apoyo. ";
		}
		if (targetStatus == SupportStatus.ACCEPTED) {
			return " aprobó administrativamente un apoyo. ";
		}
		return " devolvió administrativamente un apoyo a propuesta. ";
	}

	private boolean isOrganizationSupportEligibleForAdministrativeApproval(SupportNomination supportNomination, SupportNominationDao supportNominationDao) {
		Organization supportingOrganization = supportNomination.getSupportingOrganization();
		if (supportingOrganization == null) {
			return true;
		}
		Nomination nomination = supportNomination.getNomination();
		if (nomination == null || nomination.getElection() == null || supportingOrganization.getElection() == null
				|| supportingOrganization.getElection().getElectionId() != nomination.getElection().getElectionId()
				|| supportingOrganization.isDeudor()) {
			return false;
		}
		return !supportNominationDao.existsOrganizationGrantedSupportInElection(
				nomination.getElection().getElectionId(), supportingOrganization.getId(), nomination.getId());
	}

	private boolean hasAdministrativeSupportCapacity(SupportNomination targetSupport, List<SupportNomination> supports, CandidateElectionTaskProgress userFiveTask) {
		boolean organizationSupport = targetSupport.getSupportingOrganization() != null;
		int activeSupports = countActiveSupportsExcluding(supports, organizationSupport, targetSupport.getId());
		if (organizationSupport) {
			return activeSupports < Constants.CANDIDATE_ORGANIZATION_SUPPORTS_REQUIRED;
		}
		int maximumUserSupports = isApplicableTask(userFiveTask)
				? Constants.CANDIDATE_USER_SUPPORTS_5_REQUIRED
				: Constants.CANDIDATE_USER_SUPPORTS_2_REQUIRED;
		return activeSupports < maximumUserSupports;
	}

	private int countActiveSupportsExcluding(List<SupportNomination> supports, boolean organizationSupport, long excludedSupportId) {
		if (supports == null || supports.isEmpty()) {
			return 0;
		}
		int count = 0;
		for (SupportNomination support : supports) {
			if (support == null || support.getId() == excludedSupportId) {
				continue;
			}
			boolean currentOrganizationSupport = support.getSupportingOrganization() != null;
			if (currentOrganizationSupport != organizationSupport) {
				continue;
			}
			SupportStatus status = support.getSupportStatus();
			if (status == SupportStatus.PROPOSED || status == SupportStatus.ACCEPTED || status == SupportStatus.APPROVED) {
				count++;
			}
		}
		return count;
	}

	private boolean isApplicableTask(CandidateElectionTaskProgress taskProgress) {
		return taskProgress != null && taskProgress.getStatus() != CandidateElectionTaskStatus.OMITTED;
	}

	private boolean belongsToCandidate(SupportNomination supportNomination, long candidateId) {
		if (supportNomination == null || supportNomination.getNomination() == null || supportNomination.getNomination().getCandidate() == null) {
			return false;
		}
		Candidate candidate = supportNomination.getNomination().getCandidate();
		return candidate.getCandidateId() == candidateId
				&& candidate.getElection() != null
				&& supportNomination.getNomination().getElection() != null
				&& candidate.getElection().getElectionId() == supportNomination.getNomination().getElection().getElectionId();
	}

	private boolean isTerminalCandidateStatus(CandidateStatus status) {
		return status == CandidateStatus.COMPLETE || status == CandidateStatus.REJECTED || status == CandidateStatus.CONFIRMED_AND_PUBLISHED;
	}

	private void reopenCandidateSupportTasksWhenThresholdIsNotMet(Candidate candidate, List<SupportNomination> supports) {
		if (candidate == null) {
			return;
		}
		CandidateElectionTaskProgressDao taskProgressDao = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em);
		CandidateElectionTaskProgress organizationTask = taskProgressDao.getByCandidateIdAndTaskKeyForUpdate(candidate.getCandidateId(), ElectionTaskKey.ORG_SUPPORTS);
		CandidateElectionTaskProgress userTwoTask = taskProgressDao.getByCandidateIdAndTaskKeyForUpdate(candidate.getCandidateId(), ElectionTaskKey.USER_SUPPORTS_2);
		CandidateElectionTaskProgress userFiveTask = taskProgressDao.getByCandidateIdAndTaskKeyForUpdate(candidate.getCandidateId(), ElectionTaskKey.USER_SUPPORTS_5);
		int organizationSupports = countAcceptedSupports(supports, true);
		int userSupports = countAcceptedSupports(supports, false);
		Date now = new Date();
		reopenCompletedCandidateSupportTask(organizationTask, organizationSupports, Constants.CANDIDATE_ORGANIZATION_SUPPORTS_REQUIRED, now);
		reopenCompletedCandidateSupportTask(userTwoTask, userSupports, Constants.CANDIDATE_USER_SUPPORTS_2_REQUIRED, now);
		reopenCompletedCandidateSupportTask(userFiveTask, userSupports, Constants.CANDIDATE_USER_SUPPORTS_5_REQUIRED, now);
	}

	private void reconcileCandidateSupportTasks(List<SupportNomination> supports, CandidateElectionTaskProgress organizationTask,
			CandidateElectionTaskProgress userTwoTask, CandidateElectionTaskProgress userFiveTask, SupportNomination targetSupport, Date now) {
		int organizationSupports = countAcceptedSupports(supports, true);
		int userSupports = countAcceptedSupports(supports, false);
		if (targetSupport.getSupportingOrganization() != null) {
			reconcileCandidateSupportTask(organizationTask, organizationSupports, Constants.CANDIDATE_ORGANIZATION_SUPPORTS_REQUIRED, now);
			return;
		}

		boolean userTwoApplicable = isApplicableTask(userTwoTask);
		if (userTwoApplicable) {
			reconcileCandidateSupportTask(userTwoTask, userSupports, Constants.CANDIDATE_USER_SUPPORTS_2_REQUIRED, now);
		}
		if (!userTwoApplicable || userSupports >= Constants.CANDIDATE_USER_SUPPORTS_2_REQUIRED) {
			reconcileCandidateSupportTask(userFiveTask, userSupports, Constants.CANDIDATE_USER_SUPPORTS_5_REQUIRED, now);
		}
	}

	private int countAcceptedSupports(List<SupportNomination> supports, boolean organizationSupport) {
		if (supports == null || supports.isEmpty()) {
			return 0;
		}
		int count = 0;
		for (SupportNomination support : supports) {
			if (support == null || (support.getSupportingOrganization() != null) != organizationSupport) {
				continue;
			}
			if (support.getSupportStatus() == SupportStatus.ACCEPTED || support.getSupportStatus() == SupportStatus.APPROVED) {
				count++;
			}
		}
		return count;
	}

	private void reconcileCandidateSupportTask(CandidateElectionTaskProgress taskProgress, int currentSupports, int requiredSupports, Date now) {
		if (taskProgress == null || taskProgress.getStatus() == CandidateElectionTaskStatus.OMITTED) {
			return;
		}
		CandidateElectionTaskStatus targetStatus;
		if (currentSupports >= requiredSupports) {
			targetStatus = CandidateElectionTaskStatus.COMPLETED;
		} else {
			targetStatus = CandidateElectionTaskStatus.STARTED;
		}
		if (taskProgress.getStatus() == targetStatus) {
			return;
		}
		taskProgress.setStatus(targetStatus);
		if (taskProgress.getStartDate() == null) {
			taskProgress.setStartDate(now);
		}
		taskProgress.setEndDate(now);
		em.merge(taskProgress);
	}

	private void reopenCompletedCandidateSupportTask(CandidateElectionTaskProgress taskProgress, int currentSupports, int requiredSupports, Date now) {
		if (taskProgress == null || taskProgress.getStatus() != CandidateElectionTaskStatus.COMPLETED || currentSupports >= requiredSupports) {
			return;
		}
		taskProgress.setStatus(CandidateElectionTaskStatus.STARTED);
		if (taskProgress.getStartDate() == null) {
			taskProgress.setStartDate(now);
		}
		taskProgress.setEndDate(now);
		em.merge(taskProgress);
	}

	/**
	 * Updates the information of a candidate
	 *
	 * @param candidateId Identifier of the candidate
	 * @param userAdminId Id of the user performing the action, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void editCandidate(Candidate candidate, String userAdminId, String ip) {
		try {
			em.merge(candidate);
			String source = resolveEditCandidateSource(userAdminId);
			String activityUserName = resolveEditCandidateActivityUserName(userAdminId, candidate, source);
			String description = activityUserName + " actualizó los datos de un candidato" + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ". fuente=" + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", nombre=" + safeValue(candidate.getName()) + ", linkedin=" + safeValue(candidate.getLinkedinUrl()) + ", bioEsChars=" + safeLength(candidate.getBioSpanish()) + ", bioEnChars=" + safeLength(candidate.getBioEnglish()) + ", bioPtChars=" + safeLength(candidate.getBioPortuguese());
			persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
			ElectionsCaches.clearPublicElectionSnapshotCache();
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	@Override
	public boolean updateCandidateWinner(long candidateId, boolean winner, String userAdminId, String ip) {
		try {
			Candidate candidate = em.find(Candidate.class, candidateId);
			if (candidate == null || candidate.getElection() == null || candidate.isAbstention()) {
				return false;
			}

			if (candidate.isWinner() == winner) {
				return false;
			}

			candidate.setWinner(winner);
			em.merge(candidate);

			String actor = userAdminId != null && !userAdminId.trim().isEmpty() ? userAdminId.toUpperCase() : SYSTEM_ACTOR;
			String description;
			if (winner) {
				description = actor + " marcó al candidato " + safeValue(candidate.getName()) + " (id=" + candidate.getCandidateId() + ") como ganador" + TEXT_ELECCION + candidate.getElection().getTitleSpanish();
			} else {
				description = actor + " quitó la marca de ganador al candidato " + safeValue(candidate.getName()) + " (id=" + candidate.getCandidateId() + ")" + TEXT_ELECCION + candidate.getElection().getTitleSpanish();
			}
			persistActivity(actor, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
			ElectionsCaches.clearPublicElectionSnapshotCache();
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public void editNomination(Nomination nomination, String userAdminId, String ip) {
		try {
			em.merge(nomination);
			String actor = userAdminId != null ? userAdminId.toUpperCase() : "SYSTEM";
			long electionId = nomination != null && nomination.getElection() != null ? nomination.getElection().getElectionId() : 0L;
			String electionTitle = nomination != null && nomination.getElection() != null ? nomination.getElection().getTitleSpanish() : "-";
			String description = actor + " actualizó el motivo de nominación" + TEXT_ELECCION + electionTitle
					+ ACTIVITY_NOMINATION_ID_FRAGMENT + (nomination != null ? nomination.getId() : "-")
					+ ", nominationReasonEsChars=" + safeLength(nomination != null ? nomination.getNominationReasonSpanish() : null)
					+ ", nominationReasonEnChars=" + safeLength(nomination != null ? nomination.getNominationReasonEnglish() : null)
					+ ", nominationReasonPtChars=" + safeLength(nomination != null ? nomination.getNominationReasonPortuguese() : null);
			persistActivity(actor, ActivityType.EDIT_ELECTION, description, ip, electionId);
			ElectionsCaches.clearPublicElectionSnapshotCache();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

	@Override
	@TransactionTimeout(7200)
	public CandidateBioMigrationBatchResult migrateCandidateBiosHtmlToPlainText(String userAdminId, String ip) {
		CandidateBioMigrationBatchResult result = new CandidateBioMigrationBatchResult();
		long totalCandidates = ElectionsDaoFactory.createCandidateDao(em).countAllCandidates();
		result.setTotalCandidates(totalCandidates);

		for (int firstResult = 0; firstResult < totalCandidates; firstResult += CANDIDATE_BIO_MIGRATION_BATCH_SIZE) {
			List<Long> candidateIds = ElectionsDaoFactory.createCandidateDao(em).getCandidateIds(CANDIDATE_BIO_MIGRATION_BATCH_SIZE, firstResult);
			if (candidateIds.isEmpty()) {
				break;
			}

			for (Long candidateId : candidateIds) {
				migrateSingleCandidateBio(candidateId, result);
			}

			em.flush();
			em.clear();
			appLogger.info("Candidate bio migration progress. processedCandidates={}, totalCandidates={}, updatedCandidates={}, skippedCandidates={}, failedCandidates={}",
					Math.min((long) firstResult + candidateIds.size(), totalCandidates), totalCandidates, result.getUpdatedCandidates(), result.getSkippedCandidates(), result.getFailedCandidates());
		}

		ElectionsCaches.clearPublicElectionSnapshotCache();
		persistCandidateBioMigrationActivity(userAdminId, ip, result);
		return result;
	}

	private void migrateSingleCandidateBio(Long candidateId, CandidateBioMigrationBatchResult result) {
		if (candidateId == null || candidateId.longValue() <= 0L) {
			result.incrementFailedCandidates();
			return;
		}

		Candidate candidate = em.find(Candidate.class, candidateId.longValue());
		if (candidate == null) {
			result.incrementFailedCandidates();
			return;
		}

		String migratedBioSpanish = migrateCandidateBioField(candidate.getBioSpanish());
		String migratedBioEnglish = migrateCandidateBioField(candidate.getBioEnglish());
		String migratedBioPortuguese = migrateCandidateBioField(candidate.getBioPortuguese());
		if (safeEquals(candidate.getBioSpanish(), migratedBioSpanish)
				&& safeEquals(candidate.getBioEnglish(), migratedBioEnglish)
				&& safeEquals(candidate.getBioPortuguese(), migratedBioPortuguese)) {
			result.incrementSkippedCandidates();
			return;
		}

		candidate.setBioSpanish(migratedBioSpanish);
		candidate.setBioEnglish(migratedBioEnglish);
		candidate.setBioPortuguese(migratedBioPortuguese);
		em.merge(candidate);
		result.incrementUpdatedCandidates();
	}

	private String migrateCandidateBioField(String bio) {
		if (!shouldConvertHtmlBio(bio)) {
			return bio;
		}
		return htmlToRenderedPlainText(bio);
	}

	private boolean shouldConvertHtmlBio(String value) {
		if (!hasText(value)) {
			return false;
		}
		return HTML_TAG_PATTERN.matcher(value).find() || HTML_ENTITY_PATTERN.matcher(value).find();
	}

	private String htmlToRenderedPlainText(String html) {
		if (!hasText(html)) {
			return html;
		}

		String normalized = html.replace("\r\n", "\n").replace('\r', '\n');
		normalized = HTML_SCRIPT_STYLE_PATTERN.matcher(normalized).replaceAll(" ");
		normalized = HTML_BREAK_PATTERN.matcher(normalized).replaceAll("\n");
		normalized = HTML_LIST_ITEM_PATTERN.matcher(normalized).replaceAll("\n- ");
		normalized = HTML_CLOSING_LI_PATTERN.matcher(normalized).replaceAll("\n");
		normalized = HTML_BLOCK_PATTERN.matcher(normalized).replaceAll("\n");
		normalized = HTML_TAG_PATTERN.matcher(normalized).replaceAll(" ");
		normalized = decodeHtmlEntities(normalized);
		normalized = normalized.replace('\u00A0', ' ');
		normalized = normalized.replaceAll("[ \\t\\x0B\\f]+", " ");
		normalized = normalized.replaceAll(" *\\n *", "\n");
		normalized = normalized.replaceAll("\\n{3,}", "\n\n");
		normalized = normalized.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	private String decodeHtmlEntities(String value) {
		if (value == null || value.indexOf('&') < 0) {
			return value;
		}

		String normalized = value
				.replace("&nbsp;", " ")
				.replace("&#160;", " ")
				.replace("&amp;", "&")
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&quot;", "\"")
				.replace("&#34;", "\"")
				.replace("&#39;", "'")
				.replace("&apos;", "'")
				.replace("&ndash;", "-")
				.replace("&mdash;", "-")
				.replace("&bull;", "-")
				.replace("&middot;", "-")
				.replace("&hellip;", "...")
				.replace("&copy;", "(c)")
				.replace("&reg;", "(R)")
				.replace("&trade;", "TM");

		Matcher matcher = HTML_NUMERIC_ENTITY_PATTERN.matcher(normalized);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String rawCode = matcher.group(1);
			String replacement = matcher.group(0);
			try {
				int codePoint;
				if (rawCode != null && (rawCode.startsWith("x") || rawCode.startsWith("X"))) {
					codePoint = Integer.parseInt(rawCode.substring(1), 16);
				} else {
					codePoint = Integer.parseInt(rawCode, 10);
				}
				replacement = Character.isValidCodePoint(codePoint) ? new String(Character.toChars(codePoint)) : "";
			} catch (Exception e) {
				replacement = matcher.group(0);
			}
			matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	private boolean safeEquals(String left, String right) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null || right == null) {
			return false;
		}
		return left.equals(right);
	}

	private void persistCandidateBioMigrationActivity(String userAdminId, String ip, CandidateBioMigrationBatchResult result) {
		try {
			String actor = userAdminId != null && !userAdminId.trim().isEmpty() ? userAdminId.toUpperCase(Locale.ROOT) : SYSTEM_ACTOR;
			String description = actor + " ejecutó la migración masiva de bios HTML a texto plano. total=" + result.getTotalCandidates() + ACTIVITY_UPDATED_ROWS_FRAGMENT + result.getUpdatedCandidates()
					+ ", omitidas=" + result.getSkippedCandidates() + ", errores=" + result.getFailedCandidates();
			persistActivity(actor, ActivityType.EDIT_CANDIDATES, description, ip, null);
		} catch (Exception e) {
			appLogger.error("Unable to persist candidate bio migration activity", e);
		}
	}

	@Override
	@TransactionTimeout(7200)
	public CandidatePhotoBatchResult optimizeAllCandidatePhotos(String userAdminId, String ip) {
		CandidatePhotoBatchResult result = new CandidatePhotoBatchResult();
		long totalCandidates = ElectionsDaoFactory.createCandidateDao(em).countAllCandidates();
		result.setTotalCandidates(totalCandidates);

		for (int firstResult = 0; firstResult < totalCandidates; firstResult += CANDIDATE_PHOTO_OPTIMIZATION_BATCH_SIZE) {
			List<Long> candidateIds = ElectionsDaoFactory.createCandidateDao(em).getCandidateIds(CANDIDATE_PHOTO_OPTIMIZATION_BATCH_SIZE, firstResult);
			if (candidateIds.isEmpty()) {
				break;
			}

			for (Long candidateId : candidateIds) {
				optimizeSingleCandidatePhoto(candidateId, result);
			}

			em.flush();
			em.clear();
			appLogger.info("Candidate photo optimization progress. processedCandidates={}, totalCandidates={}, updatedCandidates={}, skippedCandidates={}, failedCandidates={}",
					Math.min((long) firstResult + candidateIds.size(), totalCandidates), totalCandidates, result.getUpdatedCandidates(), result.getSkippedCandidates(), result.getFailedCandidates());
		}

		ElectionsCaches.clearPublicElectionSnapshotCache();
		persistCandidatePhotoOptimizationActivity(userAdminId, ip, result);
		return result;
	}

	private void optimizeSingleCandidatePhoto(Long candidateId, CandidatePhotoBatchResult result) {
		if (candidateId == null || candidateId.longValue() <= 0L) {
			result.incrementFailedCandidates();
			return;
		}

		Candidate candidate = em.find(Candidate.class, candidateId.longValue());
		if (candidate == null) {
			result.incrementFailedCandidates();
			return;
		}

		byte[] currentPictureInfo = candidate.getPictureInfo();
		long currentBytes = currentPictureInfo != null ? currentPictureInfo.length : 0L;
		result.addOriginalBytes(currentBytes);

		if (currentPictureInfo == null || currentPictureInfo.length == 0) {
			result.incrementSkippedCandidates();
			result.addResultingBytes(currentBytes);
			return;
		}

		PublicPhotoResizeProcessor.ProcessingResult processingResult = PublicPhotoResizeProcessor.process(currentPictureInfo, candidate.getPictureName());
		if (!processingResult.isSuccess()) {
			result.incrementFailedCandidates();
			result.addResultingBytes(currentBytes);
			appLogger.warn("Candidate photo optimization failed. candidateId={}, reason={}", candidateId, processingResult.getFailureReason());
			return;
		}

		byte[] optimizedPictureInfo = processingResult.getOutputBytes();
		long optimizedBytes = optimizedPictureInfo != null ? optimizedPictureInfo.length : 0L;
		if (shouldSkipCandidatePhotoUpdate(candidate, processingResult, currentBytes, optimizedBytes)) {
			result.incrementSkippedCandidates();
			result.addResultingBytes(currentBytes);
			return;
		}

		candidate.setPictureInfo(optimizedPictureInfo);
		candidate.setPictureName(processingResult.getOutputFileName());
		candidate.setPictureExtension("jpg");
		em.merge(candidate);

		result.incrementUpdatedCandidates();
		result.addResultingBytes(optimizedBytes);
	}

	private boolean shouldSkipCandidatePhotoUpdate(Candidate candidate, PublicPhotoResizeProcessor.ProcessingResult processingResult, long currentBytes, long optimizedBytes) {
		if (candidate == null || processingResult == null) {
			return false;
		}
		if (!"jpg".equalsIgnoreCase(candidate.getPictureExtension())) {
			return false;
		}
		if (currentBytes <= 0L || optimizedBytes < currentBytes) {
			return false;
		}
		if (!safeEqualsIgnoreCase(candidate.getPictureName(), processingResult.getOutputFileName())) {
			return false;
		}

		BufferedImage currentImage = readBufferedImage(candidate.getPictureInfo());
		return currentImage != null
				&& currentImage.getWidth() == PublicPhotoResizeProcessor.TARGET_SIZE_PIXELS
				&& currentImage.getHeight() == PublicPhotoResizeProcessor.TARGET_SIZE_PIXELS;
	}

	private BufferedImage readBufferedImage(byte[] pictureInfo) {
		if (pictureInfo == null || pictureInfo.length == 0) {
			return null;
		}
		try {
			return ImageIO.read(new ByteArrayInputStream(pictureInfo));
		} catch (IOException e) {
			appLogger.warn("Unable to inspect candidate image bytes", e);
			return null;
		}
	}

	private boolean safeEqualsIgnoreCase(String left, String right) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null || right == null) {
			return false;
		}
		return left.trim().equalsIgnoreCase(right.trim());
	}

	private void persistCandidatePhotoOptimizationActivity(String userAdminId, String ip, CandidatePhotoBatchResult result) {
		try {
			String actor = userAdminId != null && !userAdminId.trim().isEmpty() ? userAdminId.toUpperCase(Locale.ROOT) : SYSTEM_ACTOR;
			String description = actor + " ejecutó la optimización masiva de fotos de candidatos. total=" + result.getTotalCandidates() + ACTIVITY_UPDATED_ROWS_FRAGMENT + result.getUpdatedCandidates()
					+ ", omitidas=" + result.getSkippedCandidates() + ", errores=" + result.getFailedCandidates() + ", bytesOriginales=" + result.getOriginalTotalBytes()
					+ ", bytesFinales=" + result.getResultingTotalBytes() + ", bytesAhorrados=" + result.getSavedBytes();
			persistActivity(actor, ActivityType.EDIT_CANDIDATES, description, ip, null);
		} catch (Exception e) {
			appLogger.error("Error persisting candidate photo optimization activity", e);
		}
	}

	@Override
	public boolean updateCandidateStatus(long candidateId, CandidateStatus newStatus, String comment, String userAdminId, String ip) {
		return updateCandidateStatus(candidateId, newStatus, comment, true, userAdminId, ip);
	}

	@Override
	public boolean updateCandidateStatus(long candidateId, CandidateStatus newStatus, String comment, boolean sendConfirmedAndPublishedEmail, String userAdminId, String ip) {
		try {
			if (newStatus == null) {
				return false;
			}

			Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidateForUpdate(candidateId);
			if (candidate == null || candidate.getElection() == null) {
				return false;
			}

			CandidateStatus previousStatus = candidate.getStatus();
			if (previousStatus == newStatus) {
				return false;
			}

			candidate.setStatus(newStatus);
			em.merge(candidate);

			if (sendConfirmedAndPublishedEmail
					&& newStatus == CandidateStatus.CONFIRMED_AND_PUBLISHED
					&& previousStatus != CandidateStatus.CONFIRMED_AND_PUBLISHED) {
				EJBFactory.getInstance().getMailsSendingEJB().queueCandidateConfirmedAndPublishedToCandidate(candidate.getCandidateId());
			}

			String actor = userAdminId != null && !userAdminId.trim().isEmpty() ? userAdminId.toUpperCase() : SYSTEM_ACTOR;
			String normalizedComment = normalizeCandidateStatusComment(comment);
			StringBuilder description = new StringBuilder(256);
			description.append(actor).append(" cambió estado del candidato ").append(safeValue(candidate.getName())).append(" (id=").append(candidate.getCandidateId()).append(") de ").append(previousStatus != null ? previousStatus.name() : "-").append(" a ").append(newStatus.name()).append(TEXT_ELECCION).append(candidate.getElection().getTitleSpanish());
			if (newStatus == CandidateStatus.CONFIRMED_AND_PUBLISHED && previousStatus != CandidateStatus.CONFIRMED_AND_PUBLISHED) {
				description.append(". enviarCorreoConfirmadoPublicado=").append(sendConfirmedAndPublishedEmail ? "SI" : "NO");
			}
			if (normalizedComment != null) {
				description.append(". comentario=").append(normalizedComment);
			}
			persistActivity(actor, ActivityType.EDIT_CANDIDATES, description.toString(), ip, candidate.getElection().getElectionId());
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	private String resolveEditCandidateActivityUserName(String userAdminId, Candidate candidate, String source) {
		if (ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source)) {
			if (candidate != null && candidate.getMail() != null && !candidate.getMail().trim().isEmpty()) {
				return candidate.getMail().trim();
			}
			if (candidate != null) {
				return CANDIDATE_ACTIVITY_PREFIX + candidate.getCandidateId();
			}
			return ACTIVITY_SOURCE_CANDIDATE_LINK;
		}
		return userAdminId.toUpperCase();
	}

	private String resolveEditCandidateSource(String userAdminId) {
		if (isCandidateLinkEdition(userAdminId)) {
			return ACTIVITY_SOURCE_CANDIDATE_LINK;
		}
		return ACTIVITY_SOURCE_ADMIN_PANEL;
	}

	private boolean isCandidateLinkEdition(String userAdminId) {
		return userAdminId == null || userAdminId.trim().isEmpty() || ACTIVITY_SOURCE_CANDIDATE_LINK.equalsIgnoreCase(userAdminId.trim()) || userAdminId.toUpperCase().startsWith(CANDIDATE_ACTIVITY_PREFIX);
	}

	private String safeValue(String value) {
		return value == null ? "-" : value;
	}

	private int safeLength(String value) {
		return value == null ? 0 : value.length();
	}

	private boolean hasBinary(byte[] value) {
		return value != null && value.length > 0;
	}

	private String booleanText(boolean value) {
		return value ? "SI" : "NO";
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private boolean hasAnyCampusCourse(Election election) {
		if (election == null) {
			return false;
		}
		return election.getCampusCourse() != null
				|| election.getCampusCourseEnglish() != null
				|| election.getCampusCoursePortuguese() != null;
	}

	private String normalizeCandidateStatusComment(String comment) {
		if (!hasText(comment)) {
			return null;
		}
		String trimmed = comment.trim();
		return trimmed.length() > 4000 ? trimmed.substring(0, 4000) : trimmed;
	}

	private String normalizeAdministrativeSupportComment(String comment) {
		if (!hasText(comment)) {
			return null;
		}
		String trimmed = comment.trim();
		return trimmed.length() <= Constants.ADMINISTRATIVE_SUPPORT_REJECTION_REASON_MAX_LENGTH ? trimmed : null;
	}

	/**
	 * Creates a new auditor and associates it to an election
	 *
	 * @param electionId    Identifier of the election
	 * @param auditor       Entity containing the auditor information
	 * @param electionTitle Title of the election where the auditor will be added, used for logging purposes
	 * @param userAdminId   Id of the user performing the action, used for logging purposes
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
	 * @param userAdminId Id of the user performing the action, used for logging purposes
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
	 * @param userAdminId Id of the user performing the action, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void editAuditor(Auditor auditor, String userAdminId, String ip) {
		try {
			em.merge(auditor);
			String description = userAdminId.toUpperCase() + " actualizó los datos de un auditor para la elección " + auditor.getElection().getTitleSpanish();
			persistActivity(userAdminId, ActivityType.EDIT_AUDITOR, description, ip, auditor.getElection().getElectionId());
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	/**
	 * Updates the election flag for set auditors
	 *
	 * @param electionId    Identifier of the election
	 * @param electionTitle Title of the election, used for logging purposes
	 * @param userAdminId   Id of the user performing the action, used for logging purposes
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
			appLogger.error(e.getMessage());
		}
	}

	@Override
	public void persistElectionOrganizationsSet(long electionId, String electionTitle, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setOrganizationsSet(true);
			em.persist(election);
			String description = userAdminId.toUpperCase() + " marcó organizaciones como completadas para la elección " + electionTitle;
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	@Override
	public void persistElectionCalendarSet(long electionId, String electionTitle, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setCalendarSet(true);
			em.persist(election);
			String description = userAdminId.toUpperCase() + " marcó calendario como completado para la elección " + electionTitle;
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	@Override
	public void saveElectionCalendars(long electionId, List<ElectionCalendar> calendars, String electionTitle, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			List<ElectionCalendar> existingCalendars = em.createQuery("SELECT c FROM ElectionCalendar c WHERE c.election.electionId = :electionId", ElectionCalendar.class).setParameter(QueryParameterNames.ELECTION_ID, electionId).getResultList();
			Map<ElectionCalendarKey, List<ElectionCalendar>> existingByKey = new HashMap<>();
			for (ElectionCalendar existing : existingCalendars) {
				if (existing.getCalendarKey() == null) {
					continue;
				}
				existingByKey.computeIfAbsent(existing.getCalendarKey(), k -> new ArrayList<>()).add(existing);
			}

			if (calendars != null) {
				for (ElectionCalendar incoming : calendars) {
					if (incoming == null || incoming.getCalendarKey() == null) {
						continue;
					}
					if (ElectionCalendarKey.N_16_PERIODO_VOTING.equals(incoming.getCalendarKey()) && incoming.getStartDate() == null) {
						continue;
					}
					List<ElectionCalendar> targets = existingByKey.get(incoming.getCalendarKey());
					if (targets == null || targets.isEmpty()) {
						ElectionCalendar target = new ElectionCalendar(election, incoming.getCalendarKey(), incoming.getStartDate(), incoming.getEndDate(), incoming.isPublicable());
						em.persist(target);
						existingByKey.computeIfAbsent(incoming.getCalendarKey(), k -> new ArrayList<>()).add(target);
						continue;
					}
					for (ElectionCalendar target : targets) {
						if (target == null) {
							continue;
						}
						target.setStartDate(incoming.getStartDate());
						target.setEndDate(incoming.getEndDate());
						target.setPublicable(incoming.isPublicable());
					}
				}
			}

			em.persist(election);
			String description = userAdminId.toUpperCase() + " guardó fechas del calendario para la elección " + electionTitle;
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

	@Override
	public void persistElectionTasksSet(long electionId, String electionTitle, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setTasksSet(true);
			em.persist(election);
			String description = userAdminId.toUpperCase() + " marcó tareas como completadas para la elección " + electionTitle;
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	@Override
	public void persistElectionCallSet(long electionId, String electionTitle, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setCallSet(true);
			em.persist(election);
			String description = userAdminId.toUpperCase() + " marcó convocatoria como completada para la elección " + electionTitle;
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	@Override
	public void persistElectionElectorsSet(long electionId, String electionTitle, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setElectorsSet(true);
			em.persist(election);
			String description = userAdminId.toUpperCase() + " marcó padrón como completado para la elección " + electionTitle;
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	@Override
	public void persistElectionCandidatesSet(long electionId, String electionTitle, String userAdminId, String ip) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setCandidatesSet(true);
			em.persist(election);
			String description = userAdminId.toUpperCase() + " marcó candidatos como completados para la elección " + electionTitle;
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	/**
	 * Gets a list with all the parameters
	 *
	 * @return returns a list of parameter entity with all the parameters on the system.
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
	 * @param userAdminId Id of the user performing the action, used for logging purposes
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
	 * @param userAdminId Id of the user performing the action, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void editParameter(Parameter parameter, String userAdminId, String ip) {
		try {
			EJBFactory.getInstance().getElectionsParametersEJB().editParameter(parameter);
			String description = userAdminId.toUpperCase() + " actualizó el parámetro " + parameter.getKey();
			persistActivity(userAdminId, ActivityType.EDIT_PARAMETER, description, ip, null);
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	/**
	 * Deletes a parameter from the system.
	 *
	 * @param key         Key of the parameter to delete
	 * @param userAdminId Id of the user performing the action, used for logging purposes
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
			appLogger.error(e.getMessage());
		}
	}

	/**
	 * Get list of the recipients an email template. Depending on the template it can be a list of voters or auditors
	 *
	 * @param electionEmailTemplate Entity of the email template with the information of the email template to get the recipients
	 *
	 * @return returns a list, it may be of voters entity or auditors entity depending on the email template.
	 */
	@Override
	public List getRecipientsByRecipientType(ElectionEmailTemplate electionEmailTemplate) throws Exception {
		RecipientType recipientType = electionEmailTemplate.getRecipientType();
		List<UserVoter> result;

		if (recipientType.equals(RecipientType.VOTERS)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.VOTERS_WITHOUT_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionCensusWithoutCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionCensusByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_MX)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionCensusByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersNotVotedYet(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_WITHOUT_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersNotVotedYetWithoutCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_MX)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
		} else if (recipientType.equals(RecipientType.VOTERS_ALREADY_VOTED)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersVoted(electionEmailTemplate.getElection().getElectionId());
		} else if (recipientType.equals(RecipientType.VOTERS_ALREADY_VOTED_WITHOUT_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersVotedWithoutCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_ALREADY_VOTED_BR)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersVotedByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
		} else if (recipientType.equals(RecipientType.VOTERS_ALREADY_VOTED_MX)) {
			return ElectionsDaoFactory.createUserVoterDao(em).getElectionsUserVotersVotedByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_TWO_ELECTIONS)) {
			result = ElectionsDaoFactory.createUserVoterDao(em).getJointElectionUserVotersNotVotedYet(electionEmailTemplate.getElection().getElectionId());
			return getUniqueJointElectionRecipients(result);
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_TWO_ELECTIONS_WITHOUT_BR)) {
			result = ElectionsDaoFactory.createUserVoterDao(em).getJointElectionUserVotersNotVotedYetWithoutCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
			return getUniqueJointElectionRecipients(result);
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_TWO_ELECTIONS_BR)) {
			result = ElectionsDaoFactory.createUserVoterDao(em).getJointElectionUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
			return getUniqueJointElectionRecipients(result);
		} else if (recipientType.equals(RecipientType.VOTERS_NOT_VOTED_YET_TWO_ELECTIONS_MX)) {
			result = ElectionsDaoFactory.createUserVoterDao(em).getJointElectionUserVotersNotVotedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
			return getUniqueJointElectionRecipients(result);
		} else if (recipientType.equals(RecipientType.AUDITORS)) {
			return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditors(electionEmailTemplate.getElection().getElectionId());
			} else if (recipientType.equals(RecipientType.AUDITORS_NOT_AGREED_CONFORMITY_YET)) {
				return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditorsNotAgreedConformity(electionEmailTemplate.getElection().getElectionId());
			} else if (recipientType.equals(RecipientType.AUDITORS_ALREADY_AGREED_CONFORMITY)) {
				return ElectionsDaoFactory.createAuditorDao(em).getElectionAuditorsAgreedConformity(electionEmailTemplate.getElection().getElectionId());
			} else if (recipientType.equals(RecipientType.ORGANIZATION)) {
				return ElectionsDaoFactory.createOrganizationDao(em).getNonDebtorOrganizationsByElectionId(electionEmailTemplate.getElection().getElectionId());
			} else if (recipientType.equals(RecipientType.ORGANIZATION_WITHOUT_BR)) {
				return ElectionsDaoFactory.createOrganizationDao(em).getNonDebtorOrganizationsWithoutCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
			} else if (recipientType.equals(RecipientType.ORGANIZATION_BR)) {
				return ElectionsDaoFactory.createOrganizationDao(em).getNonDebtorOrganizationsByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
			} else if (recipientType.equals(RecipientType.ORGANIZATION_MX)) {
				return ElectionsDaoFactory.createOrganizationDao(em).getNonDebtorOrganizationsByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
			} else if (recipientType.equals(RecipientType.ORGANIZATION_NOT_NOMINATED_YET)) {
				return ElectionsDaoFactory.createOrganizationDao(em).getNonDebtorOrganizationsNotNominatedYet(electionEmailTemplate.getElection().getElectionId());
			} else if (recipientType.equals(RecipientType.ORGANIZATION_WITHOUT_BR_NOT_NOMINATED_YET)) {
				return ElectionsDaoFactory.createOrganizationDao(em).getNonDebtorOrganizationsNotNominatedYetWithoutCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
			} else if (recipientType.equals(RecipientType.ORGANIZATION_BR_NOT_NOMINATED_YET)) {
				return ElectionsDaoFactory.createOrganizationDao(em).getNonDebtorOrganizationsNotNominatedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "BR");
			} else if (recipientType.equals(RecipientType.ORGANIZATION_MX_NOT_NOMINATED_YET)) {
				return ElectionsDaoFactory.createOrganizationDao(em).getNonDebtorOrganizationsNotNominatedYetByCountry(electionEmailTemplate.getElection().getElectionId(), "MX");
			} else {
				throw new Exception("Should have never got here.");
			}
		}

	/**
	 * Creates email templates for all the elections. It iterates the election and for every election it iterated the templates and for each template it creates an email template linked to the election
	 *
	 * @return returns the number of template created.
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Integer createMissingEmailTemplates() {
		int amount = 0;
		List<ElectionEmailTemplate> baseTemplates = getBaseEmailTemplates();
		if (baseTemplates == null || baseTemplates.isEmpty()) {
			return amount;
		}

		Set<String> baseTemplateTypes = new HashSet<>();
		for (ElectionEmailTemplate baseTemplate : baseTemplates) {
			if (baseTemplate != null && baseTemplate.getTemplateType() != null) {
				baseTemplateTypes.add(baseTemplate.getTemplateType().trim().toUpperCase(Locale.ROOT));
			}
		}

		Map<Long, Set<String>> existingTemplateTypesByElection = new HashMap<>();
		List<Object[]> existingTemplatePairs = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplatePairs();
		for (Object[] pair : existingTemplatePairs) {
			if (pair == null || pair.length < 2 || pair[0] == null || pair[1] == null) {
				continue;
			}
			long electionId = ((Number) pair[0]).longValue();
			String templateType = pair[1].toString().trim().toUpperCase(Locale.ROOT);
			if (!baseTemplateTypes.contains(templateType)) {
				continue;
			}
			existingTemplateTypesByElection.computeIfAbsent(electionId, key -> new HashSet<>()).add(templateType);
		}

		List<Election> elections = getElectionsAllOrderCreationDate();
		for (Election election : elections) {
			if (election == null) {
				continue;
			}
			Set<String> existingTypes = existingTemplateTypesByElection.computeIfAbsent(election.getElectionId(), key -> new HashSet<>());
			for (ElectionEmailTemplate baseTemplate : baseTemplates) {
				if (baseTemplate == null || baseTemplate.getTemplateType() == null) {
					continue;
				}
				String templateType = baseTemplate.getTemplateType().trim().toUpperCase(Locale.ROOT);
				if (!existingTypes.contains(templateType)) {
					em.persist(new ElectionEmailTemplate(election, baseTemplate));
					existingTypes.add(templateType);
					amount++;
				}
			}
		}
		return amount;
	}

	/**
	 * Sends emails to the list of voters/auditor using an email template, also creates the information on the mail table.
	 *
	 * @param users                 List of voter or auditor entity with the recipients of the email
	 * @param electionEmailTemplate Entity with the information of the election and email template.
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
	 * Moves a candidate up in the order, by switching the order with the candidate immediate above.
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
	 * Moves a candidate down in the order, by switching the order with the candidate immediate below.
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
	 * Enables/Disables the random order attribute so as to order the candidates randomly or not
	 *
	 * @param electionId Identifier of the election
	 * @param value      Boolean value to be set, true for the order to be random, false to leave the order defined.
	 */
	@Override
	public void setSortCandidatesRandomly(Long electionId, Boolean value) {
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			election.setRandomOrderCandidates(value);
			em.persist(election);
		} catch (Exception e) {
			appLogger.error(e.getMessage());
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
	 * @param userAdminId Id of the user performing the action, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 *
	 * @return returns true if the commissioner was correctly added, false if the mail was null or an exception is thrown.
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
			appLogger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Remove a commissioner from the system.
	 *
	 * @param commissionerId Identifier of the commissioner.
	 * @param name           Name of the commissioner, used for logging purposes
	 * @param userAdminId    Id of the user performing the action, used for logging purposes
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
	 * @param commissioner Entity containing the information of the commissioner to be updated.
	 * @param userAdminId  Id of the user performing the action, used for logging purposes
	 * @param ip           Ip of the user, used for logging purposes
	 */
	@Override
	public void editCommissioner(Commissioner commissioner, String userAdminId, String ip) {
		try {
			em.merge(commissioner);
			persistActivity(userAdminId, ActivityType.EDIT_COMMISSIONER, userAdminId + " ha editado el comidionado " + commissioner.getName(), ip, null);
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	/**
	 * Gets the census example excel file.
	 *
	 * @return returns the excel file.
	 */
	@Override
	public File exportCensusExample() {
		boolean includeOrgIdColumn = true;
		File exampleFile = FilesUtils.getJbossTempCensusExample(includeOrgIdColumn);
		if (exampleFile != null && exampleFile.exists()) {
			return exampleFile;
		}
		List<UserVoter> exampleUserVoters = new ArrayList<>();
		UserVoter firstExampleUserVoter = new UserVoter();
		firstExampleUserVoter.setLanguage("ES");
		firstExampleUserVoter.setName("Nombre Ejemplo");
		firstExampleUserVoter.setOrgName("Organización Ejemplo");
		firstExampleUserVoter.setMail("ejemplo@dominio.org");
		firstExampleUserVoter.setVoteAmount(1);
		firstExampleUserVoter.setCountry("UY");
		firstExampleUserVoter.setOrgID("UY-EXAMPLE-001");
		exampleUserVoters.add(firstExampleUserVoter);

		UserVoter secondExampleUserVoter = new UserVoter();
		secondExampleUserVoter.setLanguage("EN");
		secondExampleUserVoter.setName("Organization Example");
		secondExampleUserVoter.setOrgName("Example Organization");
		secondExampleUserVoter.setMail("update@domain.org");
		secondExampleUserVoter.setVoteAmount(2);
		secondExampleUserVoter.setCountry("BR");
		secondExampleUserVoter.setOrgID("BR-EXAMPLE-002");
		exampleUserVoters.add(secondExampleUserVoter);
		return ExcelUtils.exportToExcel(exampleUserVoters, "/padron_electoral_ejemplo.xlsx", includeOrgIdColumn);
	}

	@Override
	public File exportOrganizationsUpsertExample() {
		File exampleFile = FilesUtils.getJbossTempOrganizationsUpsertExample();
		if (exampleFile != null && exampleFile.exists()) {
			return exampleFile;
		}
		return ExcelUtils.exportOrganizationsUpsertExample("/exampleAddUpdateOrgs.xlsx");
	}

	@Override
	public File exportOrganizationsDeleteExample() {
		File exampleFile = FilesUtils.getJbossTempOrganizationsDeleteExample();
		if (exampleFile != null && exampleFile.exists()) {
			return exampleFile;
		}
		return ExcelUtils.exportOrganizationsSingleColumnExample("/exampleDeleteOrgs.xlsx", "ORGID");
	}

	@Override
	public File exportOrganizationsDebtorsExample() {
		File exampleFile = FilesUtils.getJbossTempOrganizationsDebtorsExample();
		if (exampleFile != null && exampleFile.exists()) {
			return exampleFile;
		}
		return ExcelUtils.exportOrganizationsSingleColumnExample("/exampleDebtors.xlsx", "ORGID");
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
	 * Resends a voter the election started and election created mails using the templates defined for the election.
	 *
	 * @param userVoter   Entity with the voter information
	 * @param election    Entity with the election information
	 * @param userAdminId Id of the user performing the action, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 */
	@Override
	public void resendUserVoterElectionMail(UserVoter userVoter, Election election, String userAdminId, String ip) {
		ElectionEmailTemplate noticeTemplate = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(EmailTemplateType.ELECTION_NOTICE.getKey(), election.getElectionId());
		ElectionEmailTemplate startedTemplate = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(EmailTemplateType.ELECTION_START.getKey(), election.getElectionId());
		if (noticeTemplate != null || startedTemplate != null) {
			Date now = new Date();
			Date votingStartDate = VotingPeriodResolver.getVotingStartDate(em, election.getElectionId());
			Date votingEndDate = VotingPeriodResolver.getVotingEndDate(em, election.getElectionId());
			if (votingStartDate != null && now.after(election.getCreationDate()) && now.before(votingStartDate)) {
				EJBFactory.getInstance().getMailsSendingEJB().queueSingleSending(noticeTemplate, userVoter, null, election, new ArrayList<>());
			}
			if (election.isVotingLinkAvailable() && votingStartDate != null && votingEndDate != null && now.after(votingStartDate) && now.before(votingEndDate)) {
				EJBFactory.getInstance().getMailsSendingEJB().queueSingleSending(startedTemplate, userVoter, null, election, new ArrayList<>());
			}
			String description = userAdminId.toUpperCase() + " envió recordatorio por email al usuario padrón " + userVoter.getName().toUpperCase() + " de la elección " + "(" + election.getTitleSpanish() + ")";
			persistActivity(userAdminId, ActivityType.RESEND_EMAIL_ELECTION_USER_CENSUS_MANUAL, description, ip, election.getElectionId());
		}
	}

	/**
	 * Gets the identifier of the first election for which the user is authorized
	 *
	 * @param userAdminId Identifier of the user.
	 *
	 * @return returns the identifier of the election.
	 */
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
	 * Enables/Disables the revision flag for an election, also, if the revision is disabled, it updates the elections auditor to disable the revision.
	 *
	 * @param electionId  Identifier of the election
	 * @param status      The value of the revision status, true - enabled, false - disabled
	 * @param userAdminId Id of the user performing the action, used for logging purposes
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
			appLogger.error(e1.getMessage());
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

	@Override
	public List<PublicElectionVoteCountRow> getElectionVoteCountRowsForPublicElectionPage(Long electionId) {
		if (electionId == null || electionId.longValue() <= 0L) {
			return Collections.emptyList();
		}
		return ElectionsDaoFactory.createVoteDao(em).getElectionVoteCountRowsForPublicElectionPage(electionId.longValue());
	}

	/**
	 * Validates if an election has the revision active
	 *
	 * @param electionId  Identifier of the election
	 * @param userAdminId Id of the user performing the action, used for logging purposes
	 * @param ip          Ip of the user, used for logging purposes
	 *
	 * @return returns true if the election has the revision active and all the auditors too, false otherwise.
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
	 * @param candidate Entity with the candidate from which the immediately above will be looked.
	 *
	 * @return returns a candidate entity with the information with the candidate ordered immediately above to the one passed, null if there's none.
	 */
	@Override
	public Candidate getNextAboveCandidate(Candidate candidate) {
		return ElectionsDaoFactory.createCandidateDao(em).getNextAboveCandidate(candidate.getElection().getElectionId(), candidate.getCandidateOrder());
	}

	/**
	 * Returns the candidate orderer immediately below the parameter
	 *
	 * @param candidate Entity with the candidate from which the immediately below will be looked.
	 *
	 * @return returns a candidate entity with the information with the candidate ordered immediately below to the one passed, null if there's none.
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
	 * @return returns true if a commissioner with the name and mail exists, false if not
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
	 * @return returns true if there is an auditor with the name and mail associated to the election, false if not.
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

	@Override
	public String getDefaultRecipient() {
		return EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.DEFAULT_RECIPIENT);
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
	 * @param userAdminId           Id of the user performing the action, used for logging purposes
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
			appLogger.error(e.getMessage());
			return false;
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int forceBaseTemplateToOpenElections(String templateType, String userAdminId, String ip) {
		try {
			if (templateType == null || templateType.trim().isEmpty()) {
				return 0;
			}

			String normalizedTemplateType = templateType.trim().toUpperCase(Locale.ROOT);
			ElectionEmailTemplate baseTemplate = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplate(normalizedTemplateType);
			if (baseTemplate == null) {
				return 0;
			}

			int affectedElections = 0;
			List<Election> elections = ElectionsDaoFactory.createElectionDao(em).getElectionsAllOrderCreationDate();
			for (Election election : elections) {
				if (election == null || election.isClosed()) {
					continue;
				}

				ElectionEmailTemplate electionTemplate = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(normalizedTemplateType, election.getElectionId());
				if (electionTemplate != null) {
					em.remove(electionTemplate);
					em.flush();
				}
				em.persist(new ElectionEmailTemplate(election, baseTemplate));
				affectedElections++;
			}

			String actor = userAdminId == null ? "" : userAdminId.toUpperCase(Locale.ROOT);
			String description = actor + " forzó la copia del template base " + normalizedTemplateType + " a " + affectedElections + " elecciones no cerradas.";
			persistActivity(userAdminId, ActivityType.ADD_BASE_TEMPLATE, description, ip, null);
			return affectedElections;
		} catch (Exception e) {
			appLogger.error("Error forcing base template copy. templateType={}", templateType, e);
			return 0;
		}
	}

	/**
	 * Get a list of the the joint elections on the system.
	 *
	 * @return returns a collection of joint election entity containing the information.
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
	 * @return returns a joint election entity on which one of the elections is the one searched by or null if it does not find it.
	 */
	@Override
	public JointElection getJointElectionForElection(long electionId) {
		return ElectionsDaoFactory.createElectionDao(em).getJointElectionForElection(electionId);
	}

	/**
	 * Creates or update (an existing) joint election.
	 *
	 * @param jointElection Entity containing the joint election information to be add/updated
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
	 * @return returns true if the election searched is joint with another, false if not.
	 */
	@Override
	public boolean isJointElection(long electionId) {
		return !ElectionsDaoFactory.createElectionDao(em).electionIsSimple(electionId);
	}

	/**
	 * Validates if the census of both elections in a joint election contains the same voters
	 *
	 * @param jointElection Entity with the joint election information (contains two elections)
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
	 * @return reutns a collection of string, on each string containing the id and title of the elections.
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
			appLogger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Saves an Activity log entry
	 *
	 * @param userAdminId  Identifier of the user who performs the activity
	 * @param activityType Enumerate containing the type of the activity
	 * @param description  A string with the description of the activity
	 * @param ip           A string with the ip address of the user performing the activity
	 * @param electionId   Identifier of the election.
	 */
	@Override
	public void persistActivity(String userAdminId, ActivityType activityType, String description, String ip, Long electionId) {
		Activity activity = new Activity(userAdminId, electionId, ip, activityType, description);
		em.persist(activity);
		CriticalOperationsLoggerUtils.logCriticalOperation(ip, userAdminId, activityType == null ? null : activityType.toString(), electionId);
	}

	/**
	 * Returns the data site key
	 */
	@Override
	public String getDataSiteKey() {
		try {
			return EJBFactory.getInstance().getElectionsParametersEJB().getDataSiteKey();
		} catch (Exception e) {
			appLogger.error(e.getMessage());
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
			Date endDate = VotingPeriodResolver.getVotingEndDate(em, electionId);
			Date currentDate = DateTime.now().toDate();

			if ((!closed) && endDate != null && (endDate.before(currentDate))) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Closes an election
	 *
	 * @param electionId  Identifier of the election searched.
	 *
	 * @param userAdminId Id of the user performing the action, used for logging purposes
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
			appLogger.error(e.getMessage());
			return false;
		}
	}

	@Override
	public AdminLoginResult login(String username, String password, String ip) {
		return login(username, password, ip, null);
	}

	@Override
	public AdminLoginResult login(String username, String password, String ip, String totp) {
		try {

			LoginData dataLDAP = UtilsLogin.loginTfa(username, password, normalizeTotp(totp));
			Set<String> grantedPortalRoles = resolveSupportedPortalRoles(dataLDAP);
			boolean hasLoginEligibleRole = hasLoginEligibleRole(grantedPortalRoles);
			if (dataLDAP != null && dataLDAP.getAuthenticated() && hasLoginEligibleRole) {
				String description = username.toUpperCase() + " se ha logueado exitosamente";
				getManagerProxy().persistActivity(username, ActivityType.LOGIN_SUCCESSFUL, description, ip, null);

				UserAdmin userAdmin = crearActualizarAdminUser(dataLDAP);
				if (userAdmin != null) {
					return new AdminLoginResult(userAdmin, grantedPortalRoles);
				}
			} else {
				String description = "Intento fallido de login de usuario " + username.toUpperCase();
				appLogger.warn("Failed LDAP login attempt. user={}, ip={}", username, ip);
				ElectionsCaches.incrementLoginCaptchaAttempt(ip);
				getManagerProxy().persistActivity(username, ActivityType.LOGIN_FAILED, description, ip, null);
				return new AdminLoginResult(resolveLoginError(dataLDAP));
			}
			return null;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
			return null;
		}
	}

	private String normalizeTotp(String totp) {
		return totp == null ? "" : totp.trim();
	}

	private String resolveLoginError(LoginData loginData) {
		if (loginData == null || loginData.getError() == null || loginData.getError().trim().isEmpty()) {
			return null;
		}
		return loginData.getError().trim();
	}

	private Set<String> resolveSupportedPortalRoles(LoginData loginData) {
		Set<String> resolvedRoles = new HashSet<>();
		if (loginData == null || loginData.getRoles() == null) {
			return resolvedRoles;
		}
		Set<String> supportedRoles = ElectionsRoles.supportedPortalRoles();
		for (String role : loginData.getRoles()) {
			if (supportedRoles.contains(role)) {
				resolvedRoles.add(role);
			}
		}
		return resolvedRoles;
	}

	private boolean hasLoginEligibleRole(Set<String> grantedPortalRoles) {
		if (grantedPortalRoles == null || grantedPortalRoles.isEmpty()) {
			return false;
		}
		return grantedPortalRoles.stream().anyMatch(role ->
				ElectionsRoles.ELECTIONS_MANAGER.equals(role)
				|| ElectionsRoles.ELECTIONS_STATUTARY_ONLY.equals(role)
				|| ElectionsRoles.ELECTIONS_NON_STATUTARY_ONLY.equals(role));
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
					return userAdmin;
				}

				} else {
					UserAdmin updatedUserAdmin = new UserAdmin(paiUser.getUsername(), "-creado en pai.lacnic.net-", paiUser.getUsername());
					em.persist(updatedUserAdmin);
					return updatedUserAdmin;
				}

		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public List<ElectionCalendar> getElectionCalendars(long electionId) {
		return ElectionsDaoFactory.createElectionCalendarDao(em).getElectionCalendars(electionId);
	}

	@Override
	public List<ElectionTask> getElectionTasks(long electionId) {
		return ElectionsDaoFactory.createElectionTaskDao(em).getElectionTasks(electionId);
	}

	@Override
	public boolean addElectionTask(long electionId, ElectionTaskKey taskKey, long electionCalendarId, TaskDependencyLevel dependencyLevel, Integer displayOrder, boolean publicable, String userAdminId,
			String ip) {
		try {
			if (taskKey == null || dependencyLevel == null) {
				return false;
			}

			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			if (election == null) {
				return false;
			}

			ElectionCalendar electionCalendar = ElectionsDaoFactory.createElectionCalendarDao(em).getElectionCalendar(electionId, electionCalendarId);
			if (electionCalendar == null || electionCalendar.getId() == 0) {
				return false;
			}

			if (ElectionsDaoFactory.createElectionTaskDao(em).getElectionTaskByKey(electionId, taskKey) != null) {
				return false;
			}

			boolean effectivePublicable = publicable && (taskKey != ElectionTaskKey.COURSE || CampusClient.isCampusTrainingEnabled(election));
			ElectionTask electionTask = new ElectionTask(election, electionCalendar, taskKey, dependencyLevel, displayOrder, effectivePublicable);
			em.persist(electionTask);
			ensureTaskProgressForAllElectionCandidates(electionTask);

			String description = userAdminId.toUpperCase() + " agregó la tarea " + taskKey + TEXT_ELECCION + election.getTitleSpanish();
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean editElectionTask(long electionTaskId, long electionCalendarId, TaskDependencyLevel dependencyLevel, Integer displayOrder, boolean publicable, String userAdminId, String ip) {
		try {
			if (dependencyLevel == null) {
				return false;
			}
			ElectionTask electionTask = ElectionsDaoFactory.createElectionTaskDao(em).getElectionTask(electionTaskId);
			if (electionTask == null) {
				return false;
			}

			Election election = electionTask.getElection();
			ElectionCalendar electionCalendar = ElectionsDaoFactory.createElectionCalendarDao(em).getElectionCalendar(election.getElectionId(), electionCalendarId);
			if (electionCalendar == null) {
				return false;
			}

			electionTask.setElectionCalendar(electionCalendar);
			electionTask.setDependencyLevel(dependencyLevel);
			electionTask.setDisplayOrder(displayOrder);
			boolean effectivePublicable = publicable && (electionTask.getTaskKey() != ElectionTaskKey.COURSE || CampusClient.isCampusTrainingEnabled(election));
			electionTask.setPublicable(effectivePublicable);
			em.merge(electionTask);
			ensureTaskProgressForAllElectionCandidates(electionTask);

			String description = userAdminId.toUpperCase() + " actualizó la tarea " + electionTask.getTaskKey() + TEXT_ELECCION + election.getTitleSpanish();
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, election.getElectionId());
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean moveElectionTaskUp(long electionTaskId, String userAdminId, String ip) {
		return moveElectionTask(electionTaskId, -1, userAdminId, ip);
	}

	@Override
	public boolean moveElectionTaskDown(long electionTaskId, String userAdminId, String ip) {
		return moveElectionTask(electionTaskId, 1, userAdminId, ip);
	}

	private boolean moveElectionTask(long electionTaskId, int direction, String userAdminId, String ip) {
		try {
			ElectionTask selectedTask = ElectionsDaoFactory.createElectionTaskDao(em).getElectionTask(electionTaskId);
			if (selectedTask == null || selectedTask.getElection() == null) {
				return false;
			}

			Election election = selectedTask.getElection();
			List<ElectionTask> electionTasks = ElectionsDaoFactory.createElectionTaskDao(em).getElectionTasks(election.getElectionId());
			if (electionTasks == null || electionTasks.isEmpty()) {
				return false;
			}

			Collections.sort(electionTasks, new Comparator<ElectionTask>() {
				@Override
				public int compare(ElectionTask left, ElectionTask right) {
					int displayOrderCompare = Integer.compare(resolveElectionTaskDisplayOrderSortValue(left), resolveElectionTaskDisplayOrderSortValue(right));
					if (displayOrderCompare != 0) {
						return displayOrderCompare;
					}
					return Long.compare(resolveElectionTaskIdSortValue(left), resolveElectionTaskIdSortValue(right));
				}
			});

			int currentIndex = findElectionTaskIndex(electionTasks, electionTaskId);
			int targetIndex = currentIndex + direction;
			if (currentIndex < 0 || targetIndex < 0 || targetIndex >= electionTasks.size()) {
				return false;
			}

			Collections.swap(electionTasks, currentIndex, targetIndex);
			renumberElectionTasks(electionTasks);

			String action = direction < 0 ? "subió" : "bajó";
			String description = userAdminId.toUpperCase() + " " + action + " la tarea " + selectedTask.getTaskKey() + TEXT_ELECCION + election.getTitleSpanish();
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, election.getElectionId());
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	private int findElectionTaskIndex(List<ElectionTask> electionTasks, long electionTaskId) {
		for (int index = 0; index < electionTasks.size(); index++) {
			ElectionTask electionTask = electionTasks.get(index);
			if (electionTask != null && electionTask.getId() == electionTaskId) {
				return index;
			}
		}
		return -1;
	}

	private int resolveElectionTaskDisplayOrderSortValue(ElectionTask electionTask) {
		if (electionTask == null || electionTask.getDisplayOrder() == null) {
			return Integer.MAX_VALUE;
		}
		return electionTask.getDisplayOrder();
	}

	private long resolveElectionTaskIdSortValue(ElectionTask electionTask) {
		return electionTask != null ? electionTask.getId() : Long.MAX_VALUE;
	}

	private void renumberElectionTasks(List<ElectionTask> electionTasks) {
		int displayOrder = 1;
		for (ElectionTask electionTask : electionTasks) {
			if (electionTask == null) {
				continue;
			}
			electionTask.setDisplayOrder(displayOrder);
			em.merge(electionTask);
			displayOrder++;
		}
	}

	private int ensureTaskProgressForAllElectionCandidates(ElectionTask electionTask) {
		if (electionTask == null || electionTask.getElection() == null) {
			return 0;
		}

		long electionId = electionTask.getElection().getElectionId();
		if (electionId <= 0) {
			return 0;
		}

		List<Candidate> electionCandidates = ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(electionId);
		if (electionCandidates == null || electionCandidates.isEmpty()) {
			return 0;
		}

		Set<Long> candidateIdsWithProgress = new HashSet<>(ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).getCandidateIdsByElectionTask(electionTask.getId()));
		int created = 0;

		for (Candidate candidate : electionCandidates) {
			if (candidate == null || candidate.isAbstention() || candidateIdsWithProgress.contains(candidate.getCandidateId())) {
				continue;
			}
			CandidateElectionTaskProgress candidateTaskProgress = new CandidateElectionTaskProgress(candidate, electionTask, CandidateElectionTaskStatus.NOT_STARTED);
			em.persist(candidateTaskProgress);
			created++;
		}
		return created;
	}

	@Override
	public boolean removeElectionTask(long electionTaskId, String userAdminId, String ip) {
		try {
			ElectionTask electionTask = ElectionsDaoFactory.createElectionTaskDao(em).getElectionTask(electionTaskId);
			if (electionTask == null) {
				return false;
			}

			if (ElectionsDaoFactory.createElectionTaskDao(em).countTaskProgress(electionTaskId) > 0) {
				return false;
			}

			Election election = electionTask.getElection();
			ElectionTaskKey taskKey = electionTask.getTaskKey();
			em.remove(electionTask);

			String description = userAdminId.toUpperCase() + " eliminó la tarea " + taskKey + TEXT_ELECCION + election.getTitleSpanish();
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, election.getElectionId());
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public List<Organization> getElectionOrganizations(long electionId) {
		return ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
	}

	@Override
	public SyncRun getElectionLatestSyncRun(long electionId) {
		return ElectionsDaoFactory.createSyncRunDao(em).getElectionLatestSyncRun(electionId);
	}

	@Override
	public List<SyncRun> getElectionSyncRuns(long electionId, int maxResults) {
		return ElectionsDaoFactory.createSyncRunDao(em).getElectionSyncRuns(electionId, maxResults);
	}

	@Override
	public List<SyncRun> getElectionAutomaticCensusSyncRuns(long electionId, int maxResults) {
		return ElectionsDaoFactory.createSyncRunDao(em).getElectionAutomaticCensusSyncRuns(electionId, maxResults);
	}

	@Override
	public Map<Long, SyncRun> getLatestSyncRunsByElectionIds(List<Long> electionIds) {
		return ElectionsDaoFactory.createSyncRunDao(em).getLatestSyncRunsByElectionIds(electionIds);
	}

	@Override
	public List<SyncRun> getSyncRuns(int maxResults) {
		return ElectionsDaoFactory.createSyncRunDao(em).getSyncRuns(maxResults);
	}

	@Override
	public Map<Long, Date[]> getMilacnicSyncCalendarWindowByElectionIds(List<Long> electionIds) {
		Set<Long> electionIdSet = new HashSet<>();
		if (electionIds != null) {
			for (Long electionId : electionIds) {
				if (electionId != null && electionId.longValue() > 0) {
					electionIdSet.add(electionId);
				}
			}
		}
		return ElectionsDaoFactory.createElectionCalendarDao(em).getCalendarWindowByElectionIds(electionIdSet, ElectionCalendarKey.N_23_PERIODO_PADRON_MILACNIC_SYNC);
	}

	@Override
	public List<SyncAudit> getElectionSyncAudits(long electionId, int maxResults) {
		int safeMaxResults = maxResults <= 0 ? 100 : Math.min(maxResults, 500);
		return em.createQuery("SELECT a FROM SyncAudit a WHERE a.election.electionId = :electionId ORDER BY a.eventDate DESC, a.id DESC", SyncAudit.class).setParameter(QueryParameterNames.ELECTION_ID, electionId).setMaxResults(safeMaxResults).getResultList();
	}

	@Override
	public List<SyncAudit> getElectionSyncAuditsByRunId(long electionId, String syncRunId, int maxResults) {
		if (electionId <= 0 || !hasText(syncRunId)) {
			return Collections.emptyList();
		}
		int safeMaxResults = maxResults <= 0 ? 500 : Math.min(maxResults, 500);
		TypedQuery<SyncAudit> query = em.createQuery(
				"SELECT a FROM SyncAudit a WHERE a.election.electionId = :electionId AND a.syncRunId = :syncRunId ORDER BY a.eventDate DESC, a.id DESC",
				SyncAudit.class)
				.setParameter(QueryParameterNames.ELECTION_ID, electionId)
				.setParameter(QueryParameterNames.SYNC_RUN_ID, syncRunId.trim())
				.setMaxResults(safeMaxResults);
		return query.getResultList();
	}

	@Override
	public List<SyncAudit> getElectionSyncAuditsByRunIds(long electionId, List<String> syncRunIds, int maxResults) {
		if (electionId <= 0 || syncRunIds == null || syncRunIds.isEmpty()) {
			return Collections.emptyList();
		}
		Set<String> normalizedRunIds = new HashSet<>();
		for (String syncRunId : syncRunIds) {
			if (hasText(syncRunId)) {
				normalizedRunIds.add(syncRunId.trim());
			}
		}
		if (normalizedRunIds.isEmpty()) {
			return Collections.emptyList();
		}
		int safeMaxResults = maxResults <= 0 ? 500 : Math.min(maxResults, 5000);
		return em.createQuery(
				"SELECT a FROM SyncAudit a WHERE a.election.electionId = :electionId AND a.syncRunId IN :syncRunIds ORDER BY a.eventDate DESC, a.id DESC",
				SyncAudit.class)
				.setParameter(QueryParameterNames.ELECTION_ID, electionId)
				.setParameter("syncRunIds", normalizedRunIds)
				.setMaxResults(safeMaxResults)
				.getResultList();
	}

	@Override
	public List<Nomination> getElectionNominations(long electionId) {
		return em.createQuery("SELECT n FROM Nomination n WHERE n.election.electionId = :electionId ORDER BY n.id", Nomination.class).setParameter(QueryParameterNames.ELECTION_ID, electionId).getResultList();
	}

	@Override
	public List<Nomination> getElectionNominationsForPublicElectionPage(long electionId) {
		return ElectionsDaoFactory.createNominationDao(em).getElectionNominationsForPublicElectionPage(electionId);
	}

	@Override
	public List<Nomination> getElectionNominationsByEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter) {
		String safeEmail = normalizeEmailText(normalizedEmail, 320);
		if (electionId <= 0 || !hasText(safeEmail)) {
			return Collections.emptyList();
		}
		int safeMaxResults = maxResults > 0 ? maxResults : 10;
		String safeCountryCodeFilter = normalizeCountryCodeFilter(countryCodeFilter);
		return ElectionsDaoFactory.createNominationDao(em).getElectionNominationsByNormalizedNominationEmail(
				electionId,
				safeEmail,
				safeMaxResults,
				safeCountryCodeFilter);
	}

	@Override
	public boolean sendCandidateReminder(long electionId, long candidateId, String userAdminId, String ip) {
		try {
			Candidate candidate = em.find(Candidate.class, candidateId);
			if (candidate == null || candidate.getElection() == null || candidate.getElection().getElectionId() != electionId || candidate.isAbstention()) {
				return false;
			}

			EJBFactory.getInstance().getMailsSendingEJB().queueCandidateReminderToCandidate(candidate.getCandidateId());
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByCandidateId(candidateId);
			String description = userAdminId.toUpperCase() + " solicitó envío de recordatorio de candidato. electionId=" + electionId + ACTIVITY_NOMINATION_ID_FRAGMENT + (nomination != null ? nomination.getId() : "-") + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", nominationName=" + safeValue(nomination != null ? nomination.getNominationName() : null) + ACTIVITY_NOMINATION_EMAIL_FRAGMENT + safeValue(nomination != null ? nomination.getNominationEmail() : null) + ", nominationToken=" + safeValue(nomination != null ? nomination.getAcceptNominationToken() : null);
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean sendNominationReminder(long electionId, long nominationId, String userAdminId, String ip) {
		try {
			Nomination nomination = em.find(Nomination.class, nominationId);
			if (nomination == null || nomination.getElection() == null || nomination.getElection().getElectionId() != electionId) {
				return false;
			}

			// Nomination reminders are intended for pending nominees that do not yet have a candidate linked.
			if (nomination.getCandidate() != null) {
				return false;
			}

			EJBFactory.getInstance().getMailsSendingEJB().queueNominationReminderToNominee(nomination.getId());
			String description = userAdminId.toUpperCase() + " solicitó envío de recordatorio de nominación al nominado. electionId=" + electionId + ACTIVITY_NOMINATION_ID_FRAGMENT + nomination.getId() + ACTIVITY_CANDIDATE_ID_FRAGMENT + "-" + ", nominationName=" + safeValue(nomination.getNominationName()) + ACTIVITY_NOMINATION_EMAIL_FRAGMENT + safeValue(nomination.getNominationEmail()) + ", nominationToken=" + safeValue(nomination.getAcceptNominationToken());
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean sendSupportReminder(long electionId, long supportNominationId, String userAdminId, String ip) {
		try {
			SupportNomination supportNomination = em.find(SupportNomination.class, supportNominationId);
			if (supportNomination == null || supportNomination.getNomination() == null || supportNomination.getNomination().getElection() == null || supportNomination.getNomination().getElection().getElectionId() != electionId) {
				return false;
			}

			EJBFactory.getInstance().getMailsSendingEJB().queueSupportReminderToSupportingContact(supportNomination.getId());
			Nomination nomination = supportNomination.getNomination();
			String description = userAdminId.toUpperCase() + " solicitó envío de recordatorio de apoyo. electionId=" + electionId + ", supportNominationId=" + supportNomination.getId() + ACTIVITY_NOMINATION_ID_FRAGMENT + nomination.getId() + ", supportingContactName=" + safeValue(supportNomination.getSupportingContactName()) + ", supportingContactEmail=" + safeValue(supportNomination.getSupportingContactEmail());
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean sendAuditorReminder(long electionId, long auditorId, String userAdminId, String ip) {
		try {
			Auditor auditor = em.find(Auditor.class, auditorId);
			if (auditor == null || auditor.getElection() == null || auditor.getElection().getElectionId() != electionId) {
				return false;
			}

			EJBFactory.getInstance().getMailsSendingEJB().queueAuditorReminderToAuditor(auditor.getAuditorId());
			String description = userAdminId.toUpperCase() + " solicitó envío de recordatorio a auditor para responder estado de candidatos. electionId=" + electionId + ", auditorId=" + auditorId + ", auditorName=" + safeValue(auditor.getName()) + ", auditorEmail=" + safeValue(auditor.getMail());
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean sendAuditorRevisionReminder(long electionId, long auditorId, String userAdminId, String ip) {
		try {
			Auditor auditor = em.find(Auditor.class, auditorId);
			if (!isAuditorRevisionReminderPending(auditor) || auditor.getElection().getElectionId() != electionId) {
				return false;
			}

			EJBFactory.getInstance().getMailsSendingEJB().queueAuditorRevisionReminderToAuditor(auditor.getAuditorId());
			String description = userAdminId.toUpperCase() + " solicitó envío de recordatorio a auditor para revisar resultados y auditar votos. electionId=" + electionId + ", auditorId=" + auditorId + ", auditorName=" + safeValue(auditor.getName()) + ", auditorEmail=" + safeValue(auditor.getMail());
			persistActivity(userAdminId, ActivityType.EDIT_ELECTION, description, ip, electionId);
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public void processDailyReminderFrequency() {
		LocalDate today = LocalDate.now();
		Map<Long, Boolean> reminderCalendarWindowByElection = new HashMap<>();
		Map<Long, Boolean> auditorCandidateReminderCalendarWindowByElection = new HashMap<>();
		Map<Long, Boolean> auditorRevisionReminderCalendarWindowByElection = new HashMap<>();
		Map<Long, Boolean> candidateReminderEvaluationExtensionCalendarWindowByElection = new HashMap<>();
		processReminderSafely("candidate reminders", () -> processCandidateDailyReminders(today, reminderCalendarWindowByElection, candidateReminderEvaluationExtensionCalendarWindowByElection));
		processReminderSafely("auditor candidate reminders", () -> processAuditorDailyReminders(today, auditorCandidateReminderCalendarWindowByElection));
		processReminderSafely("auditor revision reminders", () -> processAuditorRevisionDailyReminders(today, auditorRevisionReminderCalendarWindowByElection));
		processReminderSafely("pending nomination reminders", () -> processPendingNominationDailyReminders(today, reminderCalendarWindowByElection));
		processReminderSafely("pending support reminders", () -> processPendingSupportDailyReminders(today, reminderCalendarWindowByElection));
	}

	@Override
	@TransactionTimeout(9000)
	public void processMilacnicOrganizationsSync() {
		Date now = new Date();
		String batchRunId = buildBatchRunId(now);
		try {
			appLogger.info("MiLACNIC sync batch invocation started. batchRunId={}", batchRunId);
			List<ElectionCalendar> activeWindows = ElectionsDaoFactory.createElectionCalendarDao(em).getActiveCalendarsForOpenElectionsByKey(ElectionCalendarKey.N_23_PERIODO_PADRON_MILACNIC_SYNC, now);

			if (activeWindows == null || activeWindows.isEmpty()) {
				appLogger.info("MiLACNIC sync skipped: no active calendar windows. batchRunId={}, at={}", batchRunId, now);
				return;
			}

			String organizationsEndpointTemplate = resolveNonSensitiveTextParameter(Constants.MILACNIC_SYNC_ORGANIZATIONS_ENDPOINT_TEMPLATE);
			if (organizationsEndpointTemplate == null) {
				appLogger.info("MiLACNIC sync batch skipped: parameter {} is not configured. batchRunId={}",
						Constants.MILACNIC_SYNC_ORGANIZATIONS_ENDPOINT_TEMPLATE, batchRunId);
				return;
			}
			String milacnicApiToken = normalizeOptionalText(EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.MILACNIC_SYNC_API_TOKEN));
			Integer minOrganizationsRequired = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MIN_ORGANIZATIONS_REQUIRED);
			Integer maxMemberDeactivationsAllowed = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MAX_MEMBER_DEACTIVATIONS);
			Integer minDebtorOrganizationsRequired = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MIN_DEBTOR_ORGANIZATIONS_REQUIRED);
			Integer maxDebtorOrganizationsAllowed = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MAX_DEBTOR_ORGANIZATIONS_ALLOWED);
			Integer minBrazilOrganizationsRequired = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MIN_BRAZIL_ORGANIZATIONS_REQUIRED);
			appLogger.info(
					"MiLACNIC sync batch started. batchRunId={}, activeWindows={}, endpointTemplate={}, thresholds[minOrganizationsRequired={}, maxMemberDeactivationsAllowed={}, minDebtorOrganizationsRequired={}, maxDebtorOrganizationsAllowed={}, minBrazilOrganizationsRequired={}]",
					batchRunId, activeWindows.size(), organizationsEndpointTemplate, minOrganizationsRequired, maxMemberDeactivationsAllowed, minDebtorOrganizationsRequired,
					maxDebtorOrganizationsAllowed, minBrazilOrganizationsRequired);

			Set<Long> processedElectionIds = new HashSet<>();
			int skippedNullWindow = 0;
			int skippedNullElection = 0;
			int skippedManualElections = 0;
			int skippedDuplicateElectionWindows = 0;
			int processedElections = 0;
			int successRuns = 0;
			int noDataRuns = 0;
			int errorRuns = 0;
			int totalProcessedRows = 0;
			int totalCreatedRows = 0;
			int totalUpdatedRows = 0;
			long totalDurationMs = 0L;
			for (ElectionCalendar window : activeWindows) {
				if (window == null) {
					skippedNullWindow++;
					appLogger.warn("MiLACNIC sync window skipped: null calendar window. batchRunId={}", batchRunId);
					continue;
				}
				Election election = window.getElection();
				if (election == null) {
					skippedNullElection++;
					appLogger.warn("MiLACNIC sync window skipped: calendar window without election. batchRunId={}, windowId={}", batchRunId, window.getId());
					continue;
				}
				if (election.isManageOrganizationsManual()) {
					skippedManualElections++;
					appLogger.info("MiLACNIC sync election skipped: manual organizations enabled. batchRunId={}, electionId={}, windowId={}", batchRunId, election.getElectionId(),
							window.getId());
					continue;
				}
				if (!processedElectionIds.add(election.getElectionId())) {
					skippedDuplicateElectionWindows++;
					appLogger.info("MiLACNIC sync window skipped: duplicate election already processed in this batch. batchRunId={}, electionId={}, windowId={}", batchRunId,
							election.getElectionId(), window.getId());
					continue;
				}

				processedElections++;
				long electionId = election.getElectionId();
				String syncRunId = buildSyncRunId(electionId, now);
				String endpoint = resolveMilacnicEndpoint(organizationsEndpointTemplate, election.getElectionId());
				MilacnicSyncResult syncResult;
				if (endpoint == null) {
					syncResult = buildSyncErrorResult(MILACNIC_ENDPOINT_RESOLUTION_ERROR, syncRunId);
				} else {
					appLogger.info("MiLACNIC sync run started. batchRunId={}, electionId={}, syncRunId={}, endpoint={}", batchRunId, electionId, syncRunId, endpoint);
					syncResult = syncOrganizationsForElectionFromMilacnic(electionId, endpoint, milacnicApiToken, syncRunId, now, minOrganizationsRequired, maxMemberDeactivationsAllowed,
							minDebtorOrganizationsRequired, maxDebtorOrganizationsAllowed, minBrazilOrganizationsRequired, false);
				}

				registerOrganizationsSyncSnapshot(electionId, syncResult, now);
				totalProcessedRows += syncResult.processedRows;
				totalCreatedRows += syncResult.createdRows;
				totalUpdatedRows += syncResult.updatedRows;
				totalDurationMs += syncResult.durationMs;
				if (SYNC_STATUS_SUCCESS.equals(syncResult.status)) {
					successRuns++;
				} else if (SYNC_STATUS_NO_DATA.equals(syncResult.status)) {
					noDataRuns++;
				} else {
					errorRuns++;
				}
				appLogger.info(
						"MiLACNIC sync run finished. batchRunId={}, electionId={}, syncRunId={}, status={}, processedRows={}, createdRows={}, updatedRows={}, durationMs={}, wsStatusCode={}, wsElapsedMs={}, healthIndicators={}, message={}",
						batchRunId, electionId, syncRunId, syncResult.status, syncResult.processedRows, syncResult.createdRows, syncResult.updatedRows, syncResult.durationMs,
						syncResult.wsStatusCode, syncResult.wsElapsedMs, syncResult.healthIndicators, syncResult.message);
				if (SYNC_STATUS_ERROR.equals(syncResult.status)) {
					appLogger.warn("MiLACNIC sync finished with ERROR. batchRunId={}, electionId={}, syncRunId={}, reason={}", batchRunId, electionId, syncResult.syncRunId,
							syncResult.message);
				}
				if (syncResult.updatedRows > 0 || syncResult.createdRows > 0) {
					String description = "SYSTEM_CRON sincronizó organizaciones desde MiLACNIC. electionId=" + electionId + ", creadas=" + syncResult.createdRows + ACTIVITY_UPDATED_ROWS_FRAGMENT + syncResult.updatedRows;
					persistActivity(SYSTEM_CRON_ACTOR, ActivityType.EDIT_ORGS, description, "AUTO", electionId);
				}
			}
			appLogger.info(
					"MiLACNIC sync batch finished. batchRunId={}, processedElections={}, skippedNullWindow={}, skippedNullElection={}, skippedManualElections={}, skippedDuplicateElectionWindows={}, successRuns={}, noDataRuns={}, errorRuns={}, totalProcessedRows={}, totalCreatedRows={}, totalUpdatedRows={}, totalDurationMs={}",
					batchRunId, processedElections, skippedNullWindow, skippedNullElection, skippedManualElections, skippedDuplicateElectionWindows, successRuns, noDataRuns, errorRuns,
					totalProcessedRows, totalCreatedRows, totalUpdatedRows, totalDurationMs);
		} catch (Exception e) {
			appLogger.error("Error processing MiLACNIC organizations sync. batchRunId={}", batchRunId, e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void processAutomaticOrganizationsAndCensusSync() {
		Date now = new Date();
		String batchRunId = buildBatchRunId(now);
		try {
			List<ElectionCalendar> activeWindows = ElectionsDaoFactory.createElectionCalendarDao(em).getActiveCalendarsForOpenElectionsByKey(ElectionCalendarKey.N_23_PERIODO_PADRON_MILACNIC_SYNC, now);
			if (activeWindows == null || activeWindows.isEmpty()) {
				appLogger.info("Combined automatic sync skipped: no active calendar windows. batchRunId={}", batchRunId);
				return;
			}

			Set<Long> processedElectionIds = new HashSet<>();
			int skippedNullWindow = 0;
			int skippedNullElection = 0;
			int skippedDuplicateElectionWindows = 0;
			int skippedManualBoth = 0;
			int processedElections = 0;
			int organizationsPhaseRuns = 0;
			int organizationsPhaseSkippedLocked = 0;
			int censusPhaseRuns = 0;
			int censusPhaseSkippedLocked = 0;

			appLogger.info("Combined automatic sync batch started. batchRunId={}, activeWindows={}", batchRunId, activeWindows.size());
			for (ElectionCalendar window : activeWindows) {
				if (window == null) {
					skippedNullWindow++;
					appLogger.warn("Combined automatic sync skipped: null calendar window. batchRunId={}", batchRunId);
					continue;
				}
				Election election = window.getElection();
				if (election == null) {
					skippedNullElection++;
					appLogger.warn("Combined automatic sync skipped: calendar without election. batchRunId={}, windowId={}", batchRunId, window.getId());
					continue;
				}

				long electionId = election.getElectionId();
				if (!processedElectionIds.add(electionId)) {
					skippedDuplicateElectionWindows++;
					appLogger.info("Combined automatic sync skipped duplicate election in batch. batchRunId={}, electionId={}, windowId={}", batchRunId, electionId, window.getId());
					continue;
				}

				boolean organizationsEnabled = !election.isManageOrganizationsManual();
				boolean censusEnabled = !election.isManageVotersManual();
				if (!organizationsEnabled && !censusEnabled) {
					skippedManualBoth++;
					appLogger.info("Combined automatic sync skipped election with manual management. batchRunId={}, electionId={}", batchRunId, electionId);
					continue;
				}

				processedElections++;
				appLogger.info("Combined automatic sync election started. batchRunId={}, electionId={}, organizationsEnabled={}, censusEnabled={}", batchRunId, electionId, organizationsEnabled,
						censusEnabled);

				if (organizationsEnabled) {
					if (processAutomaticOrganizationsSyncForElectionWithLock(electionId, batchRunId, now)) {
						organizationsPhaseRuns++;
					} else {
						organizationsPhaseSkippedLocked++;
					}
				} else {
					appLogger.info("Combined automatic sync organizations phase skipped by configuration. batchRunId={}, electionId={}", batchRunId, electionId);
				}

				if (censusEnabled) {
					if (processAutomaticCensusSyncForElectionWithLock(electionId, batchRunId, now)) {
						censusPhaseRuns++;
					} else {
						censusPhaseSkippedLocked++;
					}
				} else {
					appLogger.info("Combined automatic sync census phase skipped by configuration. batchRunId={}, electionId={}", batchRunId, electionId);
				}

				appLogger.info("Combined automatic sync election finished. batchRunId={}, electionId={}", batchRunId, electionId);
			}

			appLogger.info(
					"Combined automatic sync batch finished. batchRunId={}, processedElections={}, skippedNullWindow={}, skippedNullElection={}, skippedDuplicateElectionWindows={}, skippedManualBoth={}, organizationsPhaseRuns={}, organizationsPhaseSkippedLocked={}, censusPhaseRuns={}, censusPhaseSkippedLocked={}",
					batchRunId, processedElections, skippedNullWindow, skippedNullElection, skippedDuplicateElectionWindows, skippedManualBoth, organizationsPhaseRuns, organizationsPhaseSkippedLocked,
					censusPhaseRuns, censusPhaseSkippedLocked);
		} catch (Exception e) {
			appLogger.error("Error processing combined automatic sync batch. batchRunId={}", batchRunId, e);
		}
	}

	private boolean processAutomaticOrganizationsSyncForElectionWithLock(long electionId, String batchRunId, Date executionDate) {
		boolean lockAcquired = ElectionsCaches.startOrganizationsProcessing(electionId);
		if (!lockAcquired) {
			appLogger.info("Combined automatic sync organizations phase skipped because election is already processing. batchRunId={}, electionId={}", batchRunId, electionId);
			return false;
		}

		Date referenceDate = executionDate != null ? executionDate : new Date();
		long startedAt = System.currentTimeMillis();
		try {
			appLogger.info("Combined automatic sync organizations phase acquired lock. batchRunId={}, electionId={}", batchRunId, electionId);
			getManagerProxy().processAutomaticOrganizationsSyncForElectionTx(electionId, batchRunId, referenceDate.getTime());
		} catch (Exception e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Combined automatic sync organizations phase failed. batchRunId={}, electionId={}", batchRunId, electionId, e);
		} finally {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			appLogger.info("Combined automatic sync organizations phase released lock. batchRunId={}, electionId={}, durationMs={}", batchRunId, electionId,
					Math.max(0L, System.currentTimeMillis() - startedAt));
		}
		return true;
	}

	private boolean processAutomaticCensusSyncForElectionWithLock(long electionId, String batchRunId, Date executionDate) {
		boolean lockAcquired = ElectionsCaches.startCensusProcessing(electionId);
		if (!lockAcquired) {
			appLogger.info("Combined automatic sync census phase skipped because election is already processing. batchRunId={}, electionId={}", batchRunId, electionId);
			return false;
		}

		Date referenceDate = executionDate != null ? executionDate : new Date();
		long startedAt = System.currentTimeMillis();
		try {
			appLogger.info("Combined automatic sync census phase acquired lock. batchRunId={}, electionId={}", batchRunId, electionId);
			getManagerProxy().processAutomaticCensusSyncForElectionTx(electionId, batchRunId, referenceDate.getTime());
		} catch (Exception e) {
			ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Combined automatic sync census phase failed. batchRunId={}, electionId={}", batchRunId, electionId, e);
		} finally {
			ElectionsCaches.finishCensusProcessing(electionId);
			appLogger.info("Combined automatic sync census phase released lock. batchRunId={}, electionId={}, durationMs={}", batchRunId, electionId,
					Math.max(0L, System.currentTimeMillis() - startedAt));
		}
		return true;
	}

	@Override
	@TransactionTimeout(9000)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void processAutomaticOrganizationsSyncForElectionTx(long electionId, String batchRunId, long executionTimestampMs) {
		Date referenceDate = executionTimestampMs > 0 ? new Date(executionTimestampMs) : new Date();
		long startedAt = System.currentTimeMillis();
		appLogger.info("Combined automatic sync organizations transactional phase started. batchRunId={}, electionId={}, executionTimestamp={}", batchRunId, electionId, referenceDate);
		MilacnicSyncResult syncResult = executeMilacnicSyncForElection(electionId, batchRunId, referenceDate, SYSTEM_CRON_ACTOR, true, "AUTO", false);
		if (syncResult == null) {
			syncResult = buildSyncErrorResult("Organizations sync returned no result", buildSyncRunId(electionId, referenceDate));
		}

		int totalRows = Math.max(syncResult.createdRows + syncResult.updatedRows + syncResult.deletedRows, 0);
		ElectionsCaches.updateOrganizationsProcessingProgress(electionId, totalRows, totalRows, syncResult.createdRows, syncResult.updatedRows, syncResult.deletedRows,
				syncResult.createdRows, syncResult.updatedRows, syncResult.deletedRows);
		if (SYNC_STATUS_ERROR.equals(syncResult.status)) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, new AsyncProcessingError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, null, null, syncResult.message));
		} else {
			ElectionsCaches.clearOrganizationsProcessingError(electionId);
		}
		em.flush();
		appLogger.info(
				"Combined automatic sync organizations transactional phase committed. batchRunId={}, electionId={}, status={}, processedRows={}, createdRows={}, updatedRows={}, deletedRows={}, durationMs={}",
				batchRunId, electionId, syncResult.status, syncResult.processedRows, syncResult.createdRows, syncResult.updatedRows, syncResult.deletedRows,
				Math.max(0L, System.currentTimeMillis() - startedAt));
	}

	@Override
	@TransactionTimeout(9000)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void processAutomaticCensusSyncForElectionTx(long electionId, String batchRunId, long executionTimestampMs) {
		Date referenceDate = executionTimestampMs > 0 ? new Date(executionTimestampMs) : new Date();
		long startedAt = System.currentTimeMillis();
		appLogger.info("Combined automatic sync census transactional phase started. batchRunId={}, electionId={}, executionTimestamp={}", batchRunId, electionId, referenceDate);
		MilacnicSyncResult syncResult = executeAutomaticCensusSyncForElection(electionId, false, SYSTEM_CRON_ACTOR, "AUTO", referenceDate, false);

		int totalRows = Math.max(syncResult.createdRows + syncResult.updatedRows + syncResult.deletedRows, 0);
		ElectionsCaches.updateCensusProcessingProgress(electionId, totalRows, totalRows, syncResult.createdRows, syncResult.updatedRows, syncResult.deletedRows,
				syncResult.createdRows, syncResult.updatedRows, syncResult.deletedRows);
		if (SYNC_STATUS_ERROR.equals(syncResult.status)) {
			ElectionsCaches.setCensusProcessingError(electionId, new AsyncProcessingError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, null, null, syncResult.message));
		} else {
			ElectionsCaches.clearCensusProcessingError(electionId);
		}
		em.flush();
		appLogger.info(
				"Combined automatic sync census transactional phase committed. batchRunId={}, electionId={}, status={}, processedRows={}, createdRows={}, updatedRows={}, deletedRows={}, durationMs={}",
				batchRunId, electionId, syncResult.status, syncResult.processedRows, syncResult.createdRows, syncResult.updatedRows, syncResult.deletedRows,
				Math.max(0L, System.currentTimeMillis() - startedAt));
	}

	@Override
	public void processAutomaticCensusSync() {
		Date now = new Date();
		try {
			List<ElectionCalendar> activeWindows = em.createQuery("SELECT c FROM ElectionCalendar c JOIN FETCH c.election WHERE c.calendarKey = :calendarKey AND c.startDate IS NOT NULL AND c.endDate IS NOT NULL AND c.startDate <= :now AND c.endDate > :now AND c.election.closed = false", ElectionCalendar.class).setParameter(QueryParameterNames.CALENDAR_KEY, ElectionCalendarKey.N_23_PERIODO_PADRON_MILACNIC_SYNC).setParameter("now", now).getResultList();
			if (activeWindows == null || activeWindows.isEmpty()) {
				return;
			}

			Set<Long> processedElectionIds = new HashSet<>();
			for (ElectionCalendar window : activeWindows) {
				Election election = window.getElection();
				if (election == null || election.isManageVotersManual() || !processedElectionIds.add(election.getElectionId())) {
					continue;
				}
				executeAutomaticCensusSyncForElection(election.getElectionId(), false, SYSTEM_CRON_ACTOR, "AUTO", now, false);
			}
		} catch (Exception e) {
			appLogger.error("Error processing automatic census sync", e);
		}
	}

	public SyncRun forceElectionCensusSyncFromOrganizations(long electionId, boolean regenerateVoteLinks, String userAdminId, String ip) {
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		MilacnicSyncResult syncResult = executeAutomaticCensusSyncForElection(electionId, regenerateVoteLinks, userAdminId, ip, new Date(), true);
		return syncResult.syncRun;
	}

	@Override
	public boolean queueElectionCensusSyncFromOrganizations(long electionId, boolean regenerateVoteLinks, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startCensusProcessing(electionId);
		if (!queued) {
			appLogger.info("Automatic census force sync already in progress. electionId={}", electionId);
			return false;
		}
		try {
			getManagerProxy().processElectionCensusSyncFromOrganizationsAsync(electionId, regenerateVoteLinks, userAdminId, ip);
			return true;
		} catch (Exception e) {
			ElectionsCaches.finishCensusProcessing(electionId);
			ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Unable to queue automatic census force sync. electionId={}", electionId, e);
			throw new IllegalStateException("Unable to queue automatic census force sync", e);
		}
	}

	@Override
	@Asynchronous
	@TransactionTimeout(7200)
	public void processElectionCensusSyncFromOrganizationsAsync(long electionId, boolean regenerateVoteLinks, String userAdminId, String ip) {
		try {
			ElectionsCaches.markCensusProcessingPreparing(electionId);
			MilacnicSyncResult syncResult = executeAutomaticCensusSyncForElection(electionId, regenerateVoteLinks, userAdminId, ip, new Date(), true);
			int totalRows = Math.max(syncResult.createdRows + syncResult.updatedRows + syncResult.deletedRows, 0);
			ElectionsCaches.updateCensusProcessingProgress(electionId, totalRows, totalRows, syncResult.createdRows, syncResult.updatedRows, syncResult.deletedRows,
					syncResult.createdRows, syncResult.updatedRows, syncResult.deletedRows);
			if (SYNC_STATUS_ERROR.equals(syncResult.status)) {
				ElectionsCaches.setCensusProcessingError(electionId, new AsyncProcessingError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, null, null, syncResult.message));
			} else {
				ElectionsCaches.clearCensusProcessingError(electionId);
			}
			em.flush();
			appLogger.info("Automatic census force sync async processing finished. electionId={}", electionId);
		} catch (Exception e) {
			ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Automatic census force sync async processing failed. electionId={}", electionId, e);
		} finally {
			ElectionsCaches.finishCensusProcessing(electionId);
			appLogger.info(LOG_CENSUS_PROCESSING_FLAG_RELEASED, electionId, "automatic-census-force-sync");
		}
	}

	@Override
	@TransactionTimeout(7200)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int regenerateElectionCensusVoteLinks(long electionId, String userAdminId, String ip) throws CensusValidationException {
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		if (election == null) {
			throw new IllegalStateException("Election not found. electionId=" + electionId);
		}
		List<UserVoter> userVoters = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionId);
		int totalRows = userVoters == null ? 0 : userVoters.size();
		ElectionsCaches.updateCensusProcessingProgress(electionId, 0, totalRows, 0, 0, 0, 0, totalRows, 0);
		int processedRows = 0;
		int updatedRows = 0;
		if (userVoters != null) {
			for (UserVoter userVoter : userVoters) {
				if (userVoter == null) {
					continue;
				}
				userVoter.setVoteToken(StringUtils.createSecureToken());
				em.merge(userVoter);
				processedRows++;
				updatedRows++;
				ElectionsCaches.updateCensusProcessingProgress(electionId, processedRows, totalRows, 0, updatedRows, 0, 0, totalRows, 0);
			}
		}
		String actor = hasText(userAdminId) ? userAdminId.toUpperCase() : SYSTEM_ACTOR;
		persistActivity(userAdminId, ActivityType.UPDATE_TOKEN_USER_CENSUS,
				actor + " regeneró masivamente los links de votación del padrón. Registros actualizados: " + updatedRows, ip, electionId);
		return updatedRows;
	}

	@Override
	public boolean queueElectionCensusRegenerateVoteLinks(long electionId, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startCensusProcessing(electionId);
		if (!queued) {
			appLogger.info("Census mass token regeneration already in progress. electionId={}", electionId);
			return false;
		}
		try {
			getManagerProxy().processElectionCensusRegenerateVoteLinksAsync(electionId, userAdminId, ip);
			return true;
		} catch (Exception e) {
			ElectionsCaches.finishCensusProcessing(electionId);
			ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Unable to queue census mass token regeneration. electionId={}", electionId, e);
			throw new IllegalStateException("Unable to queue census mass token regeneration", e);
		}
	}

	@Override
	@Asynchronous
	@TransactionTimeout(7200)
	public void processElectionCensusRegenerateVoteLinksAsync(long electionId, String userAdminId, String ip) {
		try {
			ElectionsCaches.markCensusProcessingPreparing(electionId);
			getManagerProxy().regenerateElectionCensusVoteLinks(electionId, userAdminId, ip);
			ElectionsCaches.clearCensusProcessingError(electionId);
			em.flush();
			appLogger.info("Census mass token regeneration finished. electionId={}", electionId);
		} catch (CensusValidationException e) {
			ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Census mass token regeneration failed with validation error. electionId={}, messageKey={}", electionId, e.getMessage(), e);
		} catch (Exception e) {
			ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Census mass token regeneration failed. electionId={}", electionId, e);
		} finally {
			ElectionsCaches.finishCensusProcessing(electionId);
			appLogger.info(LOG_CENSUS_PROCESSING_FLAG_RELEASED, electionId, "census-regenerate-links");
		}
	}

	@Override
	@TransactionTimeout(7200)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int deleteElectionCensus(long electionId, String userAdminId, String ip) throws CensusValidationException {
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		if (election == null) {
			throw new IllegalStateException("Election not found. electionId=" + electionId);
		}
		List<UserVoter> userVoters = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionId);
		validateElectionCensusDeleteAllAllowed(userVoters);
		int totalRows = userVoters == null ? 0 : userVoters.size();
		ElectionsCaches.updateCensusProcessingProgress(electionId, 0, totalRows, 0, 0, 0, 0, 0, totalRows);
		int processedRows = 0;
		int deletedRows = 0;
		if (userVoters != null) {
			for (UserVoter userVoter : userVoters) {
				if (userVoter == null) {
					continue;
				}
				em.remove(userVoter);
				processedRows++;
				deletedRows++;
				ElectionsCaches.updateCensusProcessingProgress(electionId, processedRows, totalRows, 0, 0, deletedRows, 0, 0, totalRows);
			}
		}
		election.setElectorsSet(false);
		em.persist(election);
		String actor = hasText(userAdminId) ? userAdminId.toUpperCase() : SYSTEM_ACTOR;
		persistActivity(userAdminId, ActivityType.EDIT_CENSUS,
				actor + " eliminó completamente el padrón de la elección " + election.getTitleSpanish() + ". Registros eliminados: " + deletedRows, ip, electionId);
		return deletedRows;
	}

	@Override
	public boolean queueElectionCensusDeleteAll(long electionId, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startCensusProcessing(electionId);
		if (!queued) {
			appLogger.info("Census mass delete already in progress. electionId={}", electionId);
			return false;
		}
		try {
			getManagerProxy().processElectionCensusDeleteAllAsync(electionId, userAdminId, ip);
			return true;
		} catch (Exception e) {
			ElectionsCaches.finishCensusProcessing(electionId);
			ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Unable to queue census mass delete. electionId={}", electionId, e);
			throw new IllegalStateException("Unable to queue census mass delete", e);
		}
	}

	@Override
	@Asynchronous
	@TransactionTimeout(7200)
	public void processElectionCensusDeleteAllAsync(long electionId, String userAdminId, String ip) {
		boolean requestCensusPurge = false;
		try {
			ElectionsCaches.markCensusProcessingPreparing(electionId);
			getManagerProxy().deleteElectionCensus(electionId, userAdminId, ip);
			ElectionsCaches.clearCensusProcessingError(electionId);
			requestCensusPurge = true;
			em.flush();
			appLogger.info("Census mass delete finished. electionId={}", electionId);
		} catch (CensusValidationException e) {
			ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Census mass delete failed with validation error. electionId={}, messageKey={}", electionId, e.getMessage(), e);
		} catch (Exception e) {
			ElectionsCaches.setCensusProcessingError(electionId, buildAsyncError(KEY_CENSUS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Census mass delete failed. electionId={}", electionId, e);
		} finally {
			ElectionsCaches.finishCensusProcessing(electionId);
			appLogger.info(LOG_CENSUS_PROCESSING_FLAG_RELEASED, electionId, "census-delete-all");
			if (requestCensusPurge) {
				try {
					EJBFactory.getInstance().getMailsSendingEJB().purgeCensusTablesAsync(electionId);
					appLogger.info("Census purge requested after mass delete. electionId={}", electionId);
				} catch (Exception e) {
					appLogger.error("Unable to request census purge after mass delete. electionId={}", electionId, e);
				}
			}
		}
	}

	@Override
	@TransactionTimeout(9000)
	public void processMilacnicOrganizationsSyncForElectionInWindow(long electionId) {
		processMilacnicOrganizationsSyncForElectionInWindow(electionId, false);
	}

	@Override
	@TransactionTimeout(9000)
	public void processMilacnicOrganizationsSyncForElectionInWindow(long electionId, boolean regenerateNominationLinks) {
		Date now = new Date();
		String batchRunId = buildBatchRunId(now);
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			executeMilacnicSyncForElection(electionId, batchRunId, now, SYSTEM_MANUAL_ACTOR, true, "AUTO", regenerateNominationLinks);
		} catch (Exception e) {
			appLogger.error("Error processing MiLACNIC organizations sync for election in window. batchRunId={}, electionId={}", batchRunId, electionId, e);
		}
	}

	@Override
	public boolean queueMilacnicOrganizationsSyncForElectionInWindow(long electionId, String userAdminId, String ip) {
		return queueMilacnicOrganizationsSyncForElectionInWindow(electionId, false, userAdminId, ip);
	}

	@Override
	public boolean queueMilacnicOrganizationsSyncForElectionInWindow(long electionId, boolean regenerateNominationLinks, String userAdminId, String ip) {
		return queueMilacnicOrganizationsSyncForElectionInternal(electionId, true, regenerateNominationLinks, userAdminId, ip);
	}

	@Override
	@Asynchronous
	@TransactionTimeout(9000)
	public void processMilacnicOrganizationsSyncForElectionInWindowAsync(long electionId, String userAdminId, String ip) {
		processMilacnicOrganizationsSyncForElectionInWindowAsync(electionId, false, userAdminId, ip);
	}

	@Override
	@Asynchronous
	@TransactionTimeout(9000)
	public void processMilacnicOrganizationsSyncForElectionInWindowAsync(long electionId, boolean regenerateNominationLinks, String userAdminId, String ip) {
		processMilacnicOrganizationsSyncForElectionAsyncInternal(electionId, true, regenerateNominationLinks, userAdminId, ip);
	}

	@Override
	public boolean queueMilacnicOrganizationsDebtorMirrorSyncForElectionInWindow(long electionId, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startOrganizationsProcessing(electionId);
		if (!queued) {
			appLogger.info("MiLACNIC debtor mirror sync already in progress. electionId={}", electionId);
			return false;
		}
		try {
			getManagerProxy().processMilacnicOrganizationsDebtorMirrorSyncForElectionInWindowAsync(electionId, userAdminId, ip);
			return true;
		} catch (Exception e) {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Unable to queue MiLACNIC debtor mirror sync. electionId={}", electionId, e);
			throw new IllegalStateException("Unable to queue MiLACNIC debtor mirror sync", e);
		}
	}

	@Override
	@Asynchronous
	@TransactionTimeout(9000)
	public void processMilacnicOrganizationsDebtorMirrorSyncForElectionInWindowAsync(long electionId, String userAdminId, String ip) {
		try {
			ElectionsCaches.markOrganizationsProcessingPreparing(electionId);
			Date now = new Date();
			String batchRunId = buildBatchRunId(now);
			String actor = hasText(userAdminId) ? userAdminId : SYSTEM_MANUAL_ACTOR;
			String sourceIp = hasText(ip) ? ip : "AUTO";
			MilacnicSyncResult syncResult = executeMilacnicDebtorMirrorSyncForElection(electionId, batchRunId, now, actor, true, sourceIp);
			if (syncResult != null) {
				int totalRows = Math.max(syncResult.updatedRows, 0);
				ElectionsCaches.updateOrganizationsProcessingProgress(electionId, totalRows, totalRows, 0, syncResult.updatedRows, 0, 0, totalRows, 0);
				if (SYNC_STATUS_ERROR.equals(syncResult.status)) {
					ElectionsCaches.setOrganizationsProcessingError(electionId, new AsyncProcessingError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, null, null, syncResult.message));
				} else {
					ElectionsCaches.clearOrganizationsProcessingError(electionId);
				}
			}
			em.flush();
			appLogger.info("MiLACNIC debtor mirror sync async processing finished. electionId={}", electionId);
		} catch (Exception e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("MiLACNIC debtor mirror sync async processing failed. electionId={}", electionId, e);
		} finally {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			appLogger.info(LOG_ORGANIZATIONS_PROCESSING_FLAG_RELEASED, electionId, "milacnic-debtor-mirror-sync");
		}
	}

	@Override
	@TransactionTimeout(9000)
	public void processMilacnicOrganizationsSyncForElection(long electionId) {
		processMilacnicOrganizationsSyncForElection(electionId, false);
	}

	@Override
	@TransactionTimeout(9000)
	public void processMilacnicOrganizationsSyncForElection(long electionId, boolean regenerateNominationLinks) {
		Date now = new Date();
		String batchRunId = buildBatchRunId(now);
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			executeMilacnicSyncForElection(electionId, batchRunId, now, SYSTEM_MANUAL_ACTOR, false, "AUTO", regenerateNominationLinks);
		} catch (Exception e) {
			appLogger.error("Error processing MiLACNIC organizations sync for election. batchRunId={}, electionId={}", batchRunId, electionId, e);
		}
	}

	@Override
	public boolean queueMilacnicOrganizationsSyncForElection(long electionId, String userAdminId, String ip) {
		return queueMilacnicOrganizationsSyncForElection(electionId, false, userAdminId, ip);
	}

	@Override
	public boolean queueMilacnicOrganizationsSyncForElection(long electionId, boolean regenerateNominationLinks, String userAdminId, String ip) {
		return queueMilacnicOrganizationsSyncForElectionInternal(electionId, false, regenerateNominationLinks, userAdminId, ip);
	}

	@Override
	@Asynchronous
	@TransactionTimeout(9000)
	public void processMilacnicOrganizationsSyncForElectionAsync(long electionId, String userAdminId, String ip) {
		processMilacnicOrganizationsSyncForElectionAsync(electionId, false, userAdminId, ip);
	}

	@Override
	@Asynchronous
	@TransactionTimeout(9000)
	public void processMilacnicOrganizationsSyncForElectionAsync(long electionId, boolean regenerateNominationLinks, String userAdminId, String ip) {
		processMilacnicOrganizationsSyncForElectionAsyncInternal(electionId, false, regenerateNominationLinks, userAdminId, ip);
	}

	@Override
	@TransactionTimeout(9000)
	public void processMilacnicOrganizationsSyncForAllConfiguredElections() {
		Date now = new Date();
		String batchRunId = buildBatchRunId(now);
		try {
			List<ElectionCalendar> configuredWindows = ElectionsDaoFactory.createElectionCalendarDao(em).getCalendarsForOpenElectionsByKey(ElectionCalendarKey.N_23_PERIODO_PADRON_MILACNIC_SYNC);
			if (configuredWindows == null || configuredWindows.isEmpty()) {
				appLogger.info("MiLACNIC forced batch skipped: no elections with configured sync calendar. batchRunId={}", batchRunId);
				return;
			}

			Set<Long> processedElectionIds = new HashSet<>();
			int processedElections = 0;
			int skippedElections = 0;
			int successRuns = 0;
			int noDataRuns = 0;
			int errorRuns = 0;
			for (ElectionCalendar window : configuredWindows) {
				if (window == null || window.getElection() == null) {
					skippedElections++;
					continue;
				}
				long electionId = window.getElection().getElectionId();
				if (!processedElectionIds.add(electionId)) {
					continue;
				}
			MilacnicSyncResult result = executeMilacnicSyncForElection(electionId, batchRunId, now, SYSTEM_MANUAL_ACTOR, false, "AUTO", false);
				if (result == null) {
					skippedElections++;
					continue;
				}
				processedElections++;
				if (SYNC_STATUS_SUCCESS.equals(result.status)) {
					successRuns++;
				} else if (SYNC_STATUS_NO_DATA.equals(result.status)) {
					noDataRuns++;
				} else {
					errorRuns++;
				}
			}
			appLogger.info("MiLACNIC forced batch finished. batchRunId={}, processedElections={}, skippedElections={}, successRuns={}, noDataRuns={}, errorRuns={}", batchRunId, processedElections,
					skippedElections, successRuns, noDataRuns, errorRuns);
		} catch (Exception e) {
			appLogger.error("Error processing MiLACNIC forced sync for all configured elections. batchRunId={}", batchRunId, e);
		}
	}

	@Override
	@TransactionTimeout(9000)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int regenerateElectionOrganizationsNominationLinks(long electionId, String userAdminId, String ip) throws CensusValidationException {
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		if (election == null) {
			throw new IllegalStateException("Election not found. electionId=" + electionId);
		}
		List<Organization> organizations = ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
		int totalRows = organizations == null ? 0 : organizations.size();
		ElectionsCaches.updateOrganizationsProcessingProgress(electionId, 0, totalRows, 0, 0, 0, 0, totalRows, 0);
		int processedRows = 0;
		int updatedRows = 0;
		if (organizations != null) {
			for (Organization organization : organizations) {
				if (organization == null) {
					continue;
				}
				organization.setDoNominationToken(StringUtils.createSecureToken());
				em.merge(organization);
				processedRows++;
				updatedRows++;
				ElectionsCaches.updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, 0, updatedRows, 0, 0, totalRows, 0);
			}
		}
		String actor = hasText(userAdminId) ? userAdminId.toUpperCase() : SYSTEM_ACTOR;
		persistActivity(userAdminId, ActivityType.UPDATE_TOKEN_ORG,
				actor + " regeneró masivamente los links de nominación de organizaciones. Registros actualizados: " + updatedRows, ip, electionId);
		return updatedRows;
	}

	@Override
	public boolean queueElectionOrganizationsRegenerateNominationLinks(long electionId, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startOrganizationsProcessing(electionId);
		if (!queued) {
			appLogger.info("Organizations mass nomination links regeneration already in progress. electionId={}", electionId);
			return false;
		}
		try {
			getManagerProxy().processElectionOrganizationsRegenerateNominationLinksAsync(electionId, userAdminId, ip);
			return true;
		} catch (Exception e) {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Unable to queue organizations mass nomination links regeneration. electionId={}", electionId, e);
			throw new IllegalStateException("Unable to queue organizations mass nomination links regeneration", e);
		}
	}

	@Override
	@Asynchronous
	@TransactionTimeout(9000)
	public void processElectionOrganizationsRegenerateNominationLinksAsync(long electionId, String userAdminId, String ip) {
		try {
			ElectionsCaches.markOrganizationsProcessingPreparing(electionId);
			getManagerProxy().regenerateElectionOrganizationsNominationLinks(electionId, userAdminId, ip);
			ElectionsCaches.clearOrganizationsProcessingError(electionId);
			em.flush();
			appLogger.info("Organizations mass nomination links regeneration finished. electionId={}", electionId);
		} catch (CensusValidationException e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Organizations mass nomination links regeneration failed with validation error. electionId={}, messageKey={}", electionId, e.getMessage(), e);
		} catch (Exception e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Organizations mass nomination links regeneration failed. electionId={}", electionId, e);
		} finally {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			appLogger.info(LOG_ORGANIZATIONS_PROCESSING_FLAG_RELEASED, electionId, "organizations-regenerate-nomination-links");
		}
	}

	@Override
	@TransactionTimeout(9000)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int regenerateElectionOrganizationsSupportLinks(long electionId, String userAdminId, String ip) throws CensusValidationException {
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		if (election == null) {
			throw new IllegalStateException("Election not found. electionId=" + electionId);
		}
		List<SupportNomination> supportNominations = ElectionsDaoFactory.createSupportNominationDao(em).getElectionSupportNominations(electionId);
		List<SupportNomination> supportLinksToRegenerate = new ArrayList<>();
		if (supportNominations != null) {
			for (SupportNomination supportNomination : supportNominations) {
				if (supportNomination == null || supportNomination.getSupportingOrganization() == null) {
					continue;
				}
				supportLinksToRegenerate.add(supportNomination);
			}
		}
		int totalRows = supportLinksToRegenerate.size();
		ElectionsCaches.updateOrganizationsProcessingProgress(electionId, 0, totalRows, 0, 0, 0, 0, totalRows, 0);
		int processedRows = 0;
		int updatedRows = 0;
		for (SupportNomination supportNomination : supportLinksToRegenerate) {
			supportNomination.setToken(StringUtils.createSecureToken());
			em.merge(supportNomination);
			processedRows++;
			updatedRows++;
			ElectionsCaches.updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, 0, updatedRows, 0, 0, totalRows, 0);
		}
		String actor = hasText(userAdminId) ? userAdminId.toUpperCase() : SYSTEM_ACTOR;
		persistActivity(userAdminId, ActivityType.UPDATE_TOKEN_ORG,
				actor + " regeneró masivamente los links de apoyo de organizaciones. Registros actualizados: " + updatedRows, ip, electionId);
		return updatedRows;
	}

	@Override
	public boolean queueElectionOrganizationsRegenerateSupportLinks(long electionId, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startOrganizationsProcessing(electionId);
		if (!queued) {
			appLogger.info("Organizations mass support links regeneration already in progress. electionId={}", electionId);
			return false;
		}
		try {
			getManagerProxy().processElectionOrganizationsRegenerateSupportLinksAsync(electionId, userAdminId, ip);
			return true;
		} catch (Exception e) {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Unable to queue organizations mass support links regeneration. electionId={}", electionId, e);
			throw new IllegalStateException("Unable to queue organizations mass support links regeneration", e);
		}
	}

	@Override
	@Asynchronous
	@TransactionTimeout(9000)
	public void processElectionOrganizationsRegenerateSupportLinksAsync(long electionId, String userAdminId, String ip) {
		try {
			ElectionsCaches.markOrganizationsProcessingPreparing(electionId);
			getManagerProxy().regenerateElectionOrganizationsSupportLinks(electionId, userAdminId, ip);
			ElectionsCaches.clearOrganizationsProcessingError(electionId);
			em.flush();
			appLogger.info("Organizations mass support links regeneration finished. electionId={}", electionId);
		} catch (CensusValidationException e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Organizations mass support links regeneration failed with validation error. electionId={}, messageKey={}", electionId, e.getMessage(), e);
		} catch (Exception e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Organizations mass support links regeneration failed. electionId={}", electionId, e);
		} finally {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			appLogger.info(LOG_ORGANIZATIONS_PROCESSING_FLAG_RELEASED, electionId, "organizations-regenerate-support-links");
		}
	}

	@Override
	@TransactionTimeout(9000)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int deleteElectionOrganizations(long electionId, String userAdminId, String ip) throws CensusValidationException {
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		if (election == null) {
			throw new IllegalStateException("Election not found. electionId=" + electionId);
		}
		validateElectionOrganizationsDeleteAllAllowed(electionId);
		List<Organization> organizations = ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
		int totalRows = organizations == null ? 0 : organizations.size();
		ElectionsCaches.updateOrganizationsProcessingProgress(electionId, 0, totalRows, 0, 0, 0, 0, 0, totalRows);
		int processedRows = 0;
		int deletedRows = 0;
		if (organizations != null) {
			for (Organization organization : organizations) {
				if (organization == null) {
					continue;
				}
				em.remove(organization);
				processedRows++;
				deletedRows++;
				ElectionsCaches.updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, 0, 0, deletedRows, 0, 0, totalRows);
			}
		}
		election.setOrganizationsSet(false);
		em.persist(election);
		String actor = hasText(userAdminId) ? userAdminId.toUpperCase() : SYSTEM_ACTOR;
		persistActivity(userAdminId, ActivityType.DELETE_ORGS,
				actor + " eliminó completamente las organizaciones de la elección " + election.getTitleSpanish() + ". Registros eliminados: " + deletedRows, ip, electionId);
		return deletedRows;
	}

	@Override
	public boolean queueElectionOrganizationsDeleteAll(long electionId, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startOrganizationsProcessing(electionId);
		if (!queued) {
			appLogger.info("Organizations mass delete already in progress. electionId={}", electionId);
			return false;
		}
		try {
			getManagerProxy().processElectionOrganizationsDeleteAllAsync(electionId, userAdminId, ip);
			return true;
		} catch (Exception e) {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Unable to queue organizations mass delete. electionId={}", electionId, e);
			throw new IllegalStateException("Unable to queue organizations mass delete", e);
		}
	}

	@Override
	@Asynchronous
	@TransactionTimeout(9000)
	public void processElectionOrganizationsDeleteAllAsync(long electionId, String userAdminId, String ip) {
		try {
			ElectionsCaches.markOrganizationsProcessingPreparing(electionId);
			getManagerProxy().deleteElectionOrganizations(electionId, userAdminId, ip);
			ElectionsCaches.clearOrganizationsProcessingError(electionId);
			em.flush();
			appLogger.info("Organizations mass delete finished. electionId={}", electionId);
		} catch (CensusValidationException e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Organizations mass delete failed with validation error. electionId={}, messageKey={}", electionId, e.getMessage(), e);
		} catch (Exception e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Organizations mass delete failed. electionId={}", electionId, e);
		} finally {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			appLogger.info(LOG_ORGANIZATIONS_PROCESSING_FLAG_RELEASED, electionId, "organizations-delete-all");
		}
	}

	private boolean queueMilacnicOrganizationsSyncForElectionInternal(long electionId, boolean requireActiveWindow, boolean regenerateNominationLinks, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startOrganizationsProcessing(electionId);
		if (!queued) {
			appLogger.info("MiLACNIC organizations force sync already in progress. electionId={}", electionId);
			return false;
		}
		try {
			if (requireActiveWindow) {
				getManagerProxy().processMilacnicOrganizationsSyncForElectionInWindowAsync(electionId, regenerateNominationLinks, userAdminId, ip);
			} else {
				getManagerProxy().processMilacnicOrganizationsSyncForElectionAsync(electionId, regenerateNominationLinks, userAdminId, ip);
			}
			return true;
		} catch (Exception e) {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Unable to queue MiLACNIC organizations force sync. electionId={}", electionId, e);
			throw new IllegalStateException("Unable to queue MiLACNIC organizations force sync", e);
		}
	}

	private void processMilacnicOrganizationsSyncForElectionAsyncInternal(long electionId, boolean requireActiveWindow, boolean regenerateNominationLinks, String userAdminId, String ip) {
		try {
			ElectionsCaches.markOrganizationsProcessingPreparing(electionId);
			Date now = new Date();
			String batchRunId = buildBatchRunId(now);
			String actor = hasText(userAdminId) ? userAdminId : SYSTEM_MANUAL_ACTOR;
			String sourceIp = hasText(ip) ? ip : "AUTO";
			MilacnicSyncResult syncResult = executeMilacnicSyncForElection(electionId, batchRunId, now, actor, requireActiveWindow, sourceIp, regenerateNominationLinks);
			if (syncResult != null) {
				int totalRows = Math.max(syncResult.createdRows + syncResult.updatedRows, 0);
				ElectionsCaches.updateOrganizationsProcessingProgress(electionId, totalRows, totalRows, syncResult.createdRows, syncResult.updatedRows, 0,
						syncResult.createdRows, syncResult.updatedRows, 0);
				if (SYNC_STATUS_ERROR.equals(syncResult.status)) {
					ElectionsCaches.setOrganizationsProcessingError(electionId, new AsyncProcessingError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, null, null, syncResult.message));
				} else {
					ElectionsCaches.clearOrganizationsProcessingError(electionId);
				}
			}
			em.flush();
			appLogger.info("MiLACNIC organizations force sync async processing finished. electionId={}, requireActiveWindow={}", electionId, requireActiveWindow);
		} catch (Exception e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("MiLACNIC organizations force sync async processing failed. electionId={}, requireActiveWindow={}", electionId, requireActiveWindow, e);
		} finally {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			appLogger.info(LOG_ORGANIZATIONS_PROCESSING_FLAG_RELEASED, electionId, "milacnic-organizations-force-sync");
		}
	}

	private MilacnicSyncResult executeMilacnicSyncForElection(long electionId, String batchRunId, Date now, String activityActor, boolean requireActiveWindow, String activityIp,
			boolean regenerateNominationLinks) {
		if (electionId <= 0) {
			appLogger.warn("MiLACNIC sync election run skipped: invalid electionId. batchRunId={}, electionId={}", batchRunId, electionId);
			return null;
		}

		Election election = em.find(Election.class, electionId);
		if (election == null) {
			appLogger.warn("MiLACNIC sync election run skipped: election not found. batchRunId={}, electionId={}", batchRunId, electionId);
			return null;
		}
		if (election.isClosed()) {
			appLogger.warn("MiLACNIC sync election run skipped: election is closed. batchRunId={}, electionId={}", batchRunId, electionId);
			return null;
		}
		if (election.isManageOrganizationsManual()) {
			appLogger.warn("MiLACNIC sync election run skipped: manual organizations enabled. batchRunId={}, electionId={}", batchRunId, electionId);
			return null;
		}
		if (requireActiveWindow && !isElectionInMilacnicSyncWindow(electionId, now)) {
			appLogger.warn("MiLACNIC sync election run skipped: election is out of sync window. batchRunId={}, electionId={}, referenceDate={}", batchRunId, electionId, now);
			return null;
		}

		String organizationsEndpointTemplate = resolveNonSensitiveTextParameter(Constants.MILACNIC_SYNC_ORGANIZATIONS_ENDPOINT_TEMPLATE);
		if (organizationsEndpointTemplate == null) {
			appLogger.info("MiLACNIC sync election run skipped: parameter {} is not configured. batchRunId={}, electionId={}",
					Constants.MILACNIC_SYNC_ORGANIZATIONS_ENDPOINT_TEMPLATE, batchRunId, electionId);
			return null;
		}
		String milacnicApiToken = normalizeOptionalText(EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.MILACNIC_SYNC_API_TOKEN));
		Integer minOrganizationsRequired = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MIN_ORGANIZATIONS_REQUIRED);
		Integer maxMemberDeactivationsAllowed = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MAX_MEMBER_DEACTIVATIONS);
		Integer minDebtorOrganizationsRequired = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MIN_DEBTOR_ORGANIZATIONS_REQUIRED);
		Integer maxDebtorOrganizationsAllowed = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MAX_DEBTOR_ORGANIZATIONS_ALLOWED);
		Integer minBrazilOrganizationsRequired = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MIN_BRAZIL_ORGANIZATIONS_REQUIRED);

		String syncRunId = buildSyncRunId(electionId, now);
		String endpoint = resolveMilacnicEndpoint(organizationsEndpointTemplate, electionId);
		MilacnicSyncResult syncResult;
		if (endpoint == null) {
			syncResult = buildSyncErrorResult(MILACNIC_ENDPOINT_RESOLUTION_ERROR, syncRunId);
		} else {
			appLogger.info("MiLACNIC sync election run started. batchRunId={}, electionId={}, syncRunId={}, endpoint={}, requireActiveWindow={}, regenerateNominationLinks={}", batchRunId, electionId,
					syncRunId, endpoint, requireActiveWindow, regenerateNominationLinks);
			syncResult = syncOrganizationsForElectionFromMilacnic(electionId, endpoint, milacnicApiToken, syncRunId, now, minOrganizationsRequired, maxMemberDeactivationsAllowed,
					minDebtorOrganizationsRequired, maxDebtorOrganizationsAllowed, minBrazilOrganizationsRequired, regenerateNominationLinks);
		}

		if (regenerateNominationLinks) {
			syncResult.message = appendSyncModeMessage(syncResult.message, "regenerateNominationLinks=true");
		}

		registerOrganizationsSyncSnapshot(electionId, syncResult, now);
		appLogger.info(
				"MiLACNIC sync election run finished. batchRunId={}, electionId={}, syncRunId={}, status={}, processedRows={}, createdRows={}, updatedRows={}, durationMs={}, wsStatusCode={}, wsElapsedMs={}, healthIndicators={}, message={}",
				batchRunId, electionId, syncRunId, syncResult.status, syncResult.processedRows, syncResult.createdRows, syncResult.updatedRows, syncResult.durationMs, syncResult.wsStatusCode,
				syncResult.wsElapsedMs, syncResult.healthIndicators, syncResult.message);

		if (syncResult.updatedRows > 0 || syncResult.createdRows > 0) {
			String description = activityActor + " sincronizó organizaciones desde MiLACNIC. electionId=" + electionId + ", regenerateNominationLinks="
					+ regenerateNominationLinks + ", creadas=" + syncResult.createdRows + ACTIVITY_UPDATED_ROWS_FRAGMENT + syncResult.updatedRows;
			persistActivity(activityActor, ActivityType.EDIT_ORGS, description, activityIp, electionId);
		}
		return syncResult;
	}

	private MilacnicSyncResult executeMilacnicDebtorMirrorSyncForElection(long electionId, String batchRunId, Date now, String activityActor, boolean requireActiveWindow, String activityIp) {
		if (electionId <= 0) {
			appLogger.warn("MiLACNIC debtor mirror sync skipped: invalid electionId. batchRunId={}, electionId={}", batchRunId, electionId);
			return null;
		}

		Election election = em.find(Election.class, electionId);
		if (election == null) {
			appLogger.warn("MiLACNIC debtor mirror sync skipped: election not found. batchRunId={}, electionId={}", batchRunId, electionId);
			return null;
		}
		if (election.isClosed()) {
			appLogger.warn("MiLACNIC debtor mirror sync skipped: election is closed. batchRunId={}, electionId={}", batchRunId, electionId);
			return null;
		}
		if (election.isManageOrganizationsManual()) {
			appLogger.warn("MiLACNIC debtor mirror sync skipped: manual organizations enabled. batchRunId={}, electionId={}", batchRunId, electionId);
			return null;
		}
		if (requireActiveWindow && !isElectionInMilacnicSyncWindow(electionId, now)) {
			appLogger.warn("MiLACNIC debtor mirror sync skipped: election is out of sync window. batchRunId={}, electionId={}, referenceDate={}", batchRunId, electionId, now);
			return null;
		}

		String organizationsEndpointTemplate = resolveNonSensitiveTextParameter(Constants.MILACNIC_SYNC_ORGANIZATIONS_ENDPOINT_TEMPLATE);
		if (organizationsEndpointTemplate == null) {
			appLogger.info("MiLACNIC debtor mirror sync skipped: parameter {} is not configured. batchRunId={}, electionId={}",
					Constants.MILACNIC_SYNC_ORGANIZATIONS_ENDPOINT_TEMPLATE, batchRunId, electionId);
			return null;
		}
		String milacnicApiToken = normalizeOptionalText(EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.MILACNIC_SYNC_API_TOKEN));
		Integer minOrganizationsRequired = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MIN_ORGANIZATIONS_REQUIRED);
		Integer minDebtorOrganizationsRequired = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MIN_DEBTOR_ORGANIZATIONS_REQUIRED);
		Integer maxDebtorOrganizationsAllowed = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MAX_DEBTOR_ORGANIZATIONS_ALLOWED);

		String syncRunId = buildSyncRunId(electionId, now);
		String endpoint = resolveMilacnicEndpoint(organizationsEndpointTemplate, electionId);
		MilacnicSyncResult syncResult;
		if (endpoint == null) {
			syncResult = buildSyncErrorResult(MILACNIC_ENDPOINT_RESOLUTION_ERROR, syncRunId);
		} else {
			appLogger.info(
					"MiLACNIC debtor mirror sync started. batchRunId={}, electionId={}, syncRunId={}, endpoint={}, requireActiveWindow={}",
					batchRunId, electionId, syncRunId, endpoint, requireActiveWindow);
			syncResult = syncOrganizationsDebtorMirrorForElectionFromMilacnic(
					electionId,
					endpoint,
					milacnicApiToken,
					syncRunId,
					now,
					minOrganizationsRequired,
					minDebtorOrganizationsRequired,
					maxDebtorOrganizationsAllowed);
		}

		syncResult.message = appendSyncModeMessage(syncResult.message, "debtorMirrorOnly=true");
		registerOrganizationsSyncSnapshot(electionId, syncResult, now);
		appLogger.info(
				"MiLACNIC debtor mirror sync finished. batchRunId={}, electionId={}, syncRunId={}, status={}, processedRows={}, updatedRows={}, durationMs={}, wsStatusCode={}, wsElapsedMs={}, healthIndicators={}, message={}",
				batchRunId, electionId, syncRunId, syncResult.status, syncResult.processedRows, syncResult.updatedRows, syncResult.durationMs, syncResult.wsStatusCode, syncResult.wsElapsedMs,
				syncResult.healthIndicators, syncResult.message);

		if (syncResult.updatedRows > 0) {
			String description = activityActor + " sincronizó el estado deudor desde MiLACNIC (modo espejo). electionId=" + electionId + ACTIVITY_UPDATED_ROWS_FRAGMENT + syncResult.updatedRows;
			persistActivity(activityActor, ActivityType.EDIT_ORGS, description, activityIp, electionId);
		}
		return syncResult;
	}

	private boolean isElectionInMilacnicSyncWindow(long electionId, Date referenceDate) {
		ElectionCalendar calendar = ElectionsDaoFactory.createElectionCalendarDao(em).getElectionCalendarByKey(electionId, ElectionCalendarKey.N_23_PERIODO_PADRON_MILACNIC_SYNC);
		if (calendar == null || calendar.getStartDate() == null || calendar.getEndDate() == null || referenceDate == null) {
			return false;
		}
		return !referenceDate.before(calendar.getStartDate()) && referenceDate.before(calendar.getEndDate());
	}

	private MilacnicSyncResult buildSyncErrorResult(String message, String syncRunId) {
		MilacnicSyncResult result = new MilacnicSyncResult();
		result.status = SYNC_STATUS_ERROR;
		result.message = message;
		result.syncRunId = syncRunId;
		return result;
	}

	private String appendSyncModeMessage(String message, String modeDetail) {
		if (!hasText(modeDetail)) {
			return message;
		}
		String normalizedModeDetail = modeDetail.trim();
		if (!hasText(message)) {
			return "[" + normalizedModeDetail + "]";
		}
		return message + " [" + normalizedModeDetail + "]";
	}

	private MilacnicSyncResult executeAutomaticCensusSyncForElection(long electionId, boolean regenerateVoteLinks, String userAdminId, String ip, Date executionDate, boolean forceActivityLog) {
		Date now = executionDate != null ? executionDate : new Date();
		String syncRunId = buildSyncRunId(electionId, now);

		Integer minMemberRowsThreshold = resolveNonNegativeIntegerParameter(Constants.MILACNIC_SYNC_MIN_MEMBER_ROWS, Constants.MILACNIC_SYNC_MAX_MEMBER_DEACTIVATIONS);

		MilacnicSyncResult syncResult;
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		if (election == null) {
			appLogger.warn("Cannot run automatic census sync because election was not found. electionId={}", electionId);
			syncResult = buildSyncErrorResult("Election was not found", syncRunId);
			return syncResult;
		} else if (election.isManageVotersManual()) {
			syncResult = buildSyncErrorResult("Election is configured with manual census management", syncRunId);
		} else {
			syncResult = syncElectionCensusFromLocalOrganizations(electionId, syncRunId, minMemberRowsThreshold, regenerateVoteLinks);
		}

		SyncRun syncRun = registerAutomaticCensusSyncSnapshot(electionId, syncResult, now);
		syncResult.syncRun = syncRun;
		String actor = hasText(userAdminId) ? userAdminId : SYSTEM_CRON_ACTOR;
		String sourceIp = hasText(ip) ? ip : "AUTO";
		boolean hasChanges = syncResult.createdRows > 0 || syncResult.updatedRows > 0 || syncResult.deletedRows > 0;
		if (forceActivityLog || hasChanges) {
			String description = actor.toUpperCase(Locale.ROOT) + " sincronizó padrón automático desde organizaciones locales. electionId=" + electionId + ", regenerateVoteLinks="
					+ regenerateVoteLinks + ", status=" + syncResult.status + ", altas=" + syncResult.createdRows + ACTIVITY_UPDATED_ROWS_FRAGMENT + syncResult.updatedRows + ", eliminadas="
					+ syncResult.deletedRows;
			persistActivity(actor, ActivityType.EDIT_CENSUS, description, sourceIp, electionId);
		}
		if (SYNC_STATUS_ERROR.equals(syncResult.status)) {
			appLogger.warn("Automatic census sync finished with ERROR. electionId={}, syncRunId={}, reason={}", electionId, syncResult.syncRunId, syncResult.message);
		}
		return syncResult;
	}

	private void registerOrganizationsSyncSnapshot(long electionId, MilacnicSyncResult syncResult, Date syncTimestamp) {
		if (electionId <= 0 || syncResult == null) {
			return;
		}

		String message = syncResult.message;
		String metricsMessage = buildSyncMetricsMessage(syncResult);
		if (!hasText(message)) {
			message = metricsMessage;
		} else {
			message = message + " [" + metricsMessage + "]";
		}
		if (hasText(syncResult.syncRunId)) {
			message = (message == null ? "" : message + " ") + "[syncRunId=" + syncResult.syncRunId + "]";
		}
		if (!hasText(syncResult.healthIndicators)) {
			syncResult.healthIndicators = buildSyncHealthIndicators(syncResult);
		}

		SyncRun syncRun = new SyncRun();
		syncRun.setElection(em.getReference(Election.class, electionId));
		syncRun.setSyncRunId(truncateForColumn(syncResult.syncRunId, 64));
		syncRun.setSyncAt(syncTimestamp != null ? syncTimestamp : new Date());
		syncRun.setStatus(truncateForColumn(syncResult.status, 32));
		syncRun.setSyncType(SyncRun.SYNC_TYPE_ORGANIZATIONS);
		syncRun.setProcessedRows(syncResult.processedRows);
		syncRun.setCreatedRows(syncResult.createdRows);
		syncRun.setUpdatedRows(syncResult.updatedRows);
		syncRun.setDeletedRows(syncResult.deletedRows);
		syncRun.setDurationMs(syncResult.durationMs);
		syncRun.setWsStatusCode(syncResult.wsStatusCode);
		syncRun.setWsElapsedMs(syncResult.wsElapsedMs);
		syncRun.setHealthIndicators(truncateForColumn(syncResult.healthIndicators, 1000));
		syncRun.setMessage(truncateForColumn(message, 1000));
		em.persist(syncRun);
		notifySyncFailureByEmail(electionId, "organizaciones", syncResult, syncTimestamp);
	}

	private SyncRun registerAutomaticCensusSyncSnapshot(long electionId, MilacnicSyncResult syncResult, Date syncTimestamp) {
		if (electionId <= 0 || syncResult == null) {
			return null;
		}
		String message = syncResult.message;
		String metricsMessage = buildSyncMetricsMessage(syncResult);
		if (!hasText(message)) {
			message = metricsMessage;
		} else {
			message = message + " [" + metricsMessage + "]";
		}
		if (hasText(syncResult.syncRunId)) {
			message = (message == null ? "" : message + " ") + "[syncRunId=" + syncResult.syncRunId + "]";
		}
		message = CENSUS_SYNC_MESSAGE_PREFIX + " " + message;
		SyncRun syncRun = new SyncRun();
		syncRun.setElection(em.getReference(Election.class, electionId));
		syncRun.setSyncRunId(truncateForColumn(syncResult.syncRunId, 64));
		syncRun.setSyncAt(syncTimestamp != null ? syncTimestamp : new Date());
		syncRun.setStatus(truncateForColumn(syncResult.status, 32));
		syncRun.setSyncType(SyncRun.SYNC_TYPE_CENSUS);
		syncRun.setProcessedRows(syncResult.processedRows);
		syncRun.setCreatedRows(syncResult.createdRows);
		syncRun.setUpdatedRows(syncResult.updatedRows);
		syncRun.setDeletedRows(syncResult.deletedRows);
		syncRun.setDurationMs(syncResult.durationMs);
		syncRun.setMessage(truncateForColumn(message, 1000));
		em.persist(syncRun);
		notifySyncFailureByEmail(electionId, "padrón", syncResult, syncTimestamp);
		return syncRun;
	}

	private String buildSyncMetricsMessage(MilacnicSyncResult syncResult) {
		return "processed=" + syncResult.processedRows + ",created=" + syncResult.createdRows + ",updated=" + syncResult.updatedRows + ",deleted=" + syncResult.deletedRows;
	}

	private void notifySyncFailureByEmail(long electionId, String syncTypeLabel, MilacnicSyncResult syncResult, Date syncTimestamp) {
		if (electionId <= 0 || syncResult == null || !SYNC_STATUS_ERROR.equals(syncResult.status)) {
			return;
		}

		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			if (election == null) {
				return;
			}

			String recipient = resolveSyncFailureAlertRecipient(election);
			String supportRecipient = resolveOptionalTextParameter(Constants.DEFAULT_SUPPORT_RECIPIENT);
			if (!hasText(recipient) && !hasText(supportRecipient)) {
				appLogger.warn("Sync failure email skipped because no recipients were resolved. electionId={}, syncType={}, syncRunId={}", electionId, syncTypeLabel, syncResult.syncRunId);
				return;
			}

			Map<String, Object> variables = new HashMap<>();
			variables.put("syncTypeLabel", syncTypeLabel);
			variables.put("syncTimestamp", syncTimestamp != null ? syncTimestamp : new Date());
			variables.put("syncStatus", syncResult.status);
			variables.put("processedRows", syncResult.processedRows);
			variables.put("createdRows", syncResult.createdRows);
			variables.put("updatedRows", syncResult.updatedRows);
			variables.put("deletedRows", syncResult.deletedRows);
			variables.put("syncMessage", hasText(syncResult.message) ? syncResult.message : "-");
			variables.put("syncRunId", hasText(syncResult.syncRunId) ? syncResult.syncRunId : "-");

			EJBFactory.getInstance().getMailsSendingEJB().queueTemplateEmail(
					election,
					EmailTemplateType.SYNC_ERROR_ALERT,
					hasText(recipient) ? recipient : supportRecipient,
					hasText(recipient) ? supportRecipient : null,
					Constants.DEFAULT_EMAIL_LANGUAGE,
					variables);
		} catch (Exception e) {
			appLogger.error("Error queueing sync failure email. electionId={}, syncType={}, syncRunId={}", electionId, syncTypeLabel, syncResult.syncRunId, e);
		}
	}

	private String resolveSyncFailureAlertRecipient(Election election) {
		if (election != null && hasText(election.getDefaultSender())) {
			return election.getDefaultSender().trim();
		}
		if (election != null && hasText(election.getDefaultRecipient())) {
			return election.getDefaultRecipient().trim();
		}
		String systemDefaultSender = getDefaultSender();
		if (hasText(systemDefaultSender)) {
			return systemDefaultSender.trim();
		}
		String systemDefaultRecipient = getDefaultRecipient();
		return hasText(systemDefaultRecipient) ? systemDefaultRecipient.trim() : null;
	}

	private String resolveOptionalTextParameter(String parameterKey) {
		try {
			String value = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(parameterKey);
			return hasText(value) ? value.trim() : null;
		} catch (Exception e) {
			appLogger.error("Error resolving parameter {}", parameterKey, e);
			return null;
		}
	}

	private MilacnicSyncResult syncElectionCensusFromLocalOrganizations(long electionId, String syncRunId, Integer minMemberRowsThreshold, boolean regenerateVoteLinks) {
		MilacnicSyncResult summary = new MilacnicSyncResult();
		summary.syncRunId = syncRunId;
		long syncStartedAtMs = System.currentTimeMillis();
		try {
			Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
			List<Organization> sourceOrganizations = ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
			List<Organization> safeSourceOrganizations = sourceOrganizations == null ? Collections.emptyList() : sourceOrganizations;
			summary.processedRows = safeSourceOrganizations.size();
			Set<String> sourceOrgIds = new HashSet<>();
			List<UserVoter> importedUserVoters = new ArrayList<>();
			for (Organization sourceOrganization : safeSourceOrganizations) {
				if (sourceOrganization == null || !hasText(sourceOrganization.getOrgId())) {
					continue;
				}
				String normalizedOrgId = normalizeOrgId(sourceOrganization.getOrgId());
				if (!sourceOrgIds.add(normalizedOrgId)) {
					return failAutomaticCensusValidation(summary, electionId, "Local organization source contains duplicated orgId " + normalizedOrgId);
				}
				if (sourceOrganization.isMember() && !sourceOrganization.isDeudor() && !hasText(sourceOrganization.getMembershipContactName())) {
					return failAutomaticCensusValidation(summary, electionId, "Local organization source has missing membershipContactName for orgId " + normalizedOrgId);
				}
				if (sourceOrganization.isMember() && !sourceOrganization.isDeudor() && !hasText(sourceOrganization.getMembershipContactEmail())) {
					return failAutomaticCensusValidation(summary, electionId, "Local organization source has missing membershipContactEmail for orgId " + normalizedOrgId);
				}
				if (sourceOrganization.isMember() && !sourceOrganization.isDeudor() && sourceOrganization.getVotes() == null) {
					return failAutomaticCensusValidation(summary, electionId, "Local organization source has missing votes for orgId " + normalizedOrgId);
				}
				if (shouldIncludeOrganizationInAutomaticCensus(sourceOrganization)) {
					importedUserVoters.add(buildAutomaticCensusUserVoter(sourceOrganization));
				}
			}
			if (summary.processedRows <= 0) {
				summary.status = SYNC_STATUS_NO_DATA;
				summary.message = "No organizations found in local source";
				return summary;
			}

			List<UserVoterLite> existingUserVoters = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVotersLite(electionId);
			Set<String> importedOrgIds = prepareImportedCensusOrgIds(importedUserVoters);
			List<UserVoterLite> userVotersToDelete = resolveCensusVotersToDeleteForSnapshotLite(existingUserVoters, importedOrgIds);

			if (minMemberRowsThreshold != null && minMemberRowsThreshold.intValue() > 0 && userVotersToDelete.size() > minMemberRowsThreshold.intValue()) {
				return failAutomaticCensusValidation(summary, electionId, "Automatic census sync would inactivate " + userVotersToDelete.size() + " organizations, above configured limit " + minMemberRowsThreshold);
			}

			List<String> votedUsersToDelete = new ArrayList<>();
			for (UserVoterLite userToDelete : userVotersToDelete) {
				if (userToDelete != null && userToDelete.isVoted()) {
					votedUsersToDelete.add(buildUserVoterIdentifierLite(userToDelete));
				}
			}
			if (!votedUsersToDelete.isEmpty()) {
				return failAutomaticCensusValidation(summary, electionId, "Automatic census sync would remove voters with votes: " + String.join(", ", votedUsersToDelete));
			}

			CensusUpsertResult upsertResult = applyElectionCensusSnapshot(election, importedUserVoters, regenerateVoteLinks, "automatic-sync", true, syncRunId, new Date(syncStartedAtMs));
			summary.createdRows = upsertResult.createdRows;
			summary.updatedRows = upsertResult.updatedRows;
			summary.deletedRows = upsertResult.deletedRows;

			summary.status = SYNC_STATUS_SUCCESS;
			summary.message = "OK";
			return summary;
		} catch (CensusValidationException cve) {
			return failAutomaticCensusValidation(summary, electionId, buildAutomaticCensusValidationMessage(cve));
		} catch (Exception e) {
			appLogger.error("Error syncing automatic census from local organizations for election {}", electionId, e);
			summary.status = SYNC_STATUS_ERROR;
			summary.message = e.getMessage();
			return summary;
		} finally {
			summary.durationMs = Math.max(0L, System.currentTimeMillis() - syncStartedAtMs);
		}
	}

	private String resolveOrganizationSyncLanguage(Organization organization) {
		String language = organization == null ? null : organization.getMembershipContactLanguage();
		String normalized = normalizeLanguageForStorage(language);
		if (normalized != null) {
			return normalized;
		}
		return LanguageCode.SP.getCode();
	}

	private UserVoter buildAutomaticCensusUserVoter(Organization organization) {
		UserVoter userVoter = new UserVoter();
		userVoter.setName(organization.getMembershipContactName().trim());
		userVoter.setOrgName(normalizeOptionalText(organization.getName()));
		userVoter.setMail(normalizeCensusMailForStorage(organization.getMembershipContactEmail()));
		userVoter.setVoteAmount(organization.getVotes());
		userVoter.setCountry(normalizeCountryForStorage(organization.getCountry()));
		userVoter.setLanguage(resolveOrganizationSyncLanguage(organization));
		userVoter.setOrgID(normalizeOrgId(organization.getOrgId()));
		return userVoter;
	}

	private String buildAutomaticCensusValidationMessage(CensusValidationException cve) {
		StringBuilder message = new StringBuilder("Automatic census reconciliation validation failed with key ").append(cve.getMessage());
		if (cve.getErrorRow() != null) {
			message.append(", row=").append(cve.getErrorRow());
		}
		if (hasText(cve.getErrorInfo())) {
			message.append(", info=").append(cve.getErrorInfo());
		}
		return message.toString();
	}

	private boolean shouldIncludeOrganizationInAutomaticCensus(Organization organization) {
		if (organization == null || !organization.isMember() || organization.isDeudor()) {
			return false;
		}
		return hasText(organization.getOrgId()) && hasText(organization.getMembershipContactName()) && hasText(organization.getMembershipContactEmail()) && organization.getVotes() != null;
	}

	private String resolveNonSensitiveTextParameter(String parameterName) {
		String rawValue = normalizeOptionalText(EJBFactory.getInstance().getElectionsParametersEJB().getParameter(parameterName));
		if (rawValue != null) {
			return rawValue;
		}
		appLogger.warn("Parameter {} is empty and no default value was configured", parameterName);
		return null;
	}

	private Integer resolveNonNegativeIntegerParameter(String parameterName, String... fallbackParameterNames) {
		Integer fallbackValue = resolveNonNegativeIntegerParameterFallback(parameterName);
		String rawValue = normalizeOptionalText(EJBFactory.getInstance().getElectionsParametersEJB().getParameter(parameterName));
		if (rawValue == null && fallbackParameterNames != null) {
			for (String fallbackParameterName : fallbackParameterNames) {
				if (!hasText(fallbackParameterName)) {
					continue;
				}
				rawValue = normalizeOptionalText(EJBFactory.getInstance().getElectionsParametersEJB().getParameter(fallbackParameterName));
				if (rawValue != null) {
					break;
				}
			}
		}
		if (rawValue == null) {
			return fallbackValue;
		}
		try {
			int parsed = Integer.parseInt(rawValue);
			if (parsed >= 0) {
				return Integer.valueOf(parsed);
			}
			appLogger.warn("Ignoring negative value for parameter {}: {}. Using default {}", parameterName, rawValue, fallbackValue);
			return fallbackValue;
		} catch (NumberFormatException e) {
			appLogger.warn("Ignoring invalid value for parameter {}: {}. Using default {}", parameterName, rawValue, fallbackValue);
			return fallbackValue;
		}
	}

	private Integer resolveNonNegativeIntegerParameterFallback(String parameterName) {
		if (Constants.MILACNIC_SYNC_MIN_ORGANIZATIONS_REQUIRED.equals(parameterName)) {
			return Integer.valueOf(10000);
		}
		if (Constants.MILACNIC_SYNC_MIN_RECORDS.equals(parameterName)) {
			return Integer.valueOf(10000);
		}
		if (Constants.MILACNIC_SYNC_MAX_MEMBER_DEACTIVATIONS.equals(parameterName)) {
			return Integer.valueOf(100);
		}
		if (Constants.MILACNIC_SYNC_MIN_MEMBER_ROWS.equals(parameterName)) {
			return Integer.valueOf(100);
		}
		if (Constants.MILACNIC_SYNC_MIN_DEBTOR_ORGANIZATIONS_REQUIRED.equals(parameterName)) {
			return Integer.valueOf(2);
		}
		if (Constants.MILACNIC_SYNC_MIN_DEBTOR_ROWS.equals(parameterName)) {
			return Integer.valueOf(2);
		}
		if (Constants.MILACNIC_SYNC_MAX_DEBTOR_ORGANIZATIONS_ALLOWED.equals(parameterName)) {
			return Integer.valueOf(4000);
		}
		if (Constants.MILACNIC_SYNC_MAX_DEBTOR_ROWS.equals(parameterName)) {
			return Integer.valueOf(4000);
		}
		if (Constants.MILACNIC_SYNC_MIN_BRAZIL_ORGANIZATIONS_REQUIRED.equals(parameterName)) {
			return Integer.valueOf(8000);
		}
		if (Constants.MILACNIC_SYNC_MIN_BRAZIL_ROWS.equals(parameterName)) {
			return Integer.valueOf(8000);
		}
		return null;
	}

	private String buildSyncRunId(long electionId, Date syncTimestamp) {
		long timestamp = syncTimestamp != null ? syncTimestamp.getTime() : System.currentTimeMillis();
		String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		return electionId + "-" + timestamp + "-" + shortUuid;
	}

	private String buildBatchRunId(Date syncTimestamp) {
		long timestamp = syncTimestamp != null ? syncTimestamp.getTime() : System.currentTimeMillis();
		String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		return "batch-" + timestamp + "-" + shortUuid;
	}

	private String resolveMilacnicEndpoint(String endpointTemplate, long electionId) {
		String resolved = normalizeOptionalText(endpointTemplate);
		if (resolved == null) {
			return null;
		}
		String endpoint = resolved.replace("{electionId}", String.valueOf(electionId));
		if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
			return endpoint;
		}
		appLogger.warn("Milacnic organizations sync skipped: parameter {} must be absolute (http/https). currentValue={}", Constants.MILACNIC_SYNC_ORGANIZATIONS_ENDPOINT_TEMPLATE, endpoint);
		return null;
	}

	private MilacnicSyncResult syncOrganizationsForElectionFromMilacnic(long electionId, String endpoint, String milacnicApiToken, String syncRunId, Date syncTimestamp, Integer minOrganizationsRequired,
			Integer maxMemberDeactivationsAllowed, Integer minDebtorOrganizationsRequired, Integer maxDebtorOrganizationsAllowed, Integer minBrazilOrganizationsRequired,
			boolean regenerateNominationLinks) {
		MilacnicSyncResult summary = new MilacnicSyncResult();
		summary.syncRunId = syncRunId;
		long syncStartedAtMs = System.currentTimeMillis();
		try {
			appLogger.info(
					"MiLACNIC sync election step started. electionId={}, syncRunId={}, endpoint={}, regenerateNominationLinks={}, thresholds[minOrganizationsRequired={}, maxMemberDeactivationsAllowed={}, minDebtorOrganizationsRequired={}, maxDebtorOrganizationsAllowed={}, minBrazilOrganizationsRequired={}]",
					electionId, syncRunId, endpoint, regenerateNominationLinks, minOrganizationsRequired, maxMemberDeactivationsAllowed, minDebtorOrganizationsRequired, maxDebtorOrganizationsAllowed, minBrazilOrganizationsRequired);
			MilacnicFetchResult fetchResult = fetchMilacnicOrganizations(endpoint, milacnicApiToken);
			summary.wsStatusCode = fetchResult.statusCode;
			summary.wsElapsedMs = fetchResult.elapsedMs;
			if (summary.wsStatusCode != null && (summary.wsStatusCode.intValue() < HttpStatus.SC_SUCCESS || summary.wsStatusCode.intValue() >= HttpStatus.SC_REDIRECTION)) {
				summary.status = SYNC_STATUS_ERROR;
				summary.message = "MiLACNIC sync WS returned status " + summary.wsStatusCode + " for endpoint " + endpoint;
				return summary;
			}

			List<MilacnicOrganizationRecord> incomingOrganizations = fetchResult.organizations != null ? fetchResult.organizations : Collections.emptyList();
			summary.processedRows = incomingOrganizations.size();
			appLogger.info("MiLACNIC sync payload received. electionId={}, syncRunId={}, organizationsReceived={}", electionId, syncRunId, summary.processedRows);

			if (minOrganizationsRequired != null && summary.processedRows < minOrganizationsRequired.intValue()) {
				return failSyncValidation(summary, electionId, MILACNIC_WS_RETURNED_PREFIX + summary.processedRows + " records, below configured minimum " + minOrganizationsRequired);
			}

			int debtorOrganizationsCount = 0;
			int brazilOrganizationsCount = 0;
			Set<String> incomingOrgIds = new HashSet<>();
			for (MilacnicOrganizationRecord incoming : incomingOrganizations) {
				if (incoming == null) {
					continue;
				}
				if (hasText(incoming.orgId)) {
					String normalizedOrgId = normalizeOrgId(incoming.orgId);
					if (!incomingOrgIds.add(normalizedOrgId)) {
						return failSyncValidation(summary, electionId, "MiLACNIC WS returned duplicated orgId " + normalizedOrgId);
					}
				}
				if (Boolean.TRUE.equals(incoming.debtor)) {
					debtorOrganizationsCount++;
				}
				String normalizedCountry = normalizeCountryForStorage(incoming.country);
				if ("BR".equals(normalizedCountry)) {
					brazilOrganizationsCount++;
				}
			}
			summary.debtorOrganizationsCount = Integer.valueOf(debtorOrganizationsCount);
			summary.brazilOrganizationsCount = Integer.valueOf(brazilOrganizationsCount);
			if (minDebtorOrganizationsRequired != null && debtorOrganizationsCount < minDebtorOrganizationsRequired.intValue()) {
				return failSyncValidation(summary, electionId, MILACNIC_WS_RETURNED_PREFIX + debtorOrganizationsCount + " debtor organizations, below configured minimum " + minDebtorOrganizationsRequired);
			}
			if (maxDebtorOrganizationsAllowed != null && debtorOrganizationsCount > maxDebtorOrganizationsAllowed.intValue()) {
				return failSyncValidation(summary, electionId, MILACNIC_WS_RETURNED_PREFIX + debtorOrganizationsCount + " debtor organizations, above configured maximum " + maxDebtorOrganizationsAllowed);
			}
			if (minBrazilOrganizationsRequired != null && brazilOrganizationsCount < minBrazilOrganizationsRequired.intValue()) {
				return failSyncValidation(summary, electionId, MILACNIC_WS_RETURNED_PREFIX + brazilOrganizationsCount + " organizations from Brazil, below configured minimum " + minBrazilOrganizationsRequired);
			}
			appLogger.info("MiLACNIC sync payload distribution. electionId={}, syncRunId={}, debtorOrganizationsCount={}, brazilOrganizationsCount={}", electionId, syncRunId, debtorOrganizationsCount,
					brazilOrganizationsCount);

			Election electionReference = lockElectionForOrganizationsMutation(electionId);
			List<Organization> existingOrganizations = ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
			Map<String, Organization> existingByOrgId = new HashMap<>();
			for (Organization existingOrganization : existingOrganizations) {
				existingByOrgId.put(normalizeOrgId(existingOrganization.getOrgId()), existingOrganization);
			}

			int predictedMemberDeactivations = 0;
			for (Organization existing : existingOrganizations) {
				if (!existing.isMember()) {
					continue;
				}
				if (!incomingOrgIds.contains(normalizeOrgId(existing.getOrgId()))) {
					predictedMemberDeactivations++;
				}
			}
			summary.predictedMemberDeactivations = Integer.valueOf(predictedMemberDeactivations);
			if (maxMemberDeactivationsAllowed != null && predictedMemberDeactivations > maxMemberDeactivationsAllowed.intValue()) {
				return failSyncValidation(summary, electionId, "MiLACNIC WS would set member=false for " + predictedMemberDeactivations + " organizations, above configured limit " + maxMemberDeactivationsAllowed);
			}
			appLogger.info("MiLACNIC sync pre-upsert metrics. electionId={}, syncRunId={}, existingOrganizations={}, incomingOrganizations={}, incomingOrgIds={}, predictedMemberDeactivations={}", electionId,
					syncRunId, existingOrganizations.size(), incomingOrganizations.size(), incomingOrgIds.size(), predictedMemberDeactivations);

			int totalCreatedRows = 0;
			int totalUpdatedRows = 0;
			for (MilacnicOrganizationRecord incoming : incomingOrganizations) {
				if (incoming == null || !hasText(incoming.orgId) || !hasText(incoming.name)) {
					continue;
				}
				Organization existing = existingByOrgId.get(normalizeOrgId(incoming.orgId));
				if (existing == null) {
					totalCreatedRows++;
				} else if (organizationSyncRequiresUpdate(existing, incoming, regenerateNominationLinks)) {
					totalUpdatedRows++;
				}
			}
			for (Organization existing : existingOrganizations) {
				if (existing != null && existing.isMember() && !incomingOrgIds.contains(normalizeOrgId(existing.getOrgId()))) {
					totalUpdatedRows++;
				}
			}
			ElectionsCaches.updateOrganizationsProcessingProgress(electionId, 0, totalCreatedRows + totalUpdatedRows, 0, 0, 0, totalCreatedRows, totalUpdatedRows, 0);

			if (incomingOrganizations.isEmpty()) {
				summary.status = SYNC_STATUS_NO_DATA;
				summary.message = "No organizations returned by MiLACNIC WS";
				appLogger.info("Milacnic organizations sync returned 0 organizations. electionId={}, syncRunId={}", electionId, syncRunId);
				return summary;
			}

			int updatedRowsCount = 0;
			int unchangedExistingRows = 0;
			for (MilacnicOrganizationRecord incoming : incomingOrganizations) {
				if (incoming == null || !hasText(incoming.orgId) || !hasText(incoming.name)) {
					continue;
				}
				String normalizedOrgId = normalizeOrgId(incoming.orgId);
				Organization existing = existingByOrgId.get(normalizedOrgId);
				if (existing == null) {
					Organization created = new Organization();
					created.setElection(electionReference);
					created.setOrgId(incoming.orgId.trim());
					created.setName(incoming.name.trim());
					created.setVotes(incoming.votes);
					String normalizedCategory = normalizeCategoryForStorage(incoming.category);
					if (normalizedCategory != null) {
						created.setCategory(normalizedCategory);
					}
					created.setCountry(normalizeCountryForStorage(incoming.country));
					created.setCnpj(normalizeOptionalText(incoming.cnpj));
					created.setAsn(normalizeAsnForStorage(incoming.asn));
					created.setMembershipContactId(normalizeOptionalText(incoming.membershipContactId));
					created.setMembershipContactName(normalizeOptionalText(incoming.membershipContactName));
					created.setMembershipContactEmail(normalizeOptionalText(incoming.membershipContactEmail));
					created.setMembershipContactLanguage(normalizeLanguageForStorage(incoming.membershipContactLanguage));
					created.setDoNominationToken(StringUtils.createSecureToken());
					created.setDeudor(Boolean.TRUE.equals(incoming.debtor));
					created.setMember(true);
					em.persist(created);
					existingByOrgId.put(normalizedOrgId, created);
					summary.createdRows++;
					continue;
				}

				String beforeSnapshot = buildOrganizationSyncSnapshot(existing);
				boolean changed = false;

				String incomingName = incoming.name.trim();
				if (!Objects.equals(existing.getName(), incomingName)) {
					existing.setName(incomingName);
					changed = true;
				}
				if (incoming.votes != null && !Objects.equals(existing.getVotes(), incoming.votes)) {
					existing.setVotes(incoming.votes);
					changed = true;
				}
				String normalizedCategory = normalizeCategoryForStorage(incoming.category);
				if (normalizedCategory != null && !Objects.equals(existing.getCategory(), normalizedCategory)) {
					existing.setCategory(normalizedCategory);
					changed = true;
				}
				if (incoming.country != null) {
					String normalizedCountry = normalizeCountryForStorage(incoming.country);
					if (!Objects.equals(existing.getCountry(), normalizedCountry)) {
						existing.setCountry(normalizedCountry);
						changed = true;
					}
				}
				if (incoming.cnpj != null) {
					String normalizedCnpj = normalizeOptionalText(incoming.cnpj);
					if (!Objects.equals(existing.getCnpj(), normalizedCnpj)) {
						existing.setCnpj(normalizedCnpj);
						changed = true;
					}
				}
				if (incoming.asn != null) {
					String normalizedAsn = normalizeAsnForStorage(incoming.asn);
					if (!Objects.equals(existing.getAsn(), normalizedAsn)) {
						existing.setAsn(normalizedAsn);
						changed = true;
					}
				}
				if (incoming.membershipContactId != null) {
					String normalizedMembershipContactId = normalizeOptionalText(incoming.membershipContactId);
					if (!Objects.equals(existing.getMembershipContactId(), normalizedMembershipContactId)) {
						existing.setMembershipContactId(normalizedMembershipContactId);
						changed = true;
					}
				}
				if (incoming.membershipContactName != null) {
					String normalizedMembershipContactName = normalizeOptionalText(incoming.membershipContactName);
					if (!Objects.equals(existing.getMembershipContactName(), normalizedMembershipContactName)) {
						existing.setMembershipContactName(normalizedMembershipContactName);
						changed = true;
					}
				}
				if (incoming.membershipContactEmail != null) {
					String normalizedMembershipContactEmail = normalizeOptionalText(incoming.membershipContactEmail);
					if (!Objects.equals(existing.getMembershipContactEmail(), normalizedMembershipContactEmail)) {
						existing.setMembershipContactEmail(normalizedMembershipContactEmail);
						changed = true;
					}
				}
				if (incoming.membershipContactLanguage != null) {
					String normalizedMembershipContactLanguage = normalizeLanguageForStorage(incoming.membershipContactLanguage);
					if (!Objects.equals(existing.getMembershipContactLanguage(), normalizedMembershipContactLanguage)) {
						existing.setMembershipContactLanguage(normalizedMembershipContactLanguage);
						changed = true;
					}
				}
				if (incoming.debtor != null) {
					boolean currentDebtor = existing.isDeudor();
					boolean incomingDebtor = incoming.debtor.booleanValue();
					if (currentDebtor && !incomingDebtor) {
						existing.setDeudor(false);
						changed = true;
					}
				}
				if (!existing.isMember()) {
					existing.setMember(true);
					changed = true;
				}
				boolean nominationLinkChanged = false;
				if (regenerateNominationLinks) {
					existing.setDoNominationToken(StringUtils.createSecureToken());
					nominationLinkChanged = true;
				} else if (!hasText(existing.getDoNominationToken())) {
					existing.setDoNominationToken(StringUtils.createSecureToken());
					nominationLinkChanged = true;
				}
				if (changed || nominationLinkChanged) {
					if (changed) {
						registerSyncAuditText(
								electionId,
								existing.getOrgId(),
								SYNC_AUDIT_FIELD_ORGANIZATION,
								beforeSnapshot,
								buildOrganizationSyncSnapshot(existing),
								syncRunId,
								syncTimestamp);
					}
					updatedRowsCount++;
				} else {
					unchangedExistingRows++;
				}
			}

			// Membership is now derived by presence in MiLACNIC payload:
			// if an org from DB is not returned by WS, it is no longer member.
			for (Organization existing : existingOrganizations) {
				String normalizedOrgId = normalizeOrgId(existing.getOrgId());
				if (incomingOrgIds.contains(normalizedOrgId)) {
					continue;
				}
				if (existing.isMember()) {
					String beforeSnapshot = buildOrganizationSyncSnapshot(existing);
					existing.setMember(false);
					registerSyncAuditText(
							electionId,
							existing.getOrgId(),
							SYNC_AUDIT_FIELD_ORGANIZATION,
							beforeSnapshot,
							buildOrganizationSyncSnapshot(existing),
							syncRunId,
							syncTimestamp);
					updatedRowsCount++;
				}
			}
			summary.updatedRows = updatedRowsCount;
			summary.unchangedExistingRows = Integer.valueOf(unchangedExistingRows);
			summary.status = SYNC_STATUS_SUCCESS;
			summary.message = "OK";
			appLogger.info("MiLACNIC sync election step completed. electionId={}, syncRunId={}, status={}, processedRows={}, createdRows={}, updatedRows={}", electionId, syncRunId, summary.status,
					summary.processedRows, summary.createdRows, summary.updatedRows);
			return summary;
		} catch (Exception e) {
			appLogger.error("Error syncing MiLACNIC organizations for election {}. syncRunId={}, endpoint={}", electionId, syncRunId, endpoint, e);
			summary.status = SYNC_STATUS_ERROR;
			summary.message = e.getMessage();
			return summary;
		} finally {
			summary.durationMs = Math.max(0L, System.currentTimeMillis() - syncStartedAtMs);
			summary.healthIndicators = buildSyncHealthIndicators(summary);
		}
	}

	private MilacnicSyncResult syncOrganizationsDebtorMirrorForElectionFromMilacnic(long electionId, String endpoint, String milacnicApiToken, String syncRunId, Date syncTimestamp,
			Integer minOrganizationsRequired, Integer minDebtorOrganizationsRequired, Integer maxDebtorOrganizationsAllowed) {
		MilacnicSyncResult summary = new MilacnicSyncResult();
		summary.syncRunId = syncRunId;
		long syncStartedAtMs = System.currentTimeMillis();
		try {
			appLogger.info(
					"MiLACNIC debtor mirror step started. electionId={}, syncRunId={}, endpoint={}, thresholds[minOrganizationsRequired={}, minDebtorOrganizationsRequired={}, maxDebtorOrganizationsAllowed={}]",
					electionId, syncRunId, endpoint, minOrganizationsRequired, minDebtorOrganizationsRequired, maxDebtorOrganizationsAllowed);
			MilacnicFetchResult fetchResult = fetchMilacnicOrganizations(endpoint, milacnicApiToken);
			summary.wsStatusCode = fetchResult.statusCode;
			summary.wsElapsedMs = fetchResult.elapsedMs;
			if (summary.wsStatusCode != null && (summary.wsStatusCode.intValue() < HttpStatus.SC_SUCCESS || summary.wsStatusCode.intValue() >= HttpStatus.SC_REDIRECTION)) {
				summary.status = SYNC_STATUS_ERROR;
				summary.message = "MiLACNIC sync WS returned status " + summary.wsStatusCode + " for endpoint " + endpoint;
				return summary;
			}

			List<MilacnicOrganizationRecord> incomingOrganizations = fetchResult.organizations != null ? fetchResult.organizations : Collections.emptyList();
			summary.processedRows = incomingOrganizations.size();
			if (minOrganizationsRequired != null && summary.processedRows < minOrganizationsRequired.intValue()) {
				return failSyncValidation(summary, electionId, MILACNIC_WS_RETURNED_PREFIX + summary.processedRows + " records, below configured minimum " + minOrganizationsRequired);
			}

			Map<String, Boolean> incomingDebtorByOrgId = new HashMap<>();
			int debtorOrganizationsCount = 0;
			for (MilacnicOrganizationRecord incoming : incomingOrganizations) {
				if (incoming == null || !hasText(incoming.orgId)) {
					continue;
				}
				String normalizedOrgId = normalizeOrgId(incoming.orgId);
				if (incomingDebtorByOrgId.containsKey(normalizedOrgId)) {
					return failSyncValidation(summary, electionId, "MiLACNIC WS returned duplicated orgId " + normalizedOrgId);
				}
				boolean incomingDebtor = Boolean.TRUE.equals(incoming.debtor);
				incomingDebtorByOrgId.put(normalizedOrgId, incomingDebtor);
				if (incomingDebtor) {
					debtorOrganizationsCount++;
				}
			}
			summary.debtorOrganizationsCount = Integer.valueOf(debtorOrganizationsCount);
			if (minDebtorOrganizationsRequired != null && debtorOrganizationsCount < minDebtorOrganizationsRequired.intValue()) {
				return failSyncValidation(summary, electionId, MILACNIC_WS_RETURNED_PREFIX + debtorOrganizationsCount + " debtor organizations, below configured minimum " + minDebtorOrganizationsRequired);
			}
			if (maxDebtorOrganizationsAllowed != null && debtorOrganizationsCount > maxDebtorOrganizationsAllowed.intValue()) {
				return failSyncValidation(summary, electionId, MILACNIC_WS_RETURNED_PREFIX + debtorOrganizationsCount + " debtor organizations, above configured maximum " + maxDebtorOrganizationsAllowed);
			}

			lockElectionForOrganizationsMutation(electionId);
			List<Organization> existingOrganizations = ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
			int totalRows = existingOrganizations == null ? 0 : existingOrganizations.size();
			ElectionsCaches.updateOrganizationsProcessingProgress(electionId, 0, totalRows, 0, 0, 0, 0, totalRows, 0);

			int processedRows = 0;
			int updatedRows = 0;
			int unchangedRows = 0;
			if (existingOrganizations != null) {
				for (Organization existing : existingOrganizations) {
					if (existing == null || !hasText(existing.getOrgId())) {
						continue;
					}
					String normalizedOrgId = normalizeOrgId(existing.getOrgId());
					boolean incomingDebtor = Boolean.TRUE.equals(incomingDebtorByOrgId.get(normalizedOrgId));
					boolean currentDebtor = existing.isDeudor();
					if (currentDebtor != incomingDebtor) {
						existing.setDeudor(incomingDebtor);
						registerSyncAuditText(
								electionId,
								existing.getOrgId(),
								SYNC_AUDIT_FIELD_DEUDOR,
								String.valueOf(currentDebtor),
								String.valueOf(incomingDebtor),
								syncRunId,
								syncTimestamp);
						updatedRows++;
					} else {
						unchangedRows++;
					}
					processedRows++;
					ElectionsCaches.updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, 0, updatedRows, 0, 0, totalRows, 0);
				}
			}

			summary.updatedRows = updatedRows;
			summary.unchangedExistingRows = Integer.valueOf(unchangedRows);
			summary.status = SYNC_STATUS_SUCCESS;
			summary.message = "OK";
			return summary;
		} catch (Exception e) {
			appLogger.error("Error syncing MiLACNIC debtor mirror for election {}. syncRunId={}, endpoint={}", electionId, syncRunId, endpoint, e);
			summary.status = SYNC_STATUS_ERROR;
			summary.message = e.getMessage();
			return summary;
		} finally {
			summary.durationMs = Math.max(0L, System.currentTimeMillis() - syncStartedAtMs);
			summary.healthIndicators = buildSyncHealthIndicators(summary);
		}
	}

	private String buildSyncHealthIndicators(MilacnicSyncResult summary) {
		if (summary == null) {
			return null;
		}
		StringBuilder indicators = new StringBuilder();
		indicators.append("wsStatusCode=").append(metricValue(summary.wsStatusCode));
		indicators.append(", wsElapsedMs=").append(metricValue(summary.wsElapsedMs));
		indicators.append(", durationMs=").append(summary.durationMs);
		indicators.append(", debtorOrganizationsCount=").append(metricValue(summary.debtorOrganizationsCount));
		indicators.append(", brazilOrganizationsCount=").append(metricValue(summary.brazilOrganizationsCount));
		indicators.append(", predictedMemberDeactivations=").append(metricValue(summary.predictedMemberDeactivations));
		indicators.append(", processedRows=").append(summary.processedRows);
		indicators.append(", createdRows=").append(summary.createdRows);
		indicators.append(", updatedRows=").append(summary.updatedRows);
		indicators.append(", unchangedExistingRows=").append(metricValue(summary.unchangedExistingRows));
		return indicators.toString();
	}

	private String metricValue(Number value) {
		return value == null ? "-" : String.valueOf(value);
	}

	private MilacnicSyncResult failSyncValidation(MilacnicSyncResult summary, long electionId, String message) {
		summary.status = SYNC_STATUS_ERROR;
		summary.message = message;
		appLogger.warn("MiLACNIC sync validation failed. electionId={}, syncRunId={}, reason={}", electionId, summary.syncRunId, message);
		return summary;
	}

	private MilacnicSyncResult failAutomaticCensusValidation(MilacnicSyncResult summary, long electionId, String message) {
		summary.status = SYNC_STATUS_ERROR;
		summary.message = message;
		appLogger.warn("Automatic census sync validation failed. electionId={}, syncRunId={}, reason={}", electionId, summary.syncRunId, message);
		return summary;
	}

	private boolean organizationSyncRequiresUpdate(Organization existing, MilacnicOrganizationRecord incoming, boolean regenerateNominationLinks) {
		if (existing == null || incoming == null || !hasText(incoming.name)) {
			return false;
		}
		if (regenerateNominationLinks) {
			return true;
		}
		String incomingName = incoming.name.trim();
		if (!Objects.equals(existing.getName(), incomingName)) {
			return true;
		}
		if (incoming.votes != null && !Objects.equals(existing.getVotes(), incoming.votes)) {
			return true;
		}
		String normalizedCategory = normalizeCategoryForStorage(incoming.category);
		if (normalizedCategory != null && !Objects.equals(existing.getCategory(), normalizedCategory)) {
			return true;
		}
		if (incoming.country != null) {
			String normalizedCountry = normalizeCountryForStorage(incoming.country);
			if (!Objects.equals(existing.getCountry(), normalizedCountry)) {
				return true;
			}
		}
		if (incoming.cnpj != null) {
			String normalizedCnpj = normalizeOptionalText(incoming.cnpj);
			if (!Objects.equals(existing.getCnpj(), normalizedCnpj)) {
				return true;
			}
		}
		if (incoming.asn != null) {
			String normalizedAsn = normalizeAsnForStorage(incoming.asn);
			if (!Objects.equals(existing.getAsn(), normalizedAsn)) {
				return true;
			}
		}
		if (incoming.membershipContactId != null) {
			String normalizedMembershipContactId = normalizeOptionalText(incoming.membershipContactId);
			if (!Objects.equals(existing.getMembershipContactId(), normalizedMembershipContactId)) {
				return true;
			}
		}
		if (incoming.membershipContactName != null) {
			String normalizedMembershipContactName = normalizeOptionalText(incoming.membershipContactName);
			if (!Objects.equals(existing.getMembershipContactName(), normalizedMembershipContactName)) {
				return true;
			}
		}
		if (incoming.membershipContactEmail != null) {
			String normalizedMembershipContactEmail = normalizeOptionalText(incoming.membershipContactEmail);
			if (!Objects.equals(existing.getMembershipContactEmail(), normalizedMembershipContactEmail)) {
				return true;
			}
		}
		if (incoming.membershipContactLanguage != null) {
			String normalizedMembershipContactLanguage = normalizeLanguageForStorage(incoming.membershipContactLanguage);
			if (!Objects.equals(existing.getMembershipContactLanguage(), normalizedMembershipContactLanguage)) {
				return true;
			}
		}
		if (incoming.debtor != null && existing.isDeudor() && !incoming.debtor.booleanValue()) {
			return true;
		}
		if (!existing.isMember()) {
			return true;
		}
		return !hasText(existing.getDoNominationToken());
	}

	private String buildOrganizationSyncSnapshot(Organization organization) {
		if (organization == null) {
			return "-";
		}
		StringBuilder snapshot = new StringBuilder();
		snapshot.append("name=").append(valueOrDash(organization.getName()));
		snapshot.append(", votes=").append(valueOrDash(organization.getVotes()));
		snapshot.append(", category=").append(valueOrDash(organization.getCategory()));
		snapshot.append(", country=").append(valueOrDash(organization.getCountry()));
		snapshot.append(", cnpj=").append(valueOrDash(organization.getCnpj()));
		snapshot.append(", asn=").append(valueOrDash(organization.getAsn()));
		snapshot.append(", membershipContactId=").append(valueOrDash(organization.getMembershipContactId()));
		snapshot.append(", membershipContactName=").append(valueOrDash(organization.getMembershipContactName()));
		snapshot.append(", membershipContactEmail=").append(valueOrDash(organization.getMembershipContactEmail()));
		snapshot.append(", membershipContactLanguage=").append(valueOrDash(organization.getMembershipContactLanguage()));
		snapshot.append(", member=").append(organization.isMember());
		snapshot.append(", deudor=").append(organization.isDeudor());
		return snapshot.toString();
	}

	private String buildCensusSyncSnapshot(UserVoter importedUserVoter) {
		if (importedUserVoter == null) {
			return "-";
		}
		return buildCensusSyncSnapshot(
				importedUserVoter.getName(),
				importedUserVoter.getOrgName(),
				importedUserVoter.getMail(),
				importedUserVoter.getVoteAmount(),
				importedUserVoter.getCountry(),
				importedUserVoter.getLanguage(),
				importedUserVoter.getOrgID());
	}

	private String buildCensusSyncSnapshot(UserVoterLite existingUserVoter) {
		if (existingUserVoter == null) {
			return "-";
		}
		return buildCensusSyncSnapshot(
				existingUserVoter.getName(),
				existingUserVoter.getOrgName(),
				existingUserVoter.getMail(),
				existingUserVoter.getVoteAmount(),
				existingUserVoter.getCountry(),
				existingUserVoter.getLanguage(),
				existingUserVoter.getOrgID());
	}

	private String buildCensusSyncSnapshot(String name, String orgName, String mail, Integer voteAmount, String country, String language, String orgId) {
		StringBuilder snapshot = new StringBuilder();
		snapshot.append("name=").append(valueOrDash(normalizeOptionalText(name)));
		snapshot.append(", orgName=").append(valueOrDash(normalizeOptionalText(orgName)));
		snapshot.append(", mail=").append(valueOrDash(normalizeCensusMailForStorage(mail)));
		snapshot.append(", votes=").append(valueOrDash(voteAmount));
		snapshot.append(", country=").append(valueOrDash(normalizeCountryForStorage(country)));
		snapshot.append(", language=").append(valueOrDash(normalizeLanguageForStorage(language)));
		snapshot.append(", orgId=").append(valueOrDash(normalizeCensusOrgId(orgId)));
		return snapshot.toString();
	}

	private String valueOrDash(Object value) {
		if (value == null) {
			return "-";
		}
		String text = value.toString().trim();
		return text.isEmpty() ? "-" : text;
	}

	private void registerSyncAuditText(long electionId, String orgId, String fieldName, String oldText, String newText, String syncRunId, Date syncTimestamp) {
		if (!hasText(orgId) || !hasText(fieldName) || !hasText(syncRunId)) {
			return;
		}
		SyncAudit audit = new SyncAudit();
		audit.setElection(em.getReference(Election.class, electionId));
		audit.setOrgId(orgId.trim());
		audit.setFieldName(fieldName.trim());
		audit.setOldText(normalizeOptionalText(oldText));
		audit.setNewText(normalizeOptionalText(newText));
		audit.setSyncRunId(syncRunId);
		audit.setEventDate(syncTimestamp != null ? syncTimestamp : new Date());
		em.persist(audit);
	}

	private MilacnicFetchResult fetchMilacnicOrganizations(String endpoint, String authToken) throws Exception {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			long requestStartMs = System.currentTimeMillis();
			appLogger.info("MiLACNIC WS request started. endpoint={}, hasAuthToken={}", endpoint, hasText(authToken));
			HttpGet request = new HttpGet(endpoint);
			request.setHeader(HttpHeaders.ACCEPT, "application/json");
			if (hasText(authToken)) {
				request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
				request.setHeader("X-Auth-Token", authToken);
			}
			try (CloseableHttpResponse response = httpClient.execute(request)) {
				int statusCode = response.getCode();
				String body = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : "";
				long responseElapsedMs = System.currentTimeMillis() - requestStartMs;
				appLogger.info("MiLACNIC WS response received. endpoint={}, statusCode={}, bodyLength={}, elapsedMs={}", endpoint, statusCode, body.length(), responseElapsedMs);
				MilacnicFetchResult fetchResult = new MilacnicFetchResult();
				fetchResult.statusCode = Integer.valueOf(statusCode);
				fetchResult.elapsedMs = Long.valueOf(responseElapsedMs);
				if (statusCode < HttpStatus.SC_SUCCESS || statusCode >= HttpStatus.SC_REDIRECTION) {
					appLogger.warn("MiLACNIC WS returned non-success status. endpoint={}, statusCode={}", endpoint, statusCode);
					return fetchResult;
				}
				List<MilacnicOrganizationRecord> parsedOrganizations = parseMilacnicOrganizationsResponse(body);
				long totalElapsedMs = System.currentTimeMillis() - requestStartMs;
				fetchResult.organizations = parsedOrganizations;
				fetchResult.elapsedMs = Long.valueOf(totalElapsedMs);
				appLogger.info("MiLACNIC WS response parsed. endpoint={}, parsedOrganizations={}, totalElapsedMs={}", endpoint, parsedOrganizations.size(), totalElapsedMs);
				return fetchResult;
			}
		}
	}

	private List<MilacnicOrganizationRecord> parseMilacnicOrganizationsResponse(String responseBody) throws IOException {
		List<MilacnicOrganizationRecord> result = new ArrayList<>();
		if (!hasText(responseBody)) {
			return result;
		}

		JsonNode root = OBJECT_MAPPER.readTree(responseBody);
		JsonNode organizationsNode = resolveOrganizationsNode(root);
		if (organizationsNode == null || !organizationsNode.isArray()) {
			appLogger.warn("MiLACNIC sync response did not include an organizations array");
			return result;
		}

		for (JsonNode node : organizationsNode) {
			if (node == null || !node.isObject()) {
				continue;
			}
			String orgId = readFirstText(node, "orgId", "org_id", "organizationId", "organization_id", "id");
			String name = readFirstText(node, "name", "organizationName", "organization_name");
			if (!hasText(orgId) || !hasText(name)) {
				continue;
			}

			MilacnicOrganizationRecord record = new MilacnicOrganizationRecord();
			record.orgId = orgId;
			record.name = name;
			record.votes = readFirstInteger(node, "votes", "voteAmount", "vote_amount");
			record.category = readFirstText(node, "category", "categoria");
			record.country = readFirstText(node, "country", "countryCode", "country_code");
			record.cnpj = readFirstText(node, "cnpj");
			record.asn = readFirstText(node, "asn");
			record.membershipContactId = readFirstText(node, "membershipContactId", "membership_contact_id", "contactId", "contact_id");
			record.membershipContactName = readFirstText(node, "membershipContactName", "membership_contact_name", "contactName", "contact_name");
			record.membershipContactEmail = readFirstText(node, "membershipContactEmail", "membership_contact_email", "contactEmail", "contact_email");
			record.membershipContactLanguage = readFirstText(node, "membershipContactLanguage", "membership_contact_language", "contactLanguage", "contact_language", "language", "lang");
			record.debtor = readFirstBoolean(node, "debtor", "deudor");
			result.add(record);
		}
		return result;
	}

	private JsonNode resolveOrganizationsNode(JsonNode root) {
		if (root == null) {
			return null;
		}
		if (root.isArray()) {
			return root;
		}
		if (!root.isObject()) {
			return null;
		}
		if (root.has(JSON_FIELD_ORGANIZATIONS)) {
			return root.get(JSON_FIELD_ORGANIZATIONS);
		}
		if (root.has("data")) {
			JsonNode data = root.get("data");
			if (data != null && data.isArray()) {
				return data;
			}
			if (data != null && data.isObject() && data.has(JSON_FIELD_ORGANIZATIONS)) {
				return data.get(JSON_FIELD_ORGANIZATIONS);
			}
		}
		if (root.has("items")) {
			return root.get("items");
		}
		return null;
	}

	private String readFirstText(JsonNode node, String... keys) {
		if (node == null || keys == null) {
			return null;
		}
		for (String key : keys) {
			if (key == null || !node.has(key)) {
				continue;
			}
			JsonNode valueNode = node.get(key);
			if (valueNode == null || valueNode.isNull()) {
				continue;
			}
			String text = normalizeOptionalText(valueNode.asText(null));
			if (text != null) {
				return text;
			}
		}
		return null;
	}

	private Integer readFirstInteger(JsonNode node, String... keys) {
		if (node == null || keys == null) {
			return null;
		}
		for (String key : keys) {
			if (key == null || !node.has(key)) {
				continue;
			}
			JsonNode valueNode = node.get(key);
			if (valueNode == null || valueNode.isNull()) {
				continue;
			}
			if (valueNode.isNumber()) {
				return Integer.valueOf(valueNode.asInt());
			}
			String rawValue = normalizeOptionalText(valueNode.asText(null));
			if (rawValue == null) {
				continue;
			}
			try {
				return Integer.valueOf(Integer.parseInt(rawValue));
			} catch (NumberFormatException ignored) {
				// ignored on purpose, keep looking for another key
			}
		}
		return null;
	}

	private Boolean readFirstBoolean(JsonNode node, String... keys) {
		if (node == null || keys == null) {
			return null;
		}
		for (String key : keys) {
			if (key == null || !node.has(key)) {
				continue;
			}
			JsonNode valueNode = node.get(key);
			if (valueNode == null || valueNode.isNull()) {
				continue;
			}
			if (valueNode.isBoolean()) {
				return Boolean.valueOf(valueNode.asBoolean());
			}
			String rawValue = normalizeOptionalText(valueNode.asText(null));
			if (rawValue == null) {
				continue;
			}
			String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
			if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "si".equals(normalized) || "sí".equals(normalized)) {
				return Boolean.TRUE;
			}
			if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized)) {
				return Boolean.FALSE;
			}
		}
		return null;
	}

	private String normalizeLanguageForStorage(String languageCode) {
		String normalized = normalizeOptionalText(languageCode);
		if (normalized == null) {
			return null;
		}
		String upper = normalized.toUpperCase(Locale.ROOT);
		if ("ES".equals(upper)) {
			return LanguageCode.SP.getCode();
		}
		return upper;
	}

	private String normalizeCategoryForStorage(String category) {
		String normalized = normalizeOptionalText(category);
		if (normalized == null) {
			return null;
		}
		return CategoriasEnum.fromValue(normalized) != null ? normalized : null;
	}

	private String truncateForColumn(String value, int maxLength) {
		if (value == null || maxLength <= 0 || value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}

	private void processCandidateDailyReminders(LocalDate today, Map<Long, Boolean> reminderCalendarWindowByElection, Map<Long, Boolean> candidateReminderEvaluationExtensionCalendarWindowByElection) {
		List<Candidate> candidates = ElectionsDaoFactory.createCandidateDao(em).getCandidatesWithEmail();
		for (Candidate candidate : candidates) {
			processReminderSafely("candidate reminder, candidateId=" + (candidate != null ? candidate.getCandidateId() : null), () -> processCandidateDailyReminder(candidate, today, reminderCalendarWindowByElection, candidateReminderEvaluationExtensionCalendarWindowByElection));
		}
	}

	private void processCandidateDailyReminder(Candidate candidate, LocalDate today, Map<Long, Boolean> reminderCalendarWindowByElection, Map<Long, Boolean> candidateReminderEvaluationExtensionCalendarWindowByElection) {
		if (candidate == null || !hasText(candidate.getMail()) || candidate.getElection() == null) {
			return;
		}
		if (!isCandidateReminderStageEnabled(candidate, reminderCalendarWindowByElection, candidateReminderEvaluationExtensionCalendarWindowByElection)) {
			return;
		}
		ReminderFrequency reminderFrequency = candidate.getReminderFrequency();
		if (!isReminderEnabledOnDate(reminderFrequency, today)) {
			return;
		}
		if (!hasPendingActionsForCandidate(candidate)) {
			return;
		}

		EJBFactory.getInstance().getMailsSendingEJB().queueCandidateReminderToCandidate(candidate.getCandidateId());
		String description = "SYSTEM_CRON ejecutó recordatorio diario de candidato. date=" + today + ", frequency=" + reminderFrequency.name() + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", candidateName=" + safeValue(candidate.getName()) + ", candidateEmail=" + safeValue(candidate.getMail());
		persistActivity(SYSTEM_CRON_ACTOR, ActivityType.EDIT_ELECTION, description, "AUTO", candidate.getElection().getElectionId());
	}

	private boolean isCandidateReminderStageEnabled(Candidate candidate, Map<Long, Boolean> reminderCalendarWindowByElection, Map<Long, Boolean> candidateReminderEvaluationExtensionCalendarWindowByElection) {
		if (candidate == null || candidate.getElection() == null) {
			return false;
		}
		Election election = candidate.getElection();
		if (isReminderStageEnabledForElection(election, reminderCalendarWindowByElection)) {
			return true;
		}
		if (!hasOnlyEvaluationTaskPendingForCandidate(candidate)) {
			return false;
		}
		return isCandidateReminderEvaluationExtensionStageEnabledForElection(election, candidateReminderEvaluationExtensionCalendarWindowByElection);
	}

	private void processAuditorDailyReminders(LocalDate today, Map<Long, Boolean> auditorCandidateReminderCalendarWindowByElection) {
		List<Auditor> auditors = ElectionsDaoFactory.createAuditorDao(em).getAuditorsWithEmail();
		for (Auditor auditor : auditors) {
			processReminderSafely("auditor candidate reminder, auditorId=" + (auditor != null ? auditor.getAuditorId() : null), () -> processAuditorDailyReminder(auditor, today, auditorCandidateReminderCalendarWindowByElection));
		}
	}

	private void processAuditorDailyReminder(Auditor auditor, LocalDate today, Map<Long, Boolean> auditorCandidateReminderCalendarWindowByElection) {
		if (!isAutomaticAuditorReminderEligible(auditor)) {
			return;
		}
		if (!isAuditorCandidateReminderStageEnabledForElection(auditor.getElection(), auditorCandidateReminderCalendarWindowByElection)) {
			return;
		}
		ReminderFrequency reminderFrequency = auditor.getReminderFrequency();
		if (!isReminderEnabledOnDate(reminderFrequency, today)) {
			return;
		}
		if (!hasPendingActionsForAuditor(auditor)) {
			return;
		}

		EJBFactory.getInstance().getMailsSendingEJB().queueAuditorReminderToAuditor(auditor.getAuditorId());
		String description = "SYSTEM_CRON ejecutó recordatorio diario de auditor. date=" + today + ", frequency=" + reminderFrequency.name() + ", auditorId=" + auditor.getAuditorId() + ", auditorName=" + safeValue(auditor.getName()) + ", auditorEmail=" + safeValue(auditor.getMail());
		persistActivity(SYSTEM_CRON_ACTOR, ActivityType.EDIT_ELECTION, description, "AUTO", auditor.getElection().getElectionId());
	}

	private void processAuditorRevisionDailyReminders(LocalDate today, Map<Long, Boolean> auditorRevisionReminderCalendarWindowByElection) {
		List<Auditor> auditors = ElectionsDaoFactory.createAuditorDao(em).getAuditorsWithEmail();
		for (Auditor auditor : auditors) {
			processReminderSafely("auditor revision reminder, auditorId=" + (auditor != null ? auditor.getAuditorId() : null), () -> processAuditorRevisionDailyReminder(auditor, today, auditorRevisionReminderCalendarWindowByElection));
		}
	}

	private void processAuditorRevisionDailyReminder(Auditor auditor, LocalDate today, Map<Long, Boolean> auditorRevisionReminderCalendarWindowByElection) {
		if (!isAutomaticAuditorRevisionReminderPending(auditor)) {
			return;
		}
		if (!isAuditorRevisionReminderStageEnabledForElection(auditor.getElection(), auditorRevisionReminderCalendarWindowByElection)) {
			return;
		}
		ReminderFrequency reminderFrequency = auditor.getReminderFrequency();
		if (!isReminderEnabledOnDate(reminderFrequency, today)) {
			return;
		}

		EJBFactory.getInstance().getMailsSendingEJB().queueAuditorRevisionReminderToAuditor(auditor.getAuditorId());
		String description = "SYSTEM_CRON ejecutó recordatorio diario de auditor para revisión de resultados y auditoría de votos. date=" + today + ", frequency=" + reminderFrequency.name() + ", auditorId=" + auditor.getAuditorId() + ", auditorName=" + safeValue(auditor.getName()) + ", auditorEmail=" + safeValue(auditor.getMail());
		persistActivity(SYSTEM_CRON_ACTOR, ActivityType.EDIT_ELECTION, description, "AUTO", auditor.getElection().getElectionId());
	}

	private boolean hasPendingActionsForCandidate(Candidate candidate) {
		if (candidate == null || candidate.getElection() == null || candidate.isAbstention()) {
			return false;
		}

		long totalTasks = ElectionsDaoFactory.createElectionTaskDao(em).countElectionTasks(candidate.getElection().getElectionId());
		if (totalTasks <= 0) {
			return false;
		}

		long doneTasks = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).countCompletedOrOmittedTasksByCandidate(candidate.getElection().getElectionId(), candidate.getCandidateId());
		return doneTasks < totalTasks;
	}

	private boolean hasOnlyEvaluationTaskPendingForCandidate(Candidate candidate) {
		if (candidate == null || candidate.getElection() == null) {
			return false;
		}

		long electionId = candidate.getElection().getElectionId();
		ElectionTask evaluationTask = ElectionsDaoFactory.createElectionTaskDao(em).getElectionTaskByKey(electionId, ElectionTaskKey.EVALUATION);
		if (evaluationTask == null) {
			return false;
		}

		long totalTasks = ElectionsDaoFactory.createElectionTaskDao(em).countElectionTasks(electionId);
		if (totalTasks <= 0) {
			return false;
		}

		long doneTasks = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).countCompletedOrOmittedTasksByCandidate(electionId, candidate.getCandidateId());
		long pendingTasks = totalTasks - doneTasks;
		if (pendingTasks != 1) {
			return false;
		}

		CandidateElectionTaskProgress evaluationTaskProgress = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).getByCandidateIdAndTaskKey(candidate.getCandidateId(), ElectionTaskKey.EVALUATION);
		if (evaluationTaskProgress == null) {
			return true;
		}
		CandidateElectionTaskStatus status = evaluationTaskProgress.getStatus();
		return status != CandidateElectionTaskStatus.COMPLETED && status != CandidateElectionTaskStatus.OMITTED;
	}

	private boolean hasPendingActionsForAuditor(Auditor auditor) {
		if (auditor == null || auditor.getElection() == null || !auditor.isCommissioner()) {
			return false;
		}

		long electionId = auditor.getElection().getElectionId();
		List<Candidate> candidates = ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(electionId);
		if (candidates == null || candidates.isEmpty()) {
			return false;
		}

		Map<Long, AuditorCandidateDecision> decisionsByCandidateId = new HashMap<>();
		List<AuditorCandidateDecision> decisionRows = ElectionsDaoFactory.createAuditorCandidateDecisionDao(em).getElectionAuditorCandidateDecisions(electionId);
		for (AuditorCandidateDecision decision : decisionRows) {
			if (decision == null || decision.getAuditor() == null || decision.getCandidate() == null) {
				continue;
			}
			if (decision.getAuditor().getAuditorId() != auditor.getAuditorId()) {
				continue;
			}
			decisionsByCandidateId.put(decision.getCandidate().getCandidateId(), decision);
		}

		for (Candidate candidate : candidates) {
			if (candidate == null || candidate.isAbstention()) {
				continue;
			}
			if (!AuditorCandidateDecisionUtils.isVisibleCandidateStatusForAuditor(candidate.getStatus())) {
				continue;
			}
			AuditorCandidateDecision decision = decisionsByCandidateId.get(candidate.getCandidateId());
			if (AuditorCandidateDecisionUtils.isCandidateActionRequired(candidate.getStatus(), decision)) {
				return true;
			}
		}
		return false;
	}

	boolean isAutomaticAuditorReminderEligible(Auditor auditor) {
		return auditor != null
				&& hasText(auditor.getMail())
				&& auditor.getElection() != null
				&& auditor.isCommissioner()
				&& !auditor.getElection().isClosed()
				&& auditor.getElection().isAuditorLinkAvailable();
	}

	boolean isAutomaticAuditorRevisionReminderPending(Auditor auditor) {
		return isAuditorRevisionReminderPending(auditor)
				&& isAutomaticAuditorReminderEligible(auditor)
				&& !auditor.getElection().isRevisionRequest();
	}

	boolean isAuditorRevisionReminderPending(Auditor auditor) {
		return auditor != null
				&& hasText(auditor.getMail())
				&& auditor.getElection() != null
				&& auditor.isCommissioner()
				&& !auditor.isAgreedConformity();
	}

	private void processPendingNominationDailyReminders(LocalDate today, Map<Long, Boolean> reminderCalendarWindowByElection) {
		List<Nomination> pendingNominations = ElectionsDaoFactory.createNominationDao(em).getPendingNominationsWithoutCandidate();

		for (Nomination nomination : pendingNominations) {
			processReminderSafely("pending nomination reminder, nominationId=" + (nomination != null ? nomination.getId() : null), () -> processPendingNominationDailyReminder(nomination, today, reminderCalendarWindowByElection));
		}
	}

	private void processPendingNominationDailyReminder(Nomination nomination, LocalDate today, Map<Long, Boolean> reminderCalendarWindowByElection) {
		if (nomination == null || nomination.getElection() == null || !hasText(nomination.getNominationEmail())) {
			return;
		}
		if (!isReminderStageEnabledForElection(nomination.getElection(), reminderCalendarWindowByElection)) {
			return;
		}

		EJBFactory.getInstance().getMailsSendingEJB().queueNominationReminderToNominee(nomination.getId());
		String description = "SYSTEM_CRON ejecutó recordatorio diario de nominación pendiente. date=" + today + ACTIVITY_NOMINATION_ID_FRAGMENT + nomination.getId() + ", nominationStatus=" + (nomination.getStatus() != null ? nomination.getStatus().name() : "NULL") + ", nominationName=" + safeValue(nomination.getNominationName()) + ACTIVITY_NOMINATION_EMAIL_FRAGMENT + safeValue(nomination.getNominationEmail());
		persistActivity(SYSTEM_CRON_ACTOR, ActivityType.EDIT_ELECTION, description, "AUTO", nomination.getElection().getElectionId());
	}

	private void processPendingSupportDailyReminders(LocalDate today, Map<Long, Boolean> reminderCalendarWindowByElection) {
		List<SupportNomination> pendingSupports = ElectionsDaoFactory.createSupportNominationDao(em).getPendingSupportNominations();

		for (SupportNomination supportNomination : pendingSupports) {
			processReminderSafely("pending support reminder, supportNominationId=" + (supportNomination != null ? supportNomination.getId() : null), () -> processPendingSupportDailyReminder(supportNomination, today, reminderCalendarWindowByElection));
		}
	}

	private void processPendingSupportDailyReminder(SupportNomination supportNomination, LocalDate today, Map<Long, Boolean> reminderCalendarWindowByElection) {
		if (supportNomination == null || supportNomination.getNomination() == null || supportNomination.getNomination().getElection() == null) {
			return;
		}
		if (!isReminderStageEnabledForElection(supportNomination.getNomination().getElection(), reminderCalendarWindowByElection)) {
			return;
		}

		long supportNominationId = supportNomination.getId();
		processReminderSafely("supporting contact reminder, supportNominationId=" + supportNominationId, () -> EJBFactory.getInstance().getMailsSendingEJB().queueSupportReminderToSupportingContact(supportNominationId));
		processReminderSafely("support requester reminder, supportNominationId=" + supportNominationId, () -> EJBFactory.getInstance().getMailsSendingEJB().queueSupportReminderToRequester(supportNominationId));
		String description = "SYSTEM_CRON ejecutó recordatorio diario de solicitud de apoyo pendiente (contacto de apoyo + solicitante). date=" + today + ", supportNominationId=" + supportNominationId + ", supportStatus=" + (supportNomination.getSupportStatus() != null ? supportNomination.getSupportStatus().name() : "NULL") + ", supportingContactName=" + safeValue(supportNomination.getSupportingContactName()) + ", supportingContactEmail=" + safeValue(supportNomination.getSupportingContactEmail());
		persistActivity(SYSTEM_CRON_ACTOR, ActivityType.EDIT_ELECTION, description, "AUTO", supportNomination.getNomination().getElection().getElectionId());
	}

	private boolean isReminderStageEnabledForElection(Election election, Map<Long, Boolean> reminderCalendarWindowByElection) {
		return isCalendarStageEnabledForElection(election, REMINDER_CRON_ALLOWED_CALENDAR_KEYS, reminderCalendarWindowByElection);
	}

	private boolean isAuditorCandidateReminderStageEnabledForElection(Election election, Map<Long, Boolean> auditorCandidateReminderCalendarWindowByElection) {
		return isCalendarStageEnabledForElection(election, ReminderCalendarKeys.AUDITOR_CANDIDATE_REMINDER_CRON_ALLOWED_CALENDAR_KEYS, auditorCandidateReminderCalendarWindowByElection);
	}

	private boolean isAuditorRevisionReminderStageEnabledForElection(Election election, Map<Long, Boolean> auditorRevisionReminderCalendarWindowByElection) {
		return isCalendarStageEnabledForElection(election, ReminderCalendarKeys.AUDITOR_REVISION_REMINDER_CRON_ALLOWED_CALENDAR_KEYS, auditorRevisionReminderCalendarWindowByElection);
	}

	private boolean isCandidateReminderEvaluationExtensionStageEnabledForElection(Election election, Map<Long, Boolean> candidateReminderEvaluationExtensionCalendarWindowByElection) {
		return isCalendarStageEnabledForElection(election, CANDIDATE_REMINDER_EVALUATION_EXTENSION_CALENDAR_KEYS, candidateReminderEvaluationExtensionCalendarWindowByElection);
	}

	private boolean isCalendarStageEnabledForElection(Election election, Set<ElectionCalendarKey> calendarKeys, Map<Long, Boolean> calendarWindowByElection) {
		if (election == null) {
			return false;
		}
		if (calendarKeys == null || calendarKeys.isEmpty()) {
			return true;
		}

		Long electionId = election.getElectionId();
		Boolean enabled = calendarWindowByElection.get(electionId);
		if (enabled != null) {
			return enabled.booleanValue();
		}

		Date now = new Date();
		long matchingWindows = ElectionsDaoFactory.createElectionCalendarDao(em).countActiveCalendarsByKeys(electionId, calendarKeys, now);
		boolean result = matchingWindows > 0L;
		calendarWindowByElection.put(electionId, result);
		return result;
	}

	private boolean isReminderEnabledOnDate(ReminderFrequency reminderFrequency, LocalDate date) {
		ReminderFrequency resolvedReminderFrequency = reminderFrequency == null ? ReminderFrequency.defaultValue() : reminderFrequency;
		return resolvedReminderFrequency.isEnabledOn(date);
	}

	void processReminderSafely(String scope, Runnable reminderProcess) {
		if (reminderProcess == null) {
			return;
		}
		try {
			reminderProcess.run();
		} catch (Exception e) {
			appLogger.error("Error processing automatic reminder. scope={}", scope, e);
		}
	}

	@Override
	public List<SupportNomination> getElectionSupportNominations(long electionId) {
		return ElectionsDaoFactory.createSupportNominationDao(em).getElectionSupportNominations(electionId);
	}

	@Override
	public List<SupportNomination> getElectionSupportNominationsForPublicElectionPage(long electionId) {
		return ElectionsDaoFactory.createSupportNominationDao(em).getElectionSupportNominationsForPublicElectionPage(electionId);
	}

	@Override
	public List<SupportNomination> getElectionSupportNominationsByEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter) {
		String safeEmail = normalizeEmailText(normalizedEmail, 320);
		if (electionId <= 0 || !hasText(safeEmail)) {
			return Collections.emptyList();
		}
		int safeMaxResults = maxResults > 0 ? maxResults : 10;
		String safeCountryCodeFilter = normalizeCountryCodeFilter(countryCodeFilter);
		return ElectionsDaoFactory.createSupportNominationDao(em).getElectionSupportNominationsByNormalizedContactEmail(
				electionId,
				safeEmail,
				safeMaxResults,
				safeCountryCodeFilter);
	}

	@Override
	public List<AuditorCandidateDecision> getElectionAuditorCandidateDecisions(long electionId) {
		return ElectionsDaoFactory.createAuditorCandidateDecisionDao(em).getElectionAuditorCandidateDecisions(electionId);
	}

	@Override
	public boolean updateAuditorCandidateDecisionStatus(long auditorId, long candidateId, AuditorCandidateDecisionStatus status, String actor, String ip, String comment) {
		try {
			if (status == null) {
				return false;
			}

			Auditor auditor = em.find(Auditor.class, auditorId);
			Candidate candidate = em.find(Candidate.class, candidateId);
			if (!isValidAuditorCandidateDecisionContext(auditor, candidate)) {
				return false;
			}

			CandidateStatus candidateStatus = candidate.getStatus();
			AuditorCandidateDecisionStage stage = resolveAuditorDecisionStageForCandidateStatus(candidateStatus);
			if (stage == null || !isAllowedAuditorDecisionStatusForStage(stage, status)) {
				return false;
			}
			if (!isPublicAuditorCandidateDecisionAvailable(auditor)) {
				return false;
			}

			return persistAuditorCandidateDecisionStatus(auditor, candidate, stage, status, actor, ip, comment, true, true);
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean updateAuditorCandidateDecisionStageStatus(long auditorId, long candidateId, AuditorCandidateDecisionStage stage, AuditorCandidateDecisionStatus status, String actor, String ip, String comment) {
		try {
			if (stage == null || status == null || !isAllowedAuditorDecisionStatusForStage(stage, status)) {
				return false;
			}

			Auditor auditor = em.find(Auditor.class, auditorId);
			Candidate candidate = em.find(Candidate.class, candidateId);
			if (!isValidAuditorCandidateDecisionContext(auditor, candidate)) {
				return false;
			}

			return persistAuditorCandidateDecisionStatus(auditor, candidate, stage, status, actor, ip, comment, false, false);
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	private boolean isValidAuditorCandidateDecisionContext(Auditor auditor, Candidate candidate) {
		return auditor != null
				&& candidate != null
				&& auditor.getElection() != null
				&& candidate.getElection() != null
				&& auditor.getElection().getElectionId() == candidate.getElection().getElectionId();
	}

	private boolean isPublicAuditorCandidateDecisionAvailable(Auditor auditor) {
		if (auditor == null || !auditor.isCommissioner() || auditor.getElection() == null) {
			return false;
		}
		Election election = auditor.getElection();
		if (election.isClosed() || !election.isAuditorLinkAvailable()) {
			return false;
		}
		long activeWindows = ElectionsDaoFactory.createElectionCalendarDao(em).countActiveCalendarsByKeys(
				election.getElectionId(),
				AuditorCandidateDecisionWindow.ALLOWED_CALENDAR_KEYS,
				new Date());
		return activeWindows > 0L;
	}

	private AuditorCandidateDecisionStage resolveAuditorDecisionStageForCandidateStatus(CandidateStatus candidateStatus) {
		if (candidateStatus == CandidateStatus.PRECOMPLETE) {
			return AuditorCandidateDecisionStage.PRE_VERIFICATION;
		}
		if (candidateStatus == CandidateStatus.COMPLETE) {
			return AuditorCandidateDecisionStage.FINAL_VERIFICATION;
		}
		return null;
	}

	private boolean isAllowedAuditorDecisionStatusForStage(AuditorCandidateDecisionStage stage, AuditorCandidateDecisionStatus status) {
		if (stage == AuditorCandidateDecisionStage.PRE_VERIFICATION) {
			return status == AuditorCandidateDecisionStatus.PREAPPROVED
					|| status == AuditorCandidateDecisionStatus.REJECTED;
		}
		if (stage == AuditorCandidateDecisionStage.FINAL_VERIFICATION) {
			return status == AuditorCandidateDecisionStatus.APPROVED
					|| status == AuditorCandidateDecisionStatus.REJECTED;
		}
		return false;
	}

	private boolean persistAuditorCandidateDecisionStatus(
			Auditor auditor,
			Candidate candidate,
			AuditorCandidateDecisionStage stage,
			AuditorCandidateDecisionStatus status,
			String actor,
			String ip,
			String comment,
			boolean createIfMissing,
			boolean notifyElectionSender) {
		if (auditor == null || candidate == null || stage == null || status == null) {
			return false;
		}

		AuditorCandidateDecision decision = ElectionsDaoFactory.createAuditorCandidateDecisionDao(em).getByAuditorAndCandidate(auditor.getAuditorId(), candidate.getCandidateId());
		boolean newDecision = false;
		if (decision == null) {
			if (!createIfMissing) {
				return false;
			}
			decision = new AuditorCandidateDecision();
			decision.setAuditor(auditor);
			decision.setCandidate(candidate);
			newDecision = true;
		}

		String normalizedComment = normalizeAuditorDecisionComment(comment);
		Date now = new Date();
		backfillAuditorDecisionStageData(decision);
		AuditorCandidateDecisionStatus previousStatus = getAuditorDecisionStageStatus(decision, stage);
		String previousComment = getAuditorDecisionStageComment(decision, stage);
		setAuditorDecisionStageData(decision, stage, status, normalizedComment, now);
		setAuditorDecisionMilestoneDates(decision, stage, now);
		synchronizeAuditorDecisionLegacyStatus(decision, now);

		if (newDecision) {
			em.persist(decision);
		} else {
			em.merge(decision);
		}

		if (notifyElectionSender) {
			if (status == AuditorCandidateDecisionStatus.APPROVED) {
				EJBFactory.getInstance().getMailsSendingEJB().queueAuditorCandidateApprovedToElectionSender(auditor.getAuditorId(), candidate.getCandidateId(), normalizedComment);
			} else if (status == AuditorCandidateDecisionStatus.REJECTED) {
				EJBFactory.getInstance().getMailsSendingEJB().queueAuditorCandidateRejectedToElectionSender(auditor.getAuditorId(), candidate.getCandidateId(), normalizedComment);
			}
		}

		String safeActor = actor != null && !actor.trim().isEmpty() ? actor.toUpperCase() : SYSTEM_ACTOR;
		String description = buildAuditorCandidateDecisionActivityDescription(safeActor, auditor, candidate, stage, previousStatus, status, previousComment, normalizedComment);
		persistActivity(safeActor, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
		appLogger.info("Auditor candidate decision updated. electionId={}, auditorId={}, candidateId={}, stage={}, previousStatus={}, newStatus={}, actor={}",
				candidate.getElection().getElectionId(),
				auditor.getAuditorId(),
				candidate.getCandidateId(),
				stage.name(),
				statusName(previousStatus),
				status.name(),
				safeActor);
		return true;
	}

	private AuditorCandidateDecisionStatus getAuditorDecisionStageStatus(AuditorCandidateDecision decision, AuditorCandidateDecisionStage stage) {
		if (decision == null || stage == null) {
			return null;
		}
		if (stage == AuditorCandidateDecisionStage.PRE_VERIFICATION) {
			return decision.getPreDecisionStatus();
		}
		if (stage == AuditorCandidateDecisionStage.FINAL_VERIFICATION) {
			return decision.getFinalDecisionStatus();
		}
		return null;
	}

	private String getAuditorDecisionStageComment(AuditorCandidateDecision decision, AuditorCandidateDecisionStage stage) {
		if (decision == null || stage == null) {
			return null;
		}
		if (stage == AuditorCandidateDecisionStage.PRE_VERIFICATION) {
			return decision.getPreDecisionComment();
		}
		if (stage == AuditorCandidateDecisionStage.FINAL_VERIFICATION) {
			return decision.getFinalDecisionComment();
		}
		return null;
	}

	private String buildAuditorCandidateDecisionActivityDescription(
			String actor,
			Auditor auditor,
			Candidate candidate,
			AuditorCandidateDecisionStage stage,
			AuditorCandidateDecisionStatus previousStatus,
			AuditorCandidateDecisionStatus newStatus,
			String previousComment,
			String newComment) {
		StringBuilder description = new StringBuilder(512);
		description.append(actor)
				.append(" actualizó decisión de auditoría de candidato")
				.append(TEXT_ELECCION)
				.append(candidate != null && candidate.getElection() != null ? safeValue(candidate.getElection().getTitleSpanish()) : "-")
				.append(". auditorId=")
				.append(auditor != null ? auditor.getAuditorId() : "-")
				.append(", auditorNombre=")
				.append(safeValue(auditor != null ? auditor.getName() : null))
				.append(ACTIVITY_CANDIDATE_ID_FRAGMENT)
				.append(candidate != null ? candidate.getCandidateId() : "-")
				.append(", candidateName=")
				.append(safeValue(candidate != null ? candidate.getName() : null))
				.append(", etapa=")
				.append(stage != null ? stage.name() : "-")
				.append(", estadoAnterior=")
				.append(statusName(previousStatus))
				.append(", estadoNuevo=")
				.append(statusName(newStatus));
		if (!normalizedText(previousComment).equals(normalizedText(newComment))) {
			description.append(", comentarioAnterior=").append(safeValue(previousComment));
			description.append(", comentarioNuevo=").append(safeValue(newComment));
		}
		return description.toString();
	}

	private String statusName(AuditorCandidateDecisionStatus status) {
		return status != null ? status.name() : "-";
	}

	private String normalizedText(String value) {
		return value != null ? value.trim() : "";
	}

	private void backfillAuditorDecisionStageData(AuditorCandidateDecision decision) {
		if (decision == null) {
			return;
		}
		if (decision.getPreDecisionStatus() == null) {
			AuditorCandidateDecisionStatus preStatus = resolvePersistedPreDecisionStatus(decision);
			if (preStatus != null) {
				decision.setPreDecisionStatus(preStatus);
				decision.setPreDecisionDate(firstNonNullDate(decision.getPreDecisionDate(), decision.getPreapprovedDate(), decision.getDecisionDate()));
			}
		}
		if (decision.getFinalDecisionStatus() == null) {
			AuditorCandidateDecisionStatus finalStatus = resolvePersistedFinalDecisionStatus(decision);
			if (finalStatus != null) {
				decision.setFinalDecisionStatus(finalStatus);
				decision.setFinalDecisionDate(firstNonNullDate(decision.getFinalDecisionDate(), decision.getApprovedDate(), decision.getDecisionDate()));
			}
		}
	}

	private AuditorCandidateDecisionStatus resolvePersistedPreDecisionStatus(AuditorCandidateDecision decision) {
		if (decision == null) {
			return null;
		}
		if (decision.getPreDecisionStatus() != null) {
			return decision.getPreDecisionStatus();
		}
		AuditorCandidateDecisionStatus legacyStatus = decision.getDecisionStatus();
		if (legacyStatus == AuditorCandidateDecisionStatus.PREAPPROVED) {
			return AuditorCandidateDecisionStatus.PREAPPROVED;
		}
		if (legacyStatus == AuditorCandidateDecisionStatus.REJECTED) {
			if (decision.getApprovedDate() == null || isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate())) {
				return AuditorCandidateDecisionStatus.REJECTED;
			}
			if (decision.getPreapprovedDate() != null) {
				return AuditorCandidateDecisionStatus.PREAPPROVED;
			}
		}
		if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED && decision.getPreapprovedDate() != null) {
			return AuditorCandidateDecisionStatus.PREAPPROVED;
		}
		return null;
	}

	private AuditorCandidateDecisionStatus resolvePersistedFinalDecisionStatus(AuditorCandidateDecision decision) {
		if (decision == null) {
			return null;
		}
		if (decision.getFinalDecisionStatus() != null) {
			return decision.getFinalDecisionStatus();
		}
		AuditorCandidateDecisionStatus legacyStatus = decision.getDecisionStatus();
		if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED) {
			return AuditorCandidateDecisionStatus.APPROVED;
		}
		if (legacyStatus == AuditorCandidateDecisionStatus.REJECTED
				&& decision.getApprovedDate() != null
				&& !isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate())) {
			return AuditorCandidateDecisionStatus.REJECTED;
		}
		return null;
	}

	private void setAuditorDecisionStageData(
			AuditorCandidateDecision decision,
			AuditorCandidateDecisionStage stage,
			AuditorCandidateDecisionStatus status,
			String comment,
			Date now) {
		if (decision == null || stage == null || status == null || now == null) {
			return;
		}
		if (stage == AuditorCandidateDecisionStage.PRE_VERIFICATION) {
			decision.setPreDecisionStatus(status);
			decision.setPreDecisionDate(now);
			decision.setPreDecisionComment(comment);
			return;
		}
		if (stage == AuditorCandidateDecisionStage.FINAL_VERIFICATION) {
			decision.setFinalDecisionStatus(status);
			decision.setFinalDecisionDate(now);
			decision.setFinalDecisionComment(comment);
		}
	}

	private String normalizeAuditorDecisionComment(String comment) {
		if (!hasText(comment)) {
			return null;
		}
		String trimmed = HtmlSanitizerUtils.sanitizeStrict(comment).trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		return trimmed.length() > 4000 ? trimmed.substring(0, 4000) : trimmed;
	}

	private void setAuditorDecisionMilestoneDates(
			AuditorCandidateDecision decision,
			AuditorCandidateDecisionStage stage,
			Date now) {
		if (decision == null || stage == null || now == null) {
			return;
		}
		if (stage == AuditorCandidateDecisionStage.PRE_VERIFICATION && decision.getPreapprovedDate() == null) {
			decision.setPreapprovedDate(now);
		}
		if (stage == AuditorCandidateDecisionStage.FINAL_VERIFICATION && decision.getApprovedDate() == null) {
			decision.setApprovedDate(now);
		}
	}

	private void synchronizeAuditorDecisionLegacyStatus(AuditorCandidateDecision decision, Date now) {
		if (decision == null) {
			return;
		}
		if (decision.getFinalDecisionStatus() != null) {
			decision.setDecisionStatus(decision.getFinalDecisionStatus());
			decision.setDecisionDate(firstNonNullDate(decision.getFinalDecisionDate(), decision.getDecisionDate(), now));
			return;
		}
		if (decision.getPreDecisionStatus() != null) {
			decision.setDecisionStatus(decision.getPreDecisionStatus());
			decision.setDecisionDate(firstNonNullDate(decision.getPreDecisionDate(), decision.getDecisionDate(), now));
		}
	}

	private boolean isLikelyLegacyPrecompleteRejectedMilestone(Date preapprovedDate, Date approvedDate) {
		if (preapprovedDate == null || approvedDate == null) {
			return false;
		}
		long diffMillis = Math.abs(preapprovedDate.getTime() - approvedDate.getTime());
		return diffMillis <= 1000L;
	}

	private Date firstNonNullDate(Date... dates) {
		if (dates == null) {
			return null;
		}
		for (Date date : dates) {
			if (date != null) {
				return date;
			}
		}
		return null;
	}

	private CandidateQuestionOwner resolveCurrentOwner(CandidateQuestionStatus status) {
		CandidateQuestionStatus resolvedStatus = status != null ? status : CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC;
		return resolvedStatus.resolveOwner();
	}

	private String normalizeShortText(String value, int maxLength) {
		if (!hasText(value)) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.length() <= maxLength) {
			return trimmed;
		}
		return trimmed.substring(0, maxLength);
	}

	private String normalizeLongText(String value) {
		if (!hasText(value)) {
			return null;
		}
		return value.trim();
	}

	private String normalizeQuestionText(String value) {
		return normalizeShortText(value, 500);
	}

	private String normalizeEmailText(String value, int maxLength) {
		String normalized = normalizeShortText(value, maxLength);
		return normalized != null ? normalized.toLowerCase(java.util.Locale.ROOT) : null;
	}

	private String normalizeCountryCodeFilter(String value) {
		String normalized = normalizeCountryForStorage(value);
		if (!hasText(normalized)) {
			return null;
		}
		return normalized.length() <= 8 ? normalized : normalized.substring(0, 8);
	}

	@Override
	public List<CandidateCountryLink> getElectionCandidateCountryLinks(long electionId) {
		return em.createQuery("SELECT l FROM CandidateCountryLink l WHERE l.candidate.election.electionId = :electionId ORDER BY l.id", CandidateCountryLink.class).setParameter(QueryParameterNames.ELECTION_ID, electionId).getResultList();
	}

	@Override
	public List<PublicElectionCandidateCountryLinkRow> getElectionCandidateCountryLinkRowsForPublicElectionPage(long electionId) {
		return ElectionsDaoFactory.createPublicElectionPageDao(em).getElectionCandidateCountryLinkRowsForPublicElectionPage(electionId);
	}

	@Override
	public List<CandidateWorkOrganization> getElectionCandidateWorkOrganizations(long electionId) {
		return em.createQuery("SELECT w FROM CandidateWorkOrganization w WHERE w.candidate.election.electionId = :electionId ORDER BY w.id", CandidateWorkOrganization.class).setParameter(QueryParameterNames.ELECTION_ID, electionId).getResultList();
	}

	@Override
	public List<PublicElectionCandidateWorkOrganizationRow> getElectionCandidateWorkOrganizationRowsForPublicElectionPage(long electionId) {
		return ElectionsDaoFactory.createPublicElectionPageDao(em).getElectionCandidateWorkOrganizationRowsForPublicElectionPage(electionId);
	}

	private void setPrivateFieldValue(Object target, String fieldName, Object value) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception e) {
			throw new IllegalStateException("No se pudo setear " + fieldName + " en " + target.getClass().getSimpleName(), e);
		}
	}

	@Override
	public String getAuditReportURL() {
		return getParameter("AUDIT_REPORT_LINK").getValue();
	}

	@Override
	public boolean addOrganization(long electionId, Organization organization, String userAdminId, String ip) throws CensusValidationException {
		Election lockedElection = lockElectionForOrganizationsMutation(electionId);
		if (organization.getName() == null || organization.getName().isBlank()) {
			throw new CensusValidationException("organizationsManagementAddRequired", null, null);
		}
		String normalizedOrgId = normalizeOrgId(organization.getOrgId());
		if (normalizedOrgId.isEmpty()) {
			normalizedOrgId = normalizeOrgId(UUID.randomUUID().toString());
		}
		organization.setOrgId(normalizedOrgId);
		if (ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByElectionAndOrgId(electionId, organization.getOrgId()) != null) {
			throw new CensusValidationException("organizationsManagementAddDuplicateOrgId", null, organization.getOrgId());
		}
		if (organization.getDoNominationToken() == null || organization.getDoNominationToken().isBlank()) {
			organization.setDoNominationToken(StringUtils.createSecureToken());
		}
		organization.setCountry(normalizeCountryForStorage(organization.getCountry()));
		organization.setCnpj(normalizeOptionalText(organization.getCnpj()));
		organization.setAsn(normalizeAsnForStorage(organization.getAsn()));
		if (organization.getMemberValue() == null) {
			organization.setMember(true);
		}
		organization.setElection(lockedElection);
		em.persist(organization);
		String description = userAdminId.toUpperCase() + " agregó la organización " + organization.getOrgId() + " al listado de organizaciones";
		persistActivity(userAdminId, ActivityType.EDIT_ORGANIZATION, description, ip, electionId);
		return true;
	}

	@Override
	public List<Organization> getOrganizations(long electionId) {
		return ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
	}

	@Override
	public boolean queueOrganizationsDeleteFromExcel(String contentType, long electionId, byte[] content, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startOrganizationsProcessing(electionId);
		if (!queued) {
			appLogger.info("Organizations delete upload already in progress. electionId={}", electionId);
			return false;
		}
			try {
				getManagerProxy().processOrganizationsDeleteFromExcelAsync(contentType, electionId, content, userAdminId, ip);
				return true;
			} catch (Exception e) {
				ElectionsCaches.finishOrganizationsProcessing(electionId);
				ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
				appLogger.error("Unable to queue organizations delete async update. electionId={}", electionId, e);
				throw new IllegalStateException("Unable to queue organizations delete async update", e);
			}
		}

	@Override
	@Asynchronous
	@TransactionTimeout(9000)
	public void processOrganizationsDeleteFromExcelAsync(String contentType, long electionId, byte[] content, String userAdminId, String ip) {
		try {
			ElectionsCaches.markOrganizationsProcessingPreparing(electionId);
			getManagerProxy().removeOrganizationsFromExcel(contentType, electionId, content, userAdminId, ip);
			ElectionsCaches.clearOrganizationsProcessingError(electionId);
			appLogger.info("Organizations delete async persistence finished. electionId={}", electionId);
		} catch (CensusValidationException e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Async organizations delete failed with validation error. electionId={}, messageKey={}", electionId, e.getMessage(), e);
		} catch (Exception e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Async organizations delete from excel failed. electionId={}", electionId, e);
		} finally {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			appLogger.info("Organizations processing flag released (delete). electionId={}", electionId);
		}
	}

	@Override
	@TransactionTimeout(9000)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public OrganizationBulkImportResult removeOrganizationsFromExcel(String contentType, long electionId, byte[] content, String userAdminId, String ip) throws CensusValidationException {
		OrganizationDebtorImportResult parsed = ExcelUtils.processOrganizationsDebtorsExcel(contentType, content);
		OrganizationBulkImportResult result = new OrganizationBulkImportResult();
		result.setProcessedRows(parsed.getProcessedRows());
		int deleted = 0;
		List<String> errors = new ArrayList<>();
		List<Organization> organizations = ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
		java.util.HashMap<String, Organization> organizationsByOrgId = new java.util.HashMap<>();
		for (Organization organization : organizations) {
			organizationsByOrgId.put(normalizeOrgId(organization.getOrgId()), organization);
		}

		List<String> missingOrgIds = new ArrayList<>();
		for (String orgId : parsed.getOrgIds()) {
			if (!organizationsByOrgId.containsKey(normalizeOrgId(orgId))) {
				missingOrgIds.add(orgId);
			}
		}
		if (!missingOrgIds.isEmpty()) {
			throw new CensusValidationException("organizationsManagementMissingOrgIds", null, String.join(", ", missingOrgIds));
		}

		List<Organization> organizationsToDelete = new ArrayList<>();
		for (String orgId : parsed.getOrgIds()) {
			Organization organization = organizationsByOrgId.get(normalizeOrgId(orgId));
			organizationsToDelete.add(organization);
		}
		validateOrganizationsCanBeDeleted(organizationsToDelete, true);
		int totalRows = organizationsToDelete.size();
		int processedRows = 0;
		ElectionsCaches.updateOrganizationsProcessingProgress(electionId, 0, totalRows, 0, 0, 0, 0, 0, totalRows);

		for (Organization organization : organizationsToDelete) {
			em.remove(organization);
			deleted++;
			processedRows++;
			ElectionsCaches.updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, 0, 0, deleted, 0, 0, totalRows);
		}
		result.setDeletedRows(deleted);
		result.setErrors(errors);
		String description = userAdminId.toUpperCase() + " eliminó organizaciones por Excel. Procesadas: " + result.getProcessedRows() + ", eliminadas: " + deleted + ", errores: " + errors.size();
		persistActivity(userAdminId, ActivityType.DELETE_ORGS, description, ip, electionId);
		return result;
	}

	@Override
	public boolean queueOrganizationsUpsertFromExcel(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip) {
		boolean queued = ElectionsCaches.startOrganizationsProcessing(electionId);
		if (!queued) {
			appLogger.info("Organizations upsert upload already in progress. electionId={}", electionId);
			return false;
		}
			try {
				getManagerProxy().processOrganizationsUpsertFromExcelAsync(contentType, electionId, content, overwriteAll, userAdminId, ip);
				return true;
			} catch (Exception e) {
				ElectionsCaches.finishOrganizationsProcessing(electionId);
				ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
				appLogger.error("Unable to queue organizations upsert async update. electionId={}", electionId, e);
				throw new IllegalStateException("Unable to queue organizations upsert async update", e);
			}
		}

	@Override
	@Asynchronous
	@TransactionTimeout(9000)
	public void processOrganizationsUpsertFromExcelAsync(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip) {
		try {
			ElectionsCaches.markOrganizationsProcessingPreparing(electionId);
			getManagerProxy().upsertOrganizationsFromExcel(contentType, electionId, content, overwriteAll, userAdminId, ip);
			ElectionsCaches.clearOrganizationsProcessingError(electionId);
			appLogger.info("Organizations upsert async persistence finished. electionId={}", electionId);
		} catch (CensusValidationException e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Async organizations upsert failed with validation error. electionId={}, messageKey={}", electionId, e.getMessage(), e);
		} catch (Exception e) {
			ElectionsCaches.setOrganizationsProcessingError(electionId, buildAsyncError(KEY_ORGANIZATIONS_ASYNC_PROCESSING_ERROR, e));
			appLogger.error("Async organizations upsert from excel failed. electionId={}", electionId, e);
		} finally {
			ElectionsCaches.finishOrganizationsProcessing(electionId);
			appLogger.info("Organizations processing flag released (upsert). electionId={}", electionId);
		}
	}

	private AsyncProcessingError buildAsyncError(String defaultMessageKey, CensusValidationException exception) {
		if (exception == null) {
			return new AsyncProcessingError(defaultMessageKey, null, null, null);
		}
		String key = exception.getMessage();
		if (key == null || key.trim().isEmpty()) {
			key = defaultMessageKey;
		}
		return new AsyncProcessingError(key, exception.getErrorRow(), exception.getErrorInfo(), buildAsyncTechnicalDetail(exception));
	}

	private AsyncProcessingError buildAsyncError(String defaultMessageKey, Exception exception) {
		return new AsyncProcessingError(defaultMessageKey, null, null, buildAsyncTechnicalDetail(exception));
	}

	private String buildAsyncTechnicalDetail(Throwable throwable) {
		if (throwable == null) {
			return null;
		}
		Throwable rootCause = throwable;
		while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
			rootCause = rootCause.getCause();
		}
		String message = rootCause.getMessage();
		if (message == null || message.trim().isEmpty()) {
			message = throwable.getClass().getSimpleName();
		}
		String detail = rootCause.getClass().getSimpleName() + ": " + message.trim();
		int maxLength = 500;
		return detail.length() <= maxLength ? detail : detail.substring(0, maxLength);
	}

	@Override
	@TransactionTimeout(9000)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public OrganizationBulkImportResult upsertOrganizationsFromExcel(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip) throws CensusValidationException {
		OrganizationsUpsertExcelData parsed = ExcelUtils.processOrganizationsUpsertExcel(contentType, content);
		OrganizationBulkImportResult result = new OrganizationBulkImportResult();
		result.setProcessedRows(parsed.getProcessedRows());
		if (!parsed.getErrors().isEmpty()) {
			result.setErrors(parsed.getErrors());
			result.setErrorReport(ExcelUtils.generateSingleColumnErrorReportXlsx("errores_alta_actualizacion_organizaciones", "error", parsed.getErrors()));
			return result;
		}
		Election electionReference = lockElectionForOrganizationsMutation(electionId);

		List<Organization> existingOrganizations = ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
		java.util.HashMap<String, Organization> existingByOrgId = new java.util.HashMap<>();
		for (Organization existingOrganization : existingOrganizations) {
			existingByOrgId.put(normalizeOrgId(existingOrganization.getOrgId()), existingOrganization);
		}

		Set<String> importedOrgIds = new HashSet<>();
		for (Organization incoming : parsed.getOrganizations()) {
			importedOrgIds.add(normalizeOrgId(incoming.getOrgId()));
		}
		List<Organization> organizationsToDeleteOnOverwrite = new ArrayList<>();
			if (overwriteAll) {
				for (Organization existingOrganization : existingOrganizations) {
					if (!importedOrgIds.contains(normalizeOrgId(existingOrganization.getOrgId()))) {
						organizationsToDeleteOnOverwrite.add(existingOrganization);
					}
				}
				validateOrganizationsCanBeDeleted(organizationsToDeleteOnOverwrite, true);
			}
			Set<String> knownOrgIds = new HashSet<>(existingByOrgId.keySet());
			int totalCreatedRows = 0;
			int totalUpdatedRows = 0;
			for (Organization incoming : parsed.getOrganizations()) {
				String normalizedIncomingOrgId = normalizeOrgId(incoming.getOrgId());
				if (knownOrgIds.contains(normalizedIncomingOrgId)) {
					totalUpdatedRows++;
				} else {
					totalCreatedRows++;
					knownOrgIds.add(normalizedIncomingOrgId);
				}
			}
			int totalDeletedRows = organizationsToDeleteOnOverwrite.size();
			int totalRows = parsed.getOrganizations().size() + organizationsToDeleteOnOverwrite.size();
			int processedRows = 0;
			ElectionsCaches.updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, 0, 0, 0, totalCreatedRows, totalUpdatedRows, totalDeletedRows);

		int created = 0;
		int updated = 0;
		for (Organization incoming : parsed.getOrganizations()) {
			String normalizedIncomingOrgId = normalizeOrgId(incoming.getOrgId());
			Organization existing = existingByOrgId.get(normalizedIncomingOrgId);
				if (existing == null) {
					incoming.setCountry(normalizeCountryForStorage(incoming.getCountry()));
					incoming.setDoNominationToken(StringUtils.createSecureToken());
					if (incoming.getMemberValue() == null) {
						incoming.setMember(true);
					}
					incoming.setElection(electionReference);
					em.persist(incoming);
						existingByOrgId.put(normalizedIncomingOrgId, incoming);
						created++;
					processedRows++;
					ElectionsCaches.updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, created, updated, 0, totalCreatedRows, totalUpdatedRows, totalDeletedRows);
					continue;
				}
			existing.setName(incoming.getName());
			existing.setVotes(incoming.getVotes());
			existing.setCategory(incoming.getCategory());
			existing.setCountry(normalizeCountryForStorage(incoming.getCountry()));
			existing.setCnpj(incoming.getCnpj());
			existing.setAsn(incoming.getAsn());
			existing.setMembershipContactId(incoming.getMembershipContactId());
			existing.setMembershipContactName(incoming.getMembershipContactName());
			existing.setMembershipContactEmail(incoming.getMembershipContactEmail());
			existing.setMembershipContactLanguage(incoming.getMembershipContactLanguage());
			if (incoming.getMemberValue() != null) {
				existing.setMember(incoming.isMember());
				}
				updated++;
				processedRows++;
				ElectionsCaches.updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, created, updated, 0, totalCreatedRows, totalUpdatedRows, totalDeletedRows);
			}
			int deleted = 0;
			if (overwriteAll) {
				for (Organization existingOrganization : organizationsToDeleteOnOverwrite) {
					em.remove(existingOrganization);
					deleted++;
					processedRows++;
					ElectionsCaches.updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, created, updated, deleted, totalCreatedRows, totalUpdatedRows, totalDeletedRows);
				}
			}
		result.setCreatedRows(created);
		result.setUpdatedRows(updated);
		result.setDeletedRows(deleted);
		String description = userAdminId.toUpperCase() + " cargó organizaciones por Excel. Procesadas: " + result.getProcessedRows() + ", creadas: " + created + ", actualizadas: " + updated + ", eliminadas: " + deleted
				+ ", overwriteAll: " + overwriteAll;
		persistActivity(userAdminId, ActivityType.EDIT_ORGS, description, ip, electionId);
		return result;
	}

	@Override
	public void validateOrganizationsDebtorsExcel(String contentType, byte[] content) throws CensusValidationException {
		ExcelUtils.processOrganizationsDebtorsExcel(contentType, content);
	}

	@Override
	public void validateOrganizationsDebtorsCanBeApplied(String contentType, long electionId, byte[] content) throws CensusValidationException {
		OrganizationDebtorImportResult parsed = ExcelUtils.processOrganizationsDebtorsExcel(contentType, content);
		List<Organization> organizations = ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
		java.util.HashMap<String, Organization> organizationsByOrgId = indexOrganizationsByOrgId(organizations);
		validateOrganizationsDebtorsOrgIdsExist(parsed.getOrgIds(), organizationsByOrgId);
	}

	@Override
	public void validateOrganizationsDeleteExcel(String contentType, byte[] content) throws CensusValidationException {
		ExcelUtils.processOrganizationsDeleteExcel(contentType, content);
	}

	@Override
	public List<String> validateOrganizationsUpsertExcel(String contentType, byte[] content) throws CensusValidationException {
		return validateOrganizationsUpsertExcelDetailed(contentType, content).getErrors();
	}

	@Override
	public OrganizationBulkImportResult validateOrganizationsUpsertExcelDetailed(String contentType, byte[] content) throws CensusValidationException {
		OrganizationsUpsertExcelData parsed = ExcelUtils.processOrganizationsUpsertExcel(contentType, content);
		OrganizationBulkImportResult result = new OrganizationBulkImportResult();
		result.setProcessedRows(parsed.getProcessedRows());
		if (!parsed.getErrors().isEmpty()) {
			result.setErrors(parsed.getErrors());
			result.setErrorReport(ExcelUtils.generateSingleColumnErrorReportXlsx("errores_alta_actualizacion_organizaciones", "error", parsed.getErrors()));
		}
		return result;
	}

	@Override
	public void validateOrganizationCanBeRemoved(long electionId, String orgId) throws CensusValidationException {
		Organization organization = ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByElectionAndOrgId(electionId, orgId);
		if (organization == null) {
			throw new CensusValidationException("organizationsManagementOrgNotFound", null, orgId);
		}
		List<Organization> singleOrganization = new ArrayList<>();
		singleOrganization.add(organization);
		validateOrganizationsCanBeDeleted(singleOrganization, false);
	}

	@Override
	public void validateOrganizationsDeleteCanBeApplied(String contentType, long electionId, byte[] content) throws CensusValidationException {
		long startedAt = System.currentTimeMillis();
		OrganizationDebtorImportResult parsed = ExcelUtils.processOrganizationsDebtorsExcel(contentType, content);
		List<Organization> organizations = ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
		java.util.HashMap<String, Organization> organizationsByOrgId = new java.util.HashMap<>();
		for (Organization organization : organizations) {
			organizationsByOrgId.put(normalizeOrgId(organization.getOrgId()), organization);
		}

		List<String> missingOrgIds = new ArrayList<>();
		List<Organization> organizationsToDelete = new ArrayList<>();
		for (String orgId : parsed.getOrgIds()) {
			Organization organization = organizationsByOrgId.get(normalizeOrgId(orgId));
			if (organization == null) {
				missingOrgIds.add(orgId);
				continue;
			}
			organizationsToDelete.add(organization);
		}
		if (!missingOrgIds.isEmpty()) {
			throw new CensusValidationException("organizationsManagementMissingOrgIds", null, String.join(", ", missingOrgIds));
		}
		validateOrganizationsCanBeDeleted(organizationsToDelete, true);
		appLogger.info("Organizations delete validation completed. electionId={}, parsedRows={}, existingRows={}, toDeleteRows={}, durationMs={}",
				electionId, parsed.getProcessedRows(), organizations.size(), organizationsToDelete.size(), System.currentTimeMillis() - startedAt);
	}

	@Override
	public void validateOrganizationsUpsertOverwriteCanBeApplied(String contentType, long electionId, byte[] content) throws CensusValidationException {
		long startedAt = System.currentTimeMillis();
		OrganizationsUpsertExcelData parsed = ExcelUtils.processOrganizationsUpsertExcel(contentType, content);
		long parseDurationMs = System.currentTimeMillis() - startedAt;
		if (parsed.getErrors().isEmpty()) {
			Set<String> importedOrgIds = new HashSet<>();
			for (Organization incoming : parsed.getOrganizations()) {
				importedOrgIds.add(normalizeOrgId(incoming.getOrgId()));
			}

			List<Organization> existingOrganizations = ElectionsDaoFactory.createOrganizationDao(em).getAllByElectionId(electionId);
			List<Organization> organizationsToDelete = new ArrayList<>();
			for (Organization existingOrganization : existingOrganizations) {
				if (!importedOrgIds.contains(normalizeOrgId(existingOrganization.getOrgId()))) {
					organizationsToDelete.add(existingOrganization);
				}
			}
			validateOrganizationsCanBeDeleted(organizationsToDelete, true);
			appLogger.info("Organizations upsert overwrite validation completed. electionId={}, parseDurationMs={}, importedRows={}, existingRows={}, toDeleteRows={}, totalDurationMs={}",
					electionId, parseDurationMs, parsed.getProcessedRows(), existingOrganizations.size(), organizationsToDelete.size(), System.currentTimeMillis() - startedAt);
		} else {
			appLogger.info("Organizations upsert overwrite validation skipped due to file errors. electionId={}, parseDurationMs={}, errorsCount={}, totalDurationMs={}",
					electionId, parseDurationMs, parsed.getErrors().size(), System.currentTimeMillis() - startedAt);
		}
	}

	@Override
	@TransactionTimeout(7200)
	public void validateElectionCensusUpsertCanBeApplied(String contentType, long electionId, byte[] content, boolean regenerateVoteLinks) throws CensusValidationException {
		List<UserVoter> importedUserVoters = ExcelUtils.processCensusExcel(contentType, content);
		List<UserVoterLite> existingUserVoters = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVotersLite(electionId);
		Set<String> importedOrgIds = prepareImportedCensusOrgIds(importedUserVoters);
		List<UserVoterLite> userVotersToDelete = resolveCensusVotersToDeleteForSnapshotLite(existingUserVoters, importedOrgIds);
		validateCensusVotersCanBeDeletedLite(userVotersToDelete);
	}

	@Override
	@TransactionTimeout(7200)
	public void validateElectionCensusOverwriteCanBeApplied(String contentType, long electionId, byte[] content) throws CensusValidationException {
		validateElectionCensusUpsertCanBeApplied(contentType, electionId, content, true);
	}

	@Override
	public void validateUserVoterCanBeAdded(long electionId, UserVoter userVoter) throws CensusValidationException {
		if (userVoter != null) {
			userVoter.setMail(normalizeCensusMailForStorage(userVoter.getMail()));
			userVoter.setOrgID(normalizeCensusOrgId(userVoter.getOrgID()));
			validateCensusIdentityUniqueness(electionId, userVoter);
		}
	}

	@Override
	public void validateUserVoterCanBeEdited(UserVoter userVoter) throws CensusValidationException {
		if (userVoter != null && userVoter.getElection() != null) {
			userVoter.setMail(normalizeCensusMailForStorage(userVoter.getMail()));
			userVoter.setOrgID(normalizeCensusOrgId(userVoter.getOrgID()));
			validateCensusIdentityUniqueness(userVoter.getElection().getElectionId(), userVoter);
		}
	}

	@Override
	public void validateUserVoterCanBeRemoved(long electionId, long userVoterId) throws CensusValidationException {
		UserVoter userVoter = ElectionsDaoFactory.createUserVoterDao(em).getUserVoter(userVoterId);
		if (userVoter == null || userVoter.getElection() == null || userVoter.getElection().getElectionId() != electionId) {
			throw new CensusValidationException("censusManagementUserNotFound", null, null);
		}
		if (userVoter.isVoted()) {
			throw new CensusValidationException("censusManagementDeleteBlockedByVotes", null, buildUserVoterIdentifier(userVoter));
		}
	}

	private void validateElectionCensusDeleteAllAllowed(List<UserVoter> userVoters) throws CensusValidationException {
		if (userVoters == null || userVoters.isEmpty()) {
			return;
		}
		for (UserVoter userVoter : userVoters) {
			if (userVoter != null && userVoter.isVoted()) {
				throw new CensusValidationException("censusManagementAdvancedDeleteBlockedByVotes", null, null);
			}
		}
	}

	private void validateElectionOrganizationsDeleteAllAllowed(long electionId) throws CensusValidationException {
		List<Nomination> nominations = getElectionNominations(electionId);
		if (nominations != null && !nominations.isEmpty()) {
			throw new CensusValidationException("organizationsManagementAdvancedDeleteBlockedByNominations", null, null);
		}
	}

	private static String normalizeOrgId(String orgId) {
		if (orgId == null) {
			return "";
		}
		return orgId.trim().toUpperCase(Locale.ROOT);
	}

	private Election lockElectionForOrganizationsMutation(long electionId) {
		Election election = em.find(Election.class, electionId, LockModeType.PESSIMISTIC_WRITE);
		if (election == null) {
			throw new IllegalStateException("Election not found. electionId=" + electionId);
		}
		return election;
	}

	private static String buildUserVoterIdentifier(UserVoter userVoter) {
		String mail = userVoter.getMail() == null ? "" : userVoter.getMail().trim();
		String orgId = userVoter.getOrgID() == null ? "" : userVoter.getOrgID().trim();
		if (!mail.isEmpty() && !orgId.isEmpty()) {
			return mail + " [" + orgId + "]";
		}
		if (!mail.isEmpty()) {
			return mail;
		}
		if (!orgId.isEmpty()) {
			return orgId;
		}
		return "userVoterId=" + userVoter.getUserVoterId();
	}

	private Set<String> prepareImportedCensusOrgIds(List<UserVoter> importedUserVoters) throws CensusValidationException {
		Set<String> importedOrgIds = new HashSet<>();
		for (int i = 0; i < importedUserVoters.size(); i++) {
			UserVoter importedUserVoter = importedUserVoters.get(i);
			String normalizedOrgId = normalizeCensusOrgId(importedUserVoter.getOrgID());
			int rowNumber = i + 2;
			if (normalizedOrgId == null) {
				throw new CensusValidationException("censusManagementUploadOrgIdRequired", rowNumber, null);
			}
			if (!importedOrgIds.add(normalizedOrgId)) {
				throw new CensusValidationException("censusManagementUploadDuplicateOrgId", rowNumber, importedUserVoter.getOrgID());
			}
		}
		return importedOrgIds;
	}

	private void validateCensusIdentityUniqueness(long electionId, UserVoter userVoter) throws CensusValidationException {
		String normalizedOrgId = normalizeCensusOrgId(userVoter.getOrgID());
		List<UserVoter> existingUserVoters = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionId);
		long sameUserId = userVoter.getUserVoterId();
		if (normalizedOrgId == null) {
			throw new CensusValidationException("censusManagementOrgIdRequired", null, null);
		}
		for (UserVoter existingUserVoter : existingUserVoters) {
			if (existingUserVoter != null) {
				boolean sameUser = sameUserId > 0 && existingUserVoter.getUserVoterId() == sameUserId;
				if (!sameUser) {
					if (normalizedOrgId.equals(normalizeCensusOrgId(existingUserVoter.getOrgID()))) {
						throw new CensusValidationException("censusManagementDuplicateOrgId", null, null);
					}
				}
			}
		}
	}

	private boolean sameJointElectionIdentity(UserVoter leftUserVoter, UserVoter rightUserVoter) {
		if (leftUserVoter == null || rightUserVoter == null) {
			return false;
		}
		String leftOrgId = leftUserVoter.getOrgID();
		String rightOrgId = rightUserVoter.getOrgID();
		return hasText(leftOrgId) && hasText(rightOrgId) && leftOrgId.equalsIgnoreCase(rightOrgId);
	}

	private List<UserVoter> getUniqueJointElectionRecipients(List<UserVoter> userVoters) {
		List<UserVoter> uniqueUserVoters = new ArrayList<>();
		if (userVoters == null || userVoters.isEmpty()) {
			return uniqueUserVoters;
		}
		uniqueUserVoters.add(userVoters.get(0));
		for (UserVoter userVoter : userVoters) {
			boolean exists = false;
			for (UserVoter uniqueUserVoter : uniqueUserVoters) {
				if (sameJointElectionIdentity(uniqueUserVoter, userVoter)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				uniqueUserVoters.add(userVoter);
			}
		}
		return uniqueUserVoters;
	}

	private List<UserVoterLite> resolveCensusVotersToDeleteForSnapshotLite(List<UserVoterLite> existingUserVoters, Set<String> importedOrgIds) {
		List<UserVoterLite> userVotersToDelete = new ArrayList<>();
		Set<String> seenExistingOrgIds = new HashSet<>();
		for (UserVoterLite existingUserVoter : existingUserVoters) {
			String normalizedOrgId = normalizeCensusOrgId(existingUserVoter.getOrgID());
			boolean missingOrgId = normalizedOrgId == null;
			boolean notInImportedSnapshot = !missingOrgId && !importedOrgIds.contains(normalizedOrgId);
			boolean duplicatedOrgId = !missingOrgId && !seenExistingOrgIds.add(normalizedOrgId);
			if (missingOrgId || notInImportedSnapshot || duplicatedOrgId) {
				userVotersToDelete.add(existingUserVoter);
			}
		}
		return userVotersToDelete;
	}

	private static String normalizeCensusEmail(String value) {
		if (value == null) {
			return "";
		}
		return value.trim().toUpperCase(Locale.ROOT);
	}

	private static String normalizeCensusMailForStorage(String value) {
		if (value == null) {
			return null;
		}
		return value.trim();
	}

	private static String normalizeCensusOrgId(String value) {
		String normalized = normalizeOptionalText(value);
		if (normalized == null) {
			return null;
		}
		return normalized.toUpperCase(Locale.ROOT);
	}

	private void validateCensusVotersCanBeDeletedLite(List<UserVoterLite> userVoters) throws CensusValidationException {
		if (userVoters != null && !userVoters.isEmpty()) {
			List<String> withVotes = new ArrayList<>();
			for (UserVoterLite userVoter : userVoters) {
				if (userVoter != null && userVoter.isVoted()) {
					withVotes.add(buildUserVoterIdentifierLite(userVoter));
				}
			}
			if (!withVotes.isEmpty()) {
				throw new CensusValidationException("censusManagementOverwriteBlockedByVotes", null, String.join(", ", withVotes));
			}
		}
	}

	private static String buildUserVoterIdentifierLite(UserVoterLite userVoter) {
		String mail = userVoter.getMail() == null ? "" : userVoter.getMail().trim();
		String orgId = userVoter.getOrgID() == null ? "" : userVoter.getOrgID().trim();
		if (!mail.isEmpty() && !orgId.isEmpty()) {
			return mail + " [" + orgId + "]";
		}
		if (!mail.isEmpty()) {
			return mail;
		}
		if (!orgId.isEmpty()) {
			return orgId;
		}
		return "userVoterId=" + userVoter.getUserVoterId();
	}

	private static String normalizeOptionalText(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	private static String normalizeCountryForStorage(String countryCode) {
		String normalized = normalizeOptionalText(countryCode);
		if (normalized == null) {
			return null;
		}
		return normalized.toUpperCase(Locale.ROOT);
	}

	private static String normalizeAsnForStorage(String asn) {
		return normalizeOptionalText(asn);
	}

	private void validateOrganizationsCanBeDeleted(List<Organization> organizations, boolean bulkOperation) throws CensusValidationException {
		if (organizations != null && !organizations.isEmpty()) {
			long startedAt = System.currentTimeMillis();
			Map<Long, Set<Long>> organizationIdsByElectionId = new HashMap<>();
			for (Organization organization : organizations) {
				if (organization != null && organization.getId() > 0 && organization.getElection() != null) {
					long electionId = organization.getElection().getElectionId();
					organizationIdsByElectionId.computeIfAbsent(electionId, key -> new HashSet<>()).add(organization.getId());
				}
			}

			Set<Long> organizationsWithNominations = new HashSet<>();
			Set<Long> organizationsWithSupports = new HashSet<>();
			for (Map.Entry<Long, Set<Long>> entry : organizationIdsByElectionId.entrySet()) {
				long electionId = entry.getKey();
				Set<Long> organizationIdsForElection = entry.getValue();
				organizationsWithNominations.addAll(filterOrganizationIds(getOrganizationIdsWithNominationsByElection(electionId), organizationIdsForElection));
				organizationsWithSupports.addAll(filterOrganizationIds(getOrganizationIdsWithSupportsByElection(electionId), organizationIdsForElection));
			}

			List<String> withNominations = new ArrayList<>();
			List<String> withSupports = new ArrayList<>();
			for (Organization organization : organizations) {
				if (organization == null || organization.getId() <= 0) {
					continue;
				}
				if (organizationsWithNominations.contains(organization.getId())) {
					withNominations.add(organization.getOrgId());
				}
				if (organizationsWithSupports.contains(organization.getId())) {
					withSupports.add(organization.getOrgId());
				}
			}

			if (!withNominations.isEmpty()) {
				String key = bulkOperation ? "organizationsManagementDeleteBlockedByNominations" : "organizationsManagementDeleteOrgHasNominations";
				throw new CensusValidationException(key, null, String.join(", ", withNominations));
			}
			if (!withSupports.isEmpty()) {
				String key = bulkOperation ? "organizationsManagementDeleteBlockedBySupports" : "organizationsManagementDeleteOrgHasSupports";
				throw new CensusValidationException(key, null, String.join(", ", withSupports));
			}
			appLogger.info("Organizations delete constraints validation completed. organizationsCount={}, electionsCount={}, withNominationsCount={}, withSupportsCount={}, durationMs={}",
					organizations.size(), organizationIdsByElectionId.size(), withNominations.size(), withSupports.size(), System.currentTimeMillis() - startedAt);
		}
	}

	private Set<Long> filterOrganizationIds(Set<Long> sourceIds, Set<Long> targetIds) {
		Set<Long> filtered = new HashSet<>();
		if (sourceIds == null || sourceIds.isEmpty() || targetIds == null || targetIds.isEmpty()) {
			return filtered;
		}
		for (Long sourceId : sourceIds) {
			if (sourceId != null && targetIds.contains(sourceId)) {
				filtered.add(sourceId);
			}
		}
		return filtered;
	}

	private Set<Long> getOrganizationIdsWithNominationsByElection(long electionId) {
		List<Long> referencedIds = em.createQuery("SELECT DISTINCT n.organization.id FROM Nomination n WHERE n.election.electionId = :electionId", Long.class).setParameter(QueryParameterNames.ELECTION_ID, electionId).getResultList();
		return new HashSet<>(referencedIds);
	}

	private Set<Long> getOrganizationIdsWithSupportsByElection(long electionId) {
		List<Long> referencedIds = em.createQuery("SELECT DISTINCT s.supportingOrganization.id FROM SupportNomination s WHERE s.nomination.election.electionId = :electionId", Long.class)
				.setParameter(QueryParameterNames.ELECTION_ID, electionId).getResultList();
		return new HashSet<>(referencedIds);
	}

	@Override
	public boolean renewOrganizationNominationLink(long electionId, String orgId, String userAdminId, String ip) throws CensusValidationException {
		Organization org = ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByElectionAndOrgId(electionId, orgId);
		if (org == null) {
			throw new CensusValidationException("organizationsManagementOrgNotFound", null, orgId);
		}
		org.setDoNominationToken(StringUtils.createSecureToken());
		em.merge(org);
		persistActivity(userAdminId, ActivityType.UPDATE_TOKEN_ORG, userAdminId.toUpperCase() + " renovó el link de nominación de la organización " + orgId, ip, electionId);
		return true;
	}

	@Override
	public boolean removeOrganization(long electionId, String orgId, String userAdminId, String ip) throws CensusValidationException {
		Organization org = ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByElectionAndOrgId(electionId, orgId);
		if (org == null) {
			throw new CensusValidationException("organizationsManagementOrgNotFound", null, orgId);
		}
		List<Organization> singleOrganization = new ArrayList<>();
		singleOrganization.add(org);
		validateOrganizationsCanBeDeleted(singleOrganization, false);
		em.remove(org);
		persistActivity(userAdminId, ActivityType.DELETE_ORGANIZATION, userAdminId.toUpperCase() + " eliminó la organización " + orgId, ip, electionId);
		return true;
	}

	@Override
	public String getCampusUrl() {
		try {
			return EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.CAMPUS_URL);
		} catch (Exception e) {
			appLogger.error("Error obtaining the Campus URL", e);
			return null;
		}
	}

	@Override
	public String getCampusToken() {
		try {
			return EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.CAMPUS_TOKEN);
		} catch (Exception e) {
			appLogger.error("Error obtaining the Campus token", e);
			return null;
		}
	}

	@Override
	public void verifyCampusCourseAccessForCandidates() {
		if (!CampusClient.isCampusIntegrationEnabled()) {
			return;
		}
		try {
			List<Election> elections = getElectionsAllOrderCreationDate();
			CandidateElectionTaskProgressDao candidateElectionTaskProgressDao = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em);
			ElectionCalendarDao electionCalendarDao = ElectionsDaoFactory.createElectionCalendarDao(em);
			Date now = new Date();

			for (Election election : elections) {
				if (election == null || election.isClosed() || !hasAnyCampusCourse(election)) {
					continue;
				}

				ElectionCalendar courseWindow = electionCalendarDao.getElectionCalendarByKey(election.getElectionId(), ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
				if (!isCalendarWindowActive(courseWindow, now)) {
					continue;
				}

				List<ElectionTask> electionTasks = getElectionTasks(election.getElectionId());
				ElectionTask courseTask = resolveTaskByKey(electionTasks, ElectionTaskKey.COURSE);
				if (courseTask == null) {
					continue;
				}

				Date startDate = courseWindow.getStartDate();

				List<Candidate> candidates = getElectionCandidatesOrdered(election.getElectionId());
				List<ElectionTaskKey> taskKeysToLoad = new ArrayList<>();
				taskKeysToLoad.add(ElectionTaskKey.COURSE);
				List<CandidateElectionTaskProgress> taskProgressRows = candidateElectionTaskProgressDao.getByElectionIdAndTaskKeys(election.getElectionId(), taskKeysToLoad);
				Map<Long, Map<ElectionTaskKey, CandidateElectionTaskProgress>> progressByCandidateAndTask = indexTaskProgressByCandidateAndTask(taskProgressRows);

				for (Candidate candidate : candidates) {
					if (candidate == null || candidate.getMail() == null || candidate.getMail().trim().isEmpty()) {
						continue;
					}

					CandidateElectionTaskProgress courseProgress = getTaskProgress(progressByCandidateAndTask, candidate.getCandidateId(), ElectionTaskKey.COURSE);
					boolean shouldEvaluateCourseTask = courseProgress != null && CandidateElectionTaskStatus.COMPLETED != courseProgress.getStatus();
					if (!shouldEvaluateCourseTask) {
						continue;
					}

					Candidate updatedCandidate = CampusClient.updateCandidateCampusProgress(election, candidate, startDate);
					em.merge(updatedCandidate);

					if (shouldEvaluateCourseTask && CandidateCampusCourseStatus.COMPLETED == updatedCandidate.getCampusCourseStatus()) {
						markCandidateTaskAsCompleted(courseProgress);
					}
				}
			}
		} catch (Exception e) {
			appLogger.error("Error checking campus course access for candidates", e);
		}
	}

	@Override
	public void verifyCampusEvaluationGradesForCandidates() {
		if (!CampusClient.isCampusIntegrationEnabled()) {
			return;
		}
		try {
			List<Election> elections = getElectionsAllOrderCreationDate();
			CandidateElectionTaskProgressDao candidateElectionTaskProgressDao = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em);
			ElectionCalendarDao electionCalendarDao = ElectionsDaoFactory.createElectionCalendarDao(em);
			Date now = new Date();

			for (Election election : elections) {
				if (election == null || election.isClosed() || !hasAnyCampusCourse(election)) {
					continue;
				}

				ElectionCalendar evaluationWindow = electionCalendarDao.getElectionCalendarByKey(election.getElectionId(), ElectionCalendarKey.N_6_PERIODO_ADDITIONAL_EVALUATION);
				if (!isCalendarWindowActive(evaluationWindow, now)) {
					continue;
				}

				List<ElectionTask> electionTasks = getElectionTasks(election.getElectionId());
				ElectionTask evaluationTask = resolveTaskByKey(electionTasks, ElectionTaskKey.EVALUATION);
				if (evaluationTask == null) {
					continue;
				}

				List<Candidate> candidates = getElectionCandidatesOrdered(election.getElectionId());
				List<ElectionTaskKey> taskKeysToLoad = new ArrayList<>();
				taskKeysToLoad.add(ElectionTaskKey.EVALUATION);
				List<CandidateElectionTaskProgress> taskProgressRows = candidateElectionTaskProgressDao.getByElectionIdAndTaskKeys(election.getElectionId(), taskKeysToLoad);
				Map<Long, Map<ElectionTaskKey, CandidateElectionTaskProgress>> progressByCandidateAndTask = indexTaskProgressByCandidateAndTask(taskProgressRows);

				for (Candidate candidate : candidates) {
					if (candidate == null || candidate.getMail() == null || candidate.getMail().trim().isEmpty()) {
						continue;
					}

					CandidateElectionTaskProgress evaluationProgress = getTaskProgress(progressByCandidateAndTask, candidate.getCandidateId(), ElectionTaskKey.EVALUATION);
					boolean shouldEvaluateEvaluationTask = evaluationProgress != null && CandidateElectionTaskStatus.COMPLETED != evaluationProgress.getStatus();
					if (!shouldEvaluateEvaluationTask) {
						continue;
					}

					Candidate updatedCandidate = CampusClient.updateCandidateCampusCalification(election, candidate);
					boolean hasCampusCalification = hasText(updatedCandidate.getCampusCourseCalification());
					if (hasCampusCalification) {
						markCandidateEvaluationStatusAsCompleted(updatedCandidate);
					}
					em.merge(updatedCandidate);

					if (hasCampusCalification) {
						markCandidateTaskAsCompleted(evaluationProgress);
					}
				}
			}
		} catch (Exception e) {
			appLogger.error("Error checking campus evaluation grades for candidates", e);
		}
	}

	@Override
	public void verifyCampusCourseAccessForCandidatesRateLimited(String requesterKey, String candidateMail) {
		if (!CampusClient.isCampusIntegrationEnabled()) {
			return;
		}
		String buttonKey = "COURSE";
		int maxAttempts = resolveCampusProgressCheckMaxAttempts(buttonKey);
		int currentAttempt = registerCampusProgressCheckAttempt(requesterKey, buttonKey);
		boolean blocked = currentAttempt > maxAttempts;

		appLogger.info("Campus progress check attempt recorded. buttonKey={}, requesterKey={}, candidateMail={}, attemptCount={}, maxAttempts={}, blocked={}", normalizeButtonKey(buttonKey), normalizeRequesterKey(requesterKey), normalizeCandidateMail(candidateMail), currentAttempt, maxAttempts, blocked);
		if (blocked) {
			return;
		}

		verifyCampusCourseAccessForCandidates();
	}

	@Override
	public void verifyCampusEvaluationGradesForCandidatesRateLimited(String requesterKey, String candidateMail) {
		if (!CampusClient.isCampusIntegrationEnabled()) {
			return;
		}
		String buttonKey = "EVALUATION";
		int maxAttempts = resolveCampusProgressCheckMaxAttempts(buttonKey);
		int currentAttempt = registerCampusProgressCheckAttempt(requesterKey, buttonKey);
		boolean blocked = currentAttempt > maxAttempts;

		appLogger.info("Campus progress check attempt recorded. buttonKey={}, requesterKey={}, candidateMail={}, attemptCount={}, maxAttempts={}, blocked={}", normalizeButtonKey(buttonKey), normalizeRequesterKey(requesterKey), normalizeCandidateMail(candidateMail), currentAttempt, maxAttempts, blocked);
		if (blocked) {
			return;
		}

		verifyCampusEvaluationGradesForCandidates();
	}

	private boolean isCalendarWindowActive(ElectionCalendar calendar, Date now) {
		if (calendar == null || now == null || calendar.getStartDate() == null || calendar.getEndDate() == null) {
			return false;
		}
		return !now.before(calendar.getStartDate()) && !now.after(calendar.getEndDate());
	}

	private int registerCampusProgressCheckAttempt(String requesterKey, String buttonKey) {
		String trackingKey = CAMPUS_PROGRESS_CHECK_TRACKING_PREFIX + "|" + normalizeButtonKey(buttonKey) + "|" + normalizeRequesterKey(requesterKey);
		return ElectionsCaches.incrementCampusProgressCheckAttempt(trackingKey);
	}

	private int resolveCampusProgressCheckMaxAttempts(String buttonKey) {
		String specificParameterName = resolveCampusProgressCheckMaxAttemptsParameterName(buttonKey);
		return resolvePositiveIntParameterWithFallback(
				specificParameterName,
				Constants.CAMPUS_PROGRESS_CHECK_MAX_ATTEMPTS,
				DEFAULT_CAMPUS_PROGRESS_CHECK_MAX_ATTEMPTS,
				"campus progress max attempts");
	}

	private int resolvePositiveIntParameterWithFallback(
			String primaryParameterName,
			String secondaryParameterName,
			int defaultValue,
			String description) {
		Integer primaryValue = resolvePositiveIntParameter(primaryParameterName);
		if (primaryValue != null) {
			return primaryValue;
		}

		Integer secondaryValue = resolvePositiveIntParameter(secondaryParameterName);
		if (secondaryValue != null) {
			return secondaryValue;
		}

		appLogger.warn("Unable to resolve {} from parameters {} or {}, using default {}",
				description,
				primaryParameterName,
				secondaryParameterName,
				defaultValue);
		return defaultValue;
	}

	private Integer resolvePositiveIntParameter(String parameterName) {
		try {
			String value = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(parameterName);
			if (!hasText(value)) {
				return null;
			}

			int parsedValue = Integer.parseInt(value.trim());
			if (parsedValue > 0) {
				return parsedValue;
			}

			appLogger.warn("Parameter {} must be a positive integer. Received value: {}", parameterName, value);
			return null;
		} catch (NumberFormatException e) {
			appLogger.warn("Parameter {} is not a valid integer.", parameterName, e);
			return null;
		} catch (Exception e) {
			appLogger.warn("Error resolving parameter {}", parameterName, e);
			return null;
		}
	}

	private String resolveCampusProgressCheckMaxAttemptsParameterName(String buttonKey) {
		String normalizedButtonKey = normalizeButtonKey(buttonKey);
		if ("COURSE".equals(normalizedButtonKey)) {
			return Constants.CAMPUS_PROGRESS_CHECK_COURSE_MAX_ATTEMPTS;
		}
		if ("EVALUATION".equals(normalizedButtonKey)) {
			return Constants.CAMPUS_PROGRESS_CHECK_EVALUATION_MAX_ATTEMPTS;
		}
		return Constants.CAMPUS_PROGRESS_CHECK_MAX_ATTEMPTS;
	}

	private String normalizeRequesterKey(String requesterKey) {
		return hasText(requesterKey) ? requesterKey.trim().toLowerCase(Locale.ROOT) : "anonymous";
	}

	private String normalizeButtonKey(String buttonKey) {
		return hasText(buttonKey) ? buttonKey.trim().toUpperCase(Locale.ROOT) : "UNKNOWN";
	}

	private String normalizeCandidateMail(String candidateMail) {
		return hasText(candidateMail) ? candidateMail.trim().toLowerCase(Locale.ROOT) : "unknown";
	}

	private void markCandidateTaskAsCompleted(CandidateElectionTaskProgress taskProgress) {
		if (taskProgress == null || CandidateElectionTaskStatus.COMPLETED == taskProgress.getStatus()) {
			return;
		}

		Date now = new Date();
		taskProgress.setStatus(CandidateElectionTaskStatus.COMPLETED);
		if (taskProgress.getStartDate() == null) {
			taskProgress.setStartDate(now);
		}
		taskProgress.setEndDate(now);
		em.merge(taskProgress);
	}

	private void markCandidateEvaluationStatusAsCompleted(Candidate candidate) {
		if (candidate == null || CandidateEvaluationStatus.NOT_APPLICABLE == candidate.getEvaluationStatus()) {
			return;
		}
		candidate.setEvaluationStatus(CandidateEvaluationStatus.COMPLETED);
	}

	private Map<Long, Map<ElectionTaskKey, CandidateElectionTaskProgress>> indexTaskProgressByCandidateAndTask(List<CandidateElectionTaskProgress> taskProgressRows) {
		Map<Long, Map<ElectionTaskKey, CandidateElectionTaskProgress>> indexed = new HashMap<>();
		if (taskProgressRows == null || taskProgressRows.isEmpty()) {
			return indexed;
		}

		for (CandidateElectionTaskProgress taskProgress : taskProgressRows) {
			if (taskProgress == null || taskProgress.getCandidate() == null || taskProgress.getElectionTask() == null || taskProgress.getElectionTask().getTaskKey() == null) {
				continue;
			}

			long candidateId = taskProgress.getCandidate().getCandidateId();
			ElectionTaskKey taskKey = taskProgress.getElectionTask().getTaskKey();
			Map<ElectionTaskKey, CandidateElectionTaskProgress> candidateTasks = indexed.get(candidateId);
			if (candidateTasks == null) {
				candidateTasks = new HashMap<>();
				indexed.put(candidateId, candidateTasks);
			}
			candidateTasks.put(taskKey, taskProgress);
		}

		return indexed;
	}

	private CandidateElectionTaskProgress getTaskProgress(Map<Long, Map<ElectionTaskKey, CandidateElectionTaskProgress>> progressByCandidateAndTask, long candidateId, ElectionTaskKey taskKey) {
		if (progressByCandidateAndTask == null || progressByCandidateAndTask.isEmpty() || taskKey == null) {
			return null;
		}
		Map<ElectionTaskKey, CandidateElectionTaskProgress> candidateTasks = progressByCandidateAndTask.get(candidateId);
		if (candidateTasks == null || candidateTasks.isEmpty()) {
			return null;
		}
		return candidateTasks.get(taskKey);
	}

	private ElectionTask resolveTaskByKey(List<ElectionTask> electionTasks, ElectionTaskKey taskKey) {
		if (taskKey == null) {
			return null;
		}
		if (electionTasks == null || electionTasks.isEmpty()) {
			return null;
		}
		for (ElectionTask electionTask : electionTasks) {
			if (electionTask != null && taskKey == electionTask.getTaskKey()) {
				return electionTask;
			}
		}
		return null;
	}

}
