package net.lacnic.siselecciones.admin.web.panel.elecciones;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.data.DetalleResultadoData;
import net.lacnic.siselecciones.data.ResultadoEleccionesData;

public class MoreInformationForAuditPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public MoreInformationForAuditPanel(String id, final long idEleccion) {
		super(id);
		try {

			ResultadoEleccionesData resultadoEleccionesData = Contexto.getInstance().getVotanteBeanRemote().obtenerResultadoEleccionesData(idEleccion);
			final int max = resultadoEleccionesData.getMax();
			add(new Label("votosMax", " (1 - " + max + ")"));

			final ListView<DetalleResultadoData> dataViewCandidatos = new ListView<DetalleResultadoData>("dataViewPaticipantes", resultadoEleccionesData.getDetalleResultadoData()) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<DetalleResultadoData> item) {
					final DetalleResultadoData actual = item.getModelObject();

					item.add(new Label("porcentaje", actual.getPorcentajeConSimbolo()));
					item.add(new Label("habilitados", String.valueOf(actual.getHabilitados())));
					item.add(new Label("participantes", String.valueOf(actual.getParticipantes())));
					item.add(new Label("peso", String.valueOf(actual.getPeso())));
					item.add(new Label("total", actual.getTotal(max)));

				}
			};
			add(dataViewCandidatos);
			add(new Label("porcentajeTotal", String.valueOf(resultadoEleccionesData.getPorcentajeTotalConSimbolo())));
			add(new Label("habilitadosTotal", String.valueOf(resultadoEleccionesData.getHabilitadosTotal())));
			add(new Label("participantesTotal", String.valueOf(resultadoEleccionesData.getParticipantesTotal())));
			add(new Label("totalTotal", String.valueOf(resultadoEleccionesData.getTotalTotalPosilidades())));

		} catch (Exception e) {
			appLogger.error(e);
		}
	}
}