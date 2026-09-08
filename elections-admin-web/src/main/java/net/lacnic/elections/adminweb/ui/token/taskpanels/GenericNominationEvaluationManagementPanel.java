package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.text.MessageFormat;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskMode;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolver;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateEvaluationStatus;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
import net.lacnic.elections.campus.CampusClient;
public class GenericNominationEvaluationManagementPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";

	private final String token;
	private Candidate candidate;
	private CandidateEvaluationStatus evaluationStatus;

	public GenericNominationEvaluationManagementPanel(String id, net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(buildTaskTitle("cardTitle"));

		this.candidate = resolution.getCandidate();
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";
		this.evaluationStatus = resolveEvaluationStatus(candidate != null ? candidate.getEvaluationStatus() : null);

		Label restrictionMessage = new Label("restrictionMessage", new ResourceModel(getTaskResolution().getRestrictionMessageKey(), ""));
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);

		Form<Void> trainingForm = new Form<>("trainingForm");
		add(trainingForm);

		Label statusValue = new Label("statusValue", new ResourceModel(resolveStatusLabelKey(evaluationStatus), ""));
		statusValue.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge " + resolveStatusBadgeClass(evaluationStatus)));
		trainingForm.add(statusValue);

		boolean showRequirementAlerts = shouldShowRequirementAlerts();
		WebMarkupContainer evaluationRequiredAlert = new WebMarkupContainer("evaluationRequiredAlert");
		evaluationRequiredAlert.setVisible(showRequirementAlerts);
		trainingForm.add(evaluationRequiredAlert);

		Label actionHint = new Label("actionHint", new ResourceModel(resolveStatusExplanationKey(evaluationStatus), ""));
		actionHint.setVisible(shouldShowStatusExplanation());
		trainingForm.add(actionHint);

		Label supportContactHint = new Label("supportContactHint", MessageFormat.format(getString("acceptNominationEvaluationSupportContactHint"), resolveSupportEmail()));
		supportContactHint.setVisible(shouldShowSupportContactHint());
		trainingForm.add(supportContactHint);

		WebMarkupContainer completedAlert = new WebMarkupContainer("completedAlert");
		completedAlert.setVisible(evaluationStatus == CandidateEvaluationStatus.COMPLETED);
		trainingForm.add(completedAlert);

		WebMarkupContainer notApplicableAlert = new WebMarkupContainer("notApplicableAlert");
		notApplicableAlert.setVisible(evaluationStatus == CandidateEvaluationStatus.NOT_APPLICABLE);
		trainingForm.add(notApplicableAlert);

		trainingForm.add(new BookmarkablePageLink<Void>("backButton", GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token)));

		Button sendCredentialsButton = new Button("sendCredentialsButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (!canSendCredentials()) {
					SecurityUtils.error(getString("acceptNominationEvaluationRequestInvalidState"));
					return;
				}

				Candidate updatedCandidate = AppContext.getInstance().getPreNominationBeanRemote().requestCandidateEvaluationCredentials(token, CANDIDATE_LINK_ACTIVITY_ACTOR, SecurityUtils.getClientIp());
				if (updatedCandidate == null) {
					SecurityUtils.error(getString("acceptNominationEvaluationRequestError"));
					return;
				}

				candidate = updatedCandidate;
				evaluationStatus = resolveEvaluationStatus(updatedCandidate.getEvaluationStatus());
				SecurityUtils.info(getString("acceptNominationEvaluationRequestSuccess"));
				setResponsePage(GenericAcceptNominationTasksPage.class, buildCurrentTaskPageParameters(resolveNextModeAfterEvaluationUpdate(updatedCandidate)));
			}
		};
		sendCredentialsButton.setVisible(canSendCredentials());
		trainingForm.add(sendCredentialsButton);

		Label sendCredentialsButtonLabel = new Label("sendCredentialsButtonLabel", new ResourceModel(resolveActionButtonLabelKey(), ""));
		sendCredentialsButton.add(sendCredentialsButtonLabel);

		Button checkEvaluationStatusButton = new Button("checkEvaluationStatusButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				try {
					CandidateEvaluationStatus previousStatus = evaluationStatus;
					AppContext.getInstance().getManagerBeanRemote().verifyCampusEvaluationGradesForCandidatesRateLimited(buildRequesterKey(), candidate != null ? candidate.getMail() : null);

					Candidate refreshedCandidate = refreshCandidate();
					if (refreshedCandidate != null) {
						candidate = refreshedCandidate;
						evaluationStatus = resolveEvaluationStatus(refreshedCandidate.getEvaluationStatus());
					}
					showCheckStatusFeedback(previousStatus, evaluationStatus);
					setResponsePage(GenericAcceptNominationTasksPage.class, buildCurrentTaskPageParameters(resolveNextModeAfterEvaluationUpdate(refreshedCandidate)));
				} catch (Exception e) {
					SecurityUtils.error(getString("acceptNominationEvaluationRequestError"));
				}
			}
		};
		checkEvaluationStatusButton.setDefaultFormProcessing(false);
		checkEvaluationStatusButton.setVisible(CampusClient.isCampusIntegrationEnabled() && canCheckEvaluationStatus());
		trainingForm.add(checkEvaluationStatusButton);
	}

	private CandidateEvaluationStatus resolveEvaluationStatus(CandidateEvaluationStatus status) {
		return status != null ? status : CandidateEvaluationStatus.PENDING;
	}

	private boolean canSendCredentials() {
		if (isTaskCompleted() || getTaskResolution().hasModeRestriction()) {
			return false;
		}
		if (isCompleteMode()) {
			return evaluationStatus == CandidateEvaluationStatus.PENDING;
		}
		return evaluationStatus == CandidateEvaluationStatus.PENDING || evaluationStatus == CandidateEvaluationStatus.CREDENTIALS_REQUESTED || evaluationStatus == CandidateEvaluationStatus.CREDENTIALS_SENT;
	}

	private boolean canCheckEvaluationStatus() {
		if (isTaskCompleted() || getTaskResolution().hasModeRestriction()) {
			return false;
		}
		return evaluationStatus == CandidateEvaluationStatus.CREDENTIALS_SENT;
	}

	private boolean shouldShowRequirementAlerts() {
		return evaluationStatus != CandidateEvaluationStatus.COMPLETED && evaluationStatus != CandidateEvaluationStatus.NOT_APPLICABLE;
	}

	private boolean shouldShowSupportContactHint() {
		return evaluationStatus == CandidateEvaluationStatus.CREDENTIALS_REQUESTED || evaluationStatus == CandidateEvaluationStatus.CREDENTIALS_SENT;
	}

	private boolean shouldShowStatusExplanation() {
		return evaluationStatus == CandidateEvaluationStatus.PENDING || evaluationStatus == CandidateEvaluationStatus.CREDENTIALS_REQUESTED || evaluationStatus == CandidateEvaluationStatus.CREDENTIALS_SENT;
	}

	private String resolveStatusLabelKey(CandidateEvaluationStatus status) {
		switch (status) {
		case CREDENTIALS_REQUESTED:
			return "acceptNominationEvaluationStatusRequested";
		case CREDENTIALS_SENT:
			return "acceptNominationEvaluationStatusSent";
		case COMPLETED:
			return "acceptNominationEvaluationStatusCompleted";
		case NOT_APPLICABLE:
			return "acceptNominationEvaluationStatusNotApplicable";
		case PENDING:
		default:
			return "acceptNominationEvaluationStatusPending";
		}
	}

	private String resolveStatusExplanationKey(CandidateEvaluationStatus status) {
		switch (status) {
		case CREDENTIALS_REQUESTED:
			return "acceptNominationEvaluationStatusRequestedHint";
		case CREDENTIALS_SENT:
			return "acceptNominationEvaluationStatusSentHint";
		case PENDING:
		default:
			return "acceptNominationEvaluationStatusPendingHint";
		}
	}

	private String resolveStatusBadgeClass(CandidateEvaluationStatus status) {
		switch (status) {
		case CREDENTIALS_REQUESTED:
			return "bg-warning-subtle text-warning-emphasis";
		case CREDENTIALS_SENT:
			return "bg-info-subtle text-info-emphasis";
		case COMPLETED:
			return "bg-success-subtle text-success-emphasis";
		case NOT_APPLICABLE:
			return "bg-light text-body border";
		case PENDING:
		default:
			return "bg-secondary-subtle text-secondary-emphasis";
		}
	}

	private String resolveActionButtonLabelKey() {
		if (evaluationStatus == CandidateEvaluationStatus.PENDING) {
			return "acceptNominationEvaluationEnrollButton";
		}
		return "acceptNominationEvaluationResendButton";
	}

	private boolean isTaskCompleted() {
		return getSelectedTask() != null && getSelectedTask().isCompleted();
	}

	private String resolveSupportEmail() {
		if (getTaskResolution() != null && getTaskResolution().getNomination() != null && getTaskResolution().getNomination().getElection() != null) {
			String defaultRecipient = getTaskResolution().getNomination().getElection().getDefaultRecipient();
			if (hasText(defaultRecipient)) {
				return defaultRecipient.trim();
			}
			String defaultSender = getTaskResolution().getNomination().getElection().getDefaultSender();
			if (hasText(defaultSender)) {
				return defaultSender.trim();
			}
		}
		return getString("taskEditDisabledFallbackSender");
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String buildRequesterKey() {
		StringBuilder keyBuilder = new StringBuilder();
		if (hasText(token)) {
			keyBuilder.append(token.trim());
		}
		String ip = SecurityUtils.getClientIp();
		if (hasText(ip)) {
			if (keyBuilder.length() > 0) {
				keyBuilder.append('|');
			}
			keyBuilder.append(ip.trim());
		}
		if (keyBuilder.length() == 0) {
			keyBuilder.append("anonymous");
		}
		return keyBuilder.toString();
	}

	private PageParameters buildCurrentTaskPageParameters(AcceptNominationTaskMode mode) {
		PageParameters params = UtilsParameters.getToken(token);
		if (getSelectedTask() != null && getSelectedTask().getTaskKey() != null) {
			params.add(AcceptNominationTaskResolver.TASK_PARAM, getSelectedTask().getTaskKey().name());
		}
		params.add(AcceptNominationTaskResolver.MODE_PARAM, mode.getParameterValue());
		return params;
	}

	private AcceptNominationTaskMode resolveNextModeAfterEvaluationUpdate(Candidate updatedCandidate) {
		return isEvaluationTaskCompleted(updatedCandidate) ? AcceptNominationTaskMode.VIEW : AcceptNominationTaskMode.COMPLETE;
	}

	private boolean isEvaluationTaskCompleted(Candidate updatedCandidate) {
		if (updatedCandidate == null || updatedCandidate.getTaskProgress() == null) {
			return getSelectedTask() != null && getSelectedTask().isCompleted();
		}

		for (CandidateElectionTaskProgress taskProgress : updatedCandidate.getTaskProgress()) {
			if (taskProgress == null || taskProgress.getElectionTask() == null || taskProgress.getElectionTask().getTaskKey() != ElectionTaskKey.EVALUATION) {
				continue;
			}
			CandidateElectionTaskStatus status = taskProgress.getStatus();
			return status == CandidateElectionTaskStatus.COMPLETED || status == CandidateElectionTaskStatus.OMITTED;
		}

		return getSelectedTask() != null && getSelectedTask().isCompleted();
	}

	private Candidate refreshCandidate() {
		if (!hasText(token)) {
			return null;
		}
		Nomination refreshedNomination = AppContext.getInstance().getPreNominationBeanRemote().verifyAcceptNominationAccess(token);
		return refreshedNomination != null ? refreshedNomination.getCandidate() : null;
	}

	private void showCheckStatusFeedback(CandidateEvaluationStatus previousStatus, CandidateEvaluationStatus currentStatus) {
		CandidateEvaluationStatus normalizedPrevious = resolveEvaluationStatus(previousStatus);
		CandidateEvaluationStatus normalizedCurrent = resolveEvaluationStatus(currentStatus);
		String currentStatusLabel = getString(resolveStatusLabelKey(normalizedCurrent));
		String messageKey = normalizedPrevious != normalizedCurrent
				? "acceptNominationEvaluationCheckStatusUpdated"
				: "acceptNominationEvaluationCheckStatusUnchanged";
		String message = new StringResourceModel(messageKey, this, null).setParameters(currentStatusLabel).getString();
		SecurityUtils.info(message);
	}
}
