package net.lacnic.elections.domain;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

 class ElectionLightBehaviorTest extends TestCase {

	 static Test suite() {
		return new TestSuite(ElectionLightBehaviorTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testVotingWindowsAndEnabledState() {
		ElectionLight election = new ElectionLight();
		long now = System.currentTimeMillis();

		election.setVotingPeriodStartDate(new Date(now - 60_000L));
		election.setVotingPeriodEndDate(new Date(now + 60_000L));
		election.setVotingLinkAvailable(true);

		assertTrue(election.isStarted());
		assertFalse(election.isFinished());
		assertTrue(election.isVotingWindowOpen());
		assertTrue(election.isVotingLinkEnabledNow());
		assertTrue(election.isEnabledToVote());
	}

	@org.junit.jupiter.api.Test

	 void testVotingWindowNeedsStartDateAndEndsAfterFinishes() {
		ElectionLight election = new ElectionLight();
		long now = System.currentTimeMillis();

		election.setVotingPeriodStartDate(new Date(now - 120_000L));
		election.setVotingPeriodEndDate(new Date(now - 10_000L));

		assertTrue(election.isStarted());
		assertTrue(election.isFinished());
		assertFalse(election.isVotingWindowOpen());

		election.setVotingPeriodStartDate(null);
		assertFalse(election.isVotingWindowOpen());
	}

	@org.junit.jupiter.api.Test

	 void testCopyLanguageTitlesAndDescriptionsAndUrls() {
		ElectionLight election = new ElectionLight();
		election.setTitleSpanish("ESP");
		election.setTitleEnglish("ENG");
		election.setTitlePortuguese("POR");
		election.setDescriptionSpanish("desc-es");
		election.setDescriptionEnglish("desc-en");
		election.setDescriptionPortuguese("desc-pt");
		election.setLinkSpanish("link-es");
		election.setLinkEnglish("link-en");
		election.setLinkPortuguese("link-pt");

		election.copyLanguageTitles("EN");
		assertEquals("ENG", election.getTitleSpanish());
		assertEquals("ENG", election.getTitlePortuguese());
		assertEquals("ENG", election.getTitleEnglish());

		election.copyLanguageDescriptions("pt");
		assertEquals("desc-pt", election.getDescriptionSpanish());
		assertEquals("desc-pt", election.getDescriptionEnglish());
		assertEquals("desc-pt", election.getDescriptionPortuguese());

		election.copyLanguageURLs("es");
		assertEquals("link-es", election.getLinkEnglish());
		assertEquals("link-es", election.getLinkPortuguese());
		assertEquals("link-es", election.getLinkSpanish());
	}

	@org.junit.jupiter.api.Test

	 void testGetTitleAndDescriptionDefaultLocale() {
		ElectionLight election = new ElectionLight();
		election.setTitleSpanish("ES");
		election.setTitleEnglish("EN");
		election.setTitlePortuguese("PT");
		election.setDescriptionSpanish("desc-es");
		election.setDescriptionEnglish("desc-en");
		election.setDescriptionPortuguese("desc-pt");

		assertEquals("ES", election.getTitle("xx"));
		assertEquals("EN", election.getTitle("EN"));
		assertEquals("PT", election.getTitle("PT"));
		assertEquals("desc-es", election.getDescription("xx"));
		assertEquals("desc-en", election.getDescription("EN"));
		assertEquals("desc-pt", election.getDescription("PT"));
	}

	@org.junit.jupiter.api.Test

	 void testDateStringsWhenNotConfiguredReturnEmpty() {
		ElectionLight election = new ElectionLight();
		assertEquals("", election.getVotingPeriodStartDateString());
		assertEquals("", election.getVotingPeriodEndDateString());
	}
}
