package net.lacnic.elections.domain;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

 class ElectionRestrictedCountryBehaviorTest extends TestCase {

	 static Test suite() {
		return new TestSuite(ElectionRestrictedCountryBehaviorTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testConstructorAndFieldAccessors() {
		Election election = new Election();
		ElectionRestrictedCountry restrictedCountry = new ElectionRestrictedCountry(election, "AR");

		assertEquals(election, restrictedCountry.getElection());
		assertEquals("AR", restrictedCountry.getCountryCode());

		restrictedCountry.setCountryCode("BR");
		assertEquals("BR", restrictedCountry.getCountryCode());
	}

	@org.junit.jupiter.api.Test

	 void testSetElectionAndCountryCode() {
		Election election = new Election();
		ElectionRestrictedCountry restrictedCountry = new ElectionRestrictedCountry();
		restrictedCountry.setElection(election);
		restrictedCountry.setCountryCode("uy");

		assertEquals(election, restrictedCountry.getElection());
		assertEquals("uy", restrictedCountry.getCountryCode());
	}
}
