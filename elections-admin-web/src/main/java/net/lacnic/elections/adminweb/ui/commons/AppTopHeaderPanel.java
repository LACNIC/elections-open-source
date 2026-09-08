package net.lacnic.elections.adminweb.ui.commons;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.admin.election.ElectionsDashboard;
import net.lacnic.elections.adminweb.ui.login.LoginDashboard;
import net.lacnic.elections.domain.LanguageCode;

public class AppTopHeaderPanel extends Panel {

	private static final long serialVersionUID = 3467760814935257347L;
	private Link<Void> spanishLink;
	private Link<Void> portugueseLink;
	private Link<Void> englishLink;

	public AppTopHeaderPanel(String id) {
		this(id, null);
	}

	public AppTopHeaderPanel(String id, String sectionLabel) {
		super(id);

		boolean signedIn = SecurityUtils.isSignedIn();
		String resolvedSectionLabel = (sectionLabel != null && !sectionLabel.trim().isEmpty())
				? sectionLabel
				: getString(signedIn ? "appTopHeaderSectionAdmin" : "appTopHeaderSectionPublic");
		Class<? extends Page> homePageClass = resolveHomePageClass(signedIn);

		String siteTitle = AppContext.getInstance().getManagerBeanRemote().getCustomization().getSiteTitle();
		BookmarkablePageLink<Void> siteTitleLink = new BookmarkablePageLink<>("siteTitleLink", homePageClass);
		siteTitleLink.add(new Label("siteTitle", siteTitle));
		add(siteTitleLink);
		add(new BookmarkablePageLink<Void>("homeLink", homePageClass));
		add(new Label("headerSectionLabel", resolvedSectionLabel));

		Label username = new Label("username", SecurityUtils.getUserAdminId());
		username.setVisible(signedIn);
		add(username);

		WebMarkupContainer userLanguageSeparator = new WebMarkupContainer("userLanguageSeparator");
		userLanguageSeparator.setVisible(signedIn);
		add(userLanguageSeparator);

		Link<Void> logoutLink = new Link<Void>("logoutLink") {
			private static final long serialVersionUID = -7660363462781298321L;

			@Override
			public void onClick() {
				SecurityUtils.logOut();
				setResponsePage(WebApplication.get().getHomePage());
			}
		};
		logoutLink.setVisible(signedIn);
		add(logoutLink);

		BookmarkablePageLink<Void> loginLink = new BookmarkablePageLink<>("loginLink", LoginDashboard.class);
		loginLink.setVisible(!signedIn);
		add(loginLink);

		WebMarkupContainer languageSelector = new WebMarkupContainer("languageSelector");
		languageSelector.setVisible(true);
		add(languageSelector);

		spanishLink = new Link<Void>("es") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				changeLanguage(LanguageCode.SP);
			}
		};
		languageSelector.add(spanishLink);

		englishLink = new Link<Void>("en") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				changeLanguage(LanguageCode.EN);
			}
		};
		languageSelector.add(englishLink);

		portugueseLink = new Link<Void>("pt") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				changeLanguage(LanguageCode.PT);
			}
		};
		languageSelector.add(portugueseLink);
	}

	private Class<? extends Page> resolveHomePageClass(boolean signedIn) {
		return signedIn ? ElectionsDashboard.class : SecurityUtils.getHomePage();
	}

	private void initLocaleVisibilityLinks(LanguageCode language) {
		LanguageCode resolvedLanguage = language != null ? language : LanguageCode.SP;
		portugueseLink.setVisible(true);
		spanishLink.setVisible(true);
		englishLink.setVisible(true);

		switch (resolvedLanguage) {
		case PT:
			portugueseLink.setVisible(false);
			break;
		case EN:
			englishLink.setVisible(false);
			break;
		case SP:
		default:
			spanishLink.setVisible(false);
			break;
		}
	}

	private void changeLanguage(LanguageCode language) {
		SecurityUtils.setLocale(language);
		setResponsePage(getPage());
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		initLocaleVisibilityLinks(SecurityUtils.getLanguageCode());
	}
}
