package net.lacnic.siselecciones.admin.web.estadisticas;

import org.apache.wicket.markup.html.panel.Panel;

import com.googlecode.wickedcharts.highcharts.options.ExportingOptions;
import com.googlecode.wickedcharts.highcharts.options.Options;


public class ChartPanel extends Panel {

	private static final long serialVersionUID = 8860379988454655755L;

	protected Options options;

	public ChartPanel(String id) {
		super(id);
		options = new Options();
		options.setExporting(new ExportingOptions().setEnabled(false));
	}

}
