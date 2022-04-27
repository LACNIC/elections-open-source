package net.lacnic.elections.adminweb.ui.results.review;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Vote;


public class ReviewVotesListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public ReviewVotesListPanel(String id, Long electionId) {
		super(id);
		try {
			List<Vote> votes = AppContext.getInstance().getManagerBeanRemote().getElectionVotes(electionId);
			final ListView<Vote> votesListView = new ListView<Vote>("votesList", votes) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Vote> item) {
					final Vote vote = item.getModelObject();
					try {
						item.add(new Label("voteId", vote.getVoteId()));
						item.add(new Label("candidateId", vote.getCandidate().getCandidateId()));
						item.add(new Label("code", vote.getCode()));
						item.add(new Label("voteDate", vote.getVoteDate()));
						item.add(new Label("candidate", vote.getCandidate().getName()));
						item.add(new Label("ip", vote.getIp()));

						if(vote.getUserVoter() != null) {
							item.add(new Label("voteUserDate", vote.getUserVoter().getVoteDate()));
							item.add(new Label("userVoterId", vote.getUserVoter().getUserVoterId()));
							item.add(new Label("name", vote.getUserVoter().getName()));
							item.add(new Label("email", vote.getUserVoter().getMail()));
							item.add(new Label("orgid", vote.getUserVoter().getOrgID()));
							item.add(new Label("country", vote.getUserVoter().getCountry()));
							item.add(new Label("language", vote.getUserVoter().getLanguage()));
							item.add(new Label("voteAmount", vote.getUserVoter().getVoteAmount()));
							item.add(new Label("voteToken", vote.getUserVoter().getVoteToken()));
						} else {
							item.add(new Label("voteUserDate", "-"));
							item.add(new Label("userVoterId", "-"));
							item.add(new Label("name", "-"));
							item.add(new Label("email", "-"));
							item.add(new Label("orgid", "-"));
							item.add(new Label("country", "-"));
							item.add(new Label("language", "-"));
							item.add(new Label("voteAmount", "-"));
							item.add(new Label("voteToken", "-"));
						}
					} catch (Exception e) {
						appLogger.error(e);
						error(e.getMessage());
					}
				}
			};
			add(votesListView);

		} catch (Exception e) {
			appLogger.error(e);
			error(e.getMessage());
		}
	}

}
