package net.lacnic.elections.adminweb.ui.token.taskpanels;

import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;

public class AcceptNominationOtherNonStatutoryQuestionsCompletePanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;

	public AcceptNominationOtherNonStatutoryQuestionsCompletePanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(new GenericNominationOtherNonStatutoryQuestionsManagementPanel("otherNonStatutoryQuestionsManagementPanel", resolution));
	}
}
