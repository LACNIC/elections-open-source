package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import net.lacnic.elections.utils.LinksUtils;


@Entity
public class UserVoter implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uservoter_seq")
	@SequenceGenerator(name = "uservoter_seq", sequenceName = "uservoter_seq", allocationSize = 1)
	@Column(name = "uservoter_id")
	private long userVoterId;

	@Column(nullable = true, name = "migration_id")
	private Long  migrationId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id")
	private Election election;

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

	@Column(nullable = true)
	private Date voteDate;

	@OneToMany(mappedBy = "userVoter", cascade = CascadeType.REMOVE)
	private List<Vote> votes;

	@Transient
	private String codesSummary;


	public UserVoter() { }


	public String getVoterInformation() {
		return getName().concat((getOrgID() != null && !getOrgID().isEmpty()) ? " - " + getOrgID() : "");
	}

	public String getCompleteVoterInformation() {
		String email= (getMail() != null && !getMail().isEmpty()) ? " (" + getMail() + ") " : "";
		String orgid= (getOrgID() != null && !getOrgID().isEmpty()) ? " - " + getOrgID() : "";
		String country= (getCountry() != null && !getCountry().isEmpty()) ? " - " + getCountry() : "";
		return getName().concat(email + orgid + country);
	}

	public String getVoteLink() {
		return LinksUtils.buildVoteLink(voteToken);
	}

	public void setCodesSummary(List<Vote> votes) {
		String aux = "";
		for (Vote vote : votes) {
			aux = aux.concat(vote.getCode() + " / " + vote.getCandidate().getName() + "\n");
		}
		this.codesSummary = aux;
	}


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

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
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
		if (orgID != null)
			this.orgID = orgID.toUpperCase();
		return orgID;
	}

	public void setOrgID(String orgID) {
		this.orgID = orgID;
	}

	public Date getVoteDate() {
		return voteDate;
	}

	public void setVoteDate(Date voteDate) {
		this.voteDate = voteDate;
	}

	public List<Vote> getVotes() {
		return votes;
	}

	public void setVotes(List<Vote> votes) {
		this.votes = votes;
	}

	public String getCodesSummary() {
		return codesSummary;
	}

	public void setCodesSummary(String codesSummary) {
		this.codesSummary = codesSummary;
	}

}
