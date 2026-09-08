package net.lacnic.elections.adminweb.ui.token.panel;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;

public class PublicElectionSnapshotPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public PublicElectionSnapshotPanel(String id, Component feedbackPanel, Component snapshotStatus,
			Component landingContainer, Component nominateContainer, Component candidateContainer, Component rollContainer) {
		super(id);
		add(feedbackPanel);
		add(snapshotStatus);
		add(landingContainer);
		add(nominateContainer);
		add(candidateContainer);
		add(rollContainer);
	}
}
