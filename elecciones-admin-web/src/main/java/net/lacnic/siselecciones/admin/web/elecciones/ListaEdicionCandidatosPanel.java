package net.lacnic.siselecciones.admin.web.elecciones;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardEditarCandidato;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardGestionCandidatos;
import net.lacnic.siselecciones.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.siselecciones.admin.web.commons.OnOffSwitch;
import net.lacnic.siselecciones.admin.wicket.util.ImageResource;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Candidato;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.utils.Constantes;

public class ListaEdicionCandidatosPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ListaEdicionCandidatosPanel(String id, Eleccion eleccion) {
		super(id);
		try {

			add(new OnOffSwitch("aleatoriamente", new PropertyModel<>(eleccion, "candidatosAleatorios")) {

				private static final long serialVersionUID = -4511634522624852162L;

				@Override
				protected void accion() {
					Contexto.getInstance().getManagerBeanRemote().ordenarCandidatosAleatoriamente(eleccion.getIdEleccion(), eleccion.isCandidatosAleatorios());
					setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
				}

			});
			ListView<Candidato> candidatosDataView = new ListView<Candidato>("candidatosList", Contexto.getInstance().getManagerBeanRemote().obtenerCandidatosEleccionOrdenados(eleccion.getIdEleccion())) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(final ListItem<Candidato> item) {
					final Candidato actual = item.getModelObject();
					item.add(new BotonConConfirmacionEliminar("eliminar",item.getIndex()) {

						private static final long serialVersionUID = 542913566518615150L;

						@Override
						public void onConfirmar() {

							Contexto.getInstance().getManagerBeanRemote().eliminarCandidato(actual.getIdCandidato(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
							getSession().info(getString("candidateManagemenListExitoDel"));
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
						}

					});

					Link<Void> editar = new Link<Void>("editar") {

						private static final long serialVersionUID = 8751663222625085852L;

						@Override
						public void onClick() {
							setResponsePage(DashboardEditarCandidato.class, UtilsParameters.getCandidate(actual.getIdCandidato()));
						}

					};
					editar.setMarkupId("editarCandidato"+item.getIndex());
					item.add(editar);

					Link<Void> eliminarFijar = new Link<Void>("eliminarFijar") {

						private static final long serialVersionUID = 3876001444790583642L;

						@Override
						public void onClick() {
							Contexto.getInstance().getManagerBeanRemote().nofijarCandidatoAlPrincipio(actual.getIdCandidato());
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
						}

					};
					item.add(eliminarFijar);

					int orden = actual.getOrden();
					Label label = new Label("statusOrden", "FIJO - Posición: " + item.getIndex());

					if (eleccion.isCandidatosAleatorios()) {
						label = new Label("statusOrden", "Aleatorio");
					}
					if (orden == Constantes.ORDEN_MAXIMO) {
						label = new Label("statusOrden", "FIJO - Primero");
						label.add(new AttributeModifier("class", "label label-success"));
					}
					if (orden == Constantes.ORDEN_MINIMO) {
						label = new Label("statusOrden", "FIJO - Último");
						label.add(new AttributeModifier("class", "label label-danger"));
					}
					label.add(new AttributeModifier("title", "DEV info: " + orden));
					item.add(label);

					eliminarFijar.setVisible(orden == Constantes.ORDEN_MAXIMO || orden == Constantes.ORDEN_MINIMO);

					item.add(new Label("nombre", actual.getNombre()));
					Label bio = new Label("bio", actual.getBioEspanol());
					bio.setEscapeModelStrings(false);
					item.add(bio);
					
					String linkTexto = actual.getLinkEspanol();
					ExternalLink externalLink = new ExternalLink("link", linkTexto);
					externalLink.setVisible(linkTexto != null && !linkTexto.isEmpty());
					externalLink.add(new Label("linkTexto", linkTexto));
					item.add(externalLink);
					
					item.add(new NonCachingImage("foto", new ImageResource(actual.getContenidoFoto(), actual.getExtensionFoto())));

					item.add(new Link<Void>("fijarArriba") {

						private static final long serialVersionUID = 1827814978899855817L;

						@Override
						public void onClick() {

							Contexto.getInstance().getManagerBeanRemote().fijarCandidatoAlPrincipio(actual.getIdCandidato());
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
						}
					});

					Candidato candidatoDeArriba = Contexto.getInstance().getManagerBeanRemote().obtenerCandidatoDEArriba(actual);
					Candidato candidatoDeAbajo = Contexto.getInstance().getManagerBeanRemote().obtenerCandidatoDEAbajo(actual);

					Link<Void> subir = new Link<Void>("subir") {

						private static final long serialVersionUID = 2854501115609501257L;

						@Override
						public void onClick() {

							Contexto.getInstance().getManagerBeanRemote().subirCandidato(actual.getIdCandidato());
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
						}
					};
					subir.setVisible(!eleccion.isCandidatosAleatorios() && !actual.isFijo() && candidatoDeArriba != null && candidatoDeArriba.getOrden() != Constantes.ORDEN_MAXIMO);
					item.add(subir);

					Link<Void> bajar = new Link<Void>("bajar") {

						private static final long serialVersionUID = 86645897213313910L;

						@Override
						public void onClick() {

							Contexto.getInstance().getManagerBeanRemote().bajarCandidato(actual.getIdCandidato());
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
						}
					};
					bajar.setVisible(!eleccion.isCandidatosAleatorios() && !actual.isFijo() && candidatoDeAbajo != null && candidatoDeAbajo.getOrden() != Constantes.ORDEN_MINIMO);
					item.add(bajar);

					item.add(new Link<Void>("fijarAbajo") {

						private static final long serialVersionUID = -4002690678342538186L;

						@Override
						public void onClick() {
							Contexto.getInstance().getManagerBeanRemote().fijarCandidatoAlFinal(actual.getIdCandidato());
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
						}
					});
				}
			};
			add(candidatosDataView);

		} catch (Exception e) {
			appLogger.error(e);
		}
	}
}