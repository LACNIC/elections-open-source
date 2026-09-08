package net.lacnic.elections.adminweb.ui.vote;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.UserVoter;

public class AlreadyVotedPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public AlreadyVotedPanel(String id, UserVoter userVoter) {
		super(id);

		add(new FeedbackPanel("feedbackPanel"));
		getSession().info(getString("alreadyVotedMessage"));

		add(new Label("title", userVoter.getElection().getTitle(getLanguage(userVoter))));
		add(new Label("voter", userVoter.getVoterInformation()));
		add(new Label("maxCandidates", String.valueOf(userVoter.getElection().getMaxCandidates())));
		add(new Label("voteAmount", userVoter.getVoteAmount()));

		Label desc = new Label("description", userVoter.getElection().getDescription(getLanguage(userVoter)));
		desc.setEscapeModelStrings(false);
		add(desc);

		List<Object[]> chosenCandidates = AppContext.getInstance().getVoterBeanRemote().getElectionVotesCandidateForUserVoter(userVoter.getUserVoterId(), userVoter.getElection().getElectionId());
		add(new ListView<Object[]>("codes", chosenCandidates) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<Object[]> item) {
				final Object[] current = item.getModelObject();
				int rowNumber = item.getIndex() + 1;
				item.add(new Label("number", String.valueOf(rowNumber)));
				Label code = new Label("code", (String) current[1]);
				code.setMarkupId("code" + item.getIndex());
				item.add(code);
			}
		});
	}

	private String getLanguage(UserVoter userVoter) {
		if (userVoter != null && userVoter.getLanguageEnum() != null) {
			return userVoter.getLanguageEnum().getCode();
		}
		return LanguageCode.fromValueOrDefault(SecurityUtils.getLocale() != null ? SecurityUtils.getLocale().getLanguage() : null, LanguageCode.SP).getCode();
	}
}
