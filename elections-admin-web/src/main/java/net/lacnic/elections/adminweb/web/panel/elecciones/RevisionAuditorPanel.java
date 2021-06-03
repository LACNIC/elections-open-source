package net.lacnic.elections.adminweb.web.panel.elecciones;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.dashboard.user.DashboardAuditores;
import net.lacnic.elections.adminweb.ui.components.ButtonAuditorRevision;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;

public class RevisionAuditorPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	public RevisionAuditorPanel(String id, Auditor a) {
		super(id);

		setVisible(a.getElection().isRevisionRequest() && !a.isRevisionAvailable());

		ButtonAuditorRevision botonHabilitarRevisionAuditor = new ButtonAuditorRevision("botonHabilitarRevisionAuditor") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onConfirm() {
				AppContext.getInstance().getVoterBeanRemote().enableAuditorElectionRevision(a.getAuditorId(), SecurityUtils.getClientIp());
				setResponsePage(DashboardAuditores.class, UtilsParameters.getToken(a.getResultToken()));
			}
		};
		add(botonHabilitarRevisionAuditor);

		add(new Label("eleccion", a.getElection().getTitleSpanish()));
		add(new Label("auditor", a.getName()));

	}

}