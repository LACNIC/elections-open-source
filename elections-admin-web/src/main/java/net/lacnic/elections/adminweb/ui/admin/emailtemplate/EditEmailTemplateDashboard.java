package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;


@AuthorizeInstantiation("elections-only-one")
public class EditEmailTemplateDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = 2809488890773004832L;


	public EditEmailTemplateDashboard(String tipo, PageParameters params) {
		super(params);
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);

		ElectionEmailTemplate emailTemplate = AppContext.getInstance().getManagerBeanRemote().getEmailTemplate(tipo, UtilsParameters.getIdAsLong(params));
		add(new EditEmailTemplatePanel("editTemplatePanel", emailTemplate));
	}

}
