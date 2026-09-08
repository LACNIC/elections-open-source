package net.lacnic.elections.publicelection;

public final class PublicElectionSectionKeys {

	public static final String LANDING_RESULTS = "landing.results";
	public static final String LANDING_CANDIDATES = "landing.candidates";
	public static final String LANDING_CALENDAR = "landing.calendar";
	public static final String LANDING_ROLL = "landing.roll";
	public static final String LANDING_PARTICIPATION_LINK_RECOVERY = "landing.participationLinkRecovery";
	public static final String LANDING_CANDIDATE_NOMINATION_SUMMARY = "landing.candidateNominationSummary";
	public static final String LANDING_PUBLIC_NOMINATION = "landing.publicNomination";
	public static final String CANDIDATE_PROFILE_ROOT = "candidate.profileRoot";
	public static final String CANDIDATE_PROFILE = "candidate.profile";
	public static final String CANDIDATE_STATUTORY_QUESTIONS = "candidate.statutoryQuestions";
	public static final String CANDIDATE_NON_STATUTORY_QUESTIONS = "candidate.nonStatutoryQuestions";
	public static final String CANDIDATE_QUESTION_SUBMISSION = "candidate.questionSubmission";
	public static final String CANDIDATE_COMMUNITY_QUESTIONS = "candidate.communityQuestions";
	public static final String CANDIDATE_PENDING_COMMUNITY_QUESTIONS = "candidate.pendingCommunityQuestions";
	public static final String CANDIDATE_SIDEBAR = "candidate.sidebar";

	private PublicElectionSectionKeys() {
		throw new IllegalStateException("Utility class");
	}
}
