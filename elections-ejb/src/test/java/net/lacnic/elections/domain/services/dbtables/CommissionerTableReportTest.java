package net.lacnic.elections.domain.services.dbtables;

import junit.framework.TestCase;
import net.lacnic.elections.domain.Commissioner;

 class CommissionerTableReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorMapsCommissionerFields() {
		Commissioner commissioner = new Commissioner();
		commissioner.setCommissionerId(20L);
		commissioner.setName("María");
		commissioner.setMail("maria@example.com");

		CommissionerTableReport report = new CommissionerTableReport(commissioner);

		assertEquals(20L, report.getCommissionerId().longValue());
		assertEquals("María", report.getName());
		assertEquals("maria@example.com", report.getMail());
	}
}
