package net.lacnic.elections.utils;

import java.util.Date;

import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;

public final class AuditorCandidateDecisionUtils {

	private static final long LEGACY_REJECTED_STAGE_THRESHOLD_MS = 1000L;

	private AuditorCandidateDecisionUtils() {
	}

	public static boolean isVisibleCandidateStatusForAuditor(CandidateStatus status) {
		return status == CandidateStatus.PRECOMPLETE
				|| status == CandidateStatus.CONFIRMED_AND_PUBLISHED
				|| status == CandidateStatus.COMPLETE
				|| status == CandidateStatus.REJECTED;
	}

	public static boolean isCandidateActionRequired(CandidateStatus status, AuditorCandidateDecision decision) {
		return (status == CandidateStatus.PRECOMPLETE && !hasPreStageReaction(decision, status))
				|| (status == CandidateStatus.COMPLETE && !hasFinalStageReaction(decision));
	}

	public static AuditorCandidateDecisionStatus resolveEffectiveAuditorDecisionStatus(AuditorCandidateDecision decision, CandidateStatus candidateStatus) {
		if (decision == null) {
			return null;
		}
		CandidateStatus resolvedCandidateStatus = candidateStatus != null ? candidateStatus : decision.getCandidate() != null ? decision.getCandidate().getStatus() : null;
		if (resolvedCandidateStatus == CandidateStatus.PRECOMPLETE) {
			AuditorCandidateDecisionStatus preStatus = decision.getPreDecisionStatus();
			if (preStatus != null) {
				return preStatus;
			}
			AuditorCandidateDecisionStatus legacyStatus = decision.getDecisionStatus();
			if (legacyStatus == AuditorCandidateDecisionStatus.PREAPPROVED || legacyStatus == AuditorCandidateDecisionStatus.REJECTED) {
				return legacyStatus;
			}
			return AuditorCandidateDecisionStatus.ANALYZING;
		}
		if (resolvedCandidateStatus == CandidateStatus.COMPLETE
				|| resolvedCandidateStatus == CandidateStatus.CONFIRMED_AND_PUBLISHED
				|| resolvedCandidateStatus == CandidateStatus.REJECTED) {
			AuditorCandidateDecisionStatus finalStatus = decision.getFinalDecisionStatus();
			if (finalStatus != null) {
				return finalStatus;
			}
			AuditorCandidateDecisionStatus legacyStatus = decision.getDecisionStatus();
			if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED || legacyStatus == AuditorCandidateDecisionStatus.REJECTED) {
				return legacyStatus;
			}
			return AuditorCandidateDecisionStatus.ANALYZING;
		}
		return decision.getDecisionStatus();
	}

	private static boolean hasPreStageReaction(AuditorCandidateDecision decision, CandidateStatus candidateStatus) {
		if (decision == null) {
			return false;
		}
		AuditorCandidateDecisionStatus status = decision.getPreDecisionStatus();
		if (isReactionStatus(status)) {
			return true;
		}
		AuditorCandidateDecisionStatus legacyStatus = decision.getDecisionStatus();
		if (legacyStatus == AuditorCandidateDecisionStatus.PREAPPROVED) {
			return true;
		}
		if (legacyStatus == AuditorCandidateDecisionStatus.REJECTED
				&& isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate())) {
			return true;
		}
		return candidateStatus == CandidateStatus.PRECOMPLETE
				&& legacyStatus == AuditorCandidateDecisionStatus.REJECTED;
	}

	private static boolean hasFinalStageReaction(AuditorCandidateDecision decision) {
		if (decision == null) {
			return false;
		}
		AuditorCandidateDecisionStatus status = decision.getFinalDecisionStatus();
		if (isReactionStatus(status)) {
			return true;
		}
		AuditorCandidateDecisionStatus legacyStatus = decision.getDecisionStatus();
		return legacyStatus == AuditorCandidateDecisionStatus.APPROVED
				|| (legacyStatus == AuditorCandidateDecisionStatus.REJECTED
						&& decision.getApprovedDate() != null
						&& !isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate()));
	}

	private static boolean isReactionStatus(AuditorCandidateDecisionStatus status) {
		return status == AuditorCandidateDecisionStatus.PREAPPROVED
				|| status == AuditorCandidateDecisionStatus.APPROVED
				|| status == AuditorCandidateDecisionStatus.REJECTED;
	}

	private static boolean isLikelyLegacyPrecompleteRejectedMilestone(Date preapprovedDate, Date approvedDate) {
		if (preapprovedDate == null || approvedDate == null) {
			return false;
		}
		long diffMillis = Math.abs(preapprovedDate.getTime() - approvedDate.getTime());
		return diffMillis <= LEGACY_REJECTED_STAGE_THRESHOLD_MS;
	}
}
