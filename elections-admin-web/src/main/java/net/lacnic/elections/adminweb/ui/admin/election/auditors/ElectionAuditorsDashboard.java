package net.lacnic.elections.adminweb.ui.admin.election.auditors;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;


@AuthorizeInstantiation("elections-only-one")
public class ElectionAuditorsDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = 4512602604562487597L;

	private Election election;


	public ElectionAuditorsDashboard(PageParameters params) {
		super(params);
		setElection(AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params)));
		add(new FeedbackPanel("feedback"));

		add(new ManageElectionTabsPanel("tabsPanel", election));
		add(new AuditorsListPanel("auditorsListPanel", election.getElectionId()));
		add(new AddAuditorPanel("addAuditorPanel", election));

	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

}
