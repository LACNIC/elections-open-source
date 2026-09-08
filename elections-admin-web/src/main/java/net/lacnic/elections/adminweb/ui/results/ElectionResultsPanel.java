package net.lacnic.elections.adminweb.ui.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.components.WinnerPopoverBadgePanel;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.services.publicelection.PublicElectionVoteCountRow;

public class ElectionResultsPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");


	public ElectionResultsPanel(String id, final long electionId) {
		super(id);
		try {
			List<Candidate> candidatesList = AppContext.getInstance().getVoterBeanRemote().getElectionCandidates(electionId);
			List<PublicElectionVoteCountRow> voteCountRows = AppContext.getInstance().getManagerBeanRemote().getElectionVoteCountRowsForPublicElectionPage(Long.valueOf(electionId));
			final Map<Long, Long> votesByCandidateId = buildVotesByCandidateId(voteCountRows);
			final long totalVotes = calculateTotalVotes(voteCountRows);
			final List<ResultRow> resultRows = buildResultRows(candidatesList, votesByCandidateId, totalVotes, getLocale());

			Collections.sort(resultRows, new Comparator<ResultRow>() {
				@Override
				public int compare(ResultRow row1, ResultRow row2) {
					int votesCompare = Long.compare(row2.getVotes(), row1.getVotes());
					if (votesCompare != 0) {
						return votesCompare;
					}
					return Long.compare(row1.getCandidateId(), row2.getCandidateId());
				}
			});

			final ListView<ResultRow> candidatesListView = new ListView<ResultRow>("candidatesList", resultRows) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<ResultRow> item) {
					final ResultRow row = item.getModelObject();
					try {
						Label candidateName = new Label("name", row.getCandidateName());
						candidateName.setMarkupId("candidateName" + item.getIndex());
						item.add(candidateName);
						item.add(new WinnerPopoverBadgePanel("winnerBadge", row.isWinner()));
						Label candidateVotes = new Label("votes", String.valueOf(row.getVotes()));
						candidateVotes.setMarkupId("candidateVotes" + item.getIndex());
						item.add(candidateVotes);
						item.add(new Label("percentage", row.getPercentageLabel()));
					} catch (Exception e) {
						appLogger.error(e.getMessage(), e);
					}
				}
			};
			add(candidatesListView);
			add(new Label("totalVotes", String.valueOf(totalVotes)));
			add(new Label("totalPercentage", totalVotes <= 0L ? "0%" : "100%"));
			add(new Label("totalVoters", String.valueOf(AppContext.getInstance().getVoterBeanRemote().getElectionUserVotersVotedAmount(electionId))));

		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

	static Map<Long, Long> buildVotesByCandidateId(List<PublicElectionVoteCountRow> voteCountRows) {
		Map<Long, Long> votesByCandidateId = new HashMap<>();
		if (voteCountRows == null) {
			return votesByCandidateId;
		}
		for (PublicElectionVoteCountRow row : voteCountRows) {
			if (row == null || row.getCandidateId() <= 0L || row.getVoteCount() <= 0L) {
				continue;
			}
			votesByCandidateId.put(Long.valueOf(row.getCandidateId()), Long.valueOf(row.getVoteCount()));
		}
		return votesByCandidateId;
	}

	static long calculateTotalVotes(List<PublicElectionVoteCountRow> voteCountRows) {
		long totalVotes = 0L;
		if (voteCountRows == null) {
			return totalVotes;
		}
		for (PublicElectionVoteCountRow row : voteCountRows) {
			if (row == null || row.getCandidateId() <= 0L || row.getVoteCount() <= 0L) {
				continue;
			}
			totalVotes += row.getVoteCount();
		}
		return totalVotes;
	}

	static List<ResultRow> buildResultRows(List<Candidate> candidates, Map<Long, Long> votesByCandidateId, long totalVotes, Locale locale) {
		List<ResultRow> rows = new ArrayList<>();
		if (candidates == null) {
			return rows;
		}
		for (Candidate candidate : candidates) {
			if (candidate == null || candidate.getCandidateId() <= 0L) {
				continue;
			}
			long candidateId = candidate.getCandidateId();
			long votes = votesByCandidateId != null && votesByCandidateId.containsKey(Long.valueOf(candidateId)) ? votesByCandidateId.get(Long.valueOf(candidateId)).longValue() : 0L;
			double percentage = totalVotes > 0L ? (100.0d * votes) / (double) totalVotes : 0.0d;
			rows.add(new ResultRow(candidateId, candidate.getName(), votes, percentage, candidate.isWinner(), locale));
		}
		return rows;
	}

	static class ResultRow implements Serializable {
		private static final long serialVersionUID = -6085810287980512280L;
		private final long candidateId;
		private final String candidateName;
		private final long votes;
		private final double percentage;
		private final boolean winner;
		private final Locale locale;

		ResultRow(long candidateId, String candidateName, long votes, double percentage, boolean winner, Locale locale) {
			this.candidateId = candidateId;
			this.candidateName = candidateName;
			this.votes = votes;
			this.percentage = percentage;
			this.winner = winner;
			this.locale = locale != null ? locale : Locale.ROOT;
		}

		long getCandidateId() {
			return candidateId;
		}

		String getCandidateName() {
			return candidateName;
		}

		long getVotes() {
			return votes;
		}

		boolean isWinner() {
			return winner;
		}

		String getPercentageLabel() {
			return String.format(locale, "%.1f%%", percentage);
		}
	}
}
