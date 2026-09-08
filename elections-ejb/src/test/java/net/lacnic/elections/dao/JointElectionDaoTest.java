package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.JointElection;
import org.junit.jupiter.api.Test;

class JointElectionDaoTest {

	@Test
	void shouldGetJointElectionsIdsWithPaging() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.setFirstResult(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(10L, 11L));

		JointElectionDao dao = new JointElectionDao(em);
		List<Long> result = dao.getJointElectionsIds(20, 2);

		assertEquals(List.of(10L, 11L), result);
		verify(query).setMaxResults(20);
		verify(query).setFirstResult(40);
	}

	@Test
	void shouldGetJointElectionById() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<JointElection> query = mock(TypedQuery.class);
		JointElection expected = mock(JointElection.class);
		when(em.createQuery(anyString(), eq(JointElection.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(expected);

		JointElectionDao dao = new JointElectionDao(em);
		JointElection result = dao.getJointElection(7L);

		assertSame(expected, result);
		verify(query).setParameter("jointElectionId", 7L);
	}
}
