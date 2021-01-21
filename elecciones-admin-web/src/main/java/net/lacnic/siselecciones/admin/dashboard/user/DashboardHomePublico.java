package net.lacnic.siselecciones.admin.dashboard.user;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardHomePage;
import net.lacnic.siselecciones.admin.web.bases.DashboardPublicBasePage;

public class DashboardHomePublico extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1392182581021963077L;

	public DashboardHomePublico(PageParameters params) {
		super(params);

		if (SecurityUtils.isSignedIn())
			setResponsePage(DashboardHomePage.class);

	}

	@Override
	protected Class validarToken(PageParameters params) {
		return null;
	}

}
