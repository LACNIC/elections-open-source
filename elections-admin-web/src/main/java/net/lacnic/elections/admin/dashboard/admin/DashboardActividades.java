package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.panel.admin.ListaActividadesPanel;

public class DashboardActividades extends DashboardAdminBasePage {

	private static final long serialVersionUID = -3060903190903301755L;

	public DashboardActividades(PageParameters params) {
		super(params);
		add(new ListaActividadesPanel("listActividades", -1));
	}

}
