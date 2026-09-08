package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LinksUtilsTest {

	@Test
	void buildPublicCandidatePhotoLinkShouldReturnEmptyForInvalidIds() {
		assertEquals("", LinksUtils.buildPublicCandidatePhotoLink(null, 10L));
		assertEquals("", LinksUtils.buildPublicCandidatePhotoLink(10L, null));
		assertEquals("", LinksUtils.buildPublicCandidatePhotoLink(0L, 10L));
		assertEquals("", LinksUtils.buildPublicCandidatePhotoLink(10L, 0L));
		assertEquals("", LinksUtils.buildPublicCandidatePhotoLink(-1L, 10L));
		assertEquals("", LinksUtils.buildPublicCandidatePhotoLink(10L, -1L));
		assertEquals("", LinksUtils.buildPublicCandidatePhotoLink(null));
		assertEquals("", LinksUtils.buildPublicCandidatePhotoLink(0L));
		assertEquals("", LinksUtils.buildPublicCandidatePhotoLink(-1L));
	}

	@Test
	void buildPublicCandidateProfileLinkShouldReturnEmptyForInvalidValues() {
		assertEquals("", LinksUtils.buildPublicCandidateProfileLink(null, 10L));
		assertEquals("", LinksUtils.buildPublicCandidateProfileLink("", 10L));
		assertEquals("", LinksUtils.buildPublicCandidateProfileLink("   ", 10L));
		assertEquals("", LinksUtils.buildPublicCandidateProfileLink("token", null));
		assertEquals("", LinksUtils.buildPublicCandidateProfileLink("token", 0L));
		assertEquals("", LinksUtils.buildPublicCandidateProfileLink("token", -1L));
	}

	@Test
	void buildCandidatePhotoLinkRequiresPositiveCandidateId() {
		String url = LinksUtils.buildPublicCandidatePhotoLink(20L, 10L);
		assertEquals("", url);
	}

	@Test
	void buildOfficialResultLetterLinkRequiresPositiveElectionId() {
		assertEquals("", LinksUtils.buildPublicElectionOfficialResultLetterLink(null, null));
		assertEquals("", LinksUtils.buildPublicElectionOfficialResultLetterLink(0L, null));
		assertEquals("", LinksUtils.buildPublicElectionOfficialResultLetterLink(-1L, null));
		assertEquals("", LinksUtils.buildPublicElectionOfficialResultLetterLink(20L, null));
	}

	@Test
	void buildVoteAndResultLinksShouldReturnEmptyWhenParametersAreNotConfigured() {
		assertEquals("", LinksUtils.buildVoteLink("token"));
		assertEquals("", LinksUtils.buildResultsLink("token"));
		assertEquals("", LinksUtils.buildAuditorResultsLink("token"));
	}

	@Test
	void buildTokenAndNominationLinksShouldReturnEmptyWhenParametersAreNotConfigured() {
		assertEquals("", LinksUtils.buildTokenVoteLink("token"));
		assertEquals("", LinksUtils.buildTokenResultLink("token"));
		assertEquals("", LinksUtils.buildTokenAuditLink("token"));
		assertEquals("", LinksUtils.buildTokenQuestionLink("token"));
		assertEquals("", LinksUtils.buildPublicCandidateProfileLink("token", 10L));
		assertEquals("", LinksUtils.buildAcceptNominationLink("token"));
		assertEquals("", LinksUtils.buildDoNominationLink("token"));
		assertEquals("", LinksUtils.buildSupportNominationLink("token"));
	}
}
