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
			List<Vote> votos = AppContext.getInstance().getManagerBeanRemote().getElectionVotes(idEleccion);
			final ListView<Vote> dataViewMensajes = new ListView<Vote>("listaVotos", votos) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Vote> item) {
					final Vote v = item.getModelObject();
					try {
						item.add(new Label("idV", v.getVoteId()));
						item.add(new Label("idUP", v.getUserVoter().getUserVoterId()));
						item.add(new Label("idC", v.getCandidate().getCandidateId()));
						item.add(new Label("instanteVoto", v.getVoteDate()));
						item.add(new Label("instanteUP", v.getUserVoter().getVoteDate()));
						item.add(new Label("candidato", v.getCandidate().getName()));
						item.add(new Label("votanteNombre", v.getUserVoter().getName()));
						item.add(new Label("votanteEmail", v.getUserVoter().getMail()));
						item.add(new Label("votantePais", v.getUserVoter().getCountry()));
						item.add(new Label("votanteOrgId", v.getUserVoter().getOrgID()));
						item.add(new Label("votanteIdioma", v.getUserVoter().getLanguage()));
						item.add(new Label("votanteCantidad", v.getUserVoter().getVoteAmount()));
						item.add(new Label("votanteToken", v.getUserVoter().getVoteToken()));
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