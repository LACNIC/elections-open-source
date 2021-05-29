
package net.lacnic.elections.admin.web.panel.avanzadas;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.domain.IpAccess;

public class ListadoipInhabilitadasPanel extends Panel {

	private static final long serialVersionUID = -8554113800494186242L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");	

	public ListadoipInhabilitadasPanel(String id) {
		super(id);

		List<IpAccess> listadoipInhabilitadas = AppContext.getInstance().getManagerBeanRemote().obtenerAccesosIps();

		final ListView<IpAccess> listadoIps = new ListView<IpAccess>("listadoAccesosIps", listadoipInhabilitadas) {

			private static final long serialVersionUID = 2146073776795909288L;

			@Override
			protected void populateItem(final ListItem<IpAccess> item) {

				try {
					final IpAccess ip = item.getModelObject();

					item.add(new Label("ip", ip.getIp()));
					item.add(new Label("intentos", ip.getAttemptCount()));
					item.add(new Label("fechaPrimerIntento", ip.getLastAttemptDate()));
					item.add(new Label("fechaUltimoIntento", ip.getFirstAttemptDate()));
				} catch (Exception e) {
					appLogger.error(e);
				}
			}
		};
		add(listadoIps);
	}
}
