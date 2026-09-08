package net.lacnic.elections.domain.services.detail;

import java.util.Arrays;
import java.util.Date;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.UserVoter;

 class ElectionDetailReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorBuildsAllStateAndNestedCollections() {
		Election election = new Election();
		election.setElectionId(100L);
		election.setCategory(ElectionCategory.STATUTORY);
		election.setDescriptionSpanish("Descripcion ES");
		election.setDescriptionEnglish("Description EN");
		election.setDescriptionPortuguese("Descricao PT");
		election.setCreationDate(new Date(1700000000000L));
		election.setVotingPeriodStartDate(new Date(1700000100000L));
		election.setVotingPeriodEndDate(new Date(1700000200000L));
		election.setLinkSpanish("https://es");
		election.setLinkEnglish("https://en");
		election.setLinkPortuguese("https://pt");
		election.setTitleSpanish("Título ES");
		election.setTitleEnglish("Title EN");
		election.setTitlePortuguese("Título PT");
		election.setAuditorsSet(true);
		election.setCandidatesSet(true);
		election.setElectorsSet(false);
		election.setAuditorLinkAvailable(true);
		election.setOnlySp(false);
		election.setDefaultSender("no-reply@lacnic.net");
		election.setRandomOrderCandidates(false);
		election.setDiffUTC(0);
		election.setRevisionRequest(true);
		election.setMigrationId(500L);
		election.setMigrated(true);
		election.setMaxCandidates(9);
		election.setClosed(true);
		election.setClosedDate(new Date(1700000300000L));

		Auditor auditor = new Auditor();
		auditor.setAuditorId(1L);
		auditor.setName("Auditor Uno");
		auditor.setMail("aud1@example.com");
		auditor.setElection(election);
		auditor.setCommissioner(false);
		Auditor commissioner = new Auditor();
		commissioner.setAuditorId(2L);
		commissioner.setName("Commissioner Uno");
		commissioner.setMail("comm@example.com");
		commissioner.setElection(election);
		commissioner.setCommissioner(true);
		election.setAuditors(Arrays.asList(auditor, commissioner));

		Candidate candidate = new Candidate();
		candidate.setCandidateId(3L);
		candidate.setElection(election);
		candidate.setName("Cand1");
		candidate.setMail("cand1@example.com");
		election.setCandidates(Arrays.asList(candidate));

		UserVoter userVoter = new UserVoter();
		userVoter.setUserVoterId(4L);
		userVoter.setElection(election);
		userVoter.setName("Voter");
		userVoter.setMail("voter@example.com");
		userVoter.setCountry("AR");
		userVoter.setLanguage("ES");
		userVoter.setVoteAmount(2);
		userVoter.setVoted(true);
		userVoter.setVoteDate(new Date(1700000250000L));
		userVoter.setOrgID("org1");
		userVoter.setOrgName("Org Uno");
		election.setUserVoters(Arrays.asList(userVoter));

		ElectionDetailReport report = new ElectionDetailReport(election);

		assertEquals(100L, report.getElectionId().longValue());
		assertEquals("Descripcion ES", report.getDescriptionSpanish());
		assertEquals("Description EN", report.getDescriptionEnglish());
		assertEquals("Descricao PT", report.getDescriptionPortuguese());
		assertEquals(election.getCreationDateString(), report.getCreationDate());
		assertEquals(election.getVotingPeriodStartDateString(), report.getStartDate());
		assertEquals(election.getVotingPeriodEndDateString(), report.getEndDate());
		assertEquals(Boolean.TRUE, report.getAuditorsSet());
		assertEquals(Boolean.TRUE, report.getCandidatesSet());
		assertEquals(Boolean.FALSE, report.getElectorsSet());
		assertEquals(Boolean.TRUE, report.getAuditorLinkAvailable());
		assertEquals(Boolean.FALSE, report.getOnlySp());
		assertEquals("no-reply@lacnic.net", report.getDefaultSender());
		assertEquals(Boolean.FALSE, report.getRandomOrderCandidates());
		assertEquals(Integer.valueOf(0), report.getDiffUTC());
		assertEquals(Boolean.TRUE, report.getRevisionRequest());
		assertEquals(500L, report.getMigrationId().longValue());
		assertEquals(Boolean.TRUE, report.getMigrated());
		assertEquals(Integer.valueOf(9), report.getMaxCandidates());
		assertEquals(ElectionCategory.STATUTORY.toString(), report.getCategory());
		assertEquals(Boolean.TRUE, report.getClosed());
		assertEquals(election.getClosedDateString(), report.getClosedDate());
		assertEquals(1, report.getAuditors().size());
		assertEquals("auditor uno", report.getAuditors().get(0).getName().toLowerCase());
		assertEquals(1, report.getCommissioners().size());
		assertEquals("commissioner uno", report.getCommissioners().get(0).getName().toLowerCase());
		assertEquals(1, report.getCandidates().size());
		assertEquals("cand1", report.getCandidates().get(0).getName().toLowerCase());
		assertEquals(1, report.getUserVoters().size());
		assertEquals("voter", report.getUserVoters().get(0).getName().toLowerCase());
	}
}
