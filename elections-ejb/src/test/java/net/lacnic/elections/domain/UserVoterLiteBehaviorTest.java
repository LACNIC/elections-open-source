package net.lacnic.elections.domain;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

 class UserVoterLiteBehaviorTest extends TestCase {

	 static Test suite() {
		return new TestSuite(UserVoterLiteBehaviorTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testGetOrgIDUppercases() {
		UserVoterLite userVoterLite = new UserVoterLite();
		userVoterLite.setOrgID("ab-cd");
		assertEquals("AB-CD", userVoterLite.getOrgID());

		userVoterLite.setOrgID(null);
		assertNull(userVoterLite.getOrgID());
	}
}
