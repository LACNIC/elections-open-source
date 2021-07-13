package net.lacnic.elections.adminweb.ui.results;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Candidate;


public class ElectionResultsPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public ElectionResultsPanel(String id, final long electionId) {
		super(id);
		try {
			List<Candidate> candidatesList = AppContext.getInstance().getVoterBeanRemote().getElectionCandidates(electionId);
			Collections.sort(candidatesList, new Comparator<Candidate>() {
				@Override
				public int compare(Candidate candidate1, Candidate candidate2) {
					try {
						return AppContext.getInstance().getVoterBeanRemote().getCandidateVotesAmount(candidate1.getCandidateId()) > AppContext.getInstance().getVoterBeanRemote().getCandidateVotesAmount(candidate2.getCandidateId()) ? -1 : 1;
					} catch (Exception e) {
						appLogger.error(e);
						return 0;
					}
				}
			});

			final ListView<Candidate> candidatesListView = new ListView<Candidate>("candidatesList", candidatesList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Candidate> item) {
					final Candidate candidate = item.getModelObject();
					try {
						Label candidateName = new Label("name", candidate.getName());
						candidateName.setMarkupId("candidateName" + item.getIndex());
						item.add(candidateName);
						Label candidateVotes = new Label("votes", String.valueOf(AppContext.getInstance().getVoterBeanRemote().getCandidateVotesAmount(candidate.getCandidateId())));
						candidateVotes.setMarkupId("candidateVotes" + item.getIndex());
						item.add(candidateVotes);
					} catch (Exception e) {
						appLogger.error(e);
					}
				}
			};
			add(candidatesListView);
			add(new Label("totalVotes", String.valueOf(AppContext.getInstance().getVoterBeanRemote().getElectionVotesAmount(electionId))));
			add(new Label("totalVoters", String.valueOf(AppContext.getInstance().getVoterBeanRemote().getElectionUserVotersVotedAmount(electionId))));

		} catch (Exception e) {
			appLogger.error(e);
		}
	}
}