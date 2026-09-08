package net.lacnic.elections.utils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.lacnic.elections.domain.pre.ElectionCalendarKey;

public final class ReminderCalendarKeys {

	public static final Set<ElectionCalendarKey> AUDITOR_CANDIDATE_REMINDER_CRON_ALLOWED_CALENDAR_KEYS =
			AuditorCandidateDecisionWindow.ALLOWED_CALENDAR_KEYS;

	public static final Set<ElectionCalendarKey> AUDITOR_REVISION_REMINDER_CRON_ALLOWED_CALENDAR_KEYS =
			Collections.unmodifiableSet(EnumSet.of(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT));

	private ReminderCalendarKeys() {
		throw new IllegalStateException("Utility class");
	}
}
