package net.lacnic.elections.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CandidateBioMigrationBatchResultTest {

	@Test
	void shouldAccumulateCandidateCounters() {
		CandidateBioMigrationBatchResult result = new CandidateBioMigrationBatchResult();
		result.setTotalCandidates(5);
		result.incrementUpdatedCandidates();
		result.incrementSkippedCandidates();
		result.incrementFailedCandidates();

		assertEquals(5, result.getTotalCandidates());
		assertEquals(1, result.getUpdatedCandidates());
		assertEquals(1, result.getSkippedCandidates());
		assertEquals(1, result.getFailedCandidates());
	}
}
