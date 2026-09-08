package net.lacnic.elections.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Parameter;

public class ParameterDao {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

	private EntityManager em;

	public ParameterDao(EntityManager em) {
		this.em = em;
	}

	public Parameter getParameter(String key) {
		try {
			TypedQuery<Parameter> q = em.createQuery("SELECT p FROM Parameter p WHERE p.key = :key", Parameter.class);
			q.setParameter("key", key);
			q.setMaxResults(1);
			List<Parameter> results = q.getResultList();
			return results.isEmpty() ? null : results.get(0);
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	public List<Parameter> getParametersAll() {
		TypedQuery<Parameter> q = em.createQuery("SELECT p FROM Parameter p ORDER BY p.key ASC", Parameter.class);
		return q.getResultList();
	}

}
