package net.lacnic.siselecciones.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.siselecciones.admin.web.panel.admin.AddTemplateBasePanel;
import net.lacnic.siselecciones.admin.web.panel.admin.ListaPlantillasPanel;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardPlantillasVer extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;

	public DashboardPlantillasVer(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));

		long idEleccion = UtilsParameters.getIdAsLong(params);
		AddTemplateBasePanel agregarTemplateBase = new AddTemplateBasePanel("addTemplateBasePanel");
		agregarTemplateBase.setVisible(idEleccion == 0);
		add(agregarTemplateBase);

		add(new ListaPlantillasPanel("listPlantillas", idEleccion));
	}

}
