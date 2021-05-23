package net.lacnic.elections.admin.web.panel.elecciones;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.dashboard.user.DashboardAuditores;
import net.lacnic.elections.admin.web.commons.BotonAprobacionAuditor;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;

public class AprobacionAuditorPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	public AprobacionAuditorPanel(String id, Auditor a) {
		super(id);
		setVisible(!a.isExpresoConformidad() && a.isComisionado());

		BotonAprobacionAuditor botonAprobacionAuditor = new BotonAprobacionAuditor("botonAprobacionAuditor") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onConfirmar() {
				AppContext.getInstance().getVoterBeanRemote().confirmarEleccionAuditor(a.getIdAuditor());
				setResponsePage(DashboardAuditores.class, UtilsParameters.getToken(a.getTokenResultado()));
			}
		};
		add(botonAprobacionAuditor);

		add(new Label("eleccion", a.getEleccion().getTituloEspanol()));
		add(new Label("auditor", a.getNombre()));

	}

}