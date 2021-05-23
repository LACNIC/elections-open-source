package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.elecciones.ListaAuditoresRevisionPanel;
import net.lacnic.elections.admin.web.panel.admin.ListaReviewPanel;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardReview extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;

	public DashboardReview(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));

		Long idEleccion = UtilsParameters.getIdAsLong(params);
		boolean revisionActiva = AppContext.getInstance().getManagerBeanRemote().isRevisionActiva(idEleccion, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());

		if (!revisionActiva)
			SecurityUtils.info("Faltan auditores por permitir acceso a esta sección");

		add(new ListaAuditoresRevisionPanel("listaAuditoresRevisionPanel", idEleccion));

		ListaReviewPanel listaReviewPanel = new ListaReviewPanel("listVotos", idEleccion);
		listaReviewPanel.setVisible(revisionActiva);
		add(listaReviewPanel);
	}

}
