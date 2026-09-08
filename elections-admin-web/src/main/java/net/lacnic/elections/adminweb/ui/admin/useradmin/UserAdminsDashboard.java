package net.lacnic.elections.adminweb.ui.admin.useradmin;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.ui.bases.DashboardManagerBasePage;
import net.lacnic.elections.adminweb.app.SecurityUtils;

public class UserAdminsDashboard extends DashboardManagerBasePage {

	private static final long serialVersionUID = 6244585344817907385L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	public UserAdminsDashboard(PageParameters params) {
		super(params);
		if (!SecurityUtils.isLocalAuthentication()) {
			SecurityUtils.error(getString("userAdminLocalAccessDenied"));
			throw new RestartResponseException(SecurityUtils.getHomePage());
		}
		try {
			add(new FeedbackPanel("feedback"));
			add(new AddUserAdminPanel("addUserAdminPanel"));
			add(new UserAdminsListPanel("userAdminsList"));
		} catch (Exception ex) {
			appLogger.error(ex.getMessage(), ex);
		}
	}

}
