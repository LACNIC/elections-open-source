package net.lacnic.elections.adminweb.ui.admin.election.candidates;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.utils.LinksUtils;

public class ManageCandidateTranslationsDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private final FeedbackPanel feedbackPanel;
	private Candidate candidate;
	private Nomination nomination;
	private Nomination editableNomination;

	public ManageCandidateTranslationsDashboard(PageParameters params) {
		super(params);

		feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		feedbackPanel.setOutputMarkupPlaceholderTag(true);
		add(feedbackPanel);

		long electionId = UtilsParameters.getIdAsLong(params);
		long candidateId = UtilsParameters.getCandidateAsLong(params);
		candidate = AppContext.getInstance().getManagerBeanRemote().getCandidate(candidateId);
		nomination = AppContext.getInstance().getManagerBeanRemote().getNominationByCandidateId(candidateId);
		editableNomination = nomination != null ? nomination : new Nomination();

		if (!isValidCandidate(candidate, electionId) || candidate.isAbstention()) {
			getSession().error(getString("candidateAnswersCandidateNotFound"));
			setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			return;
		}

		add(new Label("candidateName", valueOrDash(candidate.getName())));
		add(new Label("candidateId", String.valueOf(candidate.getCandidateId())));
		add(new Label("candidateMail", valueOrDash(candidate.getMail())));

		List<String> statutoryQuestions = loadOtherStatutoryQuestions();
		List<String> nonStatutoryQuestions = loadOtherNonStatutoryQuestions();

		Form<Void> translationsForm = new Form<>("translationsForm");
		add(translationsForm);
		String defaultProfileLink = buildPublicCandidateProfileLink(candidate);
		addLinkField(translationsForm, "linkSpanish", "candidateProfileLinkSpanishLabel", defaultProfileLink);
		addLinkField(translationsForm, "linkEnglish", "candidateProfileLinkEnglishLabel", defaultProfileLink);
		addLinkField(translationsForm, "linkPortuguese", "candidateProfileLinkPortugueseLabel", defaultProfileLink);

		String statutoryQuestion1Label = questionAt(statutoryQuestions, 0, "acceptNominationOtherStatutoryQuestionsQuestion1");
		String statutoryQuestion2Label = questionAt(statutoryQuestions, 1, "acceptNominationOtherStatutoryQuestionsQuestion2");
		String statutoryQuestion3Label = questionAt(statutoryQuestions, 2, "acceptNominationOtherStatutoryQuestionsQuestion3");
		String statutoryQuestion4Label = questionAt(statutoryQuestions, 3, "acceptNominationOtherStatutoryQuestionsQuestion4");
		String nonStatutoryQuestion1Label = questionAt(nonStatutoryQuestions, 0, "acceptNominationOtherNonStatutoryQuestionsQuestion1");

		translationsForm.add(new Label("statutoryQuestion1Label", statutoryQuestion1Label));
		translationsForm.add(new Label("statutoryQuestion2Label", statutoryQuestion2Label));
		translationsForm.add(new Label("statutoryQuestion3Label", statutoryQuestion3Label));
		translationsForm.add(new Label("statutoryQuestion4Label", statutoryQuestion4Label));
		translationsForm.add(new Label("nonStatutoryQuestion1Label", nonStatutoryQuestion1Label));

		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer1Spanish", statutoryQuestion1Label);
		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer1English", statutoryQuestion1Label);
		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer1Portuguese", statutoryQuestion1Label);
		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer2Spanish", statutoryQuestion2Label);
		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer2English", statutoryQuestion2Label);
		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer2Portuguese", statutoryQuestion2Label);
		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer3Spanish", statutoryQuestion3Label);
		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer3English", statutoryQuestion3Label);
		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer3Portuguese", statutoryQuestion3Label);
		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer4Spanish", statutoryQuestion4Label);
		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer4English", statutoryQuestion4Label);
		addTranslationEditor(translationsForm, "qOtherStatutoryAnswer4Portuguese", statutoryQuestion4Label);
		addTranslationEditor(translationsForm, "qOtherNonStatutoryAnswer1Spanish", nonStatutoryQuestion1Label);
		addTranslationEditor(translationsForm, "qOtherNonStatutoryAnswer1English", nonStatutoryQuestion1Label);
		addTranslationEditor(translationsForm, "qOtherNonStatutoryAnswer1Portuguese", nonStatutoryQuestion1Label);

		WebMarkupContainer nominationReasonContainer = new WebMarkupContainer("nominationReasonContainer");
		nominationReasonContainer.setVisible(nomination != null);
		translationsForm.add(nominationReasonContainer);

		String nominationReasonLabel = getString("nominationCandidateReasonLabel");
		addNominationReasonEditor(nominationReasonContainer, "nominationReasonSpanish", nominationReasonLabel);
		addNominationReasonEditor(nominationReasonContainer, "nominationReasonEnglish", nominationReasonLabel);
		addNominationReasonEditor(nominationReasonContainer, "nominationReasonPortuguese", nominationReasonLabel);

		translationsForm.add(new Button("save") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				persistTranslations(electionId);
			}
		});

		translationsForm.add(new Link<Void>("cancel") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
			}
		});
	}

	private boolean isValidCandidate(Candidate loadedCandidate, long electionId) {
		return loadedCandidate != null
				&& loadedCandidate.getElection() != null
				&& loadedCandidate.getElection().getElectionId() == electionId;
	}

	private void addTranslationEditor(Form<Void> form, String propertyName, String styleContext) {
		form.add(new AiAssistTextAreaPanel(
				propertyName + "Editor",
				new PropertyModel<>(candidate, propertyName),
				Model.of(styleContext),
				this::addExternalFeedback,
				(originalText, instruction, context) -> AppContext.getInstance().getPreNominationBeanRemote().improveCandidateTextWithStatus(
						null,
						originalText,
						instruction,
						context,
						SecurityUtils.getClientIp()),
				false,
				true,
				true,
				false,
				false,
				1000,
				4,
				true));
	}

	private void addNominationReasonEditor(WebMarkupContainer container, String propertyName, String styleContext) {
		container.add(new AiAssistTextAreaPanel(
				propertyName + "Editor",
				new PropertyModel<>(editableNomination, propertyName),
				Model.of(styleContext),
				this::addExternalFeedback,
				(originalText, instruction, context) -> AppContext.getInstance().getPreNominationBeanRemote().improveCandidateTextWithStatus(
						null,
						originalText,
						instruction,
						context,
						SecurityUtils.getClientIp()),
				false,
				true,
				true,
				false,
				false,
				2000,
				4,
				true));
	}

	private void addLinkField(Form<Void> form, String propertyName, String labelKey, String defaultProfileLink) {
		form.add(new Label(propertyName + "Label", getString(labelKey)));
		form.add(new Label(propertyName + "Help", buildCandidateProfileLinkHelp(defaultProfileLink)));
		TextField<String> field = new TextField<>(propertyName, new PropertyModel<>(candidate, propertyName));
		field.setRequired(false);
		field.add(StringValidator.maximumLength(1000));
		field.add(new UrlValidator());
		form.add(field);
	}

	private String buildCandidateProfileLinkHelp(String defaultProfileLink) {
		String resourceKey = hasText(defaultProfileLink) ? "candidateProfileLinkHelp" : "candidateProfileLinkHelpNoUrl";
		return new StringResourceModel(resourceKey, this, null).setParameters(defaultProfileLink).getString();
	}

	private String buildPublicCandidateProfileLink(Candidate currentCandidate) {
		if (currentCandidate == null || currentCandidate.getElection() == null || currentCandidate.getCandidateId() <= 0L) {
			return "";
		}
		String publicElectionToken = currentCandidate.getElection().getPublicElectionToken();
		if (!hasText(publicElectionToken)) {
			return "";
		}
		return LinksUtils.buildPublicCandidateProfileLink(publicElectionToken, currentCandidate.getCandidateId());
	}

	private void persistTranslations(long electionId) {
		try {
			AppContext.getInstance().getManagerBeanRemote().editCandidate(candidate, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
			if (nomination != null) {
				AppContext.getInstance().getManagerBeanRemote().editNomination(nomination, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
			}
			getSession().info(getString("candidateTranslationsSaveSuccess"));
			setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(electionId));
		} catch (Exception e) {
			appLogger.error("Error updating candidate translations for candidateId={}", candidate.getCandidateId(), e);
			getSession().error(getString("candidateTranslationsSaveError"));
		}
	}

	private List<String> loadOtherStatutoryQuestions() {
		List<String> fallback = new ArrayList<>();
		fallback.add(getString("acceptNominationOtherStatutoryQuestionsQuestion1"));
		fallback.add(getString("acceptNominationOtherStatutoryQuestionsQuestion2"));
		fallback.add(getString("acceptNominationOtherStatutoryQuestionsQuestion3"));
		fallback.add(getString("acceptNominationOtherStatutoryQuestionsQuestion4"));
		try {
			List<String> fromBean = AppContext.getInstance().getPreNominationBeanRemote().getOtherStatutoryQuestions(SecurityUtils.getLocale().getLanguage());
			if (fromBean != null && !fromBean.isEmpty()) {
				return fromBean;
			}
		} catch (Exception e) {
			appLogger.warn("Unable to load statutory question labels for translations dashboard", e);
		}
		return fallback;
	}

	private List<String> loadOtherNonStatutoryQuestions() {
		List<String> fallback = new ArrayList<>();
		fallback.add(getString("acceptNominationOtherNonStatutoryQuestionsQuestion1"));
		try {
			List<String> fromBean = AppContext.getInstance().getPreNominationBeanRemote().getOtherNonStatutoryQuestions(SecurityUtils.getLocale().getLanguage());
			if (fromBean != null && !fromBean.isEmpty()) {
				return fromBean;
			}
		} catch (Exception e) {
			appLogger.warn("Unable to load non statutory question labels for translations dashboard", e);
		}
		return fallback;
	}

	private String questionAt(List<String> questions, int index, String fallbackKey) {
		if (questions != null && questions.size() > index && questions.get(index) != null && !questions.get(index).trim().isEmpty()) {
			return questions.get(index);
		}
		return getString(fallbackKey);
	}

	private String valueOrDash(String value) {
		return value != null && !value.trim().isEmpty() ? value : getString("candidateManagemenListNoLink");
	}

	private void addExternalFeedback(AjaxRequestTarget target) {
		target.add(feedbackPanel);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
