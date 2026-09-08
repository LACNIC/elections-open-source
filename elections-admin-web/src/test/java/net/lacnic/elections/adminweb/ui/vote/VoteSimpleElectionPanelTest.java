package net.lacnic.elections.adminweb.ui.vote;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.CandidateType;

class VoteSimpleElectionPanelTest {

	@Test
	void shouldHideVotePagePublicProfileLinkForAbstentionCandidate() {
		Candidate candidate = new Candidate();
		candidate.setCandidateType(CandidateType.ABSTENTION);

		assertTrue(VoteSimpleElectionPanel.shouldHideVotePagePublicProfileLink(candidate));
	}

	@Test
	void shouldNotHideVotePagePublicProfileLinkForRegularCandidate() {
		Candidate candidate = new Candidate();
		candidate.setCandidateType(CandidateType.NORMAL);

		assertFalse(VoteSimpleElectionPanel.shouldHideVotePagePublicProfileLink(candidate));
	}

	@Test
	void shouldNotHideVotePagePublicProfileLinkWhenCandidateIsMissing() {
		assertFalse(VoteSimpleElectionPanel.shouldHideVotePagePublicProfileLink(null));
	}
}
