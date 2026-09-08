package net.lacnic.elections.domain;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.DomainModelTestUtil;

 class DomainPackageBasicTest extends TestCase {
	private static final Class<?>[] PACKAGE_CLASSES = new Class[] {
			net.lacnic.elections.domain.ActivityType.class,
			net.lacnic.elections.domain.ElectionType.class,
			net.lacnic.elections.domain.Vote.class,
			net.lacnic.elections.domain.Activity.class,
			net.lacnic.elections.domain.ElectionRestrictedCountry.class,
			net.lacnic.elections.domain.ElectionLinkRecoveryMode.class,
			net.lacnic.elections.domain.ElectionLight.class,
			net.lacnic.elections.domain.ReminderFrequency.class,
			net.lacnic.elections.domain.ElectionEmailTemplate.class,
			net.lacnic.elections.domain.RecipientType.class,
			net.lacnic.elections.domain.ElectionCategory.class,
			net.lacnic.elections.domain.Parameter.class,
			net.lacnic.elections.domain.LanguageCode.class,
			net.lacnic.elections.domain.Customization.class,
			net.lacnic.elections.domain.JointElection.class,
			net.lacnic.elections.domain.Commissioner.class,
			net.lacnic.elections.domain.IpAccess.class,
			net.lacnic.elections.domain.CandidateType.class,
			net.lacnic.elections.domain.EmailHistory.class,
			net.lacnic.elections.domain.Email.class,
			net.lacnic.elections.domain.Candidate.class,
			net.lacnic.elections.domain.Auditor.class
	};

	 static Test suite() {
		return new TestSuite(DomainPackageBasicTest.class);
	}

	 void testDomainPackageClassesBasicContract() {
		for (Class<?> c : PACKAGE_CLASSES) {
			DomainModelTestUtil.assertBasicBeanContract(c);
		}
	}
}
