package net.lacnic.elections.domain.services.publicelection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.DomainModelTestUtil;

 class PublicElectionPackageBasicTest extends TestCase {
	private static final Class<?>[] PACKAGE_CLASSES = new Class[] {
			net.lacnic.elections.domain.services.publicelection.PublicElectionsSnapshotMetadata.class,
			net.lacnic.elections.domain.services.publicelection.PublicElectionsSnapshot.class,
			net.lacnic.elections.domain.services.publicelection.PublicElectionVoteCountRow.class,
			net.lacnic.elections.domain.services.publicelection.PublicElectionVisibilityReport.class,
			net.lacnic.elections.domain.services.publicelection.PublicElectionSnapshotMetadata.class,
			net.lacnic.elections.domain.services.publicelection.PublicElectionRollSnapshot.class,
			net.lacnic.elections.domain.services.publicelection.PublicElectionPhotoSnapshot.class,
			net.lacnic.elections.domain.services.publicelection.PublicElectionOfficialResultSnapshot.class,
			net.lacnic.elections.domain.services.publicelection.PublicElectionCoreSnapshot.class,
			net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateWorkOrganizationRow.class,
			net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateCountryLinkRow.class
	};

	 static Test suite() {
		return new TestSuite(PublicElectionPackageBasicTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testPublicElectionPackageClassesBasicContract() {
		for (Class<?> c : PACKAGE_CLASSES) {
			DomainModelTestUtil.assertBasicBeanContract(c);
		}
	}
}
