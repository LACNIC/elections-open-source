package net.lacnic.elections.adminweb.ui.results.review;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;


@AuthorizeInstantiation("elections-only-one")
public class ReviewDashboard extends DashboardAdminBasePage {

	private static final long serialVersionUID = -1421543964237403877L;


	public ReviewDashboard(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));

		Long electionId = UtilsParameters.getIdAsLong(params);
		boolean revisionActiva = AppContext.getInstance().getManagerBeanRemote().isRevisionActive(electionId, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());

		if (!revisionActiva)
			SecurityUtils.info("Faltan auditores por permitir acceso a esta sección");

		add(new ReviewAuditorsListPanel("auditorsListPanel", electionId));

		ReviewVotesListPanel listaReviewPanel = new ReviewVotesListPanel("votesList", electionId);
		listaReviewPanel.setVisible(revisionActiva);
		add(listaReviewPanel);
	}

}
