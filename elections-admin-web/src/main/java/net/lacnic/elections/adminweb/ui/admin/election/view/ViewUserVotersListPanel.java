package net.lacnic.elections.adminweb.ui.admin.election.view;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.LinksUtils;


public class ViewUserVotersListPanel extends Panel {

	private static final long serialVersionUID = -1239455534678268981L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public ViewUserVotersListPanel(String id, Election election) {
		super(id);
		List<UserVoter> userVotersList = AppContext.getInstance().getManagerBeanRemote().getElectionUserVoters(election.getElectionId());

		final ListView<UserVoter> userVotersListView = new ListView<UserVoter>("userVotersList", userVotersList) {
			private static final long serialVersionUID = -1250986391822198393L;

			@Override
			protected void populateItem(final ListItem<UserVoter> item) {
				try {
					final UserVoter current = item.getModelObject();

					item.add(new Label("language", current.getLanguage()));
					item.add(new Label("name", current.getName()));
					item.add(new Label("mail", current.getMail()));
					item.add(new Label("voteAmount", String.valueOf(current.getVoteAmount())));
					item.add(new Label("country", current.getCountry()));
					item.add(new Label("orgid", current.getOrgID()));

					final Label voteLink = new Label("voteLink", LinksUtils.buildVoteLink(current.getVoteToken()));
					voteLink.setOutputMarkupPlaceholderTag(true);
					item.add(voteLink);

				} catch (Exception e) {
					appLogger.error(e);
				}
			}
		};
		add(userVotersListView);
	}

}
