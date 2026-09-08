package net.lacnic.elections.adminweb.ui.token.taskpanels;

import net.lacnic.elections.adminweb.ui.components.TaskEditDisabledInfoPanel;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;

public class AcceptNominationTrainingEditPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;

	public AcceptNominationTrainingEditPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		if (getSelectedTask() != null && getSelectedTask().isCompleted()) {
			add(new TaskEditDisabledInfoPanel("contentPanel", getTaskResolution()));
			return;
		}
		add(new GenericNominationTrainingManagementPanel("contentPanel", resolution));
	}
}
