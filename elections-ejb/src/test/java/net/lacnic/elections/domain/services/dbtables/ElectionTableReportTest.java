package net.lacnic.elections.domain.services.dbtables;

import java.util.Date;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionCategory;

 class ElectionTableReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorCopiesElectionStateAndDates() {
		Election election = new Election();
		election.setElectionId(111L);
		election.setDescriptionSpanish("desc es");
		election.setDescriptionEnglish("desc en");
		election.setDescriptionPortuguese("desc pt");
		election.setCreationDate(new Date(1700000000000L));
		election.setVotingPeriodStartDate(new Date(1700000600000L));
		election.setVotingPeriodEndDate(new Date(1700001200000L));
		election.setDiffUTC(0);
		election.setResultLinkAvailable(false);
		election.setVotingLinkAvailable(false);
		election.setDoNominationLinkAvailable(false);
		election.setNominationTasksLinkAvailable(false);
		election.setNominationSupportLinkAvailable(false);
		election.setPublicElectionLinkAvailable(false);
		election.setLinkSpanish("https://es");
		election.setLinkEnglish("https://en");
		election.setLinkPortuguese("https://pt");
		election.setMaxCandidates(14);
		election.setTitleSpanish("titulo es");
		election.setTitleEnglish("title en");
		election.setTitlePortuguese("titulo pt");
		election.setAuditorsSet(true);
		election.setCandidatesSet(false);
		election.setElectorsSet(true);
		election.setAuditorLinkAvailable(true);
		election.setOnlySp(true);
		election.setDefaultSender("no-reply@lacnic.net");
		election.setRandomOrderCandidates(true);
		election.setRevisionRequest(false);
		election.setMigrationId(7L);
		election.setMigrated(false);
		election.setCategory(ElectionCategory.STATUTORY);
		election.setClosed(false);
		election.setClosedDate(new Date(1700001800000L));

		ElectionTableReport report = new ElectionTableReport(election);

		assertEquals(111L, report.getElectionId().longValue());
		assertEquals("desc es", report.getDescriptionSpanish());
		assertEquals("desc en", report.getDescriptionEnglish());
		assertEquals("desc pt", report.getDescriptionPortuguese());
		assertEquals(election.getCreationDateString(), report.getCreationDate());
		assertEquals(election.getVotingPeriodEndDateString(), report.getEndDate());
		assertEquals(election.getVotingPeriodStartDateString(), report.getStartDate());
		assertEquals(Boolean.FALSE, report.getResultLinkAvailable());
		assertEquals(Boolean.FALSE, report.getVotingLinkAvailable());
		assertEquals(Boolean.FALSE, report.getDoNominationLinkAvailable());
		assertEquals(Boolean.FALSE, report.getNominationTasksLinkAvailable());
		assertEquals(Boolean.FALSE, report.getNominationSupportLinkAvailable());
		assertEquals(Boolean.FALSE, report.getPublicElectionLinkAvailable());
		assertEquals("https://es", report.getLinkSpanish());
		assertEquals("https://en", report.getLinkEnglish());
		assertEquals("https://pt", report.getLinkPortuguese());
		assertEquals(Integer.valueOf(14), report.getMaxCandidates());
		assertEquals("titulo es", report.getTitleSpanish());
		assertEquals("title en", report.getTitleEnglish());
		assertEquals("titulo pt", report.getTitlePortuguese());
		assertEquals(Boolean.TRUE, report.getAuditorsSet());
		assertEquals(Boolean.FALSE, report.getCandidatesSet());
		assertEquals(Boolean.TRUE, report.getElectorsSet());
		assertEquals(Boolean.TRUE, report.getAuditorLinkAvailable());
		assertEquals(Boolean.TRUE, report.getOnlySp());
		assertEquals("no-reply@lacnic.net", report.getDefaultSender());
		assertEquals(Boolean.TRUE, report.getRandomOrderCandidates());
		assertEquals(Integer.valueOf(0), report.getDiffUTC());
		assertEquals(Boolean.FALSE, report.getRevisionRequest());
		assertEquals(7L, report.getMigrationId().longValue());
		assertEquals(Boolean.FALSE, report.getMigrated());
		assertEquals(ElectionCategory.STATUTORY.toString(), report.getCategory());
		assertEquals(Boolean.FALSE, report.getClosed());
		assertEquals(election.getClosedDateString(), report.getClosedDate());
	}
}
