package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class OrganizationAsnValidator implements IValidator<String> {

	private static final long serialVersionUID = 8140070992486171440L;

	@Override
	public void validate(IValidatable<String> validatable) {
		if (!OrganizationValidationRules.isValidAsn(validatable.getValue())) {
			validatable.error(new ValidationError().addKey("organizationsManagementValidationAsnInvalid"));
		}
	}
}
