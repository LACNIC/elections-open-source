package net.lacnic.elections.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;

import net.lacnic.elections.utils.LinksUtils;

@Entity
@Table(name = "election")
public class ElectionLight implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;
	private static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy HH:mm";

	@Id
	@Column(name = "election_id")
	private long electionId;

	@Column
	@Enumerated(EnumType.STRING)
	ElectionCategory category;

	@Column
	private boolean migrated = false;

	@Column(nullable = false)
	private Date startDate;

	@Column(nullable = false)
	private Date endDate;

	@Column(nullable = false)
	private Date creationDate;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String titleSpanish;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String titleEnglish;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String titlePortuguese;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String linkSpanish;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String linkEnglish;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String linkPortuguese;

	@Column(columnDefinition = "TEXT")
	private String descriptionSpanish;

	@Column(columnDefinition = "TEXT")
	private String descriptionEnglish;

	@Column(columnDefinition = "TEXT")
	private String descriptionPortuguese;

	@Column(nullable = false)
	private int maxCandidates;

	@Column(nullable = false)
	private boolean votingLinkAvailable;

	@Column(nullable = false)
	private boolean resultLinkAvailable;

	@Column(nullable = false)
	private boolean auditorLinkAvailable;

	@Column(nullable = false)
	private boolean revisionRequest;

	@Column(nullable = true)
	private boolean onlySp;

	@Column(nullable = true, length = 1000)
	private String resultToken;

	@Column(nullable = true, length = 2000)
	private String defaultSender;

	@Column(nullable = true)
	private boolean electorsSet;

	@Column(nullable = true)
	private boolean candidatesSet;

	@Column(nullable = true)
	private boolean auditorsSet;

	@Column(nullable = true)
	private boolean randomOrderCandidates;

	@Column(nullable = true)
	private int diffUTC;

	@Column(nullable = true)
	private boolean closed;

	@Column(nullable = true)
	private Date closedDate;

	public ElectionLight() {
		// Default initialization
	}

	public boolean isFinished() {
		return new Date().after(getEndDate());
	}

	public boolean isStarted() {
		return new Date().after(getStartDate());
	}

	public boolean isEnabledToVote() {
		return (isStarted() && !isFinished() && isVotingLinkAvailable());
	}

	public String getDescription(String displayName) {
		if (displayName.contains("sp"))
			return getDescriptionSpanish();
		else if (displayName.contains("en"))
			return getDescriptionEnglish();
		else if (displayName.contains("pt"))
			return getDescriptionPortuguese();
		return getDescriptionSpanish();
	}

	public String getTitle(String displayName) {
		if (displayName.contains("sp"))
			return getTitleSpanish();
		else if (displayName.contains("en"))
			return getTitleEnglish();
		else if (displayName.contains("pt"))
			return getTitlePortuguese();
		return getTitleSpanish();

	}

	public void copyLanguageDescriptions(String language) {
		if (language.equalsIgnoreCase("EN")) {
			setDescriptionSpanish(getDescriptionEnglish());
			setDescriptionPortuguese(getDescriptionEnglish());
		} else if (language.equalsIgnoreCase("PT")) {
			setDescriptionEnglish(getDescriptionPortuguese());
			setDescriptionSpanish(getDescriptionPortuguese());
		} else {
			setDescriptionEnglish(getDescriptionSpanish());
			setDescriptionPortuguese(getDescriptionSpanish());
		}
	}

	public void copyLanguageTitles(String language) {
		if (language.equalsIgnoreCase("EN")) {
			setTitleSpanish(getTitleEnglish());
			setTitlePortuguese(getTitleEnglish());
		} else if (language.equalsIgnoreCase("PT")) {
			setTitleEnglish(getTitlePortuguese());
			setTitleSpanish(getTitlePortuguese());
		} else {
			setTitleEnglish(getTitleSpanish());
			setTitlePortuguese(getTitleSpanish());
		}
	}

	public void copyLanguageURLs(String language) {
		if (language.equalsIgnoreCase("EN")) {
			setLinkSpanish(getLinkEnglish());
			setLinkPortuguese(getLinkEnglish());
		} else if (language.equalsIgnoreCase("PT")) {
			setLinkEnglish(getLinkPortuguese());
			setLinkSpanish(getLinkPortuguese());
		} else {
			setLinkEnglish(getLinkSpanish());
			setLinkPortuguese(getLinkSpanish());
		}
	}

	public String getStartDateString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		return simpleDateFormat.format(new DateTime(getStartDate()).plusHours(getDiffUTC()).toDate()) + " (UTC)";
	}

	public String getEndDateString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		return simpleDateFormat.format(new DateTime(getEndDate()).plusHours(getDiffUTC()).toDate()) + " (UTC)";
	}

	public String getClosedDateString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		if (this.getClosedDate() == null) {
			return "";
		} else {
			return simpleDateFormat.format(new DateTime(getClosedDate()).plusHours(getDiffUTC()).toDate()) + " (UTC)";
		}
	}

	public String getResultLink() {
		return LinksUtils.buildResultsLink(resultToken);
	}

	public long getElectionId() {
		return electionId;
	}

	public void setElectionId(long electionId) {
		this.electionId = electionId;
	}

	public ElectionCategory getCategory() {
		return category;
	}

	public void setCategory(ElectionCategory category) {
		this.category = category;
	}

	public boolean isMigrated() {
		return migrated;
	}

	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
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

	public int getMaxCandidates() {
		return maxCandidates;
	}

	public void setMaxCandidates(int maxCandidates) {
		this.maxCandidates = maxCandidates;
	}

	public boolean isVotingLinkAvailable() {
		return votingLinkAvailable;
	}

	public void setVotingLinkAvailable(boolean votingLinkAvailable) {
		this.votingLinkAvailable = votingLinkAvailable;
	}

	public boolean isResultLinkAvailable() {
		return resultLinkAvailable;
	}

	public void setResultLinkAvailable(boolean resultLinkAvailable) {
		this.resultLinkAvailable = resultLinkAvailable;
	}

	public boolean isAuditorLinkAvailable() {
		return auditorLinkAvailable;
	}

	public void setAuditorLinkAvailable(boolean auditorLinkAvailable) {
		this.auditorLinkAvailable = auditorLinkAvailable;
	}

	public boolean isRevisionRequest() {
		return revisionRequest;
	}

	public void setRevisionRequest(boolean revisionRequest) {
		this.revisionRequest = revisionRequest;
	}

	public boolean isOnlySp() {
		return onlySp;
	}

	public void setOnlySp(boolean onlySp) {
		this.onlySp = onlySp;
	}

	public String getResultToken() {
		return resultToken;
	}

	public void setResultToken(String resultToken) {
		this.resultToken = resultToken;
	}

	public String getDefaultSender() {
		return defaultSender;
	}

	public void setDefaultSender(String defaultSender) {
		this.defaultSender = defaultSender;
	}

	public boolean isElectorsSet() {
		return electorsSet;
	}

	public void setElectorsSet(boolean electorsSet) {
		this.electorsSet = electorsSet;
	}

	public boolean isCandidatesSet() {
		return candidatesSet;
	}

	public void setCandidatesSet(boolean candidatesSet) {
		this.candidatesSet = candidatesSet;
	}

	public boolean isAuditorsSet() {
		return auditorsSet;
	}

	public void setAuditorsSet(boolean auditorsSet) {
		this.auditorsSet = auditorsSet;
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

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public Date getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(Date closedDate) {
		this.closedDate = closedDate;
	}

}
