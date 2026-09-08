package net.lacnic.elections.adminweb.ui.token;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateTextImprovementInstruction;

public abstract class AbstractAcceptNominationTaskPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final String PHOTO_GUIDE_REFERENCE_IMAGE_PATH = "image/refFoto.jpg";

	private final AcceptNominationTaskResolution resolution;

	protected AbstractAcceptNominationTaskPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id);
		this.resolution = resolution;
	}

	protected AcceptNominationTaskResolution getTaskResolution() {
		return resolution;
	}

	protected AcceptNominationTaskItem getSelectedTask() {
		return resolution.getSelectedTask();
	}

	protected AcceptNominationTaskMode getEffectiveMode() {
		return resolution.getEffectiveMode();
	}

	protected boolean isReadOnlyMode() {
		return resolution.isReadOnly();
	}

	protected boolean isEditMode() {
		return resolution.getEffectiveMode() == AcceptNominationTaskMode.EDIT;
	}

	protected boolean isCompleteMode() {
		return resolution.getEffectiveMode() == AcceptNominationTaskMode.COMPLETE;
	}

	protected boolean isSelectedTaskCompleted() {
		return getSelectedTask() != null && getSelectedTask().isCompleted();
	}

	protected boolean canContinueLater() {
		return isCompleteMode() && !isReadOnlyMode() && !isSelectedTaskCompleted();
	}

	protected boolean ensureCanContinueLater() {
		if (canContinueLater()) {
			return true;
		}
		getSession().warn(getString(isSelectedTaskCompleted()
				? "acceptNominationTaskRestrictionAlreadyCompleted"
				: "acceptNominationTaskRestrictionGeneric"));
		return false;
	}

	protected String buildTaskScopedTestId(String prefix) {
		return AcceptNominationTaskTestIdUtils.buildTaskScopedTestId(prefix, getSelectedTask() != null ? getSelectedTask().getTaskKey() : null);
	}

	protected WebMarkupContainer buildTaskTitle(String componentId) {
		WebMarkupContainer cardTitle = new WebMarkupContainer(componentId);
		cardTitle.add(AttributeModifier.replace("data-testid", buildTaskScopedTestId("card")));
		return cardTitle;
	}

	// Estandariza el hook para futuros updates reales de status/fechas en backend.
	protected void requestTaskStatusChange(CandidateElectionTaskStatus status) {
		if (getTaskResolution() == null || getTaskResolution().getNomination() == null || getSelectedTask() == null || getSelectedTask().getTaskKey() == null) {
			getSession().error(getString("acceptNominationTaskStatusChangeError"));
			return;
		}
		if (getTaskResolution().isNominationWindowClosed()) {
			getSession().warn(getString("acceptNominationTaskRestrictionNominationWindowClosed"));
			return;
		}
		if (status == CandidateElectionTaskStatus.STARTED && isSelectedTaskCompleted()) {
			getSession().warn(getString("acceptNominationTaskRestrictionAlreadyCompleted"));
			return;
		}

		String token = getTaskResolution().getNomination().getAcceptNominationToken();
		boolean updated = AppContext.getInstance().getPreNominationBeanRemote().updateCandidateTaskStatus(token, getSelectedTask().getTaskKey(), status, SecurityUtils.getClientIp());
		if (!updated) {
			getSession().error(getString("acceptNominationTaskStatusChangeError"));
			return;
		}

		getSession().success(resolveTaskStatusChangeMessage(status));
		PageParameters params = UtilsParameters.getToken(token);
		params.add(AcceptNominationTaskResolver.TASK_PARAM, getSelectedTask().getTaskKey().name());
		AcceptNominationTaskMode nextMode = status == CandidateElectionTaskStatus.COMPLETED ? AcceptNominationTaskMode.VIEW : AcceptNominationTaskMode.COMPLETE;
		params.add(AcceptNominationTaskResolver.MODE_PARAM, nextMode.getParameterValue());
		setResponsePage(GenericAcceptNominationTasksPage.class, params);
	}

	protected String resolveTaskStatusChangeMessage(CandidateElectionTaskStatus status) {
		return getString(resolveTaskStatusChangeMessageKey(status));
	}

	private String resolveTaskStatusChangeMessageKey(CandidateElectionTaskStatus status) {
		if (status == null) {
			return "acceptNominationTaskStatusChangeRequested";
		}
		switch (status) {
		case NOT_STARTED:
			return "acceptNominationTaskStatusChangeNotStarted";
		case STARTED:
			return "acceptNominationTaskStatusChangeStarted";
		case COMPLETED:
			return "acceptNominationTaskStatusChangeCompleted";
		case OMITTED:
			return "acceptNominationTaskStatusChangeOmitted";
		default:
			return "acceptNominationTaskStatusChangeRequested";
		}
	}

	protected void addPageFeedback(AjaxRequestTarget target) {
		if (getPage() instanceof GenericAcceptNominationTasksPage) {
			target.add(((GenericAcceptNominationTasksPage) getPage()).getFeedbackPanel());
		}
	}

	protected ContextImage buildPhotoGuideReferenceImage(String componentId) {
		return new ContextImage(componentId, PHOTO_GUIDE_REFERENCE_IMAGE_PATH);
	}

	protected boolean sameText(String originalText, String improvedText) {
		return StringUtils.equals(
				StringUtils.trimToEmpty(originalText).replace("\r\n", "\n"),
				StringUtils.trimToEmpty(improvedText).replace("\r\n", "\n"));
	}

	protected String resolveAppliedMessageKey(CandidateTextImprovementInstruction instruction) {
		return instruction == CandidateTextImprovementInstruction.SPELLING_REVIEW
				? "textAssistanceSpellingApplied"
				: "textAssistanceStyleApplied";
	}

	protected String resolveNoChangesMessageKey(CandidateTextImprovementInstruction instruction) {
		return instruction == CandidateTextImprovementInstruction.SPELLING_REVIEW
				? "textAssistanceSpellingNoChanges"
				: "textAssistanceStyleNoChanges";
	}
}
