package net.lacnic.elections.adminweb.ui.admin.election.calendarmodule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.tasks.ElectionTasksDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.utils.DateTimeUtils;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class ElectionCalendarModuleDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 5859304471686878489L;
	private static final String MONTH_PARAM = "month";
	private static final String MONTH_PARAM_PATTERN = "yyyy-MM";
	private static final String FIELD_START_DATE = "startDate";
	private static final String FIELD_START_TIME = "startTime";
	private static final String FIELD_END_DATE = "endDate";
	private static final String FIELD_END_TIME = "endTime";
	private static final String[] STAGE_BACKGROUND_COLORS = {
		"#ffd6d6", "#ffe2cc", "#ffefbf", "#f9f7b8", "#e8f4bf", "#d7f2c8",
		"#c9f3dd", "#c5f3ef", "#c8eefb", "#cfe4ff", "#d9dcff", "#e6d8ff",
		"#f0d5ff", "#f8d3f6", "#ffd5ea", "#ffd9df", "#ffe2d6", "#f2e2d6",
		"#e7e5d8", "#d8ead8", "#d1ece4", "#cfe6f2"
	};

	private Election election;
	private List<CalendarStage> stages;
	private List<String> weekHeaders;
	private List<CalendarWeekRow> weeks;
	private String monthTitle;
	private DateTime calendarMonth;
	private DateTime minStageDate;
	private DateTime maxStageDate;

	public ElectionCalendarModuleDashboard(PageParameters params) {
		super(params);

		Election loadedElection = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));
		if (loadedElection.isClosed()) {
			setResponsePage(ErrorElectionClosed.class);
			return;
		}

		election = loadedElection;
		stages = buildStages();
		initializeCalendarMonth(params);
		buildCalendarMonth();

		add(new FeedbackPanel("feedback"));
		add(new ManageElectionTabsPanel("tabsPanel", election, "tabCalendar"));
		add(new CalendarModuleForm("calendarForm"));
	}

	private List<CalendarStage> buildStages() {
		List<CalendarStage> result = new ArrayList<>();
		List<ElectionCalendar> existingCalendars = AppContext.getInstance().getManagerBeanRemote().getElectionCalendars(election.getElectionId());
		Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey = new HashMap<>();
		for (ElectionCalendar calendar : existingCalendars) {
			if (calendar.getCalendarKey() != null) {
				calendarsByKey.put(calendar.getCalendarKey(), calendar);
			}
		}
		ElectionCalendar votingCalendar = calendarsByKey.get(ElectionCalendarKey.N_16_PERIODO_VOTING);
		DateTime base = votingCalendar != null && votingCalendar.getStartDate() != null
				? new DateTime(votingCalendar.getStartDate()).plusHours(election.getDiffUTC()).withTimeAtStartOfDay()
				: DateTime.now().withTimeAtStartOfDay();

		ElectionCalendarKey[] keys = ElectionCalendarKey.values();
		for (int index = 0; index < keys.length; index++) {
			ElectionCalendarKey key = keys[index];
			CalendarStage stage = new CalendarStage();
			stage.setKey(key.name());
			stage.setStageCode(buildStageCode(key));
			stage.setTitle(buildDisplayName(key));
			stage.setDescription(resolveDescription(key));
			stage.setNoDateLabel(getString("electionCalendarNoDateConfigured", null, "Sin fecha configurada"));
			stage.setAllowUndefinedDate(!ElectionCalendarKey.N_16_PERIODO_VOTING.equals(key));
			stage.setCalendarEventStyle(resolveCalendarEventStyle(index));
			stage.setCalendarDotStyle("background-color: " + resolveStageBackgroundColor(index) + ";");
			stage.setPeriod(key.name().contains("_PERIODO_"));
			ElectionCalendar persisted = calendarsByKey.get(key);
			if (persisted != null) {
				stage.setPublicable(persisted.isPublicable());
				if (persisted.getStartDate() != null) {
					stage.setStartDate(formatUiDate(persisted.getStartDate()));
					stage.setStartTime(formatUiTime(persisted.getStartDate()));
					if (stage.isPeriod()) {
						Date endDate = persisted.getEndDate() != null ? persisted.getEndDate() : persisted.getStartDate();
						stage.setEndDate(formatUiDate(endDate));
						stage.setEndTime(formatUiTime(endDate));
					}
				} else if (stage.isAllowUndefinedDate()) {
					stage.setUndefinedDate(true);
					clearStageDates(stage);
				} else {
					applyDefaultDates(stage, base);
				}
			} else {
				applyDefaultDates(stage, base);
			}
			result.add(stage);
		}
		applyStatusByDates(result);

		return result;
	}

	private void applyDefaultDates(CalendarStage stage, DateTime base) {
		DateTime firstDayOfMonth = base.dayOfMonth().withMinimumValue();
		stage.setStartDate(firstDayOfMonth.toString(DateTimeUtils.ELECTION_DATE_FORMAT));
		stage.setStartTime("18:00");
		if (stage.isPeriod()) {
			stage.setEndDate(firstDayOfMonth.toString(DateTimeUtils.ELECTION_DATE_FORMAT));
			stage.setEndTime("18:00");
		} else {
			stage.setEndDate(null);
			stage.setEndTime(null);
		}
	}

	private void clearStageDates(CalendarStage stage) {
		stage.setStartDate(null);
		stage.setStartTime(null);
		stage.setEndDate(null);
		stage.setEndTime(null);
	}

	private void initializeCalendarMonth(PageParameters params) {
		DateTime[] range = resolveStageRange();
		minStageDate = range[0];
		maxStageDate = range[1];

		DateTime defaultMonth = minStageDate != null
			? minStageDate.dayOfMonth().withMinimumValue()
			: DateTime.now().withTimeAtStartOfDay().dayOfMonth().withMinimumValue();
		calendarMonth = parseMonthParam(params.get(MONTH_PARAM).toString("")) ;
		if (calendarMonth == null) {
			calendarMonth = defaultMonth;
		}
	}

	private DateTime parseMonthParam(String monthParam) {
		if (monthParam == null || monthParam.isBlank()) {
			return null;
		}
		try {
			return DateTime.parse(monthParam, DateTimeFormat.forPattern(MONTH_PARAM_PATTERN)).withTimeAtStartOfDay().dayOfMonth().withMinimumValue();
		} catch (Exception e) {
			return null;
		}
	}

	private DateTime[] resolveStageRange() {
		DateTime rangeStart = null;
		DateTime rangeEnd = null;
		for (CalendarStage stage : stages) {
			if (stage.isUndefinedDate()) {
				continue;
			}
			DateTime start = parseDateOnly(stage.getStartDate());
			if (start == null) {
				continue;
			}
			DateTime startDay = start.withTimeAtStartOfDay();
			DateTime endDay = startDay;
			if (stage.isPeriod()) {
				DateTime end = parseDateOnly(stage.getEndDate());
				if (end != null && !end.withTimeAtStartOfDay().isBefore(startDay)) {
					endDay = end.withTimeAtStartOfDay();
				}
			}
			if (rangeStart == null || startDay.isBefore(rangeStart)) {
				rangeStart = startDay;
			}
			if (rangeEnd == null || endDay.isAfter(rangeEnd)) {
				rangeEnd = endDay;
			}
		}
		return new DateTime[] { rangeStart, rangeEnd };
	}

	private void applyStatusByDates(List<CalendarStage> rows) {
		DateTime nowLocal = DateTime.now().plusHours(election.getDiffUTC());
		DateTime lastConfiguredEnd = null;
		for (CalendarStage stage : rows) {
			if (stage.isUndefinedDate()) {
				applyMissingStatus(stage);
				continue;
			}
			DateTime start = parseLocalDateTime(stage.getStartDate(), stage.getStartTime());
			DateTime end = stage.isPeriod() ? parseLocalDateTime(stage.getEndDate(), stage.getEndTime()) : start;

			if (start == null || end == null) {
				applyMissingStatus(stage);
				continue;
			}
			if (end.isBefore(start)) {
				applyOverlapStatus(stage, nowLocal.isBefore(start));
				continue;
			}
			if (lastConfiguredEnd != null && start.isBefore(lastConfiguredEnd)) {
				applyOverlapStatus(stage, nowLocal.isBefore(start));
			} else if (nowLocal.isBefore(start)) {
				applyPendingStatus(stage);
			} else {
				applyValidStatus(stage);
			}
			if (lastConfiguredEnd == null || end.isAfter(lastConfiguredEnd)) {
				lastConfiguredEnd = end;
			}
		}
	}

	private void applyValidStatus(CalendarStage stage) {
		stage.setStatusLabel(null);
		stage.setStatusBadgeClass(null);
		stage.setShowStatus(false);
		stage.setDotClass("calendar-dot-valid");
		stage.setIconClass("ti ti-check");
	}

	private void applyOverlapStatus(CalendarStage stage, boolean futureStage) {
		stage.setStatusLabel("Solapamiento detectado");
		stage.setStatusBadgeClass(futureStage
			? "bg-secondary-subtle text-secondary-emphasis border border-secondary-subtle"
			: "bg-success-subtle text-success-emphasis border border-success-subtle");
		stage.setShowStatus(true);
		stage.setDotClass(futureStage ? "calendar-dot-pending" : "calendar-dot-valid");
		stage.setIconClass(futureStage ? "ti ti-clock" : "ti ti-check");
	}

	private void applyPendingStatus(CalendarStage stage) {
		stage.setStatusLabel(null);
		stage.setStatusBadgeClass(null);
		stage.setShowStatus(false);
		stage.setDotClass("calendar-dot-pending");
		stage.setIconClass("ti ti-clock");
	}

	private void applyMissingStatus(CalendarStage stage) {
		stage.setStatusLabel(null);
		stage.setStatusBadgeClass(null);
		stage.setShowStatus(false);
		stage.setDotClass("calendar-dot-warning");
		stage.setIconClass("ti ti-alert-circle");
	}

	private String resolveDescription(ElectionCalendarKey key) {
		String descriptionKey = "electionCalendarDescription." + key.name();
		String fallback = key.getDescriptionES();
		String language = SecurityUtils.getLocale().getLanguage();
		if ("pt".equalsIgnoreCase(language)) {
			fallback = key.getDescriptionPT();
		}
		if ("en".equalsIgnoreCase(language)) {
			fallback = key.getDescriptionEN();
		}
		return getString(descriptionKey, null, fallback);
	}

	private String buildDisplayName(ElectionCalendarKey key) {
		return getString("electionCalendarKey." + key.name(), null, key.name());
	}

	private String buildStageCode(ElectionCalendarKey key) {
		if (key == null) {
			return null;
		}
		String keyName = key.name();
		if (!keyName.startsWith("N_")) {
			return keyName;
		}
		int secondUnderscore = keyName.indexOf('_', 2);
		if (secondUnderscore > 2) {
			return "N" + keyName.substring(2, secondUnderscore);
		}
		return "N" + keyName.substring(2);
	}

	private String formatUiDate(Date dbDate) {
		return new DateTime(dbDate).plusHours(election.getDiffUTC()).toString(DateTimeUtils.ELECTION_DATE_FORMAT);
	}

	private String formatUiTime(Date dbDate) {
		return new DateTime(dbDate).plusHours(election.getDiffUTC()).toString(DateTimeUtils.ELECTION_TIME_FORMAT);
	}

	private DateTime parseDateOnly(String dateValue) {
		if (dateValue == null || dateValue.isBlank()) {
			return null;
		}
		try {
			return DateTime.parse(dateValue, DateTimeFormat.forPattern(DateTimeUtils.ELECTION_DATE_FORMAT));
		} catch (Exception e) {
			return null;
		}
	}

	private Date parseDateTimeForDb(String dateValue, String timeValue) {
		if (dateValue == null || dateValue.isBlank() || timeValue == null || timeValue.isBlank()) {
			return null;
		}
		DateTime localDateTime = DateTime.parse(dateValue + " " + timeValue, DateTimeFormat.forPattern(DateTimeUtils.ELECTION_DATE_TIME_FORMAT));
		return localDateTime.minusHours(election.getDiffUTC()).toDate();
	}

	private DateTime parseLocalDateTime(String dateValue, String timeValue) {
		if (dateValue == null || dateValue.isBlank() || timeValue == null || timeValue.isBlank()) {
			return null;
		}
		try {
			return DateTime.parse(dateValue + " " + timeValue, DateTimeFormat.forPattern(DateTimeUtils.ELECTION_DATE_TIME_FORMAT));
		} catch (Exception e) {
			return null;
		}
	}

	private void buildCalendarMonth() {
		Locale locale = resolveLocale();
		DateTime monthStart = calendarMonth.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
		DateTime monthEnd = calendarMonth.dayOfMonth().withMaximumValue().withTimeAtStartOfDay();
		monthTitle = monthStart.toString("MMMM yyyy", locale).toUpperCase(locale);
		weekHeaders = new ArrayList<>();
		DateTime headerSeed = monthStart.dayOfWeek().withMinimumValue().minusDays(1);
		for (int i = 0; i < 7; i++) {
			weekHeaders.add(headerSeed.plusDays(i).toString("EEE", locale));
		}

		weeks = new ArrayList<>();
		int startOffset = monthStart.getDayOfWeek() % 7;
		DateTime gridStart = monthStart.minusDays(startOffset);
		DateTime minEndWithTwoNextWeeks = monthEnd.plusDays(14);
		int endOffset = 6 - (minEndWithTwoNextWeeks.getDayOfWeek() % 7);
		DateTime gridEnd = minEndWithTwoNextWeeks.plusDays(endOffset);
		int totalDays = Days.daysBetween(gridStart, gridEnd).getDays() + 1;
		int totalWeeks = Math.max(1, totalDays / 7);

		for (int week = 0; week < totalWeeks; week++) {
			CalendarWeekRow row = new CalendarWeekRow();
			DateTime weekStart = gridStart.plusDays(week * 7).withTimeAtStartOfDay();
			for (int day = 0; day < 7; day++) {
				DateTime current = weekStart.plusDays(day);
				CalendarDay cell = new CalendarDay();
				cell.setDayNumber(String.valueOf(current.getDayOfMonth()));
				cell.setCurrentMonth(current.getMonthOfYear() == monthStart.getMonthOfYear()
					&& current.getYear() == monthStart.getYear());
					row.getDays().add(cell);
				}
				List<CalendarWeekEvent> weekEvents = buildWeekEvents(weekStart);
				row.setEvents(weekEvents);
				row.setEventRows(Math.max(3, resolveWeekEventRows(weekEvents)));
				weeks.add(row);
			}
		}

	private boolean canGoPrevMonth() {
		return true;
	}

	private boolean canGoNextMonth() {
		return true;
	}

	private PageParameters buildCalendarPageParameters(DateTime targetMonth) {
		PageParameters parameters = UtilsParameters.getId(election.getElectionId());
		parameters.add(MONTH_PARAM, targetMonth.toString(MONTH_PARAM_PATTERN));
		return parameters;
	}

	private List<CalendarWeekEvent> buildWeekEvents(DateTime weekStart) {
		DateTime weekEnd = weekStart.plusDays(6).withTimeAtStartOfDay();
		List<CalendarWeekEvent> events = new ArrayList<>();
		for (int i = 0; i < stages.size(); i++) {
			CalendarStage stage = stages.get(i);
			if (stage.isUndefinedDate()) {
				continue;
			}
			DateTime start = parseDateOnly(stage.getStartDate());
			if (start == null) {
				continue;
			}
			DateTime startDay = start.withTimeAtStartOfDay();
			DateTime endDay = startDay;
			if (stage.isPeriod()) {
				DateTime end = parseDateOnly(stage.getEndDate());
				if (end != null && !end.withTimeAtStartOfDay().isBefore(startDay)) {
					endDay = end.withTimeAtStartOfDay();
				}
			}
			if (endDay.isBefore(weekStart) || startDay.isAfter(weekEnd)) {
				continue;
			}

			DateTime segmentStart = startDay.isBefore(weekStart) ? weekStart : startDay;
			DateTime segmentEnd = endDay.isAfter(weekEnd) ? weekEnd : endDay;
			int startColumn = Days.daysBetween(weekStart, segmentStart).getDays() + 1;
			int endColumnExclusive = Days.daysBetween(weekStart, segmentEnd).getDays() + 2;

			CalendarWeekEvent event = new CalendarWeekEvent();
			event.setText(stage.getTitle());
			event.setStageKey(stage.getKey());
			event.setStyle(stage.getCalendarEventStyle() != null ? stage.getCalendarEventStyle() : resolveCalendarEventStyle(i));
			event.setStartColumn(startColumn);
			event.setEndColumnExclusive(endColumnExclusive);
			event.setGridColumn(startColumn + " / " + endColumnExclusive);
			events.add(event);
		}
		return events;
	}

	private int resolveWeekEventRows(List<CalendarWeekEvent> weekEvents) {
		if (weekEvents == null || weekEvents.isEmpty()) {
			return 1;
		}

		List<CalendarWeekEvent> sorted = new ArrayList<>(weekEvents);
		sorted.sort((left, right) -> {
			int byStart = Integer.compare(left.getStartColumn(), right.getStartColumn());
			if (byStart != 0) {
				return byStart;
			}
			return Integer.compare(left.getEndColumnExclusive(), right.getEndColumnExclusive());
		});

		List<Integer> rowEndColumns = new ArrayList<>();
		for (CalendarWeekEvent event : sorted) {
			int rowIndex = -1;
			for (int i = 0; i < rowEndColumns.size(); i++) {
				if (event.getStartColumn() >= rowEndColumns.get(i)) {
					rowIndex = i;
					break;
				}
			}
			if (rowIndex == -1) {
				rowEndColumns.add(event.getEndColumnExclusive());
				event.setGridRow(rowEndColumns.size());
			} else {
				rowEndColumns.set(rowIndex, event.getEndColumnExclusive());
				event.setGridRow(rowIndex + 1);
			}
		}

		return rowEndColumns.size();
	}

	private void syncUndefinedDateState(
		CalendarStage row,
		TextField<String> startDate,
		TextField<String> startTime,
		TextField<String> endDate,
		TextField<String> endTime,
		CheckBox publicable) {

		boolean undefinedDateSelected = row.isAllowUndefinedDate() && row.isUndefinedDate();
		if (undefinedDateSelected && row.isPublicable()) {
			row.setPublicable(false);
		}

		startDate.setEnabled(!undefinedDateSelected);
		startTime.setEnabled(!undefinedDateSelected);
		endDate.setEnabled(row.isPeriod() && !undefinedDateSelected);
		endTime.setEnabled(row.isPeriod() && !undefinedDateSelected);
		publicable.setEnabled(!undefinedDateSelected);
	}

	private String resolveStageBackgroundColor(int index) {
		if (index < STAGE_BACKGROUND_COLORS.length) {
			return STAGE_BACKGROUND_COLORS[index];
		}
		int hue = (index * 137) % 360;
		return "hsl(" + hue + " 78% 84%)";
	}

	private String resolveCalendarEventStyle(int index) {
		return "background: " + resolveStageBackgroundColor(index) + "; color: #2f3d4f; border: 1px solid rgba(31,45,61,.24);";
	}

	private Locale resolveLocale() {
		String language = SecurityUtils.getLocale().getLanguage();
		return Locale.forLanguageTag(language == null ? "es" : language.toLowerCase());
	}

	private final class CalendarModuleForm extends Form<Void> {
		private static final long serialVersionUID = 4964716146728865817L;

			CalendarModuleForm(String id) {
				super(id);
				setOutputMarkupId(true);

				add(new Label("calendarMonthTitle", new PropertyModel<>(ElectionCalendarModuleDashboard.this, "monthTitle")));
				add(new Link<Void>("prevMonth") {
					private static final long serialVersionUID = -4795678768052822108L;

					@Override
					public void onClick() {
						setResponsePage(ElectionCalendarModuleDashboard.class, buildCalendarPageParameters(calendarMonth.minusMonths(1)));
					}
				}.setEnabled(canGoPrevMonth()));
				add(new Link<Void>("nextMonth") {
					private static final long serialVersionUID = -7457902171737380542L;

					@Override
					public void onClick() {
						setResponsePage(ElectionCalendarModuleDashboard.class, buildCalendarPageParameters(calendarMonth.plusMonths(1)));
					}
				}.setEnabled(canGoNextMonth()));

			add(new ListView<String>("calendarWeekHeaders", weekHeaders) {
				private static final long serialVersionUID = -6402264091213064345L;

				@Override
				protected void populateItem(ListItem<String> item) {
					item.add(new Label("weekHeader", item.getModelObject()));
				}
			});

			add(new ListView<CalendarWeekRow>("calendarWeeks", weeks) {
				private static final long serialVersionUID = -7872235934177706082L;

				@Override
				protected void populateItem(ListItem<CalendarWeekRow> weekItem) {
					weekItem.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_STYLE, "--calendar-event-rows: " + weekItem.getModelObject().getEventRows() + ";"));
					weekItem.add(new ListView<CalendarDay>("days", weekItem.getModelObject().getDays()) {
						private static final long serialVersionUID = 8602967727132795075L;

						@Override
						protected void populateItem(ListItem<CalendarDay> dayItem) {
							CalendarDay day = dayItem.getModelObject();
							dayItem.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  day.isCurrentMonth() ? "calendar-day-current" : "calendar-day-muted"));
							dayItem.add(new Label("dayNumber", day.getDayNumber()));
						}
					});
					weekItem.add(new ListView<CalendarWeekEvent>("weekEvents", weekItem.getModelObject().getEvents()) {
						private static final long serialVersionUID = -8083103586027709969L;

						@Override
					protected void populateItem(ListItem<CalendarWeekEvent> eventItem) {
						CalendarWeekEvent event = eventItem.getModelObject();
						eventItem.add(AttributeModifier.replace(
							MarkupLiterals.HTML_ATTRIBUTE_STYLE,
							"grid-column: " + event.getGridColumn() + "; grid-row: " + event.getGridRow() + "; " + (event.getStyle() != null ? event.getStyle() : "")));
						eventItem.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_DATA_STAGE_KEY, event.getStageKey()));
						eventItem.add(AttributeModifier.replace("title", event.getText()));
						eventItem.add(AttributeModifier.replace("data-bs-toggle", "tooltip"));
						eventItem.add(AttributeModifier.replace("data-bs-trigger", "hover focus click"));
						eventItem.add(AttributeModifier.replace("data-bs-placement", "top"));
						eventItem.add(AttributeModifier.replace("tabindex", "0"));
						eventItem.add(new Label("weekEventText", event.getText()));
					}
				});
				}
			});

			add(new ListView<CalendarStage>("calendarRows", stages) {
				private static final long serialVersionUID = 4855965171597425245L;

					@Override
					protected void populateItem(ListItem<CalendarStage> item) {
						CalendarStage row = item.getModelObject();
						item.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_DATA_STAGE_KEY, row.getKey()));
						WebMarkupContainer stageCard = new WebMarkupContainer("stageCard");
						stageCard.setOutputMarkupId(true);
						stageCard.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  new PropertyModel<>(row, "stageCardClass")));
						stageCard.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_DATA_STAGE_KEY, row.getKey()));
						stageCard.add(AttributeModifier.replace("data-period", String.valueOf(row.isPeriod())));
						stageCard.add(AttributeModifier.replace("data-no-date-label", row.getNoDateLabel()));
						item.add(stageCard);

						WebMarkupContainer eventColorDot = new WebMarkupContainer("eventColorDot");
						eventColorDot.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_STYLE, row.getCalendarDotStyle()));
						stageCard.add(eventColorDot);
						stageCard.add(new Label("eventName", row.getTitle()));
						stageCard.add(new Label("eventDateSummary", new PropertyModel<>(row, "summary")));
						stageCard.add(new Label("description", row.getDescription()));

					WebMarkupContainer connector = new WebMarkupContainer("timelineConnector");
					connector.setVisible(item.getIndex() < (stages.size() - 1));
					item.add(connector);

					WebMarkupContainer statusDot = new WebMarkupContainer("statusDot");
					statusDot.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getDotClass()));
					item.add(statusDot);
					WebMarkupContainer statusIcon = new WebMarkupContainer("statusIcon");
					statusIcon.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getIconClass()));
					statusDot.add(statusIcon);

					TextField<String> startDate = new TextField<>(FIELD_START_DATE, new PropertyModel<>(row, FIELD_START_DATE));
					startDate.setRequired(false);
					startDate.setEnabled(!row.isUndefinedDate());
					startDate.add(AttributeModifier.replace("data-default-date", new PropertyModel<>(row, FIELD_START_DATE)));
					stageCard.add(startDate);

					TextField<String> startTime = new TextField<>(FIELD_START_TIME, new PropertyModel<>(row, FIELD_START_TIME));
					startTime.setRequired(false);
					startTime.setEnabled(!row.isUndefinedDate());
					startTime.add(AttributeModifier.replace("data-default-time", new PropertyModel<>(row, FIELD_START_TIME)));
					stageCard.add(startTime);

					WebMarkupContainer periodFields = new WebMarkupContainer("periodFields");
					periodFields.setVisible(row.isPeriod());
					stageCard.add(periodFields);

					TextField<String> endDate = new TextField<>(FIELD_END_DATE, new PropertyModel<>(row, FIELD_END_DATE));
					endDate.setRequired(false);
					endDate.setEnabled(row.isPeriod() && !row.isUndefinedDate());
					endDate.add(AttributeModifier.replace("data-default-date", new PropertyModel<>(row, FIELD_END_DATE)));
					periodFields.add(endDate);

						TextField<String> endTime = new TextField<>(FIELD_END_TIME, new PropertyModel<>(row, FIELD_END_TIME));
						endTime.setRequired(false);
						endTime.add(AttributeModifier.replace("data-default-time", new PropertyModel<>(row, FIELD_END_TIME)));
						periodFields.add(endTime);

					WebMarkupContainer undefinedDateGroup = new WebMarkupContainer("undefinedDateGroup");
					undefinedDateGroup.setVisible(row.isAllowUndefinedDate());
					stageCard.add(undefinedDateGroup);

						CheckBox publicable = new CheckBox("publicable", new PropertyModel<>(row, "publicable"));
						syncUndefinedDateState(row, startDate, startTime, endDate, endTime, publicable);

						CheckBox undefinedDate = new CheckBox("undefinedDate", new PropertyModel<>(row, "undefinedDate"));
						undefinedDateGroup.add(undefinedDate);

						stageCard.add(publicable);
						Label stageCode = new Label("stageCodeText", row.getStageCode() == null ? "" : row.getStageCode());
						stageCode.setVisible(row.getStageCode() != null && !row.getStageCode().isBlank());
						stageCard.add(stageCode);
						Label status = new Label("statusText", row.getStatusLabel() == null ? "" : row.getStatusLabel());
						if (row.getStatusBadgeClass() != null && !row.getStatusBadgeClass().isBlank()) {
							status.add(AttributeModifier.append(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getStatusBadgeClass()));
					}
					status.setVisible(row.isShowStatus());
					stageCard.add(status);
				}
			});

			add(new Link<Void>("back") {
				private static final long serialVersionUID = 2354777761133469665L;

				@Override
				public void onClick() {
					setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
				}
			});

				add(new Button("saveTimeline") {
				private static final long serialVersionUID = 8277308158330172702L;

				@Override
					public void onSubmit() {
						if (!persistCalendars(false)) {
							return;
						}
						getSession().info(getString("electionCalendarSaved"));
						setResponsePage(ElectionCalendarModuleDashboard.class, buildCalendarPageParameters(calendarMonth));
					}
				});

			add(new Link<Void>("skip") {
				private static final long serialVersionUID = -3400655290544688440L;

				@Override
				public void onClick() {
					setResponsePage(ElectionTasksDashboard.class, UtilsParameters.getId(election.getElectionId()));
				}
			});

			add(new Button("markDoneNext") {
				private static final long serialVersionUID = -5015089624740759295L;

				@Override
				public void onSubmit() {
					if (!persistCalendars(true)) {
						return;
					}
					setResponsePage(ElectionTasksDashboard.class, UtilsParameters.getId(election.getElectionId()));
				}
			});
		}

		private boolean persistCalendars(boolean markCompleted) {
			try {
				List<ElectionCalendar> toSave = new ArrayList<>();
					for (CalendarStage row : stages) {
						if (!row.isAllowUndefinedDate()) {
							row.setUndefinedDate(false);
						}
						if (row.isUndefinedDate() && row.isAllowUndefinedDate()) {
							row.setPublicable(false);
						}
						ElectionCalendar calendar = new ElectionCalendar();
						calendar.setCalendarKey(ElectionCalendarKey.valueOf(row.getKey()));
						calendar.setPublicable(row.isPublicable());

						if (row.isUndefinedDate() && row.isAllowUndefinedDate()) {
							calendar.setStartDate(null);
							calendar.setEndDate(null);
							toSave.add(calendar);
							continue;
						}

						Date start = parseDateTimeForDb(row.getStartDate(), row.getStartTime());
						if (start == null) {
							error(new StringResourceModel("electionCalendarValidationStartRequired", ElectionCalendarModuleDashboard.this).setParameters(row.getTitle()).getString());
							return false;
						}

						Date end = null;
						if (row.isPeriod()) {
							end = parseDateTimeForDb(row.getEndDate(), row.getEndTime());
							if (end == null) {
								error(new StringResourceModel("electionCalendarValidationEndRequired", ElectionCalendarModuleDashboard.this).setParameters(row.getTitle()).getString());
								return false;
							}
							if (end.before(start)) {
								error(new StringResourceModel("electionCalendarValidationEndAfterStart", ElectionCalendarModuleDashboard.this).setParameters(row.getTitle()).getString());
								return false;
							}
						}

					calendar.setStartDate(start);
					calendar.setEndDate(end);
					toSave.add(calendar);
				}

				AppContext.getInstance().getManagerBeanRemote().saveElectionCalendars(
					election.getElectionId(),
					toSave,
					election.getTitleSpanish(),
					SecurityUtils.getUserAdminId(),
					SecurityUtils.getClientIp());

				if (markCompleted) {
					AppContext.getInstance().getManagerBeanRemote().persistElectionCalendarSet(
						election.getElectionId(),
						election.getTitleSpanish(),
						SecurityUtils.getUserAdminId(),
						SecurityUtils.getClientIp());
				}
				return true;
			} catch (Exception e) {
				error(getString("electionCalendarSaveError"));
				return false;
			}
		}
	}

	public static class CalendarStage implements Serializable {
		private static final long serialVersionUID = -2513078758495056146L;

		private String key;
		private String stageCode;
		private String title;
		private String description;
		private boolean period;
		private String startDate;
		private String startTime;
		private String endDate;
		private String endTime;
		private boolean publicable;
		private boolean undefinedDate;
		private boolean allowUndefinedDate;
		private String statusLabel;
		private String statusBadgeClass;
		private boolean showStatus;
		private String dotClass;
		private String iconClass;
		private String calendarEventStyle;
		private String calendarDotStyle;
		private String noDateLabel;

		public String buildSummary() {
			if (undefinedDate || startDate == null || startDate.isBlank()) {
				return noDateLabel != null ? noDateLabel : "Sin fecha configurada";
			}
			String start = startDate + ((startTime != null && !startTime.isBlank()) ? " " + startTime : "");
			if (period && endDate != null && !endDate.isBlank()) {
				String end = endDate + ((endTime != null && !endTime.isBlank()) ? " " + endTime : "");
				return start + " - " + end;
			}
			return start;
		}

		public String getSummary() {
			return buildSummary();
		}

		public String getStageCardClass() {
			if (undefinedDate) {
				return "calendar-stage-card calendar-stage-disabled calendar-stage-no-date";
			}
			return "calendar-stage-card";
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getTitle() {
			return title;
		}

		public String getStageCode() {
			return stageCode;
		}

		public void setStageCode(String stageCode) {
			this.stageCode = stageCode;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public boolean isPeriod() {
			return period;
		}

		public void setPeriod(boolean period) {
			this.period = period;
		}

		public String getStartDate() {
			return startDate;
		}

		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		public String getStartTime() {
			return startTime;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}

		public String getEndDate() {
			return endDate;
		}

		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}

		public String getEndTime() {
			return endTime;
		}

		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}

		public boolean isPublicable() {
			return publicable;
		}

		public void setPublicable(boolean publicable) {
			this.publicable = publicable;
		}

		public boolean isUndefinedDate() {
			return undefinedDate;
		}

		public void setUndefinedDate(boolean undefinedDate) {
			this.undefinedDate = undefinedDate;
		}

		public boolean isAllowUndefinedDate() {
			return allowUndefinedDate;
		}

		public void setAllowUndefinedDate(boolean allowUndefinedDate) {
			this.allowUndefinedDate = allowUndefinedDate;
		}

		public String getStatusLabel() {
			return statusLabel;
		}

		public void setStatusLabel(String statusLabel) {
			this.statusLabel = statusLabel;
		}

		public String getStatusBadgeClass() {
			return statusBadgeClass;
		}

		public void setStatusBadgeClass(String statusBadgeClass) {
			this.statusBadgeClass = statusBadgeClass;
		}

		public boolean isShowStatus() {
			return showStatus;
		}

		public void setShowStatus(boolean showStatus) {
			this.showStatus = showStatus;
		}

		public String getDotClass() {
			return dotClass;
		}

		public void setDotClass(String dotClass) {
			this.dotClass = dotClass;
		}

		public String getIconClass() {
			return iconClass;
		}

		public void setIconClass(String iconClass) {
			this.iconClass = iconClass;
		}

		public String getCalendarEventStyle() {
			return calendarEventStyle;
		}

		public void setCalendarEventStyle(String calendarEventStyle) {
			this.calendarEventStyle = calendarEventStyle;
		}

		public String getCalendarDotStyle() {
			return calendarDotStyle;
		}

		public void setCalendarDotStyle(String calendarDotStyle) {
			this.calendarDotStyle = calendarDotStyle;
		}

		public String getNoDateLabel() {
			return noDateLabel;
		}

		public void setNoDateLabel(String noDateLabel) {
			this.noDateLabel = noDateLabel;
		}
	}

	public static class CalendarWeekRow implements Serializable {
		private static final long serialVersionUID = 6909631493348387714L;
		private List<CalendarDay> days = new ArrayList<>();
		private List<CalendarWeekEvent> events = new ArrayList<>();
		private int eventRows = 3;

		public List<CalendarDay> getDays() {
			return days;
		}

		public List<CalendarWeekEvent> getEvents() {
			return events;
		}

		public void setEvents(List<CalendarWeekEvent> events) {
			this.events = events;
		}

		public int getEventRows() {
			return eventRows;
		}

		public void setEventRows(int eventRows) {
			this.eventRows = eventRows;
		}
	}

	public static class CalendarDay implements Serializable {
		private static final long serialVersionUID = 8974060184609070761L;
		private String dayNumber;
		private boolean currentMonth;

		public String getDayNumber() {
			return dayNumber;
		}

		public void setDayNumber(String dayNumber) {
			this.dayNumber = dayNumber;
		}

		public boolean isCurrentMonth() {
			return currentMonth;
		}

		public void setCurrentMonth(boolean currentMonth) {
			this.currentMonth = currentMonth;
		}
	}

	public static class CalendarWeekEvent implements Serializable {
		private static final long serialVersionUID = -761883490364715068L;
		private String text;
		private String stageKey;
		private String style;
		private String gridColumn;
		private int gridRow = 1;
		private int startColumn;
		private int endColumnExclusive;

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getStageKey() {
			return stageKey;
		}

		public void setStageKey(String stageKey) {
			this.stageKey = stageKey;
		}

		public String getStyle() {
			return style;
		}

		public void setStyle(String style) {
			this.style = style;
		}

		public String getGridColumn() {
			return gridColumn;
		}

		public void setGridColumn(String gridColumn) {
			this.gridColumn = gridColumn;
		}

		public int getGridRow() {
			return gridRow;
		}

		public void setGridRow(int gridRow) {
			this.gridRow = gridRow;
		}

		public int getStartColumn() {
			return startColumn;
		}

		public void setStartColumn(int startColumn) {
			this.startColumn = startColumn;
		}

		public int getEndColumnExclusive() {
			return endColumnExclusive;
		}

		public void setEndColumnExclusive(int endColumnExclusive) {
			this.endColumnExclusive = endColumnExclusive;
		}
	}
}
