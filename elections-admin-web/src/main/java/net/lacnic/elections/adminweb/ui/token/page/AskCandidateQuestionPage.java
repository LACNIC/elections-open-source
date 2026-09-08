package net.lacnic.elections.adminweb.ui.token.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.PublicTokenBasePage;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;

public class AskCandidateQuestionPage extends PublicTokenBasePage {

	private static final long serialVersionUID = 1L;

	private List<Candidate> publishedCandidates = new ArrayList<>();
	private Candidate selectedCandidate;
	private String askedByName;
	private String askedByEmail;
	private LanguageCode questionLanguage = LanguageCode.SP;
	private String questionText;

	public AskCandidateQuestionPage() {
		this(new PageParameters());
	}

	public AskCandidateQuestionPage(PageParameters params) {
		super(params);

		publishedCandidates = new ArrayList<>(AppContext.getInstance().getManagerBeanRemote().getElectionPublishedCandidates(getElection().getElectionId()));
		add(new FeedbackPanel("feedbackPanel"));
		add(new Label("questionElectionTitle", resolveElectionTitle()));

		boolean hasCandidates = !publishedCandidates.isEmpty();
		WebMarkupContainer noCandidates = new WebMarkupContainer("noCandidatesContainer");
		noCandidates.setVisible(!hasCandidates);
		add(noCandidates);

		Form<Void> questionForm = new Form<>("questionForm");
		questionForm.setVisible(hasCandidates);
		add(questionForm);

		DropDownChoice<Candidate> candidateChoice = new DropDownChoice<>("candidate", new PropertyModel<>(this, "selectedCandidate"), publishedCandidates, CANDIDATE_RENDERER);
		candidateChoice.setRequired(true);
		candidateChoice.setNullValid(false);
		questionForm.add(candidateChoice);

		TextField<String> askedByField = new TextField<>("askedByName", new PropertyModel<>(this, "askedByName"));
		askedByField.setRequired(true);
		askedByField.add(StringValidator.maximumLength(500));
		questionForm.add(askedByField);

		EmailTextField askedByEmailField = new EmailTextField("askedByEmail", new PropertyModel<>(this, "askedByEmail"));
		askedByEmailField.setRequired(true);
		askedByEmailField.add(StringValidator.maximumLength(320));
		askedByEmailField.add(EmailAddressValidator.getInstance());
		questionForm.add(askedByEmailField);

		DropDownChoice<LanguageCode> languageChoice = new DropDownChoice<>("questionLanguage", new PropertyModel<>(this, "questionLanguage"), Arrays.asList(LanguageCode.values()), LANGUAGE_RENDERER);
		languageChoice.setRequired(true);
		languageChoice.setNullValid(false);
		questionForm.add(languageChoice);

		AiAssistTextAreaPanel questionTextEditor = new AiAssistTextAreaPanel(
				"questionTextEditor",
				new PropertyModel<>(this, "questionText"),
				null,
				null,
				(originalText, instruction, styleContext) -> null,
				false,
				true,
				false,
				false,
				false,
				1000,
				6,
				textArea -> textArea.setRequired(true));
		questionForm.add(questionTextEditor);

		questionForm.add(new SubmitLink("submitQuestion") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (selectedCandidate == null) {
					error(getString("publicQuestionCandidateRequired"));
					return;
				}

				boolean created = AppContext.getInstance().getManagerBeanRemote().createCandidateQuestionFromPublicToken(getToken(), selectedCandidate.getCandidateId(), askedByName, askedByEmail, questionLanguage, questionText, SecurityUtils.getClientIp());
				if (!created) {
					error(getString("publicQuestionCreateError"));
					return;
				}

				info(getString("publicQuestionCreateSuccess"));
				selectedCandidate = null;
				askedByName = null;
				askedByEmail = null;
				questionLanguage = LanguageCode.SP;
				questionText = null;
				setResponsePage(AskCandidateQuestionPage.class, new PageParameters(getPageParameters()));
			}
		});
	}

	@Override
	protected Class<? extends org.apache.wicket.request.component.IRequestablePage> validateToken(PageParameters params) {
		Election election = AppContext.getInstance().getManagerBeanRemote().getElectionByQuestionToken(getToken());
		if (election == null) {
			return Error404.class;
		}

		setElection(election);
		setWhereAmI("Formulario público de preguntas usando Election.publicElectionToken");
		setContextClass(Election.class.getName());
		setContextData("electionId: " + election.getElectionId() + "\n" + "titleSpanish: " + election.getTitleSpanish() + "\n" + "publicElectionLinkAvailable: " + election.isPublicElectionLinkAvailable());
		setHeaderUserDisplay(getString("publicQuestionHeaderUser"));
		return null;
	}

	@Override
	protected TokenAccessGate resolveTokenAccessGate(PageParameters params) {
		if (getElection() != null && !getElection().isPublicElectionLinkAvailable()) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.ACCESS_NOT_AVAILABLE,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS,
					null,
					null));
		}
		ElectionCalendar calendar = findPublicCalendar(ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
		if (calendar == null || calendar.getStartDate() == null) {
			return TokenAccessGate.always(new TokenAccessGate.AccessBlock(
					PublicAccessDeniedPage.ErrorCode.CANDIDATE_QUESTIONS_NOT_OPEN,
					PublicAccessDeniedPage.CountdownTargetDate.START,
					ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS,
					null,
					null));
		}
		TokenAccessGate.AccessBlock before = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.CANDIDATE_QUESTIONS_NOT_OPEN,
				PublicAccessDeniedPage.CountdownTargetDate.START,
				ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS,
				null,
				null);
		TokenAccessGate.AccessBlock after = new TokenAccessGate.AccessBlock(
				PublicAccessDeniedPage.ErrorCode.CANDIDATE_QUESTIONS_CLOSED,
				PublicAccessDeniedPage.CountdownTargetDate.END,
				ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS,
				null,
				null);
		return TokenAccessGate.of(calendar.getStartDate(), calendar.getEndDate(), before, after);
	}

	@Override
	protected String resolveTopHeaderSectionLabel() {
		return getString("publicTokenSectionQuestions");
	}

	private String resolveLanguageLabel(LanguageCode languageCode) {
		if (languageCode == null) {
			return "-";
		}
		switch (languageCode) {
		case EN:
			return getString("candidateQuestionsLanguageEnglish");
		case PT:
			return getString("candidateQuestionsLanguagePortuguese");
		case SP:
		default:
			return getString("candidateQuestionsLanguageSpanish");
		}
	}

	private String resolveElectionTitle() {
		if (getElection() == null) {
			return "-";
		}
		String language = SecurityUtils.getLocale() != null ? SecurityUtils.getLocale().getLanguage() : null;
		String electionTitle = language != null ? getElection().getTitle(language) : null;
		if (electionTitle == null || electionTitle.trim().isEmpty()) {
			electionTitle = getElection().getTitleSpanish();
		}
		return electionTitle != null && !electionTitle.trim().isEmpty() ? electionTitle : "-";
	}

	private final IChoiceRenderer<Candidate> CANDIDATE_RENDERER = new IChoiceRenderer<Candidate>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(Candidate object) {
			if (object == null) {
				return "-";
			}
			return object.getName() + " (#" + object.getCandidateId() + ")";
		}

		@Override
		public String getIdValue(Candidate object, int index) {
			return object != null ? String.valueOf(object.getCandidateId()) : String.valueOf(index);
		}
	};

	private final IChoiceRenderer<LanguageCode> LANGUAGE_RENDERER = new IChoiceRenderer<LanguageCode>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(LanguageCode object) {
			return resolveLanguageLabel(object);
		}

		@Override
		public String getIdValue(LanguageCode object, int index) {
			return object != null ? object.name() : String.valueOf(index);
		}
	};
}
