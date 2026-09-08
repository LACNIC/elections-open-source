package net.lacnic.elections.adminweb.ui.token.taskpanels;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.pre.CandidateCampusCourseStatus;

class GenericNominationTrainingManagementPanelTest {

	@Test
	void trainingIsNotApplicableWhenCampusIsUnavailable() {
		assertEquals(
				CandidateCampusCourseStatus.NOT_APPLICABLE,
				GenericNominationTrainingManagementPanel.resolveCampusCourseStatus(
						CandidateCampusCourseStatus.PENDING,
						false));
	}

	@Test
	void trainingKeepsCandidateStatusWhenCampusIsAvailable() {
		assertEquals(
				CandidateCampusCourseStatus.PENDING,
				GenericNominationTrainingManagementPanel.resolveCampusCourseStatus(
						CandidateCampusCourseStatus.PENDING,
						true));
	}
}
