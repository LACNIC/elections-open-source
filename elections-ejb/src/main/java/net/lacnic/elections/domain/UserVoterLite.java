package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "uservoter")
public class UserVoterLite implements Serializable {

	private static final long serialVersionUID = 577711237621594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uservoter_lite_seq")
	@SequenceGenerator(name = "uservoter_lite_seq", sequenceName = "uservoter_seq", allocationSize = 1)
	@Column(name = "uservoter_id")
	private long userVoterId;

	@Column(nullable = true, name = "migration_id")
	private Long migrationId;

	@Column(name = "election_id", nullable = false)
	private Long electionId;

	@Column(nullable = false)
	private boolean voted;

	@Column(nullable = true, length = 1000)
	private String voteToken;

	@Column(nullable = false)
	private Integer voteAmount;

	@Column(nullable = false, length = 1000)
	private String name;

	@Column(nullable = false)
	private String mail;

	@Column(nullable = true)
	private String country;

	@Column(nullable = false)
	private String language;

	@Column(nullable = true)
	private String orgID;

	@Column(nullable = true, name = "orgname", length = 1000)
	private String orgName;

	@Column(nullable = true)
	private Date voteDate;

	@Version
	@Column(name = "version")
	private int version;

	public long getUserVoterId() {
		return userVoterId;
	}

	public void setUserVoterId(long userVoterId) {
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

	public boolean isVoted() {
		return voted;
	}

	public void setVoted(boolean voted) {
		this.voted = voted;
	}

	public String getVoteToken() {
		return voteToken;
	}

	public void setVoteToken(String voteToken) {
		this.voteToken = voteToken;
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
		if (orgID != null) {
			return orgID.toUpperCase();
		}
		return null;
	}

	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public Date getVoteDate() {
		return voteDate;
	}

	public void setVoteDate(Date voteDate) {
		this.voteDate = voteDate;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
