package net.lacnic.siselecciones.admin.dashboard.error;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.web.bases.DashboardPublicBasePage;

public class Error500 extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1392182581021963077L;

	public Error500(PageParameters params) {
		super(params);

		add(new BookmarkablePageLink<Void>("inicio", SecurityUtils.getHomePage()));
	}

	@Override
	protected Class validarToken(PageParameters params) {
		// TODO Auto-generated method stub
		return null;
	}
}
