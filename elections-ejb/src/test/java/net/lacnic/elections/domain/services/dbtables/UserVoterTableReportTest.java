package net.lacnic.elections.domain.services.dbtables;

import java.util.Date;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.DateTimeUtils;

 class UserVoterTableReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorCopiesVoterDataAndFormatsVoteDate() {
		Election election = new Election();
		election.setElectionId(44L);

		UserVoter voter = new UserVoter();
		voter.setUserVoterId(66L);
		voter.setMigrationId(5L);
		voter.setElection(election);
		voter.setCountry("AR");
		voter.setLanguage("EN");
		voter.setMail("voter@example.com");
		voter.setName("Voter Name");
		voter.setOrgID("org-10");
		voter.setOrgName("Org Name");
		voter.setVoteAmount(2);
		voter.setVoted(true);
		Date voteDate = new Date(1700003000000L);
		voter.setVoteDate(voteDate);

		UserVoterTableReport report = new UserVoterTableReport(voter);

		assertEquals(66L, report.getUserVoterId().longValue());
		assertEquals(5L, report.getMigrationId().longValue());
		assertEquals(44L, report.getElectionId().longValue());
		assertEquals("AR", report.getCountry());
		assertEquals("EN", report.getLanguage());
		assertEquals("voter@example.com", report.getMail());
		assertEquals("Voter Name", report.getName());
		assertEquals("ORG-10", report.getOrgID());
		assertEquals("Org Name", report.getOrgName());
		assertEquals(Integer.valueOf(2), report.getVoteAmount());
		assertEquals(Boolean.TRUE, report.getVoted());
		assertEquals(DateTimeUtils.getTableServicesDateTimeString(voteDate), report.getVoteDate());
	}
}
