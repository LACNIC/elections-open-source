package net.lacnic.elections.adminweb.ui.bases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.ElectionsWebAdminSession;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.commons.AppTopNavPublicHomeOnlyPanel;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.error.Error429;
import net.lacnic.elections.adminweb.ui.token.page.PublicAccessDeniedPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.ReminderFrequency;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.Nomination;

public abstract class PublicTokenBasePage extends WebPage {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String DEBUG_PARAM = "debug";
	private static final String LOCALE_PARAM = "locale";
	private static final String DEBUG_REQUIRED_ROLE = "elections-manager";
	private static final String DEBUG_REDACTED_VALUE = "[oculto]";
	private static final Pattern DEBUG_URL_PATTERN = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
	private static final Pattern DEBUG_ROUTE_PATTERN = Pattern.compile("(?<!\\S)/(?:token|public)[^\\s]*", Pattern.CASE_INSENSITIVE);
	private static final Pattern DEBUG_SENSITIVE_LINE_PATTERN = Pattern.compile("^\\s*([^:\\n\\r]*(?:token|url|link)[^:\\n\\r]*)\\s*:\\s*(.*)$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern DEBUG_SECRET_VALUE_PATTERN = Pattern.compile("\\b[a-f0-9]{8}-[a-f0-9-]{20,}\\b", Pattern.CASE_INSENSITIVE);
	private static final MetaDataKey<Boolean> MANUAL_TOPBAR_LOCALE_SELECTION_KEY = new MetaDataKey<Boolean>() {
		private static final long serialVersionUID = 1L;
	};

	private String token;
	private String headerElectionTitle;
	private String headerUserDisplay;
	private String headerNotificationStatus;
	private Election election;
	private String whereAmI;
	private String contextClass;
	private String contextData;
	private WebMarkupContainer debugPanel;
	private List<ElectionCalendar> cachedPublicCalendars;

	public PublicTokenBasePage() {
		this(new PageParameters());
	}

	public PublicTokenBasePage(PageParameters params) {
		super(params);
		applyLocaleRequestParameter(params);
		setHeaderElectionTitle("-");
		setHeaderUserDisplay("Invitado");
		setHeaderNotificationStatus("Estado: activadas");
		setToken(UtilsParameters.getToken(params));
		Class<? extends IRequestablePage> classError;
		if (isPublicFailedAccessRateLimitEnabled() && isPublicFailedAccessRateLimited()) {
			classError = Error429.class;
		} else {
			classError = validateToken(params);
			if (isPublicFailedAccessRateLimitEnabled() && Error404.class.equals(classError)) {
				classError = registerPublicFailedAccessAttemptAndResolveError();
			}
		}
		if (classError != null) {
			throw new RestartResponseException(asPageClass(classError));
		}

		ensureTokenAccessWindow(params);

		initializeContextPanel();

		add(buildTopHeader("topHeader"));
		add(buildTopNavPublic("topNavPublic"));
		add(buildTopbarContent("belowTopbarContent"));
		add(buildGlobalFeedbackContainer("globalFeedbackContainer"));
	}

	protected abstract Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params);

	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		return null;
	}

	private void ensureTokenAccessWindow(PageParameters params) {
		redirectToAccessDeniedPageIfBlocked(resolveTokenAccessGate(params));
	}

	protected void redirectToAccessDeniedPageIfBlocked(TokenAccessGate gate) {
		TokenAccessGate.AccessBlock block = gate == null ? null : gate.evaluate(new Date());
		if (block != null) {
			redirectToAccessDeniedPage(block);
		}
	}

	private void redirectToAccessDeniedPage(TokenAccessGate.AccessBlock block) {
		PageParameters accessDeniedParams = PublicAccessDeniedPage.buildPageParameters(
			resolveAccessDeniedElectionToken(),
			block.getErrorCode(),
			block.getTargetDate(),
			block.getTargetKey(),
			block.getErrorTitle(),
			block.getErrorMessage(),
			resolveAccessDeniedReferenceUrl()
		);
		appendLocaleParameter(accessDeniedParams);
		appendDebugParameter(accessDeniedParams);
		throw new RestartResponseException(PublicAccessDeniedPage.class, accessDeniedParams);
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Page> asPageClass(Class<? extends IRequestablePage> pageClass) {
		return (Class<? extends Page>) pageClass;
	}

	protected String resolveAccessDeniedElectionToken() {
		Election currentElection = getElection();
		if (currentElection != null && hasText(currentElection.getPublicElectionToken())) {
			return currentElection.getPublicElectionToken();
		}
		return null;
	}

	protected boolean isPublicFailedAccessRateLimitEnabled() {
		return true;
	}

	protected boolean isPublicFailedAccessRateLimited() {
		try {
			return AppContext.getInstance().getVoterBeanRemote().isPublicFailedAccessRateLimited(getClientIP());
		} catch (Exception e) {
			appLogger.error("Error checking public failed access rate limit", e);
			return false;
		}
	}

	protected Class<? extends IRequestablePage> registerPublicFailedAccessAttemptAndResolveError() {
		try {
			boolean blocked = AppContext.getInstance().getVoterBeanRemote().registerPublicFailedAccessAttempt(getClientIP());
			return blocked ? Error429.class : Error404.class;
		} catch (Exception e) {
			appLogger.error("Error registering public failed access attempt", e);
			return Error404.class;
		}
	}

	protected String getClientIP() {
		return ElectionsWebAdminSession.getIPClient();
	}

	protected String resolveAccessDeniedReferenceUrl() {
		if (getRequest() == null || getRequest().getUrl() == null) {
			return null;
		}
		String value = getRequest().getUrl().toString();
		if (!hasText(value)) {
			return null;
		}
		int queryIndex = value.indexOf('?');
		String path = queryIndex >= 0 ? value.substring(0, queryIndex) : value;
		if (!hasText(path)) {
			return null;
		}
		return path.startsWith("/") ? path : "/" + path;
	}

	protected List<ElectionCalendar> getPublicCalendars() {
		if (cachedPublicCalendars == null) {
			cachedPublicCalendars = loadPublicCalendars();
		}
		return cachedPublicCalendars;
	}

	protected ElectionCalendar findPublicCalendar(ElectionCalendarKey key) {
		if (key == null) {
			return null;
		}
		for (ElectionCalendar calendar : getPublicCalendars()) {
			if (key.equals(calendar.getCalendarKey())) {
				return calendar;
			}
		}
		return null;
	}

	protected TokenAccessGate resolveNominationTasksAccessGate(Nomination nomination) {
		List<CandidateElectionTaskProgress> progressList = nomination != null && nomination.getCandidate() != null
				? nomination.getCandidate().getTaskProgress()
				: null;
		Date earliestStart = null;
		ElectionCalendarKey earliestKey = null;
		Date latestEnd = null;
		ElectionCalendarKey latestKey = null;
		ElectionCalendarKey fallbackKey = null;

		if (progressList != null) {
			for (CandidateElectionTaskProgress progress : progressList) {
				if (progress == null || progress.getElectionTask() == null) {
					continue;
				}
				ElectionCalendar calendar = progress.getElectionTask().getElectionCalendar();
				if (calendar == null || calendar.getCalendarKey() == null) {
					continue;
				}
				if (fallbackKey == null) {
					fallbackKey = calendar.getCalendarKey();
				}
				Date start = calendar.getStartDate();
				Date end = calendar.getEndDate();
				if (start != null && (earliestStart == null || start.before(earliestStart))) {
					earliestStart = start;
					earliestKey = calendar.getCalendarKey();
				}
				Date effectiveEnd = end != null ? end : start;
				if (effectiveEnd != null && (latestEnd == null || effectiveEnd.after(latestEnd))) {
					latestEnd = effectiveEnd;
					latestKey = calendar.getCalendarKey();
				}
			}
		}

		ElectionCalendarKey accessDeniedKey = earliestKey != null ? earliestKey : (latestKey != null ? latestKey : fallbackKey);
		if (getElection() != null && !getElection().isNominationTasksLinkAvailable()) {
			return TokenAccessGate.always(buildNominationTasksAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate.START, accessDeniedKey));
		}
		if (nomination == null || nomination.getCandidate() == null) {
			return null;
		}
		if (progressList == null || progressList.isEmpty() || (earliestStart == null && latestEnd == null)) {
			return TokenAccessGate.always(buildNominationTasksAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate.START, accessDeniedKey));
		}

		TokenAccessGate.AccessBlock before = earliestStart != null
				? buildNominationTasksAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate.START, earliestKey)
				: null;
		TokenAccessGate.AccessBlock after = latestEnd != null
				? buildNominationTasksAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate.END, latestKey)
				: null;
		return TokenAccessGate.of(earliestStart, latestEnd, before, after);
	}

	protected TokenAccessGate resolveNominationAcceptanceAccessGate(Nomination nomination) {
		if (getElection() != null && !getElection().isNominationTasksLinkAvailable()) {
			return TokenAccessGate.always(buildNominationTasksAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate.START, ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES));
		}
		if (nomination == null) {
			return null;
		}
		ElectionCalendar calendar = findPublicCalendar(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
		if (calendar == null || calendar.getStartDate() == null) {
			return TokenAccessGate.always(buildNominationTasksAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate.START, ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES));
		}
		TokenAccessGate.AccessBlock before = buildNominationTasksAccessDeniedBlock(
				PublicAccessDeniedPage.CountdownTargetDate.START,
				ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
		TokenAccessGate.AccessBlock after = buildNominationTasksAccessDeniedBlock(
				PublicAccessDeniedPage.CountdownTargetDate.END,
				ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
		return TokenAccessGate.of(calendar.getStartDate(), calendar.getEndDate(), before, after);
	}

	private TokenAccessGate.AccessBlock buildNominationTasksAccessDeniedBlock(PublicAccessDeniedPage.CountdownTargetDate targetDate, ElectionCalendarKey targetKey) {
		return new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.ACCESS_NOT_AVAILABLE,
				targetDate,
				targetKey,
				null,
				null);
	}

	private List<ElectionCalendar> loadPublicCalendars() {
		Election election = getElection();
		if (election == null) {
			return Collections.emptyList();
		}
		List<ElectionCalendar> calendars = AppContext.getInstance().getManagerBeanRemote().getElectionCalendars(election.getElectionId());
		if (calendars == null || calendars.isEmpty()) {
			return Collections.emptyList();
		}
		List<ElectionCalendar> filtered = new ArrayList<>();
		for (ElectionCalendar calendar : calendars) {
			if (calendar == null || calendar.getCalendarKey() == null) {
				continue;
			}
			filtered.add(calendar);
		}
		return filtered;
	}

	private void initializeContextPanel() {
		debugPanel = new WebMarkupContainer("debugPanel");
		debugPanel.setOutputMarkupPlaceholderTag(true);
		debugPanel.add(new Label("whereAmI", valueOrDash(getWhereAmI())));
		debugPanel.add(new Label("contextClass", valueOrDash(getContextClass())));
		debugPanel.add(new MultiLineLabel("contextData", valueOrDash(getContextData())));
		debugPanel.add(new MultiLineLabel("electionData", buildElectionData()));
		add(debugPanel);
	}

	protected Component buildTopbarContent(String id) {
		WebMarkupContainer container = new WebMarkupContainer(id);
		container.setVisible(false);
		return container;
	}

	protected Component buildTopNavPublic(String id) {
		return new AppTopNavPublicHomeOnlyPanel(id);
	}

	protected Component buildTopHeader(String id) {
		return new PublicTokenTopHeaderPanel(
				id,
				this,
				resolveTopHeaderSectionLabel(),
				isTopHeaderLanguageSelectorVisible(),
				resolveTopHeaderNotificationMode(),
				resolveTopHeaderReminderFrequency());
	}

	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionGeneral", null, "Público");
	}

	protected boolean isTopHeaderLanguageSelectorVisible() {
		return true;
	}

	protected PublicTokenTopHeaderPanel.NotificationMode resolveTopHeaderNotificationMode() {
		return PublicTokenTopHeaderPanel.NotificationMode.NONE;
	}

	protected ReminderFrequency resolveTopHeaderReminderFrequency() {
		return ReminderFrequency.defaultValue();
	}

	protected Component buildGlobalFeedbackContainer(String id) {
		WebMarkupContainer container = new WebMarkupContainer(id);
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(isGlobalFeedbackEnabled());
		FeedbackPanel feedbackPanel = new FeedbackPanel("globalFeedbackPanel", new IFeedbackMessageFilter() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean accept(FeedbackMessage message) {
				return message != null && message.getReporter() == null;
			}
		});
		feedbackPanel.setOutputMarkupPlaceholderTag(true);
		container.add(feedbackPanel);
		return container;
	}

	protected boolean isGlobalFeedbackEnabled() {
		return false;
	}

	public void reloadCurrentPageWithCurrentParameters() {
		setResponsePage(getClass(), getPageParameters());
	}

	public void reloadCurrentPageWithLocale(LanguageCode language) {
		PageParameters params = new PageParameters(getPageParameters());
		params.set(LOCALE_PARAM, resolveLocaleLanguage(language).getLocaleCode());
		setResponsePage(getClass(), params);
	}

	protected void markTopbarLocaleSelectionAsManual() {
		if (getSession() != null) {
			getSession().setMetaData(MANUAL_TOPBAR_LOCALE_SELECTION_KEY, Boolean.TRUE);
		}
	}

	protected boolean hasManualTopbarLocaleSelection() {
		if (getSession() == null) {
			return false;
		}
		return Boolean.TRUE.equals(getSession().getMetaData(MANUAL_TOPBAR_LOCALE_SELECTION_KEY));
	}

	protected boolean hasLocaleRequestParameter() {
		return hasLocaleParameter(getPageParameters());
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	protected static final class TokenAccessGate {
		private final Date start;
		private final Date end;
		private final AccessBlock before;
		private final AccessBlock after;
		private final AccessBlock always;

		private TokenAccessGate(Date start, Date end, AccessBlock before, AccessBlock after, AccessBlock always) {
			this.start = start;
			this.end = end;
			this.before = before;
			this.after = after;
			this.always = always;
		}

		public static TokenAccessGate of(Date start, Date end, AccessBlock before, AccessBlock after) {
			return new TokenAccessGate(start, end, before, after, null);
		}

		public static TokenAccessGate always(AccessBlock block) {
			return new TokenAccessGate(null, null, null, null, block);
		}

		private AccessBlock evaluate(Date now) {
			if (always != null) {
				return always;
			}
			if (before != null && start != null && now.before(start)) {
				return before;
			}
			if (after != null && end != null && now.after(end)) {
				return after;
			}
			return null;
		}

		public static final class AccessBlock {
			private final PublicAccessDeniedPage.ErrorCode errorCode;
			private final PublicAccessDeniedPage.CountdownTargetDate targetDate;
			private final ElectionCalendarKey targetKey;
			private final String errorTitle;
			private final String errorMessage;

			public AccessBlock(PublicAccessDeniedPage.ErrorCode errorCode,
				PublicAccessDeniedPage.CountdownTargetDate targetDate,
				ElectionCalendarKey targetKey,
				String errorTitle,
				String errorMessage) {
				this.errorCode = errorCode;
				this.targetDate = targetDate;
				this.targetKey = targetKey;
				this.errorTitle = errorTitle;
				this.errorMessage = errorMessage;
			}

			public PublicAccessDeniedPage.ErrorCode getErrorCode() {
				return errorCode;
			}

			public PublicAccessDeniedPage.CountdownTargetDate getTargetDate() {
				return targetDate;
			}

			public ElectionCalendarKey getTargetKey() {
				return targetKey;
			}

			public String getErrorTitle() {
				return errorTitle;
			}

			public String getErrorMessage() {
				return errorMessage;
			}
		}
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		if (debugPanel != null) {
			debugPanel.setVisible(shouldShowDebugPanel());
		}
	}

	protected boolean shouldShowDebugPanel() {
		return isDebugRequestParameterEnabled(getPageParameters())
				&& SecurityUtils.isSignedIn()
				&& SecurityUtils.hasRole(DEBUG_REQUIRED_ROLE);
	}

	protected PageParameters buildTokenPageParameters() {
		PageParameters params = UtilsParameters.getToken(getToken());
		appendLocaleParameter(params);
		appendDebugParameter(params);
		return params;
	}

	protected PageParameters buildPublicElectionPageParameters() {
		String publicElectionToken = resolvePublicElectionToken(getElection());
		PageParameters params = hasText(publicElectionToken) ? UtilsParameters.getToken(publicElectionToken) : new PageParameters();
		appendLocaleParameter(params);
		appendDebugParameter(params);
		return params;
	}

	protected boolean hasPublicElectionPageToken() {
		return hasText(resolvePublicElectionToken(getElection()));
	}

	static String resolvePublicElectionToken(Election election) {
		if (election == null) {
			return null;
		}
		return hasText(election.getPublicElectionToken()) ? election.getPublicElectionToken() : null;
	}

	private String valueOrDash(Object value) {
		if (value == null) {
			return "-";
		}
		String text = String.valueOf(value);
		return text.isEmpty() ? "-" : text;
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String buildElectionData() {
		if (getElection() == null) {
			return "Sin election asociada";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("id: ").append(getElection().getElectionId()).append('\n');
		sb.append("titulo (es): ").append(valueOrDash(getElection().getTitleSpanish())).append('\n');
		sb.append("iniciada: ").append(getElection().isStarted()).append('\n');
		sb.append("voto habilitado: ").append(getElection().isEnabledToVote()).append('\n');
		sb.append("resultados publicos: ").append(getElection().isResultLinkAvailable()).append('\n');
		sb.append("auditoria publica: ").append(getElection().isAuditorLinkAvailable());
		return sb.toString();
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
		setHeaderElectionTitleFromElection(election);
	}

	public String getWhereAmI() {
		return whereAmI;
	}

	public void setWhereAmI(String whereAmI) {
		this.whereAmI = sanitizeDebugText(whereAmI);
	}

	public String getContextClass() {
		return contextClass;
	}

	public void setContextClass(String contextClass) {
		this.contextClass = contextClass;
	}

	public String getContextData() {
		return contextData;
	}

	public void setContextData(String contextData) {
		this.contextData = sanitizeDebugText(contextData);
	}

	public String getHeaderUserDisplay() {
		return headerUserDisplay;
	}

	public String getHeaderElectionTitle() {
		return headerElectionTitle;
	}

	public void setHeaderElectionTitle(String headerElectionTitle) {
		this.headerElectionTitle = valueOrDash(headerElectionTitle);
	}

	protected void setHeaderElectionTitleFromElection(Election election) {
		if (election == null) {
			setHeaderElectionTitle("-");
			return;
		}
		String language = SecurityUtils.getLocale() != null ? SecurityUtils.getLocale().getLanguage() : null;
		String electionTitle = language != null ? election.getTitle(language) : null;
		if (electionTitle == null || electionTitle.trim().isEmpty()) {
			electionTitle = election.getTitleSpanish();
		}
		setHeaderElectionTitle(electionTitle);
	}

	public void setHeaderUserDisplay(String headerUserDisplay) {
		this.headerUserDisplay = headerUserDisplay;
	}

	public String getHeaderNotificationStatus() {
		return headerNotificationStatus;
	}

	public void setHeaderNotificationStatus(String headerNotificationStatus) {
		this.headerNotificationStatus = headerNotificationStatus;
	}

	private void appendDebugParameter(PageParameters params) {
		if (params != null && isDebugRequestParameterEnabled(getPageParameters())) {
			params.add(DEBUG_PARAM, "true");
		}
	}

	private void applyLocaleRequestParameter(PageParameters params) {
		if (hasLocaleParameter(params)) {
			SecurityUtils.setLocale(resolveLocaleLanguage(UtilsParameters.getParameters(LOCALE_PARAM, params)));
		}
	}

	private void appendLocaleParameter(PageParameters params) {
		appendLocaleParameter(params, getPageParameters());
	}

	static void appendLocaleParameter(PageParameters targetParams, PageParameters sourceParams) {
		if (targetParams != null && hasLocaleParameter(sourceParams)) {
			targetParams.set(LOCALE_PARAM, resolveLocaleLanguage(UtilsParameters.getParameters(LOCALE_PARAM, sourceParams)).getLocaleCode());
		}
	}

	static LanguageCode resolveLocaleLanguage(String localeValue) {
		return LanguageCode.fromValueOrDefault(localeValue, LanguageCode.SP);
	}

	private LanguageCode resolveLocaleLanguage(LanguageCode language) {
		return language != null ? language : LanguageCode.SP;
	}

	private static boolean hasLocaleParameter(PageParameters params) {
		return params != null && params.contains(LOCALE_PARAM);
	}

	private boolean isDebugRequestParameterEnabled(PageParameters params) {
		String debugValue = UtilsParameters.getParameters(DEBUG_PARAM, params);
		if (!hasText(debugValue)) {
			return false;
		}
		String normalized = debugValue.trim().toLowerCase(Locale.ROOT);
		return "1".equals(normalized)
				|| "true".equals(normalized)
				|| "yes".equals(normalized)
				|| "on".equals(normalized);
	}

	private String sanitizeDebugText(String value) {
		if (value == null) {
			return null;
		}
		String sanitized = DEBUG_URL_PATTERN.matcher(value).replaceAll(DEBUG_REDACTED_VALUE);
		sanitized = DEBUG_ROUTE_PATTERN.matcher(sanitized).replaceAll(DEBUG_REDACTED_VALUE);
		String[] lines = sanitized.split("\\r?\\n", -1);
		for (int i = 0; i < lines.length; i++) {
			java.util.regex.Matcher matcher = DEBUG_SENSITIVE_LINE_PATTERN.matcher(lines[i]);
			if (matcher.matches()) {
				lines[i] = matcher.group(1) + ": " + DEBUG_REDACTED_VALUE;
				continue;
			}
			if (DEBUG_SECRET_VALUE_PATTERN.matcher(lines[i]).find()) {
				lines[i] = DEBUG_REDACTED_VALUE;
				continue;
			}
		}
		return String.join("\n", lines);
	}

	protected void setHeaderNotificationStatusFromReminderFrequency(ReminderFrequency reminderFrequency) {
		ReminderFrequency resolvedFrequency = reminderFrequency != null ? reminderFrequency : ReminderFrequency.defaultValue();
		String frequencyLabel = getString("reminderFrequency." + resolvedFrequency.name(), null, resolvedFrequency.name());
		setHeaderNotificationStatus(frequencyLabel);
	}
}
