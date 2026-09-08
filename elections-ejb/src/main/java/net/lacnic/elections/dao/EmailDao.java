package net.lacnic.elections.dao;

import java.sql.Timestamp;
import java.util.List;

import org.joda.time.DateTimeUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
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

	public List<Email> getPendingSendEmailsOrdered(int maxResults) {
		TypedQuery<Email> q = em.createQuery(
				"SELECT e FROM Email e " +
				"WHERE e.sent = FALSE " +
				"ORDER BY CASE WHEN e.prioritized = TRUE THEN 0 ELSE 1 END, e.createdDate ASC, e.emailId ASC",
				Email.class);
		if (maxResults > 0) {
			q.setMaxResults(maxResults);
		}
		return q.getResultList();
	}

	public List<Email> getEmailsOlderOneMonth() {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.createdDate <= :nowMinus30Days", Email.class);
		q.setParameter("nowMinus30Days", new Timestamp(DateTimeUtils.currentTimeMillis() - 86400000L));
		return q.getResultList();
	}

	public List<Email> getElectionEmails(long electionId) {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.election.electionId = :electionId", Email.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<Email> getElectionPendingSendEmails(Long electionId) {
		TypedQuery<Email> q = em.createQuery("SELECT e FROM Email e WHERE e.election.electionId = :electionId AND e.sent = FALSE", Email.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getEmailsAllIdAndDescription(int pageSize, int offset) {
		Query q = em.createQuery("SELECT e.emailId, e.subject FROM Email e ORDER BY e.emailId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getEmailsHistoryAllIdAndDescription(int pageSize, int offset) {
		Query q = em.createQuery("SELECT eh.emailHistoryId, eh.subject FROM EmailHistory eh ORDER BY eh.emailHistoryId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

}
