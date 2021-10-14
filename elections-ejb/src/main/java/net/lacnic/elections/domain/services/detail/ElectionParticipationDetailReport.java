package net.lacnic.elections.domain.services.detail;

import java.io.Serializable;

import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.DateTimeUtils;


public class ElectionParticipationDetailReport implements Serializable {

	private static final long serialVersionUID = 6797525504507951519L;

	private Long electionId;
	private String electionTitleEN;
	private String electionTitleES;
	private String electionTitleSP;
	private String category;
	private String descriptionSpanish;
	private String descriptionEnglish;
	private String descriptionPortuguese;
	private String startDate;
	private String endDate;
	private String role;


	public ElectionParticipationDetailReport() { }

	public ElectionParticipationDetailReport(Auditor auditor) {
		this.electionId = auditor.getElection().getElectionId();
		this.electionTitleEN = auditor.getElection().getTitleEnglish();
		this.electionTitleES = auditor.getElection().getTitleSpanish();
		this.electionTitleSP = auditor.getElection().getTitlePortuguese();
		this.category = auditor.getElection().getCategory().toString();
		this.descriptionEnglish =auditor.getElection().getDescriptionEnglish();
		this.descriptionPortuguese =auditor.getElection().getDescriptionPortuguese();
		this.descriptionSpanish = auditor.getElection().getDescriptionSpanish();
		if(auditor.isCommissioner()) {
			this.role = "Commissioner";
		} else {
			this.role = "Auditor";
		}
		this.startDate = DateTimeUtils.getTableServicesDateTimeString(auditor.getElection().getStartDate());
		this.endDate = DateTimeUtils.getTableServicesDateTimeString(auditor.getElection().getEndDate());
	}

	public ElectionParticipationDetailReport(Candidate candidate) {
		this.electionId = candidate.getElection().getElectionId();
		this.electionTitleEN = candidate.getElection().getTitleEnglish();
		this.electionTitleES = candidate.getElection().getTitleSpanish();
		this.electionTitleSP = candidate.getElection().getTitlePortuguese();
		this.category = candidate.getElection().getCategory().toString();
		this.descriptionEnglish = candidate.getElection().getDescriptionEnglish();
		this.descriptionPortuguese = candidate.getElection().getDescriptionPortuguese();
		this.descriptionSpanish = candidate.getElection().getDescriptionSpanish();
		this.role = "Candidate";
		this.startDate = DateTimeUtils.getTableServicesDateTimeString(candidate.getElection().getStartDate());
		this.endDate = DateTimeUtils.getTableServicesDateTimeString(candidate.getElection().getEndDate());
	}

	public ElectionParticipationDetailReport(UserVoter userVoter) {
		this.electionId = userVoter.getElection().getElectionId();
		this.electionTitleEN = userVoter.getElection().getTitleEnglish();
		this.electionTitleES = userVoter.getElection().getTitleSpanish();
		this.electionTitleSP = userVoter.getElection().getTitlePortuguese();
		this.role = "Voter";
		this.category = userVoter.getElection().getCategory().toString();
		this.descriptionEnglish = userVoter.getElection().getDescriptionEnglish();
		this.descriptionPortuguese = userVoter.getElection().getDescriptionPortuguese();
		this.descriptionSpanish = userVoter.getElection().getDescriptionSpanish();
		this.startDate = DateTimeUtils.getTableServicesDateTimeString(userVoter.getElection().getStartDate());
		this.endDate = DateTimeUtils.getTableServicesDateTimeString(userVoter.getElection().getEndDate());
	}


	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getElectionTitleEN() {
		return electionTitleEN;
	}

	public void setElectionTitleEN(String electionTitleEN) {
		this.electionTitleEN = electionTitleEN;
	}

	public String getElectionTitleES() {
		return electionTitleES;
	}

	public void setElectionTitleES(String electionTitleES) {
		this.electionTitleES = electionTitleES;
	}

	public String getElectionTitleSP() {
		return electionTitleSP;
	}

	public void setElectionTitleSP(String electionTitleSP) {
		this.electionTitleSP = electionTitleSP;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescriptionSpanish() {
		return descriptionSpanish;
	}

	public void setDescriptionSpanish(String descriptionSpanish) {
		this.descriptionSpanish=descriptionSpanish;
	}

	public String getDescriptionEnglish() {
		return descriptionEnglish;
	}

	public void setDescriptionEnglish(String descriptionEnglish) {
		this.descriptionEnglish=descriptionEnglish;
	}

	public String getDescriptionPortuguese() {
		return descriptionPortuguese;
	}

	public void setDescriptionPortuguese(String descriptionPortuguese) {
		this.descriptionPortuguese=descriptionPortuguese;
	}

}
