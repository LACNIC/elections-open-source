package net.lacnic.elections.adminweb.ui.admin.election.tasks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.pre.ElectionTaskKey;

class ElectionTasksPanelTest {

	@Test
	void coursePublicableCheckboxIsDisabledWithoutCampusTraining() {
		assertFalse(ElectionTasksPanel.canConfigurePublicable(ElectionTaskKey.COURSE, false));
	}

	@Test
	void coursePublicableCheckboxIsEnabledWithCampusTraining() {
		assertTrue(ElectionTasksPanel.canConfigurePublicable(ElectionTaskKey.COURSE, true));
	}

	@Test
	void nonCampusTaskPublicableCheckboxRemainsEnabled() {
		assertTrue(ElectionTasksPanel.canConfigurePublicable(ElectionTaskKey.PROFILE, false));
	}
}
