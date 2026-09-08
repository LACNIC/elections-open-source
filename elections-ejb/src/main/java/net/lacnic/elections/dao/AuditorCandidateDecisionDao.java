package net.lacnic.elections.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;

public class AuditorCandidateDecisionDao {

	private final EntityManager em;

	public AuditorCandidateDecisionDao(EntityManager em) {
		this.em = em;
	}

	public List<AuditorCandidateDecision> getElectionAuditorCandidateDecisions(long electionId) {
		TypedQuery<AuditorCandidateDecision> query = em.createQuery(
				"SELECT d FROM AuditorCandidateDecision d WHERE d.candidate.election.electionId = :electionId ORDER BY d.id",
				AuditorCandidateDecision.class);
		query.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return query.getResultList();
	}

	public AuditorCandidateDecision getByAuditorAndCandidate(long auditorId, long candidateId) {
		TypedQuery<AuditorCandidateDecision> query = em.createQuery(
				"SELECT d FROM AuditorCandidateDecision d WHERE d.auditor.auditorId = :auditorId AND d.candidate.candidateId = :candidateId ORDER BY d.id",
				AuditorCandidateDecision.class);
		query.setParameter(QueryParameterNames.AUDITOR_ID, auditorId);
		query.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		return query.setMaxResults(1).getResultStream().findFirst().orElse(null);
	}

	public long countDistinctAuditorsByCandidateAndStatus(long candidateId, AuditorCandidateDecisionStatus status) {
		TypedQuery<Long> query = em.createQuery(
				"SELECT COUNT(DISTINCT d.auditor.auditorId) FROM AuditorCandidateDecision d WHERE d.candidate.candidateId = :candidateId AND d.decisionStatus = :status",
				Long.class);
		query.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		query.setParameter("status", status);
		Long value = query.getSingleResult();
		return value != null ? value.longValue() : 0L;
	}

	public List<Object[]> getCandidateDecisionStatusByAuditorId(long auditorId) {
		TypedQuery<Object[]> query = em.createQuery(
				"SELECT d.candidate.candidateId, d.decisionStatus FROM AuditorCandidateDecision d WHERE d.auditor.auditorId = :auditorId",
				Object[].class);
		query.setParameter(QueryParameterNames.AUDITOR_ID, auditorId);
		return query.getResultList();
	}

	public long countDistinctResolvedCandidatesByAuditor(long electionId, long auditorId, AuditorCandidateDecisionStatus excludedStatus) {
		TypedQuery<Long> query = em.createQuery(
				"SELECT COUNT(DISTINCT d.candidate.candidateId) FROM AuditorCandidateDecision d "
						+ "WHERE d.auditor.auditorId = :auditorId "
						+ "AND d.candidate.election.electionId = :electionId "
						+ "AND d.decisionStatus <> :excludedStatus",
				Long.class);
		query.setParameter(QueryParameterNames.AUDITOR_ID, auditorId);
		query.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		query.setParameter("excludedStatus", excludedStatus);
		Long value = query.getSingleResult();
		return value != null ? value.longValue() : 0L;
	}
}
