package net.lacnic.elections.adminweb.validators;

import java.io.Serializable;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.exception.CensusValidationException;

public class CensusDeleteActionValidator implements Serializable {

	private static final long serialVersionUID = -5763635616955039564L;

	public void validateBeforeSubmit(long electionId, long userVoterId) throws CensusValidationException {
		AppContext.getInstance().getManagerBeanRemote().validateUserVoterCanBeRemoved(electionId, userVoterId);
	}
}
