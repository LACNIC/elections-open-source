package net.lacnic.siselecciones.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class ReportDao {

	private static final String ID_ELECCION = "idEleccion";

	private EntityManager em;

	public ReportDao(EntityManager em) {
		this.em = em;
	}

	public ReportDao() {

	}

	/**
	 * Obtiene una lista de e-mails con estado A Enviar
	 * 
	 * @return Retorna una lista de e-mails con estado A Enviar
	 */
	public long obtenerCantidadCorreos() {
		Query q = em.createQuery("SELECT e.id FROM Email e");
		return q.getResultList().size();
	}

	/**
	 * Obtiene la cantidad de emails sin enviar
	 * 
	 * @return Retorna un long refiriendose a la cantidad de emails sin enviar
	 * 
	 */
	public long obtenerCorreosPendientes() {
		Query q = em.createQuery("SELECT COUNT(e.id) FROM Email e WHERE e.enviado =:enviado");
		q.setParameter("enviado", false);
		return (Long) q.getSingleResult();
	}

	/**
	 * Obtiene la cantidad de emails enviados
	 * 
	 * @return Retorna un long refiriendose a la cantidad de emails enviados
	 * 
	 */
	public long obtenerCorreosEnviados() {
		Query q = em.createQuery("SELECT COUNT(e.id) FROM Email e WHERE e.enviado =:enviado");
		q.setParameter("enviado", true);
		return (Long) q.getSingleResult();
	}

	/**
	 * Obtiene la cantidad de ips que han tenido accesos fallidos
	 * 
	 *
	 * @return Retorna un long con la cantidad de ips que han tenido intento de
	 *         acceso fallido
	 */
	public long obtenerCantIpsAccesosFallidos() {
		Query q = em.createQuery("SELECT a.id FROM AccesosIps a");
		return q.getResultList().size();
	}

	/**
	 * Obtiene la suma de los accesos fallidos de cada ip de la tabla AccesosIps
	 * 
	 *
	 * @return Retorna un long con la cantidad de ips que han tenido intento de
	 *         Baneo
	 */
	public long sumaIntentosAccesosFallidos() {
		Query q = em.createQuery("SELECT COALESCE(SUM (a.intentos),0) FROM AccesosIps a");
		return (Long) q.getSingleResult();
	}


	/***
	 * Consultas para cada Eleccion
	 */

	/**
	 * Obtiene la cantidad de emails sin enviar correspondiente al id de la eleccion
	 * pasado por parametro * @param idEleccion el id de la eleccion
	 * 
	 * @return Retorna un long refiriendose a la cantidad de emails sin enviar
	 * 
	 */
	public long obtenerCorreosSinEnviarElec(long idElec) {
		Query q = em.createQuery("SELECT COUNT(e.id) FROM Email e WHERE e.eleccion.idEleccion =:idEleccion AND e.enviado = false");
		q.setParameter(ID_ELECCION, idElec);
		return (Long) q.getSingleResult();
	}

	/**
	 * Obtiene una lista con los id y nombres de la tabla Eleccion
	 * 
	 * @return Retorna un Lista de Objetos conteniendo todos los id y nombres de la tabla
	 *         Eleccion
	 * 
	 */

	public List<Object[]> idElecciones() {
		return em.createQuery("SELECT e.idEleccion, e.tituloEspanol FROM Eleccion e").getResultList();
	}

	/**
	 * Devuelve un long indicando la cantidad de votantes válidos que efectuaron su
	 * votación correctamente para la eleccion proporcionada por parametro
	 * 
	 * @param idEleccion
	 *            la elección a la que referimos el votante pertenece
	 *
	 * 
	 * @return Retorna un long indicando la cantidad de votantes válidos que
	 *         efectuaron su votación correctamente para la eleccion proporcionada
	 *         por parametro
	 * 
	 */
	public long obtenerCantidadVotantesVanVotando(long idEleccion) {
		Query q = em.createQuery("SELECT u.idUsuarioPadron FROM UsuarioPadron u WHERE u.eleccion.idEleccion =:idEleccion AND u.yaVoto=true");
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList().size();
	}

	/**
	 * Devuelve un long indicando la cantidad de votantes válidos que no han
	 * efectuado su votación para la eleccion proporcionada por parametro
	 * 
	 * @param idEleccion
	 *            la elección a la que referimos el votante pertenece
	 *
	 * 
	 * @return Retorna un long indicando la cantidad de votantes válidos que no han
	 *         efectuado su votación para la eleccion proporcionada por parametro
	 * 
	 */
	public long obtenerCantidadVotantesNoHanVotado(long idEleccion) {
		Query q = em.createQuery("SELECT u.idUsuarioPadron FROM UsuarioPadron u WHERE  u.eleccion.idEleccion =:idEleccion AND u.yaVoto=false");
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList().size();
	}

	/**
	 * Obtiene la cantidad de UsuarioPadron que participan en la eleccion de
	 * identificador pasado por parámetro * @param idEleccion el id de la elección
	 * que buscamos el padrón
	 * 
	 * @return Retorna la cantidad de usuarios asociados a la eleccion
	 */
	public long obtenerCantUsuariosPadronEleccion(long idEleccion) {
		Query q = em.createQuery("SELECT u.idUsuarioPadron FROM UsuarioPadron u WHERE  u.eleccion.idEleccion =:idEleccion");
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList().size();
	}

}
