package net.lacnic.elections.adminweb.ui.vote;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;


public class VoteSimpleElectionDetailPanel extends Panel {

	private static final long serialVersionUID = -316823064515653219L;


	public VoteSimpleElectionDetailPanel(String id, Election election, UserVoter userVoter) {
		super(id);

		add(new Label("title", election.getTitle(getLanguage(userVoter))));
		add(new Label("voter", userVoter.getVoterInformation()));
		add(new Label("voteAmount", userVoter.getVoteAmount()));

		add(new Label("maxCandidates", String.valueOf(election.getMaxCandidates())));
		Label desc = new Label("description", election.getDescription(getLanguage(userVoter)));
		desc.setEscapeModelStrings(false);
		add(desc);
	}

	public String getLanguage(UserVoter userVoter) {
		if (userVoter != null)
			return userVoter.getLanguage();
		else
			return SecurityUtils.getLocale().getDisplayName();
	}

}
