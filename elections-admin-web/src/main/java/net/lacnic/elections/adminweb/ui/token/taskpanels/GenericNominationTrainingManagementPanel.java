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
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolver;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.campus.CampusClient;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateCampusCourseStatus;
import net.lacnic.elections.domain.pre.CandidateTrainingCredentialsRequestResult;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class GenericNominationTrainingManagementPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";

	private final String token;
	private Candidate candidate;
	private CandidateCampusCourseStatus campusCourseStatus;

	public GenericNominationTrainingManagementPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(buildTaskTitle("cardTitle"));

		this.candidate = resolution.getCandidate();
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";
		this.campusCourseStatus = resolveCampusCourseStatus(
				candidate != null ? candidate.getCampusCourseStatus() : null,
				CampusClient.isCampusTrainingEnabled(candidate != null ? candidate.getElection() : null));

		Label restrictionMessage = new Label("restrictionMessage", new ResourceModel(getTaskResolution().getRestrictionMessageKey(), ""));
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);

		Form<Void> trainingForm = new Form<>("trainingForm");
		add(trainingForm);

		Label statusValue = new Label("statusValue", new ResourceModel(resolveStatusLabelKey(campusCourseStatus), ""));
		statusValue.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge " + resolveStatusBadgeClass(campusCourseStatus)));
		trainingForm.add(statusValue);

		boolean showRequirementAlerts = shouldShowRequirementAlerts();
		WebMarkupContainer courseRequiredAlert = new WebMarkupContainer("courseRequiredAlert");
		courseRequiredAlert.setVisible(showRequirementAlerts);
		trainingForm.add(courseRequiredAlert);

		Label actionHint = new Label("actionHint", new ResourceModel("acceptNominationTrainingActionHint", ""));
		actionHint.setVisible(canSendCredentials());
		trainingForm.add(actionHint);

		Label actionLanguageHint = new Label("actionLanguageHint", new ResourceModel("acceptNominationTrainingActionLanguageHint", ""));
		actionLanguageHint.setVisible(canSendCredentials() && countConfiguredCampusCourses() > 1);
		trainingForm.add(actionLanguageHint);

		Label campusGuidanceLine2 = new Label("campusGuidanceLine2", new ResourceModel("acceptNominationTrainingCampusGuidanceLine2", ""));
		campusGuidanceLine2.setEscapeModelStrings(false);
		campusGuidanceLine2.setVisible(canCheckCourseStatus());
		trainingForm.add(campusGuidanceLine2);

		Label campusGuidanceLine3 = new Label("campusGuidanceLine3", MessageFormat.format(getString("acceptNominationTrainingCampusGuidanceLine3"), resolveSupportEmail()));
		campusGuidanceLine3.setVisible(canCheckCourseStatus());
		trainingForm.add(campusGuidanceLine3);

		Label completedMessageTitle = new Label("completedMessageTitle", new ResourceModel("acceptNominationTrainingCompletedMessageTitle", ""));
		completedMessageTitle.setVisible(campusCourseStatus == CandidateCampusCourseStatus.COMPLETED);
		trainingForm.add(completedMessageTitle);

		Label completedMessageBody = new Label("completedMessageBody", new ResourceModel("acceptNominationTrainingCompletedMessageBody", ""));
		completedMessageBody.setVisible(campusCourseStatus == CandidateCampusCourseStatus.COMPLETED);
		trainingForm.add(completedMessageBody);

		WebMarkupContainer notApplicableAlert = new WebMarkupContainer("notApplicableAlert");
		notApplicableAlert.setVisible(campusCourseStatus == CandidateCampusCourseStatus.NOT_APPLICABLE);
		trainingForm.add(notApplicableAlert);

		trainingForm.add(new BookmarkablePageLink<Void>("backButton", GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token)));

		Button sendCredentialsButton = new Button("sendCredentialsButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				requestTrainingCredentials(resolveSpanishCampusCourse());
			}
		};
		sendCredentialsButton.setVisible(canSendSpanishCredentials());
		trainingForm.add(sendCredentialsButton);

		Label sendCredentialsButtonLabel = new Label("sendCredentialsButtonLabel", new ResourceModel(resolveActionButtonLabelKey(), ""));
		sendCredentialsButton.add(sendCredentialsButtonLabel);

		Button sendCredentialsEnglishButton = new Button("sendCredentialsEnglishButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				requestTrainingCredentials(resolveEnglishCampusCourse());
			}
		};
		sendCredentialsEnglishButton.setVisible(canSendCredentials() && resolveEnglishCampusCourse() != null);
		trainingForm.add(sendCredentialsEnglishButton);

		Button sendCredentialsPortugueseButton = new Button("sendCredentialsPortugueseButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				requestTrainingCredentials(resolvePortugueseCampusCourse());
			}
		};
		sendCredentialsPortugueseButton.setVisible(canSendCredentials() && resolvePortugueseCampusCourse() != null);
		trainingForm.add(sendCredentialsPortugueseButton);

		Button checkCourseStatusButton = new Button("checkCourseStatusButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				try {
					AppContext.getInstance().getManagerBeanRemote().verifyCampusCourseAccessForCandidatesRateLimited(buildRequesterKey(), candidate != null ? candidate.getMail() : null);

					Nomination refreshedNomination = AppContext.getInstance().getPreNominationBeanRemote().verifyAcceptNominationAccess(token);
					Candidate refreshedCandidate = refreshedNomination != null ? refreshedNomination.getCandidate() : null;
					if (refreshedCandidate != null) {
						candidate = refreshedCandidate;
						campusCourseStatus = resolveCampusCourseStatus(
								refreshedCandidate.getCampusCourseStatus(),
								CampusClient.isCampusTrainingEnabled(refreshedCandidate.getElection()));
					}

					boolean courseTaskCompleted = isCourseTaskCompleted(refreshedCandidate);
					if (courseTaskCompleted) {
						SecurityUtils.info(getString("acceptNominationTrainingCheckStatusCompleted"));
					} else {
						SecurityUtils.info(getString("acceptNominationTrainingCheckStatusPending"));
					}

					setResponsePage(GenericAcceptNominationTasksPage.class, buildCurrentTaskPageParameters(resolveNextModeAfterCredentialsRequest(refreshedCandidate)));
				} catch (Exception e) {
					String technicalDetails = "paso=CHECK_CAMPUS_COURSE_STATUS, errorType=" + e.getClass().getSimpleName();
					if (hasText(e.getMessage())) {
						technicalDetails = technicalDetails + ", errorMessage=" + e.getMessage().trim();
					}
					SecurityUtils.error(new StringResourceModel("acceptNominationTrainingCheckStatusErrorWithDetails", this, null).setParameters(technicalDetails).getString());
				}
			}
		};
		checkCourseStatusButton.setDefaultFormProcessing(false);
		checkCourseStatusButton.setVisible(canCheckCourseStatus());
		trainingForm.add(checkCourseStatusButton);
	}

	private void requestTrainingCredentials(Long courseId) {
		if (!canSendCredentials()) {
			SecurityUtils.error(getString("acceptNominationTrainingRequestInvalidState"));
			return;
		}

		CandidateTrainingCredentialsRequestResult requestResult;
		try {
			requestResult = AppContext.getInstance().getPreNominationBeanRemote()
					.requestCandidateTrainingCredentialsDetailed(token, CANDIDATE_LINK_ACTIVITY_ACTOR, SecurityUtils.getClientIp(), courseId);
		} catch (Exception e) {
			String technicalDetails = "paso=REQUEST_CAMPUS_CREDENTIALS, errorType=" + e.getClass().getSimpleName();
			SecurityUtils.error(new StringResourceModel("acceptNominationTrainingRequestErrorWithDetails", this, null).setParameters(technicalDetails).getString());
			return;
		}
		if (requestResult == null || !requestResult.isSuccess() || requestResult.getCandidate() == null) {
			showTrainingRequestError(requestResult);
			return;
		}
		Candidate updatedCandidate = requestResult.getCandidate();

		candidate = updatedCandidate;
		campusCourseStatus = resolveCampusCourseStatus(
				updatedCandidate.getCampusCourseStatus(),
				CampusClient.isCampusTrainingEnabled(updatedCandidate.getElection()));
		SecurityUtils.info(getString("acceptNominationTrainingRequestSuccess"));
		setResponsePage(GenericAcceptNominationTasksPage.class, buildCurrentTaskPageParameters(resolveNextModeAfterCredentialsRequest(updatedCandidate)));
	}

	static CandidateCampusCourseStatus resolveCampusCourseStatus(CandidateCampusCourseStatus status, boolean campusTrainingEnabled) {
		if (!campusTrainingEnabled) {
			return CandidateCampusCourseStatus.NOT_APPLICABLE;
		}
		return status != null ? status : CandidateCampusCourseStatus.PENDING;
	}

	private boolean canSendCredentials() {
		if (isTaskCompleted() || getTaskResolution().hasModeRestriction()) {
			return false;
		}
		return campusCourseStatus == CandidateCampusCourseStatus.PENDING;
	}

	private boolean canSendSpanishCredentials() {
		if (!canSendCredentials()) {
			return false;
		}
		return resolveSpanishCampusCourse() != null;
	}

	private boolean canCheckCourseStatus() {
		if (isTaskCompleted() || getTaskResolution().hasModeRestriction()) {
			return false;
		}
		return campusCourseStatus == CandidateCampusCourseStatus.SENT || campusCourseStatus == CandidateCampusCourseStatus.STARTED;
	}

	private boolean shouldShowRequirementAlerts() {
		return campusCourseStatus != CandidateCampusCourseStatus.COMPLETED && campusCourseStatus != CandidateCampusCourseStatus.NOT_APPLICABLE;
	}

	private int countConfiguredCampusCourses() {
		int count = 0;
		if (resolveSpanishCampusCourse() != null) {
			count++;
		}
		if (resolveEnglishCampusCourse() != null) {
			count++;
		}
		if (resolvePortugueseCampusCourse() != null) {
			count++;
		}
		return count;
	}

	private Long resolveSpanishCampusCourse() {
		return candidate != null && candidate.getElection() != null ? candidate.getElection().getCampusCourse() : null;
	}

	private Long resolveEnglishCampusCourse() {
		return candidate != null && candidate.getElection() != null ? candidate.getElection().getCampusCourseEnglish() : null;
	}

	private Long resolvePortugueseCampusCourse() {
		return candidate != null && candidate.getElection() != null ? candidate.getElection().getCampusCoursePortuguese() : null;
	}

	private void showTrainingRequestError(CandidateTrainingCredentialsRequestResult requestResult) {
		if (requestResult == null) {
			SecurityUtils.error(getString("acceptNominationTrainingRequestError"));
			return;
		}
		String message = requestResult.getErrorMessage();
		String debugDetails = requestResult.getDebugDetails();
		if (!hasText(message) && !hasText(debugDetails)) {
			SecurityUtils.error(getString("acceptNominationTrainingRequestError"));
			return;
		}
		String details = hasText(message) ? message : "";
		if (hasText(debugDetails)) {
			details = details + (details.isEmpty() ? "" : " ") + debugDetails;
		}
		SecurityUtils.error(new StringResourceModel("acceptNominationTrainingRequestErrorWithDetails", this, null).setParameters(details).getString());
	}

	private String resolveStatusLabelKey(CandidateCampusCourseStatus status) {
		switch (status) {
		case SENT:
			return "acceptNominationTrainingStatusSent";
		case STARTED:
			return "acceptNominationTrainingStatusStarted";
		case COMPLETED:
			return "acceptNominationTrainingStatusCompleted";
		case NOT_APPLICABLE:
			return "acceptNominationTrainingStatusNotApplicable";
		case PENDING:
		default:
			return "acceptNominationTrainingStatusPending";
		}
	}

	private String resolveStatusBadgeClass(CandidateCampusCourseStatus status) {
		switch (status) {
		case SENT:
			return "bg-info-subtle text-info-emphasis";
		case STARTED:
			return "bg-warning-subtle text-warning-emphasis";
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
		return "acceptNominationTrainingEnrollButton";
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

	private AcceptNominationTaskMode resolveNextModeAfterCredentialsRequest(Candidate updatedCandidate) {
		return isCourseTaskCompleted(updatedCandidate) ? AcceptNominationTaskMode.VIEW : AcceptNominationTaskMode.COMPLETE;
	}

	private boolean isCourseTaskCompleted(Candidate updatedCandidate) {
		if (updatedCandidate == null || updatedCandidate.getTaskProgress() == null) {
			return getSelectedTask() != null && getSelectedTask().isCompleted();
		}

		for (CandidateElectionTaskProgress taskProgress : updatedCandidate.getTaskProgress()) {
			if (taskProgress == null || taskProgress.getElectionTask() == null || taskProgress.getElectionTask().getTaskKey() != ElectionTaskKey.COURSE) {
				continue;
			}
			CandidateElectionTaskStatus status = taskProgress.getStatus();
			return status == CandidateElectionTaskStatus.COMPLETED || status == CandidateElectionTaskStatus.OMITTED;
		}

		return getSelectedTask() != null && getSelectedTask().isCompleted();
	}
}
