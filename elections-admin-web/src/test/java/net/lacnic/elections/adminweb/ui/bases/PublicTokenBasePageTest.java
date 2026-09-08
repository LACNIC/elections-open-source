package net.lacnic.elections.adminweb.ui.bases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.LanguageCode;

class PublicTokenBasePageTest {

	@Test
	void resolvePublicElectionTokenPrefersElectionPublicToken() {
		Election election = new Election();
		election.setPublicElectionToken("public-token");

		assertEquals("public-token", PublicTokenBasePage.resolvePublicElectionToken(election));
	}

	@Test
	void resolvePublicElectionTokenReturnsNullWhenElectionHasNoPublicToken() {
		Election election = new Election();
		election.setPublicElectionToken(" ");

		assertNull(PublicTokenBasePage.resolvePublicElectionToken(election));
	}

	@Test
	void resolvePublicElectionTokenReturnsNullWhenElectionIsMissing() {
		assertNull(PublicTokenBasePage.resolvePublicElectionToken(null));
	}

	@Test
	void resolveLocaleLanguageAcceptsSupportedLocaleParameterValues() {
		assertEquals(LanguageCode.SP, PublicTokenBasePage.resolveLocaleLanguage("es"));
		assertEquals(LanguageCode.PT, PublicTokenBasePage.resolveLocaleLanguage("pt"));
		assertEquals(LanguageCode.EN, PublicTokenBasePage.resolveLocaleLanguage("en"));
	}

	@Test
	void resolveLocaleLanguageDefaultsToSpanishForUnsupportedValues() {
		assertEquals(LanguageCode.SP, PublicTokenBasePage.resolveLocaleLanguage("fr"));
		assertEquals(LanguageCode.SP, PublicTokenBasePage.resolveLocaleLanguage(""));
		assertEquals(LanguageCode.SP, PublicTokenBasePage.resolveLocaleLanguage(null));
	}

	@Test
	void appendLocaleParameterCopiesCanonicalLocaleWhenSourceHasLocale() {
		PageParameters source = new PageParameters();
		source.add("locale", "EN");
		PageParameters target = new PageParameters();

		PublicTokenBasePage.appendLocaleParameter(target, source);

		assertEquals("en", target.get("locale").toString());
	}

	@Test
	void appendLocaleParameterDefaultsUnsupportedLocaleToSpanish() {
		PageParameters source = new PageParameters();
		source.add("locale", "de");
		PageParameters target = new PageParameters();

		PublicTokenBasePage.appendLocaleParameter(target, source);

		assertEquals("es", target.get("locale").toString());
	}

	@Test
	void appendLocaleParameterDoesNotAddLocaleWhenSourceHasNoLocale() {
		PageParameters source = new PageParameters();
		PageParameters target = new PageParameters();

		PublicTokenBasePage.appendLocaleParameter(target, source);

		assertNull(target.get("locale").toOptionalString());
	}
}
