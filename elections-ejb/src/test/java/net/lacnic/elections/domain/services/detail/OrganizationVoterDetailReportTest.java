package net.lacnic.elections.domain.services.detail;

import java.util.Date;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.DateTimeUtils;

 class OrganizationVoterDetailReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorMapsNestedElectionAndVoterFields() {
		Election election = new Election();
		election.setElectionId(301L);
		election.setVotingPeriodStartDate(new Date(1700000000000L));
		election.setVotingPeriodEndDate(new Date(1700000040000L));

		UserVoter voter = new UserVoter();
		voter.setUserVoterId(77L);
		voter.setElection(election);
		voter.setVoted(true);
		voter.setName("Elector");
		voter.setMail("elector@x.com");
		voter.setOrgID("ORG1");
		voter.setOrgName("Org Name");
		Date voteDate = new Date(1700000050000L);
		voter.setVoteDate(voteDate);

		OrganizationVoterDetailReport report = new OrganizationVoterDetailReport(voter);

		assertEquals(77L, report.getUserVoterId().longValue());
		assertEquals(Boolean.TRUE, report.getVoted());
		assertEquals("Elector", report.getName());
		assertEquals("elector@x.com", report.getMail());
		assertEquals("ORG1", report.getOrgID());
		assertEquals("Org Name", report.getOrgName());
		assertEquals(DateTimeUtils.getTableServicesDateTimeString(voteDate), report.getVoteDate());
		assertNotNull(report.getElection());
		assertEquals(301L, report.getElection().getElectionId().longValue());
	}
}
