package net.lacnic.elections.publicelection;

public final class PublicElectionStageCodes {

	public static final String CE_AUDIT = "CE_AUDIT";
	public static final String OFFICIAL_NO_CLAIMS = "OFFICIAL_NO_CLAIMS";
	public static final String OFFICIAL_WITH_CLAIMS = "OFFICIAL_WITH_CLAIMS";
	public static final String PROVISIONAL = "PROVISIONAL";
	public static final String VOTER_AUDIT = "VOTER_AUDIT";

	private PublicElectionStageCodes() {
		throw new IllegalStateException("Utility class");
	}
}
