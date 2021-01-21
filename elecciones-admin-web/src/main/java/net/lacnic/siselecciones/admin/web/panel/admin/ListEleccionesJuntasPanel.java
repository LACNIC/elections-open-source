package net.lacnic.siselecciones.admin.web.panel.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.dashboard.admin.DashboardEleccionesJuntas;
import net.lacnic.siselecciones.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.siselecciones.dominio.SupraEleccion;

public class ListEleccionesJuntasPanel extends Panel {

	private static final long serialVersionUID = -3823908256532916047L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ListEleccionesJuntasPanel(String id) {
		super(id);
		List<SupraEleccion> listaaElecciones = Contexto.getInstance().getManagerBeanRemote().obtenerSupraElecciones();

		if (listaaElecciones == null)
			listaaElecciones = new ArrayList<>();
		init(listaaElecciones);
	}

	private void init(List<SupraEleccion> listaaElecciones) {
		try {
			final ListView<SupraEleccion> dataViewAdmins = new ListView<SupraEleccion>("listaElecciones", listaaElecciones) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<SupraEleccion> item) {
					final SupraEleccion actual = item.getModelObject();
					try {
						item.add(new Label("eleccion1", Contexto.getInstance().getManagerBeanRemote().obtenerEleccion(actual.getIdEleccionA()).getTituloEspanol()));
						item.add(new Label("eleccion2", Contexto.getInstance().getManagerBeanRemote().obtenerEleccion(actual.getIdEleccionB()).getTituloEspanol()));

						BotonConConfirmacionEliminar botonConConfirmacionEliminar = new BotonConConfirmacionEliminar("eliminar", item.getIndex()) {

							private static final long serialVersionUID = 6986190296016629836L;

							@Override
							public void onConfirmar() {
								try {
									Contexto.getInstance().getManagerBeanRemote().eliminarSupraEleccion(actual);
									getSession().info(getString("unitedElecListExitoDel"));
									setResponsePage(DashboardEleccionesJuntas.class);
								} catch (Exception e) {
									appLogger.error(e);
								}
							}
						};
						item.add(botonConConfirmacionEliminar);
					} catch (Exception e) {
						appLogger.error(e);
					}
				}
			};

			add(dataViewAdmins);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}

}
