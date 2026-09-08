package net.lacnic.elections.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class HealthCheckTest {

	@Test
	void shouldCreateSuccessfulHealthCheckWithStatusOneAndMetrics() {
		List<ElectionReport> elections = Collections.emptyList();

		HealthCheck healthCheck = new HealthCheck(1, 2L, 3L, 4L, 5L, 6L, elections);

		assertEquals(1, healthCheck.getStatus());
		assertTrue(healthCheck.isBaseOk());
		assertNull(healthCheck.getErrorMessage());
		assertEquals(1, healthCheck.getSendAttempts());
		assertEquals(2L, healthCheck.getFailedAccessIps());
		assertEquals(3L, healthCheck.getFailedAccessSum());
		assertEquals(4L, healthCheck.getMailsTotal());
		assertEquals(5L, healthCheck.getMailsPending());
		assertEquals(6L, healthCheck.getMailsSent());
		assertSame(elections, healthCheck.getElections());
	}

	@Test
	void shouldCreateErrorHealthCheckWithStatusZeroAndMessage() {
		HealthCheck healthCheck = new HealthCheck("db unavailable");

		assertEquals(0, healthCheck.getStatus());
		assertFalse(healthCheck.isBaseOk());
		assertEquals("db unavailable", healthCheck.getErrorMessage());
	}

	@Test
	void shouldUpdateStatusAndErrorMessage() {
		HealthCheck healthCheck = new HealthCheck("initial error");

		healthCheck.setStatus(1);
		healthCheck.setErrorMessage(null);

		assertEquals(1, healthCheck.getStatus());
		assertNull(healthCheck.getErrorMessage());
	}
}
