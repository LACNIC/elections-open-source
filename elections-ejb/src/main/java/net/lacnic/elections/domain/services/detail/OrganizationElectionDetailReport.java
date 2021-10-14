package net.lacnic.elections.domain.services.detail;

import java.io.Serializable;

import net.lacnic.elections.domain.Election;
import net.lacnic.elections.utils.DateTimeUtils;


public class OrganizationElectionDetailReport implements Serializable {

	private static final long serialVersionUID = 3823323124312371078L;

	private Long electionId;
	private String electionTitleEN;
	private String electionTitleES;
	private String electionTitleSP;
	private String startDate;
	private String endDate;


	public OrganizationElectionDetailReport() { }

	public OrganizationElectionDetailReport(Election election) {
		this.electionId = election.getElectionId();
		this.electionTitleEN = election.getTitleEnglish();
		this.electionTitleES = election.getTitleSpanish();
		this.electionTitleSP = election.getTitlePortuguese();
		this.startDate = DateTimeUtils.getTableServicesDateTimeString(election.getStartDate());
		this.endDate = DateTimeUtils.getTableServicesDateTimeString(election.getEndDate());
	}


	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
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
