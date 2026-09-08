package net.lacnic.elections.adminweb.ui.admin.election.census;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;

public class CensusAdvancedActionsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	public CensusAdvancedActionsPanel(String id, Election election, boolean actionsEnabled, boolean hasUserVoters, boolean deleteBlockedByVotes) {
		super(id);
		setOutputMarkupId(true);

		String blockedReasonKey = deleteBlockedByVotes ? "censusManagementAdvancedDeleteBlockedByVotes" : null;
		WebMarkupContainer blockedReasonContainer = new WebMarkupContainer("blockedReasonContainer");
		blockedReasonContainer.setVisible(hasText(blockedReasonKey));
		blockedReasonContainer.add(new Label("blockedReasonText", hasText(blockedReasonKey) ? getString(blockedReasonKey) : ""));
		add(blockedReasonContainer);

		boolean regenerateEnabled = actionsEnabled && hasUserVoters;
		WebMarkupContainer regenerateLinksAction = new WebMarkupContainer("regenerateLinksAction");
		regenerateLinksAction.setOutputMarkupPlaceholderTag(true);
		regenerateLinksAction.setVisible(regenerateEnabled);
		add(regenerateLinksAction);
		WebMarkupContainer regenerateLinksConfirmContainer = new WebMarkupContainer("regenerateLinksConfirmContainer");
		regenerateLinksConfirmContainer.setOutputMarkupPlaceholderTag(true);
		regenerateLinksConfirmContainer.setVisible(false);
		regenerateLinksAction.add(regenerateLinksConfirmContainer);
		AjaxLink<Void> regenerateLinksAskLink = new AjaxLink<Void>("regenerateLinksAskLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisible(false);
				regenerateLinksConfirmContainer.setVisible(true);
				target.add(regenerateLinksAction);
			}
		};
		regenerateLinksAskLink.setOutputMarkupPlaceholderTag(true);
		regenerateLinksAskLink.setVisible(regenerateEnabled);
		regenerateLinksAction.add(regenerateLinksAskLink);
		regenerateLinksConfirmContainer.add(new Link<Void>("regenerateLinksConfirmLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				executeRegenerateLinks(election);
			}
		});
		regenerateLinksConfirmContainer.add(new AjaxLink<Void>("regenerateLinksCancelLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				regenerateLinksConfirmContainer.setVisible(false);
				regenerateLinksAskLink.setVisible(true);
				target.add(regenerateLinksAction);
			}
		});
		WebMarkupContainer regenerateLinksDisabledButton = new WebMarkupContainer("regenerateLinksDisabledButton");
		regenerateLinksDisabledButton.setVisible(!regenerateEnabled);
		add(regenerateLinksDisabledButton);

		boolean deleteEnabled = actionsEnabled && hasUserVoters && !deleteBlockedByVotes;
		WebMarkupContainer deleteAllAction = new WebMarkupContainer("deleteAllAction");
		deleteAllAction.setOutputMarkupPlaceholderTag(true);
		deleteAllAction.setVisible(deleteEnabled);
		add(deleteAllAction);
		WebMarkupContainer deleteAllConfirmContainer = new WebMarkupContainer("deleteAllConfirmContainer");
		deleteAllConfirmContainer.setOutputMarkupPlaceholderTag(true);
		deleteAllConfirmContainer.setVisible(false);
		deleteAllAction.add(deleteAllConfirmContainer);
		AjaxLink<Void> deleteAllAskLink = new AjaxLink<Void>("deleteAllAskLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisible(false);
				deleteAllConfirmContainer.setVisible(true);
				target.add(deleteAllAction);
			}
		};
		deleteAllAskLink.setOutputMarkupPlaceholderTag(true);
		deleteAllAskLink.setVisible(deleteEnabled);
		deleteAllAction.add(deleteAllAskLink);
		deleteAllConfirmContainer.add(new Link<Void>("deleteAllConfirmLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				executeDeleteAll(election);
			}
		});
		deleteAllConfirmContainer.add(new AjaxLink<Void>("deleteAllCancelLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				deleteAllConfirmContainer.setVisible(false);
				deleteAllAskLink.setVisible(true);
				target.add(deleteAllAction);
			}
		});
		WebMarkupContainer deleteAllDisabledButton = new WebMarkupContainer("deleteAllDisabledButton");
		deleteAllDisabledButton.setVisible(!deleteEnabled);
		add(deleteAllDisabledButton);
	}

	private void executeRegenerateLinks(Election election) {
		long electionId = election.getElectionId();
		try {
			boolean queued = AppContext.getInstance().getManagerBeanRemote()
					.queueElectionCensusRegenerateVoteLinks(electionId, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
			if (!queued) {
				getSession().error(getString("censusManagementProcessingInProgress"));
			}
		} catch (Exception e) {
			getSession().error(getString("censusManagementErrBif"));
			appLogger.error("Error queueing census mass token regeneration. electionId={}", electionId, e);
		}
		setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(electionId));
	}

	private void executeDeleteAll(Election election) {
		long electionId = election.getElectionId();
		try {
			boolean queued = AppContext.getInstance().getManagerBeanRemote()
					.queueElectionCensusDeleteAll(electionId, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
			if (!queued) {
				getSession().error(getString("censusManagementProcessingInProgress"));
			}
		} catch (Exception e) {
			getSession().error(getString("censusManagementErrBif"));
			appLogger.error("Error queueing census mass delete. electionId={}", electionId, e);
		}
		setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(electionId));
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
