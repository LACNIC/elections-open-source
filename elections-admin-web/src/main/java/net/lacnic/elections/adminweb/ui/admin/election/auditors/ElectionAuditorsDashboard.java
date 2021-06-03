package net.lacnic.elections.adminweb.ui.admin.election.auditors;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.admin.election.ManageElectionTabsPanel;
import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.web.elecciones.AgregarAuditoresPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;


@AuthorizeInstantiation("elections-only-one")
public class ElectionAuditorsDashboard extends DashboardAdminBasePage {
	private static final long serialVersionUID = 1L;
	private Election eleccion;

	public ElectionAuditorsDashboard(PageParameters params) {
		super(params);
		setEleccion(AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params)));
		add(new FeedbackPanel("feedback"));

		add(new ManageElectionTabsPanel("tabsPanel", eleccion));
		add(new ListaAuditoresPanel("listaAuditoresPanel", eleccion.getElectionId()));
		add(new AgregarAuditoresPanel("agregarAuditoresPanel", eleccion));

	}

	public Election getEleccion() {
		return eleccion;
	}

	public void setEleccion(Election eleccion) {
		this.eleccion = eleccion;
	}
}