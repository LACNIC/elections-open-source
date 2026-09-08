package net.lacnic.elections.publicelection;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.ElectionLinkRecoveryMode;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.services.publicelection.PublicElectionVisibilityReport;

public class PublicElectionVisibilityResolver {

	private static final String EXPLAIN_CALENDAR_N11_REACHED_PREFIX = "calendar.N11.reached=";

	private static final ElectionTaskKey[] CANDIDATE_SIDEBAR_TASKS = {
			ElectionTaskKey.COUNTRIES,
			ElectionTaskKey.ORG_SUPPORTS,
			ElectionTaskKey.USER_SUPPORTS_2,
			ElectionTaskKey.USER_SUPPORTS_5,
			ElectionTaskKey.ORGANIZATIONS,
			ElectionTaskKey.PROFILE
	};

	private final Election election;
	private final Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey;
	private final Set<ElectionTaskKey> publicTaskKeys;
	private final Date now;
	private final boolean publicNominationEnabled;

	public PublicElectionVisibilityResolver(
			Election election,
			Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey,
			Set<ElectionTaskKey> publicTaskKeys,
			Date now,
			boolean publicNominationEnabled) {
		this.election = election;
		this.calendarsByKey = calendarsByKey;
		this.publicTaskKeys = publicTaskKeys;
		this.now = now != null ? now : new Date();
		this.publicNominationEnabled = publicNominationEnabled;
	}

	public PublicElectionVisibilityReport resolve() {
		PublicElectionVisibilityReport report = new PublicElectionVisibilityReport();
		put(report, PublicElectionSectionKeys.LANDING_RESULTS, isLandingResultsVisible(), explainLandingResultsVisible());
		put(report, PublicElectionSectionKeys.LANDING_CANDIDATES, isLandingCandidatesVisible(), explainLandingCandidatesVisible());
		put(report, PublicElectionSectionKeys.LANDING_CALENDAR, isLandingCalendarVisible(), explainLandingCalendarVisible());
		put(report, PublicElectionSectionKeys.LANDING_ROLL, isLandingRollVisible(), explainLandingRollVisible());
		put(report, PublicElectionSectionKeys.LANDING_PARTICIPATION_LINK_RECOVERY, isParticipationLinkRecoveryVisible(), explainParticipationLinkRecoveryVisible());
		put(report, PublicElectionSectionKeys.LANDING_CANDIDATE_NOMINATION_SUMMARY, isCandidateNominationSummaryVisible(), explainCandidateNominationSummaryVisible());
		put(report, PublicElectionSectionKeys.LANDING_PUBLIC_NOMINATION, isPublicNominationVisible(), explainPublicNominationVisible());
		put(report, PublicElectionSectionKeys.CANDIDATE_PROFILE_ROOT, isCandidateProfileRootVisible(), explainCandidateProfileRootVisible());
		put(report, PublicElectionSectionKeys.CANDIDATE_PROFILE, isCandidateProfileVisible(), explainCandidateProfileVisible());
		put(report, PublicElectionSectionKeys.CANDIDATE_STATUTORY_QUESTIONS, isCandidateStatutoryQuestionsVisible(), explainCandidateStatutoryQuestionsVisible());
		put(report, PublicElectionSectionKeys.CANDIDATE_NON_STATUTORY_QUESTIONS, isCandidateNonStatutoryQuestionsVisible(), explainCandidateNonStatutoryQuestionsVisible());
		put(report, PublicElectionSectionKeys.CANDIDATE_QUESTION_SUBMISSION, isCandidateQuestionSubmissionVisible(), explainCandidateQuestionSubmissionVisible());
		put(report, PublicElectionSectionKeys.CANDIDATE_COMMUNITY_QUESTIONS, isCandidateCommunityQuestionsVisible(), explainCandidateCommunityQuestionsVisible());
		put(report, PublicElectionSectionKeys.CANDIDATE_PENDING_COMMUNITY_QUESTIONS, isCandidatePendingCommunityQuestionsVisible(), explainCandidatePendingCommunityQuestionsVisible());
		put(report, PublicElectionSectionKeys.CANDIDATE_SIDEBAR, isCandidateSidebarVisible(), explainCandidateSidebarVisible());
		return report;
	}

	public boolean isLandingResultsVisible() {
		return election != null
				&& election.isResultLinkAvailable()
				&& isCalendarReached(ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED);
	}

	public boolean isLandingCandidatesVisible() {
		return isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED);
	}

	public boolean isLandingCalendarVisible() {
		return isCalendarReached(ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED);
	}

	public boolean isLandingRollVisible() {
		return isStatutoryElection() && isCalendarReached(ElectionCalendarKey.N_4_SINGLE_PADRON_PUBLISHED);
	}

	public boolean isParticipationLinkRecoveryVisible() {
		return election != null
				&& !election.isClosed()
				&& election.getPublicLinkRecoveryMode() != null
				&& election.getPublicLinkRecoveryMode() != ElectionLinkRecoveryMode.NONE;
	}

	public boolean isCandidateNominationSummaryVisible() {
		return isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED);
	}

	public boolean isPublicNominationVisible() {
		return publicNominationEnabled
				&& isCalendarDuring(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
	}

	public boolean isCandidateProfileRootVisible() {
		return isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED);
	}

	public boolean isCandidateProfileVisible() {
		return isTaskPublic(ElectionTaskKey.PROFILE);
	}

	public boolean isCandidateStatutoryQuestionsVisible() {
		return isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED)
				&& isTaskPublic(ElectionTaskKey.OTHER_STATUTORY_QUESTIONS);
	}

	public boolean isCandidateNonStatutoryQuestionsVisible() {
		return isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED)
				&& isTaskPublic(ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS);
	}

	public boolean isCandidateQuestionSubmissionVisible() {
		return isCalendarDuring(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
	}

	public boolean isCandidateCommunityQuestionsVisible() {
		return isCalendarReached(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
	}

	public boolean isCandidatePendingCommunityQuestionsVisible() {
		return isCalendarDuring(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
	}

	public boolean isCandidateSidebarVisible() {
		if (!isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED)) {
			return false;
		}
		for (ElectionTaskKey taskKey : CANDIDATE_SIDEBAR_TASKS) {
			if (isTaskPublic(taskKey)) {
				return true;
			}
		}
		return false;
	}

	public String explainLandingResultsVisible() {
		return "resultLinkAvailable=" + (election != null && election.isResultLinkAvailable())
				+ ", calendar.N17.reached=" + isCalendarReached(ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED);
	}

	public String explainLandingCandidatesVisible() {
		return EXPLAIN_CALENDAR_N11_REACHED_PREFIX + isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED);
	}

	public String explainLandingCalendarVisible() {
		return "calendar.N1.reached=" + isCalendarReached(ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED);
	}

	public String explainLandingRollVisible() {
		return "statutoryElection=" + isStatutoryElection()
				+ ", calendar.N4.reached=" + isCalendarReached(ElectionCalendarKey.N_4_SINGLE_PADRON_PUBLISHED);
	}

	public String explainParticipationLinkRecoveryVisible() {
		return "closed=" + (election != null && election.isClosed())
				+ ", recoveryMode=" + resolveRecoveryModeCode();
	}

	public String explainCandidateNominationSummaryVisible() {
		return explainLandingCandidatesVisible();
	}

	public String explainPublicNominationVisible() {
		ElectionType electionType = election != null ? election.getEffectiveElectionType() : null;
		return "electionType=" + (electionType != null ? electionType.name() : null)
				+ ", parameter.PUBLIC_NOMINATION_ENABLED=" + publicNominationEnabled
				+ ", calendar.N2.during=" + isCalendarDuring(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
	}

	public String explainCandidateProfileRootVisible() {
		return explainLandingCandidatesVisible();
	}

	public String explainCandidateProfileVisible() {
		return "task.PROFILE.public=" + isTaskPublic(ElectionTaskKey.PROFILE);
	}

	public String explainCandidateStatutoryQuestionsVisible() {
		return EXPLAIN_CALENDAR_N11_REACHED_PREFIX + isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED)
				+ ", task.OTHER_STATUTORY_QUESTIONS.public=" + isTaskPublic(ElectionTaskKey.OTHER_STATUTORY_QUESTIONS);
	}

	public String explainCandidateNonStatutoryQuestionsVisible() {
		return EXPLAIN_CALENDAR_N11_REACHED_PREFIX + isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED)
				+ ", task.OTHER_NON_STATUTORY_QUESTIONS.public=" + isTaskPublic(ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS);
	}

	public String explainCandidateQuestionSubmissionVisible() {
		return "calendar.N12.during=" + isCalendarDuring(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
	}

	public String explainCandidateCommunityQuestionsVisible() {
		return "calendar.N12.reached=" + isCalendarReached(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
	}

	public String explainCandidatePendingCommunityQuestionsVisible() {
		return "calendar.N12.during=" + isCalendarDuring(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
	}

	public String explainCandidateSidebarVisible() {
		return EXPLAIN_CALENDAR_N11_REACHED_PREFIX + isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED)
				+ ", anySidebarTaskPublic=" + isCandidateSidebarVisible();
	}

	private void put(PublicElectionVisibilityReport report, String key, boolean visible, String reason) {
		if (report == null) {
			return;
		}
		report.getSections().put(key, Boolean.valueOf(visible));
		report.getReasons().put(key, reason);
	}

	private boolean isStatutoryElection() {
		return election != null && election.getCategory() == ElectionCategory.STATUTORY;
	}

	private boolean isTaskPublic(ElectionTaskKey taskKey) {
		return taskKey != null && publicTaskKeys.contains(taskKey);
	}

	private boolean isCalendarReached(ElectionCalendarKey key) {
		ElectionCalendar calendar = calendarsByKey.get(key);
		if (calendar == null || calendar.getStartDate() == null) {
			return false;
		}
		return !now.before(calendar.getStartDate());
	}

	private boolean isCalendarDuring(ElectionCalendarKey key) {
		ElectionCalendar calendar = calendarsByKey.get(key);
		if (calendar == null || calendar.getStartDate() == null) {
			return false;
		}
		Date effectiveEnd = resolveEffectiveCalendarEnd(calendar);
		return effectiveEnd != null && !now.before(calendar.getStartDate()) && !now.after(effectiveEnd);
	}

	private Date resolveEffectiveCalendarEnd(ElectionCalendar calendar) {
		if (calendar == null || calendar.getStartDate() == null) {
			return null;
		}
		if (calendar.getCalendarKey() != null && calendar.getCalendarKey().name().contains("_SINGLE_")) {
			return calendar.getStartDate();
		}
		return calendar.getEndDate() != null ? calendar.getEndDate() : calendar.getStartDate();
	}

	private String resolveRecoveryModeCode() {
		if (election == null || election.getPublicLinkRecoveryMode() == null) {
			return ElectionLinkRecoveryMode.NONE.name();
		}
		return election.getPublicLinkRecoveryMode().name();
	}
}
