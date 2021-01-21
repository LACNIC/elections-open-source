package net.lacnic.siselecciones.admin.wicket.util;

import java.util.regex.Pattern;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class Time24HoursValidator implements IValidator<String> {

	private static final long serialVersionUID = -6437141758731748938L;

	private Pattern pattern;

	private static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

	public Time24HoursValidator() {
		pattern = Pattern.compile(TIME24HOURS_PATTERN);
	}

	@Override
	public void validate(IValidatable<String> validatable) {
		final String field = validatable.getValue();

		if (!pattern.matcher(field).matches()) {
			error(validatable, "Ingrese el formato correcto para la hora inicio/fin (hh:mm)");

		}
	}

	private void error(IValidatable<String> validatable, String errorKey) {
		ValidationError error = new ValidationError(errorKey);
		validatable.error(error);
	}

}
