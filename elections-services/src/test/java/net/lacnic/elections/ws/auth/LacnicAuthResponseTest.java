package net.lacnic.elections.ws.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class LacnicAuthResponseTest {

	@Test
	void defaultConstructorInitializesEmptyState() {
		LacnicAuthResponse response = new LacnicAuthResponse();

		assertNull(response.getAuthenticated());
		assertNull(response.getToken());
		assertNull(response.getRoles());
		assertNull(response.getError());
		assertNull(response.getIpAllowed());
	}

	@Test
	void gettersAndSettersPersistValues() {
		LacnicAuthResponse response = new LacnicAuthResponse();
		List<String> roles = Arrays.asList("api-Elections", "api-ElectionsPublicInformation");

		response.setAuthenticated(Boolean.TRUE);
		response.setToken("token-123");
		response.setRoles(roles);
		response.setError("none");
		response.setIpAllowed("192.0.2.0/24");

		assertTrue(response.getAuthenticated());
		assertEquals("token-123", response.getToken());
		assertEquals(roles, response.getRoles());
		assertEquals("none", response.getError());
		assertEquals("192.0.2.0/24", response.getIpAllowed());

		response.setAuthenticated(Boolean.FALSE);
		assertFalse(response.getAuthenticated());
	}
}
