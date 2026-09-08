package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class OrganizationOrgIdValidator implements IValidator<String> {

	private static final long serialVersionUID = 4244007620648179086L;

	@Override
	public void validate(IValidatable<String> validatable) {
		if (!OrganizationValidationRules.isValidOrgId(validatable.getValue())) {
			validatable.error(new ValidationError().addKey("organizationsManagementValidationOrgIdInvalid"));
		}
	}
}
