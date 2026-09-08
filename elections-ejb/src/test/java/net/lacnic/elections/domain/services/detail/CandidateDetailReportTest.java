package net.lacnic.elections.domain.services.detail;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.CandidateStatus;

 class CandidateDetailReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorMapsCandidateFields() {
		Election election = new Election();
		election.setElectionId(700L);

		Candidate candidate = new Candidate();
		candidate.setCandidateId(33L);
		candidate.setElection(election);
		candidate.setMigrationId(4L);
		candidate.setName("Candidato");
		candidate.setMail("cand@example.com");
		candidate.setPictureInfo(new byte[] { 1, 2, 3 });
		candidate.setPictureName("cand.png");
		candidate.setBioSpanish("bio ES");
		candidate.setBioEnglish("bio EN");
		candidate.setBioPortuguese("bio PT");
		candidate.setPictureExtension("png");
		candidate.setCandidateOrder(2);
		candidate.setOnlySp(true);
		candidate.setLinkSpanish("http://es");
		candidate.setLinkEnglish("http://en");
		candidate.setLinkPortuguese("http://pt");
		candidate.setStatus(CandidateStatus.INCOMPLETE);

		CandidateDetailReport report = new CandidateDetailReport(candidate);

		assertEquals(33L, report.getCandidateId().longValue());
		assertEquals(4L, report.getMigrationId().longValue());
		assertEquals("Candidato", report.getName());
		assertEquals("cand@example.com", report.getMail());
		assertEquals("AQID", report.getPictureInfo());
		assertNull(report.getPictureUrl());
		assertEquals("cand.png", report.getPictureName());
		assertEquals("bio ES", report.getBioSpanish());
		assertEquals("bio EN", report.getBioEnglish());
		assertEquals("bio PT", report.getBioPortuguese());
		assertEquals("png", report.getPictureExtension());
		assertEquals(Integer.valueOf(2), report.getCandidateOrder());
		assertEquals(Boolean.TRUE, report.isOnlySp());
		assertEquals("http://es", report.getLinkSpanish());
		assertEquals("http://en", report.getLinkEnglish());
		assertEquals("http://pt", report.getLinkPortuguese());
	}

	@org.junit.jupiter.api.Test

	 void testConstructorBuildsPictureUrlForPublicCandidate() {
		Candidate publishedCandidate = new Candidate();
		publishedCandidate.setCandidateId(12L);
		Election election = new Election();
		election.setElectionId(13L);
		publishedCandidate.setElection(election);
		publishedCandidate.setStatus(CandidateStatus.CONFIRMED_AND_PUBLISHED);

		CandidateDetailReport report = new CandidateDetailReport(publishedCandidate);
		assertNotNull(report.getPictureUrl());
	}
}
