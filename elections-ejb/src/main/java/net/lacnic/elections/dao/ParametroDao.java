package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.Parameter;

public class ParametroDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;

	public ParametroDao(EntityManager em) {
		this.em = em;
	}

	public Parameter getParametro(String clave) {
		try {
			TypedQuery<Parameter> q = em.createQuery("SELECT p FROM Parametro p WHERE p.clave = :clave", Parameter.class);
			q.setParameter("clave", clave);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<Parameter> obtenerParametros() {
		TypedQuery<Parameter> q = em.createQuery("SELECT p FROM Parametro p", Parameter.class);
		return q.getResultList();
	}

}
