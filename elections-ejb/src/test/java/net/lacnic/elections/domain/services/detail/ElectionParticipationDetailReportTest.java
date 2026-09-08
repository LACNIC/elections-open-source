package net.lacnic.elections.domain.services.detail;

import java.util.Date;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.DateTimeUtils;

 class ElectionParticipationDetailReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorFromAuditorSetsCommissionerRoleAndElectionMetadata() {
		Election election = new Election();
		election.setElectionId(200L);
		election.setCategory(ElectionCategory.STATUTORY);
		election.setTitleEnglish("Title EN");
		election.setTitleSpanish("Titulo ES");
		election.setTitlePortuguese("Título PT");
		election.setDescriptionEnglish("Desc EN");
		election.setDescriptionSpanish("Desc ES");
		election.setDescriptionPortuguese("Desc PT");
		election.setVotingPeriodStartDate(new Date(1700000000000L));
		election.setVotingPeriodEndDate(new Date(1700000010000L));

		Auditor auditor = new Auditor();
		auditor.setElection(election);
		auditor.setName("A");
		auditor.setCommissioner(true);

		ElectionParticipationDetailReport report = new ElectionParticipationDetailReport(auditor);
		assertEquals(200L, report.getElectionId().longValue());
		assertEquals("Commissioner", report.getRole());
		assertEquals(election.getVotingPeriodStartDateString(), report.getStartDate());
		assertEquals(election.getVotingPeriodEndDateString(), report.getEndDate());
		assertEquals(election.getTitleEnglish(), report.getElectionTitleEN());
		assertEquals(election.getTitleSpanish(), report.getElectionTitleES());
		assertEquals(election.getTitlePortuguese(), report.getElectionTitleSP());
		assertEquals("Commissioner", report.getRole());
	}

	@org.junit.jupiter.api.Test

	 void testConstructorFromCandidateSetsCandidateData() {
		Election election = new Election();
		election.setCategory(ElectionCategory.STATUTORY);
		election.setVotingPeriodStartDate(new Date(1700000000000L));
		election.setVotingPeriodEndDate(new Date(1700000010000L));
		Candidate candidate = new Candidate();
		candidate.setElection(election);
		candidate.setBioSpanish("Bio ES");
		candidate.setLinkSpanish("http://link");

		ElectionParticipationDetailReport report = new ElectionParticipationDetailReport(candidate);
		assertEquals("Candidate", report.getRole());
		assertEquals("Bio ES", report.getCandidateBio());
		assertEquals("http://link", report.getCandidateLink());
	}

	@org.junit.jupiter.api.Test

	 void testConstructorFromUserVoterSetsVoterInfo() {
		Election election = new Election();
		election.setCategory(ElectionCategory.STATUTORY);
		election.setVotingPeriodStartDate(new Date(1700000000000L));
		election.setVotingPeriodEndDate(new Date(1700000010000L));
		UserVoter userVoter = new UserVoter();
		userVoter.setElection(election);
		userVoter.setName("V");
		userVoter.setLanguage("ES");
		userVoter.setVoted(true);
		userVoter.setVoteAmount(10);
		userVoter.setVoteDate(new Date(1700000020000L));
		userVoter.setOrgID("ORG");
		userVoter.setCountry("AR");
		userVoter.setMail("v@x.com");

		ElectionParticipationDetailReport report = new ElectionParticipationDetailReport(userVoter);
		assertEquals("Voter", report.getRole());
		assertEquals("ORG", report.getVoterOrgID());
		assertEquals("AR", report.getVoterCountry());
		assertEquals("SP", report.getVoterLanguage());
		assertEquals(10, report.getVoterVoteAmount());
		assertEquals(true, report.isVoterVoted());
		assertEquals(DateTimeUtils.getTableServicesDateTimeString(new Date(1700000020000L)), report.getVoterVoteDate());
	}
}
