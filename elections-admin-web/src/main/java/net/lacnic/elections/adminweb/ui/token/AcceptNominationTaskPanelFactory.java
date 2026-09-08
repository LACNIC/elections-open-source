package net.lacnic.elections.adminweb.ui.token;

import org.apache.wicket.Component;

import net.lacnic.elections.adminweb.ui.token.taskpanels.*;
import net.lacnic.elections.domain.pre.ElectionTaskKey;

public class AcceptNominationTaskPanelFactory {

	public Component create(String id, AcceptNominationTaskResolution resolution) {
		if (resolution == null || resolution.getSelectedTask() == null || resolution.getSelectedTask().getTaskKey() == null) {
			return new EmptyTaskPanel(id);
		}

		ElectionTaskKey taskKey = resolution.getSelectedTask().getTaskKey();
		AcceptNominationTaskMode effectiveMode = resolution.getEffectiveMode() != null ? resolution.getEffectiveMode() : AcceptNominationTaskMode.VIEW;

		switch (taskKey) {
		case PROFILE:
			return createProfilePanel(id, resolution, effectiveMode);
		case INCOMPATIBILITIES:
			return createIncompatibilitiesPanel(id, resolution, effectiveMode);
		case OTHER_STATUTORY_QUESTIONS:
			return createOtherStatutoryQuestionsPanel(id, resolution, effectiveMode);
		case OTHER_NON_STATUTORY_QUESTIONS:
			return createOtherNonStatutoryQuestionsPanel(id, resolution, effectiveMode);
		case COUNTRIES:
			return createCountriesPanel(id, resolution, effectiveMode);
		case ORGANIZATIONS:
			return createOrganizationsPanel(id, resolution, effectiveMode);
		case COURSE:
			return createCoursePanel(id, resolution, effectiveMode);
		case EVALUATION:
			return createEvaluationPanel(id, resolution, effectiveMode);
		case ORG_SUPPORTS:
			return createOrgSupportsPanel(id, resolution, effectiveMode);
		case USER_SUPPORTS_2:
			return createUserSupports2Panel(id, resolution, effectiveMode);
		case USER_SUPPORTS_5:
			return createUserSupports5Panel(id, resolution, effectiveMode);
		case DECLARATIONS:
			return createDeclarationsPanel(id, resolution, effectiveMode);
		case DECLARATIONS_NON_STATUTORY:
			return createNonStatutoryDeclarationsPanel(id, resolution, effectiveMode);
		default:
			return new EmptyTaskPanel(id);
		}
	}

	private Component createProfilePanel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationProfileEditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationProfileCompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationProfileViewPanel(id, resolution);
		}
	}

	private Component createIncompatibilitiesPanel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationIncompatibilitiesEditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationIncompatibilitiesCompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationIncompatibilitiesViewPanel(id, resolution);
		}
	}

	private Component createOtherStatutoryQuestionsPanel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationOtherStatutoryQuestionsEditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationOtherStatutoryQuestionsCompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationOtherStatutoryQuestionsViewPanel(id, resolution);
		}
	}

	private Component createOtherNonStatutoryQuestionsPanel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationOtherNonStatutoryQuestionsEditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationOtherNonStatutoryQuestionsCompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationOtherNonStatutoryQuestionsViewPanel(id, resolution);
		}
	}

	private Component createCountriesPanel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationCountriesEditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationCountriesCompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationCountriesViewPanel(id, resolution);
		}
	}

	private Component createOrganizationsPanel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationOrganizationsEditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationOrganizationsCompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationOrganizationsViewPanel(id, resolution);
		}
	}

	private Component createCoursePanel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationTrainingEditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationTrainingCompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationTrainingViewPanel(id, resolution);
		}
	}

	private Component createEvaluationPanel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationEvaluationEditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationEvaluationCompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationEvaluationViewPanel(id, resolution);
		}
	}

	private Component createOrgSupportsPanel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationOrgSupportsEditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationOrgSupportsCompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationOrgSupportsViewPanel(id, resolution);
		}
	}

	private Component createUserSupports2Panel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationUserSupports2EditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationUserSupports2CompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationUserSupports2ViewPanel(id, resolution);
		}
	}

	private Component createUserSupports5Panel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationUserSupports5EditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationUserSupports5CompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationUserSupports5ViewPanel(id, resolution);
		}
	}

	private Component createDeclarationsPanel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationDeclarationsEditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationDeclarationsCompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationDeclarationsViewPanel(id, resolution);
		}
	}

	private Component createNonStatutoryDeclarationsPanel(String id, AcceptNominationTaskResolution resolution, AcceptNominationTaskMode mode) {
		switch (mode) {
		case EDIT:
			return new AcceptNominationNonStatutoryDeclarationsEditPanel(id, resolution);
		case COMPLETE:
			return new AcceptNominationNonStatutoryDeclarationsCompletePanel(id, resolution);
		case VIEW:
		default:
			return new AcceptNominationNonStatutoryDeclarationsViewPanel(id, resolution);
		}
	}
}
