package net.lacnic.elections.adminweb.ui.admin.email;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Email;


public class EmailsListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private List<Email> emailsList;


	public EmailsListPanel(String id, Long electionId, boolean all) {
		super(id);
		if (electionId == 0) {
			if (all)
				emailsList = AppContext.getInstance().getManagerBeanRemote().getEmailsAll();
			else
				emailsList = AppContext.getInstance().getManagerBeanRemote().getPendingSendEmails();
		} else {
			if (all)
				emailsList = AppContext.getInstance().getManagerBeanRemote().getElectionEmails(electionId);
			else
				emailsList = AppContext.getInstance().getManagerBeanRemote().getElectionPendingSendEmails(electionId);
		}
		init(emailsList);
	}

	private void init(List<Email> emailsList) {
		try {
			final ListView<Email> emailsListView = new ListView<Email>("emailsList", emailsList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Email> item) {
					final Email currentEmail = item.getModelObject();
					try {
						item.add(new Label("sent", currentEmail.getSent() ? getString("messageListSentYes") : getString("messageListSentNo")));
						item.add(new Label("recipients", currentEmail.getRecipients()));
						item.add(new Label("templateType", currentEmail.getTemplateType()));
						item.add(new Label("subject", currentEmail.getSubject()));
						item.add(new MultiLineLabel("body", getString("messageListBodyNotAvailable")));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(emailsListView);
		} catch (Exception e) {
			error(e.getMessage());
		}
	}


	public List<Email> getEmailsList() {
		return emailsList;
	}

	public void setEmailsList(List<Email> emailsList) {
		this.emailsList = emailsList;
	}

}
