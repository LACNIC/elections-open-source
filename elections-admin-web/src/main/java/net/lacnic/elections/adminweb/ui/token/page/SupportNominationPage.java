package net.lacnic.elections.adminweb.ui.token.page;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonRejectNomination;
import net.lacnic.elections.adminweb.ui.token.AnswerCardRowView;
import net.lacnic.elections.adminweb.ui.token.CandidateAnswersTaskCardPanel;
import net.lacnic.elections.adminweb.ui.token.CandidateCountriesCardPanel;
import net.lacnic.elections.adminweb.ui.token.CandidateProfileInfoCardPanel;
import net.lacnic.elections.adminweb.ui.token.CountryCardRowView;
import net.lacnic.elections.adminweb.ui.token.TokenResourceKeys;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.pre.CandidateCountryLink;
import net.lacnic.elections.domain.pre.CandidateDeclarationCode;
import net.lacnic.elections.domain.pre.CandidateDeclarationDefinition;
import net.lacnic.elections.domain.pre.CandidateDeclarationInputType;
import net.lacnic.elections.domain.pre.CandidateDeclarationOptionDefinition;
import net.lacnic.elections.domain.pre.CandidateDeclarationsDefinition;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidatePepDeclaration;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;
import net.lacnic.elections.utils.CountryUtils;

import net.lacnic.elections.adminweb.wicket.util.BootstrapCssClasses;
import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class SupportNominationPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;
	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();

	private SupportNomination supportNomination;
	private Nomination nomination;
	private Candidate candidate;

	private Set<ElectionTaskKey> enabledTaskKeys = Collections.emptySet();
	private List<CandidateElectionTaskProgress> taskProgressRows = Collections.emptyList();

	private List<AnswerItemView> incompatibilityAnswers = Collections.emptyList();
	private List<AnswerItemView> statutoryAnswers = Collections.emptyList();
	private List<AnswerItemView> nonStatutoryAnswers = Collections.emptyList();
	private List<AnswerItemView> statutoryDeclarationAnswers = Collections.emptyList();
	private List<AnswerItemView> nonStatutoryDeclarationAnswers = Collections.emptyList();
	private List<CountryLinkItemView> countryRows = Collections.emptyList();
	private List<String> restrictedCountryCodes = Collections.emptyList();
	private boolean showNoRegionalCitizenshipWarning;

	private String electionTitle = "-";
	private String candidateName = "-";
	private String candidateMail = "-";
	private String profileLinkedin = "-";
	private String profileBio = "-";
	private String supportCurrentDate = "-";
	private String supportDecisionHeaderSubtitle = "-";
	private String supportDecisionContextParagraph = "-";
	private String supportSubjectLabel = "-";
	private String supportSubjectValue = "-";
	private String nominationOrganizationName = "-";
	private String nominationOrganizationId = "-";
	private String nominationOrganizationCountry = "-";
	private String nominationContactName = "-";
	private boolean confirmSupportChecked;
	private boolean confirmInformationChecked;
	private String confirmSupportQuestionText = "-";
	private String confirmInformationQuestionText = "-";

	public SupportNominationPage() {
		this(new PageParameters());
	}

	public SupportNominationPage(PageParameters params) {
		super(params);
		loadData();

		add(new FeedbackPanel("feedbackPanel"));

		boolean profileTaskEnabled = isTaskEnabled(ElectionTaskKey.PROFILE);
		boolean incompatibilityTaskEnabled = isTaskEnabled(ElectionTaskKey.INCOMPATIBILITIES);
		boolean statutoryDeclarationTaskEnabled = isTaskEnabled(ElectionTaskKey.DECLARATIONS) && !statutoryDeclarationAnswers.isEmpty();
		boolean countryTaskEnabled = isTaskEnabled(ElectionTaskKey.COUNTRIES);

		Component candidatePicture = buildCandidatePicture("candidatePicture");
		CandidateProfileInfoCardPanel profileInfoCard = new CandidateProfileInfoCardPanel(
				"profileInfoCard",
				candidatePicture,
				candidateName,
				candidateMail,
				profileLinkedin,
				profileBio);
		profileInfoCard.setVisible(profileTaskEnabled);
		add(profileInfoCard);

		WebMarkupContainer nominationCard = new WebMarkupContainer("nominationCard");
		nominationCard.setVisible(hasNominationInformation());
		nominationCard.add(new Label("nominationOrganizationName", valueOrDash(nominationOrganizationName)));
		nominationCard.add(new Label("nominationOrganizationId", valueOrDash(nominationOrganizationId)));
		nominationCard.add(new Label("nominationOrganizationCountry", valueOrDash(nominationOrganizationCountry)));
		nominationCard.add(new Label("nominationContactName", valueOrDash(nominationContactName)));
		add(nominationCard);

		BadgeView incompatibilityBadge = resolveSummaryTaskBadge(taskProgressRows, ElectionTaskKey.INCOMPATIBILITIES);
		CandidateAnswersTaskCardPanel incompatibilityCard = new CandidateAnswersTaskCardPanel(
				"incompatibilityCard",
				getString("auditPublicCandidateDetailIncompatibilitiesTitle"),
				incompatibilityBadge.getText(),
				incompatibilityBadge.getCssClass(),
				incompatibilityAnswers,
				true,
				false);
		incompatibilityCard.setVisible(incompatibilityTaskEnabled);
		add(incompatibilityCard);

		BadgeView declarationBadge = resolveSummaryTaskBadge(taskProgressRows, ElectionTaskKey.DECLARATIONS);
		CandidateAnswersTaskCardPanel declarationCard = new CandidateAnswersTaskCardPanel(
				"declarationCard",
				getString("acceptNominationDeclarationsTitle"),
				declarationBadge.getText(),
				declarationBadge.getCssClass(),
				statutoryDeclarationAnswers,
				false,
				true);
		declarationCard.setVisible(statutoryDeclarationTaskEnabled);
		add(declarationCard);

		BadgeView countryBadge = resolveSummaryTaskBadge(taskProgressRows, ElectionTaskKey.COUNTRIES);
		CandidateCountriesCardPanel countryCard = new CandidateCountriesCardPanel(
				"countryCard",
				countryBadge.getText(),
				countryBadge.getCssClass(),
				countryRows,
				showNoRegionalCitizenshipWarning,
				getString("auditPublicCandidateDetailNoRegionalCitizenshipWarning"));
		countryCard.setVisible(countryTaskEnabled);
		add(countryCard);

		SupportStatus supportStatus = currentSupportStatus();
		WebMarkupContainer supportDecisionCard = new WebMarkupContainer("supportDecisionCard");
		supportDecisionCard.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveDecisionCardClass(supportStatus)));
		add(supportDecisionCard);

		WebMarkupContainer supportDecisionCardHeader = new WebMarkupContainer("supportDecisionCardHeader");
		supportDecisionCardHeader.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  resolveDecisionCardHeaderClass(supportStatus)));
		supportDecisionCardHeader.add(new Label("supportDecisionHeaderSubtitle", valueOrDash(supportDecisionHeaderSubtitle)));
		supportDecisionCard.add(supportDecisionCardHeader);

		Form<Void> supportDecisionForm = buildSupportDecisionForm();
		supportDecisionForm.setVisible(!isDecisionLockedForSupport(supportStatus));
		supportDecisionCard.add(supportDecisionForm);

		WebMarkupContainer supportDecisionCompletedContainer = new WebMarkupContainer("supportDecisionCompletedContainer");
		supportDecisionCompletedContainer.setVisible(isDecisionLockedForSupport(supportStatus));
		supportDecisionCompletedContainer.add(new Label("decisionCompletedStatus", valueOrDash(resolveDecisionCompletedStatusText(supportStatus))));
		String decisionSupportEmail = resolveDecisionSupportEmail();
		boolean hasDecisionSupportEmail = hasText(decisionSupportEmail);
		Label decisionCompletedSupportMailLink = new Label(
				"decisionCompletedSupportMailLink",
				buildAnchorHtml(hasDecisionSupportEmail ? "mailto:" + decisionSupportEmail : null, valueOrDash(decisionSupportEmail), "link-primary fw-semibold"));
		decisionCompletedSupportMailLink.setEscapeModelStrings(false);
		decisionCompletedSupportMailLink.setVisible(hasDecisionSupportEmail);
		supportDecisionCompletedContainer.add(decisionCompletedSupportMailLink);
		supportDecisionCompletedContainer.add(new Label("decisionCompletedSupportMailText", valueOrDash(decisionSupportEmail))
				.setVisible(!hasDecisionSupportEmail));
		supportDecisionCard.add(supportDecisionCompletedContainer);
	}

	private void loadData() {
		nomination = resolveNominationWithDetails();
		if (nomination == null) {
			nomination = supportNomination != null ? supportNomination.getNomination() : null;
		}

		Candidate nominationCandidate = nomination != null ? nomination.getCandidate() : null;
		if (nominationCandidate != null && nominationCandidate.getCandidateId() > 0) {
			try {
				Candidate loaded = AppContext.getInstance().getManagerBeanRemote().getCandidate(nominationCandidate.getCandidateId());
				candidate = loaded != null ? loaded : nominationCandidate;
			} catch (Exception e) {
				candidate = nominationCandidate;
			}
		}

		if (nomination != null && nomination.getElection() != null) {
			electionTitle = nomination.getElection().getTitle(SecurityUtils.getLocale().getLanguage());
			if (!hasText(electionTitle)) {
				electionTitle = nomination.getElection().getTitleSpanish();
			}
			enabledTaskKeys = loadEnabledTaskKeys(nomination.getElection().getElectionId());
		}
		resolveNominationCardData();

		taskProgressRows = getCandidateTaskProgressRows(candidate, nomination);

		candidateName = candidate != null ? candidate.getName() : null;
		candidateMail = candidate != null ? candidate.getMail() : null;
		profileLinkedin = candidate != null ? candidate.getLinkedinUrl() : null;
		profileBio = valueOrDash(resolveLocalizedBio());
		supportCurrentDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, SecurityUtils.getLocale()).format(new Date());
		resolveSupportDecisionTexts();

		incompatibilityAnswers = buildIncompatibilityAnswers();
		statutoryAnswers = buildOtherStatutoryAnswers();
		nonStatutoryAnswers = buildOtherNonStatutoryAnswers();
		statutoryDeclarationAnswers = buildDeclarationAnswers(ElectionTaskKey.DECLARATIONS);
		nonStatutoryDeclarationAnswers = buildDeclarationAnswers(ElectionTaskKey.DECLARATIONS_NON_STATUTORY);

		long electionId = nomination != null && nomination.getElection() != null
				? nomination.getElection().getElectionId()
				: candidate != null && candidate.getElection() != null ? candidate.getElection().getElectionId() : 0L;
		long candidateId = candidate != null ? candidate.getCandidateId() : 0L;
		restrictedCountryCodes = loadRestrictedCountryCodes(electionId);
		countryRows = buildCountryRows(loadCandidateCountryLinks(electionId, candidateId, candidate));
		showNoRegionalCitizenshipWarning = isStatutoryElection() && !safeList(countryRows).isEmpty() && !hasCitizenshipInLacnicRegion(countryRows);
	}

	private Form<Void> buildSupportDecisionForm() {
		Form<Void> form = new Form<>("supportDecisionForm");
		form.add(new CheckBox("confirmSupportCheckbox", new PropertyModel<>(this, "confirmSupportChecked")));
		form.add(new CheckBox("confirmInformationCheckbox", new PropertyModel<>(this, "confirmInformationChecked")));
		form.add(new Label("confirmSupportQuestion", valueOrDash(confirmSupportQuestionText)));
		form.add(new Label("confirmInformationQuestion", valueOrDash(confirmInformationQuestionText)));

		form.add(new Button("confirmSupportButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				submitSupportDecision(SupportStatus.ACCEPTED);
			}
		});

		form.add(new ButtonRejectNomination(
				"rejectSupportButton",
				"supportNominationRejectAskButton",
				"supportNominationRejectIrreversible",
				"supportNominationRejectConfirmButton",
				"supportNominationRejectCancelButton",
				"btn btn-sm btn-light",
				"btn btn-sm btn-danger",
				"btn btn-sm btn-light") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onConfirm() {
				submitSupportDecision(SupportStatus.REJECTED);
			}
		});

		form.add(new Label("decisionElectionTitle", valueOrDash(electionTitle)));
		form.add(new Label("decisionCandidateName", valueOrDash(candidateName)));
		form.add(new Label("decisionSupportSubjectLabel", valueOrDash(supportSubjectLabel)));
		form.add(new Label("decisionSupportSubjectValue", valueOrDash(supportSubjectValue)));
		form.add(new Label("decisionCurrentDate", valueOrDash(supportCurrentDate)));
		form.add(new Label("decisionContextParagraph", valueOrDash(supportDecisionContextParagraph)));
		form.add(new Label("supportStatusValue", valueOrDash(resolveDecisionCompletedStatusText(currentSupportStatus()))));
		return form;
	}

	private void submitSupportDecision(SupportStatus targetStatus) {
		if (targetStatus == SupportStatus.ACCEPTED) {
			if (!confirmSupportChecked || !confirmInformationChecked) {
				error(safeString("supportNominationDecisionValidationChecksRequired", "Para confirmar el apoyo debe marcar ambas confirmaciones."));
				return;
			}
		}
		boolean updated = AppContext.getInstance().getPreNominationBeanRemote().updateSupportNominationStatus(getToken(), targetStatus, SecurityUtils.getClientIp());
		if (!updated) {
			error(safeString("supportNominationDecisionUpdateError", "No se pudo actualizar el estado de la solicitud de apoyo."));
			return;
		}
		if (targetStatus == SupportStatus.ACCEPTED) {
			info(isOrganizationSupportFlow()
					? safeString("supportNominationDecisionAcceptedOrganization", "Su organización confirmó el apoyo a la candidatura.")
					: safeString("supportNominationDecisionAcceptedUser", "Usted confirmó el apoyo a la candidatura."));
		} else {
			info(isOrganizationSupportFlow()
					? safeString("supportNominationDecisionRejectedOrganization", "Su organización rechazó el apoyo a la candidatura.")
					: safeString("supportNominationDecisionRejectedUser", "Usted rechazó el apoyo a la candidatura."));
		}
		setResponsePage(SupportNominationPage.class, new PageParameters(getPageParameters()));
	}

	private SupportStatus currentSupportStatus() {
		return supportNomination != null ? supportNomination.getSupportStatus() : null;
	}

	private boolean isDecisionLockedForSupport(SupportStatus status) {
		return status == SupportStatus.ACCEPTED
				|| status == SupportStatus.REJECTED
				|| status == SupportStatus.APPROVED
				|| status == SupportStatus.INVALID;
	}

	private String resolveDecisionCardClass(SupportStatus status) {
		if (status == SupportStatus.ACCEPTED || status == SupportStatus.APPROVED) {
			return "card mb-3 border border-success shadow-sm";
		}
		if (status == SupportStatus.REJECTED || status == SupportStatus.INVALID) {
			return "card mb-3 border border-danger shadow-sm";
		}
		return "card mb-3 border border-dark shadow-sm";
	}

	private String resolveDecisionCardHeaderClass(SupportStatus status) {
		if (status == SupportStatus.ACCEPTED || status == SupportStatus.APPROVED) {
			return "card-header bg-primary text-white";
		}
		if (status == SupportStatus.REJECTED || status == SupportStatus.INVALID) {
			return "card-header bg-danger text-white";
		}
		return "card-header bg-dark text-white";
	}

	private String resolveDecisionCompletedStatusText(SupportStatus status) {
		return mapSupportStatusText(status);
	}

	private String resolveDecisionSupportEmail() {
		if (nomination == null || nomination.getElection() == null) {
			return null;
		}
		String defaultRecipient = trimToNull(nomination.getElection().getDefaultRecipient());
		if (defaultRecipient != null) {
			return defaultRecipient;
		}
		return trimToNull(nomination.getElection().getDefaultSender());
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

	private void resolveSupportDecisionTexts() {
		if (isOrganizationSupportFlow()) {
			supportDecisionHeaderSubtitle = safeString(
					"supportNominationDecisionHeaderOrganization",
					"Confirme o rechace el apoyo de su organización a esta candidatura.");
			supportDecisionContextParagraph = safeString(
					"supportNominationDecisionContextOrganization",
					"Este es el espacio donde su organización puede confirmar o rechazar el apoyo a esta candidatura. Revise la información del candidato y luego registre su decisión.");
			confirmSupportQuestionText = safeString(
					"supportNominationDecisionQuestion1Organization",
					"Marque esta opción si su organización confirma el apoyo a la persona candidata en esta elección.");
			confirmInformationQuestionText = safeString(
					"supportNominationDecisionQuestion2Organization",
					"Marque esta opción si su organización respalda la veracidad de la información presentada por la persona candidata, así como su competencia e idoneidad.");
			supportSubjectLabel = safeString("supportNominationDecisionSupportSubjectLabelOrganization", "Organización de apoyo");
			supportSubjectValue = valueOrDash(resolveSupportingOrganizationValue());
			return;
		}

		supportDecisionHeaderSubtitle = safeString(
				"supportNominationDecisionHeaderUser",
				"Confirme o rechace su apoyo a esta candidatura.");
		supportDecisionContextParagraph = safeString(
				"supportNominationDecisionContextUser",
				"Este es el espacio donde puede confirmar o rechazar su apoyo a esta candidatura. Revise la información del candidato y luego registre su decisión.");
		confirmSupportQuestionText = safeString(
				"supportNominationDecisionQuestion1User",
				"Marque esta opción si usted confirma su apoyo a la persona candidata en esta elección.");
		confirmInformationQuestionText = safeString(
				"supportNominationDecisionQuestion2User",
				"Marque esta opción si usted respalda la veracidad de la información presentada por la persona candidata, así como su competencia e idoneidad.");
		supportSubjectLabel = safeString("supportNominationDecisionSupportSubjectLabelUser", "Contacto de apoyo");
		supportSubjectValue = valueOrDash(resolveSupportingContactValue());
	}

	private boolean hasNominationInformation() {
		return hasText(nominationOrganizationName)
				|| hasText(nominationOrganizationId)
				|| hasText(nominationOrganizationCountry)
				|| hasText(nominationContactName);
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

				Label answerBadge = new Label("answerBadge", "Atención");
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
			String question = questions.size() > i ? questions.get(i) : "Pregunta " + (i + 1);
			rows.add(buildTextAnswer(question, answers[i]));
		}
		return rows;
	}

	private List<AnswerItemView> buildOtherNonStatutoryAnswers() {
		List<AnswerItemView> rows = new ArrayList<>();
		List<String> questions = safeOtherNonStatutoryQuestions();
		String question = !questions.isEmpty() ? questions.get(0) : "Pregunta";
		String answer = resolveLocalizedOtherNonStatutoryAnswer1();
		rows.add(buildTextAnswer(question, answer));
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
					isRestrictedCountryCode(normalizedCode),
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

	private List<CandidateCountryLink> loadCandidateCountryLinks(long electionId, long candidateId, Candidate fallbackCandidate) {
		if (electionId <= 0 || candidateId <= 0) {
			return safeList(fallbackCandidate != null ? fallbackCandidate.getCountryLinks() : null);
		}
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
			// Fallback below.
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
		if (candidate != null && candidate.getElection() != null) {
			return ElectionCategory.STATUTORY == candidate.getElection().getCategory();
		}
		return nomination != null
				&& nomination.getElection() != null
				&& ElectionCategory.STATUTORY == nomination.getElection().getCategory();
	}

	private String normalizeCountryCode(String code) {
		if (!hasText(code)) {
			return null;
		}
		return code.trim().toUpperCase(Locale.ROOT);
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

	private AnswerItemView buildBooleanAnswer(String question, Boolean value, boolean positiveWhenTrue) {
		if (value == null) {
			return new AnswerItemView(question, getString(TokenResourceKeys.CANDIDATE_ANSWERS_NO_DATA), false, false);
		}
		if (value.booleanValue()) {
			return new AnswerItemView(question, getString("acceptNominationIncompatibilitiesOptionYes"), true, positiveWhenTrue);
		}
		return new AnswerItemView(question, getString("acceptNominationIncompatibilitiesOptionNo"), true, !positiveWhenTrue);
	}

	private AnswerItemView buildTextAnswer(String question, String value) {
		if (!hasText(value)) {
			return new AnswerItemView(question, getString(TokenResourceKeys.CANDIDATE_ANSWERS_NO_DATA), false, false);
		}
		return new AnswerItemView(question, value, true, true);
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

	private String buildPepDeclarationText(String declarationText, String pepLabel) {
		String baseText = valueOrDash(declarationText);
		String selectedLabel = hasText(pepLabel) && !"-".equals(pepLabel) ? pepLabel : null;
		if (!hasText(selectedLabel)) {
			return baseText;
		}
		return selectedLabel;
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
			return selectBySessionLanguage(candidate.getQOtherStatutoryAnswer1Spanish(), candidate.getQOtherStatutoryAnswer1English(), candidate.getQOtherStatutoryAnswer1Portuguese());
		case 2:
			return selectBySessionLanguage(candidate.getQOtherStatutoryAnswer2Spanish(), candidate.getQOtherStatutoryAnswer2English(), candidate.getQOtherStatutoryAnswer2Portuguese());
		case 3:
			return selectBySessionLanguage(candidate.getQOtherStatutoryAnswer3Spanish(), candidate.getQOtherStatutoryAnswer3English(), candidate.getQOtherStatutoryAnswer3Portuguese());
		case 4:
			return selectBySessionLanguage(candidate.getQOtherStatutoryAnswer4Spanish(), candidate.getQOtherStatutoryAnswer4English(), candidate.getQOtherStatutoryAnswer4Portuguese());
		default:
			return null;
		}
	}

	private String resolveLocalizedOtherNonStatutoryAnswer1() {
		if (candidate == null) {
			return null;
		}
		return selectBySessionLanguage(candidate.getQOtherNonStatutoryAnswer1Spanish(), candidate.getQOtherNonStatutoryAnswer1English(), candidate.getQOtherNonStatutoryAnswer1Portuguese());
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

	private Nomination resolveNominationWithDetails() {
		if (supportNomination == null || supportNomination.getNomination() == null) {
			return null;
		}
		Nomination baseNomination = supportNomination.getNomination();
		String token = baseNomination.getAcceptNominationToken();
		if (hasText(token)) {
			try {
				Nomination detailed = AppContext.getInstance().getPreNominationBeanRemote().verifyAcceptNominationAccess(token);
				if (detailed != null) {
					return detailed;
				}
			} catch (Exception e) {
				// Fallback below
			}
		}
		return baseNomination;
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
			return "badge bg-success-subtle text-success";
		}
		if (status == CandidateElectionTaskStatus.STARTED) {
			return "badge bg-info-subtle text-info";
		}
		if (status == CandidateElectionTaskStatus.OMITTED) {
			return "badge bg-secondary-subtle text-secondary";
		}
		return BootstrapCssClasses.BADGE_WARNING_SUBTLE_TEXT_WARNING;
	}

	private String mapSupportStatusText(SupportStatus status) {
		if (status == null) {
			return "Pendiente";
		}
		String prefix = supportNomination != null && supportNomination.getSupportingOrganization() != null
				? "acceptNominationOrgSupportsStatus"
				: "acceptNominationUserSupportsStatus";
		switch (status) {
		case PROPOSED:
			return safeString(prefix + "Proposed", "Solicitado");
		case ACCEPTED:
			return safeString(prefix + "Accepted", "Aceptado");
		case REJECTED:
			return safeString(prefix + "Rejected", "Rechazado");
		case INVALID:
			return safeString(prefix + "Invalid", "Inválido");
		case APPROVED:
			return safeString(prefix + "Approved", "Aprobado");
		default:
			return status.name();
		}
	}

	private String safeString(String key, String fallback) {
		try {
			return getString(key);
		} catch (Exception e) {
			return fallback;
		}
	}

	private String resolveSupportingOrganizationValue() {
		if (supportNomination == null || supportNomination.getSupportingOrganization() == null) {
			return "-";
		}
		String name = supportNomination.getSupportingOrganization().getName();
		String orgId = supportNomination.getSupportingOrganization().getOrgId();
		if (name == null || name.isEmpty()) {
			return orgId == null || orgId.isEmpty() ? "-" : orgId;
		}
		if (orgId == null || orgId.isEmpty()) {
			return name;
		}
		return name + " (" + orgId + ")";
	}

	private boolean isOrganizationSupportFlow() {
		return supportNomination != null && supportNomination.getSupportingOrganization() != null;
	}

	private String resolveSupportingContactValue() {
		if (supportNomination == null) {
			return "-";
		}
		String name = trimToNull(supportNomination.getSupportingContactName());
		String email = trimToNull(supportNomination.getSupportingContactEmail());
		if (hasText(name) && hasText(email)) {
			return name + " (" + email + ")";
		}
		if (hasText(name)) {
			return name;
		}
		if (hasText(email)) {
			return email;
		}
		return "-";
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

	private String valueOrDash(String value) {
		return hasText(value) ? value : "-";
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

	@Override
	protected Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params) {
		supportNomination = AppContext.getInstance().getPreNominationBeanRemote().verifySupportNominationAccess(getToken());
		if (supportNomination == null) {
			return Error404.class;
		}
		nomination = supportNomination.getNomination();

		setWhereAmI("Pantalla support nomination usando SupportNomination.token");
		setContextClass(SupportNomination.class.getName());
		setContextData("supportNominationId: " + supportNomination.getId() + "\n"
				+ "supportStatus: " + supportNomination.getSupportStatus() + "\n"
				+ "supportingContactName: " + supportNomination.getSupportingContactName() + "\n"
				+ "supportingContactEmail: " + supportNomination.getSupportingContactEmail() + "\n"
				+ "nominationId: " + (nomination != null ? nomination.getId() : "-"));

		String headerUser = trimToNull(supportNomination.getSupportingContactName());
		if (!hasText(headerUser) && supportNomination.getSupportingOrganization() != null) {
			headerUser = trimToNull(supportNomination.getSupportingOrganization().getName());
		}
		setHeaderUserDisplay(valueOrDash(headerUser));

		if (nomination != null) {
			setElection(nomination.getElection());
		}
		return null;
	}

	@Override
	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		if (getElection() != null && !getElection().isNominationSupportLinkAvailable()) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.ACCESS_NOT_AVAILABLE,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
					null,
					null));
		}
		ElectionCalendar calendar = findPublicCalendar(ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
		if (calendar == null || calendar.getStartDate() == null) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.PUBLIC_ELECTION_NOT_AVAILABLE,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
					null,
					null));
		}
		TokenAccessGate.AccessBlock before = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.PUBLIC_ELECTION_NOT_AVAILABLE,
				PublicAccessDeniedPage.CountdownTargetDate.START,
				ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
				null,
				null);
		TokenAccessGate.AccessBlock after = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.PUBLIC_ELECTION_NOT_AVAILABLE,
				PublicAccessDeniedPage.CountdownTargetDate.END,
				ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES,
				null,
				null);
		return TokenAccessGate.of(calendar.getStartDate(), calendar.getEndDate(), before, after);
	}

	@Override
	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionNominationSupport");
	}

	private static final class AnswerItemView implements Serializable, AnswerCardRowView {
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

	private static final class CountryLinkItemView implements Serializable, CountryCardRowView {
		private static final long serialVersionUID = 1L;
		private final String countryLabel;
		private final boolean primaryCountry;
		private final boolean restrictedCountry;
		private final boolean citizen;
		private final String qCitizen;
		private final String qResidenceOver5y;
		private final String qLongEmploymentOrAdvisory5y;
		private final String qFamilyResidenceOver5y;
		private final String qInternetCommunityOrgParticipation;
		private final String qEligibleForCitizenship;
		private final String countryCode;

		CountryLinkItemView(String countryLabel, boolean primaryCountry, boolean restrictedCountry, boolean citizen, String qCitizen, String qResidenceOver5y,
				String qLongEmploymentOrAdvisory5y, String qFamilyResidenceOver5y, String qInternetCommunityOrgParticipation,
				String qEligibleForCitizenship, String countryCode) {
			this.countryLabel = countryLabel;
			this.primaryCountry = primaryCountry;
			this.restrictedCountry = restrictedCountry;
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

		public boolean isRestrictedCountry() {
			return restrictedCountry;
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
