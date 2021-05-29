package net.lacnic.elections.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.lacnic.elections.domain.UserVoter;

public class UsuarioPadronDao {

	private static final String ID_ELECCION = "idEleccion";
	private static final String PAIS = "pais";
	private static final String MAIL = "mail";

	private EntityManager em;

	public UsuarioPadronDao(EntityManager em) {
		this.em = em;
	}


	public UserVoter obtenerUsuarioPadron(long idUsuarioPadron) {
		return em.find(UserVoter.class,idUsuarioPadron);
	}

	public List<UserVoter> obtenerUsuariosPadronEleccion(long idEleccion) {		
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UsuarioPadron u WHERE  u.eleccion.idEleccion =:idEleccion ORDER BY u.idUsuarioPadron", UserVoter.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public boolean existeUsuarioPadronEleccionEmail(long idEleccion, String mail) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UsuarioPadron u WHERE u.eleccion.idEleccion = :idEleccion AND u.mail = :mail", UserVoter.class);
		q.setParameter(ID_ELECCION, idEleccion);
		q.setParameter(MAIL, mail);
		List<UserVoter> users = q.getResultList();

		return (users != null && !users.isEmpty());
	}

	public List<UserVoter> obtenerUsuariosPadronPaisEleccion(long idEleccion, String pais) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UsuarioPadron u WHERE u.eleccion.idEleccion =:idEleccion AND u.pais = :pais ORDER BY u.idUsuarioPadron", UserVoter.class);
		q.setParameter(ID_ELECCION, idEleccion);
		q.setParameter(PAIS, pais.toUpperCase());
		return q.getResultList();
	}

	public List<UserVoter> obtenerUsuariosPadronEleccionQueAunNoVotaron(long idEleccion) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UsuarioPadron u WHERE  u.eleccion.idEleccion =:idEleccion AND u.yaVoto=FALSE ORDER BY u.idUsuarioPadron desc", UserVoter.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public List<UserVoter> obtenerUsuariosPadronPaisEleccionQueAunNoVotaron(long idEleccion, String pais) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UsuarioPadron u WHERE  u.eleccion.idEleccion =:idEleccion AND u.yaVoto=FALSE AND u.pais = :pais ORDER BY u.idUsuarioPadron desc", UserVoter.class);
		q.setParameter(ID_ELECCION, idEleccion);
		q.setParameter(PAIS, pais.toUpperCase());
		return q.getResultList();
	}

	public List<UserVoter> obtenerUsuariosPadronEleccionQueYaVotaron(long idEleccion) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UsuarioPadron u WHERE  u.eleccion.idEleccion =:idEleccion AND u.yaVoto=TRUE ORDER BY u.fechaVoto ASC", UserVoter.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public List<UserVoter> obtenerUsuariosPadronPaisEleccionQueYaVotaron(long idEleccion, String pais) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UsuarioPadron u WHERE  u.eleccion.idEleccion =:idEleccion AND u.yaVoto=TRUE AND u.pais = :pais ORDER BY u.fechaVoto ASC", UserVoter.class);
		q.setParameter(ID_ELECCION, idEleccion);
		q.setParameter(PAIS, pais.toUpperCase());
		return q.getResultList();
	}

	public List<UserVoter> obtenerUsuariosPadronEleccionQueNoVotaronEleccionDual(long idEleccion) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UsuarioPadron u, SupraEleccion p WHERE (p.idEleccionA =:elec OR p.idEleccionB =:elec) AND (u.eleccion.idEleccion = p.idEleccionA OR u.eleccion.idEleccion = p.idEleccionB) AND u.yaVoto=FALSE ORDER BY u.fechaVoto ASC", UserVoter.class);
		q.setParameter("elec", idEleccion);

		List<UserVoter> listaUnificada = new ArrayList<>();
		boolean existe = false;

		if (!q.getResultList().isEmpty())
			listaUnificada.add(q.getResultList().get(0));

		for (UserVoter padron : q.getResultList()) {
			existe = false;
			for (UserVoter usu : listaUnificada) {
				if (usu.getMail().equalsIgnoreCase(padron.getMail())) {
					existe = true;
					break;
				}
			}

			if (!existe)
				listaUnificada.add(padron);
		}

		return listaUnificada;
	}

	public List<UserVoter> obtenerUsuariosPadronEleccionQueNoVotaronEleccionDual(long idEleccion, String pais) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UsuarioPadron u, SupraEleccion p WHERE (p.idEleccionA =:elec OR p.idEleccionB =:elec) AND (u.eleccion.idEleccion = p.idEleccionA OR u.eleccion.idEleccion = p.idEleccionB) AND u.yaVoto=FALSE AND u.pais = :pais ORDER BY u.fechaVoto ASC", UserVoter.class);
		q.setParameter("elec", idEleccion);
		q.setParameter(PAIS, pais.toUpperCase());

		List<UserVoter> listaUnificada = new ArrayList<>();
		boolean existe = false;

		if (!q.getResultList().isEmpty())
			listaUnificada.add(q.getResultList().get(0));

		for (UserVoter padron : q.getResultList()) {
			for (UserVoter usu : listaUnificada) {
				if (usu.getMail().equalsIgnoreCase(padron.getMail())) {
					existe = true;
					break;
				}
			}

			if (!existe)
				listaUnificada.add(padron);
		}

		return listaUnificada;
	}

	public List<Object[]> obtenerObjetosUsuariosPadronEleccion(long idEleccion) {
		Query nq = em.createNativeQuery("SELECT u.idioma, u.nombre, u.mail, u.cantvotos, u.pais, u.orgid, u.tokenVotacion FROM UsuarioPadron u WHERE  u.id_eleccion = :elec ORDER BY u.id_usuario_padron asc");
		nq.setParameter("elec", idEleccion);
		@SuppressWarnings("unchecked")
		List<Object[]> q = nq.getResultList();
		return q;

	}

	public Long obtenerCantidadUsuariosPadron(long idEleccion) {
		Query q = em.createQuery("SELECT COUNT(u.idUsuarioPadron) FROM UsuarioPadron u WHERE u.eleccion.idEleccion =:idEleccion");
		q.setParameter(ID_ELECCION, idEleccion);
		return (Long) q.getSingleResult();
	}

	public UserVoter obtenerUsuarioPadronToken(String token) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UsuarioPadron u WHERE u.tokenVotacion =:token", UserVoter.class);
		q.setParameter("token", token);
		return q.getSingleResult();
	}

	public Long obtenerCantidadVotantesQueVotaronEleccion(long idEleccion) {
		Query q = em.createQuery("SELECT COUNT(u.idUsuarioPadron) FROM UsuarioPadron u WHERE u.eleccion.idEleccion =:idEleccion AND u.yaVoto=true");
		q.setParameter(ID_ELECCION, idEleccion);
		return (Long) q.getSingleResult();
	}

	public List<Integer> obtenerTodosLosPesosDistintosDeLosUP(Long idEleccion) {
		TypedQuery<Integer> q = em.createQuery("SELECT DISTINCT(u.cantVotos) FROM UsuarioPadron u WHERE u.eleccion.idEleccion =:idEleccion ORDER BY u.cantVotos", Integer.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public Long obtenerCantidadVotantesPorPesoPorEleccion(long idEleccion, Integer pesos) {
		Query q = em.createQuery("SELECT COUNT(u.idUsuarioPadron) FROM UsuarioPadron u WHERE u.eleccion.idEleccion =:idEleccion AND u.cantVotos=:cantVotos");
		q.setParameter(ID_ELECCION, idEleccion);
		q.setParameter("cantVotos", pesos);
		return (Long) q.getSingleResult();
	}

	public Long obtenerCantidadVotantesQueVotaronEleccionPorPeso(long idEleccion, Integer pesos) {
		Query q = em.createQuery("SELECT COUNT(u.idUsuarioPadron) FROM UsuarioPadron u WHERE u.eleccion.idEleccion =:idEleccion AND u.cantVotos=:cantVotos AND u.yaVoto=true");
		q.setParameter(ID_ELECCION, idEleccion);
		q.setParameter("cantVotos", pesos);
		return (Long) q.getSingleResult();
	}

	public void borrarUsuariosPadron(long idEleccion) {
		Query q = em.createQuery("DELETE FROM UsuarioPadron u WHERE u.eleccion.idEleccion=:idEleccion");
		q.setParameter(ID_ELECCION, idEleccion);
		q.executeUpdate();
	}

	public UserVoter obtenerUsuariosPadronOrgId(String org, Long idEleccion) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UsuarioPadron u WHERE u.orgID = :org AND u.eleccion.idEleccion =:idEleccion", UserVoter.class);
		q.setParameter(ID_ELECCION, idEleccion);
		q.setParameter("org", org);
		List<UserVoter> users = q.getResultList();
		if (!users.isEmpty())
			return users.get(0);
		else
			return null;
	}

	public boolean padronesIgualesEleccionDual(long idEleccionA, long idEleccionB) {
		TypedQuery<String> q1 = em.createQuery("SELECT u.mail FROM UsuarioPadron u where u.eleccion.idEleccion = :idEleccion", String.class);
		q1.setParameter(ID_ELECCION, idEleccionA);
		List<String> padron1 = q1.getResultList();

		TypedQuery<String> q2 = em.createQuery("SELECT u.mail FROM UsuarioPadron u where u.eleccion.idEleccion = :idEleccion", String.class);
		q2.setParameter(ID_ELECCION, idEleccionB);
		List<String> padron2 = q2.getResultList();

		Collections.sort(padron1);
		Collections.sort(padron2);

		return padron1.equals(padron2);
	}

}