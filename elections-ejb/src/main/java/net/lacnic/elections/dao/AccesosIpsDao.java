package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.IpAccess;

public class AccesosIpsDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;

	public AccesosIpsDao(EntityManager em) {
		this.em = em;
	}

	public IpAccess obteneriP(String ip) {
		try {
			TypedQuery<IpAccess> q = em.createQuery("SELECT i FROM ipaccess i WHERE i.ip =:ip", IpAccess.class);
			q.setParameter("ip", ip);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<IpAccess> obtenerIpsInhabilitadasTodas() {
		TypedQuery<IpAccess> q = em.createQuery("SELECT i FROM ipaccess i", IpAccess.class);
		return q.getResultList();
	}

}
