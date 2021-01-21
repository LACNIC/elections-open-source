package net.lacnic.siselecciones.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.siselecciones.dominio.AccesosIps;

public class AccesosIpsDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;

	public AccesosIpsDao(EntityManager em) {
		this.em = em;
	}

	public AccesosIps obteneriP(String ip) {
		try {
			TypedQuery<AccesosIps> q = em.createQuery("SELECT i FROM AccesosIps i WHERE i.ip =:ip", AccesosIps.class);
			q.setParameter("ip", ip);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<AccesosIps> obtenerIpsInhabilitadasTodas() {
		TypedQuery<AccesosIps> q = em.createQuery("SELECT i FROM AccesosIps i", AccesosIps.class);
		return q.getResultList();
	}

}
