package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.IpAccess;
import org.junit.jupiter.api.Test;

class IpAccessDaoTest {

	@Test
	void shouldGetIpByValue() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<IpAccess> query = mock(TypedQuery.class);
		IpAccess ipAccess = mock(IpAccess.class);
		when(em.createQuery(anyString(), eq(IpAccess.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(ipAccess);

		IpAccessDao dao = new IpAccessDao(em);
		IpAccess result = dao.getIP("1.2.3.4");

		assertSame(ipAccess, result);
	}

	@Test
	void shouldReturnNullWhenIpIsMissing() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<IpAccess> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(IpAccess.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.getSingleResult()).thenThrow(new NoResultException());

		IpAccessDao dao = new IpAccessDao(em);
		assertNull(dao.getIP("1.2.3.4"));
	}

	@Test
	void shouldReturnNullWhenIpQueryFails() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<IpAccess> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(IpAccess.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.getSingleResult()).thenThrow(new RuntimeException("db"));

		IpAccessDao dao = new IpAccessDao(em);
		assertNull(dao.getIP("1.2.3.4"));
	}

	@Test
	void shouldGetAllDisabledIps() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<IpAccess> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(IpAccess.class))).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(IpAccess.class)));

		IpAccessDao dao = new IpAccessDao(em);
		List<IpAccess> result = dao.getAllDisabledIPs();

		assertEquals(1, result.size());
	}

	@Test
	void shouldGetIpAccessIdAndDescriptionWithPaging() {
		EntityManager em = mock(EntityManager.class);
		Query query = mock(Query.class);
		when(em.createQuery(anyString())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.setFirstResult(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.<Object[]>of(new Object[] { 5L, "10.0.0.1" }));

		IpAccessDao dao = new IpAccessDao(em);
		List<Object[]> result = dao.getIpAccessesAllIdAndDescription(25, 3);

		assertEquals(1, result.size());
		assertEquals(5L, result.get(0)[0]);
		assertEquals("10.0.0.1", result.get(0)[1]);
		verify(query).setMaxResults(25);
		verify(query).setFirstResult(75);
	}

	@Test
	void shouldGetIpAccessById() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<IpAccess> query = mock(TypedQuery.class);
		IpAccess ipAccess = mock(IpAccess.class);
		when(em.createQuery(anyString(), eq(IpAccess.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(ipAccess);

		IpAccessDao dao = new IpAccessDao(em);
		IpAccess result = dao.getIpAccess(91L);

		assertSame(ipAccess, result);
		verify(query).setParameter("ipAccessId", 91L);
	}
}
