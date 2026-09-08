package net.lacnic.elections.adminweb.wicket.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class RestrictedCountriesFormatterTest {

	@Test
	void formatReturnsEmptyStringForNullOrEmptyInput() {
		assertEquals("", RestrictedCountriesFormatter.format(null));
		assertEquals("", RestrictedCountriesFormatter.format(Collections.emptyList()));
	}

	@Test
	void formatTrimsCountryCodesAndBuildsDisplayText() {
		String result = RestrictedCountriesFormatter.format(Arrays.asList(" AR ", "BR"));
		assertEquals("Argentina (AR), Brazil (BR)", result);
	}

	@Test
	void formatDeduplicatesCountryCodesPreservingFirstOccurrence() {
		String result = RestrictedCountriesFormatter.format(Arrays.asList("BR", " AR ", "br", "AR"));
		assertEquals("Brazil (BR), Argentina (AR)", result);
	}

	@Test
	void formatSkipsNullBlankEntriesAndFallsBackToCodeWhenNameNotFound() {
		String result = RestrictedCountriesFormatter.format(Arrays.asList(null, " ", "ZZ"));
		assertEquals("ZZ", result);
	}

	@Test
	void formatUsesProvidedLocaleForCountryNames() {
		String spanish = RestrictedCountriesFormatter.format(Arrays.asList("AR", "BR"), new Locale("es"));
		assertEquals("Argentina (AR), Brasil (BR)", spanish);

		String portuguese = RestrictedCountriesFormatter.format(Arrays.asList("AR", "BR"), new Locale("pt"));
		assertEquals("Argentina (AR), Brasil (BR)", portuguese);
	}

	@Test
	void formatKeepsExplicitIslandsNameForCaribbeanNetherlandsInAllSupportedLanguages() {
		String spanish = RestrictedCountriesFormatter.format(Arrays.asList("BQ"), new Locale("es"));
		assertEquals("Bonaire / San Eustacio / Saba (BQ)", spanish);

		String english = RestrictedCountriesFormatter.format(Arrays.asList("BQ"), Locale.ENGLISH);
		assertEquals("Bonaire / Sint Eustatius / Saba (BQ)", english);

		String portuguese = RestrictedCountriesFormatter.format(Arrays.asList("BQ"), new Locale("pt"));
		assertEquals("Bonaire / Santo Eustáquio / Saba (BQ)", portuguese);
	}
}
