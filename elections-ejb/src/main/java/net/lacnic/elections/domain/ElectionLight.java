package net.lacnic.elections.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import net.lacnic.elections.utils.DateTimeUtils;
import net.lacnic.elections.utils.LinksUtils;

@Entity
@Table(name = "election")
public class ElectionLight implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;
	private static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy HH:mm";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "election_seq")
	@SequenceGenerator(name = "election_seq", sequenceName = "election_seq", allocationSize = 1)
	@Column(name = "election_id")
	private long electionId;

	@Column
	@Enumerated(EnumType.STRING)
	ElectionCategory category;

	@Column(name = "election_type")
	@Enumerated(EnumType.STRING)
	ElectionType electionType;

	@Column(name = "public_link_recovery_mode")
	@Enumerated(EnumType.STRING)
	ElectionLinkRecoveryMode publicLinkRecoveryMode;

	@Column
	private boolean migrated = false;

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

	@Column(columnDefinition = "TEXT")
	private String callSpanish;

	@Column(columnDefinition = "TEXT")
	private String callEnglish;

	@Column(columnDefinition = "TEXT")
	private String callPortuguese;

	@Column(nullable = false)
	private int maxCandidates;

	@Column(nullable = false)
	private boolean votingLinkAvailable;

	@Column(nullable = false)
	private boolean resultLinkAvailable;

	@Column(nullable = false)
	private boolean auditorLinkAvailable;

	@Column(nullable = false)
	private boolean doNominationLinkAvailable;

	@Column(nullable = false)
	private boolean nominationTasksLinkAvailable;

	@Column(nullable = false)
	private boolean nominationSupportLinkAvailable;

	@Column(nullable = false)
	private boolean publicElectionLinkAvailable;

	@Column(nullable = false)
	private boolean revisionRequest;

	@Column(nullable = true)
	private boolean onlySp;

	@Column(nullable = true, length = 1000)
	private String resultToken;

	@Column(nullable = true, length = 1000, name = "public_election_token")
	private String publicElectionToken;

	@Column(nullable = true, length = 2000)
	private String defaultSender;

	@Column(nullable = true, length = 2000)
	private String defaultRecipient;

	@Column(nullable = true)
	private boolean electorsSet;

	@Column(nullable = true)
	private boolean candidatesSet;

	@Column(nullable = true)
	private boolean auditorsSet;

	@Column(nullable = true)
	private boolean callSet;

	@Column(nullable = true)
	private boolean randomOrderCandidates;

	@Column(nullable = true)
	private int diffUTC;

	@Column(nullable = true)
	private boolean closed;

	@Column(nullable = true)
	private Date closedDate;

	@Transient
	private Date votingPeriodStartDate;

	@Transient
	private Date votingPeriodEndDate;

	@Transient
	private Date nominationPeriodStartDate;

	@Transient
	private Date nominationPeriodEndDate;

	public ElectionLight() {
		// Intencionalmente vacio: JPA lo requiere para materializar la entidad.
	}

	public boolean isFinished() {
		Date endDate = getVotingPeriodEndDate();
		return endDate != null && new Date().after(endDate);
	}

	public boolean isStarted() {
		Date startDate = getVotingPeriodStartDate();
		return startDate != null && new Date().after(startDate);
	}

	public boolean isEnabledToVote() {
		return isVotingLinkAvailable() && isVotingWindowOpen();
	}

	public boolean isNominationFinished() {
		Date endDate = getNominationPeriodEndDate();
		return endDate != null && new Date().after(endDate);
	}

	public boolean isNominationStarted() {
		Date startDate = getNominationPeriodStartDate();
		return startDate != null && new Date().after(startDate);
	}

	public boolean isEnabledToNominate() {
		return isDoNominationLinkAvailable() && isNominationWindowOpen();
	}

	public boolean isEnabledToSupportNomination() {
		return isNominationSupportLinkAvailable() && isSupportWindowOpen();
	}

	public boolean isVotingWindowOpen() {
		return isWithinWindow(getVotingPeriodStartDate(), getVotingPeriodEndDate());
	}

	public boolean isNominationWindowOpen() {
		return isWithinWindow(getNominationPeriodStartDate(), getNominationPeriodEndDate());
	}

	public boolean isSupportWindowOpen() {
		return isNominationWindowOpen();
	}

	public boolean isVotingLinkEnabledNow() {
		return isVotingLinkAvailable() && isVotingWindowOpen();
	}

	public boolean isDoNominationLinkEnabledNow() {
		return isDoNominationLinkAvailable() && isNominationWindowOpen();
	}

	public boolean isNominationSupportLinkEnabledNow() {
		return isNominationSupportLinkAvailable() && isSupportWindowOpen();
	}

	private boolean isWithinWindow(Date startDate, Date endDate) {
		if (startDate == null) {
			return false;
		}
		Date now = new Date();
		if (now.before(startDate)) {
			return false;
		}
		return endDate == null || !now.after(endDate);
	}

	public String getDescription(String displayName) {
		LanguageCode languageCode = LanguageCode.fromValueOrDefault(displayName, LanguageCode.SP);
		switch (languageCode) {
		case EN:
			return getDescriptionEnglish();
		case PT:
			return getDescriptionPortuguese();
		case SP:
		default:
			return getDescriptionSpanish();
		}
	}

	public String getTitle(String displayName) {
		LanguageCode languageCode = LanguageCode.fromValueOrDefault(displayName, LanguageCode.SP);
		switch (languageCode) {
		case EN:
			return getTitleEnglish();
		case PT:
			return getTitlePortuguese();
		case SP:
		default:
			return getTitleSpanish();
		}

	}

	public void copyLanguageDescriptions(String language) {
		LanguageCode languageCode = LanguageCode.fromValueOrDefault(language, LanguageCode.SP);
		if (LanguageCode.EN == languageCode) {
			setDescriptionSpanish(getDescriptionEnglish());
			setDescriptionPortuguese(getDescriptionEnglish());
		} else if (LanguageCode.PT == languageCode) {
			setDescriptionEnglish(getDescriptionPortuguese());
			setDescriptionSpanish(getDescriptionPortuguese());
		} else {
			setDescriptionEnglish(getDescriptionSpanish());
			setDescriptionPortuguese(getDescriptionSpanish());
		}
	}

	public void copyLanguageTitles(String language) {
		LanguageCode languageCode = LanguageCode.fromValueOrDefault(language, LanguageCode.SP);
		if (LanguageCode.EN == languageCode) {
			setTitleSpanish(getTitleEnglish());
			setTitlePortuguese(getTitleEnglish());
		} else if (LanguageCode.PT == languageCode) {
			setTitleEnglish(getTitlePortuguese());
			setTitleSpanish(getTitlePortuguese());
		} else {
			setTitleEnglish(getTitleSpanish());
			setTitlePortuguese(getTitleSpanish());
		}
	}

	public void copyLanguageURLs(String language) {
		LanguageCode languageCode = LanguageCode.fromValueOrDefault(language, LanguageCode.SP);
		if (LanguageCode.EN == languageCode) {
			setLinkSpanish(getLinkEnglish());
			setLinkPortuguese(getLinkEnglish());
		} else if (LanguageCode.PT == languageCode) {
			setLinkEnglish(getLinkPortuguese());
			setLinkSpanish(getLinkPortuguese());
		} else {
			setLinkEnglish(getLinkSpanish());
			setLinkPortuguese(getLinkSpanish());
		}
	}

	public String getVotingPeriodStartDateString() {
		Date startDate = getVotingPeriodStartDate();
		if (startDate == null) {
			return "";
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		return simpleDateFormat.format(new DateTime(startDate).plusHours(getDiffUTC()).toDate()) + DateTimeUtils.UTC_SUFFIX;
	}

	public String getVotingPeriodEndDateString() {
		Date endDate = getVotingPeriodEndDate();
		if (endDate == null) {
			return "";
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		return simpleDateFormat.format(new DateTime(endDate).plusHours(getDiffUTC()).toDate()) + DateTimeUtils.UTC_SUFFIX;
	}

	@Deprecated
	public String getStartDateString() {
		return getVotingPeriodStartDateString();
	}

	@Deprecated
	public String getEndDateString() {
		return getVotingPeriodEndDateString();
	}

	public Date getVotingPeriodStartDate() {
		return votingPeriodStartDate;
	}

	public void setVotingPeriodStartDate(Date votingPeriodStartDate) {
		this.votingPeriodStartDate = votingPeriodStartDate;
	}

	public Date getVotingPeriodEndDate() {
		return votingPeriodEndDate != null ? votingPeriodEndDate : votingPeriodStartDate;
	}

	public void setVotingPeriodEndDate(Date votingPeriodEndDate) {
		this.votingPeriodEndDate = votingPeriodEndDate;
	}

	public String getNominationPeriodStartDateString() {
		Date startDate = getNominationPeriodStartDate();
		if (startDate == null) {
			return "";
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		return simpleDateFormat.format(new DateTime(startDate).plusHours(getDiffUTC()).toDate()) + DateTimeUtils.UTC_SUFFIX;
	}

	public String getNominationPeriodEndDateString() {
		Date endDate = getNominationPeriodEndDate();
		if (endDate == null) {
			return "";
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		return simpleDateFormat.format(new DateTime(endDate).plusHours(getDiffUTC()).toDate()) + DateTimeUtils.UTC_SUFFIX;
	}

	public Date getNominationPeriodStartDate() {
		return nominationPeriodStartDate;
	}

	public void setNominationPeriodStartDate(Date nominationPeriodStartDate) {
		this.nominationPeriodStartDate = nominationPeriodStartDate;
	}

	public Date getNominationPeriodEndDate() {
		return nominationPeriodEndDate != null ? nominationPeriodEndDate : nominationPeriodStartDate;
	}

	public void setNominationPeriodEndDate(Date nominationPeriodEndDate) {
		this.nominationPeriodEndDate = nominationPeriodEndDate;
	}

	public String getClosedDateString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		if (this.getClosedDate() == null) {
			return "";
		} else {
			return simpleDateFormat.format(new DateTime(getClosedDate()).plusHours(getDiffUTC()).toDate()) + DateTimeUtils.UTC_SUFFIX;
		}
	}

	public String getResultLink() {
		return LinksUtils.buildResultsLink(resultToken);
	}

	public String getTokenResultLink() {
		return LinksUtils.buildTokenResultLink(resultToken);
	}

	public String getTokenPublicElectionLink() {
		return LinksUtils.buildTokenQuestionLink(publicElectionToken);
	}

	@Deprecated
	public String getTokenQuestionLink() {
		return getTokenPublicElectionLink();
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

	public ElectionType getElectionType() {
		return electionType;
	}

	public void setElectionType(ElectionType electionType) {
		this.electionType = electionType;
	}

	public ElectionLinkRecoveryMode getPublicLinkRecoveryMode() {
		return publicLinkRecoveryMode;
	}

	public void setPublicLinkRecoveryMode(ElectionLinkRecoveryMode publicLinkRecoveryMode) {
		this.publicLinkRecoveryMode = publicLinkRecoveryMode;
	}

	public boolean isMigrated() {
		return migrated;
	}

	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
	}

	@Deprecated
	public Date getStartDate() {
		return getVotingPeriodStartDate();
	}

	@Deprecated
	public void setStartDate(Date startDate) {
		this.votingPeriodStartDate = startDate;
	}

	@Deprecated
	public Date getEndDate() {
		return getVotingPeriodEndDate();
	}

	@Deprecated
	public void setEndDate(Date endDate) {
		this.votingPeriodEndDate = endDate;
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

	public String getCallSpanish() {
		return callSpanish;
	}

	public void setCallSpanish(String callSpanish) {
		this.callSpanish = callSpanish;
	}

	public String getCallEnglish() {
		return callEnglish;
	}

	public void setCallEnglish(String callEnglish) {
		this.callEnglish = callEnglish;
	}

	public String getCallPortuguese() {
		return callPortuguese;
	}

	public void setCallPortuguese(String callPortuguese) {
		this.callPortuguese = callPortuguese;
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

	public boolean isDoNominationLinkAvailable() {
		return doNominationLinkAvailable;
	}

	public void setDoNominationLinkAvailable(boolean doNominationLinkAvailable) {
		this.doNominationLinkAvailable = doNominationLinkAvailable;
	}

	public boolean isNominationTasksLinkAvailable() {
		return nominationTasksLinkAvailable;
	}

	public void setNominationTasksLinkAvailable(boolean nominationTasksLinkAvailable) {
		this.nominationTasksLinkAvailable = nominationTasksLinkAvailable;
	}

	public boolean isNominationSupportLinkAvailable() {
		return nominationSupportLinkAvailable;
	}

	public void setNominationSupportLinkAvailable(boolean nominationSupportLinkAvailable) {
		this.nominationSupportLinkAvailable = nominationSupportLinkAvailable;
	}

	public boolean isPublicElectionLinkAvailable() {
		return publicElectionLinkAvailable;
	}

	public void setPublicElectionLinkAvailable(boolean publicElectionLinkAvailable) {
		this.publicElectionLinkAvailable = publicElectionLinkAvailable;
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

	public String getPublicElectionToken() {
		return publicElectionToken;
	}

	public void setPublicElectionToken(String publicElectionToken) {
		this.publicElectionToken = publicElectionToken;
	}

	@Deprecated
	public String getQuestionToken() {
		return getPublicElectionToken();
	}

	@Deprecated
	public void setQuestionToken(String questionToken) {
		setPublicElectionToken(questionToken);
	}

	public String getDefaultSender() {
		return defaultSender;
	}

	public void setDefaultSender(String defaultSender) {
		this.defaultSender = defaultSender;
	}

	public String getDefaultRecipient() {
		return defaultRecipient;
	}

	public void setDefaultRecipient(String defaultRecipient) {
		this.defaultRecipient = defaultRecipient;
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

	public boolean isCallSet() {
		return callSet;
	}

	public void setCallSet(boolean callSet) {
		this.callSet = callSet;
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
