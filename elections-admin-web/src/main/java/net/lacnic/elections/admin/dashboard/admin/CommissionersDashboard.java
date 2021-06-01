package net.lacnic.elections.admin.dashboard.admin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.panel.admin.AddCommissionerPanel;
import net.lacnic.elections.admin.web.panel.admin.CommissionersListPanel;
import net.lacnic.elections.domain.Commissioner;

public class CommissionersDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4630074025091464359L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private Commissioner commissioner;


	public CommissionersDashboard(PageParameters params) {
		super(params);
		try {
			add(new FeedbackPanel("feedback"));
			add(new AddCommissionerPanel("addCommissionerPanel"));
			add(new CommissionersListPanel("commissionersList"));
		} catch (Exception ex) {
			appLogger.error(ex);
		}
	}


	public Commissioner getCommissioner() {
		return commissioner;
	}

	public void setCommissioner(Commissioner commissioner) {
		this.commissioner = commissioner;
	}

}
