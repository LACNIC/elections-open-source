package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardManagerBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;

public class EditEmailTemplateDashboard extends DashboardManagerBasePage {

	private static final long serialVersionUID = 2809488890773004832L;


	public EditEmailTemplateDashboard(String tipo, PageParameters params) {
		super(params);
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);

		ElectionEmailTemplate emailTemplate = AppContext.getInstance().getManagerBeanRemote().getEmailTemplate(tipo, UtilsParameters.getIdAsLong(params));
		add(new EditEmailTemplatePanel("editTemplatePanel", emailTemplate, params));
	}

}
