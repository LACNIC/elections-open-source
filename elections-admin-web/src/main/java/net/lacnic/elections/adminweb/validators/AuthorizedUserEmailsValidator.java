package net.lacnic.elections.adminweb.validators;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.EmailAddressValidator;

public class AuthorizedUserEmailsValidator implements IValidator<String> {

	private static final long serialVersionUID = 1L;
	private static final String EMPTY_VALUES_ERROR_KEY = "electionAuthorizedUserEmailsEmptyValues";
	private static final String INVALID_VALUES_ERROR_KEY = "electionAuthorizedUserEmailsInvalidValues";
	private static final String INVALID_AND_EMPTY_VALUES_ERROR_KEY = "electionAuthorizedUserEmailsInvalidAndEmptyValues";
	private static final String INVALID_EMAILS_VARIABLE = "invalidEmails";

	@Override
	public void validate(IValidatable<String> validatable) {
		String rawValue = validatable.getValue();
		if (rawValue == null || rawValue.trim().isEmpty()) {
			return;
		}

		List<String> invalidEmails = new ArrayList<>();
		boolean hasEmptyValue = false;
		String[] tokens = rawValue.split(",", -1);
		for (String token : tokens) {
			String email = token == null ? null : token.trim();
			if (email == null || email.isEmpty()) {
				hasEmptyValue = true;
				continue;
			}
			if (!isValidEmail(email)) {
				invalidEmails.add(email);
			}
		}
		if (!invalidEmails.isEmpty()) {
			ValidationError error = new ValidationError().addKey(hasEmptyValue ? INVALID_AND_EMPTY_VALUES_ERROR_KEY : INVALID_VALUES_ERROR_KEY);
			error.setVariable(INVALID_EMAILS_VARIABLE, String.join(", ", invalidEmails));
			validatable.error(error);
			return;
		}
		if (hasEmptyValue) {
			validatable.error(new ValidationError().addKey(EMPTY_VALUES_ERROR_KEY));
		}
	}

	private boolean isValidEmail(String email) {
		TokenValidatable tokenValidatable = new TokenValidatable(email);
		EmailAddressValidator.getInstance().validate(tokenValidatable);
		return tokenValidatable.isValid();
	}

	private static final class TokenValidatable implements IValidatable<String> {

		private final String value;
		private boolean valid = true;

		private TokenValidatable(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void error(IValidationError error) {
			valid = false;
		}

		@Override
		public boolean isValid() {
			return valid;
		}

		@Override
		public IModel<String> getModel() {
			return null;
		}
	}
}
