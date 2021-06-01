package net.lacnic.elections.admin.dashboard.user;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.dashboard.error.Error404;
import net.lacnic.elections.admin.dashboard.error.ErrorAuditoriaNoPublica;
import net.lacnic.elections.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.elections.admin.web.panel.elecciones.AprobacionAuditorPanel;
import net.lacnic.elections.admin.web.panel.elecciones.CodigosCandidatoPanel;
import net.lacnic.elections.admin.web.panel.elecciones.MoreInformationForAuditPanel;
import net.lacnic.elections.admin.web.panel.elecciones.ResultadoEleccionPanel;
import net.lacnic.elections.admin.web.panel.elecciones.RevisionAuditorPanel;
import net.lacnic.elections.domain.Auditor;

public class DashboardAuditores extends DashboardPublicBasePage {

	private static final long serialVersionUID = 5362590873487534369L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private Auditor a;

	public DashboardAuditores(final PageParameters params) {
		super(params);
		try {
			if (a.isAgreedConformity() && a.isCommissioner())
				getSession().info(getString("auditorconforme"));

			boolean seSolicitoRevision = a.getElection().isRevisionRequest();
			if (a.isRevisionAvailable() && seSolicitoRevision && a.isCommissioner())
				getSession().info(getString("revisionactiva"));

			add(new FeedbackPanel("feedbackPanel"));

			// si se solicita revisión el auditor solo puede ver el panel de habilitación de revisión
			RevisionAuditorPanel revisionAuditorPanel = new RevisionAuditorPanel("revisionAuditorPanel", a);
			revisionAuditorPanel.setVisibilityAllowed(seSolicitoRevision);
			add(revisionAuditorPanel);

			WebMarkupContainer sinRevision = new WebMarkupContainer("sinRevision");
			sinRevision.setVisibilityAllowed(!seSolicitoRevision);
			sinRevision.add(new Label("titulo", getEleccion().getTitle(getIdioma())));
			Label desc = new Label("descripcion", getEleccion().getDescription(getIdioma()));
			desc.setEscapeModelStrings(false);
			sinRevision.add(desc);
			sinRevision.add(new AprobacionAuditorPanel("aprobacionAuditorPanel", a));
			sinRevision.add(new ResultadoEleccionPanel("resultadoPanel", getEleccion().getElectionId()));
			sinRevision.add(new CodigosCandidatoPanel("codigosCandidatoPanel", getEleccion().getElectionId()));
			sinRevision.add(new MoreInformationForAuditPanel("moreInformationForAuditPanel", getEleccion().getElectionId()));
			add(sinRevision);

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public Class validarToken(PageParameters params) {
		a = AppContext.getInstance().getVoterBeanRemote().verifyAuditorResultAccess(getToken());
		if (a == null) {
			AppContext.getInstance().getVoterBeanRemote().saveFailedAccessIp(getIP());
			return Error404.class;
		} else {
			setEleccion(a.getElection());
			if (!a.getElection().isAuditorLinkAvailable()) {
				return ErrorAuditoriaNoPublica.class;
			}
		}
		return null;

	}

}