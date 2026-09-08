package net.lacnic.elections.domain;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

 class ElectionBehaviorTest extends TestCase {

	 static Test suite() {
		return new TestSuite(ElectionBehaviorTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testDefaultConstructorAndConfiguredDefaults() {
		Election election = new Election();

		assertNotNull(election.getCreationDate());
		assertEquals(ElectionType.BOARD, election.getElectionType());
		assertEquals(ElectionLinkRecoveryMode.ONLY_BR, election.getPublicLinkRecoveryMode());
		assertEquals(3, election.getDiffUTC());
		assertEquals(false, election.isMigrated());
		assertEquals(true, election.isVotingLinkAvailable());
		assertEquals(true, election.isResultLinkAvailable());
		assertEquals(true, election.isAuditorLinkAvailable());
		assertEquals(true, election.isDoNominationLinkAvailable());
		assertEquals(true, election.isNominationTasksLinkAvailable());
		assertEquals(true, election.isNominationSupportLinkAvailable());
		assertEquals(true, election.isPublicElectionLinkAvailable());
		assertNotNull(election.getResultToken());
		assertNotNull(election.getPublicElectionToken());
	}

	@org.junit.jupiter.api.Test

	 void testIdConstructorAssignsBaseDefaults() {
		Election election = new Election(99L);

		assertEquals(99L, election.getElectionId());
		assertEquals("TODOS", election.getTitleSpanish());
		assertEquals("TODOS", election.getTitleEnglish());
		assertEquals("TODOS", election.getTitlePortuguese());
		assertEquals(ElectionLinkRecoveryMode.ONLY_BR, election.getPublicLinkRecoveryMode());
	}

	@org.junit.jupiter.api.Test

	 void testVotingWindowDerivedDatesAndEnabledToVote() {
		Election election = new Election();
		long now = System.currentTimeMillis();
		election.setVotingPeriodStartDate(new Date(now - 60_000L));
		election.setVotingPeriodEndDate(new Date(now + 60_000L));
		election.setVotingLinkAvailable(true);

		assertEquals(new Date(now - 60_000L), election.getVotingPeriodStartDate());
		assertEquals(new Date(now + 60_000L), election.getVotingPeriodEndDate());
		assertTrue(election.isStarted());
		assertFalse(election.isFinished());
		assertTrue(election.isEnabledToVote());
	}

	@org.junit.jupiter.api.Test

	 void testVotingWindowEndsAndFallbackWhenMissingEndDate() {
		Election election = new Election();
		Date start = new Date(System.currentTimeMillis() - 120_000L);
		election.setVotingPeriodStartDate(start);
		election.setVotingPeriodEndDate(null);
		election.setVotingLinkAvailable(true);

		assertEquals(start, election.getVotingPeriodEndDate());
		assertEquals(start, election.getVotingPeriodEndDate());
		election.setVotingPeriodEndDate(new Date(System.currentTimeMillis() - 10_000L));
		assertTrue(election.isFinished());
		assertFalse(election.isEnabledToVote());
	}

	@org.junit.jupiter.api.Test

	 void testGetTitleDescriptionByLanguageAndCopyLanguageURLs() {
		Election election = new Election();
		election.setTitleSpanish("ES");
		election.setTitleEnglish("EN");
		election.setTitlePortuguese("PT");
		election.setDescriptionSpanish("Desc ES");
		election.setDescriptionEnglish("Desc EN");
		election.setDescriptionPortuguese("Desc PT");
		election.setLinkSpanish("link-es");
		election.setLinkEnglish("link-en");
		election.setLinkPortuguese("link-pt");

		assertEquals("EN", election.getTitle("en"));
		assertEquals("PT", election.getTitle("PT"));
		assertEquals("ES", election.getTitle("xx"));
		assertEquals("Desc PT", election.getDescription("pt"));

		election.copyLanguageURLs("pt");
		assertEquals("link-pt", election.getLinkEnglish());
		assertEquals("link-pt", election.getLinkSpanish());
		assertEquals("link-pt", election.getLinkPortuguese());
	}

	@org.junit.jupiter.api.Test

	 void testPublicLinkRecoveryModeNullSafe() {
		Election election = new Election();
		election.setPublicLinkRecoveryMode(null);

		assertEquals(ElectionLinkRecoveryMode.NONE, election.getPublicLinkRecoveryMode());
	}

	@org.junit.jupiter.api.Test

	 void testSetRestrictedCountriesLinksAndCodes() {
		Election election = new Election();
		ElectionRestrictedCountry ar = new ElectionRestrictedCountry(election, "ar");
		ElectionRestrictedCountry br = new ElectionRestrictedCountry(election, "br");
		election.setRestrictedCountries(Arrays.asList(ar, br));

		List<String> restrictedCodes = election.getRestrictedCountryCodes();
		assertEquals(2, restrictedCodes.size());
		assertEquals("ar", restrictedCodes.get(0));
		assertEquals("br", restrictedCodes.get(1));
	}

	@org.junit.jupiter.api.Test

	 void testCampusCoursesByLanguageAreOptionalAndAssignable() {
		Election election = new Election();

		assertNull(election.getCampusCourse());
		assertNull(election.getCampusCourseEnglish());
		assertNull(election.getCampusCoursePortuguese());

		election.setCampusCourse(10L);
		election.setCampusCourseEnglish(20L);
		election.setCampusCoursePortuguese(30L);

		assertEquals(Long.valueOf(10L), election.getCampusCourse());
		assertEquals(Long.valueOf(20L), election.getCampusCourseEnglish());
		assertEquals(Long.valueOf(30L), election.getCampusCoursePortuguese());
	}
}
