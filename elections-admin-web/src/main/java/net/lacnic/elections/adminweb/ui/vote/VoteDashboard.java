package net.lacnic.elections.adminweb.ui.vote;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.error.ErrorVoteNotPublic;
import net.lacnic.elections.adminweb.ui.error.ErrorVoteNotStarted;
import net.lacnic.elections.domain.UserVoter;


public class VoteDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = -867241975964848115L;


	public Class validateToken(PageParameters params) {
		UserVoter userVoter = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccess(getToken());

		if (userVoter == null) {
			AppContext.getInstance().getVoterBeanRemote().saveFailedAccessIp(getIP());
			return Error404.class;
		} else {
			setElection(userVoter.getElection());

			if (!getElection().isStarted()) {
				setResponsePage(ErrorVoteNotStarted.class, getPageParameters());
			} else if (!getElection().isEnabledToVote()) {
				return ErrorVoteNotPublic.class;
			}

			if (AppContext.getInstance().getVoterBeanRemote().electionIsSimple(getElection().getElectionId()))
				setResponsePage(VoteSimpleElectionDashboard.class, params);
			else
				setResponsePage(VoteJointElectionDashboard.class, params);
		}
		return null;
	}

	public VoteDashboard(PageParameters params) {
		super(params);
	}

}