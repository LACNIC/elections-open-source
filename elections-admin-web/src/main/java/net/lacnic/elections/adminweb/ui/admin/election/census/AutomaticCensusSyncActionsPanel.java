package net.lacnic.elections.adminweb.ui.admin.election.census;

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

public class AutomaticCensusSyncActionsPanel extends Panel {

	private static final long serialVersionUID = -6611216500772436717L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	public AutomaticCensusSyncActionsPanel(String id, Election election, boolean actionEnabled) {
		super(id);
		setOutputMarkupId(true);

		WebMarkupContainer forceSyncAction = new WebMarkupContainer("forceSyncAction");
		forceSyncAction.setOutputMarkupPlaceholderTag(true);
		forceSyncAction.setVisible(actionEnabled);
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
		forceSyncAskLink.setVisible(actionEnabled);
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
		forceSyncDisabledButton.setVisible(!actionEnabled);
		add(forceSyncDisabledButton);
	}

	private void executeSync(Election election) {
		long electionId = election.getElectionId();
		try {
			boolean queued = AppContext.getInstance().getManagerBeanRemote()
					.queueElectionCensusSyncFromOrganizations(electionId, false, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
			if (!queued) {
				getSession().error(getString("censusManagementProcessingInProgress"));
			}
		} catch (Exception e) {
			getSession().error(getString("censusManagementErrBif"));
			appLogger.error("Error forcing census sync from organizations. electionId={}", electionId, e);
		}
		setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(electionId));
	}
}
