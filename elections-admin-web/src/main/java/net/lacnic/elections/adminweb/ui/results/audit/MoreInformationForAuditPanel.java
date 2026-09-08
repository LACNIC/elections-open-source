package net.lacnic.elections.adminweb.ui.results.audit;

import java.util.Collections;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.data.ResultDetailData;
import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.data.ElectionsResultsData;


public class MoreInformationForAuditPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");


	public MoreInformationForAuditPanel(String id, final long electionId) {
		super(id);

		// Safe defaults to keep markup/component tree consistent even if remote call fails.
		add(new Label("maxVotes", ""));
		add(new ListView<ResultDetailData>("participantsList", Collections.<ResultDetailData>emptyList()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<ResultDetailData> item) {
				// Intencionalmente vacio: placeholder de Wicket cuando no hay datos.
			}
		});
		add(new Label("percentageTotal", "-"));
		add(new Label("enabledTotal", "-"));
		add(new Label("participantsTotal", "-"));
		add(new Label("totalTotal", "-"));

		try {

			ElectionsResultsData electionsResultsData = AppContext.getInstance().getVoterBeanRemote().getElectionsResultsData(electionId);
			if (electionsResultsData == null) {
				return;
			}
			final int max = electionsResultsData.getMax();
			replace(new Label("maxVotes", " (1 - " + max + ")"));

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
			replace(participantsListView);
			replace(new Label("percentageTotal", String.valueOf(electionsResultsData.getTotalPercentageWithSymbol())));
			replace(new Label("enabledTotal", String.valueOf(electionsResultsData.getTotalEnabled())));
			replace(new Label("participantsTotal", String.valueOf(electionsResultsData.getTotalParticipants())));
			replace(new Label("totalTotal", String.valueOf(electionsResultsData.getTotalTotalPossible())));

		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

}
