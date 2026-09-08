package net.lacnic.elections.adminweb.validators;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class CandidateProfileLinkedinUrlValidator implements IValidator<String> {

	private static final long serialVersionUID = 1L;
	private final int maximumLength;

	public CandidateProfileLinkedinUrlValidator(int maximumLength) {
		this.maximumLength = maximumLength;
	}

	@Override
	public void validate(IValidatable<String> validatable) {
		String value = StringUtils.trimToNull(validatable.getValue());
		if (value == null) {
			return;
		}

		if (value.length() > maximumLength) {
			validatable.error(new ValidationError().addKey("candidateProfileLinkedInMaxLength"));
			return;
		}

		if (!isValidLinkedinUrl(value)) {
			validatable.error(new ValidationError().addKey("candidateProfileLinkedInInvalid"));
		}
	}

	private boolean isValidLinkedinUrl(String url) {
		try {
			URI uri = URI.create(url);
			String scheme = StringUtils.lowerCase(uri.getScheme());
			String host = StringUtils.lowerCase(StringUtils.removeEnd(StringUtils.defaultString(uri.getHost()), "."));
			boolean validScheme = "https".equals(scheme);
			boolean validLinkedinHost = "linkedin.com".equals(host) || StringUtils.endsWith(host, ".linkedin.com");
			return validScheme && validLinkedinHost;
		} catch (Exception ex) {
			return false;
		}
	}
}
