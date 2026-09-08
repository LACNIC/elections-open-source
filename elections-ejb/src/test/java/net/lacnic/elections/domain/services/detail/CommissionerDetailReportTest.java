package net.lacnic.elections.domain.services.detail;

import junit.framework.TestCase;

 class CommissionerDetailReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorMapsCommissionerFields() {
		net.lacnic.elections.domain.Commissioner commissioner = new net.lacnic.elections.domain.Commissioner();
		commissioner.setCommissionerId(44L);
		commissioner.setName("Juan Pérez");
		commissioner.setMail("jperez@example.com");

		CommissionerDetailReport report = new CommissionerDetailReport(commissioner);

		assertEquals(44L, report.getCommissionerId().longValue());
		assertEquals("Juan Pérez", report.getName());
		assertEquals("jperez@example.com", report.getMail());
	}
}
