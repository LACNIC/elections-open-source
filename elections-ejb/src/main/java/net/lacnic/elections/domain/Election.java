package net.lacnic.elections.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Transient;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import net.lacnic.elections.utils.DateTimeUtils;
import net.lacnic.elections.utils.LinksUtils;
import net.lacnic.elections.utils.StringUtils;

@Entity
public class Election implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "election_seq")
	@SequenceGenerator(name = "election_seq", sequenceName = "election_seq", allocationSize = 1)
	@Column(name = "election_id")
	private long electionId;

	@Column(nullable = true, name = "migration_id")
	private Long migrationId;

	@Column
	@Enumerated(EnumType.STRING)
	private ElectionCategory category;

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

	@Column(nullable = true, columnDefinition = "TEXT", name = "authorized_user_emails")
	private String authorizedUserEmails;

	@Column(nullable = true, columnDefinition = "TEXT", name = "authorized_support_emails")
	private String authorizedSupportEmails;

	@Column(nullable = true, columnDefinition = "TEXT", name = "authorized_nominate_emails")
	private String authorizedNominateEmails;

	@Column(nullable = true)
	private boolean electorsSet;

	@Column(nullable = true)
	private boolean candidatesSet;

	@Column(nullable = true)
	private boolean auditorsSet;

	@Column(nullable = true)
	private boolean organizationsSet;

	@Column(nullable = true)
	private boolean calendarSet;

	@Column(nullable = true)
	private boolean tasksSet;

	@Column(nullable = true)
	private boolean callSet;

	@Column(nullable = true)
	private boolean randomOrderCandidates;

	@Column(nullable = true)
	private Boolean manageVotersManual;

	@Column(nullable = true)
	private Boolean manageOrganizationsManual;

	@Column(nullable = true, name = "election_type")
	@Enumerated(EnumType.STRING)
	private ElectionType electionType;

	@Column(nullable = true, name = "public_link_recovery_mode")
	@Enumerated(EnumType.STRING)
	private ElectionLinkRecoveryMode publicLinkRecoveryMode;

	@Column(nullable = true)
	private int diffUTC;

	@Column(nullable = true)
	private Long campusCourse;

	@Column(nullable = true)
	private Long campusCourseEnglish;

	@Column(nullable = true)
	private Long campusCoursePortuguese;

	@OneToMany(mappedBy = "election", cascade = CascadeType.REMOVE)
	private List<Candidate> candidates;

	@OneToMany(mappedBy = "election", cascade = CascadeType.REMOVE)
	private List<UserVoter> userVoters;

	@OneToMany(mappedBy = "election", cascade = CascadeType.REMOVE)
	private List<Auditor> auditors;

	@OneToMany(mappedBy = "election", cascade = CascadeType.REMOVE)
	private List<ElectionEmailTemplate> electionTemplates;

	@OneToMany(mappedBy = "election", cascade = CascadeType.REMOVE)
	private List<Vote> votes;

	@OneToMany(mappedBy = "election", cascade = CascadeType.REMOVE)
	private List<Email> email;

	@OneToMany(mappedBy = "election", cascade = CascadeType.REMOVE)
	private List<ElectionCalendar> electionCalendars;

	@OneToMany(mappedBy = "election", cascade = CascadeType.REMOVE)
	private List<ElectionTask> electionTasks;

	@OneToMany(mappedBy = "election", cascade = CascadeType.REMOVE)
	private List<Nomination> nominations;

	@OneToMany(mappedBy = "election", cascade = CascadeType.REMOVE)
	private List<Organization> organizations;

	@OneToMany(mappedBy = "election", cascade = CascadeType.REMOVE)
	private List<CandidateQuestion> candidateQuestions;

	@OneToMany(mappedBy = "election", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ElectionRestrictedCountry> restrictedCountries;

	@Column(nullable = false)
	private boolean closed;

	@Column(nullable = true)
	private Date closedDate;

	@Transient
	String auxStartDate = "";

	@Transient
	String auxStartHour = "";

	@Transient
	String auxEndDate = "";

	@Transient
	String auxEndHour = "";

	@Transient
	private List<String> restrictedCountryCodes;

	@Transient
	private String restrictedCountrySelection;

	@Transient
	private Date votingPeriodStartDate;

	@Transient
	private Date votingPeriodEndDate;

	public Election() {
		setMigrated(false);
		setCreationDate(new Date());
		setResultLinkAvailable(true);
		setVotingLinkAvailable(true);
		setAuditorLinkAvailable(true);
		setDoNominationLinkAvailable(true);
		setNominationTasksLinkAvailable(true);
		setNominationSupportLinkAvailable(true);
		setPublicElectionLinkAvailable(true);
		setElectorsSet(false);
		setOnlySp(true);
		setCandidatesSet(false);
		setAuditorsSet(false);
		setOrganizationsSet(false);
		setCalendarSet(false);
		setTasksSet(false);
		setCallSet(false);
		setRandomOrderCandidates(true);
		setManageVotersManual(true);
		setManageOrganizationsManual(true);
		setElectionType(ElectionType.BOARD);
		setPublicLinkRecoveryMode(ElectionLinkRecoveryMode.defaultForElectionType(getElectionType()));
		setResultToken(StringUtils.createSecureToken());
		setPublicElectionToken(StringUtils.createSecureToken());
		setDiffUTC(3);
		setClosed(false);
	}

	public Election(long id) {
		setElectionId(id);
		setMigrated(false);
		setCreationDate(new Date());
		setResultLinkAvailable(true);
		setVotingLinkAvailable(true);
		setAuditorLinkAvailable(true);
		setDoNominationLinkAvailable(true);
		setNominationTasksLinkAvailable(true);
		setNominationSupportLinkAvailable(true);
		setPublicElectionLinkAvailable(true);
		setElectorsSet(false);
		setOnlySp(true);
		setCandidatesSet(false);
		setAuditorsSet(false);
		setOrganizationsSet(false);
		setCalendarSet(false);
		setTasksSet(false);
		setRandomOrderCandidates(true);
		setManageVotersManual(true);
		setManageOrganizationsManual(true);
		setElectionType(ElectionType.BOARD);
		setPublicLinkRecoveryMode(ElectionLinkRecoveryMode.defaultForElectionType(getElectionType()));
		setResultToken(StringUtils.createSecureToken());
		setPublicElectionToken(StringUtils.createSecureToken());
		setTitle("TODOS");
		setClosed(false);
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
		return (isStarted() && !isFinished() && isVotingLinkAvailable());
	}

	private void setTitle(String title) {
		setTitleSpanish(title);
		setTitlePortuguese(title);
		setTitleEnglish(title);
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

	/**
	 * This method is called always before creating or editing an election, in both cases it updates dates. Timezone is no longer initializated here, it is calculated at creation time and can later be modified.
	 */
	public void initDatesStartEndDates() {
		// Deprecated: voting dates are now resolved from ElectionCalendar (N_16_PERIODO_VOTING).
	}

	/**
	 * This method is always called before displaying the election, time difference is added to show UTC times.
	 */
	public void initStringsStartEndDates() {
		Date unchangedStartDate = getVotingPeriodStartDate();
		Date unchangedEndDate = getVotingPeriodEndDate();
		if (unchangedEndDate != null && unchangedStartDate != null) {
			SimpleDateFormat day = new SimpleDateFormat(DateTimeUtils.ELECTION_DATE_FORMAT);
			SimpleDateFormat hour = new SimpleDateFormat(DateTimeUtils.ELECTION_TIME_FORMAT);

			setAuxStartDate(day.format(new DateTime(unchangedStartDate).plusHours(getDiffUTC()).toDate()));
			setAuxStartHour(hour.format(new DateTime(unchangedStartDate).plusHours(getDiffUTC()).toDate()));
			setAuxEndDate(day.format(new DateTime(unchangedEndDate).plusHours(getDiffUTC()).toDate()));
			setAuxEndHour(hour.format(new DateTime(unchangedEndDate).plusHours(getDiffUTC()).toDate()));
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

	public String getVotingPeriodStartDateString() {
		Date startDate = getVotingPeriodStartDate();
		if (startDate == null) {
			return "";
		}
		return DateTimeUtils.getElectionDateTimeString(new DateTime(startDate).plusHours(getDiffUTC()).toDate()) + DateTimeUtils.UTC_SUFFIX;
	}

	public String getVotingPeriodEndDateString() {
		Date endDate = getVotingPeriodEndDate();
		if (endDate == null) {
			return "";
		}
		return DateTimeUtils.getElectionDateTimeString(new DateTime(endDate).plusHours(getDiffUTC()).toDate()) + DateTimeUtils.UTC_SUFFIX;
	}

	@Deprecated
	public String getStartDateString() {
		return getVotingPeriodStartDateString();
	}

	@Deprecated
	public String getEndDateString() {
		return getVotingPeriodEndDateString();
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

	public String getCreationDateString() {
		return DateTimeUtils.getElectionDateTimeString(new DateTime(getCreationDate()).plusHours(getDiffUTC()).toDate()) + DateTimeUtils.UTC_SUFFIX;
	}

	public String getClosedDateString() {
		if (this.getClosedDate() == null) {
			return "";
		} else {
			return DateTimeUtils.getElectionDateTimeString(new DateTime(getClosedDate()).plusHours(getDiffUTC()).toDate()) + DateTimeUtils.UTC_SUFFIX;
		}
	}

	public long getElectionId() {
		return electionId;
	}

	public void setElectionId(long electionId) {
		this.electionId = electionId;
	}

	public Long getMigrationId() {
		return migrationId;
	}

	public void setMigrationId(Long migrationId) {
		this.migrationId = migrationId;
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

	public String getAuthorizedUserEmails() {
		return authorizedUserEmails;
	}

	public void setAuthorizedUserEmails(String authorizedUserEmails) {
		this.authorizedUserEmails = authorizedUserEmails;
	}

	public String getAuthorizedSupportEmails() {
		return authorizedSupportEmails;
	}

	public void setAuthorizedSupportEmails(String authorizedSupportEmails) {
		this.authorizedSupportEmails = authorizedSupportEmails;
	}

	public String getAuthorizedNominateEmails() {
		return authorizedNominateEmails;
	}

	public void setAuthorizedNominateEmails(String authorizedNominateEmails) {
		this.authorizedNominateEmails = authorizedNominateEmails;
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

	public boolean isOrganizationsSet() {
		return organizationsSet;
	}

	public void setOrganizationsSet(boolean organizationsSet) {
		this.organizationsSet = organizationsSet;
	}

	public boolean isCalendarSet() {
		return calendarSet;
	}

	public void setCalendarSet(boolean calendarSet) {
		this.calendarSet = calendarSet;
	}

	public boolean isTasksSet() {
		return tasksSet;
	}

	public void setTasksSet(boolean tasksSet) {
		this.tasksSet = tasksSet;
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

	public Long getCampusCourse() {
		return campusCourse;
	}

	public void setCampusCourse(Long campusCourse) {
		this.campusCourse = campusCourse;
	}

	public Long getCampusCourseEnglish() {
		return campusCourseEnglish;
	}

	public void setCampusCourseEnglish(Long campusCourseEnglish) {
		this.campusCourseEnglish = campusCourseEnglish;
	}

	public Long getCampusCoursePortuguese() {
		return campusCoursePortuguese;
	}

	public void setCampusCoursePortuguese(Long campusCoursePortuguese) {
		this.campusCoursePortuguese = campusCoursePortuguese;
	}

	public boolean isManageVotersManual() {
		return manageVotersManual == null || Boolean.TRUE.equals(manageVotersManual);
	}

	public void setManageVotersManual(boolean manageVotersManual) {
		this.manageVotersManual = manageVotersManual;
	}

	public boolean isManageOrganizationsManual() {
		return manageOrganizationsManual == null || Boolean.TRUE.equals(manageOrganizationsManual);
	}

	public void setManageOrganizationsManual(boolean manageOrganizationsManual) {
		this.manageOrganizationsManual = manageOrganizationsManual;
	}

	public ElectionType getElectionType() {
		return electionType;
	}

	public void setElectionType(ElectionType electionType) {
		this.electionType = electionType;
	}

	public ElectionLinkRecoveryMode getPublicLinkRecoveryMode() {
		return publicLinkRecoveryMode != null ? publicLinkRecoveryMode : ElectionLinkRecoveryMode.NONE;
	}

	public void setPublicLinkRecoveryMode(ElectionLinkRecoveryMode publicLinkRecoveryMode) {
		this.publicLinkRecoveryMode = publicLinkRecoveryMode != null ? publicLinkRecoveryMode : ElectionLinkRecoveryMode.NONE;
	}

	@Transient
	public ElectionType getEffectiveElectionType() {
		return electionType;
	}

	public List<ElectionRestrictedCountry> getRestrictedCountries() {
		return restrictedCountries;
	}

	public void setRestrictedCountries(List<ElectionRestrictedCountry> restrictedCountries) {
		if (this.restrictedCountries == restrictedCountries) {
			this.restrictedCountryCodes = null;
			return;
		}
		if (this.restrictedCountries == null) {
			this.restrictedCountries = restrictedCountries;
		} else {
			this.restrictedCountries.clear();
			if (restrictedCountries != null) {
				this.restrictedCountries.addAll(restrictedCountries);
			}
		}
		if (this.restrictedCountries != null) {
			for (ElectionRestrictedCountry restrictedCountry : this.restrictedCountries) {
				if (restrictedCountry != null) {
					restrictedCountry.setElection(this);
				}
			}
		}
		this.restrictedCountryCodes = null;
	}

	public List<String> getRestrictedCountryCodes() {
		if (restrictedCountryCodes == null) {
			restrictedCountryCodes = new ArrayList<>();
			if (restrictedCountries != null) {
				for (ElectionRestrictedCountry restrictedCountry : restrictedCountries) {
					if (restrictedCountry != null && restrictedCountry.getCountryCode() != null) {
						restrictedCountryCodes.add(restrictedCountry.getCountryCode());
					}
				}
			}
		}
		return restrictedCountryCodes;
	}

	public void setRestrictedCountryCodes(List<String> restrictedCountryCodes) {
		this.restrictedCountryCodes = restrictedCountryCodes;
	}

	public String getRestrictedCountrySelection() {
		return restrictedCountrySelection;
	}

	public void setRestrictedCountrySelection(String restrictedCountrySelection) {
		this.restrictedCountrySelection = restrictedCountrySelection;
	}

	public void applyRestrictedCountryCodes() {
		if (restrictedCountryCodes == null) {
			return;
		}
		if (restrictedCountries == null) {
			restrictedCountries = new ArrayList<>();
		}

		Set<String> targetCountryCodes = new LinkedHashSet<>();
		for (String countryCode : restrictedCountryCodes) {
			if (countryCode == null || countryCode.trim().isEmpty()) {
				continue;
			}
			String normalizedCountryCode = countryCode.trim().toUpperCase(java.util.Locale.ROOT);
			if (normalizedCountryCode.length() != 2) {
				continue;
			}
			targetCountryCodes.add(normalizedCountryCode);
		}

		Set<String> existingCountryCodes = new LinkedHashSet<>();
		for (java.util.Iterator<ElectionRestrictedCountry> iterator = restrictedCountries.iterator(); iterator.hasNext();) {
			ElectionRestrictedCountry restrictedCountry = iterator.next();
			if (restrictedCountry == null || restrictedCountry.getCountryCode() == null) {
				iterator.remove();
				continue;
			}
			String normalizedCountryCode = restrictedCountry.getCountryCode().trim();
			if (normalizedCountryCode.isEmpty()) {
				iterator.remove();
				continue;
			}
			normalizedCountryCode = normalizedCountryCode.toUpperCase(java.util.Locale.ROOT);
			if (normalizedCountryCode.length() != 2 || !targetCountryCodes.contains(normalizedCountryCode) || existingCountryCodes.contains(normalizedCountryCode)) {
				iterator.remove();
				continue;
			}
			restrictedCountry.setCountryCode(normalizedCountryCode);
			existingCountryCodes.add(normalizedCountryCode);
		}

		for (String targetCountryCode : targetCountryCodes) {
			if (!existingCountryCodes.contains(targetCountryCode)) {
				restrictedCountries.add(new ElectionRestrictedCountry(this, targetCountryCode));
			}
		}
	}

	public List<Candidate> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<Candidate> candidates) {
		this.candidates = candidates;
	}

	public List<UserVoter> getUserVoters() {
		return userVoters;
	}

	public void setUserVoters(List<UserVoter> userVoters) {
		this.userVoters = userVoters;
	}

	public List<Auditor> getAuditors() {
		return auditors;
	}

	public void setAuditors(List<Auditor> auditors) {
		this.auditors = auditors;
	}

	public List<ElectionEmailTemplate> getElectionTemplates() {
		return electionTemplates;
	}

	public void setElectionTemplates(List<ElectionEmailTemplate> electionTemplates) {
		this.electionTemplates = electionTemplates;
	}

	public List<Vote> getVotes() {
		return votes;
	}

	public void setVotes(List<Vote> votes) {
		this.votes = votes;
	}

	public List<Email> getEmail() {
		return email;
	}

	public void setEmail(List<Email> email) {
		this.email = email;
	}

	public List<Organization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<Organization> organizations) {
		this.organizations = organizations;
	}

	public List<CandidateQuestion> getCandidateQuestions() {
		return candidateQuestions;
	}

	public void setCandidateQuestions(List<CandidateQuestion> candidateQuestions) {
		this.candidateQuestions = candidateQuestions;
	}

	public String getAuxStartDate() {
		return auxStartDate;
	}

	public void setAuxStartDate(String auxStartDate) {
		this.auxStartDate = auxStartDate;
	}

	public String getAuxStartHour() {
		return auxStartHour;
	}

	public void setAuxStartHour(String auxStartHour) {
		this.auxStartHour = auxStartHour;
	}

	public String getAuxEndDate() {
		return auxEndDate;
	}

	public void setAuxEndDate(String auxEndDate) {
		this.auxEndDate = auxEndDate;
	}

	public String getAuxEndHour() {
		return auxEndHour;
	}

	public void setAuxEndHour(String auxEndHour) {
		this.auxEndHour = auxEndHour;
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
