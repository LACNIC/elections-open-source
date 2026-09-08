package net.lacnic.elections.adminweb.ui.admin.emailtemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.Test;

import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;

class EmailTemplateFilterStateTest {

	@Test
	void fromPageParametersReadsKnownFiltersAndNormalizesText() {
		PageParameters params = UtilsParameters.getId(107L);
		params.add(EmailTemplateFilterState.PARAM_RECIPIENT, "voters");
		params.add(EmailTemplateFilterState.PARAM_ELECTION_SCOPE, "statutory");
		params.add(EmailTemplateFilterState.PARAM_LANGUAGE_MODE, "trilingual");
		params.add(EmailTemplateFilterState.PARAM_SEND_MODE, "automatic");
		params.add(EmailTemplateFilterState.PARAM_TEMPLATE_TYPE, "  Candidate  ");

		EmailTemplateFilterState state = EmailTemplateFilterState.from(params);

		assertEquals(EmailTemplateFilterCatalog.RecipientFilter.VOTERS, state.getRecipientFilter());
		assertEquals(EmailTemplateFilterCatalog.ElectionScopeFilter.STATUTORY, state.getElectionScopeFilter());
		assertEquals(EmailTemplateFilterCatalog.LanguageModeFilter.TRILINGUAL, state.getLanguageModeFilter());
		assertEquals(EmailTemplateFilterCatalog.SendModeFilter.AUTOMATIC, state.getSendModeFilter());
		assertEquals("candidate", state.getTemplateType());
	}

	@Test
	void toPageParametersOmitsDefaultFiltersAndKeepsElectionId() {
		EmailTemplateFilterState state = new EmailTemplateFilterState();
		state.setSendModeFilter(EmailTemplateFilterCatalog.SendModeFilter.MANUAL);
		state.setTemplateType(" reminder ");

		PageParameters params = state.toPageParameters(88L);

		assertEquals(88L, UtilsParameters.getIdAsLong(params));
		assertEquals("MANUAL", params.get(EmailTemplateFilterState.PARAM_SEND_MODE).toString());
		assertEquals("reminder", params.get(EmailTemplateFilterState.PARAM_TEMPLATE_TYPE).toString());
		assertTrue(params.get(EmailTemplateFilterState.PARAM_RECIPIENT).isEmpty());
		assertTrue(params.get(EmailTemplateFilterState.PARAM_ELECTION_SCOPE).isEmpty());
		assertTrue(params.get(EmailTemplateFilterState.PARAM_LANGUAGE_MODE).isEmpty());
	}
}
