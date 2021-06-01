package net.lacnic.elections.admin.web.panel.candidatos;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.domain.Candidate;

public class ListCandidatosEleccionPanel extends Panel {

	private static final long serialVersionUID = -1239455534678268981L;

	public ListCandidatosEleccionPanel(String id, long idEleccion) {
		super(id);
		ListView<Candidate> candidatosDataView = new ListView<Candidate>("listaCandidatosPanel", AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(idEleccion)) {

			private static final long serialVersionUID = 4017591177604969632L;

			@Override
			protected void populateItem(final ListItem<Candidate> item) {
				final Candidate actual = item.getModelObject();
				item.add(new CandidatoDetallePanel("candidato", actual));

			}
		};
		add(candidatosDataView);
	}
}