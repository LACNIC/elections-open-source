package net.lacnic.elections.adminweb.ui.results.audit;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonAuditorRevision;
import net.lacnic.elections.domain.Auditor;


public class AuditorRevisionPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;


	public AuditorRevisionPanel(String id, Auditor auditor) {
		super(id);
		setOutputMarkupPlaceholderTag(true);

		setVisible(isRevisionAuthorizationPending(auditor));

		ButtonAuditorRevision auditorRevisionButton = new ButtonAuditorRevision("auditorRevisionButton") {
			private static final long serialVersionUID = -4732183068550356175L;

			@Override
			public void onConfirm() {
				AppContext.getInstance().getVoterBeanRemote().enableAuditorElectionRevision(auditor.getAuditorId(), SecurityUtils.getClientIp());
				auditor.setRevisionAvailable(true);
				AuditorRevisionPanel.this.setVisible(false);
				getSession().info(getString("revisionActive"));
			}
		};
		add(auditorRevisionButton);

		add(new Label("election", auditor.getElection().getTitleSpanish()));
		add(new Label("auditor", auditor.getName()));
	}

	private boolean isRevisionAuthorizationPending(Auditor auditor) {
		return auditor != null
				&& auditor.isCommissioner()
				&& auditor.getElection() != null
				&& auditor.getElection().isRevisionRequest()
				&& !auditor.isRevisionAvailable();
	}

}
