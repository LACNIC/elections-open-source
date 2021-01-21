package net.lacnic.siselecciones.admin.web.commons;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.Application;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.wicket.util.ImageResource;
import net.lacnic.siselecciones.dominio.Personalizacion;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardLoginPage; 


public class TopBarPublic extends Panel {

	private static final long serialVersionUID = -6437181317906564167L;

	private Personalizacion personalizacion;

	Link<Void> spanishLink;
	Link<Void> portugueseLink;
	Link<Void> englishLink;
	
	public TopBarPublic(String id) {
		super(id);
		BookmarkablePageLink<Void> pagelink = new BookmarkablePageLink<>("home", Application.get().getHomePage());
		add(pagelink);
		
		BookmarkablePageLink<Void> loginLink = new BookmarkablePageLink<>("linkLogin", DashboardLoginPage.class);
		add(loginLink);

		/* Cargo el logo */
		String ext = "";
		byte[] archivoPicSmallLogo;
		String nombreArchivoSmallLogo;

		personalizacion = Contexto.getInstance().getManagerBeanRemote().getPersonalizacion();
		nombreArchivoSmallLogo = personalizacion.getPicSmallLogo();
		archivoPicSmallLogo = personalizacion.getContPicSmallLogo();
		if (archivoPicSmallLogo == null) {
			pagelink.add(new ContextImage("fotoSmallLogo","image/" + nombreArchivoSmallLogo));
		} else {
			ext = FilenameUtils.getExtension(nombreArchivoSmallLogo);
			pagelink.add(new NonCachingImage("fotoSmallLogo", new ImageResource(archivoPicSmallLogo, ext)));
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
