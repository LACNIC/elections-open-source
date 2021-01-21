package net.lacnic.siselecciones.admin.web.panel.candidatos;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.dominio.Candidato;

public class ListCandidatosEleccionPanel extends Panel {

	private static final long serialVersionUID = -1239455534678268981L;

	public ListCandidatosEleccionPanel(String id, long idEleccion) {
		super(id);
		ListView<Candidato> candidatosDataView = new ListView<Candidato>("listaCandidatosPanel", Contexto.getInstance().getManagerBeanRemote().obtenerCandidatosEleccionOrdenados(idEleccion)) {

			private static final long serialVersionUID = 4017591177604969632L;

			@Override
			protected void populateItem(final ListItem<Candidato> item) {
				final Candidato actual = item.getModelObject();
				item.add(new CandidatoDetallePanel("candidato", actual));

			}
		};
		add(candidatosDataView);
	}
}