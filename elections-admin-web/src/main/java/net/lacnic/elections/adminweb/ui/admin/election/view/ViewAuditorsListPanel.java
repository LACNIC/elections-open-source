package net.lacnic.elections.adminweb.ui.admin.election.view;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Auditor;

public class ViewAuditorsListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public ViewAuditorsListPanel(String id, long electionId) {
		super(id);

		try {
			List<Auditor> auditorsList = AppContext.getInstance().getManagerBeanRemote().getElectionAuditors(electionId);

			ViewAuditorsListPanel.this.setOutputMarkupPlaceholderTag(true);
			final Form<Void> form = new Form<>("form");
			final WebMarkupContainer auditorsListContainer = new WebMarkupContainer("auditorsListContainer");
			auditorsListContainer.setOutputMarkupPlaceholderTag(true);

			final ListView<Auditor> auditorsListView = new ListView<Auditor>("auditorsList", auditorsList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Auditor> item) {
					final Auditor current = item.getModelObject();

					item.add(new Label("name", current.getName()));
					item.add(new Label("email", current.getMail()));
					item.add(new Label("agreedConformity", current.isAgreedConformity() ? getString("auditorsListAgreedConformityYes") : getString("auditorsListAgreedConformityNo")));

					// F_WatchAuditLinkRegistryAndHideAutitLink
//					final Label resultsLink = new Label("resultsLink", LinksUtils.buildAuditorResultsLink(current.getResultToken()));
//					resultsLink.setOutputMarkupPlaceholderTag(true);	
//					item.add(resultsLink);
				}
			};
			auditorsListContainer.add(auditorsListView);
			form.add(auditorsListContainer);
			add(form);

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}
