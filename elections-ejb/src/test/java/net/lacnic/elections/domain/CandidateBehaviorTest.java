package net.lacnic.elections.domain;

import net.lacnic.elections.utils.Constants;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

 class CandidateBehaviorTest extends TestCase {

	 static Test suite() {
		return new TestSuite(CandidateBehaviorTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testDefaultValuesForReminderFrequencyAndType() {
		Candidate candidate = new Candidate();

		assertEquals(ReminderFrequency.MONDAY_TO_FRIDAY, candidate.getReminderFrequency());
		assertEquals(CandidateType.NORMAL, candidate.getCandidateType());
	}

	@org.junit.jupiter.api.Test

	 void testSettersDefaultReminderAndTypeWhenNull() {
		Candidate candidate = new Candidate();

		candidate.setReminderFrequency(null);
		candidate.setCandidateType(null);

		assertEquals(ReminderFrequency.MONDAY_TO_FRIDAY, candidate.getReminderFrequency());
		assertEquals(CandidateType.NORMAL, candidate.getCandidateType());
	}

	@org.junit.jupiter.api.Test

	 void testFixedOrderBoundaries() {
		Candidate candidate = new Candidate();
		candidate.setCandidateOrder(Constants.MIN_ORDER);
		assertTrue(candidate.isFixed());

		candidate.setCandidateOrder(Constants.MAX_ORDER);
	assertTrue(candidate.isFixed());

		candidate.setCandidateOrder(Constants.MIN_ORDER + 1);
		assertFalse(candidate.isFixed());
	}

	@org.junit.jupiter.api.Test

	 void testBioAndLinkByLanguage() {
		Candidate candidate = new Candidate();
		candidate.setBioSpanish("bio-es");
		candidate.setBioEnglish("bio-en");
		candidate.setBioPortuguese("bio-pt");
		candidate.setLinkSpanish("link-es");
		candidate.setLinkEnglish("link-en");
		candidate.setLinkPortuguese("link-pt");

		assertEquals("bio-en", candidate.getBio("EN"));
		assertEquals("bio-pt", candidate.getBio("PT"));
		assertEquals("bio-es", candidate.getBio("IT"));
		assertEquals("link-en", candidate.getLink("EN"));
		assertEquals("link-pt", candidate.getLink("pt"));
		assertEquals("link-es", candidate.getLink("es_MX"));
	}

	@org.junit.jupiter.api.Test

	 void testCopyBioToOtherLanguagesCopiesFromSpanish() {
		Candidate candidate = new Candidate();
		candidate.setBioSpanish("bio-es");
		candidate.setLinkSpanish("link-es");
		candidate.setQOtherStatutoryAnswer1Spanish("q1-es");
		candidate.setQOtherStatutoryAnswer2Spanish("q2-es");
		candidate.setQOtherStatutoryAnswer3Spanish("q3-es");
		candidate.setQOtherStatutoryAnswer4Spanish("q4-es");
		candidate.setQOtherNonStatutoryAnswer1Spanish("q5-es");

		candidate.copyBioToOtherLanguages();

		assertEquals("bio-es", candidate.getBioEnglish());
		assertEquals("bio-es", candidate.getBioPortuguese());
		assertEquals("link-es", candidate.getLinkEnglish());
		assertEquals("link-es", candidate.getLinkPortuguese());
		assertEquals("q1-es", candidate.getQOtherStatutoryAnswer1English());
		assertEquals("q1-es", candidate.getQOtherStatutoryAnswer1Portuguese());
		assertEquals("q5-es", candidate.getQOtherNonStatutoryAnswer1English());
		assertEquals("q5-es", candidate.getQOtherNonStatutoryAnswer1Portuguese());
	}

	@org.junit.jupiter.api.Test

	 void testCleanNullsSensitiveFields() {
		Candidate candidate = new Candidate();
		candidate.setName("A Name");
		candidate.setBioSpanish("bio");
		candidate.setBioEnglish("bio");
		candidate.setPictureName("photo");
		candidate.setPictureExtension("png");
		candidate.setPictureInfo(new byte[] { 1, 2 });
		candidate.setMail("mail@example.org");

		candidate.clean();

		assertNull(candidate.getName());
		assertNull(candidate.getBioSpanish());
		assertNull(candidate.getBioEnglish());
		assertNull(candidate.getBioPortuguese());
		assertNull(candidate.getPictureName());
		assertNull(candidate.getPictureExtension());
		assertNull(candidate.getPictureInfo());
		assertNull(candidate.getMail());
	}

	@org.junit.jupiter.api.Test

	 void testCampusCourseSelectedIsOptionalAndAssignable() {
		Candidate candidate = new Candidate();

		assertNull(candidate.getCampusCourseSelected());

		candidate.setCampusCourseSelected(20L);

		assertEquals(Long.valueOf(20L), candidate.getCampusCourseSelected());
	}
}
