package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.lacnic.elections.domain.Auditor;


public class AuditorDao {

	private EntityManager em;

	public AuditorDao(EntityManager em) {
		this.em = em;
	}

	/**
	 * Gets Auditor by id.
	 * 
	 * @param auditorId
	 *            Auditor id.
	 * @return Returns the Auditor entity corresponding to the id 
	 */
	public Auditor getAuditor(long auditorId) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.auditorId = :auditorId", Auditor.class);
		q.setParameter("auditorId", auditorId);
		return q.getSingleResult();
	}

	/**
	 * Gets all the auditor from the system.
	 * 
	 * @return returns all the auditors present on the system.
	 */
	public List<Auditor> getAuditorsAll() {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a", Auditor.class);
		return q.getResultList();
	}

	/**
	 * Gets a list of Auditors related to a particular election.
	 * 
	 * @param electionId
	 *            Election identifier.
	 * @return Returns a list of auditor related to a particular election.
	 */
	public List<Auditor> getElectionAuditors(long electionId) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.election.electionId = :electionId", Auditor.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public List<Auditor> getAuditorsByEmail(String email) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.mail = :email", Auditor.class);
		q.setParameter("email", email);
		return q.getResultList();
	}
	
	public boolean auditorExists(long electionId, String name, String mail) {
		Query q = em.createQuery("SELECT a.auditorId FROM Auditor a WHERE a.election.electionId = :electionId AND a.name = :name AND a.mail = :mail");
		q.setParameter("electionId", electionId);
		q.setParameter("mail", mail);
		q.setParameter("name", name);
		q.setMaxResults(1);
		return !q.getResultList().isEmpty();
	}

	public Auditor getAuditorByResultToken(String resultToken) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.resultToken = :resultToken", Auditor.class);
		q.setParameter("resultToken", resultToken);
		return q.getSingleResult();
	}

	/**
	 * Gets a list of result tokens from the Auditors
	 * 
	 * 
	 * @return returns a list of result tokens from the Auditors
	 */
	public List<String> getAllAuditorsUUIDs() {
		TypedQuery<String> q = em.createQuery("SELECT a.resultToken FROM Auditor a", String.class);
		return q.getResultList();
	}

	public List<Auditor> getElectionAuditorsNotAgreedConformity(long electionId) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.commissioner = TRUE AND a.agreedConformity = FALSE AND a.election.electionId = :electionId", Auditor.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public List<Auditor> getElectionAuditorsAgreedConformity(long electionId) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.commissioner = TRUE AND a.agreedConformity = TRUE AND a.election.electionId = :electionId", Auditor.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getAuditorsAllIdAndDescription(int pageSize, int offset) {
		Query q = em.createQuery("SELECT a.auditorId, a.name FROM Auditor a ORDER BY a.auditorId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

}
