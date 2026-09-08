package net.lacnic.elections.adminweb.ui.token.page;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.ui.components.WinnerPopoverBadgePanel;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.token.CandidateBiographyUtils;
import net.lacnic.elections.adminweb.ui.token.PublicCalendarTimelinePanel;
import net.lacnic.elections.adminweb.ui.token.PublicCalendarTimelinePanel.TimelineEntryView;
import net.lacnic.elections.adminweb.ui.token.PublicNominationFormPanel;
import net.lacnic.elections.adminweb.ui.token.TokenResourceKeys;
import net.lacnic.elections.adminweb.ui.token.panel.PublicElectionCacheDebugFooterPanel;
import net.lacnic.elections.adminweb.ui.token.panel.PublicElectionRecoverLinkPanel;
import net.lacnic.elections.adminweb.ui.token.panel.PublicElectionSnapshotPanel;
import net.lacnic.elections.adminweb.ui.token.page.PublicAccessDeniedPage;
import net.lacnic.elections.adminweb.wicket.util.BootstrapCssClasses;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.RestrictedCountriesMessageResolver;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.domain.ElectionLinkRecoveryMode;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCoreSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionOfficialResultSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionPhotoSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionRollSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionSnapshotMetadata;
import net.lacnic.elections.publicelection.PublicElectionStageCodes;
import net.lacnic.elections.publicelection.PublicElectionSectionKeys;
import net.lacnic.elections.utils.CountryUtils;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class PublicElectionPageV2 extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;
	private static final int ROLL_PAGE_SIZE = 50;
	private static final int ASK_QUESTION_NAME_MAX_LENGTH = 500;
	private static final int ASK_QUESTION_EMAIL_MAX_LENGTH = 320;
	private static final int ASK_QUESTION_TEXT_MAX_LENGTH = 1000;
	private static final String CANDIDATE_PROFILE_LINK_MARKUP_ID_PREFIX = "CandidateID_";
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();
	private static final String DISPLAY_MODE_VISIBLE = "VISIBLE";
	private static final String DISPLAY_MODE_MASKED = "MASKED";
	private static final String DISPLAY_MODE_WAITING_REVIEW = "WAITING_REVIEW";
	private static final String DISPLAY_MODE_WAITING_CANDIDATE = "WAITING_CANDIDATE";
	private static final String DISPLAY_MODE_WAITING_LACNIC = "WAITING_LACNIC";
	private static final String CANDIDATE_STATUS_COMPLETE = "COMPLETE";
	private static final String CANDIDATE_STATUS_CONFIRMED_AND_PUBLISHED = "CONFIRMED_AND_PUBLISHED";
	private static final String CANDIDATE_STATUS_PRECOMPLETE = "PRECOMPLETE";
	private static final String CANDIDATE_STATUS_REJECTED = "REJECTED";

	private Election election;
	private PublicElectionCoreSnapshot coreSnapshot;
	private PublicElectionRollSnapshot rollSnapshot;
	private PublicElectionPhotoSnapshot photoSnapshot;
	private PublicElectionOfficialResultSnapshot officialResultSnapshot;

	private final Map<Long, PublicElectionCoreSnapshot.CandidateData> candidateById = new HashMap<>();
	private final Map<Long, PublicElectionPhotoSnapshot.CandidatePhotoData> photoByCandidateId = new HashMap<>();
	private final List<RollEntryView> rollEntries = new ArrayList<>();
	private final List<RollEntryView> rollPageRows = new ArrayList<>();
	private final List<String> rollCountryOptions = new ArrayList<>();
	private final Date now = new Date();
	private final boolean cacheDebugModeEnabled;

	private PageMode pageMode = PageMode.LANDING;
	private PublicElectionCoreSnapshot.CandidateData selectedCandidate;
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

	public PublicElectionPageV2() {
		this(new PageParameters());
	}

	public PublicElectionPageV2(PageParameters params) {
		super(params);
		cacheDebugModeEnabled = parseCacheDebugMode(params != null ? params.get(PublicElectionPageParameters.DEBUG).toString("") : "");

		if (election == null) {
			addEmptyPageScaffold();
			return;
		}

		loadSnapshots();
		if (coreSnapshot == null) {
			addEmptyPageScaffold();
			return;
		}

		resolvePageMode(params);
		if (pageMode == PageMode.INVALID_CANDIDATE) {
			setResponsePage(Error404.class);
			addEmptyPageScaffold();
			return;
		}

		if (pageMode == PageMode.NOMINATE && !isPublicNominationVisible()) {
			setResponsePage(PublicElectionPageV2.class, baseLandingParameters());
			addEmptyPageScaffold();
			return;
		}

		if (pageMode == PageMode.CANDIDATE && !isSectionVisible(PublicElectionSectionKeys.CANDIDATE_PROFILE_ROOT)) {
			setResponsePage(PublicElectionPageV2.class, baseLandingParameters());
			addEmptyPageScaffold();
			return;
		}

		if (pageMode == PageMode.ROLL && !isSectionVisible(PublicElectionSectionKeys.LANDING_ROLL)) {
			setResponsePage(PublicElectionPageV2.class, baseLandingParameters());
			addEmptyPageScaffold();
			return;
		}

		if (pageMode == PageMode.ROLL) {
			loadRollCollections();
			loadRollPageData(params);
		}

		add(buildSnapshotPanel());
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
		setWhereAmI(getString("publicElectionWhereAmI") + " V2");
		setContextClass(Election.class.getName());
		setContextData("electionId: " + election.getElectionId() + "\n"
				+ "titleSpanish: " + election.getTitleSpanish() + "\n"
				+ "publicElectionLinkAvailable: " + election.isPublicElectionLinkAvailable() + "\n"
				+ "snapshotMode: true");
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

	private void loadSnapshots() {
		Long electionId = election != null ? election.getElectionId() : null;
		if (electionId == null || electionId.longValue() <= 0L) {
			return;
		}
		coreSnapshot = AppContext.getInstance().getMonitorBeanRemote().getPublicElectionCoreSnapshot(electionId);
		rollSnapshot = AppContext.getInstance().getMonitorBeanRemote().getPublicElectionRollSnapshot(electionId);
		photoSnapshot = AppContext.getInstance().getMonitorBeanRemote().getPublicElectionPhotoSnapshot(electionId);
		officialResultSnapshot = AppContext.getInstance().getMonitorBeanRemote().getPublicElectionOfficialResultSnapshot(electionId);
		indexCandidates();
		indexPhotos();
	}

	private void indexCandidates() {
		candidateById.clear();
		if (coreSnapshot == null) {
			return;
		}
		for (PublicElectionCoreSnapshot.CandidateData candidate : safeList(coreSnapshot.getCandidates())) {
			if (candidate == null || candidate.getCandidateId() == null) {
				continue;
			}
			candidateById.put(candidate.getCandidateId(), candidate);
		}
	}

	private void indexPhotos() {
		photoByCandidateId.clear();
		if (photoSnapshot == null) {
			return;
		}
		for (PublicElectionPhotoSnapshot.CandidatePhotoData photoData : safeList(photoSnapshot.getCandidates())) {
			if (photoData == null || photoData.getCandidateId() == null) {
				continue;
			}
			photoByCandidateId.put(photoData.getCandidateId(), photoData);
		}
	}

	private void resolvePageMode(PageParameters params) {
		String action = normalizeAction(params.get(PublicElectionPageParameters.ACTION).toString(""));
		if (!hasText(action) || "landing".equals(action)) {
			pageMode = PageMode.LANDING;
			return;
		}
		if ("candidate".equals(action)) {
			long candidateId = params.get(PublicElectionPageParameters.CANDIDATE_ID).toLong(-1L);
			selectedCandidate = candidateById.get(Long.valueOf(candidateId));
			pageMode = selectedCandidate != null ? PageMode.CANDIDATE : PageMode.INVALID_CANDIDATE;
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

	private void loadRollCollections() {
		rollEntries.clear();
		for (PublicElectionRollSnapshot.RollEntryData row : safeList(rollSnapshot != null ? rollSnapshot.getRows() : null)) {
			if (row == null) {
				continue;
			}
			rollEntries.add(new RollEntryView(
					valueOrDash(row.getCountryCode()),
					valueOrDash(row.getOrganizationName()),
					valueOrDash(row.getRepresentativeMask())));
		}
		rollCountryOptions.clear();
		rollCountryOptions.addAll(buildRollCountryOptions(rollEntries));
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

	private Component buildSnapshotPanel() {
		return new PublicElectionSnapshotPanel(
				"contentPanel",
				new FeedbackPanel("feedbackPanel"),
				buildSnapshotStatusPanel(),
				buildLandingContainer(),
				buildNominateContainer(),
				buildCandidateContainer(),
				buildRollContainer());
	}

	private WebMarkupContainer buildSnapshotStatusPanel() {
		WebMarkupContainer container = new WebMarkupContainer("snapshotStatus");
		container.setOutputMarkupPlaceholderTag(true);
		PublicElectionSnapshotMetadata metadata = coreSnapshot != null ? coreSnapshot.getMetadata() : null;
		container.setVisible(metadata != null);
		container.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveSnapshotStatusCssClass(metadata)));

		Label freshnessBadge = new Label("snapshotFreshnessBadge", resolveSnapshotFreshnessLabel(metadata));
		freshnessBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveSnapshotFreshnessBadgeCssClass(metadata)));
		container.add(freshnessBadge);
		Label snapshotContext = new Label("snapshotContext", buildSnapshotContextLabel());
		snapshotContext.setVisible(hasText(snapshotContext.getDefaultModelObjectAsString()));
		container.add(snapshotContext);
		container.add(new Label("snapshotLastUpdated", formatDateTime(metadata != null ? metadata.getLastUpdatedUtc() : null)));
		container.add(new Label("snapshotNextRefresh", formatDateTime(metadata != null ? metadata.getNextRefreshUtc() : null)));
		container.add(new Label("snapshotNextBusinessTransition", formatDateTime(metadata != null ? metadata.getNextBusinessTransitionUtc() : null)));
		container.add(new Label("snapshotRefreshStatus", resolveSnapshotRefreshStatusLabel(metadata != null ? metadata.getRefreshStatus() : null)));
		Label refreshMessage = new Label("snapshotRefreshMessage", valueOrDash(metadata != null ? metadata.getRefreshMessage() : null));
		refreshMessage.setVisible(metadata != null && hasText(metadata.getRefreshMessage()));
		container.add(refreshMessage);
		return container;
	}

	private String buildSnapshotContextLabel() {
		Long electionId = election != null ? election.getElectionId() : null;
		String token = getToken();
		if (electionId == null && !hasText(token)) {
			return null;
		}
		return new StringResourceModel("publicElectionSnapshotStatusContext", this, null)
				.setParameters(valueOrDash(electionId != null ? String.valueOf(electionId) : null), valueOrDash(token))
				.getString();
	}

	private WebMarkupContainer buildLandingContainer() {
		WebMarkupContainer container = new WebMarkupContainer("landingContainer");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(pageMode == PageMode.LANDING);

		container.add(buildOfficialResultsSection("officialResultsSection"));
		container.add(buildCandidateCallBannerSection("candidateCallBannerSection"));

		WebMarkupContainer resultSection = new WebMarkupContainer("resultSection");
		resultSection.setOutputMarkupPlaceholderTag(true);
		boolean resultVisible = isSectionVisible(PublicElectionSectionKeys.LANDING_RESULTS) && coreSnapshot.getResultSummary() != null;
		resultSection.setVisible(resultVisible);
		resultSection.add(new Label("resultPublicationStatus", resolveResultPublicationStatusText(coreSnapshot != null && coreSnapshot.getResultSummary() != null ? coreSnapshot.getResultSummary().getPublicationStageCode() : null)));
		WebMarkupContainer resultHelpButton = new WebMarkupContainer("resultPublicationHelpButton");
		resultHelpButton.add(AttributeModifier.replace("data-bs-content", resolveResultPublicationHelpText(coreSnapshot != null && coreSnapshot.getResultSummary() != null ? coreSnapshot.getResultSummary().getPublicationStageCode() : null)));
		resultSection.add(resultHelpButton);
		resultSection.add(buildCacheDebugFooter("resultStatusDebugFooter", buildResultStatusDebugPayload()));
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
				Long candidateId = row != null ? row.getCandidateId() : null;
				PublicElectionCoreSnapshot.CandidateData candidate = candidateId != null ? candidateById.get(candidateId) : null;
				CountryDisplayRow primaryCountryRow = resolvePrimaryCountryRow(candidate);
				item.add(buildWinnerListPicture("winnerPicture", candidateId != null ? photoByCandidateId.get(candidateId) : null));
				item.add(new Label("winnerName", valueOrDash(row != null ? row.getCandidateName() : null)));
				item.add(new WinnerPopoverBadgePanel("winnerWinnerBadge", row != null && row.isWinner()));
				item.add(new Label("winnerVotes", formatWinnerVotesLabel(row != null ? row.getVotesLabel() : "0")));
				item.add(new Label("winnerPercentage", row != null ? row.getPercentageLabel() : "0%"));
				boolean profileVisible = canLinkToCandidateProfile(candidateId);
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
				BookmarkablePageLink<Void> winnerProfileLink = new BookmarkablePageLink<Void>(
						"winnerProfileLink",
						PublicElectionPageV2.class,
						buildCandidateParameters(candidateId != null ? candidateId.longValue() : 0L));
				winnerProfileLink.setVisible(profileVisible);
				String linkedinUrl = candidate != null ? candidate.getLinkedinUrl() : null;
				ExternalLink winnerLinkedinLink = new ExternalLink("winnerLinkedinLink", hasText(linkedinUrl) ? linkedinUrl : "#");
				winnerLinkedinLink.setVisible(hasText(linkedinUrl));
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
		long totalVotes = safeLong(coreSnapshot != null && coreSnapshot.getResultSummary() != null ? coreSnapshot.getResultSummary().getTotalVotes() : null);
		resultSection.add(new Label("resultTotalVotes", formatLong(totalVotes)));
		resultSection.add(new Label("resultTotalPercentage", totalVotes <= 0L ? "0%" : "100%"));
		resultSection.add(buildCacheDebugFooter("resultTableDebugFooter", buildResultTableDebugPayload()));
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
		resultSection.add(buildCacheDebugFooter("resultStagesDebugFooter", buildResultStagesDebugPayload()));
		resultSection.add(new Label("summaryEnabledVoters", formatLong(safeLong(coreSnapshot != null && coreSnapshot.getResultSummary() != null ? coreSnapshot.getResultSummary().getEnabledVoters() : null))));
		resultSection.add(new Label("summaryOrganizationsVoted", formatLong(safeLong(coreSnapshot != null && coreSnapshot.getResultSummary() != null ? coreSnapshot.getResultSummary().getOrganizationsVoted() : null))));
		resultSection.add(new Label("summaryParticipation", formatPercentage(safeDouble(coreSnapshot != null && coreSnapshot.getResultSummary() != null ? coreSnapshot.getResultSummary().getParticipationPercentage() : null))));
		resultSection.add(buildCacheDebugFooter("resultSummaryDebugFooter", buildResultSummaryDebugPayload()));
		String contactEmail = coreSnapshot != null && coreSnapshot.getElection() != null ? coreSnapshot.getElection().getResultContactEmail() : null;
		resultSection.add(new ExternalLink("resultContactEmailLink", hasText(contactEmail) ? "mailto:" + contactEmail : "#", valueOrDash(contactEmail)));
		resultSection.add(buildCacheDebugFooter("resultContactDebugFooter", buildResultContactDebugPayload()));
		resultSection.add(new Label("resultFootnote", getString("publicElectionResultFootnote")));
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
		boolean candidatesVisible = isSectionVisible(PublicElectionSectionKeys.LANDING_CANDIDATES);
		candidatesSection.setVisible(candidatesVisible);
		List<PublicElectionCoreSnapshot.CandidateData> candidates = buildLandingCandidatesForDisplay();
		boolean hasPublishedCandidates = !candidates.isEmpty();
		WebMarkupContainer candidateCardsGrid = new WebMarkupContainer("candidateCardsGrid");
		candidateCardsGrid.setOutputMarkupPlaceholderTag(true);
		candidateCardsGrid.setVisible(hasPublishedCandidates);
		candidateCardsGrid.add(new ListView<PublicElectionCoreSnapshot.CandidateData>("candidateCards", candidates) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<PublicElectionCoreSnapshot.CandidateData> item) {
				PublicElectionCoreSnapshot.CandidateData candidate = item.getModelObject();
				CountryDisplayRow primaryCountryRow = resolvePrimaryCountryRow(candidate);
				item.add(buildCandidatePicture("candidatePicture", candidate != null ? photoByCandidateId.get(candidate.getCandidateId()) : null, 96, "rounded-circle"));
				item.add(new Label("candidateName", valueOrDash(candidate != null ? candidate.getName() : null)));
				item.add(new WinnerPopoverBadgePanel("candidateWinnerBadge", candidate != null && Boolean.TRUE.equals(candidate.getWinner())));
				item.add(new Label("candidateOrganizations", resolveCandidateOrganizationsLabel(candidate)));
				item.add(buildCountryFlagImage("candidateCountryFlag", primaryCountryRow, 24, 16));
				item.add(new Label("candidateCountry", valueOrDash(primaryCountryRow.getCountryLabel())));
				item.add(new Label("candidateBioSnippet", resolveCandidateBioSnippet(candidate)));
				long candidateId = candidate != null && candidate.getCandidateId() != null ? candidate.getCandidateId().longValue() : 0L;
				BookmarkablePageLink<Void> candidateLink = new BookmarkablePageLink<Void>("candidateLink", PublicElectionPageV2.class,
						buildCandidateParameters(candidateId));
				candidateLink.setOutputMarkupId(true);
				candidateLink.setMarkupId(CANDIDATE_PROFILE_LINK_MARKUP_ID_PREFIX + candidateId);
				item.add(candidateLink);
				item.add(buildCacheDebugFooter("candidateCardDebugFooter", buildCandidateCardDebugPayload(candidate)));
			}
		});
		candidatesSection.add(candidateCardsGrid);
		WebMarkupContainer candidateCardsEmpty = new WebMarkupContainer("candidateCardsEmpty");
		candidateCardsEmpty.setOutputMarkupPlaceholderTag(true);
		candidateCardsEmpty.setVisible(!hasPublishedCandidates);
		candidatesSection.add(candidateCardsEmpty);
		WebMarkupContainer abstentionNotice = new WebMarkupContainer("abstentionNotice");
		abstentionNotice.setVisible(hasPublishedCandidates && coreSnapshot != null && coreSnapshot.getElection() != null && Boolean.TRUE.equals(coreSnapshot.getElection().getPublishedAbstentionCandidate()));
		candidatesSection.add(abstentionNotice);
		WebMarkupContainer randomOrderNotice = new WebMarkupContainer("randomOrderNotice");
		randomOrderNotice.setVisible(hasPublishedCandidates && coreSnapshot != null && coreSnapshot.getElection() != null && Boolean.TRUE.equals(coreSnapshot.getElection().getRandomOrderCandidates()));
		candidatesSection.add(randomOrderNotice);
		candidatesSection.add(buildCacheDebugFooter("candidatesCardDebugFooter", buildCandidatesDebugPayload(candidates)));
		container.add(candidatesSection);

		WebMarkupContainer calendarSection = new WebMarkupContainer("calendarSection");
		calendarSection.setOutputMarkupPlaceholderTag(true);
		boolean calendarVisible = isSectionVisible(PublicElectionSectionKeys.LANDING_CALENDAR);
		calendarSection.setVisible(calendarVisible);
		calendarSection.add(new PublicCalendarTimelinePanel("timelinePanel", buildLandingTimelineRows(), true));
		calendarSection.add(buildCacheDebugFooter("calendarCardDebugFooter", buildCalendarDebugPayload()));
		container.add(calendarSection);

		WebMarkupContainer recoverLinksSection = new WebMarkupContainer("recoverLinksSection");
		recoverLinksSection.setOutputMarkupPlaceholderTag(true);
		boolean recoverLinksVisible = isSectionVisible(PublicElectionSectionKeys.LANDING_PARTICIPATION_LINK_RECOVERY)
				&& coreSnapshot != null && coreSnapshot.getRecovery() != null && Boolean.TRUE.equals(coreSnapshot.getRecovery().getVisible());
		recoverLinksSection.setVisible(recoverLinksVisible);
		recoverLinksSection.add(new Label("recoverLinkCardTitle", new ResourceModel("recoverLinkTitleParticipation")));
		WebMarkupContainer recoverLinkModeHelp = new WebMarkupContainer("recoverLinkModeHelp");
		recoverLinkModeHelp.setOutputMarkupPlaceholderTag(true);
		recoverLinkModeHelp.setVisible(isOnlyBrRecoveryMode());
		recoverLinkModeHelp.add(AttributeModifier.replace("title", new ResourceModel("recoverLinkModeHelpTitle")));
		recoverLinkModeHelp.add(AttributeModifier.replace("aria-label", new ResourceModel("recoverLinkModeHelpTitle")));
		recoverLinkModeHelp.add(AttributeModifier.replace("data-bs-content", new ResourceModel("recoverLinkModeHelpOnlyBr")));
		recoverLinksSection.add(recoverLinkModeHelp);
		if (recoverLinksVisible) {
			recoverLinksSection.add(new PublicElectionRecoverLinkPanel("recoverLinkPanel", election));
		} else {
			recoverLinksSection.add(emptyContainer("recoverLinkPanel"));
		}
		recoverLinksSection.add(buildCacheDebugFooter("recoverLinksCardDebugFooter", buildRecoveryDebugPayload()));
		container.add(recoverLinksSection);

		WebMarkupContainer rollSection = new WebMarkupContainer("rollSection");
		rollSection.setOutputMarkupPlaceholderTag(true);
		boolean rollVisible = isSectionVisible(PublicElectionSectionKeys.LANDING_ROLL) && coreSnapshot != null && coreSnapshot.getRollSummary() != null;
		rollSection.setVisible(rollVisible);
		rollSection.add(new Label("rollSummaryOrganizations", formatLong(safeLong(coreSnapshot != null && coreSnapshot.getRollSummary() != null ? coreSnapshot.getRollSummary().getTotalOrganizations() : null))));
		rollSection.add(new Label("rollSummaryCountries", formatLong(safeLong(coreSnapshot != null && coreSnapshot.getRollSummary() != null ? coreSnapshot.getRollSummary().getTotalCountries() : null))));
		rollSection.add(new Label("rollSummarySpanish", formatLong(safeLong(coreSnapshot != null && coreSnapshot.getRollSummary() != null ? coreSnapshot.getRollSummary().getSpanishVoters() : null))));
		rollSection.add(new Label("rollSummaryEnglish", formatLong(safeLong(coreSnapshot != null && coreSnapshot.getRollSummary() != null ? coreSnapshot.getRollSummary().getEnglishVoters() : null))));
		rollSection.add(new Label("rollSummaryPortuguese", formatLong(safeLong(coreSnapshot != null && coreSnapshot.getRollSummary() != null ? coreSnapshot.getRollSummary().getPortugueseVoters() : null))));
		rollSection.add(new BookmarkablePageLink<Void>("rollLink", PublicElectionPageV2.class, buildRollParameters(1)));
		rollSection.add(buildCacheDebugFooter("rollSummaryCardDebugFooter", buildRollSummaryDebugPayload()));
		container.add(rollSection);

		WebMarkupContainer candidateNominationSummarySection = new WebMarkupContainer("incompleteSection");
		candidateNominationSummarySection.setOutputMarkupPlaceholderTag(true);
		boolean nominationSummaryVisible = isSectionVisible(PublicElectionSectionKeys.LANDING_CANDIDATE_NOMINATION_SUMMARY);
		candidateNominationSummarySection.setVisible(nominationSummaryVisible);
		candidateNominationSummarySection.add(new ListView<NominationSummaryRow>("incompleteRows", buildNominationSummaryRows()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<NominationSummaryRow> item) {
				NominationSummaryRow row = item.getModelObject();
				item.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getRowCssClass()));

				Label candidateName = new Label("summaryCandidateName", row.getCandidateName());
				candidateName.setVisible(!row.isPublicCandidateLink());
				item.add(candidateName);

				BookmarkablePageLink<Void> candidateLink = new BookmarkablePageLink<Void>("summaryCandidateLink", PublicElectionPageV2.class, buildCandidateParameters(row.getCandidateId()));
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
		candidateNominationSummarySection.add(buildCacheDebugFooter("nominationSummaryCardDebugFooter", buildNominationSummaryDebugPayload()));
		container.add(candidateNominationSummarySection);
		return container;
	}

	private WebMarkupContainer buildOfficialResultsSection(String id) {
		WebMarkupContainer section = new WebMarkupContainer(id);
		section.setOutputMarkupPlaceholderTag(true);

		String officialResultsHtml = resolveOfficialResultsHtml();
		String officialResultLetterUrl = resolveOfficialResultLetterUrl();
		boolean hasOfficialResultsHtml = hasText(officialResultsHtml);
		boolean hasOfficialResultLetter = hasText(officialResultLetterUrl);
		section.setVisible(hasOfficialResultsHtml || hasOfficialResultLetter);

		section.add(new Label("officialResultsTitle", resolveOfficialResultsTitle()));

		Label content = new Label("officialResultsContent",
				hasOfficialResultsHtml ? CandidateBiographyUtils.toRenderableMarkup(officialResultsHtml) : "");
		content.setEscapeModelStrings(false);
		content.setVisible(hasOfficialResultsHtml);
		section.add(content);

		ExternalLink letterLink = new ExternalLink(
				"officialResultsLetterLink",
				hasOfficialResultLetter ? officialResultLetterUrl : "#");
		if (hasOfficialResultLetter) {
			String cacheBustingSeparator = officialResultLetterUrl.contains("?") ? "&" : "?";
			letterLink.add(AttributeModifier.replace(
					"onclick",
					"this.href='"
							+ officialResultLetterUrl
							+ cacheBustingSeparator
							+ "ts=' + new Date().getTime();"));
		}
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
		candidateNominationCard.add(new BookmarkablePageLink<Void>("nominateLink", PublicElectionPageV2.class, buildNominateParameters()));
		section.add(candidateNominationCard);
		return section;
	}

	private WebMarkupContainer buildNominateContainer() {
		WebMarkupContainer container = new WebMarkupContainer("nominateContainer");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(pageMode == PageMode.NOMINATE && isPublicNominationVisible());

		container.add(new Label("nominatePageTitle", resolveNominatePageTitle()));
		container.add(new BookmarkablePageLink<Void>("nominateBackLink", PublicElectionPageV2.class, baseLandingParameters()));
		container.add(new WebMarkupContainer("nominateDescription").setVisible(false));
		container.add(new PublicNominationFormPanel("publicNominationFormPanel", election, getToken()));
		return container;
	}

	private WebMarkupContainer buildCandidateContainer() {
		WebMarkupContainer container = new WebMarkupContainer("candidateContainer");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(pageMode == PageMode.CANDIDATE && selectedCandidate != null);

		PublicElectionCoreSnapshot.CandidateData candidate = selectedCandidate;
		container.add(buildCandidatePicture("detailCandidatePicture", candidate != null ? photoByCandidateId.get(candidate.getCandidateId()) : null, 160, "rounded-circle img-thumbnail"));
		container.add(new Label("detailCandidateName", valueOrDash(candidate != null ? candidate.getName() : null)));
		container.add(new WinnerPopoverBadgePanel("detailCandidateWinnerBadge", candidate != null && Boolean.TRUE.equals(candidate.getWinner())));
		container.add(new Label("detailCandidateOrganization", valueOrDash(candidate != null ? candidate.getNominationOrganizationName() : null)));
		String linkedinUrl = candidate != null ? candidate.getLinkedinUrl() : null;
		ExternalLink linkedinLink = new ExternalLink("detailCandidateLinkedin", hasText(linkedinUrl) ? linkedinUrl : "#", getString("publicElectionModernDetailLinkedin"));
		linkedinLink.setVisible(hasText(linkedinUrl));
		container.add(linkedinLink);
		container.add(new BookmarkablePageLink<Void>("detailBackLink", PublicElectionPageV2.class, baseLandingParameters()));

		WebMarkupContainer profileBlock = new WebMarkupContainer("profileBlock");
		profileBlock.setOutputMarkupPlaceholderTag(true);
		profileBlock.setVisible(isSectionVisible(PublicElectionSectionKeys.CANDIDATE_PROFILE));
		Label profileBio = new Label("profileBio", CandidateBiographyUtils.toRenderableMarkup(resolveCandidateBio(candidate)));
		profileBio.setEscapeModelStrings(false);
		profileBlock.add(profileBio);
		profileBlock.add(buildCacheDebugFooter("profileBlockDebugFooter", buildCandidateProfileDebugPayload(candidate)));
		container.add(profileBlock);

		WebMarkupContainer statutoryBlock = new WebMarkupContainer("statutoryBlock");
		statutoryBlock.setOutputMarkupPlaceholderTag(true);
		statutoryBlock.setVisible(isSectionVisible(PublicElectionSectionKeys.CANDIDATE_STATUTORY_QUESTIONS));
		List<QuestionAnswerView> statutoryAnswers = buildQuestionAnswerViews(candidate != null ? candidate.getStatutoryAnswers() : null);
		statutoryBlock.add(new WebMarkupContainer("statutoryEmpty").setVisible(statutoryAnswers.isEmpty()));
		statutoryBlock.add(new ListView<QuestionAnswerView>("statutoryRows", statutoryAnswers) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<QuestionAnswerView> item) {
				QuestionAnswerView row = item.getModelObject();
				item.add(new Label("statutoryAskedByInitials", "CE"));
				item.add(new Label("statutoryQuestion", row.getQuestionLabel()));
				item.add(new Label("statutoryDate", row.getDateLabel()));
				item.add(new Label("statutoryAnswer", row.getAnswer()));
			}
		});
		statutoryBlock.add(buildCacheDebugFooter("statutoryBlockDebugFooter", buildCandidateStatutoryDebugPayload(candidate)));
		container.add(statutoryBlock);

		WebMarkupContainer nonStatutoryBlock = new WebMarkupContainer("nonStatutoryBlock");
		nonStatutoryBlock.setOutputMarkupPlaceholderTag(true);
		nonStatutoryBlock.setVisible(isSectionVisible(PublicElectionSectionKeys.CANDIDATE_NON_STATUTORY_QUESTIONS));
		List<QuestionAnswerView> nonStatutoryAnswers = buildQuestionAnswerViews(candidate != null ? candidate.getNonStatutoryAnswers() : null);
		nonStatutoryBlock.add(new WebMarkupContainer("nonStatutoryEmpty").setVisible(nonStatutoryAnswers.isEmpty()));
		nonStatutoryBlock.add(new ListView<QuestionAnswerView>("nonStatutoryRows", nonStatutoryAnswers) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<QuestionAnswerView> item) {
				QuestionAnswerView row = item.getModelObject();
				item.add(new Label("nonStatutoryAskedByInitials", "CE"));
				item.add(new Label("nonStatutoryQuestion", row.getQuestionLabel()));
				item.add(new Label("nonStatutoryDate", row.getDateLabel()));
				item.add(new Label("nonStatutoryAnswer", row.getAnswer()));
			}
		});
		nonStatutoryBlock.add(buildCacheDebugFooter("nonStatutoryBlockDebugFooter", buildCandidateNonStatutoryDebugPayload(candidate)));
		container.add(nonStatutoryBlock);

		WebMarkupContainer askQuestionsBlock = new WebMarkupContainer("askQuestionsBlock");
		askQuestionsBlock.setOutputMarkupPlaceholderTag(true);
		boolean questionSubmissionVisible = isSectionVisible(PublicElectionSectionKeys.CANDIDATE_QUESTION_SUBMISSION);
		askQuestionsBlock.setVisible(questionSubmissionVisible);
		boolean shouldResolveCaptcha = pageMode == PageMode.CANDIDATE && questionSubmissionVisible;
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
			setResponsePage(PublicElectionPageV2.class, buildCandidateParameters(selectedCandidate != null && selectedCandidate.getCandidateId() != null ? selectedCandidate.getCandidateId().longValue() : 0L));
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
				textArea -> textArea.setRequired(true));
		askForm.add(questionTextEditor);
		askForm.add(new CheckBox("askSameQuestionToAllCandidates", new PropertyModel<Boolean>(this, "askSameQuestionToAllCandidates")));
		WebMarkupContainer reCaptcha = new WebMarkupContainer("reCaptcha");
		reCaptcha.add(AttributeModifier.replace("data-sitekey", askQuestionDataSiteKey));
		reCaptcha.setVisibilityAllowed(askQuestionCaptchaEnabled);
		askForm.add(reCaptcha);
		askQuestionsBlock.add(askForm);
		askQuestionsBlock.add(buildCacheDebugFooter("askQuestionsBlockDebugFooter", buildCandidateQuestionSubmissionDebugPayload(candidate)));
		container.add(askQuestionsBlock);

		WebMarkupContainer communityBlock = new WebMarkupContainer("communityBlock");
		communityBlock.setOutputMarkupPlaceholderTag(true);
		communityBlock.setVisible(isSectionVisible(PublicElectionSectionKeys.CANDIDATE_COMMUNITY_QUESTIONS));
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
		communityBlock.add(buildCacheDebugFooter("communityBlockDebugFooter", buildCandidateCommunityDebugPayload(candidate)));
		container.add(communityBlock);
		container.add(buildCacheDebugFooter("candidateDetailCardDebugFooter", buildCandidateDetailDebugPayload(candidate)));

		WebMarkupContainer sidebarBlock = new WebMarkupContainer("sidebarBlock");
		sidebarBlock.setOutputMarkupPlaceholderTag(true);
		sidebarBlock.setVisible(isSectionVisible(PublicElectionSectionKeys.CANDIDATE_SIDEBAR));
		CountryDisplayRow primaryCountryRow = resolvePrimaryCountryRow(candidate);
		sidebarBlock.add(buildCountryFlagImage("sidebarPrimaryCountryFlag", primaryCountryRow, 30, 20));
		sidebarBlock.add(new Label("sidebarPrimaryCountry", valueOrDash(primaryCountryRow.getCountryLabel())));
		List<CountryDisplayRow> otherCountryRows = resolveOtherCountryRows(candidate);
		ListView<CountryDisplayRow> otherCountries = new ListView<CountryDisplayRow>("sidebarOtherCountries", otherCountryRows) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<CountryDisplayRow> item) {
				CountryDisplayRow row = item.getModelObject();
				item.add(buildCountryFlagImage("otherCountryFlag", row, 30, 20));
				item.add(new Label("otherCountryName", valueOrDash(row.getCountryLabel())));
			}
		};
		otherCountries.setVisible(!otherCountryRows.isEmpty());
		sidebarBlock.add(otherCountries);
		sidebarBlock.add(new Label("sidebarOtherCountriesEmpty", getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_APPLICABLE)).setVisible(otherCountryRows.isEmpty()));
		sidebarBlock.add(new ListView<String>("sidebarSupports", resolveSupportOrganizationNames(candidate)) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("supportName", valueOrDash(item.getModelObject())));
			}
		});
		WebMarkupContainer sidebarNominationBlock = new WebMarkupContainer("sidebarNominationBlock");
		sidebarNominationBlock.setOutputMarkupPlaceholderTag(true);
		sidebarNominationBlock.setVisible(isSectionVisible(PublicElectionSectionKeys.CANDIDATE_PROFILE));
		sidebarNominationBlock.add(new Label("sidebarNominationOrg", valueOrDash(candidate != null ? candidate.getNominationOrganizationName() : null)));
		sidebarNominationBlock.add(new Label("sidebarNominationReason", valueOrDash(resolveLocalizedText(candidate != null ? candidate.getNominationReason() : null))));
		sidebarBlock.add(sidebarNominationBlock);
		List<String> organizationNames = resolveWorkOrganizationNames(candidate);
		ListView<String> organizations = new ListView<String>("sidebarOrganizations", organizationNames) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("organizationName", valueOrDash(item.getModelObject())));
			}
		};
		organizations.setVisible(!organizationNames.isEmpty());
		sidebarBlock.add(organizations);
		sidebarBlock.add(new Label("sidebarOrganizationsEmpty", getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_APPLICABLE)).setVisible(organizationNames.isEmpty()));
		sidebarBlock.add(buildCacheDebugFooter("candidateSidebarCardDebugFooter", buildCandidateSidebarDebugPayload(candidate)));
		container.add(sidebarBlock);
		return container;
	}

	private WebMarkupContainer buildRollContainer() {
		WebMarkupContainer container = new WebMarkupContainer("rollContainer");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(pageMode == PageMode.ROLL && isSectionVisible(PublicElectionSectionKeys.LANDING_ROLL));
		container.add(new BookmarkablePageLink<Void>("rollBackLink", PublicElectionPageV2.class, baseLandingParameters()));
		container.add(new Label("rollTotalRows", formatLong((long) rollTotalRows)));
		container.add(new Label("rollPageRange", buildRollRangeLabel()));

		Form<Void> rollFilterForm = new Form<Void>("rollFilterForm") {
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit() {
				rollCountryFilter = normalizeCountryFilter(rollCountryFilter);
				rollOrganizationFilter = normalizeOrganizationFilter(rollOrganizationFilter);
				setResponsePage(PublicElectionPageV2.class, buildRollParameters(1));
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

		BookmarkablePageLink<Void> clearFilters = new BookmarkablePageLink<Void>("rollClearButton", PublicElectionPageV2.class, buildRollParametersWithoutFilters(1));
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

		BookmarkablePageLink<Void> prev = new BookmarkablePageLink<Void>("rollPrevPage", PublicElectionPageV2.class, buildRollParameters(rollPage - 1));
		prev.setVisible(rollPage > 1);
		container.add(prev);
		BookmarkablePageLink<Void> next = new BookmarkablePageLink<Void>("rollNextPage", PublicElectionPageV2.class, buildRollParameters(rollPage + 1));
		next.setVisible(rollPage < rollTotalPages);
		container.add(next);
		container.add(new Label("rollPageNumber", String.valueOf(rollPage)));
		container.add(new Label("rollPageCount", String.valueOf(rollTotalPages)));
		container.add(buildCacheDebugFooter("rollDetailCardDebugFooter", buildRollDetailDebugPayload()));
		return container;
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

	private boolean isSectionVisible(String key) {
		return coreSnapshot != null
				&& coreSnapshot.getVisibility() != null
				&& coreSnapshot.getVisibility().getSections() != null
				&& Boolean.TRUE.equals(coreSnapshot.getVisibility().getSections().get(key));
	}

	private List<ResultRowView> buildResultRows() {
		List<ResultRowView> rows = new ArrayList<>();
		Locale locale = resolveUiLocale();
		for (PublicElectionCoreSnapshot.ResultRowData row : safeList(coreSnapshot != null && coreSnapshot.getResultSummary() != null ? coreSnapshot.getResultSummary().getRows() : null)) {
			if (row == null) {
				continue;
			}
			rows.add(new ResultRowView(
					row.getCandidateId(),
					valueOrDash(row.getCandidateName()),
					safeLong(row.getVoteCount()),
					safeDouble(row.getPercentage()),
					Boolean.TRUE.equals(row.getWinner()),
					locale));
		}
		return rows;
	}

	private List<ResultPublicationStageView> buildResultPublicationStages() {
		List<ResultPublicationStageView> rows = new ArrayList<>();
		for (PublicElectionCoreSnapshot.ResultPublicationStageData row : safeList(coreSnapshot != null ? coreSnapshot.getResultPublicationStages() : null)) {
			if (row == null || !Boolean.TRUE.equals(row.getConfigured())) {
				continue;
			}
			rows.add(new ResultPublicationStageView(
					resolveResultStageTitle(row.getStageCode()),
					buildDateRangeLabel(row.getStartUtc(), row.getEndUtc()),
					Boolean.TRUE.equals(row.getCurrent())));
		}
		return rows;
	}

	private List<TimelineEntryView> buildLandingTimelineRows() {
		List<TimelineEntryView> rows = new ArrayList<>();
		int sortOrder = 0;
		for (PublicElectionCoreSnapshot.CalendarEventData event : safeList(coreSnapshot != null ? coreSnapshot.getPublicCalendarEvents() : null)) {
			if (event == null) {
				continue;
			}
				rows.add(new TimelineEntryView(
						event.getStartUtc(),
						Boolean.TRUE.equals(event.getSingleEvent()) ? null : event.getEndUtc(),
						sortOrder++,
						getString("electionCalendarKey." + valueOrDash(event.getKeyCode()), null, valueOrDash(event.getKeyCode())),
						buildCalendarScheduleLabel(event),
						resolveTimelinePhaseLabel(event.getKeyCode()),
						null));
		}
		return rows;
	}

	private List<NominationSummaryRow> buildNominationSummaryRows() {
		List<NominationSummaryRow> rows = new ArrayList<>();
		for (PublicElectionCoreSnapshot.CandidateNominationSummaryData row : safeList(coreSnapshot != null ? coreSnapshot.getCandidateNominationSummaries() : null)) {
			if (row == null) {
				continue;
			}
			StatusBadge candidateBadge = resolveCandidateStatusBadge(row.getCandidateStatusCode(), row.getNominationStatusCode());
			rows.add(new NominationSummaryRow(
					Boolean.TRUE.equals(row.getPublicCandidateLink()),
					row.getCandidateId() != null ? row.getCandidateId().longValue() : 0L,
					valueOrDash(row.getCandidateName()),
					valueOrDash(row.getCountryCode()),
					candidateBadge.text,
					candidateBadge.cssClass,
					buildTasksLabel(row.getCompletedPublicTasks(), row.getTotalPublicTasks()),
					resolveNominationStatusLabel(row.getNominationStatusCode()),
					resolveNominationRowClass(row.getCandidateStatusCode(), row.getNominationStatusCode()),
					resolveNominationSortRank(row.getCandidateStatusCode(), row.getNominationStatusCode())));
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

	private List<QuestionAnswerView> buildQuestionAnswerViews(List<PublicElectionCoreSnapshot.QuestionAnswerData> rows) {
		List<QuestionAnswerView> views = new ArrayList<>();
		for (PublicElectionCoreSnapshot.QuestionAnswerData row : safeList(rows)) {
			if (row == null) {
				continue;
			}
			String question = resolveLocalizedText(row.getQuestionText());
			String answer = resolveLocalizedText(row.getAnswerText());
			if (!hasText(answer)) {
				continue;
			}
			views.add(new QuestionAnswerView(
					valueOrDash(question),
					valueOrDash(answer),
					formatDateTime(row.getDateUtc())));
		}
		return views;
	}

	private List<CommunityQuestionView> buildCommunityQuestionRows(PublicElectionCoreSnapshot.CandidateData candidate) {
		List<CommunityQuestionView> rows = new ArrayList<>();
		if (candidate == null) {
			return rows;
		}
		for (PublicElectionCoreSnapshot.CommunityQuestionData question : safeList(candidate.getCommunityQuestions())) {
			if (question == null) {
				continue;
			}
			rows.add(new CommunityQuestionView(
					formatDateTime(question.getQuestionDateUtc()),
					formatDateTime(question.getAnswerDateUtc()),
					resolveCommunityQuestionText(question),
					resolveCommunityAnswerText(question),
					valueOrDash(question.getAskedByInitials()),
					question.getCandidateQuestionId() != null ? question.getCandidateQuestionId().longValue() : 0L));
		}
		Collections.sort(rows, new Comparator<CommunityQuestionView>() {
			@Override
			public int compare(CommunityQuestionView a, CommunityQuestionView b) {
				return Long.compare(b.getSortQuestionId(), a.getSortQuestionId());
			}
		});
		return rows;
	}

	private String resolveCommunityQuestionText(PublicElectionCoreSnapshot.CommunityQuestionData question) {
		String displayMode = question != null ? question.getQuestionDisplayMode() : null;
		if (DISPLAY_MODE_VISIBLE.equals(displayMode)) {
			return valueOrDash(resolveLocalizedText(question.getQuestionText()));
		}
		if (DISPLAY_MODE_MASKED.equals(displayMode)) {
			return valueOrDash(resolveLocalizedText(question.getQuestionMaskedPreview()));
		}
		return getString("publicElectionPendingQuestionInReview");
	}

	private String resolveCommunityAnswerText(PublicElectionCoreSnapshot.CommunityQuestionData question) {
		String displayMode = question != null ? question.getAnswerDisplayMode() : null;
		if (DISPLAY_MODE_VISIBLE.equals(displayMode)) {
			return valueOrDash(resolveLocalizedText(question.getAnswerText()));
		}
		if (DISPLAY_MODE_MASKED.equals(displayMode)) {
			return valueOrDash(resolveLocalizedText(question.getAnswerMaskedPreview()));
		}
		if (DISPLAY_MODE_WAITING_CANDIDATE.equals(displayMode)) {
			return getString("publicElectionPendingAnswerNoResponse");
		}
		if (DISPLAY_MODE_WAITING_LACNIC.equals(displayMode)) {
			return getString("publicElectionPendingAnswerWaitingLacnic");
		}
		if (DISPLAY_MODE_WAITING_REVIEW.equals(displayMode)) {
			return getString("publicElectionPendingAnswerInReview");
		}
		return getString("publicElectionPendingAnswerInReview");
	}

	private List<String> resolveSupportOrganizationNames(PublicElectionCoreSnapshot.CandidateData candidate) {
		List<String> values = new ArrayList<>();
		for (String value : safeList(candidate != null ? candidate.getSupportOrganizationNames() : null)) {
			if (hasText(value)) {
				values.add(value);
			}
		}
		if (values.isEmpty()) {
			values.add(getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_APPLICABLE));
		}
		return values;
	}

	private List<String> resolveWorkOrganizationNames(PublicElectionCoreSnapshot.CandidateData candidate) {
		List<String> values = new ArrayList<>();
		for (String value : safeList(candidate != null ? candidate.getWorkOrganizationNames() : null)) {
			if (hasText(value)) {
				values.add(value);
			}
		}
		return values;
	}

	private String resolveCandidateOrganizationsLabel(PublicElectionCoreSnapshot.CandidateData candidate) {
		List<String> values = resolveWorkOrganizationNames(candidate);
		if (values.isEmpty()) {
			return getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_APPLICABLE);
		}
		return String.join(", ", values);
	}

	private String resolveCandidateBio(PublicElectionCoreSnapshot.CandidateData candidate) {
		String bio = resolveLocalizedText(candidate != null ? candidate.getBio() : null);
		if (hasText(bio)) {
			return bio;
		}
		return getString(TokenResourceKeys.PUBLIC_ELECTION_NO_PUBLIC_BIO);
	}

	private String resolveOfficialResultsTitle() {
		String electionTitle = resolveLocalizedElectionTitle();
		if (!hasText(electionTitle)) {
			return getString("publicElectionOfficialResultsTitle");
		}
		return MessageFormat.format(getString("publicElectionOfficialResultsTitleWithElection"), electionTitle);
	}

	private String resolveOfficialResultsHtml() {
		return officialResultSnapshot != null ? officialResultSnapshot.getResult(resolveCurrentPageLanguageCode()) : null;
	}

	private String resolveOfficialResultLetterUrl() {
		return officialResultSnapshot != null ? officialResultSnapshot.getResultLetterUrl(resolveCurrentPageLanguageCode()) : null;
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

	private String resolveElectionCall() {
		return coreSnapshot != null && coreSnapshot.getElection() != null
				? resolveLocalizedText(coreSnapshot.getElection().getCall())
				: null;
	}

	private String resolveLocalizedElectionTitle() {
		if (coreSnapshot != null && coreSnapshot.getElection() != null) {
			String localizedTitle = resolveLocalizedText(
					coreSnapshot.getElection().getTitleSpanish(),
					coreSnapshot.getElection().getTitleEnglish(),
					coreSnapshot.getElection().getTitlePortuguese());
			if (hasText(localizedTitle)) {
				return localizedTitle.trim();
			}
		}
		String fallbackTitle = election != null ? election.getTitle(SecurityUtils.getLocale().getLanguage()) : null;
		if (hasText(fallbackTitle)) {
			return fallbackTitle.trim();
		}
		return election != null && hasText(election.getTitleSpanish()) ? election.getTitleSpanish().trim() : null;
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
		ElectionCalendar calendar = findPublicCalendar(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
		if (calendar == null || calendar.getStartDate() == null) {
			return false;
		}
		Date endDate = calendar.getEndDate() != null ? calendar.getEndDate() : calendar.getStartDate();
		return !now.before(calendar.getStartDate()) && !now.after(endDate);
	}

	private boolean isPublicNominationVisible() {
		return isSectionVisible(PublicElectionSectionKeys.LANDING_PUBLIC_NOMINATION);
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
		for (PublicElectionCoreSnapshot.CalendarEventData event : safeList(coreSnapshot != null ? coreSnapshot.getPublicCalendarEvents() : null)) {
			if (event == null || !ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED.name().equals(event.getKeyCode())) {
				continue;
			}
			if (event.getStartUtc() != null) {
				return event.getStartUtc();
			}
		}
		if (coreSnapshot != null && coreSnapshot.getElection() != null && coreSnapshot.getElection().getVotingPeriodStartUtc() != null) {
			return coreSnapshot.getElection().getVotingPeriodStartUtc();
		}
		return coreSnapshot != null && coreSnapshot.getElection() != null
				? coreSnapshot.getElection().getVotingPeriodEndUtc()
				: null;
	}

	private String resolveCandidateBioSnippet(PublicElectionCoreSnapshot.CandidateData candidate) {
		String bio = resolveCandidateBio(candidate);
		String snippet = CandidateBiographyUtils.toPlainTextSnippet(bio, 150);
		if (!hasText(snippet) || getString(TokenResourceKeys.PUBLIC_ELECTION_NO_PUBLIC_BIO).equals(snippet)) {
			return getString(TokenResourceKeys.PUBLIC_ELECTION_NO_PUBLIC_BIO);
		}
		return snippet;
	}

	private String resolveLocalizedText(PublicElectionCoreSnapshot.LocalizedTextData value) {
		if (value == null) {
			return null;
		}
		LanguageCode languageCode = SecurityUtils.getLanguageCode();
		switch (languageCode) {
		case EN:
			if (hasText(value.getEnglish())) {
				return value.getEnglish();
			}
			if (hasText(value.getSpanish())) {
				return value.getSpanish();
			}
			return hasText(value.getPortuguese()) ? value.getPortuguese() : null;
		case PT:
			if (hasText(value.getPortuguese())) {
				return value.getPortuguese();
			}
			if (hasText(value.getSpanish())) {
				return value.getSpanish();
			}
			return hasText(value.getEnglish()) ? value.getEnglish() : null;
		case SP:
		default:
			if (hasText(value.getSpanish())) {
				return value.getSpanish();
			}
			if (hasText(value.getEnglish())) {
				return value.getEnglish();
			}
			return hasText(value.getPortuguese()) ? value.getPortuguese() : null;
		}
	}

	private String resolveLocalizedText(String spanish, String english, String portuguese) {
		PublicElectionCoreSnapshot.LocalizedTextData value = new PublicElectionCoreSnapshot.LocalizedTextData();
		value.setSpanish(spanish);
		value.setEnglish(english);
		value.setPortuguese(portuguese);
		return resolveLocalizedText(value);
	}

	private CountryDisplayRow resolvePrimaryCountryRow(PublicElectionCoreSnapshot.CandidateData candidate) {
		return resolveCountryRow(candidate != null ? candidate.getPrimaryCountryCode() : null);
	}

	private CountryDisplayRow resolveCountryRow(String countryCode) {
		if (!hasText(countryCode)) {
			return new CountryDisplayRow(null, getString(TokenResourceKeys.PUBLIC_ELECTION_NOT_AVAILABLE), null);
		}
		String normalizedCode = normalizeCountryCodeForFlag(countryCode);
		String countryLabel = resolveCountryLabel(countryCode);
		return new CountryDisplayRow(
				normalizedCode,
				countryLabel,
				normalizedCode != null ? "v2/images/flags/" + normalizedCode.toLowerCase(Locale.ROOT) + ".svg" : null);
	}

	private List<CountryDisplayRow> resolveOtherCountryRows(PublicElectionCoreSnapshot.CandidateData candidate) {
		List<CountryDisplayRow> rows = new ArrayList<>();
		Set<String> added = new HashSet<>();
		for (String countryCode : safeList(candidate != null ? candidate.getOtherCountryCodes() : null)) {
			if (!hasText(countryCode)) {
				continue;
			}
			String normalizedCode = normalizeCountryCodeForFlag(countryCode);
			String countryLabel = resolveCountryLabel(countryCode);
			String dedupeKey = hasText(countryLabel) ? countryLabel.trim().toUpperCase(Locale.ROOT)
					: countryCode.trim().toUpperCase(Locale.ROOT);
			if (!added.add(dedupeKey)) {
				continue;
			}
			rows.add(new CountryDisplayRow(
					normalizedCode,
					countryLabel,
					normalizedCode != null ? "v2/images/flags/" + normalizedCode.toLowerCase(Locale.ROOT) + ".svg" : null));
		}
		return rows;
	}

	private Component buildCandidatePicture(String id, PublicElectionPhotoSnapshot.CandidatePhotoData photoData, int size, String cssClass) {
		String effectiveCssClass = hasText(cssClass) ? cssClass + " candidate-photo-sepia" : "candidate-photo-sepia";
		String fixedSquareStyle = "width:" + size + "px;height:" + size + "px;object-fit:cover;object-position:center;";
		if (photoData != null && photoData.getPictureBytes() != null && photoData.getPictureBytes().length > 0) {
			String extension = hasText(photoData.getPictureExtension()) ? photoData.getPictureExtension() : "jpg";
			NonCachingImage image = new NonCachingImage(id, new ImageResource(photoData.getPictureBytes(), extension));
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

	private Component buildWinnerListPicture(String id, PublicElectionPhotoSnapshot.CandidatePhotoData photoData) {
		String cssClass = "rounded-circle";
		String fixedSquareStyle = "width:40px;height:40px;object-fit:cover;object-position:center;";
		if (photoData != null && photoData.getPictureBytes() != null && photoData.getPictureBytes().length > 0) {
			String extension = hasText(photoData.getPictureExtension()) ? photoData.getPictureExtension() : "jpg";
			NonCachingImage image = new NonCachingImage(id, new ImageResource(photoData.getPictureBytes(), extension));
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

	private String resolveCountryLabel(String countryCode) {
		if (!hasText(countryCode)) {
			return "-";
		}
		String normalized = countryCode.trim().toUpperCase(Locale.ROOT);
		return COUNTRY_UTILS.getDisplayLabel(normalized, getLocale(), true);
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

	private String resolveResultPublicationStatusText(String stageCode) {
		if (PublicElectionStageCodes.OFFICIAL_WITH_CLAIMS.equals(stageCode)) {
			return getString("publicElectionResultStatusOfficialWithClaims");
		}
		if (PublicElectionStageCodes.OFFICIAL_NO_CLAIMS.equals(stageCode)) {
			return getString("publicElectionResultStatusOfficialNoClaims");
		}
		if (PublicElectionStageCodes.VOTER_AUDIT.equals(stageCode)) {
			return getString("publicElectionResultStatusVoterAudit");
		}
		if (PublicElectionStageCodes.CE_AUDIT.equals(stageCode)) {
			return getString("publicElectionResultStatusCeAudit");
		}
		if (PublicElectionStageCodes.PROVISIONAL.equals(stageCode)) {
			return getString("publicElectionResultStatusProvisional");
		}
		return getString("publicElectionResultStatusNotPublished");
	}

	private String resolveResultPublicationHelpText(String stageCode) {
		if (PublicElectionStageCodes.OFFICIAL_WITH_CLAIMS.equals(stageCode)) {
			return getString("publicElectionResultHelpOfficialWithClaims");
		}
		if (PublicElectionStageCodes.OFFICIAL_NO_CLAIMS.equals(stageCode)) {
			return getString("publicElectionResultHelpOfficialNoClaims");
		}
		if (PublicElectionStageCodes.VOTER_AUDIT.equals(stageCode)) {
			return getString("publicElectionResultHelpVoterAudit");
		}
		if (PublicElectionStageCodes.CE_AUDIT.equals(stageCode)) {
			return getString("publicElectionResultHelpCeAudit");
		}
		if (PublicElectionStageCodes.PROVISIONAL.equals(stageCode)) {
			return getString("publicElectionResultHelpProvisional");
		}
		return getString("publicElectionResultHelpNotPublished");
	}

	private String resolveResultStageTitle(String stageCode) {
		if (PublicElectionStageCodes.OFFICIAL_WITH_CLAIMS.equals(stageCode)) {
			return getString("publicElectionResultStageOfficialWithClaims");
		}
		if (PublicElectionStageCodes.OFFICIAL_NO_CLAIMS.equals(stageCode)) {
			return getString("publicElectionResultStageOfficialNoClaims");
		}
		if (PublicElectionStageCodes.VOTER_AUDIT.equals(stageCode)) {
			return getString("publicElectionResultStageVoterAudit");
		}
		if (PublicElectionStageCodes.CE_AUDIT.equals(stageCode)) {
			return getString("publicElectionResultStageCeAudit");
		}
		return getString("publicElectionResultStageProvisional");
	}

	private String resolveTimelinePhaseLabel(String keyCode) {
		if (!hasText(keyCode)) {
			return getString("auditPublicV2PhaseCalendar");
		}
		String name = keyCode.toUpperCase(Locale.ROOT);
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

	private String buildCalendarScheduleLabel(PublicElectionCoreSnapshot.CalendarEventData event) {
		if (event == null) {
			return "-";
		}
			Date endDate = Boolean.TRUE.equals(event.getSingleEvent()) ? null : event.getEndUtc();
			return buildDateRangeLabel(event.getStartUtc(), endDate);
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

	private String buildRollRangeLabel() {
		if (rollTotalRows == 0) {
			return getString("publicElectionRollRangeEmpty");
		}
		return getString("publicElectionRollRangePrefix")
				+ " " + rollPageFrom + " - " + rollPageTo
				+ " " + getString("publicElectionRollRangeOf")
				+ " " + rollTotalRows + " " + getString("publicElectionRollRangeRecords");
	}

	private List<String> buildRollCountryOptions() {
		return new ArrayList<>(rollCountryOptions);
	}

	private List<String> buildRollCountryOptions(List<RollEntryView> sourceRows) {
		Set<String> countryCodes = new HashSet<>();
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
		List<String> ordered = new ArrayList<>(countryCodes);
		Collections.sort(ordered, new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				return resolveCountryLabel(a).compareToIgnoreCase(resolveCountryLabel(b));
			}
		});
		List<String> options = new ArrayList<>();
		options.add("");
		options.addAll(ordered);
		return options;
	}

	private Component buildCacheDebugFooter(String id, Object payload) {
		return new PublicElectionCacheDebugFooterPanel(
				id,
				getString("publicElectionCacheDebugFooterTitle"),
				payload,
				isCacheDebugModeEnabled());
	}

	private boolean isCacheDebugModeEnabled() {
		return cacheDebugModeEnabled;
	}

	private boolean parseCacheDebugMode(String debugValue) {
		if (!hasText(debugValue)) {
			return false;
		}
		String normalized = debugValue.trim().toLowerCase(Locale.ROOT);
		return "1".equals(normalized)
				|| "true".equals(normalized)
				|| "yes".equals(normalized)
				|| "on".equals(normalized);
	}

	private Map<String, Object> buildResultStatusDebugPayload() {
		Map<String, Object> bindings = new LinkedHashMap<>();
		PublicElectionCoreSnapshot.ResultSummaryData resultSummary = coreSnapshot != null ? coreSnapshot.getResultSummary() : null;
		bindings.put("coreSnapshot.getResultSummary().publicationStageCode", resultSummary != null ? resultSummary.getPublicationStageCode() : null);
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_RESULTS, bindings);
	}

	private Map<String, Object> buildResultTableDebugPayload() {
		Map<String, Object> bindings = new LinkedHashMap<>();
		PublicElectionCoreSnapshot.ResultSummaryData resultSummary = coreSnapshot != null ? coreSnapshot.getResultSummary() : null;
		bindings.put("coreSnapshot.getResultSummary().rows", buildResultRowsDebugData(resultSummary != null ? resultSummary.getRows() : null));
		bindings.put("coreSnapshot.getResultSummary().totalVotes", resultSummary != null ? resultSummary.getTotalVotes() : null);
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_RESULTS, bindings);
	}

	private Map<String, Object> buildResultStagesDebugPayload() {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put("coreSnapshot.getResultPublicationStages()", buildResultPublicationStagesDebugData(coreSnapshot != null ? coreSnapshot.getResultPublicationStages() : null));
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_RESULTS, bindings);
	}

	private Map<String, Object> buildResultSummaryDebugPayload() {
		Map<String, Object> bindings = new LinkedHashMap<>();
		PublicElectionCoreSnapshot.ResultSummaryData resultSummary = coreSnapshot != null ? coreSnapshot.getResultSummary() : null;
		bindings.put("coreSnapshot.getResultSummary().enabledVoters", resultSummary != null ? resultSummary.getEnabledVoters() : null);
		bindings.put("coreSnapshot.getResultSummary().organizationsVoted", resultSummary != null ? resultSummary.getOrganizationsVoted() : null);
		bindings.put("coreSnapshot.getResultSummary().participationPercentage", resultSummary != null ? resultSummary.getParticipationPercentage() : null);
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_RESULTS, bindings);
	}

	private Map<String, Object> buildResultContactDebugPayload() {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put("coreSnapshot.getElection().resultContactEmail", coreSnapshot != null && coreSnapshot.getElection() != null ? coreSnapshot.getElection().getResultContactEmail() : null);
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_RESULTS, bindings);
	}

	private Map<String, Object> buildCandidatesDebugPayload(List<PublicElectionCoreSnapshot.CandidateData> displayedCandidates) {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put("coreSnapshot.getCandidates().displayedOrder", buildLandingCandidatesDebugRows(displayedCandidates));
		bindings.put("photoSnapshot.getCandidates().displayedOrder", buildLandingCandidatePhotosDebugRows(displayedCandidates));
		bindings.put("coreSnapshot.getElection().publishedAbstentionCandidate", coreSnapshot != null && coreSnapshot.getElection() != null ? coreSnapshot.getElection().getPublishedAbstentionCandidate() : null);
		bindings.put("coreSnapshot.getElection().randomOrderCandidates", coreSnapshot != null && coreSnapshot.getElection() != null ? coreSnapshot.getElection().getRandomOrderCandidates() : null);
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_CANDIDATES, bindings);
	}

	private Map<String, Object> buildCandidateCardDebugPayload(PublicElectionCoreSnapshot.CandidateData candidate) {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put(resolveCandidateCachePath(candidate), buildLandingCandidateCardDebugData(candidate));
		bindings.put(resolvePhotoCachePath(candidate != null ? candidate.getCandidateId() : null), buildPhotoDebugData(candidate != null ? photoByCandidateId.get(candidate.getCandidateId()) : null));
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_CANDIDATES, bindings);
	}

	private Map<String, Object> buildCalendarDebugPayload() {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put("coreSnapshot.getPublicCalendarEvents()", coreSnapshot != null ? coreSnapshot.getPublicCalendarEvents() : null);
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_CALENDAR, bindings);
	}

	private Map<String, Object> buildRecoveryDebugPayload() {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put("coreSnapshot.getRecovery().visible", coreSnapshot != null && coreSnapshot.getRecovery() != null ? coreSnapshot.getRecovery().getVisible() : null);
		bindings.put("coreSnapshot.getRecovery().modeCode", coreSnapshot != null && coreSnapshot.getRecovery() != null ? coreSnapshot.getRecovery().getModeCode() : null);
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_PARTICIPATION_LINK_RECOVERY, bindings);
	}

	private Map<String, Object> buildRollSummaryDebugPayload() {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put("coreSnapshot.getRollSummary()", buildRollSummaryDebugData());
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_ROLL, bindings);
	}

	private Map<String, Object> buildNominationSummaryDebugPayload() {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put("coreSnapshot.getCandidateNominationSummaries()", coreSnapshot != null ? coreSnapshot.getCandidateNominationSummaries() : null);
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_CANDIDATE_NOMINATION_SUMMARY, bindings);
	}

	private Map<String, Object> buildCandidateDetailDebugPayload(PublicElectionCoreSnapshot.CandidateData candidate) {
		Map<String, Object> bindings = new LinkedHashMap<>();
		String candidatePath = resolveCandidateCachePath(candidate);
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("name", candidate != null ? candidate.getName() : null);
		data.put("nominationOrganizationName", candidate != null ? candidate.getNominationOrganizationName() : null);
		data.put("linkedinUrl", candidate != null ? candidate.getLinkedinUrl() : null);
		bindings.put(candidatePath, data);
		bindings.put(resolvePhotoCachePath(candidate != null ? candidate.getCandidateId() : null), buildPhotoDebugData(candidate != null ? photoByCandidateId.get(candidate.getCandidateId()) : null));
		return buildDebugPayload(PublicElectionSectionKeys.CANDIDATE_PROFILE_ROOT, bindings);
	}

	private Map<String, Object> buildCandidateProfileDebugPayload(PublicElectionCoreSnapshot.CandidateData candidate) {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put(resolveCandidateCachePath(candidate) + ".bio", candidate != null ? candidate.getBio() : null);
		return buildDebugPayload(PublicElectionSectionKeys.CANDIDATE_PROFILE, bindings);
	}

	private Map<String, Object> buildCandidateStatutoryDebugPayload(PublicElectionCoreSnapshot.CandidateData candidate) {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put(resolveCandidateCachePath(candidate) + ".statutoryAnswers", candidate != null ? candidate.getStatutoryAnswers() : null);
		return buildDebugPayload(PublicElectionSectionKeys.CANDIDATE_STATUTORY_QUESTIONS, bindings);
	}

	private Map<String, Object> buildCandidateNonStatutoryDebugPayload(PublicElectionCoreSnapshot.CandidateData candidate) {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put(resolveCandidateCachePath(candidate) + ".nonStatutoryAnswers", candidate != null ? candidate.getNonStatutoryAnswers() : null);
		return buildDebugPayload(PublicElectionSectionKeys.CANDIDATE_NON_STATUTORY_QUESTIONS, bindings);
	}

	private Map<String, Object> buildCandidateCommunityDebugPayload(PublicElectionCoreSnapshot.CandidateData candidate) {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put(resolveCandidateCachePath(candidate) + ".communityQuestions", buildCommunityQuestionsDebugData(candidate != null ? candidate.getCommunityQuestions() : null));
		return buildDebugPayload(PublicElectionSectionKeys.CANDIDATE_COMMUNITY_QUESTIONS, bindings);
	}

	private Map<String, Object> buildCandidateQuestionSubmissionDebugPayload(PublicElectionCoreSnapshot.CandidateData candidate) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("section", buildSectionDebugState(PublicElectionSectionKeys.CANDIDATE_QUESTION_SUBMISSION));
		payload.put("bindings", Collections.emptyMap());
		payload.put("cacheDrivesVisibilityOnly", Boolean.TRUE);
		return payload;
	}

	private Map<String, Object> buildCandidateSidebarDebugPayload(PublicElectionCoreSnapshot.CandidateData candidate) {
		Map<String, Object> bindings = new LinkedHashMap<>();
		String candidatePath = resolveCandidateCachePath(candidate);
		bindings.put(candidatePath + ".primaryCountryCode", candidate != null ? candidate.getPrimaryCountryCode() : null);
		bindings.put(candidatePath + ".otherCountryCodes", candidate != null ? candidate.getOtherCountryCodes() : null);
		bindings.put(candidatePath + ".workOrganizationNames", candidate != null ? candidate.getWorkOrganizationNames() : null);
		bindings.put(candidatePath + ".supportOrganizationNames", candidate != null ? candidate.getSupportOrganizationNames() : null);
		bindings.put(candidatePath + ".nominationOrganizationName", candidate != null ? candidate.getNominationOrganizationName() : null);
		bindings.put(candidatePath + ".nominationReason", candidate != null ? candidate.getNominationReason() : null);
		return buildDebugPayload(PublicElectionSectionKeys.CANDIDATE_SIDEBAR, bindings);
	}

	private Map<String, Object> buildRollDetailDebugPayload() {
		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put("rollSnapshot.getRows().totalCount", Integer.valueOf(safeList(rollSnapshot != null ? rollSnapshot.getRows() : null).size()));
		bindings.put("rollSnapshot.getRows().displayedPage", buildDisplayedRollRowsDebugData());
		bindings.put("rollSnapshot.pagination.currentPage", Integer.valueOf(rollPage));
		bindings.put("rollSnapshot.pagination.totalPages", Integer.valueOf(rollTotalPages));
		bindings.put("rollSnapshot.pagination.pageSize", Integer.valueOf(ROLL_PAGE_SIZE));
		bindings.put("rollSnapshot.filters.countryCode", rollCountryFilter);
		bindings.put("rollSnapshot.filters.organizationName", rollOrganizationFilter);
		return buildDebugPayload(PublicElectionSectionKeys.LANDING_ROLL, bindings);
	}

	private Map<String, Object> buildDebugPayload(String sectionKey, Map<String, Object> bindings) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("section", buildSectionDebugState(sectionKey));
		payload.put("bindings", bindings);
		return payload;
	}

	private List<PublicElectionCoreSnapshot.CandidateData> buildLandingCandidatesForDisplay() {
		List<PublicElectionCoreSnapshot.CandidateData> candidates = new ArrayList<>(safeList(coreSnapshot != null ? coreSnapshot.getCandidates() : null));
		if (Boolean.TRUE.equals(coreSnapshot != null && coreSnapshot.getElection() != null ? coreSnapshot.getElection().getRandomOrderCandidates() : null) && candidates.size() > 1) {
			Collections.shuffle(candidates);
		}
		return candidates;
	}

	private List<ResultRowView> buildWinnerCandidatesForDisplay() {
		List<ResultRowView> winners = new ArrayList<>();
		for (ResultRowView row : buildResultRows()) {
			if (row != null && row.isWinner()) {
				winners.add(row);
			}
		}
		return winners;
	}

	private boolean canLinkToCandidateProfile(Long candidateId) {
		return candidateId != null
				&& candidateId.longValue() > 0L
				&& isSectionVisible(PublicElectionSectionKeys.CANDIDATE_PROFILE_ROOT);
	}

	private List<Map<String, Object>> buildLandingCandidatesDebugRows(List<PublicElectionCoreSnapshot.CandidateData> displayedCandidates) {
		List<Map<String, Object>> rows = new ArrayList<>();
		for (PublicElectionCoreSnapshot.CandidateData candidate : safeList(displayedCandidates)) {
			rows.add(buildLandingCandidateCardDebugData(candidate));
		}
		return rows;
	}

	private List<Map<String, Object>> buildLandingCandidatePhotosDebugRows(List<PublicElectionCoreSnapshot.CandidateData> displayedCandidates) {
		List<Map<String, Object>> rows = new ArrayList<>();
		for (PublicElectionCoreSnapshot.CandidateData candidate : safeList(displayedCandidates)) {
			rows.add(buildPhotoDebugData(candidate != null ? photoByCandidateId.get(candidate.getCandidateId()) : null));
		}
		return rows;
	}

	private Map<String, Object> buildLandingCandidateCardDebugData(PublicElectionCoreSnapshot.CandidateData candidate) {
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("name", candidate != null ? candidate.getName() : null);
		data.put("workOrganizationNames", candidate != null ? candidate.getWorkOrganizationNames() : null);
		data.put("primaryCountryCode", candidate != null ? candidate.getPrimaryCountryCode() : null);
		data.put("bio", candidate != null ? candidate.getBio() : null);
		return data;
	}

	private Map<String, Object> buildPhotoDebugData(PublicElectionPhotoSnapshot.CandidatePhotoData photoData) {
		if (photoData == null) {
			return null;
		}
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("pictureExtension", photoData.getPictureExtension());
		data.put("pictureBytesLength", Integer.valueOf(photoData.getPictureBytes() != null ? photoData.getPictureBytes().length : 0));
		return data;
	}

	private List<Map<String, Object>> buildResultRowsDebugData(List<PublicElectionCoreSnapshot.ResultRowData> rows) {
		List<Map<String, Object>> values = new ArrayList<>();
		for (PublicElectionCoreSnapshot.ResultRowData row : safeList(rows)) {
			if (row == null) {
				continue;
			}
			Map<String, Object> data = new LinkedHashMap<>();
			data.put("candidateName", row.getCandidateName());
			data.put("voteCount", row.getVoteCount());
			data.put("percentage", row.getPercentage());
			data.put("winner", row.getWinner());
			values.add(data);
		}
		return values;
	}

	private List<Map<String, Object>> buildResultPublicationStagesDebugData(List<PublicElectionCoreSnapshot.ResultPublicationStageData> rows) {
		List<Map<String, Object>> values = new ArrayList<>();
		for (PublicElectionCoreSnapshot.ResultPublicationStageData row : safeList(rows)) {
			if (row == null || !Boolean.TRUE.equals(row.getConfigured())) {
				continue;
			}
			Map<String, Object> data = new LinkedHashMap<>();
			data.put("stageCode", row.getStageCode());
			data.put("startUtc", row.getStartUtc());
			data.put("endUtc", row.getEndUtc());
			data.put("current", row.getCurrent());
			values.add(data);
		}
		return values;
	}

	private Map<String, Object> buildRollSummaryDebugData() {
		Map<String, Object> data = new LinkedHashMap<>();
		PublicElectionCoreSnapshot.RollSummaryData rollSummary = coreSnapshot != null ? coreSnapshot.getRollSummary() : null;
		data.put("totalOrganizations", rollSummary != null ? rollSummary.getTotalOrganizations() : null);
		data.put("totalCountries", rollSummary != null ? rollSummary.getTotalCountries() : null);
		data.put("spanishVoters", rollSummary != null ? rollSummary.getSpanishVoters() : null);
		data.put("englishVoters", rollSummary != null ? rollSummary.getEnglishVoters() : null);
		data.put("portugueseVoters", rollSummary != null ? rollSummary.getPortugueseVoters() : null);
		return data;
	}

	private List<Map<String, Object>> buildCommunityQuestionsDebugData(List<PublicElectionCoreSnapshot.CommunityQuestionData> rows) {
		List<Map<String, Object>> values = new ArrayList<>();
		for (PublicElectionCoreSnapshot.CommunityQuestionData row : safeList(rows)) {
			if (row == null) {
				continue;
			}
			Map<String, Object> data = new LinkedHashMap<>();
			data.put("askedByInitials", row.getAskedByInitials());
			data.put("questionDateUtc", row.getQuestionDateUtc());
			data.put("answerDateUtc", row.getAnswerDateUtc());
			data.put("questionDisplayMode", row.getQuestionDisplayMode());
			data.put("answerDisplayMode", row.getAnswerDisplayMode());
			data.put("questionText", row.getQuestionText());
			data.put("answerText", row.getAnswerText());
			data.put("questionMaskedPreview", row.getQuestionMaskedPreview());
			data.put("answerMaskedPreview", row.getAnswerMaskedPreview());
			values.add(data);
		}
		return values;
	}

	private List<Map<String, Object>> buildDisplayedRollRowsDebugData() {
		List<Map<String, Object>> rows = new ArrayList<>();
		for (RollEntryView row : rollPageRows) {
			if (row == null) {
				continue;
			}
			Map<String, Object> data = new LinkedHashMap<>();
			data.put("countryCode", row.getCountryCode());
			data.put("organizationName", row.getOrganizationName());
			data.put("representativeMask", row.getMaskedRepresentative());
			rows.add(data);
		}
		return rows;
	}

	private String resolveCandidateCachePath(PublicElectionCoreSnapshot.CandidateData candidate) {
		Long candidateId = candidate != null ? candidate.getCandidateId() : null;
		return "coreSnapshot.getCandidates()[candidateId=" + (candidateId != null ? candidateId : "?") + "]";
	}

	private String resolvePhotoCachePath(Long candidateId) {
		return "photoSnapshot.getCandidates()[candidateId=" + (candidateId != null ? candidateId : "?") + "]";
	}

	private Map<String, Object> buildSectionDebugState(String sectionKey) {
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("key", sectionKey);
		data.put("visible", Boolean.valueOf(isSectionVisible(sectionKey)));
		data.put("reason", resolveSectionReason(sectionKey));
		return data;
	}

	private String resolveSectionReason(String sectionKey) {
		if (!hasText(sectionKey)
				|| coreSnapshot == null
				|| coreSnapshot.getVisibility() == null
				|| coreSnapshot.getVisibility().getReasons() == null) {
			return null;
		}
		return coreSnapshot.getVisibility().getReasons().get(sectionKey);
	}

	private String resolveSnapshotStatusCssClass(PublicElectionSnapshotMetadata metadata) {
		boolean stale = metadata != null && Boolean.TRUE.equals(metadata.getStale());
		return stale ? "alert alert-warning mb-3" : "alert alert-info mb-3";
	}

	private String resolveSnapshotFreshnessLabel(PublicElectionSnapshotMetadata metadata) {
		boolean stale = metadata != null && Boolean.TRUE.equals(metadata.getStale());
		return stale ? getString("publicElectionSnapshotFreshnessStale") : getString("publicElectionSnapshotFreshnessFresh");
	}

	private String resolveSnapshotFreshnessBadgeCssClass(PublicElectionSnapshotMetadata metadata) {
		boolean stale = metadata != null && Boolean.TRUE.equals(metadata.getStale());
		return stale ? BootstrapCssClasses.BADGE_TEXT_BG_WARNING : BootstrapCssClasses.BADGE_TEXT_BG_SUCCESS;
	}

	private String resolveSnapshotRefreshStatusLabel(String refreshStatus) {
		if ("READY".equalsIgnoreCase(refreshStatus)) {
			return getString("publicElectionSnapshotRefreshStatusReady");
		}
		if ("ERROR".equalsIgnoreCase(refreshStatus)) {
			return getString("publicElectionSnapshotRefreshStatusError");
		}
		if ("EMPTY".equalsIgnoreCase(refreshStatus)) {
			return getString("publicElectionSnapshotRefreshStatusEmpty");
		}
		return getString("publicElectionSnapshotRefreshStatusUnknown");
	}

	private boolean isOnlyBrRecoveryMode() {
		String modeCode = coreSnapshot != null && coreSnapshot.getRecovery() != null ? coreSnapshot.getRecovery().getModeCode() : null;
		if (hasText(modeCode)) {
			return ElectionLinkRecoveryMode.ONLY_BR.name().equalsIgnoreCase(modeCode);
		}
		return election != null && election.getPublicLinkRecoveryMode() == ElectionLinkRecoveryMode.ONLY_BR;
	}

	private String resolveNominationStatusLabel(String nominationStatusCode) {
		if (!hasText(nominationStatusCode)) {
			return "-";
		}
		if ("ACCEPTED_BY_CANDIDATE".equals(nominationStatusCode)) {
			return getString("publicElectionNominationStatusAccepted");
		}
		if (NominationStatus.PROPOSED.name().equals(nominationStatusCode)) {
			return getString("publicElectionNominationStatusPending");
		}
		if (NominationStatus.REJECTED_BY_CANDIDATE.name().equals(nominationStatusCode)) {
			return getString("publicElectionNominationStatusRejectedByCandidate");
		}
		if ("INVALID".equals(nominationStatusCode)) {
			return getString("publicElectionNominationStatusInvalid");
		}
		if ("APPROVED".equals(nominationStatusCode)) {
			return getString("publicElectionNominationStatusApproved");
		}
		return nominationStatusCode;
	}

	private String resolveNominationRowClass(String candidateStatusCode, String nominationStatusCode) {
		if (isCandidateConfirmedAndPublished(candidateStatusCode)) {
			return "table-success";
		}
		if (isNominationRejected(candidateStatusCode, nominationStatusCode)) {
			return "table-danger";
		}
		return "table-warning";
	}

	private int resolveNominationSortRank(String candidateStatusCode, String nominationStatusCode) {
		if (CANDIDATE_STATUS_CONFIRMED_AND_PUBLISHED.equals(candidateStatusCode)) {
			return 0;
		}
		if (isNominationRejected(candidateStatusCode, nominationStatusCode)) {
			return 1;
		}
		if (CANDIDATE_STATUS_COMPLETE.equals(candidateStatusCode)) {
			return 2;
		}
		if (NominationStatus.PROPOSED.name().equals(nominationStatusCode)
				|| NominationStatus.REJECTED_BY_CANDIDATE.name().equals(nominationStatusCode)) {
			return 4;
		}
		return 3;
	}

	private boolean isCandidateConfirmedAndPublished(String candidateStatusCode) {
		return CANDIDATE_STATUS_CONFIRMED_AND_PUBLISHED.equals(candidateStatusCode);
	}

	private boolean isNominationRejected(String candidateStatusCode, String nominationStatusCode) {
		return CANDIDATE_STATUS_REJECTED.equals(candidateStatusCode) || "INVALID".equals(nominationStatusCode);
	}

	private StatusBadge resolveCandidateStatusBadge(String candidateStatusCode, String nominationStatusCode) {
		if (isCandidateConfirmedAndPublished(candidateStatusCode)) {
			return new StatusBadge(getString("publicElectionCandidateStatusConfirmed"), BootstrapCssClasses.BADGE_TEXT_BG_SUCCESS);
		}
		if (CANDIDATE_STATUS_COMPLETE.equals(candidateStatusCode)) {
			return new StatusBadge(getString("publicElectionCandidateStatusCompleted"), BootstrapCssClasses.BADGE_TEXT_BG_WARNING);
		}
		if (isNominationRejected(candidateStatusCode, nominationStatusCode)) {
			return new StatusBadge(getString("publicElectionCandidateStatusRejected"), "badge text-bg-danger");
		}
		if (NominationStatus.PROPOSED.name().equals(nominationStatusCode) || NominationStatus.REJECTED_BY_CANDIDATE.name().equals(nominationStatusCode)) {
			return new StatusBadge(getString("publicElectionCandidateStatusNotStarted"), BootstrapCssClasses.BADGE_TEXT_BG_WARNING);
		}
		return new StatusBadge(getString("publicElectionCandidateStatusIncomplete"), BootstrapCssClasses.BADGE_TEXT_BG_WARNING);
	}

	private String buildTasksLabel(Integer completedPublicTasks, Integer totalPublicTasks) {
		int completed = completedPublicTasks != null ? completedPublicTasks.intValue() : 0;
		int total = totalPublicTasks != null ? totalPublicTasks.intValue() : 0;
		return completed + " / " + total;
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

	private boolean containsIgnoreCase(String value, String query) {
		if (!hasText(value) || !hasText(query)) {
			return false;
		}
		return value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
	}

	private String formatDateTime(Date date) {
		if (date == null) {
			return "-";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy · HH:mm", resolveUiLocale());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date) + " UTC";
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

	private long safeLong(Long value) {
		return value != null ? value.longValue() : 0L;
	}

	private double safeDouble(Double value) {
		return value != null ? value.doubleValue() : 0.0d;
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private boolean hasBinary(byte[] value) {
		return value != null && value.length > 0;
	}

	private WebMarkupContainer emptyContainer(String id) {
		WebMarkupContainer container = new WebMarkupContainer(id);
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(false);
		return container;
	}

	private void addEmptyPageScaffold() {
		add(emptyContainer("contentPanel"));
	}

	private List<Long> resolveQuestionTargetCandidateIds() {
		List<Long> candidateIds = new ArrayList<>();
		if (askSameQuestionToAllCandidates) {
			for (PublicElectionCoreSnapshot.CandidateData candidate : safeList(coreSnapshot != null ? coreSnapshot.getCandidates() : null)) {
				if (candidate != null && candidate.getCandidateId() != null && Boolean.TRUE.equals(candidate.getPublicCandidate())) {
					candidateIds.add(candidate.getCandidateId());
				}
			}
			return candidateIds;
		}
		if (selectedCandidate != null && selectedCandidate.getCandidateId() != null) {
			candidateIds.add(selectedCandidate.getCandidateId());
		}
		return candidateIds;
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

	private static class QuestionAnswerView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String questionLabel;
		private final String answer;
		private final String dateLabel;

		QuestionAnswerView(String questionLabel, String answer, String dateLabel) {
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

		CommunityQuestionView(String questionDateLabel, String answerDateLabel, String question, String answer,
				String askedByInitials, long sortQuestionId) {
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

		public String getCountryLabel() {
			return countryLabel;
		}

		public String getFlagImagePath() {
			return flagImagePath;
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
