package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CountryTest {

	@Test
	void shouldExposeCountryDataAndToString() {
		Country country = new Country("UY", "Uruguay");

		assertEquals("UY", country.getId());
		assertEquals("Uruguay", country.getName());
		assertEquals("Uruguay", country.toString());

		country.setId("AR");
		country.setName("Argentina");
		assertEquals("AR", country.getId());
		assertEquals("Argentina", country.getName());
		assertEquals("Argentina", country.toString());
	}
}
