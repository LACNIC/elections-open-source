package net.lacnic.elections.adminweb.ui.results.audit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.data.ResultDetailData;
import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.data.ElectionsResultsData;


public class MoreInformationForAuditPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public MoreInformationForAuditPanel(String id, final long electionId) {
		super(id);
		try {

			ElectionsResultsData electionsResultsData = AppContext.getInstance().getVoterBeanRemote().getElectionsResultsData(electionId);
			final int max = electionsResultsData.getMax();
			add(new Label("maxVotes", " (1 - " + max + ")"));

			final ListView<ResultDetailData> participantsListView = new ListView<ResultDetailData>("participantsList", electionsResultsData.getResultDetailData()) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<ResultDetailData> item) {
					final ResultDetailData current = item.getModelObject();

					item.add(new Label("percentage", current.getPercentageWithSymbol()));
					item.add(new Label("enabled", String.valueOf(current.getEnabled())));
					item.add(new Label("participants", String.valueOf(current.getParticipants())));
					item.add(new Label("weight", String.valueOf(current.getWeight())));
					item.add(new Label("total", current.getTotal(max)));
				}
			};
			add(participantsListView);
			add(new Label("percentageTotal", String.valueOf(electionsResultsData.getTotalPercentageWithSymbol())));
			add(new Label("enabledTotal", String.valueOf(electionsResultsData.getTotalEnabled())));
			add(new Label("participantsTotal", String.valueOf(electionsResultsData.getTotalParticipants())));
			add(new Label("totalTotal", String.valueOf(electionsResultsData.getTotalTotalPossible())));

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}
