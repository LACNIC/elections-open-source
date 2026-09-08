package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class OrganizationCnpjValidator implements IValidator<String> {

	private static final long serialVersionUID = 4620409213959022774L;

	@Override
	public void validate(IValidatable<String> validatable) {
		if (!OrganizationValidationRules.isValidCnpj(validatable.getValue())) {
			validatable.error(new ValidationError().addKey("organizationsManagementValidationCnpjInvalid"));
		}
	}
}
