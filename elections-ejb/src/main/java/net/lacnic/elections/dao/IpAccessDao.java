package net.lacnic.elections.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.data.TablesReportData;
import net.lacnic.elections.domain.ActivityReportTable;
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
	
	public List<TablesReportData> getIpAccessData() {
		Query q = em.createQuery("SELECT i.ipAccessId, i.ip FROM IpAccess i ORDER BY i.ipAccessId ");
		@SuppressWarnings("unchecked")
		List<Object[]> result = q.getResultList();

		List<TablesReportData> resultList = new ArrayList<>();
		for (int i = 0; i < result.size(); i++) {
			TablesReportData trd = new TablesReportData((long)result.get(i)[0], result.get(i)[1].toString());
			resultList.add(trd);
		}
		return resultList;	
	}
	
	public IpAccess getIpAccess(Long id) {
		TypedQuery<IpAccess> q = em.createQuery("SELECT i FROM IpAccess i WHERE i.ipAccessId = :ipAccessId", IpAccess.class);
		q.setParameter("ipAccessId", id);
		return q.getSingleResult();
	
	}

}
