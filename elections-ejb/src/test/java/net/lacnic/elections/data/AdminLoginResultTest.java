package net.lacnic.elections.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.UserAdmin;

class AdminLoginResultTest {

	@Test
	void shouldStoreUserAndNormalizeRolesCollection() {
		UserAdmin admin = new UserAdmin("ADMIN", "secret", "ADMIN@MAIL.TEST");
		AdminLoginResult result = new AdminLoginResult(admin, Arrays.asList("ROLE_B", "ROLE_A", "ROLE_B"));

		assertNotNull(result.getUserAdmin());
		assertEquals("admin", result.getUserAdmin().getUserAdminId());
		assertEquals("SECRET", result.getUserAdmin().getPassword());
		assertEquals("admin@mail.test", result.getUserAdmin().getEmail());
		assertIterableEquals(Arrays.asList("ROLE_B", "ROLE_A"), result.getRoles());

		result.setRoles(null);
		assertTrue(result.getRoles().isEmpty());
	}

	@Test
	void shouldStoreAuthenticationErrorMessage() {
		AdminLoginResult result = new AdminLoginResult("Ingrese código 2FA");

		assertEquals("Ingrese código 2FA", result.getError());
		assertTrue(result.getRoles().isEmpty());
	}
}
