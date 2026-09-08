package net.lacnic.elections.adminweb.ui.commons;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.ui.home.PublicElectionsDashboard;
import net.lacnic.elections.adminweb.ui.home.PublicHomeDashboard;

public class AppTopNavPublicPanel extends Panel {

	private static final long serialVersionUID = 5437013051723112511L;

	public AppTopNavPublicPanel(String id) {
		super(id);

		add(new BookmarkablePageLink<>("homeLink", PublicHomeDashboard.class));
		add(new BookmarkablePageLink<>("publicElectionsLink", PublicElectionsDashboard.class));
		add(new ExternalLink("documentationLink", ProjectUrls.documentation()));
		add(new ExternalLink("servicesDocumentationLink", ProjectUrls.servicesDocumentation()));
		add(new ExternalLink("sourceCodeLink", ProjectUrls.SOURCE_CODE));
		add(new ExternalLink("releaseNotesLink", ProjectUrls.releaseNotes()));
		add(new ExternalLink("licenseLink", ProjectUrls.license()));
		add(new ExternalLink("contactLink", ProjectUrls.CONTACT));
	}
}
