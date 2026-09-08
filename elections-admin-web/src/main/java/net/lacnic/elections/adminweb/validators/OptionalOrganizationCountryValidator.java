package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class OptionalOrganizationCountryValidator implements IValidator<String> {

	private static final long serialVersionUID = 3839014215558048945L;
	private final String messageKey;

	public OptionalOrganizationCountryValidator(String messageKey) {
		this.messageKey = messageKey;
	}

	@Override
	public void validate(IValidatable<String> validatable) {
		String value = validatable.getValue();
		if (value == null || value.trim().isEmpty()) {
			return;
		}
		if (!OrganizationValidationRules.isValidCountry(value)) {
			validatable.error(new ValidationError().addKey(messageKey));
		}
	}
}
