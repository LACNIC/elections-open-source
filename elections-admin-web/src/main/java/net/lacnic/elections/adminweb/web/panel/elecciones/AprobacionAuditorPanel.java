package net.lacnic.elections.adminweb.web.panel.elecciones;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.dashboard.user.DashboardAuditores;
import net.lacnic.elections.adminweb.ui.components.ButtonAuditorApproval;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;

public class AprobacionAuditorPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	public AprobacionAuditorPanel(String id, Auditor a) {
		super(id);
		setVisible(!a.isAgreedConformity() && a.isCommissioner());

		ButtonAuditorApproval botonAprobacionAuditor = new ButtonAuditorApproval("botonAprobacionAuditor") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onConfirm() {
				AppContext.getInstance().getVoterBeanRemote().confirmAuditorAgreedConformity(a.getAuditorId());
				setResponsePage(DashboardAuditores.class, UtilsParameters.getToken(a.getResultToken()));
			}
		};
		add(botonAprobacionAuditor);

		add(new Label("eleccion", a.getElection().getTitleSpanish()));
		add(new Label("auditor", a.getName()));

	}

}