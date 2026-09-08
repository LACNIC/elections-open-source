package net.lacnic.elections.adminweb.ui.token.taskpanels;

import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskMode;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.domain.pre.CandidateCampusCourseStatus;
import net.lacnic.elections.domain.pre.CandidateEvaluationStatus;

public class CampusTaskPanelStateFactory {

	public CoursePanelState resolveCourseState(AcceptNominationTaskResolution resolution, CandidateCampusCourseStatus campusStatus) {
		CoursePanelScenario scenario = CoursePanelScenario.fromStatus(campusStatus);
		boolean actionsEnabled = isActionsEnabled(resolution);

		return new CoursePanelState(
				scenario,
				actionsEnabled && scenario == CoursePanelScenario.PENDING_ENROLL,
				actionsEnabled && scenario == CoursePanelScenario.ACCESS_PENDING_CHECK);
	}

	public EvaluationPanelState resolveEvaluationState(AcceptNominationTaskResolution resolution, CandidateEvaluationStatus evaluationStatus) {
		EvaluationPanelScenario scenario = EvaluationPanelScenario.fromStatus(evaluationStatus);
		boolean actionsEnabled = isActionsEnabled(resolution);
		boolean completeMode = resolution != null && resolution.getEffectiveMode() == AcceptNominationTaskMode.COMPLETE;

		boolean canSendRequest;
		if (!actionsEnabled) {
			canSendRequest = false;
		} else if (completeMode) {
			canSendRequest = scenario == EvaluationPanelScenario.PENDING_REQUEST;
		} else {
			canSendRequest = scenario == EvaluationPanelScenario.PENDING_REQUEST
					|| scenario == EvaluationPanelScenario.REQUESTED_WAITING_CREDENTIALS
					|| scenario == EvaluationPanelScenario.CREDENTIALS_SENT_READY_TO_CHECK;
		}

		boolean canCheckStatus = actionsEnabled && scenario == EvaluationPanelScenario.CREDENTIALS_SENT_READY_TO_CHECK;
		boolean showSupportHint = scenario == EvaluationPanelScenario.REQUESTED_WAITING_CREDENTIALS
				|| scenario == EvaluationPanelScenario.CREDENTIALS_SENT_READY_TO_CHECK;

		return new EvaluationPanelState(
				scenario,
				canSendRequest,
				canCheckStatus,
				showSupportHint,
				completeMode);
	}

	private boolean isActionsEnabled(AcceptNominationTaskResolution resolution) {
		if (resolution == null || resolution.hasModeRestriction()) {
			return false;
		}
		return resolution.getSelectedTask() != null && !resolution.getSelectedTask().isCompleted();
	}

	public enum CoursePanelScenario {
		PENDING_ENROLL,
		ACCESS_PENDING_CHECK,
		COMPLETED,
		NOT_APPLICABLE;

		public static CoursePanelScenario fromStatus(CandidateCampusCourseStatus status) {
			if (status == null) {
				return PENDING_ENROLL;
			}
			switch (status) {
			case SENT:
			case STARTED:
				return ACCESS_PENDING_CHECK;
			case COMPLETED:
				return COMPLETED;
			case NOT_APPLICABLE:
				return NOT_APPLICABLE;
			case PENDING:
			default:
				return PENDING_ENROLL;
			}
		}
	}

	public static final class CoursePanelState {
		private final CoursePanelScenario scenario;
		private final boolean canSendRequest;
		private final boolean canCheckStatus;

		private CoursePanelState(CoursePanelScenario scenario, boolean canSendRequest, boolean canCheckStatus) {
			this.scenario = scenario;
			this.canSendRequest = canSendRequest;
			this.canCheckStatus = canCheckStatus;
		}

		public CoursePanelScenario getScenario() {
			return scenario;
		}

		public boolean showRequirementAlert() {
			return scenario != CoursePanelScenario.COMPLETED && scenario != CoursePanelScenario.NOT_APPLICABLE;
		}

		public boolean showActionHint() {
			return canSendRequest;
		}

		public boolean showCampusGuidance() {
			return canCheckStatus;
		}

		public boolean showCompletedAlert() {
			return scenario == CoursePanelScenario.COMPLETED;
		}

		public boolean showNotApplicableAlert() {
			return scenario == CoursePanelScenario.NOT_APPLICABLE;
		}

		public boolean canSendRequest() {
			return canSendRequest;
		}

		public boolean canCheckStatus() {
			return canCheckStatus;
		}
	}

	public enum EvaluationPanelScenario {
		PENDING_REQUEST,
		REQUESTED_WAITING_CREDENTIALS,
		CREDENTIALS_SENT_READY_TO_CHECK,
		COMPLETED,
		NOT_APPLICABLE;

		public static EvaluationPanelScenario fromStatus(CandidateEvaluationStatus status) {
			if (status == null) {
				return PENDING_REQUEST;
			}
			switch (status) {
			case CREDENTIALS_REQUESTED:
				return REQUESTED_WAITING_CREDENTIALS;
			case CREDENTIALS_SENT:
				return CREDENTIALS_SENT_READY_TO_CHECK;
			case COMPLETED:
				return COMPLETED;
			case NOT_APPLICABLE:
				return NOT_APPLICABLE;
			case PENDING:
			default:
				return PENDING_REQUEST;
			}
		}
	}

	public static final class EvaluationPanelState {
		private final EvaluationPanelScenario scenario;
		private final boolean canSendRequest;
		private final boolean canCheckStatus;
		private final boolean showSupportHint;
		private final boolean completeMode;

		private EvaluationPanelState(EvaluationPanelScenario scenario, boolean canSendRequest, boolean canCheckStatus, boolean showSupportHint, boolean completeMode) {
			this.scenario = scenario;
			this.canSendRequest = canSendRequest;
			this.canCheckStatus = canCheckStatus;
			this.showSupportHint = showSupportHint;
			this.completeMode = completeMode;
		}

		public EvaluationPanelScenario getScenario() {
			return scenario;
		}

		public boolean showRequirementAlert() {
			return scenario != EvaluationPanelScenario.COMPLETED && scenario != EvaluationPanelScenario.NOT_APPLICABLE;
		}

		public boolean showActionHint() {
			return completeMode && canSendRequest;
		}

		public boolean showSupportHint() {
			return showSupportHint;
		}

		public boolean showCompletedAlert() {
			return scenario == EvaluationPanelScenario.COMPLETED;
		}

		public boolean showNotApplicableAlert() {
			return scenario == EvaluationPanelScenario.NOT_APPLICABLE;
		}

		public boolean canSendRequest() {
			return canSendRequest;
		}

		public boolean canCheckStatus() {
			return canCheckStatus;
		}
	}
}
