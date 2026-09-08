package net.lacnic.elections.adminweb.ui.admin.election.create;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ElectionCreatePanelTest {
	private WicketTester wicketTester;

	@BeforeEach
	void setUp() {
		wicketTester = new WicketTester();
	}

	@AfterEach
	void tearDown() {
		wicketTester.destroy();
	}

	@Test
	void hidesCampusSectionWhenIntegrationIsNotConfigured() {
		WebMarkupContainer campusCourses = ElectionCreatePanel.createCampusCoursesContainer(false);

		assertFalse(campusCourses.isVisible());
	}

	@Test
	void showsCampusSectionWhenIntegrationIsConfigured() {
		WebMarkupContainer campusCourses = ElectionCreatePanel.createCampusCoursesContainer(true);

		assertTrue(campusCourses.isVisible());
	}
}
