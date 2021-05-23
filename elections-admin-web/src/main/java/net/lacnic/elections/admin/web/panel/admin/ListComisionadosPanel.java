package net.lacnic.elections.admin.web.panel.admin;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.admin.DashboardComisionados;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarComisionado;
import net.lacnic.elections.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Comisionado;

public class ListComisionadosPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ListComisionadosPanel(String id) {
		super(id);
		List<Comisionado> listaComisionados = AppContext.getInstance().getManagerBeanRemote().obtenerComisionados();
		init(listaComisionados);
	}

	private void init(List<Comisionado> listaComisionados) {
		try {
			final ListView<Comisionado> dataViewComisionados = new ListView<Comisionado>("listaComisionados", listaComisionados) {
				
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Comisionado> item) {
					final Comisionado actual = item.getModelObject();
					try {
						item.add(new Label("nombre", actual.getNombre()));
						item.add(new Label("email", actual.getMail()));

						BookmarkablePageLink<Void> editarComisionado = new BookmarkablePageLink<>("editarComisionado", DashboardEditarComisionado.class, UtilsParameters.getAudit(actual.getIdComisionado()));
						editarComisionado.setMarkupId("editarComisionado" + item.getIndex());
						item.add(editarComisionado);

						BotonConConfirmacionEliminar botonConConfirmacionEliminar = new BotonConConfirmacionEliminar("eliminar",item.getIndex()) {

							private static final long serialVersionUID = 3950434097284027591L;

							@Override
							public void onConfirmar() {

								try {
									AppContext.getInstance().getManagerBeanRemote().eliminarComisionado(actual.getIdComisionado(), actual.getNombre(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
									getSession().info(getString("commissionerListExitoDel"));
									setResponsePage(DashboardComisionados.class);
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

			add(dataViewComisionados);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}
}