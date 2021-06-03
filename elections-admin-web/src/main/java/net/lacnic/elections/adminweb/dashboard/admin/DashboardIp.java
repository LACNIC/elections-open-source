package net.lacnic.elections.adminweb.dashboard.admin;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.web.panel.avanzadas.ListadoipInhabilitadasPanel;

public class DashboardIp extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;
	
	public DashboardIp( PageParameters params)  {
		super(params);
		add(new ListadoipInhabilitadasPanel("listadoIpInhabilitadas"));

	}

}

