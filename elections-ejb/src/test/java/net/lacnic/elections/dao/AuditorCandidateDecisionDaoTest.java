package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import org.junit.jupiter.api.Test;

class AuditorCandidateDecisionDaoTest {

	@Test
	void shouldGetElectionAuditorCandidateDecisions() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<AuditorCandidateDecision> query = mock(TypedQuery.class);
		AuditorCandidateDecision decision = mock(AuditorCandidateDecision.class);
		when(em.createQuery(anyString(), eq(AuditorCandidateDecision.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(decision));

		AuditorCandidateDecisionDao dao = new AuditorCandidateDecisionDao(em);
		List<AuditorCandidateDecision> result = dao.getElectionAuditorCandidateDecisions(77L);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertSame(decision, result.get(0));
	}

	@Test
	void shouldGetByAuditorAndCandidateReturnsFirstResult() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<AuditorCandidateDecision> query = mock(TypedQuery.class);
		AuditorCandidateDecision decision = mock(AuditorCandidateDecision.class);
		when(em.createQuery(anyString(), eq(AuditorCandidateDecision.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultStream()).thenReturn(Stream.of(decision, mock(AuditorCandidateDecision.class)));

		AuditorCandidateDecisionDao dao = new AuditorCandidateDecisionDao(em);
		AuditorCandidateDecision result = dao.getByAuditorAndCandidate(5L, 8L);

		assertSame(decision, result);
		verify(query).setMaxResults(1);
	}

	@Test
	void shouldCountDistinctAuditorsByCandidateAndStatus() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setParameter(anyString(), eq(AuditorCandidateDecisionStatus.APPROVED))).thenReturn(query);
		when(query.getSingleResult()).thenReturn(11L);

		AuditorCandidateDecisionDao dao = new AuditorCandidateDecisionDao(em);
		long result = dao.countDistinctAuditorsByCandidateAndStatus(9L, AuditorCandidateDecisionStatus.APPROVED);

		assertEquals(11L, result);
	}

	@Test
	void shouldGetCandidateDecisionStatusByAuditorId() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Object[]> query = mock(TypedQuery.class);
		Object[] row = new Object[] { 12L, AuditorCandidateDecisionStatus.APPROVED };
		when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.<Object[]>of(row));

		AuditorCandidateDecisionDao dao = new AuditorCandidateDecisionDao(em);
		List<Object[]> result = dao.getCandidateDecisionStatusByAuditorId(12L);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(12L, result.get(0)[0]);
	}

	@Test
	void shouldCountDistinctResolvedCandidatesByAuditor() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setParameter(anyString(), eq(AuditorCandidateDecisionStatus.REJECTED))).thenReturn(query);
		when(query.getSingleResult()).thenReturn(3L);

		AuditorCandidateDecisionDao dao = new AuditorCandidateDecisionDao(em);
		long result = dao.countDistinctResolvedCandidatesByAuditor(5L, 7L, AuditorCandidateDecisionStatus.REJECTED);

		assertEquals(3L, result);
	}

	@Test
	void shouldReturnEmptyWhenNoCandidateDecisionByAuditorAndCandidate() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<AuditorCandidateDecision> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(AuditorCandidateDecision.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setMaxResults(1)).thenReturn(query);
		when(query.getResultStream()).thenReturn(Stream.empty());

		AuditorCandidateDecisionDao dao = new AuditorCandidateDecisionDao(em);
		AuditorCandidateDecision result = dao.getByAuditorAndCandidate(9L, 10L);

		assertNull(result);
	}
}
