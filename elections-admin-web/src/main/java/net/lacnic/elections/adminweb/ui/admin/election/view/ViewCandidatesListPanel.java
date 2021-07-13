package net.lacnic.elections.adminweb.ui.admin.election.view;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Candidate;


public class ViewCandidatesListPanel extends Panel {

	private static final long serialVersionUID = -1239455534678268981L;


	public ViewCandidatesListPanel(String id, long electionId) {
		super(id);

		ListView<Candidate> candidatesListView = new ListView<Candidate>("candidatesList", AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(electionId)) {
			private static final long serialVersionUID = 4017591177604969632L;

			@Override
			protected void populateItem(final ListItem<Candidate> item) {
				final Candidate current = item.getModelObject();
				item.add(new ViewCandidatePanel("candidate", current));
			}
		};
		add(candidatesListView);
	}

}
