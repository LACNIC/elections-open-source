package net.lacnic.elections.adminweb.ui.admin.election.calendar;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.tasks.ElectionTasksDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.adminweb.app.SecurityUtils;

public class ElectionCalendarDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 5934498480260075880L;

	private Election election;

	public ElectionCalendarDashboard(PageParameters params) {
		super(params);

		Election election = reloadAndEnforceElectionAccess(UtilsParameters.getIdAsLong(params));
		if (election.isClosed()) {
			setResponsePage(ErrorElectionClosed.class);
			return;
		}

		setElection(election);
		add(new FeedbackPanel("feedback"));
		add(new ManageElectionTabsPanel("tabsPanel", election, "tabCalendar"));
		add(new Link<Void>("back") {
			private static final long serialVersionUID = -8365989223174079044L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		});
		add(new Link<Void>("markDoneNext") {
			private static final long serialVersionUID = -2412738257605876930L;

			@Override
			public void onClick() {
				Election currentElection = reloadAndEnforceElectionAccess(election.getElectionId());
				if (currentElection.isClosed()) {
					setResponsePage(ErrorElectionClosed.class);
					return;
				}
				AppContext.getInstance().getManagerBeanRemote().persistElectionCalendarSet(
					currentElection.getElectionId(),
					currentElection.getTitleSpanish(),
					SecurityUtils.getUserAdminId(),
					SecurityUtils.getClientIp()
				);
				setResponsePage(ElectionTasksDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
			}
		});
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}
}
