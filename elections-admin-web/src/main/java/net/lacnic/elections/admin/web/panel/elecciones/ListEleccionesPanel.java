package net.lacnic.elections.admin.web.panel.elecciones;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.admin.DashboardConfiguracion;
import net.lacnic.elections.admin.dashboard.admin.DashboardDetalleEleccion;
import net.lacnic.elections.admin.dashboard.admin.DashboardEstadisticas;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionAuditores;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionCandidatos;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionEleccion;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionPadron;
import net.lacnic.elections.admin.dashboard.admin.DashboardHomePage;
import net.lacnic.elections.admin.dashboard.admin.DashboardPlantillasVer;
import net.lacnic.elections.admin.dashboard.admin.DashboardReview;
import net.lacnic.elections.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

public class ListEleccionesPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private long idUsuario;

	public ListEleccionesPanel(String id, PageParameters pars) {
		super(id);
		List<Election> listaElecciones = new ArrayList<>();
		Long idEleccionAutorizado = SecurityUtils.getIdEleccionAutorizado();
		if (idEleccionAutorizado != 0) {
			listaElecciones = new ArrayList<>();
			listaElecciones.add(AppContext.getInstance().getManagerBeanRemote().obtenerEleccion(idEleccionAutorizado));
		} else {
			if (UtilsParameters.isAll(pars)) {
				listaElecciones = AppContext.getInstance().getManagerBeanRemote().obtenerEleccionesLight();
			} else {
				listaElecciones = AppContext.getInstance().getManagerBeanRemote().obtenerEleccionesLightEsteAnio();
			}
		}
		init(listaElecciones);
	}

	private void init(List<Election> listaElecciones) {
		try {

			setOutputMarkupPlaceholderTag(true);

			final ListView<Election> dataViewElecciones = new ListView<Election>("listaElecciones", listaElecciones) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Election> item) {
					final Election actual = item.getModelObject();
					try {
						Label titulos = new Label("titulos", actual.getTitleSpanish());
						titulos.setEscapeModelStrings(false);

						item.add(new Label("fechaCreacion", new SimpleDateFormat("dd/MM/yyyy").format(actual.getCreationDate())));
						item.add(new Label("fechaInicio", actual.getStartDateString()));
						item.add(new Label("fechaFin", actual.getEndDateString()));

						item.add(new BookmarkablePageLink<Void>("detalleEleccion", DashboardDetalleEleccion.class, UtilsParameters.getId(actual.getIdElection())).add(titulos));

						BookmarkablePageLink<Void> editarEleccion = new BookmarkablePageLink<>("editarEleccion", DashboardGestionEleccion.class, UtilsParameters.getId(actual.getIdElection()));
						editarEleccion.setMarkupId("editarEleccion" + actual.getIdElection());
						item.add(editarEleccion);

						BookmarkablePageLink<Void> padron = new BookmarkablePageLink<>("gestionPadron", DashboardGestionPadron.class, UtilsParameters.getId(actual.getIdElection()));
						padron.setMarkupId("padronEleccion" + actual.getIdElection());
						item.add(padron);

						BookmarkablePageLink<Void> candidatos = new BookmarkablePageLink<>("candidatos", DashboardGestionCandidatos.class, UtilsParameters.getId(actual.getIdElection()));
						candidatos.setMarkupId("candidatosEleccion" + actual.getIdElection());
						item.add(candidatos);

						BookmarkablePageLink<Void> auditores = new BookmarkablePageLink<>("auditores", DashboardGestionAuditores.class, UtilsParameters.getId(actual.getIdElection()));
						auditores.setMarkupId("auditoresEleccion" + actual.getIdElection());
						item.add(auditores);

						if (actual.isCandidatesSet())
							candidatos.add(new AttributeModifier("class", "btn-circle btn-primary btn-sm"));
						if (actual.isElectorsSet())
							padron.add(new AttributeModifier("class", "btn-circle btn-primary btn-sm"));
						if (actual.isAuditorsSet())
							auditores.add(new AttributeModifier("class", "btn-circle btn-primary btn-sm"));

						BotonConConfirmacionEliminar botonConConfirmacionEliminar = new BotonConConfirmacionEliminar("eliminarEleccion", actual.getIdElection()) {
							private static final long serialVersionUID = -2068256428165604654L;

							@Override
							public void onConfirmar() {
								try {
									boolean esNueva = true;
									boolean esSupra = false;
									// Valido si la eleccion esta junta a otra, entonces NO puedo modificar la fecha de inicio
									if (actual.getIdElection() == 0) {
										esNueva = true;
									} else {
										esNueva = false;
										esSupra = AppContext.getInstance().getManagerBeanRemote().isSupraEleccion(actual.getIdElection());
									};
									
									if ((!esNueva) && (esSupra)) {
										//error();
										SecurityUtils.error(getString("errorEliminoEleccionSupra"));
										setResponsePage(DashboardHomePage.class);
									} else {									
										AppContext.getInstance().getManagerBeanRemote().darDeBajaEleccion(actual.getIdElection(), actual.getTitleSpanish(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
										SecurityUtils.info(getString("eliminoEleccion"));
										setResponsePage(DashboardHomePage.class);
									}
								} catch (Exception e) {
									appLogger.error(e);
								}
							}
						};
						item.add(botonConConfirmacionEliminar);
						botonConConfirmacionEliminar.setVisible(SecurityUtils.getIdEleccionAutorizado() == 0);

						BookmarkablePageLink<Void> verEstadisticasEleccion = new BookmarkablePageLink<>("verStatsEleccion", DashboardEstadisticas.class, UtilsParameters.getId(actual.getIdElection()));
						verEstadisticasEleccion.setMarkupId("verEstadisticasEleccion" + actual.getIdElection());
						item.add(verEstadisticasEleccion);

						BookmarkablePageLink<Void> revisionEleccion = new BookmarkablePageLink<>("revision", DashboardReview.class, UtilsParameters.getId(actual.getIdElection()));
						revisionEleccion.setMarkupId("revisionEleccion" + actual.getIdElection());
						revisionEleccion.setVisible(actual.isRevisionRequest());
						item.add(revisionEleccion);

						BookmarkablePageLink<Void> gestionDeMailLink = new BookmarkablePageLink<>("gestionEmails", DashboardPlantillasVer.class, UtilsParameters.getId(actual.getIdElection()));
						gestionDeMailLink.setMarkupId("gestionDeMailLink" + actual.getIdElection());
						item.add(gestionDeMailLink); 

						BookmarkablePageLink<Void> configuracion = new BookmarkablePageLink<>("configuracion", DashboardConfiguracion.class, UtilsParameters.getId(actual.getIdElection()));
						configuracion.setMarkupId("configuracionEleccion" + actual.getIdElection());
						item.add(configuracion);
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(dataViewElecciones);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}

	public long getIdUsuario() {
		return idUsuario;
	}
}