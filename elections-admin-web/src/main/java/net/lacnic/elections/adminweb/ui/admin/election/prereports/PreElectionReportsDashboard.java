package net.lacnic.elections.adminweb.ui.admin.election.prereports;

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

public class PreElectionReportsDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 1L;

	private Election election;

	public PreElectionReportsDashboard(PageParameters params) {
		super(params);

		Election loadedElection = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));
		if (loadedElection.isClosed()) {
			setResponsePage(ErrorElectionClosed.class);
			return;
		}

		election = loadedElection;
		add(new FeedbackPanel("feedback"));
		add(new Label("electionTitle", election.getTitle(getLanguage())));
		add(new PreElectionReportsPanel("preElectionReportsPanel", election));
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
