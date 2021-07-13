package net.lacnic.elections.adminweb.ui.admin.customization;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;


public class CustomizationDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = 7194875637803429096L;


	public CustomizationDashboard(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));
		add(new CustomizationPanel("customizationPanel"));
	}

}
