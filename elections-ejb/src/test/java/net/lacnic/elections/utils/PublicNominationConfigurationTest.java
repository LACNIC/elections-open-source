package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.ElectionType;

class PublicNominationConfigurationTest {

	@Test
	void explicitTrueEnablesEveryElectionType() {
		assertTrue(PublicNominationConfiguration.isEnabled("true", ElectionType.OTHER));
		assertTrue(PublicNominationConfiguration.isEnabled("TRUE", ElectionType.BOARD));
	}

	@Test
	void explicitFalseDisablesEveryElectionType() {
		assertFalse(PublicNominationConfiguration.isEnabled("false", ElectionType.IANA));
		assertFalse(PublicNominationConfiguration.isEnabled("FALSE", ElectionType.ASO));
	}

	@Test
	void missingOrInvalidValuePreservesLegacyTypes() {
		assertTrue(PublicNominationConfiguration.isEnabled(null, ElectionType.IANA));
		assertTrue(PublicNominationConfiguration.isEnabled("", ElectionType.ASO));
		assertFalse(PublicNominationConfiguration.isEnabled(null, ElectionType.OTHER));
		assertFalse(PublicNominationConfiguration.isEnabled("invalid", ElectionType.OTHER));
	}
}
