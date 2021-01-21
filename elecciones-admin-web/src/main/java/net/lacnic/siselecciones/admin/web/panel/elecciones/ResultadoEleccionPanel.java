package net.lacnic.siselecciones.admin.web.panel.elecciones;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.dominio.Candidato;

public class ResultadoEleccionPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ResultadoEleccionPanel(String id, final long idEleccion) {
		super(id);
		try {
			List<Candidato> listaCandidatos = Contexto.getInstance().getVotanteBeanRemote().obtenerCandidatosEleccionConVotos(idEleccion);
			Collections.sort(listaCandidatos, new Comparator<Candidato>() {
				@Override
				public int compare(Candidato o1, Candidato o2) {
					try {
						return Contexto.getInstance().getVotanteBeanRemote().obtenerVotosCandidato(o1.getIdCandidato()) > Contexto.getInstance().getVotanteBeanRemote().obtenerVotosCandidato(o2.getIdCandidato()) ? -1 : 1;
					} catch (Exception e) {
						return 0;
					}
				}
			});

			final ListView<Candidato> dataViewCandidatos = new ListView<Candidato>("dataViewCandidatos", listaCandidatos) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Candidato> item) {
					final Candidato c = item.getModelObject();
					try {
						Label nombreCandidato = new Label("nombre", c.getNombre());
						nombreCandidato.setMarkupId("nombreCandidato"+item.getIndex());
						item.add(nombreCandidato);
						Label votosCandidato = new Label("votos", String.valueOf(Contexto.getInstance().getVotanteBeanRemote().obtenerVotosCandidato(c.getIdCandidato())));
						votosCandidato.setMarkupId("votosCandidato"+item.getIndex());
						item.add(votosCandidato);
					} catch (Exception e) {
						appLogger.error(e);
					}
				}
			};
			add(dataViewCandidatos);
			add(new Label("totalVotos", String.valueOf(Contexto.getInstance().getVotanteBeanRemote().obtenerTotalVotosEleccion(idEleccion))));
			add(new Label("totalVotantes", String.valueOf(Contexto.getInstance().getVotanteBeanRemote().obtenerCantidadVotantesQueVotaronEleccion(idEleccion))));

		} catch (Exception e) {
			appLogger.error(e);
		}
	}
}