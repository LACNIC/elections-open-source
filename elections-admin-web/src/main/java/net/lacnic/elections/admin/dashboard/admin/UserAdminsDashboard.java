package net.lacnic.elections.admin.dashboard.admin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.panel.admin.AddUserAdminPanel;
import net.lacnic.elections.admin.web.panel.admin.UserAdminsListPanel;


public class UserAdminsDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = 6244585344817907385L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public UserAdminsDashboard(PageParameters params) {
		super(params);
		try {
			add(new FeedbackPanel("feedback"));
			add(new AddUserAdminPanel("addUserAdminPanel"));
			add(new UserAdminsListPanel("userAdminsList"));
		} catch (Exception ex) {
			appLogger.error(ex);
		}
	}

}
