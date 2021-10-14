package net.lacnic.elections.adminweb.ui.commons;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.Application;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.login.LoginDashboard;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.domain.Customization; 


public class PublicTopBarPanel extends Panel {

	private static final long serialVersionUID = -6437181317906564167L;

	private Customization customization;
	private String siteTitle;
	private Link<Void> spanishLink;
	private Link<Void> portugueseLink;
	private Link<Void> englishLink;


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

		String ext = "";
		byte[] smallLogoFile;
		String smallLogoFileName;
		smallLogoFileName = customization.getPicSmallLogo();
		smallLogoFile = customization.getContPicSmallLogo();
		if (smallLogoFile == null) {
			home.add(new ContextImage("smallLogoPicture","image/" + smallLogoFileName));
		} else {
			ext = FilenameUtils.getExtension(smallLogoFileName);
			home.add(new NonCachingImage("smallLogoPicture", new ImageResource(smallLogoFile, ext)));
		}

		spanishLink = new Link<Void>("es") {
			private static final long serialVersionUID = -664820640708411168L;

			@Override
			public void onClick() {
				SecurityUtils.setLocale("ES");
				setResponsePage(getWebPage());
				portugueseLink.setVisible(true);
				spanishLink.setVisible(false);
				englishLink.setVisible(true);
			}
		};
		add(spanishLink);

		englishLink = new Link<Void>("en") {
			private static final long serialVersionUID = -6590594722943249118L;

			@Override
			public void onClick() {
				SecurityUtils.setLocale("EN");
				setResponsePage(getWebPage());
				portugueseLink.setVisible(true);
				spanishLink.setVisible(true);
				englishLink.setVisible(false);
			}
		};
		add(englishLink);

		portugueseLink = new Link<Void>("pt") {
			private static final long serialVersionUID = 7840770556788324727L;

			@Override
			public void onClick() {
				SecurityUtils.setLocale("PT");
				setResponsePage(getWebPage());
				portugueseLink.setVisible(false);
				spanishLink.setVisible(true);
				englishLink.setVisible(true);
			}
		};
		add(portugueseLink);

		setLocaleLinksVisibility(SecurityUtils.getLocale().getLanguage());
	}

	private void setLocaleLinksVisibility(String language) {
		portugueseLink.setVisible(true);
		spanishLink.setVisible(true);
		englishLink.setVisible(true);

		switch(language) {
		case "pt":
			portugueseLink.setVisible(false);
			break;

		case "en":
			englishLink.setVisible(false);
			break;

		case "es":
			spanishLink.setVisible(false);
			break;	
		}
	}

}
