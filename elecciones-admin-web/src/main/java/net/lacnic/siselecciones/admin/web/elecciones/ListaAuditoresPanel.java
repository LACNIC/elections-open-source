package net.lacnic.siselecciones.admin.web.elecciones;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardEditarAuditor;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionAuditores;
import net.lacnic.siselecciones.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Auditor;
import net.lacnic.siselecciones.utils.UtilsLinks;

public class ListaAuditoresPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ListaAuditoresPanel(String id, long idEleccion) {
		super(id);
		try {
			List<Auditor> auditores = Contexto.getInstance().getManagerBeanRemote().obtenerAuditoresEleccion(idEleccion);
			ListView<Auditor> auditoresDataView = new ListView<Auditor>("auditoresList", auditores) {
				
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(final ListItem<Auditor> item) {
					final Auditor actual = item.getModelObject();
					item.add(new Label("nombreA", actual.getNombre()));
					item.add(new Label("mailA", actual.getMail()));
					item.add(new Label("isComisionadoA", (actual.isComisionado() ? "SI" : "NO")));
					item.add(new Label("expresoConformidad", (actual.isComisionado() ? (actual.isExpresoConformidad() ? "SI" : "NO") : "-")));
					String calcularLinkVotar = UtilsLinks.calcularLinkResultadoAuditor(actual.getTokenResultado());
					Label textoLinkVotar = new Label("textoLink", calcularLinkVotar);
					ExternalLink linkvotar = new ExternalLink("link", calcularLinkVotar);
					linkvotar.add(textoLinkVotar);
					item.add(linkvotar);

					Link<Void> editarAuditor = new Link<Void>("editarAuditor") {

						private static final long serialVersionUID = -2734403145438500636L;

						@Override
						public void onClick() {
							setResponsePage(DashboardEditarAuditor.class, UtilsParameters.getAudit(actual.getIdAuditor()));
						}

					};
					editarAuditor.setMarkupId("editarAuditor"+item.getIndex());
					item.add(editarAuditor);

					item.add(new BotonConConfirmacionEliminar("eliminar",item.getIndex()) {

						private static final long serialVersionUID = 4479213289810959012L;

						@Override
						public void onConfirmar() {
							SecurityUtils.info(getString("auditorManagementExitoDel"));
							Contexto.getInstance().getManagerBeanRemote().eliminarAuditor(actual.getIdAuditor(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
							setResponsePage(DashboardGestionAuditores.class, UtilsParameters.getId(idEleccion));
						}

					});
				}
			};
			add(auditoresDataView);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}
}