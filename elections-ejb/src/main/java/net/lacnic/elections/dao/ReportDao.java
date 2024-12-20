package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class ReportDao {

	private EntityManager em;
	private static final String ELECTION_ID = "electionId";

	public ReportDao(EntityManager em) {
		this.em = em;
	}

	public ReportDao() {
	}

	/**
	 * Gets the amount of emails in the system
	 * 
	 * @return returns the amount of emails in the system
	 */
	public long getEmailsAmount() {
		Query q = em.createQuery("SELECT COUNT(e.emailId) FROM Email e");
		return (long) q.getSingleResult();
	}

	/**
	 * Gets the amount of unsent emails
	 * 
	 * @return returns the amount of unsent emails
	 * 
	 */
	public long getPendingSendEmailsAmount() {
		Query q = em.createQuery("SELECT COUNT(e.emailId) FROM Email e WHERE e.sent = FALSE");
		return (long) q.getSingleResult();
	}

	/**
	 * Gets the amount of sent emails
	 * 
	 * @return Returns the amount of sent emails
	 * 
	 */
	public long getSentEmailsAmount() {
		Query q = em.createQuery("SELECT COUNT(e.emailId) FROM Email e WHERE e.sent = TRUE");
		return (long) q.getSingleResult();
	}

	/**
	 * Gets the amount of failed access attempts
	 * 
	 *
	 * @return returns the amount of failed access attempts
	 */
	public long getFailedIpAccesesAmount() {
		Query q = em.createQuery("SELECT COUNT(a.ipAccessId) FROM IpAccess a");
		return (long) q.getSingleResult();
	}

	/**
	 * Gets the sum of all the amount of failed access attempts
	 * 
	 *
	 * @return returns the sum of all the amount of failed access attempts
	 */
	public long getFailedIpAccesesSum() {
		Query q = em.createQuery("SELECT COALESCE(SUM (a.attemptCount), 0) FROM IpAccess a");
		return (long) q.getSingleResult();
	}

	/***
	 * Election Querys
	 */

	/**
	 * Gets the amount of pending emails for a particular election
	 * 
	 * @param electionId election identifier
	 * 
	 * @return returns the amount of pending emails for a particular election
	 * 
	 */
	public long getElectionPendingSendEmailsAmount(long electionId) {
		Query q = em.createQuery("SELECT COUNT(e.emailId) FROM Email e WHERE e.election.electionId = :electionId AND e.sent = FALSE");
		q.setParameter(ELECTION_ID, electionId);
		return (long) q.getSingleResult();
	}

	/**
	 * Obtiene una lista con los id y nombres de la tabla Eleccion
	 * 
	 * @return Retorna un Lista de Objetos conteniendo todos los id y nombres de la tabla Eleccion
	 * 
	 */

	@SuppressWarnings("unchecked")
	public List<Object[]> getElectionsAllIdName() {
		return em.createQuery("SELECT e.electionId, e.titleSpanish FROM Election e").getResultList();
	}

	/**
	 * Gets the amount of voter who actually vote on the election.
	 * 
	 * @param electionId election identifier
	 *
	 * 
	 * @return returns the amount of voters who actually vote on the election.
	 * 
	 */
	public long getElectionAlreadyVotedAmount(long electionId) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId =: electionId AND u.voted = TRUE");
		q.setParameter(ELECTION_ID, electionId);
		return (long) q.getSingleResult();
	}

	/**
	 * Gets the amount of voters who did not vote on the election.
	 * 
	 * @param electionId election identifier
	 *
	 * 
	 * @return returns the amount of voters who did not vote on the election.
	 * 
	 */
	public long getElectionNotVotedYetAmount(long electionId) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId =: electionId AND u.voted = FALSE");
		q.setParameter(ELECTION_ID, electionId);
		return (long) q.getSingleResult();
	}

	/**
	 * Get the amount of voters of a particular election
	 * 
	 * @param electionId election identifier
	 * 
	 * @return returns the amount of voters of a particular election
	 * 
	 */
	public long getElectionCensusSize(long electionId) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId =: electionId");
		q.setParameter(ELECTION_ID, electionId);
		return (long) q.getSingleResult();
	}

}
