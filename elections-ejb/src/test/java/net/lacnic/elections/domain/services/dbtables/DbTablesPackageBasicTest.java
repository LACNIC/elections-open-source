package net.lacnic.elections.domain.services.dbtables;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.DomainModelTestUtil;

 class DbTablesPackageBasicTest extends TestCase {
	private static final Class<?>[] PACKAGE_CLASSES = new Class[] {
			net.lacnic.elections.domain.services.dbtables.VoteTableReport.class,
			net.lacnic.elections.domain.services.dbtables.UserVoterTableReport.class,
			net.lacnic.elections.domain.services.dbtables.UserAdminTableReport.class,
			net.lacnic.elections.domain.services.dbtables.EmailTableReport.class,
			net.lacnic.elections.domain.services.dbtables.ElectionTableReport.class,
			net.lacnic.elections.domain.services.dbtables.ElectionEmailTemplateTableReport.class,
			net.lacnic.elections.domain.services.dbtables.CustomizationTableReport.class,
			net.lacnic.elections.domain.services.dbtables.CommissionerTableReport.class,
			net.lacnic.elections.domain.services.dbtables.CandidateTableReport.class,
			net.lacnic.elections.domain.services.dbtables.AuditorTableReport.class,
			net.lacnic.elections.domain.services.dbtables.ActivityTableReport.class
	};

	 static Test suite() {
		return new TestSuite(DbTablesPackageBasicTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testDbTablesPackageClassesBasicContract() {
		for (Class<?> c : PACKAGE_CLASSES) {
			DomainModelTestUtil.assertBasicBeanContract(c);
		}
	}
}

