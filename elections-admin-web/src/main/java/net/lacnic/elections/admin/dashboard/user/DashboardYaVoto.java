package net.lacnic.elections.admin.dashboard.user;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.dashboard.error.Error404;
import net.lacnic.elections.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.elections.domain.UserVoter;

public class DashboardYaVoto extends DashboardPublicBasePage {

	private static final long serialVersionUID = -7536199173314577391L;
	
	private UserVoter upd;

	public DashboardYaVoto(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedbackPanel"));
		getSession().info(getString("titulo_yv"));

		add(new Label("titulo", upd.getElection().getTitle(getIdioma())));
		add(new Label("votante", upd.getVoterInformation()));
		add(new Label("maximo", String.valueOf(upd.getElection().getMaxCandidate())));
		add(new Label("cantidadVotos", upd.getVoteAmount()));

		Label desc = new Label("descripcion", upd.getElection().getDescription(getIdioma()));
		desc.setEscapeModelStrings(false);
		add(desc);

		List<Object[]> elegidos = AppContext.getInstance().getVoterBeanRemote().obtenerCandidatosVotacion(upd.getIdUserVoter(), upd.getElection().getIdElection());
		final ListView<Object[]> seleccionados = new ListView<Object[]>("codigos", elegidos) {
			private static final long serialVersionUID = 1786359392545666490L;			
			@Override
			protected void populateItem(final ListItem<Object[]> item) {
				final Object[] actual = item.getModelObject();				
				int nroFila = item.getIndex() + 1;				
				item.add(new Label("numero", String.valueOf(nroFila)));
				Label codigo = new Label("codigo", (String) actual[1]);
				codigo.setMarkupId("codigo" + item.getIndex());
				item.add(codigo);
				
			}
		};
		add(seleccionados);
	}

	@Override
	public Class validarToken(PageParameters params) {
		upd = AppContext.getInstance().getVoterBeanRemote().verificarAccesoUP(getToken());
		if (upd == null) {
			AppContext.getInstance().getVoterBeanRemote().intentoFallidoIp(getIP());
			return Error404.class;
		} else {
			setEleccion(upd.getElection());
		}
		return null;
	}

}
