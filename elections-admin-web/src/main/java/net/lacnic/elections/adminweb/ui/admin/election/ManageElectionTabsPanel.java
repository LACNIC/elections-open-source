package net.lacnic.elections.adminweb.ui.admin.election;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.ui.admin.election.call.ElectionCallDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.calendarmodule.ElectionCalendarModuleDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.organizations.ElectionOrganizationsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.tasks.ElectionTasksDashboard;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class ManageElectionTabsPanel extends Panel {

	private static final long serialVersionUID = 7849505995342956937L;
	private static final String ICON_DONE_CLASS = " text-success";
	private static final String ICON_PENDING_CLASS = " text-dark";
	private static final String ICON_ACTIVE_UNDERLINE_CLASS = " text-decoration-underline";

	private final String activeTabId;

	public ManageElectionTabsPanel(String id, Election election) {
		this(id, election, null);
	}

	public ManageElectionTabsPanel(String id, Election election, String activeTabId) {
		super(id);
		this.activeTabId = activeTabId;

		Link<Void> detail = new Link<Void>("detail") {
			private static final long serialVersionUID = 1791144645448735702L;

			@Override
			public void onClick() {
				setResponsePage(ElectionDetailDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		detail.setEnabled(election.getElectionId() != 0);

		WebMarkupContainer detailIcon = new WebMarkupContainer("detailIcon");
		detail.add(detailIcon);
		add(detail);

		Link<Void> call = new Link<Void>("call") {
			private static final long serialVersionUID = 2745772471881763419L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCallDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		call.setEnabled(election.getElectionId() != 0);

		WebMarkupContainer callIcon = new WebMarkupContainer("callIcon");
		call.add(callIcon);
		add(call);

		Link<Void> organizations = new Link<Void>("organizations") {
			private static final long serialVersionUID = -7460590980710825717L;

			@Override
			public void onClick() {
				setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		organizations.setEnabled(election.getElectionId() != 0);

		WebMarkupContainer organizationsIcon = new WebMarkupContainer("organizationsIcon");
		organizations.add(organizationsIcon);
		add(organizations);

		Link<Void> census = new Link<Void>("census") {
			private static final long serialVersionUID = -846692535588349478L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		census.setEnabled(election.getElectionId() != 0);

		WebMarkupContainer censusIcon = new WebMarkupContainer("censusIcon");
		census.add(censusIcon);
		add(census);

		Link<Void> calendar = new Link<Void>("calendar") {
			private static final long serialVersionUID = 7095978201912653834L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCalendarModuleDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		calendar.setEnabled(election.getElectionId() != 0);

		WebMarkupContainer calendarIcon = new WebMarkupContainer("calendarIcon");
		calendar.add(calendarIcon);
		add(calendar);

		Link<Void> tasks = new Link<Void>("tasks") {
			private static final long serialVersionUID = 6032210241512178160L;

			@Override
			public void onClick() {
				setResponsePage(ElectionTasksDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		tasks.setEnabled(election.getElectionId() != 0);

		WebMarkupContainer tasksIcon = new WebMarkupContainer("tasksIcon");
		tasks.add(tasksIcon);
		add(tasks);

		Link<Void> candidates = new Link<Void>("candidates") {
			private static final long serialVersionUID = 8317962582176759530L;

			@Override
			public void onClick() {
				setResponsePage(ElectionCandidatesDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		candidates.setEnabled(election.getElectionId() != 0);

		WebMarkupContainer candidatesIcon = new WebMarkupContainer("candidatesIcon");
		candidates.add(candidatesIcon);
		add(candidates);

		Link<Void> auditors = new Link<Void>("auditors") {
			private static final long serialVersionUID = 5621792633991539504L;

			@Override
			public void onClick() {
				setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		};
		auditors.setEnabled(election.getElectionId() != 0);

		WebMarkupContainer auditorsIcon = new WebMarkupContainer("auditorsIcon");
		auditors.add(auditorsIcon);
		add(auditors);

		applyIconState(detailIcon, isActive("tabDetail"), true);
		applyIconState(callIcon, isActive("tabCall"), election.isCallSet());
		applyIconState(organizationsIcon, isActive("tabOrganizations"), election.isOrganizationsSet());
		applyIconState(censusIcon, isActive("tabCensus"), election.isElectorsSet());
		applyIconState(calendarIcon, isActive("tabCalendar"), election.isCalendarSet());
		applyIconState(tasksIcon, isActive("tabTasks"), election.isTasksSet());
		applyIconState(candidatesIcon, isActive("tabCandidates"), election.isCandidatesSet());
		applyIconState(auditorsIcon, isActive("tabAuditors"), election.isAuditorsSet());

	}

	private void applyIconState(WebMarkupContainer icon, boolean active, boolean completed) {
		icon.add(new AttributeAppender(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  completed ? ICON_DONE_CLASS : ICON_PENDING_CLASS));
		if (active) {
			icon.add(new AttributeAppender(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  ICON_ACTIVE_UNDERLINE_CLASS));
		}
	}

	private boolean isActive(String tabId) {
		return activeTabId != null && activeTabId.equalsIgnoreCase(tabId);
	}

}
