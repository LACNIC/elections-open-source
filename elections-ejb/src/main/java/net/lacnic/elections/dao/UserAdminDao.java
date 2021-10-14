package net.lacnic.elections.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.UserAdmin;

public class UserAdminDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;


	public UserAdminDao(EntityManager em) {
		this.em = em;
	}


	public UserAdmin verifyUserLogin(String userAdminId, String password) {
		try {
			TypedQuery<UserAdmin> q = em.createQuery("SELECT a FROM UserAdmin a WHERE a.userAdminId = :userAdminId and a.password = :password", UserAdmin.class);
			q.setParameter("userAdminId", userAdminId.toLowerCase());
			q.setParameter("password", password.toUpperCase());
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<UserAdmin> getUserAdminsAll() {
		try {
			TypedQuery<UserAdmin> q = em.createQuery("SELECT a FROM UserAdmin a", UserAdmin.class);
			return q.getResultList();
		} catch (Exception e) {
			appLogger.error(e);
			return Collections.emptyList();
		}
	}

	public UserAdmin getUserAdmin(String userAdminId) {
		try {
			TypedQuery<UserAdmin> q = em.createQuery("SELECT a FROM UserAdmin a WHERE UPPER(a.userAdminId) = :userAdminId", UserAdmin.class);
			q.setParameter("userAdminId", userAdminId.toUpperCase());
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<UserAdmin> getElectionUserAdmins(long electionId) {
		try {
			TypedQuery<UserAdmin> q = em.createQuery("SELECT a FROM UserAdmin a WHERE a.authorizedElectionId = :electionId", UserAdmin.class);
			q.setParameter("electionId", electionId);
			return q.getResultList();
		} catch (Exception e) {
			appLogger.error(e);
			return Collections.emptyList();
		}
	}

	public Long getUserAuthorizedElectionId(String userAdminId) {
		try {
			Query q = em.createQuery("SELECT a.authorizedElectionId FROM UserAdmin a WHERE UPPER(a.userAdminId) = :userAdminId");
			q.setParameter("userAdminId", userAdminId.toUpperCase());
			return (long) q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return 0L;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getUserAdminsAllIdAndName(int pageSize, int offset) {
		Query q = em.createQuery("SELECT a.userAdminId, a.email FROM UserAdmin a ORDER BY a.userAdminId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();		
	}

}