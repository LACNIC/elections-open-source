package net.lacnic.siselecciones.admin.dashboard.admin;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.siselecciones.admin.web.panel.avanzadas.ListadoipInhabilitadasPanel;

public class DashboardIp extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;
	
	public DashboardIp( PageParameters params)  {
		super(params);
		add(new ListadoipInhabilitadasPanel("listadoIpInhabilitadas"));

	}

}

