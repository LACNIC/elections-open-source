package net.lacnic.elections.adminweb.ui.vote;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.domain.UserVoter;

public class AlreadyVotedDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = -7536199173314577391L;
	
	private UserVoter userVoter;

	public AlreadyVotedDashboard(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedbackPanel"));
		getSession().info(getString("alreadyVotedMessage"));

		add(new Label("title", userVoter.getElection().getTitle(getLanguage())));
		add(new Label("voter", userVoter.getVoterInformation()));
		add(new Label("maxCandidates", String.valueOf(userVoter.getElection().getMaxCandidates())));
		add(new Label("voteAmount", userVoter.getVoteAmount()));

		Label desc = new Label("description", userVoter.getElection().getDescription(getLanguage()));
		desc.setEscapeModelStrings(false);
		add(desc);

		List<Object[]> chosenCandidates = AppContext.getInstance().getVoterBeanRemote().getElectionVotesCandidateForUserVoter(userVoter.getUserVoterId(), userVoter.getElection().getElectionId());
		final ListView<Object[]> chosenCandidatesListView = new ListView<Object[]>("codes", chosenCandidates) {
			private static final long serialVersionUID = 1786359392545666490L;

			@Override
			protected void populateItem(final ListItem<Object[]> item) {
				final Object[] current = item.getModelObject();				
				int rowNumber = item.getIndex() + 1;				
				item.add(new Label("number", String.valueOf(rowNumber)));
				Label code = new Label("code", (String) current[1]);
				code.setMarkupId("code" + item.getIndex());
				item.add(code);
			}
		};
		add(chosenCandidatesListView);
	}

	@Override
	public Class validateToken(PageParameters params) {
		userVoter = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccess(getToken());
		if (userVoter == null) {
			AppContext.getInstance().getVoterBeanRemote().saveFailedAccessIp(getIP());
			return Error404.class;
		} else {
			setElection(userVoter.getElection());
		}
		return null;
	}

}
