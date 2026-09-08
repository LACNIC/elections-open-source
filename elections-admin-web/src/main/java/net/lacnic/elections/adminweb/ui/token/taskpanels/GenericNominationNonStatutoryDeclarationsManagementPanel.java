package net.lacnic.elections.adminweb.ui.token.taskpanels;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.token.AcceptNominationTaskResolution;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateDeclarationsDefinition;

public class GenericNominationNonStatutoryDeclarationsManagementPanel extends GenericNominationDeclarationsManagementPanel {

	private static final long serialVersionUID = 1L;

	public GenericNominationNonStatutoryDeclarationsManagementPanel(String id, AcceptNominationTaskResolution resolution) {
		super(id, resolution);
	}

	@Override
	protected CandidateDeclarationsDefinition loadDeclarationsDefinition(String token, String language) {
		return AppContext.getInstance().getPreNominationBeanRemote().getCandidateNonStatutoryDeclarationsDefinition(token, language);
	}

	@Override
	protected Candidate saveDeclarations(String token, Candidate candidateData, String actor, String clientIp) {
		return AppContext.getInstance().getPreNominationBeanRemote().saveCandidateNonStatutoryDeclarations(token, candidateData, actor, clientIp);
	}
}
