package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import org.junit.jupiter.api.Test;

class CandidateElectionTaskProgressDaoTest {

	@Test
	void shouldGetByNominationTokenAndTaskKey() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateElectionTaskProgress> query = mock(TypedQuery.class);
		CandidateElectionTaskProgress progress = mock(CandidateElectionTaskProgress.class);
		ElectionTaskKey taskKey = ElectionTaskKey.PROFILE;
		when(em.createQuery(anyString(), eq(CandidateElectionTaskProgress.class))).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(progress));

		CandidateElectionTaskProgressDao dao = new CandidateElectionTaskProgressDao(em);
		CandidateElectionTaskProgress result = dao.getByNominationTokenAndTaskKey("tok", taskKey);

		assertSame(progress, result);
		verify(query).setMaxResults(1);
	}

	@Test
	void shouldGetByNominationTokenAndTaskKeyReturnsNullWhenNoResult() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateElectionTaskProgress> query = mock(TypedQuery.class);
		ElectionTaskKey taskKey = ElectionTaskKey.PROFILE;
		when(em.createQuery(anyString(), eq(CandidateElectionTaskProgress.class))).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of());

		CandidateElectionTaskProgressDao dao = new CandidateElectionTaskProgressDao(em);
		CandidateElectionTaskProgress result = dao.getByNominationTokenAndTaskKey("tok", taskKey);

		assertNull(result);
	}

	@Test
	void shouldGetByCandidateIdAndTaskKey() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateElectionTaskProgress> query = mock(TypedQuery.class);
		CandidateElectionTaskProgress progress = mock(CandidateElectionTaskProgress.class);
		ElectionTaskKey taskKey = ElectionTaskKey.PROFILE;
		when(em.createQuery(anyString(), eq(CandidateElectionTaskProgress.class))).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(progress));

		CandidateElectionTaskProgressDao dao = new CandidateElectionTaskProgressDao(em);
		CandidateElectionTaskProgress result = dao.getByCandidateIdAndTaskKey(55L, taskKey);

		assertSame(progress, result);
		verify(query).setMaxResults(1);
	}

	@Test
	void shouldLockTaskProgressForCandidateUpdate() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateElectionTaskProgress> query = mock(TypedQuery.class);
		CandidateElectionTaskProgress progress = mock(CandidateElectionTaskProgress.class);
		when(em.createQuery(anyString(), eq(CandidateElectionTaskProgress.class))).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		when(query.setLockMode(LockModeType.PESSIMISTIC_WRITE)).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(progress));

		CandidateElectionTaskProgress result = new CandidateElectionTaskProgressDao(em)
				.getByCandidateIdAndTaskKeyForUpdate(55L, ElectionTaskKey.USER_SUPPORTS_5);

		assertSame(progress, result);
		verify(query).setLockMode(LockModeType.PESSIMISTIC_WRITE);
	}

	@Test
	void shouldGetByElectionIdAndTaskKeysReturnsEmptyForEmptyTaskKeys() {
		EntityManager em = mock(EntityManager.class);

		CandidateElectionTaskProgressDao dao = new CandidateElectionTaskProgressDao(em);
		List<CandidateElectionTaskProgress> result = dao.getByElectionIdAndTaskKeys(9L, List.of());

		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	void shouldGetByElectionIdAndTaskKeys() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateElectionTaskProgress> query = mock(TypedQuery.class);
		List<CandidateElectionTaskProgress> expected = List.of(mock(CandidateElectionTaskProgress.class));
		when(em.createQuery(anyString(), eq(CandidateElectionTaskProgress.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		when(query.getResultList()).thenReturn(expected);

		CandidateElectionTaskProgressDao dao = new CandidateElectionTaskProgressDao(em);
		List<CandidateElectionTaskProgress> result = dao.getByElectionIdAndTaskKeys(9L,
				List.of(ElectionTaskKey.PROFILE, ElectionTaskKey.COUNTRIES));

		assertEquals(expected, result);
	}

	@Test
	void shouldGetByCandidateId() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateElectionTaskProgress> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(CandidateElectionTaskProgress.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(CandidateElectionTaskProgress.class)));

		CandidateElectionTaskProgressDao dao = new CandidateElectionTaskProgressDao(em);
		List<CandidateElectionTaskProgress> result = dao.getByCandidateId(77L);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void shouldGetCandidateIdsByElectionTask() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(101L, 102L));

		CandidateElectionTaskProgressDao dao = new CandidateElectionTaskProgressDao(em);
		List<Long> result = dao.getCandidateIdsByElectionTask(33L);

		assertNotNull(result);
		assertEquals(2, result.size());
	}

	@Test
	void shouldCountCompletedOrOmittedTasksByCandidate() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(6L);

		CandidateElectionTaskProgressDao dao = new CandidateElectionTaskProgressDao(em);
		long result = dao.countCompletedOrOmittedTasksByCandidate(9L, 7L);

		assertEquals(6L, result);
	}
}
