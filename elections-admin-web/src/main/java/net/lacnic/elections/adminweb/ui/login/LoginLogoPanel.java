package net.lacnic.elections.adminweb.ui.login;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.domain.Customization;


public class LoginLogoPanel extends Panel {

	private static final long serialVersionUID = -7352907021232873762L;

	private Customization customization;


	public LoginLogoPanel(String id) {
		super(id);

		BookmarkablePageLink<Void> pageLink = new BookmarkablePageLink<>("homeLink", getApplication().getHomePage());
		add(pageLink);

		String extension = "";
		byte[] bigLogoFile;
		String bigLogoFileName;

		customization = AppContext.getInstance().getManagerBeanRemote().getCustomization();
		bigLogoFileName = customization.getPicBigLogo();
		bigLogoFile = customization.getContPicBigLogo();
		if (bigLogoFile == null) {
			pageLink.add(new ContextImage("logoImage","image/" + bigLogoFileName));
		} else {
			extension = FilenameUtils.getExtension(bigLogoFileName);
			pageLink.add(new NonCachingImage("logoImage", new ImageResource(bigLogoFile, extension)));
		}
	}

}
