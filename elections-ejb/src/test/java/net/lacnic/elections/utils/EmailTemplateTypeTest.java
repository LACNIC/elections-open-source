package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmailTemplateTypeTest {

	@Test
	void fromKeyShouldReturnNullForBlankInput() {
		assertNull(EmailTemplateType.fromKey(null));
		assertNull(EmailTemplateType.fromKey("   "));
	}

	@Test
	void fromKeyShouldResolveKnownKey() {
		assertEquals(EmailTemplateType.VOTE_RESULT, EmailTemplateType.fromKey("VOTE_RESULT"));
		assertEquals(EmailTemplateType.CANDIDATE_CONFIRMED_AND_PUBLISHED,
				EmailTemplateType.fromKey("CANDIDATE_CONFIRMED_AND_PUBLISHED"));
		assertEquals(EmailTemplateType.NOMINATION_ORG_CANDIDATES_INVITATION_NOTA_NO_VOTA,
				EmailTemplateType.fromKey("NOMINATION_ORG_CANDIDATES_INVITATION_NOTA_NO_VOTA"));
		assertEquals(EmailTemplateType.AUDITOR_REMINDER_REVISION,
				EmailTemplateType.fromKey("AUDITOR_REMINDER_REVISION"));
	}

	@Test
	void isContainedInShouldVerifyByCodeInText() {
		assertTrue(EmailTemplateType.CANDIDATE_CONFIRMED_AND_PUBLISHED
				.isContainedIn("Notificación: CANDIDATE_CONFIRMED_AND_PUBLISHED enviará correo."));
	}

	@Test
	void resolveStandardDispatchEventTypeShouldMapKnownType() {
		assertEquals("NOMINATION_SUBMITTED", EmailTemplateType.NOMINATION_SUBMITTED_NOMINEE.resolveStandardDispatchEventType());
		assertEquals("SUPPORT_REMINDER", EmailTemplateType.SUPPORT_REMINDER.resolveStandardDispatchEventType());
		assertEquals("AUDITOR_REMINDER_REVISION", EmailTemplateType.AUDITOR_REMINDER_REVISION.resolveStandardDispatchEventType());
	}

	@Test
	void resolveStandardDispatchEventTypeShouldFallbackToKey() {
		assertEquals("NEW", EmailTemplateType.NEW.resolveStandardDispatchEventType());
	}

	@Test
	void shouldSendStandardDispatchShouldRespectDefinition() {
		assertEquals(false, EmailTemplateType.NEW.shouldSendStandardDispatch());
		assertEquals(false, EmailTemplateType.STANDARD_DISPATCH_NOTICE.shouldSendStandardDispatch());
	}

	@Test
	void shouldShowSendButtonShouldBeConfigured() {
		assertEquals(true, EmailTemplateType.NEW.shouldShowSendButton());
		assertEquals(false, EmailTemplateType.CANDIDATE_REMINDER.shouldShowSendButton());
		assertEquals(false, EmailTemplateType.AUDITOR_REMINDER_REVISION.shouldShowSendButton());
	}

	@Test
	void isPrioritizedShouldRespectDefinition() {
		assertEquals(false, EmailTemplateType.NEW.isPrioritized());
		assertEquals(true, EmailTemplateType.VOTE_CODES.isPrioritized());
		assertEquals(true, EmailTemplateType.AUDITOR_REMINDER_REVISION.isPrioritized());
	}
}
