package net.lacnic.elections.adminweb.ui.results;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;
import net.lacnic.elections.adminweb.ui.error.Error404;
import net.lacnic.elections.adminweb.ui.error.ErrorResultsNotPublic;
import net.lacnic.elections.domain.Election;


public class ResultsDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = 2304496268074384354L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public ResultsDashboard(PageParameters params) {
		super(params);
		try {
			add(new Label("title", getElection().getTitle(getLanguage())));
			Label description = new Label("description", getElection().getDescription(getLanguage()));
			description.setEscapeModelStrings(false);
			add(description);
			add(new ElectionResultsPanel("resultsPanel", getElection().getElectionId()));
			add(new CandidateCodesPanel("candidateCodesPanel", getElection().getElectionId()));
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public Class validateToken(PageParameters params) {
		Election election = AppContext.getInstance().getVoterBeanRemote().verifyResultAccess(getToken());
		if (election == null) {
			AppContext.getInstance().getVoterBeanRemote().saveFailedAccessIp(getIP());
			return Error404.class;
		} else {
			setElection(election);
			if (!election.isResultLinkAvailable()) {
				return ErrorResultsNotPublic.class;
			}
		}
		return null;
	}

}
