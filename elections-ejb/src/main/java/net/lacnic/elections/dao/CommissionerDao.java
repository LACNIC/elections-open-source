package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.Commissioner;


public class CommissionerDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;

	public CommissionerDao(EntityManager em) {
		this.em = em;
	}

	public Commissioner getCommissioner(long commissionerId) {
		TypedQuery<Commissioner> q = em.createQuery("SELECT c FROM Commissioner c WHERE c.commissionerId =:commissionerId", Commissioner.class);
		q.setParameter("commissionerId", commissionerId);		
		return q.getSingleResult();
	}

	public List<Commissioner> getCommissionersAll() {
		TypedQuery<Commissioner> q = em.createQuery("SELECT c FROM Commissioner c", Commissioner.class);
		return q.getResultList();
	}

	public Commissioner getCommissionerByMail(String mail) {
		try {
			TypedQuery<Commissioner> q = em.createQuery("SELECT c FROM Commissioner c WHERE UPPER(c.mail) = :mail", Commissioner.class);
			q.setParameter("mail", mail.toUpperCase().trim());
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public boolean commissionerExists(String name, String mail) {
		Query q = em.createQuery("SELECT c.commissionerId FROM Commissioner c WHERE UPPER(c.name) = :name AND UPPER(c.mail) = :mail");
		q.setParameter("mail", mail.toUpperCase().trim());
		q.setParameter("name", name.toUpperCase().trim());
		q.setMaxResults(1);
		return !q.getResultList().isEmpty();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getCommissionersAllIdAndDescription(int pageSize, int offset) {
		Query q = em.createQuery("SELECT c.commissionerId, c.name FROM Commissioner c ORDER BY c.commissionerId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

}
