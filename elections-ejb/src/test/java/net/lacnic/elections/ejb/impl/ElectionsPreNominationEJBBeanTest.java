package net.lacnic.elections.ejb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.Organization;

class ElectionsPreNominationEJBBeanTest {

	@Test
	void shouldNotAcceptNominationWhenElectionHasNoTasksConfigured() throws Exception {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Nomination> nominationQuery = mock(TypedQuery.class);
		@SuppressWarnings("unchecked")
		TypedQuery<ElectionCalendar> calendarQuery = mock(TypedQuery.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Long> countQuery = mock(TypedQuery.class);
		@SuppressWarnings("unchecked")
		TypedQuery<ElectionTask> taskQuery = mock(TypedQuery.class);

		Election election = mock(Election.class);
		when(election.getElectionId()).thenReturn(92L);
		when(election.isNominationTasksLinkAvailable()).thenReturn(true);

		Nomination nomination = mock(Nomination.class);
		when(nomination.getId()).thenReturn(10004L);
		when(nomination.getElection()).thenReturn(election);
		when(nomination.getStatus()).thenReturn(NominationStatus.PROPOSED);
		when(nomination.getNominationEmail()).thenReturn("gerardorada84@gmail.com");

		ElectionCalendar calendar = mock(ElectionCalendar.class);
		when(calendar.getStartDate()).thenReturn(new Date(System.currentTimeMillis() - 60_000));
		when(calendar.getEndDate()).thenReturn(new Date(System.currentTimeMillis() + 60_000));

		when(em.createQuery(contains("WHERE n.acceptNominationToken = :token"), eq(Nomination.class))).thenReturn(nominationQuery);
		when(nominationQuery.setParameter(eq("token"), any())).thenReturn(nominationQuery);
		when(nominationQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE)).thenReturn(nominationQuery);
		when(nominationQuery.getResultStream()).thenReturn(Stream.of(nomination));

		when(em.createQuery(contains("SELECT c FROM ElectionCalendar c WHERE c.election.electionId = :electionId AND c.calendarKey = :calendarKey"), eq(ElectionCalendar.class)))
				.thenReturn(calendarQuery);
		when(calendarQuery.setParameter(anyString(), any())).thenReturn(calendarQuery);
		when(calendarQuery.setMaxResults(anyInt())).thenReturn(calendarQuery);
		when(calendarQuery.getResultList()).thenReturn(List.of(calendar));

		when(em.createQuery(contains("SELECT COUNT(n) FROM Nomination n WHERE n.election.electionId = :electionId"), eq(Long.class))).thenReturn(countQuery);
		when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
		when(countQuery.getSingleResult()).thenReturn(0L);

		when(em.createQuery(contains("SELECT t FROM ElectionTask t WHERE t.election.electionId = :electionId"), eq(ElectionTask.class))).thenReturn(taskQuery);
		when(taskQuery.setParameter(eq("electionId"), anyLong())).thenReturn(taskQuery);
		when(taskQuery.getResultList()).thenReturn(List.of());

		ElectionsPreNominationEJBBean bean = new ElectionsPreNominationEJBBean();
		setEntityManager(bean, em);

		boolean accepted = bean.acceptNomination("token-x", "127.0.0.1");

		assertFalse(accepted);
		verify(taskQuery).getResultList();
		verify(em, never()).persist(any());
		verify(em, never()).merge(any());
		verify(calendarQuery).setParameter("calendarKey", ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
	}

	@Test
	void shouldResolveTextImprovementAccessWithOrganizationTokenWhenNoNominationMatches() throws Exception {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Nomination> nominationQuery = mock(TypedQuery.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Organization> organizationQuery = mock(TypedQuery.class);
		Organization organization = new Organization();
		organization.setId(123L);

		when(em.createQuery(contains("WHERE n.acceptNominationToken = :token"), eq(Nomination.class))).thenReturn(nominationQuery);
		when(nominationQuery.setParameter(eq("token"), any())).thenReturn(nominationQuery);
		when(nominationQuery.getResultStream()).thenReturn(Stream.empty());
		when(em.createQuery(contains("WHERE o.doNominationToken = :token"), eq(Organization.class))).thenReturn(organizationQuery);
		when(organizationQuery.setParameter(eq("token"), any())).thenReturn(organizationQuery);
		when(organizationQuery.getResultList()).thenReturn(List.of(organization));

		ElectionsPreNominationEJBBean bean = new ElectionsPreNominationEJBBean();
		setEntityManager(bean, em);

		Object accessContext = resolveTextImprovementAccessContext(bean, "organization-token", "203.0.113.10");

		assertEquals("ORG:123", invokeAccessContextMethod(accessContext, "getRateLimitScopeKey"));
		assertEquals(Boolean.TRUE, invokeAccessContextMethod(accessContext, "isOpenAiAllowed"));
		assertNull(invokeAccessContextMethod(accessContext, "getCandidate"));
	}

	private void setEntityManager(ElectionsPreNominationEJBBean bean, EntityManager em) throws Exception {
		Field field = ElectionsPreNominationEJBBean.class.getDeclaredField("em");
		field.setAccessible(true);
		field.set(bean, em);
	}

	private Object resolveTextImprovementAccessContext(ElectionsPreNominationEJBBean bean, String token, String clientIp) throws Exception {
		Method method = ElectionsPreNominationEJBBean.class.getDeclaredMethod("resolveTextImprovementAccessContext", String.class, String.class);
		method.setAccessible(true);
		return method.invoke(bean, token, clientIp);
	}

	private Object invokeAccessContextMethod(Object accessContext, String methodName) throws Exception {
		assertNotNull(accessContext);
		Method method = accessContext.getClass().getDeclaredMethod(methodName);
		method.setAccessible(true);
		return method.invoke(accessContext);
	}
}
