package net.lacnic.elections.adminweb.ui.admin.election.charts;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountryVotersGraphPanel extends Panel {

	private static final long serialVersionUID = -8927777065216923806L;
	private static final String CHART_ID = "countryVotersEvolutionChart";
	private static final String DRAW_FUNCTION = "drawCountryVotersEvolutionChart";
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private String rows;
	private String columns;
	private String ticks;

	public CountryVotersGraphPanel(String id, CountryParticipationStats stats) {
		super(id);

		Label noDataText = new Label("noData", new ResourceModel("dshbStatsNoData"));
		add(noDataText);

		WebMarkupContainer chartContainer = new WebMarkupContainer("chartContainer");
		add(chartContainer);

		if (stats == null || !stats.hasChartData()) {
			noDataText.setVisible(true);
			chartContainer.setVisible(false);
			return;
		}

		noDataText.setVisible(false);
		rows = CountryParticipationStats.buildChartRows(stats.getChartLabels(), stats.getChartSeries());
		columns = CountryParticipationStats.buildChartColumns(stats.getChartSeries());
		ticks = VotersGraphPanel.buildTicks(stats.getChartLabels());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		if (rows == null || columns == null || ticks == null) {
			appLogger.warn("CountryVotersGraphPanel - renderHead skipped because chart payload is missing");
			return;
		}

		response.render(JavaScriptHeaderItem.forUrl("https://www.gstatic.com/charts/loader.js"));
		response.render(JavaScriptHeaderItem.forScript(buildJs(), null));
	}

	private String buildJs() {
		String title = VotersGraphPanel.escapeJs(getString("dshbStatsByCountryTitle"));
		String axisTitle = VotersGraphPanel.escapeJs(getString("dshbStatsVotersAxis"));

		return "google.charts.load('current', {'packages':['corechart']});\n"
				+ "google.charts.setOnLoadCallback(" + DRAW_FUNCTION + ");\n"
				+ "function " + DRAW_FUNCTION + "() {\n"
				+ "  var chartDiv = document.getElementById('" + CHART_ID + "');\n"
				+ "  if (!chartDiv) { return; }\n"
				+ "  var data = new google.visualization.DataTable();\n"
				+ "  data.addColumn('string', '');\n"
				+ columns
				+ "  data.addRows([" + rows + "]);\n"
				+ "  var options = {\n"
				+ "    title: '" + title + "',\n"
				+ "    legend: { position: 'right' },\n"
				+ "    chartArea: { left: 70, top: 50, width: '68%', height: '70%' },\n"
				+ "    hAxis: { ticks: [" + ticks + "] },\n"
				+ "    vAxis: { title: '" + axisTitle + "', minValue: 0, viewWindow: { min: 0 } }\n"
				+ "  };\n"
				+ "  var chart = new google.visualization.LineChart(chartDiv);\n"
				+ "  chart.draw(data, options);\n"
				+ "}\n";
	}
}
