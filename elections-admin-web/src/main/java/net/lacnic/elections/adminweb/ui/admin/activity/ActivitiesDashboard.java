package net.lacnic.elections.adminweb.ui.admin.activity;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;


public class ActivitiesDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -3060903190903301755L;


	public ActivitiesDashboard(PageParameters params) {
		super(params);
		add(new ActivitiesListPanel("activitiesList", -1));
	}

}
