package net.lacnic.elections.adminweb.ui.admin.useradmin;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
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

						BookmarkablePageLink<Void> editUserAdminPassword = new BookmarkablePageLink<>("editUserAdminPassword", EditUserAdminPasswordDashboard.class, UtilsParameters.getAdminId(currentUserAdmin.getUserAdminId()));
						editUserAdminPassword.setMarkupId("editUserAdminPassword" + currentUserAdmin.getUserAdminId());
						item.add(editUserAdminPassword);

						ButtonDeleteWithConfirmation buttonDeleteWithConfirmation = new ButtonDeleteWithConfirmation("remove", item.getIndex()) {

							private static final long serialVersionUID = -5905771461896619354L;

							@Override
							public void onConfirm() {
								try {
									if (currentUserAdmin.getUserAdminId().equalsIgnoreCase(SecurityUtils.getUserAdminId())) {
										getSession().error(getString("adminUserListErrorDel"));
									} else {
										AppContext.getInstance().getManagerBeanRemote().removeUserAdmin(currentUserAdmin.getUserAdminId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
										getSession().info(getString("adminUserListSuccessDel"));
										setResponsePage(UserAdminsDashboard.class);
									}
								} catch (Exception e) {
									appLogger.error(e);
								}
							}
						};
						item.add(buttonDeleteWithConfirmation);
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
