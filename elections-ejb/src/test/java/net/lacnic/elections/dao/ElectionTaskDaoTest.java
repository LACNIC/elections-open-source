package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
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
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import org.junit.jupiter.api.Test;

class ElectionTaskDaoTest {

	@Test
	void shouldGetElectionTasks() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<ElectionTask> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(ElectionTask.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(ElectionTask.class)));

		ElectionTaskDao dao = new ElectionTaskDao(em);
		List<ElectionTask> result = dao.getElectionTasks(9L);

		assertEquals(1, result.size());
		verify(query).setParameter("electionId", 9L);
	}

	@Test
	void shouldGetElectionTaskKeys() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<ElectionTaskKey> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(ElectionTaskKey.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(ElectionTaskKey.PROFILE));

		ElectionTaskDao dao = new ElectionTaskDao(em);
		List<ElectionTaskKey> result = dao.getElectionTaskKeys(9L);

		assertEquals(List.of(ElectionTaskKey.PROFILE), result);
	}

	@Test
	void shouldGetElectionTaskByIdUsingEntityManagerFind() {
		EntityManager em = mock(EntityManager.class);
		ElectionTask task = mock(ElectionTask.class);
		when(em.find(ElectionTask.class, 13L)).thenReturn(task);

		ElectionTaskDao dao = new ElectionTaskDao(em);
		ElectionTask result = dao.getElectionTask(13L);

		assertSame(task, result);
	}

	@Test
	void shouldGetElectionTaskByKeyReturnsFirstResult() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<ElectionTask> query = mock(TypedQuery.class);
		ElectionTask task = mock(ElectionTask.class);
		when(em.createQuery(anyString(), eq(ElectionTask.class))).thenReturn(query);
		when(query.setParameter(eq("electionId"), anyLong())).thenReturn(query);
		when(query.setParameter(eq("taskKey"), any(ElectionTaskKey.class))).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(task));

		ElectionTaskDao dao = new ElectionTaskDao(em);
		ElectionTask result = dao.getElectionTaskByKey(8L, ElectionTaskKey.PROFILE);

		assertSame(task, result);
	}

	@Test
	void shouldGetElectionTaskByKeyReturnsNullWhenMissing() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<ElectionTask> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(ElectionTask.class))).thenReturn(query);
		when(query.setParameter(eq("electionId"), anyLong())).thenReturn(query);
		when(query.setParameter(eq("taskKey"), any(ElectionTaskKey.class))).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of());

		ElectionTaskDao dao = new ElectionTaskDao(em);
		ElectionTask result = dao.getElectionTaskByKey(8L, ElectionTaskKey.PROFILE);

		assertNull(result);
	}

	@Test
	void shouldCountTaskProgress() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(6L);

		ElectionTaskDao dao = new ElectionTaskDao(em);
		long result = dao.countTaskProgress(99L);

		assertEquals(6L, result);
		verify(query).setParameter("electionTaskId", 99L);
	}

	@Test
	void shouldCountElectionTasks() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(4L);

		ElectionTaskDao dao = new ElectionTaskDao(em);
		long result = dao.countElectionTasks(20L);

		assertEquals(4L, result);
	}

	@Test
	void shouldCountElectionTasksReturnsZeroWhenValueIsNull() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(null);

		ElectionTaskDao dao = new ElectionTaskDao(em);
		long result = dao.countElectionTasks(20L);

		assertEquals(0L, result);
	}
}
