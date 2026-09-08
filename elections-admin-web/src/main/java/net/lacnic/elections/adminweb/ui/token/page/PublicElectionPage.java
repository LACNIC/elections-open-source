package net.lacnic.elections.adminweb.ui.token.page;

import java.io.Serializable;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.ui.components.WinnerPopoverBadgePanel;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.commons.ElectionResultLetterSupport;
import net.lacnic.elections.adminweb.ui.token.CandidateBiographyUtils;
import net.lacnic.elections.adminweb.ui.token.PublicNominationFormPanel;
import net.lacnic.elections.adminweb.ui.token.panel.PublicElectionModernPanel;
import net.lacnic.elections.adminweb.ui.token.panel.PublicElectionRecoverLinkPanel;
import net.lacnic.elections.adminweb.ui.token.PublicCalendarTimelinePanel;
import net.lacnic.elections.adminweb.ui.token.PublicCalendarTimelinePanel.TimelineEntryView;
import net.lacnic.elections.adminweb.ui.token.TokenResourceKeys;
import net.lacnic.elections.adminweb.wicket.util.BootstrapCssClasses;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.RestrictedCountriesMessageResolver;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionAuditorResult;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.ElectionLinkRecoveryMode;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
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
import net.lacnic.elections.domain.services.publicelection.PublicElectionVoteCountRow;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.CountryUtils;
import net.lacnic.elections.utils.PublicNominationConfiguration;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class PublicElectionPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();

	private static final int ROLL_PAGE_SIZE = 50;
	private static final int DEFAULT_LEGACY_MAX_ELECTION_ID = 81;
	private static final int ASK_QUESTION_NAME_MAX_LENGTH = 500;
	private static final int ASK_QUESTION_EMAIL_MAX_LENGTH = 320;
	private static final int ASK_QUESTION_TEXT_MAX_LENGTH = 1000;
	private static final String CANDIDATE_PROFILE_LINK_MARKUP_ID_PREFIX = "CandidateID_";

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

	private static final ElectionTaskKey[] CANDIDATE_SIDEBAR_TASKS = {
			ElectionTaskKey.COUNTRIES,
			ElectionTaskKey.ORG_SUPPORTS,
			ElectionTaskKey.USER_SUPPORTS_2,
			ElectionTaskKey.USER_SUPPORTS_5,
			ElectionTaskKey.ORGANIZATIONS,
			ElectionTaskKey.PROFILE
	};

	private Election election;
	private ElectionAuditorResult electionAuditorResult;

	private final Map<ElectionCalendarKey, ElectionCalendar> calendarsByKey = new EnumMap<>(ElectionCalendarKey.class);
	private final Set<ElectionTaskKey> publicTaskKeys = EnumSet.noneOf(ElectionTaskKey.class);
	private final Map<ElectionTaskKey, Boolean> taskPublicableByKey = new EnumMap<>(ElectionTaskKey.class);

	private final List<Candidate> publishedCandidates = new ArrayList<>();
	private final Map<Long, Candidate> publishedCandidateById = new HashMap<>();
	private final Map<Long, Candidate> candidateById = new HashMap<>();
	private final Map<Long, Nomination> nominationByCandidateId = new HashMap<>();
	private final Map<Long, List<SupportNomination>> supportsByNominationId = new HashMap<>();
	private final Map<Long, List<PublicElectionCandidateCountryLinkRow>> countryLinksByCandidateId = new HashMap<>();
	private final Map<Long, List<PublicElectionCandidateWorkOrganizationRow>> organizationsByCandidateId = new HashMap<>();
	private final Map<Long, List<CandidateQuestion>> publishedQuestionsByCandidateId = new HashMap<>();
	private final Map<Long, List<CandidateQuestion>> pendingQuestionsByCandidateId = new HashMap<>();
	private final List<Nomination> nominations = new ArrayList<>();
	private final List<UserVoter> userVoters = new ArrayList<>();
	private final Map<Long, Long> votesByCandidateId = new HashMap<>();
	private long totalVotes;

	private final Date now = new Date();

	private PageMode pageMode = PageMode.LANDING;
	private Candidate selectedCandidate;
	private boolean rollApplicable;
	private boolean legacyMode;
	private int legacyMaxElectionId = DEFAULT_LEGACY_MAX_ELECTION_ID;

	private String askedByName;
	private String askedByEmail;
	private String questionText;
	private boolean askSameQuestionToAllCandidates;

	private int rollPage = 1;
	private String rollCountryFilter;
	private String rollOrganizationFilter;
	private int rollTotalPages;
	private int rollTotalRows;
	private int rollPageFrom;
	private int rollPageTo;
	private final List<RollEntryView> rollPageRows = new ArrayList<>();
	private final List<RollEntryView> rollEntries = new ArrayList<>();
	private final List<String> rollCountryOptions = new ArrayList<>();

	public PublicElectionPage() {
		this(new PageParameters());
	}

	public PublicElectionPage(PageParameters params) {
		super(params);

		if (election == null) {
			addEmptyPageScaffold();
			return;
		}

		legacyMaxElectionId = resolveLegacyMaxElectionId();
		legacyMode = election.getElectionId() <= legacyMaxElectionId;
		if (legacyMode) {
			addEmptyPageScaffold();
			return;
		}

		loadElectionAuditorResult(election.getElectionId());
		loadData();
		resolvePageMode(params);
		if (pageMode == PageMode.INVALID_CANDIDATE) {
			setResponsePage(Error404.class);
			addEmptyPageScaffold();
			return;
		}

		if (pageMode == PageMode.ROLL && !rollApplicable) {
			setResponsePage(PublicElectionPage.class, baseLandingParameters());
			addEmptyPageScaffold();
			return;
		}

		if (pageMode == PageMode.NOMINATE && !isPublicNominationVisible()) {
			setResponsePage(PublicElectionPage.class, baseLandingParameters());
			addEmptyPageScaffold();
			return;
		}

		if (pageMode == PageMode.CANDIDATE && !isCandidateProfileRootVisible()) {
			setResponsePage(PublicElectionPage.class, baseLandingParameters());
			addEmptyPageScaffold();
			return;
		}

		if (pageMode == PageMode.ROLL && !isElectoralRollVisible()) {
			setResponsePage(PublicElectionPage.class, baseLandingParameters());
			addEmptyPageScaffold();
			return;
		}

		if (shouldLoadQuestionsForCurrentView()) {
			loadQuestions(election.getElectionId());
		}

		if (pageMode == PageMode.ROLL) {
			loadRollCollections();
			loadRollPageData(params);
		}

		add(buildModernPanel());
	}

	@Override
	protected boolean isGlobalFeedbackEnabled() {
		return false;
	}

	@Override
	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionPublicElection");
	}

	@Override
	protected Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params) {
		if (!hasText(getToken())) {
			return Error404.class;
		}
		election = AppContext.getInstance().getManagerBeanRemote().getElectionByQuestionToken(getToken());
		if (election == null) {
			return Error404.class;
		}
		preloadRestrictedCountryCodes(election);

		setElection(election);
		setHeaderElectionTitleFromElection(election);
			setHeaderUserDisplay(getString("publicCalendarCountdownHeaderUser"));
			setWhereAmI(getString("publicElectionWhereAmI"));
		setContextClass(Election.class.getName());
		setContextData("electionId: " + election.getElectionId() + "\n"
				+ "titleSpanish: " + election.getTitleSpanish() + "\n"
				+ "publicElectionLinkAvailable: " + election.isPublicElectionLinkAvailable());
		return null;
	}

	private void preloadRestrictedCountryCodes(Election currentElection) {
		if (currentElection == null) {
			return;
		}
		currentElection.setRestrictedCountryCodes(loadRestrictedCountryCodes(currentElection.getElectionId()));
	}

	private List<String> loadRestrictedCountryCodes(long electionId) {
		if (electionId <= 0) {
			return Collections.emptyList();
		}
		try {
			Election detailedElection = AppContext.getInstance().getManagerBeanRemote().getElectionWithRestrictedCountries(electionId);
			if (detailedElection == null || detailedElection.getRestrictedCountryCodes() == null) {
				return Collections.emptyList();
			}
			return new ArrayList<>(detailedElection.getRestrictedCountryCodes());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	@Override
	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		if (election != null && !election.isPublicElectionLinkAvailable()) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.ACCESS_NOT_AVAILABLE,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
					null,
					null));
		}
		ElectionCalendar calendar = findPublicCalendar(ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED);
		if (calendar == null || calendar.getStartDate() == null) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.PUBLIC_ELECTION_NOT_AVAILABLE,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
					null,
					null));
		}
		TokenAccessGate.AccessBlock before = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.PUBLIC_ELECTION_NOT_AVAILABLE,
				PublicAccessDeniedPage.CountdownTargetDate.START,
				ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
				null,
				null);
		return TokenAccessGate.of(calendar.getStartDate(), null, before, null);
	}

	private void loadData() {
		long electionId = election.getElectionId();

		loadCalendars();
		loadPublicTasks(electionId);
		loadCandidates(electionId);
		loadNominationsAndSupports(electionId);
		loadCountryLinks(electionId);
		loadWorkOrganizations(electionId);
		loadUserVotersAndVotes(electionId);

		rollApplicable = isStatutoryElection();
	}

	private boolean shouldLoadQuestionsForCurrentView() {
		if (pageMode != PageMode.CANDIDATE || selectedCandidate == null) {
			return false;
		}
		return isCommunityQuestionsVisible() || isPendingCommunityQuestionsVisible();
	}

	private void loadCalendars() {
		for (ElectionCalendar calendar : safeList(getPublicCalendars())) {
			if (calendar == null || calendar.getCalendarKey() == null) {
				continue;
			}
			if (!calendarsByKey.containsKey(calendar.getCalendarKey())) {
				calendarsByKey.put(calendar.getCalendarKey(), calendar);
			}
		}
	}

	private void loadPublicTasks(long electionId) {
		for (ElectionTask task : safeList(AppContext.getInstance().getManagerBeanRemote().getElectionTasks(electionId))) {
			if (task == null || task.getTaskKey() == null) {
				continue;
			}
			Boolean current = taskPublicableByKey.get(task.getTaskKey());
			boolean isPublicable = task.isPublicable() || Boolean.TRUE.equals(current);
			taskPublicableByKey.put(task.getTaskKey(), Boolean.valueOf(isPublicable));
			if (isPublicable) {
				publicTaskKeys.add(task.getTaskKey());
			}
		}
	}

	private void loadCandidates(long electionId) {
		for (Candidate candidate : safeList(AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(electionId))) {
			if (candidate == null) {
				continue;
			}
			candidateById.put(candidate.getCandidateId(), candidate);
			if (isPublicCandidate(candidate)) {
				publishedCandidates.add(candidate);
				publishedCandidateById.put(candidate.getCandidateId(), candidate);
			}
		}

		Collections.sort(publishedCandidates, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate a, Candidate b) {
				int aOrder = a != null ? a.getCandidateOrder() : 0;
				int bOrder = b != null ? b.getCandidateOrder() : 0;
				return Integer.compare(bOrder, aOrder);
			}
		});
		if (election != null && election.isRandomOrderCandidates()) {
			Collections.shuffle(publishedCandidates);
		}
	}

	private void loadNominationsAndSupports(long electionId) {
		nominations.addAll(safeList(AppContext.getInstance().getManagerBeanRemote().getElectionNominationsForPublicElectionPage(electionId)));
		for (Nomination nomination : nominations) {
			if (nomination == null || nomination.getCandidate() == null) {
				continue;
			}
			nominationByCandidateId.put(nomination.getCandidate().getCandidateId(), nomination);
		}

		for (SupportNomination support : safeList(AppContext.getInstance().getManagerBeanRemote().getElectionSupportNominationsForPublicElectionPage(electionId))) {
			if (support == null || support.getNomination() == null) {
				continue;
			}
			long nominationId = support.getNomination().getId();
			supportsByNominationId.computeIfAbsent(nominationId, key -> new ArrayList<SupportNomination>()).add(support);
		}
	}

	private void loadCountryLinks(long electionId) {
		for (PublicElectionCandidateCountryLinkRow link : safeList(AppContext.getInstance().getManagerBeanRemote().getElectionCandidateCountryLinkRowsForPublicElectionPage(electionId))) {
			if (link == null || link.getCandidateId() <= 0L) {
				continue;
			}
			countryLinksByCandidateId.computeIfAbsent(link.getCandidateId(), key -> new ArrayList<PublicElectionCandidateCountryLinkRow>()).add(link);
		}
	}

	private void loadWorkOrganizations(long electionId) {
		for (PublicElectionCandidateWorkOrganizationRow link : safeList(AppContext.getInstance().getManagerBeanRemote().getElectionCandidateWorkOrganizationRowsForPublicElectionPage(electionId))) {
			if (link == null || link.getCandidateId() <= 0L) {
				continue;
			}
			organizationsByCandidateId.computeIfAbsent(link.getCandidateId(), key -> new ArrayList<PublicElectionCandidateWorkOrganizationRow>()).add(link);
		}
	}

	private void loadQuestions(long electionId) {
		for (CandidateQuestion question : safeList(AppContext.getInstance().getManagerBeanRemote().getElectionCandidateQuestionsForPublicElectionPage(electionId))) {
			if (question == null || question.getCandidate() == null || question.getStatus() == null) {
				continue;
			}
			long candidateId = question.getCandidate().getCandidateId();
			if (question.getStatus() == CandidateQuestionStatus.PUBLISHED) {
				publishedQuestionsByCandidateId.computeIfAbsent(candidateId, key -> new ArrayList<CandidateQuestion>()).add(question);
				continue;
			}
			if (isPendingCommunityQuestionStatus(question.getStatus())) {
				pendingQuestionsByCandidateId.computeIfAbsent(candidateId, key -> new ArrayList<CandidateQuestion>()).add(question);
			}
		}
	}

	private void loadUserVotersAndVotes(long electionId) {
		if (shouldLoadUserVoters()) {
			userVoters.addAll(safeList(AppContext.getInstance().getManagerBeanRemote().getElectionUserVoters(electionId)));
		}
		if (shouldLoadVotes()) {
			totalVotes = 0L;
			votesByCandidateId.clear();
			for (PublicElectionVoteCountRow row : safeList(AppContext.getInstance().getManagerBeanRemote().getElectionVoteCountRowsForPublicElectionPage(electionId))) {
				if (row == null) {
					continue;
				}
				long candidateId = row.getCandidateId();
				long voteCount = row.getVoteCount();
				if (candidateId <= 0L || voteCount <= 0L) {
					continue;
				}
				votesByCandidateId.put(candidateId, voteCount);
				totalVotes += voteCount;
			}
		}
	}

	private void loadRollCollections() {
		rollEntries.clear();
		rollEntries.addAll(buildRollEntries());
		rollCountryOptions.clear();
		rollCountryOptions.addAll(buildRollCountryOptions(rollEntries));
	}

	private boolean shouldLoadUserVoters() {
		return isCalendarReached(ElectionCalendarKey.N_4_SINGLE_PADRON_PUBLISHED) || shouldLoadVotes();
	}

	private boolean shouldLoadVotes() {
		return isResultSectionVisible();
	}

	private void resolvePageMode(PageParameters params) {
		String action = normalizeAction(params.get(PublicElectionPageParameters.ACTION).toString(""));
		if (!hasText(action) || "landing".equals(action)) {
			pageMode = PageMode.LANDING;
			return;
		}

		if ("candidate".equals(action)) {
			long candidateId = params.get(PublicElectionPageParameters.CANDIDATE_ID).toLong(-1L);
			Candidate candidate = publishedCandidateById.get(candidateId);
			if (candidate == null) {
				pageMode = PageMode.INVALID_CANDIDATE;
				return;
			}
			selectedCandidate = candidate;
			pageMode = PageMode.CANDIDATE;
			return;
		}

		if ("roll".equals(action) || "electoral-roll".equals(action) || "padron".equals(action)) {
			pageMode = PageMode.ROLL;
			return;
		}

		if ("nominate".equals(action) || "nomination".equals(action)) {
			pageMode = PageMode.NOMINATE;
			return;
		}

		pageMode = PageMode.LANDING;
	}

	private void loadRollPageData(PageParameters params) {
		rollPage = Math.max(1, params.get(PublicElectionPageParameters.PAGE).toInt(1));
		rollCountryFilter = normalizeCountryFilter(params.get(PublicElectionPageParameters.COUNTRY).toString(""));
		rollOrganizationFilter = normalizeOrganizationFilter(params.get(PublicElectionPageParameters.ORG_NAME).toString(""));

		List<RollEntryView> filteredRows = new ArrayList<>();
		for (RollEntryView row : rollEntries) {
			if (row == null) {
				continue;
			}
			if (hasText(rollCountryFilter) && !rollCountryFilter.equalsIgnoreCase(row.getCountryCode())) {
				continue;
			}
			if (hasText(rollOrganizationFilter) && !containsIgnoreCase(row.getOrganizationName(), rollOrganizationFilter)) {
				continue;
			}
			filteredRows.add(row);
		}

		rollTotalRows = filteredRows.size();
		rollTotalPages = Math.max(1, (int) Math.ceil((double) rollTotalRows / (double) ROLL_PAGE_SIZE));
		if (rollPage > rollTotalPages) {
			rollPage = rollTotalPages;
		}

		int startIndex = (rollPage - 1) * ROLL_PAGE_SIZE;
		int endIndex = Math.min(startIndex + ROLL_PAGE_SIZE, rollTotalRows);
		rollPageFrom = rollTotalRows == 0 ? 0 : startIndex + 1;
		rollPageTo = endIndex;

		rollPageRows.clear();
		if (startIndex < endIndex) {
			rollPageRows.addAll(filteredRows.subList(startIndex, endIndex));
		}
	}

	private WebMarkupContainer buildLandingContainer() {
		WebMarkupContainer container = new WebMarkupContainer("landingContainer");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(pageMode == PageMode.LANDING);

		container.add(buildOfficialResultsSection("officialResultsSection"));
		container.add(buildCandidateCallBannerSection("candidateCallBannerSection"));

		WebMarkupContainer resultSection = new WebMarkupContainer("resultSection");
		resultSection.setOutputMarkupPlaceholderTag(true);
		boolean resultVisible = isResultSectionVisible();
		resultSection.setVisible(resultVisible);
		resultSection.add(new Label("resultPublicationStatus", resolveResultPublicationStatusText()));
		WebMarkupContainer resultHelpButton = new WebMarkupContainer("resultPublicationHelpButton");
		resultHelpButton.add(AttributeModifier.replace("data-bs-content", resolveResultPublicationHelpText()));
		resultSection.add(resultHelpButton);
		List<ResultRowView> winnerCandidates = buildWinnerCandidatesForDisplay();
		final String winnerElectionYearLabel = resolveWinnerElectionYearLabel();
		WebMarkupContainer winnerCardsSection = new WebMarkupContainer("winnerCardsSection");
		winnerCardsSection.setOutputMarkupPlaceholderTag(true);
		winnerCardsSection.setVisible(!winnerCandidates.isEmpty());
		winnerCardsSection.add(new ListView<ResultRowView>("winnerCards", winnerCandidates) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<ResultRowView> item) {
				ResultRowView row = item.getModelObject();
				Candidate candidate = row != null && row.getCandidateId() != null ? candidateById.get(row.getCandidateId()) : null;
				CountryDisplayRow primaryCountryRow = resolvePrimaryCountryRow(candidate);
				item.add(buildWinnerListPicture("winnerPicture", candidate));
				item.add(new Label("winnerName", valueOrDash(row != null ? row.getCandidateName() : null)));
				item.add(new WinnerPopoverBadgePanel("winnerWinnerBadge", row != null && row.isWinner()));
				item.add(new Label("winnerVotes", formatWinnerVotesLabel(row != null ? row.getVotesLabel() : "0")));
				item.add(new Label("winnerPercentage", row != null ? row.getPercentageLabel() : "0%"));
				long candidateId = row != null && row.getCandidateId() != null ? row.getCandidateId().longValue() : 0L;
				BookmarkablePageLink<Void> winnerProfileLink = new BookmarkablePageLink<Void>("winnerProfileLink", PublicElectionPage.class, buildCandidateParameters(candidateId));
				boolean profileVisible = canLinkToCandidateProfile(candidate);
				winnerProfileLink.setVisible(profileVisible);
				String linkedinUrl = resolveLinkedin(candidate, false);
				ExternalLink winnerLinkedinLink = new ExternalLink("winnerLinkedinLink", hasText(linkedinUrl) ? linkedinUrl : "#");
				winnerLinkedinLink.setVisible(hasText(linkedinUrl));
				boolean countryVisible = hasWinnerCountryData(primaryCountryRow);
				WebMarkupContainer winnerCountryBlock = new WebMarkupContainer("winnerCountryBlock");
				winnerCountryBlock.setOutputMarkupPlaceholderTag(true);
				winnerCountryBlock.setVisible(countryVisible);
				winnerCountryBlock.add(buildCountryFlagImage("winnerCountryFlag", primaryCountryRow, 12, 12));
				winnerCountryBlock.add(new Label("winnerCountry", countryVisible ? primaryCountryRow.getCountryLabel() : ""));
				WebMarkupContainer winnerYearBadge = new WebMarkupContainer("winnerYearBadge");
				winnerYearBadge.setOutputMarkupPlaceholderTag(true);
				winnerYearBadge.setVisible(hasText(winnerElectionYearLabel));
				winnerYearBadge.add(new Label("winnerYear", valueOrDash(winnerElectionYearLabel)));
				item.add(winnerCountryBlock);
				item.add(winnerYearBadge);
				item.add(winnerProfileLink);
				item.add(winnerLinkedinLink);
			}
		});
		resultSection.add(winnerCardsSection);
		resultSection.add(new ListView<ResultRowView>("resultRows", buildResultRows()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<ResultRowView> item) {
				ResultRowView row = item.getModelObject();
				item.add(new Label("resultCandidate", row.getCandidateName()));
				item.add(new WinnerPopoverBadgePanel("resultWinnerBadge", row.isWinner()));
				item.add(new Label("resultVotes", row.getVotesLabel()));
				item.add(new Label("resultPercentage", row.getPercentageLabel()));
			}
		});
		resultSection.add(new Label("resultTotalVotes", formatLong(totalVotes)));
		resultSection.add(new Label("resultTotalPercentage", totalVotes <= 0L ? "0%" : "100%"));
		resultSection.add(new ListView<ResultPublicationStageView>("resultStageRows", buildResultPublicationStages()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<ResultPublicationStageView> item) {
				ResultPublicationStageView row = item.getModelObject();
				item.add(new Label("stageTitle", row.getTitle()));
				item.add(new Label("stageSchedule", row.getSchedule()));
				WebMarkupContainer stageCurrentBadge = new WebMarkupContainer("stageCurrentBadge");
				stageCurrentBadge.setVisible(row.isCurrent());
				item.add(stageCurrentBadge);
			}
		});
		resultSection.add(new Label("summaryEnabledVoters", formatLong(calculateEnabledVoters())));
		resultSection.add(new Label("summaryOrganizationsVoted", formatLong(calculateOrganizationsVoted())));
		resultSection.add(new Label("summaryParticipation", calculateParticipationLabel()));
		String contactEmail = resolveResultContactEmail();
		resultSection.add(new ExternalLink("resultContactEmailLink",
				hasText(contactEmail) ? "mailto:" + contactEmail : "#",
				valueOrDash(contactEmail)));
		resultSection.add(new Label("resultFootnote", resolveResultFootnoteText()));
		container.add(resultSection);

		String electionCall = resolveElectionCall();
		WebMarkupContainer callSection = new WebMarkupContainer("callSection");
		callSection.setOutputMarkupPlaceholderTag(true);
		callSection.setVisible(hasText(electionCall));
		Label callContent = new Label("callContent", CandidateBiographyUtils.toRenderableMarkup(electionCall));
		callContent.setEscapeModelStrings(false);
		callSection.add(callContent);
		container.add(callSection);

		WebMarkupContainer candidatesSection = new WebMarkupContainer("candidatesSection");
		candidatesSection.setOutputMarkupPlaceholderTag(true);
		candidatesSection.setVisible(isLandingCandidatesVisible());
		boolean hasPublishedCandidates = !publishedCandidates.isEmpty();
		WebMarkupContainer candidateCardsGrid = new WebMarkupContainer("candidateCardsGrid");
		candidateCardsGrid.setOutputMarkupPlaceholderTag(true);
		candidateCardsGrid.setVisible(hasPublishedCandidates);
		WebMarkupContainer abstentionNotice = new WebMarkupContainer("abstentionNotice");
		abstentionNotice.setVisible(hasPublishedCandidates && hasPublishedAbstentionCandidate());
		candidatesSection.add(abstentionNotice);
		WebMarkupContainer randomOrderNotice = new WebMarkupContainer("randomOrderNotice");
		randomOrderNotice.setVisible(hasPublishedCandidates && election != null && election.isRandomOrderCandidates());
		candidatesSection.add(randomOrderNotice);
		candidateCardsGrid.add(new ListView<Candidate>("candidateCards", publishedCandidates) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<Candidate> item) {
				Candidate candidate = item.getModelObject();
				CountryDisplayRow primaryCountryRow = resolvePrimaryCountryRow(candidate);
				item.add(buildCandidatePicture("candidatePicture", candidate, 96, "rounded-circle"));
				item.add(new Label("candidateName", valueOrDash(candidate != null ? candidate.getName() : null)));
				item.add(new WinnerPopoverBadgePanel("candidateWinnerBadge", candidate != null && candidate.isWinner()));
				item.add(new Label("candidateOrganizations", resolveCandidateOrganizationsLabel(candidate)));
				item.add(buildCountryFlagImage("candidateCountryFlag", primaryCountryRow, 24, 16));
				item.add(new Label("candidateCountry", valueOrDash(primaryCountryRow.getCountryLabel())));
				item.add(new Label("candidateBioSnippet", resolveCandidateBioSnippet(candidate)));
				long candidateId = candidate != null ? candidate.getCandidateId() : 0L;
				BookmarkablePageLink<Void> candidateLink = new BookmarkablePageLink<Void>("candidateLink", PublicElectionPage.class, buildCandidateParameters(candidateId));
				candidateLink.setOutputMarkupId(true);
				candidateLink.setMarkupId(CANDIDATE_PROFILE_LINK_MARKUP_ID_PREFIX + candidateId);
				item.add(candidateLink);
			}
		});
		candidatesSection.add(candidateCardsGrid);
		WebMarkupContainer candidateCardsEmpty = new WebMarkupContainer("candidateCardsEmpty");
		candidateCardsEmpty.setOutputMarkupPlaceholderTag(true);
		candidateCardsEmpty.setVisible(!hasPublishedCandidates);
		candidatesSection.add(candidateCardsEmpty);
		container.add(candidatesSection);

		WebMarkupContainer calendarSection = new WebMarkupContainer("calendarSection");
		calendarSection.setOutputMarkupPlaceholderTag(true);
		calendarSection.setVisible(isCalendarSectionVisible());
		calendarSection.add(new PublicCalendarTimelinePanel("timelinePanel", buildLandingTimelineRows(), true));
		container.add(calendarSection);

		WebMarkupContainer rollSection = new WebMarkupContainer("rollSection");
		rollSection.setOutputMarkupPlaceholderTag(true);
		rollSection.setVisible(isElectoralRollVisible());
		rollSection.add(new Label("rollSummaryOrganizations", formatLong(countDistinctOrganizations())));
		rollSection.add(new Label("rollSummaryCountries", formatLong(countDistinctCountries())));
		rollSection.add(new Label("rollSummarySpanish", formatLong(countVotersByLanguage(LanguageCode.SP))));
		rollSection.add(new Label("rollSummaryEnglish", formatLong(countVotersByLanguage(LanguageCode.EN))));
		rollSection.add(new Label("rollSummaryPortuguese", formatLong(countVotersByLanguage(LanguageCode.PT))));
		rollSection.add(new BookmarkablePageLink<Void>("rollLink", PublicElectionPage.class, buildRollParameters(1)));
		container.add(rollSection);

		WebMarkupContainer recoverLinksSection = new WebMarkupContainer("recoverLinksSection");
		recoverLinksSection.setOutputMarkupPlaceholderTag(true);
		boolean recoverLinksVisible = isRecoverLinksSectionVisible();
		recoverLinksSection.setVisible(recoverLinksVisible);
		recoverLinksSection.add(new Label("recoverLinkCardTitle", new ResourceModel("recoverLinkTitleParticipation")));
		WebMarkupContainer recoverLinkModeHelp = new WebMarkupContainer("recoverLinkModeHelp");
		recoverLinkModeHelp.setOutputMarkupPlaceholderTag(true);
		recoverLinkModeHelp.setVisible(shouldShowRecoverLinksModePopover());
		recoverLinkModeHelp.add(AttributeModifier.replace("title", new ResourceModel("recoverLinkModeHelpTitle")));
		recoverLinkModeHelp.add(AttributeModifier.replace("aria-label", new ResourceModel("recoverLinkModeHelpTitle")));
		recoverLinkModeHelp.add(AttributeModifier.replace("data-bs-content", new ResourceModel("recoverLinkModeHelpOnlyBr")));
		recoverLinksSection.add(recoverLinkModeHelp);
		if (recoverLinksVisible) {
			recoverLinksSection.add(new PublicElectionRecoverLinkPanel("recoverLinkPanel", election));
		} else {
			recoverLinksSection.add(emptyContainer("recoverLinkPanel"));
		}
		container.add(recoverLinksSection);

		WebMarkupContainer incompleteSection = new WebMarkupContainer("incompleteSection");
		incompleteSection.setOutputMarkupPlaceholderTag(true);
		incompleteSection.setVisible(isIncompleteCandidatesVisible());
		incompleteSection.add(new ListView<NominationSummaryRow>("incompleteRows", buildNominationSummaryRows()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<NominationSummaryRow> item) {
				NominationSummaryRow row = item.getModelObject();
				item.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getRowCssClass()));

				Label candidateName = new Label("summaryCandidateName", row.getCandidateName());
				candidateName.setVisible(!row.isPublicCandidateLink());
				item.add(candidateName);

				BookmarkablePageLink<Void> candidateLink = new BookmarkablePageLink<Void>("summaryCandidateLink", PublicElectionPage.class, buildCandidateParameters(row.getCandidateId()));
				candidateLink.add(new Label("summaryCandidateLinkLabel", row.getCandidateName()));
				candidateLink.setVisible(row.isPublicCandidateLink());
				item.add(candidateLink);

				item.add(new Label("summaryCountry", row.getCountryLabel()));

				Label statusBadge = new Label("summaryCandidateStatus", row.getCandidateStatusLabel());
				statusBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getCandidateStatusCssClass()));
				item.add(statusBadge);

				item.add(new Label("summaryTasks", row.getTasksLabel()));
				item.add(new Label("summaryNomination", row.getNominationStatusLabel()));
			}
		});
		container.add(incompleteSection);
		container.add(buildDevSection("devSection", getString("publicElectionDevScopeLanding"), buildLandingDevSummaryRows()));

		return container;
	}

	private WebMarkupContainer buildOfficialResultsSection(String id) {
		WebMarkupContainer section = new WebMarkupContainer(id);
		section.setOutputMarkupPlaceholderTag(true);

		String officialResultsHtml = resolveOfficialResultsHtml();
		byte[] officialResultLetter = resolveOfficialResultLetter();
		boolean hasOfficialResultsHtml = hasText(officialResultsHtml);
		boolean hasOfficialResultLetter = hasBinary(officialResultLetter);
		section.setVisible(hasOfficialResultsHtml || hasOfficialResultLetter);

		section.add(new Label("officialResultsTitle", resolveOfficialResultsTitle()));

		Label content = new Label("officialResultsContent",
				hasOfficialResultsHtml ? CandidateBiographyUtils.toRenderableMarkup(officialResultsHtml) : "");
		content.setEscapeModelStrings(false);
		content.setVisible(hasOfficialResultsHtml);
		section.add(content);

		String contentType = ElectionResultLetterSupport.resolveContentType(officialResultLetter);
		Link<Void> letterLink = new Link<Void>("officialResultsLetterLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				if (!hasOfficialResultLetter) {
					return;
				}
				long downloadTimestamp = System.currentTimeMillis();
				String fileName = ElectionResultLetterSupport.appendUniqueSuffix(
						ElectionResultLetterSupport.resolveFileName(
								election != null ? election.getElectionId() : 0L,
								SecurityUtils.getLanguageCode(),
								officialResultLetter),
						downloadTimestamp);
				AbstractResourceStreamWriter resourceStream = new AbstractResourceStreamWriter() {
					private static final long serialVersionUID = 1L;

					@Override
					public void write(OutputStream output) throws IOException {
						output.write(officialResultLetter);
					}

					@Override
					public String getContentType() {
						return contentType;
					}
				};
				ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(resourceStream, fileName);
				handler.setContentDisposition(ContentDisposition.ATTACHMENT);
				getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
			}
		};
		letterLink.setVisible(hasOfficialResultLetter);
		section.add(letterLink);
		return section;
	}

	private WebMarkupContainer buildCandidateCallBannerSection(String id) {
		WebMarkupContainer section = new WebMarkupContainer(id);
		section.setOutputMarkupPlaceholderTag(true);

		String candidateCallInfo = resolveCandidateCallInfo();
		boolean candidateCallInfoVisible = hasText(candidateCallInfo);
		boolean publicNominationVisible = isPublicNominationVisible();
		section.setVisible(candidateCallInfoVisible || publicNominationVisible);

		WebMarkupContainer candidateCallInfoCard = new WebMarkupContainer("candidateCallInfoCard");
		candidateCallInfoCard.setOutputMarkupPlaceholderTag(true);
		candidateCallInfoCard.setVisible(candidateCallInfoVisible);
		candidateCallInfoCard.add(new Label("candidateCallTitle", resolveCandidateCallTitle()));
		candidateCallInfoCard.add(buildCandidateCallInfoAlert("candidateCallInfoAlert"));
		section.add(candidateCallInfoCard);

		WebMarkupContainer candidateNominationCard = new WebMarkupContainer("candidateNominationCard");
		candidateNominationCard.setOutputMarkupPlaceholderTag(true);
		candidateNominationCard.setVisible(publicNominationVisible);

		WebMarkupContainer bannerHeader = new WebMarkupContainer("candidateCallBannerHeader");
		bannerHeader.setOutputMarkupPlaceholderTag(true);
		bannerHeader.add(new Label("nominateTitle", resolveNominateLandingTitle()));
		candidateNominationCard.add(bannerHeader);
		candidateNominationCard.add(new BookmarkablePageLink<Void>("nominateLink", PublicElectionPage.class, buildNominateParameters()));
		section.add(candidateNominationCard);

		return section;
	}

	private WebMarkupContainer buildNominateContainer() {
		WebMarkupContainer container = new WebMarkupContainer("nominateContainer");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(pageMode == PageMode.NOMINATE && isPublicNominationVisible());

		container.add(new Label("nominatePageTitle", resolveNominatePageTitle()));
		container.add(new BookmarkablePageLink<Void>("nominateBackLink", PublicElectionPage.class, baseLandingParameters()));
		container.add(new WebMarkupContainer("nominateDescription").setVisible(false));
		container.add(new PublicNominationFormPanel("publicNominationFormPanel", election, getToken()));

		return container;
	}

	private WebMarkupContainer buildCandidateContainer() {
		WebMarkupContainer container = new WebMarkupContainer("candidateContainer");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(pageMode == PageMode.CANDIDATE && selectedCandidate != null);

		Candidate candidate = selectedCandidate;
		Nomination nomination = nominationByCandidateId.get(candidate != null ? candidate.getCandidateId() : 0L);

		container.add(buildCandidatePicture("detailCandidatePicture", candidate, 160, "rounded-circle img-thumbnail"));
		container.add(new Label("detailCandidateName", valueOrDash(candidate != null ? candidate.getName() : null)));
		container.add(new WinnerPopoverBadgePanel("detailCandidateWinnerBadge", candidate != null && candidate.isWinner()));
			container.add(new Label("detailCandidateOrganization", resolveNominationOrganizationName(nomination)));
			container.add(new ExternalLink("detailCandidateLinkedin", resolveLinkedin(candidate, true), getString("publicElectionModernDetailLinkedin")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(hasText(resolveLinkedin(candidate, false)));
			}
		});
		container.add(new BookmarkablePageLink<Void>("detailBackLink", PublicElectionPage.class, baseLandingParameters()));

		WebMarkupContainer profileBlock = new WebMarkupContainer("profileBlock");
		profileBlock.setOutputMarkupPlaceholderTag(true);
		profileBlock.setVisible(isTaskPublic(ElectionTaskKey.PROFILE));
		Label profileBio = new Label("profileBio", CandidateBiographyUtils.toRenderableMarkup(resolveCandidateBio(candidate)));
		profileBio.setEscapeModelStrings(false);
		profileBlock.add(profileBio);
		container.add(profileBlock);

		WebMarkupContainer statutoryBlock = new WebMarkupContainer("statutoryBlock");
		statutoryBlock.setOutputMarkupPlaceholderTag(true);
		statutoryBlock.setVisible(isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED) && isTaskPublic(ElectionTaskKey.OTHER_STATUTORY_QUESTIONS));
		List<StatutoryAnswerView> statutoryAnswers = buildStatutoryAnswerRows(candidate);
		statutoryBlock.add(new WebMarkupContainer("statutoryEmpty").setVisible(statutoryAnswers.isEmpty()));
		statutoryBlock.add(new ListView<StatutoryAnswerView>("statutoryRows", statutoryAnswers) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<StatutoryAnswerView> item) {
				StatutoryAnswerView row = item.getModelObject();
				item.add(new Label("statutoryAskedByInitials", "CE"));
				item.add(new Label("statutoryQuestion", row.getQuestionLabel()));
				item.add(new Label("statutoryDate", row.getDateLabel()));
				item.add(new Label("statutoryAnswer", row.getAnswer()));
			}
		});
		container.add(statutoryBlock);

		WebMarkupContainer nonStatutoryBlock = new WebMarkupContainer("nonStatutoryBlock");
		nonStatutoryBlock.setOutputMarkupPlaceholderTag(true);
		nonStatutoryBlock.setVisible(isCandidateNonStatutoryBlockVisible());
		List<StatutoryAnswerView> nonStatutoryAnswers = buildNonStatutoryAnswerRows(candidate);
		nonStatutoryBlock.add(new WebMarkupContainer("nonStatutoryEmpty").setVisible(nonStatutoryAnswers.isEmpty()));
		nonStatutoryBlock.add(new ListView<StatutoryAnswerView>("nonStatutoryRows", nonStatutoryAnswers) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<StatutoryAnswerView> item) {
				StatutoryAnswerView row = item.getModelObject();
				item.add(new Label("nonStatutoryAskedByInitials", "CE"));
				item.add(new Label("nonStatutoryQuestion", row.getQuestionLabel()));
				item.add(new Label("nonStatutoryDate", row.getDateLabel()));
				item.add(new Label("nonStatutoryAnswer", row.getAnswer()));
			}
		});
		container.add(nonStatutoryBlock);

		WebMarkupContainer askQuestionsBlock = new WebMarkupContainer("askQuestionsBlock");
		askQuestionsBlock.setOutputMarkupPlaceholderTag(true);
		askQuestionsBlock.setVisible(isCandidateQuestionFormVisible());
		boolean shouldResolveCaptcha = pageMode == PageMode.CANDIDATE && isCandidateQuestionFormVisible();
		String askQuestionDataSiteKey = shouldResolveCaptcha ? AppContext.getInstance().getManagerBeanRemote().getDataSiteKey() : "";
		boolean askQuestionCaptchaEnabled = shouldResolveCaptcha
				&& hasText(askQuestionDataSiteKey)
				&& AppContext.getInstance().getManagerBeanRemote().isShowCaptcha();
		Form<Void> askForm = new Form<Void>("askQuestionForm") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onValidate() {
				super.onValidate();
				if (!askQuestionCaptchaEnabled) {
					return;
				}
				HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
				String captchaResponse = request.getParameter("g-recaptcha-response");
				boolean valid = AppContext.getInstance().getManagerBeanRemote().isValidCaptchaResponse(captchaResponse);
				if (!valid) {
					error(getString("areYouRobot"));
				}
			}

				@Override
				protected void onSubmit() {
					LanguageCode languageCode = SecurityUtils.getLanguageCode();
					int createdCount = 0;
					for (Long candidateId : resolveQuestionTargetCandidateIds()) {
						if (candidateId == null || candidateId.longValue() <= 0L) {
							continue;
						}
						boolean created = AppContext.getInstance().getManagerBeanRemote().createCandidateQuestionFromPublicToken(
								getToken(),
								candidateId.longValue(),
								askedByName,
								askedByEmail,
								languageCode,
								questionText,
								SecurityUtils.getClientIp());
						if (created) {
							createdCount++;
						}
					}
					if (createdCount <= 0) {
						error(getString("publicQuestionCreateError"));
						return;
					}
					info(getString("publicQuestionCreateSuccess"));
					askedByName = null;
					askedByEmail = null;
					questionText = null;
					askSameQuestionToAllCandidates = false;
					setResponsePage(PublicElectionPage.class, buildCandidateParameters(selectedCandidate.getCandidateId()));
				}
			};
		TextField<String> askedByNameField = new TextField<String>("askedByName", new PropertyModel<String>(this, "askedByName"));
		askedByNameField.setRequired(true);
		askedByNameField.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_MAXLENGTH, String.valueOf(ASK_QUESTION_NAME_MAX_LENGTH)));
		askedByNameField.add(StringValidator.maximumLength(ASK_QUESTION_NAME_MAX_LENGTH));
		askForm.add(askedByNameField);

		EmailTextField askedByEmailField = new EmailTextField("askedByEmail", new PropertyModel<String>(this, "askedByEmail"));
		askedByEmailField.setRequired(true);
		askedByEmailField.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_MAXLENGTH, String.valueOf(ASK_QUESTION_EMAIL_MAX_LENGTH)));
		askedByEmailField.add(StringValidator.maximumLength(ASK_QUESTION_EMAIL_MAX_LENGTH));
		askedByEmailField.add(EmailAddressValidator.getInstance());
		askForm.add(askedByEmailField);

		AiAssistTextAreaPanel questionTextEditor = new AiAssistTextAreaPanel(
				"questionTextEditor",
				new PropertyModel<String>(this, "questionText"),
				null,
				null,
				(originalText, instruction, styleContext) -> null,
				false,
				true,
				false,
				false,
				false,
				ASK_QUESTION_TEXT_MAX_LENGTH,
				4,
				textArea -> {
					textArea.setRequired(true);
					textArea.add(AttributeModifier.replace("data-testid", "questionTextEditor:textArea"));
				});
		askForm.add(questionTextEditor);
		askForm.add(new CheckBox("askSameQuestionToAllCandidates", new PropertyModel<Boolean>(this, "askSameQuestionToAllCandidates")));
		WebMarkupContainer reCaptcha = new WebMarkupContainer("reCaptcha");
		reCaptcha.add(AttributeModifier.replace("data-sitekey", askQuestionDataSiteKey));
		reCaptcha.setVisibilityAllowed(askQuestionCaptchaEnabled);
		askForm.add(reCaptcha);
		askQuestionsBlock.add(askForm);
		container.add(askQuestionsBlock);

		WebMarkupContainer communityBlock = new WebMarkupContainer("communityBlock");
		communityBlock.setOutputMarkupPlaceholderTag(true);
		communityBlock.setVisible(isCommunityQuestionsVisible());
		List<CommunityQuestionView> communityQuestions = buildCommunityQuestionRows(candidate);
		communityBlock.add(new WebMarkupContainer("communityEmpty").setVisible(communityQuestions.isEmpty()));
		communityBlock.add(new ListView<CommunityQuestionView>("communityRows", communityQuestions) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<CommunityQuestionView> item) {
				CommunityQuestionView row = item.getModelObject();
				item.add(new Label("communityAskedByInitials", row.getAskedByInitials()));
				item.add(new Label("communityQuestionDate", row.getQuestionDateLabel()));
				WebMarkupContainer answerDateBlock = new WebMarkupContainer("communityAnswerDateBlock");
				answerDateBlock.setOutputMarkupPlaceholderTag(true);
				answerDateBlock.setVisible(hasText(row.getAnswerDateLabel()));
				answerDateBlock.add(new Label("communityAnswerDate", valueOrDash(row.getAnswerDateLabel())));
				item.add(answerDateBlock);
				item.add(new Label("communityQuestion", row.getQuestion()));
				item.add(new Label("communityAnswer", row.getAnswer()));
			}
		});
		container.add(communityBlock);

		WebMarkupContainer sidebarBlock = new WebMarkupContainer("sidebarBlock");
		sidebarBlock.setOutputMarkupPlaceholderTag(true);
		sidebarBlock.setVisible(isCandidateSidebarVisible());
		CountryDisplayRow sidebarPrimaryCountryRow = resolvePrimaryCountryRow(candidate);
		sidebarBlock.add(buildCountryFlagImage("sidebarPrimaryCountryFlag", sidebarPrimaryCountryRow, 30, 20));
		sidebarBlock.add(new Label("sidebarPrimaryCountry", valueOrDash(sidebarPrimaryCountryRow != null ? sidebarPrimaryCountryRow.getCountryLabel() : null)));
		List<CountryDisplayRow> sidebarOtherCountryRows = resolveOtherCountryRows(candidate);
		ListView<CountryDisplayRow> sidebarOtherCountries = new ListView<CountryDisplayRow>("sidebarOtherCountries", sidebarOtherCountryRows) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<CountryDisplayRow> item) {
				CountryDisplayRow row = item.getModelObject();
				item.add(buildCountryFlagImage("otherCountryFlag", row, 30, 20));
				item.add(new Label("otherCountryName", valueOrDash(row != null ? row.getCountryLabel() : null)));
			}
		};
		sidebarOtherCountries.setVisible(!sidebarOtherCountryRows.isEmpty());
		sidebarBlock.add(sidebarOtherCountries);
		sidebarBlock.add(new Label("sidebarOtherCountriesEmpty", getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_APPLICABLE)).setVisible(sidebarOtherCountryRows.isEmpty()));
		sidebarBlock.add(new ListView<String>("sidebarSupports", resolveSupportOrganizationNames(candidate)) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("supportName", valueOrDash(item.getModelObject())));
			}
		});
		WebMarkupContainer sidebarNominationBlock = new WebMarkupContainer("sidebarNominationBlock");
		sidebarNominationBlock.setOutputMarkupPlaceholderTag(true);
		sidebarNominationBlock.setVisible(isTaskPublic(ElectionTaskKey.PROFILE));
		sidebarNominationBlock.add(new Label("sidebarNominationOrg", resolveNominationOrganizationName(nomination)));
		sidebarNominationBlock.add(new Label("sidebarNominationReason", resolveNominationReason(nomination)));
		sidebarBlock.add(sidebarNominationBlock);
		List<String> sidebarOrganizationNames = resolveWorkOrganizationNames(candidate);
		ListView<String> sidebarOrganizations = new ListView<String>("sidebarOrganizations", sidebarOrganizationNames) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("organizationName", valueOrDash(item.getModelObject())));
			}
		};
		sidebarOrganizations.setVisible(!sidebarOrganizationNames.isEmpty());
		sidebarBlock.add(sidebarOrganizations);
		sidebarBlock.add(new Label("sidebarOrganizationsEmpty", getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_APPLICABLE)).setVisible(sidebarOrganizationNames.isEmpty()));
		container.add(sidebarBlock);
		container.add(buildDevSection("devSection", getString("publicElectionDevScopeCandidateProfile"), buildCandidateDevSummaryRows()));

		return container;
	}

	private WebMarkupContainer buildRollContainer() {
		WebMarkupContainer container = new WebMarkupContainer("rollContainer");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(pageMode == PageMode.ROLL && isElectoralRollVisible());

		container.add(new BookmarkablePageLink<Void>("rollBackLink", PublicElectionPage.class, baseLandingParameters()));
		container.add(new Label("rollTotalRows", formatLong((long) rollTotalRows)));
		container.add(new Label("rollPageRange", buildRollRangeLabel()));
			Form<Void> rollFilterForm = new Form<Void>("rollFilterForm") {
				private static final long serialVersionUID = 1L;
				@Override
				protected void onSubmit() {
					rollCountryFilter = normalizeCountryFilter(rollCountryFilter);
				rollOrganizationFilter = normalizeOrganizationFilter(rollOrganizationFilter);
					setResponsePage(PublicElectionPage.class, buildRollParameters(1));
				}
			};
			List<String> countryOptions = buildRollCountryOptions();
			if (hasText(rollCountryFilter) && !countryOptions.contains(rollCountryFilter)) {
				rollCountryFilter = "";
			}
			DropDownChoice<String> countryInput = new DropDownChoice<String>(
					"rollCountryInput",
					new PropertyModel<String>(this, "rollCountryFilter"),
					countryOptions,
					new ChoiceRenderer<String>() {
						private static final long serialVersionUID = 1L;
						@Override
						public Object getDisplayValue(String object) {
							if (!hasText(object)) {
								return getString("publicElectionModernRollFilterCountryFallback");
							}
							return resolveCountryLabel(object);
						}
						@Override
						public String getIdValue(String object, int index) {
							return object == null ? "" : object;
						}
					});
			countryInput.setNullValid(false);
			rollFilterForm.add(countryInput);
			rollFilterForm.add(new TextField<String>("rollOrgInput", new PropertyModel<String>(this, "rollOrganizationFilter")).add(StringValidator.maximumLength(1000)));

		BookmarkablePageLink<Void> clearFilters = new BookmarkablePageLink<Void>("rollClearButton", PublicElectionPage.class, buildRollParametersWithoutFilters(1));
		clearFilters.setVisible(hasText(rollCountryFilter) || hasText(rollOrganizationFilter));
		rollFilterForm.add(clearFilters);
		container.add(rollFilterForm);

		container.add(new ListView<RollEntryView>("rollRows", rollPageRows) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<RollEntryView> item) {
				RollEntryView row = item.getModelObject();
				item.add(new Label("rollCountry", row.getCountryCode()));
				item.add(new Label("rollOrganization", row.getOrganizationName()));
				item.add(new Label("rollRepresentative", row.getMaskedRepresentative()));
			}
		});

		BookmarkablePageLink<Void> prev = new BookmarkablePageLink<Void>("rollPrevPage", PublicElectionPage.class, buildRollParameters(rollPage - 1));
		prev.setVisible(rollPage > 1);
		container.add(prev);

		BookmarkablePageLink<Void> next = new BookmarkablePageLink<Void>("rollNextPage", PublicElectionPage.class, buildRollParameters(rollPage + 1));
		next.setVisible(rollPage < rollTotalPages);
		container.add(next);

		container.add(new Label("rollPageNumber", String.valueOf(rollPage)));
		container.add(new Label("rollPageCount", String.valueOf(rollTotalPages)));

		return container;
	}

	private WebMarkupContainer buildDevSection(String id, String scopeLabel, List<DevKeyValueRow> summaryRows) {
		WebMarkupContainer section = new WebMarkupContainer(id) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(PublicElectionPage.this.shouldShowDebugPanel());
			}
		};
		section.setOutputMarkupPlaceholderTag(true);
		section.add(new Label("devScopeLabel", scopeLabel));
		section.add(new Label("devNowServer", formatDateTimeServer(now)));
		section.add(new Label("devNowUtc", formatDateTimeUtc(now)));

		section.add(new ListView<DevKeyValueRow>("devSummaryRows", summaryRows) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<DevKeyValueRow> item) {
				DevKeyValueRow row = item.getModelObject();
				item.add(new Label("devSummaryKey", row.getKey()));
				item.add(new Label("devSummaryValue", row.getValue()));
				item.add(new Label("devSummaryDetail", row.getDetail()));
			}
		});

		section.add(new ListView<DevTaskRow>("devTaskRows", buildDevTaskRows()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<DevTaskRow> item) {
				DevTaskRow row = item.getModelObject();
				item.add(new Label("devTaskKey", row.getTaskKey()));
				item.add(new Label("devTaskConfigured", row.getConfiguredLabel()));
				item.add(new Label("devTaskPublicable", row.getPublicableLabel()));
			}
		});

		section.add(new ListView<DevCalendarRow>("devCalendarRows", buildDevCalendarRows()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<DevCalendarRow> item) {
				DevCalendarRow row = item.getModelObject();
				item.add(new Label("devCalendarKey", row.getCalendarKey()));
				item.add(new Label("devCalendarConfigured", row.getConfiguredLabel()));
				item.add(new Label("devCalendarPublicable", row.getPublicableLabel()));
				item.add(new Label("devCalendarStart", row.getStartLabel()));
				item.add(new Label("devCalendarEnd", row.getEndLabel()));
				item.add(new Label("devCalendarReached", row.getReachedLabel()));
				item.add(new Label("devCalendarDuring", row.getDuringLabel()));
			}
		});

		return section;
	}

	private List<DevKeyValueRow> buildLandingDevSummaryRows() {
		List<DevKeyValueRow> rows = new ArrayList<DevKeyValueRow>();
		rows.add(new DevKeyValueRow("pageMode", pageMode != null ? pageMode.name() : "-",
				getString("publicElectionDevLandingDetailPageMode")));
		rows.add(new DevKeyValueRow("rollApplicable", booleanText(rollApplicable),
				getString("publicElectionDevLandingDetailRollApplicable")));
		rows.add(new DevKeyValueRow("isCalendarSectionVisible", booleanText(isCalendarSectionVisible()),
				getString("publicElectionDevLandingDetailCalendarSectionVisible")));
		rows.add(new DevKeyValueRow("isLandingCandidatesVisible", booleanText(isLandingCandidatesVisible()),
				getString("publicElectionDevLandingDetailLandingCandidatesVisible")));
		rows.add(new DevKeyValueRow("isPublicNominationVisible", booleanText(isPublicNominationVisible()),
				getString("publicElectionDevLandingDetailPublicNominationVisible")));
		rows.add(new DevKeyValueRow("isIncompleteCandidatesVisible", booleanText(isIncompleteCandidatesVisible()),
				getString("publicElectionDevLandingDetailIncompleteCandidatesVisible")));
		rows.add(new DevKeyValueRow("isResultSectionVisible", booleanText(isResultSectionVisible()),
				getString("publicElectionDevLandingDetailResultSectionVisible")));
		rows.add(new DevKeyValueRow("isElectoralRollVisible", booleanText(isElectoralRollVisible()),
				getString("publicElectionDevLandingDetailElectoralRollVisible")));
		rows.add(new DevKeyValueRow("calendar.N_1.reached", booleanText(isCalendarReached(ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED)),
				getString("publicElectionDevLandingDetailN1Reached")));
		rows.add(new DevKeyValueRow("calendar.N_2.during", booleanText(isCalendarDuring(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES)),
				getString("publicElectionDevLandingDetailN2During")));
		rows.add(new DevKeyValueRow("calendar.N_4.reached", booleanText(isCalendarReached(ElectionCalendarKey.N_4_SINGLE_PADRON_PUBLISHED)),
				getString("publicElectionDevLandingDetailN4Reached")));
		rows.add(new DevKeyValueRow("calendar.N_11.reached", booleanText(isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED)),
				getString("publicElectionDevLandingDetailN11Reached")));
		rows.add(new DevKeyValueRow("calendar.N_17.reached", booleanText(isCalendarReached(ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED)),
				getString("publicElectionDevLandingDetailN17Reached")));
		return rows;
	}

	private List<DevKeyValueRow> buildCandidateDevSummaryRows() {
		List<DevKeyValueRow> rows = new ArrayList<DevKeyValueRow>();
		rows.add(new DevKeyValueRow("pageMode", pageMode != null ? pageMode.name() : "-",
				getString("publicElectionDevCandidateDetailPageMode")));
		rows.add(new DevKeyValueRow("selectedCandidateId", selectedCandidate != null ? String.valueOf(selectedCandidate.getCandidateId()) : "-",
				getString("publicElectionDevCandidateDetailSelectedId")));
		rows.add(new DevKeyValueRow("selectedCandidateStatus", selectedCandidate != null && selectedCandidate.getStatus() != null
				? selectedCandidate.getStatus().name() : "-",
				getString("publicElectionDevCandidateDetailSelectedStatus")));
		rows.add(new DevKeyValueRow("isCandidateProfileRootVisible", booleanText(isCandidateProfileRootVisible()),
				getString("publicElectionDevCandidateDetailProfileRootVisible")));
		rows.add(new DevKeyValueRow("isCandidateSidebarVisible", booleanText(isCandidateSidebarVisible()),
				getString("publicElectionDevCandidateDetailSidebarVisible")));
		rows.add(new DevKeyValueRow("isCandidateNonStatutoryBlockVisible", booleanText(isCandidateNonStatutoryBlockVisible()),
				getString("publicElectionDevCandidateDetailNonStatutoryVisible")));
		rows.add(new DevKeyValueRow("isCandidateQuestionFormVisible", booleanText(isCandidateQuestionFormVisible()),
				getString("publicElectionDevCandidateDetailQuestionFormVisible")));
		rows.add(new DevKeyValueRow("isCommunityQuestionsVisible", booleanText(isCommunityQuestionsVisible()),
				getString("publicElectionDevCandidateDetailCommunityVisible")));
			rows.add(new DevKeyValueRow("isPendingCommunityQuestionsVisible", booleanText(isPendingCommunityQuestionsVisible()),
					getString("publicElectionDevCandidateDetailPendingCommunityVisible")));
		rows.add(new DevKeyValueRow("task.PROFILE.public", booleanText(isTaskPublic(ElectionTaskKey.PROFILE)),
				getString("publicElectionDevCandidateDetailTaskProfile")));
		rows.add(new DevKeyValueRow("task.OTHER_STATUTORY_QUESTIONS.public", booleanText(isTaskPublic(ElectionTaskKey.OTHER_STATUTORY_QUESTIONS)),
				getString("publicElectionDevCandidateDetailTaskStatutory")));
		rows.add(new DevKeyValueRow("task.OTHER_NON_STATUTORY_QUESTIONS.public", booleanText(isTaskPublic(ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS)),
				getString("publicElectionDevCandidateDetailTaskNonStatutory")));
		rows.add(new DevKeyValueRow("calendar.N_11.reached", booleanText(isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED)),
				getString("publicElectionDevCandidateDetailN11Reached")));
		rows.add(new DevKeyValueRow("calendar.N_12.during", booleanText(isCalendarDuring(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS)),
				getString("publicElectionDevCandidateDetailN12During")));
		return rows;
	}

	private List<DevTaskRow> buildDevTaskRows() {
		List<DevTaskRow> rows = new ArrayList<DevTaskRow>();
		for (ElectionTaskKey taskKey : ElectionTaskKey.values()) {
			boolean configured = taskPublicableByKey.containsKey(taskKey);
			boolean publicable = Boolean.TRUE.equals(taskPublicableByKey.get(taskKey));
			rows.add(new DevTaskRow(
					taskKey.name(),
					booleanText(configured),
					booleanText(publicable)));
		}
		return rows;
	}

	private List<DevCalendarRow> buildDevCalendarRows() {
		List<DevCalendarRow> rows = new ArrayList<DevCalendarRow>();
		for (ElectionCalendarKey calendarKey : ElectionCalendarKey.values()) {
			ElectionCalendar calendar = calendarsByKey.get(calendarKey);
			boolean configured = calendar != null;
			boolean publicable = calendar != null && calendar.isPublicable();
			Date startDate = calendar != null ? calendar.getStartDate() : null;
			Date endDate = resolveEffectiveCalendarEnd(calendar);
			boolean reached = configured && startDate != null && !now.before(startDate);
			boolean during = configured && startDate != null && endDate != null
					&& !now.before(startDate) && !now.after(endDate);

			rows.add(new DevCalendarRow(
					calendarKey.name(),
					booleanText(configured),
					booleanText(publicable),
					formatDateTimeUtc(startDate),
					formatDateTimeUtc(endDate),
					booleanText(reached),
					booleanText(during)));
		}
		return rows;
	}

	private String booleanText(boolean value) {
		return value ? getString("publicElectionDevBooleanYes") : getString("publicElectionDevBooleanNo");
	}

	private void addEmptyPageScaffold() {
		add(emptyContainer("contentPanel"));
	}

	private WebMarkupContainer emptyContainer(String id) {
		WebMarkupContainer container = new WebMarkupContainer(id);
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(false);
		return container;
	}

	private Component buildModernPanel() {
		return new PublicElectionModernPanel("contentPanel",
				new FeedbackPanel("feedbackPanel"),
				buildLandingContainer(),
				buildNominateContainer(),
				buildCandidateContainer(),
				buildRollContainer());
	}

	private int resolveLegacyMaxElectionId() {
		try {
			Parameter parameter = AppContext.getInstance().getManagerBeanRemote()
					.getParameter(Constants.PUBLIC_ELECTION_LEGACY_MAX_ELECTION_ID);
			if (parameter == null || !hasText(parameter.getValue())) {
				return DEFAULT_LEGACY_MAX_ELECTION_ID;
			}
			return Integer.parseInt(parameter.getValue().trim());
		} catch (Exception e) {
			return DEFAULT_LEGACY_MAX_ELECTION_ID;
		}
	}

	private boolean isLandingCandidatesVisible() {
		return isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED);
	}

	private boolean isPublicNominationVisible() {
		return election != null
				&& isPublicNominationElectionType()
				&& isCalendarDuring(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
	}

	private boolean isPublicNominationElectionType() {
		ElectionType electionType = election != null ? election.getEffectiveElectionType() : null;
		try {
			Parameter parameter = AppContext.getInstance().getManagerBeanRemote().getParameter(Constants.PUBLIC_NOMINATION_ENABLED);
			return PublicNominationConfiguration.isEnabled(parameter != null ? parameter.getValue() : null, electionType);
		} catch (Exception e) {
			return PublicNominationConfiguration.isEnabled(null, electionType);
		}
	}

	private boolean isCalendarSectionVisible() {
		return isCalendarReached(ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED);
	}

	private boolean isResultSectionVisible() {
		return election != null
				&& election.isResultLinkAvailable()
				&& isCalendarReached(ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED);
	}

	private boolean isIncompleteCandidatesVisible() {
		return isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED);
	}

	private boolean isCandidateProfileRootVisible() {
		return isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED);
	}

	private boolean isCandidateQuestionFormVisible() {
		return isCalendarDuring(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
	}

	private boolean isCommunityQuestionsVisible() {
		return isCalendarReached(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
	}

	private boolean isPendingCommunityQuestionsVisible() {
		return isCalendarDuring(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
	}

	private boolean isCandidateSidebarVisible() {
		return isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED) && isAnyTaskPublic(CANDIDATE_SIDEBAR_TASKS);
	}

	private boolean isCandidateNonStatutoryBlockVisible() {
		return isCalendarReached(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED)
				&& isTaskPublic(ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS);
	}

	private boolean isElectoralRollVisible() {
		return rollApplicable && isCalendarReached(ElectionCalendarKey.N_4_SINGLE_PADRON_PUBLISHED);
	}

	private boolean isRecoverLinksSectionVisible() {
		return election != null
				&& !election.isClosed()
				&& !resolveLinkRecoveryMode().isNone();
	}

	private boolean shouldShowRecoverLinksModePopover() {
		return resolveLinkRecoveryMode().isOnlyBr();
	}

	private ElectionLinkRecoveryMode resolveLinkRecoveryMode() {
		return election != null ? election.getPublicLinkRecoveryMode() : ElectionLinkRecoveryMode.NONE;
	}

	private boolean isCalendarReached(ElectionCalendarKey key) {
		ElectionCalendar calendar = calendarsByKey.get(key);
		if (calendar == null || calendar.getStartDate() == null) {
			return false;
		}
		return !now.before(calendar.getStartDate());
	}

	private boolean isCalendarDuring(ElectionCalendarKey key) {
		ElectionCalendar calendar = calendarsByKey.get(key);
		if (calendar == null || calendar.getStartDate() == null) {
			return false;
		}
		Date endDate = resolveEffectiveCalendarEnd(calendar);
		return !now.before(calendar.getStartDate()) && !now.after(endDate);
	}

	private boolean isTaskPublic(ElectionTaskKey key) {
		return key != null && publicTaskKeys.contains(key);
	}

	private boolean isAnyTaskPublic(ElectionTaskKey... keys) {
		if (keys == null || keys.length == 0) {
			return false;
		}
		for (ElectionTaskKey key : keys) {
			if (isTaskPublic(key)) {
				return true;
			}
		}
		return false;
	}

	private boolean isPendingCommunityQuestionStatus(CandidateQuestionStatus status) {
		return status == CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC
				|| status == CandidateQuestionStatus.QUESTION_READY_FOR_CANDIDATE
				|| status == CandidateQuestionStatus.ANSWER_SUBMITTED_BY_CANDIDATE;
	}

	private PageParameters baseLandingParameters() {
		return buildTokenPageParameters();
	}

	private PageParameters buildCandidateParameters(long candidateId) {
		PageParameters parameters = baseLandingParameters();
		parameters.add(PublicElectionPageParameters.ACTION, "candidate");
		parameters.add(PublicElectionPageParameters.CANDIDATE_ID, candidateId);
		return parameters;
	}

	private PageParameters buildNominateParameters() {
		PageParameters parameters = baseLandingParameters();
		parameters.add(PublicElectionPageParameters.ACTION, "nominate");
		return parameters;
	}

	private boolean canLinkToCandidateProfile(Candidate candidate) {
		return candidate != null
				&& candidate.getCandidateId() > 0L
				&& isCandidateProfileRootVisible();
	}

	private PageParameters buildRollParameters(int page) {
		PageParameters parameters = baseLandingParameters();
		parameters.add(PublicElectionPageParameters.ACTION, "roll");
		parameters.add(PublicElectionPageParameters.PAGE, Math.max(1, page));
		if (hasText(rollCountryFilter)) {
			parameters.add(PublicElectionPageParameters.COUNTRY, rollCountryFilter);
		}
		if (hasText(rollOrganizationFilter)) {
			parameters.add(PublicElectionPageParameters.ORG_NAME, rollOrganizationFilter);
		}
		return parameters;
	}

	private PageParameters buildRollParametersWithoutFilters(int page) {
		PageParameters parameters = baseLandingParameters();
		parameters.add(PublicElectionPageParameters.ACTION, "roll");
		parameters.add(PublicElectionPageParameters.PAGE, Math.max(1, page));
		return parameters;
	}

	private List<ResultRowView> buildResultRows() {
		List<ResultRowView> rows = new ArrayList<ResultRowView>();
		for (Candidate candidate : buildResultCandidates()) {
			if (candidate == null || candidate.getCandidateId() <= 0L) {
				continue;
			}
			long candidateId = candidate.getCandidateId();
			long votesAmount = votesByCandidateId.containsKey(candidateId) ? votesByCandidateId.get(candidateId).longValue() : 0L;
			double pct = totalVotes > 0L ? (100.0d * votesAmount) / (double) totalVotes : 0.0d;
			rows.add(new ResultRowView(
					Long.valueOf(candidateId),
					candidate.getName() != null ? candidate.getName() : getString("publicElectionCandidateFallbackPrefix") + " " + candidateId,
					votesAmount,
					pct,
					candidate.isWinner(),
					resolveUiLocale()));
		}

		Collections.sort(rows, new Comparator<ResultRowView>() {
			@Override
			public int compare(ResultRowView a, ResultRowView b) {
				int votesCompare = Long.compare(b.getVotes(), a.getVotes());
				if (votesCompare != 0) {
					return votesCompare;
				}
				long aCandidateId = a.getCandidateId() != null ? a.getCandidateId().longValue() : 0L;
				long bCandidateId = b.getCandidateId() != null ? b.getCandidateId().longValue() : 0L;
				return Long.compare(aCandidateId, bCandidateId);
			}
		});
		return rows;
	}

	private List<Candidate> buildResultCandidates() {
		Map<Long, Candidate> resultCandidateById = new HashMap<Long, Candidate>();
		for (Candidate candidate : publishedCandidates) {
			if (candidate != null && candidate.getCandidateId() > 0L) {
				resultCandidateById.put(Long.valueOf(candidate.getCandidateId()), candidate);
			}
		}
		for (Candidate candidate : candidateById.values()) {
			if (candidate != null
					&& candidate.getCandidateId() > 0L
					&& candidate.isAbstention()
					&& candidate.getStatus() == CandidateStatus.CONFIRMED_AND_PUBLISHED) {
				resultCandidateById.put(Long.valueOf(candidate.getCandidateId()), candidate);
			}
		}
		return new ArrayList<Candidate>(resultCandidateById.values());
	}

	private List<ResultRowView> buildWinnerCandidatesForDisplay() {
		List<ResultRowView> winners = new ArrayList<ResultRowView>();
		for (ResultRowView row : buildResultRows()) {
			if (row != null && row.isWinner()) {
				winners.add(row);
			}
		}
		return winners;
	}

	private List<TimelineEntryView> buildLandingTimelineRows() {
		List<TimelineEntryView> rows = new ArrayList<TimelineEntryView>();
		List<ElectionCalendar> calendars = new ArrayList<ElectionCalendar>(calendarsByKey.values());
		Collections.sort(calendars, new Comparator<ElectionCalendar>() {
			@Override
			public int compare(ElectionCalendar a, ElectionCalendar b) {
				Date aStart = a != null ? a.getStartDate() : null;
				Date bStart = b != null ? b.getStartDate() : null;
				if (aStart == null && bStart == null) {
					ElectionCalendarKey aKey = a != null ? a.getCalendarKey() : null;
					ElectionCalendarKey bKey = b != null ? b.getCalendarKey() : null;
					if (aKey == null && bKey == null) {
						return 0;
					}
					if (aKey == null) {
						return 1;
					}
					if (bKey == null) {
						return -1;
					}
					return Integer.compare(aKey.ordinal(), bKey.ordinal());
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

		for (ElectionCalendar calendar : calendars) {
			if (calendar == null || !calendar.isPublicable() || calendar.getCalendarKey() == null) {
				continue;
			}
			ElectionCalendarKey key = calendar.getCalendarKey();
			rows.add(new TimelineEntryView(
					calendar.getStartDate(),
					resolveTimelineEndDate(calendar),
					key.ordinal(),
					getString("electionCalendarKey." + key.name(), null, key.name()),
					buildCalendarScheduleLabel(calendar),
					resolveTimelinePhaseLabel(key),
					null));
		}
		return rows;
	}

	private String resolveTimelinePhaseLabel(ElectionCalendarKey key) {
		if (key == null || key.name() == null) {
			return getString("auditPublicV2PhaseCalendar");
		}
		String name = key.name();
		if (name.contains("CALL_FOR_CANDIDATES")) {
			return getString("auditPublicV2PhaseCall");
		}
		if (name.contains("PADRON")) {
			return getString("auditPublicV2PhaseCensus");
		}
		if (name.contains("CANDIDATE") || name.contains("EVALUATION")) {
			return getString("auditPublicV2PhaseCandidates");
		}
		if (name.contains("VOTING")) {
			return getString("auditPublicV2PhaseVoting");
		}
		if (name.contains("AUDIT")) {
			return getString("auditPublicV2PhaseAudit");
		}
		if (name.contains("RESULT")) {
			return getString("auditPublicV2PhaseResults");
		}
		return getString("auditPublicV2PhaseCalendar");
	}

	private List<NominationSummaryRow> buildNominationSummaryRows() {
		List<NominationSummaryRow> rows = new ArrayList<NominationSummaryRow>();
		int enabledSummaryTasks = countEnabledSummaryTasks();
		for (Nomination nomination : nominations) {
			if (nomination == null) {
				continue;
			}

			Candidate candidate = nomination.getCandidate();
			CandidateStatus candidateStatus = candidate != null ? candidate.getStatus() : null;
			boolean isPublicCandidate = candidate != null && isPublicCandidate(candidate);
			String candidateName = isPublicCandidate ? valueOrDash(candidate.getName()) : "*******************";

			String countryLabel = resolveNominationCountry(nomination, candidate);
			StatusBadge candidateBadge = resolveCandidateStatusBadge(candidateStatus, nomination.getStatus());
			String tasksLabel = resolveTaskProgressLabel(candidate, enabledSummaryTasks);
			String nominationLabel = resolveNominationStatusLabel(nomination.getStatus());
			String rowClass = resolveNominationRowClass(candidateStatus, nomination.getStatus());
			int sortRank = resolveNominationSortRank(candidateStatus, nomination.getStatus());

			rows.add(new NominationSummaryRow(
					isPublicCandidate,
					candidate != null ? candidate.getCandidateId() : 0L,
					candidateName,
					countryLabel,
					candidateBadge.text,
					candidateBadge.cssClass,
					tasksLabel,
					nominationLabel,
					rowClass,
					sortRank));
		}

		Collections.sort(rows, new Comparator<NominationSummaryRow>() {
			@Override
			public int compare(NominationSummaryRow a, NominationSummaryRow b) {
				int rankCompare = Integer.valueOf(a.getSortRank()).compareTo(Integer.valueOf(b.getSortRank()));
				if (rankCompare != 0) {
					return rankCompare;
				}
				int nameCompare = a.getCandidateName().compareToIgnoreCase(b.getCandidateName());
				if (nameCompare != 0) {
					return nameCompare;
				}
				return Long.valueOf(a.getCandidateId()).compareTo(Long.valueOf(b.getCandidateId()));
			}
		});
		return rows;
	}

	private List<StatutoryAnswerView> buildStatutoryAnswerRows(Candidate candidate) {
		if (candidate == null) {
			return Collections.emptyList();
		}
		List<String> configuredQuestions = safeOtherStatutoryQuestions();
		String dateLabel = resolveCommissionQuestionsDateLabel();
		List<StatutoryAnswerView> rows = new ArrayList<StatutoryAnswerView>();
		addStatutoryAnswerRow(rows, configuredQuestions, 0, resolveLocalizedText(
				candidate.getQOtherStatutoryAnswer1Spanish(),
				candidate.getQOtherStatutoryAnswer1English(),
				candidate.getQOtherStatutoryAnswer1Portuguese()), dateLabel);
		addStatutoryAnswerRow(rows, configuredQuestions, 1, resolveLocalizedText(
				candidate.getQOtherStatutoryAnswer2Spanish(),
				candidate.getQOtherStatutoryAnswer2English(),
				candidate.getQOtherStatutoryAnswer2Portuguese()), dateLabel);
		addStatutoryAnswerRow(rows, configuredQuestions, 2, resolveLocalizedText(
				candidate.getQOtherStatutoryAnswer3Spanish(),
				candidate.getQOtherStatutoryAnswer3English(),
				candidate.getQOtherStatutoryAnswer3Portuguese()), dateLabel);
		addStatutoryAnswerRow(rows, configuredQuestions, 3, resolveLocalizedText(
				candidate.getQOtherStatutoryAnswer4Spanish(),
				candidate.getQOtherStatutoryAnswer4English(),
				candidate.getQOtherStatutoryAnswer4Portuguese()), dateLabel);
		return rows;
	}

	private List<StatutoryAnswerView> buildNonStatutoryAnswerRows(Candidate candidate) {
		if (candidate == null) {
			return Collections.emptyList();
		}
		String answer = resolveLocalizedText(candidate.getQOtherNonStatutoryAnswer1Spanish(),
				candidate.getQOtherNonStatutoryAnswer1English(),
				candidate.getQOtherNonStatutoryAnswer1Portuguese());
		if (!hasText(answer)) {
			return Collections.emptyList();
		}

		List<String> configuredQuestions = safeOtherNonStatutoryQuestions();
		String questionLabel = !configuredQuestions.isEmpty() ? configuredQuestions.get(0) : getString("publicElectionNonStatutoryQuestionFallback");
		List<StatutoryAnswerView> rows = new ArrayList<StatutoryAnswerView>();
		rows.add(new StatutoryAnswerView(questionLabel, answer, resolveCommissionQuestionsDateLabel()));
		return rows;
	}

	private String resolveCommissionQuestionsDateLabel() {
		ElectionCalendar calendar = calendarsByKey.get(ElectionCalendarKey.N_11_SINGLE_CANDIDATES_PUBLISHED);
		Date referenceDate = calendar != null && calendar.getStartDate() != null ? calendar.getStartDate() : now;
		return formatDateTime(referenceDate);
	}

	private void addStatutoryAnswerRow(List<StatutoryAnswerView> rows, List<String> configuredQuestions,
			int questionIndex, String answer, String dateLabel) {
		if (!hasText(answer)) {
			return;
		}
		String questionLabel = configuredQuestions != null && configuredQuestions.size() > questionIndex
				? configuredQuestions.get(questionIndex)
				: null;
		if (!hasText(questionLabel)) {
			questionLabel = getString("acceptNominationOtherStatutoryQuestionsQuestion" + (questionIndex + 1));
		}
		rows.add(new StatutoryAnswerView(questionLabel, answer, dateLabel));
	}

	private List<CommunityQuestionView> buildCommunityQuestionRows(Candidate candidate) {
		if (candidate == null) {
			return Collections.emptyList();
		}
		List<CommunityQuestionView> publishedRows = new ArrayList<CommunityQuestionView>();
		for (CandidateQuestion question : safeList(publishedQuestionsByCandidateId.get(candidate.getCandidateId()))) {
			if (question == null) {
				continue;
			}
			String questionTextValue = resolveLocalizedText(question.getQuestionSpanish(), question.getQuestionEnglish(), question.getQuestionPortuguese());
			String answerTextValue = resolveLocalizedText(question.getAnswerSpanish(), question.getAnswerEnglish(), question.getAnswerPortuguese());
			if (!hasText(questionTextValue) || !hasText(answerTextValue)) {
				continue;
			}
			Date questionDate = question.getCreationDate();
			Date answerDate = question.getPublishedDate() != null ? question.getPublishedDate() : question.getUpdateDate();
			publishedRows.add(new CommunityQuestionView(
					formatDateTime(questionDate),
					formatDateTime(answerDate),
					questionTextValue,
					answerTextValue,
					resolveAskedByInitials(question.getAskedByName()),
					question.getCandidateQuestionId()));
		}

		List<CommunityQuestionView> pendingRows = new ArrayList<CommunityQuestionView>();
		for (CandidateQuestion question : safeList(pendingQuestionsByCandidateId.get(candidate.getCandidateId()))) {
			if (question == null || !isPendingCommunityQuestionStatus(question.getStatus())) {
				continue;
			}
			CandidateQuestionStatus status = question.getStatus();
			if (status == null || status == CandidateQuestionStatus.REJECTED) {
				continue;
			}
			String questionTextValue = resolveLocalizedText(question.getQuestionSpanish(), question.getQuestionEnglish(), question.getQuestionPortuguese());
			String answerTextValue = resolveLocalizedText(question.getAnswerSpanish(), question.getAnswerEnglish(), question.getAnswerPortuguese());
			boolean hasAnswer = hasText(answerTextValue);
			boolean hasQuestion = hasText(questionTextValue);

			String resolvedQuestion = hasQuestion ? questionTextValue : getString("publicElectionPendingQuestionInReview");
			String resolvedAnswer = hasAnswer
					? maskPendingText(answerTextValue, getString("publicElectionPendingAnswerInReview"))
					: getString("publicElectionPendingAnswerInReview");
			switch (status) {
			case QUESTION_RECEIVED_LACNIC:
				resolvedQuestion = maskPendingText(questionTextValue, getString("publicElectionPendingQuestionInReview"));
				resolvedAnswer = getString("publicElectionPendingAnswerWaitingLacnic");
				break;
			case QUESTION_READY_FOR_CANDIDATE:
				resolvedAnswer = getString("publicElectionPendingAnswerNoResponse");
				break;
			case ANSWER_SUBMITTED_BY_CANDIDATE:
				break;
			case PUBLISHED:
				// Published questions are resolved from the published map.
				continue;
			case REJECTED:
			default:
				continue;
			}

			Date questionDate = question.getCreationDate() != null ? question.getCreationDate() : now;
			Date answerDate = hasAnswer ? (question.getUpdateDate() != null ? question.getUpdateDate() : null) : null;
			pendingRows.add(new CommunityQuestionView(
					formatDateTime(questionDate),
					formatDateTime(answerDate),
					resolvedQuestion,
					resolvedAnswer,
					resolveAskedByInitials(question.getAskedByName()),
					question.getCandidateQuestionId()));
		}

		List<CommunityQuestionView> rows = new ArrayList<CommunityQuestionView>(publishedRows.size() + pendingRows.size());
		rows.addAll(publishedRows);
		rows.addAll(pendingRows);
		Collections.sort(rows, new Comparator<CommunityQuestionView>() {
			@Override
			public int compare(CommunityQuestionView a, CommunityQuestionView b) {
				return Long.compare(b.getSortQuestionId(), a.getSortQuestionId());
			}
		});
		return rows;
	}

	private String maskPendingText(String value, String fallback) {
		if (!hasText(value)) {
			return fallback;
		}
		String normalized = value.trim();
		Character first = null;
		Character last = null;
		for (int i = 0; i < normalized.length(); i++) {
			char c = normalized.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				if (first == null) {
					first = c;
				}
				last = c;
			}
		}
		if (first == null) {
			return "***";
		}
		int maskedLength = Math.max(3, Math.min(12, normalized.length() / 2));
		String maskedCore = repeatChar('*', maskedLength);
		if (last == null || Character.toUpperCase(first.charValue()) == Character.toUpperCase(last.charValue())) {
			return String.valueOf(first) + maskedCore;
		}
		return String.valueOf(first) + maskedCore + last;
	}

	private String repeatChar(char value, int times) {
		int safeTimes = Math.max(0, times);
		StringBuilder builder = new StringBuilder(safeTimes);
		for (int i = 0; i < safeTimes; i++) {
			builder.append(value);
		}
		return builder.toString();
	}

	private String resolveAskedByInitials(String askedByNameValue) {
		if (!hasText(askedByNameValue)) {
			return "?";
		}
		String[] parts = askedByNameValue.trim().split("\\s+");
		Character first = null;
		Character last = null;
		int count = 0;
		for (String part : parts) {
			Character initial = extractInitial(part);
			if (initial == null) {
				continue;
			}
			if (first == null) {
				first = initial;
			}
			last = initial;
			count++;
		}
		if (first == null) {
			return "?";
		}
		if (count <= 1 || last == null) {
			return String.valueOf(first);
		}
		return String.valueOf(first) + last;
	}

	private Character extractInitial(String value) {
		if (!hasText(value)) {
			return null;
		}
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				return Character.toUpperCase(c);
			}
		}
		return null;
	}

	private List<RollEntryView> buildRollEntries() {
		List<RollEntryView> rows = new ArrayList<RollEntryView>();
		for (UserVoter userVoter : userVoters) {
			if (userVoter == null) {
				continue;
			}
			String country = normalizeCountry(userVoter.getCountry());
			String organizationName = resolveOrganizationName(userVoter);
			String representative = maskRepresentative(userVoter.getName());
			rows.add(new RollEntryView(country, organizationName, representative));
		}

		Collections.sort(rows, new Comparator<RollEntryView>() {
			@Override
			public int compare(RollEntryView a, RollEntryView b) {
				int orgCmp = valueOrDash(a.getOrganizationName()).compareToIgnoreCase(valueOrDash(b.getOrganizationName()));
				if (orgCmp != 0) {
					return orgCmp;
				}
				return valueOrDash(a.getCountryCode()).compareToIgnoreCase(valueOrDash(b.getCountryCode()));
			}
		});
		return rows;
	}

	private List<String> buildRollCountryOptions() {
		return new ArrayList<String>(rollCountryOptions);
	}

	private List<String> buildRollCountryOptions(List<RollEntryView> sourceRows) {
		Set<String> countryCodes = new HashSet<String>();
		for (RollEntryView row : sourceRows) {
			if (row == null) {
				continue;
			}
			String code = normalizeCountryFilter(row.getCountryCode());
			if (!hasText(code) || "-".equals(code)) {
				continue;
			}
			countryCodes.add(code);
		}
		List<String> ordered = new ArrayList<String>(countryCodes);
		Collections.sort(ordered, new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				return resolveCountryLabel(a).compareToIgnoreCase(resolveCountryLabel(b));
			}
		});
		List<String> options = new ArrayList<String>();
		options.add("");
		options.addAll(ordered);
		return options;
	}

	private long calculateEnabledVoters() {
		return userVoters.size();
	}

	private long calculateOrganizationsVoted() {
		Set<String> orgs = new HashSet<String>();
		for (UserVoter userVoter : userVoters) {
			if (userVoter == null || !userVoter.isVoted() || !hasText(userVoter.getOrgID())) {
				continue;
			}
			orgs.add(userVoter.getOrgID().toUpperCase(Locale.ROOT));
		}
		return orgs.size();
	}

	private String calculateParticipationLabel() {
		long enabled = calculateEnabledVoters();
		if (enabled <= 0L) {
			return "0%";
		}
		long voted = 0L;
		for (UserVoter userVoter : userVoters) {
			if (userVoter != null && userVoter.isVoted()) {
				voted++;
			}
		}
		double pct = (100.0d * voted) / (double) enabled;
		return formatPercentage(pct);
	}

	private long countDistinctOrganizations() {
		Set<String> ids = new HashSet<String>();
		for (UserVoter userVoter : userVoters) {
			if (userVoter != null && hasText(userVoter.getOrgID())) {
				ids.add(userVoter.getOrgID().toUpperCase(Locale.ROOT));
			}
		}
		return ids.size();
	}

	private long countDistinctCountries() {
		Set<String> codes = new HashSet<String>();
		for (UserVoter userVoter : userVoters) {
			if (userVoter != null && hasText(userVoter.getCountry())) {
				codes.add(userVoter.getCountry().toUpperCase(Locale.ROOT));
			}
		}
		return codes.size();
	}

	private long countVotersByLanguage(LanguageCode languageCode) {
		if (languageCode == null) {
			return 0L;
		}
		long total = 0L;
		for (UserVoter userVoter : userVoters) {
			if (userVoter == null) {
				continue;
			}
			LanguageCode voterLanguage = userVoter.getLanguageEnum();
			if (voterLanguage == languageCode) {
				total++;
			}
		}
		return total;
	}

	private ResultPublicationStage resolveResultPublicationStage() {
		if (isCalendarReached(ElectionCalendarKey.N_22_SINGLE_OFFICIAL_RESULTS_WITH_CLAIMS_PUBLISHED)) {
			return ResultPublicationStage.OFFICIAL_WITH_CLAIMS;
		}
		if (isCalendarReached(ElectionCalendarKey.N_20_SINGLE_OFFICIAL_RESULTS_NO_CLAIMS_PUBLISHED)) {
			return ResultPublicationStage.OFFICIAL_NO_CLAIMS;
		}
		if (isCalendarReached(ElectionCalendarKey.N_19_PERIODO_VOTER_AUDIT)
				|| isCalendarReached(ElectionCalendarKey.N_21_PERIODO_VOTER_CLAIMS_RESPONSE)) {
			return ResultPublicationStage.VOTER_AUDIT;
		}
		if (isCalendarReached(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT)) {
			return ResultPublicationStage.CE_AUDIT;
		}
		if (isCalendarReached(ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED)) {
			return ResultPublicationStage.PROVISIONAL;
		}
		return ResultPublicationStage.NOT_PUBLISHED;
	}

	private String resolveResultPublicationStatusText() {
		ResultPublicationStage stage = resolveResultPublicationStage();
		switch (stage) {
		case OFFICIAL_WITH_CLAIMS:
			return getString("publicElectionResultStatusOfficialWithClaims");
		case OFFICIAL_NO_CLAIMS:
			return getString("publicElectionResultStatusOfficialNoClaims");
		case VOTER_AUDIT:
			return getString("publicElectionResultStatusVoterAudit");
		case CE_AUDIT:
			return getString("publicElectionResultStatusCeAudit");
		case PROVISIONAL:
			return getString("publicElectionResultStatusProvisional");
		case NOT_PUBLISHED:
		default:
			return getString("publicElectionResultStatusNotPublished");
		}
	}

	private String resolveResultPublicationHelpText() {
		ResultPublicationStage stage = resolveResultPublicationStage();
		switch (stage) {
		case OFFICIAL_WITH_CLAIMS:
			return getString("publicElectionResultHelpOfficialWithClaims");
		case OFFICIAL_NO_CLAIMS:
			return getString("publicElectionResultHelpOfficialNoClaims");
		case VOTER_AUDIT:
			return getString("publicElectionResultHelpVoterAudit");
		case CE_AUDIT:
			return getString("publicElectionResultHelpCeAudit");
		case PROVISIONAL:
			return getString("publicElectionResultHelpProvisional");
		case NOT_PUBLISHED:
		default:
			return getString("publicElectionResultHelpNotPublished");
		}
	}

	private List<ResultPublicationStageView> buildResultPublicationStages() {
		ResultPublicationStage currentStage = resolveResultPublicationStage();
		List<ResultPublicationStageView> rows = new ArrayList<ResultPublicationStageView>();
		rows.add(new ResultPublicationStageView(
				getString("publicElectionResultStageProvisional"),
				resolveStageScheduleSingle(ElectionCalendarKey.N_17_SINGLE_PROVISIONAL_RESULTS_PUBLISHED),
				currentStage == ResultPublicationStage.PROVISIONAL));
		rows.add(new ResultPublicationStageView(
				getString("publicElectionResultStageCeAudit"),
				resolveCeAuditSchedule(),
				currentStage == ResultPublicationStage.CE_AUDIT));
		rows.add(new ResultPublicationStageView(
				getString("publicElectionResultStageVoterAudit"),
				resolveVoterAuditSchedule(),
				currentStage == ResultPublicationStage.VOTER_AUDIT));
		rows.add(new ResultPublicationStageView(
				getString("publicElectionResultStageOfficialNoClaims"),
				resolveOfficialNoClaimsSchedule(),
				currentStage == ResultPublicationStage.OFFICIAL_NO_CLAIMS));
		if (hasOfficialWithClaimsStageConfigured()) {
			rows.add(new ResultPublicationStageView(
					getString("publicElectionResultStageOfficialWithClaims"),
					resolveOfficialWithClaimsSchedule(),
					currentStage == ResultPublicationStage.OFFICIAL_WITH_CLAIMS));
		}
		return rows;
	}

	private List<String> safeOtherStatutoryQuestions() {
		List<String> fallback = new ArrayList<String>();
		fallback.add(getString("acceptNominationOtherStatutoryQuestionsQuestion1"));
		fallback.add(getString("acceptNominationOtherStatutoryQuestionsQuestion2"));
		fallback.add(getString("acceptNominationOtherStatutoryQuestionsQuestion3"));
		fallback.add(getString("acceptNominationOtherStatutoryQuestionsQuestion4"));
		try {
			List<String> fromBean = AppContext.getInstance().getPreNominationBeanRemote().getOtherStatutoryQuestions(SecurityUtils.getLocale().getLanguage());
			if (fromBean != null && fromBean.size() >= 4) {
				return fromBean;
			}
		} catch (Exception e) {
			return fallback;
		}
		return fallback;
	}

	private List<String> safeOtherNonStatutoryQuestions() {
		List<String> fallback = new ArrayList<String>();
		fallback.add(getString("acceptNominationOtherNonStatutoryQuestionsQuestion1"));
		try {
			List<String> fromBean = AppContext.getInstance().getPreNominationBeanRemote().getOtherNonStatutoryQuestions(SecurityUtils.getLocale().getLanguage());
			if (fromBean != null && !fromBean.isEmpty()) {
				return fromBean;
			}
		} catch (Exception e) {
			return fallback;
		}
		return fallback;
	}

	private boolean hasOfficialWithClaimsStageConfigured() {
		ElectionCalendar calendar = calendarsByKey.get(ElectionCalendarKey.N_22_SINGLE_OFFICIAL_RESULTS_WITH_CLAIMS_PUBLISHED);
		return calendar != null && calendar.getStartDate() != null;
	}

	private String resolveStageScheduleSingle(ElectionCalendarKey key) {
		ElectionCalendar calendar = calendarsByKey.get(key);
		if (calendar == null || calendar.getStartDate() == null) {
			return "-";
		}
		return formatDateTime(calendar.getStartDate());
	}

	private String resolveCeAuditSchedule() {
		ElectionCalendar calendar = calendarsByKey.get(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT);
		if (calendar == null || calendar.getStartDate() == null) {
			return "-";
		}
		Date start = calendar.getStartDate();
		Date end = calendar.getEndDate() != null ? calendar.getEndDate() : calendar.getStartDate();
		return buildDateRangeLabel(start, end);
	}

	private String resolveVoterAuditSchedule() {
		Date start = firstNonNullCalendarStart(
				ElectionCalendarKey.N_19_PERIODO_VOTER_AUDIT,
				ElectionCalendarKey.N_21_PERIODO_VOTER_CLAIMS_RESPONSE);
		Date end = firstNonNullCalendarEnd(
				ElectionCalendarKey.N_21_PERIODO_VOTER_CLAIMS_RESPONSE,
				ElectionCalendarKey.N_19_PERIODO_VOTER_AUDIT);
		return buildDateRangeLabel(start, end);
	}

	private String resolveOfficialNoClaimsSchedule() {
		return resolveStageScheduleSingle(ElectionCalendarKey.N_20_SINGLE_OFFICIAL_RESULTS_NO_CLAIMS_PUBLISHED);
	}

	private String resolveOfficialWithClaimsSchedule() {
		return resolveStageScheduleSingle(ElectionCalendarKey.N_22_SINGLE_OFFICIAL_RESULTS_WITH_CLAIMS_PUBLISHED);
	}

	private Date firstNonNullCalendarStart(ElectionCalendarKey... keys) {
		if (keys == null) {
			return null;
		}
		for (ElectionCalendarKey key : keys) {
			ElectionCalendar calendar = calendarsByKey.get(key);
			if (calendar == null || calendar.getStartDate() == null) {
				continue;
			}
			return calendar.getStartDate();
		}
		return null;
	}

	private Date firstNonNullCalendarEnd(ElectionCalendarKey... keys) {
		if (keys == null) {
			return null;
		}
		for (ElectionCalendarKey key : keys) {
			ElectionCalendar calendar = calendarsByKey.get(key);
			if (calendar == null) {
				continue;
			}
			if (calendar.getEndDate() != null) {
				return calendar.getEndDate();
			}
			if (calendar.getStartDate() != null) {
				return calendar.getStartDate();
			}
		}
		return null;
	}

	private String buildDateRangeLabel(Date start, Date end) {
		if (start == null && end == null) {
			return "-";
		}
		if (start == null) {
			return formatDateTime(end);
		}
		if (end == null) {
			return formatDateTime(start);
		}
		if (formatDateOnly(start).equals(formatDateOnly(end))) {
			return formatDateTime(start);
		}
		return formatDateOnly(start) + " → " + formatDateTime(end);
	}

	private String resolveResultContactEmail() {
		String recipient = election != null ? election.getDefaultRecipient() : null;
		if (!hasText(recipient)) {
			return null;
		}
		String normalized = recipient.trim();
		String[] split = normalized.split("[,;\\s]+");
		for (String value : split) {
			if (hasText(value) && value.contains("@")) {
				return value.trim();
			}
		}
		return null;
	}

	private String resolveResultFootnoteText() {
		return getString("publicElectionResultFootnote");
	}

	private String resolveOfficialResultsTitle() {
		String electionTitle = resolveLocalizedElectionTitle();
		if (!hasText(electionTitle)) {
			return getString("publicElectionOfficialResultsTitle");
		}
		return MessageFormat.format(getString("publicElectionOfficialResultsTitleWithElection"), electionTitle);
	}

	private String resolveElectionTitle() {
		String localizedTitle = resolveLocalizedElectionTitle();
		if (hasText(localizedTitle)) {
			return localizedTitle;
		}
		return valueOrDash(election != null ? election.getTitleSpanish() : null);
	}

	private String resolveLocalizedElectionTitle() {
		String localizedTitle = election != null ? election.getTitle(SecurityUtils.getLocale().getLanguage()) : null;
		if (hasText(localizedTitle)) {
			return localizedTitle.trim();
		}
		String titleSpanish = election != null ? election.getTitleSpanish() : null;
		return hasText(titleSpanish) ? titleSpanish.trim() : null;
	}

	private String resolveCandidateCallTitle() {
		return resolveElectionScopedTitle("publicElectionModernCandidateCallTitleWithElection", "publicElectionModernCandidateCallTitle");
	}

	private String resolveNominateLandingTitle() {
		return resolveElectionScopedTitle("publicElectionModernNominateTitleWithElection", "publicElectionModernNominateTitle");
	}

	private String resolveNominatePageTitle() {
		return resolveElectionScopedTitle("publicElectionModernNominatePageTitleWithElection", "publicElectionModernNominatePageTitle");
	}

	private String resolveElectionScopedTitle(String titleWithElectionKey, String fallbackKey) {
		String electionTitle = resolveLocalizedElectionTitle();
		if (!hasText(electionTitle)) {
			return getString(fallbackKey);
		}
		return MessageFormat.format(getString(titleWithElectionKey), electionTitle);
	}

	private String buildElectionRangeLabel() {
		ElectionCalendar voting = calendarsByKey.get(ElectionCalendarKey.N_16_PERIODO_VOTING);
		if (voting == null || voting.getStartDate() == null) {
			return getString("publicElectionNoVotingDate");
		}
		String start = formatDateTime(voting.getStartDate());
		String end = formatDateTime(voting.getEndDate() != null ? voting.getEndDate() : voting.getStartDate());
		return start + " - " + end;
	}

	private String buildCalendarScheduleLabel(ElectionCalendar calendar) {
		if (calendar == null || calendar.getStartDate() == null) {
			return "-";
		}
		String start = formatDateTime(calendar.getStartDate());
		Date endDate = resolveTimelineEndDate(calendar);
		if (endDate == null || endDate.equals(calendar.getStartDate())) {
			return start;
		}
		return start + " → " + formatDateTime(endDate);
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

	private String buildRollRangeLabel() {
		if (rollTotalRows == 0) {
			return getString("publicElectionRollRangeEmpty");
		}
		return getString("publicElectionRollRangePrefix")
				+ " " + rollPageFrom + " - " + rollPageTo
				+ " " + getString("publicElectionRollRangeOf")
				+ " " + rollTotalRows + " " + getString("publicElectionRollRangeRecords");
	}

	private String resolveCandidateBio(Candidate candidate) {
		if (candidate == null) {
			return "-";
		}
		String bio = candidate.getBio(SecurityUtils.getLocale().getLanguage());
		if (hasText(bio)) {
			return bio;
		}
		return valueOrDash(candidate.getBioSpanish());
	}

	private String resolveElectionCall() {
		if (election == null) {
			return null;
		}
		return resolveLocalizedText(election.getCallSpanish(), election.getCallEnglish(), election.getCallPortuguese());
	}

	private String resolveOfficialResultsHtml() {
		return electionAuditorResult != null ? electionAuditorResult.getResult(resolveCurrentPageLanguageCode()) : null;
	}

	private byte[] resolveOfficialResultLetter() {
		return electionAuditorResult != null ? electionAuditorResult.getResultLetter(resolveCurrentPageLanguageCode()) : null;
	}

	private LanguageCode resolveCurrentPageLanguageCode() {
		if (getPageParameters() != null) {
			LanguageCode requestedLanguage = LanguageCode.fromValue(getPageParameters().get("locale").toString(""));
			if (requestedLanguage != null) {
				return requestedLanguage;
			}
		}
		return SecurityUtils.getLanguageCode();
	}

	private Component buildCandidateCallInfoAlert(String id) {
		String candidateCallInfo = resolveCandidateCallInfo();
		Label alert = new Label(id, hasText(candidateCallInfo) ? CandidateBiographyUtils.toRenderableMarkup(candidateCallInfo) : "");
		alert.setEscapeModelStrings(false);
		alert.setOutputMarkupPlaceholderTag(true);
		alert.setVisible(hasText(candidateCallInfo));
		return alert;
	}

	private String resolveCandidateCallInfo() {
		if (!isCandidateCallInfoVisible() || election == null) {
			return null;
		}

		String introTemplate = resolveCandidateCallInfoTemplate();
		if (!hasText(introTemplate)) {
			return null;
		}

		String candidateCallInfo = MessageFormat.format(
				introTemplate,
				formatLong(Math.max(0, election.getMaxCandidates())));
		String restrictedCountriesParagraph = RestrictedCountriesMessageResolver.resolve(this, election);
		if (!hasText(restrictedCountriesParagraph)) {
			return candidateCallInfo;
		}
		return candidateCallInfo + "<br/><br/>" + restrictedCountriesParagraph;
	}

	private boolean isCandidateCallInfoVisible() {
		return isCalendarDuring(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
	}

	private String resolveCandidateCallInfoTemplate() {
		ElectionType electionType = election != null ? election.getEffectiveElectionType() : null;
		String specificKey = electionType != null
				? "publicElectionCandidateCallInfo." + electionType.name()
				: "publicElectionCandidateCallInfo.default";
		String template = getString(specificKey, null, null);
		if (hasText(template)) {
			return template;
		}
		return getString("publicElectionCandidateCallInfo.default", null, null);
	}

	private boolean hasWinnerCountryData(CountryDisplayRow row) {
		return row != null
				&& hasText(row.getCountryLabel())
				&& !getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_AVAILABLE).equals(row.getCountryLabel());
	}

	private String formatWinnerVotesLabel(String votesLabel) {
		return valueOrDash(votesLabel) + " " + getString("publicElectionModernWinnerVotesSuffix");
	}

	private String resolveWinnerElectionYearLabel() {
		Date yearDate = resolveWinnerElectionYearDate();
		if (yearDate == null) {
			return null;
		}
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		yearFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return yearFormat.format(yearDate);
	}

	private Date resolveWinnerElectionYearDate() {
		Date callPublishedDate = firstNonNullCalendarStart(ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED);
		if (callPublishedDate != null) {
			return callPublishedDate;
		}
		if (election != null && election.getVotingPeriodStartDate() != null) {
			return election.getVotingPeriodStartDate();
		}
		return election != null ? election.getVotingPeriodEndDate() : null;
	}

	private String resolveCandidateBioSnippet(Candidate candidate) {
		String bio = resolveCandidateBio(candidate);
		String snippet = CandidateBiographyUtils.toPlainTextSnippet(bio, 150);
		if (!hasText(snippet) || "-".equals(snippet)) {
			return getString(TokenResourceKeys.PUBLIC_ELECTION_NO_PUBLIC_BIO);
		}
		return snippet;
	}

	private String resolveNominationOrganizationName(Nomination nomination) {
		if (nomination == null || nomination.getOrganization() == null) {
			return getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_AVAILABLE);
		}
		return valueOrDash(nomination.getOrganization().getName());
	}

	private String resolveNominationReason(Nomination nomination) {
		if (nomination == null) {
			return getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_AVAILABLE);
		}
		return valueOrDash(nomination.getNominationReason(SecurityUtils.getLocale().getLanguage()));
	}

	private String resolveLinkedin(Candidate candidate, boolean withFallback) {
		String value = candidate != null ? candidate.getLinkedinUrl() : null;
		if (hasText(value)) {
			return value;
		}
		return withFallback ? "#" : null;
	}

	private String resolvePrimaryCountryLabel(Candidate candidate) {
		return resolvePrimaryCountryRow(candidate).getCountryLabel();
	}

	private CountryDisplayRow resolvePrimaryCountryRow(Candidate candidate) {
		if (candidate == null) {
			return new CountryDisplayRow(null, getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_AVAILABLE), null);
		}
		for (PublicElectionCandidateCountryLinkRow link : safeList(countryLinksByCandidateId.get(candidate.getCandidateId()))) {
			if (link == null || !link.isPrimaryCountry() || !hasText(link.getCountryCode())) {
				continue;
			}
			String normalizedCode = normalizeCountryCodeForFlag(link.getCountryCode());
			String countryLabel = resolveCountryLabel(link.getCountryCode());
			return new CountryDisplayRow(
					normalizedCode,
					countryLabel,
					normalizedCode != null ? "v2/images/flags/" + normalizedCode.toLowerCase(Locale.ROOT) + ".svg" : null);
		}
		return new CountryDisplayRow(null, getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_AVAILABLE), null);
	}

	private List<CountryDisplayRow> resolveOtherCountryRows(Candidate candidate) {
		if (candidate == null) {
			return Collections.emptyList();
		}
		List<CountryDisplayRow> rows = new ArrayList<CountryDisplayRow>();
		Set<String> addedLabels = new HashSet<String>();
		for (PublicElectionCandidateCountryLinkRow link : safeList(countryLinksByCandidateId.get(candidate.getCandidateId()))) {
			if (link == null || link.isPrimaryCountry() || !hasText(link.getCountryCode())) {
				continue;
			}
			String normalizedCode = normalizeCountryCodeForFlag(link.getCountryCode());
			String countryLabel = resolveCountryLabel(link.getCountryCode());
			String dedupeKey = hasText(countryLabel) ? countryLabel.trim().toUpperCase(Locale.ROOT)
					: (normalizedCode != null ? normalizedCode : link.getCountryCode().trim().toUpperCase(Locale.ROOT));
			if (!addedLabels.add(dedupeKey)) {
				continue;
			}
			rows.add(new CountryDisplayRow(
					normalizedCode,
					countryLabel,
					normalizedCode != null ? "v2/images/flags/" + normalizedCode.toLowerCase(Locale.ROOT) + ".svg" : null));
		}
		return rows;
	}

	private Component buildCountryFlagImage(String id, CountryDisplayRow row, int width, int height) {
		if (row != null && hasText(row.getFlagImagePath())) {
			ContextImage flag = new ContextImage(id, row.getFlagImagePath());
			flag.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_WIDTH, String.valueOf(width)));
			flag.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_HEIGHT, String.valueOf(height)));
			flag.add(AttributeModifier.replace("alt", valueOrDash(row.getCountryLabel())));
			flag.add(AttributeModifier.replace("onerror", "this.style.display='none';"));
			return flag;
		}
		WebMarkupContainer hidden = new WebMarkupContainer(id);
		hidden.setVisible(false);
		return hidden;
	}

	private String normalizeCountryCodeForFlag(String countryCode) {
		if (!hasText(countryCode)) {
			return null;
		}
		String normalized = countryCode.trim().toUpperCase(Locale.ROOT);
		if (normalized.length() != 2) {
			return null;
		}
		return normalized;
	}

	private Component buildWinnerListPicture(String id, Candidate candidate) {
		String cssClass = "rounded-circle";
		String fixedSquareStyle = "width:40px;height:40px;object-fit:cover;object-position:center;";
		if (candidate != null && candidate.getPictureInfo() != null && candidate.getPictureInfo().length > 0) {
			String extension = hasText(candidate.getPictureExtension()) ? candidate.getPictureExtension() : "jpg";
			NonCachingImage image = new NonCachingImage(id, new ImageResource(candidate.getPictureInfo(), extension));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, cssClass));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_WIDTH, "40"));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_HEIGHT, "40"));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_STYLE, fixedSquareStyle));
			return image;
		}
		ContextImage image = new ContextImage(id, "image/default_candidate_photo.jpg");
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, cssClass));
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_WIDTH, "40"));
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_HEIGHT, "40"));
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_STYLE, fixedSquareStyle));
		return image;
	}

	private List<String> resolveSupportOrganizationNames(Candidate candidate) {
		if (candidate == null) {
			return Collections.singletonList(getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_APPLICABLE));
		}
		Nomination nomination = nominationByCandidateId.get(candidate.getCandidateId());
		if (nomination == null) {
			return Collections.singletonList(getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_APPLICABLE));
		}
		List<String> values = new ArrayList<String>();
		for (SupportNomination support : safeList(supportsByNominationId.get(nomination.getId()))) {
			if (support == null) {
				continue;
			}
			if (support.getSupportStatus() != SupportStatus.ACCEPTED && support.getSupportStatus() != SupportStatus.APPROVED) {
				continue;
			}
			if (support.getSupportingOrganization() != null && hasText(support.getSupportingOrganization().getName())) {
				values.add(support.getSupportingOrganization().getName());
			}
		}
		if (values.isEmpty()) {
			return Collections.singletonList(getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_APPLICABLE));
		}
		return values;
	}

	private List<String> resolveWorkOrganizationNames(Candidate candidate) {
		if (candidate == null) {
			return Collections.emptyList();
		}
		List<String> values = new ArrayList<String>();
		for (PublicElectionCandidateWorkOrganizationRow workOrg : safeList(organizationsByCandidateId.get(candidate.getCandidateId()))) {
			if (workOrg != null && hasText(workOrg.getOrganizationName())) {
				values.add(workOrg.getOrganizationName());
			}
		}
		return values;
	}

	private String resolveCandidateOrganizationsLabel(Candidate candidate) {
		List<String> values = resolveWorkOrganizationNames(candidate);
		if (values.isEmpty()) {
			return getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_APPLICABLE);
		}
		return String.join(", ", values);
	}

	private String resolveNominationCountry(Nomination nomination, Candidate candidate) {
		if (candidate != null) {
			for (PublicElectionCandidateCountryLinkRow link : safeList(countryLinksByCandidateId.get(candidate.getCandidateId()))) {
				if (link != null && link.isPrimaryCountry() && hasText(link.getCountryCode())) {
					return link.getCountryCode().toUpperCase(Locale.ROOT);
				}
			}
		}
		if (nomination != null && nomination.getOrganization() != null && hasText(nomination.getOrganization().getCountry())) {
			return nomination.getOrganization().getCountry().toUpperCase(Locale.ROOT);
		}
		return "-";
	}

	private int countEnabledSummaryTasks() {
		int count = 0;
		for (ElectionTaskKey key : LANDING_INCOMPLETE_TASKS) {
			if (isTaskPublic(key)) {
				count++;
			}
		}
		return count;
	}

	private String resolveTaskProgressLabel(Candidate candidate, int enabledTasks) {
		if (candidate == null) {
			return "0 / " + enabledTasks;
		}
		List<CandidateElectionTaskProgress> progressRows = tryGetTaskProgressRows(candidate);
		if (progressRows.isEmpty()) {
			return estimateTaskProgressLabel(candidate.getStatus(), enabledTasks);
		}

		int completed = 0;
		for (CandidateElectionTaskProgress progress : progressRows) {
			if (progress == null || progress.getElectionTask() == null || progress.getElectionTask().getTaskKey() == null) {
				continue;
			}
			ElectionTaskKey key = progress.getElectionTask().getTaskKey();
			if (!isTaskPublic(key) || !isSummaryTaskKey(key)) {
				continue;
			}
			if (progress.getStatus() == CandidateElectionTaskStatus.COMPLETED) {
				completed++;
			}
		}
		return completed + " / " + enabledTasks;
	}

	private List<CandidateElectionTaskProgress> tryGetTaskProgressRows(Candidate candidate) {
		if (candidate == null) {
			return Collections.emptyList();
		}
		try {
			List<CandidateElectionTaskProgress> rows = candidate.getTaskProgress();
			if (rows == null) {
				return Collections.emptyList();
			}
			// Forces initialization when available; if detached/lazy this may throw.
			rows.size();
			return rows;
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private String estimateTaskProgressLabel(CandidateStatus status, int enabledTasks) {
		if (enabledTasks <= 0) {
			return "0 / 0";
		}
		if (status == CandidateStatus.CONFIRMED_AND_PUBLISHED || status == CandidateStatus.COMPLETE) {
			return enabledTasks + " / " + enabledTasks;
		}
		if (status == CandidateStatus.PRECOMPLETE) {
			int estimated = Math.max(1, enabledTasks - 1);
			return estimated + " / " + enabledTasks;
		}
		return "0 / " + enabledTasks;
	}

	private boolean isSummaryTaskKey(ElectionTaskKey key) {
		for (ElectionTaskKey row : LANDING_INCOMPLETE_TASKS) {
			if (row == key) {
				return true;
			}
		}
		return false;
	}

	private String resolveNominationStatusLabel(NominationStatus nominationStatus) {
		if (nominationStatus == null) {
			return "-";
		}
		switch (nominationStatus) {
		case ACCEPTED_BY_CANDIDATE:
			return getString("publicElectionNominationStatusAccepted");
		case PROPOSED:
			return getString("publicElectionNominationStatusPending");
		case REJECTED_BY_CANDIDATE:
			return getString("publicElectionNominationStatusRejectedByCandidate");
		case INVALID:
			return getString("publicElectionNominationStatusInvalid");
		case APPROVED:
			return getString("publicElectionNominationStatusApproved");
		default:
			return nominationStatus.name();
		}
	}

	private String resolveNominationRowClass(CandidateStatus status, NominationStatus nominationStatus) {
		if (isCandidateConfirmedAndPublished(status)) {
			return "table-success";
		}
		if (isNominationRejected(status, nominationStatus)) {
			return "table-danger";
		}
		return "table-warning";
	}

	private int resolveNominationSortRank(CandidateStatus status, NominationStatus nominationStatus) {
		if (status == CandidateStatus.CONFIRMED_AND_PUBLISHED) {
			return 0;
		}
		if (isNominationRejected(status, nominationStatus)) {
			return 1;
		}
		if (status == CandidateStatus.COMPLETE) {
			return 2;
		}
		if (nominationStatus == NominationStatus.PROPOSED
				|| nominationStatus == NominationStatus.REJECTED_BY_CANDIDATE) {
			return 4;
		}
		return 3;
	}

	private boolean isCandidateConfirmedAndPublished(CandidateStatus status) {
		return status == CandidateStatus.CONFIRMED_AND_PUBLISHED;
	}

	private boolean isNominationRejected(CandidateStatus status, NominationStatus nominationStatus) {
		return status == CandidateStatus.REJECTED || nominationStatus == NominationStatus.INVALID;
	}

	private StatusBadge resolveCandidateStatusBadge(CandidateStatus status, NominationStatus nominationStatus) {
		if (isCandidateConfirmedAndPublished(status)) {
			return new StatusBadge(getString("publicElectionCandidateStatusConfirmed"), BootstrapCssClasses.BADGE_TEXT_BG_SUCCESS);
		}
		if (status == CandidateStatus.COMPLETE) {
			return new StatusBadge(getString("publicElectionCandidateStatusCompleted"), BootstrapCssClasses.BADGE_TEXT_BG_WARNING);
		}
		if (isNominationRejected(status, nominationStatus)) {
			return new StatusBadge(getString("publicElectionCandidateStatusRejected"), "badge text-bg-danger");
		}
		if (nominationStatus == NominationStatus.PROPOSED || nominationStatus == NominationStatus.REJECTED_BY_CANDIDATE) {
			return new StatusBadge(getString("publicElectionCandidateStatusNotStarted"), BootstrapCssClasses.BADGE_TEXT_BG_WARNING);
		}
		return new StatusBadge(getString("publicElectionCandidateStatusIncomplete"), BootstrapCssClasses.BADGE_TEXT_BG_WARNING);
	}

	private Component buildCandidatePicture(String id, Candidate candidate, int size, String cssClass) {
		String effectiveCssClass = hasText(cssClass) ? cssClass + " candidate-photo-sepia" : "candidate-photo-sepia";
		String fixedSquareStyle = "width:" + size + "px;height:" + size + "px;object-fit:cover;object-position:center;";
		if (candidate != null && candidate.getPictureInfo() != null && candidate.getPictureInfo().length > 0) {
			String extension = hasText(candidate.getPictureExtension()) ? candidate.getPictureExtension() : "jpg";
			NonCachingImage image = new NonCachingImage(id, new ImageResource(candidate.getPictureInfo(), extension));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  effectiveCssClass));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_WIDTH, String.valueOf(size)));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_HEIGHT, String.valueOf(size)));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_STYLE, fixedSquareStyle));
			return image;
		}
		ContextImage image = new ContextImage(id, "image/default_candidate_photo.jpg");
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  effectiveCssClass));
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_WIDTH, String.valueOf(size)));
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_HEIGHT, String.valueOf(size)));
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_STYLE, fixedSquareStyle));
		return image;
	}

	private String resolveLocalizedText(String spanish, String english, String portuguese) {
		LanguageCode languageCode = SecurityUtils.getLanguageCode();
		switch (languageCode) {
		case EN:
			if (hasText(english)) {
				return english;
			}
			if (hasText(spanish)) {
				return spanish;
			}
			return hasText(portuguese) ? portuguese : null;
		case PT:
			if (hasText(portuguese)) {
				return portuguese;
			}
			if (hasText(spanish)) {
				return spanish;
			}
			return hasText(english) ? english : null;
		case SP:
		default:
			if (hasText(spanish)) {
				return spanish;
			}
			if (hasText(english)) {
				return english;
			}
			return hasText(portuguese) ? portuguese : null;
		}
	}

	private String resolveCountryLabel(String countryCode) {
		if (!hasText(countryCode)) {
			return "-";
		}
		String normalized = countryCode.trim().toUpperCase(Locale.ROOT);
		return COUNTRY_UTILS.getDisplayLabel(normalized, getLocale(), true);
	}

	private void loadElectionAuditorResult(long electionId) {
		if (electionId <= 0L) {
			electionAuditorResult = null;
			return;
		}
		electionAuditorResult = AppContext.getInstance().getManagerBeanRemote().getElectionAuditorResult(electionId);
	}

	private boolean hasBinary(byte[] value) {
		return value != null && value.length > 0;
	}

	private String resolveOrganizationName(UserVoter userVoter) {
		String orgName = userVoter != null ? normalizeOrganizationFilter(userVoter.getOrgName()) : "";
		return hasText(orgName) ? orgName : getString("publicElectionRollOrganizationUnavailable");
	}

	private String maskRepresentative(String fullName) {
		if (!hasText(fullName)) {
			return "********";
		}
		String[] parts = fullName.trim().split("\\s+");
		StringBuilder sb = new StringBuilder();
		int maxInitials = Math.min(2, parts.length);
		for (int i = 0; i < maxInitials; i++) {
			if (!hasText(parts[i])) {
				continue;
			}
			sb.append(Character.toUpperCase(parts[i].charAt(0))).append(". ");
		}
		sb.append("********");
		return sb.toString().trim();
	}

	private String normalizeCountry(String country) {
		if (!hasText(country)) {
			return "-";
		}
		return country.trim().toUpperCase(Locale.ROOT);
	}

	private String normalizeAction(String action) {
		if (!hasText(action)) {
			return "";
		}
		return action.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeCountryFilter(String value) {
		return hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "";
	}

	private String normalizeOrganizationFilter(String value) {
		return hasText(value) ? value.trim() : "";
	}

	private String formatDateTime(Date date) {
		if (date == null) {
			return "-";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy · HH:mm", resolveUiLocale());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date) + " UTC";
	}

	private String formatDateTimeServer(Date date) {
		if (date == null) {
			return "-";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.ROOT);
		return sdf.format(date);
	}

	private String formatDateTimeUtc(Date date) {
		if (date == null) {
			return "-";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.ROOT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

	private String formatDateOnly(Date date) {
		if (date == null) {
			return "-";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", resolveUiLocale());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

	private String formatLong(long value) {
		return NumberFormat.getNumberInstance(resolveUiLocale()).format(value);
	}

	private String formatPercentage(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return "0%";
		}
		return String.format(resolveUiLocale(), "%.1f%%", value);
	}

	private Locale resolveUiLocale() {
		Locale locale = getLocale();
		if (locale != null) {
			return locale;
		}
		Locale securityLocale = SecurityUtils.getLocale();
		if (securityLocale != null) {
			return securityLocale;
		}
		return new Locale("es", "ES");
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private List<Long> resolveQuestionTargetCandidateIds() {
		List<Long> candidateIds = new ArrayList<Long>();
		if (askSameQuestionToAllCandidates) {
			for (Candidate candidate : publishedCandidates) {
				if (isPublicCandidate(candidate)) {
					candidateIds.add(Long.valueOf(candidate.getCandidateId()));
				}
			}
			return candidateIds;
		}
		if (isPublicCandidate(selectedCandidate)) {
			candidateIds.add(Long.valueOf(selectedCandidate.getCandidateId()));
		}
		return candidateIds;
	}

	private void addIfPresent(List<String> rows, String value) {
		if (hasText(value)) {
			rows.add(value);
		}
	}

	private boolean containsIgnoreCase(String value, String query) {
		if (!hasText(value) || !hasText(query)) {
			return false;
		}
		return value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
	}

	private boolean isPublicCandidate(Candidate candidate) {
		return candidate != null && !candidate.isAbstention() && candidate.getStatus() == CandidateStatus.CONFIRMED_AND_PUBLISHED;
	}

	private boolean isStatutoryElection() {
		return election != null && election.getCategory() == ElectionCategory.STATUTORY;
	}

	private boolean hasPublishedAbstentionCandidate() {
		for (Candidate candidate : candidateById.values()) {
			if (candidate != null && candidate.isAbstention() && candidate.getStatus() == CandidateStatus.CONFIRMED_AND_PUBLISHED) {
				return true;
			}
		}
		return false;
	}

	private <T> List<T> safeList(List<T> values) {
		return values != null ? values : Collections.<T>emptyList();
	}

	private enum PageMode {
		LANDING,
		NOMINATE,
		CANDIDATE,
		ROLL,
		INVALID_CANDIDATE
	}

	private enum ResultPublicationStage {
		NOT_PUBLISHED,
		PROVISIONAL,
		CE_AUDIT,
		VOTER_AUDIT,
		OFFICIAL_NO_CLAIMS,
		OFFICIAL_WITH_CLAIMS
	}

	private static class ResultRowView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final Long candidateId;
		private final String candidateName;
		private final long votes;
		private final double percentage;
		private final boolean winner;
		private final Locale locale;

		ResultRowView(Long candidateId, String candidateName, long votes, double percentage, boolean winner, Locale locale) {
			this.candidateId = candidateId;
			this.candidateName = candidateName;
			this.votes = votes;
			this.percentage = percentage;
			this.winner = winner;
			this.locale = locale != null ? locale : Locale.ROOT;
		}

		public Long getCandidateId() {
			return candidateId;
		}

		public String getCandidateName() {
			return candidateName;
		}

		public long getVotes() {
			return votes;
		}

		public boolean isWinner() {
			return winner;
		}

		public String getVotesLabel() {
			return NumberFormat.getNumberInstance(locale).format(votes);
		}

		public String getPercentageLabel() {
			return String.format(locale, "%.1f%%", percentage);
		}
	}

	private static class ResultPublicationStageView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String title;
		private final String schedule;
		private final boolean current;

		ResultPublicationStageView(String title, String schedule, boolean current) {
			this.title = title;
			this.schedule = schedule;
			this.current = current;
		}

		public String getTitle() {
			return title;
		}

		public String getSchedule() {
			return schedule;
		}

		public boolean isCurrent() {
			return current;
		}
	}

	private static class NominationSummaryRow implements Serializable {
		private static final long serialVersionUID = 1L;
		private final boolean publicCandidateLink;
		private final long candidateId;
		private final String candidateName;
		private final String countryLabel;
		private final String candidateStatusLabel;
		private final String candidateStatusCssClass;
		private final String tasksLabel;
		private final String nominationStatusLabel;
		private final String rowCssClass;
		private final int sortRank;

		NominationSummaryRow(boolean publicCandidateLink, long candidateId, String candidateName, String countryLabel,
				String candidateStatusLabel, String candidateStatusCssClass, String tasksLabel,
				String nominationStatusLabel, String rowCssClass, int sortRank) {
			this.publicCandidateLink = publicCandidateLink;
			this.candidateId = candidateId;
			this.candidateName = candidateName;
			this.countryLabel = countryLabel;
			this.candidateStatusLabel = candidateStatusLabel;
			this.candidateStatusCssClass = candidateStatusCssClass;
			this.tasksLabel = tasksLabel;
			this.nominationStatusLabel = nominationStatusLabel;
			this.rowCssClass = rowCssClass;
			this.sortRank = sortRank;
		}

		public boolean isPublicCandidateLink() {
			return publicCandidateLink;
		}

		public long getCandidateId() {
			return candidateId;
		}

		public String getCandidateName() {
			return candidateName;
		}

		public String getCountryLabel() {
			return countryLabel;
		}

		public String getCandidateStatusLabel() {
			return candidateStatusLabel;
		}

		public String getCandidateStatusCssClass() {
			return candidateStatusCssClass;
		}

		public String getTasksLabel() {
			return tasksLabel;
		}

		public String getNominationStatusLabel() {
			return nominationStatusLabel;
		}

		public String getRowCssClass() {
			return rowCssClass;
		}

		public int getSortRank() {
			return sortRank;
		}
	}

	private static class DevKeyValueRow implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String key;
		private final String value;
		private final String detail;

		DevKeyValueRow(String key, String value, String detail) {
			this.key = key;
			this.value = value;
			this.detail = detail;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		public String getDetail() {
			return detail;
		}
	}

	private static class DevTaskRow implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String taskKey;
		private final String configuredLabel;
		private final String publicableLabel;

		DevTaskRow(String taskKey, String configuredLabel, String publicableLabel) {
			this.taskKey = taskKey;
			this.configuredLabel = configuredLabel;
			this.publicableLabel = publicableLabel;
		}

		public String getTaskKey() {
			return taskKey;
		}

		public String getConfiguredLabel() {
			return configuredLabel;
		}

		public String getPublicableLabel() {
			return publicableLabel;
		}
	}

	private static class DevCalendarRow implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String calendarKey;
		private final String configuredLabel;
		private final String publicableLabel;
		private final String startLabel;
		private final String endLabel;
		private final String reachedLabel;
		private final String duringLabel;

		DevCalendarRow(String calendarKey, String configuredLabel, String publicableLabel, String startLabel,
				String endLabel, String reachedLabel, String duringLabel) {
			this.calendarKey = calendarKey;
			this.configuredLabel = configuredLabel;
			this.publicableLabel = publicableLabel;
			this.startLabel = startLabel;
			this.endLabel = endLabel;
			this.reachedLabel = reachedLabel;
			this.duringLabel = duringLabel;
		}

		public String getCalendarKey() {
			return calendarKey;
		}

		public String getConfiguredLabel() {
			return configuredLabel;
		}

		public String getPublicableLabel() {
			return publicableLabel;
		}

		public String getStartLabel() {
			return startLabel;
		}

		public String getEndLabel() {
			return endLabel;
		}

		public String getReachedLabel() {
			return reachedLabel;
		}

		public String getDuringLabel() {
			return duringLabel;
		}
	}

	private static class CountryDisplayRow implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String countryCode;
		private final String countryLabel;
		private final String flagImagePath;

		CountryDisplayRow(String countryCode, String countryLabel, String flagImagePath) {
			this.countryCode = countryCode;
			this.countryLabel = countryLabel;
			this.flagImagePath = flagImagePath;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public String getCountryLabel() {
			return countryLabel;
		}

		public String getFlagImagePath() {
			return flagImagePath;
		}
	}

	private static class StatutoryAnswerView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String questionLabel;
		private final String answer;
		private final String dateLabel;

		StatutoryAnswerView(String questionLabel, String answer, String dateLabel) {
			this.questionLabel = questionLabel;
			this.answer = answer;
			this.dateLabel = dateLabel;
		}

		public String getQuestionLabel() {
			return questionLabel;
		}

		public String getAnswer() {
			return answer;
		}

		public String getDateLabel() {
			return dateLabel;
		}
	}

	private static class CommunityQuestionView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String questionDateLabel;
		private final String answerDateLabel;
		private final String question;
		private final String answer;
		private final String askedByInitials;
		private final long sortQuestionId;

		CommunityQuestionView(String questionDateLabel, String answerDateLabel, String question, String answer, String askedByInitials, long sortQuestionId) {
			this.questionDateLabel = questionDateLabel;
			this.answerDateLabel = answerDateLabel;
			this.question = question;
			this.answer = answer;
			this.askedByInitials = askedByInitials;
			this.sortQuestionId = sortQuestionId;
		}

		public String getQuestionDateLabel() {
			return questionDateLabel;
		}

		public String getAnswerDateLabel() {
			return answerDateLabel;
		}

		public String getQuestion() {
			return question;
		}

		public String getAnswer() {
			return answer;
		}

		public String getAskedByInitials() {
			return askedByInitials;
		}

		public long getSortQuestionId() {
			return sortQuestionId;
		}
	}

	private static class RollEntryView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String countryCode;
		private final String organizationName;
		private final String maskedRepresentative;

		RollEntryView(String countryCode, String organizationName, String maskedRepresentative) {
			this.countryCode = countryCode;
			this.organizationName = organizationName;
			this.maskedRepresentative = maskedRepresentative;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public String getOrganizationName() {
			return organizationName;
		}

		public String getMaskedRepresentative() {
			return maskedRepresentative;
		}
	}

	private static class StatusBadge implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String text;
		private final String cssClass;

		StatusBadge(String text, String cssClass) {
			this.text = text;
			this.cssClass = cssClass;
		}
	}
}
