package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CriticalOperationsLoggerUtilsTest {

	@Test
	void buildCriticalOperationLogShouldTrimActorsIpOperationAndFallbackForMissingInputs() {
		String log = CriticalOperationsLoggerUtils.buildCriticalOperationLog(null, "  alice  ", "  SEND  ", 10L, "  detail  ");

		String[] fields = splitLogFields(log);
		assertEquals("", fields[0]);
		assertEquals("alice", fields[1]);
		assertEquals("-", fields[3]);
		assertEquals("SEND", fields[4]);
		assertEquals("10", fields[5]);
		assertEquals("detail", fields[6]);
		assertFalse(fields[2].trim().isEmpty());
		assertEquals(8, fields.length);
	}

	@Test
	void buildCriticalOperationLogShouldMarkMissingActorIpOperationAndDetailAsDash() {
		String log = CriticalOperationsLoggerUtils.buildCriticalOperationLog(null, null, null, null, null);
		String[] fields = splitLogFields(log);

		assertEquals("-", fields[1]);
		assertEquals("-", fields[3]);
		assertEquals("-", fields[4]);
		assertEquals("-", fields[5]);
		assertEquals("-", fields[6]);
	}

	@Test
	void buildCriticalOperationLogShouldSanitizeSensitiveTokensAndBearerValues() {
		String detail = "/api?token=abc123&acceptNominationToken=zzz&x=1 token=manualValue Bearer secret_123";
		String log = CriticalOperationsLoggerUtils.buildCriticalOperationLog("op", "127.0.0.1", "action", 99L, detail);
		String sanitized = splitLogFields(log)[6];

		assertFalse(sanitized.contains("abc123"));
		assertFalse(sanitized.contains("zzz"));
		assertFalse(sanitized.contains("manualValue"));
		assertFalse(sanitized.contains("secret_123"));
		assertTrue(sanitized.contains("token=[REDACTED]"));
		assertTrue(sanitized.contains("acceptNominationToken=[REDACTED]"));
		assertTrue(sanitized.contains("Bearer [REDACTED]"));
	}

	@Test
	void buildCriticalOperationLogShouldCapVeryLongDetailsToConfiguredLimit() {
		String longDetail = "x".repeat(600);
		String log = CriticalOperationsLoggerUtils.buildCriticalOperationLog("op", "127.0.0.1", "action", 99L, longDetail);
		String sanitized = splitLogFields(log)[6];

		assertEquals(503, sanitized.length());
		assertEquals("x".repeat(500) + "...", sanitized);
	}

	@Test
	void logCriticalOperationShouldNotThrow() {
		assertDoesNotThrow(() -> CriticalOperationsLoggerUtils.logCriticalOperation("10.0.0.1", "alice", "ACTION", 33L));
	}

	private String[] splitLogFields(String logLine) {
		return logLine.split("\\|", -1);
	}
}
