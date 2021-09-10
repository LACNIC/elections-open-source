package net.lacnic.elections.domain.services.detail;

import java.io.Serializable;

import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.DateTimeUtils;


public class OrganizationVoterDetailReport implements Serializable {

	private static final long serialVersionUID = 4933453990097788863L;

	private Long userVoterId;
	private Boolean voted;
	private String name;
	private String mail;
	private String orgID;
	private String voteDate;
	private OrganizationElectionDetailReport election;


	public OrganizationVoterDetailReport() { }

	public OrganizationVoterDetailReport(UserVoter userVoter) {
		this.userVoterId = userVoter.getUserVoterId();
		this.voted = userVoter.isVoted();
		this.name = userVoter.getName();
		this.mail = userVoter.getMail();
		this.orgID = userVoter.getOrgID();
		this.voteDate = DateTimeUtils.getTableServicesDateTimeString(userVoter.getVoteDate());
		this.election = new OrganizationElectionDetailReport(userVoter.getElection());
	}


	public Long getUserVoterId() {
		return userVoterId;
	}

	public void setUserVoterId(Long userVoterId) {
		this.userVoterId = userVoterId;
	}

	public Boolean getVoted() {
		return voted;
	}

	public void setVoted(Boolean voted) {
		this.voted = voted;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getOrgID() {
		return orgID;
	}

	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}

	public String getVoteDate() {
		return voteDate;
	}

	public void setVoteDate(String voteDate) {
		this.voteDate = voteDate;
	}

	public OrganizationElectionDetailReport getElection() {
		return election;
	}

	public void setElection(OrganizationElectionDetailReport election) {
		this.election = election;
	}

}
