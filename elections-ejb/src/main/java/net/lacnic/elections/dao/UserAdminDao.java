package net.lacnic.elections.dao;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.UserAdmin;

public class UserAdminDao {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

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
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	public List<UserAdmin> getUserAdminsAll() {
		try {
			TypedQuery<UserAdmin> q = em.createQuery("SELECT a FROM UserAdmin a", UserAdmin.class);
			return q.getResultList();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	public UserAdmin getUserAdmin(String userAdminId) {
		try {
			TypedQuery<UserAdmin> q = em.createQuery("SELECT a FROM UserAdmin a WHERE UPPER(a.userAdminId) = :userAdminId", UserAdmin.class);
			q.setParameter("userAdminId", userAdminId.toUpperCase());
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getUserAdminsAllIdAndName(int pageSize, int offset) {
		Query q = em.createQuery("SELECT a.userAdminId, a.email FROM UserAdmin a ORDER BY a.userAdminId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

	public UserAdmin getUserAdminByEmail(String email) {
		try {
			TypedQuery<UserAdmin> q = em.createQuery("SELECT a FROM UserAdmin a WHERE a.email = :userAdminEmail", UserAdmin.class);
			q.setParameter("userAdminEmail", email);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

}
