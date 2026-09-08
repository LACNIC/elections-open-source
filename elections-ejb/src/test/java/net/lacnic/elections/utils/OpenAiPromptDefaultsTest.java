package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.pre.CandidateTextImprovementInstruction;

class OpenAiPromptDefaultsTest {

	@Test
	void getDefaultTextImprovementPromptShouldReturnNullWhenInstructionIsNull() {
		assertNull(OpenAiPromptDefaults.getDefaultTextImprovementPrompt(null));
	}

	@Test
	void getDefaultTextImprovementPromptShouldReturnSpellingPrompt() {
		String prompt = OpenAiPromptDefaults.getDefaultTextImprovementPrompt(
				CandidateTextImprovementInstruction.SPELLING_REVIEW);

		assertNotNull(prompt);
		assertTrue(prompt.contains("Eres un asistente especializado en corrección ortográfica y formal del texto."));
	}

	@Test
	void getDefaultTextImprovementPromptShouldReturnStylePrompt() {
		String prompt = OpenAiPromptDefaults.getDefaultTextImprovementPrompt(CandidateTextImprovementInstruction.WRITING_STYLE_REVIEW);
		assertNotNull(prompt);
		assertTrue(prompt.startsWith("ROL DEL MODELO:"));
		assertTrue(prompt.contains("mejora de redacción"));
	}

	@Test
	void getDefaultTextImprovementPromptShouldReturnTranslationPrompt() {
		String prompt = OpenAiPromptDefaults.getDefaultTextImprovementPrompt(
				CandidateTextImprovementInstruction.TRANSLATE_TO_ENGLISH);

		assertNotNull(prompt);
		assertTrue(prompt.startsWith("ROL DEL MODELO:"));
		assertTrue(prompt.contains("traduce"));
	}
}
