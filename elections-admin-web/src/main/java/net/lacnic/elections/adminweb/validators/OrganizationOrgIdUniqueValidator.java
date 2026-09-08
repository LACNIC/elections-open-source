package net.lacnic.elections.adminweb.validators;

import java.util.List;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.pre.Organization;

public class OrganizationOrgIdUniqueValidator implements IValidator<String> {

	private static final long serialVersionUID = 7898188011300436235L;
	private final long electionId;

	public OrganizationOrgIdUniqueValidator(long electionId) {
		this.electionId = electionId;
	}

	@Override
	public void validate(IValidatable<String> validatable) {
		String incomingOrgId = OrganizationValidationRules.normalizeOrgId(validatable.getValue());
		if (incomingOrgId.isEmpty()) {
			return;
		}
		List<Organization> organizations = AppContext.getInstance().getManagerBeanRemote().getOrganizations(electionId);
		for (Organization organization : organizations) {
			if (incomingOrgId.equals(OrganizationValidationRules.normalizeOrgId(organization.getOrgId()))) {
				validatable.error(new ValidationError().addKey("organizationsManagementValidationDuplicateOrgId"));
				return;
			}
		}
	}
}
