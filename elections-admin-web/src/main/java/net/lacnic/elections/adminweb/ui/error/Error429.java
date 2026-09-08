package net.lacnic.elections.adminweb.ui.error;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.SecurityUtils;

public class Error429 extends WebPage {

	private static final long serialVersionUID = 1392182581021963077L;

	public Error429(PageParameters params) {
		add(new BookmarkablePageLink<Void>("home", SecurityUtils.getHomePage()));
	}
}
