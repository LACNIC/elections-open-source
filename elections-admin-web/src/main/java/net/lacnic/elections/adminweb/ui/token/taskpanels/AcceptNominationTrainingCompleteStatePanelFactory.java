package net.lacnic.elections.adminweb.ui.token.taskpanels;

import org.apache.wicket.Component;

import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.domain.pre.CandidateCampusCourseStatus;

public class AcceptNominationTrainingCompleteStatePanelFactory {

	private final CampusTaskPanelStateFactory panelStateFactory = new CampusTaskPanelStateFactory();

	public Component create(String id, AcceptNominationTaskResolution resolution) {
		CandidateCampusCourseStatus courseStatus = resolution != null && resolution.getCandidate() != null
				? resolution.getCandidate().getCampusCourseStatus()
				: null;

		CampusTaskPanelStateFactory.CoursePanelScenario scenario = panelStateFactory
				.resolveCourseState(resolution, courseStatus)
				.getScenario();

		switch (scenario) {
		case ACCESS_PENDING_CHECK:
			return new AcceptNominationTrainingCompleteAccessPendingPanel(id, resolution);
		case COMPLETED:
			return new AcceptNominationTrainingCompleteCompletedPanel(id, resolution);
		case NOT_APPLICABLE:
			return new AcceptNominationTrainingCompleteNotApplicablePanel(id, resolution);
		case PENDING_ENROLL:
		default:
			return new AcceptNominationTrainingCompletePendingPanel(id, resolution);
		}
	}
}
