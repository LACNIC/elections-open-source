package net.lacnic.elections.domain.services.dbtables;

import java.io.Serializable;

import net.lacnic.elections.domain.Election;


public class ElectionTableReport implements Serializable {

	private static final long serialVersionUID = -6249197041343974691L;

	private Long electionId;
	private String descriptionSpanish;
	private String descriptionEnglish;
	private String descriptionPortuguese;
	private String creationDate;
	private String endDate;
	private String startDate;
	private Boolean resultLinkAvailable;
	private Boolean votingLinkAvailable;
	private String linkSpanish;
	private String linkEnglish;
	private String linkPortuguese;
	private Integer maxCandidates;
	private String titleSpanish;
	private String titleEnglish;
	private String titlePortuguese;
	private Boolean auditorsSet;
	private Boolean candidatesSet;
	private Boolean electorsSet;
	private Boolean auditorLinkAvailable;
	private Boolean onlySp;
	private String defaultSender;
	private Boolean randomOrderCandidates;
	private Integer diffUTC;
	private Boolean revisionRequest;
	private Long migrationId;
	private Boolean migrated;
	private String category;
	private Boolean closed;
	private String closedDate;


	public ElectionTableReport() {	}

	public ElectionTableReport(Election election) {
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
		this.closed = election.isClosed();
		this.closedDate = election.getClosedDateString();
	}


	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
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

	public Boolean getResultLinkAvailable() {
		return resultLinkAvailable;
	}

	public void setResultLinkAvailable(Boolean resultLinkAvailable) {
		this.resultLinkAvailable = resultLinkAvailable;
	}

	public Boolean getVotingLinkAvailable() {
		return votingLinkAvailable;
	}

	public void setVotingLinkAvailable(Boolean votingLinkAvailable) {
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

	public Integer getMaxCandidates() {
		return maxCandidates;
	}

	public void setMaxCandidates(Integer maxCandidates) {
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

	public Boolean getAuditorsSet() {
		return auditorsSet;
	}

	public void setAuditorsSet(Boolean auditorsSet) {
		this.auditorsSet = auditorsSet;
	}

	public Boolean getCandidatesSet() {
		return candidatesSet;
	}

	public void setCandidatesSet(Boolean candidatesSet) {
		this.candidatesSet = candidatesSet;
	}

	public Boolean getElectorsSet() {
		return electorsSet;
	}

	public void setElectorsSet(Boolean electorsSet) {
		this.electorsSet = electorsSet;
	}

	public Boolean getAuditorLinkAvailable() {
		return auditorLinkAvailable;
	}

	public void setAuditorLinkAvailable(Boolean auditorLinkAvailable) {
		this.auditorLinkAvailable = auditorLinkAvailable;
	}

	public Boolean getOnlySp() {
		return onlySp;
	}

	public void setOnlySp(Boolean onlySp) {
		this.onlySp = onlySp;
	}

	public String getDefaultSender() {
		return defaultSender;
	}

	public void setDefaultSender(String defaultSender) {
		this.defaultSender = defaultSender;
	}

	public Boolean getRandomOrderCandidates() {
		return randomOrderCandidates;
	}

	public void setRandomOrderCandidates(Boolean randomOrderCandidates) {
		this.randomOrderCandidates = randomOrderCandidates;
	}

	public Integer getDiffUTC() {
		return diffUTC;
	}

	public void setDiffUTC(Integer diffUTC) {
		this.diffUTC = diffUTC;
	}

	public Boolean getRevisionRequest() {
		return revisionRequest;
	}

	public void setRevisionRequest(Boolean revisionRequest) {
		this.revisionRequest = revisionRequest;
	}

	public Long getMigrationId() {
		return migrationId;
	}

	public void setMigrationId(Long migrationId) {
		this.migrationId = migrationId;
	}

	public Boolean getMigrated() {
		return migrated;
	}

	public void setMigrated(Boolean migrated) {
		this.migrated = migrated;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Boolean getClosed() {
		return closed;
	}

	public void setClosed(Boolean closed) {
		this.closed = closed;
	}

	public String getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(String closedDate) {
		this.closedDate = closedDate;
	}

	@Override
	public boolean equals(Object election) {
		return this.electionId.equals(((ElectionTableReport)election).electionId);
	}

}
