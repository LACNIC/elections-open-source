package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;

class AuditorCandidateDecisionWindowTest {

	private static final long HOUR = 60L * 60L * 1000L;

	@Test
	void shouldAllowN7AndN8AtInclusiveBoundaries() {
		ElectionCalendar n7 = calendar(ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION, 10L, 20L);
		ElectionCalendar n8 = calendar(ElectionCalendarKey.N_8_PERIODO_EVALUATIONS_VALIDATION, 30L, 40L);

		assertTrue(AuditorCandidateDecisionWindow.isActive(Arrays.asList(n7, n8), dateAtHour(10L)));
		assertTrue(AuditorCandidateDecisionWindow.isActive(Arrays.asList(n7, n8), dateAtHour(20L)));
		assertTrue(AuditorCandidateDecisionWindow.isActive(Arrays.asList(n7, n8), dateAtHour(30L)));
		assertTrue(AuditorCandidateDecisionWindow.isActive(Arrays.asList(n7, n8), dateAtHour(40L)));
	}

	@Test
	void shouldRejectGapBetweenN7AndN8() {
		ElectionCalendar n7 = calendar(ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION, 10L, 20L);
		ElectionCalendar n8 = calendar(ElectionCalendarKey.N_8_PERIODO_EVALUATIONS_VALIDATION, 30L, 40L);

		assertFalse(AuditorCandidateDecisionWindow.isActive(Arrays.asList(n7, n8), dateAtHour(25L)));
	}

	@Test
	void shouldIgnoreOtherCalendarsAndIncompleteWindows() {
		ElectionCalendar other = calendar(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT, 10L, 40L);
		ElectionCalendar missingEnd = calendar(ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION, 10L, 20L);
		missingEnd.setEndDate(null);

		assertFalse(AuditorCandidateDecisionWindow.isActive(Arrays.asList(other, missingEnd), dateAtHour(15L)));
		assertFalse(AuditorCandidateDecisionWindow.isActive(null, dateAtHour(15L)));
	}

	private ElectionCalendar calendar(ElectionCalendarKey key, long startHour, long endHour) {
		return new ElectionCalendar(null, key, dateAtHour(startHour), dateAtHour(endHour), true);
	}

	private Date dateAtHour(long hour) {
		return new Date(hour * HOUR);
	}
}
