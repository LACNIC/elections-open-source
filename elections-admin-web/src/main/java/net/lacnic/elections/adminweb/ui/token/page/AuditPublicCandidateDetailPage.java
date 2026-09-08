package net.lacnic.elections.adminweb.ui.token.page;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ByteArrayResource;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenTopHeaderPanel;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.token.CandidateBiographyUtils;
import net.lacnic.elections.adminweb.ui.token.TokenResourceKeys;
import net.lacnic.elections.adminweb.wicket.util.BootstrapCssClasses;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.ReminderFrequency;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateCountryLink;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateDeclarationCode;
import net.lacnic.elections.domain.pre.CandidateDeclarationDefinition;
import net.lacnic.elections.domain.pre.CandidateDeclarationInputType;
import net.lacnic.elections.domain.pre.CandidateDeclarationOptionDefinition;
import net.lacnic.elections.domain.pre.CandidateDeclarationsDefinition;
import net.lacnic.elections.domain.pre.CandidatePepDeclaration;
import net.lacnic.elections.domain.pre.CandidateWorkOrganization;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;
import net.lacnic.elections.domain.pre.WorkOrganizationType;
import net.lacnic.elections.utils.AuditorCandidateDecisionWindow;
import net.lacnic.elections.utils.CountryUtils;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
import net.lacnic.elections.campus.CampusClient;
public class AuditPublicCandidateDetailPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();
	private static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
	private static final int INCOMPATIBILITIES_TOTAL_QUESTIONS = 6;
	private static final int STATUTORY_TOTAL_QUESTIONS = 4;
	private static final int MAX_DECISION_COMMENT_LENGTH = 4000;
	private static final double TRAINING_SCORE_GREEN_MIN = 50d;
	private static final double TRAINING_SCORE_RED_MAX = 30d;
	private static final String LINK_PRIMARY_FW_SEMIBOLD = "link-primary fw-semibold";
	private static final String KEY_DECISION_DEADLINE_NOT_CONFIGURED = "auditPublicCandidateDetailDecisionDeadlineNotConfigured";
	private static final String KEY_DECISION_SAVE_ERROR = "auditPublicCandidateDetailDecisionSaveError";
	private static final String KEY_AVAILABILITY_PERIOD_NOT_CONFIGURED = "auditPublicV2AvailabilityPeriodNotConfigured";
	private static final String AVAILABILITY_BADGE_BASE_CLASS = "badge border text-uppercase fw-semibold";
	private static final String CARD_SUCCESS_CLASS = "card mb-3 border border-success shadow-sm";
	private static final String CARD_HEADER_SUCCESS_CLASS = "card-header bg-success text-white";
	private static final String KEY_SUMMARY_VALUE_OK = "auditPublicCandidateDetailSummaryValueOk";
	private static final String KEY_SUMMARY_DESC_ANSWERED = "auditPublicCandidateDetailSummaryDescAnswered";
	private static final String[] DEFAULT_PICTURE_NAME_MARKERS = {
			"default_candidate_photo", "default-profile-picture", "foto_candidato_default"
	};

	private Auditor auditor;
	private Election election;
	private Candidate candidate;
	private Nomination nomination;
	private StageDecisionView preStageDecision = StageDecisionView.empty();
	private StageDecisionView completeStageDecision = StageDecisionView.empty();

	private List<SummaryCardView> summaryCards = Collections.emptyList();
	private List<AnswerItemView> incompatibilityAnswers = Collections.emptyList();
	private List<AnswerItemView> statutoryAnswers = Collections.emptyList();
	private List<AnswerItemView> nonStatutoryAnswers = Collections.emptyList();
	private List<AnswerItemView> statutoryDeclarationAnswers = Collections.emptyList();
	private List<AnswerItemView> nonStatutoryDeclarationAnswers = Collections.emptyList();
	private List<SupportItemView> supportRows = Collections.emptyList();
	private List<WorkOrganizationItemView> organizationRows = Collections.emptyList();
	private List<CountryLinkItemView> countryRows = Collections.emptyList();
	private List<String> restrictedCountryCodes = Collections.emptyList();
	private boolean showNoRegionalCitizenshipWarning;
	private Boolean hasOrganizationRelationship;
	private List<CandidateElectionTaskProgress> taskProgressRows = Collections.emptyList();
	private Set<ElectionTaskKey> enabledTaskKeys = Collections.emptySet();

	private String electionTitle = "-";
	private String candidateName = "-";
	private String candidateMail = "-";
	private String profileLinkedin = "-";
	private String nominationOrganizationName = "-";
	private String nominationOrganizationId = "-";
	private String nominationOrganizationCountry = "-";
	private String nominationContactName = "-";
	private String headerPhaseLabel = "-";
	private String headerMilestoneLabel = "-";
	private String decisionCandidatesToElect = "-";
	private String decisionCurrentDate = "-";
	private String decisionContextParagraph = "-";
	private String preDecisionDeadlineDateLabel = "-";
	private String preDecisionDeadlineRemainingLabel = "-";
	private String finalDecisionDeadlineDateLabel = "-";
	private String finalDecisionDeadlineRemainingLabel = "-";

	private AuditorCandidateDecisionStatus selectedPreAuditorDecisionStatus;
	private String preAuditorDecisionComment = "";
	private AuditorCandidateDecisionStatus selectedAuditorDecisionStatus;
	private String auditorDecisionComment = "";

	private String profileBio = "-";
	private String proctorioScore = "-";
	private boolean proctorioEnabledForElection;
	private boolean hasProctorioFile;
	private CandidateElectionTaskStatus proctorioTaskStatus;
	private String proctorioCompletionBadgeText = "-";
	private String proctorioCompletionBadgeClass = BootstrapCssClasses.BADGE_WARNING_SUBTLE_TEXT_WARNING;

	public AuditPublicCandidateDetailPage() {
		this(new PageParameters());
	}

	public AuditPublicCandidateDetailPage(PageParameters params) {
		super(params);

		loadData();

		add(new FeedbackPanel("feedbackPanel"));
		add(new Label("candidateName", valueOrDash(candidateName)));
		add(new Label("candidateMail", valueOrDash(candidateMail)));
		CandidateStatus candidateStatus = candidate != null ? candidate.getStatus() : null;
		String decisionSupportEmail = resolveDecisionSupportEmail();
		boolean hasDecisionSupportEmail = hasText(decisionSupportEmail);
		boolean candidateVerificationAllowed = isCurrentAuditorCommissioner();
		Date decisionReferenceDate = new Date();
		boolean candidateDecisionWindowActive = isCandidateDecisionWindowActive(decisionReferenceDate);
		boolean preDecisionEditableByCandidateStatus = isPreDecisionEditable(candidateStatus);
		boolean preDecisionLockedByAuditor = preStageDecision.hasReaction();
		boolean preDecisionPendingOutsideWindow = isDecisionPendingOutsideWindow(
				candidateVerificationAllowed,
				preDecisionEditableByCandidateStatus,
				preDecisionLockedByAuditor,
				candidateDecisionWindowActive);
		boolean decisionEditableByCandidateStatus = isCompleteDecisionEditable(candidateStatus);
		boolean decisionLockedByAuditor = completeStageDecision.hasReaction();
		boolean decisionPendingOutsideWindow = isDecisionPendingOutsideWindow(
				candidateVerificationAllowed,
				decisionEditableByCandidateStatus,
				decisionLockedByAuditor,
				candidateDecisionWindowActive);
		add(buildCandidateDecisionAvailabilityCard(
				preDecisionPendingOutsideWindow || decisionPendingOutsideWindow,
				decisionReferenceDate));

		WebMarkupContainer preAuditDecisionCard = new WebMarkupContainer("preAuditDecisionCard");
		preAuditDecisionCard.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveDecisionCardClass(preStageDecision.getStatus(), CandidateStatus.PRECOMPLETE)));
		boolean preDecisionCardVisible = candidateVerificationAllowed
				&& isPreDecisionCardVisible(candidateStatus, preStageDecision)
				&& !preDecisionPendingOutsideWindow;
		preAuditDecisionCard.setVisible(preDecisionCardVisible);
		add(preAuditDecisionCard);

		WebMarkupContainer preAuditDecisionCardHeader = new WebMarkupContainer("preAuditDecisionCardHeader");
		preAuditDecisionCardHeader.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveDecisionCardHeaderClass(preStageDecision.getStatus(), CandidateStatus.PRECOMPLETE)));
		preAuditDecisionCard.add(preAuditDecisionCardHeader);

		Form<Void> preAuditDecisionForm = buildPreAuditorDecisionForm(buildTokenPageParameters());
		preAuditDecisionForm.setVisible(shouldShowDecisionForm(
				candidateVerificationAllowed,
				preDecisionEditableByCandidateStatus,
				preDecisionLockedByAuditor,
				candidateDecisionWindowActive));
		preAuditDecisionCard.add(preAuditDecisionForm);

		WebMarkupContainer preAuditDecisionCompletedContainer = new WebMarkupContainer("preAuditDecisionCompletedContainer");
		boolean preDecisionCompletedVisible = preDecisionCardVisible
				&& (!preDecisionEditableByCandidateStatus || preDecisionLockedByAuditor);
		preAuditDecisionCompletedContainer.setVisible(preDecisionCompletedVisible);
		preAuditDecisionCompletedContainer.add(new Label(
				"preDecisionCompletedStatus",
				valueOrDash(resolveStageDecisionCompletedStatusText(preStageDecision.getStatus(), DecisionStage.PRECOMPLETE))));
		Label preDecisionCompletedSupportMailLink = new Label(
				"preDecisionCompletedSupportMailLink",
				buildAnchorHtml(
						hasDecisionSupportEmail ? "mailto:" + decisionSupportEmail : null,
						valueOrDash(decisionSupportEmail),
						LINK_PRIMARY_FW_SEMIBOLD));
		preDecisionCompletedSupportMailLink.setEscapeModelStrings(false);
		preDecisionCompletedSupportMailLink.setVisible(hasDecisionSupportEmail);
		preAuditDecisionCompletedContainer.add(preDecisionCompletedSupportMailLink);
		preAuditDecisionCompletedContainer.add(new Label("preDecisionCompletedSupportMailText", valueOrDash(decisionSupportEmail))
				.setVisible(!hasDecisionSupportEmail));
		preAuditDecisionCard.add(preAuditDecisionCompletedContainer);

		WebMarkupContainer preAuditDecisionCommentContainer = new WebMarkupContainer("preAuditDecisionCommentContainer");
		preAuditDecisionCommentContainer.setVisible(preDecisionCompletedVisible);
		preAuditDecisionCommentContainer.add(new MultiLineLabel("preDecisionCompletedComment", valueOrDash(preStageDecision.getComment())));
		preAuditDecisionCard.add(preAuditDecisionCommentContainer);

		preAuditDecisionCard.add(new Label("preDecisionDeadlineDate", valueOrDash(preDecisionDeadlineDateLabel)));
		preAuditDecisionCard.add(new Label("preDecisionDeadlineRemaining", valueOrDash(preDecisionDeadlineRemainingLabel)));
		WebMarkupContainer auditDecisionCard = new WebMarkupContainer("auditDecisionCard");
		auditDecisionCard.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveDecisionCardClass(completeStageDecision.getStatus(), candidateStatus)));
		boolean completeDecisionCardVisible = candidateVerificationAllowed
				&& isCompleteDecisionCardVisible(candidateStatus, completeStageDecision)
				&& !decisionPendingOutsideWindow;
		auditDecisionCard.setVisible(completeDecisionCardVisible);
		add(auditDecisionCard);

		WebMarkupContainer auditDecisionCardHeader = new WebMarkupContainer("auditDecisionCardHeader");
		auditDecisionCardHeader.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveDecisionCardHeaderClass(completeStageDecision.getStatus(), candidateStatus)));
		auditDecisionCard.add(auditDecisionCardHeader);

		String profileLinkedinUrl = normalizeExternalUrl(profileLinkedin);
		boolean hasProfileLinkedin = hasText(profileLinkedinUrl);
		Label profileLinkedinLink = new Label("profileLinkedinLink", buildAnchorHtml(profileLinkedinUrl, valueOrDash(profileLinkedin), LINK_PRIMARY_FW_SEMIBOLD));
		profileLinkedinLink.setEscapeModelStrings(false);
		profileLinkedinLink.setVisible(hasProfileLinkedin);
		add(profileLinkedinLink);
		add(new Label("profileLinkedinNotAvailable", "-").setVisible(!hasProfileLinkedin));
		Form<Void> auditDecisionForm = buildAuditorDecisionForm(buildTokenPageParameters());
		auditDecisionForm.setVisible(shouldShowDecisionForm(
				candidateVerificationAllowed,
				decisionEditableByCandidateStatus,
				decisionLockedByAuditor,
				candidateDecisionWindowActive));
		auditDecisionCard.add(auditDecisionForm);

		WebMarkupContainer auditDecisionCompletedContainer = new WebMarkupContainer("auditDecisionCompletedContainer");
		boolean decisionLocked = decisionLockedByAuditor || isDecisionLockedByCandidateStatus(candidateStatus);
		boolean decisionCompletedVisible = completeDecisionCardVisible && (decisionLocked || !decisionEditableByCandidateStatus);
		auditDecisionCompletedContainer.setVisible(decisionCompletedVisible);
		auditDecisionCompletedContainer.add(new Label(
				"decisionCompletedStatus",
				valueOrDash(resolveStageDecisionCompletedStatusText(completeStageDecision.getStatus(), DecisionStage.COMPLETE))));
		Label decisionCompletedSupportMailLink = new Label(
				"decisionCompletedSupportMailLink",
				buildAnchorHtml(hasDecisionSupportEmail ? "mailto:" + decisionSupportEmail : null, valueOrDash(decisionSupportEmail), LINK_PRIMARY_FW_SEMIBOLD));
		decisionCompletedSupportMailLink.setEscapeModelStrings(false);
		decisionCompletedSupportMailLink.setVisible(hasDecisionSupportEmail);
		auditDecisionCompletedContainer.add(decisionCompletedSupportMailLink);
		auditDecisionCompletedContainer.add(new Label("decisionCompletedSupportMailText", valueOrDash(decisionSupportEmail))
				.setVisible(!hasDecisionSupportEmail));
		auditDecisionCard.add(auditDecisionCompletedContainer);

		WebMarkupContainer auditDecisionCommentContainer = new WebMarkupContainer("auditDecisionCommentContainer");
		auditDecisionCommentContainer.setVisible(decisionCompletedVisible);
		auditDecisionCommentContainer.add(new MultiLineLabel("decisionCompletedComment", valueOrDash(completeStageDecision.getComment())));
		auditDecisionCard.add(auditDecisionCommentContainer);

		auditDecisionCard.add(new Label("decisionDeadlineDate", valueOrDash(finalDecisionDeadlineDateLabel)));
		auditDecisionCard.add(new Label("decisionDeadlineRemaining", valueOrDash(finalDecisionDeadlineRemainingLabel)));
		boolean profileTaskEnabled = isTaskEnabled(ElectionTaskKey.PROFILE);
		boolean incompatibilityTaskEnabled = isTaskEnabled(ElectionTaskKey.INCOMPATIBILITIES);
		boolean statutoryTaskEnabled = isTaskEnabled(ElectionTaskKey.OTHER_STATUTORY_QUESTIONS);
		boolean nonStatutoryTaskEnabled = isTaskEnabled(ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS);
		boolean statutoryDeclarationTaskEnabled = isTaskEnabled(ElectionTaskKey.DECLARATIONS) && !statutoryDeclarationAnswers.isEmpty();
		boolean nonStatutoryDeclarationTaskEnabled = isTaskEnabled(ElectionTaskKey.DECLARATIONS_NON_STATUTORY) && !nonStatutoryDeclarationAnswers.isEmpty();
		boolean countryTaskEnabled = isTaskEnabled(ElectionTaskKey.COUNTRIES);
		boolean organizationTaskEnabled = isTaskEnabled(ElectionTaskKey.ORGANIZATIONS);
		boolean supportTaskEnabled = isAnyTaskEnabled(ElectionTaskKey.ORG_SUPPORTS, ElectionTaskKey.USER_SUPPORTS_2, ElectionTaskKey.USER_SUPPORTS_5);

		add(buildTaskCardVisibilityMarker("profileTaskCardVisibility", profileTaskEnabled));
		add(buildTaskCardVisibilityMarker("supportTaskCardVisibility", supportTaskEnabled));
		add(buildTaskCardVisibilityMarker("organizationTaskCardVisibility", organizationTaskEnabled));
		add(buildTaskCardVisibilityMarker("incompatibilityTaskCardVisibility", incompatibilityTaskEnabled));
		add(buildTaskCardVisibilityMarker("statutoryTaskCardVisibility", statutoryTaskEnabled));
		add(buildTaskCardVisibilityMarker("nonStatutoryTaskCardVisibility", nonStatutoryTaskEnabled));
		add(buildTaskCardVisibilityMarker("declarationTaskCardVisibility", statutoryDeclarationTaskEnabled));
		add(buildTaskCardVisibilityMarker("nonStatutoryDeclarationTaskCardVisibility", nonStatutoryDeclarationTaskEnabled));
		add(buildTaskCardVisibilityMarker("countryTaskCardVisibility", countryTaskEnabled));

		Component candidatePicture = buildCandidatePicture("candidatePicture");
		candidatePicture.setVisible(profileTaskEnabled);
		add(candidatePicture);

		Label profileBioLabel = new Label("profileBio", CandidateBiographyUtils.toRenderableMarkup(valueOrDash(profileBio)));
		profileBioLabel.setEscapeModelStrings(false);
		profileBioLabel.setVisible(profileTaskEnabled);
		add(profileBioLabel);

		WebMarkupContainer nominationCard = new WebMarkupContainer("nominationCard");
		nominationCard.setVisible(hasNominationInformation());
		nominationCard.add(new Label("nominationOrganizationName", valueOrDash(nominationOrganizationName)));
		nominationCard.add(new Label("nominationOrganizationId", valueOrDash(nominationOrganizationId)));
		nominationCard.add(new Label("nominationOrganizationCountry", valueOrDash(nominationOrganizationCountry)));
		nominationCard.add(new Label("nominationContactName", valueOrDash(nominationContactName)));
		add(nominationCard);

		add(buildSummaryCardsView());
		add(buildTaskCompletionBadge("incompatibilityCompletionBadge", ElectionTaskKey.INCOMPATIBILITIES));
		add(buildTaskCompletionBadge("statutoryCompletionBadge", ElectionTaskKey.OTHER_STATUTORY_QUESTIONS));
		add(buildTaskCompletionBadge("nonStatutoryCompletionBadge", ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS));
		add(buildTaskCompletionBadge("declarationCompletionBadge", ElectionTaskKey.DECLARATIONS));
		add(buildTaskCompletionBadge("nonStatutoryDeclarationCompletionBadge", ElectionTaskKey.DECLARATIONS_NON_STATUTORY));
		add(buildTaskCompletionBadge("countryCompletionBadge", ElectionTaskKey.COUNTRIES));
		add(buildTaskCompletionBadge("supportCompletionBadge", ElectionTaskKey.ORG_SUPPORTS, ElectionTaskKey.USER_SUPPORTS_2, ElectionTaskKey.USER_SUPPORTS_5));
		add(buildTaskCompletionBadge("organizationCompletionBadge", ElectionTaskKey.ORGANIZATIONS));
		ListView<AnswerItemView> incompatibilityRowsView = buildAnswerList("incompatibilityRows", incompatibilityAnswers, true);
		incompatibilityRowsView.setVisible(incompatibilityTaskEnabled);
		add(incompatibilityRowsView);

		ListView<AnswerItemView> statutoryRowsView = buildAnswerList("statutoryRows", statutoryAnswers, false);
		statutoryRowsView.setVisible(statutoryTaskEnabled);
		add(statutoryRowsView);

		ListView<AnswerItemView> nonStatutoryRowsView = buildAnswerList("nonStatutoryRows", nonStatutoryAnswers, false);
		nonStatutoryRowsView.setVisible(nonStatutoryTaskEnabled);
		add(nonStatutoryRowsView);

		ListView<AnswerItemView> declarationRowsView = buildDeclarationList("declarationRows", statutoryDeclarationAnswers);
		declarationRowsView.setVisible(statutoryDeclarationTaskEnabled);
		add(declarationRowsView);

		ListView<AnswerItemView> nonStatutoryDeclarationRowsView = buildDeclarationList("nonStatutoryDeclarationRows", nonStatutoryDeclarationAnswers);
		nonStatutoryDeclarationRowsView.setVisible(nonStatutoryDeclarationTaskEnabled);
		add(nonStatutoryDeclarationRowsView);

		ListView<WorkOrganizationItemView> organizationsView = buildOrganizationsView();
		organizationsView.setVisible(organizationTaskEnabled);
		add(organizationsView);
		add(new Label("organizationQuestionLabel", getString("acceptNominationOrganizationsQuestion")));
		add(new Label("organizationAnswerValue", resolveOrganizationAnswerLabel()));
		add(new Label("organizationInfoText", resolveOrganizationInfoText()));

		ListView<SupportItemView> supportsView = buildSupportsView();
		supportsView.setVisible(supportTaskEnabled);
		add(supportsView);
		add(new WebMarkupContainer("supportEmptyMessage")
				.setVisible(supportTaskEnabled && supportRows.isEmpty()));

		List<CountryLinkItemView> primaryCountryRows = filterCountryRows(true);
		List<CountryLinkItemView> secondaryCountryRows = filterCountryRows(false);

		WebMarkupContainer primaryCountrySection = new WebMarkupContainer("primaryCountrySection");
		primaryCountrySection.setVisible(countryTaskEnabled && !primaryCountryRows.isEmpty());
		primaryCountrySection.add(buildCountryLinksView("primaryCountryRows", primaryCountryRows));
		add(primaryCountrySection);

		WebMarkupContainer secondaryCountrySection = new WebMarkupContainer("secondaryCountrySection");
		secondaryCountrySection.setVisible(countryTaskEnabled && !secondaryCountryRows.isEmpty());
		secondaryCountrySection.add(buildCountryLinksView("secondaryCountryRows", secondaryCountryRows));
		add(secondaryCountrySection);

		WebMarkupContainer noRegionalCitizenshipWarning = new WebMarkupContainer("noRegionalCitizenshipWarning");
		noRegionalCitizenshipWarning.setOutputMarkupPlaceholderTag(true);
		noRegionalCitizenshipWarning.setVisible(countryTaskEnabled && showNoRegionalCitizenshipWarning);
		add(noRegionalCitizenshipWarning);

		WebMarkupContainer proctorioCard = new WebMarkupContainer("proctorioCard");
		proctorioCard.setVisible(proctorioEnabledForElection);
		add(proctorioCard);

		Label proctorioCompletionBadge = new Label("proctorioCompletionBadge", valueOrDash(proctorioCompletionBadgeText));
		proctorioCompletionBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  valueOrDash(proctorioCompletionBadgeClass)));
		proctorioCard.add(proctorioCompletionBadge);
		proctorioCard.add(new Label("proctorioScore", valueOrDash(proctorioScore)));
		proctorioCard.add(buildProctorioDownloadLink());
		proctorioCard.add(new WebMarkupContainer("proctorioUnavailable")
				.setVisible(proctorioEnabledForElection && !hasProctorioFile));

	}

	@Override
	protected boolean isGlobalFeedbackEnabled() {
		return true;
	}

	@Override
	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionAudit");
	}

	@Override
	protected PublicTokenTopHeaderPanel.NotificationMode resolveTopHeaderNotificationMode() {
		return PublicTokenTopHeaderPanel.NotificationMode.AUDIT;
	}

	@Override
	protected ReminderFrequency resolveTopHeaderReminderFrequency() {
		return auditor != null ? auditor.getReminderFrequency() : ReminderFrequency.defaultValue();
	}

	@Override
	protected Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params) {
		auditor = AppContext.getInstance().getVoterBeanRemote().verifyAuditorResultAccess(getToken());
		if (auditor == null) {
			return Error404.class;
		}

		election = auditor.getElection();
		if (election == null) {
			return Error404.class;
		}
		setElection(election);

		long candidateId = UtilsParameters.getCandidateAsLong(params);
		if (candidateId <= 0) {
			return Error404.class;
		}
		try {
			Candidate loadedCandidate = AppContext.getInstance().getManagerBeanRemote().getCandidate(candidateId);
			if (loadedCandidate == null || loadedCandidate.getElection() == null || loadedCandidate.getElection().getElectionId() != election.getElectionId()
					|| !isVisibleCandidateStatusForAuditor(loadedCandidate.getStatus())) {
				return Error404.class;
			}
			candidate = loadedCandidate;
		} catch (Exception e) {
			return Error404.class;
		}

		setHeaderUserDisplay(auditor.getName());
		setHeaderNotificationStatusFromReminderFrequency(auditor.getReminderFrequency());
		setWhereAmI("Detalle público de candidatura para auditoría");
		setContextClass(Candidate.class.getName());
		setContextData("candidateId: " + candidate.getCandidateId() + "\n"
				+ "candidateName: " + candidate.getName() + "\n"
				+ "candidateStatus: " + candidate.getStatus() + "\n"
				+ "auditorId: " + auditor.getAuditorId() + "\n"
				+ "electionId: " + election.getElectionId());
		return null;
	}

	@Override
	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		if (election != null && !election.isAuditorLinkAvailable()) {
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
					PublicAccessDeniedPage.ErrorCode.AUDIT_NOT_PUBLIC,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
					null,
					null));
		}
		TokenAccessGate.AccessBlock before = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.AUDIT_NOT_PUBLIC,
				PublicAccessDeniedPage.CountdownTargetDate.START,
				ElectionCalendarKey.N_1_SINGLE_CALL_FOR_CANDIDATES_PUBLISHED,
				null,
				null);
		return TokenAccessGate.of(calendar.getStartDate(), null, before, null);
	}

	private void loadData() {
		if (candidate == null || election == null) {
			return;
		}

		electionTitle = candidate.getElection() != null ? candidate.getElection().getTitle(SecurityUtils.getLocale().getLanguage()) : null;
		if (!hasText(electionTitle)) {
			electionTitle = candidate.getElection() != null ? candidate.getElection().getTitleSpanish() : "-";
		}
		resolveHeaderTimelineContext(election != null ? election.getElectionId() : 0L);

		candidateName = candidate.getName();
		candidateMail = candidate.getMail();
		profileLinkedin = candidate.getLinkedinUrl();

		profileBio = valueOrDash(resolveLocalizedBio());

		AuditorDecisionView currentDecision = resolveCurrentDecision(election.getElectionId(), auditor != null ? auditor.getAuditorId() : null, candidate.getCandidateId());
		preStageDecision = resolvePreStageDecisionView(currentDecision, candidate.getStatus());
		completeStageDecision = resolveCompleteStageDecisionView(currentDecision, candidate.getStatus());
		selectedPreAuditorDecisionStatus = preStageDecision.isEditableStatus() ? preStageDecision.getStatus() : null;
		preAuditorDecisionComment = valueOrEmpty(preStageDecision.getComment());
		selectedAuditorDecisionStatus = completeStageDecision.isEditableStatus() ? completeStageDecision.getStatus() : null;
		auditorDecisionComment = valueOrEmpty(completeStageDecision.getComment());
			decisionCandidatesToElect = election != null && election.getMaxCandidates() > 0 ? String.valueOf(election.getMaxCandidates()) : "-";
			decisionCurrentDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, SecurityUtils.getLocale()).format(new Date());
			decisionContextParagraph = getString("auditPublicCandidateDetailDecisionContextParagraph");
			CalendarRange firstReactionRange = resolveFirstReactionDecisionWindow();
			preDecisionDeadlineDateLabel = firstReactionRange.getEnd() != null
					? formatDateTime(firstReactionRange.getEnd())
					: getString(KEY_DECISION_DEADLINE_NOT_CONFIGURED);
			preDecisionDeadlineRemainingLabel = resolveDecisionDeadlineRemaining(firstReactionRange.getEnd());

			CalendarRange finalDecisionRange = resolveFinalDecisionWindow();
			finalDecisionDeadlineDateLabel = finalDecisionRange.getEnd() != null
					? formatDateTime(finalDecisionRange.getEnd())
					: getString(KEY_DECISION_DEADLINE_NOT_CONFIGURED);
			finalDecisionDeadlineRemainingLabel = resolveDecisionDeadlineRemaining(finalDecisionRange.getEnd());

			nomination = resolveNominationWithDetails(election.getElectionId(), candidate.getCandidateId());
			resolveNominationCardData();
			organizationRows = buildWorkOrganizationRows(loadCandidateOrganizations(election.getElectionId(), candidate.getCandidateId(), candidate));
			hasOrganizationRelationship = resolveHasOrganizationRelationship(candidate, organizationRows);
			countryRows = buildCountryRows(loadCandidateCountryLinks(election.getElectionId(), candidate.getCandidateId(), candidate));
			restrictedCountryCodes = loadRestrictedCountryCodes(election.getElectionId());
			showNoRegionalCitizenshipWarning = isStatutoryElection() && !safeList(countryRows).isEmpty() && !hasCitizenshipInLacnicRegion(countryRows);
			supportRows = buildSupportRows(safeList(nomination != null ? nomination.getSupports() : null));
			enabledTaskKeys = loadEnabledTaskKeys(election != null ? election.getElectionId() : 0L);

		incompatibilityAnswers = buildIncompatibilityAnswers();
		statutoryAnswers = buildOtherStatutoryAnswers();
		nonStatutoryAnswers = buildOtherNonStatutoryAnswers();
		statutoryDeclarationAnswers = buildDeclarationAnswers(ElectionTaskKey.DECLARATIONS);
		nonStatutoryDeclarationAnswers = buildDeclarationAnswers(ElectionTaskKey.DECLARATIONS_NON_STATUTORY);

		taskProgressRows = getCandidateTaskProgressRows(candidate, nomination);
		proctorioEnabledForElection = CampusClient.isCampusIntegrationEnabled()
				&& isAnyTaskEnabled(ElectionTaskKey.COURSE, ElectionTaskKey.EVALUATION);
		proctorioTaskStatus = resolveSummaryTaskStatus(taskProgressRows, ElectionTaskKey.COURSE, ElectionTaskKey.EVALUATION);
		byte[] proctorioResult = candidate.getProctorioResultFile();
		hasProctorioFile = proctorioResult != null && proctorioResult.length > 0;
		if (proctorioEnabledForElection) {
			proctorioScore = hasText(candidate.getCampusCourseCalification())
					? candidate.getCampusCourseCalification()
					: getString("candidateAnswersLinkNotAvailable");
		} else {
			proctorioScore = "-";
		}
		proctorioCompletionBadgeText = resolveTaskStatusLabel(proctorioTaskStatus);
		proctorioCompletionBadgeClass = resolveTaskStatusBadgeClass(proctorioTaskStatus);
		summaryCards = buildSummaryCards(taskProgressRows);
	}

	private CalendarRange resolveFirstReactionDecisionWindow() {
		return resolveCalendarWindow(ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION);
	}

	private CalendarRange resolveFinalDecisionWindow() {
		CalendarRange n7Window = resolveCalendarWindow(ElectionCalendarKey.N_7_PERIODO_CANDIDATE_FORMS_VERIFICATION);
		CalendarRange n8Window = resolveCalendarWindow(ElectionCalendarKey.N_8_PERIODO_EVALUATIONS_VALIDATION);
		Date finalDeadline = maxDate(n7Window.getEnd(), n8Window.getEnd());
		return new CalendarRange(minDate(n7Window.getStart(), n8Window.getStart()), finalDeadline);
	}

	private WebMarkupContainer buildCandidateDecisionAvailabilityCard(boolean visible, Date referenceDate) {
		CalendarRange configuredWindow = resolveCandidateDecisionAvailabilityWindow();
		boolean windowConfigured = configuredWindow.getStart() != null && configuredWindow.getEnd() != null;
		boolean windowClosed = windowConfigured
				&& ((election != null && election.isClosed()) || (referenceDate != null && referenceDate.after(configuredWindow.getEnd())));
		WebMarkupContainer container = new WebMarkupContainer("candidateDecisionAvailabilityCard");
		container.setVisible(visible);

		Label statusBadge = new Label("candidateDecisionAvailabilityStatusBadge", resolveCandidateDecisionAvailabilityStatusText(windowConfigured, windowClosed));
		statusBadge.add(AttributeModifier.replace(
				MarkupLiterals.HTML_ATTRIBUTE_CLASS,
				resolveCandidateDecisionAvailabilityBadgeClass(windowConfigured, windowClosed)));
		container.add(statusBadge);

		Label periodNotConfigured = new Label("candidateDecisionAvailabilityPeriodNotConfigured", getString(KEY_AVAILABILITY_PERIOD_NOT_CONFIGURED));
		periodNotConfigured.setVisible(!windowConfigured);
		container.add(periodNotConfigured);

		WebMarkupContainer periodContent = new WebMarkupContainer("candidateDecisionAvailabilityPeriodContent");
		periodContent.setVisible(windowConfigured);
		periodContent.add(new Label("candidateDecisionAvailabilityStartDate", formatDateTime(configuredWindow.getStart())));
		periodContent.add(new Label("candidateDecisionAvailabilityEndDate", formatDateTime(configuredWindow.getEnd())));
		container.add(periodContent);
		return container;
	}

	private CalendarRange resolveCandidateDecisionAvailabilityWindow() {
		Date earliestStart = null;
		Date latestEnd = null;
		for (ElectionCalendarKey calendarKey : AuditorCandidateDecisionWindow.ALLOWED_CALENDAR_KEYS) {
			ElectionCalendar calendar = findPublicCalendar(calendarKey);
			if (calendar == null || calendar.getStartDate() == null || calendar.getEndDate() == null) {
				continue;
			}
			earliestStart = minDate(earliestStart, calendar.getStartDate());
			latestEnd = maxDate(latestEnd, calendar.getEndDate());
		}
		return new CalendarRange(earliestStart, latestEnd);
	}

	private boolean isCandidateDecisionWindowActive(Date referenceDate) {
		return election != null
				&& !election.isClosed()
				&& election.isAuditorLinkAvailable()
				&& AuditorCandidateDecisionWindow.isActive(getPublicCalendars(), referenceDate);
	}

	private String resolveCandidateDecisionAvailabilityStatusText(boolean windowConfigured, boolean windowClosed) {
		if (!windowConfigured) {
			return getString("auditPublicV2AvailabilityStatusNotConfigured");
		}
		if (windowClosed) {
			return getString("auditPublicV2AvailabilityStatusClosed");
		}
		return getString("auditPublicV2AvailabilityStatusUpcoming");
	}

	private String resolveCandidateDecisionAvailabilityBadgeClass(boolean windowConfigured, boolean windowClosed) {
		if (!windowConfigured) {
			return AVAILABILITY_BADGE_BASE_CLASS + " text-bg-dark";
		}
		if (windowClosed) {
			return AVAILABILITY_BADGE_BASE_CLASS + " text-bg-secondary";
		}
		return AVAILABILITY_BADGE_BASE_CLASS + " text-bg-warning";
	}

	static boolean shouldShowDecisionForm(boolean candidateVerificationAllowed, boolean stageEditable, boolean locked, boolean windowActive) {
		return candidateVerificationAllowed && stageEditable && !locked && windowActive;
	}

	static boolean isDecisionPendingOutsideWindow(boolean candidateVerificationAllowed, boolean stageEditable, boolean locked, boolean windowActive) {
		return candidateVerificationAllowed && stageEditable && !locked && !windowActive;
	}

	private CalendarRange resolveCalendarWindow(ElectionCalendarKey key) {
		ElectionCalendar calendar = findPublicCalendar(key);
		if (calendar == null || calendar.getStartDate() == null) {
			return new CalendarRange(null, null);
		}
		Date start = calendar.getStartDate();
		Date end = calendar.getEndDate() != null ? calendar.getEndDate() : start;
		return new CalendarRange(start, end);
	}

	private Date maxDate(Date left, Date right) {
		if (left == null) {
			return right;
		}
		if (right == null) {
			return left;
		}
		return left.after(right) ? left : right;
	}

	private Date minDate(Date left, Date right) {
		if (left == null) {
			return right;
		}
		if (right == null) {
			return left;
		}
		return left.before(right) ? left : right;
	}

	private String resolveDecisionDeadlineRemaining(Date deadline) {
		if (deadline == null) {
			return getString(KEY_DECISION_DEADLINE_NOT_CONFIGURED);
		}
		Date now = new Date();
		if (now.after(deadline)) {
			return getString("auditPublicCandidateDetailDecisionDeadlineExpired");
		}
		long remainingMillis = deadline.getTime() - now.getTime();
		long remainingHours = Math.max(1L, (remainingMillis + (60L * 60L * 1000L - 1L)) / (60L * 60L * 1000L));
		if (remainingHours < 24L) {
			return MessageFormat.format(getString("auditPublicCandidateDetailDecisionDeadlineHoursRemaining"), Long.valueOf(remainingHours));
		}
		long remainingDays = Math.max(1L, (remainingMillis + (24L * 60L * 60L * 1000L - 1L)) / (24L * 60L * 60L * 1000L));
		if (remainingDays <= 1L) {
			return getString("auditPublicCandidateDetailDecisionDeadlineToday");
		}
		return MessageFormat.format(getString("auditPublicCandidateDetailDecisionDeadlineDaysRemaining"), Long.valueOf(remainingDays));
	}

	private String formatDateTime(Date date) {
		if (date == null) {
			return "-";
		}
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, SecurityUtils.getLocale());
		return formatter.format(date);
	}

	private Boolean resolveHasOrganizationRelationship(Candidate candidate, List<WorkOrganizationItemView> rows) {
		if (candidate == null) {
			return null;
		}
		if (Boolean.TRUE.equals(candidate.isQUnemployed())) {
			return Boolean.FALSE;
		}
		if (!safeList(rows).isEmpty()) {
			return Boolean.TRUE;
		}
		return null;
	}

	private String resolveOrganizationAnswerLabel() {
		if (hasOrganizationRelationship == null) {
			return getString("acceptNominationOrganizationsAnswerNotProvided");
		}
		return hasOrganizationRelationship.booleanValue()
				? getString("acceptNominationOrganizationsOptionYes")
				: getString("acceptNominationOrganizationsOptionNo");
	}

	private String resolveOrganizationInfoText() {
		if (Boolean.FALSE.equals(hasOrganizationRelationship)) {
			return getString("auditPublicOrganizationsInfoNo");
		}
		if (Boolean.TRUE.equals(hasOrganizationRelationship) && safeList(organizationRows).isEmpty()) {
			return getString("auditPublicOrganizationsInfoYesNoRows");
		}
		if (Boolean.TRUE.equals(hasOrganizationRelationship)) {
			return getString("auditPublicOrganizationsInfoYesWithRows");
		}
		return getString("auditPublicOrganizationsInfoNotAnswered");
	}

	private void resolveNominationCardData() {
		if (nomination == null) {
			return;
		}
		Organization organization = nomination.getOrganization();
		if (organization != null) {
			nominationOrganizationName = organization.getName();
			nominationOrganizationId = organization.getOrgId();
			nominationOrganizationCountry = resolveCountryLabel(organization.getCountry());
		}

		nominationContactName = hasText(nomination.getNominationName())
				? nomination.getNominationName()
				: organization != null ? organization.getMembershipContactName() : null;
	}

	private boolean hasNominationInformation() {
		return hasText(nominationOrganizationName)
				|| hasText(nominationOrganizationId)
				|| hasText(nominationOrganizationCountry)
				|| hasText(nominationContactName);
	}

	private Form<Void> buildPreAuditorDecisionForm(PageParameters boardParams) {
		Form<Void> form = new Form<>("preAuditDecisionForm");
		List<AuditorCandidateDecisionStatus> decisionStatusChoices = resolveDecisionStatusChoices(DecisionStage.PRECOMPLETE);

		DropDownChoice<AuditorCandidateDecisionStatus> decisionStatusSelect = new DropDownChoice<>(
				"preDecisionStatusSelect",
				new PropertyModel<>(this, "selectedPreAuditorDecisionStatus"),
				decisionStatusChoices,
				new ChoiceRenderer<AuditorCandidateDecisionStatus>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(AuditorCandidateDecisionStatus object) {
						return resolveDecisionStatusLabel(object);
					}
				});
		decisionStatusSelect.setNullValid(true);
		form.add(decisionStatusSelect);

		AiAssistTextAreaPanel commentInput = new AiAssistTextAreaPanel(
				"preDecisionCommentInput",
				new PropertyModel<>(this, "preAuditorDecisionComment"),
				null,
				null,
				(originalText, instruction, styleContext) -> null,
				false,
				false,
				false,
				false,
				false,
				MAX_DECISION_COMMENT_LENGTH,
				5,
				textArea -> textArea.setRequired(false));
		form.add(commentInput);

		form.add(new Button("submitPreDecisionButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				submitAuditorDecision(
						DecisionStage.PRECOMPLETE,
						selectedPreAuditorDecisionStatus,
						preAuditorDecisionComment);
			}
		});

		form.add(new BookmarkablePageLink<Void>("backToAuditHomePreDecision", AuditPublicDashboardPage.class, boardParams));
		form.add(new Label("preDecisionElectionTitle", valueOrDash(electionTitle)));
		form.add(new Label("preDecisionCandidateName", valueOrDash(candidateName)));
		form.add(new Label("preDecisionCandidatesToElect", valueOrDash(decisionCandidatesToElect)));
		form.add(new Label("preDecisionCurrentDate", valueOrDash(decisionCurrentDate)));
		form.add(new Label("preDecisionContextParagraph", valueOrDash(decisionContextParagraph)));
		return form;
	}

	private Form<Void> buildAuditorDecisionForm(PageParameters boardParams) {
		Form<Void> form = new Form<>("auditDecisionForm");
		List<AuditorCandidateDecisionStatus> decisionStatusChoices = resolveDecisionStatusChoices(DecisionStage.COMPLETE);

		DropDownChoice<AuditorCandidateDecisionStatus> decisionStatusSelect = new DropDownChoice<>(
				"decisionStatusSelect",
				new PropertyModel<>(this, "selectedAuditorDecisionStatus"),
				decisionStatusChoices,
				new ChoiceRenderer<AuditorCandidateDecisionStatus>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(AuditorCandidateDecisionStatus object) {
						return resolveDecisionStatusLabel(object);
					}
				});
		decisionStatusSelect.setNullValid(true);
		form.add(decisionStatusSelect);

		AiAssistTextAreaPanel commentInput = new AiAssistTextAreaPanel(
				"decisionCommentInput",
				new PropertyModel<>(this, "auditorDecisionComment"),
				null,
				null,
				(originalText, instruction, styleContext) -> null,
				false,
				false,
				false,
				false,
				false,
				MAX_DECISION_COMMENT_LENGTH,
				5,
				textArea -> textArea.setRequired(false));
		form.add(commentInput);

		form.add(new Button("submitDecisionButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				submitAuditorDecision(
						DecisionStage.COMPLETE,
						selectedAuditorDecisionStatus,
						auditorDecisionComment);
			}
		});

		form.add(new BookmarkablePageLink<Void>("backToAuditHomeDecision", AuditPublicDashboardPage.class, boardParams));
		form.add(new Label("decisionElectionTitle", valueOrDash(electionTitle)));
		form.add(new Label("decisionCandidateName", valueOrDash(candidateName)));
		form.add(new Label("decisionCandidatesToElect", valueOrDash(decisionCandidatesToElect)));
		form.add(new Label("decisionCurrentDate", valueOrDash(decisionCurrentDate)));
		form.add(new Label("decisionContextParagraph", valueOrDash(decisionContextParagraph)));
		return form;
	}

	private void submitAuditorDecision(DecisionStage stage, AuditorCandidateDecisionStatus selectedStatus, String comment) {
		if (auditor == null || candidate == null || stage == null) {
			error(getString(KEY_DECISION_SAVE_ERROR));
			return;
		}
		if (!isCurrentAuditorCommissioner()) {
			error(getString(KEY_DECISION_SAVE_ERROR));
			return;
		}
		CandidateStatus candidateStatus = candidate.getStatus();
		if (!isStageEditableForCandidateStatus(stage, candidateStatus)) {
			error(getString(KEY_DECISION_SAVE_ERROR));
			return;
		}
		if (!isEditableDecisionStatus(selectedStatus)) {
			error(getString("auditPublicCandidateDetailDecisionStatusRequired"));
			return;
		}
		List<AuditorCandidateDecisionStatus> choices = resolveDecisionStatusChoices(stage);
		if (!choices.contains(selectedStatus)) {
			error(getString("auditPublicCandidateDetailDecisionStatusRequired"));
			return;
		}
		try {
			boolean updated = AppContext.getInstance().getManagerBeanRemote().updateAuditorCandidateDecisionStatus(
					auditor.getAuditorId(),
					candidate.getCandidateId(),
					selectedStatus,
					"AUDITOR_" + auditor.getAuditorId(),
					SecurityUtils.getClientIp(),
					normalizeDecisionComment(comment));
			if (updated) {
				info(getString("auditPublicCandidateDetailDecisionSaveSuccess"));
				setResponsePage(AuditPublicCandidateDetailPage.class, new PageParameters(getPageParameters()));
				return;
			}
		} catch (Exception e) {
			// Keep generic feedback below.
		}
		error(getString(KEY_DECISION_SAVE_ERROR));
	}

	private boolean isEditableDecisionStatus(AuditorCandidateDecisionStatus status) {
		return status == AuditorCandidateDecisionStatus.PREAPPROVED
				|| status == AuditorCandidateDecisionStatus.APPROVED
				|| status == AuditorCandidateDecisionStatus.REJECTED;
	}

	private boolean isCurrentAuditorCommissioner() {
		return auditor != null && auditor.isCommissioner();
	}

	private static boolean isStageEditableForCandidateStatus(DecisionStage stage, CandidateStatus status) {
		if (stage == DecisionStage.PRECOMPLETE) {
			return status == CandidateStatus.PRECOMPLETE;
		}
		if (stage == DecisionStage.COMPLETE) {
			return status == CandidateStatus.COMPLETE;
		}
		return false;
	}

	static boolean isPreDecisionEditable(CandidateStatus status) {
		return isStageEditableForCandidateStatus(DecisionStage.PRECOMPLETE, status);
	}

	static boolean isCompleteDecisionEditable(CandidateStatus status) {
		return isStageEditableForCandidateStatus(DecisionStage.COMPLETE, status);
	}

	private boolean isPreDecisionCardVisible(CandidateStatus candidateStatus, StageDecisionView stageDecision) {
		if (candidateStatus == CandidateStatus.PRECOMPLETE) {
			return true;
		}
		return stageDecision != null && stageDecision.hasReaction();
	}

	private boolean isCompleteDecisionCardVisible(CandidateStatus candidateStatus, StageDecisionView stageDecision) {
		if (candidateStatus == CandidateStatus.COMPLETE
				|| candidateStatus == CandidateStatus.CONFIRMED_AND_PUBLISHED
				|| candidateStatus == CandidateStatus.REJECTED) {
			return true;
		}
		return stageDecision != null && stageDecision.hasReaction();
	}

	private boolean isDecisionLockedByCandidateStatus(CandidateStatus status) {
		return status == CandidateStatus.CONFIRMED_AND_PUBLISHED
				|| status == CandidateStatus.REJECTED;
	}

	private List<AuditorCandidateDecisionStatus> resolveDecisionStatusChoices(DecisionStage stage) {
		if (stage == DecisionStage.PRECOMPLETE) {
			return Arrays.asList(AuditorCandidateDecisionStatus.PREAPPROVED, AuditorCandidateDecisionStatus.REJECTED);
		}
		if (stage == DecisionStage.COMPLETE) {
			return Arrays.asList(AuditorCandidateDecisionStatus.APPROVED, AuditorCandidateDecisionStatus.REJECTED);
		}
		return Collections.emptyList();
	}

	private String resolveDecisionStatusLabel(AuditorCandidateDecisionStatus status) {
		if (status == null) {
			return "";
		}
		if (status == AuditorCandidateDecisionStatus.PREAPPROVED) {
			return getString("auditPublicCandidateDetailDecisionPreApproveAction");
		}
		if (status == AuditorCandidateDecisionStatus.APPROVED) {
			return getString("auditPublicCandidateDetailDecisionApproveAction");
		}
		if (status == AuditorCandidateDecisionStatus.REJECTED) {
			return getString("auditPublicCandidateDetailDecisionRejectAction");
		}
		return status.name();
	}

	private String resolveDecisionCardClass(AuditorCandidateDecisionStatus status, CandidateStatus candidateStatus) {
		if (candidateStatus == CandidateStatus.CONFIRMED_AND_PUBLISHED) {
			return CARD_SUCCESS_CLASS;
		}
		if (candidateStatus == CandidateStatus.REJECTED) {
			return "card mb-3 border border-danger shadow-sm";
		}
		if (status == AuditorCandidateDecisionStatus.PREAPPROVED) {
			return CARD_SUCCESS_CLASS;
		}
		if (status == AuditorCandidateDecisionStatus.APPROVED) {
			return CARD_SUCCESS_CLASS;
		}
		if (status == AuditorCandidateDecisionStatus.REJECTED) {
			return "card mb-3 border border-danger shadow-sm";
		}
		return "card mb-3 border border-dark shadow-sm";
	}

	private String resolveDecisionCardHeaderClass(AuditorCandidateDecisionStatus status, CandidateStatus candidateStatus) {
		if (candidateStatus == CandidateStatus.CONFIRMED_AND_PUBLISHED) {
			return CARD_HEADER_SUCCESS_CLASS;
		}
		if (candidateStatus == CandidateStatus.REJECTED) {
			return "card-header bg-danger text-white";
		}
		if (status == AuditorCandidateDecisionStatus.PREAPPROVED) {
			return CARD_HEADER_SUCCESS_CLASS;
		}
		if (status == AuditorCandidateDecisionStatus.APPROVED) {
			return CARD_HEADER_SUCCESS_CLASS;
		}
		if (status == AuditorCandidateDecisionStatus.REJECTED) {
			return "card-header bg-danger text-white";
		}
		return "card-header bg-dark text-white";
	}

	private String resolveStageDecisionCompletedStatusText(AuditorCandidateDecisionStatus status, DecisionStage stage) {
		if (status == null || status == AuditorCandidateDecisionStatus.ANALYZING) {
			return getString(TokenResourceKeys.AUDIT_PUBLIC_V2_DECISION_PENDING);
		}
		if (stage == DecisionStage.PRECOMPLETE && status == AuditorCandidateDecisionStatus.PREAPPROVED) {
			return getString("auditPublicCandidateDetailDecisionPreApproveAction");
		}
		return toDecisionBadge(status).getText();
	}

	private String resolveDecisionSupportEmail() {
		if (election == null) {
			return null;
		}
		String defaultRecipient = trimToNull(election.getDefaultRecipient());
		if (defaultRecipient != null) {
			return defaultRecipient;
		}
		return trimToNull(election.getDefaultSender());
	}

	private String normalizeDecisionComment(String rawComment) {
		if (!hasText(rawComment)) {
			return null;
		}
		String trimmed = rawComment.trim();
		return trimmed.length() > 4000 ? trimmed.substring(0, 4000) : trimmed;
	}

	private Label buildTaskCompletionBadge(String id, ElectionTaskKey... taskKeys) {
		BadgeView badge = resolveSummaryTaskBadge(taskProgressRows, taskKeys);
		Label completionBadge = new Label(id, valueOrDash(badge.getText()));
		completionBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  valueOrDash(badge.getCssClass())));
		return completionBadge;
	}

	private WebMarkupContainer buildTaskCardVisibilityMarker(String id, boolean visible) {
		WebMarkupContainer marker = new WebMarkupContainer(id);
		marker.setRenderBodyOnly(true);
		marker.setVisible(visible);
		return marker;
	}

	private ListView<SummaryCardView> buildSummaryCardsView() {
		return new ListView<SummaryCardView>("summaryCards", summaryCards) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<SummaryCardView> item) {
				SummaryCardView row = item.getModelObject();
				WebMarkupContainer card = new WebMarkupContainer("summaryCard");
				card.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "card summary-card border-0 shadow-none mb-0 " + row.getCardClass()));
				item.add(card);

				card.add(new Label("summaryTitle", row.getTitle()));
				card.add(new Label("summaryValue", row.getValue()));
				card.add(new Label("summaryDescription", row.getDescription()));

				WebMarkupContainer iconContainer = new WebMarkupContainer("summaryIconContainer");
				iconContainer.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "avatar-title rounded-circle fs-22 " + row.getIconContainerClass()));
				card.add(iconContainer);

				WebMarkupContainer icon = new WebMarkupContainer("summaryIcon");
				icon.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getIconClass()));
				iconContainer.add(icon);
			}
		};
	}

	private ListView<AnswerItemView> buildAnswerList(String id, List<AnswerItemView> rows, boolean showAttentionBadge) {
		return new ListView<AnswerItemView>(id, rows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AnswerItemView> item) {
				AnswerItemView row = item.getModelObject();
				String itemClass = item.getIndex() == rows.size() - 1 ? "answer-item" : "answer-item mb-3";
				item.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  itemClass));
				item.add(new Label("questionLabel", valueOrDash(row.getQuestion())));

				MultiLineLabel answerText = new MultiLineLabel("answerText", valueOrDash(row.getAnswer()));
				answerText.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "mb-0 text-muted"));
				item.add(answerText);

				Label answerBadge = new Label("answerBadge", getString("auditPublicCandidateDetailAttentionBadge"));
				answerBadge.setVisible(showAttentionBadge && row.isAnswered() && !row.isAccepted());
				answerBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, BootstrapCssClasses.BADGE_WARNING_SUBTLE_TEXT_WARNING));
				item.add(answerBadge);

				Label answerDescription = new Label("answerDescription", valueOrDash(row.getDescription()));
				answerDescription.setEscapeModelStrings(false);
				answerDescription.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "mt-1 mb-0 small text-muted"));
				answerDescription.setVisible(hasText(row.getDescription()));
				item.add(answerDescription);
			}
		};
	}

	private ListView<AnswerItemView> buildDeclarationList(String id, List<AnswerItemView> rows) {
		return new ListView<AnswerItemView>(id, rows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<AnswerItemView> item) {
				AnswerItemView row = item.getModelObject();
				String itemClass = item.getIndex() == rows.size() - 1 ? "answer-item" : "answer-item mb-3";
				item.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  itemClass));
				item.add(new Label("questionLabel", valueOrDash(row.getQuestion())));

				MultiLineLabel declarationText = new MultiLineLabel("declarationText", valueOrDash(row.getDescription()));
				declarationText.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "mt-2 mb-0 fs-6 text-body"));
				item.add(declarationText);
			}
		};
	}

	private ListView<SupportItemView> buildSupportsView() {
		return new ListView<SupportItemView>("supportRows", supportRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<SupportItemView> item) {
				SupportItemView row = item.getModelObject();
				item.add(new Label("supportTitle", valueOrDash(row.getDisplayTitle())));
				Label subtitle = new Label("supportSubtitle", row.getSubtitle());
				subtitle.setVisible(hasText(row.getSubtitle()));
				item.add(subtitle);
				Label status = new Label("supportStatusBadge", valueOrDash(row.getStatusBadgeText()));
				status.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getStatusBadgeClass()));
				item.add(status);
				Label supportDate = new Label("supportDate", valueOrDash(row.getSupportDateText()));
				supportDate.setVisible(hasText(row.getSupportDateText()));
				item.add(supportDate);
			}
		};
	}

	private ListView<WorkOrganizationItemView> buildOrganizationsView() {
		return new ListView<WorkOrganizationItemView>("organizationRows", organizationRows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<WorkOrganizationItemView> item) {
				WorkOrganizationItemView row = item.getModelObject();
				item.add(new Label("organizationName", valueOrDash(row.getName())));
				item.add(new Label("organizationGroup", valueOrDash(row.getGroup())));
				Label typeBadge = new Label("organizationTypeBadge", valueOrDash(row.getTypeBadgeText()));
				typeBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.getTypeBadgeClass()));
				item.add(typeBadge);
			}
		};
	}

	private ListView<CountryLinkItemView> buildCountryLinksView(String id, List<CountryLinkItemView> rows) {
		return new ListView<CountryLinkItemView>(id, rows) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CountryLinkItemView> item) {
				CountryLinkItemView row = item.getModelObject();
				item.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, 
						row.isPrimaryCountry()
								? "border rounded p-3 mb-3 border-primary-subtle bg-primary-subtle bg-opacity-10"
								: "border rounded p-3 mb-3"));
					item.add(new Label("countryLabel", row.getCountryLabel()));

					Label restrictedCountryBadge = new Label("restrictedCountryBadge", getString("auditPublicCandidateDetailRestrictedCountryBadge"));
					restrictedCountryBadge.setOutputMarkupPlaceholderTag(true);
					restrictedCountryBadge.setVisible(isRestrictedCountryCode(row.getCountryCode()));
					restrictedCountryBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "badge bg-danger-subtle text-danger border border-danger-subtle"));
					item.add(restrictedCountryBadge);

					Label primaryBadge = new Label("primaryBadge", row.isPrimaryCountry()
							? getString("auditPublicCandidateDetailCountryPrimaryBadge")
							: getString("auditPublicCandidateDetailCountrySecondaryBadge"));
					primaryBadge.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  row.isPrimaryCountry()
						? BootstrapCssClasses.BADGE_PRIMARY_SUBTLE_TEXT_PRIMARY
						: "badge bg-light-subtle text-muted border"));
				item.add(primaryBadge);

				item.add(new Label("qCitizen", row.getQCitizen()));
				item.add(new Label("qResidenceOver5y", row.getQResidenceOver5y()));
				item.add(new Label("qLongEmploymentOrAdvisory5y", row.getQLongEmploymentOrAdvisory5y()));
				item.add(new Label("qFamilyResidenceOver5y", row.getQFamilyResidenceOver5y()));
				item.add(new Label("qInternetCommunityOrgParticipation", row.getQInternetCommunityOrgParticipation()));
				item.add(new Label("qEligibleForCitizenship", row.getQEligibleForCitizenship()));
			}
		};
	}

	private List<CountryLinkItemView> filterCountryRows(boolean primary) {
		List<CountryLinkItemView> rows = new ArrayList<>();
		for (CountryLinkItemView row : safeList(countryRows)) {
			if (row != null && row.isPrimaryCountry() == primary) {
				rows.add(row);
			}
		}
		return rows;
	}

	private Component buildCandidatePicture(String id) {
		if (candidate != null && candidate.getPictureInfo() != null && candidate.getPictureInfo().length > 0) {
			String extension = hasText(candidate.getPictureExtension()) ? candidate.getPictureExtension() : "jpg";
			NonCachingImage image = new NonCachingImage(id, new ImageResource(candidate.getPictureInfo(), extension));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "img-fluid rounded-circle img-thumbnail candidate-photo-sepia"));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_WIDTH, "220"));
			image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_HEIGHT, "220"));
			return image;
		}
		ContextImage image = new ContextImage(id, "image/default_candidate_photo.jpg");
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "img-fluid rounded-circle img-thumbnail candidate-photo-sepia"));
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_WIDTH, "220"));
		image.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_HEIGHT, "220"));
		return image;
	}

	private Component buildProctorioDownloadLink() {
		if (!proctorioEnabledForElection || !hasProctorioFile || candidate == null) {
			return new WebMarkupContainer("proctorioDownloadLink").setVisible(false);
		}
		byte[] proctorioResult = candidate.getProctorioResultFile();
		String contentType = resolveProctorioContentType(proctorioResult);
		String fileName = resolveProctorioFileName(candidate.getCandidateId(), proctorioResult);
		ResourceLink<Void> link = new ResourceLink<>("proctorioDownloadLink", new ByteArrayResource(contentType, proctorioResult, fileName));
		link.add(AttributeModifier.replace("download", fileName));
		return link;
	}

	private List<SummaryCardView> buildSummaryCards(List<CandidateElectionTaskProgress> taskProgressRows) {
		return new SummaryCardsFactory(taskProgressRows).build();
	}

	private BadgeView resolveSummaryTaskBadge(List<CandidateElectionTaskProgress> taskProgressRows, ElectionTaskKey... taskKeys) {
		CandidateElectionTaskStatus status = resolveSummaryTaskStatus(taskProgressRows, taskKeys);
		return new BadgeView(resolveTaskStatusLabel(status), resolveTaskStatusBadgeClass(status));
	}

	private CandidateElectionTaskStatus resolveSummaryTaskStatus(List<CandidateElectionTaskProgress> taskProgressRows, ElectionTaskKey... taskKeys) {
		if (taskKeys == null || taskKeys.length == 0) {
			return CandidateElectionTaskStatus.NOT_STARTED;
		}
		List<CandidateElectionTaskStatus> statuses = new ArrayList<>();
		for (ElectionTaskKey taskKey : taskKeys) {
			if (!isTaskEnabled(taskKey)) {
				continue;
			}
			CandidateElectionTaskStatus status = resolveTaskStatus(taskProgressRows, taskKey);
			statuses.add(status != null ? status : CandidateElectionTaskStatus.NOT_STARTED);
		}
		if (statuses.isEmpty()) {
			return CandidateElectionTaskStatus.NOT_STARTED;
		}

		boolean allCompleted = true;
		boolean allOmitted = true;
		boolean anyStarted = false;
		boolean anyCompleted = false;
		for (CandidateElectionTaskStatus status : statuses) {
			if (status == CandidateElectionTaskStatus.COMPLETED) {
				anyCompleted = true;
				allOmitted = false;
				continue;
			}
			if (status == CandidateElectionTaskStatus.STARTED) {
				anyStarted = true;
				allCompleted = false;
				allOmitted = false;
				continue;
			}
			if (status == CandidateElectionTaskStatus.OMITTED) {
				allCompleted = false;
				continue;
			}
			allCompleted = false;
			allOmitted = false;
		}

		if (allCompleted) {
			return CandidateElectionTaskStatus.COMPLETED;
		}
		if (anyStarted || anyCompleted) {
			return CandidateElectionTaskStatus.STARTED;
		}
		if (allOmitted) {
			return CandidateElectionTaskStatus.OMITTED;
		}
		return CandidateElectionTaskStatus.NOT_STARTED;
	}

	private List<AnswerItemView> buildIncompatibilityAnswers() {
		List<AnswerItemView> rows = new ArrayList<>();
		rows.add(buildBooleanAnswer(getString("acceptNominationIncompatibilitiesQuestionCanSpeakSpanish"), candidate != null ? candidate.getQCanSpeakSpanish() : null, true));
		rows.add(buildBooleanAnswer(getString("acceptNominationIncompatibilitiesQuestionAdultInCountry"), candidate != null ? candidate.getQAdultInCountry() : null, true));
		rows.add(buildBooleanAnswer(getString("acceptNominationIncompatibilitiesQuestionCivilRightsLimitation"), candidate != null ? candidate.getQCivilRightsLimitation() : null, false));
		rows.add(buildBooleanAnswer(getString("acceptNominationIncompatibilitiesQuestionLegalLimitationAnyCountry"), candidate != null ? candidate.getQLegalLimitationAnyCountry() : null, false));
		rows.add(buildBooleanAnswer(getString("acceptNominationIncompatibilitiesQuestionHealthTravelLimitation"), candidate != null ? candidate.getQHealthTravelLimitation() : null, false));
		rows.add(buildBooleanAnswer(getString("acceptNominationIncompatibilitiesQuestionHealthMentalLimitation"), candidate != null ? candidate.getQHealthMentalLimitation() : null, false));
		return rows;
	}

	private List<AnswerItemView> buildOtherStatutoryAnswers() {
		List<AnswerItemView> rows = new ArrayList<>();
		List<String> questions = safeOtherStatutoryQuestions();
		String[] answers = {
				resolveLocalizedOtherStatutoryAnswer(1),
				resolveLocalizedOtherStatutoryAnswer(2),
				resolveLocalizedOtherStatutoryAnswer(3),
				resolveLocalizedOtherStatutoryAnswer(4)
		};
		for (int i = 0; i < answers.length; i++) {
			String question = questions.size() > i
					? questions.get(i)
					: formatMessage("auditPublicCandidateDetailQuestionFallbackIndexed", i + 1);
			rows.add(buildTextAnswer(question, answers[i]));
		}
		return rows;
	}

	private List<AnswerItemView> buildOtherNonStatutoryAnswers() {
		List<AnswerItemView> rows = new ArrayList<>();
		List<String> questions = safeOtherNonStatutoryQuestions();
		String question = !questions.isEmpty() ? questions.get(0) : getString("auditPublicCandidateDetailQuestionFallback");
		String answer = resolveLocalizedOtherNonStatutoryAnswer1();
		rows.add(buildTextAnswer(question, answer));
		return rows;
	}

	private String resolveLocalizedBio() {
		if (candidate == null) {
			return null;
		}
		return selectBySessionLanguage(candidate.getBioSpanish(), candidate.getBioEnglish(), candidate.getBioPortuguese());
	}

	private String resolveLocalizedOtherStatutoryAnswer(int answerNumber) {
		if (candidate == null) {
			return null;
		}
		switch (answerNumber) {
		case 1:
			return selectBySessionLanguage(
					candidate.getQOtherStatutoryAnswer1Spanish(),
					candidate.getQOtherStatutoryAnswer1English(),
					candidate.getQOtherStatutoryAnswer1Portuguese());
		case 2:
			return selectBySessionLanguage(
					candidate.getQOtherStatutoryAnswer2Spanish(),
					candidate.getQOtherStatutoryAnswer2English(),
					candidate.getQOtherStatutoryAnswer2Portuguese());
		case 3:
			return selectBySessionLanguage(
					candidate.getQOtherStatutoryAnswer3Spanish(),
					candidate.getQOtherStatutoryAnswer3English(),
					candidate.getQOtherStatutoryAnswer3Portuguese());
		case 4:
			return selectBySessionLanguage(
					candidate.getQOtherStatutoryAnswer4Spanish(),
					candidate.getQOtherStatutoryAnswer4English(),
					candidate.getQOtherStatutoryAnswer4Portuguese());
		default:
			return null;
		}
	}

	private String resolveLocalizedOtherNonStatutoryAnswer1() {
		if (candidate == null) {
			return null;
		}
		return selectBySessionLanguage(
				candidate.getQOtherNonStatutoryAnswer1Spanish(),
				candidate.getQOtherNonStatutoryAnswer1English(),
				candidate.getQOtherNonStatutoryAnswer1Portuguese());
	}

	private String selectBySessionLanguage(String spanishValue, String englishValue, String portugueseValue) {
		LanguageCode language = SecurityUtils.getLanguageCode();
		switch (language) {
		case EN:
			return englishValue;
		case PT:
			return portugueseValue;
		case SP:
		default:
			return spanishValue;
		}
	}

	private List<AnswerItemView> buildDeclarationAnswers(ElectionTaskKey taskKey) {
		List<AnswerItemView> rows = new ArrayList<>();
		if (!isTaskEnabled(taskKey)) {
			return rows;
		}
		String nominationToken = nomination != null ? nomination.getAcceptNominationToken() : null;
		if (!hasText(nominationToken)) {
			return rows;
		}

		try {
			CandidateDeclarationsDefinition definitions = loadCandidateDeclarationsDefinition(nominationToken, taskKey);
			if (definitions == null || definitions.getDeclarations() == null) {
				return rows;
			}

			for (CandidateDeclarationDefinition definition : definitions.getDeclarations()) {
				if (definition == null || definition.getCode() == null || definition.getInputType() == null) {
					continue;
				}
				String questionText = hasText(definition.getTitle()) ? definition.getTitle() : definition.getDeclarationText();
				if (!hasText(questionText)) {
					questionText = definition.getCode().name();
				}
				if (definition.getInputType() == CandidateDeclarationInputType.CHECKBOX) {
					Boolean value = readDeclarationCheckboxValue(definition.getCode());
					if (value == null) {
						continue;
					}
					AnswerItemView row = buildBooleanAnswer(questionText, value, true);
					row.setDescription(definition.getDeclarationText());
					rows.add(row);
					continue;
				}

				CandidatePepDeclaration pepValue = candidate != null ? candidate.getQDeclarationPep() : null;
				if (pepValue == null) {
					continue;
				}
				String pepLabel = resolvePepLabel(definition.getOptions(), pepValue);
				AnswerItemView row = new AnswerItemView(questionText, pepLabel, pepValue != null, pepValue != null);
				row.setDescription(buildPepDeclarationText(definition.getDeclarationText(), pepLabel));
				rows.add(row);
			}
			return rows;
		} catch (Exception e) {
			return rows;
		}
	}

	private List<SupportItemView> buildSupportRows(List<SupportNomination> supports) {
		if (supports == null || supports.isEmpty()) {
			return Collections.emptyList();
		}
		List<SupportItemView> rows = new ArrayList<>();
		for (SupportNomination support : supports) {
			if (support == null) {
				continue;
			}
			Organization organization = support.getSupportingOrganization();
			String title;
			String subtitle;
			String statusPrefix;
			if (organization != null) {
				title = hasText(organization.getName()) ? organization.getName() : organization.getOrgId();
				subtitle = resolveSupportOrganizationSubtitle(organization);
				statusPrefix = "acceptNominationOrgSupportsStatus";
			} else {
				title = hasText(support.getSupportingContactName()) ? support.getSupportingContactName() : support.getSupportingContactEmail();
				subtitle = trimToNull(support.getSupportingContactEmail());
				statusPrefix = "acceptNominationUserSupportsStatus";
			}
			SupportStatus status = support.getSupportStatus();
			String statusText = mapSupportStatusText(status, statusPrefix);
			String badgeClass = mapSupportStatusBadgeClass(status);
			String supportDateText = formatSupportDate(support.getSupportResponseInstant(), status);
			rows.add(new SupportItemView(title, subtitle, status, statusText, badgeClass, supportDateText));
		}
		rows.sort(Comparator.comparingInt((SupportItemView row) -> resolveSupportSortPriority(row.getSupportStatus()))
				.thenComparing(SupportItemView::getTitle, String.CASE_INSENSITIVE_ORDER));

		int acceptedOrder = 1;
		for (SupportItemView row : rows) {
			if (row != null && isAcceptedSupportStatus(row.getSupportStatus())) {
				row.setDisplayTitle(acceptedOrder + ". " + valueOrDash(row.getTitle()));
				acceptedOrder++;
			}
		}
		return rows;
	}

	private String formatSupportDate(Date supportDate, SupportStatus supportStatus) {
		if (supportDate == null || !isAcceptedSupportStatus(supportStatus)) {
			return null;
		}
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, SecurityUtils.getLocale());
		return getString("auditPublicCandidateDetailDateLabel") + " " + formatter.format(supportDate);
	}

	private String resolveSupportOrganizationSubtitle(Organization organization) {
		if (organization == null) {
			return null;
		}
		List<String> parts = new ArrayList<>();
		String orgId = trimToNull(organization.getOrgId());
		String country = trimToNull(resolveCountryLabel(organization.getCountry()));
		if (hasText(orgId)) {
			parts.add(orgId);
		}
		if (hasText(country)) {
			parts.add(country);
		}
		return parts.isEmpty() ? null : String.join(" · ", parts);
	}

	private List<WorkOrganizationItemView> buildWorkOrganizationRows(List<CandidateWorkOrganization> organizations) {
		if (organizations == null || organizations.isEmpty()) {
			return Collections.emptyList();
		}
		List<WorkOrganizationItemView> rows = new ArrayList<>();
		for (CandidateWorkOrganization organization : organizations) {
			if (organization == null) {
				continue;
			}
			WorkOrganizationType type = organization.getWorkOrganizationType();
			String typeText = type == WorkOrganizationType.PAID
					? getString("acceptNominationOrganizationsTypePaidShort")
					: getString("acceptNominationOrganizationsTypeAdHonoremShort");
			String typeClass = type == WorkOrganizationType.PAID
					? BootstrapCssClasses.BADGE_PRIMARY_SUBTLE_TEXT_PRIMARY
					: "badge bg-info-subtle text-info";
			String typeBadgeText = typeText;
			rows.add(new WorkOrganizationItemView(
					valueOrDash(organization.getOrganizationName()),
					valueOrDash(organization.getOrganizationGroup()),
					typeText,
					typeBadgeText,
					typeClass));
		}
		rows.sort(Comparator.comparing(WorkOrganizationItemView::getName, String.CASE_INSENSITIVE_ORDER));
		return rows;
	}

	private List<CountryLinkItemView> buildCountryRows(List<CandidateCountryLink> links) {
		if (links == null || links.isEmpty()) {
			return Collections.emptyList();
		}

		List<CountryLinkItemView> rows = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		for (CandidateCountryLink link : links) {
			if (link == null || !hasText(link.getCountryCode())) {
				continue;
			}
			String normalizedCode = normalizeCountryCode(link.getCountryCode());
			if (!hasText(normalizedCode) || !seen.add(normalizedCode + "|" + link.isPrimaryCountry())) {
				continue;
			}
				rows.add(new CountryLinkItemView(
						resolveCountryLabel(normalizedCode),
						link.isPrimaryCountry(),
						link.isQCitizen(),
						formatBoolean(link.isQCitizen()),
						formatBoolean(link.isQResidenceOver5y()),
						formatBoolean(link.isQLongEmploymentOrAdvisory5y()),
					formatBoolean(link.isQFamilyResidenceOver5y()),
					formatBoolean(link.isQInternetCommunityOrgParticipation()),
					formatBoolean(link.isQEligibleForCitizenship()),
					normalizedCode));
		}
		rows.sort(Comparator.comparing(CountryLinkItemView::isPrimaryCountry).reversed()
				.thenComparing(CountryLinkItemView::getCountryLabel, String.CASE_INSENSITIVE_ORDER));
		return rows;
	}

	private Nomination resolveNominationWithDetails(long electionId, long candidateId) {
		try {
			List<Nomination> nominations = AppContext.getInstance().getManagerBeanRemote().getElectionNominations(electionId);
			for (Nomination item : nominations) {
				if (item == null || item.getCandidate() == null || item.getCandidate().getCandidateId() != candidateId) {
					continue;
				}
				String token = item.getAcceptNominationToken();
				if (hasText(token)) {
					Nomination detailed = AppContext.getInstance().getPreNominationBeanRemote().verifyAcceptNominationAccess(token);
					if (detailed != null) {
						return detailed;
					}
				}
				return item;
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	private List<CandidateWorkOrganization> loadCandidateOrganizations(long electionId, long candidateId, Candidate fallbackCandidate) {
		try {
			List<CandidateWorkOrganization> fromManager = AppContext.getInstance().getManagerBeanRemote().getElectionCandidateWorkOrganizations(electionId);
			List<CandidateWorkOrganization> filtered = new ArrayList<>();
			for (CandidateWorkOrganization row : safeList(fromManager)) {
				if (row != null && row.getCandidate() != null && row.getCandidate().getCandidateId() == candidateId) {
					filtered.add(row);
				}
			}
			if (!filtered.isEmpty()) {
				return filtered;
			}
		} catch (Exception e) {
			// Fallback to candidate snapshot.
		}
		return safeList(fallbackCandidate != null ? fallbackCandidate.getWorkOrganizations() : null);
	}

	private List<CandidateCountryLink> loadCandidateCountryLinks(long electionId, long candidateId, Candidate fallbackCandidate) {
		try {
			List<CandidateCountryLink> fromManager = AppContext.getInstance().getManagerBeanRemote().getElectionCandidateCountryLinks(electionId);
			List<CandidateCountryLink> filtered = new ArrayList<>();
			for (CandidateCountryLink row : safeList(fromManager)) {
				if (row != null && row.getCandidate() != null && row.getCandidate().getCandidateId() == candidateId) {
					filtered.add(row);
				}
			}
			if (!filtered.isEmpty()) {
				return filtered;
			}
		} catch (Exception e) {
			// Fallback to candidate snapshot.
		}
		return safeList(fallbackCandidate != null ? fallbackCandidate.getCountryLinks() : null);
	}

	private List<String> loadRestrictedCountryCodes(long electionId) {
		if (electionId <= 0) {
			return Collections.emptyList();
		}
		try {
			Election detailedElection = AppContext.getInstance().getManagerBeanRemote().getElectionWithRestrictedCountries(electionId);
			if (detailedElection == null) {
				return Collections.emptyList();
			}
			return safeList(detailedElection.getRestrictedCountryCodes());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private AuditorDecisionView resolveCurrentDecision(long electionId, Long auditorId, long candidateId) {
		if (auditorId == null) {
			return AuditorDecisionView.empty();
		}
		try {
			List<AuditorCandidateDecision> decisions = AppContext.getInstance().getManagerBeanRemote().getElectionAuditorCandidateDecisions(electionId);
			for (AuditorCandidateDecision decision : safeList(decisions)) {
				Long decisionAuditorId = readEntityLongField(readFieldValue(decision, "auditor"), "auditorId");
				Long decisionCandidateId = readEntityLongField(readFieldValue(decision, "candidate"), "candidateId");
				if (auditorId.equals(decisionAuditorId) && Long.valueOf(candidateId).equals(decisionCandidateId)) {
					AuditorCandidateDecisionStatus legacyStatus = readDecisionStatusField(decision, "decisionStatus");
					Date legacyDecisionDate = readDateField(decision, "decisionDate");
					AuditorCandidateDecisionStatus preStatus = readDecisionStatusField(decision, "preDecisionStatus");
					Date preDate = readDateField(decision, "preDecisionDate");
					String preComment = readStringField(decision, "preDecisionComment");

					AuditorCandidateDecisionStatus finalStatus = readDecisionStatusField(decision, "finalDecisionStatus");
					Date finalDate = readDateField(decision, "finalDecisionDate");
					String finalComment = readStringField(decision, "finalDecisionComment");

					Date preapprovedDate = readDateField(decision, "preapprovedDate");
					Date approvedDate = readDateField(decision, "approvedDate");

						return new AuditorDecisionView(
								legacyStatus,
								legacyDecisionDate,
								preStatus,
								preDate,
								preComment,
							finalStatus,
							finalDate,
							finalComment,
							preapprovedDate,
							approvedDate);
				}
			}
		} catch (Exception e) {
			return AuditorDecisionView.empty();
		}
		return AuditorDecisionView.empty();
	}

	private StageDecisionView resolvePreStageDecisionView(AuditorDecisionView decision, CandidateStatus candidateStatus) {
		if (decision == null) {
			return StageDecisionView.empty();
		}

		AuditorCandidateDecisionStatus status = decision.getPreStatus();
		Date date = decision.getPreDate();
		String comment = decision.getPreComment();

		if (status == null) {
			AuditorCandidateDecisionStatus legacyStatus = decision.getLegacyStatus();
			if (legacyStatus == AuditorCandidateDecisionStatus.PREAPPROVED
					|| (legacyStatus == AuditorCandidateDecisionStatus.REJECTED
							&& isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate()))) {
				status = legacyStatus;
				date = decision.getPreapprovedDate() != null ? decision.getPreapprovedDate() : decision.getLegacyDate();
			}
		}

		if (candidateStatus == CandidateStatus.PRECOMPLETE && status == null) {
			status = decision.getLegacyStatus();
			if (status == AuditorCandidateDecisionStatus.APPROVED || status == AuditorCandidateDecisionStatus.NO_APPLY) {
				status = null;
			}
			date = date != null ? date : decision.getLegacyDate();
		}

		return new StageDecisionView(status, date, comment);
	}

	private StageDecisionView resolveCompleteStageDecisionView(AuditorDecisionView decision, CandidateStatus candidateStatus) {
		if (decision == null) {
			return StageDecisionView.empty();
		}

		AuditorCandidateDecisionStatus status = decision.getFinalStatus();
		Date date = decision.getFinalDate();
		String comment = decision.getFinalComment();

		if (status == null) {
			AuditorCandidateDecisionStatus legacyStatus = decision.getLegacyStatus();
			if (legacyStatus == AuditorCandidateDecisionStatus.APPROVED
					|| (legacyStatus == AuditorCandidateDecisionStatus.REJECTED
							&& decision.getApprovedDate() != null
							&& !isLikelyLegacyPrecompleteRejectedMilestone(decision.getPreapprovedDate(), decision.getApprovedDate()))) {
				status = legacyStatus;
				date = decision.getApprovedDate() != null ? decision.getApprovedDate() : decision.getLegacyDate();
			}
		}

		if (candidateStatus == CandidateStatus.COMPLETE && status == null) {
			status = decision.getLegacyStatus() == AuditorCandidateDecisionStatus.APPROVED ? AuditorCandidateDecisionStatus.APPROVED : null;
			date = date != null ? date : decision.getApprovedDate();
		}

		return new StageDecisionView(status, date, comment);
	}

	private AuditorCandidateDecisionStatus readDecisionStatusField(Object instance, String fieldName) {
		Object value = readFieldValue(instance, fieldName);
		return value instanceof AuditorCandidateDecisionStatus ? (AuditorCandidateDecisionStatus) value : null;
	}

	private Date readDateField(Object instance, String fieldName) {
		Object value = readFieldValue(instance, fieldName);
		return value instanceof Date ? (Date) value : null;
	}

	private String readStringField(Object instance, String fieldName) {
		Object value = readFieldValue(instance, fieldName);
		String text = value instanceof String ? (String) value : null;
		return hasText(text) ? text : "";
	}

	private boolean isLikelyLegacyPrecompleteRejectedMilestone(Date preapprovedDate, Date approvedDate) {
		if (preapprovedDate == null || approvedDate == null) {
			return false;
		}
		long diffMillis = Math.abs(preapprovedDate.getTime() - approvedDate.getTime());
		return diffMillis <= 1000L;
	}

	private List<String> safeOtherStatutoryQuestions() {
		List<String> fallback = new ArrayList<>();
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
		List<String> fallback = new ArrayList<>();
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

	private BadgeView toDecisionBadge(AuditorCandidateDecisionStatus status) {
		if (status == null) {
			return new BadgeView(getString(TokenResourceKeys.AUDIT_PUBLIC_V2_DECISION_PENDING), BootstrapCssClasses.BADGE_WARNING_SUBTLE_TEXT_WARNING);
		}
		switch (status) {
		case PREAPPROVED:
			return new BadgeView(getString("auditPublicV2DecisionPreApproved"), BootstrapCssClasses.BADGE_SUCCESS_SUBTLE_TEXT_SUCCESS);
		case APPROVED:
			return new BadgeView(getString("auditPublicV2DecisionApproved"), BootstrapCssClasses.BADGE_SUCCESS_SUBTLE_TEXT_SUCCESS);
		case REJECTED:
			return new BadgeView(getString("auditPublicV2DecisionRejected"), "badge bg-danger-subtle text-danger");
		case ANALYZING:
			return new BadgeView(getString(TokenResourceKeys.AUDIT_PUBLIC_V2_DECISION_PENDING), BootstrapCssClasses.BADGE_WARNING_SUBTLE_TEXT_WARNING);
		case NO_APPLY:
			return new BadgeView(getString("auditPublicV2DecisionNoApply"), BootstrapCssClasses.BADGE_SECONDARY_SUBTLE_TEXT_SECONDARY);
		default:
			return new BadgeView(getString(TokenResourceKeys.AUDIT_PUBLIC_V2_DECISION_PENDING), BootstrapCssClasses.BADGE_WARNING_SUBTLE_TEXT_WARNING);
		}
	}

	private AnswerItemView buildBooleanAnswer(String question, Boolean value, boolean positiveWhenTrue) {
		if (value == null) {
			return new AnswerItemView(
					question,
					getString(TokenResourceKeys.CANDIDATE_ANSWERS_NO_DATA),
					false,
					false);
		}
		if (value.booleanValue()) {
			return new AnswerItemView(
					question,
					getString("acceptNominationIncompatibilitiesOptionYes"),
					true,
					positiveWhenTrue);
		}
		return new AnswerItemView(
				question,
				getString("acceptNominationIncompatibilitiesOptionNo"),
				true,
				!positiveWhenTrue);
	}

	private AnswerItemView buildTextAnswer(String question, String value) {
		if (!hasText(value)) {
			return new AnswerItemView(
					question,
					getString(TokenResourceKeys.CANDIDATE_ANSWERS_NO_DATA),
					false,
					false);
		}
		return new AnswerItemView(
				question,
				value,
				true,
				true);
	}

	private Boolean readDeclarationCheckboxValue(CandidateDeclarationCode code) {
		if (candidate == null || code == null) {
			return null;
		}
		switch (code) {
		case INCOMPATIBILITIES_AND_CAPACITIES:
			return candidate.getQDeclarationIncompatibilities();
		case CONFLICTS_OF_INTEREST:
			return candidate.getQDeclarationConflictsOfInterest();
		case COMPETENCIES_AND_SUITABILITY:
			return candidate.getQDeclarationCompetenciesAndSuitability();
		case DISCIPLINARY_REGULATION:
			return candidate.getQDeclarationDisciplinaryRegulation();
		case IANA_KNOWLEDGE:
			return candidate.getQDeclarationIanaKnowledge();
		case ASO_KNOWLEDGE:
			return candidate.getQDeclarationAsoKnowledge();
		case DYNAMIC_COMMITMENTS:
			return candidate.getQDeclarationDynamicCommitments();
		case DATA_USAGE_AND_PUBLICATION:
			return candidate.getQDeclarationDataUsageAndPublication();
		default:
			return null;
		}
	}

	private String resolvePepLabel(List<CandidateDeclarationOptionDefinition> options, CandidatePepDeclaration value) {
		if (value == null) {
			return getString(TokenResourceKeys.CANDIDATE_ANSWERS_NO_DATA);
		}
		for (CandidateDeclarationOptionDefinition option : safeList(options)) {
			if (option == null || !hasText(option.getValue())) {
				continue;
			}
			if (value.name().equalsIgnoreCase(option.getValue()) && hasText(option.getLabel())) {
				return option.getLabel();
			}
		}
		return value.name();
	}

	private String mapSupportStatusText(SupportStatus status, String prefix) {
		if (status == null) {
			return getString(TokenResourceKeys.CANDIDATE_ANSWERS_NO_DATA);
		}
		switch (status) {
		case PROPOSED:
			return getString(prefix + "Proposed");
		case ACCEPTED:
			return getString(prefix + "Accepted");
		case REJECTED:
			return getString(prefix + "Rejected");
		case INVALID:
			return getString(prefix + "Invalid");
		case APPROVED:
			return getString(prefix + "Approved");
		default:
			return status.name();
		}
	}

	private String mapSupportStatusBadgeClass(SupportStatus status) {
		if (status == null) {
			return BootstrapCssClasses.BADGE_SECONDARY_SUBTLE_TEXT_SECONDARY;
		}
		switch (status) {
		case PROPOSED:
			return BootstrapCssClasses.BADGE_WARNING_SUBTLE_TEXT_WARNING;
		case ACCEPTED:
			return BootstrapCssClasses.BADGE_SUCCESS_SUBTLE_TEXT_SUCCESS;
		case REJECTED:
			return "badge bg-danger-subtle text-danger";
		case INVALID:
			return BootstrapCssClasses.BADGE_SECONDARY_SUBTLE_TEXT_SECONDARY;
		case APPROVED:
			return BootstrapCssClasses.BADGE_PRIMARY_SUBTLE_TEXT_PRIMARY;
		default:
			return BootstrapCssClasses.BADGE_SECONDARY_SUBTLE_TEXT_SECONDARY;
		}
	}

	private int countAnswered(List<AnswerItemView> rows) {
		int count = 0;
		for (AnswerItemView row : safeList(rows)) {
			if (row != null && row.isAnswered()) {
				count++;
			}
		}
		return count;
	}

	private boolean isTaskCompleted(List<CandidateElectionTaskProgress> taskProgressRows, ElectionTaskKey... taskKeys) {
		return resolveSummaryTaskStatus(taskProgressRows, taskKeys) == CandidateElectionTaskStatus.COMPLETED;
	}

	private int countNoLimitationAnswers() {
		return countNoLimitation(candidate != null ? candidate.getQCanSpeakSpanish() : null, true)
				+ countNoLimitation(candidate != null ? candidate.getQAdultInCountry() : null, true)
				+ countNoLimitation(candidate != null ? candidate.getQCivilRightsLimitation() : null, false)
				+ countNoLimitation(candidate != null ? candidate.getQLegalLimitationAnyCountry() : null, false)
				+ countNoLimitation(candidate != null ? candidate.getQHealthTravelLimitation() : null, false)
				+ countNoLimitation(candidate != null ? candidate.getQHealthMentalLimitation() : null, false);
	}

	private int countLimitationAnswers() {
		return countLimitation(candidate != null ? candidate.getQCanSpeakSpanish() : null, false)
				+ countLimitation(candidate != null ? candidate.getQAdultInCountry() : null, false)
				+ countLimitation(candidate != null ? candidate.getQCivilRightsLimitation() : null, true)
				+ countLimitation(candidate != null ? candidate.getQLegalLimitationAnyCountry() : null, true)
				+ countLimitation(candidate != null ? candidate.getQHealthTravelLimitation() : null, true)
				+ countLimitation(candidate != null ? candidate.getQHealthMentalLimitation() : null, true);
	}

	private int countNoLimitation(Boolean answer, boolean noLimitationWhenTrue) {
		if (answer == null) {
			return 0;
		}
		boolean noLimitation = noLimitationWhenTrue ? answer.booleanValue() : !answer.booleanValue();
		return noLimitation ? 1 : 0;
	}

	private int countLimitation(Boolean answer, boolean limitationWhenTrue) {
		if (answer == null) {
			return 0;
		}
		boolean limitation = limitationWhenTrue ? answer.booleanValue() : !answer.booleanValue();
		return limitation ? 1 : 0;
	}

	private int countConfirmedSupports(List<SupportItemView> rows) {
		int count = 0;
		for (SupportItemView row : safeList(rows)) {
			if (row != null && isAcceptedSupportStatus(row.getSupportStatus())) {
				count++;
			}
		}
		return count;
	}

	private int resolveSupportSortPriority(SupportStatus status) {
		if (isAcceptedSupportStatus(status)) {
			return 0;
		}
		if (status == SupportStatus.PROPOSED) {
			return 1;
		}
		return 2;
	}

	private boolean isAcceptedSupportStatus(SupportStatus status) {
		return status == SupportStatus.ACCEPTED || status == SupportStatus.APPROVED;
	}

	private Double parseTrainingScore(String rawScore) {
		if (!hasText(rawScore)) {
			return null;
		}
		String normalized = rawScore.replace(',', '.');
		Matcher matcher = Pattern.compile("-?\\d+(?:\\.\\d+)?").matcher(normalized);
		if (!matcher.find()) {
			return null;
		}
		try {
			return Double.valueOf(matcher.group());
		} catch (Exception e) {
			return null;
		}
	}

	private boolean isDefaultPictureInUse() {
		if (candidate == null) {
			return true;
		}

		String pictureName = candidate.getPictureName();
		if (hasText(pictureName)) {
			String normalized = pictureName.trim().toLowerCase(Locale.ROOT);
			for (String marker : DEFAULT_PICTURE_NAME_MARKERS) {
				if (normalized.contains(marker)) {
					return true;
				}
			}
		}

		byte[] pictureInfo = candidate.getPictureInfo();
		return pictureInfo == null || pictureInfo.length == 0;
	}

	private int countRestrictedCountries(List<CountryLinkItemView> rows) {
		if (rows == null || rows.isEmpty()) {
			return 0;
		}
		int count = 0;
		for (CountryLinkItemView row : rows) {
			if (row != null && isRestrictedCountryCode(row.getCountryCode())) {
				count++;
			}
		}
		return count;
	}

	private boolean hasCitizenshipInLacnicRegion(List<CountryLinkItemView> rows) {
		for (CountryLinkItemView row : safeList(rows)) {
			if (row == null || !row.isCitizen()) {
				continue;
			}
			String normalizedCode = normalizeCountryCode(row.getCountryCode());
			if (hasText(normalizedCode) && COUNTRY_UTILS.isLacnicCoverageCountryCode(normalizedCode)) {
				return true;
			}
		}
		return false;
	}

	private boolean isRestrictedCountryCode(String code) {
		String normalizedCode = normalizeCountryCode(code);
		if (!hasText(normalizedCode) || restrictedCountryCodes == null || restrictedCountryCodes.isEmpty()) {
			return false;
		}
		for (String restrictedCode : restrictedCountryCodes) {
			String normalizedRestrictedCode = normalizeCountryCode(restrictedCode);
			if (hasText(normalizedRestrictedCode) && normalizedCode.equals(normalizedRestrictedCode)) {
				return true;
			}
		}
		return false;
	}

	private boolean isStatutoryElection() {
		return candidate != null
				&& candidate.getElection() != null
				&& ElectionCategory.STATUTORY == candidate.getElection().getCategory();
	}

	private String normalizeCountryCode(String code) {
		if (!hasText(code)) {
			return null;
		}
		String normalized = code.trim().toUpperCase(Locale.ROOT);
		return normalized.length() == 2 ? normalized : normalized;
	}

	private String resolveCountryLabel(String code) {
		String normalizedCode = normalizeCountryCode(code);
		if (!hasText(normalizedCode)) {
			return "-";
		}
		return COUNTRY_UTILS.getDisplayLabel(normalizedCode, getLocale(), true);
	}

	private String formatBoolean(boolean value) {
		return value ? getString("acceptNominationIncompatibilitiesOptionYes") : getString("acceptNominationIncompatibilitiesOptionNo");
	}

	private List<CandidateElectionTaskProgress> getCandidateTaskProgressRows(Candidate candidate, Nomination nomination) {
		List<CandidateElectionTaskProgress> direct = safeList(candidate != null ? candidate.getTaskProgress() : null);
		if (!direct.isEmpty()) {
			return direct;
		}
		if (nomination != null && nomination.getCandidate() != null) {
			return safeList(nomination.getCandidate().getTaskProgress());
		}
		return Collections.emptyList();
	}

	private Set<ElectionTaskKey> loadEnabledTaskKeys(long electionId) {
		if (electionId <= 0) {
			return Collections.emptySet();
		}
		Set<ElectionTaskKey> keys = new HashSet<>();
		try {
			List<ElectionTask> electionTasks = safeList(AppContext.getInstance().getManagerBeanRemote().getElectionTasks(electionId));
			for (ElectionTask row : electionTasks) {
				if (row != null && row.getTaskKey() != null) {
					keys.add(row.getTaskKey());
				}
			}
			return keys;
		} catch (Exception e) {
			return Collections.emptySet();
		}
	}

	private boolean isTaskEnabled(ElectionTaskKey key) {
		return key != null && enabledTaskKeys.contains(key);
	}

	private boolean isAnyTaskEnabled(ElectionTaskKey... keys) {
		if (keys == null || keys.length == 0) {
			return false;
		}
		for (ElectionTaskKey key : keys) {
			if (isTaskEnabled(key)) {
				return true;
			}
		}
		return false;
	}

	private CandidateDeclarationsDefinition loadCandidateDeclarationsDefinition(String nominationToken, ElectionTaskKey taskKey) {
		if (!hasText(nominationToken)) {
			return null;
		}
		try {
			if (taskKey == ElectionTaskKey.DECLARATIONS) {
				return AppContext.getInstance().getPreNominationBeanRemote().getCandidateDeclarationsDefinition(nominationToken, SecurityUtils.getLocale().getLanguage());
			}
			if (taskKey == ElectionTaskKey.DECLARATIONS_NON_STATUTORY) {
				return AppContext.getInstance().getPreNominationBeanRemote().getCandidateNonStatutoryDeclarationsDefinition(nominationToken, SecurityUtils.getLocale().getLanguage());
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	private CandidateElectionTaskStatus resolveTaskStatus(List<CandidateElectionTaskProgress> taskProgressRows, ElectionTaskKey taskKey) {
		for (CandidateElectionTaskProgress row : safeList(taskProgressRows)) {
			if (row == null || row.getElectionTask() == null || row.getElectionTask().getTaskKey() == null) {
				continue;
			}
			if (row.getElectionTask().getTaskKey() == taskKey) {
				return row.getStatus();
			}
		}
		return null;
	}

	private String resolveTaskStatusLabel(CandidateElectionTaskStatus status) {
		if (status == null || status == CandidateElectionTaskStatus.NOT_STARTED) {
			return getString(TokenResourceKeys.ACCEPT_NOMINATION_TASKS_STATUS_PENDING);
		}
		switch (status) {
		case STARTED:
			return getString("acceptNominationTasksStatusInProgress");
		case COMPLETED:
			return getString("acceptNominationTasksStatusCompleted");
		case OMITTED:
			return getString("acceptNominationTasksStatusOmitted");
		case NOT_STARTED:
		default:
			return getString(TokenResourceKeys.ACCEPT_NOMINATION_TASKS_STATUS_PENDING);
		}
	}

	private String resolveTaskStatusBadgeClass(CandidateElectionTaskStatus status) {
		if (status == CandidateElectionTaskStatus.COMPLETED) {
			return BootstrapCssClasses.BADGE_SUCCESS_SUBTLE_TEXT_SUCCESS;
		}
		if (status == CandidateElectionTaskStatus.STARTED) {
			return "badge bg-info-subtle text-info";
		}
		if (status == CandidateElectionTaskStatus.OMITTED) {
			return BootstrapCssClasses.BADGE_SECONDARY_SUBTLE_TEXT_SECONDARY;
		}
		return BootstrapCssClasses.BADGE_WARNING_SUBTLE_TEXT_WARNING;
	}

	private String resolveProctorioFileName(long candidateId, byte[] content) {
		return "proctorio-result-" + candidateId + "." + resolveProctorioExtension(resolveProctorioContentType(content));
	}

	private String resolveProctorioContentType(byte[] content) {
		if (startsWith(content, (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47,
				(byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A)) {
			return "image/png";
		}
		if (startsWith(content, (byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46)) {
			return "application/pdf";
		}
		if (startsWith(content, (byte) 0xFF, (byte) 0xD8, (byte) 0xFF)) {
			return "image/jpeg";
		}
		if (startsWith(content, (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38)) {
			return "image/gif";
		}
		if (startsWith(content, (byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04)) {
			return "application/zip";
		}
		return CONTENT_TYPE_OCTET_STREAM;
	}

	private String resolveProctorioExtension(String contentType) {
		switch (contentType) {
		case "image/png":
			return "png";
		case "application/pdf":
			return "pdf";
		case "image/jpeg":
			return "jpg";
		case "image/gif":
			return "gif";
		case "application/zip":
			return "zip";
		default:
			return "bin";
		}
	}

	private boolean startsWith(byte[] content, byte... signature) {
		if (content == null || signature == null || content.length < signature.length) {
			return false;
		}
		for (int i = 0; i < signature.length; i++) {
			if (content[i] != signature[i]) {
				return false;
			}
		}
		return true;
	}

	private void resolveHeaderTimelineContext(long electionId) {
		headerPhaseLabel = "-";
		headerMilestoneLabel = "-";
		List<ElectionCalendar> calendars = safeElectionCalendars(electionId);
		if (calendars.isEmpty()) {
			return;
		}

		List<ElectionCalendar> publicCalendars = new ArrayList<>();
		for (ElectionCalendar calendar : calendars) {
			if (calendar != null && isPublicCalendar(calendar)) {
				publicCalendars.add(calendar);
			}
		}
		if (publicCalendars.isEmpty()) {
			return;
		}

		publicCalendars.sort(new Comparator<ElectionCalendar>() {
			@Override
			public int compare(ElectionCalendar a, ElectionCalendar b) {
				Date aStart = a != null ? a.getStartDate() : null;
				Date bStart = b != null ? b.getStartDate() : null;
				if (aStart == null && bStart == null) {
					return compareCalendarKey(a, b);
				}
				if (aStart == null) {
					return 1;
				}
				if (bStart == null) {
					return -1;
				}
				int startCompare = aStart.compareTo(bStart);
				if (startCompare != 0) {
					return startCompare;
				}
				return compareCalendarKey(a, b);
			}
		});

		int currentIndex = resolveCurrentTimelineIndex(publicCalendars);
		ElectionCalendar selected = publicCalendars.get(currentIndex);
		ElectionCalendarKey key = selected != null ? selected.getCalendarKey() : null;
		headerPhaseLabel = resolvePhaseLabel(key);
		headerMilestoneLabel = extractTitle(resolveCalendarDescription(key));
	}

	private int compareCalendarKey(ElectionCalendar a, ElectionCalendar b) {
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

	private int resolveCurrentTimelineIndex(List<ElectionCalendar> calendars) {
		Date now = new Date();
		for (int i = 0; i < calendars.size(); i++) {
			ElectionCalendar calendar = calendars.get(i);
			Date start = calendar != null ? calendar.getStartDate() : null;
			Date end = resolveTimelineEndDate(calendar);
			if (start != null && end != null && !now.before(start) && !now.after(end)) {
				return i;
			}
		}
		for (int i = 0; i < calendars.size(); i++) {
			ElectionCalendar calendar = calendars.get(i);
			Date start = calendar != null ? calendar.getStartDate() : null;
			if (start != null && now.before(start)) {
				return i;
			}
		}
		return calendars.size() - 1;
	}

	private Date resolveTimelineEndDate(ElectionCalendar calendar) {
		if (calendar == null || calendar.getStartDate() == null) {
			return null;
		}
		if (isSingleCalendarKey(calendar.getCalendarKey())) {
			return calendar.getStartDate();
		}
		return calendar.getEndDate() != null ? calendar.getEndDate() : calendar.getStartDate();
	}

	private boolean isSingleCalendarKey(ElectionCalendarKey key) {
		return key != null && key.name().contains("_SINGLE_");
	}

	private String resolveCalendarDescription(ElectionCalendarKey key) {
		if (key == null) {
			return "";
		}
		String language = SecurityUtils.getLocale().getLanguage();
		if ("en".equalsIgnoreCase(language)) {
			return key.getDescriptionEN();
		}
		if ("pt".equalsIgnoreCase(language)) {
			return key.getDescriptionPT();
		}
		return key.getDescriptionES();
	}

	private String extractTitle(String description) {
		if (!hasText(description)) {
			return getString("auditPublicV2UntitledMilestone");
		}
		int index = description.indexOf('.');
		if (index > 0) {
			return description.substring(0, index).trim();
		}
		return description;
	}

	private String resolvePhaseLabel(ElectionCalendarKey key) {
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

	private List<ElectionCalendar> safeElectionCalendars(long electionId) {
		if (electionId <= 0) {
			return Collections.emptyList();
		}
		try {
			List<ElectionCalendar> calendars = AppContext.getInstance().getManagerBeanRemote().getElectionCalendars(electionId);
			return calendars != null ? calendars : Collections.<ElectionCalendar>emptyList();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private boolean isPublicCalendar(ElectionCalendar calendar) {
		Object publicable = readFieldValue(calendar, "publicable");
		return publicable instanceof Boolean && ((Boolean) publicable).booleanValue();
	}

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
	}

	private String valueOrEmpty(String value) {
		return value != null ? value : "";
	}

	private String formatMessage(String key, Object... arguments) {
		return MessageFormat.format(getString(key), arguments);
	}

	private boolean isVisibleCandidateStatusForAuditor(CandidateStatus status) {
		return status == CandidateStatus.PRECOMPLETE
				|| status == CandidateStatus.CONFIRMED_AND_PUBLISHED
				|| status == CandidateStatus.COMPLETE
				|| status == CandidateStatus.REJECTED;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String normalizeExternalUrl(String value) {
		String trimmedValue = trimToNull(value);
		if (!hasText(trimmedValue)) {
			return null;
		}
		if (trimmedValue.startsWith("http://") || trimmedValue.startsWith("https://")) {
			return trimmedValue;
		}
		return "https://" + trimmedValue;
	}

	private String buildAnchorHtml(String href, String text, String cssClass) {
		if (!hasText(href)) {
			return valueOrDash(text);
		}
		String safeHref = escapeHtml(href);
		String safeText = escapeHtml(valueOrDash(text));
		String safeClass = hasText(cssClass) ? " class=\"" + escapeHtml(cssClass) + "\"" : "";
		return "<a href=\"" + safeHref + "\" target=\"_blank\" rel=\"noopener noreferrer\"" + safeClass + ">" + safeText + "</a>";
	}

	private String escapeHtml(String value) {
		if (value == null) {
			return "";
		}
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}

	private String buildPepDeclarationText(String declarationText, String pepLabel) {
		String baseText = valueOrDash(declarationText);
		String selectedLabel = hasText(pepLabel) && !"-".equals(pepLabel) ? pepLabel : null;
		if (!hasText(selectedLabel)) {
			return baseText;
		}
		return selectedLabel;
	}

	private Object readFieldValue(Object instance, String fieldName) {
		if (instance == null || fieldName == null) {
			return null;
		}
		try {
			Field field = instance.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(instance);
		} catch (Exception e) {
			return null;
		}
	}

	private Long readEntityLongField(Object instance, String fieldName) {
		Object value = readFieldValue(instance, fieldName);
		if (value == null) {
			return null;
		}
		try {
			return Long.valueOf(String.valueOf(value));
		} catch (Exception e) {
			return null;
		}
	}

	private <T> List<T> safeList(List<T> source) {
		if (source == null) {
			return Collections.emptyList();
		}
		try {
			source.size();
			return source;
		} catch (RuntimeException e) {
			return Collections.emptyList();
		}
	}

	private enum SummaryCardType {
		INCOMPATIBILITIES("auditPublicCandidateDetailSummaryTitleIncompatibilities", "ti ti-alert-hexagon"),
		COUNTRIES("auditPublicCandidateDetailSummaryTitleCountries", "ti ti-map"),
		PROFILE("auditPublicCandidateDetailSummaryTitleProfile", "ti ti-user-circle"),
		COURSE("auditPublicCandidateDetailSummaryTitleTraining", "ti ti-school"),
		EVALUATION("auditPublicCandidateDetailSummaryTitleEvaluation", "ti ti-school"),
		DECLARATIONS("auditPublicCandidateDetailSummaryTitleDeclarations", "ti ti-lock"),
		DECLARATIONS_NON_STATUTORY("acceptNominationDeclarationsNonStatutoryTitle", "ti ti-lock"),
		OTHER_STATUTORY_QUESTIONS("auditPublicCandidateDetailSummaryTitleOtherStatutoryQuestions", "ti ti-files"),
		OTHER_NON_STATUTORY_QUESTIONS("auditPublicCandidateDetailSummaryTitleOtherQuestions", "ti ti-files"),
		ORGANIZATIONS("auditPublicCandidateDetailSummaryTitleOrganizations", "ti ti-building"),
		SUPPORTS("auditPublicCandidateDetailSummaryTitleSupports", "ti ti-users");

		private final String titleKey;
		private final String iconClass;

		SummaryCardType(String titleKey, String iconClass) {
			this.titleKey = titleKey;
			this.iconClass = iconClass;
		}

		public String getTitleKey() {
			return titleKey;
		}

		public String getIconClass() {
			return iconClass;
		}
	}

	private enum SummaryCardSeverity {
		SUCCESS("bg-success bg-opacity-10", "text-bg-success bg-opacity-90"),
		WARNING("bg-warning bg-opacity-10", "text-bg-warning bg-opacity-90"),
		DANGER("bg-danger bg-opacity-10", "text-bg-danger bg-opacity-90");

		private final String cardClass;
		private final String iconContainerClass;

		SummaryCardSeverity(String cardClass, String iconContainerClass) {
			this.cardClass = cardClass;
			this.iconContainerClass = iconContainerClass;
		}

		public String getCardClass() {
			return cardClass;
		}

		public String getIconContainerClass() {
			return iconContainerClass;
		}
	}

	private final class SummaryCardsFactory {
		private final List<CandidateElectionTaskProgress> taskProgressRows;

		SummaryCardsFactory(List<CandidateElectionTaskProgress> taskProgressRows) {
			this.taskProgressRows = safeList(taskProgressRows);
		}

		List<SummaryCardView> build() {
			List<SummaryCardView> cards = new ArrayList<>();
			List<SummaryCardType> displayOrder = getDisplayOrder();

			for (SummaryCardType type : displayOrder) {
				if (!isEnabled(type)) {
					continue;
				}
				cards.add(buildConfiguredCard(type));
			}

			return cards;
		}

		private List<SummaryCardType> getDisplayOrder() {
			LinkedHashSet<SummaryCardType> orderedTypes = new LinkedHashSet<>(getPriorityOrder());
			Collections.addAll(orderedTypes, SummaryCardType.values());
			return new ArrayList<>(orderedTypes);
		}

		private List<SummaryCardType> getPriorityOrder() {
			// Configuración central del criterio de prioridad visual.
			List<SummaryCardType> order = new ArrayList<>();
			order.add(SummaryCardType.PROFILE);
			order.add(SummaryCardType.COUNTRIES);
			order.add(SummaryCardType.INCOMPATIBILITIES);
			order.add(SummaryCardType.COURSE);
			order.add(SummaryCardType.EVALUATION);
			order.add(SummaryCardType.SUPPORTS);
			order.add(SummaryCardType.OTHER_STATUTORY_QUESTIONS);
			order.add(SummaryCardType.OTHER_NON_STATUTORY_QUESTIONS);
			order.add(SummaryCardType.ORGANIZATIONS);
			order.add(SummaryCardType.DECLARATIONS);
			order.add(SummaryCardType.DECLARATIONS_NON_STATUTORY);
			return order;
		}

		private boolean isEnabled(SummaryCardType type) {
			switch (type) {
			case INCOMPATIBILITIES:
				return isTaskEnabled(ElectionTaskKey.INCOMPATIBILITIES);
			case COUNTRIES:
				return isTaskEnabled(ElectionTaskKey.COUNTRIES);
			case PROFILE:
				return isTaskEnabled(ElectionTaskKey.PROFILE);
			case COURSE:
				return isTaskEnabled(ElectionTaskKey.COURSE);
			case EVALUATION:
				return isTaskEnabled(ElectionTaskKey.EVALUATION);
			case DECLARATIONS:
				return isTaskEnabled(ElectionTaskKey.DECLARATIONS) && !safeList(statutoryDeclarationAnswers).isEmpty();
			case DECLARATIONS_NON_STATUTORY:
				return isTaskEnabled(ElectionTaskKey.DECLARATIONS_NON_STATUTORY) && !safeList(nonStatutoryDeclarationAnswers).isEmpty();
			case OTHER_STATUTORY_QUESTIONS:
				return isTaskEnabled(ElectionTaskKey.OTHER_STATUTORY_QUESTIONS);
			case OTHER_NON_STATUTORY_QUESTIONS:
				return isTaskEnabled(ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS);
			case ORGANIZATIONS:
				return isTaskEnabled(ElectionTaskKey.ORGANIZATIONS);
			case SUPPORTS:
				return isAnyTaskEnabled(ElectionTaskKey.ORG_SUPPORTS, ElectionTaskKey.USER_SUPPORTS_2, ElectionTaskKey.USER_SUPPORTS_5);
			default:
				return false;
			}
		}

		private SummaryCardView buildConfiguredCard(SummaryCardType type) {
			switch (type) {
			case INCOMPATIBILITIES:
				return buildIncompatibilitiesCard();
			case COUNTRIES:
				return buildCountriesCard();
			case PROFILE:
				return buildProfileCard();
			case COURSE:
				return buildTrainingCard(
						SummaryCardType.COURSE,
						ElectionTaskKey.COURSE,
						"auditPublicCandidateDetailSummaryDescTrainingCompleted",
						"auditPublicCandidateDetailSummaryDescTrainingPending");
			case EVALUATION:
				return buildTrainingCard(
						SummaryCardType.EVALUATION,
						ElectionTaskKey.EVALUATION,
						"auditPublicCandidateDetailSummaryDescEvaluationCompleted",
						"auditPublicCandidateDetailSummaryDescEvaluationPending");
			case DECLARATIONS:
				return buildDeclarationsCard(SummaryCardType.DECLARATIONS, ElectionTaskKey.DECLARATIONS);
			case DECLARATIONS_NON_STATUTORY:
				return buildDeclarationsCard(SummaryCardType.DECLARATIONS_NON_STATUTORY, ElectionTaskKey.DECLARATIONS_NON_STATUTORY);
			case OTHER_STATUTORY_QUESTIONS:
				return buildQuestionCard(type, ElectionTaskKey.OTHER_STATUTORY_QUESTIONS, statutoryAnswers);
			case OTHER_NON_STATUTORY_QUESTIONS:
				return buildQuestionCard(type, ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS, nonStatutoryAnswers);
			case ORGANIZATIONS:
				return buildOrganizationsCard();
			case SUPPORTS:
				return buildSupportsCard();
			default:
				throw new IllegalArgumentException("Unsupported summary card type: " + type);
			}
		}

		private SummaryCardView buildIncompatibilitiesCard() {
			int noLimitationCount = countNoLimitationAnswers();
			int limitationCount = countLimitationAnswers();
			int answeredCount = countAnswered(incompatibilityAnswers);
			CandidateElectionTaskStatus status = resolveSummaryTaskStatus(taskProgressRows, ElectionTaskKey.INCOMPATIBILITIES);
			SummaryCardSeverity severity;
			String description;
			if (answeredCount < INCOMPATIBILITIES_TOTAL_QUESTIONS) {
				severity = SummaryCardSeverity.WARNING;
				description = formatMessage(
						"auditPublicCandidateDetailSummaryDescIncompatibilitiesIncomplete",
						answeredCount,
						INCOMPATIBILITIES_TOTAL_QUESTIONS);
			} else if (noLimitationCount == INCOMPATIBILITIES_TOTAL_QUESTIONS && status == CandidateElectionTaskStatus.COMPLETED) {
				severity = SummaryCardSeverity.SUCCESS;
				description = getString("auditPublicCandidateDetailSummaryDescIncompatibilitiesAllGood");
			} else {
				severity = SummaryCardSeverity.WARNING;
				description = limitationCount == 1
						? getString("auditPublicCandidateDetailSummaryDescIncompatibilitiesOne")
						: formatMessage("auditPublicCandidateDetailSummaryDescIncompatibilitiesMany", limitationCount);
			}
			return createCard(
					SummaryCardType.INCOMPATIBILITIES,
					noLimitationCount + "/" + INCOMPATIBILITIES_TOTAL_QUESTIONS,
					description,
					severity);
		}

		private SummaryCardView buildCountriesCard() {
			int restrictedCountriesCount = countRestrictedCountries(countryRows);
			int totalCountries = safeList(countryRows).size();
			SummaryCardSeverity severity = restrictedCountriesCount > 0 ? SummaryCardSeverity.WARNING : SummaryCardSeverity.SUCCESS;
			String value = String.valueOf(totalCountries);
			String description = restrictedCountriesCount > 0
					? (restrictedCountriesCount == 1
							? getString("auditPublicCandidateDetailSummaryDescCountriesRestrictedOne")
							: formatMessage("auditPublicCandidateDetailSummaryDescCountriesRestrictedMany", restrictedCountriesCount))
					: getString("auditPublicCandidateDetailSummaryDescCountriesNoRestricted");
			return createCard(
					SummaryCardType.COUNTRIES,
					value,
					description,
					severity);
		}

		private SummaryCardView buildProfileCard() {
			boolean hasLocalizedBio = hasText(resolveLocalizedBio());
			boolean defaultPictureInUse = isDefaultPictureInUse();
			boolean hasNonDefaultPicture = !defaultPictureInUse;
			SummaryCardSeverity severity = hasNonDefaultPicture && hasLocalizedBio
					? SummaryCardSeverity.SUCCESS
					: SummaryCardSeverity.WARNING;
			String value = hasNonDefaultPicture && hasLocalizedBio
					? getString(KEY_SUMMARY_VALUE_OK)
					: getString("auditPublicCandidateDetailSummaryValuePartial");
			String description;
			if (hasNonDefaultPicture && hasLocalizedBio) {
				description = getString("auditPublicCandidateDetailSummaryDescProfileComplete");
			} else if (hasLocalizedBio && defaultPictureInUse) {
				description = getString("auditPublicCandidateDetailSummaryDescProfileDefaultPicture");
			} else if (hasNonDefaultPicture) {
				description = getString("auditPublicCandidateDetailSummaryDescProfileMissingBio");
			} else {
				description = getString("auditPublicCandidateDetailSummaryDescProfileMissingBioAndPhoto");
			}
			return createCard(
					SummaryCardType.PROFILE,
					value,
					description,
					severity);
		}

		private SummaryCardView buildTrainingCard(SummaryCardType type, ElectionTaskKey taskKey, String completedDescriptionKey, String pendingDescriptionKey) {
			CandidateElectionTaskStatus status = resolveSummaryTaskStatus(taskProgressRows, taskKey);
			String value;
			String description;
			SummaryCardSeverity severity;

			if (hasProctorioFile) {
				value = valueOrDash(proctorioScore);
				Double parsedScore = parseTrainingScore(proctorioScore);
				if (parsedScore == null) {
					severity = SummaryCardSeverity.WARNING;
					description = getString("auditPublicCandidateDetailSummaryDescTrainingScoreUnavailable");
				} else if (parsedScore.doubleValue() > TRAINING_SCORE_GREEN_MIN) {
					severity = SummaryCardSeverity.SUCCESS;
					description = getString("auditPublicCandidateDetailSummaryDescTrainingScore");
				} else if (parsedScore.doubleValue() <= TRAINING_SCORE_RED_MAX) {
					severity = SummaryCardSeverity.DANGER;
					description = getString("auditPublicCandidateDetailSummaryDescTrainingScoreLow");
				} else {
					severity = SummaryCardSeverity.WARNING;
					description = getString("auditPublicCandidateDetailSummaryDescTrainingScoreReview");
				}
			} else if (status == CandidateElectionTaskStatus.COMPLETED) {
				value = getString(KEY_SUMMARY_VALUE_OK);
				description = getString(completedDescriptionKey);
				severity = SummaryCardSeverity.SUCCESS;
			} else {
				value = valueOrDash(resolveTaskStatusLabel(status));
				description = getString(pendingDescriptionKey);
				severity = SummaryCardSeverity.WARNING;
			}

			return createCard(
					type,
					value,
					description,
					severity);
		}

		private SummaryCardView buildDeclarationsCard(SummaryCardType type, ElectionTaskKey taskKey) {
			CandidateElectionTaskStatus status = resolveSummaryTaskStatus(taskProgressRows, taskKey);
			SummaryCardSeverity severity = status == CandidateElectionTaskStatus.COMPLETED ? SummaryCardSeverity.SUCCESS : SummaryCardSeverity.WARNING;
			String value = status == CandidateElectionTaskStatus.COMPLETED
					? getString(KEY_SUMMARY_VALUE_OK)
					: valueOrDash(resolveTaskStatusLabel(status));
			String description = status == CandidateElectionTaskStatus.COMPLETED
					? getString("auditPublicCandidateDetailSummaryDescDeclarationsCompleted")
					: getString("auditPublicCandidateDetailSummaryDescDeclarationsPending");
			return createCard(
					type,
					value,
					description,
					severity);
		}

		private SummaryCardView buildQuestionCard(SummaryCardType type, ElectionTaskKey taskKey, List<AnswerItemView> answers) {
			if (type == SummaryCardType.OTHER_STATUTORY_QUESTIONS) {
				int answered = Math.min(countAnswered(answers), STATUTORY_TOTAL_QUESTIONS);
				SummaryCardSeverity severity = answered == STATUTORY_TOTAL_QUESTIONS
						? SummaryCardSeverity.SUCCESS
						: SummaryCardSeverity.WARNING;
				return createCard(
						type,
						answered + "/" + STATUTORY_TOTAL_QUESTIONS,
						getString(KEY_SUMMARY_DESC_ANSWERED),
						severity);
			}

			int total = safeList(answers).size();
			int answered = countAnswered(answers);
			SummaryCardSeverity severity;
			String description;
			if (total == 0) {
				severity = SummaryCardSeverity.WARNING;
				description = getString("auditPublicCandidateDetailSummaryDescQuestionsNotConfigured");
			} else if (answered == total && isTaskCompleted(taskProgressRows, taskKey)) {
				severity = SummaryCardSeverity.SUCCESS;
				description = getString(KEY_SUMMARY_DESC_ANSWERED);
			} else {
				severity = SummaryCardSeverity.WARNING;
				description = getString(KEY_SUMMARY_DESC_ANSWERED);
			}
			return createCard(
					type,
					answered + "/" + total,
					description,
					severity);
		}

		private SummaryCardView buildOrganizationsCard() {
			int total = safeList(organizationRows).size();
			String value = total == 0 ? getString(KEY_SUMMARY_VALUE_OK) : String.valueOf(total);
			return createCard(
					SummaryCardType.ORGANIZATIONS,
					value,
					getString("auditPublicCandidateDetailSummaryDescOrganizationsDeclared"),
					SummaryCardSeverity.SUCCESS);
		}

		private SummaryCardView buildSupportsCard() {
			int total = safeList(supportRows).size();
			SupportRequirement requirement = resolveSupportRequirement();
			int metric = requirement.requireConfirmed ? countConfirmedSupports(supportRows) : total;
			int reached = requirement.required > 0 ? Math.min(metric, requirement.required) : metric;
			SummaryCardSeverity severity = requirement.required > 0 && metric >= requirement.required
					? SummaryCardSeverity.SUCCESS
					: SummaryCardSeverity.DANGER;
			String value = requirement.required > 0 ? reached + "/" + requirement.required : String.valueOf(metric);
			String description = requirement.requireConfirmed
					? getString("auditPublicCandidateDetailSummaryDescSupportsConfirmed")
					: getString("auditPublicCandidateDetailSummaryDescSupportsRegistered");
			return createCard(
					SummaryCardType.SUPPORTS,
					value,
					description,
					severity);
		}

		private SupportRequirement resolveSupportRequirement() {
			if (isTaskEnabled(ElectionTaskKey.USER_SUPPORTS_5)) {
				return new SupportRequirement(5, true);
			}
			if (isTaskEnabled(ElectionTaskKey.USER_SUPPORTS_2)) {
				return new SupportRequirement(2, true);
			}
			if (isTaskEnabled(ElectionTaskKey.ORG_SUPPORTS)) {
				return new SupportRequirement(2, false);
			}
			return new SupportRequirement(0, false);
		}

		private final class SupportRequirement {
			private final int required;
			private final boolean requireConfirmed;

			SupportRequirement(int required, boolean requireConfirmed) {
				this.required = required;
				this.requireConfirmed = requireConfirmed;
			}
		}

		private SummaryCardView createCard(SummaryCardType type, String value, String description, SummaryCardSeverity severity) {
			return new SummaryCardView(
					getString(type.getTitleKey()),
					valueOrDash(value),
					valueOrDash(description),
					severity.getCardClass(),
					severity.getIconContainerClass(),
					type.getIconClass());
		}
	}

	private static final class CalendarRange implements Serializable {
		private static final long serialVersionUID = 1L;
		private final Date start;
		private final Date end;

		CalendarRange(Date start, Date end) {
			this.start = start;
			this.end = end;
		}

		Date getStart() {
			return start;
		}

		Date getEnd() {
			return end;
		}
	}

	private enum DecisionStage {
		PRECOMPLETE,
		COMPLETE
	}

	private static final class StageDecisionView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final AuditorCandidateDecisionStatus status;
		private final Date date;
		private final String comment;

		StageDecisionView(AuditorCandidateDecisionStatus status, Date date, String comment) {
			this.status = status;
			this.date = date;
			this.comment = comment;
		}

		static StageDecisionView empty() {
			return new StageDecisionView(null, null, "");
		}

		AuditorCandidateDecisionStatus getStatus() {
			return status;
		}

		Date getDate() {
			return date;
		}

		String getComment() {
			return comment;
		}

		boolean hasReaction() {
			return status == AuditorCandidateDecisionStatus.PREAPPROVED
					|| status == AuditorCandidateDecisionStatus.APPROVED
					|| status == AuditorCandidateDecisionStatus.REJECTED;
		}

		boolean isEditableStatus() {
			return hasReaction();
		}
	}

	private static final class AuditorDecisionView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final AuditorCandidateDecisionStatus legacyStatus;
		private final Date legacyDate;
		private final AuditorCandidateDecisionStatus preStatus;
		private final Date preDate;
		private final String preComment;
		private final AuditorCandidateDecisionStatus finalStatus;
		private final Date finalDate;
		private final String finalComment;
		private final Date preapprovedDate;
		private final Date approvedDate;

		AuditorDecisionView(
				AuditorCandidateDecisionStatus legacyStatus,
				Date legacyDate,
				AuditorCandidateDecisionStatus preStatus,
				Date preDate,
				String preComment,
				AuditorCandidateDecisionStatus finalStatus,
				Date finalDate,
				String finalComment,
				Date preapprovedDate,
				Date approvedDate) {
			this.legacyStatus = legacyStatus;
			this.legacyDate = legacyDate;
			this.preStatus = preStatus;
			this.preDate = preDate;
			this.preComment = preComment;
			this.finalStatus = finalStatus;
			this.finalDate = finalDate;
			this.finalComment = finalComment;
			this.preapprovedDate = preapprovedDate;
			this.approvedDate = approvedDate;
		}

		static AuditorDecisionView empty() {
			return new AuditorDecisionView(null, null, null, null, "", null, null, "", null, null);
		}

		AuditorCandidateDecisionStatus getLegacyStatus() {
			return legacyStatus;
		}

		Date getLegacyDate() {
			return legacyDate;
		}

		AuditorCandidateDecisionStatus getPreStatus() {
			return preStatus;
		}

		Date getPreDate() {
			return preDate;
		}

		String getPreComment() {
			return preComment;
		}

		AuditorCandidateDecisionStatus getFinalStatus() {
			return finalStatus;
		}

		Date getFinalDate() {
			return finalDate;
		}

		String getFinalComment() {
			return finalComment;
		}

		Date getPreapprovedDate() {
			return preapprovedDate;
		}

		Date getApprovedDate() {
			return approvedDate;
		}
	}

	private static final class SummaryCardView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String title;
		private final String value;
		private final String description;
		private final String cardClass;
		private final String iconContainerClass;
		private final String iconClass;

		SummaryCardView(String title, String value, String description, String cardClass, String iconContainerClass, String iconClass) {
			this.title = title;
			this.value = value;
			this.description = description;
			this.cardClass = cardClass;
			this.iconContainerClass = iconContainerClass;
			this.iconClass = iconClass;
		}

		public String getTitle() {
			return title;
		}

		public String getValue() {
			return value;
		}

		public String getDescription() {
			return description;
		}

		public String getCardClass() {
			return cardClass;
		}

		public String getIconContainerClass() {
			return iconContainerClass;
		}

		public String getIconClass() {
			return iconClass;
		}
	}

	private static final class AnswerItemView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String question;
		private final String answer;
		private final boolean answered;
		private final boolean accepted;
		private String description;

		AnswerItemView(String question, String answer, boolean answered, boolean accepted) {
			this.question = question;
			this.answer = answer;
			this.answered = answered;
			this.accepted = accepted;
			this.description = "";
		}

		public String getQuestion() {
			return question;
		}

		public String getAnswer() {
			return answer;
		}

		public boolean isAnswered() {
			return answered;
		}

		public boolean isAccepted() {
			return accepted;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	private static final class SupportItemView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String title;
		private String displayTitle;
		private final String subtitle;
		private final SupportStatus supportStatus;
		private final String statusBadgeText;
		private final String statusBadgeClass;
		private final String supportDateText;

		SupportItemView(String title, String subtitle, SupportStatus supportStatus, String statusBadgeText, String statusBadgeClass, String supportDateText) {
			this.title = title;
			this.displayTitle = title;
			this.subtitle = subtitle;
			this.supportStatus = supportStatus;
			this.statusBadgeText = statusBadgeText;
			this.statusBadgeClass = statusBadgeClass;
			this.supportDateText = supportDateText;
		}

		public String getTitle() {
			return title;
		}

		public String getDisplayTitle() {
			return displayTitle;
		}

		public void setDisplayTitle(String displayTitle) {
			this.displayTitle = displayTitle;
		}

		public String getSubtitle() {
			return subtitle;
		}

		public SupportStatus getSupportStatus() {
			return supportStatus;
		}

		public String getStatusBadgeText() {
			return statusBadgeText;
		}

		public String getStatusBadgeClass() {
			return statusBadgeClass;
		}

		public String getSupportDateText() {
			return supportDateText;
		}
	}

	private static final class WorkOrganizationItemView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String name;
		private final String group;
		private final String typeText;
		private final String typeBadgeText;
		private final String typeBadgeClass;

		WorkOrganizationItemView(String name, String group, String typeText, String typeBadgeText, String typeBadgeClass) {
			this.name = name;
			this.group = group;
			this.typeText = typeText;
			this.typeBadgeText = typeBadgeText;
			this.typeBadgeClass = typeBadgeClass;
		}

		public String getName() {
			return name;
		}

		public String getGroup() {
			return group;
		}

		public String getTypeText() {
			return typeText;
		}

		public String getTypeBadgeText() {
			return typeBadgeText;
		}

		public String getTypeBadgeClass() {
			return typeBadgeClass;
		}
	}

	private static final class CountryLinkItemView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String countryLabel;
		private final boolean primaryCountry;
		private final boolean citizen;
		private final String qCitizen;
		private final String qResidenceOver5y;
		private final String qLongEmploymentOrAdvisory5y;
		private final String qFamilyResidenceOver5y;
		private final String qInternetCommunityOrgParticipation;
		private final String qEligibleForCitizenship;
		private final String countryCode;

		CountryLinkItemView(String countryLabel, boolean primaryCountry, boolean citizen, String qCitizen, String qResidenceOver5y,
				String qLongEmploymentOrAdvisory5y, String qFamilyResidenceOver5y, String qInternetCommunityOrgParticipation,
				String qEligibleForCitizenship, String countryCode) {
			this.countryLabel = countryLabel;
			this.primaryCountry = primaryCountry;
			this.citizen = citizen;
			this.qCitizen = qCitizen;
			this.qResidenceOver5y = qResidenceOver5y;
			this.qLongEmploymentOrAdvisory5y = qLongEmploymentOrAdvisory5y;
			this.qFamilyResidenceOver5y = qFamilyResidenceOver5y;
			this.qInternetCommunityOrgParticipation = qInternetCommunityOrgParticipation;
			this.qEligibleForCitizenship = qEligibleForCitizenship;
			this.countryCode = countryCode;
		}

		public String getCountryLabel() {
			return countryLabel;
		}

		public boolean isPrimaryCountry() {
			return primaryCountry;
		}

		public boolean isCitizen() {
			return citizen;
		}

		public String getQCitizen() {
			return qCitizen;
		}

		public String getQResidenceOver5y() {
			return qResidenceOver5y;
		}

		public String getQLongEmploymentOrAdvisory5y() {
			return qLongEmploymentOrAdvisory5y;
		}

		public String getQFamilyResidenceOver5y() {
			return qFamilyResidenceOver5y;
		}

		public String getQInternetCommunityOrgParticipation() {
			return qInternetCommunityOrgParticipation;
		}

		public String getQEligibleForCitizenship() {
			return qEligibleForCitizenship;
		}

		public String getCountryCode() {
			return countryCode;
		}
	}

	private static final class BadgeView implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String text;
		private final String cssClass;

		BadgeView(String text, String cssClass) {
			this.text = text;
			this.cssClass = cssClass;
		}

		public String getText() {
			return text;
		}

		public String getCssClass() {
			return cssClass;
		}
	}
}
