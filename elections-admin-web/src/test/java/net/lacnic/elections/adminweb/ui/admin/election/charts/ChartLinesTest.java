package net.lacnic.elections.adminweb.ui.admin.election.charts;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ChartLinesTest {

	@Test
	void shouldGenerateChartScriptWithExpectedIdsAndHooks() {
		ChartLines chartLines = new ChartLines(
				"chartId",
				"['23/10/2025',10,10],['24/10/2025',5,15]",
				"'23/10/2025','24/10/2025'",
				"Evolucion",
				"Cantidad por dia",
				"Totales",
				"cantidadPorDia",
				"totales");

		String js = chartLines.getJs();

		assertTrue(js.contains("google.charts.load('current'"));
		assertTrue(js.contains("document.getElementById('chartId')"));
		assertTrue(js.contains("data.addRows(["));
		assertTrue(js.contains("['23/10/2025',10,10],['24/10/2025',5,15]"));
		assertTrue(js.contains("ticks: ['23/10/2025','24/10/2025'"));
		assertTrue(js.contains("[ChartLines] init"));
		assertTrue(js.contains("change-chart button not found"));
		assertTrue(js.contains("drawMaterialChart();"));
	}
}
