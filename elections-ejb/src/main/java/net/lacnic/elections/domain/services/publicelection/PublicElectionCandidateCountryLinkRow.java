package net.lacnic.elections.domain.services.publicelection;

import java.io.Serializable;

public class PublicElectionCandidateCountryLinkRow implements Serializable {

	private static final long serialVersionUID = -7169036409913560788L;

	private final long candidateId;
	private final boolean primaryCountry;
	private final String countryCode;

	public PublicElectionCandidateCountryLinkRow(Long candidateId, Boolean primaryCountry, String countryCode) {
		this(candidateId != null ? candidateId.longValue() : 0L,
				Boolean.TRUE.equals(primaryCountry),
				countryCode);
	}

	public PublicElectionCandidateCountryLinkRow(long candidateId, boolean primaryCountry, String countryCode) {
		this.candidateId = candidateId;
		this.primaryCountry = primaryCountry;
		this.countryCode = countryCode;
	}

	public long getCandidateId() {
		return candidateId;
	}

	public boolean isPrimaryCountry() {
		return primaryCountry;
	}

	public String getCountryCode() {
		return countryCode;
	}
}
