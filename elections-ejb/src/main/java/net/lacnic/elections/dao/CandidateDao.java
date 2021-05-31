package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.utils.Constants;


public class CandidateDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;


	public CandidateDao(EntityManager em) {
		this.em = em;
	}

	public Candidate getCandidate(long candidateId) {
		TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.candidateId = :candidateId", Candidate.class);
		q.setParameter("candidateId", candidateId);
		return q.getSingleResult();
	}

	public Candidate getElectionFirstCandidate(long electionId) {
		TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.election.electionId = :electionId ORDER BY c.candidateOrder DESC", Candidate.class);
		q.setParameter("electionId", electionId);
		q.setMaxResults(1);
		return q.getSingleResult();
	}

	public Candidate getElectionLastCandidate(long electionId) {
		TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.election.electionId = :electionId ORDER BY c.candidateOrder", Candidate.class);
		q.setParameter("electionId", electionId);
		q.setMaxResults(1);
		return q.getSingleResult();
	}

	public List<Candidate> getElectionCandidates(long electionId) {
		TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.election.electionId = :electionId ORDER BY c.candidateOrder DESC", Candidate.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public long getCandidateVotesAmount(long candidateId) {
		Query q = em.createQuery("SELECT COUNT(v.voteId) FROM Vote v WHERE v.candidate.candidateId = :candidateId");
		q.setParameter("candidateId", candidateId);
		return (long) q.getSingleResult();
	}

	public int getLastNonFixedCandidateOrder(long electionId) {
		try {
			Query q = em.createQuery("SELECT c.candidateOrder FROM Candidate c WHERE c.election.electionId = :electionId AND c.candidateOrder != :maxorder AND c.candidateOrder != :minorder ORDER BY c.candidateOrder DESC");
			q.setParameter("electionId", electionId);
			q.setParameter("maxorder", Constants.MAX_ORDER);
			q.setParameter("minorder", Constants.MIN_ORDER);
			q.setMaxResults(1);
			return (int) q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return 1;
		}
	}

	public Candidate getNextAboveCandidate(long electionId, int candidateOrder) {
		try {
			TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.election.electionId = :electionId AND c.candidateOrder > :candidateOrder ORDER BY c.candidateOrder", Candidate.class);
			q.setParameter("electionId", electionId);
			q.setParameter("candidateOrder", candidateOrder);
			q.setMaxResults(1);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public Candidate getNextBelowCandidate(long electionId, int candidateOrder) {
		try {
			TypedQuery<Candidate> q = em.createQuery("SELECT c FROM Candidate c WHERE c.election.electionId = :electionId AND c.candidateOrder < :candidateOrder ORDER BY c.candidateOrder DESC", Candidate.class);
			q.setParameter("electionId", electionId);
			q.setParameter("candidateOrder", candidateOrder);
			q.setMaxResults(1);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

}
