package net.lacnic.elections.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.CandidateQuestion;

public class CandidateQuestionDao {

	private EntityManager em;

	public CandidateQuestionDao(EntityManager em) {
		this.em = em;
	}

	public CandidateQuestion getCandidateQuestion(long candidateQuestionId) {
		TypedQuery<CandidateQuestion> q = em.createQuery(
				"SELECT cq FROM CandidateQuestion cq WHERE cq.candidateQuestionId = :candidateQuestionId",
				CandidateQuestion.class);
		q.setParameter("candidateQuestionId", candidateQuestionId);
		return q.getSingleResult();
	}

	public List<CandidateQuestion> getElectionCandidateQuestions(long electionId) {
		TypedQuery<CandidateQuestion> q = em.createQuery(
				"SELECT cq FROM CandidateQuestion cq WHERE cq.election.electionId = :electionId ORDER BY cq.creationDate DESC, cq.candidateQuestionId DESC",
				CandidateQuestion.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<CandidateQuestion> getElectionCandidateQuestionsForPublicElectionPage(long electionId) {
		TypedQuery<CandidateQuestion> q = em.createQuery(
				"SELECT cq FROM CandidateQuestion cq LEFT JOIN FETCH cq.candidate WHERE cq.election.electionId = :electionId ORDER BY cq.creationDate DESC, cq.candidateQuestionId DESC",
				CandidateQuestion.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<CandidateQuestion> getCandidateQuestionsByCandidateId(long candidateId) {
		TypedQuery<CandidateQuestion> q = em.createQuery(
				"SELECT cq FROM CandidateQuestion cq WHERE cq.candidate.candidateId = :candidateId ORDER BY cq.creationDate DESC, cq.candidateQuestionId DESC",
				CandidateQuestion.class);
		q.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		return q.getResultList();
	}

	public CandidateQuestion getCandidateQuestionByIdAndCandidateId(long candidateQuestionId, long candidateId) {
		TypedQuery<CandidateQuestion> q = em.createQuery(
				"SELECT cq FROM CandidateQuestion cq WHERE cq.candidateQuestionId = :candidateQuestionId AND cq.candidate.candidateId = :candidateId",
				CandidateQuestion.class);
		q.setParameter("candidateQuestionId", candidateQuestionId);
		q.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		List<CandidateQuestion> result = q.setMaxResults(1).getResultList();
		return result.isEmpty() ? null : result.get(0);
	}
}
