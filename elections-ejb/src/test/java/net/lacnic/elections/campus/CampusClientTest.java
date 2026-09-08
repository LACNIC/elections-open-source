package net.lacnic.elections.campus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.ejb.commons.ElectionsParametersEJB;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;

class CampusClientTest {

	private EJBFactory ejbFactory;
	private ElectionsParametersEJB originalParametersEJB;
	private ElectionsParametersEJB parametersEJB;

	@BeforeEach
	void setUp() {
		ejbFactory = EJBFactory.getInstance();
		originalParametersEJB = ejbFactory.getElectionsParametersEJB();
		parametersEJB = mock(ElectionsParametersEJB.class);
		ejbFactory.setElectionsParametersEJB(parametersEJB);
	}

	@AfterEach
	void tearDown() {
		ejbFactory.setElectionsParametersEJB(originalParametersEJB);
	}

	@Test
	void appAuthenticationDisablesCampusWithoutReadingCampusConfiguration() {
		when(parametersEJB.getParameter(Constants.WS_AUTH_METHOD)).thenReturn(Constants.WS_AUTH_TYPE_APP);
		Candidate candidate = new Candidate();

		assertFalse(CampusClient.isCampusIntegrationEnabled());
		assertNull(CampusClient.getCampusUrl());
		assertTrue(CampusClient.getCourses().isEmpty());
		assertEquals(0L, CampusClient.getCampusUserIdFromEmail("candidate@example.org"));
		assertFalse(CampusClient.enrollUser(10L, "5", "20"));
		assertSame(candidate, CampusClient.enrollCandidate(candidate, "5", "20"));
		assertSame(candidate, CampusClient.updateCandidateCampusProgress(null, candidate, null));

		verify(parametersEJB, atLeastOnce()).getParameter(Constants.WS_AUTH_METHOD);
		verify(parametersEJB, never()).getParameter(Constants.CAMPUS_URL);
		verify(parametersEJB, never()).getParameter(Constants.CAMPUS_TOKEN);
	}

	@Test
	void lacnicAuthenticationWithCampusConfigurationKeepsCampusEnabled() {
		when(parametersEJB.getParameter(Constants.WS_AUTH_METHOD)).thenReturn(Constants.WS_AUTH_TYPE_LACNIC);
		when(parametersEJB.getParameter(Constants.CAMPUS_URL)).thenReturn("https://campus.example.org");
		when(parametersEJB.getParameter(Constants.CAMPUS_TOKEN)).thenReturn("token");

		assertTrue(CampusClient.isCampusIntegrationEnabled());
	}

	@Test
	void missingCampusUrlDisablesCampus() {
		when(parametersEJB.getParameter(Constants.WS_AUTH_METHOD)).thenReturn(Constants.WS_AUTH_TYPE_LACNIC);
		when(parametersEJB.getParameter(Constants.CAMPUS_TOKEN)).thenReturn("token");

		assertFalse(CampusClient.isCampusIntegrationEnabled());
	}

	@Test
	void missingCampusTokenDisablesCampus() {
		when(parametersEJB.getParameter(Constants.WS_AUTH_METHOD)).thenReturn(Constants.WS_AUTH_TYPE_LACNIC);
		when(parametersEJB.getParameter(Constants.CAMPUS_URL)).thenReturn("https://campus.example.org");

		assertFalse(CampusClient.isCampusIntegrationEnabled());
	}

	@Test
	void blankCampusConfigurationDisablesCampus() {
		when(parametersEJB.getParameter(Constants.WS_AUTH_METHOD)).thenReturn(Constants.WS_AUTH_TYPE_LACNIC);
		when(parametersEJB.getParameter(Constants.CAMPUS_URL)).thenReturn("  ");
		when(parametersEJB.getParameter(Constants.CAMPUS_TOKEN)).thenReturn("  ");

		assertFalse(CampusClient.isCampusIntegrationEnabled());
	}

	@Test
	void campusTrainingIsDisabledWhenIntegrationIsDisabled() {
		Election election = new Election();
		election.setCampusCourse(10L);

		assertFalse(CampusClient.isCampusTrainingEnabled(election));
	}

	@Test
	void campusTrainingIsDisabledWhenElectionHasNoCourses() {
		when(parametersEJB.getParameter(Constants.WS_AUTH_METHOD)).thenReturn(Constants.WS_AUTH_TYPE_LACNIC);
		when(parametersEJB.getParameter(Constants.CAMPUS_URL)).thenReturn("https://campus.example.org");
		when(parametersEJB.getParameter(Constants.CAMPUS_TOKEN)).thenReturn("token");

		assertFalse(CampusClient.isCampusTrainingEnabled(new Election()));
	}

	@Test
	void campusTrainingIsEnabledWhenIntegrationAndCourseAreConfigured() {
		when(parametersEJB.getParameter(Constants.WS_AUTH_METHOD)).thenReturn(Constants.WS_AUTH_TYPE_LACNIC);
		when(parametersEJB.getParameter(Constants.CAMPUS_URL)).thenReturn("https://campus.example.org");
		when(parametersEJB.getParameter(Constants.CAMPUS_TOKEN)).thenReturn("token");
		Election election = new Election();
		election.setCampusCourseEnglish(20L);

		assertTrue(CampusClient.isCampusTrainingEnabled(election));
	}
}
