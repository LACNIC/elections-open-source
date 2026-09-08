package net.lacnic.elections.adminweb.ui.commons;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.ContextRelativeResource;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.domain.LanguageCode;


public class AdminTopBarPanel extends Panel {

	private static final long serialVersionUID = 2624199794320344167L;

	Link<Void> spanishLink;
	Link<Void> portugueseLink;
	Link<Void> englishLink;
	Image selectedLanguageImage;
	Label selectedLanguageCode;


	public AdminTopBarPanel(String id) {
		super(id);

		LanguageCode resolvedLanguage = resolveLanguage(SecurityUtils.getLanguageCode());

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
				changeLanguage(LanguageCode.SP);
			}
		};
		add(spanishLink);

		englishLink = new Link<Void>("en") {
			private static final long serialVersionUID = -6590594722943249118L;

			@Override
			public void onClick() {
				changeLanguage(LanguageCode.EN);
			}
		};
		add(englishLink);

		portugueseLink = new Link<Void>("pt") {
			private static final long serialVersionUID = 7840770556788324727L;

			@Override
			public void onClick() {
				changeLanguage(LanguageCode.PT);
			}
		};
		add(portugueseLink);

		selectedLanguageImage = new Image("selectedLanguageImage", new ContextRelativeResource(resolveFlagPath(resolvedLanguage)));
		selectedLanguageImage.add(new AttributeModifier("alt", resolvedLanguage.name()));
		selectedLanguageImage.setOutputMarkupId(true);
		add(selectedLanguageImage);

		selectedLanguageCode = new Label("selectedLanguageCode", resolvedLanguage.name());
		selectedLanguageCode.setOutputMarkupId(true);
		add(selectedLanguageCode);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		initLocaleVisibilityLinks(SecurityUtils.getLanguageCode());
	}

	private void initLocaleVisibilityLinks(LanguageCode language) {
		LanguageCode resolvedLanguage = language != null ? language : LanguageCode.SP;
		portugueseLink.setVisible(true);
		spanishLink.setVisible(true);
		englishLink.setVisible(true);

		switch(resolvedLanguage) {
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

	private LanguageCode resolveLanguage(LanguageCode language) {
		return language != null ? language : LanguageCode.SP;
	}

	private String resolveFlagPath(LanguageCode language) {
		LanguageCode resolved = resolveLanguage(language);
		switch (resolved) {
		case EN:
			return "v2/images/flags/us.svg";
		case PT:
			return "v2/images/flags/pt.svg";
		case SP:
		default:
			return "v2/images/flags/es.svg";
		}
	}

	private void changeLanguage(LanguageCode language) {
		SecurityUtils.setLocale(language);
		setResponsePage(getWebPage());
	}

}
