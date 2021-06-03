package net.lacnic.elections.adminweb.panel.user;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.dashboard.user.DashboardVotarEleccionSimple;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;

public class DetalleVoteEleccionJuntasPanel extends Panel {

	private static final long serialVersionUID = 5505752645083978888L;
	private int numero = 1;

	public DetalleVoteEleccionJuntasPanel(String id, PageParameters parms, Election eleccion, UserVoter upd, boolean leftPanel) {
		super(id);
		WebMarkupContainer yaVoto = new WebMarkupContainer("yavoto");
		yaVoto.setVisible(upd.isVoted());
		add(yaVoto);
		WebMarkupContainer noVoto = new WebMarkupContainer("novoto");
		noVoto.setVisible(!yaVoto.isVisible());
		add(noVoto);

		add(new Label("tituloHeader", leftPanel ? getString("tituloDetalleEleccion_v1") : getString("tituloDetalleEleccion_v2")));
		add(new Label("titulo", eleccion.getTitle(getIdioma(upd))));
		add(new Label("votante", upd.getVoterInformation()));
		add(new Label("cantidadVotos", upd.getVoteAmount()));

		add(new Label("maximo", String.valueOf(eleccion.getMaxCandidates())));
		Label desc = new Label("descripcion", eleccion.getDescription(getIdioma(upd)));
		desc.setEscapeModelStrings(false);
		add(desc);

		Link<Void> votar = new Link<Void>("votar") {
			private static final long serialVersionUID = 4499783887456801848L;

			public void onClick() {
				if (leftPanel) {
					parms.add("token1", upd.getVoteToken());
					parms.add("token2", "");
				} else {
					parms.add("token2", upd.getVoteToken());
					parms.add("token1", "");
				}

				setResponsePage(DashboardVotarEleccionSimple.class, parms);
			}

		};

		votar.setVisible(!yaVoto.isVisible());
		add(votar);
		List<Object[]> elegidos = AppContext.getInstance().getVoterBeanRemote().getElectionVotesCandidateForUserVoter(upd.getUserVoterId(), upd.getElection().getElectionId());

		WebMarkupContainer yavotoCodigo = new WebMarkupContainer("yavotoCodigo");
		yavotoCodigo.setVisible(!elegidos.isEmpty() && yaVoto.isVisible());
		add(yavotoCodigo);

		final ListView<Object[]> seleccionados = new ListView<Object[]>("codigos", elegidos) {
			private static final long serialVersionUID = 1786359392545666490L;

			@Override
			protected void populateItem(final ListItem<Object[]> item) {
				final Object[] actual = item.getModelObject();
				item.add(new Label("numero", String.valueOf(numero)));
				Label codigo = new Label("codigo", (String) actual[1]);
				codigo.setMarkupId("codigo" + item.getIndex());
				item.add(codigo);
				numero = numero + 1;
			}
		};
		yavotoCodigo.add(seleccionados);
	}

	public String getIdioma(UserVoter upd) {
		if (upd != null)
			return upd.getLanguage();
		else
			return SecurityUtils.getLocale().getDisplayName();
	}

}
