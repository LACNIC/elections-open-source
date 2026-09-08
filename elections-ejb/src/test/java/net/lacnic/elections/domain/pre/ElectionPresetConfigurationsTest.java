package net.lacnic.elections.domain.pre;

import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.ElectionType;

 class ElectionPresetConfigurationsTest extends TestCase {

	 static Test suite() {
		return new TestSuite(ElectionPresetConfigurationsTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testGetDefaultsByElectionType() {
		ElectionPresetConfigurations.ElectionPresetConfiguration board = ElectionPresetConfigurations.get(ElectionType.BOARD);
		assertNotNull(board);
		assertEquals(ElectionType.BOARD, board.getElectionType());
		assertTrue(board.isManageOrganizationsManual());
		assertTrue(board.isManageVotersManual());
		assertTrue(board.getPresetTasks().contains(ElectionTaskKey.DECLARATIONS));
		assertTrue(board.getCalendarsWithDefaultDate().contains(ElectionCalendarKey.N_6_PERIODO_ADDITIONAL_EVALUATION));
		assertTrue(board.getPresetTasks().indexOf(ElectionTaskKey.INCOMPATIBILITIES) < board.getPresetTasks().indexOf(ElectionTaskKey.COURSE));
		assertTrue(board.getPresetTasks().indexOf(ElectionTaskKey.COURSE) < board.getPresetTasks().indexOf(ElectionTaskKey.ORG_SUPPORTS));

		ElectionPresetConfigurations.ElectionPresetConfiguration other = ElectionPresetConfigurations.get(ElectionType.OTHER);
		assertNotNull(other);
		assertTrue(other.isManageOrganizationsManual());
		assertTrue(other.isManageVotersManual());
		assertTrue(other.getPresetTasks().contains(ElectionTaskKey.DECLARATIONS));

		ElectionPresetConfigurations.ElectionPresetConfiguration iana = ElectionPresetConfigurations.get(ElectionType.IANA);
		assertNotNull(iana);
		assertTrue(iana.isManageOrganizationsManual());
		assertTrue(iana.isManageVotersManual());

		ElectionPresetConfigurations.ElectionPresetConfiguration aso = ElectionPresetConfigurations.get(ElectionType.ASO);
		assertNotNull(aso);
		assertTrue(aso.isManageOrganizationsManual());
		assertTrue(aso.isManageVotersManual());

		for (ElectionType electionType : ElectionType.values()) {
			ElectionPresetConfigurations.ElectionPresetConfiguration configuration = ElectionPresetConfigurations.get(electionType);
			assertNotNull(configuration);
			assertTrue(configuration.isManageOrganizationsManual());
			assertTrue(configuration.isManageVotersManual());
		}
	}

	@org.junit.jupiter.api.Test

	 void testGetReturnsNullForUnknownInput() {
		assertNull(ElectionPresetConfigurations.get(null));
	}

	@org.junit.jupiter.api.Test

	 void testTaskStageAndDefaultPublicHelpers() {
		assertEquals(ElectionCalendarKey.N_6_PERIODO_ADDITIONAL_EVALUATION, ElectionPresetConfigurations.resolveTaskStage(ElectionTaskKey.EVALUATION));
		assertEquals(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES, ElectionPresetConfigurations.resolveTaskStage(ElectionTaskKey.PROFILE));
		assertTrue(ElectionPresetConfigurations.isDefaultPublicCalendar(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED));
		assertFalse(ElectionPresetConfigurations.isDefaultPublicCalendar(ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION));
		assertTrue(ElectionPresetConfigurations.isDefaultPublicTask(ElectionTaskKey.PROFILE));
		assertTrue(ElectionPresetConfigurations.isDefaultPublicTask(ElectionTaskKey.USER_SUPPORTS_5));
		assertTrue(ElectionPresetConfigurations.isDefaultPublicTask(ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS));
		assertFalse(ElectionPresetConfigurations.isDefaultPublicTask(null));
		assertFalse(ElectionPresetConfigurations.isDefaultPublicCalendar(null));
	}

	@org.junit.jupiter.api.Test
	 void testCampusTasksRequireConfiguredCampus() {
		assertFalse(ElectionPresetConfigurations.isPresetTaskEnabled(ElectionTaskKey.COURSE, false));
		assertFalse(ElectionPresetConfigurations.isPresetTaskEnabled(ElectionTaskKey.EVALUATION, false));
		assertTrue(ElectionPresetConfigurations.isPresetTaskEnabled(ElectionTaskKey.PROFILE, false));
		assertTrue(ElectionPresetConfigurations.isPresetTaskEnabled(ElectionTaskKey.COURSE, true));
		assertTrue(ElectionPresetConfigurations.isPresetTaskEnabled(ElectionTaskKey.EVALUATION, true));
	}

	@org.junit.jupiter.api.Test
	 void testDefaultTaskDisplayOrderPlacesCourseBeforeSupports() {
		assertEquals(30, ElectionTaskKey.INCOMPATIBILITIES.getDefaultDisplayOrder());
		assertEquals(40, ElectionTaskKey.COURSE.getDefaultDisplayOrder());
		assertEquals(50, ElectionTaskKey.ORG_SUPPORTS.getDefaultDisplayOrder());
		assertEquals(60, ElectionTaskKey.USER_SUPPORTS_2.getDefaultDisplayOrder());
		assertEquals(70, ElectionTaskKey.USER_SUPPORTS_5.getDefaultDisplayOrder());
		assertEquals(80, ElectionTaskKey.ORGANIZATIONS.getDefaultDisplayOrder());
	}

	@org.junit.jupiter.api.Test

	 void testBuildDefaultCalendarDateUtc() {
		long now = System.currentTimeMillis();
		java.util.Date defaultDate = ElectionPresetConfigurations.buildDefaultCalendarDateUtc();

		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calendar.setTime(defaultDate);

		assertNotNull(defaultDate);
		assertEquals(18, calendar.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, calendar.get(Calendar.MINUTE));
		assertEquals(0, calendar.get(Calendar.SECOND));
		assertTrue(defaultDate.getTime() > now);
	}
}
