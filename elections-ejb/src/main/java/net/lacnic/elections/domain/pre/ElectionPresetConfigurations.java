package net.lacnic.elections.domain.pre;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import net.lacnic.elections.domain.ElectionType;

public final class ElectionPresetConfigurations {

	private static final int CALENDAR_DEFAULT_HOUR_UTC = 18;
	private static final ElectionCalendarKey DEFAULT_TASKS_STAGE = ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES;
	private static final ElectionCalendarKey EVALUATION_TASK_STAGE = ElectionCalendarKey.N_6_PERIODO_ADDITIONAL_EVALUATION;
	private static final Set<ElectionTaskKey> DEFAULT_PUBLIC_TASK_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			ElectionTaskKey.PROFILE,
			ElectionTaskKey.COUNTRIES,
			ElectionTaskKey.ORG_SUPPORTS,
			ElectionTaskKey.USER_SUPPORTS_2,
			ElectionTaskKey.USER_SUPPORTS_5,
			ElectionTaskKey.ORGANIZATIONS,
			ElectionTaskKey.OTHER_STATUTORY_QUESTIONS,
			ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS)));
	private static final Set<ElectionCalendarKey> DEFAULT_PUBLIC_CALENDAR_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
			ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
			ElectionCalendarKey.N_4_SINGLE_PADRON_PUBLISHED,
			ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED,
			ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS,
			ElectionCalendarKey.N_16_PERIODO_VOTING,
			ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED,
			ElectionCalendarKey.N_18_PERIODO_CE_AUDIT)));

	private static final Set<ElectionCalendarKey> BOARD_CALENDARS_WITH_DEFAULT_DATE = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
			ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
			ElectionCalendarKey.N_4_SINGLE_PADRON_PUBLISHED,
			ElectionCalendarKey.N_6_PERIODO_ADDITIONAL_EVALUATION,
			ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION,
			ElectionCalendarKey.N_8_PERIODO_EVALUATIONS_VALIDATION,
			ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED,
			ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS,
			ElectionCalendarKey.N_16_PERIODO_VOTING,
			ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED,
			ElectionCalendarKey.N_18_PERIODO_CE_AUDIT,
			ElectionCalendarKey.N_23_PERIODO_PADRON_MILACNIC_SYNC)));
	private static final Set<ElectionCalendarKey> COMMISSION_LIKE_CALENDARS_WITH_DEFAULT_DATE = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
			ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
			ElectionCalendarKey.N_4_SINGLE_PADRON_PUBLISHED,
			ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION,
			ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED,
			ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS,
			ElectionCalendarKey.N_16_PERIODO_VOTING,
			ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED,
			ElectionCalendarKey.N_18_PERIODO_CE_AUDIT,
			ElectionCalendarKey.N_23_PERIODO_PADRON_MILACNIC_SYNC)));

	private static final List<ElectionTaskKey> BOARD_TASKS = Collections.unmodifiableList(Arrays.asList(
			ElectionTaskKey.PROFILE,
			ElectionTaskKey.COUNTRIES,
			ElectionTaskKey.INCOMPATIBILITIES,
			ElectionTaskKey.COURSE,
			ElectionTaskKey.ORG_SUPPORTS,
			ElectionTaskKey.ORGANIZATIONS,
			ElectionTaskKey.OTHER_STATUTORY_QUESTIONS,
			ElectionTaskKey.DECLARATIONS,
			ElectionTaskKey.EVALUATION));
	private static final List<ElectionTaskKey> ELECTORAL_COMMISSION_TASKS = Collections.unmodifiableList(Arrays.asList(
			ElectionTaskKey.PROFILE,
			ElectionTaskKey.COUNTRIES,
			ElectionTaskKey.INCOMPATIBILITIES,
			ElectionTaskKey.ORG_SUPPORTS,
			ElectionTaskKey.OTHER_STATUTORY_QUESTIONS,
			ElectionTaskKey.ORGANIZATIONS,
			ElectionTaskKey.DECLARATIONS));
	private static final List<ElectionTaskKey> FISCAL_COMMISSION_TASKS = Collections.unmodifiableList(Arrays.asList(
			ElectionTaskKey.PROFILE,
			ElectionTaskKey.COUNTRIES,
			ElectionTaskKey.INCOMPATIBILITIES,
			ElectionTaskKey.ORG_SUPPORTS,
			ElectionTaskKey.OTHER_STATUTORY_QUESTIONS,
			ElectionTaskKey.ORGANIZATIONS,
			ElectionTaskKey.DECLARATIONS));
	private static final List<ElectionTaskKey> MODERATORS_TASKS = Collections.unmodifiableList(Arrays.asList(
			ElectionTaskKey.PROFILE,
			ElectionTaskKey.COUNTRIES,
			ElectionTaskKey.INCOMPATIBILITIES,
			ElectionTaskKey.USER_SUPPORTS_2,
			ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS,
			ElectionTaskKey.ORGANIZATIONS,
			ElectionTaskKey.DECLARATIONS_NON_STATUTORY));
	private static final List<ElectionTaskKey> IANA_TASKS = Collections.unmodifiableList(Arrays.asList(
			ElectionTaskKey.PROFILE,
			ElectionTaskKey.COUNTRIES,
			ElectionTaskKey.INCOMPATIBILITIES,
			ElectionTaskKey.USER_SUPPORTS_2,
			ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS,
			ElectionTaskKey.ORGANIZATIONS,
			ElectionTaskKey.DECLARATIONS_NON_STATUTORY));
	private static final List<ElectionTaskKey> ASO_TASKS = Collections.unmodifiableList(Arrays.asList(
			ElectionTaskKey.PROFILE,
			ElectionTaskKey.COUNTRIES,
			ElectionTaskKey.INCOMPATIBILITIES,
			ElectionTaskKey.USER_SUPPORTS_5,
			ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS,
			ElectionTaskKey.ORGANIZATIONS,
			ElectionTaskKey.DECLARATIONS_NON_STATUTORY));
	private static final List<ElectionTaskKey> OTHER_TASKS = Collections.unmodifiableList(Arrays.asList(
			ElectionTaskKey.PROFILE,
			ElectionTaskKey.COUNTRIES,
			ElectionTaskKey.INCOMPATIBILITIES,
			ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS,
			ElectionTaskKey.ORGANIZATIONS,
			ElectionTaskKey.DECLARATIONS));

	private static final ElectionPresetConfiguration BOARD = new ElectionPresetConfiguration(ElectionType.BOARD, true, true, BOARD_TASKS, BOARD_CALENDARS_WITH_DEFAULT_DATE);
	private static final ElectionPresetConfiguration ELECTORAL_COMMISSION = new ElectionPresetConfiguration(ElectionType.ELECTORAL_COMMISSION, true, true, ELECTORAL_COMMISSION_TASKS,
			COMMISSION_LIKE_CALENDARS_WITH_DEFAULT_DATE);
	private static final ElectionPresetConfiguration FISCAL_COMMISSION = new ElectionPresetConfiguration(ElectionType.FISCAL_COMMISSION, true, true, FISCAL_COMMISSION_TASKS,
			COMMISSION_LIKE_CALENDARS_WITH_DEFAULT_DATE);
	private static final ElectionPresetConfiguration MODERATORS = new ElectionPresetConfiguration(ElectionType.MODERATORS, true, true, MODERATORS_TASKS, COMMISSION_LIKE_CALENDARS_WITH_DEFAULT_DATE);
	private static final ElectionPresetConfiguration IANA = new ElectionPresetConfiguration(ElectionType.IANA, true, true, IANA_TASKS, COMMISSION_LIKE_CALENDARS_WITH_DEFAULT_DATE);
	private static final ElectionPresetConfiguration ASO = new ElectionPresetConfiguration(ElectionType.ASO, true, true, ASO_TASKS, COMMISSION_LIKE_CALENDARS_WITH_DEFAULT_DATE);
	private static final ElectionPresetConfiguration OTHER = new ElectionPresetConfiguration(ElectionType.OTHER, true, true, OTHER_TASKS, COMMISSION_LIKE_CALENDARS_WITH_DEFAULT_DATE);

	private ElectionPresetConfigurations() {
	}

	public static ElectionPresetConfiguration get(ElectionType electionType) {
		if (electionType == null) {
			return null;
		}
		switch (electionType) {
		case BOARD:
			return BOARD;
		case ELECTORAL_COMMISSION:
			return ELECTORAL_COMMISSION;
		case FISCAL_COMMISSION:
			return FISCAL_COMMISSION;
		case MODERATORS:
			return MODERATORS;
		case IANA:
			return IANA;
		case ASO:
			return ASO;
		case OTHER:
			return OTHER;
		default:
			return null;
		}
	}

	public static Date buildDefaultCalendarDateUtc() {
		DateTime nowUtc = DateTime.now(DateTimeZone.UTC);
		return nowUtc.plusDays(2).withTime(CALENDAR_DEFAULT_HOUR_UTC, 0, 0, 0).toDate();
	}

	public static ElectionCalendarKey resolveTaskStage(ElectionTaskKey taskKey) {
		return taskKey == ElectionTaskKey.EVALUATION ? EVALUATION_TASK_STAGE : DEFAULT_TASKS_STAGE;
	}

	public static boolean isDefaultPublicCalendar(ElectionCalendarKey calendarKey) {
		return calendarKey != null && DEFAULT_PUBLIC_CALENDAR_KEYS.contains(calendarKey);
	}

	public static boolean isDefaultPublicTask(ElectionTaskKey taskKey) {
		return taskKey != null && DEFAULT_PUBLIC_TASK_KEYS.contains(taskKey);
	}

	/**
	 * Campus tasks are only part of a new election preset when the Campus
	 * integration is configured. This keeps the preset consistent with the
	 * runtime feature flag instead of creating unusable tasks that must later be
	 * hidden from the administration screen.
	 */
	public static boolean isPresetTaskEnabled(ElectionTaskKey taskKey, boolean campusConfigured) {
		if (taskKey == ElectionTaskKey.COURSE || taskKey == ElectionTaskKey.EVALUATION) {
			return campusConfigured;
		}
		return true;
	}

	public static final class ElectionPresetConfiguration {
		private final ElectionType electionType;
		private final boolean manageOrganizationsManual;
		private final boolean manageVotersManual;
		private final List<ElectionTaskKey> presetTasks;
		private final Set<ElectionCalendarKey> calendarsWithDefaultDate;

		private ElectionPresetConfiguration(ElectionType electionType, boolean manageOrganizationsManual, boolean manageVotersManual, List<ElectionTaskKey> presetTasks,
				Set<ElectionCalendarKey> calendarsWithDefaultDate) {
			this.electionType = electionType;
			this.manageOrganizationsManual = manageOrganizationsManual;
			this.manageVotersManual = manageVotersManual;
			this.presetTasks = presetTasks;
			this.calendarsWithDefaultDate = calendarsWithDefaultDate;
		}

		public ElectionType getElectionType() {
			return electionType;
		}

		public boolean isManageOrganizationsManual() {
			return manageOrganizationsManual;
		}

		public boolean isManageVotersManual() {
			return manageVotersManual;
		}

		public List<ElectionTaskKey> getPresetTasks() {
			return presetTasks;
		}

		public Set<ElectionCalendarKey> getCalendarsWithDefaultDate() {
			return calendarsWithDefaultDate;
		}
	}
}
