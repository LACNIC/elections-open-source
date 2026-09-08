package net.lacnic.elections.adminweb.ui.results;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.services.publicelection.PublicElectionVoteCountRow;

class ElectionResultsPanelTest {

	@Test
	void shouldCalculatePercentagesLikePublicElectionResults() {
		Candidate first = candidate(10L, "Candidate A", false);
		Candidate second = candidate(20L, "Candidate B", true);
		List<PublicElectionVoteCountRow> voteCountRows = List.of(
				new PublicElectionVoteCountRow(10L, 30L),
				new PublicElectionVoteCountRow(20L, 70L));

		Map<Long, Long> votesByCandidateId = ElectionResultsPanel.buildVotesByCandidateId(voteCountRows);
		long totalVotes = ElectionResultsPanel.calculateTotalVotes(voteCountRows);
		List<ElectionResultsPanel.ResultRow> rows = ElectionResultsPanel.buildResultRows(List.of(first, second), votesByCandidateId, totalVotes, Locale.ENGLISH);

		assertEquals(100L, totalVotes);
		assertEquals("30.0%", rows.get(0).getPercentageLabel());
		assertEquals("70.0%", rows.get(1).getPercentageLabel());
	}

	private static Candidate candidate(long candidateId, String name, boolean winner) {
		Candidate candidate = new Candidate();
		candidate.setCandidateId(candidateId);
		candidate.setName(name);
		candidate.setWinner(winner);
		return candidate;
	}
}
