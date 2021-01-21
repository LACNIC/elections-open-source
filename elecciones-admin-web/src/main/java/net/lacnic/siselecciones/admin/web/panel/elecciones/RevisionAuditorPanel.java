package net.lacnic.siselecciones.admin.web.panel.elecciones;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.dashboard.user.DashboardAuditores;
import net.lacnic.siselecciones.admin.web.commons.BotonRevisionAuditor;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Auditor;

public class RevisionAuditorPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	public RevisionAuditorPanel(String id, Auditor a) {
		super(id);

		setVisible(a.getEleccion().isSolicitarRevision() && !a.isHabilitaRevision());

		BotonRevisionAuditor botonHabilitarRevisionAuditor = new BotonRevisionAuditor("botonHabilitarRevisionAuditor") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onConfirmar() {
				Contexto.getInstance().getVotanteBeanRemote().habilitarRevisionEleccionAuditor(a.getIdAuditor(), SecurityUtils.getIPClient());
				setResponsePage(DashboardAuditores.class, UtilsParameters.getToken(a.getTokenResultado()));
			}
		};
		add(botonHabilitarRevisionAuditor);

		add(new Label("eleccion", a.getEleccion().getTituloEspanol()));
		add(new Label("auditor", a.getNombre()));

	}

}