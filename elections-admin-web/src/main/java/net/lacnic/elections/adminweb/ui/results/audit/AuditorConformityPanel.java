package net.lacnic.elections.adminweb.ui.results.audit;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonAuditorApproval;
import net.lacnic.elections.adminweb.ui.token.page.AuditPublicDashboardPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Auditor;


public class AuditorConformityPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;


	public AuditorConformityPanel(String id, Auditor auditor, String reactionDeadlineDate, String reactionDeadlineRemaining) {
		super(id);
		setVisible(!auditor.isAgreedConformity() && auditor.isCommissioner());
		add(new BookmarkablePageLink<Void>("backToAuditDashboard", AuditPublicDashboardPage.class,
				UtilsParameters.getToken(auditor.getResultToken())));

		ButtonAuditorApproval auditorConformButton = new ButtonAuditorApproval("auditorConformButton") {
			private static final long serialVersionUID = 2644989317078624142L;

			@Override
			public void onConfirm() {
				AppContext.getInstance().getVoterBeanRemote().confirmAuditorAgreedConformity(auditor.getAuditorId());
				getSession().info(getString("auditorAgreedConfority"));
				setResponsePage(AuditPublicDashboardPage.class, UtilsParameters.getToken(auditor.getResultToken()));
			}
		};
		add(auditorConformButton);

		add(new Label("election", auditor.getElection().getTitle(SecurityUtils.getLocale().getLanguage())));
		add(new Label("auditor", auditor.getName()));
		add(new Label("reactionDeadlineDate", reactionDeadlineDate != null ? reactionDeadlineDate : "-"));
		add(new Label("reactionDeadlineRemaining", reactionDeadlineRemaining != null ? reactionDeadlineRemaining : "-"));
	}

}
