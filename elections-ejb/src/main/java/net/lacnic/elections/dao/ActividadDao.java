package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import net.lacnic.elections.domain.Actividad;

public class ActividadDao {

	private EntityManager em;

	public ActividadDao(EntityManager em) {
		this.em = em;
	}

	public List<Actividad> obtenerTodasLasActividades() {
		TypedQuery<Actividad> q = em.createQuery("SELECT a FROM Actividad a ORDER BY a.idActividad DESC", Actividad.class);
		return q.getResultList();
	}

	public List<Actividad> obtenerTodasLasActividades(long idEleccion) {
		TypedQuery<Actividad> q = em.createQuery("SELECT a FROM Actividad a WHERE idEleccion =:idEleccion ORDER BY a.idActividad DESC", Actividad.class);
		q.setParameter("idEleccion", idEleccion);
		return q.getResultList();
	}

	public List<Actividad> obtenerActividadesDeAdmin(String nomAdmin) {
		TypedQuery<Actividad> q = em.createQuery("SELECT a FROM Actividad a WHERE a.nomUser =:nomAdmin ORDER BY a.tiempo", Actividad.class);
		q.setParameter("nomAdmin", nomAdmin);
		return q.getResultList();
	}

}
