package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class HtmlSanitizerUtilsTest {

	@Test
	void sanitizeStrictShouldReturnNullInputAsNull() {
		assertNull(HtmlSanitizerUtils.sanitizeStrict(null));
	}

	@Test
	void sanitizeStrictShouldRemoveUnsafeHtmlTags() {
		String sanitized = HtmlSanitizerUtils.sanitizeStrict("<script>alert(1)</script><b>Hello</b>");
		assertEquals("Hello", sanitized);
	}

	@Test
	void sanitizeStrictToNullShouldReturnNullForBlankResult() {
		assertNull(HtmlSanitizerUtils.sanitizeStrictToNull("<script></script>   "));
	}

	@Test
	void sanitizeStrictToNullShouldTrimOutput() {
		assertEquals("Hi", HtmlSanitizerUtils.sanitizeStrictToNull("  <b>Hi</b>   "));
	}

	@Test
	void sanitizeStrictRemovesNoscriptStyleAndEventHandlers() {
		String unsafe = "<noscript><img src=x onerror=alert(1)></noscript>"
				+ "<style>body{background:url(javascript:alert(1))}</style>"
				+ "<p onclick=alert(1)>Biografía segura</p>";
		assertEquals("Biografía segura", HtmlSanitizerUtils.sanitizeStrict(unsafe));
	}

	@Test
	void sanitizeStrictPreservesAccentsAndEscapesMarkupEntities() {
		assertEquals("José &amp; Ana &lt;texto&gt;",
				HtmlSanitizerUtils.sanitizeStrict("<p>José &amp; Ana &lt;texto&gt;</p>"));
	}
}
