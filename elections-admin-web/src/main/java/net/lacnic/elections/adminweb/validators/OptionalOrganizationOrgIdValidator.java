package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class OptionalOrganizationOrgIdValidator implements IValidator<String> {

	private static final long serialVersionUID = 145862677373532413L;
	private final String messageKey;

	public OptionalOrganizationOrgIdValidator(String messageKey) {
		this.messageKey = messageKey;
	}

	@Override
	public void validate(IValidatable<String> validatable) {
		String value = validatable.getValue();
		if (value == null || value.trim().isEmpty()) {
			return;
		}
		if (!OrganizationValidationRules.isValidOrgId(value)) {
			validatable.error(new ValidationError().addKey(messageKey));
		}
	}
}
