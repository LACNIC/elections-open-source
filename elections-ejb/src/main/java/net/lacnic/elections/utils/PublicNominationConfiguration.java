package net.lacnic.elections.utils;

import net.lacnic.elections.domain.ElectionType;

public final class PublicNominationConfiguration {

	private PublicNominationConfiguration() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isEnabled(String configuredValue, ElectionType electionType) {
		if (configuredValue == null || configuredValue.isBlank()) {
			return isLegacyEnabledType(electionType);
		}
		if (configuredValue.equalsIgnoreCase(Boolean.TRUE.toString())) {
			return true;
		}
		if (configuredValue.equalsIgnoreCase(Boolean.FALSE.toString())) {
			return false;
		}
		return isLegacyEnabledType(electionType);
	}

	private static boolean isLegacyEnabledType(ElectionType electionType) {
		return electionType == ElectionType.IANA || electionType == ElectionType.ASO;
	}
}
