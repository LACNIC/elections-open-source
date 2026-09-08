package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Parameter;
import org.junit.jupiter.api.Test;

class ParameterDaoTest {

	@Test
	void shouldGetParameterByKey() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Parameter> query = mock(TypedQuery.class);
		Parameter parameter = mock(Parameter.class);
		when(em.createQuery(anyString(), eq(Parameter.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(parameter));

		ParameterDao dao = new ParameterDao(em);
		Parameter result = dao.getParameter("site.name");

		assertSame(parameter, result);
		verify(query).setParameter("key", "site.name");
		verify(query).setMaxResults(1);
	}

	@Test
	void shouldReturnNullWhenParameterByKeyIsMissing() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Parameter> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Parameter.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyString())).thenReturn(query);
		when(query.setMaxResults(anyInt())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of());

		ParameterDao dao = new ParameterDao(em);
		Parameter result = dao.getParameter("missing");

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenParameterQueryFails() {
		EntityManager em = mock(EntityManager.class);
		when(em.createQuery(anyString(), eq(Parameter.class))).thenThrow(new RuntimeException("boom"));

		ParameterDao dao = new ParameterDao(em);
		assertNull(dao.getParameter("site.name"));
	}

	@Test
	void shouldGetAllParameters() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<Parameter> query = mock(TypedQuery.class);
		when(em.createQuery(anyString(), eq(Parameter.class))).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(mock(Parameter.class)));

		ParameterDao dao = new ParameterDao(em);
		List<Parameter> result = dao.getParametersAll();

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(em).createQuery("SELECT p FROM Parameter p ORDER BY p.key ASC", Parameter.class);
	}
}
