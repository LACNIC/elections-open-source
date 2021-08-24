package net.lacnic.elections.domain.services;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import net.lacnic.elections.domain.Vote;

public class VoteReportTable implements Serializable {

	private static final long serialVersionUID = 8648483458946434365L;
	private static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy HH:mm";
	
	private long voteId;
	private String ip;
	private String voteDate;
	private long candidateId;
	private long electionId;
	
	public VoteReportTable() { }
	
	public VoteReportTable(Vote vote) {
		this.voteId = vote.getVoteId();
		this.ip = vote.getIp();
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		if (vote.getVoteDate() == null) {
			this.voteDate = "";
		} else {
			this.voteDate = simpleDateFormat.format(vote.getVoteDate());
		};
		
		this.candidateId = vote.getCandidate().getCandidateId();
		this.electionId = vote.getElection().getElectionId();
		
	}
	
	public long getVoteId() {
		return voteId;
	}
	
	public void setVoteId(long voteId) {
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
	
	public long getCandidateId() {
		return candidateId;
	}
	
	public void setCandidateId(long candidateId) {
		this.candidateId = candidateId;
	}
	
	public long getElectionId() {
		return electionId;
	}
	
	public void setElectionId(long electionId) {
		this.electionId = electionId;
	}
	
	

}
