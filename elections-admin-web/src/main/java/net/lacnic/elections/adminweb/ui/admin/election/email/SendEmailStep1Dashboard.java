package net.lacnic.elections.adminweb.ui.admin.election.email;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardManagerBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;


public class SendEmailStep1Dashboard extends DashboardManagerBasePage {

	private static final long serialVersionUID = 3492162799382642026L;


	public SendEmailStep1Dashboard(String templateType, PageParameters params) {
		super(params);
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);

		ElectionEmailTemplate editTemplates = AppContext.getInstance().getManagerBeanRemote().getEmailTemplate(templateType, UtilsParameters.getIdAsLong(params));
		add(new SendEmailStep1Panel("editTemplates", editTemplates, params));
	}

}
