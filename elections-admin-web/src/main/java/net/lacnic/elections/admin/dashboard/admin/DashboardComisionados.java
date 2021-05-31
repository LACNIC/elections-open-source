package net.lacnic.elections.admin.dashboard.admin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.panel.admin.AgregarComisionadoPanel;
import net.lacnic.elections.admin.web.panel.admin.ListComisionadosPanel;
import net.lacnic.elections.domain.Commissioner;

public class DashboardComisionados extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4630074025091464359L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private Commissioner comisionado;

	public DashboardComisionados(PageParameters params) {
		super(params);
		try {
			add(new FeedbackPanel("feedback"));
			add(new AgregarComisionadoPanel("agregarComisionadoPanel"));
			add(new ListComisionadosPanel("listaComisionados"));

		} catch (Exception ex) {
			appLogger.error(ex);
		}

	}

	public Commissioner getComisionado() {
		return comisionado;
	}

	public void setComisionado(Commissioner comisionado) {
		this.comisionado = comisionado;
	}

}
