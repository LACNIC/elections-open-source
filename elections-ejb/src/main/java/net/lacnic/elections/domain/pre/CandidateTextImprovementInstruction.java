package net.lacnic.elections.domain.pre;

import net.lacnic.elections.utils.Constants;

public enum CandidateTextImprovementInstruction {
	SPELLING_REVIEW("AI_TEXT_PROMPT_SPELLING_REVIEW"),
	WRITING_STYLE_REVIEW("AI_TEXT_PROMPT_WRITING_STYLE_REVIEW"),
	TRANSLATE_TO_SPANISH(Constants.AI_TEXT_PROMPT_TRANSLATION, "es", "español"),
	TRANSLATE_TO_PORTUGUESE(Constants.AI_TEXT_PROMPT_TRANSLATION, "pt", "portugués"),
	TRANSLATE_TO_ENGLISH(Constants.AI_TEXT_PROMPT_TRANSLATION, "en", "inglés");

	private final String parameterKeyPrefix;
	private final String targetLanguage;
	private final String targetLanguageLabel;

	CandidateTextImprovementInstruction(String parameterKeyPrefix) {
		this.parameterKeyPrefix = parameterKeyPrefix;
		this.targetLanguage = null;
		this.targetLanguageLabel = null;
	}

	CandidateTextImprovementInstruction(String parameterKeyPrefix, String targetLanguage, String targetLanguageLabel) {
		this.parameterKeyPrefix = parameterKeyPrefix;
		this.targetLanguage = targetLanguage;
		this.targetLanguageLabel = targetLanguageLabel;
	}

	public String getParameterKeyPrefix() {
		return parameterKeyPrefix;
	}

	public String getTargetLanguage() {
		return targetLanguage;
	}

	public String getTargetLanguageLabel() {
		return targetLanguageLabel;
	}

	public boolean isTranslation() {
		return this.targetLanguage != null;
	}
}
