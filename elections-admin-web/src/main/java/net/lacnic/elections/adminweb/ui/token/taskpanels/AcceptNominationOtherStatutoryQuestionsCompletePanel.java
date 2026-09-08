package net.lacnic.elections.adminweb.ui.token.taskpanels;

import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;

public class AcceptNominationOtherStatutoryQuestionsCompletePanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;

	public AcceptNominationOtherStatutoryQuestionsCompletePanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(new GenericNominationOtherStatutoryQuestionsManagementPanel("otherStatutoryQuestionsManagementPanel", resolution));
	}
}
