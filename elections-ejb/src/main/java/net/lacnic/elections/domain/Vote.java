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
	private long idVote;

	@Column(nullable = false)
	private String code;

	@Column(nullable = true)
	private String ip;

	@Column(nullable = true)
	private Date voteDate;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_candidate")
	private Candidate candidate;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_election")
	private Election election;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_user_vote")
	private UserVoter userVote;


	public Vote() { }


	public UserVoter getUserVote() {
		return userVote;
	}

	public void setUserVote(UserVoter userVote) {
		this.userVote = userVote;
	}

	public long getIdVote() {
		return idVote;
	}

	public void setIdVote(long idVote) {
		this.idVote = idVote;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
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

}
