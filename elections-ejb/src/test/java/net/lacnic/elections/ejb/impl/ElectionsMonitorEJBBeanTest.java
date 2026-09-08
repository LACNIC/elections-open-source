package net.lacnic.elections.ejb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.utils.ElectionsCaches;

class ElectionsMonitorEJBBeanTest {

	@AfterEach
	void clearHealthCheckCache() throws Exception {
		ElectionsCaches.clearHealthCheckCache();
	}

	@Test
	void updateHealthCheckDataReturnsErrorPayloadWhenMetricsQueryFails() throws Exception {
		ElectionsCaches.clearHealthCheckCache();
		EntityManager em = mock(EntityManager.class);
		Query healthCheckQuery = mock(Query.class);
		when(em.createNativeQuery(anyString())).thenReturn(healthCheckQuery);
		when(healthCheckQuery.getSingleResult()).thenReturn(1);
		when(em.createQuery(anyString())).thenThrow(new IllegalStateException("db unavailable"));
		ElectionsMonitorEJBBean bean = new ElectionsMonitorEJBBean();
		setEntityManager(bean, em);

		HealthCheck healthCheck = bean.updateHealthCheckData();

		assertEquals(0, healthCheck.getStatus());
		assertEquals("db unavailable", healthCheck.getErrorMessage());
		assertEquals(0, healthCheck.getSendAttempts());
		assertNull(healthCheck.getElections());
		assertNull(ElectionsCaches.getHealthCheck());
	}

	@Test
	void getHealthCheckDataDoesNotCacheErrorPayloadWhenInitialRefreshFails() throws Exception {
		ElectionsCaches.clearHealthCheckCache();
		EntityManager em = mock(EntityManager.class);
		when(em.createNativeQuery(anyString())).thenThrow(new IllegalStateException("db unavailable"));
		when(em.createQuery(anyString())).thenThrow(new IllegalStateException("db unavailable"));
		ElectionsMonitorEJBBean bean = new ElectionsMonitorEJBBean();
		setEntityManager(bean, em);

		HealthCheck healthCheck = bean.getHealthCheckData();

		assertEquals(0, healthCheck.getStatus());
		assertEquals("database unavailable", healthCheck.getErrorMessage());
		assertNull(ElectionsCaches.getHealthCheck());
	}

	@Test
	void debtorOrganizationDoesNotExposeNominationLink() {
		Organization organization = new Organization();
		organization.setDeudor(true);
		organization.setDoNominationToken("nomination-token");

		assertFalse(ElectionsMonitorEJBBean.canExposeNominationLink(organization, true));
	}

	@Test
	void nonDebtorOrganizationCanExposeNominationLinkWhenWindowIsOpen() {
		Organization organization = new Organization();
		organization.setDeudor(false);
		organization.setDoNominationToken("nomination-token");

		assertTrue(ElectionsMonitorEJBBean.canExposeNominationLink(organization, true));
		assertFalse(ElectionsMonitorEJBBean.canExposeNominationLink(organization, false));
	}

	private void setEntityManager(ElectionsMonitorEJBBean bean, EntityManager em) throws Exception {
		Field field = ElectionsMonitorEJBBean.class.getDeclaredField("em");
		field.setAccessible(true);
		field.set(bean, em);
	}
}
