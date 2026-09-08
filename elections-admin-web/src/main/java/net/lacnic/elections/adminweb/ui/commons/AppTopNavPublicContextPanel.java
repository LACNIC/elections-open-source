package net.lacnic.elections.adminweb.ui.commons;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.ui.home.PublicHomeDashboard;

public class AppTopNavPublicContextPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public AppTopNavPublicContextPanel(String id, String sectionLabel) {
		super(id);
		String resolvedSectionLabel = valueOrDash(sectionLabel);
		add(new Label("sectionTitle", resolvedSectionLabel));
		add(new Label("currentSection", resolvedSectionLabel));
		add(new BookmarkablePageLink<>("homeLink", PublicHomeDashboard.class));
	}

	private String valueOrDash(String value) {
		if (value == null || value.trim().isEmpty()) {
			return "-";
		}
		return value;
	}
}
