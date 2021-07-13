package net.lacnic.elections.adminweb.ui.vote;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.error.ErrorVoteNotPublic;
import net.lacnic.elections.adminweb.ui.error.ErrorVoteNotStarted;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;


public class VoteJointElectionDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = -867241975964848115L;

	private UserVoter[] userVoters;


	public VoteJointElectionDashboard(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedbackPanel"));

		WebMarkupContainer voteNotComplete = new WebMarkupContainer("voteNotComplete");
		voteNotComplete.setVisible(!userVoters[0].isVoted() || !userVoters[1].isVoted());
		add(voteNotComplete);

		add(new Label("voter", userVoters[0].getCompleteVoterInformation()));
		add(new VoteJointElectionDetailPanel("election1Detail", params, userVoters[0].getElection(), userVoters[0], true));
		add(new VoteJointElectionDetailPanel("election2Detail", params, userVoters[1].getElection(), userVoters[1], false));
	}

	@Override
	public Class validateToken(PageParameters params) {
		userVoters = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccessJointElection(getToken());
		if (userVoters == null) {
			AppContext.getInstance().getVoterBeanRemote().saveFailedAccessIp(getIP());
			return Error404.class;
		} else {
			setElections(new Election[] { userVoters[0].getElection(), userVoters[1].getElection() });
			if (!userVoters[0].getElection().isStarted()) {
				setResponsePage(ErrorVoteNotStarted.class, getPageParameters());
			} else if (!userVoters[0].getElection().isEnabledToVote()) {
				return ErrorVoteNotPublic.class;
			}
		}
		return null;
	}

}
