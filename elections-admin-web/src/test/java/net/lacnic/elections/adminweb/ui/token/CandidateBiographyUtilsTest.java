package net.lacnic.elections.adminweb.ui.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CandidateBiographyUtilsTest {

	@Test
	void toRenderableMarkupReturnsInputForNullBlankAndDash() {
		assertNull(CandidateBiographyUtils.toRenderableMarkup(null));
		assertEquals("   ", CandidateBiographyUtils.toRenderableMarkup("   "));
		assertEquals(" - ", CandidateBiographyUtils.toRenderableMarkup(" - "));
	}

	@Test
	void toRenderableMarkupReturnsOriginalWhenHtmlIsPresent() {
		String biography = "<p>Line 1</p><div>Line 2</div>";
		assertEquals(biography, CandidateBiographyUtils.toRenderableMarkup(biography));
	}

	@Test
	void toRenderableMarkupConvertsLineBreaksWhenHtmlIsNotPresent() {
		String biography = "Line 1\r\nLine 2\rLine 3\nLine 4";
		assertEquals("Line 1<br/>Line 2<br/>Line 3<br/>Line 4", CandidateBiographyUtils.toRenderableMarkup(biography));
	}

	@Test
	void toPlainTextSnippetReturnsInputForNullBlankAndDash() {
		assertNull(CandidateBiographyUtils.toPlainText(null));
		assertEquals("   ", CandidateBiographyUtils.toPlainText("   "));
		assertEquals("-", CandidateBiographyUtils.toPlainText("-"));
	}

	@Test
	void toPlainTextRemovesHtmlAndUnescapesEntities() {
		String biography = "<p>One&nbsp;&amp;&nbsp;Two</p><li>Three</li><div>&lt;Four&#39;s&gt;</div>";
		assertEquals("One & Two - Three <Four's>", CandidateBiographyUtils.toPlainText(biography));
	}

	@Test
	void toPlainTextPreservesAccentsAndSpecialCharacters() {
		String biography = "<p>Biograf&iacute;a de Jos&eacute; Mu&ntilde;oz &amp; A&ccedil;&atilde;o</p>";
		assertEquals("Biografía de José Muñoz & Ação", CandidateBiographyUtils.toPlainText(biography));
	}

	@Test
	void toPlainTextNormalizesBreakTagsAndWhitespace() {
		String biography = "Line 1<br>Line 2<br/>Line 3\r\nLine 4";
		assertEquals("Line 1 Line 2 Line 3 Line 4", CandidateBiographyUtils.toPlainText(biography));
	}

	@Test
	void toPlainTextRemovesScriptAndStyleContent() {
		String biography = "<style>.x{color:red}</style><p>Visible</p><script>alert('x')</script><div>Text</div>";
		assertEquals("Visible Text", CandidateBiographyUtils.toPlainText(biography));
	}

	@Test
	void toPlainTextSnippetTruncatesAfterPlainTextConversion() {
		String biography = "<p>Uno dos tres cuatro cinco seis siete ocho nueve diez</p>";
		assertEquals("Uno dos tres cuatro...", CandidateBiographyUtils.toPlainTextSnippet(biography, 24));
	}
}
