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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Activity;
import org.junit.jupiter.api.Test;

class ActivityDaoTest {

	@Test
	void shouldGetActivitiesAll() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Activity> query = mock(TypedQuery.class);
		Activity activity = mock(Activity.class);
		when(em.createQuery(anyString(), eq(Activity.class))).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(activity));

		ActivityDao dao = new ActivityDao(em);
		List<Activity> result = dao.getActivitiesAll();

		assertNotNull(result);
		assertEquals(1, result.size());
		assertSame(activity, result.get(0));
	}

	@Test
	void shouldGetActivityById() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Activity> query = mock(TypedQuery.class);
		Activity activity = mock(Activity.class);
		when(em.createQuery(anyString(), eq(Activity.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(activity);

		ActivityDao dao = new ActivityDao(em);
		Activity result = dao.getActivity(10L);

		assertSame(activity, result);
		verify(query).setParameter("activityId", 10L);
	}

	@Test
	void shouldGetElectionActivities() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Activity> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Activity.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of());

		ActivityDao dao = new ActivityDao(em);
		List<Activity> result = dao.getElectionActivities(55L);

		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(query).setParameter("electionId", 55L);
	}

	@Test
	void shouldGetActivitiesAllIdAndDescriptionWithPaging() {
		EntityManager em = mock(EntityManager.class);
		Query query = mock(Query.class);
		when(em.createQuery(anyString())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.setFirstResult(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.<Object[]>of(new Object[] { 1L, "activity" }));

		ActivityDao dao = new ActivityDao(em);
		List<Object[]> result = dao.getActivitiesAllIdAndDescription(10, 2);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(1L, result.get(0)[0]);
		assertEquals("activity", result.get(0)[1]);
		verify(query).setMaxResults(10);
		verify(query).setFirstResult(20);
	}

	@Test
	void shouldCountCandidateTextImprovementAttemptsSince() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		Date sinceDate = new Date(1_700_000_000_000L);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.setParameter(anyString(), any(Date.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(7L);

		ActivityDao dao = new ActivityDao(em);
		long result = dao.countCandidateTextImprovementAttemptsSince(3L, sinceDate);

		assertEquals(7L, result);
	}
}
