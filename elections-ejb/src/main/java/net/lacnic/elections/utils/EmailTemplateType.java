package net.lacnic.elections.utils;

public enum EmailTemplateType {

	NEW("NEW", false),
	STANDARD_DISPATCH_NOTICE("STANDARD_DISPATCH_NOTICE", false, true, false),
	
	SIGNATURE("SIGNATURE", false, false, false),

	AUDITOR("AUDITOR", false),
	AUDITOR_SP_PT_EN("AUDITOR_SP_PT_EN", false),
	AUDITOR_NOMINATION_SP_PT_EN("AUDITOR_NOMINATION_SP_PT_EN", false),
	AUDITOR_AGREEMENT("AUDITOR_AGREEMENT", false, true, false),
	AUDITOR_REVISION("AUDITOR_REVISION", false, true, false),
	
	AUDITOR_CANDIDATE_APPROVED("AUDITOR_CANDIDATE_APPROVED", false, true, false),
	AUDITOR_CANDIDATE_REJECTED("AUDITOR_CANDIDATE_REJECTED", false, true, false),
	
	ELECTION_NOTICE("ELECTION_NOTICE", false),
	ELECTION_NOTICE_SP_PT_EN("ELECTION_NOTICE_SP_PT_EN", false),
	SYNC_ERROR_ALERT("SYNC_ERROR_ALERT", false, true, false),
	ELECTION_START("ELECTION_START", false),
	ELECTION_START_SP_PT_EN("ELECTION_START_SP_PT_EN", false),
	ELECTION_ABOUT_TO_END("ELECTION_ABOUT_TO_END", false),
	ELECTION_ABOUT_TO_END_SP_PT_EN("ELECTION_ABOUT_TO_END_SP_PT_EN", false),
	
	VOTE_RESULT("VOTE_RESULT", false),
	VOTE_RESULT_SP_PT_EN("VOTE_RESULT_SP_PT_EN", false),
	VOTE_CODES("VOTE_CODES", false, true, false),

	NOMINATION_SUBMITTED_NOMINEE("NOMINATION_SUBMITTED_NOMINEE", true, true, false),	
	NOMINATION_ORG_CANDIDATES_INVITATION("NOMINATION_ORG_CANDIDATES_INVITATION", false, false, true),
	NOMINATION_ORG_CANDIDATES_INVITATION_NOTA_NO_VOTA("NOMINATION_ORG_CANDIDATES_INVITATION_NOTA_NO_VOTA", false, false, true),
	
	NOMINATION_ORG_SUPPORT_REQUEST("NOMINATION_ORG_SUPPORT_REQUEST", true, true, false),
	NOMINATION_USER_SUPPORT_REQUEST("NOMINATION_USER_SUPPORT_REQUEST", true, true, false),
	NOMINATION_SUPPORT_STATUS_APPROVED_CANDIDATE("NOMINATION_SUPPORT_STATUS_APPROVED_CANDIDATE", true, true, false),
	NOMINATION_SUPPORT_STATUS_REJECTED_CANDIDATE("NOMINATION_SUPPORT_STATUS_REJECTED_CANDIDATE", true, true, false),
	
	NOMINATION_ACCEPTED_REPRESENTATIVE("NOMINATION_ACCEPTED_REPRESENTATIVE", true, true, false),
	NOMINATION_REJECTED_REPRESENTATIVE("NOMINATION_REJECTED_REPRESENTATIVE", true, true, false),
	
	SUPPORT_REMINDER("SUPPORT_REMINDER", true, true, false),
	SUPPORT_REQUESTER_REMINDER("SUPPORT_REQUESTER_REMINDER", true, true, false),
	NOMINATION_REMINDER("NOMINATION_REMINDER", false, true, false),
	CANDIDATE_REMINDER("CANDIDATE_REMINDER", true, true, false),
	AUDITOR_REMINDER("AUDITOR_REMINDER", true, true, false),
	AUDITOR_REMINDER_REVISION("AUDITOR_REMINDER_REVISION", true, true, false),
	LINK_RECOVERY("LINK_RECOVERY", false, true, false),
		
			CANDIDATE_CONFIRMED_AND_PUBLISHED("CANDIDATE_CONFIRMED_AND_PUBLISHED", true, true, false),
			CANDIDATE_QUESTION_STATUS_NOTIFICATION("CANDIDATE_QUESTION_STATUS_NOTIFICATION", false, true, false),
			CANDIDATE_TRAINING_ACCESS_REQUESTED_ADMIN("CANDIDATE_TRAINING_ACCESS_REQUESTED_ADMIN", true, true, false),
		CANDIDATE_EVALUATION_ACCESS_REQUESTED_ADMIN("CANDIDATE_EVALUATION_ACCESS_REQUESTED_ADMIN", false, true, false),
		CANDIDATE_TASK_COMPLETED_ADMIN("CANDIDATE_TASK_COMPLETED_ADMIN", false, true, false),
		CANDIDATE_ALL_TASKS_COMPLETED_ADMIN("CANDIDATE_ALL_TASKS_COMPLETED_ADMIN", false, true, false);
	
	

	private final String key;
	private final boolean standardDispatchEnabled;
	private final boolean prioritized;
	private final boolean showSendButton;
	public static final String KEY_EVENT_TYPE = "eventType";
	public static final String KEY_TARGET_TEMPLATE_TYPE = "targetTemplateType";

	EmailTemplateType(String key, boolean standardDispatchEnabled) {
		this(key, standardDispatchEnabled, false, true);
	}

	EmailTemplateType(String key, boolean standardDispatchEnabled, boolean prioritized) {
		this(key, standardDispatchEnabled, prioritized, true);
	}

	EmailTemplateType(String key, boolean standardDispatchEnabled, boolean prioritized, boolean showSendButton) {
		this.key = key;
		this.standardDispatchEnabled = standardDispatchEnabled;
		this.prioritized = prioritized;
		this.showSendButton = showSendButton;
	}

	public String getKey() {
		return key;
	}

	public boolean isContainedIn(String value) {
		return value != null && value.contains(key);
	}

	public static EmailTemplateType fromKey(String key) {
		if (key == null || key.trim().isEmpty()) {
			return null;
		}
		for (EmailTemplateType type : values()) {
			if (type.getKey().equals(key)) {
				return type;
			}
		}
		return null;
	}

	public boolean shouldSendStandardDispatch() {
		return standardDispatchEnabled;
	}

	public boolean isPrioritized() {
		return prioritized;
	}

	public boolean shouldShowSendButton() {
		return showSendButton;
	}

	public String resolveStandardDispatchEventType() {
		switch (this) {
		case NOMINATION_SUBMITTED_NOMINEE:
			return "NOMINATION_SUBMITTED";
		case NOMINATION_ACCEPTED_REPRESENTATIVE:
			return "NOMINATION_ACCEPTED";
		case NOMINATION_REJECTED_REPRESENTATIVE:
			return "NOMINATION_REJECTED";
		case NOMINATION_SUPPORT_STATUS_APPROVED_CANDIDATE:
			return "NOMINATION_SUPPORT_APPROVED";
		case NOMINATION_SUPPORT_STATUS_REJECTED_CANDIDATE:
			return "NOMINATION_SUPPORT_REJECTED";
		case SUPPORT_REMINDER:
			return "SUPPORT_REMINDER";
		case SUPPORT_REQUESTER_REMINDER:
			return "SUPPORT_REQUESTER_REMINDER";
		case CANDIDATE_REMINDER:
			return "CANDIDATE_REMINDER";
		case AUDITOR_REMINDER:
			return "AUDITOR_REMINDER";
		case AUDITOR_REMINDER_REVISION:
			return "AUDITOR_REMINDER_REVISION";
		case CANDIDATE_CONFIRMED_AND_PUBLISHED:
			return "CANDIDATE_CONFIRMED_AND_PUBLISHED";
		default:
			return getKey();
		}
	}
}
