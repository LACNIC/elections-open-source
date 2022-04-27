package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;


@Entity
public class Vote implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vote_seq")
	@SequenceGenerator(name = "vote_seq", sequenceName = "vote_seq", allocationSize = 1)
	@Column(name = "vote_id")
	private long voteId;

	@Column(nullable = false)
	private String code;

	@Column(nullable = true)
	private String ip;

	@Column(nullable = true)
	private Date voteDate;

	@ManyToOne(optional = false)
	@JoinColumn(name = "candidate_id")
	private Candidate candidate;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id")
	private Election election;

	@ManyToOne(optional = true)
	@JoinColumn(name = "uservoter_id")
	private UserVoter userVoter;


	public Vote() { }


	public long getVoteId() {
		return voteId;
	}

	public void setVoteId(long voteId) {
		this.voteId = voteId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getVoteDate() {
		return voteDate;
	}

	public void setVoteDate(Date voteDate) {
		this.voteDate = voteDate;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public UserVoter getUserVoter() {
		return userVoter;
	}

	public void setUserVoter(UserVoter userVote) {
		this.userVoter = userVote;
	}

}
