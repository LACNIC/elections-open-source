package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;


@AuthorizeInstantiation("elections-only-one")
public class EmailTemplatesDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -3810171188976111337L;


	public EmailTemplatesDashboard(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedback"));

		long electionId = UtilsParameters.getIdAsLong(params);
		AddEmailTemplatePanel addEmailTemplatePanel = new AddEmailTemplatePanel("addEmailTemplatePanel");
		addEmailTemplatePanel.setVisible(electionId == 0);
		add(addEmailTemplatePanel);

		add(new EmailTemplatesListPanel("emailTemplatesList", electionId));
	}

}
