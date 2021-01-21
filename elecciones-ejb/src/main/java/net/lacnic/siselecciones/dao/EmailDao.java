package net.lacnic.siselecciones.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.joda.time.DateTimeUtils;

import net.lacnic.siselecciones.dominio.Email;

public class EmailDao {

	private static final String ID_ELECCION = "idEleccion";

	private EntityManager em;

	public EmailDao(EntityManager em) {
		this.em = em;
	}

	public List<Email> obtenerEmailsAll() {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e", Email.class);
		return q.getResultList();
	}

	public List<Email> obtenerEmailsParaEnviarElecciones() {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.enviado = :estado", Email.class);
		q.setParameter("estado", false);
		return q.getResultList();
	}

	public List<Email> obtenerEmailsViejo() {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.fechaCreado <= :ahoraMenos30Dias", Email.class);
		q.setParameter("ahoraMenos30Dias", new Timestamp(DateTimeUtils.currentTimeMillis() - 86400000L));
		return q.getResultList();
	}

	public void marcarEmailsComoEnviados() {
		Query q = em.createQuery("UPDATE Email SET enviado = TRUE WHERE enviado=FALSE");
		q.executeUpdate();
	}

	public Email obtenerEmail(long idEmail) {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.id = :id", Email.class);
		q.setParameter("id", idEmail);
		return q.getSingleResult();
	}

	public List<Email> obtenerEnviosVotacion(long idEleccion) {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.eleccion.idEleccion = :idEleccion", Email.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public List<Email> obtenerMailsDeEleccion(Long idEleccion) {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.eleccion.idEleccion = :idEleccion", Email.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public List<Email> obtenerMailsPorEnviarDeEleccion(Long idEleccion) {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.eleccion.idEleccion = :idEleccion AND e.enviado=FALSE", Email.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

}
