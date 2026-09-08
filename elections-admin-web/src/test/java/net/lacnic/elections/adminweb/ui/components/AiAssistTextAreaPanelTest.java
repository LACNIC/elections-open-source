package net.lacnic.elections.adminweb.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.utils.Constants;

class AiAssistTextAreaPanelTest {

	@Test
	void normalizeImprovedTextReturnsNullForNullInput() {
		assertNull(AiAssistTextAreaPanel.normalizeImprovedText(null));
	}

	@Test
	void normalizeImprovedTextDecodesApostrophesAndAccents() {
		String value = "&#39;a&#39;e aaee&eacute; &iacute; Mu&ntilde;oz A&ccedil;&atilde;o";
		assertEquals("'a'e aaeeé í Muñoz Ação", AiAssistTextAreaPanel.normalizeImprovedText(value));
	}

	@Test
	void normalizeImprovedTextKeepsPlainTextUnchanged() {
		String value = "'a'e aaeeé í Muñoz Ação";
		assertEquals(value, AiAssistTextAreaPanel.normalizeImprovedText(value));
	}

	@Test
	void aiTextImprovementIsHiddenWithoutCompleteConfiguration() {
		assertFalse(AiAssistTextAreaPanel.isAiTextImprovementConfigured(null));
		assertFalse(AiAssistTextAreaPanel.isAiTextImprovementConfigured(Collections.emptyList()));
		assertFalse(AiAssistTextAreaPanel.isAiTextImprovementConfigured(Arrays.asList(
				parameter(Constants.OPENAI_URL, "https://ai.example.test"),
				parameter(Constants.OPENAI_API_KEY, " "))));
	}

	@Test
	void aiTextImprovementIsHiddenWhenFeatureIsDisabled() {
		assertFalse(AiAssistTextAreaPanel.isAiTextImprovementConfigured(Arrays.asList(
				parameter(Constants.OPENAI_URL, "https://ai.example.test"),
				parameter(Constants.OPENAI_API_KEY, "secret"),
				parameter(Constants.AI_TEXT_IMPROVEMENT_ENABLED, "false"))));
	}

	@Test
	void aiTextImprovementIsAvailableWithCompleteEnabledConfiguration() {
		assertTrue(AiAssistTextAreaPanel.isAiTextImprovementConfigured(Arrays.asList(
				parameter(Constants.OPENAI_URL, "https://ai.example.test"),
				parameter(Constants.OPENAI_API_KEY, "secret"),
				parameter(Constants.AI_TEXT_IMPROVEMENT_ENABLED, "true"))));
	}

	private Parameter parameter(String key, String value) {
		Parameter parameter = new Parameter();
		parameter.setKey(key);
		parameter.setValue(value);
		return parameter;
	}
}
