package net.lacnic.elections.adminweb.ui.admin.election.tasks;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.admin.election.calendarmodule.ElectionCalendarModuleDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.ui.bases.DashboardElectionBasePage;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.adminweb.app.SecurityUtils;

public class ElectionTasksDashboard extends DashboardElectionBasePage {

	private static final long serialVersionUID = 5060171534532657087L;

	private Election election;

	public ElectionTasksDashboard(PageParameters params) {
		super(params);

		Election election = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));
		if (election.isClosed()) {
			setResponsePage(ErrorElectionClosed.class);
			return;
		}

		setElection(election);
		add(new FeedbackPanel("feedback"));
		add(new ManageElectionTabsPanel("tabsPanel", election, "tabTasks"));
		add(new ElectionTasksPanel("tasksPanel", election));
		add(new Link<Void>("back") {
			private static final long serialVersionUID = -5722823656461311596L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCalendarModuleDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		});
			add(new Link<Void>("markDoneNext") {
				private static final long serialVersionUID = 5164791305722009602L;

			@Override
			public void onClick() {
				if (AppContext.getInstance().getManagerBeanRemote().getElectionTasks(election.getElectionId()).isEmpty()) {
					error(getString("electionTasksManagementCannotCompleteWithoutTasks"));
					return;
				}
				AppContext.getInstance().getManagerBeanRemote().persistElectionTasksSet(
					election.getElectionId(),
					election.getTitleSpanish(),
					SecurityUtils.getUserAdminId(),
					SecurityUtils.getClientIp()
				);
					setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
				}
			});
			add(new Link<Void>("skip") {
				private static final long serialVersionUID = -866982579816930908L;

				@Override
				public void onClick() {
					setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
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
