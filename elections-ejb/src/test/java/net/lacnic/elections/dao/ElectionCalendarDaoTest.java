package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;

class ElectionCalendarDaoTest {

	@Test
	void activeCalendarCountShouldRequireDefinedStartAndEndDates() {
		EntityManager entityManager = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(1L);
		Set<ElectionCalendarKey> keys = Set.of(ElectionCalendarKey.N_18_PERIODO_CE_AUDIT);
		Date referenceDate = new Date();

		long result = new ElectionCalendarDao(entityManager).countActiveCalendarsByKeys(95L, keys, referenceDate);

		ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
		verify(entityManager).createQuery(jpql.capture(), eq(Long.class));
		assertTrue(jpql.getValue().contains("c.startDate IS NOT NULL"));
		assertTrue(jpql.getValue().contains("c.endDate IS NOT NULL"));
		assertTrue(jpql.getValue().contains("c.startDate <= :referenceDate"));
		assertTrue(jpql.getValue().contains("c.endDate >= :referenceDate"));
		verify(query).setParameter("electionId", 95L);
		verify(query).setParameter("calendarKeys", keys);
		verify(query).setParameter("referenceDate", referenceDate);
		assertEquals(1L, result);
	}
}
