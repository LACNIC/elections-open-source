package net.lacnic.elections.adminweb.ui.token.taskpanels;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.AiAssistTextAreaPanel;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class GenericNominationOtherStatutoryQuestionsManagementPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";
	private static final String KEY_REQUIRED_ERROR = "acceptNominationOtherStatutoryQuestionsRequiredError";
	private static final int MAX_ANSWER_LENGTH = 1000;

	private final String token;
	private Candidate candidate;

	private String question1Answer;
	private String question2Answer;
	private String question3Answer;
	private String question4Answer;

	private final List<String> questions = new ArrayList<>();

	public GenericNominationOtherStatutoryQuestionsManagementPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";
		this.candidate = resolution.getCandidate();
		add(buildTaskTitle("cardTitle"));

		loadFormState();
		loadQuestions();

		Label restrictionMessage = new Label("restrictionMessage", getTaskResolution().hasModeRestriction() ? getString(getTaskResolution().getRestrictionMessageKey()) : "");
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);

		Form<Void> questionsForm = new Form<>("questionsForm");
		add(questionsForm);

		questionsForm.add(new Label("question1Label", resolveQuestionLabel(1)));
		questionsForm.add(new Label("question2Label", resolveQuestionLabel(2)));
		questionsForm.add(new Label("question3Label", resolveQuestionLabel(3)));
		questionsForm.add(new Label("question4Label", resolveQuestionLabel(4)));

		questionsForm.add(createQuestionEditor("question1Editor", 1));
		questionsForm.add(createQuestionEditor("question2Editor", 2));
		questionsForm.add(createQuestionEditor("question3Editor", 3));
		questionsForm.add(createQuestionEditor("question4Editor", 4));

		questionsForm.add(new BookmarkablePageLink<Void>("backButton", GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token)));

		Button continueLaterButton = new Button("continueLaterButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (!ensureCanContinueLater()) {
					return;
				}
				if (!persistAnswers()) {
					SecurityUtils.error(getString("acceptNominationOtherStatutoryQuestionsSaveError"));
					return;
				}
				requestTaskStatusChange(CandidateElectionTaskStatus.STARTED);
			}
		};
		continueLaterButton.setVisible(canContinueLater());
		questionsForm.add(continueLaterButton);

		Button finishAndSendButton = new Button("finishAndSendButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (isReadOnlyMode()) {
					return;
				}
				if (!validateRequiredAnswers()) {
					return;
				}
				if (!persistAnswers()) {
					SecurityUtils.error(getString("acceptNominationOtherStatutoryQuestionsSaveError"));
					return;
				}
				requestTaskStatusChange(CandidateElectionTaskStatus.COMPLETED);
			}
		};
		finishAndSendButton.setVisible(isCompleteMode() && !isReadOnlyMode());
		questionsForm.add(finishAndSendButton);
	}

	private AiAssistTextAreaPanel createQuestionEditor(String id, int questionNumber) {
		return new AiAssistTextAreaPanel(
				id,
				createQuestionAnswerModel(questionNumber),
				new LoadableDetachableModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					protected String load() {
						return resolveQuestionLabel(questionNumber);
					}
				},
				this::addExternalFeedback,
				(originalText, instruction, styleContext) -> AppContext.getInstance().getPreNominationBeanRemote().improveCandidateTextWithStatus(
						token,
						originalText,
						instruction,
						styleContext,
						SecurityUtils.getClientIp()),
				isReadOnlyMode(),
				true,
				true,
				true,
				true,
				MAX_ANSWER_LENGTH,
				6);
	}

	private PropertyModel<String> createQuestionAnswerModel(int questionNumber) {
		switch (questionNumber) {
		case 1:
			return new PropertyModel<>(this, "question1Answer");
		case 2:
			return new PropertyModel<>(this, "question2Answer");
		case 3:
			return new PropertyModel<>(this, "question3Answer");
		case 4:
		default:
			return new PropertyModel<>(this, "question4Answer");
		}
	}

	private void addExternalFeedback(AjaxRequestTarget target) {
		addPageFeedback(target);
	}

	private void loadFormState() {
		if (candidate == null) {
			question1Answer = null;
			question2Answer = null;
			question3Answer = null;
			question4Answer = null;
			return;
		}
		question1Answer = candidate.getQOtherStatutoryAnswer1();
		question2Answer = candidate.getQOtherStatutoryAnswer2();
		question3Answer = candidate.getQOtherStatutoryAnswer3();
		question4Answer = candidate.getQOtherStatutoryAnswer4();
	}

	private void loadQuestions() {
		questions.clear();
		List<String> configuredQuestions = AppContext.getInstance().getPreNominationBeanRemote().getOtherStatutoryQuestions(SecurityUtils.getLocale().getLanguage());
		if (configuredQuestions == null) {
			configuredQuestions = new ArrayList<>();
		}
		for (int i = 0; i < 4; i++) {
			String value = configuredQuestions.size() > i ? configuredQuestions.get(i) : null;
			questions.add(StringUtils.defaultIfBlank(value, getFallbackQuestionLabel(i + 1)));
		}
	}

	private String resolveQuestionLabel(int index) {
		int questionIndex = index - 1;
		if (questionIndex >= 0 && questionIndex < questions.size()) {
			return StringUtils.defaultString(questions.get(questionIndex));
		}
		return getFallbackQuestionLabel(index);
	}

	private String getFallbackQuestionLabel(int index) {
		return getString("acceptNominationOtherStatutoryQuestionsQuestion" + index);
	}

	private boolean validateRequiredAnswers() {
		if (StringUtils.isBlank(StringUtils.trimToNull(question1Answer))) {
			error(new StringResourceModel(KEY_REQUIRED_ERROR, this, null).setParameters(resolveQuestionLabel(1)).getString());
			return false;
		}
		if (StringUtils.isBlank(StringUtils.trimToNull(question2Answer))) {
			error(new StringResourceModel(KEY_REQUIRED_ERROR, this, null).setParameters(resolveQuestionLabel(2)).getString());
			return false;
		}
		if (StringUtils.isBlank(StringUtils.trimToNull(question3Answer))) {
			error(new StringResourceModel(KEY_REQUIRED_ERROR, this, null).setParameters(resolveQuestionLabel(3)).getString());
			return false;
		}
		if (StringUtils.isBlank(StringUtils.trimToNull(question4Answer))) {
			error(new StringResourceModel(KEY_REQUIRED_ERROR, this, null).setParameters(resolveQuestionLabel(4)).getString());
			return false;
		}
		return true;
	}

	private boolean persistAnswers() {
		try {
			Candidate candidateData = new Candidate();
			candidateData.setQOtherStatutoryAnswer1(question1Answer);
			candidateData.setQOtherStatutoryAnswer2(question2Answer);
			candidateData.setQOtherStatutoryAnswer3(question3Answer);
			candidateData.setQOtherStatutoryAnswer4(question4Answer);

			Candidate updatedCandidate = AppContext.getInstance().getPreNominationBeanRemote().saveCandidateOtherStatutoryQuestions(token, candidateData,
					CANDIDATE_LINK_ACTIVITY_ACTOR, SecurityUtils.getClientIp());
			if (updatedCandidate == null) {
				return false;
			}

			candidate = updatedCandidate;
			loadFormState();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public String getQuestion1Answer() {
		return question1Answer;
	}

	public void setQuestion1Answer(String question1Answer) {
		this.question1Answer = question1Answer;
	}

	public String getQuestion2Answer() {
		return question2Answer;
	}

	public void setQuestion2Answer(String question2Answer) {
		this.question2Answer = question2Answer;
	}

	public String getQuestion3Answer() {
		return question3Answer;
	}

	public void setQuestion3Answer(String question3Answer) {
		this.question3Answer = question3Answer;
	}

	public String getQuestion4Answer() {
		return question4Answer;
	}

	public void setQuestion4Answer(String question4Answer) {
		this.question4Answer = question4Answer;
	}
}
