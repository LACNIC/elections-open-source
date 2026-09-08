package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class NonBlankStringValidator implements IValidator<String> {

	private static final long serialVersionUID = -7670748645386515159L;
	private final String messageKey;

	public NonBlankStringValidator(String messageKey) {
		this.messageKey = messageKey;
	}

	@Override
	public void validate(IValidatable<String> validatable) {
		String value = validatable.getValue();
		if (value == null || value.trim().isEmpty()) {
			validatable.error(new ValidationError().addKey(messageKey));
		}
	}
}
