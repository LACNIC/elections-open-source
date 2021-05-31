package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.panel.admin.EnviarEmailPaso1Panel;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardEnviarEmailPaso1 extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;

	public DashboardEnviarEmailPaso1(String tipo, PageParameters params) {
		super(params);
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);

		ElectionEmailTemplate t = AppContext.getInstance().getManagerBeanRemote().obtenerTemplate(tipo, UtilsParameters.getIdAsLong(params));
		add(new EnviarEmailPaso1Panel("editarPlantilla", t));
	}

}
