package net.lacnic.elections.domain.services;

import java.io.Serializable;

import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.DateTimeUtils;


public class UserVoterReportTable implements Serializable {

	private static final long serialVersionUID = 4933453990097788863L;

	private Long userVoterId;
	private Long migrationId;
	private Long electionId;
	private Boolean voted;
	private Integer voteAmount;
	private String name;
	private String mail;
	private String country;
	private String language;
	private String orgID;
	private String voteDate;


	public UserVoterReportTable() { }

	public UserVoterReportTable(UserVoter userVoter) {
		this.userVoterId = userVoter.getUserVoterId();
		this.migrationId = userVoter.getMigrationId();
		this.electionId = userVoter.getElection().getElectionId();
		this.country = userVoter.getCountry();
		this.language = userVoter.getLanguage();
		this.mail = userVoter.getMail();
		this.name = userVoter.getName();
		this.orgID = userVoter.getOrgID();
		this.voteAmount = userVoter.getVoteAmount();
		this.voted = userVoter.isVoted();
		this.voteDate = DateTimeUtils.getTableServicesDateTimeString(userVoter.getVoteDate());
	}


	public Long getUserVoterId() {
		return userVoterId;
	}

	public void setUserVoterId(Long userVoterId) {
		this.userVoterId = userVoterId;
	}

	public Long getMigrationId() {
		return migrationId;
	}

	public void setMigrationId(Long migrationId) {
		this.migrationId = migrationId;
	}

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

	public Boolean getVoted() {
		return voted;
	}

	public void setVoted(Boolean voted) {
		this.voted = voted;
	}

	public Integer getVoteAmount() {
		return voteAmount;
	}

	public void setVoteAmount(Integer voteAmount) {
		this.voteAmount = voteAmount;
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

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
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

}
