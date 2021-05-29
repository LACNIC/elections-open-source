package net.lacnic.elections.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.UserAdmin;

public class UsuarioAdminDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;

	public UsuarioAdminDao(EntityManager em) {
		this.em = em;
	}

	/**
	 * Devuelve la eleccion de id=idEleccion
	 * 
	 * @param idEleccion
	 *            indica el identificador de la elección
	 * @return Retorna la instancia de eleccion con id=idEleccion
	 */
	public UserAdmin comprobarUsuarioAdmin(String userId, String password) {
		try {
			TypedQuery<UserAdmin> q = em.createQuery("SELECT a FROM UsuarioAdmin a WHERE a.userId =:userId and a.password =:password", UserAdmin.class);
			q.setParameter("userId", userId.toLowerCase());
			q.setParameter("password", password.toUpperCase());
			
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<UserAdmin> obtenerUsuariosAdmin() {
		try {
			TypedQuery<UserAdmin> q = em.createQuery("SELECT a FROM UsuarioAdmin a", UserAdmin.class);
			return q.getResultList();
		} catch (Exception e) {
			appLogger.error(e);
			return Collections.emptyList();
		}
	}

	public UserAdmin obtenerUsuarioAdmin(String userAdminId) {
		try {
			TypedQuery<UserAdmin> q = em.createQuery("SELECT a FROM UsuarioAdmin a WHERE UPPER(a.userId) =:userId", UserAdmin.class);
			q.setParameter("userId", userAdminId.toUpperCase());
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<UserAdmin> obtenerUsuariosAdmin(long idEleccion) {
		try {
			TypedQuery<UserAdmin> q = em.createQuery("SELECT a FROM UsuarioAdmin a Where a.idEleccionAutorizado =:idEleccion", UserAdmin.class);
			q.setParameter("idEleccion", idEleccion);
			return q.getResultList();
		} catch (Exception e) {
			appLogger.error(e);
			return Collections.emptyList();
		}
	}

	public Long obtenerIdEleccionUsuAdmin(String adminId) {
		try {
			Query q = em.createQuery("SELECT a.idEleccionAutorizado FROM UsuarioAdmin a Where  UPPER(a.userId) =:adminId");
			q.setParameter("adminId", adminId.toUpperCase());
			return (long) q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return 0L;
		}
	}

}