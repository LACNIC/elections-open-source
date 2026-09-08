package net.lacnic.elections.ejb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Election;

class ElectionsManagerEJBBeanReminderTest {

	private final ElectionsManagerEJBBean bean = new ElectionsManagerEJBBean();

	@Test
	void automaticReminderShouldRequireOpenElectionAndAvailableAuditorLink() {
		Auditor auditor = eligibleAuditor();
		assertTrue(bean.isAutomaticAuditorReminderEligible(auditor));

		auditor.getElection().setClosed(true);
		assertFalse(bean.isAutomaticAuditorReminderEligible(auditor));

		auditor.getElection().setClosed(false);
		auditor.getElection().setAuditorLinkAvailable(false);
		assertFalse(bean.isAutomaticAuditorReminderEligible(auditor));
	}

	@Test
	void automaticRevisionReminderShouldRequirePendingConformityWithoutRevisionRequest() {
		Auditor auditor = eligibleAuditor();
		assertTrue(bean.isAutomaticAuditorRevisionReminderPending(auditor));

		auditor.setAgreedConformity(true);
		assertFalse(bean.isAutomaticAuditorRevisionReminderPending(auditor));

		auditor.setAgreedConformity(false);
		auditor.getElection().setRevisionRequest(true);
		assertFalse(bean.isAutomaticAuditorRevisionReminderPending(auditor));
	}

	@Test
	void manualRevisionReminderEligibilityShouldRemainAvailableDuringRevisionRequest() {
		Auditor auditor = eligibleAuditor();
		auditor.getElection().setRevisionRequest(true);

		assertTrue(bean.isAuditorRevisionReminderPending(auditor));
	}

	@Test
	void safeReminderProcessingShouldContinueAfterAnIsolatedFailure() {
		AtomicInteger processedReminders = new AtomicInteger();

		bean.processReminderSafely("failing reminder", () -> {
			processedReminders.incrementAndGet();
			throw new IllegalStateException("simulated failure");
		});
		bean.processReminderSafely("next reminder", processedReminders::incrementAndGet);

		assertEquals(2, processedReminders.get());
	}

	private Auditor eligibleAuditor() {
		Election election = new Election();
		election.setClosed(false);
		election.setAuditorLinkAvailable(true);
		election.setRevisionRequest(false);

		Auditor auditor = new Auditor();
		auditor.setElection(election);
		auditor.setCommissioner(true);
		auditor.setMail("auditor@example.net");
		auditor.setAgreedConformity(false);
		return auditor;
	}
}
