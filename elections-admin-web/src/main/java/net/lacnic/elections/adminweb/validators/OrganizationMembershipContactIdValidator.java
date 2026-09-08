package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class OrganizationMembershipContactIdValidator implements IValidator<String> {

	private static final long serialVersionUID = -4746777800619361709L;

	@Override
	public void validate(IValidatable<String> validatable) {
		if (!OrganizationValidationRules.isValidMembershipContactId(validatable.getValue())) {
			validatable.error(new ValidationError().addKey("organizationsManagementValidationMembershipContactIdInvalid"));
		}
	}
}
