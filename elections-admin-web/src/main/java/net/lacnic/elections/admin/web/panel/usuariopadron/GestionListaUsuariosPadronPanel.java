package net.lacnic.elections.admin.web.panel.usuariopadron;

import java.io.File;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarUsuarioPadron;
import net.lacnic.elections.admin.dashboard.admin.DashboardGestionPadron;
import net.lacnic.elections.admin.web.commons.BotonActualizarToken;
import net.lacnic.elections.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.elections.admin.web.commons.BotonReenviarEmailVotacion;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Eleccion;
import net.lacnic.elections.domain.UsuarioPadron;
import net.lacnic.elections.utils.UtilsLinks;

public class GestionListaUsuariosPadronPanel extends Panel {

	private static final long serialVersionUID = -1239455534678268981L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private List<UsuarioPadron> usuariosPadron;
	private File archivo;

	public GestionListaUsuariosPadronPanel(String id, Eleccion eleccion) {
		super(id);
		usuariosPadron = AppContext.getInstance().getManagerBeanRemote().obtenerUsuariosPadronEleccion(eleccion.getIdEleccion());

		add(new Label("cantidad", String.valueOf(usuariosPadron.size())));

		DownloadLink downloadLink = new DownloadLink("exportarPadron", new PropertyModel<>(GestionListaUsuariosPadronPanel.this, "archivo")) {

			private static final long serialVersionUID = 5415706945162526592L;

			@Override
			public void onClick() {
				setArchivo(AppContext.getInstance().getManagerBeanRemote().exportarPadronElectoral(eleccion.getIdEleccion()));
				super.onClick();
			}
		};
		add(downloadLink);
		downloadLink.setVisible(eleccion.isPadronSeteado());

		final ListView<UsuarioPadron> usuariosPadronDataView = new ListView<UsuarioPadron>("usuarioPadronList", usuariosPadron) {
			private static final long serialVersionUID = 1786359392545666490L;

			@Override
			protected void populateItem(final ListItem<UsuarioPadron> item) {
				try {
					final UsuarioPadron actual = item.getModelObject();

					item.add(new Label("idioma", actual.getIdioma()));
					item.add(new Label("nombre", actual.getNombre()));
					item.add(new Label("mail", actual.getMail()));
					item.add(new Label("cantidadVotos", String.valueOf(actual.getCantVotos())));
					item.add(new Label("pais", actual.getPais()));
					item.add(new Label("orgId", actual.getOrgID()));
					item.add(new Label("voto", (actual.isYaVoto() ? "SI" : "NO")));
					String calcularLinkVotar = UtilsLinks.calcularLinkVotar(actual.getTokenVotacion());
					Label textoLinkVotar = new Label("textoLinkVotar", calcularLinkVotar);
					ExternalLink linkvotar = new ExternalLink("linkVotar", calcularLinkVotar);
					linkvotar.add(textoLinkVotar);
					item.add(linkvotar);

					BotonActualizarToken botonActualizarToken = new BotonActualizarToken("actualizar") {

						private static final long serialVersionUID = 3609140813722818708L;

						@Override
						public void onConfirmar() {

							try {
								AppContext.getInstance().getManagerBeanRemote().actualizarTokenUsuarioPadron(actual.getIdUsuarioPadron(), actual.getNombre(), eleccion.getTituloEspanol(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
								getSession().info(getString("censusManagementUserListExitoToken"));
								setResponsePage(DashboardGestionPadron.class, UtilsParameters.getId(eleccion.getIdEleccion()));
							} catch (Exception e) {
								appLogger.error(e);
							}

						}
					};
					item.add(botonActualizarToken);

					BotonReenviarEmailVotacion botonReenviarEmailVotacion = new BotonReenviarEmailVotacion("enviarLink") {

						private static final long serialVersionUID = -4628772989608517427L;

						@Override
						public void onConfirmar() {

							try {
								AppContext.getInstance().getManagerBeanRemote().reenviarEmailPadron(actual, eleccion, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
								getSession().info(getString("censusManagementUserListExitoLink"));
								setResponsePage(DashboardGestionPadron.class, UtilsParameters.getId(eleccion.getIdEleccion()));
							} catch (Exception e) {
								appLogger.error(e);
							}

						}
					};
					item.add(botonReenviarEmailVotacion);

					BotonConConfirmacionEliminar botonConConfirmacionEliminar = new BotonConConfirmacionEliminar("eliminar",item.getIndex()) {

						private static final long serialVersionUID = -6583106894827434879L;

						@Override
						public void onConfirmar() {

							try {
								AppContext.getInstance().getManagerBeanRemote().eliminarUsuarioPadron(actual, eleccion.getTituloEspanol(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
								getSession().info(getString("censusManagementUserListExitoDelete"));
								setResponsePage(DashboardGestionPadron.class, UtilsParameters.getId(eleccion.getIdEleccion()));
							} catch (Exception e) {
								appLogger.error(e);
							}
						}
					};
					item.add(botonConConfirmacionEliminar);

					Link<Void> editarUsuarioPadron = new Link<Void>("editarUsuarioPadron") {

						private static final long serialVersionUID = 8268292966477896858L;

						@Override
						public void onClick() {
							setResponsePage(DashboardEditarUsuarioPadron.class, UtilsParameters.getUser(actual.getIdUsuarioPadron()));
						}

					};
					editarUsuarioPadron.setMarkupId("editarUsu"+item.getIndex());
					item.add(editarUsuarioPadron);

				} catch (Exception e) {
					appLogger.error(e);
				}
			}
		};
		add(usuariosPadronDataView);
	}

	public File getArchivo() {
		return archivo;
	}

	public void setArchivo(File archivo) {
		this.archivo = archivo;
	}
}