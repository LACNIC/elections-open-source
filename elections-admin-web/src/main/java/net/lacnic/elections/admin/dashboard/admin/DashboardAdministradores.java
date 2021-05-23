package net.lacnic.elections.admin.dashboard.admin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.panel.admin.AgregarAdminPanel;
import net.lacnic.elections.admin.web.panel.admin.ListaAdministradoresPanel;

public class DashboardAdministradores extends DashboardAdminBasePage {

	private static final long serialVersionUID = 6244585344817907385L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public DashboardAdministradores(PageParameters params) {
		super(params);
		try {
			add(new FeedbackPanel("feedback"));
			add(new AgregarAdminPanel("agregarAdminPanel"));
			add(new ListaAdministradoresPanel("listaAdministradores"));

		} catch (Exception ex) {
			appLogger.error(ex);
		}

	}
}
