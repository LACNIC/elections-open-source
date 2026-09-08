package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Auditor;
import org.junit.jupiter.api.Test;

class AuditorDaoTest {

	@Test
	void shouldGetAuditorById() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Auditor> query = mock(TypedQuery.class);
		Auditor auditor = mock(Auditor.class);
		when(em.createQuery(anyString(), eq(Auditor.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(auditor);

		AuditorDao dao = new AuditorDao(em);
		Auditor result = dao.getAuditor(20L);

		assertSame(auditor, result);
		verify(query).setParameter("auditorId", 20L);
	}

	@Test
	void shouldGetAuditorsAll() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Auditor> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Auditor.class))).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(Auditor.class), mock(Auditor.class)));

		AuditorDao dao = new AuditorDao(em);
		List<Auditor> auditors = dao.getAuditorsAll();

		assertNotNull(auditors);
		assertEquals(2, auditors.size());
	}

	@Test
	void shouldGetElectionAuditors() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Auditor> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Auditor.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of());

		AuditorDao dao = new AuditorDao(em);
		List<Auditor> auditors = dao.getElectionAuditors(123L);

		assertNotNull(auditors);
		assertTrue(auditors.isEmpty());
		verify(query).setParameter("electionId", 123L);
	}

	@Test
	void shouldCountElectionAuditors() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(9L);

		AuditorDao dao = new AuditorDao(em);
		long count = dao.countElectionAuditors(123L);

		assertEquals(9L, count);
	}

	@Test
	void shouldReportAuditorExistsWhenQueryHasResult() {
		EntityManager em = mock(EntityManager.class);
		Query query = mock(Query.class);
		when(em.createQuery(anyString())).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(1L));

		AuditorDao dao = new AuditorDao(em);
		boolean exists = dao.auditorExists(1L, "John", "john@example.com");

		assertTrue(exists);
		verify(query).setParameter("electionId", 1L);
		verify(query).setParameter("name", "John");
		verify(query).setParameter("mail", "john@example.com");
	}

	@Test
	void shouldGetAuditorByResultToken() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Auditor> query = mock(TypedQuery.class);
		Auditor auditor = mock(Auditor.class);
		when(em.createQuery(anyString(), eq(Auditor.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(auditor);

		AuditorDao dao = new AuditorDao(em);
		Auditor result = dao.getAuditorByResultToken("token");

		assertSame(auditor, result);
	}

	@Test
	void shouldGetAuditorsByEmailWithPagination() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Auditor> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Auditor.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.setFirstResult(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(Auditor.class)));

		AuditorDao dao = new AuditorDao(em);
		List<Auditor> result = dao.getAuditorsByEmail("a@b.com", 10, 2);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(query).setMaxResults(10);
		verify(query).setFirstResult(20);
	}
}
