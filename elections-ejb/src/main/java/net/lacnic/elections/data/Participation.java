package net.lacnic.elections.data;

import java.io.Serializable;
import java.util.Date;

public class Participation implements Serializable {

	private static final long serialVersionUID = -273769493770752777L;

	private String orgId;
	private String email;
	private String electionTitleSP;
	private String electionTitleEN;
	private String electionTitlePT;
	private String name;
	private String country;
	private Date electionStartDate;
	private Date electionEndDate;
	private String category;
	private String electionLinkSP;
	private String electionLinkEN;
	private String electionLinkPT;
	private String voteLink;
	boolean voted;

	public Participation() {
		// Default initialization
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getElectionTitleSP() {
		return electionTitleSP;
	}

	public void setElectionTitleSP(String electionTitleSP) {
		this.electionTitleSP = electionTitleSP;
	}

	public String getElectionTitleEN() {
		return electionTitleEN;
	}

	public void setElectionTitleEN(String electionTitleEN) {
		this.electionTitleEN = electionTitleEN;
	}

	public String getElectionTitlePT() {
		return electionTitlePT;
	}

	public void setElectionTitlePT(String electionTitlePT) {
		this.electionTitlePT = electionTitlePT;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Date getElectionStartDate() {
		return electionStartDate;
	}

	public void setElectionStartDate(Date electionStartDate) {
		this.electionStartDate = electionStartDate;
	}

	public Date getElectionEndDate() {
		return electionEndDate;
	}

	public void setElectionEndDate(Date electionEndDate) {
		this.electionEndDate = electionEndDate;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getElectionLinkSP() {
		return electionLinkSP;
	}

	public void setElectionLinkSP(String electionLinkSP) {
		this.electionLinkSP = electionLinkSP;
	}

	public String getElectionLinkEN() {
		return electionLinkEN;
	}

	public void setElectionLinkEN(String electionLinkEN) {
		this.electionLinkEN = electionLinkEN;
	}

	public String getElectionLinkPT() {
		return electionLinkPT;
	}

	public void setElectionLinkPT(String electionLinkPT) {
		this.electionLinkPT = electionLinkPT;
	}

	public String getVoteLink() {
		return voteLink;
	}

	public void setVoteLink(String voteLink) {
		this.voteLink = voteLink;
	}

	public boolean isVoted() {
		return voted;
	}

	public void setVoted(boolean voted) {
		this.voted = voted;
	}

}
