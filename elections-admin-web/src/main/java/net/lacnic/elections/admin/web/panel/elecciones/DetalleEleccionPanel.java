package net.lacnic.elections.admin.web.panel.elecciones;

import java.text.SimpleDateFormat;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionAuditores;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionCandidatos;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionEleccion;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionPadron;
import net.lacnic.elections.admin.dashboard.admin.DashboardHomePage;
import net.lacnic.elections.admin.web.panel.admin.ListaActividadesPanel;
import net.lacnic.elections.admin.web.panel.admin.ListaMensajesPanel;
import net.lacnic.elections.admin.web.panel.candidatos.ListCandidatosEleccionPanel;
import net.lacnic.elections.admin.web.panel.usuariopadron.ListUsuariosPadronEleccionPanel;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

public class DetalleEleccionPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public DetalleEleccionPanel(String id, final long idEleccion, final boolean sent) {
		super(id);
		try {

			setOutputMarkupPlaceholderTag(true);
			Election eleccion = AppContext.getInstance().getManagerBeanRemote().obtenerEleccion(idEleccion);
			eleccion.initStringsStartEndDates();

			add(new Label("linkEspanol", eleccion.getLinkSpanish()));
			add(new Label("linkIngles", eleccion.getLinkEnglish()));
			add(new Label("linkPortugues", eleccion.getLinkPortuguese()));
			add(new Label("tituloEspanol", eleccion.getTitleSpanish()));
			add(new Label("tituloIngles", eleccion.getTitleEnglish()));
			add(new Label("tituloPortugues", eleccion.getTitlePortuguese()));

			Label descripcionEspanol = new Label("descripcionEspanol", eleccion.getDescriptionSpanish());
			descripcionEspanol.setEscapeModelStrings(false);
			add(descripcionEspanol);
			Label descripcionIngles = new Label("descripcionIngles", eleccion.getDescriptionEnglish());
			descripcionIngles.setEscapeModelStrings(false);
			add(descripcionIngles);
			Label descripcionPortugues = new Label("descripcionPortugues", eleccion.getDescriptionPortuguese());
			descripcionPortugues.setEscapeModelStrings(false);
			add(descripcionPortugues);

			add(new Label("maximoCandidatos", String.valueOf(eleccion.getMaxCandidate())));
			add(new Label("diffUTC", String.valueOf(eleccion.getDiffUTC())));
			add(new Label("fechaCreacion", new SimpleDateFormat("dd/MM/yyyy").format(eleccion.getCreationDate())));
			add(new Label("fechaInicio", eleccion.getAuxStartDate() + " " + eleccion.getAuxStartHour() + " (UTC)"));
			add(new Label("fechaFin", eleccion.getAuxEndDate() + " " + eleccion.getAuxEndHour() + " (UTC)"));
			add(new Label("habilitadoLinkVotacion", eleccion.isVotingLinkAvailable() ? "SI" : "NO"));
			add(new Label("habilitadoLinkResultado", eleccion.isResultLinkAvailable() ? "SI" : "NO"));
			add(new Label("linkResultado", AppContext.getInstance().getManagerBeanRemote().obtenerLinkresultado(eleccion)));

			add(new BookmarkablePageLink<Void>("editarElec", DashboardGestionEleccion.class, UtilsParameters.getId(eleccion.getIdElection())));

			add(new AjaxLazyLoadPanel<ListCandidatosEleccionPanel>("listaCandidatosPanel") {
				private static final long serialVersionUID = 6513156554118602169L;

				@Override
				public ListCandidatosEleccionPanel getLazyLoadComponent(String markupId) {
					return new ListCandidatosEleccionPanel(markupId, eleccion.getIdElection());
				}
			});

			add(new BookmarkablePageLink<Void>("editarCandidatos", DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdElection())));

			add(new AjaxLazyLoadPanel<ListaAuditoresPanel>("auditoresListPanel") {
				private static final long serialVersionUID = -8684993569281131596L;

				@Override
				public ListaAuditoresPanel getLazyLoadComponent(String markupId) {
					return new ListaAuditoresPanel(markupId, eleccion.getIdElection());
				}
			});
			add(new BookmarkablePageLink<Void>("editarAuditores", DashboardGestionAuditores.class, UtilsParameters.getId(eleccion.getIdElection())));

			add(new AjaxLazyLoadPanel<ListUsuariosPadronEleccionPanel>("usuariosPadron") {
				private static final long serialVersionUID = -5066564828514741892L;

				@Override
				public ListUsuariosPadronEleccionPanel getLazyLoadComponent(String markupId) {
					return new ListUsuariosPadronEleccionPanel(markupId, eleccion);
				}
			});
			add(new BookmarkablePageLink<Void>("editarPadron", DashboardGestionPadron.class, UtilsParameters.getId(eleccion.getIdElection())));

			add(new AjaxLazyLoadPanel<ListaActividadesPanel>("listadoActividadesEleccion") {
				private static final long serialVersionUID = 5350609383247662704L;

				@Override
				public ListaActividadesPanel getLazyLoadComponent(String markupId) {
					return new ListaActividadesPanel(markupId, eleccion.getIdElection());
				}
			});

			add(new AjaxLazyLoadPanel<ListaMensajesPanel>("listadoEmails") {
				private static final long serialVersionUID = -6326434661632018604L;

				@Override
				public ListaMensajesPanel getLazyLoadComponent(String markupId) {
					return new ListaMensajesPanel(markupId, eleccion.getIdElection(), sent);
				}
			});

			Link<Void> atras = new Link<Void>("atras") {
				private static final long serialVersionUID = -5761950650383408715L;

				@Override
				public void onClick() {
					try {
						setResponsePage(DashboardHomePage.class);
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(atras);

		} catch (Exception e) {
			appLogger.error(e);
		}

	}

}