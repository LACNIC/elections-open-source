package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Commissioner;
import org.junit.jupiter.api.Test;

class CommissionerDaoTest {

	@Test
	void shouldGetCommissionerById() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Commissioner> query = mock(TypedQuery.class);
		Commissioner commissioner = mock(Commissioner.class);
		when(em.createQuery(anyString(), eq(Commissioner.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(commissioner);

		CommissionerDao dao = new CommissionerDao(em);
		Commissioner result = dao.getCommissioner(19L);

		assertSame(commissioner, result);
	}

	@Test
	void shouldGetAllCommissioners() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Commissioner> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Commissioner.class))).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(Commissioner.class)));

		CommissionerDao dao = new CommissionerDao(em);
		List<Commissioner> result = dao.getCommissionersAll();

		assertEquals(1, result.size());
	}

	@Test
	void shouldGetCommissionerByMailUsingTrimAndUppercase() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Commissioner> query = mock(TypedQuery.class);
		Commissioner commissioner = mock(Commissioner.class);
		when(em.createQuery(anyString(), eq(Commissioner.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(commissioner);

		CommissionerDao dao = new CommissionerDao(em);
		Commissioner result = dao.getCommissionerByMail("  test@mail.com ");

		assertSame(commissioner, result);
		verify(query).setParameter("mail", "TEST@MAIL.COM");
	}

	@Test
	void shouldReturnNullWhenGetCommissionerByMailFails() {
		EntityManager em = mock(EntityManager.class);
		when(em.createQuery(anyString(), eq(Commissioner.class))).thenThrow(new RuntimeException("db"));

		CommissionerDao dao = new CommissionerDao(em);
		assertNull(dao.getCommissionerByMail("test@mail.com"));
	}

	@Test
	void shouldReportCommissionerExists() {
		EntityManager em = mock(EntityManager.class);
		Query query = mock(Query.class);
		when(em.createQuery(anyString())).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(1L));

		CommissionerDao dao = new CommissionerDao(em);
		boolean exists = dao.commissionerExists(" John ", " john@mail.com ");

		assertTrue(exists);
		verify(query).setParameter("name", "JOHN");
		verify(query).setParameter("mail", "JOHN@MAIL.COM");
	}

	@Test
	void shouldReportCommissionerDoesNotExist() {
		EntityManager em = mock(EntityManager.class);
		Query query = mock(Query.class);
		when(em.createQuery(anyString())).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of());

		CommissionerDao dao = new CommissionerDao(em);
		boolean exists = dao.commissionerExists("John", "john@mail.com");

		assertFalse(exists);
	}

	@Test
	void shouldGetCommissionersIdAndDescriptionWithPaging() {
		EntityManager em = mock(EntityManager.class);
		Query query = mock(Query.class);
		when(em.createQuery(anyString())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.setFirstResult(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.<Object[]>of(new Object[] { 3L, "Name" }));

		CommissionerDao dao = new CommissionerDao(em);
		List<Object[]> result = dao.getCommissionersAllIdAndDescription(10, 2);

		assertEquals(1, result.size());
		assertEquals(3L, result.get(0)[0]);
		assertEquals("Name", result.get(0)[1]);
		verify(query).setMaxResults(10);
		verify(query).setFirstResult(20);
	}
}
