package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.Nomination;
import org.junit.jupiter.api.Test;

class DaoPackageLogicTest {

	@Test
	void shouldCreateDaoFromFactory() {
		EntityManager entityManager = mock(EntityManager.class);

		assertInstanceOf(CandidateDao.class, ElectionsDaoFactory.createCandidateDao(entityManager));
		assertInstanceOf(CandidateQuestionDao.class, ElectionsDaoFactory.createCandidateQuestionDao(entityManager));
		assertInstanceOf(CandidateElectionTaskProgressDao.class,
				ElectionsDaoFactory.createCandidateElectionTaskProgressDao(entityManager));
		assertInstanceOf(ActivityDao.class, ElectionsDaoFactory.createActivityDao(entityManager));
		assertInstanceOf(VoteDao.class, ElectionsDaoFactory.createVoteDao(entityManager));
		assertInstanceOf(ElectionDao.class, ElectionsDaoFactory.createElectionDao(entityManager));
		assertInstanceOf(IpAccessDao.class, ElectionsDaoFactory.createIpAccessDao(entityManager));
		assertInstanceOf(ElectionEmailTemplateDao.class, ElectionsDaoFactory.createElectionEmailTemplateDao(entityManager));
		assertInstanceOf(ElectionTaskDao.class, ElectionsDaoFactory.createElectionTaskDao(entityManager));
		assertInstanceOf(ElectionCalendarDao.class, ElectionsDaoFactory.createElectionCalendarDao(entityManager));
		assertInstanceOf(EmailDao.class, ElectionsDaoFactory.createEmailDao(entityManager));
		assertInstanceOf(CommissionerDao.class, ElectionsDaoFactory.createCommissionerDao(entityManager));
		assertInstanceOf(AuditorDao.class, ElectionsDaoFactory.createAuditorDao(entityManager));
		assertInstanceOf(AuditorCandidateDecisionDao.class,
				ElectionsDaoFactory.createAuditorCandidateDecisionDao(entityManager));
		assertInstanceOf(ReportDao.class, ElectionsDaoFactory.createReportDao(entityManager));
		assertInstanceOf(UserAdminDao.class, ElectionsDaoFactory.createUserAdminDao(entityManager));
		assertInstanceOf(ParameterDao.class, ElectionsDaoFactory.createParameterDao(entityManager));
		assertInstanceOf(CustomizationDao.class, ElectionsDaoFactory.createCustomizationDao(entityManager));
		assertInstanceOf(JointElectionDao.class, ElectionsDaoFactory.createJointElectionDao(entityManager));
		assertInstanceOf(NominationDao.class, ElectionsDaoFactory.createNominationDao(entityManager));
		assertInstanceOf(OrganizationDao.class, ElectionsDaoFactory.createOrganizationDao(entityManager));
		assertInstanceOf(SyncRunDao.class, ElectionsDaoFactory.createSyncRunDao(entityManager));
		assertInstanceOf(SupportNominationDao.class, ElectionsDaoFactory.createSupportNominationDao(entityManager));
		assertInstanceOf(PublicElectionPageDao.class, ElectionsDaoFactory.createPublicElectionPageDao(entityManager));
		assertInstanceOf(UserVoterDao.class, ElectionsDaoFactory.createUserVoterDao(entityManager));
	}

	@Test
	void shouldReturnNullWhenElectionCandidateIsNotFound() {
		EntityManager entityManager = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Candidate> query = mock(TypedQuery.class);
		when(entityManager.createQuery(anyString(), any(Class.class))).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		when(query.setMaxResults(1)).thenReturn(query);
		when(query.getSingleResult()).thenThrow(new NoResultException());
		CandidateDao dao = new CandidateDao(entityManager);

		assertNull(dao.getElectionFirstCandidate(123L));
		assertNull(dao.getElectionLastCandidate(123L));
		assertNull(dao.getElectionAbstentionCandidate(123L));
	}

	@Test
	void shouldReturnNullWhenAcceptNominationTokenIsNotFound() {
		EntityManager entityManager = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Nomination> query = mock(TypedQuery.class);
		when(entityManager.createQuery(anyString(), any(Class.class))).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		when(query.getResultStream()).thenReturn(Stream.empty());
		NominationDao dao = new NominationDao(entityManager);

		assertNull(dao.getNominationByAcceptNominationToken("missing-token"));
	}

	@Test
	void shouldNotQueryCalendarWindowWhenNoIdsAreProvided() {
		EntityManager entityManager = mock(EntityManager.class);
		ElectionCalendarDao dao = new ElectionCalendarDao(entityManager);
		assertTrue(dao.getCalendarWindowByElectionIds(new HashSet<>(), ElectionCalendarKey.N_16_PERIODO_VOTING).isEmpty());
		verifyNoInteractions(entityManager);

		assertTrue(dao.getCalendarWindowByElectionIds(null, ElectionCalendarKey.N_16_PERIODO_VOTING).isEmpty());
	}

	@Test
	void shouldUseStartDateWhenEndDateIsNullInCalendarWindow() {
		EntityManager entityManager = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> query = mock(TypedQuery.class);
		Date start = new Date(100L);
		when(entityManager.createQuery(anyString(), any(Class.class))).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		List<Object[]> calendarWindow = List.of(new Object[][] { new Object[] { 1L, start, null } });
		when(query.getResultList()).thenReturn(calendarWindow);

		ElectionCalendarDao dao = new ElectionCalendarDao(entityManager);
		Map<Long, Date[]> windows = dao.getCalendarWindowByElectionIds(Set.of(1L), ElectionCalendarKey.N_16_PERIODO_VOTING);

		assertNotNull(windows.get(1L));
		assertEquals(2, windows.get(1L).length);
		assertEquals(start, windows.get(1L)[0]);
		assertEquals(start, windows.get(1L)[1]);
	}

	@Test
	void shouldReturnEmptyWhenTemplateTypeIsBlank() {
		ElectionEmailTemplateDao dao = new ElectionEmailTemplateDao(mock(EntityManager.class));
		assertNull(dao.getBaseTemplate("   "));
	}

	@Test
	void shouldNotTouchPersistenceWhenEmailFiltersAreInvalid() {
		EntityManager entityManager = mock(EntityManager.class);
		SupportNominationDao supportDao = new SupportNominationDao(entityManager);
		NominationDao nominationDao = new NominationDao(entityManager);
		UserVoterDao userDao = new UserVoterDao(entityManager);
		SyncRunDao syncRunDao = new SyncRunDao(entityManager);

		assertTrue(supportDao.getElectionSupportNominationsByNormalizedContactEmail(1L, " ", 10, "US").isEmpty());
		assertTrue(nominationDao.getElectionNominationsByNormalizedNominationEmail(1L, " ", 10, "US").isEmpty());
		assertTrue(userDao.getElectionUserVotersByNormalizedEmail(1L, " ", 10, "US").isEmpty());
		assertTrue(syncRunDao.getLatestSyncRunsByElectionIds(null).isEmpty());
		verifyNoInteractions(entityManager);
	}
}
