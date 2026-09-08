package net.lacnic.elections.adminweb.ui.admin.election.organizations;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

public class AutomaticOrganizationsSyncActionsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	public AutomaticOrganizationsSyncActionsPanel(String id, Election election, boolean forceSyncEnabled) {
		super(id);
		setOutputMarkupId(true);

		WebMarkupContainer forceSyncAction = new WebMarkupContainer("forceSyncAction");
		forceSyncAction.setOutputMarkupPlaceholderTag(true);
		forceSyncAction.setVisible(forceSyncEnabled);
		add(forceSyncAction);

		WebMarkupContainer forceSyncConfirmContainer = new WebMarkupContainer("forceSyncConfirmContainer");
		forceSyncConfirmContainer.setOutputMarkupPlaceholderTag(true);
		forceSyncConfirmContainer.setVisible(false);
		forceSyncAction.add(forceSyncConfirmContainer);

		AjaxLink<Void> forceSyncAskLink = new AjaxLink<Void>("forceSyncAskLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisible(false);
				forceSyncConfirmContainer.setVisible(true);
				target.add(forceSyncAction);
			}
		};
		forceSyncAskLink.setOutputMarkupPlaceholderTag(true);
		forceSyncAskLink.setVisible(forceSyncEnabled);
		forceSyncAction.add(forceSyncAskLink);

		Link<Void> forceSyncConfirmLink = new Link<Void>("forceSyncConfirmLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				executeSync(election);
			}
		};
		forceSyncConfirmContainer.add(forceSyncConfirmLink);

		AjaxLink<Void> forceSyncCancelLink = new AjaxLink<Void>("forceSyncCancelLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				forceSyncConfirmContainer.setVisible(false);
				forceSyncAskLink.setVisible(true);
				target.add(forceSyncAction);
			}
		};
		forceSyncCancelLink.setOutputMarkupPlaceholderTag(true);
		forceSyncConfirmContainer.add(forceSyncCancelLink);

		WebMarkupContainer forceSyncDisabledButton = new WebMarkupContainer("forceSyncDisabledButton");
		forceSyncDisabledButton.setVisible(!forceSyncEnabled);
		add(forceSyncDisabledButton);

	}

	private void executeSync(Election election) {
		long electionId = election.getElectionId();
		try {
			boolean queued = AppContext.getInstance().getManagerBeanRemote().queueMilacnicOrganizationsSyncForElectionInWindow(
					electionId,
					false,
					SecurityUtils.getUserAdminId(),
					SecurityUtils.getClientIp());
			if (!queued) {
				getSession().error(getString("organizationsManagementProcessingInProgress"));
			}
		} catch (Exception e) {
			getSession().error(getString("organizationsManagementErrBif"));
			appLogger.error("Error forcing organizations sync from MiLACNIC. electionId={}", electionId, e);
		}
		setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(electionId));
	}

}
