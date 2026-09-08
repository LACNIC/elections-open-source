package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class OrganizationMembershipContactNameValidator implements IValidator<String> {

	private static final long serialVersionUID = -7934685333814833856L;

	@Override
	public void validate(IValidatable<String> validatable) {
		if (!OrganizationValidationRules.isValidMembershipContactName(validatable.getValue())) {
			validatable.error(new ValidationError().addKey("organizationsManagementValidationMembershipContactNameInvalid"));
		}
	}
}
