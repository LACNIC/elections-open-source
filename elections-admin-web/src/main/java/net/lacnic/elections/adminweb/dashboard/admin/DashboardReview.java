package net.lacnic.elections.adminweb.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.web.elecciones.ListaAuditoresRevisionPanel;
import net.lacnic.elections.adminweb.web.panel.admin.ListaReviewPanel;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;

@AuthorizeInstantiation("elections-only-one")
public class DashboardReview extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;

	public DashboardReview(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));

		Long idEleccion = UtilsParameters.getIdAsLong(params);
		boolean revisionActiva = AppContext.getInstance().getManagerBeanRemote().isRevisionActive(idEleccion, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());

		if (!revisionActiva)
			SecurityUtils.info("Faltan auditores por permitir acceso a esta sección");

		add(new ListaAuditoresRevisionPanel("listaAuditoresRevisionPanel", idEleccion));

		ListaReviewPanel listaReviewPanel = new ListaReviewPanel("listVotos", idEleccion);
		listaReviewPanel.setVisible(revisionActiva);
		add(listaReviewPanel);
	}

}
