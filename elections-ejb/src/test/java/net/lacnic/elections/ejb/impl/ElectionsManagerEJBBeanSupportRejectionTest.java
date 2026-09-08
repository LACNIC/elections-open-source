package net.lacnic.elections.ejb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;

class ElectionsManagerEJBBeanSupportRejectionTest {

	private static final long ELECTION_ID = 81L;
	private static final long CANDIDATE_ID = 91L;
	private static final long SUPPORT_ID = 101L;

	@Test
	void shouldReadRealSupportTaskStatusesAndKeepAbsentDistinctFromOmitted() throws Exception {
		Candidate candidate = new Candidate();
		candidate.setCandidateId(CANDIDATE_ID);
		List<CandidateElectionTaskProgress> persistedProgress = List.of(
				task(candidate, ElectionTaskKey.ORG_SUPPORTS, CandidateElectionTaskStatus.OMITTED),
				task(candidate, ElectionTaskKey.USER_SUPPORTS_2, CandidateElectionTaskStatus.STARTED),
				task(candidate, ElectionTaskKey.COURSE, CandidateElectionTaskStatus.COMPLETED));
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<CandidateElectionTaskProgress> query = mock(TypedQuery.class);
		when(em.createQuery(contains("WHERE tp.candidate.candidateId = :candidateId"), eq(CandidateElectionTaskProgress.class))).thenReturn(query);
		when(query.setParameter(any(String.class), any())).thenReturn(query);
		when(query.getResultList()).thenReturn(persistedProgress);

		Map<ElectionTaskKey, CandidateElectionTaskStatus> statuses = beanWithEntityManager(em)
				.getCandidateSupportTaskStatuses(CANDIDATE_ID);

		assertEquals(CandidateElectionTaskStatus.OMITTED, statuses.get(ElectionTaskKey.ORG_SUPPORTS));
		assertEquals(CandidateElectionTaskStatus.STARTED, statuses.get(ElectionTaskKey.USER_SUPPORTS_2));
		assertFalse(statuses.containsKey(ElectionTaskKey.USER_SUPPORTS_5));
		assertFalse(statuses.containsKey(ElectionTaskKey.COURSE));
		verify(query, never()).setLockMode(any(LockModeType.class));
	}

	@Test
	void shouldRejectOrganizationSupportReopenOnlyOrganizationTaskAndAuditChange() throws Exception {
		TestContext context = context(true);
		context.supports.add(acceptedOrganizationSupport(context.nomination, 102L));
		addAcceptedUserSupports(context, 5);
		CandidateElectionTaskProgress organizationTask = completedTask(context.candidate, ElectionTaskKey.ORG_SUPPORTS);
		CandidateElectionTaskProgress userTwoTask = task(context.candidate, ElectionTaskKey.USER_SUPPORTS_2, CandidateElectionTaskStatus.NOT_STARTED);
		CandidateElectionTaskProgress userFiveTask = task(context.candidate, ElectionTaskKey.USER_SUPPORTS_5, CandidateElectionTaskStatus.NOT_STARTED);
		context.tasks.put(ElectionTaskKey.ORG_SUPPORTS, organizationTask);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_2, userTwoTask);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_5, userFiveTask);
		EntityManager em = mockEntityManager(context);

		boolean rejected = beanWithEntityManager(em).rejectCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "  organización incorrecta  ", "admin-user", "203.0.113.5");

		assertTrue(rejected);
		assertEquals(SupportStatus.REJECTED, context.target.getSupportStatus());
		assertEquals(CandidateElectionTaskStatus.STARTED, organizationTask.getStatus());
		assertEquals(CandidateElectionTaskStatus.NOT_STARTED, userTwoTask.getStatus());
		assertEquals(CandidateElectionTaskStatus.NOT_STARTED, userFiveTask.getStatus());
		ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
		verify(em).persist(activityCaptor.capture());
		Activity activity = activityCaptor.getValue();
		assertEquals("ADMIN-USER", activity.getUserName());
		assertEquals("203.0.113.5", activity.getIp());
		assertEquals(ActivityType.EDIT_CANDIDATES, activity.getActivityType());
		assertTrue(activity.getDescription().contains("supportNominationId=" + SUPPORT_ID));
		assertTrue(activity.getDescription().contains("previousStatus=ACCEPTED"));
		assertTrue(activity.getDescription().contains("newStatus=REJECTED"));
		assertTrue(activity.getDescription().contains("rechazó administrativamente"));
		assertTrue(activity.getDescription().contains("comment=organización incorrecta"));
		InOrder lockOrder = inOrder(em);
		lockOrder.verify(em).find(Candidate.class, CANDIDATE_ID, LockModeType.PESSIMISTIC_WRITE);
		lockOrder.verify(em).find(SupportNomination.class, SUPPORT_ID, LockModeType.PESSIMISTIC_WRITE);
	}

	@Test
	void shouldReopenUserFiveTaskButKeepUserTwoCompleted() throws Exception {
		TestContext context = context(false);
		addAcceptedUserSupports(context, 3);
		context.supports.add(acceptedOrganizationSupport(context.nomination, 201L));
		context.supports.add(acceptedOrganizationSupport(context.nomination, 202L));
		CandidateElectionTaskProgress userTwoTask = completedTask(context.candidate, ElectionTaskKey.USER_SUPPORTS_2);
		CandidateElectionTaskProgress userFiveTask = completedTask(context.candidate, ElectionTaskKey.USER_SUPPORTS_5);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_2, userTwoTask);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_5, userFiveTask);
		EntityManager em = mockEntityManager(context);

		boolean rejected = beanWithEntityManager(em).rejectCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "duplicate contact", "ADMIN", "127.0.0.1");

		assertTrue(rejected);
		assertEquals(CandidateElectionTaskStatus.COMPLETED, userTwoTask.getStatus());
		assertEquals(CandidateElectionTaskStatus.STARTED, userFiveTask.getStatus());
	}

	@Test
	void shouldPreserveOmittedTaskAndRejectInvalidRequestsWithoutMutation() throws Exception {
		TestContext context = context(true);
		CandidateElectionTaskProgress omittedTask = task(context.candidate, ElectionTaskKey.ORG_SUPPORTS, CandidateElectionTaskStatus.OMITTED);
		context.tasks.put(ElectionTaskKey.ORG_SUPPORTS, omittedTask);
		EntityManager em = mockEntityManager(context);
		ElectionsManagerEJBBean bean = beanWithEntityManager(em);

		assertFalse(bean.rejectCandidateSupport(CANDIDATE_ID, SUPPORT_ID, "   ", "ADMIN", "127.0.0.1"));
		assertEquals(SupportStatus.ACCEPTED, context.target.getSupportStatus());

		context.candidate.setStatus(CandidateStatus.COMPLETE);
		assertFalse(bean.rejectCandidateSupport(CANDIDATE_ID, SUPPORT_ID, "invalid", "ADMIN", "127.0.0.1"));
		assertEquals(SupportStatus.ACCEPTED, context.target.getSupportStatus());
		assertEquals(CandidateElectionTaskStatus.OMITTED, omittedTask.getStatus());
		verify(em, never()).persist(any(Activity.class));
	}

	@Test
	void shouldRejectMissingOrOversizedCommentsBeforeLoadingOrMutatingSupport() throws Exception {
		EntityManager em = mock(EntityManager.class);
		ElectionsManagerEJBBean bean = beanWithEntityManager(em);
		String oversizedComment = "x".repeat(4001);

		assertFalse(bean.rejectCandidateSupport(CANDIDATE_ID, SUPPORT_ID, "   ", "ADMIN", "127.0.0.1"));
		assertFalse(bean.approveRejectedCandidateSupport(CANDIDATE_ID, SUPPORT_ID, null, "ADMIN", "127.0.0.1"));
		assertFalse(bean.returnRejectedCandidateSupportToProposal(
				CANDIDATE_ID, SUPPORT_ID, oversizedComment, "ADMIN", "127.0.0.1"));

		verify(em, never()).find(any(Class.class), any(), any(LockModeType.class));
		verify(em, never()).persist(any(Activity.class));
	}

	@Test
	void shouldRejectOnlyNoOpActionsWithoutMutation() throws Exception {
		TestContext acceptedContext = context(false);
		EntityManager acceptedEntityManager = mockEntityManager(acceptedContext);
		ElectionsManagerEJBBean acceptedBean = beanWithEntityManager(acceptedEntityManager);

		assertFalse(acceptedBean.approveRejectedCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "valid comment", "ADMIN", "127.0.0.1"));
		assertEquals(SupportStatus.ACCEPTED, acceptedContext.target.getSupportStatus());
		verify(acceptedEntityManager, never()).persist(any(Activity.class));

		TestContext proposedContext = context(false);
		proposedContext.target.setSupportStatus(SupportStatus.PROPOSED);
		EntityManager proposedEntityManager = mockEntityManager(proposedContext);
		assertFalse(beanWithEntityManager(proposedEntityManager).returnRejectedCandidateSupportToProposal(
				CANDIDATE_ID, SUPPORT_ID, "valid comment", "ADMIN", "127.0.0.1"));
		assertEquals(SupportStatus.PROPOSED, proposedContext.target.getSupportStatus());
		verify(proposedEntityManager, never()).persist(any(Activity.class));

		TestContext rejectedContext = context(false);
		rejectedContext.target.setSupportStatus(SupportStatus.REJECTED);
		EntityManager rejectedEntityManager = mockEntityManager(rejectedContext);

		assertFalse(beanWithEntityManager(rejectedEntityManager).rejectCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "valid comment", "ADMIN", "127.0.0.1"));
		assertEquals(SupportStatus.REJECTED, rejectedContext.target.getSupportStatus());
		verify(rejectedEntityManager, never()).persist(any(Activity.class));
	}

	@Test
	void shouldTransitionProposedSupportToRejectedAndAuditActualPreviousStatus() throws Exception {
		TestContext context = context(false);
		context.target.setSupportStatus(SupportStatus.PROPOSED);
		EntityManager em = mockEntityManager(context);

		assertTrue(beanWithEntityManager(em).rejectCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "administrative rejection", "ADMIN", "127.0.0.1"));

		assertEquals(SupportStatus.REJECTED, context.target.getSupportStatus());
		assertNotNull(context.target.getSupportResponseInstant());
		ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
		verify(em).persist(activityCaptor.capture());
		assertTrue(activityCaptor.getValue().getDescription().contains("previousStatus=PROPOSED"));
		assertTrue(activityCaptor.getValue().getDescription().contains("newStatus=REJECTED"));
	}

	@Test
	void shouldTransitionProposedSupportToAcceptedWhenCapacityIsAvailable() throws Exception {
		TestContext context = context(false);
		context.target.setSupportStatus(SupportStatus.PROPOSED);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_2,
				task(context.candidate, ElectionTaskKey.USER_SUPPORTS_2, CandidateElectionTaskStatus.STARTED));
		EntityManager em = mockEntityManager(context);

		assertTrue(beanWithEntityManager(em).approveRejectedCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "administrative approval", "ADMIN", "127.0.0.1"));

		assertEquals(SupportStatus.ACCEPTED, context.target.getSupportStatus());
		assertNotNull(context.target.getSupportResponseInstant());
		ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
		verify(em).persist(activityCaptor.capture());
		assertTrue(activityCaptor.getValue().getDescription().contains("previousStatus=PROPOSED"));
		assertTrue(activityCaptor.getValue().getDescription().contains("newStatus=ACCEPTED"));
	}

	@Test
	void shouldTransitionAcceptedAndInvalidSupportsToProposal() throws Exception {
		TestContext acceptedContext = context(false);
		EntityManager acceptedEntityManager = mockEntityManager(acceptedContext);

		assertTrue(beanWithEntityManager(acceptedEntityManager).returnRejectedCandidateSupportToProposal(
				CANDIDATE_ID, SUPPORT_ID, "return accepted", "ADMIN", "127.0.0.1"));
		assertEquals(SupportStatus.PROPOSED, acceptedContext.target.getSupportStatus());
		assertNull(acceptedContext.target.getSupportResponseInstant());

		TestContext invalidContext = context(false);
		invalidContext.target.setSupportStatus(SupportStatus.INVALID);
		EntityManager invalidEntityManager = mockEntityManager(invalidContext);

		assertTrue(beanWithEntityManager(invalidEntityManager).returnRejectedCandidateSupportToProposal(
				CANDIDATE_ID, SUPPORT_ID, "return invalid", "ADMIN", "127.0.0.1"));
		assertEquals(SupportStatus.PROPOSED, invalidContext.target.getSupportStatus());
		assertNull(invalidContext.target.getSupportResponseInstant());
	}

	@Test
	void shouldAllowActiveSupportToChangeTargetAtFullCapacity() throws Exception {
		TestContext context = context(false);
		context.target.setSupportStatus(SupportStatus.APPROVED);
		context.supports.add(acceptedUserSupport(context.nomination, 102L));
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_2,
				task(context.candidate, ElectionTaskKey.USER_SUPPORTS_2, CandidateElectionTaskStatus.COMPLETED));
		EntityManager em = mockEntityManager(context);

		assertTrue(beanWithEntityManager(em).approveRejectedCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "normalize active support", "ADMIN", "127.0.0.1"));

		assertEquals(SupportStatus.ACCEPTED, context.target.getSupportStatus());
	}

	@Test
	void shouldAllowEverySupportStatusToTransitionToEveryDifferentAdministrativeTarget() throws Exception {
		List<SupportStatus> targetStatuses = List.of(SupportStatus.REJECTED, SupportStatus.PROPOSED, SupportStatus.ACCEPTED);
		for (SupportStatus sourceStatus : SupportStatus.values()) {
			for (SupportStatus targetStatus : targetStatuses) {
				TestContext context = context(false);
				context.target.setSupportStatus(sourceStatus);
				EntityManager em = mockEntityManager(context);
				ElectionsManagerEJBBean bean = beanWithEntityManager(em);

				boolean changed = transitionSupport(bean, targetStatus);

				boolean expectedChange = sourceStatus != targetStatus;
				assertEquals(expectedChange, changed, sourceStatus + " -> " + targetStatus);
				assertEquals(expectedChange ? targetStatus : sourceStatus, context.target.getSupportStatus(),
						sourceStatus + " -> " + targetStatus);
				if (expectedChange) {
					verify(em).persist(any(Activity.class));
				} else {
					verify(em, never()).persist(any(Activity.class));
				}
			}
		}
	}

	private boolean transitionSupport(ElectionsManagerEJBBean bean, SupportStatus targetStatus) {
		if (targetStatus == SupportStatus.REJECTED) {
			return bean.rejectCandidateSupport(CANDIDATE_ID, SUPPORT_ID, "matrix transition", "ADMIN", "127.0.0.1");
		}
		if (targetStatus == SupportStatus.ACCEPTED) {
			return bean.approveRejectedCandidateSupport(CANDIDATE_ID, SUPPORT_ID, "matrix transition", "ADMIN", "127.0.0.1");
		}
		return bean.returnRejectedCandidateSupportToProposal(CANDIDATE_ID, SUPPORT_ID, "matrix transition", "ADMIN", "127.0.0.1");
	}

	@Test
	void shouldAcceptCommentAtMaximumLength() throws Exception {
		TestContext context = context(false);
		context.target.setSupportStatus(SupportStatus.REJECTED);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_2,
				task(context.candidate, ElectionTaskKey.USER_SUPPORTS_2, CandidateElectionTaskStatus.STARTED));
		EntityManager em = mockEntityManager(context);
		String maximumLengthComment = "x".repeat(4000);

		assertTrue(beanWithEntityManager(em).returnRejectedCandidateSupportToProposal(
				CANDIDATE_ID, SUPPORT_ID, maximumLengthComment, "ADMIN", "127.0.0.1"));

		ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
		verify(em).persist(activityCaptor.capture());
		assertTrue(activityCaptor.getValue().getDescription().endsWith("comment=" + maximumLengthComment));
	}

	@Test
	void shouldApproveRejectedUserSupportCompleteApplicableTaskAndAuditChange() throws Exception {
		TestContext context = context(false);
		context.target.setSupportStatus(SupportStatus.REJECTED);
		context.target.setSupportResponseInstant(new Date(1_000L));
		context.supports.add(acceptedUserSupport(context.nomination, 102L));
		CandidateElectionTaskProgress userTwoTask = task(context.candidate, ElectionTaskKey.USER_SUPPORTS_2, CandidateElectionTaskStatus.STARTED);
		CandidateElectionTaskProgress userFiveTask = task(context.candidate, ElectionTaskKey.USER_SUPPORTS_5, CandidateElectionTaskStatus.OMITTED);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_2, userTwoTask);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_5, userFiveTask);
		EntityManager em = mockEntityManager(context);

		boolean approved = beanWithEntityManager(em).approveRejectedCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "  revisión aprobada  ", "admin-user", "203.0.113.6");

		assertTrue(approved);
		assertEquals(SupportStatus.ACCEPTED, context.target.getSupportStatus());
		assertNotNull(context.target.getSupportResponseInstant());
		assertEquals(CandidateElectionTaskStatus.COMPLETED, userTwoTask.getStatus());
		assertEquals(CandidateElectionTaskStatus.OMITTED, userFiveTask.getStatus());
		ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
		verify(em).persist(activityCaptor.capture());
		Activity activity = activityCaptor.getValue();
		assertEquals("ADMIN-USER", activity.getUserName());
		assertEquals("203.0.113.6", activity.getIp());
		assertTrue(activity.getDescription().contains("supportNominationId=" + SUPPORT_ID));
		assertTrue(activity.getDescription().contains("aprobó administrativamente"));
		assertTrue(activity.getDescription().contains("previousStatus=REJECTED"));
		assertTrue(activity.getDescription().contains("newStatus=ACCEPTED"));
		assertTrue(activity.getDescription().contains("comment=revisión aprobada"));
		InOrder lockOrder = inOrder(em);
		lockOrder.verify(em).find(Candidate.class, CANDIDATE_ID, LockModeType.PESSIMISTIC_WRITE);
		lockOrder.verify(em).find(SupportNomination.class, SUPPORT_ID, LockModeType.PESSIMISTIC_WRITE);
	}

	@Test
	void shouldApproveSecondRejectedUserSupportCompleteLowerTaskAndStartNextTask() throws Exception {
		TestContext context = context(false);
		context.target.setSupportStatus(SupportStatus.REJECTED);
		context.supports.add(acceptedUserSupport(context.nomination, 102L));
		CandidateElectionTaskProgress userTwoTask = task(context.candidate, ElectionTaskKey.USER_SUPPORTS_2, CandidateElectionTaskStatus.NOT_STARTED);
		CandidateElectionTaskProgress userFiveTask = task(context.candidate, ElectionTaskKey.USER_SUPPORTS_5, CandidateElectionTaskStatus.NOT_STARTED);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_2, userTwoTask);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_5, userFiveTask);
		EntityManager em = mockEntityManager(context);

		assertTrue(beanWithEntityManager(em).approveRejectedCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "valid comment", "ADMIN", "127.0.0.1"));

		assertEquals(CandidateElectionTaskStatus.COMPLETED, userTwoTask.getStatus());
		assertEquals(CandidateElectionTaskStatus.STARTED, userFiveTask.getStatus());
	}

	@Test
	void shouldReturnRejectedUserSupportToProposalStartOnlyLowerTask() throws Exception {
		TestContext context = context(false);
		context.target.setSupportStatus(SupportStatus.REJECTED);
		CandidateElectionTaskProgress userTwoTask = task(context.candidate, ElectionTaskKey.USER_SUPPORTS_2, CandidateElectionTaskStatus.NOT_STARTED);
		CandidateElectionTaskProgress userFiveTask = task(context.candidate, ElectionTaskKey.USER_SUPPORTS_5, CandidateElectionTaskStatus.NOT_STARTED);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_2, userTwoTask);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_5, userFiveTask);
		EntityManager em = mockEntityManager(context);

		assertTrue(beanWithEntityManager(em).returnRejectedCandidateSupportToProposal(
				CANDIDATE_ID, SUPPORT_ID, "valid comment", "ADMIN", "127.0.0.1"));

		assertEquals(CandidateElectionTaskStatus.STARTED, userTwoTask.getStatus());
		assertEquals(CandidateElectionTaskStatus.NOT_STARTED, userFiveTask.getStatus());
	}

	@Test
	void shouldApproveFifthRejectedUserSupportAndKeepLowerThresholdCompleted() throws Exception {
		TestContext context = context(false);
		context.target.setSupportStatus(SupportStatus.REJECTED);
		addAcceptedUserSupports(context, 4);
		CandidateElectionTaskProgress userTwoTask = completedTask(context.candidate, ElectionTaskKey.USER_SUPPORTS_2);
		CandidateElectionTaskProgress userFiveTask = task(context.candidate, ElectionTaskKey.USER_SUPPORTS_5, CandidateElectionTaskStatus.STARTED);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_2, userTwoTask);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_5, userFiveTask);
		EntityManager em = mockEntityManager(context);

		assertTrue(beanWithEntityManager(em).approveRejectedCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "valid comment", "ADMIN", "127.0.0.1"));

		assertEquals(CandidateElectionTaskStatus.COMPLETED, userTwoTask.getStatus());
		assertEquals(CandidateElectionTaskStatus.COMPLETED, userFiveTask.getStatus());
	}

	@Test
	void shouldReturnRejectedOrganizationSupportToProposalClearDateAndStartTask() throws Exception {
		TestContext context = context(true);
		context.target.setSupportStatus(SupportStatus.REJECTED);
		context.target.setSupportResponseInstant(new Date(1_000L));
		context.supports.add(acceptedOrganizationSupport(context.nomination, 102L));
		CandidateElectionTaskProgress organizationTask = task(context.candidate, ElectionTaskKey.ORG_SUPPORTS, CandidateElectionTaskStatus.NOT_STARTED);
		CandidateElectionTaskProgress userTwoTask = task(context.candidate, ElectionTaskKey.USER_SUPPORTS_2, CandidateElectionTaskStatus.NOT_STARTED);
		CandidateElectionTaskProgress userFiveTask = task(context.candidate, ElectionTaskKey.USER_SUPPORTS_5, CandidateElectionTaskStatus.NOT_STARTED);
		context.tasks.put(ElectionTaskKey.ORG_SUPPORTS, organizationTask);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_2, userTwoTask);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_5, userFiveTask);
		EntityManager em = mockEntityManager(context);

		boolean returned = beanWithEntityManager(em).returnRejectedCandidateSupportToProposal(
				CANDIDATE_ID, SUPPORT_ID, "  requiere nueva respuesta  ", "ADMIN", "127.0.0.1");

		assertTrue(returned);
		assertEquals(SupportStatus.PROPOSED, context.target.getSupportStatus());
		assertNull(context.target.getSupportResponseInstant());
		assertEquals(CandidateElectionTaskStatus.STARTED, organizationTask.getStatus());
		assertEquals(CandidateElectionTaskStatus.NOT_STARTED, userTwoTask.getStatus());
		assertEquals(CandidateElectionTaskStatus.NOT_STARTED, userFiveTask.getStatus());
		ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
		verify(em).persist(activityCaptor.capture());
		assertTrue(activityCaptor.getValue().getDescription().contains("devolvió administrativamente un apoyo a propuesta"));
		assertTrue(activityCaptor.getValue().getDescription().contains("newStatus=PROPOSED"));
		assertTrue(activityCaptor.getValue().getDescription().contains("comment=requiere nueva respuesta"));
	}

	@Test
	void shouldReturnRejectedOrganizationSupportWithOneAcceptedAndAnotherRejected() throws Exception {
		TestContext context = context(true);
		context.target.setSupportStatus(SupportStatus.REJECTED);
		context.target.setSupportResponseInstant(new Date(1_000L));
		context.supports.add(acceptedOrganizationSupport(context.nomination, 102L));
		SupportNomination otherRejectedSupport = acceptedOrganizationSupport(context.nomination, 103L);
		otherRejectedSupport.setSupportStatus(SupportStatus.REJECTED);
		context.supports.add(otherRejectedSupport);
		CandidateElectionTaskProgress organizationTask = task(
				context.candidate, ElectionTaskKey.ORG_SUPPORTS, CandidateElectionTaskStatus.COMPLETED);
		context.tasks.put(ElectionTaskKey.ORG_SUPPORTS, organizationTask);
		EntityManager em = mockEntityManager(context);

		boolean returned = beanWithEntityManager(em).returnRejectedCandidateSupportToProposal(
				CANDIDATE_ID, SUPPORT_ID, "revisión exacta del caso", "ADMIN", "127.0.0.1");

		assertTrue(returned);
		assertEquals(SupportStatus.PROPOSED, context.target.getSupportStatus());
		assertNull(context.target.getSupportResponseInstant());
		assertEquals(SupportStatus.REJECTED, otherRejectedSupport.getSupportStatus());
		assertEquals(CandidateElectionTaskStatus.STARTED, organizationTask.getStatus());
		ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
		verify(em).persist(activityCaptor.capture());
		assertTrue(activityCaptor.getValue().getDescription().contains("newStatus=PROPOSED"));
		assertTrue(activityCaptor.getValue().getDescription().contains("comment=revisión exacta del caso"));
	}

	@Test
	void shouldRejectThirdOrganizationReactivationWhenAcceptedAndProposedConsumeCapacity() throws Exception {
		TestContext context = context(true);
		context.target.setSupportStatus(SupportStatus.REJECTED);
		context.target.setSupportResponseInstant(new Date(1_000L));
		context.supports.add(acceptedOrganizationSupport(context.nomination, 102L));
		SupportNomination proposedSupport = acceptedOrganizationSupport(context.nomination, 103L);
		proposedSupport.setSupportStatus(SupportStatus.PROPOSED);
		context.supports.add(proposedSupport);
		CandidateElectionTaskProgress organizationTask = task(
				context.candidate, ElectionTaskKey.ORG_SUPPORTS, CandidateElectionTaskStatus.STARTED);
		context.tasks.put(ElectionTaskKey.ORG_SUPPORTS, organizationTask);
		EntityManager em = mockEntityManager(context);

		boolean returned = beanWithEntityManager(em).returnRejectedCandidateSupportToProposal(
				CANDIDATE_ID, SUPPORT_ID, "sin capacidad", "ADMIN", "127.0.0.1");

		assertFalse(returned);
		assertEquals(SupportStatus.REJECTED, context.target.getSupportStatus());
		assertNotNull(context.target.getSupportResponseInstant());
		assertEquals(CandidateElectionTaskStatus.STARTED, organizationTask.getStatus());
		verify(em, never()).persist(any(Activity.class));
	}

	@Test
	void shouldRejectReactivationWhenApplicableUserCapacityIsFull() throws Exception {
		TestContext context = context(false);
		context.target.setSupportStatus(SupportStatus.REJECTED);
		context.target.setSupportResponseInstant(new Date(1_000L));
		SupportNomination proposedSupport = acceptedUserSupport(context.nomination, 102L);
		proposedSupport.setSupportStatus(SupportStatus.PROPOSED);
		SupportNomination approvedSupport = acceptedUserSupport(context.nomination, 103L);
		approvedSupport.setSupportStatus(SupportStatus.APPROVED);
		context.supports.add(proposedSupport);
		context.supports.add(approvedSupport);
		context.tasks.put(ElectionTaskKey.USER_SUPPORTS_2,
				task(context.candidate, ElectionTaskKey.USER_SUPPORTS_2, CandidateElectionTaskStatus.COMPLETED));
		EntityManager em = mockEntityManager(context);

		assertFalse(beanWithEntityManager(em).approveRejectedCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "valid comment", "ADMIN", "127.0.0.1"));

		assertEquals(SupportStatus.REJECTED, context.target.getSupportStatus());
		assertNotNull(context.target.getSupportResponseInstant());
		verify(em, never()).persist(any(Activity.class));
	}

	@Test
	void shouldRejectApprovalForDebtorOrganizationWithoutMutation() throws Exception {
		TestContext context = context(true);
		context.target.setSupportStatus(SupportStatus.REJECTED);
		context.target.getSupportingOrganization().setDeudor(true);
		EntityManager em = mockEntityManager(context);

		assertFalse(beanWithEntityManager(em).approveRejectedCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "valid comment", "ADMIN", "127.0.0.1"));

		assertEquals(SupportStatus.REJECTED, context.target.getSupportStatus());
		verify(em, never()).persist(any(Activity.class));
	}

	@Test
	void shouldRejectApprovalWhenOrganizationAlreadyGrantedInElection() throws Exception {
		TestContext context = context(true);
		context.target.setSupportStatus(SupportStatus.REJECTED);
		EntityManager em = mockEntityManager(context);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> organizationGrantedQuery = mock(TypedQuery.class);
		when(em.createQuery(contains("s.supportingOrganization.id = :supportingOrganizationId"), eq(Long.class))).thenReturn(organizationGrantedQuery);
		when(organizationGrantedQuery.setParameter(any(String.class), any())).thenReturn(organizationGrantedQuery);
		when(organizationGrantedQuery.getSingleResult()).thenReturn(1L);

		assertFalse(beanWithEntityManager(em).approveRejectedCandidateSupport(
				CANDIDATE_ID, SUPPORT_ID, "valid comment", "ADMIN", "127.0.0.1"));

		assertEquals(SupportStatus.REJECTED, context.target.getSupportStatus());
		verify(em, never()).persist(any(Activity.class));
	}

	private EntityManager mockEntityManager(TestContext context) {
		EntityManager em = mock(EntityManager.class);
		when(em.find(Candidate.class, CANDIDATE_ID, LockModeType.PESSIMISTIC_WRITE)).thenReturn(context.candidate);
		when(em.find(SupportNomination.class, SUPPORT_ID, LockModeType.PESSIMISTIC_WRITE)).thenReturn(context.target);

		@SuppressWarnings("unchecked")
		TypedQuery<SupportNomination> supportByIdQuery = mock(TypedQuery.class);
		when(em.createQuery(contains("WHERE s.id = :supportNominationId"), eq(SupportNomination.class))).thenReturn(supportByIdQuery);
		when(supportByIdQuery.setParameter(any(String.class), any())).thenReturn(supportByIdQuery);
		when(supportByIdQuery.getResultStream()).thenAnswer(invocation -> Stream.of(context.target));

		@SuppressWarnings("unchecked")
		TypedQuery<SupportNomination> supportListQuery = mock(TypedQuery.class);
		when(em.createQuery(contains("s.nomination.candidate.candidateId = :candidateId"), eq(SupportNomination.class))).thenReturn(supportListQuery);
		when(supportListQuery.setParameter(any(String.class), any())).thenReturn(supportListQuery);
		when(supportListQuery.getResultList()).thenReturn(context.supports);

		@SuppressWarnings("unchecked")
		TypedQuery<Long> organizationGrantedQuery = mock(TypedQuery.class);
		when(em.createQuery(contains("s.supportingOrganization.id = :supportingOrganizationId"), eq(Long.class))).thenReturn(organizationGrantedQuery);
		when(organizationGrantedQuery.setParameter(any(String.class), any())).thenReturn(organizationGrantedQuery);
		when(organizationGrantedQuery.getSingleResult()).thenReturn(0L);

		@SuppressWarnings("unchecked")
		TypedQuery<CandidateElectionTaskProgress> taskQuery = mock(TypedQuery.class);
		AtomicReference<ElectionTaskKey> requestedTask = new AtomicReference<>();
		when(em.createQuery(contains("FROM CandidateElectionTaskProgress"), eq(CandidateElectionTaskProgress.class))).thenReturn(taskQuery);
		when(taskQuery.setParameter(any(String.class), any())).thenAnswer(invocation -> {
			if ("taskKey".equals(invocation.getArgument(0))) {
				requestedTask.set(invocation.getArgument(1));
			}
			return taskQuery;
		});
		when(taskQuery.setMaxResults(anyInt())).thenReturn(taskQuery);
		when(taskQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE)).thenReturn(taskQuery);
		when(taskQuery.getResultList()).thenAnswer(invocation -> {
			CandidateElectionTaskProgress progress = context.tasks.get(requestedTask.get());
			return progress == null ? List.of() : List.of(progress);
		});
		return em;
	}

	private TestContext context(boolean organizationTarget) {
		Election election = new Election();
		election.setElectionId(ELECTION_ID);
		Candidate candidate = new Candidate();
		candidate.setCandidateId(CANDIDATE_ID);
		candidate.setElection(election);
		candidate.setStatus(CandidateStatus.INCOMPLETE);
		Nomination nomination = new Nomination();
		nomination.setId(71L);
		nomination.setElection(election);
		nomination.setCandidate(candidate);
		nomination.setStatus(NominationStatus.ACCEPTED_BY_CANDIDATE);
		SupportNomination target = organizationTarget
				? acceptedOrganizationSupport(nomination, SUPPORT_ID)
				: acceptedUserSupport(nomination, SUPPORT_ID);
		TestContext context = new TestContext(candidate, nomination, target);
		context.supports.add(target);
		return context;
	}

	private void addAcceptedUserSupports(TestContext context, int count) {
		for (int index = 0; index < count; index++) {
			context.supports.add(acceptedUserSupport(context.nomination, 300L + index));
		}
	}

	private SupportNomination acceptedOrganizationSupport(Nomination nomination, long id) {
		Organization organization = new Organization();
		organization.setId(900L + id);
		organization.setElection(nomination.getElection());
		SupportNomination support = acceptedUserSupport(nomination, id);
		support.setSupportingOrganization(organization);
		return support;
	}

	private SupportNomination acceptedUserSupport(Nomination nomination, long id) {
		SupportNomination support = new SupportNomination();
		support.setId(id);
		support.setNomination(nomination);
		support.setSupportStatus(SupportStatus.ACCEPTED);
		return support;
	}

	private CandidateElectionTaskProgress completedTask(Candidate candidate, ElectionTaskKey taskKey) {
		return task(candidate, taskKey, CandidateElectionTaskStatus.COMPLETED);
	}

	private CandidateElectionTaskProgress task(Candidate candidate, ElectionTaskKey taskKey, CandidateElectionTaskStatus status) {
		ElectionTask task = new ElectionTask();
		task.setTaskKey(taskKey);
		return new CandidateElectionTaskProgress(candidate, task, status);
	}

	private ElectionsManagerEJBBean beanWithEntityManager(EntityManager em) throws Exception {
		ElectionsManagerEJBBean bean = new ElectionsManagerEJBBean();
		Field entityManagerField = ElectionsManagerEJBBean.class.getDeclaredField("em");
		entityManagerField.setAccessible(true);
		entityManagerField.set(bean, em);
		return bean;
	}

	private static final class TestContext {
		private final Candidate candidate;
		private final Nomination nomination;
		private final SupportNomination target;
		private final List<SupportNomination> supports = new ArrayList<>();
		private final Map<ElectionTaskKey, CandidateElectionTaskProgress> tasks = new EnumMap<>(ElectionTaskKey.class);

		TestContext(Candidate candidate, Nomination nomination, SupportNomination target) {
			this.candidate = candidate;
			this.nomination = nomination;
			this.target = target;
		}
	}
}
