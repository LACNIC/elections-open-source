package net.lacnic.elections.adminweb.ui.token.taskpanels;

import net.lacnic.elections.adminweb.ui.components.TaskEditDisabledInfoPanel;
import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;

public class AcceptNominationNonStatutoryDeclarationsEditPanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;

	public AcceptNominationNonStatutoryDeclarationsEditPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(new TaskEditDisabledInfoPanel("completedInfoPanel", getTaskResolution()));
	}
}
