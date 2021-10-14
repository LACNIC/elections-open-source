package net.lacnic.elections.adminweb.ui.results.review;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.utils.LinksUtils;


public class ReviewAuditorsListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public ReviewAuditorsListPanel(String id, long electionId) {
		super(id);
		try {
			List<Auditor> auditors = AppContext.getInstance().getManagerBeanRemote().getElectionAuditors(electionId);
			ListView<Auditor> auditorsListView = new ListView<Auditor>("auditorsList", auditors) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(final ListItem<Auditor> item) {
					final Auditor current = item.getModelObject();
					item.add(new Label("name", current.getName()));
					item.add(new Label("mail", current.getMail()));
					item.add(new Label("revision", (current.isRevisionAvailable() ? getString("auditorRevListHabRevYes") : getString("auditorRevListHabRevNo"))));
					String linkTextString = LinksUtils.buildAuditorResultsLink(current.getResultToken());
					Label linkText = new Label("linkText", linkTextString);
					ExternalLink link = new ExternalLink("link", linkTextString);
					link.add(linkText);
					item.add(link);
					item.setVisible(current.isCommissioner());
				}
			};
			add(auditorsListView);
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}
