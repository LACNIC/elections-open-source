package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class ConstantsTest {

	@Test
	void urlBuildersShouldComposePathWithWarSegment() {
		String base = "https://example.org";
		assertEquals("https://example.org/elections/vote", Constants.getVotesURL(base));
		assertEquals("https://example.org/elections/result", Constants.getResultsURL(base));
		assertEquals("https://example.org/elections/token/vote", Constants.getTokenVoteURL(base));
		assertEquals("https://example.org/elections/token/public-election", Constants.getTokenQuestionURL(base));
	}

	@Test
	void urlBuildersShouldPreserveInputIfAlreadyEmptyOrNullByContract() {
		assertEquals("elections/vote", Constants.getVotesURL(""));
		assertEquals("null/elections/vote", Constants.getVotesURL(null));
	}

	@Test
	void supportedRolesShouldIncludeManagerAndRestrictedProfiles() {
		Set<String> roles = ElectionsRoles.supportedPortalRoles();
		assertEquals(4, roles.size());
		assertTrue(roles.contains(ElectionsRoles.ELECTIONS_MANAGER));
		assertTrue(roles.contains(ElectionsRoles.ELECTIONS_DELETER));
		assertTrue(roles.contains(ElectionsRoles.ELECTIONS_STATUTARY_ONLY));
		assertTrue(roles.contains(ElectionsRoles.ELECTIONS_NON_STATUTARY_ONLY));
	}

	@Test
	void urlAndLanguageConstantsShouldHaveExpectedDefaults() {
		assertNotNull(Constants.DEFAULT_EMAIL_LANGUAGE);
		assertEquals("SP", Constants.DEFAULT_EMAIL_LANGUAGE.getCode());
		assertEquals("api-Elections", Constants.api_elections);
	}
}
