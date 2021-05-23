
package net.lacnic.elections.admin.web.panel.avanzadas;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.domain.AccesosIps;

public class ListadoipInhabilitadasPanel extends Panel {

	private static final long serialVersionUID = -8554113800494186242L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");	

	public ListadoipInhabilitadasPanel(String id) {
		super(id);

		List<AccesosIps> listadoipInhabilitadas = AppContext.getInstance().getManagerBeanRemote().obtenerAccesosIps();

		final ListView<AccesosIps> listadoIps = new ListView<AccesosIps>("listadoAccesosIps", listadoipInhabilitadas) {

			private static final long serialVersionUID = 2146073776795909288L;

			@Override
			protected void populateItem(final ListItem<AccesosIps> item) {

				try {
					final AccesosIps ip = item.getModelObject();

					item.add(new Label("ip", ip.getIp()));
					item.add(new Label("intentos", ip.getIntentos()));
					item.add(new Label("fechaPrimerIntento", ip.getFechaPrimerIntento()));
					item.add(new Label("fechaUltimoIntento", ip.getFechaUltimoIntento()));
				} catch (Exception e) {
					appLogger.error(e);
				}
			}
		};
		add(listadoIps);
	}
}
