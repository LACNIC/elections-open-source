package net.lacnic.elections.adminweb.ui.token.taskpanels;

import net.lacnic.elections.adminweb.ui.token.AbstractAcceptNominationTaskPanel;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.domain.pre.ElectionTaskKey;

public class AcceptNominationUserSupports2CompletePanel extends AbstractAcceptNominationTaskPanel {

	private static final long serialVersionUID = 1L;

	public AcceptNominationUserSupports2CompletePanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
		add(new GenericNominationUserSupportsManagementPanel("userSupportsManagementPanel", resolution, ElectionTaskKey.USER_SUPPORTS_2, 2));
	}
}
