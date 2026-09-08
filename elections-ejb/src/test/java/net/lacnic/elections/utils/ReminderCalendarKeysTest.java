package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.pre.ElectionCalendarKey;

class ReminderCalendarKeysTest {

	@Test
	void candidateReminderShouldUseCandidateAuditWindows() {
		assertEquals(Set.of(
				ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION,
				ElectionCalendarKey.N_8_PERIODO_EVALUATIONS_VALIDATION),
				ReminderCalendarKeys.AUDITOR_CANDIDATE_REMINDER_CRON_ALLOWED_CALENDAR_KEYS);
	}

	@Test
	void revisionReminderShouldUseElectionAuditWindow() {
		assertEquals(Set.of(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT),
				ReminderCalendarKeys.AUDITOR_REVISION_REMINDER_CRON_ALLOWED_CALENDAR_KEYS);
	}
}
