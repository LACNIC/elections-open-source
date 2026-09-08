package net.lacnic.elections.adminweb.ui.admin.election.charts;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.results.CandidateCodesPanel;
import net.lacnic.elections.adminweb.ui.results.ElectionResultsPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;

public class StatsDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 2304496268074384354L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	public StatsDashboard(PageParameters params) {
		super(params);

		try {
			Election election = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));

			add(new Label("electionTitle", election.getTitle(getLanguage())));

			add(new ElectionResultsPanel("resultsPanel", election.getElectionId()).setVisible(election.isFinished()));

			add(new CandidateCodesPanel("candidateCodesPanel", election.getElectionId()).setVisible(election.isFinished()));

			add(new VotersGraphPanel("graphPanel", election.getElectionId()));

			List<UserVoter> voters = AppContext.getInstance().getManagerBeanRemote().getElectionUserVoters(election.getElectionId());
			CountryParticipationStats countryParticipationStats = CountryParticipationStats.build(voters, getLocale());
			add(new CountryVotersGraphPanel("countryGraphPanel", countryParticipationStats));
			add(new CountryParticipationReportPanel("countryParticipationReportPanel", countryParticipationStats));

			add(new Label("message", getString("dshbStatsMessage")).setVisible(!election.isFinished()));

		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

}
