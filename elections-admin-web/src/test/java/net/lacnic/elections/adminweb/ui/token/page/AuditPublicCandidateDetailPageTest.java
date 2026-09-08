package net.lacnic.elections.adminweb.ui.token.page;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.pre.CandidateStatus;

class AuditPublicCandidateDetailPageTest {

	@Test
	void shouldPreserveCandidateStatusRulesForEachDecisionForm() {
		assertTrue(AuditPublicCandidateDetailPage.isPreDecisionEditable(CandidateStatus.PRECOMPLETE));
		assertFalse(AuditPublicCandidateDetailPage.isPreDecisionEditable(CandidateStatus.COMPLETE));
		assertFalse(AuditPublicCandidateDetailPage.isPreDecisionEditable(CandidateStatus.CONFIRMED_AND_PUBLISHED));
		assertFalse(AuditPublicCandidateDetailPage.isPreDecisionEditable(CandidateStatus.REJECTED));

		assertTrue(AuditPublicCandidateDetailPage.isCompleteDecisionEditable(CandidateStatus.COMPLETE));
		assertFalse(AuditPublicCandidateDetailPage.isCompleteDecisionEditable(CandidateStatus.PRECOMPLETE));
		assertFalse(AuditPublicCandidateDetailPage.isCompleteDecisionEditable(CandidateStatus.CONFIRMED_AND_PUBLISHED));
		assertFalse(AuditPublicCandidateDetailPage.isCompleteDecisionEditable(CandidateStatus.REJECTED));
	}

	@Test
	void shouldShowDecisionFormOnlyWhenWindowIsActive() {
		assertTrue(AuditPublicCandidateDetailPage.shouldShowDecisionForm(true, true, false, true));
		assertFalse(AuditPublicCandidateDetailPage.shouldShowDecisionForm(true, true, false, false));
		assertFalse(AuditPublicCandidateDetailPage.shouldShowDecisionForm(false, true, false, true));
		assertFalse(AuditPublicCandidateDetailPage.shouldShowDecisionForm(true, true, true, true));
	}

	@Test
	void shouldShowAvailabilityOnlyForPendingDecisionOutsideWindow() {
		assertTrue(AuditPublicCandidateDetailPage.isDecisionPendingOutsideWindow(true, true, false, false));
		assertFalse(AuditPublicCandidateDetailPage.isDecisionPendingOutsideWindow(true, true, false, true));
		assertFalse(AuditPublicCandidateDetailPage.isDecisionPendingOutsideWindow(true, true, true, false));
		assertFalse(AuditPublicCandidateDetailPage.isDecisionPendingOutsideWindow(false, true, false, false));
	}
}
