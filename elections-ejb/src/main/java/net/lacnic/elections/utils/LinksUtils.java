package net.lacnic.elections.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.domain.LanguageCode;

public class LinksUtils {

	private static final String TOKEN = "?token=";
	private static final String ACTION_CANDIDATE = "&action=candidate";
	private static final String ACTION_NOMINATE = "&action=nominate";
	private static final String CANDIDATE_ID = "?candidateId=";
	private static final String CANDIDATE_ID_SUFFIX = "&candidateId=";
	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

	private LinksUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String buildVoteLink(String token) {
		try {
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getTokenVoteURL(baseUrl) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildResultsLink(String token) {
		try {
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getTokenResultURL(baseUrl) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildAuditorResultsLink(String token) {
		try {
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getTokenAuditURL(baseUrl) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildAcceptNominationLink(String token) {
		try {
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getAcceptNominationURL(baseUrl) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildDoNominationLink(String token) {
		try {
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getDoNominationURL(baseUrl) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildSupportNominationLink(String token) {
		try {
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getSupportNominationURL(baseUrl) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildTokenVoteLink(String token) {
		try {
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getTokenVoteURL(baseUrl) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildTokenResultLink(String token) {
		try {
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getTokenResultURL(baseUrl) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildTokenAuditLink(String token) {
		try {
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getTokenAuditURL(baseUrl) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildTokenQuestionLink(String token) {
		try {
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getTokenQuestionURL(baseUrl) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildPublicCandidateNominationLink(String token) {
		try {
			if (token == null || token.trim().isEmpty()) {
				return "";
			}
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getTokenQuestionURL(baseUrl) + TOKEN + token + ACTION_NOMINATE;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildPublicCandidateProfileLink(String token, Long candidateId) {
		try {
			if (candidateId == null || candidateId.longValue() <= 0L || token == null || token.trim().isEmpty()) {
				return "";
			}
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getTokenQuestionURL(baseUrl) + TOKEN + token + ACTION_CANDIDATE + CANDIDATE_ID_SUFFIX + candidateId;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildPublicCandidatePhotoLink(Long electionId, Long candidateId) {
		try {
			if (electionId == null || electionId.longValue() <= 0L || candidateId == null || candidateId.longValue() <= 0L) {
				return "";
			}
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			return Constants.getPublicCandidatePhotoURL(baseUrl) + "?electionId=" + electionId + CANDIDATE_ID_SUFFIX + candidateId;
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

	public static String buildPublicCandidatePhotoLink(Long candidateId) {
		return buildPublicCandidatePhotoLink(null, candidateId);
	}

	public static String buildPublicElectionOfficialResultLetterLink(Long electionId, LanguageCode languageCode) {
		try {
			if (electionId == null || electionId.longValue() <= 0L) {
				return "";
			}
			String baseUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.URL);
			LanguageCode resolvedLanguageCode = languageCode != null ? languageCode : LanguageCode.SP;
			return Constants.getPublicElectionOfficialResultLetterURL(baseUrl)
					+ "?electionId=" + electionId
					+ "&lang=" + resolvedLanguageCode.getCode();
		} catch (Exception e1) {
			appLogger.error(e1.getMessage(), e1);
		}
		return "";
	}

}
