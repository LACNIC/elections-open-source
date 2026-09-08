package net.lacnic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionRestrictedCountry;

 class ElectionRestrictedCountriesSyncTest {

	@Test
	 void testApplyRestrictedCountryCodesKeepsExistingEntitiesWhenUnchanged() {
		Election election = new Election();
		ElectionRestrictedCountry existingCountry = new ElectionRestrictedCountry(election, "AR");
		existingCountry.setId(100L);

		ArrayList<ElectionRestrictedCountry> restrictedCountries = new ArrayList<>();
		restrictedCountries.add(existingCountry);
		election.setRestrictedCountries(restrictedCountries);

		election.setRestrictedCountryCodes(Arrays.asList("AR"));
		election.applyRestrictedCountryCodes();

		assertEquals(1, election.getRestrictedCountries().size());
		assertSame(existingCountry, election.getRestrictedCountries().get(0));
		assertEquals(100L, election.getRestrictedCountries().get(0).getId());
		assertEquals("AR", election.getRestrictedCountries().get(0).getCountryCode());
	}

	@Test
	 void testApplyRestrictedCountryCodesAddsNewCountries() {
		Election election = new Election();
		election.setRestrictedCountries(new ArrayList<ElectionRestrictedCountry>());

		election.setRestrictedCountryCodes(Arrays.asList("AR", "BR"));
		election.applyRestrictedCountryCodes();

		assertEquals(2, election.getRestrictedCountries().size());
		assertEquals("AR", election.getRestrictedCountries().get(0).getCountryCode());
		assertEquals("BR", election.getRestrictedCountries().get(1).getCountryCode());
		assertSame(election, election.getRestrictedCountries().get(0).getElection());
		assertSame(election, election.getRestrictedCountries().get(1).getElection());
	}

	@Test
	 void testApplyRestrictedCountryCodesRemovesMissingCountries() {
		Election election = new Election();
		ElectionRestrictedCountry existingAr = new ElectionRestrictedCountry(election, "AR");
		ElectionRestrictedCountry existingBr = new ElectionRestrictedCountry(election, "BR");
		ArrayList<ElectionRestrictedCountry> restrictedCountries = new ArrayList<>();
		restrictedCountries.add(existingAr);
		restrictedCountries.add(existingBr);
		election.setRestrictedCountries(restrictedCountries);

		election.setRestrictedCountryCodes(Arrays.asList("AR"));
		election.applyRestrictedCountryCodes();

		assertEquals(1, election.getRestrictedCountries().size());
		assertSame(existingAr, election.getRestrictedCountries().get(0));
		assertEquals("AR", election.getRestrictedCountries().get(0).getCountryCode());
	}

	@Test
	 void testApplyRestrictedCountryCodesIgnoresNullEmptyAndDuplicates() {
		Election election = new Election();
		election.setRestrictedCountries(new ArrayList<ElectionRestrictedCountry>());
		election.setRestrictedCountryCodes(Arrays.asList(null, "", "   ", "AR", " AR ", "BR", "BR", "  BR  "));

		election.applyRestrictedCountryCodes();
		List<ElectionRestrictedCountry> restrictedCountries = election.getRestrictedCountries();

		assertEquals(2, restrictedCountries.size());
		assertEquals("AR", restrictedCountries.get(0).getCountryCode());
		assertEquals("BR", restrictedCountries.get(1).getCountryCode());
	}

	@Test
	 void testApplyRestrictedCountryCodesRemovesInvalidExistingEntries() {
		Election election = new Election();
		ElectionRestrictedCountry valid = new ElectionRestrictedCountry(election, " AR ");
		ElectionRestrictedCountry nullCode = new ElectionRestrictedCountry(election, null);
		ElectionRestrictedCountry blankCode = new ElectionRestrictedCountry(election, "   ");
		ArrayList<ElectionRestrictedCountry> restrictedCountries = new ArrayList<>();
		restrictedCountries.add(valid);
		restrictedCountries.add(null);
		restrictedCountries.add(nullCode);
		restrictedCountries.add(blankCode);
		election.setRestrictedCountries(restrictedCountries);

		election.setRestrictedCountryCodes(Arrays.asList("AR"));
		election.applyRestrictedCountryCodes();

		assertEquals(1, election.getRestrictedCountries().size());
		assertSame(valid, election.getRestrictedCountries().get(0));
		assertEquals("AR", election.getRestrictedCountries().get(0).getCountryCode());
	}

	@Test
	 void testApplyRestrictedCountryCodesInitializesListWhenNull() {
		Election election = new Election();
		election.setRestrictedCountries(null);

		election.setRestrictedCountryCodes(Arrays.asList("UY"));
		election.applyRestrictedCountryCodes();

		assertNotNull(election.getRestrictedCountries());
		assertEquals(1, election.getRestrictedCountries().size());
		assertEquals("UY", election.getRestrictedCountries().get(0).getCountryCode());
	}

	@Test
	 void testSetRestrictedCountriesKeepsManagedCollectionReference() {
		Election election = new Election();
		ArrayList<ElectionRestrictedCountry> managedRestrictedCountries = new ArrayList<>();
		ElectionRestrictedCountry existingCountry = new ElectionRestrictedCountry(election, "AR");
		managedRestrictedCountries.add(existingCountry);
		election.setRestrictedCountries(managedRestrictedCountries);

		ArrayList<ElectionRestrictedCountry> incomingRestrictedCountries = new ArrayList<>();
		ElectionRestrictedCountry incomingCountry = new ElectionRestrictedCountry(null, "BR");
		incomingRestrictedCountries.add(incomingCountry);

		election.setRestrictedCountries(incomingRestrictedCountries);

		assertSame(managedRestrictedCountries, election.getRestrictedCountries());
		assertEquals(1, election.getRestrictedCountries().size());
		assertSame(incomingCountry, election.getRestrictedCountries().get(0));
		assertSame(election, election.getRestrictedCountries().get(0).getElection());
	}

	@Test
	 void testSetRestrictedCountriesClearsManagedCollectionWhenIncomingIsNull() {
		Election election = new Election();
		ArrayList<ElectionRestrictedCountry> managedRestrictedCountries = new ArrayList<>();
		managedRestrictedCountries.add(new ElectionRestrictedCountry(election, "AR"));
		election.setRestrictedCountries(managedRestrictedCountries);

		election.setRestrictedCountries(null);

		assertSame(managedRestrictedCountries, election.getRestrictedCountries());
		assertTrue(election.getRestrictedCountries().isEmpty());
	}
}
