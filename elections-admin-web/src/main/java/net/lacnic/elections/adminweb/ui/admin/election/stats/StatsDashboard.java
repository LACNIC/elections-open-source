package net.lacnic.elections.adminweb.ui.admin.election.stats;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.googlecode.wickedcharts.wicket7.JavaScriptResourceRegistry;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.ui.results.CandidateCodesPanel;
import net.lacnic.elections.adminweb.ui.results.ElectionResultsPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;


@AuthorizeInstantiation("elections-only-one")
public class StatsDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = 2304496268074384354L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	@Override
	protected void onInitialize() {
		super.onInitialize();
		String highcharts = "https://code.highcharts.com/highcharts.js";
		String exporting = "https://code.highcharts.com/modules/exporting.js";
		JavaScriptResourceRegistry.getInstance().setHighchartsReference(highcharts);
		JavaScriptResourceRegistry.getInstance().setHighchartsExportingReference(exporting);
	}


	public StatsDashboard(PageParameters params) {
		super(params);

		try {
			Election election = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));
			add(new Label("electionTitle", election.getTitle(getLanguage())));
			add(new ElectionResultsPanel("resultsPanel", election.getElectionId()).setVisible(election.isFinished()));
			add(new CandidateCodesPanel("candidateCodesPanel", election.getElectionId()).setVisible(election.isFinished()));
			add(new VotersGraphPanel("graphPanel", election.getElectionId()));
			add(new Label("message", getString("dshbStatsMessage")).setVisible(!election.isFinished()));
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}
