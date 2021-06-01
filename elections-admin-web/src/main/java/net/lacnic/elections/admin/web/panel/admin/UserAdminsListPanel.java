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
import net.lacnic.elections.admin.dashboard.admin.EditUserAdminDashboard;
import net.lacnic.elections.admin.dashboard.admin.DashboardEditarPasswordAdministrador;
import net.lacnic.elections.admin.dashboard.admin.UserAdminsDashboard;
import net.lacnic.elections.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.UserAdmin;


public class UserAdminsListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private long userAdminId;


	public UserAdminsListPanel(String id) {
		super(id);
		List<UserAdmin> listaaAdmins = AppContext.getInstance().getManagerBeanRemote().getUserAdminsAll();
		init(listaaAdmins);
	}


	private void init(List<UserAdmin> userAdminsList) {
		try {
			final ListView<UserAdmin> userAdminsDataView = new ListView<UserAdmin>("userAdminsList", userAdminsList) {

				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<UserAdmin> item) {
					final UserAdmin currentUserAdmin = item.getModelObject();
					try {
						item.add(new Label("userAdminId", currentUserAdmin.getUserAdminId()));
						item.add(new Label("email", currentUserAdmin.getEmail()));
						item.add(new Label("authorizedElection", currentUserAdmin.getAuthorizedElectionId() == 0 ? "TODAS" : AppContext.getInstance().getManagerBeanRemote().getElection(currentUserAdmin.getAuthorizedElectionId()).getTitleSpanish()));

						BookmarkablePageLink<Void> editUserAdmin = new BookmarkablePageLink<>("editUserAdmin", EditUserAdminDashboard.class, UtilsParameters.getAdminId(currentUserAdmin.getUserAdminId()));
						editUserAdmin.setMarkupId("editUserAdmin" + currentUserAdmin.getUserAdminId());
						item.add(editUserAdmin);

						BookmarkablePageLink<Void> editUserAdminPassword = new BookmarkablePageLink<>("editUserAdminPassword", DashboardEditarPasswordAdministrador.class, UtilsParameters.getAdminId(currentUserAdmin.getUserAdminId()));
						editUserAdminPassword.setMarkupId("editUserAdminPassword" + currentUserAdmin.getUserAdminId());
						item.add(editUserAdminPassword);

						BotonConConfirmacionEliminar botonConConfirmacionEliminar = new BotonConConfirmacionEliminar("remove", item.getIndex()) {

							private static final long serialVersionUID = -5905771461896619354L;

							@Override
							public void onConfirmar() {
								try {
									if (currentUserAdmin.getUserAdminId().equalsIgnoreCase(SecurityUtils.getAdminId())) {
										getSession().error(getString("adminUserListErrorDel"));
									} else {
										AppContext.getInstance().getManagerBeanRemote().removeUserAdmin(currentUserAdmin.getUserAdminId(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
										getSession().info(getString("adminUserListSuccessDel"));
										setResponsePage(UserAdminsDashboard.class);
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

			add(userAdminsDataView);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}

	public long getUserAdminId() {
		return userAdminId;
	}

}
