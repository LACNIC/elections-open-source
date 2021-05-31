package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.IpAccess;

public class IpAccessDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;

	public IpAccessDao(EntityManager em) {
		this.em = em;
	}

	public IpAccess getIP(String ip) {
		try {
			TypedQuery<IpAccess> q = em.createQuery("SELECT i FROM IpAccess i WHERE i.ip =:ip", IpAccess.class);
			q.setParameter("ip", ip);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<IpAccess> getAllDisabledIPs() {
		TypedQuery<IpAccess> q = em.createQuery("SELECT i FROM IpAccess i", IpAccess.class);
		return q.getResultList();
	}

}
