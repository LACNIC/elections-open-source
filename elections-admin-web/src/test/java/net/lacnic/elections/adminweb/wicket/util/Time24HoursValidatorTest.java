package net.lacnic.elections.adminweb.wicket.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;
import org.junit.jupiter.api.Test;

class Time24HoursValidatorTest {

	private static final String EXPECTED_ERROR_MESSAGE = "Ingrese el formato correcto para la hora inicio/fin (hh:mm)";

	private final Time24HoursValidator validator = new Time24HoursValidator();

	@Test
	void validateAccepts24HourValidValues() {
		String[] validValues = { "0:00", "09:30", "23:59" };

		for (String value : validValues) {
			TestValidatable validatable = new TestValidatable(value);
			validator.validate(validatable);
			assertTrue(validatable.isValid(), "Se esperaba valor válido para: " + value);
		}
	}

	@Test
	void validateRejectsInvalidValuesAndAddsErrorMessage() {
		String[] invalidValues = { "24:00", "12:60", "12-30", "9:5", "99:99", "abcd" };

		for (String value : invalidValues) {
			TestValidatable validatable = new TestValidatable(value);
			validator.validate(validatable);

			assertFalse(validatable.isValid(), "Se esperaba valor inválido para: " + value);
			ValidationError error = assertInstanceOf(ValidationError.class, validatable.getError());
			assertEquals(EXPECTED_ERROR_MESSAGE, error.getMessage());
		}
	}

	private static final class TestValidatable implements IValidatable<String> {
		private final String value;
		private IValidationError error;

		private TestValidatable(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void error(IValidationError error) {
			this.error = error;
		}

		@Override
		public boolean isValid() {
			return error == null;
		}

		@Override
		public IModel<String> getModel() {
			return null;
		}

		private IValidationError getError() {
			return error;
		}
	}
}
