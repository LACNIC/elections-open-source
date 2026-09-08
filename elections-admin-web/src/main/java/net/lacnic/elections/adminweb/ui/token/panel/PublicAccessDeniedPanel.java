package net.lacnic.elections.adminweb.ui.token.panel;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.token.PublicCalendarTimelinePanel;
import net.lacnic.elections.adminweb.ui.token.PublicCalendarTimelinePanel.TimelineEntryView;
import net.lacnic.elections.adminweb.ui.token.page.PublicAccessDeniedPage;
import net.lacnic.elections.adminweb.ui.token.page.PublicAccessDeniedPage.CountdownTargetDate;
import net.lacnic.elections.adminweb.ui.token.page.PublicAccessDeniedPage.ErrorCode;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;

public class PublicAccessDeniedPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final String DEBUG_PARAM = "debug";

	private final Election election;
	private final String token;
	private final String requestedTargetKey;
	private final CountdownTargetDate requestedTargetDateType;
	private final ErrorCode requestedErrorCode;
	private final String requestedErrorTitle;
	private final String requestedErrorMessage;
	private final String referenceUrl;

	private final List<ElectionCalendar> publicCalendars = new ArrayList<>();
	private final List<ElectionCalendar> allCalendars = new ArrayList<>();
	private final List<PreviewLink> previewErrorLinks = new ArrayList<>();
	private final List<PreviewLink> previewTargetLinks = new ArrayList<>();

	private WebMarkupContainer calendarContainer;
	private WebMarkupContainer countdownContainer;
	private WebMarkupContainer debugMetadataContainer;
	private WebMarkupContainer previewToolsContainer;

	private ResolvedTarget resolvedTarget;
	private boolean countdownVisible;

	private String errorTitle;
	private String errorMessage;
	private String targetDate;
	private String targetKey;
	private String targetDateTypeLabel;
	private String targetResolution;
	private String targetDateContext;
	private String targetKeyContext;
	private String targetDateTypeLabelContext;
	private String targetResolutionContext;
	private String electionName;
	private String electionToken;
	private String currentTimeLabel;
	private String requestedReferenceUrl;
	private String calendarSubline;
	private String daysValue;
	private String hoursValue;
	private String minutesValue;
	private String secondsValue;

	public PublicAccessDeniedPanel(String id, Election election, String token, PageParameters params) {
		super(id);
		this.election = election;
		this.token = normalizeText(token);
		this.requestedTargetKey = normalizeText(params != null ? params.get(PublicAccessDeniedPage.PARAM_TARGET_KEY).toString("") : null);
		this.requestedTargetDateType = CountdownTargetDateResolver.from(
				params != null ? params.get(PublicAccessDeniedPage.PARAM_TARGET_DATE).toString("") : null);
		this.requestedErrorCode = ErrorCodeResolver.from(params != null ? params.get(PublicAccessDeniedPage.PARAM_ERROR_CODE).toString("") : null);
		this.requestedErrorTitle = normalizeText(params != null ? params.get(PublicAccessDeniedPage.PARAM_ERROR_TITLE).toString("") : null);
		this.requestedErrorMessage = normalizeText(params != null ? params.get(PublicAccessDeniedPage.PARAM_ERROR_MESSAGE).toString("") : null);
		this.referenceUrl = normalizeText(params != null ? params.get(PublicAccessDeniedPage.PARAM_REFERENCE_URL).toString("") : null);

		setOutputMarkupId(true);
		loadPublicCalendars();
		buildPreviewLinks();

		add(new Label("errorTitle", new PropertyModel<String>(this, "errorTitle")));
		add(new Label("electionName", new PropertyModel<String>(this, "electionName")));
		add(new Label("currentTimeLabel", new PropertyModel<String>(this, "currentTimeLabel")));
		add(new Label("targetDateContext", new PropertyModel<String>(this, "targetDateContext")));

		debugMetadataContainer = new WebMarkupContainer("debugMetadataContainer");
		debugMetadataContainer.setOutputMarkupPlaceholderTag(true);
		debugMetadataContainer.setVisible(false);
		debugMetadataContainer.add(new Label("electionToken", new PropertyModel<String>(this, "electionToken")));
		debugMetadataContainer.add(new Label("referenceUrl", new PropertyModel<String>(this, "requestedReferenceUrl")));
		debugMetadataContainer.add(new Label("targetKeyContext", new PropertyModel<String>(this, "targetKeyContext")));
		debugMetadataContainer.add(new Label("targetResolutionContext", new PropertyModel<String>(this, "targetResolutionContext")));
		add(debugMetadataContainer);

		countdownContainer = new WebMarkupContainer("countdownContainer");
		countdownContainer.setOutputMarkupPlaceholderTag(true);
		countdownContainer.add(new Label("daysValue", new PropertyModel<String>(this, "daysValue")));
		countdownContainer.add(new Label("hoursValue", new PropertyModel<String>(this, "hoursValue")));
		countdownContainer.add(new Label("minutesValue", new PropertyModel<String>(this, "minutesValue")));
		countdownContainer.add(new Label("secondsValue", new PropertyModel<String>(this, "secondsValue")));
		add(countdownContainer);

		calendarContainer = new WebMarkupContainer("calendarContainer");
		calendarContainer.setOutputMarkupId(true);
		calendarContainer.add(new Label("calendarSubline", new PropertyModel<String>(this, "calendarSubline")));
		calendarContainer.add(new PublicCalendarTimelinePanel("timelinePanel", Collections.<TimelineEntryView>emptyList(), true));
		add(calendarContainer);

		previewToolsContainer = new WebMarkupContainer("previewToolsContainer");
		previewToolsContainer.setOutputMarkupPlaceholderTag(true);
		previewToolsContainer.setVisible(false);
		previewToolsContainer.add(new ListView<PreviewLink>("previewErrorLinks", previewErrorLinks) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<PreviewLink> item) {
				PreviewLink previewLink = item.getModelObject();
				BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("previewLink", PublicAccessDeniedPage.class, previewLink.getParams());
				link.add(new Label("previewLabel", previewLink.getLabel()));
				item.add(link);
			}
		});
		previewToolsContainer.add(new ListView<PreviewLink>("previewTargetLinks", previewTargetLinks) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<PreviewLink> item) {
				PreviewLink previewLink = item.getModelObject();
				BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("previewLink", PublicAccessDeniedPage.class, previewLink.getParams());
				link.add(new Label("previewLabel", previewLink.getLabel()));
				item.add(link);
			}
		});
		add(previewToolsContainer);

		refreshViewState();

		add(new AbstractAjaxTimerBehavior(Duration.ofSeconds(1)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onTimer(AjaxRequestTarget target) {
				refreshViewState();
				target.add(PublicAccessDeniedPanel.this);
			}
		});
	}

	private void buildPreviewLinks() {
		previewErrorLinks.clear();
		previewTargetLinks.clear();

		ErrorCode currentError = requestedErrorCode != null ? requestedErrorCode : ErrorCode.ACCESS_NOT_AVAILABLE;
		for (ErrorCode errorCode : ErrorCode.values()) {
			previewErrorLinks.add(new PreviewLink(
					errorCode.name(),
					buildPreviewParams(errorCode, requestedTargetKey, requestedTargetDateType)));
		}

		previewTargetLinks.add(new PreviewLink(
				"AUTO · próximo hito público",
				buildPreviewParams(currentError, null, null)));

		for (ElectionCalendar calendar : publicCalendars) {
			if (calendar == null || calendar.getCalendarKey() == null || calendar.getStartDate() == null) {
				continue;
			}
			String key = calendar.getCalendarKey().name();
			if (isPeriod(calendar)) {
				previewTargetLinks.add(new PreviewLink(
						key + " · start",
						buildPreviewParams(currentError, key, CountdownTargetDate.START)));
				previewTargetLinks.add(new PreviewLink(
						key + " · end",
						buildPreviewParams(currentError, key, CountdownTargetDate.END)));
			} else {
				previewTargetLinks.add(new PreviewLink(
						key + " · point",
						buildPreviewParams(currentError, key, CountdownTargetDate.POINT)));
			}
		}
	}

	private PageParameters buildPreviewParams(ErrorCode errorCode, String targetKeyValue, CountdownTargetDate targetDateTypeValue) {
		PageParameters params = hasText(token) ? UtilsParameters.getToken(token) : new PageParameters();
		if (errorCode != null) {
			params.add(PublicAccessDeniedPage.PARAM_ERROR_CODE, errorCode.name());
		}
		if (hasText(targetKeyValue)) {
			params.add(PublicAccessDeniedPage.PARAM_TARGET_KEY, targetKeyValue);
		}
		if (targetDateTypeValue != null) {
			params.add(PublicAccessDeniedPage.PARAM_TARGET_DATE, targetDateTypeValue.name().toLowerCase(Locale.ROOT));
		}
		if (hasText(referenceUrl)) {
			params.add(PublicAccessDeniedPage.PARAM_REFERENCE_URL, referenceUrl);
		}
		params.add(DEBUG_PARAM, "true");
		return params;
	}

	private void loadPublicCalendars() {
		allCalendars.clear();
		publicCalendars.clear();
		if (election == null) {
			return;
		}
		List<ElectionCalendar> calendars = AppContext.getInstance().getManagerBeanRemote().getElectionCalendars(election.getElectionId());
		if (calendars == null) {
			return;
		}
		for (ElectionCalendar calendar : calendars) {
			if (calendar == null || calendar.getCalendarKey() == null || calendar.getStartDate() == null) {
				continue;
			}
			allCalendars.add(calendar);
			if (!calendar.isPublicable()) {
				continue;
			}
			publicCalendars.add(calendar);
		}
		Collections.sort(publicCalendars, new Comparator<ElectionCalendar>() {
			@Override
			public int compare(ElectionCalendar left, ElectionCalendar right) {
				Date leftStart = left != null ? left.getStartDate() : null;
				Date rightStart = right != null ? right.getStartDate() : null;
				if (leftStart == null && rightStart == null) {
					return compareCalendarKeys(left, right);
				}
				if (leftStart == null) {
					return 1;
				}
				if (rightStart == null) {
					return -1;
				}
				int dateCompare = leftStart.compareTo(rightStart);
				if (dateCompare != 0) {
					return dateCompare;
				}
				return compareCalendarKeys(left, right);
			}
		});
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		if (debugMetadataContainer != null) {
			debugMetadataContainer.setVisible(shouldShowPreviewTools());
		}
		if (previewToolsContainer != null) {
			previewToolsContainer.setVisible(shouldShowPreviewTools());
		}
	}

	private boolean shouldShowPreviewTools() {
		if (!(getPage() instanceof PublicAccessDeniedPage)) {
			return false;
		}
		return ((PublicAccessDeniedPage) getPage()).shouldShowPreviewTools();
	}

	private int compareCalendarKeys(ElectionCalendar left, ElectionCalendar right) {
		ElectionCalendarKey leftKey = left != null ? left.getCalendarKey() : null;
		ElectionCalendarKey rightKey = right != null ? right.getCalendarKey() : null;
		if (leftKey == null && rightKey == null) {
			return 0;
		}
		if (leftKey == null) {
			return 1;
		}
		if (rightKey == null) {
			return -1;
		}
		return Integer.compare(leftKey.ordinal(), rightKey.ordinal());
	}

	private void refreshViewState() {
		Date now = new Date();
		resolvedTarget = resolveTarget(now);
		countdownVisible = resolvedTarget != null
				&& resolvedTarget.getTargetDate() != null
				&& resolvedTarget.getTargetDate().after(now);

		resolveErrorTexts(now);
		electionName = resolveElectionTitle();
		electionToken = valueOrDash(token);
		currentTimeLabel = formatDateTime(now);
		requestedReferenceUrl = valueOrDash(referenceUrl);
		calendarSubline = MessageFormat.format(getString("publicCalendarCountdownCalendarSubline"), electionName);
		targetDateContext = resolveContextTargetDate();
		targetKeyContext = resolveContextTargetKey();
		targetDateTypeLabelContext = resolveContextTargetDateTypeLabel();
		targetResolutionContext = resolveContextTargetResolution();

		if (countdownVisible) {
			targetDate = targetDateContext;
			targetKey = targetKeyContext;
			targetDateTypeLabel = targetDateTypeLabelContext;
			targetResolution = targetResolutionContext;
			refreshCountdownValues(now);
		} else {
			targetDate = "-";
			targetKey = "-";
			targetDateTypeLabel = "-";
			targetResolution = getString("publicCalendarCountdownResolutionMissing");
			daysValue = "00";
			hoursValue = "00";
			minutesValue = "00";
			secondsValue = "00";
		}

		countdownContainer.setVisible(countdownVisible);
		calendarContainer.addOrReplace(new PublicCalendarTimelinePanel("timelinePanel", buildTimelineRows(), true));
	}

	private void resolveErrorTexts(Date now) {
		if (hasText(requestedErrorTitle) || hasText(requestedErrorMessage)) {
			errorTitle = hasText(requestedErrorTitle) ? requestedErrorTitle : defaultErrorTitle(now);
			errorMessage = hasText(requestedErrorMessage) ? requestedErrorMessage : defaultErrorMessage(now);
			return;
		}
		errorTitle = defaultErrorTitle(now);
		errorMessage = defaultErrorMessage(now);
	}

	private String defaultErrorTitle(Date now) {
		String suffix = countdownVisible ? "Upcoming" : "Unavailable";
		switch (requestedErrorCode != null ? requestedErrorCode : ErrorCode.ACCESS_NOT_AVAILABLE) {
		case VOTE_NOT_OPEN:
			return getString("publicCalendarCountdownErrorTitleVoteNotOpen");
		case VOTE_CLOSED:
			return getString("publicCalendarCountdownErrorTitleVoteClosed");
		case RESULTS_NOT_PUBLIC:
			return getString("publicCalendarCountdownErrorTitleResults");
		case AUDIT_NOT_PUBLIC:
			return getString("publicCalendarCountdownErrorTitleAudit");
		case CANDIDATE_QUESTIONS_NOT_OPEN:
			return getString("publicCalendarCountdownErrorTitleQuestionsNotOpen");
		case CANDIDATE_QUESTIONS_CLOSED:
			return getString("publicCalendarCountdownErrorTitleQuestionsClosed");
		case PUBLIC_ELECTION_NOT_AVAILABLE:
			return getString("publicCalendarCountdownErrorTitlePublicElection");
		case ACCESS_NOT_AVAILABLE:
		default:
			if (resolvedTarget != null && resolvedTarget.getTargetDate() != null && !resolvedTarget.getTargetDate().after(now)) {
				return getString("publicCalendarCountdownErrorTitleUnavailable");
			}
			return getString("publicCalendarCountdownErrorTitle" + suffix);
		}
	}

	private String defaultErrorMessage(Date now) {
		switch (requestedErrorCode != null ? requestedErrorCode : ErrorCode.ACCESS_NOT_AVAILABLE) {
		case VOTE_NOT_OPEN:
			return countdownVisible
					? getString("publicCalendarCountdownErrorMessageVoteNotOpenUpcoming")
					: getString("publicCalendarCountdownErrorMessageVoteNotOpenUnavailable");
		case VOTE_CLOSED:
			return getString("publicCalendarCountdownErrorMessageVoteClosed");
		case RESULTS_NOT_PUBLIC:
			return countdownVisible
					? getString("publicCalendarCountdownErrorMessageResultsUpcoming")
					: getString("publicCalendarCountdownErrorMessageResultsUnavailable");
		case AUDIT_NOT_PUBLIC:
			return countdownVisible
					? getString("publicCalendarCountdownErrorMessageAuditUpcoming")
					: getString("publicCalendarCountdownErrorMessageAuditUnavailable");
		case CANDIDATE_QUESTIONS_NOT_OPEN:
			return countdownVisible
					? getString("publicCalendarCountdownErrorMessageQuestionsNotOpenUpcoming")
					: getString("publicCalendarCountdownErrorMessageQuestionsNotOpenUnavailable");
		case CANDIDATE_QUESTIONS_CLOSED:
			return getString("publicCalendarCountdownErrorMessageQuestionsClosed");
		case PUBLIC_ELECTION_NOT_AVAILABLE:
			return countdownVisible
					? getString("publicCalendarCountdownErrorMessagePublicElectionUpcoming")
					: getString("publicCalendarCountdownErrorMessagePublicElectionUnavailable");
		case ACCESS_NOT_AVAILABLE:
		default:
			if (resolvedTarget != null && resolvedTarget.getTargetDate() != null && !resolvedTarget.getTargetDate().after(now)) {
				return getString("publicCalendarCountdownErrorMessageUnavailable");
			}
			return countdownVisible
					? getString("publicCalendarCountdownErrorMessageUpcoming")
					: getString("publicCalendarCountdownErrorMessageUnavailable");
		}
	}

	private String resolveContextTargetDate() {
		if (resolvedTarget == null || resolvedTarget.getTargetDate() == null) {
			return "-";
		}
		return formatDateTime(resolvedTarget.getTargetDate());
	}

	private String resolveContextTargetKey() {
		if (resolvedTarget != null && hasText(resolvedTarget.getCalendarKey())) {
			return resolvedTarget.getCalendarKey();
		}
		return valueOrDash(requestedTargetKey);
	}

	private String resolveContextTargetDateTypeLabel() {
		if (resolvedTarget != null && hasText(resolvedTarget.getTargetDateLabel())) {
			return resolvedTarget.getTargetDateLabel();
		}
		if (hasText(requestedTargetKey)) {
			return resolveTargetDateLabel(requestedTargetDateType);
		}
		return "-";
	}

	private String resolveContextTargetResolution() {
		if (resolvedTarget != null) {
			return resolvedTarget.isAutomatic()
					? getString("publicCalendarCountdownResolutionAutomatic")
					: getString("publicCalendarCountdownResolutionParameterized");
		}
		if (hasText(requestedTargetKey)) {
			return getString("publicCalendarCountdownResolutionParameterized");
		}
		return getString("publicCalendarCountdownResolutionMissing");
	}

	private void refreshCountdownValues(Date now) {
		long diff = Math.max(0L, resolvedTarget.getTargetDate().getTime() - now.getTime());
		long totalSeconds = diff / 1000L;
		long days = totalSeconds / 86400L;
		long hours = (totalSeconds % 86400L) / 3600L;
		long minutes = (totalSeconds % 3600L) / 60L;
		long seconds = totalSeconds % 60L;

		daysValue = pad(days);
		hoursValue = pad(hours);
		minutesValue = pad(minutes);
		secondsValue = pad(seconds);
	}

	private ResolvedTarget resolveTarget(Date now) {
		if (!hasText(requestedTargetKey)) {
			if (publicCalendars.isEmpty()) {
				return null;
			}
			return resolveAutomaticTarget(now);
		}
		for (ElectionCalendar calendar : allCalendars) {
			if (calendar.getCalendarKey() == null || !calendar.getCalendarKey().name().equalsIgnoreCase(requestedTargetKey)) {
				continue;
			}
			return buildResolvedTarget(calendar, requestedTargetDateType, false);
		}
		return null;
	}

	private ResolvedTarget resolveAutomaticTarget(Date now) {
		List<ResolvedTarget> candidates = new ArrayList<ResolvedTarget>();
		for (ElectionCalendar calendar : publicCalendars) {
			Date startDate = calendar.getStartDate();
			Date endDate = effectiveEndDate(calendar);
			if (startDate != null && !startDate.before(now)) {
				candidates.add(buildResolvedTarget(calendar, CountdownTargetDate.START, true));
			}
			if (isPeriod(calendar) && endDate != null && !endDate.before(now)) {
				candidates.add(buildResolvedTarget(calendar, CountdownTargetDate.END, true));
			}
			if (!isPeriod(calendar) && startDate != null && !startDate.before(now)) {
				candidates.add(buildResolvedTarget(calendar, CountdownTargetDate.POINT, true));
			}
		}
		Collections.sort(candidates, new Comparator<ResolvedTarget>() {
			@Override
			public int compare(ResolvedTarget left, ResolvedTarget right) {
				Date leftDate = left != null ? left.getTargetDate() : null;
				Date rightDate = right != null ? right.getTargetDate() : null;
				if (leftDate == null && rightDate == null) {
					return 0;
				}
				if (leftDate == null) {
					return 1;
				}
				if (rightDate == null) {
					return -1;
				}
				int compare = leftDate.compareTo(rightDate);
				if (compare != 0) {
					return compare;
				}
				return left.getCalendarKey().compareTo(right.getCalendarKey());
			}
		});
		return candidates.isEmpty() ? null : candidates.get(0);
	}

	private ResolvedTarget buildResolvedTarget(ElectionCalendar calendar, CountdownTargetDate requestedType, boolean automatic) {
		ResolvedTarget target = new ResolvedTarget();
		target.automatic = automatic;
		target.calendar = calendar;
		target.targetDateType = normalizeTargetDateType(calendar, requestedType);
		target.targetDate = resolveTargetDate(calendar, target.targetDateType);
		target.calendarKey = calendar.getCalendarKey().name();
		target.targetDateLabel = resolveTargetDateLabel(target.targetDateType);
		return target;
	}

	private String resolveTargetDateLabel(CountdownTargetDate targetDateType) {
		if (targetDateType == CountdownTargetDate.END) {
			return getString("publicCalendarCountdownTargetDateTypeEnd");
		}
		if (targetDateType == CountdownTargetDate.POINT) {
			return getString("publicCalendarCountdownTargetDateTypePoint");
		}
		return getString("publicCalendarCountdownTargetDateTypeStart");
	}

	private CountdownTargetDate normalizeTargetDateType(ElectionCalendar calendar, CountdownTargetDate requestedType) {
		if (!isPeriod(calendar)) {
			return CountdownTargetDate.POINT;
		}
		return requestedType == CountdownTargetDate.END ? CountdownTargetDate.END : CountdownTargetDate.START;
	}

	private Date resolveTargetDate(ElectionCalendar calendar, CountdownTargetDate targetDateType) {
		if (calendar == null) {
			return null;
		}
		if (targetDateType == CountdownTargetDate.END) {
			return effectiveEndDate(calendar);
		}
		return calendar.getStartDate();
	}

	private List<TimelineEntryView> buildTimelineRows() {
		List<TimelineEntryView> rows = new ArrayList<TimelineEntryView>();
		for (ElectionCalendar calendar : publicCalendars) {
			ElectionCalendarKey key = calendar.getCalendarKey();
			rows.add(new TimelineEntryView(
					calendar.getStartDate(),
					calendar.getEndDate(),
					key.ordinal(),
					resolveCalendarTitle(key),
					buildCalendarScheduleLabel(calendar),
					resolveTimelinePhaseLabel(key),
					null));
		}
		return rows;
	}

	private String resolveElectionTitle() {
		if (election == null) {
			return "-";
		}
		String language = SecurityUtils.getLocale() != null ? SecurityUtils.getLocale().getLanguage() : null;
		String title = language != null ? election.getTitle(language) : null;
		if (hasText(title)) {
			return title.trim();
		}
		return valueOrDash(election.getTitleSpanish());
	}

	private String resolveCalendarTitle(ElectionCalendarKey key) {
		if (key == null) {
			return "-";
		}
		return getString("electionCalendarKey." + key.name(), null, key.name());
	}

	private String resolveTimelinePhaseLabel(ElectionCalendarKey key) {
		if (key == null || key.name() == null) {
			return getString("auditPublicV2PhaseCalendar");
		}
		String name = key.name();
		if (name.contains("CALL_FOR_CANDIDATES")) {
			return getString("auditPublicV2PhaseCall");
		}
		if (name.contains("PADRON")) {
			return getString("auditPublicV2PhaseCensus");
		}
		if (name.contains("CANDIDATE") || name.contains("EVALUATION")) {
			return getString("auditPublicV2PhaseCandidates");
		}
		if (name.contains("VOTING")) {
			return getString("auditPublicV2PhaseVoting");
		}
		if (name.contains("AUDIT")) {
			return getString("auditPublicV2PhaseAudit");
		}
		if (name.contains("RESULT")) {
			return getString("auditPublicV2PhaseResults");
		}
		return getString("auditPublicV2PhaseCalendar");
	}

	private String buildCalendarScheduleLabel(ElectionCalendar calendar) {
		if (calendar == null || calendar.getStartDate() == null) {
			return "-";
		}
		String start = formatDateTime(calendar.getStartDate());
		Date endDate = calendar.getEndDate();
		if (endDate == null || endDate.equals(calendar.getStartDate())) {
			return start;
		}
		return start + " → " + formatDateTime(endDate);
	}

	private boolean isPeriod(ElectionCalendar calendar) {
		if (calendar == null || calendar.getStartDate() == null) {
			return false;
		}
		Date endDate = calendar.getEndDate();
		return endDate != null && !endDate.equals(calendar.getStartDate());
	}

	private Date effectiveEndDate(ElectionCalendar calendar) {
		if (calendar == null) {
			return null;
		}
		return calendar.getEndDate() != null ? calendar.getEndDate() : calendar.getStartDate();
	}

	private String formatDateTime(Date date) {
		if (date == null) {
			return "-";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy · HH:mm", resolveDisplayLocale());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date) + " UTC";
	}

	private Locale resolveDisplayLocale() {
		Locale locale = SecurityUtils.getLocale();
		return locale != null ? locale : new Locale("es", "ES");
	}

	private String pad(long value) {
		return value < 10 ? "0" + value : String.valueOf(value);
	}

	private String normalizeText(String value) {
		return hasText(value) ? value.trim() : null;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value.trim() : "-";
	}

	public String getErrorTitle() {
		return errorTitle;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getTargetDate() {
		return targetDate;
	}

	public String getTargetKey() {
		return targetKey;
	}

	public String getTargetDateTypeLabel() {
		return targetDateTypeLabel;
	}

	public String getTargetResolution() {
		return targetResolution;
	}

	public String getTargetDateContext() {
		return targetDateContext;
	}

	public String getTargetKeyContext() {
		return targetKeyContext;
	}

	public String getTargetDateTypeLabelContext() {
		return targetDateTypeLabelContext;
	}

	public String getTargetResolutionContext() {
		return targetResolutionContext;
	}

	public String getElectionName() {
		return electionName;
	}

	public String getElectionToken() {
		return electionToken;
	}

	public String getCurrentTimeLabel() {
		return currentTimeLabel;
	}

	public String getRequestedReferenceUrl() {
		return requestedReferenceUrl;
	}

	public String getCalendarSubline() {
		return calendarSubline;
	}

	public String getDaysValue() {
		return daysValue;
	}

	public String getHoursValue() {
		return hoursValue;
	}

	public String getMinutesValue() {
		return minutesValue;
	}

	public String getSecondsValue() {
		return secondsValue;
	}

	private static class ResolvedTarget implements Serializable {
		private static final long serialVersionUID = 1L;

		private ElectionCalendar calendar;
		private CountdownTargetDate targetDateType;
		private Date targetDate;
		private boolean automatic;
		private String calendarKey;
		private String targetDateLabel;

		public Date getTargetDate() {
			return targetDate;
		}

		public boolean isAutomatic() {
			return automatic;
		}

		public String getCalendarKey() {
			return calendarKey;
		}

		public String getTargetDateLabel() {
			return targetDateLabel;
		}
	}

	private static final class CountdownTargetDateResolver {
		private static CountdownTargetDate from(String targetDate) {
			String resolved = normalize(targetDate);
			if ("end".equals(resolved)) {
				return CountdownTargetDate.END;
			}
			if ("point".equals(resolved) || "single".equals(resolved) || "at".equals(resolved)) {
				return CountdownTargetDate.POINT;
			}
			return CountdownTargetDate.START;
		}

		private static String normalize(String value) {
			return value != null ? value.trim().toLowerCase(Locale.ROOT) : null;
		}
	}

	private static final class ErrorCodeResolver {
		private static ErrorCode from(String value) {
			if (value == null || value.trim().isEmpty()) {
				return null;
			}
			try {
				return ErrorCode.valueOf(value.trim().toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	private static final class PreviewLink implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String label;
		private final PageParameters params;

		private PreviewLink(String label, PageParameters params) {
			this.label = label;
			this.params = params;
		}

		public String getLabel() {
			return label;
		}

		public PageParameters getParams() {
			return params;
		}
	}
}
