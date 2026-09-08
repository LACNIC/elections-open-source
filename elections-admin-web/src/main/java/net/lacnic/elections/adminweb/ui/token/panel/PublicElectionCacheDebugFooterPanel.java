package net.lacnic.elections.adminweb.ui.token.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class PublicElectionCacheDebugFooterPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public PublicElectionCacheDebugFooterPanel(String id, String title, Object payload, boolean visible) {
		super(id);
		setOutputMarkupPlaceholderTag(true);
		setVisible(visible);
		add(new Label("debugTitle", title));
		add(new Label("debugPayload", PublicElectionCacheDebugFormatter.format(payload)));
	}
}
