package net.lacnic.elections.domain;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import net.lacnic.elections.utils.StringUtils;
import net.lacnic.elections.utils.LinksUtils;


@Entity
public class Election implements Serializable {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	private static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy HH:mm";
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

	@Transient
	String auxStartDate = "";
	@Transient
	String auxStartHour = "";
	@Transient
	String auxEndDate = "";
	@Transient
	String auxEndHour = "";


	public Election() {
		setMigrated(false);
		setCreationDate(new Date());
		setResultLinkAvailable(false);
		setVotingLinkAvailable(true);
		setAuditorLinkAvailable(false);
		setElectorsSet(false);
		setOnlySp(true);
		setCandidatesSet(false);
		setAuditorsSet(false);
		setRandomOrderCandidates(true);
		setResultToken(StringUtils.createSecureToken());
		setDiffUTC(3);
	}

	public Election(long id) {
		setElectionId(id);
		setMigrated(false);
		setCreationDate(new Date());
		setResultLinkAvailable(false);
		setVotingLinkAvailable(true);
		setAuditorLinkAvailable(false);
		setElectorsSet(false);
		setOnlySp(true);
		setCandidatesSet(false);
		setAuditorsSet(false);
		setRandomOrderCandidates(true);
		setResultToken(StringUtils.createSecureToken());
		setTitle("TODOS");
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

	private void setTitle(String title) {
		setTitleSpanish(title);
		setTitlePortuguese(title);
		setTitleEnglish(title);
	}

	public String getDescription(String displayName) {
		if (displayName.toLowerCase().contains("en") || displayName.toLowerCase().contains("english"))
			return getDescriptionEnglish();
		else if (displayName.toLowerCase().contains("pt") || displayName.toLowerCase().contains("portuguese"))
			return getDescriptionPortuguese();
		return getDescriptionSpanish();
	}

	public String getTitle(String displayName) {
		if (displayName.toLowerCase().contains("en") || displayName.toLowerCase().contains("english"))
			return getTitleEnglish();
		else if (displayName.toLowerCase().contains("pt") || displayName.toLowerCase().contains("portuguese"))
			return getTitlePortuguese();
		else
			return getTitleSpanish();
	}

	/**
	 * This method is called always before creating or editing an election, in both cases it updates dates.
	 * Timezone is no longer initializated here, it is calculated at creation time and can later be modified.
	 */
	public void initDatesStartEndDates() {
		if (!getAuxEndDate().isEmpty() && !getAuxStartDate().isEmpty() && !getAuxStartHour().isEmpty() && !getAuxEndHour().isEmpty()) {

			String startDatetime = getAuxStartDate() + " " + getAuxStartHour();
			String endDatetime = getAuxEndDate() + " " + getAuxEndHour();
			SimpleDateFormat sdf = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

			try {
				Date beforeSubtractStart = sdf.parse(startDatetime);
				Date beforeSubtractEnd = sdf.parse(endDatetime);
				setStartDate(new DateTime(beforeSubtractStart).minusHours(getDiffUTC()).toDate());
				setEndDate(new DateTime(beforeSubtractEnd).minusHours(getDiffUTC()).toDate());
			} catch (ParseException e) {
				appLogger.error(e);
			}
		}

	}

	/**
	 * This method is always called before displaying the election, time difference is added to show UTC times.
	 */
	public void initStringsStartEndDates() {
		Date unchangedStartDate = getStartDate();
		Date unchangedEndDate = getEndDate();
		if (unchangedEndDate != null && unchangedStartDate != null) {
			SimpleDateFormat day = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat hour = new SimpleDateFormat("HH:mm");

			setAuxStartDate(day.format(new DateTime(getStartDate()).plusHours(getDiffUTC()).toDate()));
			setAuxStartHour(hour.format(new DateTime(getStartDate()).plusHours(getDiffUTC()).toDate()));
			setAuxEndDate(day.format(new DateTime(getEndDate()).plusHours(getDiffUTC()).toDate()));
			setAuxEndHour(hour.format(new DateTime(getEndDate()).plusHours(getDiffUTC()).toDate()));
		}
	}

	public void copyLanguageDescriptions(String language)  {
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

	public void copyLanguageURLs(String language)  {
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

	public String getResultLink() {
		return LinksUtils.buildResultsLink(resultToken);
	}

	public String getStartDateString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		return simpleDateFormat.format(new DateTime(getStartDate()).plusHours(getDiffUTC()).toDate()) + " (UTC)";
	}

	public String getEndDateString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		return simpleDateFormat.format(new DateTime(getEndDate()).plusHours(getDiffUTC()).toDate()) + " (UTC)";
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

}
