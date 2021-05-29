package net.lacnic.elections.admin.web.elecciones;

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

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarCandidato;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionCandidatos;
import net.lacnic.elections.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.elections.admin.web.commons.OnOffSwitch;
import net.lacnic.elections.admin.wicket.util.ImageResource;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.utils.Constants;

public class ListaEdicionCandidatosPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ListaEdicionCandidatosPanel(String id, Election eleccion) {
		super(id);
		try {

			add(new OnOffSwitch("aleatoriamente", new PropertyModel<>(eleccion, "candidatosAleatorios")) {

				private static final long serialVersionUID = -4511634522624852162L;

				@Override
				protected void accion() {
					AppContext.getInstance().getManagerBeanRemote().ordenarCandidatosAleatoriamente(eleccion.getIdElection(), eleccion.isRandomOrderCandidates());
					setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdElection()));
				}

			});
			ListView<Candidate> candidatosDataView = new ListView<Candidate>("candidatosList", AppContext.getInstance().getManagerBeanRemote().obtenerCandidatosEleccionOrdenados(eleccion.getIdElection())) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(final ListItem<Candidate> item) {
					final Candidate actual = item.getModelObject();
					item.add(new BotonConConfirmacionEliminar("eliminar",item.getIndex()) {

						private static final long serialVersionUID = 542913566518615150L;

						@Override
						public void onConfirmar() {

							AppContext.getInstance().getManagerBeanRemote().eliminarCandidato(actual.getIdCandidate(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
							getSession().info(getString("candidateManagemenListExitoDel"));
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdElection()));
						}

					});

					Link<Void> editar = new Link<Void>("editar") {

						private static final long serialVersionUID = 8751663222625085852L;

						@Override
						public void onClick() {
							setResponsePage(DashboardEditarCandidato.class, UtilsParameters.getCandidate(actual.getIdCandidate()));
						}

					};
					editar.setMarkupId("editarCandidato"+item.getIndex());
					item.add(editar);

					Link<Void> eliminarFijar = new Link<Void>("eliminarFijar") {

						private static final long serialVersionUID = 3876001444790583642L;

						@Override
						public void onClick() {
							AppContext.getInstance().getManagerBeanRemote().nofijarCandidatoAlPrincipio(actual.getIdCandidate());
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdElection()));
						}

					};
					item.add(eliminarFijar);

					int orden = actual.getCandidateOrder();
					Label label = new Label("statusOrden", "FIJO - Posición: " + item.getIndex());

					if (eleccion.isRandomOrderCandidates()) {
						label = new Label("statusOrden", "Aleatorio");
					}
					if (orden == Constants.MAX_ORDER) {
						label = new Label("statusOrden", "FIJO - Primero");
						label.add(new AttributeModifier("class", "label label-success"));
					}
					if (orden == Constants.MIN_ORDER) {
						label = new Label("statusOrden", "FIJO - Último");
						label.add(new AttributeModifier("class", "label label-danger"));
					}
					label.add(new AttributeModifier("title", "DEV info: " + orden));
					item.add(label);

					eliminarFijar.setVisible(orden == Constants.MAX_ORDER || orden == Constants.MIN_ORDER);

					item.add(new Label("nombre", actual.getName()));
					Label bio = new Label("bio", actual.getBioSpanish());
					bio.setEscapeModelStrings(false);
					item.add(bio);
					
					String linkTexto = actual.getLinkSpanish();
					ExternalLink externalLink = new ExternalLink("link", linkTexto);
					externalLink.setVisible(linkTexto != null && !linkTexto.isEmpty());
					externalLink.add(new Label("linkTexto", linkTexto));
					item.add(externalLink);
					
					item.add(new NonCachingImage("foto", new ImageResource(actual.getPictureInfo(), actual.getPictureExtension())));

					item.add(new Link<Void>("fijarArriba") {

						private static final long serialVersionUID = 1827814978899855817L;

						@Override
						public void onClick() {

							AppContext.getInstance().getManagerBeanRemote().fijarCandidatoAlPrincipio(actual.getIdCandidate());
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdElection()));
						}
					});

					Candidate candidatoDeArriba = AppContext.getInstance().getManagerBeanRemote().obtenerCandidatoDEArriba(actual);
					Candidate candidatoDeAbajo = AppContext.getInstance().getManagerBeanRemote().obtenerCandidatoDEAbajo(actual);

					Link<Void> subir = new Link<Void>("subir") {

						private static final long serialVersionUID = 2854501115609501257L;

						@Override
						public void onClick() {

							AppContext.getInstance().getManagerBeanRemote().subirCandidato(actual.getIdCandidate());
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdElection()));
						}
					};
					subir.setVisible(!eleccion.isRandomOrderCandidates() && !actual.isFixed() && candidatoDeArriba != null && candidatoDeArriba.getCandidateOrder() != Constants.MAX_ORDER);
					item.add(subir);

					Link<Void> bajar = new Link<Void>("bajar") {

						private static final long serialVersionUID = 86645897213313910L;

						@Override
						public void onClick() {

							AppContext.getInstance().getManagerBeanRemote().bajarCandidato(actual.getIdCandidate());
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdElection()));
						}
					};
					bajar.setVisible(!eleccion.isRandomOrderCandidates() && !actual.isFixed() && candidatoDeAbajo != null && candidatoDeAbajo.getCandidateOrder() != Constants.MIN_ORDER);
					item.add(bajar);

					item.add(new Link<Void>("fijarAbajo") {

						private static final long serialVersionUID = -4002690678342538186L;

						@Override
						public void onClick() {
							AppContext.getInstance().getManagerBeanRemote().fijarCandidatoAlFinal(actual.getIdCandidate());
							setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdElection()));
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