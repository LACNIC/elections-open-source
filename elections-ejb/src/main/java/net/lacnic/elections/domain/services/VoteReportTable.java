package net.lacnic.elections.domain.services;

import java.io.Serializable;

import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.utils.DateTimeUtils;


public class VoteReportTable implements Serializable {

	private static final long serialVersionUID = 8648483458946434365L;

	private Long voteId;
	private String ip;
	private String voteDate;
	private Long candidateId;
	private Long electionId;


	public VoteReportTable() { }

	public VoteReportTable(Vote vote) {
		this.voteId = vote.getVoteId();
		this.ip = vote.getIp();
		this.voteDate = DateTimeUtils.getTableServicesDateTimeString(vote.getVoteDate());
		this.candidateId = vote.getCandidate().getCandidateId();
		this.electionId = vote.getElection().getElectionId();
	}


	public Long getVoteId() {
		return voteId;
	}

	public void setVoteId(Long voteId) {
		this.voteId = voteId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getVoteDate() {
		return voteDate;
	}

	public void setVoteDate(String voteDate) {
		this.voteDate = voteDate;
	}

	public Long getCandidateId() {
		return candidateId;
	}

	public void setCandidateId(Long candidateId) {
		this.candidateId = candidateId;
	}

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

}
