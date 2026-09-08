package net.lacnic.elections.domain.services.publicelection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PublicElectionCoreSnapshot implements Serializable {

	private static final long serialVersionUID = -4264650503914034178L;

	private PublicElectionSnapshotMetadata metadata;
	private ElectionData election;
	private PublicElectionVisibilityReport visibility = new PublicElectionVisibilityReport();
	private List<CalendarEventData> publicCalendarEvents = new ArrayList<>();
	private List<ResultPublicationStageData> resultPublicationStages = new ArrayList<>();
	private ResultSummaryData resultSummary;
	private RollSummaryData rollSummary;
	private RecoveryData recovery;
	private QuestionCatalogData questionCatalog;
	private OfficialResultData officialResult;
	private List<CandidateData> candidates = new ArrayList<>();
	private List<CandidateNominationSummaryData> candidateNominationSummaries = new ArrayList<>();

	public PublicElectionSnapshotMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(PublicElectionSnapshotMetadata metadata) {
		this.metadata = metadata;
	}

	public ElectionData getElection() {
		return election;
	}

	public void setElection(ElectionData election) {
		this.election = election;
	}

	public PublicElectionVisibilityReport getVisibility() {
		return visibility;
	}

	public void setVisibility(PublicElectionVisibilityReport visibility) {
		this.visibility = visibility;
	}

	public List<CalendarEventData> getPublicCalendarEvents() {
		return publicCalendarEvents;
	}

	public void setPublicCalendarEvents(List<CalendarEventData> publicCalendarEvents) {
		this.publicCalendarEvents = publicCalendarEvents;
	}

	public List<ResultPublicationStageData> getResultPublicationStages() {
		return resultPublicationStages;
	}

	public void setResultPublicationStages(List<ResultPublicationStageData> resultPublicationStages) {
		this.resultPublicationStages = resultPublicationStages;
	}

	public ResultSummaryData getResultSummary() {
		return resultSummary;
	}

	public void setResultSummary(ResultSummaryData resultSummary) {
		this.resultSummary = resultSummary;
	}

	public RollSummaryData getRollSummary() {
		return rollSummary;
	}

	public void setRollSummary(RollSummaryData rollSummary) {
		this.rollSummary = rollSummary;
	}

	public RecoveryData getRecovery() {
		return recovery;
	}

	public void setRecovery(RecoveryData recovery) {
		this.recovery = recovery;
	}

	public QuestionCatalogData getQuestionCatalog() {
		return questionCatalog;
	}

	public void setQuestionCatalog(QuestionCatalogData questionCatalog) {
		this.questionCatalog = questionCatalog;
	}

	public OfficialResultData getOfficialResult() {
		return officialResult;
	}

	public void setOfficialResult(OfficialResultData officialResult) {
		this.officialResult = officialResult;
	}

	public List<CandidateData> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<CandidateData> candidates) {
		this.candidates = candidates;
	}

	public List<CandidateNominationSummaryData> getCandidateNominationSummaries() {
		return candidateNominationSummaries;
	}

	public void setCandidateNominationSummaries(List<CandidateNominationSummaryData> candidateNominationSummaries) {
		this.candidateNominationSummaries = candidateNominationSummaries;
	}

	public static class LocalizedTextData implements Serializable {
		private static final long serialVersionUID = -2357338391654306103L;

		private String spanish;
		private String english;
		private String portuguese;

		public String getSpanish() {
			return spanish;
		}

		public void setSpanish(String spanish) {
			this.spanish = spanish;
		}

		public String getEnglish() {
			return english;
		}

		public void setEnglish(String english) {
			this.english = english;
		}

		public String getPortuguese() {
			return portuguese;
		}

		public void setPortuguese(String portuguese) {
			this.portuguese = portuguese;
		}
	}

	public static class ElectionData implements Serializable {
		private static final long serialVersionUID = 5183128231575852265L;

		private Long electionId;
		private String categoryCode;
		private String electionTypeCode;
		private String titleSpanish;
		private String titleEnglish;
		private String titlePortuguese;
		private String descriptionSpanish;
		private String descriptionEnglish;
		private String descriptionPortuguese;
		private LocalizedTextData call;
		private Boolean onlySpanish;
		private Boolean randomOrderCandidates;
		private Boolean publishedAbstentionCandidate;
		private Boolean resultLinkAvailable;
		private Boolean publicElectionLinkAvailable;
		private Boolean closed;
		private String publicLinkRecoveryModeCode;
		private Date votingPeriodStartUtc;
		private Date votingPeriodEndUtc;
		private String resultContactEmail;
		private String publicNominationUrl;

		public Long getElectionId() {
			return electionId;
		}

		public void setElectionId(Long electionId) {
			this.electionId = electionId;
		}

		public String getCategoryCode() {
			return categoryCode;
		}

		public void setCategoryCode(String categoryCode) {
			this.categoryCode = categoryCode;
		}

		public String getElectionTypeCode() {
			return electionTypeCode;
		}

		public void setElectionTypeCode(String electionTypeCode) {
			this.electionTypeCode = electionTypeCode;
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

		public LocalizedTextData getCall() {
			return call;
		}

		public void setCall(LocalizedTextData call) {
			this.call = call;
		}

		public Boolean getOnlySpanish() {
			return onlySpanish;
		}

		public void setOnlySpanish(Boolean onlySpanish) {
			this.onlySpanish = onlySpanish;
		}

		public Boolean getRandomOrderCandidates() {
			return randomOrderCandidates;
		}

		public void setRandomOrderCandidates(Boolean randomOrderCandidates) {
			this.randomOrderCandidates = randomOrderCandidates;
		}

		public Boolean getPublishedAbstentionCandidate() {
			return publishedAbstentionCandidate;
		}

		public void setPublishedAbstentionCandidate(Boolean publishedAbstentionCandidate) {
			this.publishedAbstentionCandidate = publishedAbstentionCandidate;
		}

		public Boolean getResultLinkAvailable() {
			return resultLinkAvailable;
		}

		public void setResultLinkAvailable(Boolean resultLinkAvailable) {
			this.resultLinkAvailable = resultLinkAvailable;
		}

		public Boolean getPublicElectionLinkAvailable() {
			return publicElectionLinkAvailable;
		}

		public void setPublicElectionLinkAvailable(Boolean publicElectionLinkAvailable) {
			this.publicElectionLinkAvailable = publicElectionLinkAvailable;
		}

		public Boolean getClosed() {
			return closed;
		}

		public void setClosed(Boolean closed) {
			this.closed = closed;
		}

		public String getPublicLinkRecoveryModeCode() {
			return publicLinkRecoveryModeCode;
		}

		public void setPublicLinkRecoveryModeCode(String publicLinkRecoveryModeCode) {
			this.publicLinkRecoveryModeCode = publicLinkRecoveryModeCode;
		}

		public Date getVotingPeriodStartUtc() {
			return votingPeriodStartUtc;
		}

		public void setVotingPeriodStartUtc(Date votingPeriodStartUtc) {
			this.votingPeriodStartUtc = votingPeriodStartUtc;
		}

		public Date getVotingPeriodEndUtc() {
			return votingPeriodEndUtc;
		}

		public void setVotingPeriodEndUtc(Date votingPeriodEndUtc) {
			this.votingPeriodEndUtc = votingPeriodEndUtc;
		}

		public String getResultContactEmail() {
			return resultContactEmail;
		}

		public void setResultContactEmail(String resultContactEmail) {
			this.resultContactEmail = resultContactEmail;
		}

		public String getPublicNominationUrl() {
			return publicNominationUrl;
		}

		public void setPublicNominationUrl(String publicNominationUrl) {
			this.publicNominationUrl = publicNominationUrl;
		}
	}

	public static class OfficialResultData implements Serializable {
		private static final long serialVersionUID = -8476855340527666289L;

		private String resultSpanish;
		private String resultEnglish;
		private String resultPortuguese;
		private String resultLetterSpanishUrl;
		private String resultLetterEnglishUrl;
		private String resultLetterPortugueseUrl;

		public String getResultSpanish() {
			return resultSpanish;
		}

		public void setResultSpanish(String resultSpanish) {
			this.resultSpanish = resultSpanish;
		}

		public String getResultEnglish() {
			return resultEnglish;
		}

		public void setResultEnglish(String resultEnglish) {
			this.resultEnglish = resultEnglish;
		}

		public String getResultPortuguese() {
			return resultPortuguese;
		}

		public void setResultPortuguese(String resultPortuguese) {
			this.resultPortuguese = resultPortuguese;
		}

		public String getResultLetterSpanishUrl() {
			return resultLetterSpanishUrl;
		}

		public void setResultLetterSpanishUrl(String resultLetterSpanishUrl) {
			this.resultLetterSpanishUrl = resultLetterSpanishUrl;
		}

		public String getResultLetterEnglishUrl() {
			return resultLetterEnglishUrl;
		}

		public void setResultLetterEnglishUrl(String resultLetterEnglishUrl) {
			this.resultLetterEnglishUrl = resultLetterEnglishUrl;
		}

		public String getResultLetterPortugueseUrl() {
			return resultLetterPortugueseUrl;
		}

		public void setResultLetterPortugueseUrl(String resultLetterPortugueseUrl) {
			this.resultLetterPortugueseUrl = resultLetterPortugueseUrl;
		}
	}

	public static class CalendarEventData implements Serializable {
		private static final long serialVersionUID = -7314196159989945365L;

		private String keyCode;
		private Date startUtc;
		private Date endUtc;
		private Boolean singleEvent;

		public String getKeyCode() {
			return keyCode;
		}

		public void setKeyCode(String keyCode) {
			this.keyCode = keyCode;
		}

		public Date getStartUtc() {
			return startUtc;
		}

		public void setStartUtc(Date startUtc) {
			this.startUtc = startUtc;
		}

		public Date getEndUtc() {
			return endUtc;
		}

		public void setEndUtc(Date endUtc) {
			this.endUtc = endUtc;
		}

		public Boolean getSingleEvent() {
			return singleEvent;
		}

		public void setSingleEvent(Boolean singleEvent) {
			this.singleEvent = singleEvent;
		}
	}

	public static class ResultPublicationStageData implements Serializable {
		private static final long serialVersionUID = 1235688157859598214L;

		private String stageCode;
		private Date startUtc;
		private Date endUtc;
		private Boolean current;
		private Boolean configured;

		public String getStageCode() {
			return stageCode;
		}

		public void setStageCode(String stageCode) {
			this.stageCode = stageCode;
		}

		public Date getStartUtc() {
			return startUtc;
		}

		public void setStartUtc(Date startUtc) {
			this.startUtc = startUtc;
		}

		public Date getEndUtc() {
			return endUtc;
		}

		public void setEndUtc(Date endUtc) {
			this.endUtc = endUtc;
		}

		public Boolean getCurrent() {
			return current;
		}

		public void setCurrent(Boolean current) {
			this.current = current;
		}

		public Boolean getConfigured() {
			return configured;
		}

		public void setConfigured(Boolean configured) {
			this.configured = configured;
		}
	}

	public static class ResultSummaryData implements Serializable {
		private static final long serialVersionUID = -8699615027586289090L;

		private String publicationStageCode;
		private Long totalVotes;
		private Long enabledVoters;
		private Long organizationsVoted;
		private Double participationPercentage;
		private List<ResultRowData> rows = new ArrayList<>();

		public String getPublicationStageCode() {
			return publicationStageCode;
		}

		public void setPublicationStageCode(String publicationStageCode) {
			this.publicationStageCode = publicationStageCode;
		}

		public Long getTotalVotes() {
			return totalVotes;
		}

		public void setTotalVotes(Long totalVotes) {
			this.totalVotes = totalVotes;
		}

		public Long getEnabledVoters() {
			return enabledVoters;
		}

		public void setEnabledVoters(Long enabledVoters) {
			this.enabledVoters = enabledVoters;
		}

		public Long getOrganizationsVoted() {
			return organizationsVoted;
		}

		public void setOrganizationsVoted(Long organizationsVoted) {
			this.organizationsVoted = organizationsVoted;
		}

		public Double getParticipationPercentage() {
			return participationPercentage;
		}

		public void setParticipationPercentage(Double participationPercentage) {
			this.participationPercentage = participationPercentage;
		}

		public List<ResultRowData> getRows() {
			return rows;
		}

		public void setRows(List<ResultRowData> rows) {
			this.rows = rows;
		}
	}

	public static class ResultRowData implements Serializable {
		private static final long serialVersionUID = 6625373536680446812L;

		private Long candidateId;
		private String candidateName;
		private Long voteCount;
		private Double percentage;
		private Boolean winner;

		public Long getCandidateId() {
			return candidateId;
		}

		public void setCandidateId(Long candidateId) {
			this.candidateId = candidateId;
		}

		public String getCandidateName() {
			return candidateName;
		}

		public void setCandidateName(String candidateName) {
			this.candidateName = candidateName;
		}

		public Long getVoteCount() {
			return voteCount;
		}

		public void setVoteCount(Long voteCount) {
			this.voteCount = voteCount;
		}

		public Double getPercentage() {
			return percentage;
		}

		public void setPercentage(Double percentage) {
			this.percentage = percentage;
		}

		public Boolean getWinner() {
			return winner;
		}

		public void setWinner(Boolean winner) {
			this.winner = winner;
		}
	}

	public static class RollSummaryData implements Serializable {
		private static final long serialVersionUID = -7932984306260068337L;

		private Long totalRows;
		private Long totalOrganizations;
		private Long totalCountries;
		private Long spanishVoters;
		private Long englishVoters;
		private Long portugueseVoters;

		public Long getTotalRows() {
			return totalRows;
		}

		public void setTotalRows(Long totalRows) {
			this.totalRows = totalRows;
		}

		public Long getTotalOrganizations() {
			return totalOrganizations;
		}

		public void setTotalOrganizations(Long totalOrganizations) {
			this.totalOrganizations = totalOrganizations;
		}

		public Long getTotalCountries() {
			return totalCountries;
		}

		public void setTotalCountries(Long totalCountries) {
			this.totalCountries = totalCountries;
		}

		public Long getSpanishVoters() {
			return spanishVoters;
		}

		public void setSpanishVoters(Long spanishVoters) {
			this.spanishVoters = spanishVoters;
		}

		public Long getEnglishVoters() {
			return englishVoters;
		}

		public void setEnglishVoters(Long englishVoters) {
			this.englishVoters = englishVoters;
		}

		public Long getPortugueseVoters() {
			return portugueseVoters;
		}

		public void setPortugueseVoters(Long portugueseVoters) {
			this.portugueseVoters = portugueseVoters;
		}
	}

	public static class RecoveryData implements Serializable {
		private static final long serialVersionUID = -1483954505758874560L;

		private Boolean visible;
		private String modeCode;

		public Boolean getVisible() {
			return visible;
		}

		public void setVisible(Boolean visible) {
			this.visible = visible;
		}

		public String getModeCode() {
			return modeCode;
		}

		public void setModeCode(String modeCode) {
			this.modeCode = modeCode;
		}
	}

	public static class QuestionCatalogData implements Serializable {
		private static final long serialVersionUID = -214756097861397486L;

		private List<LocalizedTextData> otherStatutoryQuestions = new ArrayList<>();
		private List<LocalizedTextData> otherNonStatutoryQuestions = new ArrayList<>();

		public List<LocalizedTextData> getOtherStatutoryQuestions() {
			return otherStatutoryQuestions;
		}

		public void setOtherStatutoryQuestions(List<LocalizedTextData> otherStatutoryQuestions) {
			this.otherStatutoryQuestions = otherStatutoryQuestions;
		}

		public List<LocalizedTextData> getOtherNonStatutoryQuestions() {
			return otherNonStatutoryQuestions;
		}

		public void setOtherNonStatutoryQuestions(List<LocalizedTextData> otherNonStatutoryQuestions) {
			this.otherNonStatutoryQuestions = otherNonStatutoryQuestions;
		}
	}

	public static class CandidateData implements Serializable {
		private static final long serialVersionUID = -4613464168457807789L;

		private Long candidateId;
		private String name;
		private Integer candidateOrder;
		private String statusCode;
		private Boolean winner;
		private Boolean publicCandidate;
		private String primaryCountryCode;
		private List<String> otherCountryCodes = new ArrayList<>();
		private List<String> workOrganizationNames = new ArrayList<>();
		private List<String> supportOrganizationNames = new ArrayList<>();
		private String nominationOrganizationName;
		private LocalizedTextData nominationReason;
		private String linkedinUrl;
		private LocalizedTextData bio;
		private List<QuestionAnswerData> statutoryAnswers = new ArrayList<>();
		private List<QuestionAnswerData> nonStatutoryAnswers = new ArrayList<>();
		private List<CommunityQuestionData> communityQuestions = new ArrayList<>();

		public Long getCandidateId() {
			return candidateId;
		}

		public void setCandidateId(Long candidateId) {
			this.candidateId = candidateId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getCandidateOrder() {
			return candidateOrder;
		}

		public void setCandidateOrder(Integer candidateOrder) {
			this.candidateOrder = candidateOrder;
		}

		public String getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(String statusCode) {
			this.statusCode = statusCode;
		}

		public Boolean getWinner() {
			return winner;
		}

		public void setWinner(Boolean winner) {
			this.winner = winner;
		}

		public Boolean getPublicCandidate() {
			return publicCandidate;
		}

		public void setPublicCandidate(Boolean publicCandidate) {
			this.publicCandidate = publicCandidate;
		}

		public String getPrimaryCountryCode() {
			return primaryCountryCode;
		}

		public void setPrimaryCountryCode(String primaryCountryCode) {
			this.primaryCountryCode = primaryCountryCode;
		}

		public List<String> getOtherCountryCodes() {
			return otherCountryCodes;
		}

		public void setOtherCountryCodes(List<String> otherCountryCodes) {
			this.otherCountryCodes = otherCountryCodes;
		}

		public List<String> getWorkOrganizationNames() {
			return workOrganizationNames;
		}

		public void setWorkOrganizationNames(List<String> workOrganizationNames) {
			this.workOrganizationNames = workOrganizationNames;
		}

		public List<String> getSupportOrganizationNames() {
			return supportOrganizationNames;
		}

		public void setSupportOrganizationNames(List<String> supportOrganizationNames) {
			this.supportOrganizationNames = supportOrganizationNames;
		}

		public String getNominationOrganizationName() {
			return nominationOrganizationName;
		}

		public void setNominationOrganizationName(String nominationOrganizationName) {
			this.nominationOrganizationName = nominationOrganizationName;
		}

		public LocalizedTextData getNominationReason() {
			return nominationReason;
		}

		public void setNominationReason(LocalizedTextData nominationReason) {
			this.nominationReason = nominationReason;
		}

		public String getLinkedinUrl() {
			return linkedinUrl;
		}

		public void setLinkedinUrl(String linkedinUrl) {
			this.linkedinUrl = linkedinUrl;
		}

		public LocalizedTextData getBio() {
			return bio;
		}

		public void setBio(LocalizedTextData bio) {
			this.bio = bio;
		}

		public List<QuestionAnswerData> getStatutoryAnswers() {
			return statutoryAnswers;
		}

		public void setStatutoryAnswers(List<QuestionAnswerData> statutoryAnswers) {
			this.statutoryAnswers = statutoryAnswers;
		}

		public List<QuestionAnswerData> getNonStatutoryAnswers() {
			return nonStatutoryAnswers;
		}

		public void setNonStatutoryAnswers(List<QuestionAnswerData> nonStatutoryAnswers) {
			this.nonStatutoryAnswers = nonStatutoryAnswers;
		}

		public List<CommunityQuestionData> getCommunityQuestions() {
			return communityQuestions;
		}

		public void setCommunityQuestions(List<CommunityQuestionData> communityQuestions) {
			this.communityQuestions = communityQuestions;
		}
	}

	public static class QuestionAnswerData implements Serializable {
		private static final long serialVersionUID = 7743154857609665660L;

		private LocalizedTextData questionText;
		private LocalizedTextData answerText;
		private Date dateUtc;

		public LocalizedTextData getQuestionText() {
			return questionText;
		}

		public void setQuestionText(LocalizedTextData questionText) {
			this.questionText = questionText;
		}

		public LocalizedTextData getAnswerText() {
			return answerText;
		}

		public void setAnswerText(LocalizedTextData answerText) {
			this.answerText = answerText;
		}

		public Date getDateUtc() {
			return dateUtc;
		}

		public void setDateUtc(Date dateUtc) {
			this.dateUtc = dateUtc;
		}
	}

	public static class CommunityQuestionData implements Serializable {
		private static final long serialVersionUID = 6041045017837885431L;

		private Long candidateQuestionId;
		private String statusCode;
		private String askedByInitials;
		private Date questionDateUtc;
		private Date answerDateUtc;
		private String questionDisplayMode;
		private String answerDisplayMode;
		private LocalizedTextData questionText;
		private LocalizedTextData answerText;
		private LocalizedTextData questionMaskedPreview;
		private LocalizedTextData answerMaskedPreview;

		public Long getCandidateQuestionId() {
			return candidateQuestionId;
		}

		public void setCandidateQuestionId(Long candidateQuestionId) {
			this.candidateQuestionId = candidateQuestionId;
		}

		public String getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(String statusCode) {
			this.statusCode = statusCode;
		}

		public String getAskedByInitials() {
			return askedByInitials;
		}

		public void setAskedByInitials(String askedByInitials) {
			this.askedByInitials = askedByInitials;
		}

		public Date getQuestionDateUtc() {
			return questionDateUtc;
		}

		public void setQuestionDateUtc(Date questionDateUtc) {
			this.questionDateUtc = questionDateUtc;
		}

		public Date getAnswerDateUtc() {
			return answerDateUtc;
		}

		public void setAnswerDateUtc(Date answerDateUtc) {
			this.answerDateUtc = answerDateUtc;
		}

		public String getQuestionDisplayMode() {
			return questionDisplayMode;
		}

		public void setQuestionDisplayMode(String questionDisplayMode) {
			this.questionDisplayMode = questionDisplayMode;
		}

		public String getAnswerDisplayMode() {
			return answerDisplayMode;
		}

		public void setAnswerDisplayMode(String answerDisplayMode) {
			this.answerDisplayMode = answerDisplayMode;
		}

		public LocalizedTextData getQuestionText() {
			return questionText;
		}

		public void setQuestionText(LocalizedTextData questionText) {
			this.questionText = questionText;
		}

		public LocalizedTextData getAnswerText() {
			return answerText;
		}

		public void setAnswerText(LocalizedTextData answerText) {
			this.answerText = answerText;
		}

		public LocalizedTextData getQuestionMaskedPreview() {
			return questionMaskedPreview;
		}

		public void setQuestionMaskedPreview(LocalizedTextData questionMaskedPreview) {
			this.questionMaskedPreview = questionMaskedPreview;
		}

		public LocalizedTextData getAnswerMaskedPreview() {
			return answerMaskedPreview;
		}

		public void setAnswerMaskedPreview(LocalizedTextData answerMaskedPreview) {
			this.answerMaskedPreview = answerMaskedPreview;
		}
	}

	public static class CandidateNominationSummaryData implements Serializable {
		private static final long serialVersionUID = 2082399967547153814L;

		private Long candidateId;
		private String candidateName;
		private Boolean publicCandidateLink;
		private String countryCode;
		private String candidateStatusCode;
		private Integer completedPublicTasks;
		private Integer totalPublicTasks;
		private String nominationStatusCode;

		public Long getCandidateId() {
			return candidateId;
		}

		public void setCandidateId(Long candidateId) {
			this.candidateId = candidateId;
		}

		public String getCandidateName() {
			return candidateName;
		}

		public void setCandidateName(String candidateName) {
			this.candidateName = candidateName;
		}

		public Boolean getPublicCandidateLink() {
			return publicCandidateLink;
		}

		public void setPublicCandidateLink(Boolean publicCandidateLink) {
			this.publicCandidateLink = publicCandidateLink;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}

		public String getCandidateStatusCode() {
			return candidateStatusCode;
		}

		public void setCandidateStatusCode(String candidateStatusCode) {
			this.candidateStatusCode = candidateStatusCode;
		}

		public Integer getCompletedPublicTasks() {
			return completedPublicTasks;
		}

		public void setCompletedPublicTasks(Integer completedPublicTasks) {
			this.completedPublicTasks = completedPublicTasks;
		}

		public Integer getTotalPublicTasks() {
			return totalPublicTasks;
		}

		public void setTotalPublicTasks(Integer totalPublicTasks) {
			this.totalPublicTasks = totalPublicTasks;
		}

		public String getNominationStatusCode() {
			return nominationStatusCode;
		}

		public void setNominationStatusCode(String nominationStatusCode) {
			this.nominationStatusCode = nominationStatusCode;
		}
	}
}
