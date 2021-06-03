package net.lacnic.elections.adminweb.dashboard.admin;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.web.panel.avanzadas.EditPersonalizacionPanel;


public class DashboardPersonalizacion extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;
	
	public DashboardPersonalizacion(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));
		add(new EditPersonalizacionPanel("editPersonalizacion"));
		

	}


}
