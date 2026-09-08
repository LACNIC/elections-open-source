package net.lacnic.elections.domain;

public enum ElectionLinkRecoveryMode {

	NONE,
	ONLY_BR,
	ALL;

	public boolean isNone() {
		return this == NONE;
	}

	public boolean isOnlyBr() {
		return this == ONLY_BR;
	}

	public static ElectionLinkRecoveryMode defaultForElectionType(ElectionType electionType) {
		return ALL;
	}
}
