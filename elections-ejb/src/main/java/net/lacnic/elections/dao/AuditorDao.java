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
	 * Obtiene el Auditor de id=id
	 * 
	 * @param id
	 *            el id de un Auditor
	 * @return Devuelve la entidad Auditor correspondiente al identificador
	 */
	public Auditor getAuditor(long auditorId) {
		TypedQuery<Auditor> q = em.createQuery("SELECT c FROM Auditor c WHERE c.auditorId = :auditorId", Auditor.class);
		q.setParameter("auditorId", auditorId);
		return q.getSingleResult();
	}

	/**
	 * Obtiene todos los auditores del sistema sin importar la eleccion a la que
	 * corresponden
	 * 
	 * @return Devfuelve todos los auditores del sistema sin importar la
	 *         eleccion a la que corresponden
	 */
	public List<Auditor> getAuditorsAll() {
		TypedQuery<Auditor> q = em.createQuery("SELECT c FROM Auditor c", Auditor.class);
		return q.getResultList();
	}

	/**
	 * Obtiene una lista con los auditores de la eleccion de id=idEleccion
	 * 
	 * @param idEleccion
	 *            el id de la Eleccion de la cual queremos obtener los auditores
	 * @return Devuelve una lista de Auditores que aplican para la eleccion de
	 *         idEleccion
	 */
	public List<Auditor> getElectionAuditors(long electionId) {
		TypedQuery<Auditor> q = em.createQuery("SELECT c FROM Auditor c WHERE c.election.electionId = :electionId", Auditor.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public boolean auditorExists(long electionId, String name, String mail) {
		Query q = em.createQuery("SELECT c.idAuditor FROM Auditor c WHERE c.election.electionId = :electionId AND c.name = :name AND c.mail = :mail");
		q.setParameter("electionId", electionId);
		q.setParameter("mail", mail);
		q.setParameter("nombre", name);
		q.setMaxResults(1);
		return !q.getResultList().isEmpty();
	}

	public Auditor getAuditorByResultToken(String resultToken) {
		TypedQuery<Auditor> q = em.createQuery("SELECT a FROM Auditor a WHERE a.resultToken = :resultToken", Auditor.class);
		q.setParameter("resultToken", resultToken);
		return q.getSingleResult();
	}

	/**
	 * Obtiene todos los UUID de auditores existentes, es utilizado para chekear
	 * que no se repitan los codigos generados
	 * 
	 * 
	 * @return Devuelve una lista con todos los UUIDS creados hasta el momento
	 *         para descartar repeticiones
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

}
