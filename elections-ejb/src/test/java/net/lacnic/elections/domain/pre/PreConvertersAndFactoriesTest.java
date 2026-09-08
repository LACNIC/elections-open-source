package net.lacnic.elections.domain.pre;

import jakarta.persistence.AttributeConverter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.evra.registro.CategoriasEnum;

 class PreConvertersAndFactoriesTest extends TestCase {

	 static Test suite() {
		return new TestSuite(PreConvertersAndFactoriesTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testOrganizationCategoryConverter() {
		AttributeConverter<CategoriasEnum, String> converter = new OrganizationCategoryConverter();

		assertNull(converter.convertToDatabaseColumn(null));
		assertEquals("NONE", converter.convertToDatabaseColumn(CategoriasEnum.NONE));
		assertEquals(CategoriasEnum.NONE, converter.convertToEntityAttribute("none"));
		assertEquals(CategoriasEnum.NONE, converter.convertToEntityAttribute("NONE"));
		assertEquals(CategoriasEnum.ASN_ONLY, converter.convertToEntityAttribute("asn-only"));
		assertNull(converter.convertToEntityAttribute(" "));
	}

	@org.junit.jupiter.api.Test

	 void testLanguageCodeConverter() {
		AttributeConverter<LanguageCode, String> converter = new LanguageCodeConverter();

		assertNull(converter.convertToDatabaseColumn(null));
		assertEquals("EN", converter.convertToDatabaseColumn(LanguageCode.EN));
		assertEquals(LanguageCode.EN, converter.convertToEntityAttribute("en"));
		assertEquals(LanguageCode.PT, converter.convertToEntityAttribute("pt-br"));
		assertNull(converter.convertToEntityAttribute("zz"));
	}

	@org.junit.jupiter.api.Test

	 void testCandidateTextImprovementResponseFactories() {
		CandidateTextImprovementResponse success = CandidateTextImprovementResponse.success("texto");
		assertTrue(success.isProcessed());
		assertFalse(success.isDailyLimitReached());
		assertEquals("texto", success.getImprovedText());

		CandidateTextImprovementResponse limited = CandidateTextImprovementResponse.dailyLimitReached();
		assertFalse(limited.isProcessed());
		assertTrue(limited.isDailyLimitReached());
		assertNull(limited.getImprovedText());

		CandidateTextImprovementResponse failed = CandidateTextImprovementResponse.processingError();
		assertFalse(failed.isProcessed());
		assertFalse(failed.isDailyLimitReached());
		assertNull(failed.getImprovedText());
	}

	@org.junit.jupiter.api.Test

	 void testCandidateTextImprovementInstructionMetadata() {
		assertFalse(CandidateTextImprovementInstruction.SPELLING_REVIEW.isTranslation());
		assertEquals(null, CandidateTextImprovementInstruction.SPELLING_REVIEW.getTargetLanguage());
		assertEquals("AI_TEXT_PROMPT_SPELLING_REVIEW", CandidateTextImprovementInstruction.SPELLING_REVIEW.getParameterKeyPrefix());

		assertTrue(CandidateTextImprovementInstruction.TRANSLATE_TO_ENGLISH.isTranslation());
		assertEquals("en", CandidateTextImprovementInstruction.TRANSLATE_TO_ENGLISH.getTargetLanguage());
		assertEquals("AI_TEXT_PROMPT_TRANSLATION", CandidateTextImprovementInstruction.TRANSLATE_TO_ENGLISH.getParameterKeyPrefix());
	}
}
