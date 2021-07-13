package net.lacnic.elections.adminweb.ui.admin.ipaccess;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;


public class IpAccessDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -1585312634111966702L;


	public IpAccessDashboard(PageParameters params)  {
		super(params);
		add(new IpAccessListPanel("ipAccessList"));
	}

}
