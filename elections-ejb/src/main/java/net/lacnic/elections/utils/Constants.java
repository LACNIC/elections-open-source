package net.lacnic.elections.utils;

import java.util.HashMap;


/**
 * This class is used to store system constants like template paths, services URLs, etc.
 * 
 * @author LACNIC
 *
 */
public class Constants {

	private static HashMap<String, String> parameters = new HashMap<>();

	// Parameter names for 'parameter' database table
	public static final String APP = "APP";
	public static final String URL = "URL";
	public static final String EMAIL_HOST = "EMAIL_HOST";
	public static final String EMAIL_USER = "EMAIL_USER";
	public static final String EMAIL_PASSWORD = "EMAIL_PASSWORD";
	public static final String DEFAULT_SENDER = "DEFAULT_SENDER";
	public static final String DEFAULT_RECIPIENT = "DEFAULT_RECIPIENT";
	public static final String WS_AUTH_METHOD = "WS_AUTH_METHOD";
	public static final String WS_LACNIC_AUTH_URL = "WS_LACNIC_AUTH_URL";
	public static final String WS_AUTH_TOKEN = "WS_AUTH_TOKEN";
	public static final String WS_AUTHORIZED_IPS = "WS_AUTHORIZED_IPS";
	public static final String WS_MAX_PAGE_SIZE = "WS_MAX_PAGE_SIZE";
	public static final String WEBSITE_DEFAULT = "WEBSITE_DEFAULT";

	// Possible values for the 'WS_AUTH_METHOD' parameter
	public static final String WS_AUTH_TYPE_APP = "APP";
	public static final String WS_AUTH_TYPE_LACNIC = "LACNIC";

	// Used for Candidates order
	public static final int MIN_ORDER = 0;
	public static final int MAX_ORDER = 100000;

	// Used for EJB naming
	public static final String EJB_PREFIX = "ejb:/";
	public static final String JAR_NAME = "elections-ejb";

	public static final String SkGoogleApiReCaptcha = "SkGoogleApiReCaptcha";
	public static final String DataSiteKeyReCaptcha = "DataSiteKeyReCaptcha";

	private static final String WAR_NAME = "elections";
	private static final String VOTE = "/vote";
	private static final String RESULT = "/result";
	private static final String AUDIT = "/audit";

	public static final String TemplateTypeAUDITOR = "AUDITOR";
	public static final String TemplateTypeAUDITOR_AGREEMENT = "AUDITOR_AGREEMENT";
	public static final String TemplateTypeAUDITOR_REVISION = "AUDITOR_REVISION";
	public static final String TemplateTypeELECTION_NOTICE = "ELECTION_NOTICE";
	public static final String TemplateTypeELECTION_START = "ELECTION_START";
	public static final String TemplateTypeELECTION_ABOUT_TO_END = "ELECTION_ABOUT_TO_END";
	public static final String TemplateTypeNEW = "NEW";
	public static final String TemplateTypeSIGNATURE = "SIGNATURE";
	public static final String TemplateTypeVOTE_RESULT = "VOTE_RESULT";	
	public static final String TemplateTypeVOTE_CODES = "VOTE_CODES";


	private Constants() {
		throw new IllegalStateException("Utility class");
	}

	public static String getVotesURL(String path) {
		return path + WAR_NAME + VOTE;
	}

	public static String getResultsURL(String path) {
		return path + WAR_NAME + RESULT;
	}

	public static String getResultsAuditURL(String path) {
		return path + WAR_NAME + AUDIT;
	}

	public static void clean() {
		cleanParametersCache();
	}

	public static void cleanParametersCache() {
		setParameters(new HashMap<String, String>());
	}

	public static HashMap<String, String> getParameters() {
		return parameters;
	}

	public static void setParameters(HashMap<String, String> parameters) {
		Constants.parameters = parameters;
	}

}
