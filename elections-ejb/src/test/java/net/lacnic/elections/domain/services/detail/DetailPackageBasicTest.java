package net.lacnic.elections.domain.services.detail;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.DomainModelTestUtil;

 class DetailPackageBasicTest extends TestCase {
	private static final Class<?>[] PACKAGE_CLASSES = new Class[] {
			net.lacnic.elections.domain.services.detail.UserVoterDetailReport.class,
			net.lacnic.elections.domain.services.detail.OrganizationVoterDetailReport.class,
			net.lacnic.elections.domain.services.detail.OrganizationElectionDetailReport.class,
			net.lacnic.elections.domain.services.detail.OrganizationDebtorImportResult.class,
			net.lacnic.elections.domain.services.detail.ElectionParticipationDetailReport.class,
			net.lacnic.elections.domain.services.detail.ElectionDetailReport.class,
			net.lacnic.elections.domain.services.detail.CommissionerDetailReport.class,
			net.lacnic.elections.domain.services.detail.CandidateDetailReport.class,
			net.lacnic.elections.domain.services.detail.AuditorDetailReport.class
	};

	 static Test suite() {
		return new TestSuite(DetailPackageBasicTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testDetailPackageClassesBasicContract() {
		for (Class<?> c : PACKAGE_CLASSES) {
			DomainModelTestUtil.assertBasicBeanContract(c);
		}
	}
}

