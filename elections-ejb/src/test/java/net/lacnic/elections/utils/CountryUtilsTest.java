package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class CountryUtilsTest {

	private static final CountryUtils COUNTRY_UTILS = new CountryUtils();

	@Test
	void getDisplayLabelShouldAppendCodeForLacnicCountries() {
		String label = COUNTRY_UTILS.getDisplayLabel("VE", new Locale("es"), true);
		assertEquals("Venezuela (VE)", label);
	}

	@Test
	void getDisplayLabelShouldNotDuplicateSuffixWhenLabelAlreadyContainsCode() {
		String label = COUNTRY_UTILS.getDisplayLabel("BQ", new Locale("es"), true);
		assertEquals("Bonaire / San Eustacio / Saba (BQ)", label);
	}

	@Test
	void getDisplayLabelShouldKeepNameWithoutCodeWhenAppendFlagIsFalse() {
		String label = COUNTRY_UTILS.getDisplayLabel("AR", new Locale("es"), false);
		assertEquals("Argentina", label);
	}

	@Test
	void getDisplayLabelShouldReturnDefaultLabelForPlaceHolderCountry() {
		String label = COUNTRY_UTILS.getDisplayLabel("AA", new Locale("en"), true);
		assertEquals("Select country", label);
	}

	@Test
	void normalizeCountryCodeShouldTrimAndUppercase() {
		assertEquals("AR", COUNTRY_UTILS.normalizeCountryCode("  ar  "));
	}

	@Test
	void normalizeCountryCodeShouldReturnNullForBlankValues() {
		assertEquals(null, COUNTRY_UTILS.normalizeCountryCode("   "));
	}

	@Test
	void isLacnicCoverageCountryCodeShouldBeCaseInsensitive() {
		assertEquals(true, COUNTRY_UTILS.isLacnicCoverageCountryCode("ar"));
		assertEquals(false, COUNTRY_UTILS.isLacnicCoverageCountryCode("ZZ"));
	}

	@Test
	void getIdsListLacnicFirstShouldPreferLacnicCountriesAndPlaceHolder() {
		assertEquals("AA", COUNTRY_UTILS.getIdsListLacnicFirst(true).get(0));
		assertEquals("AR", COUNTRY_UTILS.getIdsListLacnicFirst(true).get(1));
	}

	@Test
	void getIdsListLacnicFirstShouldExcludePlaceholderWhenRequested() {
		assertFalse(COUNTRY_UTILS.getIdsListLacnicFirst(false).contains("AA"));
		assertEquals("AR", COUNTRY_UTILS.getIdsListLacnicFirst(false).get(0));
	}

	@Test
	void getLacnicCoverageIdsShouldIncludeKnownCountries() {
		assertTrue(COUNTRY_UTILS.getLacnicCoverageIds().contains("AR"));
		assertTrue(COUNTRY_UTILS.getLacnicCoverageIds().contains("PY"));
		assertTrue(COUNTRY_UTILS.getLacnicCoverageIds().contains("VE"));
	}

	@Test
	void getNameByIdShouldReturnLocalizedOrDefaultLabel() {
		assertEquals("Argentina", COUNTRY_UTILS.getNameById("AR", new Locale("es")));
		assertEquals("Argentina", COUNTRY_UTILS.getNameById("AR", new Locale("en")));
		assertEquals("Venezuela", COUNTRY_UTILS.getNameById("ve", new Locale("es")));
	}

	@Test
	void getNameByIdShouldReturnNullWhenUnknownCountry() {
		assertEquals(null, COUNTRY_UTILS.getNameById("ZZ"));
	}

	@Test
	void getIdToNameMapExcludingDefaultShouldNotContainPlaceholder() {
		assertFalse(COUNTRY_UTILS.getIdToNameMapExcludingDefault().containsKey("AA"));
		assertNotNull(COUNTRY_UTILS.getIdToNameMapExcludingDefault().get("AR"));
	}

	@Test
	void getDisplayLabelShouldFallbackToCodeWhenUnknown() {
		assertEquals("ZZ", COUNTRY_UTILS.getDisplayLabel("ZZ", new Locale("en"), true));
		assertEquals("ZZ", COUNTRY_UTILS.getDisplayLabel("ZZ", new Locale("en"), false));
	}

	@Test
	void getDisplayLabelShouldReturnEmptyForNullOrBlankCode() {
		assertEquals("", COUNTRY_UTILS.getDisplayLabel(null, new Locale("en"), true));
		assertEquals("", COUNTRY_UTILS.getDisplayLabel("   ", new Locale("en"), true));
	}
}
