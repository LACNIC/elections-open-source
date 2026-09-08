package net.lacnic.elections.utils;

import net.lacnic.elections.domain.LanguageCode;

/**
 * This class is used to store system constants like template paths, services
 * URLs, etc.
 * 
 * @author LACNIC
 *
 */
public class Constants {

	// Configuration keys. WS authentication keys are loaded from elections.properties.
	public static final String APP = "APP";
	public static final String URL = "URL";
	public static final String EMAIL_HOST = "EMAIL_HOST";
	public static final String EMAIL_USER = "EMAIL_USER";
	public static final String EMAIL_PASSWORD = "EMAIL_PASSWORD";
	public static final String DEFAULT_SENDER = "DEFAULT_SENDER";
	public static final String DEFAULT_RECIPIENT = "DEFAULT_RECIPIENT";
	public static final String DEFAULT_SUPPORT_RECIPIENT = "DEFAULT_SUPPORT_RECIPIENT";
	public static final String WS_AUTH_METHOD = "WS_AUTH_METHOD";
	public static final String WS_LACNIC_AUTH_URL = "WS_LACNIC_AUTH_URL";
	public static final String WS_AUTH_TOKEN = "WS_AUTH_TOKEN";
	public static final String PORTAL_APIKEY = "PORTAL_APIKEY";
	public static final String WS_AUTHORIZED_IPS = "WS_AUTHORIZED_IPS";
	public static final String WS_MAX_PAGE_SIZE = "WS_MAX_PAGE_SIZE";
	public static final String MILACNIC_SYNC_ORGANIZATIONS_ENDPOINT_TEMPLATE = "MILACNIC_SYNC_ORGANIZATIONS_ENDPOINT_TEMPLATE";
	public static final String MILACNIC_SYNC_API_TOKEN = "MILACNIC_SYNC_API_TOKEN";
	public static final String MILACNIC_SYNC_MIN_ORGANIZATIONS_REQUIRED = "MILACNIC_SYNC_MIN_ORGANIZATIONS_REQUIRED";
	public static final String MILACNIC_SYNC_MAX_MEMBER_DEACTIVATIONS = "MILACNIC_SYNC_MAX_MEMBER_DEACTIVATIONS";
	public static final String MILACNIC_SYNC_MIN_DEBTOR_ORGANIZATIONS_REQUIRED = "MILACNIC_SYNC_MIN_DEBTOR_ORGANIZATIONS_REQUIRED";
	public static final String MILACNIC_SYNC_MAX_DEBTOR_ORGANIZATIONS_ALLOWED = "MILACNIC_SYNC_MAX_DEBTOR_ORGANIZATIONS_ALLOWED";
	public static final String MILACNIC_SYNC_MIN_BRAZIL_ORGANIZATIONS_REQUIRED = "MILACNIC_SYNC_MIN_BRAZIL_ORGANIZATIONS_REQUIRED";
	// Legacy threshold keys used by automatic census sync from local organizations.
	public static final String MILACNIC_SYNC_MIN_RECORDS = "MILACNIC_SYNC_MIN_RECORDS";
	public static final String MILACNIC_SYNC_MIN_MEMBER_ROWS = "MILACNIC_SYNC_MIN_MEMBER_ROWS";
	public static final String MILACNIC_SYNC_MIN_DEBTOR_ROWS = "MILACNIC_SYNC_MIN_DEBTOR_ROWS";
	public static final String MILACNIC_SYNC_MAX_DEBTOR_ROWS = "MILACNIC_SYNC_MAX_DEBTOR_ROWS";
	public static final String MILACNIC_SYNC_MIN_BRAZIL_ROWS = "MILACNIC_SYNC_MIN_BRAZIL_ROWS";
	public static final String WEBSITE_DEFAULT = "WEBSITE_DEFAULT";
	public static final String ACCEPT_NOMINATION_CONDITIONS_ES = "ACCEPT_NOMINATION_CONDITIONS_ES";
	public static final String ACCEPT_NOMINATION_CONDITIONS_EN = "ACCEPT_NOMINATION_CONDITIONS_EN";
	public static final String ACCEPT_NOMINATION_CONDITIONS_PT = "ACCEPT_NOMINATION_CONDITIONS_PT";
	public static final String DEFAULT_PHOTO = "DEFAULT_PHOTO";
	public static final String ABSTENTION_DEFAULT_TEXT = "ABSTENTION_DEFAULT_TEXT";
	public static final String DEFAULT_ABSTENTION_TEXT = "Usted puede optar por la abstención / You can choose to abstain / Você pode optar pela abstenção.";
	public static final String OPENAI_URL = "OPENAI_URL";
	public static final String OPENAI_API_KEY = "OPENAI_API_KEY";
	public static final String OPENAI_MODEL = "OPENAI_MODEL";
	public static final String AI_TEXT_IMPROVEMENT_ENABLED = "AI_TEXT_IMPROVEMENT_ENABLED";
	public static final String AI_TEXT_PROMPT_TRANSLATION = "AI_TEXT_PROMPT_TRANSLATION";
	public static final String AI_TEXT_IMPROVEMENT_MAX_DAILY_REQUESTS = "AI_TEXT_IMPROVEMENT_MAX_DAILY_REQUESTS";
	public static final String AI_TEXT_IMPROVEMENT_RATE_LIMIT_CACHE_RESET_HOURS = "AI_TEXT_IMPROVEMENT_RATE_LIMIT_CACHE_RESET_HOURS";
	public static final String PUBLIC_FAILED_ACCESS_MAX_ATTEMPTS = "PUBLIC_FAILED_ACCESS_MAX_ATTEMPTS";
	public static final String PUBLIC_FAILED_ACCESS_RATE_LIMIT_CACHE_RESET_HOURS = "PUBLIC_FAILED_ACCESS_RATE_LIMIT_CACHE_RESET_HOURS";
	public static final String PUBLIC_NOMINATION_ENABLED = "PUBLIC_NOMINATION_ENABLED";
	public static final String LOGIN_CAPTCHA_ENABLED = "LOGIN_CAPTCHA_ENABLED";
	public static final String LOGIN_CAPTCHA_MAX_ATTEMPTS = "LOGIN_CAPTCHA_MAX_ATTEMPTS";
	public static final String LOGIN_CAPTCHA_RATE_LIMIT_CACHE_RESET_HOURS = "LOGIN_CAPTCHA_RATE_LIMIT_CACHE_RESET_HOURS";
	public static final String PUBLIC_ELECTION_LEGACY_MAX_ELECTION_ID = "PUBLIC_ELECTION_LEGACY_MAX_ELECTION_ID";
	public static final String CAMPUS_URL = "CAMPUS_URL";
	public static final String CAMPUS_TOKEN = "CAMPUS_TOKEN";
	public static final String CAMPUS_COURSE_MIN_ID = "CAMPUS_COURSE_MIN_ID";
	public static final String CAMPUS_PROGRESS_CHECK_MAX_ATTEMPTS = "CAMPUS_PROGRESS_CHECK_MAX_ATTEMPTS";
	public static final String CAMPUS_PROGRESS_CHECK_WINDOW_HOURS = "CAMPUS_PROGRESS_CHECK_WINDOW_HOURS";
	public static final String CAMPUS_PROGRESS_CHECK_RATE_LIMIT_CACHE_RESET_HOURS = "CAMPUS_PROGRESS_CHECK_RATE_LIMIT_CACHE_RESET_HOURS";
	public static final String CAMPUS_PROGRESS_CHECK_COURSE_RATE_LIMIT_CACHE_RESET_HOURS = "CAMPUS_PROGRESS_CHECK_COURSE_RATE_LIMIT_CACHE_RESET_HOURS";
	public static final String CAMPUS_PROGRESS_CHECK_EVALUATION_RATE_LIMIT_CACHE_RESET_HOURS = "CAMPUS_PROGRESS_CHECK_EVALUATION_RATE_LIMIT_CACHE_RESET_HOURS";
	public static final String CAMPUS_PROGRESS_CHECK_COURSE_MAX_ATTEMPTS = "CAMPUS_PROGRESS_CHECK_COURSE_MAX_ATTEMPTS";
	public static final String CAMPUS_PROGRESS_CHECK_EVALUATION_MAX_ATTEMPTS = "CAMPUS_PROGRESS_CHECK_EVALUATION_MAX_ATTEMPTS";
	public static final String CAMPUS_PROGRESS_CHECK_COURSE_WINDOW_HOURS = "CAMPUS_PROGRESS_CHECK_COURSE_WINDOW_HOURS";
	public static final String CAMPUS_PROGRESS_CHECK_EVALUATION_WINDOW_HOURS = "CAMPUS_PROGRESS_CHECK_EVALUATION_WINDOW_HOURS";

	// Possible values for the 'WS_AUTH_METHOD' parameter
	public static final String WS_AUTH_TYPE_APP = "APP";
	public static final String WS_AUTH_TYPE_LACNIC = "LACNIC";

	// Used for Candidates order
	public static final int MIN_ORDER = 0;
	public static final int MAX_ORDER = 100000;
	public static final int ADMINISTRATIVE_SUPPORT_REJECTION_REASON_MAX_LENGTH = 4000;
	public static final int CANDIDATE_ORGANIZATION_SUPPORTS_REQUIRED = 2;
	public static final int CANDIDATE_USER_SUPPORTS_2_REQUIRED = 2;
	public static final int CANDIDATE_USER_SUPPORTS_5_REQUIRED = 5;

	// Used for EJB naming
	public static final String EJB_PREFIX = "ejb:/";
	public static final String JAR_NAME = "elections-ejb-1.0";

	public static final String SkGoogleApiReCaptcha = "SkGoogleApiReCaptcha";
	public static final String DataSiteKeyReCaptcha = "DataSiteKeyReCaptcha";

	private static final String WAR_NAME = "elections";
	private static final String VOTE = "/vote";
	private static final String RESULT = "/result";
	private static final String AUDIT = "/audit";
	private static final String NOMINATION_ACCEPT = "/token/nomination/tasks";
	private static final String ORGANIZATION_DO_NOMINATION = "/token/organization/do-nomination";
	private static final String NOMINATION_SUPPORT = "/token/nomination/support";
	private static final String TOKEN_VOTE = "/token/vote";
	private static final String TOKEN_RESULT = "/token/result";
	private static final String TOKEN_AUDIT = "/token/audit";
	private static final String TOKEN_QUESTION = "/token/public-election";
	private static final String PUBLIC_CANDIDATE_PHOTO = "/public/candidate/photo";
	private static final String PUBLIC_ELECTION_OFFICIAL_RESULT_LETTER = "/public/election/result-letter";

	public static final String TemplateTypeAUDITOR_AGREEMENT = "AUDITOR_AGREEMENT";
	public static final String TemplateTypeAUDITOR_REVISION = "AUDITOR_REVISION";
	public static final String TemplateTypeNEW = "NEW";
	public static final String TemplateTypeSIGNATURE = "SIGNATURE";
	public static final String TemplateTypeVOTE_RESULT = "VOTE_RESULT";
	public static final String TemplateTypeVOTE_CODES = "VOTE_CODES";
	public static final String api_elections = "api-Elections";
	public static final String api_electionsPublicInformation = "api-ElectionsPublicInformation";
	public static final String elections_manager = "elections-manager";
	public static final LanguageCode DEFAULT_EMAIL_LANGUAGE = LanguageCode.SP;
	public static final String EMAIL_CODE_SUMMARY_TOKEN = "$user.codesSummary";
	public static final String SYNC_STATUS_SUCCESS = "SUCCESS";
	public static final String SYNC_STATUS_NO_DATA = "NO_DATA";
	public static final String SYNC_STATUS_ERROR = "ERROR";
	public static final String SYNC_AUDIT_FIELD_ORGANIZATION = "ORGANIZATION";
	public static final String SYNC_AUDIT_FIELD_CENSUS = "CENSUS";
	public static final String CENSUS_SYNC_MESSAGE_PREFIX = "[AUTO_CENSUS_SYNC]";

	private Constants() {
		throw new IllegalStateException("Utility class");
	}

	private static String sanitizeBasePath(String path) {
		String safePath = String.valueOf(path);
		if (safePath.isEmpty()) {
			return "";
		}
		if (safePath.endsWith("/")) {
			return safePath;
		}
		return safePath + "/";
	}

	private static String composeUrl(String path, String segment) {
		return sanitizeBasePath(path) + WAR_NAME + segment;
	}

	public static String getVotesURL(String path) {
		return composeUrl(path, VOTE);
	}

	public static String getResultsURL(String path) {
		return composeUrl(path, RESULT);
	}

	public static String getResultsAuditURL(String path) {
		return composeUrl(path, AUDIT);
	}

	public static String getAcceptNominationURL(String path) {
		return composeUrl(path, NOMINATION_ACCEPT);
	}

	public static String getDoNominationURL(String path) {
		return composeUrl(path, ORGANIZATION_DO_NOMINATION);
	}

	public static String getSupportNominationURL(String path) {
		return composeUrl(path, NOMINATION_SUPPORT);
	}

	public static String getTokenVoteURL(String path) {
		return composeUrl(path, TOKEN_VOTE);
	}

	public static String getTokenResultURL(String path) {
		return composeUrl(path, TOKEN_RESULT);
	}

	public static String getTokenAuditURL(String path) {
		return composeUrl(path, TOKEN_AUDIT);
	}

	public static String getTokenQuestionURL(String path) {
		return composeUrl(path, TOKEN_QUESTION);
	}

	public static String getPublicCandidatePhotoURL(String path) {
		return composeUrl(path, PUBLIC_CANDIDATE_PHOTO);
	}

	public static String getPublicElectionOfficialResultLetterURL(String path) {
		return composeUrl(path, PUBLIC_ELECTION_OFFICIAL_RESULT_LETTER);
	}

}
