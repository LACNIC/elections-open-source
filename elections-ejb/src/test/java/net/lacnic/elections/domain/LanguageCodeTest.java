package net.lacnic.elections.domain;

import java.util.Arrays;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

 class LanguageCodeTest extends TestCase {

	 static Test suite() {
		return new TestSuite(LanguageCodeTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testFromValueAcceptsAliasesAndNormalizeInput() {
		assertEquals(LanguageCode.SP, LanguageCode.fromValue("es"));
		assertEquals(LanguageCode.SP, LanguageCode.fromValue("  Es_MX "));
		assertEquals(LanguageCode.EN, LanguageCode.fromValue("en-us"));
		assertEquals(LanguageCode.EN, LanguageCode.fromValue("english"));
		assertEquals(LanguageCode.PT, LanguageCode.fromValue("português"));
	}

	@org.junit.jupiter.api.Test

	 void testFromValueReturnsNullForBlankOrUnknownValues() {
		assertNull(LanguageCode.fromValue(null));
		assertNull(LanguageCode.fromValue("   "));
		assertNull(LanguageCode.fromValue("zz"));
	}

	@org.junit.jupiter.api.Test

	 void testFromValueOrDefaultUsesDefaultLanguage() {
		assertEquals(LanguageCode.EN, LanguageCode.fromValueOrDefault("en", LanguageCode.SP));
		assertEquals(LanguageCode.PT, LanguageCode.fromValueOrDefault("xx", LanguageCode.PT));
		assertEquals(LanguageCode.SP, LanguageCode.fromValueOrDefault("", LanguageCode.SP));
	}

	@org.junit.jupiter.api.Test

	 void testFromLocaleAndCanonicalCodes() {
		assertEquals(LanguageCode.PT, LanguageCode.fromLocale(Locale.forLanguageTag("pt-PT")));
		assertNull(LanguageCode.fromLocale(Locale.ROOT));
		assertEquals(Arrays.asList("SP", "EN", "PT"), LanguageCode.canonicalCodes());
	}

	@org.junit.jupiter.api.Test

	 void testFromLocaleSupportsLowercaseCountryVariants() {
		assertEquals(LanguageCode.EN, LanguageCode.fromLocale(Locale.US));
		assertEquals(LanguageCode.PT, LanguageCode.fromLocale(Locale.forLanguageTag("pt-br")));
		assertEquals(null, LanguageCode.fromLocale(Locale.CHINA));
	}

	@org.junit.jupiter.api.Test

	 void testToLocaleReturnsExpectedLocale() {
		assertEquals("es", LanguageCode.SP.toLocale().getLanguage());
		assertEquals("en", LanguageCode.EN.toLocale().getLanguage());
		assertEquals("pt", LanguageCode.PT.toLocale().getLanguage());
	}

	@org.junit.jupiter.api.Test

	 void testCanonicalCodesAreNotEmpty() {
		assertEquals(3, LanguageCode.canonicalCodes().size());
		assertEquals("SP", LanguageCode.canonicalCodes().get(0));
	}
}
