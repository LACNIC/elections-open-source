package net.lacnic.elections.adminweb.ui.admin.election.questions;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

public class ElectionQuestionsDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1L;

	private Election election;

	public ElectionQuestionsDashboard(PageParameters params) {
		super(params);

		Election loadedElection = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));
		if (loadedElection.isClosed()) {
			setResponsePage(ErrorElectionClosed.class);
			return;
		}

		election = loadedElection;
		long questionIdToEdit = UtilsParameters.getQuestionAsLong(params);
		boolean showQuestionForm = questionIdToEdit > 0 || UtilsParameters.isNewQuestion(params);

		FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		feedbackPanel.setOutputMarkupPlaceholderTag(true);
		add(feedbackPanel);

		WebMarkupContainer questionEditContainer = new WebMarkupContainer("questionEditContainer");
		questionEditContainer.setVisible(showQuestionForm);
		questionEditContainer.add(new Label("electionTitle", election.getTitle(getLanguage())));
		questionEditContainer.add(new QuestionEditPanel(
				"questionEditPanel",
				election,
				questionIdToEdit > 0 ? questionIdToEdit : null,
				target -> target.add(feedbackPanel)));
		add(questionEditContainer);
		add(new QuestionsListPanel("questionsListPanel", election));
		add(new Link<Void>("newQuestion") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(ElectionQuestionsDashboard.class, UtilsParameters.getNewQuestion(election.getElectionId()));
			}
		}.setVisible(!showQuestionForm));
		add(new Link<Void>("back") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(SecurityUtils.getHomePage());
			}
		});
	}

	public Election getElection() {
		return election;
	}
}
