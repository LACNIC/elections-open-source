package net.lacnic.elections.adminweb.dashboard.admin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.web.panel.admin.EditEleccionesJuntasPanel;
import net.lacnic.elections.adminweb.web.panel.admin.ListEleccionesJuntasPanel;

public class DashboardEleccionesJuntas extends DashboardAdminBasePage {

	private static final long serialVersionUID = -7174020647398765740L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public DashboardEleccionesJuntas() {
		super(new PageParameters());

		try {
			FeedbackPanel feedbackPanel = new FeedbackPanel("feedbackPanel");
			feedbackPanel.setOutputMarkupPlaceholderTag(true);
			add(feedbackPanel);

			crearMarkupContainerAcciones();
		} catch (Exception e) {
			setResponsePage(getApplication().getHomePage());
			appLogger.error(e);
		}
	}

	private void crearMarkupContainerAcciones() {
		ListEleccionesJuntasPanel eleccionesLineas = new ListEleccionesJuntasPanel("eleccionesLineas");
		add(eleccionesLineas);

		EditEleccionesJuntasPanel eleccionesCabezal = new EditEleccionesJuntasPanel("eleccionesCabezal");
		add(eleccionesCabezal);
	}
}
