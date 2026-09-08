package net.lacnic.elections.domain.services.publicelection;

import java.io.Serializable;

public class PublicElectionVoteCountRow implements Serializable {

	private static final long serialVersionUID = -205529605116603457L;

	private final long candidateId;
	private final long voteCount;

	public PublicElectionVoteCountRow(Long candidateId, Long voteCount) {
		this(candidateId != null ? candidateId.longValue() : 0L,
				voteCount != null ? voteCount.longValue() : 0L);
	}

	public PublicElectionVoteCountRow(long candidateId, long voteCount) {
		this.candidateId = candidateId;
		this.voteCount = voteCount;
	}

	public long getCandidateId() {
		return candidateId;
	}

	public long getVoteCount() {
		return voteCount;
	}
}
