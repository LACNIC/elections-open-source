package net.lacnic.elections.adminweb.validators;

import java.io.Serializable;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.exception.CensusValidationException;

public class OrganizationBulkDeleteActionValidator implements Serializable {

	private static final long serialVersionUID = -446560739988338875L;

	public void validateDeleteExcelBeforeSubmit(String contentType, long electionId, byte[] content) throws CensusValidationException {
		AppContext.getInstance().getManagerBeanRemote().validateOrganizationsDeleteCanBeApplied(contentType, electionId, content);
	}

	public void validateUpsertOverwriteBeforeSubmit(String contentType, long electionId, byte[] content, boolean overwriteAllOrganizations) throws CensusValidationException {
		if (!overwriteAllOrganizations) {
			return;
		}
		AppContext.getInstance().getManagerBeanRemote().validateOrganizationsUpsertOverwriteCanBeApplied(contentType, electionId, content);
	}
}
