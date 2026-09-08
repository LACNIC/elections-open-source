package net.lacnic.elections.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Auditor;

public class AuditorDao {

	private EntityManager em;

	public AuditorDao(EntityManager em) {
		this.em = em;
	}

	/**
	 * Gets Auditor by id.
	 * 
	 * @param auditorId Auditor id.
	 * @return Returns the Auditor entity corresponding to the id
	 */
	public Auditor getAuditor(long auditorId) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.auditorId = :auditorId", Auditor.class);
		q.setParameter(QueryParameterNames.AUDITOR_ID, auditorId);
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
	 * @param electionId Election identifier.
	 * @return Returns a list of auditor related to a particular election.
	 */
	public List<Auditor> getElectionAuditors(long electionId) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.election.electionId = :electionId", Auditor.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public long countElectionAuditors(long electionId) {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(a) FROM Auditor a WHERE a.election.electionId = :electionId", Long.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		Long value = q.getSingleResult();
		return value != null ? value.longValue() : 0L;
	}

	public List<Auditor> getAuditorsWithEmail() {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.mail IS NOT NULL", Auditor.class);
		return q.getResultList();
	}

	public List<Auditor> getAuditorsByEmail(String email, int pageSize, int offset) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.mail = :email ORDER BY a.auditorId", Auditor.class);
		q.setParameter("email", email);
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

	public boolean auditorExists(long electionId, String name, String mail) {
		Query q = em.createQuery("SELECT a.auditorId FROM Auditor a WHERE a.election.electionId = :electionId AND a.name = :name AND a.mail = :mail");
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
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
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<Auditor> getElectionAuditorsAgreedConformity(long electionId) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.commissioner = TRUE AND a.agreedConformity = TRUE AND a.election.electionId = :electionId", Auditor.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
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
