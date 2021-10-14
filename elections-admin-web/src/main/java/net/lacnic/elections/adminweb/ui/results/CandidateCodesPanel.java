package net.lacnic.elections.adminweb.ui.results;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;


public class CandidateCodesPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;


	public CandidateCodesPanel(long electionId) {
		this("candidateCodesPanel", electionId);
	}

	public CandidateCodesPanel(String id, long electionId) {
		super(id);
		final ListView<Object[]> codesListView = new ListView<Object[]>("codesList", AppContext.getInstance().getVoterBeanRemote().getElectionVotesCandidateAndCode(electionId)) {
			private static final long serialVersionUID = 1786359392545666490L;

			@Override
			protected void populateItem(final ListItem<Object[]> item) {
				final Object[] current = item.getModelObject();
				int row = item.getIndex() + 1;

				item.add(new Label("number", String.valueOf(row)));
				item.add(new Label("candidate", (String) current[0]));
				Label candidateCode = new Label("code", (String) current[1]);
				candidateCode.setMarkupId("candidateCode" + item.getIndex());
				item.add(candidateCode);
			}
		};
		add(codesListView);
	}

}