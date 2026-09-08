package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.LanguageCode;

class PublicLinkRecoveryTypeTest {

	@Test
	void fromKeyShouldNormalizeAndResolveKnownValues() {
		assertEquals(PublicLinkRecoveryType.VOTER_VOTE_LINK, PublicLinkRecoveryType.fromKey("voter_vote_link"));
		assertEquals(PublicLinkRecoveryType.NOMINATION_LINK, PublicLinkRecoveryType.fromKey("nomination_link"));
		assertEquals(PublicLinkRecoveryType.SUPPORT_LINK, PublicLinkRecoveryType.fromKey("  support_link  "));
	}

	@Test
	void fromKeyShouldReturnNullWhenInvalid() {
		assertNull(PublicLinkRecoveryType.fromKey(null));
		assertNull(PublicLinkRecoveryType.fromKey("   "));
		assertNull(PublicLinkRecoveryType.fromKey("INVALID"));
	}

	@Test
	void getPublicValuesShouldExposeAllDefinedValues() {
		assertEquals(3, PublicLinkRecoveryType.getPublicValues().size());
		assertNotNull(PublicLinkRecoveryType.getPublicValues().get(0));
	}

	@Test
	void resolveLabelShouldReturnSpanishByDefaultWhenLanguageIsNull() {
		assertEquals("Enlace de votacion", PublicLinkRecoveryType.VOTER_VOTE_LINK.resolveLabel(null));
	}

	@Test
	void resolveLabelShouldReturnByLanguage() {
		assertEquals("Support confirmation link", PublicLinkRecoveryType.SUPPORT_LINK.resolveLabel(LanguageCode.EN));
		assertEquals("Link para realizar nomeacao", PublicLinkRecoveryType.NOMINATION_LINK.resolveLabel(LanguageCode.PT));
		assertEquals("Enlace de votacion", PublicLinkRecoveryType.VOTER_VOTE_LINK.resolveLabel(LanguageCode.SP));
	}

	@Test
	void getDisplayResourceKeysShouldMatchType() {
		assertEquals("recoverLinkType.VOTER_VOTE_LINK", PublicLinkRecoveryType.VOTER_VOTE_LINK.getDisplayResourceKey());
		assertEquals("recoverLinkTypeHelp.VOTER_VOTE_LINK", PublicLinkRecoveryType.VOTER_VOTE_LINK.getHelpResourceKey());
	}

}
