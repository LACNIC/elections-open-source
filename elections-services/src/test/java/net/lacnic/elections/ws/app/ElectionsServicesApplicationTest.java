package net.lacnic.elections.ws.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.ws.json.JacksonConfigurationProvider;
import net.lacnic.elections.ws.services.ElectionsService;
import net.lacnic.elections.ws.services.ElectionsTablesServices;

class ElectionsServicesApplicationTest {

	@Test
	void getSingletonsRegistersExpectedResources() {
		ElectionsServicesApplication application = new ElectionsServicesApplication();

		Set<Object> singletons = application.getSingletons();

		assertEquals(3, singletons.size());
		assertTrue(singletons.stream().anyMatch(ElectionsService.class::isInstance));
		assertTrue(singletons.stream().anyMatch(ElectionsTablesServices.class::isInstance));
		assertTrue(singletons.stream().anyMatch(JacksonConfigurationProvider.class::isInstance));
	}
}
