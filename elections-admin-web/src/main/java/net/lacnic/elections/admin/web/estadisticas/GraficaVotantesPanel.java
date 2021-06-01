package net.lacnic.elections.admin.web.estadisticas;

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

import net.lacnic.elections.admin.app.AppContext;

public class GraficaVotantesPanel extends ChartPanel {

	private static final long serialVersionUID = 8860379988454655755L;

	public GraficaVotantesPanel(String id, long idEleccion) {
		super(id);

		List<Object> data = AppContext.getInstance().getVoterBeanRemote().getElectionVoteEvolutionData(idEleccion);
		Label texto = new Label("noData","No hay datos en este momento para mostrar");
		add(texto);
		texto.setVisible(false);

		if (data == null) {
			add(new WebMarkupContainer("chart"));
			texto.setVisible(true);
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

			Axis yAxis = new Axis();
			yAxis.setTitle(new Title("Cantidad Por Dia"));
			yAxis.setPlotLines(Collections.singletonList(plotLines));
			yAxis.setMin(0);

			Axis yAxis2 = new Axis();
			yAxis2.setTitle(new Title("Totales"));
			yAxis2.setPlotLines(Collections.singletonList(plotLines));
			yAxis2.setMin(0);
			yAxis2.setOpposite(true);

			ArrayList<Axis> yAxiss = new ArrayList<>();
			yAxiss.add(yAxis);
			yAxiss.add(yAxis2);
			options.setyAxis(yAxiss);

			Legend legend = new Legend();
			legend.setLayout(LegendLayout.HORIZONTAL);
			legend.setAlign(HorizontalAlignment.CENTER);
			legend.setVerticalAlign(VerticalAlignment.TOP);
			legend.setBorderWidth(0);
			options.setLegend(legend);

			Series<Number> series1 = new SimpleSeries();
			series1.setName("En el día");
			List<Number> listTotales = (List<Number>) data.get(1);
			series1.setData(listTotales.toArray(new Number[listTotales.size()]));
			series1.setyAxis(0);
			series1.setColor(new HexColor("#f8ac59"));
			options.addSeries(series1);
			//			f8ac59
			//			ed5565
			Series<Number> series2 = new SimpleSeries();
			series2.setName("Totales");
			List<Number> listDia = (List<Number>) data.get(2);
			series2.setData(listDia.toArray(new Number[listTotales.size()]));
			series2.setyAxis(1);
			series2.setColor(new HexColor("#1c84c6"));
			options.addSeries(series2);

			add(new Chart("chart", options));
		}

	}

}
