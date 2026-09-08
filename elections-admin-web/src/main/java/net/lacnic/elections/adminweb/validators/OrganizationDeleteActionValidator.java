package net.lacnic.elections.adminweb.validators;

import java.io.Serializable;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.exception.CensusValidationException;

public class OrganizationDeleteActionValidator implements Serializable {

	private static final long serialVersionUID = 2676840676989128994L;

	public void validateBeforeSubmit(long electionId, String orgId) throws CensusValidationException {
		AppContext.getInstance().getManagerBeanRemote().validateOrganizationCanBeRemoved(electionId, orgId);
	}
}
