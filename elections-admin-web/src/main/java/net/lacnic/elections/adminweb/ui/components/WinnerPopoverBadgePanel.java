package net.lacnic.elections.adminweb.ui.components;

import org.apache.wicket.markup.html.panel.Panel;

public class WinnerPopoverBadgePanel extends Panel {

	private static final long serialVersionUID = 1L;

	public WinnerPopoverBadgePanel(String id, boolean visible) {
		super(id);
		setOutputMarkupPlaceholderTag(true);
		setVisible(visible);
	}
}
