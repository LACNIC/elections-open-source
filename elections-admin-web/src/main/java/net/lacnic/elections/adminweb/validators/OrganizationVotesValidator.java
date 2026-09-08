package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class OrganizationVotesValidator implements IValidator<Integer> {

	private static final long serialVersionUID = -523263060679076550L;

	@Override
	public void validate(IValidatable<Integer> validatable) {
		if (!OrganizationValidationRules.isValidVotes(validatable.getValue())) {
			validatable.error(new ValidationError().addKey("organizationsManagementValidationVotesInvalid"));
		}
	}
}
