package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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

	@SuppressWarnings("unchecked")
	public List<Object[]> getIpAccessesAllIdAndDescription(int pageSize, int offset) {
		Query q = em.createQuery("SELECT i.ipAccessId, i.ip FROM IpAccess i ORDER BY i.ipAccessId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

	public IpAccess getIpAccess(Long ipAccessId) {
		TypedQuery<IpAccess> q = em.createQuery("SELECT i FROM IpAccess i WHERE i.ipAccessId = :ipAccessId", IpAccess.class);
		q.setParameter("ipAccessId", ipAccessId);
		return q.getSingleResult();
	}

}
