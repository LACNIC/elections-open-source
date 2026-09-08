package net.lacnic.elections.utils;

import junit.framework.TestCase;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;

class AuditorCandidateDecisionUtilsTest extends TestCase {

	@org.junit.jupiter.api.Test
	void testPrecompleteWithoutDecisionRequiresAction() {
		assertTrue(AuditorCandidateDecisionUtils.isVisibleCandidateStatusForAuditor(CandidateStatus.PRECOMPLETE));
		assertTrue(AuditorCandidateDecisionUtils.isCandidateActionRequired(CandidateStatus.PRECOMPLETE, null));
	}

	@org.junit.jupiter.api.Test
	void testPrecompleteWithPreDecisionDoesNotRequireAction() {
		AuditorCandidateDecision decision = new AuditorCandidateDecision();
		decision.setPreDecisionStatus(AuditorCandidateDecisionStatus.PREAPPROVED);

		assertFalse(AuditorCandidateDecisionUtils.isCandidateActionRequired(CandidateStatus.PRECOMPLETE, decision));
	}

	@org.junit.jupiter.api.Test
	void testCompleteWithOnlyPreDecisionRequiresFinalAction() {
		AuditorCandidateDecision decision = new AuditorCandidateDecision();
		decision.setPreDecisionStatus(AuditorCandidateDecisionStatus.PREAPPROVED);

		assertTrue(AuditorCandidateDecisionUtils.isVisibleCandidateStatusForAuditor(CandidateStatus.COMPLETE));
		assertTrue(AuditorCandidateDecisionUtils.isCandidateActionRequired(CandidateStatus.COMPLETE, decision));
	}

	@org.junit.jupiter.api.Test
	void testCompleteWithFinalDecisionDoesNotRequireAction() {
		AuditorCandidateDecision decision = new AuditorCandidateDecision();
		decision.setFinalDecisionStatus(AuditorCandidateDecisionStatus.APPROVED);

		assertFalse(AuditorCandidateDecisionUtils.isCandidateActionRequired(CandidateStatus.COMPLETE, decision));
	}

	@org.junit.jupiter.api.Test
	void testPublishedOrRejectedCandidatesDoNotRequireAction() {
		assertTrue(AuditorCandidateDecisionUtils.isVisibleCandidateStatusForAuditor(CandidateStatus.CONFIRMED_AND_PUBLISHED));
		assertTrue(AuditorCandidateDecisionUtils.isVisibleCandidateStatusForAuditor(CandidateStatus.REJECTED));
		assertFalse(AuditorCandidateDecisionUtils.isCandidateActionRequired(CandidateStatus.CONFIRMED_AND_PUBLISHED, null));
		assertFalse(AuditorCandidateDecisionUtils.isCandidateActionRequired(CandidateStatus.REJECTED, null));
	}

	@org.junit.jupiter.api.Test
	void testIncompleteCandidateIsNotVisibleForAuditor() {
		assertFalse(AuditorCandidateDecisionUtils.isVisibleCandidateStatusForAuditor(CandidateStatus.INCOMPLETE));
	}
}
