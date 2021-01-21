package net.lacnic.siselecciones.admin.dashboard.user;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.dashboard.error.Error404;
import net.lacnic.siselecciones.admin.dashboard.error.ErrorAuditoriaNoPublica;
import net.lacnic.siselecciones.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.siselecciones.admin.web.panel.elecciones.AprobacionAuditorPanel;
import net.lacnic.siselecciones.admin.web.panel.elecciones.CodigosCandidatoPanel;
import net.lacnic.siselecciones.admin.web.panel.elecciones.MoreInformationForAuditPanel;
import net.lacnic.siselecciones.admin.web.panel.elecciones.ResultadoEleccionPanel;
import net.lacnic.siselecciones.admin.web.panel.elecciones.RevisionAuditorPanel;
import net.lacnic.siselecciones.dominio.Auditor;

public class DashboardAuditores extends DashboardPublicBasePage {

	private static final long serialVersionUID = 5362590873487534369L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private Auditor a;

	public DashboardAuditores(final PageParameters params) {
		super(params);
		try {
			if (a.isExpresoConformidad() && a.isComisionado())
				getSession().info(getString("auditorconforme"));

			boolean seSolicitoRevision = a.getEleccion().isSolicitarRevision();
			if (a.isHabilitaRevision() && seSolicitoRevision && a.isComisionado())
				getSession().info(getString("revisionactiva"));

			add(new FeedbackPanel("feedbackPanel"));

			// si se solicita revisión el auditor solo puede ver el panel de habilitación de revisión
			RevisionAuditorPanel revisionAuditorPanel = new RevisionAuditorPanel("revisionAuditorPanel", a);
			revisionAuditorPanel.setVisibilityAllowed(seSolicitoRevision);
			add(revisionAuditorPanel);

			WebMarkupContainer sinRevision = new WebMarkupContainer("sinRevision");
			sinRevision.setVisibilityAllowed(!seSolicitoRevision);
			sinRevision.add(new Label("titulo", getEleccion().getTitulo(getIdioma())));
			Label desc = new Label("descripcion", getEleccion().getDescripcion(getIdioma()));
			desc.setEscapeModelStrings(false);
			sinRevision.add(desc);
			sinRevision.add(new AprobacionAuditorPanel("aprobacionAuditorPanel", a));
			sinRevision.add(new ResultadoEleccionPanel("resultadoPanel", getEleccion().getIdEleccion()));
			sinRevision.add(new CodigosCandidatoPanel("codigosCandidatoPanel", getEleccion().getIdEleccion()));
			sinRevision.add(new MoreInformationForAuditPanel("moreInformationForAuditPanel", getEleccion().getIdEleccion()));
			add(sinRevision);

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public Class validarToken(PageParameters params) {
		a = Contexto.getInstance().getVotanteBeanRemote().verificarAccesoResultadoAuditor(getToken());
		if (a == null) {
			Contexto.getInstance().getVotanteBeanRemote().intentoFallidoIp(getIP());
			return Error404.class;
		} else {
			setEleccion(a.getEleccion());
			if (!a.getEleccion().isHabilitadoLinkAuditor()) {
				return ErrorAuditoriaNoPublica.class;
			}
		}
		return null;

	}

}