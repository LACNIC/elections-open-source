package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.Parameter;


public class ParameterDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;


	public ParameterDao(EntityManager em) {
		this.em = em;
	}

	public Parameter getParameter(String key) {
		try {
			TypedQuery<Parameter> q = em.createQuery("SELECT p FROM Parameter p WHERE p.key = :key", Parameter.class);
			q.setParameter("key", key);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<Parameter> getParametersAll() {
		TypedQuery<Parameter> q = em.createQuery("SELECT p FROM Parameter p", Parameter.class);
		return q.getResultList();
	}

}
