package net.lacnic.elections.adminweb.ui.admin.election.questions;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import net.lacnic.elections.domain.pre.CandidateQuestionOwner;
import net.lacnic.elections.domain.pre.CandidateQuestionStatus;
import net.lacnic.elections.domain.Election;

public class QuestionsListPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public QuestionsListPanel(String id, Election election) {
		super(id);

		List<CandidateQuestion> questions = new ArrayList<>(AppContext.getInstance().getManagerBeanRemote().getElectionCandidateQuestions(election.getElectionId()));

		add(new ListView<CandidateQuestion>("questionsList", questions) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<CandidateQuestion> item) {
				CandidateQuestion question = item.getModelObject();

				item.add(new Label("questionId", String.valueOf(question.getCandidateQuestionId())));
				item.add(new Label("candidateName", resolveCandidateName(question.getCandidate())));
				item.add(new Label("status", resolveStatusLabel(question.getStatus())));
				item.add(new Label("owner", resolveOwnerLabel(question.getCurrentOwner())));
				item.add(new Label("questionPreview", resolvePreview(question)));

				item.add(new Link<Void>("editQuestion") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick() {
						PageParameters parameters = UtilsParameters.getId(election.getElectionId());
						parameters.add(UtilsParameters.getQuestionText(), question.getCandidateQuestionId());
						setResponsePage(ElectionQuestionsDashboard.class, parameters);
					}
				});
			}
		});

		add(new Label("emptyQuestionsMessage", getString("candidateQuestionsListEmpty")).setVisible(questions.isEmpty()));
	}

	private String resolveCandidateName(Candidate candidate) {
		if (candidate == null) {
			return "-";
		}
		return candidate.getName() + " (#" + candidate.getCandidateId() + ")";
	}

	private String resolvePreview(CandidateQuestion question) {
		if (question == null) {
			return "-";
		}
		String preview = question.getQuestionPreview();
		if (preview == null || preview.trim().isEmpty()) {
			return "-";
		}
		preview = preview.trim();
		return preview.length() <= 160 ? preview : preview.substring(0, 160) + "...";
	}

	private String resolveStatusLabel(CandidateQuestionStatus status) {
		if (status == null) {
			return "-";
		}
		return getString("candidateQuestionStatus." + status.name());
	}

	private String resolveOwnerLabel(CandidateQuestionOwner owner) {
		if (owner == null) {
			return "-";
		}
		return getString("candidateQuestionOwner." + owner.name());
	}
}
