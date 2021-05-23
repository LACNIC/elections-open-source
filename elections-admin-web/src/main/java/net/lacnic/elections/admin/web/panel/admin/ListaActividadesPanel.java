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
import net.lacnic.elections.domain.Actividad;

public class ListaActividadesPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;
	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");
	
	private List<Actividad> listaActividades; 


	public ListaActividadesPanel(String id, long idEleccion){
		super(id);
		if(idEleccion == -1)
			listaActividades = AppContext.getInstance().getManagerBeanRemote().obtenerTodasLasActividades();
		else
			listaActividades = AppContext.getInstance().getManagerBeanRemote().obtenerTodasLasActividades(idEleccion);
		init(listaActividades);
	}

	private void init(List<Actividad> actividades) {
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			final ListView<Actividad> dataViewActividades = new ListView<Actividad>("listaActividades", actividades) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Actividad> item) {
					final Actividad actual = item.getModelObject();
					try {
						item.add(new Label("nombre", actual.getNomUser()));
						item.add(new Label("tipoActividad", actual.getTipoActividad().toString()));
						item.add(new Label("idEleccion", actual.getIdEleccion()));
						item.add(new Label("ip", actual.getIp()));
						item.add(new Label("tiempo", sdf.format(actual.getTiempo())));
						item.add(new MultiLineLabel("descripcion", actual.getDescripcion()));
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
