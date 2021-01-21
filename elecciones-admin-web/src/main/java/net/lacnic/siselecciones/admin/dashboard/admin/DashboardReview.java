package net.lacnic.siselecciones.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.siselecciones.admin.web.elecciones.ListaAuditoresRevisionPanel;
import net.lacnic.siselecciones.admin.web.panel.admin.ListaReviewPanel;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardReview extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;

	public DashboardReview(PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));

		Long idEleccion = UtilsParameters.getIdAsLong(params);
		boolean revisionActiva = Contexto.getInstance().getManagerBeanRemote().isRevisionActiva(idEleccion, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());

		if (!revisionActiva)
			SecurityUtils.info("Faltan auditores por permitir acceso a esta sección");

		add(new ListaAuditoresRevisionPanel("listaAuditoresRevisionPanel", idEleccion));

		ListaReviewPanel listaReviewPanel = new ListaReviewPanel("listVotos", idEleccion);
		listaReviewPanel.setVisible(revisionActiva);
		add(listaReviewPanel);
	}

}
