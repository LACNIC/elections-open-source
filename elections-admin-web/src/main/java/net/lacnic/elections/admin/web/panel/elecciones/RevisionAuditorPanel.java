package net.lacnic.elections.admin.web.panel.elecciones;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.user.DashboardAuditores;
import net.lacnic.elections.admin.web.commons.BotonRevisionAuditor;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;

public class RevisionAuditorPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	public RevisionAuditorPanel(String id, Auditor a) {
		super(id);

		setVisible(a.getElection().isRevisionRequest() && !a.isRevisionAvailable());

		BotonRevisionAuditor botonHabilitarRevisionAuditor = new BotonRevisionAuditor("botonHabilitarRevisionAuditor") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onConfirmar() {
				AppContext.getInstance().getVoterBeanRemote().habilitarRevisionEleccionAuditor(a.getIdAuditor(), SecurityUtils.getIPClient());
				setResponsePage(DashboardAuditores.class, UtilsParameters.getToken(a.getResulttoke()));
			}
		};
		add(botonHabilitarRevisionAuditor);

		add(new Label("eleccion", a.getElection().getTitleSpanish()));
		add(new Label("auditor", a.getName()));

	}

}