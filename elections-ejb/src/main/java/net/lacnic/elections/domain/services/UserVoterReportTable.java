package net.lacnic.elections.domain.services;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import net.lacnic.elections.domain.UserVoter;

public class UserVoterReportTable implements Serializable{

	private static final long serialVersionUID = 4933453990097788863L;
	private static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy HH:mm";
	
	private long userVoterId;
	private long migrationId;
	private long electionId;
	private boolean voted;
	private int voteAmount;
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
		this.country = userVoter.getCountry();
		this.language = userVoter.getLanguage();
		this.mail = userVoter.getMail();
		this.name = userVoter.getName();
		this.orgID = userVoter.getOrgID();
		this.voteAmount = userVoter.getVoteAmount().intValue();
		this.voted = userVoter.isVoted();
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		if (userVoter.getVoteDate() == null) {
			this.voteDate = "";
		} else {
			this.voteDate = simpleDateFormat.format(userVoter.getVoteDate());
		};
		
	}
	
	public long getUserVoterId() {
		return userVoterId;
	}
	public void setUserVoterId(long userVoterId) {
		this.userVoterId = userVoterId;
	}
	public long getMigrationId() {
		return migrationId;
	}
	public void setMigrationId(long migrationId) {
		this.migrationId = migrationId;
	}
	public long getElectionId() {
		return electionId;
	}
	public void setElectionId(long electionId) {
		this.electionId = electionId;
	}
	public boolean isVoted() {
		return voted;
	}
	public void setVoted(boolean voted) {
		this.voted = voted;
	}
	public int getVoteAmount() {
		return voteAmount;
	}
	public void setVoteAmount(int voteAmount) {
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
