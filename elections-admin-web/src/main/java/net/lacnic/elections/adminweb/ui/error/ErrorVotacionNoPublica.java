package net.lacnic.elections.adminweb.ui.error;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.web.bases.DashboardPublicBasePage;

public class ErrorVotacionNoPublica extends DashboardPublicBasePage {

	private static final long serialVersionUID = 1392182581021963077L;

	public ErrorVotacionNoPublica(PageParameters params) {
		super(params);
		add(new BookmarkablePageLink<Void>("inicio", SecurityUtils.getHomePage()));
	}

	@Override
	protected Class validateToken(PageParameters params) {
		// TODO Auto-generated method stub
		return null;
	}

}
