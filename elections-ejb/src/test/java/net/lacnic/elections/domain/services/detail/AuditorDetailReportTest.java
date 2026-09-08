package net.lacnic.elections.domain.services.detail;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Election;

 class AuditorDetailReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorCopiesAuditorFields() {
		Election election = new Election();
		election.setElectionId(500L);

		Auditor auditor = new Auditor();
		auditor.setAuditorId(10L);
		auditor.setCommissioner(true);
		auditor.setAgreedConformity(false);
		auditor.setRevisionAvailable(true);
		auditor.setName("Comisión");
		auditor.setMail("comision@example.com");
		auditor.setElection(election);
		auditor.setMigrationId(22L);

		AuditorDetailReport report = new AuditorDetailReport(auditor);

		assertEquals(10L, report.getAuditorId().longValue());
		assertEquals(Boolean.TRUE, report.getCommissioner());
		assertEquals(Boolean.FALSE, report.getAgreedConformity());
		assertEquals(Boolean.TRUE, report.getRevisionAvailable());
		assertEquals("Comisión", report.getName());
		assertEquals("comision@example.com", report.getMail());
		assertEquals(500L, report.getElectionId().longValue());
		assertEquals(22L, report.getMigrationId().longValue());
	}
}
