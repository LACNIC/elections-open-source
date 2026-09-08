package net.lacnic.elections.adminweb.wicket.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.Test;

class UtilsParametersTest {

	@Test
	void getFilterAndIsAllUseExpectedDefaults() {
		PageParameters empty = new PageParameters();
		assertEquals("", UtilsParameters.getFilter(empty));
		assertFalse(UtilsParameters.isAll(empty));
		assertTrue(UtilsParameters.isAll(UtilsParameters.getFilter("ALL")));
	}

	@Test
	void idBuildersAndReadersWorkForStringAndLongOverloads() {
		PageParameters idAsString = UtilsParameters.getId("123");
		PageParameters idAsLong = UtilsParameters.getId(123L);

		assertEquals("123", UtilsParameters.getId(idAsString));
		assertEquals("123", UtilsParameters.getId(idAsLong));
		assertEquals(123L, UtilsParameters.getIdAsLong(idAsString));
		assertTrue(UtilsParameters.isId(idAsString));
	}

	@Test
	void buildersAndReadersWorkForUserCandidateQuestionAuditAndCommissioner() {
		PageParameters user = UtilsParameters.getUser(7L);
		PageParameters candidate = UtilsParameters.getCandidate(8L);
		PageParameters question = UtilsParameters.getQuestion(9L);
		PageParameters audit = UtilsParameters.getAudit(10L);
		PageParameters commissioner = UtilsParameters.getCommissioner(11L);

		assertEquals(7L, UtilsParameters.getUserAsLong(user));
		assertEquals(8L, UtilsParameters.getCandidateAsLong(candidate));
		assertEquals(9L, UtilsParameters.getQuestionAsLong(question));
		assertEquals(10L, UtilsParameters.getAuditAsLong(audit));
		assertEquals(11L, UtilsParameters.getCommissionerAsLong(commissioner));
	}

	@Test
	void buildersAndReadersWorkForAdminClaveOrgIdAndToken() {
		PageParameters admin = UtilsParameters.getAdminId("44");
		PageParameters clave = UtilsParameters.getClaveId("abc");
		PageParameters org = UtilsParameters.getOrgId("ORG-1");
		PageParameters token = UtilsParameters.getToken("token-value");

		assertEquals("44", UtilsParameters.getAdminId(admin));
		assertEquals("abc", UtilsParameters.getClaveId(clave));
		assertEquals("ORG-1", UtilsParameters.getOrgId(org));
		assertEquals("token-value", UtilsParameters.getToken(token));
	}

	@Test
	void getParamsAndGenericReaderSupportObjectValues() {
		PageParameters params = UtilsParameters.getParams(UtilsParameters.getQuestionText(), 42L);
		assertEquals("42", UtilsParameters.getParameters(UtilsParameters.getQuestionText(), params));
		assertEquals(42L, UtilsParameters.getQuestionAsLong(params));
	}

	@Test
	void newQuestionBuilderKeepsElectionIdAndMarksNewQuestionMode() {
		PageParameters params = UtilsParameters.getNewQuestion(93L);

		assertEquals(93L, UtilsParameters.getIdAsLong(params));
		assertTrue(UtilsParameters.isNewQuestion(params));
		assertFalse(UtilsParameters.isNewQuestion(new PageParameters()));
	}

	@Test
	void numericReadersReturnZeroWhenParameterIsMissingOrInvalid() {
		PageParameters missing = new PageParameters();
		PageParameters invalid = UtilsParameters.getParams(UtilsParameters.getIdText(), "invalid");

		assertEquals(0L, UtilsParameters.getIdAsLong(missing));
		assertEquals(0L, UtilsParameters.getUserAsLong(missing));
		assertEquals(0L, UtilsParameters.getAuditAsLong(missing));
		assertEquals(0L, UtilsParameters.getCandidateAsLong(missing));
		assertEquals(0L, UtilsParameters.getQuestionAsLong(missing));
		assertEquals(0L, UtilsParameters.getCommissionerAsLong(missing));
		assertEquals(0L, UtilsParameters.getIdAsLong(invalid));
	}
}
