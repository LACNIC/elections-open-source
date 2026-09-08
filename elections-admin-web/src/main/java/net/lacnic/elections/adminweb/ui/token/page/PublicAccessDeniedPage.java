package net.lacnic.elections.adminweb.ui.token.page;

import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.token.panel.PublicAccessDeniedPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

public class PublicAccessDeniedPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;
	public static final String PARAM_TARGET_KEY = "targetKey";
	public static final String PARAM_TARGET_DATE = "targetDate";
	public static final String PARAM_ERROR_CODE = "errorCode";
	public static final String PARAM_ERROR_TITLE = "errorTitle";
	public static final String PARAM_ERROR_MESSAGE = "errorMessage";
	public static final String PARAM_REFERENCE_URL = "referenceUrl";

	private Election election;

	public PublicAccessDeniedPage() {
		this(new PageParameters());
	}

	public PublicAccessDeniedPage(PageParameters params) {
		super(params);
		add(new PublicAccessDeniedPanel("contentPanel", election, getToken(), params));
	}

	@Override
	protected Class<? extends IRequestablePage> validateToken(PageParameters params) {
		if (!hasText(getToken())) {
			return Error404.class;
		}
		election = AppContext.getInstance().getManagerBeanRemote().getElectionByQuestionToken(getToken());
		if (election == null) {
			return Error404.class;
		}
		setElection(election);
		setHeaderElectionTitleFromElection(election);
		setHeaderUserDisplay(getString("publicCalendarCountdownHeaderUser"));
		setWhereAmI("Pantalla genérica de acceso denegado");
		setContextClass(Election.class.getName());
		setContextData("errorCode: " + params.get(PARAM_ERROR_CODE).toString("")
				+ "\ntargetKey: " + params.get(PARAM_TARGET_KEY).toString("")
				+ "\ntargetDate: " + params.get(PARAM_TARGET_DATE).toString(""));
		return null;
	}

	@Override
	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionAccessDenied");
	}

	public static PageParameters buildPageParameters(String electionToken, ErrorCode errorCode, CountdownTargetDate targetDate,
			net.lacnic.elections.domain.pre.ElectionCalendarKey targetKey, String errorTitle, String errorMessage, String referenceUrl) {
		PageParameters params = hasTextStatic(electionToken) ? UtilsParameters.getToken(electionToken) : new PageParameters();
		if (errorCode != null) {
			params.add(PARAM_ERROR_CODE, errorCode.name());
		}
		if (targetDate != null) {
			params.add(PARAM_TARGET_DATE, targetDate.name().toLowerCase(java.util.Locale.ROOT));
		}
		if (targetKey != null) {
			params.add(PARAM_TARGET_KEY, targetKey.name());
		}
		if (hasTextStatic(errorTitle)) {
			params.add(PARAM_ERROR_TITLE, errorTitle);
		}
		if (hasTextStatic(errorMessage)) {
			params.add(PARAM_ERROR_MESSAGE, errorMessage);
		}
		if (hasTextStatic(referenceUrl)) {
			params.add(PARAM_REFERENCE_URL, referenceUrl);
		}
		return params;
	}

	private static boolean hasTextStatic(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	public boolean shouldShowPreviewTools() {
		return shouldShowDebugPanel();
	}

	public enum CountdownTargetDate {
		START,
		END,
		POINT
	}

	public enum ErrorCode {
		ACCESS_NOT_AVAILABLE,
		VOTE_NOT_OPEN,
		VOTE_CLOSED,
		RESULTS_NOT_PUBLIC,
		AUDIT_NOT_PUBLIC,
		CANDIDATE_QUESTIONS_NOT_OPEN,
		CANDIDATE_QUESTIONS_CLOSED,
		PUBLIC_ELECTION_NOT_AVAILABLE
	}
}
