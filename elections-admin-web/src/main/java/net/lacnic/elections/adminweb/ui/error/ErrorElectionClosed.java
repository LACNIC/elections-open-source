package net.lacnic.elections.adminweb.ui.error;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;


public class ErrorElectionClosed extends DashboardPublicBasePage {

	private static final long serialVersionUID = -7657623655369083975L;


	public ErrorElectionClosed(PageParameters params) {
		super(params);
		add(new BookmarkablePageLink<Void>("home", SecurityUtils.getHomePage()));
	}

	@Override
	protected Class validateToken(PageParameters params) {
		return null;
	}

}
