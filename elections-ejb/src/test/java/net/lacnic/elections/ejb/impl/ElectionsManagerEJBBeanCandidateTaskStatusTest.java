package net.lacnic.elections.ejb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;

class ElectionsManagerEJBBeanCandidateTaskStatusTest {

	@Test
	void pendingStatusClearsTaskDates() {
		CandidateElectionTaskProgress progress = progressWithDates();

		ElectionsManagerEJBBean.applyManualCandidateTaskStatus(progress, CandidateElectionTaskStatus.NOT_STARTED, new Date(3000L));

		assertEquals(CandidateElectionTaskStatus.NOT_STARTED, progress.getStatus());
		assertNull(progress.getStartDate());
		assertNull(progress.getEndDate());
	}

	@Test
	void startedStatusKeepsExistingStartAndClearsEnd() {
		CandidateElectionTaskProgress progress = progressWithDates();
		Date originalStart = progress.getStartDate();

		ElectionsManagerEJBBean.applyManualCandidateTaskStatus(progress, CandidateElectionTaskStatus.STARTED, new Date(3000L));

		assertEquals(CandidateElectionTaskStatus.STARTED, progress.getStatus());
		assertSame(originalStart, progress.getStartDate());
		assertNull(progress.getEndDate());
	}

	@Test
	void completedStatusInitializesStartAndEndDates() {
		CandidateElectionTaskProgress progress = new CandidateElectionTaskProgress();
		Date now = new Date(3000L);

		ElectionsManagerEJBBean.applyManualCandidateTaskStatus(progress, CandidateElectionTaskStatus.COMPLETED, now);

		assertEquals(CandidateElectionTaskStatus.COMPLETED, progress.getStatus());
		assertSame(now, progress.getStartDate());
		assertSame(now, progress.getEndDate());
	}

	@Test
	void manualUpdateLocksAndAuditsChangedTask() throws Exception {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateElectionTaskProgress> query = mock(TypedQuery.class, Answers.RETURNS_SELF);
		Candidate candidate = new Candidate();
		candidate.setCandidateId(42L);
		candidate.setName("Candidate");
		Election election = new Election();
		election.setElectionId(9L);
		election.setTitleSpanish("Election");
		candidate.setElection(election);
		ElectionTask task = new ElectionTask();
		task.setTaskKey(ElectionTaskKey.EVALUATION);
		CandidateElectionTaskProgress progress = new CandidateElectionTaskProgress(candidate, task, CandidateElectionTaskStatus.COMPLETED);
		progress.setStartDate(new Date(1000L));
		progress.setEndDate(new Date(2000L));
		when(em.find(Candidate.class, 42L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(candidate);
		when(em.createQuery(anyString(), eq(CandidateElectionTaskProgress.class))).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(progress));

		ElectionsManagerEJBBean bean = new ElectionsManagerEJBBean();
		setEntityManager(bean, em);
		Map<ElectionTaskKey, CandidateElectionTaskStatus> statuses = new EnumMap<>(ElectionTaskKey.class);
		statuses.put(ElectionTaskKey.EVALUATION, CandidateElectionTaskStatus.NOT_STARTED);

		assertTrue(bean.updateCandidateTaskStatuses(42L, statuses, "admin", "127.0.0.1"));
		assertEquals(CandidateElectionTaskStatus.NOT_STARTED, progress.getStatus());
		assertNull(progress.getStartDate());
		assertNull(progress.getEndDate());
		verify(query).setLockMode(LockModeType.PESSIMISTIC_WRITE);
		verify(em).merge(progress);
		verify(em).persist(any(Activity.class));
	}

	private CandidateElectionTaskProgress progressWithDates() {
		CandidateElectionTaskProgress progress = new CandidateElectionTaskProgress();
		progress.setStatus(CandidateElectionTaskStatus.COMPLETED);
		progress.setStartDate(new Date(1000L));
		progress.setEndDate(new Date(2000L));
		return progress;
	}

	private void setEntityManager(ElectionsManagerEJBBean bean, EntityManager em) throws Exception {
		Field field = ElectionsManagerEJBBean.class.getDeclaredField("em");
		field.setAccessible(true);
		field.set(bean, em);
	}
}
