package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

	@Test
	void createSecureTokenShouldContainTwoUuidValues() {
		String token = StringUtils.createSecureToken();
		assertNotNull(token);
		assertEquals(72, token.length());

		String anotherToken = StringUtils.createSecureToken();
		assertNotEquals(token, anotherToken);
	}

	@Test
	void createSmallTokenShouldReturnUuidFormat() {
		assertEquals(36, StringUtils.createSmallToken().length());
	}

	@Test
	void arrayToCommaSeparatedShouldHandleNull() {
		assertEquals("", StringUtils.arrayToCommaSeparated(null));
		assertEquals("one,two,three", StringUtils.arrayToCommaSeparated(new String[] { "one", "two", "three" }));
	}

	@Test
	void commaSeparatedToArrayShouldSplitInput() {
		assertArrayEquals(new String[] { "one", "two", "three" }, StringUtils.commaSeparatedToArray("one,two,three"));
	}

	@Test
	void stringsToArrayShouldReturnEquivalentArray() {
		assertArrayEquals(new String[] { "a", "b" }, StringUtils.stringsToArray("a", "b"));
	}

	@Test
	void sha256ShouldGenerateStableDigest() {
		assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
				StringUtils.sha256("hello"));
	}
}
