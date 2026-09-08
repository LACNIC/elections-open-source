package net.lacnic.elections.ejb.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStage;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.utils.AuditorCandidateDecisionWindow;

class ElectionsManagerEJBBeanAuditorDecisionWindowTest {

	private static final long AUDITOR_ID = 559L;
	private static final long CANDIDATE_ID = 321L;

	@Test
	void publicDecisionShouldBeRejectedOutsideN7AndN8() throws Exception {
		EntityManager em = mock(EntityManager.class);
		TypedQuery<Long> calendarQuery = mockLongQuery(em, 0L);
		DecisionContext context = decisionContext();
		when(em.find(Auditor.class, AUDITOR_ID)).thenReturn(context.auditor);
		when(em.find(Candidate.class, CANDIDATE_ID)).thenReturn(context.candidate);
		ElectionsManagerEJBBean bean = beanWithEntityManager(em);

		boolean updated = bean.updateAuditorCandidateDecisionStatus(
				AUDITOR_ID,
				CANDIDATE_ID,
				AuditorCandidateDecisionStatus.PREAPPROVED,
				"AUDITOR_559",
				"127.0.0.1",
				"");

		assertFalse(updated);
		verify(calendarQuery).setParameter("calendarKeys", AuditorCandidateDecisionWindow.ALLOWED_CALENDAR_KEYS);
		verify(em, never()).persist(any());
		verify(em, never()).merge(any());
	}

	@Test
	void publicDecisionShouldRemainAvailableInsideN7OrN8() throws Exception {
		EntityManager em = mock(EntityManager.class);
		mockLongQuery(em, 1L);
		mockMissingDecisionQuery(em);
		DecisionContext context = decisionContext();
		when(em.find(Auditor.class, AUDITOR_ID)).thenReturn(context.auditor);
		when(em.find(Candidate.class, CANDIDATE_ID)).thenReturn(context.candidate);
		ElectionsManagerEJBBean bean = beanWithEntityManager(em);

		boolean updated = bean.updateAuditorCandidateDecisionStatus(
				AUDITOR_ID,
				CANDIDATE_ID,
				AuditorCandidateDecisionStatus.PREAPPROVED,
				"AUDITOR_559",
				"127.0.0.1",
				"");

		assertTrue(updated);
		verify(em).persist(any(AuditorCandidateDecision.class));
	}

	@Test
	void administrativeDecisionShouldRemainAvailableOutsideCalendarWindow() throws Exception {
		EntityManager em = mock(EntityManager.class);
		DecisionContext context = decisionContext();
		AuditorCandidateDecision existingDecision = new AuditorCandidateDecision();
		existingDecision.setAuditor(context.auditor);
		existingDecision.setCandidate(context.candidate);
		mockExistingDecisionQuery(em, existingDecision);
		when(em.find(Auditor.class, AUDITOR_ID)).thenReturn(context.auditor);
		when(em.find(Candidate.class, CANDIDATE_ID)).thenReturn(context.candidate);
		ElectionsManagerEJBBean bean = beanWithEntityManager(em);

		boolean updated = bean.updateAuditorCandidateDecisionStageStatus(
				AUDITOR_ID,
				CANDIDATE_ID,
				AuditorCandidateDecisionStage.PRE_VERIFICATION,
				AuditorCandidateDecisionStatus.PREAPPROVED,
				"ADMIN",
				"127.0.0.1",
				"");

		assertTrue(updated);
		verify(em).merge(existingDecision);
		verify(em, never()).createQuery(anyString(), eq(Long.class));
	}

	@Test
	void publicDecisionShouldRequireCommissionerOpenElectionAndAvailableLink() throws Exception {
		EntityManager em = mock(EntityManager.class);
		DecisionContext context = decisionContext();
		when(em.find(Auditor.class, AUDITOR_ID)).thenReturn(context.auditor);
		when(em.find(Candidate.class, CANDIDATE_ID)).thenReturn(context.candidate);
		ElectionsManagerEJBBean bean = beanWithEntityManager(em);

		context.auditor.setCommissioner(false);
		assertFalse(updatePreDecision(bean));

		context.auditor.setCommissioner(true);
		context.election.setClosed(true);
		assertFalse(updatePreDecision(bean));

		context.election.setClosed(false);
		context.election.setAuditorLinkAvailable(false);
		assertFalse(updatePreDecision(bean));

		verify(em, never()).createQuery(anyString(), eq(Long.class));
	}

	private boolean updatePreDecision(ElectionsManagerEJBBean bean) {
		return bean.updateAuditorCandidateDecisionStatus(
				AUDITOR_ID,
				CANDIDATE_ID,
				AuditorCandidateDecisionStatus.PREAPPROVED,
				"AUDITOR_559",
				"127.0.0.1",
				"");
	}

	private TypedQuery<Long> mockLongQuery(EntityManager em, long result) {
		@SuppressWarnings("unchecked")
		TypedQuery<Long> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(Long.valueOf(result));
		return query;
	}

	private void mockMissingDecisionQuery(EntityManager em) {
		@SuppressWarnings("unchecked")
		TypedQuery<AuditorCandidateDecision> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(AuditorCandidateDecision.class))).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultStream()).thenReturn(Stream.empty());
	}

	private void mockExistingDecisionQuery(EntityManager em, AuditorCandidateDecision decision) {
		@SuppressWarnings("unchecked")
		TypedQuery<AuditorCandidateDecision> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(AuditorCandidateDecision.class))).thenReturn(query);
		when(query.setParameter(anyString(), any())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultStream()).thenReturn(Stream.of(decision));
	}

	private ElectionsManagerEJBBean beanWithEntityManager(EntityManager em) throws Exception {
		ElectionsManagerEJBBean bean = new ElectionsManagerEJBBean();
		Field entityManagerField = ElectionsManagerEJBBean.class.getDeclaredField("em");
		entityManagerField.setAccessible(true);
		entityManagerField.set(bean, em);
		return bean;
	}

	private DecisionContext decisionContext() {
		Election election = new Election();
		election.setElectionId(95L);
		election.setClosed(false);
		election.setAuditorLinkAvailable(true);

		Auditor auditor = new Auditor();
		auditor.setAuditorId(AUDITOR_ID);
		auditor.setElection(election);
		auditor.setCommissioner(true);

		Candidate candidate = new Candidate();
		candidate.setCandidateId(CANDIDATE_ID);
		candidate.setElection(election);
		candidate.setStatus(CandidateStatus.PRECOMPLETE);
		return new DecisionContext(election, auditor, candidate);
	}

	private static final class DecisionContext {
		private final Election election;
		private final Auditor auditor;
		private final Candidate candidate;

		DecisionContext(Election election, Auditor auditor, Candidate candidate) {
			this.election = election;
			this.auditor = auditor;
			this.candidate = candidate;
		}
	}
}
