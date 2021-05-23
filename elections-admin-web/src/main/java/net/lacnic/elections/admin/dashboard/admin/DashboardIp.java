package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.panel.avanzadas.ListadoipInhabilitadasPanel;

public class DashboardIp extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;
	
	public DashboardIp( PageParameters params)  {
		super(params);
		add(new ListadoipInhabilitadasPanel("listadoIpInhabilitadas"));

	}

}

