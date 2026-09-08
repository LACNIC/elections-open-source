package net.lacnic.elections.domain.services.detail;

import java.util.Date;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.DateTimeUtils;

 class UserVoterDetailReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorMapsUserVoterFields() {
		Election election = new Election();
		election.setElectionId(401L);

		UserVoter userVoter = new UserVoter();
		userVoter.setUserVoterId(88L);
		userVoter.setMigrationId(9L);
		userVoter.setElection(election);
		userVoter.setCountry("BR");
		userVoter.setLanguage("PT");
		userVoter.setMail("u@x.com");
		userVoter.setName("Usuaria");
		userVoter.setOrgID("ORG88");
		userVoter.setOrgName("Org 88");
		userVoter.setVoteAmount(4);
		userVoter.setVoted(false);
		Date voteDate = new Date(1700000060000L);
		userVoter.setVoteDate(voteDate);

		UserVoterDetailReport report = new UserVoterDetailReport(userVoter);

		assertEquals(88L, report.getUserVoterId().longValue());
		assertEquals(9L, report.getMigrationId().longValue());
		assertEquals(401L, report.getElectionId().longValue());
		assertEquals("BR", report.getCountry());
		assertEquals("PT", report.getLanguage());
		assertEquals("u@x.com", report.getMail());
		assertEquals("Usuaria", report.getName());
		assertEquals("ORG88", report.getOrgID());
		assertEquals("Org 88", report.getOrgName());
		assertEquals(Integer.valueOf(4), report.getVoteAmount());
		assertEquals(Boolean.FALSE, report.getVoted());
		assertEquals(DateTimeUtils.getTableServicesDateTimeString(voteDate), report.getVoteDate());
	}
}
