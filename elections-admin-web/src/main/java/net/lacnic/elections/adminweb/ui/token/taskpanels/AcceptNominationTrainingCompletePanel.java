package net.lacnic.elections.adminweb.ui.token.taskpanels;

import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;

public class AcceptNominationTrainingCompletePanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;

	public AcceptNominationTrainingCompletePanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(new AcceptNominationTrainingCompleteStatePanelFactory().create("trainingManagementPanel", resolution));
	}
}
