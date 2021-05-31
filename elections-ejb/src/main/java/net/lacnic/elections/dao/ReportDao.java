package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;


public class ReportDao {

	private EntityManager em;


	public ReportDao(EntityManager em) {
		this.em = em;
	}

	public ReportDao() { }

	/**
	 * Obtiene una lista de e-mails con estado A Enviar
	 * 
	 * @return Retorna una lista de e-mails con estado A Enviar
	 */
	public long getEmailsAmount() {
		Query q = em.createQuery("SELECT COUNT(e.emailId) FROM Email e");
		return (long) q.getSingleResult();
	}

	/**
	 * Obtiene la cantidad de emails sin enviar
	 * 
	 * @return Retorna un long refiriendose a la cantidad de emails sin enviar
	 * 
	 */
	public long getPendingSendEmailsAmount() {
		Query q = em.createQuery("SELECT COUNT(e.emailId) FROM Email e WHERE e.sent = FALSE");
		return (long) q.getSingleResult();
	}

	/**
	 * Obtiene la cantidad de emails enviados
	 * 
	 * @return Retorna un long refiriendose a la cantidad de emails enviados
	 * 
	 */
	public long getSentEmailsAmount() {
		Query q = em.createQuery("SELECT COUNT(e.emailId) FROM Email e WHERE e.sent = TRUE");
		return (long) q.getSingleResult();
	}

	/**
	 * Obtiene la cantidad de ips que han tenido accesos fallidos
	 * 
	 *
	 * @return Retorna un long con la cantidad de ips que han tenido intento de
	 *         acceso fallido
	 */
	public long getFailedIpAccesesAmount() {
		Query q = em.createQuery("SELECT COUNT(a.ipAccessId) FROM IpAccess a");
		return (long) q.getSingleResult();
	}

	/**
	 * Obtiene la suma de los accesos fallidos de cada ip de la tabla AccesosIps
	 * 
	 *
	 * @return Retorna un long con la cantidad de ips que han tenido intento de
	 *         Baneo
	 */
	public long getFailedIpAccesesSum() {
		Query q = em.createQuery("SELECT COALESCE(SUM (a.attemptCount), 0) FROM IpAccess a");
		return (long) q.getSingleResult();
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
	public long getElectionPendingSendEmailsAmount(long electionId) {
		Query q = em.createQuery("SELECT COUNT(e.emailId) FROM Email e WHERE e.election.electionId = :electionId AND e.sent = FALSE");
		q.setParameter("electionId", electionId);
		return (long) q.getSingleResult();
	}

	/**
	 * Obtiene una lista con los id y nombres de la tabla Eleccion
	 * 
	 * @return Retorna un Lista de Objetos conteniendo todos los id y nombres de la tabla
	 *         Eleccion
	 * 
	 */
	public List<Object[]> getElectionsAllIdName() {
		return em.createQuery("SELECT e.electionId, e.titleSpanish FROM Election e").getResultList();
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
	public long getElectionAlreadyVotedAmount(long electionId) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId =: electionId AND u.voted = TRUE");
		q.setParameter("electionId", electionId);
		return (long) q.getSingleResult();
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
	public long getElectionNotVotedYetAmount(long electionId) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId =: electionId AND u.voted = FALSE");
		q.setParameter("electionId", electionId);
		return (long) q.getSingleResult();
	}

	/**
	 * Obtiene la cantidad de UsuarioPadron que participan en la eleccion de
	 * identificador pasado por parámetro * @param idEleccion el id de la elección
	 * que buscamos el padrón
	 * 
	 * @return Retorna la cantidad de usuarios asociados a la eleccion
	 */
	public long getElectionCensusSize(long electionId) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId =: electionId");
		q.setParameter("electionId", electionId);
		return (long) q.getSingleResult();
	}

}
