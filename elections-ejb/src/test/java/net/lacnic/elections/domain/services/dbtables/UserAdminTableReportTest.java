package net.lacnic.elections.domain.services.dbtables;

import junit.framework.TestCase;
import net.lacnic.elections.domain.UserAdmin;

 class UserAdminTableReportTest extends TestCase {

	@org.junit.jupiter.api.Test

	 void testConstructorMapsUserAdminAndPasswordIsExcludedFromSource() {
		UserAdmin userAdmin = new UserAdmin("ADMIN-01", "secret", "ADMIN@EXAMPLE.COM");
		UserAdminTableReport report = new UserAdminTableReport(userAdmin);

		assertEquals("admin-01", report.getUserAdminId());
		assertEquals("admin@example.com", report.getEmail());
		assertNull(report.getPassword());

		report.setPassword("new-pass");
		assertEquals("new-pass", report.getPassword());
	}
}
