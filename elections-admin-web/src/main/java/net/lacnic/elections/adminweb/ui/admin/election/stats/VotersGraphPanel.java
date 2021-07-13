package net.lacnic.elections.adminweb.ui.admin.election.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import com.googlecode.wickedcharts.highcharts.options.Axis;
import com.googlecode.wickedcharts.highcharts.options.ChartOptions;
import com.googlecode.wickedcharts.highcharts.options.HorizontalAlignment;
import com.googlecode.wickedcharts.highcharts.options.Legend;
import com.googlecode.wickedcharts.highcharts.options.LegendLayout;
import com.googlecode.wickedcharts.highcharts.options.PlotLine;
import com.googlecode.wickedcharts.highcharts.options.SeriesType;
import com.googlecode.wickedcharts.highcharts.options.Title;
import com.googlecode.wickedcharts.highcharts.options.VerticalAlignment;
import com.googlecode.wickedcharts.highcharts.options.ZoomType;
import com.googlecode.wickedcharts.highcharts.options.color.HexColor;
import com.googlecode.wickedcharts.highcharts.options.series.Series;
import com.googlecode.wickedcharts.highcharts.options.series.SimpleSeries;
import com.googlecode.wickedcharts.wicket7.highcharts.Chart;

import net.lacnic.elections.adminweb.app.AppContext;


public class VotersGraphPanel extends ChartPanel {

	private static final long serialVersionUID = 8860379988454655755L;


	public VotersGraphPanel(String id, long electionId) {
		super(id);

		List<Object> data = AppContext.getInstance().getVoterBeanRemote().getElectionVoteEvolutionData(electionId);
		Label noDataText = new Label("noData", getString("dshbStatsNoData"));
		add(noDataText);
		noDataText.setVisible(false);

		if (data == null) {
			add(new WebMarkupContainer("chart"));
			noDataText.setVisible(true);
		} else {
			ChartOptions chartOptions = new ChartOptions();
			chartOptions.setType(SeriesType.LINE);
			chartOptions.setZoomType(ZoomType.X);
			options.setChartOptions(chartOptions);

			Title title = new Title("");
			options.setTitle(title);

			Axis xAxis = new Axis();
			xAxis.setCategories((List<String>) data.get(0));
			xAxis.setTickInterval((float) 7);
			options.setxAxis(xAxis);

			PlotLine plotLines = new PlotLine();
			plotLines.setValue(0f);
			plotLines.setWidth(1);
			plotLines.setColor(new HexColor("#999999"));

			Axis dailyYAxis = new Axis();
			dailyYAxis.setTitle(new Title(getString("dshbStatsAmountPerDayAxis")));
			dailyYAxis.setPlotLines(Collections.singletonList(plotLines));
			dailyYAxis.setMin(0);

			Axis totalYAxis = new Axis();
			totalYAxis.setTitle(new Title(getString("dshbStatsTotalsAxis")));
			totalYAxis.setPlotLines(Collections.singletonList(plotLines));
			totalYAxis.setMin(0);
			totalYAxis.setOpposite(true);

			ArrayList<Axis> yAxles = new ArrayList<>();
			yAxles.add(dailyYAxis);
			yAxles.add(totalYAxis);
			options.setyAxis(yAxles);

			Legend legend = new Legend();
			legend.setLayout(LegendLayout.HORIZONTAL);
			legend.setAlign(HorizontalAlignment.CENTER);
			legend.setVerticalAlign(VerticalAlignment.TOP);
			legend.setBorderWidth(0);
			options.setLegend(legend);

			Series<Number> dailySeries = new SimpleSeries();
			dailySeries.setName(getString("dshbStatsAmountPerDaySeries"));
			List<Number> totalsList = (List<Number>) data.get(1);
			dailySeries.setData(totalsList.toArray(new Number[totalsList.size()]));
			dailySeries.setyAxis(0);
			dailySeries.setColor(new HexColor("#f8ac59"));
			options.addSeries(dailySeries);

			Series<Number> totalSeries = new SimpleSeries();
			totalSeries.setName(getString("dshbStatsTotalsSeries"));
			List<Number> dayList = (List<Number>) data.get(2);
			totalSeries.setData(dayList.toArray(new Number[totalsList.size()]));
			totalSeries.setyAxis(1);
			totalSeries.setColor(new HexColor("#1c84c6"));
			options.addSeries(totalSeries);

			add(new Chart("chart", options));
		}
	}

}
