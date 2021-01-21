package net.lacnic.siselecciones.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.siselecciones.admin.web.commons.GestionEleccionStatusPanel;
import net.lacnic.siselecciones.admin.web.elecciones.AgregarAuditoresPanel;
import net.lacnic.siselecciones.admin.web.elecciones.ListaAuditoresPanel;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Eleccion;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardGestionAuditores extends DashboardAdminBasePage {
	private static final long serialVersionUID = 1L;
	private Eleccion eleccion;

	public DashboardGestionAuditores(PageParameters params) {
		super(params);
		setEleccion(Contexto.getInstance().getManagerBeanRemote().obtenerEleccion(UtilsParameters.getIdAsLong(params)));
		add(new FeedbackPanel("feedback"));

		add(new GestionEleccionStatusPanel("tabAuditores", eleccion));
		add(new ListaAuditoresPanel("listaAuditoresPanel", eleccion.getIdEleccion()));
		add(new AgregarAuditoresPanel("agregarAuditoresPanel", eleccion));

	}

	public Eleccion getEleccion() {
		return eleccion;
	}

	public void setEleccion(Eleccion eleccion) {
		this.eleccion = eleccion;
	}
}