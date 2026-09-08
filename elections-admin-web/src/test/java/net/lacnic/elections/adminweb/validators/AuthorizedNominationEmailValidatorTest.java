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

class AuthorizedNominationEmailValidatorTest {

	@Test
	void validateDoesNotApplyWhenAuthorizedEmailsAreBlank() {
		AuthorizedNominationEmailValidator validator = new AuthorizedNominationEmailValidator("  ", "nominaciones@example.net");
		TestValidatable validatable = new TestValidatable("not-authorized@example.net");

		validator.validate(validatable);

		assertTrue(validatable.isValid());
	}

	@Test
	void validateIgnoresBlankNominationEmail() {
		AuthorizedNominationEmailValidator validator = new AuthorizedNominationEmailValidator("authorized@example.net", "nominaciones@example.net");
		TestValidatable validatable = new TestValidatable("  ");

		validator.validate(validatable);

		assertTrue(validatable.isValid());
	}

	@Test
	void validateAcceptsConfiguredEmailIgnoringCaseAndSpaces() {
		AuthorizedNominationEmailValidator validator = new AuthorizedNominationEmailValidator(" one@example.net, Two@Example.net ", "nominaciones@example.net");
		TestValidatable validatable = new TestValidatable(" two@example.NET ");

		validator.validate(validatable);

		assertTrue(validatable.isValid());
	}

	@Test
	void validateRejectsEmailOutsideConfiguredList() {
		AuthorizedNominationEmailValidator validator = new AuthorizedNominationEmailValidator("one@example.net,two@example.net", "nominaciones@example.net");
		TestValidatable validatable = new TestValidatable("three@example.net");

		validator.validate(validatable);

		assertFalse(validatable.isValid());
		ValidationError error = assertInstanceOf(ValidationError.class, validatable.getError());
		assertEquals("nominationCandidateEmailUnauthorized", error.getKeys().get(0));
		assertEquals("nominaciones@example.net", error.getVariables().get("nominationRecipient"));
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
