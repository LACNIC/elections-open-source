package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.commons.GestionEleccionStatusPanel;
import net.lacnic.elections.admin.web.elecciones.AgregarAuditoresPanel;
import net.lacnic.elections.admin.web.elecciones.ListaAuditoresPanel;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardGestionAuditores extends DashboardAdminBasePage {
	private static final long serialVersionUID = 1L;
	private Election eleccion;

	public DashboardGestionAuditores(PageParameters params) {
		super(params);
		setEleccion(AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params)));
		add(new FeedbackPanel("feedback"));

		add(new GestionEleccionStatusPanel("tabAuditores", eleccion));
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