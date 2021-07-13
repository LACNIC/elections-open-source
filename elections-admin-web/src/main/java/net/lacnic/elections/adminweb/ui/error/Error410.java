package net.lacnic.elections.adminweb.ui.error;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;


public class Error410 extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1392182581021963077L;

	public Error410(PageParameters params) {
		super(params);
		add(new BookmarkablePageLink<Void>("home", SecurityUtils.getHomePage()));
	}

	@Override
	protected Class validateToken(PageParameters params) {
		return null;
	}

}
