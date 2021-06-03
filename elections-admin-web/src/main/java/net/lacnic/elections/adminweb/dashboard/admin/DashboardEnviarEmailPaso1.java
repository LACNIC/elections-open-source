package net.lacnic.elections.adminweb.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.web.panel.admin.EnviarEmailPaso1Panel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;

@AuthorizeInstantiation("elections-only-one")
public class DashboardEnviarEmailPaso1 extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;

	public DashboardEnviarEmailPaso1(String tipo, PageParameters params) {
		super(params);
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);

		ElectionEmailTemplate t = AppContext.getInstance().getManagerBeanRemote().getEmailTemplate(tipo, UtilsParameters.getIdAsLong(params));
		add(new EnviarEmailPaso1Panel("editarPlantilla", t));
	}

}
