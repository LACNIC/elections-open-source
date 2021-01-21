package net.lacnic.siselecciones.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.siselecciones.dominio.Candidato;
import net.lacnic.siselecciones.utils.Constantes;

public class CandidatoDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private static final String ID_ELECCION = "idEleccion";

	private EntityManager em;

	public CandidatoDao(EntityManager em) {
		this.em = em;
	}

	public Candidato obtenerCandidato(long id) {
		TypedQuery<Candidato> q = em.createQuery("SELECT c FROM Candidato c WHERE c.idCandidato =:idCandidato", Candidato.class);
		q.setParameter("idCandidato", id);
		return q.getSingleResult();
	}

	public Candidato obtenerPrimerCandidato(long idEleccion) {
		TypedQuery<Candidato> q = em.createQuery("SELECT c FROM Candidato c WHERE c.eleccion.idEleccion =:idEleccion ORDER BY c.orden DESC", Candidato.class);
		q.setParameter(ID_ELECCION, idEleccion);
		q.setMaxResults(1);
		return q.getSingleResult();
	}

	public Candidato obtenerUltimoCandidato(long idEleccion) {
		TypedQuery<Candidato> q = em.createQuery("SELECT c FROM Candidato c WHERE c.eleccion.idEleccion =:idEleccion ORDER BY c.orden", Candidato.class);
		q.setParameter(ID_ELECCION, idEleccion);
		q.setMaxResults(1);
		return q.getSingleResult();
	}

	public List<Candidato> obtenerCandidatosEleccion(long idEleccion) {
		TypedQuery<Candidato> q = em.createQuery("SELECT c FROM Candidato c WHERE c.eleccion.idEleccion =:idEleccion ORDER BY c.orden DESC", Candidato.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public long obtenerCantVotosCandidato(long idCandidato) {
		Query q = em.createQuery("SELECT COUNT(v.idVoto) FROM Voto v WHERE v.candidato.idCandidato =:idCandidato");
		q.setParameter("idCandidato", idCandidato);
		return (long) q.getSingleResult();
	}

	public int obtenerOrdenDelUltimoCandidatoNofijado(long idEleccion) {
		try {
			Query q = em.createQuery("SELECT c.orden FROM Candidato c WHERE c.eleccion.idEleccion =:idEleccion AND c.orden!=:ordenmax AND c.orden!=:ordenmin ORDER BY c.orden DESC");
			q.setParameter(ID_ELECCION, idEleccion);
			q.setParameter("ordenmax", Constantes.ORDEN_MAXIMO);
			q.setParameter("ordenmin", Constantes.ORDEN_MINIMO);
			q.setMaxResults(1);
			return (int) q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return 1;
		}
	}

	public Candidato obtenerCandidatoDeArriba(long idEleccion, int orden) {
		try {
			TypedQuery<Candidato> q = em.createQuery("SELECT c FROM Candidato c WHERE c.eleccion.idEleccion =:idEleccion AND c.orden > :orden ORDER BY c.orden", Candidato.class);
			q.setParameter(ID_ELECCION, idEleccion);
			q.setParameter("orden", orden);
			q.setMaxResults(1);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public Candidato obtenerCandidatoDeAbajo(long idEleccion, int orden) {
		try {
			TypedQuery<Candidato> q = em.createQuery("SELECT c FROM Candidato c WHERE c.eleccion.idEleccion =:idEleccion AND c.orden < :orden ORDER BY c.orden DESC", Candidato.class);
			q.setParameter(ID_ELECCION, idEleccion);
			q.setParameter("orden", orden);
			q.setMaxResults(1);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

}
