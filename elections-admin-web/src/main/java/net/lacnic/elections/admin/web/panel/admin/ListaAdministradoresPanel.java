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
import net.lacnic.elections.admin.dashboard.admin.DashboardAdministradores;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarAdministrador;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarPasswordAdministrador;
import net.lacnic.elections.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.UserAdmin;

public class ListaAdministradoresPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private long idUsuario;

	public ListaAdministradoresPanel(String id) {
		super(id);
		List<UserAdmin> listaaAdmins = AppContext.getInstance().getManagerBeanRemote().obtenerUsuariosAdmin();
		init(listaaAdmins);
	}

	private void init(List<UserAdmin> listaAdmins) {
		try {
			final ListView<UserAdmin> dataViewAdmins = new ListView<UserAdmin>("listaAdmins", listaAdmins) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<UserAdmin> item) {
					final UserAdmin actual = item.getModelObject();
					try {
						item.add(new Label("username", actual.getUserAdminId()));
						item.add(new Label("email", actual.getEmail()));
						item.add(new Label("eleccionesAutorizado", actual.getAuthorizedElectionId() == 0 ? "TODAS" : AppContext.getInstance().getManagerBeanRemote().obtenerEleccion(actual.getAuthorizedElectionId()).getTitleSpanish()));


						BookmarkablePageLink<Void> editarUsuarioAdmin = new BookmarkablePageLink<>("editarUsuarioAdmin", DashboardEditarAdministrador.class, UtilsParameters.getAdminId(actual.getUserAdminId()));
						editarUsuarioAdmin.setMarkupId("editarUsuarioAdmin" + actual.getUserAdminId());
						item.add(editarUsuarioAdmin);

						BookmarkablePageLink<Void> editarPasswordAdmin = new BookmarkablePageLink<>("editarPasswordAdmin", DashboardEditarPasswordAdministrador.class, UtilsParameters.getAdminId(actual.getUserAdminId()));
						editarPasswordAdmin.setMarkupId("editarPasswordAdmin" + actual.getUserAdminId());
						item.add(editarPasswordAdmin);


						BotonConConfirmacionEliminar botonConConfirmacionEliminar = new BotonConConfirmacionEliminar("eliminar", item.getIndex()) {

							private static final long serialVersionUID = -5905771461896619354L;

							@Override
							public void onConfirmar() {

								try {
									if (actual.getUserAdminId().equalsIgnoreCase(SecurityUtils.getAdminId())) {
										getSession().error(getString("adminUserListErrorDel"));
									} else {
										AppContext.getInstance().getManagerBeanRemote().eliminarUsuarioAdmin(actual.getUserAdminId(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
										getSession().info(getString("adminUserListExitoDel"));
										setResponsePage(DashboardAdministradores.class);
									}

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

	public long getIdUsuario() {
		return idUsuario;
	}
}