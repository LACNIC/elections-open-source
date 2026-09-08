package net.lacnic.elections.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.IpAccess;

public class IpAccessDao {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

	private EntityManager em;

	public IpAccessDao(EntityManager em) {
		this.em = em;
	}

	public IpAccess getIP(String ip) {
		try {
			TypedQuery<IpAccess> q = em.createQuery("SELECT i FROM IpAccess i WHERE i.ip =:ip", IpAccess.class);
			q.setParameter("ip", ip);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
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
