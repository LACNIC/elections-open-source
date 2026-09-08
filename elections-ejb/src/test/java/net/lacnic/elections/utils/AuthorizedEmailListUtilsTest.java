package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class AuthorizedEmailListUtilsTest {

	@Test
	void parseAuthorizedEmailsNormalizesAndDeduplicatesValues() {
		Set<String> emails = AuthorizedEmailListUtils.parseAuthorizedEmails(" One@example.net, two@example.net ,one@example.net , ");

		assertEquals(Set.of("one@example.net", "two@example.net"), emails);
	}

	@Test
	void isEmailAuthorizedReturnsTrueWhenRestrictionListIsBlank() {
		assertTrue(AuthorizedEmailListUtils.isEmailAuthorized("candidate@example.net", "   "));
	}

	@Test
	void isEmailAuthorizedMatchesIgnoringCaseAndSpaces() {
		assertTrue(AuthorizedEmailListUtils.isEmailAuthorized(" Candidate@Example.NET ", "one@example.net, candidate@example.net"));
	}

	@Test
	void isEmailAuthorizedRejectsEmailOutsideConfiguredList() {
		assertFalse(AuthorizedEmailListUtils.isEmailAuthorized("candidate@example.net", "one@example.net,two@example.net"));
	}
}
