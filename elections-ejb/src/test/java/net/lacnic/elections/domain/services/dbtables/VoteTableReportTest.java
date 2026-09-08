package net.lacnic.elections.domain.services.dbtables;

import java.util.Date;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.utils.DateTimeUtils;

 class VoteTableReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorCopiesVoteFieldsAndDate() {
		Election election = new Election();
		election.setElectionId(91L);
		Candidate candidate = new Candidate();
		candidate.setCandidateId(77L);
		candidate.setElection(election);

		Vote vote = new Vote();
		vote.setVoteId(12L);
		vote.setIp("127.0.0.1");
		Date voteDate = new Date(1700003600000L);
		vote.setVoteDate(voteDate);
		vote.setCandidate(candidate);
		vote.setElection(election);

		VoteTableReport report = new VoteTableReport(vote);

		assertEquals(12L, report.getVoteId().longValue());
		assertEquals("127.0.0.1", report.getIp());
		assertEquals(77L, report.getCandidateId().longValue());
		assertEquals(91L, report.getElectionId().longValue());
		assertEquals(DateTimeUtils.getTableServicesDateTimeString(voteDate), report.getVoteDate());
	}
}
