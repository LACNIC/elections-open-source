package net.lacnic.siselecciones.admin.web.panel.elecciones;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;

public class CodigosCandidatoPanel extends Panel {
	private int numero = 1;

	public CodigosCandidatoPanel(long idEleccion) {
		this("codigosCandidatoPanel", idEleccion);
	}

	public CodigosCandidatoPanel(String id, long idEleccion) {
		super(id);
		final ListView<Object[]> comisionadosDataView = new ListView<Object[]>("codigos", Contexto.getInstance().getVotanteBeanRemote().obtenerCodigosdeVotacion(idEleccion)) {
			private static final long serialVersionUID = 1786359392545666490L;

			@Override
			protected void populateItem(final ListItem<Object[]> item) {
				final Object[] actual = item.getModelObject();

				item.add(new Label("numero", String.valueOf(numero)));
				item.add(new Label("candidato", (String) actual[0]));
				Label codigoC = new Label("codigo", (String) actual[1]);
				codigoC.setMarkupId("codigoCandidato"+item.getIndex());
				item.add(codigoC);
				numero = numero + 1;

			}
		};
		add(comisionadosDataView);
	}

	private static final long serialVersionUID = -7217245542954325281L;

}