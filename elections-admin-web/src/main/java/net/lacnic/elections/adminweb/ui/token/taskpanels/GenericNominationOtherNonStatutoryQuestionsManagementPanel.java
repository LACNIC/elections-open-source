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
public class GenericNominationOtherNonStatutoryQuestionsManagementPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;
	private static final String CANDIDATE_LINK_ACTIVITY_ACTOR = "CANDIDATE_LINK";
	private static final int MAX_ANSWER_LENGTH = 1000;

	private final String token;
	private Candidate candidate;

	private String question1Answer;
	private String question1Label;

	public GenericNominationOtherNonStatutoryQuestionsManagementPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		this.token = resolution.getNomination() != null ? resolution.getNomination().getAcceptNominationToken() : "";
		this.candidate = resolution.getCandidate();
		add(buildTaskTitle("cardTitle"));

		loadFormState();
		loadQuestion();

		Label restrictionMessage = new Label("restrictionMessage", getTaskResolution().hasModeRestriction() ? getString(getTaskResolution().getRestrictionMessageKey()) : "");
		restrictionMessage.setVisible(getTaskResolution().hasModeRestriction());
		restrictionMessage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "text-warning mb-3"));
		add(restrictionMessage);

		Form<Void> questionsForm = new Form<>("questionsForm");
		add(questionsForm);

		questionsForm.add(new Label("question1Label", StringUtils.defaultString(question1Label)));

		AiAssistTextAreaPanel question1Editor = new AiAssistTextAreaPanel(
				"question1Editor",
				new PropertyModel<>(this, "question1Answer"),
				new PropertyModel<>(this, "question1Label"),
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
		questionsForm.add(question1Editor);

		questionsForm.add(new BookmarkablePageLink<Void>("backButton", GenericAcceptNominationTasksPage.class, UtilsParameters.getToken(token)));

		Button continueLaterButton = new Button("continueLaterButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if (!ensureCanContinueLater()) {
					return;
				}
				if (!persistAnswers()) {
					SecurityUtils.error(getString("acceptNominationOtherNonStatutoryQuestionsSaveError"));
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
				if (!validateRequiredAnswer()) {
					return;
				}
				if (!persistAnswers()) {
					SecurityUtils.error(getString("acceptNominationOtherNonStatutoryQuestionsSaveError"));
					return;
				}
				requestTaskStatusChange(CandidateElectionTaskStatus.COMPLETED);
			}
		};
		finishAndSendButton.setVisible(isCompleteMode() && !isReadOnlyMode());
		questionsForm.add(finishAndSendButton);
	}

	private void loadFormState() {
		if (candidate == null) {
			question1Answer = null;
			return;
		}
		question1Answer = candidate.getQOtherNonStatutoryAnswer1();
	}

	private void loadQuestion() {
		List<String> configuredQuestions = AppContext.getInstance().getPreNominationBeanRemote().getOtherNonStatutoryQuestions(SecurityUtils.getLocale().getLanguage());
		if (configuredQuestions == null) {
			configuredQuestions = new ArrayList<>();
		}
		question1Label = configuredQuestions.isEmpty() ? getString("acceptNominationOtherNonStatutoryQuestionsQuestion1") : configuredQuestions.get(0);
	}

	private boolean validateRequiredAnswer() {
		if (StringUtils.isBlank(StringUtils.trimToNull(question1Answer))) {
			error(new StringResourceModel("acceptNominationOtherNonStatutoryQuestionsRequiredError", this, null)
					.setParameters(StringUtils.defaultString(question1Label)).getString());
			return false;
		}
		return true;
	}

	private boolean persistAnswers() {
		try {
			Candidate candidateData = new Candidate();
			candidateData.setQOtherNonStatutoryAnswer1(question1Answer);

			Candidate updatedCandidate = AppContext.getInstance().getPreNominationBeanRemote().saveCandidateOtherNonStatutoryQuestions(token, candidateData,
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

	private void addExternalFeedback(AjaxRequestTarget target) {
		addPageFeedback(target);
	}

}
