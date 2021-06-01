package net.lacnic.elections.admin.web.panel.admin;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.domain.Activity;

public class ListaActividadesPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;
	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");
	
	private List<Activity> listaActividades; 


	public ListaActividadesPanel(String id, long idEleccion){
		super(id);
		if(idEleccion == -1)
			listaActividades = AppContext.getInstance().getManagerBeanRemote().getActivitiesAll();
		else
			listaActividades = AppContext.getInstance().getManagerBeanRemote().getElectionActivities(idEleccion);
		init(listaActividades);
	}

	private void init(List<Activity> actividades) {
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			final ListView<Activity> dataViewActividades = new ListView<Activity>("listaActividades", actividades) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Activity> item) {
					final Activity actual = item.getModelObject();
					try {
						item.add(new Label("nombre", actual.getUserName()));
						item.add(new Label("tipoActividad", actual.getActivityType().toString()));
						item.add(new Label("idEleccion", actual.getElectionId()));
						item.add(new Label("ip", actual.getIp()));
						item.add(new Label("tiempo", sdf.format(actual.getTimestamp())));
						item.add(new MultiLineLabel("descripcion", actual.getDescription()));
					} catch (Exception e) {
						appLogger.error(e);
						error(e.getMessage());
					}
				}
			};
			add(dataViewActividades);

		} catch (Exception e) {
			appLogger.error(e);
			error(e.getMessage());
		}
	}

}
