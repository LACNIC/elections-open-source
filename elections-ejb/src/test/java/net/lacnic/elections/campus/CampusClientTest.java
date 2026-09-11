package net.lacnic.elections.campus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.ejb.commons.ElectionsParametersEJB;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.ElectionsProperties;

class CampusClientTest {

	@TempDir
	Path configurationDirectory;

	private String originalConfigurationDirectory;
	private Properties originalProperties;
	private Field propertiesField;
	private EJBFactory ejbFactory;
	private ElectionsParametersEJB originalParametersEJB;
	private ElectionsParametersEJB parametersEJB;

	@BeforeEach
	void setUp() throws Exception {
		originalConfigurationDirectory = System.getProperty("jboss.server.config.dir");
		propertiesField = ElectionsProperties.class.getDeclaredField("properties");
		propertiesField.setAccessible(true);
		originalProperties = (Properties) propertiesField.get(null);
		System.setProperty("jboss.server.config.dir", configurationDirectory.toString());
		configureAuthentication(Constants.WS_AUTH_TYPE_LACNIC);
		ejbFactory = EJBFactory.getInstance();
		originalParametersEJB = ejbFactory.getElectionsParametersEJB();
		parametersEJB = mock(ElectionsParametersEJB.class);
		ejbFactory.setElectionsParametersEJB(parametersEJB);
	}

	@AfterEach
	void tearDown() throws Exception {
		ejbFactory.setElectionsParametersEJB(originalParametersEJB);
		propertiesField.set(null, originalProperties);
		if (originalConfigurationDirectory == null) {
			System.clearProperty("jboss.server.config.dir");
		} else {
			System.setProperty("jboss.server.config.dir", originalConfigurationDirectory);
		}
	}

	@Test
	void appAuthenticationDisablesCampusWithoutReadingCampusConfiguration() throws Exception {
		configureAuthentication(Constants.WS_AUTH_TYPE_APP);
		Candidate candidate = new Candidate();

		assertFalse(CampusClient.isCampusIntegrationEnabled());
		assertNull(CampusClient.getCampusUrl());
		assertTrue(CampusClient.getCourses().isEmpty());
		assertEquals(0L, CampusClient.getCampusUserIdFromEmail("candidate@example.org"));
		assertFalse(CampusClient.enrollUser(10L, "5", "20"));
		assertSame(candidate, CampusClient.enrollCandidate(candidate, "5", "20"));
		assertSame(candidate, CampusClient.updateCandidateCampusProgress(null, candidate, null));

		verify(parametersEJB, never()).getParameter(Constants.WS_AUTH_METHOD);
		verify(parametersEJB, never()).getParameter(Constants.CAMPUS_URL);
		verify(parametersEJB, never()).getParameter(Constants.CAMPUS_TOKEN);
	}

	@Test
	void lacnicAuthenticationWithCampusConfigurationKeepsCampusEnabled() {
		when(parametersEJB.getParameter(Constants.CAMPUS_URL)).thenReturn("https://campus.example.org");
		when(parametersEJB.getParameter(Constants.CAMPUS_TOKEN)).thenReturn("token");

		assertTrue(CampusClient.isCampusIntegrationEnabled());
	}

	@Test
	void missingCampusUrlDisablesCampus() {
		when(parametersEJB.getParameter(Constants.CAMPUS_TOKEN)).thenReturn("token");

		assertFalse(CampusClient.isCampusIntegrationEnabled());
	}

	@Test
	void missingCampusTokenDisablesCampus() {
		when(parametersEJB.getParameter(Constants.CAMPUS_URL)).thenReturn("https://campus.example.org");

		assertFalse(CampusClient.isCampusIntegrationEnabled());
	}

	@Test
	void blankCampusConfigurationDisablesCampus() {
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
		when(parametersEJB.getParameter(Constants.CAMPUS_URL)).thenReturn("https://campus.example.org");
		when(parametersEJB.getParameter(Constants.CAMPUS_TOKEN)).thenReturn("token");

		assertFalse(CampusClient.isCampusTrainingEnabled(new Election()));
	}

	@Test
	void campusTrainingIsEnabledWhenIntegrationAndCourseAreConfigured() {
		when(parametersEJB.getParameter(Constants.CAMPUS_URL)).thenReturn("https://campus.example.org");
		when(parametersEJB.getParameter(Constants.CAMPUS_TOKEN)).thenReturn("token");
		Election election = new Election();
		election.setCampusCourseEnglish(20L);

		assertTrue(CampusClient.isCampusTrainingEnabled(election));
	}
	private void configureAuthentication(String authMethod) throws Exception {
		Files.writeString(configurationDirectory.resolve("elections.properties"),
				Constants.WS_AUTH_METHOD + "=" + authMethod + "\n");
		propertiesField.set(null, null);
	}

}
