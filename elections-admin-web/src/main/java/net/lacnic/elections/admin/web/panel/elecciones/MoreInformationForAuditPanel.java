package net.lacnic.elections.admin.web.panel.elecciones;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.data.ResultDetailData;
import net.lacnic.elections.data.ElectionsResultsData;

public class MoreInformationForAuditPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public MoreInformationForAuditPanel(String id, final long idEleccion) {
		super(id);
		try {

			ElectionsResultsData resultadoEleccionesData = AppContext.getInstance().getVoterBeanRemote().getElectionsResultsData(idEleccion);
			final int max = resultadoEleccionesData.getMax();
			add(new Label("votosMax", " (1 - " + max + ")"));

			final ListView<ResultDetailData> dataViewCandidatos = new ListView<ResultDetailData>("dataViewPaticipantes", resultadoEleccionesData.getResultDetailData()) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<ResultDetailData> item) {
					final ResultDetailData actual = item.getModelObject();

					item.add(new Label("porcentaje", actual.getPercentageWithSymbol()));
					item.add(new Label("habilitados", String.valueOf(actual.getEnabled())));
					item.add(new Label("participantes", String.valueOf(actual.getParticipants())));
					item.add(new Label("peso", String.valueOf(actual.getWeight())));
					item.add(new Label("total", actual.getTotal(max)));

				}
			};
			add(dataViewCandidatos);
			add(new Label("porcentajeTotal", String.valueOf(resultadoEleccionesData.getTotalPercentageWithSymbol())));
			add(new Label("habilitadosTotal", String.valueOf(resultadoEleccionesData.getTotalEnabled())));
			add(new Label("participantesTotal", String.valueOf(resultadoEleccionesData.getTotalParticipants())));
			add(new Label("totalTotal", String.valueOf(resultadoEleccionesData.getTotalTotalPossible())));

		} catch (Exception e) {
			appLogger.error(e);
		}
	}
}