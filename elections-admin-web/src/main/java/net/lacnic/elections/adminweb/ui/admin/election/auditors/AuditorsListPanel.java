package net.lacnic.elections.adminweb.ui.admin.election.auditors;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.ui.components.ButtonViewLink;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.utils.LinksUtils;

public class AuditorsListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public AuditorsListPanel(String id, Election election) {
		super(id);
		try {
			List<Auditor> auditors = AppContext.getInstance().getManagerBeanRemote().getElectionAuditors(election.getElectionId());
			ListView<Auditor> auditorsDataView = new ListView<Auditor>("auditorsList", auditors) {

				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(final ListItem<Auditor> item) {
					final Auditor currentAuditor = item.getModelObject();

					item.add(new Label("name", currentAuditor.getName()));
					item.add(new Label("mail", currentAuditor.getMail()));
					item.add(new Label("isCommissioner", (currentAuditor.isCommissioner() ? getString("auditorManagementCommissionerYes") : getString("auditorManagementCommissionerNo"))));
					item.add(new Label("agreedConformity", (currentAuditor.isCommissioner() ? (currentAuditor.isAgreedConformity() ? getString("auditorManagementCommissionerYes") : getString("auditorManagementCommissionerNo")) : "-")));

//					String auditorLinkText = LinksUtils.buildAuditorResultsLink(currentAuditor.getResultToken());
//					Label auditorLinkTextLabel = new Label("auditorLinkText", auditorLinkText);
//					ExternalLink auditorLink = new ExternalLink("auditorLink", auditorLinkText);
// 					auditorLink.add(auditorLinkTextLabel);
//					item.add(auditorLink);

					// Feature: WatchAuditLinkRegistryAndHideAutitLink

					String auditorLinkText = LinksUtils.buildAuditorResultsLink(currentAuditor.getResultToken());
					String userAdminId = SecurityUtils.getUserAdminId();
					String activityDescription = userAdminId.toUpperCase() + " vió el link de auditoría de resultados del auditor " + currentAuditor.getName() + " en la elección " + election.getTitleSpanish();
					ButtonViewLink viewLinkButton = new ButtonViewLink("viewLinkButton", item.getIndex(), auditorLinkText) {
						private static final long serialVersionUID = 3666243113529801997L;

						@Override
						public void registerActivity() {
							AppContext.getInstance().getManagerBeanRemote().persistActivity(SecurityUtils.getUserAdminId(), ActivityType.VIEW_LINK_AUDIT, activityDescription, SecurityUtils.getClientIp(), currentAuditor.getElection().getElectionId());
						}
					};
					item.add(viewLinkButton);

					// end Feature: WatchAuditLinkRegistryAndHideAutitLink

					Link<Void> editAuditor = new Link<Void>("editAuditor") {
						private static final long serialVersionUID = -2734403145438500636L;

						@Override
						public void onClick() {
							setResponsePage(EditAuditorDashboard.class, UtilsParameters.getAudit(currentAuditor.getAuditorId()));
						}
					};
					editAuditor.setMarkupId("editAuditor" + item.getIndex());
					item.add(editAuditor);

					ButtonDeleteWithConfirmation buttonDeleteWithConfirmation = new ButtonDeleteWithConfirmation("removeAuditor", item.getIndex()) {
						private static final long serialVersionUID = 4479213289810959012L;

						@Override
						public void onConfirm() {
							SecurityUtils.info(getString("auditorManagementSuccessDel"));
							AppContext.getInstance().getManagerBeanRemote().removeAuditor(currentAuditor.getAuditorId(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
							setResponsePage(ElectionAuditorsDashboard.class, UtilsParameters.getId(election.getElectionId()));
						}
					};
					buttonDeleteWithConfirmation.setMarkupId("removeAuditor" + item.getIndex());
					item.add(buttonDeleteWithConfirmation);
				}
			};
			add(auditorsDataView);

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}
