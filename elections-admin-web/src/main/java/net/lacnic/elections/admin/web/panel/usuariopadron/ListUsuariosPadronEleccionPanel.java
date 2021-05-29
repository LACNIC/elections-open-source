package net.lacnic.elections.admin.web.panel.usuariopadron;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.UtilsLinks;

public class ListUsuariosPadronEleccionPanel extends Panel {

	private static final long serialVersionUID = -1239455534678268981L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ListUsuariosPadronEleccionPanel(String id, Election e) {
		super(id);
		List<UserVoter> usuariosPadron = AppContext.getInstance().getManagerBeanRemote().obtenerUsuariosPadron(e.getIdElection());

		final ListView<UserVoter> usuariosPadronView = new ListView<UserVoter>("usuarioPadronList", usuariosPadron) {

			private static final long serialVersionUID = -1250986391822198393L;

			@Override
			protected void populateItem(final ListItem<UserVoter> item) {
				try {
					final UserVoter actual = item.getModelObject();

					item.add(new Label("idioma", actual.getLanguage()));
					item.add(new Label("nombre", actual.getName()));
					item.add(new Label("mail", actual.getMail()));
					item.add(new Label("cantidadVotos", String.valueOf(actual.getVoteAmount())));
					item.add(new Label("pais", actual.getCountry()));
					item.add(new Label("orgId", actual.getOrgID()));

					final Label urlLinkVotacionUsuario = new Label("urlLinkVotacionUsuario", UtilsLinks.calcularLinkVotar(actual.getVoteToken()));
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
