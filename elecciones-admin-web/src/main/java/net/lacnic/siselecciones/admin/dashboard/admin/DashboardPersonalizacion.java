package net.lacnic.siselecciones.admin.dashboard.admin;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.siselecciones.admin.web.panel.avanzadas.EditPersonalizacionPanel;


public class DashboardPersonalizacion extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;
	
	public DashboardPersonalizacion(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));
		add(new EditPersonalizacionPanel("editPersonalizacion"));
		

	}


}
