package net.lacnic.elections.adminweb.ui.token.panel;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;

public class PublicElectionModernPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public PublicElectionModernPanel(String id, Component feedbackPanel, Component landingContainer,
			Component nominateContainer, Component candidateContainer, Component rollContainer) {
		super(id);
		add(feedbackPanel);
		add(landingContainer);
		add(nominateContainer);
		add(candidateContainer);
		add(rollContainer);
	}
}
