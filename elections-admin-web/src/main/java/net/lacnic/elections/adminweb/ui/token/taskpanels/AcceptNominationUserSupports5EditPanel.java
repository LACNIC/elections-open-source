package net.lacnic.elections.adminweb.ui.token.taskpanels;

import net.lacnic.elections.adminweb.ui.components.TaskEditDisabledInfoPanel;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;

public class AcceptNominationUserSupports5EditPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;

	public AcceptNominationUserSupports5EditPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(new TaskEditDisabledInfoPanel("completedInfoPanel", getTaskResolution()));
	}
}
