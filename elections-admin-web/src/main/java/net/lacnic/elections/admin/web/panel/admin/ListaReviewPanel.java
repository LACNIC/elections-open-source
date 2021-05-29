package net.lacnic.elections.admin.web.panel.admin;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.domain.Vote;

public class ListaReviewPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	public ListaReviewPanel(String id, Long idEleccion) {
		super(id);
		try {
			List<Vote> votos = AppContext.getInstance().getManagerBeanRemote().obtenerVotos(idEleccion);
			final ListView<Vote> dataViewMensajes = new ListView<Vote>("listaVotos", votos) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Vote> item) {
					final Vote v = item.getModelObject();
					try {
						item.add(new Label("idV", v.getIdVote()));
						item.add(new Label("idUP", v.getUserVote().getIdUserVoter()));
						item.add(new Label("idC", v.getCandidate().getIdCandidate()));
						item.add(new Label("instanteVoto", v.getVoteDate()));
						item.add(new Label("instanteUP", v.getUserVote().getVoteDate()));
						item.add(new Label("candidato", v.getCandidate().getName()));
						item.add(new Label("votanteNombre", v.getUserVote().getName()));
						item.add(new Label("votanteEmail", v.getUserVote().getMail()));
						item.add(new Label("votantePais", v.getUserVote().getCountry()));
						item.add(new Label("votanteOrgId", v.getUserVote().getOrgID()));
						item.add(new Label("votanteIdioma", v.getUserVote().getLanguage()));
						item.add(new Label("votanteCantidad", v.getUserVote().getVoteAmount()));
						item.add(new Label("votanteToken", v.getUserVote().getVoteToken()));
						item.add(new Label("codigo", v.getCode()));
						item.add(new Label("ip", v.getIp()));

					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(dataViewMensajes);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}

}