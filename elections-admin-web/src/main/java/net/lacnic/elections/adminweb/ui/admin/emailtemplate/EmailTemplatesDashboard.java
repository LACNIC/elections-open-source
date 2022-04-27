package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.ui.error.ErrorElectionClosed;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;


@AuthorizeInstantiation("elections-only-one")
public class EmailTemplatesDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -3810171188976111337L;


	public EmailTemplatesDashboard(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedback"));

		long electionId = UtilsParameters.getIdAsLong(params);
		if(electionId != 0) {
			// Check if election is closed (user might be using a direct link to get to this page)
			Election election = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));
			if(election.isClosed()) {
				setResponsePage(ErrorElectionClosed.class);
				return;
			}
		}

		AddEmailTemplatePanel addEmailTemplatePanel = new AddEmailTemplatePanel("addEmailTemplatePanel");
		addEmailTemplatePanel.setVisible(electionId == 0);
		add(addEmailTemplatePanel);
		add(new EmailTemplatesListPanel("emailTemplatesList", electionId));
	}

}
