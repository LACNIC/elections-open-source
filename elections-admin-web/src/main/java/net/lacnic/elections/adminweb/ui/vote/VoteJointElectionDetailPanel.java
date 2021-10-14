package net.lacnic.elections.adminweb.ui.vote;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;


public class VoteJointElectionDetailPanel extends Panel {

	private static final long serialVersionUID = 5505752645083978888L;

	private int number = 1;


	public VoteJointElectionDetailPanel(String id, PageParameters params, Election election, UserVoter userVoter, boolean leftPanel) {
		super(id);

		WebMarkupContainer alreadyVoted = new WebMarkupContainer("alreadyVoted");
		alreadyVoted.setVisible(userVoter.isVoted());
		add(alreadyVoted);
		WebMarkupContainer notVotedYet = new WebMarkupContainer("notVotedYet");
		notVotedYet.setVisible(!alreadyVoted.isVisible());
		add(notVotedYet);

		add(new Label("headerTitle", leftPanel ? getString("jointElection1DetailTitle") : getString("jointElection2DetailTitle")));
		add(new Label("title", election.getTitle(getLanguage(userVoter))));

		Label desc = new Label("description", election.getDescription(getLanguage(userVoter)));
		desc.setEscapeModelStrings(false);
		add(desc);

		List<Object[]> chosenCandidates = AppContext.getInstance().getVoterBeanRemote().getElectionVotesCandidateForUserVoter(userVoter.getUserVoterId(), userVoter.getElection().getElectionId());
		WebMarkupContainer voteCodesContainer = new WebMarkupContainer("voteCodesContainer");
		voteCodesContainer.setVisible(!chosenCandidates.isEmpty() && alreadyVoted.isVisible());
		add(voteCodesContainer);

		//		add(new Label("votante", userVoter.getVoterInformation()));
		//		add(new Label("cantidadVotos", userVoter.getVoteAmount()));
		//		add(new Label("maximo", String.valueOf(election.getMaxCandidates())));

		Link<Void> vote = new Link<Void>("vote") {
			private static final long serialVersionUID = 4499783887456801848L;

			public void onClick() {
				if (leftPanel) {
					params.add("token1", userVoter.getVoteToken());
					params.add("token2", "");
				} else {
					params.add("token2", userVoter.getVoteToken());
					params.add("token1", "");
				}
				setResponsePage(VoteSimpleElectionDashboard.class, params);
			}
		};
		vote.setVisible(!alreadyVoted.isVisible());
		add(vote);

		final ListView<Object[]> voteCodesListView = new ListView<Object[]>("voteCodesList", chosenCandidates) {
			private static final long serialVersionUID = 1786359392545666490L;

			@Override
			protected void populateItem(final ListItem<Object[]> item) {
				final Object[] current = item.getModelObject();
				item.add(new Label("number", String.valueOf(number)));
				Label codigo = new Label("code", (String) current[1]);
				codigo.setMarkupId("code" + item.getIndex());
				item.add(codigo);
				number = number + 1;
			}
		};
		voteCodesContainer.add(voteCodesListView);
	}

	public String getLanguage(UserVoter userVoter) {
		if (userVoter != null)
			return userVoter.getLanguage();
		else
			return SecurityUtils.getLocale().getDisplayName();
	}

}
