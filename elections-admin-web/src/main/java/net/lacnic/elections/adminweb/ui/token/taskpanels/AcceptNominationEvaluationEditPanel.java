package net.lacnic.elections.adminweb.ui.token.taskpanels;

import net.lacnic.elections.adminweb.ui.components.TaskEditDisabledInfoPanel;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;

public class AcceptNominationEvaluationEditPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;

	public AcceptNominationEvaluationEditPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		if (getSelectedTask() != null && getSelectedTask().isCompleted()) {
			add(new TaskEditDisabledInfoPanel("contentPanel", getTaskResolution()));
			return;
		}
		add(new GenericNominationEvaluationManagementPanel("contentPanel", resolution));
	}
}
