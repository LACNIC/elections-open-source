package net.lacnic.elections.admin.dashboard.admin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.googlecode.wickedcharts.wicket7.JavaScriptResourceRegistry;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.estadisticas.GraficaVotantesPanel;
import net.lacnic.elections.admin.web.panel.elecciones.CodigosCandidatoPanel;
import net.lacnic.elections.admin.web.panel.elecciones.ResultadoEleccionPanel;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardEstadisticas extends DashboardAdminBasePage {

	private static final long serialVersionUID = 2304496268074384354L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	@Override
	protected void onInitialize() {
		super.onInitialize();
		String highcharts = "https://code.highcharts.com/highcharts.js";
		String exporting = "https://code.highcharts.com/modules/exporting.js";
		JavaScriptResourceRegistry.getInstance().setHighchartsReference(highcharts);
		JavaScriptResourceRegistry.getInstance().setHighchartsExportingReference(exporting);
	}


	public DashboardEstadisticas(PageParameters params) {
		super(params);
		try {
			Election eleccion = AppContext.getInstance().getManagerBeanRemote().getElection(UtilsParameters.getIdAsLong(params));
			add(new ResultadoEleccionPanel("resultadoPanel", eleccion.getElectionId()).setVisible(eleccion.isFinished()));
			add(new CodigosCandidatoPanel("codigosCandidatoPanel", eleccion.getElectionId()).setVisible(eleccion.isFinished()));
			add(new GraficaVotantesPanel("graficaPanel", eleccion.getElectionId()));
			add(new Label("tituloEleccion", eleccion.getTitleSpanish()));
			add(new Label("mensaje", getString("dshbStatsMessage")).setVisible(!eleccion.isFinished()));
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}