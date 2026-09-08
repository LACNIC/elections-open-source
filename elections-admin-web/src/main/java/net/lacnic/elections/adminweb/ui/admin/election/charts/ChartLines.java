package net.lacnic.elections.adminweb.ui.admin.election.charts;

public class ChartLines {
	private String js;

	private static final long serialVersionUID = 8860379988454655755L;

	public ChartLines(String id, String datos, String fechas, String tituloGrafico, String ejeY1Nombre, String ejeY2Nombre, String axisY1IdName, String axisY2IdName) {

		final StringBuilder jsBuf = new StringBuilder();

		String js1 = "google.charts.load('current', {'packages':['line', 'corechart']});";
		String js2 = "google.charts.setOnLoadCallback(drawChart);";
		String js3 = "function drawChart() {\n" + "\n" + "var button = document.getElementById('change-chart');\n" + "var chartDiv = document.getElementById(" + "'" + id + "'" + ");\n" + "console.info('[ChartLines] init', { chartId: '" + id + "', hasButton: !!button, hasChartDiv: !!chartDiv });\n" + "if (!chartDiv) {\n" + "  console.error('[ChartLines] chart container not found', { chartId: '" + id + "' });\n" + "  return;\n" + "}\n" + "\n" + "var data = new google.visualization.DataTable();\n" + "data.addColumn('string', '');\n" + "data.addColumn('number', " + "'" + ejeY1Nombre + "'" + ");\n" + "data.addColumn('number', " + "'" + ejeY2Nombre + "'" + ");\n" + "\n" + "data.addRows([\n" + datos + "  \n" + "]);\n" + "console.info('[ChartLines] data table ready', { chartId: '" + id + "', rows: data.getNumberOfRows(), columns: data.getNumberOfColumns() });\n" + "if (data.getNumberOfRows() === 0) {\n" + "  console.warn('[ChartLines] data table has no rows', { chartId: '" + id + "' });\n" + "}\n" + "\n" + "var materialOptions = {\n" + "  title: " + "'" + tituloGrafico + "'" + ",\n" + "  series: {\n" + "    // Gives each series an axis name that matches the Y-axis below.\n" + "    0: {axis: " + "'" + axisY1IdName + "'" + "},\n" + "    1: {axis: " + "'" + axisY2IdName + "'" + "}\n" + "  },\n" + "  axes: {\n" + "    // Adds labels to each axis; they don't have to match the axis names.\n" + "    y: {\n" + "      " + axisY1IdName + ": {label: " + "'" + ejeY1Nombre + "'" + "},\n" + "      " + axisY2IdName + ": {label: " + "'" + ejeY2Nombre + "'" + "}\n" + "    }\n" + "  }\n" + "};\n" + "\n" + "var classicOptions = {\n" + "  title: " + "'" + tituloGrafico + "',"
		// + " width: 800,\n" + " height: 600,\n" + " // Gives each series an axis that matches the vAxes number below.\n"
				+ "  series: {\n" + "    0: {targetAxisIndex: 0},\n" + "    1: {targetAxisIndex: 1}\n" + "  },\n" + "  vAxes: {\n" + "    // Adds titles to each axis.\n" + "    0: {title: " + "'" + ejeY1Nombre + "'" + "},\n" + "    1: {title: " + "'" + ejeY2Nombre + "'" + "}\n" + "  },\n" + "  hAxis: {\n" + "    ticks: [" + fechas + "           ]\n" + "  },\n" + "  // vAxis: {\n" + "  //   viewWindow: {\n" + "  //     max: 50\n" + "  //   }\n" + "  // }\n" + "};\n" + "\n" + "function updateButton(label, handler) {\n" + "  if (!button) {\n" + "    console.warn('[ChartLines] change-chart button not found', { chartId: '" + id + "' });\n" + "    return;\n" + "  }\n" + "  button.innerText = label;\n" + "  button.onclick = handler;\n" + "}\n" + "\n" + "function drawMaterialChart() {\n" + "  try {\n" + "    console.info('[ChartLines] drawing material chart', { chartId: '" + id + "' });\n" + "    var materialChart = new google.charts.Line(chartDiv);\n" + "    materialChart.draw(data, materialOptions);\n" + "    updateButton('Change to Classic', drawClassicChart);\n" + "    console.info('[ChartLines] material chart drawn', { chartId: '" + id + "' });\n" + "  } catch (e) {\n" + "    console.error('[ChartLines] material chart draw failed', { chartId: '" + id + "', error: e });\n" + "  }\n" + "}\n" + "\n" + "function drawClassicChart() {\n" + "  try {\n" + "    console.info('[ChartLines] drawing classic chart', { chartId: '" + id + "' });\n" + "    var classicChart = new google.visualization.LineChart(chartDiv);\n" + "    classicChart.draw(data, classicOptions);\n" + "    updateButton('Change to Material', drawMaterialChart);\n" + "    console.info('[ChartLines] classic chart drawn', { chartId: '" + id + "' });\n" + "  } catch (e) {\n" + "    console.error('[ChartLines] classic chart draw failed', { chartId: '" + id + "', error: e });\n" + "  }\n" + "}\n" + "\n" + "drawMaterialChart();\n" + "\n" + "}";

		jsBuf.append(js1);
		jsBuf.append(js2);
		jsBuf.append(js3);

		setJs(jsBuf.toString());

	}

	public String getJs() {
		return js;
	}

	public void setJs(String js) {
		this.js = js;
	}

}
