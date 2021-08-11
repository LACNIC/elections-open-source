package net.lacnic.elections.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.joda.time.DateTimeUtils;

import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.EmailHistory;


public class EmailDao {

	private EntityManager em;


	public EmailDao(EntityManager em) {
		this.em = em;
	}

	public List<Email> getEmailsAll() {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e", Email.class);
		return q.getResultList();
	}

	public Email getEmail(long emailId) {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.emailId = :emailId", Email.class);
		q.setParameter("emailId", emailId);
		return q.getSingleResult();
	}

	public EmailHistory getEmailHistory(long emailHistoryId) {
		TypedQuery<EmailHistory> q = em.createQuery("SELECT eh FROM EmailHistory eh WHERE eh.emailHistoryId = :emailHistoryId", EmailHistory.class);
		q.setParameter("emailHistoryId", emailHistoryId);
		return q.getSingleResult();
	}

	public List<Email> getPendingSendEmails() {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.sent = FALSE", Email.class);
		return q.getResultList();
	}

	public List<Email> getEmailsOlderOneMonth() {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.createdDate <= :nowMinus30Days", Email.class);
		q.setParameter("nowMinus30Days", new Timestamp(DateTimeUtils.currentTimeMillis() - 86400000L));
		return q.getResultList();
	}

	public void markAllEmailsAsSent() {
		Query q = em.createQuery("UPDATE Email SET sent = TRUE WHERE sent = FALSE");
		q.executeUpdate();
	}

	public List<Email> getElectionEmails(long electionId) {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.election.electionId = :electionId", Email.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public List<Email> getElectionPendingSendEmails(Long electionId) {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.election.electionId = :electionId AND e.sent = FALSE", Email.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getEmailsAllIdAndDescription() {
		Query q = em.createQuery("SELECT e.emailId, e.subject FROM Email e ORDER BY e.emailId");
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getEmailsHistoryAllIdAndDescription() {
		Query q = em.createQuery("SELECT eh.emailHistoryId, eh.subject FROM EmailHistory eh ORDER BY eh.emailHistoryId");
		return q.getResultList();
	}

}
