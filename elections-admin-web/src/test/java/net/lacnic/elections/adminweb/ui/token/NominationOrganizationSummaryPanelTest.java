package net.lacnic.elections.adminweb.ui.token;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NominationOrganizationSummaryPanelTest {

	@Test
	void shouldAutoShowRestrictedCountriesModalReturnsTrueOnlyOnFirstEligibleVisit() {
		assertTrue(NominationOrganizationSummaryPanel.shouldAutoShowRestrictedCountriesModal(true, false));
	}

	@Test
	void shouldAutoShowRestrictedCountriesModalReturnsFalseWhenAlreadyShownInSession() {
		assertFalse(NominationOrganizationSummaryPanel.shouldAutoShowRestrictedCountriesModal(true, true));
	}

	@Test
	void shouldAutoShowRestrictedCountriesModalReturnsFalseWhenElectionHasNoRestrictedCountries() {
		assertFalse(NominationOrganizationSummaryPanel.shouldAutoShowRestrictedCountriesModal(false, false));
	}
}
