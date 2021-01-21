package net.lacnic.siselecciones.admin.web.commons;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebApplication;

import net.lacnic.siselecciones.admin.app.SecurityUtils;

public class TopBar extends Panel {

	private static final long serialVersionUID = 1L;

	public TopBar(String id) {
		super(id);

		add(new Link<Void>("linkLogout") {

			@Override
			public void onClick() {
				SecurityUtils.logOut();
				setResponsePage(WebApplication.get().getHomePage());
			}

		});

		add(new BookmarkablePageLink<Void>("linkHome", WebApplication.get().getHomePage()));
	}

}
