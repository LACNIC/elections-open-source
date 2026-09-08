package net.lacnic.elections.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CandidatePhotoBatchResultTest {

	@Test
	void shouldAccumulateCountersAndBytes() {
		CandidatePhotoBatchResult result = new CandidatePhotoBatchResult();
		result.setTotalCandidates(10);
		result.incrementUpdatedCandidates();
		result.incrementSkippedCandidates();
		result.incrementFailedCandidates();
		result.addOriginalBytes(150);
		result.addOriginalBytes(-10);
		result.addResultingBytes(90);
		result.addResultingBytes(0);

		assertEquals(10, result.getTotalCandidates());
		assertEquals(1, result.getUpdatedCandidates());
		assertEquals(1, result.getSkippedCandidates());
		assertEquals(1, result.getFailedCandidates());
		assertEquals(150, result.getOriginalTotalBytes());
		assertEquals(90, result.getResultingTotalBytes());
		assertEquals(60, result.getSavedBytes());
	}
}
