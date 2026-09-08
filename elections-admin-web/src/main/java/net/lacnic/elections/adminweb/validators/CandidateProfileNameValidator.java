package net.lacnic.elections.adminweb.validators;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class CandidateProfileNameValidator implements IValidator<String> {

	private static final long serialVersionUID = 1L;
	private final int minimumLength;
	private final int maximumLength;

	public CandidateProfileNameValidator(int minimumLength, int maximumLength) {
		this.minimumLength = minimumLength;
		this.maximumLength = maximumLength;
	}

	@Override
	public void validate(IValidatable<String> validatable) {
		String value = StringUtils.trimToNull(validatable.getValue());
		if (value == null) {
			return;
		}

		if (value.length() > maximumLength) {
			validatable.error(new ValidationError().addKey("candidateProfileNameMaxLength"));
			return;
		}

		String[] words = StringUtils.split(value);
		if (value.length() < minimumLength || words == null || words.length < 2) {
			validatable.error(new ValidationError().addKey("candidateProfileNameFormatError"));
		}
	}
}
