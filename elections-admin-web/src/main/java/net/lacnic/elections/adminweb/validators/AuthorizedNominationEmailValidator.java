package net.lacnic.elections.adminweb.validators;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import net.lacnic.elections.utils.AuthorizedEmailListUtils;

public class AuthorizedNominationEmailValidator implements IValidator<String> {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_KEY = "nominationCandidateEmailUnauthorized";
	private static final String NOMINATION_RECIPIENT_VARIABLE = "nominationRecipient";

	private final Set<String> authorizedEmails;
	private final String nominationRecipient;

	public AuthorizedNominationEmailValidator(String authorizedNominateEmails, String nominationRecipient) {
		this.authorizedEmails = AuthorizedEmailListUtils.parseAuthorizedEmails(authorizedNominateEmails);
		this.nominationRecipient = nominationRecipient;
	}

	@Override
	public void validate(IValidatable<String> validatable) {
		String normalizedEmail = AuthorizedEmailListUtils.normalizeEmail(validatable.getValue());
		if (normalizedEmail == null || authorizedEmails.isEmpty()) {
			return;
		}
		if (!authorizedEmails.contains(normalizedEmail)) {
			ValidationError error = new ValidationError().addKey(ERROR_KEY);
			error.setVariable(NOMINATION_RECIPIENT_VARIABLE, StringUtils.defaultIfBlank(nominationRecipient, "-"));
			validatable.error(error);
		}
	}
}
