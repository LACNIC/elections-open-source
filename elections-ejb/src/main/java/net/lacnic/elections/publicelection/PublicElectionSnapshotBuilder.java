package net.lacnic.elections.publicelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jakarta.persistence.EntityManager;
import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionAuditorResult;
import net.lacnic.elections.domain.ElectionLinkRecoveryMode;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidatePepDeclaration;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import net.lacnic.elections.domain.pre.CandidateQuestionStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateCountryLinkRow;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateWorkOrganizationRow;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCoreSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionOfficialResultSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionPhotoSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionRollSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionSnapshotMetadata;
import net.lacnic.elections.domain.services.publicelection.PublicElectionVoteCountRow;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.LinksUtils;
import net.lacnic.elections.utils.PublicNominationConfiguration;
import net.lacnic.elections.utils.VotingPeriodResolver;

public class PublicElectionSnapshotBuilder {

	private static final int REFRESH_INTERVAL_MINUTES = 5;
	private static final String REFRESH_STATUS_READY = "READY";
	private static final String DEFAULT_HIDDEN_CANDIDATE_NAME = "*******************";
	private static final String DEFAULT_EMPTY_REPRESENTATIVE = "********";
	private static final String DISPLAY_MODE_VISIBLE = "VISIBLE";
	private static final String DISPLAY_MODE_MASKED = "MASKED";
	private static final String DISPLAY_MODE_WAITING_REVIEW = "WAITING_REVIEW";
	private static final String DISPLAY_MODE_WAITING_CANDIDATE = "WAITING_CANDIDATE";
	private static final String DISPLAY_MODE_WAITING_LACNIC = "WAITING_LACNIC";
	private static final String PARAM_OTHER_STATUTORY_Q_PREFIX = "OTHER_STATUTORY_Q";
	private static final String PARAM_LABEL_SUFFIX = "_LABEL";
	private static final String PARAM_OTHER_NON_STATUTORY_Q1_LABEL = "OTHER_NON_STATUTORY_Q1_LABEL";

	private static final ElectionTaskKey[] LANDING_INCOMPLETE_TASKS = {
			ElectionTaskKey.PROFILE,
			ElectionTaskKey.COUNTRIES,
			ElectionTaskKey.INCOMPATIBILITIES,
			ElectionTaskKey.ORG_SUPPORTS,
			ElectionTaskKey.USER_SUPPORTS_2,
			ElectionTaskKey.USER_SUPPORTS_5,
			ElectionTaskKey.ORGANIZATIONS,
			ElectionTaskKey.COURSE,
			ElectionTaskKey.DECLARATIONS,
			ElectionTaskKey.DECLARATIONS_NON_STATUTORY,
			ElectionTaskKey.EVALUATION
	};

	private final EntityManager em;

	public PublicElectionSnapshotBuilder(EntityManager em) {
		this.em = em;
	}

	public PublicElectionSnapshotBundle build(long electionId) {
		if (electionId <= 0L) {
			return null;
		}

		Date generatedAt = new Date();
		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		if (election == null) {
			return null;
		}
		VotingPeriodResolver.applyVotingWindow(em, election);

		List<ElectionCalendar> calendars = ElectionsDaoFactory.createElectionCalendarDao(em).getElectionCalendars(electionId);
		Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey = indexCalendars(calendars);
		List<ElectionTask> electionTasks = ElectionsDaoFactory.createElectionTaskDao(em).getElectionTasks(electionId);
		Set<ElectionTaskKey> publicTaskKeys = resolvePublicTaskKeys(electionTasks);
		Parameter publicNominationParameter = ElectionsDaoFactory.createParameterDao(em).getParameter(Constants.PUBLIC_NOMINATION_ENABLED);
		String configuredValue = publicNominationParameter != null ? publicNominationParameter.getValue() : null;
		boolean publicNominationEnabled = PublicNominationConfiguration.isEnabled(configuredValue, election.getEffectiveElectionType());
		PublicElectionVisibilityResolver visibilityResolver = new PublicElectionVisibilityResolver(election, calendarsByKey, publicTaskKeys, generatedAt, publicNominationEnabled);

		List<Candidate> candidates = ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(electionId);
		Map<Long, Candidate> candidateById = indexCandidates(candidates);
		List<Candidate> publishedCandidates = resolvePublishedCandidates(candidates, election, generatedAt);
		List<Nomination> nominations = ElectionsDaoFactory.createNominationDao(em).getElectionNominationsForPublicElectionPage(electionId);
		Map<Long, Nomination> nominationByCandidateId = indexNominationsByCandidate(nominations);
		Map<Long, List<SupportNomination>> supportsByNominationId = indexSupportsByNomination(
				ElectionsDaoFactory.createSupportNominationDao(em).getElectionSupportNominationsForPublicElectionPage(electionId));
		Map<Long, List<PublicElectionCandidateCountryLinkRow>> countryLinksByCandidateId = indexCountryLinks(
				ElectionsDaoFactory.createPublicElectionPageDao(em).getElectionCandidateCountryLinkRowsForPublicElectionPage(electionId));
		Map<Long, List<PublicElectionCandidateWorkOrganizationRow>> workOrganizationsByCandidateId = indexWorkOrganizations(
				ElectionsDaoFactory.createPublicElectionPageDao(em).getElectionCandidateWorkOrganizationRowsForPublicElectionPage(electionId));

		List<CandidateQuestion> candidateQuestions = Collections.emptyList();
		if (visibilityResolver.isCandidateCommunityQuestionsVisible() || visibilityResolver.isCandidatePendingCommunityQuestionsVisible()) {
			candidateQuestions = ElectionsDaoFactory.createCandidateQuestionDao(em).getElectionCandidateQuestionsForPublicElectionPage(electionId);
		}
		Map<Long, List<CandidateQuestion>> questionsByCandidateId = indexQuestionsByCandidate(candidateQuestions);

		List<UserVoter> userVoters = Collections.emptyList();
		if (visibilityResolver.isLandingResultsVisible() || visibilityResolver.isLandingRollVisible()) {
			userVoters = ElectionsDaoFactory.createUserVoterDao(em).getElectionUserVoters(electionId);
		}

		List<PublicElectionVoteCountRow> voteCountRows = Collections.emptyList();
		if (visibilityResolver.isLandingResultsVisible()) {
			voteCountRows = ElectionsDaoFactory.createVoteDao(em).getElectionVoteCountRowsForPublicElectionPage(electionId);
		}
		ElectionAuditorResult electionAuditorResult = ElectionsDaoFactory.createElectionAuditorResultDao(em)
				.getElectionAuditorResult(electionId);

		Date nextBusinessTransitionUtc = resolveNextBusinessTransitionUtc(calendars, generatedAt);
		PublicElectionSnapshotMetadata metadata = buildMetadata(electionId, generatedAt, nextBusinessTransitionUtc);
		PublicElectionCoreSnapshot coreSnapshot = buildCoreSnapshot(
				metadata,
				election,
				calendars,
				calendarsByKey,
				visibilityResolver,
				publicTaskKeys,
				publishedCandidates,
				nominations,
				nominationByCandidateId,
				supportsByNominationId,
				countryLinksByCandidateId,
				workOrganizationsByCandidateId,
				questionsByCandidateId,
				userVoters,
				voteCountRows,
				candidateById,
				electionAuditorResult,
				generatedAt);
		PublicElectionRollSnapshot rollSnapshot = buildRollSnapshot(metadata, electionId, visibilityResolver, userVoters);
		PublicElectionPhotoSnapshot photoSnapshot = buildPhotoSnapshot(metadata, electionId, visibilityResolver, publishedCandidates);
		PublicElectionOfficialResultSnapshot officialResultSnapshot =
				buildOfficialResultSnapshot(metadata, electionId, electionAuditorResult);
		return new PublicElectionSnapshotBundle(coreSnapshot, rollSnapshot, photoSnapshot, officialResultSnapshot);
	}

	private PublicElectionCoreSnapshot buildCoreSnapshot(
			PublicElectionSnapshotMetadata metadata,
			Election election,
			List<ElectionCalendar> calendars,
			Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey,
			PublicElectionVisibilityResolver visibilityResolver,
			Set<ElectionTaskKey> publicTaskKeys,
			List<Candidate> publishedCandidates,
			List<Nomination> nominations,
			Map<Long, Nomination> nominationByCandidateId,
			Map<Long, List<SupportNomination>> supportsByNominationId,
			Map<Long, List<PublicElectionCandidateCountryLinkRow>> countryLinksByCandidateId,
			Map<Long, List<PublicElectionCandidateWorkOrganizationRow>> workOrganizationsByCandidateId,
			Map<Long, List<CandidateQuestion>> questionsByCandidateId,
			List<UserVoter> userVoters,
			List<PublicElectionVoteCountRow> voteCountRows,
			Map<Long, Candidate> candidateById,
			ElectionAuditorResult electionAuditorResult,
			Date generatedAt) {
		PublicElectionCoreSnapshot snapshot = new PublicElectionCoreSnapshot();
		snapshot.setMetadata(cloneMetadata(metadata));
		snapshot.setElection(buildElectionData(election, visibilityResolver));
		snapshot.getElection().setPublishedAbstentionCandidate(Boolean.valueOf(hasPublishedAbstentionCandidate(candidateById.values())));
		snapshot.setVisibility(visibilityResolver.resolve());
		snapshot.setOfficialResult(buildOfficialResultData(election.getElectionId(), electionAuditorResult));

		if (visibilityResolver.isLandingCalendarVisible()) {
			for (ElectionCalendar calendar : safeList(calendars)) {
				if (calendar == null || calendar.getCalendarKey() == null || !calendar.isPublicable()) {
					continue;
				}
				snapshot.getPublicCalendarEvents().add(buildCalendarEventData(calendar));
			}
			Collections.sort(snapshot.getPublicCalendarEvents(), new Comparator<PublicElectionCoreSnapshot.CalendarEventData>() {
				@Override
				public int compare(PublicElectionCoreSnapshot.CalendarEventData a, PublicElectionCoreSnapshot.CalendarEventData b) {
					Date aStart = a != null ? a.getStartUtc() : null;
					Date bStart = b != null ? b.getStartUtc() : null;
					if (aStart == null && bStart == null) {
						return 0;
					}
					if (aStart == null) {
						return 1;
					}
					if (bStart == null) {
						return -1;
					}
					return aStart.compareTo(bStart);
				}
			});
		}

		if (visibilityResolver.isLandingResultsVisible()) {
			snapshot.setResultSummary(buildResultSummary(voteCountRows, buildResultCandidates(publishedCandidates, candidateById.values()), userVoters, calendarsByKey));
			snapshot.getResultPublicationStages().addAll(buildResultPublicationStages(calendarsByKey, generatedAt));
		}

		if (visibilityResolver.isLandingRollVisible()) {
			snapshot.setRollSummary(buildRollSummary(userVoters));
		}

		snapshot.setRecovery(buildRecoveryData(election, visibilityResolver));

		if (visibilityResolver.isCandidateStatutoryQuestionsVisible() || visibilityResolver.isCandidateNonStatutoryQuestionsVisible()) {
			snapshot.setQuestionCatalog(buildQuestionCatalog(visibilityResolver));
		}

		if (visibilityResolver.isLandingCandidatesVisible() || visibilityResolver.isCandidateProfileRootVisible()) {
			for (Candidate candidate : safeList(publishedCandidates)) {
				if (candidate == null) {
					continue;
				}
				Nomination nomination = nominationByCandidateId.get(candidate.getCandidateId());
				List<SupportNomination> supports = nomination != null ? supportsByNominationId.get(nomination.getId()) : Collections.<SupportNomination>emptyList();
				List<CandidateQuestion> candidateQuestions = questionsByCandidateId.get(candidate.getCandidateId());
				snapshot.getCandidates().add(buildCandidateData(
						candidate,
						nomination,
						supports,
						countryLinksByCandidateId.get(candidate.getCandidateId()),
						workOrganizationsByCandidateId.get(candidate.getCandidateId()),
						candidateQuestions,
						calendarsByKey,
						visibilityResolver));
			}
		}

		if (visibilityResolver.isCandidateNominationSummaryVisible()) {
			snapshot.getCandidateNominationSummaries().addAll(buildCandidateNominationSummaries(
					nominations,
					publicTaskKeys,
					countryLinksByCandidateId));
		}

		return snapshot;
	}

	private PublicElectionCoreSnapshot.ElectionData buildElectionData(Election election) {
		return buildElectionData(election, null);
	}

	private PublicElectionCoreSnapshot.ElectionData buildElectionData(Election election, PublicElectionVisibilityResolver visibilityResolver) {
		PublicElectionCoreSnapshot.ElectionData data = new PublicElectionCoreSnapshot.ElectionData();
		data.setElectionId(election.getElectionId());
		data.setCategoryCode(election.getCategory() != null ? election.getCategory().name() : null);
		data.setElectionTypeCode(election.getElectionType() != null ? election.getElectionType().name() : null);
		data.setTitleSpanish(election.getTitleSpanish());
		data.setTitleEnglish(election.getTitleEnglish());
		data.setTitlePortuguese(election.getTitlePortuguese());
		data.setDescriptionSpanish(election.getDescriptionSpanish());
		data.setDescriptionEnglish(election.getDescriptionEnglish());
		data.setDescriptionPortuguese(election.getDescriptionPortuguese());
		data.setCall(localizedText(election.getCallSpanish(), election.getCallEnglish(), election.getCallPortuguese()));
		data.setOnlySpanish(election.isOnlySp());
		data.setRandomOrderCandidates(election.isRandomOrderCandidates());
		data.setResultLinkAvailable(election.isResultLinkAvailable());
		data.setPublicElectionLinkAvailable(election.isPublicElectionLinkAvailable());
		data.setClosed(election.isClosed());
		data.setPublicLinkRecoveryModeCode(election.getPublicLinkRecoveryMode() != null ? election.getPublicLinkRecoveryMode().name() : ElectionLinkRecoveryMode.NONE.name());
		data.setVotingPeriodStartUtc(election.getVotingPeriodStartDate());
		data.setVotingPeriodEndUtc(election.getVotingPeriodEndDate());
		data.setResultContactEmail(resolveResultContactEmail(election));
		if (visibilityResolver != null && visibilityResolver.isPublicNominationVisible()) {
			data.setPublicNominationUrl(trimToNull(LinksUtils.buildPublicCandidateNominationLink(election.getPublicElectionToken())));
		}
		return data;
	}

	private PublicElectionCoreSnapshot.OfficialResultData buildOfficialResultData(long electionId, ElectionAuditorResult electionAuditorResult) {
		if (electionAuditorResult == null) {
			return null;
		}
		PublicElectionCoreSnapshot.OfficialResultData data = new PublicElectionCoreSnapshot.OfficialResultData();
		data.setResultSpanish(trimToNull(electionAuditorResult.getResultSpanish()));
		data.setResultEnglish(trimToNull(electionAuditorResult.getResultEnglish()));
		data.setResultPortuguese(trimToNull(electionAuditorResult.getResultPortuguese()));
		if (hasBinary(electionAuditorResult.getResultLetterSpanish())) {
			data.setResultLetterSpanishUrl(LinksUtils.buildPublicElectionOfficialResultLetterLink(Long.valueOf(electionId), LanguageCode.SP));
		}
		if (hasBinary(electionAuditorResult.getResultLetterEnglish())) {
			data.setResultLetterEnglishUrl(LinksUtils.buildPublicElectionOfficialResultLetterLink(Long.valueOf(electionId), LanguageCode.EN));
		}
		if (hasBinary(electionAuditorResult.getResultLetterPortuguese())) {
			data.setResultLetterPortugueseUrl(LinksUtils.buildPublicElectionOfficialResultLetterLink(Long.valueOf(electionId), LanguageCode.PT));
		}
		return hasOfficialResultData(data) ? data : null;
	}

	private boolean hasOfficialResultData(PublicElectionCoreSnapshot.OfficialResultData data) {
		return data != null
				&& (hasText(data.getResultSpanish())
						|| hasText(data.getResultEnglish())
						|| hasText(data.getResultPortuguese())
						|| hasText(data.getResultLetterSpanishUrl())
						|| hasText(data.getResultLetterEnglishUrl())
						|| hasText(data.getResultLetterPortugueseUrl()));
	}

	private PublicElectionCoreSnapshot.CalendarEventData buildCalendarEventData(ElectionCalendar calendar) {
		PublicElectionCoreSnapshot.CalendarEventData data = new PublicElectionCoreSnapshot.CalendarEventData();
		data.setKeyCode(calendar.getCalendarKey() != null ? calendar.getCalendarKey().name() : null);
		data.setStartUtc(calendar.getStartDate());
		data.setEndUtc(resolveTimelineEndDate(calendar));
		data.setSingleEvent(Boolean.valueOf(isSingleCalendarKey(calendar.getCalendarKey())));
		return data;
	}

	private PublicElectionCoreSnapshot.ResultSummaryData buildResultSummary(
			List<PublicElectionVoteCountRow> voteCountRows,
			List<Candidate> resultCandidates,
			List<UserVoter> userVoters,
			Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey) {
		Map<Long, Long> votesByCandidateId = new LinkedHashMap<>();
		long totalVotes = 0L;
		for (PublicElectionVoteCountRow row : safeList(voteCountRows)) {
			if (row == null || row.getCandidateId() <= 0L || row.getVoteCount() <= 0L) {
				continue;
			}
			votesByCandidateId.put(row.getCandidateId(), row.getVoteCount());
			totalVotes += row.getVoteCount();
		}

		PublicElectionCoreSnapshot.ResultSummaryData data = new PublicElectionCoreSnapshot.ResultSummaryData();
		data.setPublicationStageCode(resolveResultPublicationStage(calendarsByKey));
		data.setTotalVotes(Long.valueOf(totalVotes));
		data.setEnabledVoters(Long.valueOf(userVoters.size()));
		data.setOrganizationsVoted(Long.valueOf(calculateOrganizationsVoted(userVoters)));
		data.setParticipationPercentage(Double.valueOf(calculateParticipationPercentage(userVoters)));
		for (Candidate candidate : safeList(resultCandidates)) {
			if (candidate == null || candidate.getCandidateId() <= 0L) {
				continue;
			}
			PublicElectionCoreSnapshot.ResultRowData rowData = new PublicElectionCoreSnapshot.ResultRowData();
			long candidateId = candidate.getCandidateId();
			long voteCount = votesByCandidateId.containsKey(candidateId) ? votesByCandidateId.get(candidateId).longValue() : 0L;
			rowData.setCandidateId(Long.valueOf(candidateId));
			rowData.setCandidateName(candidate.getName() != null ? candidate.getName() : "Candidate " + candidateId);
			rowData.setVoteCount(Long.valueOf(voteCount));
			rowData.setPercentage(Double.valueOf(totalVotes > 0L ? (100.0d * voteCount) / (double) totalVotes : 0.0d));
			rowData.setWinner(Boolean.valueOf(candidate.isWinner()));
			data.getRows().add(rowData);
		}
		Collections.sort(data.getRows(), new Comparator<PublicElectionCoreSnapshot.ResultRowData>() {
			@Override
			public int compare(PublicElectionCoreSnapshot.ResultRowData a, PublicElectionCoreSnapshot.ResultRowData b) {
				int votesCompare = Long.compare(b.getVoteCount() != null ? b.getVoteCount().longValue() : 0L, a.getVoteCount() != null ? a.getVoteCount().longValue() : 0L);
				if (votesCompare != 0) {
					return votesCompare;
				}
				long aCandidateId = a.getCandidateId() != null ? a.getCandidateId().longValue() : 0L;
				long bCandidateId = b.getCandidateId() != null ? b.getCandidateId().longValue() : 0L;
				return Long.compare(aCandidateId, bCandidateId);
			}
		});
		return data;
	}

	private List<Candidate> buildResultCandidates(List<Candidate> publishedCandidates, Iterable<Candidate> allCandidates) {
		Map<Long, Candidate> resultCandidateById = new LinkedHashMap<>();
		for (Candidate candidate : safeList(publishedCandidates)) {
			if (candidate != null && candidate.getCandidateId() > 0L) {
				resultCandidateById.put(Long.valueOf(candidate.getCandidateId()), candidate);
			}
		}
		if (allCandidates != null) {
			for (Candidate candidate : allCandidates) {
				if (candidate != null
						&& candidate.getCandidateId() > 0L
						&& candidate.isAbstention()
						&& candidate.getStatus() == CandidateStatus.CONFIRMED_AND_PUBLISHED) {
					resultCandidateById.put(Long.valueOf(candidate.getCandidateId()), candidate);
				}
			}
		}
		return new ArrayList<>(resultCandidateById.values());
	}

	private List<PublicElectionCoreSnapshot.ResultPublicationStageData> buildResultPublicationStages(
			Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey,
			Date now) {
		String currentStage = resolveResultPublicationStage(calendarsByKey);
		List<PublicElectionCoreSnapshot.ResultPublicationStageData> rows = new ArrayList<>();
		rows.add(buildResultStageData(PublicElectionStageCodes.PROVISIONAL, currentStage, calendarsByKey.get(ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED)));
		rows.add(buildRangeStageData(PublicElectionStageCodes.CE_AUDIT, currentStage, calendarsByKey.get(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT)));
		rows.add(buildVoterAuditStageData(currentStage, calendarsByKey));
		rows.add(buildResultStageData(PublicElectionStageCodes.OFFICIAL_NO_CLAIMS, currentStage, calendarsByKey.get(ElectionCalendarKey.N_20_SINGLE_OFFICIAL_RESULTS_NO_CLAIMS_PUBLISHED)));
		ElectionCalendar officialWithClaims = calendarsByKey.get(ElectionCalendarKey.N_22_SINGLE_OFFICIAL_RESULTS_WITH_CLAIMS_PUBLISHED);
		if (officialWithClaims != null && officialWithClaims.getStartDate() != null) {
			rows.add(buildResultStageData(PublicElectionStageCodes.OFFICIAL_WITH_CLAIMS, currentStage, officialWithClaims));
		}
		for (PublicElectionCoreSnapshot.ResultPublicationStageData row : rows) {
			if (row.getCurrent() == null) {
				row.setCurrent(Boolean.FALSE);
			}
			if (row.getConfigured() == null) {
				row.setConfigured(Boolean.valueOf(row.getStartUtc() != null));
			}
		}
		return rows;
	}

	private PublicElectionCoreSnapshot.ResultPublicationStageData buildResultStageData(
			String stageCode,
			String currentStage,
			ElectionCalendar calendar) {
		PublicElectionCoreSnapshot.ResultPublicationStageData data = new PublicElectionCoreSnapshot.ResultPublicationStageData();
		data.setStageCode(stageCode);
		if (calendar != null) {
			data.setStartUtc(calendar.getStartDate());
			data.setEndUtc(resolveEffectiveCalendarEnd(calendar));
		}
		data.setCurrent(Boolean.valueOf(stageCode.equals(currentStage)));
		data.setConfigured(Boolean.valueOf(calendar != null && calendar.getStartDate() != null));
		return data;
	}

	private PublicElectionCoreSnapshot.ResultPublicationStageData buildRangeStageData(
			String stageCode,
			String currentStage,
			ElectionCalendar calendar) {
		PublicElectionCoreSnapshot.ResultPublicationStageData data = buildResultStageData(stageCode, currentStage, calendar);
		if (calendar != null) {
			data.setEndUtc(calendar.getEndDate() != null ? calendar.getEndDate() : calendar.getStartDate());
		}
		return data;
	}

	private PublicElectionCoreSnapshot.ResultPublicationStageData buildVoterAuditStageData(
			String currentStage,
			Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey) {
		PublicElectionCoreSnapshot.ResultPublicationStageData data = new PublicElectionCoreSnapshot.ResultPublicationStageData();
		data.setStageCode(PublicElectionStageCodes.VOTER_AUDIT);
		data.setStartUtc(firstNonNullCalendarStart(calendarsByKey,
				ElectionCalendarKey.N_19_PERIODO_VOTER_AUDIT,
				ElectionCalendarKey.N_21_PERIODO_VOTER_CLAIMS_RESPONSE));
		data.setEndUtc(firstNonNullCalendarEnd(calendarsByKey,
				ElectionCalendarKey.N_21_PERIODO_VOTER_CLAIMS_RESPONSE,
				ElectionCalendarKey.N_19_PERIODO_VOTER_AUDIT));
		data.setCurrent(Boolean.valueOf(PublicElectionStageCodes.VOTER_AUDIT.equals(currentStage)));
		data.setConfigured(Boolean.valueOf(data.getStartUtc() != null));
		return data;
	}

	private PublicElectionCoreSnapshot.RollSummaryData buildRollSummary(List<UserVoter> userVoters) {
		PublicElectionCoreSnapshot.RollSummaryData data = new PublicElectionCoreSnapshot.RollSummaryData();
		data.setTotalRows(Long.valueOf(userVoters.size()));
		data.setTotalOrganizations(Long.valueOf(countDistinctOrganizations(userVoters)));
		data.setTotalCountries(Long.valueOf(countDistinctCountries(userVoters)));
		data.setSpanishVoters(Long.valueOf(countVotersByLanguage(userVoters, LanguageCode.SP)));
		data.setEnglishVoters(Long.valueOf(countVotersByLanguage(userVoters, LanguageCode.EN)));
		data.setPortugueseVoters(Long.valueOf(countVotersByLanguage(userVoters, LanguageCode.PT)));
		return data;
	}

	private PublicElectionCoreSnapshot.RecoveryData buildRecoveryData(Election election, PublicElectionVisibilityResolver visibilityResolver) {
		PublicElectionCoreSnapshot.RecoveryData data = new PublicElectionCoreSnapshot.RecoveryData();
		data.setVisible(Boolean.valueOf(visibilityResolver.isParticipationLinkRecoveryVisible()));
		data.setModeCode(election.getPublicLinkRecoveryMode() != null ? election.getPublicLinkRecoveryMode().name() : ElectionLinkRecoveryMode.NONE.name());
		return data;
	}

	private PublicElectionCoreSnapshot.QuestionCatalogData buildQuestionCatalog(PublicElectionVisibilityResolver visibilityResolver) {
		PublicElectionCoreSnapshot.QuestionCatalogData data = new PublicElectionCoreSnapshot.QuestionCatalogData();
		if (visibilityResolver.isCandidateStatutoryQuestionsVisible()) {
			for (int i = 1; i <= 4; i++) {
				data.getOtherStatutoryQuestions().add(resolveOtherStatutoryQuestion(i));
			}
		}
		if (visibilityResolver.isCandidateNonStatutoryQuestionsVisible()) {
			data.getOtherNonStatutoryQuestions().add(resolveOtherNonStatutoryQuestion());
		}
		return data;
	}

	private PublicElectionCoreSnapshot.CandidateData buildCandidateData(
			Candidate candidate,
			Nomination nomination,
			List<SupportNomination> supports,
			List<PublicElectionCandidateCountryLinkRow> countryLinks,
			List<PublicElectionCandidateWorkOrganizationRow> workOrganizations,
			List<CandidateQuestion> candidateQuestions,
			Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey,
			PublicElectionVisibilityResolver visibilityResolver) {
		PublicElectionCoreSnapshot.CandidateData data = new PublicElectionCoreSnapshot.CandidateData();
		data.setCandidateId(Long.valueOf(candidate.getCandidateId()));
		data.setName(candidate.getName());
		data.setCandidateOrder(Integer.valueOf(candidate.getCandidateOrder()));
		data.setStatusCode(candidate.getStatus() != null ? candidate.getStatus().name() : null);
		data.setWinner(Boolean.valueOf(candidate.isWinner()));
		data.setPublicCandidate(Boolean.valueOf(isPublicCandidate(candidate)));
		data.setPrimaryCountryCode(resolvePrimaryCountryCode(countryLinks));
		data.getOtherCountryCodes().addAll(resolveOtherCountryCodes(countryLinks));
		data.getWorkOrganizationNames().addAll(resolveWorkOrganizationNames(workOrganizations));
		data.getSupportOrganizationNames().addAll(resolveSupportOrganizationNames(supports));
		data.setNominationOrganizationName(nomination != null && nomination.getOrganization() != null ? nomination.getOrganization().getName() : null);
		data.setNominationReason(localizedText(
				nomination != null ? nomination.getNominationReasonSpanish() : null,
				nomination != null ? nomination.getNominationReasonEnglish() : null,
				nomination != null ? nomination.getNominationReasonPortuguese() : null));
		data.setLinkedinUrl(candidate.getLinkedinUrl());
		data.setBio(localizedText(candidate.getBioSpanish(), candidate.getBioEnglish(), candidate.getBioPortuguese()));
		if (visibilityResolver.isCandidateStatutoryQuestionsVisible()) {
			data.getStatutoryAnswers().addAll(buildStatutoryAnswers(candidate, calendarsByKey.get(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED)));
		}
		if (visibilityResolver.isCandidateNonStatutoryQuestionsVisible()) {
			data.getNonStatutoryAnswers().addAll(buildNonStatutoryAnswers(candidate, calendarsByKey.get(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED)));
		}
		if (visibilityResolver.isCandidateCommunityQuestionsVisible() || visibilityResolver.isCandidatePendingCommunityQuestionsVisible()) {
			data.getCommunityQuestions().addAll(buildCommunityQuestions(candidateQuestions));
		}
		return data;
	}

	private List<PublicElectionCoreSnapshot.QuestionAnswerData> buildStatutoryAnswers(Candidate candidate, ElectionCalendar publicationCalendar) {
		List<PublicElectionCoreSnapshot.QuestionAnswerData> rows = new ArrayList<>();
		Date questionDate = publicationCalendar != null && publicationCalendar.getStartDate() != null ? publicationCalendar.getStartDate() : null;
		addQuestionAnswer(rows, resolveOtherStatutoryQuestion(1), localizedText(
				candidate.getQOtherStatutoryAnswer1Spanish(),
				candidate.getQOtherStatutoryAnswer1English(),
				candidate.getQOtherStatutoryAnswer1Portuguese()), questionDate);
		addQuestionAnswer(rows, resolveOtherStatutoryQuestion(2), localizedText(
				candidate.getQOtherStatutoryAnswer2Spanish(),
				candidate.getQOtherStatutoryAnswer2English(),
				candidate.getQOtherStatutoryAnswer2Portuguese()), questionDate);
		addQuestionAnswer(rows, resolveOtherStatutoryQuestion(3), localizedText(
				candidate.getQOtherStatutoryAnswer3Spanish(),
				candidate.getQOtherStatutoryAnswer3English(),
				candidate.getQOtherStatutoryAnswer3Portuguese()), questionDate);
		addQuestionAnswer(rows, resolveOtherStatutoryQuestion(4), localizedText(
				candidate.getQOtherStatutoryAnswer4Spanish(),
				candidate.getQOtherStatutoryAnswer4English(),
				candidate.getQOtherStatutoryAnswer4Portuguese()), questionDate);
		return rows;
	}

	private List<PublicElectionCoreSnapshot.QuestionAnswerData> buildNonStatutoryAnswers(Candidate candidate, ElectionCalendar publicationCalendar) {
		List<PublicElectionCoreSnapshot.QuestionAnswerData> rows = new ArrayList<>();
		addQuestionAnswer(rows, resolveOtherNonStatutoryQuestion(), localizedText(
				candidate.getQOtherNonStatutoryAnswer1Spanish(),
				candidate.getQOtherNonStatutoryAnswer1English(),
				candidate.getQOtherNonStatutoryAnswer1Portuguese()),
				publicationCalendar != null ? publicationCalendar.getStartDate() : null);
		return rows;
	}

	private void addQuestionAnswer(
			List<PublicElectionCoreSnapshot.QuestionAnswerData> rows,
			PublicElectionCoreSnapshot.LocalizedTextData question,
			PublicElectionCoreSnapshot.LocalizedTextData answer,
			Date questionDate) {
		if (!hasAnyText(answer)) {
			return;
		}
		PublicElectionCoreSnapshot.QuestionAnswerData row = new PublicElectionCoreSnapshot.QuestionAnswerData();
		row.setQuestionText(question);
		row.setAnswerText(answer);
		row.setDateUtc(questionDate);
		rows.add(row);
	}

	private List<PublicElectionCoreSnapshot.CommunityQuestionData> buildCommunityQuestions(List<CandidateQuestion> questions) {
		List<PublicElectionCoreSnapshot.CommunityQuestionData> rows = new ArrayList<>();
		for (CandidateQuestion question : safeList(questions)) {
			if (question == null || question.getStatus() == null) {
				continue;
			}
			if (question.getStatus() == CandidateQuestionStatus.PUBLISHED) {
				PublicElectionCoreSnapshot.CommunityQuestionData row = buildPublishedCommunityQuestion(question);
				if (row != null) {
					rows.add(row);
				}
				continue;
			}
			if (isPendingCommunityQuestionStatus(question.getStatus())) {
				rows.add(buildPendingCommunityQuestion(question));
			}
		}
		Collections.sort(rows, new Comparator<PublicElectionCoreSnapshot.CommunityQuestionData>() {
			@Override
			public int compare(PublicElectionCoreSnapshot.CommunityQuestionData a, PublicElectionCoreSnapshot.CommunityQuestionData b) {
				long aId = a.getCandidateQuestionId() != null ? a.getCandidateQuestionId().longValue() : 0L;
				long bId = b.getCandidateQuestionId() != null ? b.getCandidateQuestionId().longValue() : 0L;
				return Long.compare(bId, aId);
			}
		});
		return rows;
	}

	private PublicElectionCoreSnapshot.CommunityQuestionData buildPublishedCommunityQuestion(CandidateQuestion question) {
		PublicElectionCoreSnapshot.LocalizedTextData questionText = localizedText(
				question.getQuestionSpanish(),
				question.getQuestionEnglish(),
				question.getQuestionPortuguese());
		PublicElectionCoreSnapshot.LocalizedTextData answerText = localizedText(
				question.getAnswerSpanish(),
				question.getAnswerEnglish(),
				question.getAnswerPortuguese());
		if (!hasAnyText(questionText) || !hasAnyText(answerText)) {
			return null;
		}
		PublicElectionCoreSnapshot.CommunityQuestionData row = new PublicElectionCoreSnapshot.CommunityQuestionData();
		row.setCandidateQuestionId(Long.valueOf(question.getCandidateQuestionId()));
		row.setStatusCode(question.getStatus().name());
		row.setAskedByInitials(resolveAskedByInitials(question.getAskedByName()));
		row.setQuestionDateUtc(question.getCreationDate());
		row.setAnswerDateUtc(question.getPublishedDate() != null ? question.getPublishedDate() : question.getUpdateDate());
		row.setQuestionDisplayMode(DISPLAY_MODE_VISIBLE);
		row.setAnswerDisplayMode(DISPLAY_MODE_VISIBLE);
		row.setQuestionText(questionText);
		row.setAnswerText(answerText);
		return row;
	}

	private PublicElectionCoreSnapshot.CommunityQuestionData buildPendingCommunityQuestion(CandidateQuestion question) {
		PublicElectionCoreSnapshot.CommunityQuestionData row = new PublicElectionCoreSnapshot.CommunityQuestionData();
		row.setCandidateQuestionId(Long.valueOf(question.getCandidateQuestionId()));
		row.setStatusCode(question.getStatus().name());
		row.setAskedByInitials(resolveAskedByInitials(question.getAskedByName()));
		row.setQuestionDateUtc(question.getCreationDate());
		row.setAnswerDateUtc(hasAnyText(localizedText(question.getAnswerSpanish(), question.getAnswerEnglish(), question.getAnswerPortuguese()))
				? question.getUpdateDate()
				: null);

		PublicElectionCoreSnapshot.LocalizedTextData questionText = localizedText(
				question.getQuestionSpanish(),
				question.getQuestionEnglish(),
				question.getQuestionPortuguese());
		PublicElectionCoreSnapshot.LocalizedTextData answerText = localizedText(
				question.getAnswerSpanish(),
				question.getAnswerEnglish(),
				question.getAnswerPortuguese());

		switch (question.getStatus()) {
		case QUESTION_RECEIVED_LACNIC:
			row.setQuestionDisplayMode(DISPLAY_MODE_MASKED);
			row.setQuestionMaskedPreview(maskPendingText(questionText));
			row.setAnswerDisplayMode(DISPLAY_MODE_WAITING_LACNIC);
			break;
		case QUESTION_READY_FOR_CANDIDATE:
			row.setQuestionDisplayMode(DISPLAY_MODE_VISIBLE);
			row.setQuestionText(questionText);
			row.setAnswerDisplayMode(DISPLAY_MODE_WAITING_CANDIDATE);
			break;
		case ANSWER_SUBMITTED_BY_CANDIDATE:
			row.setQuestionDisplayMode(DISPLAY_MODE_VISIBLE);
			row.setQuestionText(questionText);
			row.setAnswerDisplayMode(DISPLAY_MODE_MASKED);
			row.setAnswerMaskedPreview(maskPendingText(answerText));
			break;
		default:
			row.setQuestionDisplayMode(DISPLAY_MODE_WAITING_REVIEW);
			row.setAnswerDisplayMode(DISPLAY_MODE_WAITING_REVIEW);
			break;
		}
		return row;
	}

	private List<PublicElectionCoreSnapshot.CandidateNominationSummaryData> buildCandidateNominationSummaries(
			List<Nomination> nominations,
			Set<ElectionTaskKey> publicTaskKeys,
			Map<Long, List<PublicElectionCandidateCountryLinkRow>> countryLinksByCandidateId) {
		List<PublicElectionCoreSnapshot.CandidateNominationSummaryData> rows = new ArrayList<>();
		int enabledSummaryTasks = countEnabledSummaryTasks(publicTaskKeys);
		for (Nomination nomination : safeList(nominations)) {
			if (nomination == null) {
				continue;
			}
			Candidate candidate = nomination.getCandidate();
			CandidateStatus candidateStatus = candidate != null ? candidate.getStatus() : null;
			boolean publicCandidate = isPublicCandidate(candidate);
			PublicElectionCoreSnapshot.CandidateNominationSummaryData row = new PublicElectionCoreSnapshot.CandidateNominationSummaryData();
			row.setCandidateId(Long.valueOf(candidate != null ? candidate.getCandidateId() : 0L));
			row.setCandidateName(publicCandidate && candidate != null ? candidate.getName() : DEFAULT_HIDDEN_CANDIDATE_NAME);
			row.setPublicCandidateLink(Boolean.valueOf(publicCandidate));
			row.setCountryCode(resolveNominationCountryCode(nomination, candidate, countryLinksByCandidateId));
			row.setCandidateStatusCode(candidateStatus != null ? candidateStatus.name() : null);
			row.setCompletedPublicTasks(Integer.valueOf(resolveCompletedPublicSummaryTasks(candidate, publicTaskKeys, enabledSummaryTasks)));
			row.setTotalPublicTasks(Integer.valueOf(enabledSummaryTasks));
			row.setNominationStatusCode(nomination.getStatus() != null ? nomination.getStatus().name() : null);
			rows.add(row);
		}
		Collections.sort(rows, new Comparator<PublicElectionCoreSnapshot.CandidateNominationSummaryData>() {
			@Override
			public int compare(
					PublicElectionCoreSnapshot.CandidateNominationSummaryData a,
					PublicElectionCoreSnapshot.CandidateNominationSummaryData b) {
				int rankCompare = Integer.compare(
						resolveNominationSortRank(a.getCandidateStatusCode(), a.getNominationStatusCode()),
						resolveNominationSortRank(b.getCandidateStatusCode(), b.getNominationStatusCode()));
				if (rankCompare != 0) {
					return rankCompare;
				}
				String aName = a.getCandidateName() != null ? a.getCandidateName() : "";
				String bName = b.getCandidateName() != null ? b.getCandidateName() : "";
				int nameCompare = aName.compareToIgnoreCase(bName);
				if (nameCompare != 0) {
					return nameCompare;
				}
				long aId = a.getCandidateId() != null ? a.getCandidateId().longValue() : 0L;
				long bId = b.getCandidateId() != null ? b.getCandidateId().longValue() : 0L;
				return Long.compare(aId, bId);
			}
		});
		return rows;
	}

	private PublicElectionRollSnapshot buildRollSnapshot(
			PublicElectionSnapshotMetadata metadata,
			long electionId,
			PublicElectionVisibilityResolver visibilityResolver,
			List<UserVoter> userVoters) {
		PublicElectionRollSnapshot snapshot = new PublicElectionRollSnapshot();
		snapshot.setMetadata(cloneMetadata(metadata));
		snapshot.setElectionId(Long.valueOf(electionId));
		if (!visibilityResolver.isLandingRollVisible()) {
			return snapshot;
		}
		List<PublicElectionRollSnapshot.RollEntryData> rows = new ArrayList<>();
		for (UserVoter userVoter : safeList(userVoters)) {
			if (userVoter == null) {
				continue;
			}
			PublicElectionRollSnapshot.RollEntryData row = new PublicElectionRollSnapshot.RollEntryData();
			row.setCountryCode(normalizeCountry(userVoter.getCountry()));
			row.setOrganizationName(resolveOrganizationName(userVoter));
			row.setRepresentativeMask(maskRepresentative(userVoter.getName()));
			rows.add(row);
		}
		Collections.sort(rows, new Comparator<PublicElectionRollSnapshot.RollEntryData>() {
			@Override
			public int compare(PublicElectionRollSnapshot.RollEntryData a, PublicElectionRollSnapshot.RollEntryData b) {
				int orgCompare = safeString(a.getOrganizationName()).compareToIgnoreCase(safeString(b.getOrganizationName()));
				if (orgCompare != 0) {
					return orgCompare;
				}
				return safeString(a.getCountryCode()).compareToIgnoreCase(safeString(b.getCountryCode()));
			}
		});
		snapshot.getRows().addAll(rows);
		return snapshot;
	}

	private PublicElectionPhotoSnapshot buildPhotoSnapshot(
			PublicElectionSnapshotMetadata metadata,
			long electionId,
			PublicElectionVisibilityResolver visibilityResolver,
			List<Candidate> publishedCandidates) {
		PublicElectionPhotoSnapshot snapshot = new PublicElectionPhotoSnapshot();
		snapshot.setMetadata(cloneMetadata(metadata));
		snapshot.setElectionId(Long.valueOf(electionId));
		if (!visibilityResolver.isLandingCandidatesVisible() && !visibilityResolver.isCandidateProfileRootVisible()) {
			return snapshot;
		}
		for (Candidate candidate : safeList(publishedCandidates)) {
			if (candidate == null || candidate.getPictureInfo() == null || candidate.getPictureInfo().length == 0) {
				continue;
			}
			PublicElectionPhotoSnapshot.CandidatePhotoData photoData = new PublicElectionPhotoSnapshot.CandidatePhotoData();
			photoData.setCandidateId(Long.valueOf(candidate.getCandidateId()));
			photoData.setPictureUrl(
					LinksUtils.buildPublicCandidatePhotoLink(Long.valueOf(electionId), Long.valueOf(candidate.getCandidateId())));
			photoData.setPictureBytes(candidate.getPictureInfo());
			photoData.setPictureName(candidate.getPictureName());
			photoData.setPictureExtension(candidate.getPictureExtension());
			snapshot.getCandidates().add(photoData);
		}
		return snapshot;
	}

	private PublicElectionOfficialResultSnapshot buildOfficialResultSnapshot(
			PublicElectionSnapshotMetadata metadata,
			long electionId,
			ElectionAuditorResult electionAuditorResult) {
		PublicElectionOfficialResultSnapshot snapshot = new PublicElectionOfficialResultSnapshot();
		snapshot.setMetadata(cloneMetadata(metadata));
		snapshot.setElectionId(Long.valueOf(electionId));
		if (electionAuditorResult == null) {
			return snapshot;
		}
		snapshot.setResult(localizedText(
				electionAuditorResult.getResultSpanish(),
				electionAuditorResult.getResultEnglish(),
				electionAuditorResult.getResultPortuguese()));
		snapshot.setSpanishLetter(buildOfficialResultLetterData(electionId, LanguageCode.SP, electionAuditorResult.getResultLetterSpanish()));
		snapshot.setEnglishLetter(buildOfficialResultLetterData(electionId, LanguageCode.EN, electionAuditorResult.getResultLetterEnglish()));
		snapshot.setPortugueseLetter(buildOfficialResultLetterData(electionId, LanguageCode.PT, electionAuditorResult.getResultLetterPortuguese()));
		return snapshot;
	}

	private PublicElectionOfficialResultSnapshot.OfficialResultLetterData buildOfficialResultLetterData(
			long electionId,
			LanguageCode languageCode,
			byte[] content) {
		if (content == null || content.length == 0) {
			return null;
		}
		PublicElectionOfficialResultSnapshot.OfficialResultLetterData data =
				new PublicElectionOfficialResultSnapshot.OfficialResultLetterData();
		data.setLanguageCode(languageCode);
		data.setContent(content);
		data.setUrl(LinksUtils.buildPublicElectionOfficialResultLetterLink(Long.valueOf(electionId), languageCode));
		return data;
	}

	private Map<ElectionCalendarKey, ElectionCalendar> indexCalendars(List<ElectionCalendar> calendars) {
		Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey = new EnumMap<>(ElectionCalendarKey.class);
		for (ElectionCalendar calendar : safeList(calendars)) {
			if (calendar == null || calendar.getCalendarKey() == null || calendarsByKey.containsKey(calendar.getCalendarKey())) {
				continue;
			}
			calendarsByKey.put(calendar.getCalendarKey(), calendar);
		}
		return calendarsByKey;
	}

	private Set<ElectionTaskKey> resolvePublicTaskKeys(List<ElectionTask> electionTasks) {
		Set<ElectionTaskKey> taskKeys = EnumSet.noneOf(ElectionTaskKey.class);
		Map<ElectionTaskKey, Boolean> publicableByKey = new EnumMap<>(ElectionTaskKey.class);
		for (ElectionTask task : safeList(electionTasks)) {
			if (task == null || task.getTaskKey() == null) {
				continue;
			}
			boolean publicable = task.isPublicable() || Boolean.TRUE.equals(publicableByKey.get(task.getTaskKey()));
			publicableByKey.put(task.getTaskKey(), Boolean.valueOf(publicable));
			if (publicable) {
				taskKeys.add(task.getTaskKey());
			}
		}
		return taskKeys;
	}

	private Map<Long, Candidate> indexCandidates(List<Candidate> candidates) {
		Map<Long, Candidate> rows = new HashMap<>();
		for (Candidate candidate : safeList(candidates)) {
			if (candidate == null) {
				continue;
			}
			rows.put(candidate.getCandidateId(), candidate);
		}
		return rows;
	}

	private List<Candidate> resolvePublishedCandidates(List<Candidate> candidates, Election election, Date generatedAt) {
		List<Candidate> rows = new ArrayList<>();
		for (Candidate candidate : safeList(candidates)) {
			if (isPublicCandidate(candidate)) {
				rows.add(candidate);
			}
		}
		Collections.sort(rows, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate a, Candidate b) {
				return Integer.compare(b != null ? b.getCandidateOrder() : 0, a != null ? a.getCandidateOrder() : 0);
			}
		});
		if (election != null && election.isRandomOrderCandidates()) {
			Collections.shuffle(rows, new Random());
		}
		return rows;
	}

	private boolean hasPublishedAbstentionCandidate(Iterable<Candidate> candidates) {
		if (candidates == null) {
			return false;
		}
		for (Candidate candidate : candidates) {
			if (candidate != null && candidate.isAbstention() && candidate.getStatus() == CandidateStatus.CONFIRMED_AND_PUBLISHED) {
				return true;
			}
		}
		return false;
	}

	private Map<Long, Nomination> indexNominationsByCandidate(List<Nomination> nominations) {
		Map<Long, Nomination> rows = new HashMap<>();
		for (Nomination nomination : safeList(nominations)) {
			if (nomination == null || nomination.getCandidate() == null) {
				continue;
			}
			rows.put(nomination.getCandidate().getCandidateId(), nomination);
		}
		return rows;
	}

	private Map<Long, List<SupportNomination>> indexSupportsByNomination(List<SupportNomination> supports) {
		Map<Long, List<SupportNomination>> rows = new HashMap<>();
		for (SupportNomination support : safeList(supports)) {
			if (support == null || support.getNomination() == null) {
				continue;
			}
			rows.computeIfAbsent(support.getNomination().getId(), key -> new ArrayList<SupportNomination>()).add(support);
		}
		return rows;
	}

	private Map<Long, List<PublicElectionCandidateCountryLinkRow>> indexCountryLinks(List<PublicElectionCandidateCountryLinkRow> countryLinks) {
		Map<Long, List<PublicElectionCandidateCountryLinkRow>> rows = new HashMap<>();
		for (PublicElectionCandidateCountryLinkRow link : safeList(countryLinks)) {
			if (link == null || link.getCandidateId() <= 0L) {
				continue;
			}
			rows.computeIfAbsent(link.getCandidateId(), key -> new ArrayList<PublicElectionCandidateCountryLinkRow>()).add(link);
		}
		return rows;
	}

	private Map<Long, List<PublicElectionCandidateWorkOrganizationRow>> indexWorkOrganizations(List<PublicElectionCandidateWorkOrganizationRow> workOrganizations) {
		Map<Long, List<PublicElectionCandidateWorkOrganizationRow>> rows = new HashMap<>();
		for (PublicElectionCandidateWorkOrganizationRow workOrganization : safeList(workOrganizations)) {
			if (workOrganization == null || workOrganization.getCandidateId() <= 0L) {
				continue;
			}
			rows.computeIfAbsent(workOrganization.getCandidateId(), key -> new ArrayList<PublicElectionCandidateWorkOrganizationRow>()).add(workOrganization);
		}
		return rows;
	}

	private Map<Long, List<CandidateQuestion>> indexQuestionsByCandidate(List<CandidateQuestion> candidateQuestions) {
		Map<Long, List<CandidateQuestion>> rows = new HashMap<>();
		for (CandidateQuestion question : safeList(candidateQuestions)) {
			if (question == null || question.getCandidate() == null) {
				continue;
			}
			rows.computeIfAbsent(question.getCandidate().getCandidateId(), key -> new ArrayList<CandidateQuestion>()).add(question);
		}
		return rows;
	}

	private PublicElectionSnapshotMetadata buildMetadata(long electionId, Date generatedAt, Date nextBusinessTransitionUtc) {
		PublicElectionSnapshotMetadata metadata = new PublicElectionSnapshotMetadata();
		metadata.setElectionId(Long.valueOf(electionId));
		metadata.setLastUpdatedUtc(generatedAt);
		metadata.setNextRefreshUtc(new Date(generatedAt.getTime() + (REFRESH_INTERVAL_MINUTES * 60L * 1000L)));
		metadata.setNextBusinessTransitionUtc(nextBusinessTransitionUtc);
		metadata.setStale(Boolean.FALSE);
		metadata.setRefreshStatus(REFRESH_STATUS_READY);
		metadata.setRefreshMessage(null);
		return metadata;
	}

	private PublicElectionSnapshotMetadata cloneMetadata(PublicElectionSnapshotMetadata source) {
		PublicElectionSnapshotMetadata metadata = new PublicElectionSnapshotMetadata();
		metadata.setElectionId(source != null ? source.getElectionId() : null);
		metadata.setLastUpdatedUtc(source != null ? source.getLastUpdatedUtc() : null);
		metadata.setNextRefreshUtc(source != null ? source.getNextRefreshUtc() : null);
		metadata.setNextBusinessTransitionUtc(source != null ? source.getNextBusinessTransitionUtc() : null);
		metadata.setStale(source != null ? source.getStale() : null);
		metadata.setRefreshStatus(source != null ? source.getRefreshStatus() : null);
		metadata.setRefreshMessage(source != null ? source.getRefreshMessage() : null);
		return metadata;
	}

	private Date resolveNextBusinessTransitionUtc(List<ElectionCalendar> calendars, Date now) {
		Date next = null;
		for (ElectionCalendar calendar : safeList(calendars)) {
			if (calendar == null) {
				continue;
			}
			next = minFutureDate(next, calendar.getStartDate(), now);
			next = minFutureDate(next, resolveEffectiveCalendarEnd(calendar), now);
		}
		return next;
	}

	private Date minFutureDate(Date current, Date candidate, Date now) {
		if (candidate == null || now == null || !candidate.after(now)) {
			return current;
		}
		if (current == null || candidate.before(current)) {
			return candidate;
		}
		return current;
	}

	private String resolveResultPublicationStage(Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey) {
		if (isCalendarReached(calendarsByKey, ElectionCalendarKey.N_22_SINGLE_OFFICIAL_RESULTS_WITH_CLAIMS_PUBLISHED)) {
			return PublicElectionStageCodes.OFFICIAL_WITH_CLAIMS;
		}
		if (isCalendarReached(calendarsByKey, ElectionCalendarKey.N_20_SINGLE_OFFICIAL_RESULTS_NO_CLAIMS_PUBLISHED)) {
			return PublicElectionStageCodes.OFFICIAL_NO_CLAIMS;
		}
		if (isCalendarReached(calendarsByKey, ElectionCalendarKey.N_19_PERIODO_VOTER_AUDIT)
				|| isCalendarReached(calendarsByKey, ElectionCalendarKey.N_21_PERIODO_VOTER_CLAIMS_RESPONSE)) {
			return PublicElectionStageCodes.VOTER_AUDIT;
		}
		if (isCalendarReached(calendarsByKey, ElectionCalendarKey.N_18_PERIODO_CE_AUDIT)) {
			return PublicElectionStageCodes.CE_AUDIT;
		}
		if (isCalendarReached(calendarsByKey, ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED)) {
			return PublicElectionStageCodes.PROVISIONAL;
		}
		return "NOT_PUBLISHED";
	}

	private boolean isCalendarReached(Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey, ElectionCalendarKey key) {
		ElectionCalendar calendar = calendarsByKey.get(key);
		return calendar != null && calendar.getStartDate() != null && !new Date().before(calendar.getStartDate());
	}

	private Date firstNonNullCalendarStart(Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey, ElectionCalendarKey... keys) {
		for (ElectionCalendarKey key : keys) {
			ElectionCalendar calendar = calendarsByKey.get(key);
			if (calendar != null && calendar.getStartDate() != null) {
				return calendar.getStartDate();
			}
		}
		return null;
	}

	private Date firstNonNullCalendarEnd(Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey, ElectionCalendarKey... keys) {
		for (ElectionCalendarKey key : keys) {
			ElectionCalendar calendar = calendarsByKey.get(key);
			if (calendar == null) {
				continue;
			}
			Date end = resolveEffectiveCalendarEnd(calendar);
			if (end != null) {
				return end;
			}
		}
		return null;
	}

	private Date resolveEffectiveCalendarEnd(ElectionCalendar calendar) {
		if (calendar == null || calendar.getStartDate() == null) {
			return null;
		}
		if (isSingleCalendarKey(calendar.getCalendarKey())) {
			return calendar.getStartDate();
		}
		return calendar.getEndDate() != null ? calendar.getEndDate() : calendar.getStartDate();
	}

	private Date resolveTimelineEndDate(ElectionCalendar calendar) {
		if (calendar == null || isSingleCalendarKey(calendar.getCalendarKey())) {
			return null;
		}
		return calendar.getEndDate();
	}

	private boolean isSingleCalendarKey(ElectionCalendarKey key) {
		return key != null && key.name().contains("_SINGLE_");
	}

	private String resolveResultContactEmail(Election election) {
		String recipient = election != null ? election.getDefaultRecipient() : null;
		if (!hasText(recipient)) {
			return null;
		}
		String[] split = recipient.trim().split("[,;\\s]+");
		for (String value : split) {
			if (hasText(value) && value.contains("@")) {
				return value.trim();
			}
		}
		return null;
	}

	private int calculateOrganizationsVoted(List<UserVoter> userVoters) {
		Set<String> orgs = new HashSet<>();
		for (UserVoter userVoter : safeList(userVoters)) {
			if (userVoter == null || !userVoter.isVoted() || !hasText(userVoter.getOrgID())) {
				continue;
			}
			orgs.add(userVoter.getOrgID().trim().toUpperCase(Locale.ROOT));
		}
		return orgs.size();
	}

	private double calculateParticipationPercentage(List<UserVoter> userVoters) {
		int enabled = 0;
		int voted = 0;
		for (UserVoter userVoter : safeList(userVoters)) {
			if (userVoter == null) {
				continue;
			}
			enabled++;
			if (userVoter.isVoted()) {
				voted++;
			}
		}
		if (enabled <= 0) {
			return 0.0d;
		}
		return (100.0d * voted) / (double) enabled;
	}

	private long countDistinctOrganizations(List<UserVoter> userVoters) {
		Set<String> ids = new HashSet<>();
		for (UserVoter userVoter : safeList(userVoters)) {
			if (userVoter != null && hasText(userVoter.getOrgID())) {
				ids.add(userVoter.getOrgID().trim().toUpperCase(Locale.ROOT));
			}
		}
		return ids.size();
	}

	private long countDistinctCountries(List<UserVoter> userVoters) {
		Set<String> codes = new HashSet<>();
		for (UserVoter userVoter : safeList(userVoters)) {
			if (userVoter != null && hasText(userVoter.getCountry())) {
				codes.add(userVoter.getCountry().trim().toUpperCase(Locale.ROOT));
			}
		}
		return codes.size();
	}

	private long countVotersByLanguage(List<UserVoter> userVoters, LanguageCode languageCode) {
		long total = 0L;
		for (UserVoter userVoter : safeList(userVoters)) {
			if (userVoter != null && userVoter.getLanguageEnum() == languageCode) {
				total++;
			}
		}
		return total;
	}

	private String resolvePrimaryCountryCode(List<PublicElectionCandidateCountryLinkRow> countryLinks) {
		for (PublicElectionCandidateCountryLinkRow link : safeList(countryLinks)) {
			if (link != null && link.isPrimaryCountry() && hasText(link.getCountryCode())) {
				return link.getCountryCode().trim().toUpperCase(Locale.ROOT);
			}
		}
		return null;
	}

	private List<String> resolveOtherCountryCodes(List<PublicElectionCandidateCountryLinkRow> countryLinks) {
		List<String> rows = new ArrayList<>();
		Set<String> dedup = new HashSet<>();
		for (PublicElectionCandidateCountryLinkRow link : safeList(countryLinks)) {
			if (link == null || link.isPrimaryCountry() || !hasText(link.getCountryCode())) {
				continue;
			}
			String countryCode = link.getCountryCode().trim().toUpperCase(Locale.ROOT);
			if (dedup.add(countryCode)) {
				rows.add(countryCode);
			}
		}
		Collections.sort(rows);
		return rows;
	}

	private List<String> resolveWorkOrganizationNames(List<PublicElectionCandidateWorkOrganizationRow> workOrganizations) {
		List<String> rows = new ArrayList<>();
		for (PublicElectionCandidateWorkOrganizationRow workOrganization : safeList(workOrganizations)) {
			if (workOrganization != null && hasText(workOrganization.getOrganizationName())) {
				rows.add(workOrganization.getOrganizationName().trim());
			}
		}
		return rows;
	}

	private List<String> resolveSupportOrganizationNames(List<SupportNomination> supports) {
		List<String> rows = new ArrayList<>();
		for (SupportNomination support : safeList(supports)) {
			if (support == null) {
				continue;
			}
			if (support.getSupportStatus() != SupportStatus.ACCEPTED && support.getSupportStatus() != SupportStatus.APPROVED) {
				continue;
			}
			if (support.getSupportingOrganization() != null && hasText(support.getSupportingOrganization().getName())) {
				rows.add(support.getSupportingOrganization().getName().trim());
			}
		}
		return rows;
	}

	private String resolveNominationCountryCode(
			Nomination nomination,
			Candidate candidate,
			Map<Long, List<PublicElectionCandidateCountryLinkRow>> countryLinksByCandidateId) {
		if (candidate != null) {
			String candidateCountryCode = resolvePrimaryCountryCode(countryLinksByCandidateId.get(candidate.getCandidateId()));
			if (hasText(candidateCountryCode)) {
				return candidateCountryCode;
			}
		}
		if (nomination != null && nomination.getOrganization() != null && hasText(nomination.getOrganization().getCountry())) {
			return nomination.getOrganization().getCountry().trim().toUpperCase(Locale.ROOT);
		}
		return null;
	}

	private int countEnabledSummaryTasks(Set<ElectionTaskKey> publicTaskKeys) {
		int count = 0;
		for (ElectionTaskKey taskKey : LANDING_INCOMPLETE_TASKS) {
			if (publicTaskKeys.contains(taskKey)) {
				count++;
			}
		}
		return count;
	}

	private int resolveCompletedPublicSummaryTasks(Candidate candidate, Set<ElectionTaskKey> publicTaskKeys, int enabledSummaryTasks) {
		if (candidate == null) {
			return 0;
		}
		List<CandidateElectionTaskProgress> progressRows = safeList(candidate.getTaskProgress());
		if (progressRows.isEmpty()) {
			return estimateCompletedTasks(candidate.getStatus(), enabledSummaryTasks);
		}
		int completed = 0;
		for (CandidateElectionTaskProgress progress : progressRows) {
			if (progress == null || progress.getElectionTask() == null || progress.getElectionTask().getTaskKey() == null) {
				continue;
			}
			ElectionTaskKey taskKey = progress.getElectionTask().getTaskKey();
			if (!publicTaskKeys.contains(taskKey) || !isSummaryTaskKey(taskKey)) {
				continue;
			}
			if (progress.getStatus() == CandidateElectionTaskStatus.COMPLETED) {
				completed++;
			}
		}
		return completed;
	}

	private int estimateCompletedTasks(CandidateStatus status, int enabledSummaryTasks) {
		if (enabledSummaryTasks <= 0) {
			return 0;
		}
		if (status == CandidateStatus.CONFIRMED_AND_PUBLISHED || status == CandidateStatus.COMPLETE) {
			return enabledSummaryTasks;
		}
		if (status == CandidateStatus.PRECOMPLETE) {
			return Math.max(1, enabledSummaryTasks - 1);
		}
		return 0;
	}

	private boolean isSummaryTaskKey(ElectionTaskKey taskKey) {
		for (ElectionTaskKey current : LANDING_INCOMPLETE_TASKS) {
			if (current == taskKey) {
				return true;
			}
		}
		return false;
	}

	private int resolveNominationSortRank(String candidateStatusCode, String nominationStatusCode) {
		CandidateStatus candidateStatus = parseCandidateStatus(candidateStatusCode);
		NominationStatus nominationStatus = parseNominationStatus(nominationStatusCode);
		if (candidateStatus == CandidateStatus.CONFIRMED_AND_PUBLISHED) {
			return 0;
		}
		if (candidateStatus == CandidateStatus.REJECTED || nominationStatus == NominationStatus.INVALID) {
			return 1;
		}
		if (candidateStatus == CandidateStatus.COMPLETE) {
			return 2;
		}
		if (nominationStatus == NominationStatus.PROPOSED
				|| nominationStatus == NominationStatus.REJECTED_BY_CANDIDATE) {
			return 4;
		}
		return 3;
	}

	private CandidateStatus parseCandidateStatus(String statusCode) {
		if (!hasText(statusCode)) {
			return null;
		}
		try {
			return CandidateStatus.valueOf(statusCode);
		} catch (Exception e) {
			return null;
		}
	}

	private NominationStatus parseNominationStatus(String statusCode) {
		if (!hasText(statusCode)) {
			return null;
		}
		try {
			return NominationStatus.valueOf(statusCode);
		} catch (Exception e) {
			return null;
		}
	}

	private boolean isPublicCandidate(Candidate candidate) {
		return candidate != null && !candidate.isAbstention() && candidate.getStatus() == CandidateStatus.CONFIRMED_AND_PUBLISHED;
	}

	private boolean isPendingCommunityQuestionStatus(CandidateQuestionStatus status) {
		return status == CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC
				|| status == CandidateQuestionStatus.QUESTION_READY_FOR_CANDIDATE
				|| status == CandidateQuestionStatus.ANSWER_SUBMITTED_BY_CANDIDATE;
	}

	private PublicElectionCoreSnapshot.LocalizedTextData resolveOtherStatutoryQuestion(int index) {
		PublicElectionCoreSnapshot.LocalizedTextData data = new PublicElectionCoreSnapshot.LocalizedTextData();
		data.setSpanish(getQuestionParameterValue(PARAM_OTHER_STATUTORY_Q_PREFIX + index + PARAM_LABEL_SUFFIX, "es", getDefaultOtherStatutoryQuestionLabel(index, "es")));
		data.setEnglish(getQuestionParameterValue(PARAM_OTHER_STATUTORY_Q_PREFIX + index + PARAM_LABEL_SUFFIX, "en", getDefaultOtherStatutoryQuestionLabel(index, "en")));
		data.setPortuguese(getQuestionParameterValue(PARAM_OTHER_STATUTORY_Q_PREFIX + index + PARAM_LABEL_SUFFIX, "pt", getDefaultOtherStatutoryQuestionLabel(index, "pt")));
		return data;
	}

	private PublicElectionCoreSnapshot.LocalizedTextData resolveOtherNonStatutoryQuestion() {
		PublicElectionCoreSnapshot.LocalizedTextData data = new PublicElectionCoreSnapshot.LocalizedTextData();
		data.setSpanish(getQuestionParameterValue(PARAM_OTHER_NON_STATUTORY_Q1_LABEL, "es", getDefaultOtherNonStatutoryQuestionLabel("es")));
		data.setEnglish(getQuestionParameterValue(PARAM_OTHER_NON_STATUTORY_Q1_LABEL, "en", getDefaultOtherNonStatutoryQuestionLabel("en")));
		data.setPortuguese(getQuestionParameterValue(PARAM_OTHER_NON_STATUTORY_Q1_LABEL, "pt", getDefaultOtherNonStatutoryQuestionLabel("pt")));
		return data;
	}

	private String getQuestionParameterValue(String keyPrefix, String language, String defaultValue) {
		String normalizedLanguage = normalizeLanguage(language);
		String parameterKey = keyPrefix + "_" + normalizedLanguage.toUpperCase(Locale.ROOT);
		String parameterValue = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(parameterKey);
		if (hasText(parameterValue)) {
			return parameterValue;
		}
		if (!"es".equals(normalizedLanguage)) {
			String spanishValue = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(keyPrefix + "_ES");
			if (hasText(spanishValue)) {
				return spanishValue;
			}
		}
		return defaultValue;
	}

	private String normalizeLanguage(String language) {
		if (!hasText(language)) {
			return "es";
		}
		String normalized = language.trim().toLowerCase(Locale.ROOT);
		if ("en".equals(normalized) || "pt".equals(normalized)) {
			return normalized;
		}
		return "es";
	}

	private String getDefaultOtherStatutoryQuestionLabel(int index, String language) {
		String normalizedLanguage = normalizeLanguage(language);
		if ("en".equals(normalizedLanguage)) {
			switch (index) {
			case 1:
				return "Could you briefly describe your personal profile and interests?*";
			case 2:
				return "How did your interest in participating in LACNIC arise and what has your trajectory or link with LACNIC been?*";
			case 3:
				return "What is your vision of LACNIC, its mission, and its contribution to the region's development and growth?*";
			case 4:
			default:
				return "What do you consider your main contribution to LACNIC as a member of the body, if elected, and what motivates you to apply for that position?*";
			}
		}
		if ("pt".equals(normalizedLanguage)) {
			switch (index) {
			case 1:
				return "Poderia descrever brevemente seu perfil pessoal e seus interesses?*";
			case 2:
				return "Como surgiu seu interesse em participar da LACNIC e qual tem sido sua trajetória ou vínculo com a LACNIC?*";
			case 3:
				return "Qual é sua visão sobre a LACNIC, sua missão e sua contribuição para o desenvolvimento e crescimento da região?*";
			case 4:
			default:
				return "Qual considera que seria sua principal contribuição para a LACNIC como integrante do órgão, caso seja eleito, e o que o motiva a se candidatar a essa posição?*";
			}
		}
		switch (index) {
		case 1:
			return "¿Podría describir brevemente su perfil personal y sus intereses?*";
		case 2:
			return "¿Cómo surgió su interés por participar en LACNIC y cuál ha sido su trayectoria o vinculación con LACNIC?*";
		case 3:
			return "¿Cuál es su visión sobre LACNIC, su misión y su contribución al desarrollo y crecimiento de la región?*";
		case 4:
		default:
			return "¿Cuál considera que sería su principal aporte a LACNIC como integrante del órgano, en caso de ser elegido, y qué le motiva a postularse a dicha posición?*";
		}
	}

	private String getDefaultOtherNonStatutoryQuestionLabel(String language) {
		String normalizedLanguage = normalizeLanguage(language);
		if ("en".equals(normalizedLanguage)) {
			return "What are your main motivations for this nomination, and what value do you expect to contribute from this position?*";
		}
		if ("pt".equals(normalizedLanguage)) {
			return "Quais são suas principais motivações para esta indicação e que valor você espera contribuir a partir desta posição?*";
		}
		return "¿Cuáles son sus principales motivaciones para esta nominación y qué valor espera aportar desde esta posición?*";
	}

	private PublicElectionCoreSnapshot.LocalizedTextData localizedText(String spanish, String english, String portuguese) {
		PublicElectionCoreSnapshot.LocalizedTextData data = new PublicElectionCoreSnapshot.LocalizedTextData();
		data.setSpanish(trimToNull(spanish));
		data.setEnglish(trimToNull(english));
		data.setPortuguese(trimToNull(portuguese));
		return data;
	}

	private PublicElectionCoreSnapshot.LocalizedTextData maskPendingText(PublicElectionCoreSnapshot.LocalizedTextData value) {
		PublicElectionCoreSnapshot.LocalizedTextData data = new PublicElectionCoreSnapshot.LocalizedTextData();
		data.setSpanish(maskPendingValue(value != null ? value.getSpanish() : null));
		data.setEnglish(maskPendingValue(value != null ? value.getEnglish() : null));
		data.setPortuguese(maskPendingValue(value != null ? value.getPortuguese() : null));
		return data;
	}

	private String maskPendingValue(String value) {
		if (!hasText(value)) {
			return null;
		}
		String normalized = value.trim();
		Character first = null;
		Character last = null;
		for (int i = 0; i < normalized.length(); i++) {
			char current = normalized.charAt(i);
			if (Character.isLetterOrDigit(current)) {
				if (first == null) {
					first = Character.valueOf(current);
				}
				last = Character.valueOf(current);
			}
		}
		if (first == null) {
			return "***";
		}
		int maskedLength = Math.max(3, Math.min(12, normalized.length() / 2));
		String maskedCore = repeat('*', maskedLength);
		if (last == null || Character.toUpperCase(first.charValue()) == Character.toUpperCase(last.charValue())) {
			return String.valueOf(first.charValue()) + maskedCore;
		}
		return String.valueOf(first.charValue()) + maskedCore + last.charValue();
	}

	private String repeat(char value, int times) {
		StringBuilder builder = new StringBuilder(Math.max(0, times));
		for (int i = 0; i < times; i++) {
			builder.append(value);
		}
		return builder.toString();
	}

	private String resolveAskedByInitials(String askedByName) {
		if (!hasText(askedByName)) {
			return "?";
		}
		String[] parts = askedByName.trim().split("\\s+");
		Character first = null;
		Character last = null;
		int count = 0;
		for (String part : parts) {
			Character current = extractInitial(part);
			if (current == null) {
				continue;
			}
			if (first == null) {
				first = current;
			}
			last = current;
			count++;
		}
		if (first == null) {
			return "?";
		}
		if (count <= 1 || last == null) {
			return String.valueOf(first.charValue());
		}
		return String.valueOf(first.charValue()) + last.charValue();
	}

	private Character extractInitial(String value) {
		if (!hasText(value)) {
			return null;
		}
		for (int i = 0; i < value.length(); i++) {
			char current = value.charAt(i);
			if (Character.isLetterOrDigit(current)) {
				return Character.valueOf(Character.toUpperCase(current));
			}
		}
		return null;
	}

	private String resolveOrganizationName(UserVoter userVoter) {
		String orgName = userVoter != null ? trimToNull(userVoter.getOrgName()) : null;
		return hasText(orgName) ? orgName : null;
	}

	private String maskRepresentative(String fullName) {
		if (!hasText(fullName)) {
			return DEFAULT_EMPTY_REPRESENTATIVE;
		}
		String[] parts = fullName.trim().split("\\s+");
		StringBuilder builder = new StringBuilder();
		int maxInitials = Math.min(2, parts.length);
		for (int i = 0; i < maxInitials; i++) {
			if (!hasText(parts[i])) {
				continue;
			}
			builder.append(Character.toUpperCase(parts[i].charAt(0))).append(". ");
		}
		builder.append(DEFAULT_EMPTY_REPRESENTATIVE);
		return builder.toString().trim();
	}

	private String normalizeCountry(String country) {
		if (!hasText(country)) {
			return null;
		}
		return country.trim().toUpperCase(Locale.ROOT);
	}

	private String trimToNull(String value) {
		return hasText(value) ? value.trim() : null;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private boolean hasBinary(byte[] value) {
		return value != null && value.length > 0;
	}

	private boolean hasAnyText(PublicElectionCoreSnapshot.LocalizedTextData value) {
		return value != null && (hasText(value.getSpanish()) || hasText(value.getEnglish()) || hasText(value.getPortuguese()));
	}

	private String safeString(String value) {
		return value != null ? value : "";
	}

	private <T> List<T> safeList(List<T> values) {
		return values != null ? values : Collections.<T>emptyList();
	}
}
