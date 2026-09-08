package net.lacnic.elections.adminweb.ui.commons;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.login.LoginDashboard;
import net.lacnic.elections.domain.Customization; 


public class PublicTopBarPanel extends Panel {

	private static final long serialVersionUID = -6437181317906564167L;

	private Customization customization;
	private String siteTitle;


	public PublicTopBarPanel(String id) {
		super(id);

		customization = AppContext.getInstance().getManagerBeanRemote().getCustomization();
		siteTitle = customization.getSiteTitle();

		Label siteTitleLabel = new Label("siteTitle", new PropertyModel<>(PublicTopBarPanel.this, "siteTitle"));
		BookmarkablePageLink<Void> home = new BookmarkablePageLink<>("home", Application.get().getHomePage());
		home.add(siteTitleLabel);
		add(home);

		BookmarkablePageLink<Void> loginLink = new BookmarkablePageLink<>("loginLink", LoginDashboard.class);
		add(loginLink);
	}

}
