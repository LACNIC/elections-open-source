package net.lacnic.elections.domain.services;

import java.io.Serializable;

import net.lacnic.elections.domain.Election;


public class ElectionReportTable implements Serializable {

	private static final long serialVersionUID = -6249197041343974691L;

	private long electionId;
	private String descriptionSpanish;
	private String descriptionEnglish;
	private String descriptionPortuguese;
	private String creationDate;
	private String endDate;
	private String startDate;
	private boolean resultLinkAvailable;
	private boolean votingLinkAvailable;
	private String linkSpanish;
	private String linkEnglish;
	private String linkPortuguese;
	private int maxCandidates;
	private String titleSpanish;
	private String titleEnglish;
	private String titlePortuguese;
	private boolean auditorsSet;
	private boolean candidatesSet;
	private boolean electorsSet;
	private boolean auditorLinkAvailable;
	private boolean onlySp;
	private String defaultSender;
	private boolean randomOrderCandidates;
	private int diffUTC;
	private boolean revisionRequest;
	private Long migrationId;
	private boolean migrated;
	private String category;


	public ElectionReportTable() {	}

	public ElectionReportTable(Election election) {
		this.electionId = election.getElectionId();
		this.descriptionSpanish = election.getDescriptionSpanish();
		this.descriptionEnglish = election.getDescriptionEnglish();
		this.descriptionPortuguese = election.getDescriptionPortuguese();
		this.creationDate = election.getCreationDateString();
		this.endDate = election.getEndDateString();
		this.startDate = election.getStartDateString();
		this.resultLinkAvailable = election.isResultLinkAvailable();
		this.votingLinkAvailable = election.isVotingLinkAvailable();
		this.linkSpanish = election.getLinkSpanish();
		this.linkEnglish = election.getLinkEnglish();
		this.linkPortuguese = election.getLinkPortuguese();
		this.maxCandidates = election.getMaxCandidates();
		this.titleSpanish = election.getTitleSpanish();
		this.titleEnglish = election.getTitleEnglish();
		this.titlePortuguese = election.getTitlePortuguese();
		this.auditorsSet = election.isAuditorsSet();
		this.candidatesSet = election.isCandidatesSet();
		this.electorsSet = election.isElectorsSet();
		this.auditorLinkAvailable = election.isAuditorLinkAvailable();
		this.onlySp = election.isOnlySp();
		this.defaultSender = election.getDefaultSender();
		this.randomOrderCandidates = election.isRandomOrderCandidates();
		this.diffUTC = election.getDiffUTC();
		this.revisionRequest = election.isRevisionRequest();
		this.migrationId = election.getMigrationId();
		this.migrated = election.isMigrated();
		this.category = election.getCategory().toString();
	}


	public long getElectionId() {
		return electionId;
	}

	public void setElectionId(long electionId) {
		this.electionId = electionId;
	}

	public String getDescriptionSpanish() {
		return descriptionSpanish;
	}

	public void setDescriptionSpanish(String descriptionSpanish) {
		this.descriptionSpanish = descriptionSpanish;
	}

	public String getDescriptionEnglish() {
		return descriptionEnglish;
	}

	public void setDescriptionEnglish(String descriptionEnglish) {
		this.descriptionEnglish = descriptionEnglish;
	}

	public String getDescriptionPortuguese() {
		return descriptionPortuguese;
	}

	public void setDescriptionPortuguese(String descriptionPortuguese) {
		this.descriptionPortuguese = descriptionPortuguese;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public boolean isResultLinkAvailable() {
		return resultLinkAvailable;
	}

	public void setResultLinkAvailable(boolean resultLinkAvailable) {
		this.resultLinkAvailable = resultLinkAvailable;
	}

	public boolean isVotingLinkAvailable() {
		return votingLinkAvailable;
	}

	public void setVotingLinkAvailable(boolean votingLinkAvailable) {
		this.votingLinkAvailable = votingLinkAvailable;
	}

	public String getLinkSpanish() {
		return linkSpanish;
	}

	public void setLinkSpanish(String linkSpanish) {
		this.linkSpanish = linkSpanish;
	}

	public String getLinkEnglish() {
		return linkEnglish;
	}

	public void setLinkEnglish(String linkEnglish) {
		this.linkEnglish = linkEnglish;
	}

	public String getLinkPortuguese() {
		return linkPortuguese;
	}

	public void setLinkPortuguese(String linkPortuguese) {
		this.linkPortuguese = linkPortuguese;
	}

	public int getMaxCandidates() {
		return maxCandidates;
	}

	public void setMaxCandidates(int maxCandidates) {
		this.maxCandidates = maxCandidates;
	}

	public String getTitleSpanish() {
		return titleSpanish;
	}

	public void setTitleSpanish(String titleSpanish) {
		this.titleSpanish = titleSpanish;
	}

	public String getTitleEnglish() {
		return titleEnglish;
	}

	public void setTitleEnglish(String titleEnglish) {
		this.titleEnglish = titleEnglish;
	}

	public String getTitlePortuguese() {
		return titlePortuguese;
	}

	public void setTitlePortuguese(String titlePortuguese) {
		this.titlePortuguese = titlePortuguese;
	}

	public boolean isAuditorsSet() {
		return auditorsSet;
	}

	public void setAuditorsSet(boolean auditorsSet) {
		this.auditorsSet = auditorsSet;
	}

	public boolean isCandidatesSet() {
		return candidatesSet;
	}

	public void setCandidatesSet(boolean candidatesSet) {
		this.candidatesSet = candidatesSet;
	}

	public boolean isElectorsSet() {
		return electorsSet;
	}

	public void setElectorsSet(boolean electorsSet) {
		this.electorsSet = electorsSet;
	}

	public boolean isAuditorLinkAvailable() {
		return auditorLinkAvailable;
	}

	public void setAuditorLinkAvailable(boolean auditorLinkAvailable) {
		this.auditorLinkAvailable = auditorLinkAvailable;
	}

	public boolean isOnlySp() {
		return onlySp;
	}

	public void setOnlySp(boolean onlySp) {
		this.onlySp = onlySp;
	}

	public String getDefaultSender() {
		return defaultSender;
	}

	public void setDefaultSender(String defaultSender) {
		this.defaultSender = defaultSender;
	}

	public boolean isRandomOrderCandidates() {
		return randomOrderCandidates;
	}

	public void setRandomOrderCandidates(boolean randomOrderCandidates) {
		this.randomOrderCandidates = randomOrderCandidates;
	}

	public int getDiffUTC() {
		return diffUTC;
	}

	public void setDiffUTC(int diffUTC) {
		this.diffUTC = diffUTC;
	}

	public boolean isRevisionRequest() {
		return revisionRequest;
	}

	public void setRevisionRequest(boolean revisionRequest) {
		this.revisionRequest = revisionRequest;
	}

	public Long getMigrationId() {
		return migrationId;
	}

	public void setMigrationId(Long migrationId) {
		this.migrationId = migrationId;
	}

	public boolean isMigrated() {
		return migrated;
	}

	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
