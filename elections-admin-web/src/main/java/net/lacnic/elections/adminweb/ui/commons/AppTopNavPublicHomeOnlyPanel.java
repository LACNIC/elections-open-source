package net.lacnic.elections.adminweb.ui.commons;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.ui.home.PublicHomeDashboard;

public class AppTopNavPublicHomeOnlyPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public AppTopNavPublicHomeOnlyPanel(String id) {
		super(id);
		add(new BookmarkablePageLink<>("homeLink", PublicHomeDashboard.class));
	}
}
