package net.lacnic.elections.domain.services.detail;

import java.util.Date;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Election;

 class OrganizationElectionDetailReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorMapsElectionData() {
		Election election = new Election();
		election.setElectionId(300L);
		election.setTitleEnglish("EN");
		election.setTitleSpanish("ES");
		election.setTitlePortuguese("PT");
		election.setVotingPeriodStartDate(new Date(1700000000000L));
		election.setVotingPeriodEndDate(new Date(1700000040000L));

		OrganizationElectionDetailReport report = new OrganizationElectionDetailReport(election);

		assertEquals(300L, report.getElectionId().longValue());
		assertEquals("EN", report.getElectionTitleEN());
		assertEquals("ES", report.getElectionTitleES());
		assertEquals("PT", report.getElectionTitleSP());
		assertEquals(election.getVotingPeriodStartDateString(), report.getStartDate());
		assertEquals(election.getVotingPeriodEndDateString(), report.getEndDate());
	}
}
