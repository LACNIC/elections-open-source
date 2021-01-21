package net.lacnic.siselecciones.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.siselecciones.dominio.Parametro;

public class ParametroDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;

	public ParametroDao(EntityManager em) {
		this.em = em;
	}

	public Parametro getParametro(String clave) {
		try {
			TypedQuery<Parametro> q = em.createQuery("SELECT p FROM Parametro p WHERE p.clave = :clave", Parametro.class);
			q.setParameter("clave", clave);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<Parametro> obtenerParametros() {
		TypedQuery<Parametro> q = em.createQuery("SELECT p FROM Parametro p", Parametro.class);
		return q.getResultList();
	}

}
