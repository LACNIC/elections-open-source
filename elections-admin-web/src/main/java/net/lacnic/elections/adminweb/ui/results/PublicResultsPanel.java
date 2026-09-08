package net.lacnic.elections.adminweb.ui.results;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.domain.Election;

public class PublicResultsPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public PublicResultsPanel(String id, Election election, String language) {
		super(id);

		add(new Label("title", election.getTitle(language)));
		Label description = new Label("description", election.getDescription(language));
		description.setEscapeModelStrings(false);
		add(description);
		add(new ElectionResultsPanel("resultsPanel", election.getElectionId()));
		add(new CandidateCodesPanel("candidateCodesPanel", election.getElectionId()));
	}
}
