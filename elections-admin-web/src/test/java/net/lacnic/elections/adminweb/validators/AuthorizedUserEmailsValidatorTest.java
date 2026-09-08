package net.lacnic.elections.adminweb.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;
import org.junit.jupiter.api.Test;

class AuthorizedUserEmailsValidatorTest {

	private final AuthorizedUserEmailsValidator validator = new AuthorizedUserEmailsValidator();

	@Test
	void validateAcceptsBlankValue() {
		TestValidatable validatable = new TestValidatable("  ");

		validator.validate(validatable);

		assertTrue(validatable.isValid());
	}

	@Test
	void validateAcceptsCommaSeparatedValidEmails() {
		TestValidatable validatable = new TestValidatable("one@example.net, two@example.net");

		validator.validate(validatable);

		assertTrue(validatable.isValid());
	}

	@Test
	void validateReportsInvalidEmailValues() {
		TestValidatable validatable = new TestValidatable("one@example.net, invalid-email, two@example.net, broken@");

		validator.validate(validatable);

		assertFalse(validatable.isValid());
		ValidationError error = assertInstanceOf(ValidationError.class, validatable.getError());
		assertEquals("electionAuthorizedUserEmailsInvalidValues", error.getKeys().get(0));
		assertEquals("invalid-email, broken@", error.getVariables().get("invalidEmails"));
	}

	@Test
	void validateReportsEmptyValues() {
		TestValidatable validatable = new TestValidatable("one@example.net, , two@example.net,");

		validator.validate(validatable);

		assertFalse(validatable.isValid());
		ValidationError error = assertInstanceOf(ValidationError.class, validatable.getError());
		assertEquals("electionAuthorizedUserEmailsEmptyValues", error.getKeys().get(0));
	}

	@Test
	void validateReportsInvalidEmailValuesAndEmptyValuesTogether() {
		TestValidatable validatable = new TestValidatable("one@example.net, invalid-email, , broken@,");

		validator.validate(validatable);

		assertFalse(validatable.isValid());
		ValidationError error = assertInstanceOf(ValidationError.class, validatable.getError());
		assertEquals("electionAuthorizedUserEmailsInvalidAndEmptyValues", error.getKeys().get(0));
		assertEquals("invalid-email, broken@", error.getVariables().get("invalidEmails"));
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
