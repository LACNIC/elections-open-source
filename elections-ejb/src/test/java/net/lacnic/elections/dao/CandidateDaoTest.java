package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.CandidateType;
import net.lacnic.elections.domain.pre.CandidateStatus;
import org.junit.jupiter.api.Test;

class CandidateDaoTest {

	@Test
	void shouldGetCandidateById() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Candidate> query = mock(TypedQuery.class);
		Candidate candidate = mock(Candidate.class);
		when(em.createQuery(anyString(), eq(Candidate.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(candidate);

		CandidateDao dao = new CandidateDao(em);
		Candidate result = dao.getCandidate(55L);

		assertSame(candidate, result);
		verify(query).setParameter("candidateId", 55L);
	}

	@Test
	void shouldGetCandidatesByEmailWithPaging() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Candidate> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Candidate.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.setFirstResult(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of());

		CandidateDao dao = new CandidateDao(em);
		List<Candidate> result = dao.getCandidatesByEmail("user@x.com", 25, 3);

		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(query).setParameter("email", "user@x.com");
		verify(query).setMaxResults(25);
		verify(query).setFirstResult(75);
	}

	@Test
	void shouldGetElectionFirstCandidate() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Candidate> query = mock(TypedQuery.class);
		Candidate candidate = mock(Candidate.class);
		when(em.createQuery(anyString(), eq(Candidate.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(candidate);

		CandidateDao dao = new CandidateDao(em);
		Candidate result = dao.getElectionFirstCandidate(11L);

		assertSame(candidate, result);
	}

	@Test
	void shouldGetElectionFirstCandidateReturnsNullWhenNotFound() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Candidate> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Candidate.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getSingleResult()).thenThrow(new NoResultException());

		CandidateDao dao = new CandidateDao(em);
		Candidate result = dao.getElectionFirstCandidate(11L);

		assertNull(result);
	}

	@Test
	void shouldGetElectionLastCandidateReturnsNullWhenNotFound() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Candidate> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Candidate.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getSingleResult()).thenThrow(new NoResultException());

		CandidateDao dao = new CandidateDao(em);
		Candidate result = dao.getElectionLastCandidate(11L);

		assertNull(result);
	}

	@Test
	void shouldGetElectionCandidates() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Candidate> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Candidate.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(Candidate.class), mock(Candidate.class)));

		CandidateDao dao = new CandidateDao(em);
		List<Candidate> result = dao.getElectionCandidates(33L);

		assertNotNull(result);
		assertEquals(2, result.size());
	}

	@Test
	void shouldGetElectionPublishedCandidates() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Candidate> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Candidate.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setParameter(anyString(), eq(CandidateStatus.CONFIRMED_AND_PUBLISHED))).thenReturn(query);
		when(query.setParameter(anyString(), eq(CandidateType.ABSTENTION))).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(Candidate.class)));

		CandidateDao dao = new CandidateDao(em);
		List<Candidate> result = dao.getElectionPublishedCandidates(33L);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(query).setParameter("publishedStatus", CandidateStatus.CONFIRMED_AND_PUBLISHED);
		verify(query).setParameter("abstentionType", CandidateType.ABSTENTION);
	}

	@Test
	void shouldGetElectionAbstentionCandidateReturnsNullWhenNotFound() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Candidate> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Candidate.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setParameter(anyString(), any(CandidateType.class))).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getSingleResult()).thenThrow(new NoResultException());

		CandidateDao dao = new CandidateDao(em);
		Candidate result = dao.getElectionAbstentionCandidate(33L);

		assertNull(result);
	}

	@Test
	void shouldCountElectionCandidatesExcludingAbstention() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setParameter(anyString(), any(CandidateType.class))).thenReturn(query);
		when(query.getSingleResult()).thenReturn(12L);

		CandidateDao dao = new CandidateDao(em);
		long result = dao.countElectionCandidatesExcludingAbstention(33L);

		assertEquals(12L, result);
	}

	@Test
	void shouldCountElectionCandidates() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(8L);

		CandidateDao dao = new CandidateDao(em);
		long result = dao.countElectionCandidates(33L);

		assertEquals(8L, result);
	}

	@Test
	void shouldCountAllCandidatesReturnsZeroWhenQueryReturnsNull() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.getSingleResult()).thenReturn(null);

		CandidateDao dao = new CandidateDao(em);
		long result = dao.countAllCandidates();

		assertEquals(0L, result);
	}

	@Test
	void shouldGetCandidateIdsWithPaging() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.setFirstResult(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(1L, 2L, 3L));

		CandidateDao dao = new CandidateDao(em);
		List<Long> result = dao.getCandidateIds(10, 4);

		assertNotNull(result);
		assertEquals(3, result.size());
		verify(query).setMaxResults(10);
		verify(query).setFirstResult(4);
	}

	@Test
	void shouldGetCandidateVotesAmount() {
		EntityManager em = mock(EntityManager.class);
		Query query = mock(Query.class);
		when(em.createQuery(anyString())).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(14L);

		CandidateDao dao = new CandidateDao(em);
		long result = dao.getCandidateVotesAmount(55L);

		assertEquals(14L, result);
	}

	@Test
	void shouldGetLastNonFixedCandidateOrder() {
		EntityManager em = mock(EntityManager.class);
		Query query = mock(Query.class);
		when(em.createQuery(anyString())).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setParameter(anyString(), anyInt())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(44);

		CandidateDao dao = new CandidateDao(em);
		int result = dao.getLastNonFixedCandidateOrder(11L);

		assertEquals(44, result);
	}

	@Test
	void shouldReturnDefaultLastNonFixedCandidateOrderWhenQueryFails() {
		EntityManager em = mock(EntityManager.class);
		Query query = mock(Query.class);
		when(em.createQuery(anyString())).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setParameter(anyString(), anyInt())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getSingleResult()).thenThrow(new RuntimeException("boom"));

		CandidateDao dao = new CandidateDao(em);
		int result = dao.getLastNonFixedCandidateOrder(11L);

		assertEquals(1, result);
	}

	@Test
	void shouldGetNextAboveCandidate() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Candidate> query = mock(TypedQuery.class);
		Candidate candidate = mock(Candidate.class);
		when(em.createQuery(anyString(), eq(Candidate.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setParameter(anyString(), anyInt())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(candidate);

		CandidateDao dao = new CandidateDao(em);
		Candidate result = dao.getNextAboveCandidate(7L, 2);

		assertSame(candidate, result);
	}

	@Test
	void shouldGetNextBelowCandidateReturnsNullWhenNoResult() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Candidate> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Candidate.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setParameter(anyString(), anyInt())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getSingleResult()).thenThrow(new NoResultException());

		CandidateDao dao = new CandidateDao(em);
		Candidate result = dao.getNextBelowCandidate(7L, 2);

		assertNull(result);
	}

	@Test
	void shouldGetCandidatesAllIdAndDescription() {
		EntityManager em = mock(EntityManager.class);
		Query query = mock(Query.class);
		when(em.createQuery(anyString())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.setFirstResult(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.<Object[]>of(new Object[] { 9L, "Candidate A" }));

		CandidateDao dao = new CandidateDao(em);
		List<Object[]> result = dao.getCandidatesAllIdAndDescription(25, 1);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(9L, result.get(0)[0]);
		assertEquals("Candidate A", result.get(0)[1]);
	}
}
