package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.lacnic.elections.domain.Vote;

public class VotoDao {
	
	private static final String ID_ELECCION = "idEleccion";
	
	private EntityManager em;

	public VotoDao(EntityManager em) {
		this.em = em;
	}

	public List<Vote> obtenerVotosEleccion(long idEleccion) {
		TypedQuery<Vote> q = em.createQuery("SELECT v FROM Voto v WHERE v.eleccion.idEleccion =:idEleccion ORDER BY v.idVoto desc", Vote.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public List<Vote> obtenerVotos(long idUsuarioPadron, long idEleccion) {
		TypedQuery<Vote> q = em.createQuery("SELECT v FROM Voto v WHERE v.usuarioPadron.idUsuarioPadron =:idUsuarioPadron AND v.eleccion.idEleccion =:idEleccion", Vote.class);
		q.setParameter("idUsuarioPadron", idUsuarioPadron);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public List<Vote> obtenerVotosCandidatos(long idCandidato) {
		TypedQuery<Vote> q = em.createQuery("SELECT v FROM Voto v WHERE v.candidato.idCandidato =:idCandidato ORDER BY v.idVoto desc", Vote.class);
		q.setParameter("idCandidato", idCandidato);
		return q.getResultList();
	}

	public List<Object[]> obtenerCodigosdeVotacion(long idEleccion) {
		Query q = em.createQuery("SELECT v.candidato.nombre, v.codigo FROM Voto v WHERE v.eleccion.idEleccion =:idEleccion ORDER BY v.codigo");
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public long obtenerTotalVotosEleccion(long idEleccion) {
		return obtenerVotosEleccion(idEleccion).size();
	}

	public List<Object[]> obtenerCandidatosVotacion(long idUsuarioPadron, long idEleccion) {
		Query q = em.createQuery("SELECT v.candidato.nombre, v.codigo, v.candidato.contenidoFoto FROM Voto v WHERE v.usuarioPadron.idUsuarioPadron =:idUsuarioPadron AND v.eleccion.idEleccion =:idEleccion");
		q.setParameter("idUsuarioPadron", idUsuarioPadron);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public void borrarVotos(long idEleccion) {
		Query q = em.createQuery("DELETE FROM Voto v WHERE v.eleccion.idEleccion=:idEleccion");
		q.setParameter(ID_ELECCION, idEleccion);
		q.executeUpdate();
	}
}