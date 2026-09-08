package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class OrganizationCountryValidator implements IValidator<String> {

	private static final long serialVersionUID = 2050540737211441641L;

	@Override
	public void validate(IValidatable<String> validatable) {
		if (!OrganizationValidationRules.isValidCountry(validatable.getValue())) {
			validatable.error(new ValidationError().addKey("organizationsManagementValidationCountryInvalid"));
		}
	}
}
