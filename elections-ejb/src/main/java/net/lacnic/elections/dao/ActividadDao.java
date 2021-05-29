package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import net.lacnic.elections.domain.Activity;

public class ActividadDao {

	private EntityManager em;

	public ActividadDao(EntityManager em) {
		this.em = em;
	}

	public List<Activity> obtenerTodasLasActividades() {
		TypedQuery<Activity> q = em.createQuery("SELECT a FROM activity a ORDER BY a.id_activity DESC", Activity.class);
		return q.getResultList();
	}

	public List<Activity> obtenerTodasLasActividades(long idEleccion) {
		TypedQuery<Activity> q = em.createQuery("SELECT a FROM activity a WHERE idEleccion =:idEleccion ORDER BY a.id_activity DESC", Activity.class);
		q.setParameter("idEleccion", idEleccion);
		return q.getResultList();
	}

	public List<Activity> obtenerActividadesDeAdmin(String nomAdmin) {
		TypedQuery<Activity> q = em.createQuery("SELECT a FROM activity a WHERE a.username =:nomAdmin ORDER BY a.tiempo", Activity.class);
		q.setParameter("nomAdmin", nomAdmin);
		return q.getResultList();
	}

}
