package net.lacnic.elections.admin.dashboard.error;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.web.bases.DashboardPublicBasePage;

public class Error404 extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1392182581021963077L;

	public Error404(PageParameters params) {
		super(params);
		add(new BookmarkablePageLink<Void>("inicio", SecurityUtils.getHomePage()));
	}

	@Override
	protected Class validarToken(PageParameters params) {
		// TODO Auto-generated method stub
		return null;
	}
}
