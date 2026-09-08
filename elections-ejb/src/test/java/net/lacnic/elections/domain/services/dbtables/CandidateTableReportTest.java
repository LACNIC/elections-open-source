package net.lacnic.elections.domain.services.dbtables;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.CandidateStatus;

 class CandidateTableReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorMapsCandidateFields() {
		Election election = new Election();
		election.setElectionId(33L);

		Candidate candidate = new Candidate();
		candidate.setCandidateId(44L);
		candidate.setMigrationId(11L);
		candidate.setElection(election);
		candidate.setName("John Doe");
		candidate.setMail("john@example.com");
		candidate.setPictureInfo(new byte[] { 1, 2, 3 });
		candidate.setPictureName("john.png");
		candidate.setBioSpanish("biografía");
		candidate.setBioEnglish("bio en");
		candidate.setBioPortuguese("bio pt");
		candidate.setPictureExtension("png");
		candidate.setCandidateOrder(4);
		candidate.setOnlySp(false);
		candidate.setLinkSpanish("https://es");
		candidate.setLinkEnglish("https://en");
		candidate.setLinkPortuguese("https://pt");
		candidate.setStatus(CandidateStatus.INCOMPLETE);

		CandidateTableReport report = new CandidateTableReport(candidate);

		assertEquals(44L, report.getCandidateId().longValue());
		assertEquals(11L, report.getMigrationId().longValue());
		assertEquals("John Doe", report.getName());
		assertEquals("john@example.com", report.getMail());
		assertEquals(33L, report.getElectionId().longValue());
		assertEquals("AQID", report.getPictureInfo());
		assertEquals("john.png", report.getPictureName());
		assertEquals("biografía", report.getBioSpanish());
		assertEquals("bio en", report.getBioEnglish());
		assertEquals("bio pt", report.getBioPortuguese());
		assertEquals("png", report.getPictureExtension());
		assertEquals(Integer.valueOf(4), report.getCandidateOrder());
		assertFalse(report.isOnlySp());
		assertEquals("https://es", report.getLinkSpanish());
		assertEquals("https://en", report.getLinkEnglish());
		assertEquals("https://pt", report.getLinkPortuguese());
		assertNull(report.getPictureUrl());
	}
}
