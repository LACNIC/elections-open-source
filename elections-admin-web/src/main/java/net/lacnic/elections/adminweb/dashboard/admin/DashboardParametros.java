package net.lacnic.elections.adminweb.dashboard.admin;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.web.panel.avanzadas.AddParametrosPanel;
import net.lacnic.elections.adminweb.web.panel.avanzadas.ListadoParametrosPanel;

public class DashboardParametros extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;

	public DashboardParametros(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));		
		add(new AddParametrosPanel("agregarParametro"));
		add(new ListadoParametrosPanel("listadoParametro"));

	}

}
