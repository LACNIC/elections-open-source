package net.lacnic.siselecciones.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.siselecciones.dominio.Comisionado;

public class ComisionadoDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;

	public ComisionadoDao(EntityManager em) {
		this.em = em;
	}

	public Comisionado obtenerComisionado(long id) {
		TypedQuery<Comisionado> q = em.createQuery("SELECT c FROM Comisionado c WHERE c.idComisionado =:idComisionado", Comisionado.class);
		q.setParameter("idComisionado", id);		
		return q.getSingleResult();
	}

	public List<Comisionado> obtenerComisionados() {
		TypedQuery<Comisionado> q = em.createQuery("SELECT c FROM Comisionado c", Comisionado.class);
		return q.getResultList();
	}

	public Comisionado obtenerComisionado(String mail) {
		try {
			TypedQuery<Comisionado> q = em.createQuery("SELECT c FROM Comisionado c WHERE UPPER(c.mail) =:mail", Comisionado.class);
			q.setParameter("mail", mail.toUpperCase().trim());
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public boolean existeComisionado(String nombre, String mail) {
		Query q = em.createQuery("SELECT c.idComisionado FROM Comisionado c WHERE UPPER(c.nombre)=:nombre AND UPPER(c.mail) =:mail");
		q.setParameter("mail", mail.toUpperCase().trim());
		q.setParameter("nombre", nombre.toUpperCase().trim());
		q.setMaxResults(1);
		return !q.getResultList().isEmpty();
	}

}
