package net.lacnic.elections.admin.web.commons;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.wicket.util.ImageResource;
import net.lacnic.elections.domain.Customization;

public class FooterLogoPanel extends Panel {

	private static final long serialVersionUID = -7352907021232873762L;

	private Customization personalizacion;

	public FooterLogoPanel(String id) {
		super(id);
		BookmarkablePageLink<Void> pageLink = new BookmarkablePageLink<>("home", getApplication().getHomePage());
		add(pageLink);

		/* Cargo el logo */
		String ext = "";
		byte[] archivoPicBigLogo;
		String nombreArchivoBigLogo;

		personalizacion = AppContext.getInstance().getManagerBeanRemote().getCustomization();
		nombreArchivoBigLogo = personalizacion.getPicBigLogo();
		archivoPicBigLogo = personalizacion.getContPicBigLogo();
		if (archivoPicBigLogo == null) {
			pageLink.add(new ContextImage("fotoBigLogo","image/" + nombreArchivoBigLogo));
		} else {
			ext = FilenameUtils.getExtension(nombreArchivoBigLogo);
			pageLink.add(new NonCachingImage("fotoBigLogo", new ImageResource(archivoPicBigLogo, ext)));
		}
	}

}
