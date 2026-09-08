package net.lacnic.elections.domain.services.publicelection;

import java.io.Serializable;

public class PublicElectionCandidateWorkOrganizationRow implements Serializable {

	private static final long serialVersionUID = 4053066500572374484L;

	private final long candidateId;
	private final String organizationName;

	public PublicElectionCandidateWorkOrganizationRow(Long candidateId, String organizationName) {
		this(candidateId != null ? candidateId.longValue() : 0L, organizationName);
	}

	public PublicElectionCandidateWorkOrganizationRow(long candidateId, String organizationName) {
		this.candidateId = candidateId;
		this.organizationName = organizationName;
	}

	public long getCandidateId() {
		return candidateId;
	}

	public String getOrganizationName() {
		return organizationName;
	}
}
