package net.lacnic.elections.adminweb.ui.admin.election.charts;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

public class CountryParticipationReportPanel extends Panel {

	private static final long serialVersionUID = -2729929410032318773L;

	public CountryParticipationReportPanel(String id, CountryParticipationStats stats) {
		super(id);

		Label noDataText = new Label("noData", new ResourceModel("dshbStatsNoData"));
		add(noDataText);

		WebMarkupContainer tableContainer = new WebMarkupContainer("tableContainer");
		add(tableContainer);

		if (stats == null || !stats.hasCountries()) {
			noDataText.setVisible(true);
			tableContainer.setVisible(false);
			return;
		}

		noDataText.setVisible(false);
		tableContainer.add(new ListView<CountryParticipationStats.CountryReportRow>("countryRows", stats.getCountryReportRows()) {

			private static final long serialVersionUID = -8031270097277784500L;

			@Override
			protected void populateItem(ListItem<CountryParticipationStats.CountryReportRow> item) {
				CountryParticipationStats.CountryReportRow row = item.getModelObject();
				if (row.isTotalRow()) {
					item.add(new AttributeAppender("class", " table-light fw-bold"));
				}
				item.add(new Label("countryLabel", row.getCountryLabel()));
				item.add(new Label("totalVoters", row.getTotalVoters()));
				item.add(new Label("totalVotes", row.getTotalVotes()));
				item.add(new Label("votedVoters", row.getVotedVoters()));
				item.add(new Label("votedVotes", row.getVotedVotes()));
				item.add(new Label("notVotedVoters", row.getNotVotedVoters()));
				item.add(new Label("notVotedVotes", row.getNotVotedVotes()));
				item.add(new Label("voterPercentage", row.getVoterPercentage()));
				item.add(new Label("votePercentage", row.getVotePercentage()));
				item.add(new Label("globalVotePercentage", row.getGlobalVotePercentage()));
			}
		});
	}
}
