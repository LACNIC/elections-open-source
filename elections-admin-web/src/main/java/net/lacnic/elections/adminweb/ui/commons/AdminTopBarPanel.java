package net.lacnic.elections.adminweb.ui.commons;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebApplication;

import net.lacnic.elections.adminweb.app.SecurityUtils;


public class AdminTopBarPanel extends Panel {

	private static final long serialVersionUID = 2624199794320344167L;

	Link<Void> spanishLink;
	Link<Void> portugueseLink;
	Link<Void> englishLink;


	public AdminTopBarPanel(String id) {
		super(id);

		add(new Link<Void>("logoutLink") {
			private static final long serialVersionUID = -7660363462781298321L;

			@Override
			public void onClick() {
				SecurityUtils.logOut();
				setResponsePage(WebApplication.get().getHomePage());
			}
		});

		add(new BookmarkablePageLink<Void>("homeLink", WebApplication.get().getHomePage()));

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

		initLocaleVisibilityLinks(SecurityUtils.getLocale().getLanguage());
	}

	private void initLocaleVisibilityLinks(String language) {
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
