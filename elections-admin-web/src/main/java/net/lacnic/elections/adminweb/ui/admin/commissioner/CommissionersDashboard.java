package net.lacnic.elections.adminweb.ui.admin.commissioner;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.ui.bases.DashboardManagerBasePage;
import net.lacnic.elections.domain.Commissioner;

public class CommissionersDashboard extends DashboardManagerBasePage {

	private static final long serialVersionUID = -4630074025091464359L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private Commissioner commissioner;

	public CommissionersDashboard(PageParameters params) {
		super(params);
		try {
			add(new FeedbackPanel("feedback"));
			add(new AddCommissionerPanel("addCommissionerPanel"));
			add(new CommissionersListPanel("commissionersList"));
		} catch (Exception ex) {
			appLogger.error(ex.getMessage(), ex);
		}
	}

	public Commissioner getCommissioner() {
		return commissioner;
	}

	public void setCommissioner(Commissioner commissioner) {
		this.commissioner = commissioner;
	}

}
