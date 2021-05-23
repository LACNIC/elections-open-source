package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.panel.elecciones.ListEleccionesPanel;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardHomePage extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;

	public DashboardHomePage(PageParameters params) {
		super(params);
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		add(new ListEleccionesPanel("listaElecciones", params));
		
	}
}
