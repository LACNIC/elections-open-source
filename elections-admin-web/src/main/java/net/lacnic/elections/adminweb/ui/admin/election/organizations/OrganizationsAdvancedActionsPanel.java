package net.lacnic.elections.adminweb.ui.admin.election.organizations;

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

public class OrganizationsAdvancedActionsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	public OrganizationsAdvancedActionsPanel(String id, Election election, boolean actionsEnabled, boolean hasOrganizations, boolean hasSupportLinks,
			boolean deleteBlockedByNominations, boolean debtorMirrorAllowedByModeAndWindow) {
		super(id);
		setOutputMarkupId(true);

		String blockedReasonKey = deleteBlockedByNominations ? "organizationsManagementAdvancedDeleteBlockedByNominations" : null;
		WebMarkupContainer blockedReasonContainer = new WebMarkupContainer("blockedReasonContainer");
		blockedReasonContainer.setVisible(hasText(blockedReasonKey));
		blockedReasonContainer.add(new Label("blockedReasonText", hasText(blockedReasonKey) ? getString(blockedReasonKey) : ""));
		add(blockedReasonContainer);

		boolean debtorMirrorEnabled = actionsEnabled && hasOrganizations && debtorMirrorAllowedByModeAndWindow;
		WebMarkupContainer debtorMirrorAction = new WebMarkupContainer("debtorMirrorAction");
		debtorMirrorAction.setOutputMarkupPlaceholderTag(true);
		debtorMirrorAction.setVisible(debtorMirrorEnabled);
		add(debtorMirrorAction);
		WebMarkupContainer debtorMirrorConfirmContainer = new WebMarkupContainer("debtorMirrorConfirmContainer");
		debtorMirrorConfirmContainer.setOutputMarkupPlaceholderTag(true);
		debtorMirrorConfirmContainer.setVisible(false);
		debtorMirrorAction.add(debtorMirrorConfirmContainer);
		AjaxLink<Void> debtorMirrorAskLink = new AjaxLink<Void>("debtorMirrorAskLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisible(false);
				debtorMirrorConfirmContainer.setVisible(true);
				target.add(debtorMirrorAction);
			}
		};
		debtorMirrorAskLink.setOutputMarkupPlaceholderTag(true);
		debtorMirrorAskLink.setVisible(debtorMirrorEnabled);
		debtorMirrorAction.add(debtorMirrorAskLink);
		debtorMirrorConfirmContainer.add(new Link<Void>("debtorMirrorConfirmLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				executeDebtorMirrorSync(election);
			}
		});
		debtorMirrorConfirmContainer.add(new AjaxLink<Void>("debtorMirrorCancelLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				debtorMirrorConfirmContainer.setVisible(false);
				debtorMirrorAskLink.setVisible(true);
				target.add(debtorMirrorAction);
			}
		});
		WebMarkupContainer debtorMirrorDisabledButton = new WebMarkupContainer("debtorMirrorDisabledButton");
		debtorMirrorDisabledButton.setVisible(!debtorMirrorEnabled);
		add(debtorMirrorDisabledButton);

		boolean regenerateNominationEnabled = actionsEnabled && hasOrganizations;
		WebMarkupContainer regenerateNominationAction = new WebMarkupContainer("regenerateNominationAction");
		regenerateNominationAction.setOutputMarkupPlaceholderTag(true);
		regenerateNominationAction.setVisible(regenerateNominationEnabled);
		add(regenerateNominationAction);
		WebMarkupContainer regenerateNominationConfirmContainer = new WebMarkupContainer("regenerateNominationConfirmContainer");
		regenerateNominationConfirmContainer.setOutputMarkupPlaceholderTag(true);
		regenerateNominationConfirmContainer.setVisible(false);
		regenerateNominationAction.add(regenerateNominationConfirmContainer);
		AjaxLink<Void> regenerateNominationAskLink = new AjaxLink<Void>("regenerateNominationAskLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisible(false);
				regenerateNominationConfirmContainer.setVisible(true);
				target.add(regenerateNominationAction);
			}
		};
		regenerateNominationAskLink.setOutputMarkupPlaceholderTag(true);
		regenerateNominationAskLink.setVisible(regenerateNominationEnabled);
		regenerateNominationAction.add(regenerateNominationAskLink);
		regenerateNominationConfirmContainer.add(new Link<Void>("regenerateNominationConfirmLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				executeRegenerateNominationLinks(election);
			}
		});
		regenerateNominationConfirmContainer.add(new AjaxLink<Void>("regenerateNominationCancelLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				regenerateNominationConfirmContainer.setVisible(false);
				regenerateNominationAskLink.setVisible(true);
				target.add(regenerateNominationAction);
			}
		});
		WebMarkupContainer regenerateNominationDisabledButton = new WebMarkupContainer("regenerateNominationDisabledButton");
		regenerateNominationDisabledButton.setVisible(!regenerateNominationEnabled);
		add(regenerateNominationDisabledButton);

		boolean regenerateSupportEnabled = actionsEnabled && hasSupportLinks;
		WebMarkupContainer regenerateSupportAction = new WebMarkupContainer("regenerateSupportAction");
		regenerateSupportAction.setOutputMarkupPlaceholderTag(true);
		regenerateSupportAction.setVisible(regenerateSupportEnabled);
		add(regenerateSupportAction);
		WebMarkupContainer regenerateSupportConfirmContainer = new WebMarkupContainer("regenerateSupportConfirmContainer");
		regenerateSupportConfirmContainer.setOutputMarkupPlaceholderTag(true);
		regenerateSupportConfirmContainer.setVisible(false);
		regenerateSupportAction.add(regenerateSupportConfirmContainer);
		AjaxLink<Void> regenerateSupportAskLink = new AjaxLink<Void>("regenerateSupportAskLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisible(false);
				regenerateSupportConfirmContainer.setVisible(true);
				target.add(regenerateSupportAction);
			}
		};
		regenerateSupportAskLink.setOutputMarkupPlaceholderTag(true);
		regenerateSupportAskLink.setVisible(regenerateSupportEnabled);
		regenerateSupportAction.add(regenerateSupportAskLink);
		regenerateSupportConfirmContainer.add(new Link<Void>("regenerateSupportConfirmLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				executeRegenerateSupportLinks(election);
			}
		});
		regenerateSupportConfirmContainer.add(new AjaxLink<Void>("regenerateSupportCancelLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				regenerateSupportConfirmContainer.setVisible(false);
				regenerateSupportAskLink.setVisible(true);
				target.add(regenerateSupportAction);
			}
		});
		WebMarkupContainer regenerateSupportDisabledButton = new WebMarkupContainer("regenerateSupportDisabledButton");
		regenerateSupportDisabledButton.setVisible(!regenerateSupportEnabled);
		add(regenerateSupportDisabledButton);

		boolean deleteEnabled = actionsEnabled && hasOrganizations && !deleteBlockedByNominations;
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

	private void executeRegenerateNominationLinks(Election election) {
		long electionId = election.getElectionId();
		try {
			boolean queued = AppContext.getInstance().getManagerBeanRemote()
					.queueElectionOrganizationsRegenerateNominationLinks(electionId, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
			if (!queued) {
				getSession().error(getString(OrganizationsResourceKeys.PROCESSING_IN_PROGRESS));
			}
		} catch (Exception e) {
			getSession().error(getString(OrganizationsResourceKeys.ERR_BIF));
			appLogger.error("Error queueing organizations mass nomination links regeneration. electionId={}", electionId, e);
		}
		setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(electionId));
	}

	private void executeDebtorMirrorSync(Election election) {
		long electionId = election.getElectionId();
		try {
			boolean queued = AppContext.getInstance().getManagerBeanRemote()
					.queueMilacnicOrganizationsDebtorMirrorSyncForElectionInWindow(electionId, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
			if (!queued) {
				getSession().error(getString(OrganizationsResourceKeys.PROCESSING_IN_PROGRESS));
			}
		} catch (Exception e) {
			getSession().error(getString(OrganizationsResourceKeys.ERR_BIF));
			appLogger.error("Error queueing organizations debtor mirror sync from MiLACNIC. electionId={}", electionId, e);
		}
		setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(electionId));
	}

	private void executeRegenerateSupportLinks(Election election) {
		long electionId = election.getElectionId();
		try {
			boolean queued = AppContext.getInstance().getManagerBeanRemote()
					.queueElectionOrganizationsRegenerateSupportLinks(electionId, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
			if (!queued) {
				getSession().error(getString(OrganizationsResourceKeys.PROCESSING_IN_PROGRESS));
			}
		} catch (Exception e) {
			getSession().error(getString(OrganizationsResourceKeys.ERR_BIF));
			appLogger.error("Error queueing organizations mass support links regeneration. electionId={}", electionId, e);
		}
		setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(electionId));
	}

	private void executeDeleteAll(Election election) {
		long electionId = election.getElectionId();
		try {
			boolean queued = AppContext.getInstance().getManagerBeanRemote()
					.queueElectionOrganizationsDeleteAll(electionId, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
			if (!queued) {
				getSession().error(getString(OrganizationsResourceKeys.PROCESSING_IN_PROGRESS));
			}
		} catch (Exception e) {
			getSession().error(getString(OrganizationsResourceKeys.ERR_BIF));
			appLogger.error("Error queueing organizations mass delete. electionId={}", electionId, e);
		}
		setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(electionId));
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
