package net.lacnic.siselecciones.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.siselecciones.admin.web.panel.elecciones.DetalleEleccionPanel;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardDetalleEleccion extends DashboardAdminBasePage {

	private static final long serialVersionUID = -3122456234867022677L;

	public DashboardDetalleEleccion(PageParameters params) {
		super(params);
		add(new DetalleEleccionPanel("detalleEleccion", UtilsParameters.getIdAsLong(params), UtilsParameters.isAll(params)));
	}
}
