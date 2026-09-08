package net.lacnic.elections.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

 class UserVoterBehaviorTest extends TestCase {

	 static Test suite() {
		return new TestSuite(UserVoterBehaviorTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testLanguageNormalizationAndFallbacks() {
		UserVoter userVoter = new UserVoter();

		userVoter.setLanguage("en-us");
		assertEquals("EN", userVoter.getLanguage());
		assertEquals(LanguageCode.EN, userVoter.getLanguageEnum());

		userVoter.setLanguage("   ");
		assertNull(userVoter.getLanguage());
		assertNull(userVoter.getLanguageEnum());

		userVoter.setLanguage("xx");
		assertEquals("SP", userVoter.getLanguage());
		assertEquals(LanguageCode.SP, userVoter.getLanguageEnum());
	}

	@org.junit.jupiter.api.Test

	 void testVoterInformationFormatting() {
		UserVoter userVoter = new UserVoter();
		userVoter.setName("Ana");
		userVoter.setOrgID("org-123");
		userVoter.setMail("ana@example.org");
		userVoter.setCountry("Uruguay");

		assertEquals("Ana - ORG-123", userVoter.getVoterInformation());
		assertEquals("Ana (ana@example.org)  - ORG-123 - Uruguay", userVoter.getCompleteVoterInformation());

		userVoter.setOrgID(null);
		assertEquals("Ana", userVoter.getVoterInformation());
		assertEquals("Ana (ana@example.org)  - Uruguay", userVoter.getCompleteVoterInformation());
	}

	@org.junit.jupiter.api.Test

	 void testCodesSummaryBuildsFromVotes() {
		UserVoter userVoter = new UserVoter();
		Candidate candidateA = new Candidate();
		candidateA.setName("CandA");
		Candidate candidateB = new Candidate();
		candidateB.setName("CandB");

		Vote voteA = new Vote();
		voteA.setCode("A01");
		voteA.setCandidate(candidateA);
		Vote voteB = new Vote();
		voteB.setCode("B02");
		voteB.setCandidate(candidateB);

		List<Vote> votes = new ArrayList<>(Arrays.asList(voteA, voteB));
		userVoter.setCodesSummary(votes);

		String expected = "A01 / CandA\nB02 / CandB\n";
		assertEquals(expected, userVoter.getCodesSummary());
	}
}
