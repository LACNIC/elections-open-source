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
import net.lacnic.elections.admin.dashboard.admin.CommissionersDashboard;
import net.lacnic.elections.admin.dashboard.admin.EditCommissionerDashboard;
import net.lacnic.elections.admin.web.commons.BotonConConfirmacionEliminar;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Commissioner;


public class CommissionersListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public CommissionersListPanel(String id) {
		super(id);
		List<Commissioner> commissionersList = AppContext.getInstance().getManagerBeanRemote().getCommissionersAll();
		init(commissionersList);
	}


	private void init(List<Commissioner> commissionersList) {
		try {
			final ListView<Commissioner> commissionersDataView = new ListView<Commissioner>("commissionersList", commissionersList) {
				
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Commissioner> item) {
					final Commissioner currentCommissioner = item.getModelObject();
					try {
						item.add(new Label("name", currentCommissioner.getName()));
						item.add(new Label("mail", currentCommissioner.getMail()));

						BookmarkablePageLink<Void> editCommissioner = new BookmarkablePageLink<>("editCommissioner", EditCommissionerDashboard.class, UtilsParameters.getAudit(currentCommissioner.getCommissionerId()));
						editCommissioner.setMarkupId("editCommissioner" + item.getIndex());
						item.add(editCommissioner);

						BotonConConfirmacionEliminar botonConConfirmacionEliminar = new BotonConConfirmacionEliminar("remove", item.getIndex()) {

							private static final long serialVersionUID = 3950434097284027591L;

							@Override
							public void onConfirmar() {
								try {
									AppContext.getInstance().getManagerBeanRemote().removeCommissioner(currentCommissioner.getCommissionerId(), currentCommissioner.getName(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
									getSession().info(getString("commissionerListExitoDel"));
									setResponsePage(CommissionersDashboard.class);
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

			add(commissionersDataView);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}
}