package net.lacnic.elections.domain;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

class ElectionAuditorResultLanguageSelectionTest extends TestCase {

	static Test suite() {
		return new TestSuite(ElectionAuditorResultLanguageSelectionTest.class);
	}

	@org.junit.jupiter.api.Test
	void testGetResultLetterReturnsRequestedLanguageWhenAvailable() {
		ElectionAuditorResult result = new ElectionAuditorResult();
		byte[] spanish = new byte[] { 1 };
		byte[] english = new byte[] { 2 };
		byte[] portuguese = new byte[] { 3 };
		result.setResultLetterSpanish(spanish);
		result.setResultLetterEnglish(english);
		result.setResultLetterPortuguese(portuguese);

		assertSame(spanish, result.getResultLetter(LanguageCode.SP));
		assertSame(english, result.getResultLetter(LanguageCode.EN));
		assertSame(portuguese, result.getResultLetter(LanguageCode.PT));
	}

	@org.junit.jupiter.api.Test
	void testGetResultReturnsRequestedLanguageWhenAvailable() {
		ElectionAuditorResult result = new ElectionAuditorResult();
		result.setResultSpanish("es");
		result.setResultEnglish("en");
		result.setResultPortuguese("pt");

		assertEquals("es", result.getResult(LanguageCode.SP));
		assertEquals("en", result.getResult(LanguageCode.EN));
		assertEquals("pt", result.getResult(LanguageCode.PT));
	}
}
