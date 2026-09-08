package net.lacnic.elections.domain.services.dbtables;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Election;

 class AuditorTableReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorCopiesAuditorFields() {
		Election election = new Election();
		election.setElectionId(55L);

		Auditor auditor = new Auditor();
		auditor.setAuditorId(4L);
		auditor.setCommissioner(true);
		auditor.setAgreedConformity(true);
		auditor.setRevisionAvailable(true);
		auditor.setName("Auditor Name");
		auditor.setMail("auditor@example.com");
		auditor.setElection(election);
		auditor.setMigrationId(90L);

		AuditorTableReport report = new AuditorTableReport(auditor);

		assertEquals(4L, report.getAuditorId().longValue());
		assertEquals(Boolean.TRUE, report.getCommissioner());
		assertEquals(Boolean.TRUE, report.getAgreedConformity());
		assertEquals("auditor@example.com", report.getMail());
		assertEquals("Auditor Name", report.getName());
		assertEquals(55L, report.getElectionId().longValue());
		assertEquals(Boolean.TRUE, report.getRevisionAvailable());
		assertEquals(90L, report.getMigrationId().longValue());
	}
}
