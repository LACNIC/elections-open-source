package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ElectionsRolesTest {

	@Test
	void supportedPortalRolesReturnsExpectedRolesInStableOrder() {
		Set<String> roles = ElectionsRoles.supportedPortalRoles();

		assertEquals(
				List.of(
						ElectionsRoles.ELECTIONS_MANAGER,
						ElectionsRoles.ELECTIONS_DELETER,
						ElectionsRoles.ELECTIONS_STATUTARY_ONLY,
						ElectionsRoles.ELECTIONS_NON_STATUTARY_ONLY),
				new ArrayList<>(roles));
	}

	@Test
	void supportedPortalRolesReturnsFreshSetOnEachCall() {
		Set<String> firstCall = ElectionsRoles.supportedPortalRoles();
		firstCall.clear();

		Set<String> secondCall = ElectionsRoles.supportedPortalRoles();
		assertEquals(4, secondCall.size());
		assertTrue(secondCall.contains(ElectionsRoles.ELECTIONS_MANAGER));
	}
}
