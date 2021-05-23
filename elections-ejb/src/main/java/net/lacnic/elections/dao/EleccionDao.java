package net.lacnic.elections.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;

import net.lacnic.elections.data.Participacion;
import net.lacnic.elections.domain.Eleccion;
import net.lacnic.elections.domain.EleccionLight;
import net.lacnic.elections.domain.SupraEleccion;

public class EleccionDao {

	private EntityManager em;

	public EleccionDao(EntityManager em) {
		this.em = em;
	}

	public Eleccion obtenerEleccion(long idEleccion) {
		Query q = em.createQuery("SELECT e FROM Eleccion e WHERE e.idEleccion =:idEleccion");
		q.setParameter("idEleccion", idEleccion);
		return (Eleccion) q.getSingleResult();
	}

	public Eleccion obtenerEleccionConTokenResultado(String token) {
		TypedQuery<Eleccion> q = em.createQuery("SELECT e FROM Eleccion e WHERE e.tokenResultado =:tokenResultado", Eleccion.class);
		q.setParameter("tokenResultado", token);
		return q.getSingleResult();
	}

	public Eleccion obtenerEleccionConTokenAuditor(String token) {		
		return obtenerEleccionConTokenResultado(token);
	}

	public List<Eleccion> obtenerElecciones() {
		TypedQuery<Eleccion> q = em.createQuery("SELECT e FROM Eleccion e ORDER BY e.fechaCreacion", Eleccion.class);
		return q.getResultList();
	}

	public List<Eleccion> obtenerEleccionesDesc() {
		TypedQuery<Eleccion> q = em.createQuery("SELECT e FROM Eleccion e ORDER BY e.fechaInicio DESC", Eleccion.class);
		return q.getResultList();
	}

	public List<EleccionLight> obtenerEleccionesLightDesc() {
		TypedQuery<EleccionLight> q = em.createQuery("SELECT e FROM EleccionLight e ORDER BY e.fechaInicio DESC", EleccionLight.class);
		return q.getResultList();
	}

	public List<Eleccion> obtenerEleccionesLightEsteAnio() {
		TypedQuery<Eleccion> q = em.createQuery("SELECT e FROM Eleccion e WHERE e.fechaCreacion > :ini ORDER BY e.fechaCreacion desc", Eleccion.class);
		Date ini = new DateTime().minusYears(1).toDate();
		q.setParameter("ini", ini);
		return q.getResultList();
	}

	public boolean existeAlgunaEleccion() {
		Query q = em.createQuery("SELECT e.idEleccion FROM Eleccion e");
		q.setMaxResults(1);
		return !q.getResultList().isEmpty();
	}

	public List<Participacion> obtenerParticipacionesOrgId(String org) {
		Query q = em.createQuery("SELECT u.nombre, u.mail, u.pais, u.yaVoto, e.fechaInicio, "
				+ "e.fechaFin, e.tituloEspanol, e.tituloIngles, e.tituloPortugues, e.categoria " 
				+ "FROM UsuarioPadron u, Eleccion e " 
				+ "WHERE u.orgID= :org AND u.eleccion.idEleccion=e.idEleccion");
		q.setParameter("org", org);
		List<Object[]> result = q.getResultList();

		List<Participacion> listaResultado = new ArrayList<>();
		for (int i = 0; i < result.size(); i++) {
			Participacion p = new Participacion();
			p.setNombre((String) result.get(i)[0]);
			p.setEmail((String) result.get(i)[1]);
			p.setPais((String) result.get(i)[2]);
			p.setYaVoto((Boolean) result.get(i)[3]);
			p.setFechaInicioEleccion((Date) result.get(i)[4]);
			p.setFechaFinEleccion((Date) result.get(i)[5]);
			p.setTituloEleccionSP((String) result.get(i)[6]);
			p.setTituloEleccionEN((String) result.get(i)[7]);
			p.setTituloEleccionPT((String) result.get(i)[8]);
			p.setCategoria((String) result.get(i)[9]);
			listaResultado.add(p);
		}
		return listaResultado;
	}

	public List<Participacion> obtenerElecciones(String org) {
		return obtenerParticipacionesOrgId(org);
	}

	public boolean isEleccionSimple(long idEleccion) {
		Query q = em.createQuery("SELECT e FROM SupraEleccion e WHERE e.idEleccionA =:eleccion OR e.idEleccionB =:eleccion");
		q.setParameter("eleccion", idEleccion);

		return (q.getResultList() == null || q.getResultList().isEmpty());
	}

	public SupraEleccion obtenerSupraEleccion(long idEleccion) {
		TypedQuery<SupraEleccion> q = em.createQuery("SELECT e FROM SupraEleccion e WHERE e.idEleccionA =:eleccion OR e.idEleccionB =:eleccion", SupraEleccion.class);
		q.setParameter("eleccion", idEleccion);
		return q.getSingleResult();
	}

	public List<SupraEleccion> obtenerSupraElecciones() {
		TypedQuery<SupraEleccion> q = em.createQuery("SELECT e FROM SupraEleccion e", SupraEleccion.class);
		return q.getResultList();
	}

	public List<String> obtenerEleccionesIdDesc() {
		Query q = em.createQuery("SELECT e.idEleccion, e.tituloEspanol FROM Eleccion e ORDER BY e.idEleccion");
		List<Object[]> result = q.getResultList();

		List<String> listaResultado = new ArrayList<>();

		for (int i = 0; i < result.size(); i++) {
			listaResultado.add(result.get(i)[0].toString() + "-" + result.get(i)[1].toString());
		}
		return listaResultado;
	}

}