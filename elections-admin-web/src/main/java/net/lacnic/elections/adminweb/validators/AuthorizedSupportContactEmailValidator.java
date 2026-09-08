package net.lacnic.elections.adminweb.validators;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import net.lacnic.elections.utils.AuthorizedEmailListUtils;

public class AuthorizedSupportContactEmailValidator implements IValidator<String> {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_KEY = "acceptNominationUserSupportsUnauthorizedEmail";
	private static final String SUPPORT_RECIPIENT_VARIABLE = "supportRecipient";

	private final Set<String> authorizedEmails;
	private final String supportRecipient;

	public AuthorizedSupportContactEmailValidator(String authorizedSupportEmails, String supportRecipient) {
		this.authorizedEmails = AuthorizedEmailListUtils.parseAuthorizedEmails(authorizedSupportEmails);
		this.supportRecipient = supportRecipient;
	}

	@Override
	public void validate(IValidatable<String> validatable) {
		String normalizedEmail = AuthorizedEmailListUtils.normalizeEmail(validatable.getValue());
		if (normalizedEmail == null || authorizedEmails.isEmpty()) {
			return;
		}
		if (!authorizedEmails.contains(normalizedEmail)) {
			ValidationError error = new ValidationError().addKey(ERROR_KEY);
			error.setVariable(SUPPORT_RECIPIENT_VARIABLE, StringUtils.defaultIfBlank(supportRecipient, "-"));
			validatable.error(error);
		}
	}
}
