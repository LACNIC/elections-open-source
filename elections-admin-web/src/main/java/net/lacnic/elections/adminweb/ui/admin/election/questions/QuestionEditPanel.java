package net.lacnic.elections.adminweb.ui.admin.election.questions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.emailtemplate.EmailTemplatesDashboard;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import net.lacnic.elections.domain.pre.CandidateQuestionStatus;
import net.lacnic.elections.utils.EmailTemplateType;

public class QuestionEditPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private CandidateQuestion candidateQuestion;
	private Long selectedCandidateId;
	private List<Long> candidateChoiceIds;
	private Map<Long, Candidate> candidateById;
	private final AiAssistTextAreaPanel.FeedbackUpdater feedbackUpdater;

	public QuestionEditPanel(String id, Election election, Long questionIdToEdit, AiAssistTextAreaPanel.FeedbackUpdater feedbackUpdater) {
		super(id);
		this.feedbackUpdater = feedbackUpdater;
		loadCandidates(election);
		candidateQuestion = resolveQuestionForForm(election, questionIdToEdit);

		Form<Void> questionForm = new Form<>("questionForm");
		add(questionForm);

		DropDownChoice<Long> candidateChoice = new DropDownChoice<>(
				"candidateId",
				new PropertyModel<>(this, "selectedCandidateId"),
				candidateChoiceIds,
				CANDIDATE_RENDERER);
		candidateChoice.setRequired(true);
		candidateChoice.setNullValid(false);
		questionForm.add(candidateChoice);

		TextField<String> askedByName = new TextField<>("askedByName", new PropertyModel<>(candidateQuestion, "askedByName"));
		askedByName.add(StringValidator.maximumLength(500));
		questionForm.add(askedByName);

		EmailTextField askedByEmail = new EmailTextField("askedByEmail", new PropertyModel<>(candidateQuestion, "askedByEmail"));
		askedByEmail.add(StringValidator.maximumLength(320));
		askedByEmail.add(EmailAddressValidator.getInstance());
		questionForm.add(askedByEmail);

		DropDownChoice<LanguageCode> questionLanguage = new DropDownChoice<>(
				"questionLanguage",
				new PropertyModel<>(candidateQuestion, "questionLanguage"),
				Arrays.asList(LanguageCode.values()),
				LANGUAGE_RENDERER);
		questionLanguage.setRequired(true);
		questionLanguage.setNullValid(false);
		questionForm.add(questionLanguage);

		DropDownChoice<CandidateQuestionStatus> status = new DropDownChoice<>(
				"status",
				new PropertyModel<>(candidateQuestion, "status"),
				Arrays.asList(CandidateQuestionStatus.values()),
				STATUS_RENDERER);
		status.setRequired(true);
		status.setNullValid(false);
		questionForm.add(status);
		ExternalLink questionStatusTemplateLink = new ExternalLink(
				"questionStatusTemplateLink",
				resolveCandidateQuestionStatusTemplateUrl(election.getElectionId()));
		questionStatusTemplateLink.add(AttributeModifier.replace("target", "_blank"));
		questionStatusTemplateLink.add(AttributeModifier.replace("rel", "noopener noreferrer"));
		questionForm.add(questionStatusTemplateLink);

		addQuestionTextEditor(questionForm, "questionSpanish", "candidateQuestionsQuestionSpanishLabel");
		addQuestionTextEditor(questionForm, "questionEnglish", "candidateQuestionsQuestionEnglishLabel");
		addQuestionTextEditor(questionForm, "questionPortuguese", "candidateQuestionsQuestionPortugueseLabel");
		addQuestionTextEditor(questionForm, "answerSpanish", "candidateQuestionsAnswerSpanishLabel");
		addQuestionTextEditor(questionForm, "answerEnglish", "candidateQuestionsAnswerEnglishLabel");
		addQuestionTextEditor(questionForm, "answerPortuguese", "candidateQuestionsAnswerPortugueseLabel");

		questionForm.add(new Button("saveQuestionWithEmail") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				persistQuestion(election, true);
			}
		});
		questionForm.add(new Button("saveQuestionWithoutEmail") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				persistQuestion(election, false);
			}
		});

		Link<Void> cancelQuestion = new Link<Void>("cancelQuestion") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ElectionQuestionsDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		questionForm.add(cancelQuestion);
	}

	private void addQuestionTextEditor(Form<Void> form, String propertyName, String labelKey) {
		form.add(new AiAssistTextAreaPanel(
				propertyName,
				new PropertyModel<>(candidateQuestion, propertyName),
				Model.of(getString(labelKey)),
				feedbackUpdater,
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
				5,
				true,
				textArea -> {
					textArea.setLabel(Model.of(getString(labelKey)));
					textArea.add(AttributeModifier.replace("data-testid", propertyName));
				}));
	}

	private void persistQuestion(Election election, boolean sendStatusNotificationEmail) {
		if (selectedCandidateId == null || selectedCandidateId <= 0) {
			error(getString("candidateQuestionsCandidateRequired"));
			return;
		}
		Candidate selectedCandidate = candidateById.get(selectedCandidateId);
		if (selectedCandidate == null) {
			error(getString("candidateQuestionsCandidateRequired"));
			return;
		}

		candidateQuestion.setElection(election);
		candidateQuestion.setCandidate(selectedCandidate);
		CandidateQuestion updated = AppContext.getInstance().getManagerBeanRemote().saveCandidateQuestion(
				candidateQuestion,
				sendStatusNotificationEmail,
				SecurityUtils.getUserAdminId(),
				SecurityUtils.getClientIp());
		if (updated == null) {
			error(getString("candidateQuestionsSaveError"));
			return;
		}
		getSession().info(getString("candidateQuestionsSaveSuccess"));
		setResponsePage(ElectionQuestionsDashboard.class, UtilsParameters.getId(election.getElectionId()));
	}

	private String resolveCandidateQuestionStatusTemplateUrl(long electionId) {
		CharSequence templatesUrl = urlFor(EmailTemplatesDashboard.class, UtilsParameters.getId(electionId));
		return templatesUrl.toString() + "#" + resolveTemplateAnchorId(EmailTemplateType.CANDIDATE_QUESTION_STATUS_NOTIFICATION.getKey());
	}

	private String resolveTemplateAnchorId(String templateType) {
		if (!hasText(templateType)) {
			return "template";
		}
		return "template-" + templateType.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private CandidateQuestion resolveQuestionForForm(Election election, Long questionIdToEdit) {
		CandidateQuestion existing = null;
		if (questionIdToEdit != null && questionIdToEdit > 0) {
			existing = AppContext.getInstance().getManagerBeanRemote().getCandidateQuestion(questionIdToEdit);
			if (existing != null && existing.getElection() != null && existing.getElection().getElectionId() == election.getElectionId()) {
				Long existingCandidateId = existing.getCandidate() != null ? existing.getCandidate().getCandidateId() : null;
				selectedCandidateId = existingCandidateId != null && candidateById.containsKey(existingCandidateId)
						? existingCandidateId
						: null;
				if (selectedCandidateId == null && !candidateChoiceIds.isEmpty()) {
					selectedCandidateId = candidateChoiceIds.get(0);
				}
				return existing;
			}
		}

		CandidateQuestion created = new CandidateQuestion();
		created.setElection(election);
		created.setStatus(CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC);
		if (!candidateChoiceIds.isEmpty()) {
			selectedCandidateId = candidateChoiceIds.get(0);
		}
		return created;
	}

	private void loadCandidates(Election election) {
		candidateById = new LinkedHashMap<>();
		candidateChoiceIds = new ArrayList<>();
		List<Candidate> candidates = AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(election.getElectionId());
		for (Candidate candidate : candidates) {
			if (candidate == null || candidate.isAbstention()) {
				continue;
			}
			long candidateId = candidate.getCandidateId();
			candidateById.put(candidateId, candidate);
			candidateChoiceIds.add(candidateId);
		}
	}

	private String resolveCandidateLabel(Long candidateId) {
		if (candidateId == null) {
			return "-";
		}
		Candidate candidate = candidateById.get(candidateId);
		if (candidate == null) {
			return String.valueOf(candidateId);
		}
		return candidate.getName() + " (#" + candidate.getCandidateId() + ")";
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

	private final IChoiceRenderer<Long> CANDIDATE_RENDERER = new IChoiceRenderer<Long>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(Long object) {
			return resolveCandidateLabel(object);
		}

		@Override
		public String getIdValue(Long object, int index) {
			return object != null ? String.valueOf(object) : String.valueOf(index);
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

	private final IChoiceRenderer<CandidateQuestionStatus> STATUS_RENDERER = new IChoiceRenderer<CandidateQuestionStatus>() {
		private static final long serialVersionUID = 1L;

		@Override
		public Object getDisplayValue(CandidateQuestionStatus object) {
			if (object == null) {
				return "-";
			}
			return getString("candidateQuestionStatus." + object.name());
		}

		@Override
		public String getIdValue(CandidateQuestionStatus object, int index) {
			return object != null ? object.name() : String.valueOf(index);
		}
	};
}
