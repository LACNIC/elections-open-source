package net.lacnic.elections.adminweb.ui.token.taskpanels;

import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;

public class AcceptNominationEvaluationCompletePanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;

	public AcceptNominationEvaluationCompletePanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(new GenericNominationEvaluationManagementPanel("evaluationManagementPanel", resolution));
	}
}
