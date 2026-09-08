package net.lacnic.elections.adminweb.wicket.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UtilsStringTest {

	@Test
	void wantHashMd5ReturnsExpectedSha256InUpperCase() {
		String hash = UtilsString.wantHashMd5("abc");
		assertEquals("BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD", hash);
	}

	@Test
	void wantHashMd5IsDeterministicAndInputSensitive() {
		String hash1 = UtilsString.wantHashMd5("same-input");
		String hash2 = UtilsString.wantHashMd5("same-input");
		String hash3 = UtilsString.wantHashMd5("other-input");

		assertEquals(hash1, hash2);
		assertNotEquals(hash1, hash3);
	}

	@Test
	void wantHashMd5ThrowsWhenInputIsNull() {
		assertThrows(NullPointerException.class, () -> UtilsString.wantHashMd5(null));
	}
}
