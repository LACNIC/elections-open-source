package net.lacnic.elections.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.CandidateType;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.utils.Constants;

public class CandidateDao {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

	private EntityManager em;

	public CandidateDao(EntityManager em) {
		this.em = em;
	}

	public Candidate getCandidate(long candidateId) {
		TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.candidateId = :candidateId", Candidate.class);
		q.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		return q.getSingleResult();
	}

	public Candidate getCandidateForUpdate(long candidateId) {
		Candidate candidate = em.find(Candidate.class, candidateId, LockModeType.PESSIMISTIC_WRITE);
		if (candidate != null) {
			em.refresh(candidate, LockModeType.PESSIMISTIC_WRITE);
		}
		return candidate;
	}

	public List<Candidate> getCandidatesByEmail(String email, int pageSize, int offset) {
		TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.mail = :email ORDER BY c.candidateId", Candidate.class);
		q.setParameter("email", email);
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

	public Candidate getElectionFirstCandidate(long electionId) {
		try {
			TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.election.electionId = :electionId ORDER BY c.candidateOrder DESC", Candidate.class);
			q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
			q.setMaxResults(1);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Candidate getElectionLastCandidate(long electionId) {
		try {
			TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.election.electionId = :electionId ORDER BY c.candidateOrder", Candidate.class);
			q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
			q.setMaxResults(1);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<Candidate> getElectionCandidates(long electionId) {
		TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.election.electionId = :electionId ORDER BY c.candidateOrder DESC", Candidate.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<Candidate> getElectionPublishedCandidates(long electionId) {
		TypedQuery<Candidate> q = em.createQuery(
				"SELECT c FROM Candidate c WHERE c.election.electionId = :electionId AND c.status = :publishedStatus AND c.candidateType <> :abstentionType ORDER BY c.candidateOrder DESC",
				Candidate.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("publishedStatus", CandidateStatus.CONFIRMED_AND_PUBLISHED);
		q.setParameter("abstentionType", CandidateType.ABSTENTION);
		return q.getResultList();
	}

	public Candidate getElectionAbstentionCandidate(long electionId) {
		try {
			TypedQuery<Candidate> q = em.createQuery(
					"SELECT c FROM Candidate c WHERE c.election.electionId = :electionId AND c.candidateType = :candidateType",
					Candidate.class);
			q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
			q.setParameter("candidateType", CandidateType.ABSTENTION);
			q.setMaxResults(1);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public long countElectionCandidatesExcludingAbstention(long electionId) {
		TypedQuery<Long> q = em.createQuery(
				"SELECT COUNT(c) FROM Candidate c WHERE c.election.electionId = :electionId AND c.candidateType <> :candidateType",
				Long.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("candidateType", CandidateType.ABSTENTION);
		Long value = q.getSingleResult();
		return value != null ? value.longValue() : 0L;
	}

	public long countElectionCandidates(long electionId) {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(c) FROM Candidate c WHERE c.election.electionId = :electionId", Long.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		Long value = q.getSingleResult();
		return value != null ? value.longValue() : 0L;
	}

	public List<Candidate> getCandidatesWithEmail() {
		TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.mail IS NOT NULL", Candidate.class);
		return q.getResultList();
	}

	public long countAllCandidates() {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(c) FROM Candidate c", Long.class);
		Long value = q.getSingleResult();
		return value != null ? value.longValue() : 0L;
	}

	public List<Long> getCandidateIds(int pageSize, int firstResult) {
		TypedQuery<Long> q = em.createQuery("SELECT c.candidateId FROM Candidate c ORDER BY c.candidateId", Long.class);
		q.setMaxResults(pageSize);
		q.setFirstResult(firstResult);
		return q.getResultList();
	}

	public long getCandidateVotesAmount(long candidateId) {
		Query q = em.createQuery("SELECT COUNT(v.voteId) FROM Vote v WHERE v.candidate.candidateId = :candidateId");
		q.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		return (long) q.getSingleResult();
	}

	public int getLastNonFixedCandidateOrder(long electionId) {
		try {
			Query q = em.createQuery("SELECT c.candidateOrder FROM Candidate c WHERE c.election.electionId = :electionId AND c.candidateOrder != :maxorder AND c.candidateOrder != :minorder ORDER BY c.candidateOrder DESC");
			q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
			q.setParameter("maxorder", Constants.MAX_ORDER);
			q.setParameter("minorder", Constants.MIN_ORDER);
			q.setMaxResults(1);
			return (int) q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e.getMessage());
			return 1;
		}
	}

	public Candidate getNextAboveCandidate(long electionId, int candidateOrder) {
		try {
			TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.election.electionId = :electionId AND c.candidateOrder > :candidateOrder ORDER BY c.candidateOrder", Candidate.class);
			q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
			q.setParameter("candidateOrder", candidateOrder);
			q.setMaxResults(1);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
			return null;
		}
	}

	public Candidate getNextBelowCandidate(long electionId, int candidateOrder) {
		try {
			TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.election.electionId = :electionId AND c.candidateOrder < :candidateOrder ORDER BY c.candidateOrder DESC", Candidate.class);
			q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
			q.setParameter("candidateOrder", candidateOrder);
			q.setMaxResults(1);
			return q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getCandidatesAllIdAndDescription(int pageSize, int offset) {
		Query q = em.createQuery("SELECT c.candidateId, c.name FROM Candidate c ORDER BY c.candidateId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

}
