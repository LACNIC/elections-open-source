package net.lacnic.siselecciones.admin.web.panel.elecciones;

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

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardConfiguracion;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardDetalleEleccion;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardEstadisticas;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionAuditores;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionCandidatos;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionEleccion;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionPadron;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardHomePage;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardPlantillasVer;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardReview;
import net.lacnic.siselecciones.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Eleccion;

public class ListEleccionesPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private long idUsuario;

	public ListEleccionesPanel(String id, PageParameters pars) {
		super(id);
		List<Eleccion> listaElecciones = new ArrayList<>();
		Long idEleccionAutorizado = SecurityUtils.getIdEleccionAutorizado();
		if (idEleccionAutorizado != 0) {
			listaElecciones = new ArrayList<>();
			listaElecciones.add(Contexto.getInstance().getManagerBeanRemote().obtenerEleccion(idEleccionAutorizado));
		} else {
			if (UtilsParameters.isAll(pars)) {
				listaElecciones = Contexto.getInstance().getManagerBeanRemote().obtenerEleccionesLight();
			} else {
				listaElecciones = Contexto.getInstance().getManagerBeanRemote().obtenerEleccionesLightEsteAnio();
			}
		}
		init(listaElecciones);
	}

	private void init(List<Eleccion> listaElecciones) {
		try {

			setOutputMarkupPlaceholderTag(true);

			final ListView<Eleccion> dataViewElecciones = new ListView<Eleccion>("listaElecciones", listaElecciones) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Eleccion> item) {
					final Eleccion actual = item.getModelObject();
					try {
						Label titulos = new Label("titulos", actual.getTituloEspanol());
						titulos.setEscapeModelStrings(false);

						item.add(new Label("fechaCreacion", new SimpleDateFormat("dd/MM/yyyy").format(actual.getFechaCreacion())));
						item.add(new Label("fechaInicio", actual.getFechaInicioString()));
						item.add(new Label("fechaFin", actual.getFechaFinString()));

						item.add(new BookmarkablePageLink<Void>("detalleEleccion", DashboardDetalleEleccion.class, UtilsParameters.getId(actual.getIdEleccion())).add(titulos));

						BookmarkablePageLink<Void> editarEleccion = new BookmarkablePageLink<>("editarEleccion", DashboardGestionEleccion.class, UtilsParameters.getId(actual.getIdEleccion()));
						editarEleccion.setMarkupId("editarEleccion" + actual.getIdEleccion());
						item.add(editarEleccion);

						BookmarkablePageLink<Void> padron = new BookmarkablePageLink<>("gestionPadron", DashboardGestionPadron.class, UtilsParameters.getId(actual.getIdEleccion()));
						padron.setMarkupId("padronEleccion" + actual.getIdEleccion());
						item.add(padron);

						BookmarkablePageLink<Void> candidatos = new BookmarkablePageLink<>("candidatos", DashboardGestionCandidatos.class, UtilsParameters.getId(actual.getIdEleccion()));
						candidatos.setMarkupId("candidatosEleccion" + actual.getIdEleccion());
						item.add(candidatos);

						BookmarkablePageLink<Void> auditores = new BookmarkablePageLink<>("auditores", DashboardGestionAuditores.class, UtilsParameters.getId(actual.getIdEleccion()));
						auditores.setMarkupId("auditoresEleccion" + actual.getIdEleccion());
						item.add(auditores);

						if (actual.isCandidatosSeteado())
							candidatos.add(new AttributeModifier("class", "btn-circle btn-primary btn-sm"));
						if (actual.isPadronSeteado())
							padron.add(new AttributeModifier("class", "btn-circle btn-primary btn-sm"));
						if (actual.isAuditoresSeteado())
							auditores.add(new AttributeModifier("class", "btn-circle btn-primary btn-sm"));

						BotonConConfirmacionEliminar botonConConfirmacionEliminar = new BotonConConfirmacionEliminar("eliminarEleccion", actual.getIdEleccion()) {
							private static final long serialVersionUID = -2068256428165604654L;

							@Override
							public void onConfirmar() {
								try {
									boolean esNueva = true;
									boolean esSupra = false;
									// Valido si la eleccion esta junta a otra, entonces NO puedo modificar la fecha de inicio
									if (actual.getIdEleccion() == 0) {
										esNueva = true;
									} else {
										esNueva = false;
										esSupra = Contexto.getInstance().getManagerBeanRemote().isSupraEleccion(actual.getIdEleccion());
									};
									
									if ((!esNueva) && (esSupra)) {
										//error();
										SecurityUtils.error(getString("errorEliminoEleccionSupra"));
										setResponsePage(DashboardHomePage.class);
									} else {									
										Contexto.getInstance().getManagerBeanRemote().darDeBajaEleccion(actual.getIdEleccion(), actual.getTituloEspanol(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
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

						BookmarkablePageLink<Void> verEstadisticasEleccion = new BookmarkablePageLink<>("verStatsEleccion", DashboardEstadisticas.class, UtilsParameters.getId(actual.getIdEleccion()));
						verEstadisticasEleccion.setMarkupId("verEstadisticasEleccion" + actual.getIdEleccion());
						item.add(verEstadisticasEleccion);

						BookmarkablePageLink<Void> revisionEleccion = new BookmarkablePageLink<>("revision", DashboardReview.class, UtilsParameters.getId(actual.getIdEleccion()));
						revisionEleccion.setMarkupId("revisionEleccion" + actual.getIdEleccion());
						revisionEleccion.setVisible(actual.isSolicitarRevision());
						item.add(revisionEleccion);

						BookmarkablePageLink<Void> gestionDeMailLink = new BookmarkablePageLink<>("gestionEmails", DashboardPlantillasVer.class, UtilsParameters.getId(actual.getIdEleccion()));
						gestionDeMailLink.setMarkupId("gestionDeMailLink" + actual.getIdEleccion());
						item.add(gestionDeMailLink); 

						BookmarkablePageLink<Void> configuracion = new BookmarkablePageLink<>("configuracion", DashboardConfiguracion.class, UtilsParameters.getId(actual.getIdEleccion()));
						configuracion.setMarkupId("configuracionEleccion" + actual.getIdEleccion());
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