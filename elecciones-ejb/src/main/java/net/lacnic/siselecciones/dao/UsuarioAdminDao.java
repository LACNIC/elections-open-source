package net.lacnic.siselecciones.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.siselecciones.dominio.UsuarioAdmin;

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
	public UsuarioAdmin comprobarUsuarioAdmin(String userId, String password) {
		try {
			TypedQuery<UsuarioAdmin> q = em.createQuery("SELECT a FROM UsuarioAdmin a WHERE a.userId =:userId and a.password =:password", UsuarioAdmin.class);
			q.setParameter("userId", userId.toLowerCase());
			q.setParameter("password", password.toUpperCase());
			
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<UsuarioAdmin> obtenerUsuariosAdmin() {
		try {
			TypedQuery<UsuarioAdmin> q = em.createQuery("SELECT a FROM UsuarioAdmin a", UsuarioAdmin.class);
			return q.getResultList();
		} catch (Exception e) {
			appLogger.error(e);
			return Collections.emptyList();
		}
	}

	public UsuarioAdmin obtenerUsuarioAdmin(String userAdminId) {
		try {
			TypedQuery<UsuarioAdmin> q = em.createQuery("SELECT a FROM UsuarioAdmin a WHERE UPPER(a.userId) =:userId", UsuarioAdmin.class);
			q.setParameter("userId", userAdminId.toUpperCase());
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public List<UsuarioAdmin> obtenerUsuariosAdmin(long idEleccion) {
		try {
			TypedQuery<UsuarioAdmin> q = em.createQuery("SELECT a FROM UsuarioAdmin a Where a.idEleccionAutorizado =:idEleccion", UsuarioAdmin.class);
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