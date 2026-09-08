package net.lacnic.elections.adminweb.ui.admin.election.charts;

import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;

public class VotersGraphPanel extends Panel {

	private static final long serialVersionUID = 8860379988454655755L;
	private static final String CHART_ID = "votersEvolutionChart";
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private final long electionId;
	private String rows;
	private String ticks;

	public VotersGraphPanel(String id, long electionId) {
		super(id);
		this.electionId = electionId;

		appLogger.info("VotersGraphPanel - starting chart setup for electionId={}", electionId);

		List<Object> data = AppContext.getInstance().getVoterBeanRemote().getElectionVoteEvolutionData(electionId);
		appLogger.info("VotersGraphPanel - data fetched for electionId={}, dataNull={}, dataSize={}", electionId, data == null, data != null ? data.size() : 0);

		Label noDataText = new Label("noData", new ResourceModel("dshbStatsNoData"));
		add(noDataText);

		WebMarkupContainer chartContainer = new WebMarkupContainer("chartContainer");
		add(chartContainer);

		if (data == null || data.size() < 3) {
			appLogger.warn("VotersGraphPanel - no chart data available for electionId={}, data={}", electionId, data);
			noDataText.setVisible(true);
			chartContainer.setVisible(false);
			return;
		}

		List<String> labels = (List<String>) data.get(0);
		List<Number> daily = (List<Number>) data.get(1);
		List<Number> totals = (List<Number>) data.get(2);

		appLogger.info(
				"VotersGraphPanel - parsed data for electionId={}, labelsCount={}, dailyCount={}, totalsCount={}, firstLabel={}, lastLabel={}",
				electionId,
				labels != null ? labels.size() : 0,
				daily != null ? daily.size() : 0,
				totals != null ? totals.size() : 0,
				labels != null && !labels.isEmpty() ? labels.get(0) : null,
				labels != null && !labels.isEmpty() ? labels.get(labels.size() - 1) : null);

		if (labels == null || daily == null || totals == null || labels.isEmpty()) {
			appLogger.warn("VotersGraphPanel - invalid chart series for electionId={}, labelsNull={}, dailyNull={}, totalsNull={}, labelsEmpty={}",
					electionId,
					labels == null,
					daily == null,
					totals == null,
					labels != null && labels.isEmpty());
			noDataText.setVisible(true);
			chartContainer.setVisible(false);
			return;
		}

		noDataText.setVisible(false);

		rows = buildRows(labels, daily, totals);
		ticks = buildTicks(labels);

		appLogger.info("VotersGraphPanel - chart payload built for electionId={}, rowsLength={}, ticksLength={}, rowsPreview={}, ticksPreview={}",
				electionId,
				rows.length(),
				ticks.length(),
				truncate(rows),
				truncate(ticks));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		if (rows == null || ticks == null) {
			appLogger.warn("VotersGraphPanel - renderHead skipped because chart payload is missing for electionId={}", electionId);
			return;
		}

		ChartLines chartLines = new ChartLines(
				CHART_ID,
				rows,
				ticks,
				getString("dshbStatsTitle"),
				getString("dshbStatsAmountPerDaySeries"),
				getString("dshbStatsTotalsSeries"),
				"cantidadPorDia",
				"totales");
		String js = chartLines.getJs();
		appLogger.info("VotersGraphPanel - chart JS generated for electionId={}, chartId={}, jsLength={}", electionId, CHART_ID, js != null ? js.length() : 0);

		appLogger.info("VotersGraphPanel - rendering Google Charts assets for electionId={}, chartId={}", electionId, CHART_ID);
		response.render(JavaScriptHeaderItem.forUrl("https://www.gstatic.com/charts/loader.js"));
		response.render(JavaScriptHeaderItem.forScript(js, null));
	}

	static String buildRows(List<String> labels, List<Number> daily, List<Number> totals) {
		StringBuilder rows = new StringBuilder();
		int size = Math.min(labels.size(), Math.min(daily.size(), totals.size()));

		for (int i = 0; i < size; i++) {
			if (rows.length() > 0) {
				rows.append(',');
			}

			rows.append("['")
					.append(escapeJs(labels.get(i)))
					.append("',")
					.append(numberToJs(daily.get(i)))
					.append(',')
					.append(numberToJs(totals.get(i)))
					.append(']');
		}

		return rows.length() > 0 ? rows.toString() : "['',0,0]";
	}

	static String buildTicks(List<String> labels) {
		StringBuilder ticks = new StringBuilder();

		for (String label : labels) {
			if (ticks.length() > 0) {
				ticks.append(',');
			}
			ticks.append('\'').append(escapeJs(label)).append('\'');
		}

		return ticks.length() > 0 ? ticks.toString() : "''";
	}

	static String numberToJs(Number number) {
		return number == null ? "0" : number.toString();
	}

	static String escapeJs(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("'", "\\'");
	}

	static String truncate(String value) {
		if (value == null) {
			return null;
		}
		return value.length() > 250 ? value.substring(0, 250) + "..." : value;
	}
}
