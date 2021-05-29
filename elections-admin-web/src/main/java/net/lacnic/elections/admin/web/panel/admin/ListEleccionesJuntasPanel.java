package net.lacnic.elections.admin.web.panel.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.dashboard.admin.DashboardEleccionesJuntas;
import net.lacnic.elections.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.elections.domain.JointElection;

public class ListEleccionesJuntasPanel extends Panel {

	private static final long serialVersionUID = -3823908256532916047L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ListEleccionesJuntasPanel(String id) {
		super(id);
		List<JointElection> listaaElecciones = AppContext.getInstance().getManagerBeanRemote().obtenerSupraElecciones();

		if (listaaElecciones == null)
			listaaElecciones = new ArrayList<>();
		init(listaaElecciones);
	}

	private void init(List<JointElection> listaaElecciones) {
		try {
			final ListView<JointElection> dataViewAdmins = new ListView<JointElection>("listaElecciones", listaaElecciones) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<JointElection> item) {
					final JointElection actual = item.getModelObject();
					try {
						item.add(new Label("eleccion1", AppContext.getInstance().getManagerBeanRemote().obtenerEleccion(actual.getIdElectionA()).getTitleSpanish()));
						item.add(new Label("eleccion2", AppContext.getInstance().getManagerBeanRemote().obtenerEleccion(actual.getIdElectionB()).getTitleSpanish()));

						BotonConConfirmacionEliminar botonConConfirmacionEliminar = new BotonConConfirmacionEliminar("eliminar", item.getIndex()) {

							private static final long serialVersionUID = 6986190296016629836L;

							@Override
							public void onConfirmar() {
								try {
									AppContext.getInstance().getManagerBeanRemote().eliminarSupraEleccion(actual);
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
