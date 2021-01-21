package net.lacnic.siselecciones.admin.web.panel.usuariopadron;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.dominio.UsuarioPadron;
import net.lacnic.siselecciones.utils.UtilsLinks;

public class ListUsuariosPadronEleccionPanel extends Panel {

	private static final long serialVersionUID = -1239455534678268981L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ListUsuariosPadronEleccionPanel(String id, Eleccion e) {
		super(id);
		List<UsuarioPadron> usuariosPadron = Contexto.getInstance().getManagerBeanRemote().obtenerUsuariosPadron(e.getIdEleccion());

		final ListView<UsuarioPadron> usuariosPadronView = new ListView<UsuarioPadron>("usuarioPadronList", usuariosPadron) {

			private static final long serialVersionUID = -1250986391822198393L;

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

					final Label urlLinkVotacionUsuario = new Label("urlLinkVotacionUsuario", UtilsLinks.calcularLinkVotar(actual.getTokenVotacion()));
					urlLinkVotacionUsuario.setOutputMarkupPlaceholderTag(true);
					item.add(urlLinkVotacionUsuario);

				} catch (Exception e) {
					appLogger.error(e);
				}
			}
		};
		add(usuariosPadronView);
	}

}
