package net.lacnic.elections.adminweb.ui.commons;

import org.apache.wicket.markup.html.panel.Panel;

public class AppTopNavPrivatePanel extends Panel {

	private static final long serialVersionUID = -1201978732865307842L;

	public AppTopNavPrivatePanel(String id) {
		this(id, 0L);
	}

	public AppTopNavPrivatePanel(String id, long electionId) {
		super(id);

		add(new AdminNavBarPanel("navBar", electionId));
	}
}
