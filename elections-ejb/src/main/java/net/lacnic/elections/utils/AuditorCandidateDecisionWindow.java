package net.lacnic.elections.utils;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;

public final class AuditorCandidateDecisionWindow {

	public static final Set<ElectionCalendarKey> ALLOWED_CALENDAR_KEYS =
			Collections.unmodifiableSet(EnumSet.of(
					ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION,
					ElectionCalendarKey.N_8_PERIODO_EVALUATIONS_VALIDATION));

	private AuditorCandidateDecisionWindow() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isActive(Iterable<ElectionCalendar> calendars, Date referenceDate) {
		if (calendars == null || referenceDate == null) {
			return false;
		}
		for (ElectionCalendar calendar : calendars) {
			if (isActive(calendar, referenceDate)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isActive(ElectionCalendar calendar, Date referenceDate) {
		if (calendar == null
				|| !ALLOWED_CALENDAR_KEYS.contains(calendar.getCalendarKey())
				|| calendar.getStartDate() == null
				|| calendar.getEndDate() == null) {
			return false;
		}
		return !referenceDate.before(calendar.getStartDate())
				&& !referenceDate.after(calendar.getEndDate());
	}
}
