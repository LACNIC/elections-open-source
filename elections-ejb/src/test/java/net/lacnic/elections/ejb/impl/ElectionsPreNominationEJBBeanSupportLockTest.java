package net.lacnic.elections.ejb.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;

class ElectionsPreNominationEJBBeanSupportLockTest {

	@Test
	void shouldLockCandidateAggregateBeforeSupportStatusRow() throws Exception {
		long candidateId = 91L;
		long supportId = 101L;
		Candidate candidate = new Candidate();
		candidate.setCandidateId(candidateId);
		candidate.setElection(new Election());
		Nomination nomination = new Nomination();
		nomination.setId(71L);
		nomination.setCandidate(candidate);
		nomination.setStatus(NominationStatus.ACCEPTED_BY_CANDIDATE);
		SupportNomination support = new SupportNomination();
		support.setId(supportId);
		support.setNomination(nomination);
		support.setSupportStatus(SupportStatus.ACCEPTED);

		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<SupportNomination> supportByTokenQuery = mock(TypedQuery.class);
		when(em.createQuery(any(String.class), eq(SupportNomination.class))).thenReturn(supportByTokenQuery);
		when(supportByTokenQuery.setParameter(eq("token"), any())).thenReturn(supportByTokenQuery);
		when(supportByTokenQuery.getSingleResult()).thenReturn(support);
		when(em.find(Candidate.class, candidateId, LockModeType.PESSIMISTIC_WRITE)).thenReturn(candidate);
		when(em.find(SupportNomination.class, supportId, LockModeType.PESSIMISTIC_WRITE)).thenReturn(support);

		ElectionsPreNominationEJBBean bean = new ElectionsPreNominationEJBBean();
		Field entityManagerField = ElectionsPreNominationEJBBean.class.getDeclaredField("em");
		entityManagerField.setAccessible(true);
		entityManagerField.set(bean, em);

		assertFalse(bean.updateSupportNominationStatus("support-token", SupportStatus.REJECTED, "127.0.0.1"));
		InOrder lockOrder = inOrder(em);
		lockOrder.verify(em).find(Candidate.class, candidateId, LockModeType.PESSIMISTIC_WRITE);
		lockOrder.verify(em).find(SupportNomination.class, supportId, LockModeType.PESSIMISTIC_WRITE);
	}
}
