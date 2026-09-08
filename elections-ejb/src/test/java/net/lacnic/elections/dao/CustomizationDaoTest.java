package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Customization;
import org.junit.jupiter.api.Test;

class CustomizationDaoTest {

	@Test
	void shouldGetCustomization() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Customization> query = mock(TypedQuery.class);
		Customization customization = mock(Customization.class);
		when(em.createQuery(anyString(), eq(Customization.class))).thenReturn(query);
		when(query.getSingleResult()).thenReturn(customization);

		CustomizationDao dao = new CustomizationDao(em);
		Customization result = dao.getCustomization();

		assertSame(customization, result);
	}

	@Test
	void shouldReturnNullWhenGetCustomizationFails() {
		EntityManager em = mock(EntityManager.class);
		when(em.createQuery(anyString(), eq(Customization.class))).thenThrow(new RuntimeException("boom"));

		CustomizationDao dao = new CustomizationDao(em);
		assertNull(dao.getCustomization());
	}

	@Test
	void shouldGetCustomizationById() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Customization> query = mock(TypedQuery.class);
		Customization customization = mock(Customization.class);
		when(em.createQuery(anyString(), eq(Customization.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(customization);

		CustomizationDao dao = new CustomizationDao(em);
		Customization result = dao.getCustomizationById(12L);

		assertSame(customization, result);
		verify(query).setParameter("customizationId", 12L);
	}

	@Test
	void shouldGetCustomizationsIdAndDescription() {
		EntityManager em = mock(EntityManager.class);
		Query query = mock(Query.class);
		when(em.createQuery(anyString())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.<Object[]>of(new Object[] { 1L, "Site" }));

		CustomizationDao dao = new CustomizationDao(em);
		List<Object[]> result = dao.getCustomizationsAllIdAndDescription();

		assertEquals(1, result.size());
		assertEquals(1L, result.get(0)[0]);
		assertEquals("Site", result.get(0)[1]);
	}
}
