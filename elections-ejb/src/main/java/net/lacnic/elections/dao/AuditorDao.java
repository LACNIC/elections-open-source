package net.lacnic.elections.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.lacnic.elections.data.TablesReportData;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.AuditorReportTable;


public class AuditorDao {

	private EntityManager em;

	public AuditorDao(EntityManager em) {
		this.em = em;
	}

	/**
	 * Gets Auditor by id.
	 * 
	 * @param id
	 *            Auditor id.
	 * @return Returns the Auditor entity corresponding to the id 
	 */
	public Auditor getAuditor(long auditorId) {
		TypedQuery<Auditor> q = em.createQuery("SELECT c FROM Auditor c WHERE c.auditorId = :auditorId", Auditor.class);
		q.setParameter("auditorId", auditorId);
		return q.getSingleResult();
	}

	/**
	 * Gets all the auditor from the system.
	 * 
	 * @return returns all the auditors present on the system.
	 */
	public List<Auditor> getAuditorsAll() {
		TypedQuery<Auditor> q = em.createQuery("SELECT c FROM Auditor c", Auditor.class);
		return q.getResultList();
	}

	/**
	 * Gets a list of Auditors related to a particular election.
	 * 
	 * @param idEleccion
	 *            Election identifier.
	 * @return Returns a list of auditor related to a particular election.
	 */
	public List<Auditor> getElectionAuditors(long electionId) {
		TypedQuery<Auditor> q = em.createQuery("SELECT c FROM Auditor c WHERE c.election.electionId = :electionId", Auditor.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public boolean auditorExists(long electionId, String name, String mail) {
		Query q = em.createQuery("SELECT c.auditorId FROM Auditor c WHERE c.election.electionId = :electionId AND c.name = :name AND c.mail = :mail");
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
	
	
	public List<TablesReportData> getAuditorsData() {
		Query q = em.createQuery("SELECT a.auditorId, a.name FROM Auditor a ORDER BY a.auditorId");
		@SuppressWarnings("unchecked")
		List<Object[]> result = q.getResultList();

		List<TablesReportData> resultList = new ArrayList<>();
		for (int i = 0; i < result.size(); i++) {
			TablesReportData trd = new TablesReportData((long)result.get(i)[0], result.get(i)[1].toString());
			resultList.add(trd);
		}
		return resultList;	
	}
	
	public AuditorReportTable getAuditorTableReport(Long id) {
		TypedQuery<AuditorReportTable> q = em.createQuery("SELECT a FROM AuditorReportTable a WHERE a.auditorId = :auditorId", AuditorReportTable.class);
		q.setParameter("auditorId", id);
		return q.getSingleResult();
	
	}

}
