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
	private String startDate;
	private String endDate;
	private String role;


	public ElectionParticipationDetailReport() { }

	public ElectionParticipationDetailReport(Auditor auditor) {
		this.electionId = auditor.getElection().getElectionId();
		this.electionTitleEN = auditor.getElection().getTitleEnglish();
		this.electionTitleES = auditor.getElection().getTitleSpanish();
		this.electionTitleSP = auditor.getElection().getTitlePortuguese();
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
		this.role = "Candidate";
	}

	public ElectionParticipationDetailReport(UserVoter userVoter) {
		this.electionId = userVoter.getElection().getElectionId();
		this.electionTitleEN = userVoter.getElection().getTitleEnglish();
		this.electionTitleES = userVoter.getElection().getTitleSpanish();
		this.electionTitleSP = userVoter.getElection().getTitlePortuguese();
		this.role = "Voter";
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

}
