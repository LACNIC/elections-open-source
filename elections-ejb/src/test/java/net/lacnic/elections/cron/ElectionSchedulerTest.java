package net.lacnic.elections.cron;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.lacnic.elections.ejb.ElectionsManagerEJB;
import net.lacnic.elections.ejb.commons.ElectionsParametersEJB;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;

class ElectionSchedulerTest {

	private EJBFactory ejbFactory;
	private ElectionsManagerEJB originalManagerEJB;
	private ElectionsParametersEJB originalParametersEJB;
	private ElectionsManagerEJB managerEJB;
	private ElectionsParametersEJB parametersEJB;

	@BeforeEach
	void setUp() {
		ejbFactory = EJBFactory.getInstance();
		originalManagerEJB = ejbFactory.getElectionsManagerEJB();
		originalParametersEJB = ejbFactory.getElectionsParametersEJB();
		managerEJB = mock(ElectionsManagerEJB.class);
		parametersEJB = mock(ElectionsParametersEJB.class);
		ejbFactory.setElectionsManagerEJB(managerEJB);
		ejbFactory.setElectionsParametersEJB(parametersEJB);
		when(parametersEJB.getParameter(Constants.WS_AUTH_METHOD)).thenReturn(Constants.WS_AUTH_TYPE_LACNIC);
	}

	@AfterEach
	void tearDown() {
		ejbFactory.setElectionsManagerEJB(originalManagerEJB);
		ejbFactory.setElectionsParametersEJB(originalParametersEJB);
	}

	@Test
	void missingCampusUrlSkipsCandidateProgressScheduler() {
		when(parametersEJB.getParameter(Constants.CAMPUS_TOKEN)).thenReturn("token");

		new ElectionScheduler().updateCampusCandidateProgress();

		verify(managerEJB, never()).verifyCampusCourseAccessForCandidates();
		verify(managerEJB, never()).verifyCampusEvaluationGradesForCandidates();
	}

	@Test
	void missingCampusTokenSkipsCandidateProgressScheduler() {
		when(parametersEJB.getParameter(Constants.CAMPUS_URL)).thenReturn("https://campus.example.org");

		new ElectionScheduler().updateCampusCandidateProgress();

		verify(managerEJB, never()).verifyCampusCourseAccessForCandidates();
		verify(managerEJB, never()).verifyCampusEvaluationGradesForCandidates();
	}

	@Test
	void missingCampusConfigurationSkipsRateLimitCacheScheduler() {
		new ElectionScheduler().clearCampusProgressCheckRateLimitCache();

		verify(parametersEJB, never()).getParameter(Constants.CAMPUS_PROGRESS_CHECK_COURSE_RATE_LIMIT_CACHE_RESET_HOURS);
		verify(parametersEJB, never()).getParameter(Constants.CAMPUS_PROGRESS_CHECK_EVALUATION_RATE_LIMIT_CACHE_RESET_HOURS);
	}
}
