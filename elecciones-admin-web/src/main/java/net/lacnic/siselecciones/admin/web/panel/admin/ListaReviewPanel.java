package net.lacnic.siselecciones.admin.web.panel.admin;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.dominio.Voto;

public class ListaReviewPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	public ListaReviewPanel(String id, Long idEleccion) {
		super(id);
		try {
			List<Voto> votos = Contexto.getInstance().getManagerBeanRemote().obtenerVotos(idEleccion);
			final ListView<Voto> dataViewMensajes = new ListView<Voto>("listaVotos", votos) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Voto> item) {
					final Voto v = item.getModelObject();
					try {
						item.add(new Label("idV", v.getIdVoto()));
						item.add(new Label("idUP", v.getUsuarioPadron().getIdUsuarioPadron()));
						item.add(new Label("idC", v.getCandidato().getIdCandidato()));
						item.add(new Label("instanteVoto", v.getFechaVoto()));
						item.add(new Label("instanteUP", v.getUsuarioPadron().getFechaVoto()));
						item.add(new Label("candidato", v.getCandidato().getNombre()));
						item.add(new Label("votanteNombre", v.getUsuarioPadron().getNombre()));
						item.add(new Label("votanteEmail", v.getUsuarioPadron().getMail()));
						item.add(new Label("votantePais", v.getUsuarioPadron().getPais()));
						item.add(new Label("votanteOrgId", v.getUsuarioPadron().getOrgID()));
						item.add(new Label("votanteIdioma", v.getUsuarioPadron().getIdioma()));
						item.add(new Label("votanteCantidad", v.getUsuarioPadron().getCantVotos()));
						item.add(new Label("votanteToken", v.getUsuarioPadron().getTokenVotacion()));
						item.add(new Label("codigo", v.getCodigo()));
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