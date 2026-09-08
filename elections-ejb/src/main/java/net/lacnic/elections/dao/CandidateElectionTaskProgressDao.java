package net.lacnic.elections.dao;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.ElectionTaskKey;

public class CandidateElectionTaskProgressDao {

	private final EntityManager em;

	public CandidateElectionTaskProgressDao(EntityManager em) {
		this.em = em;
	}

	public CandidateElectionTaskProgress getByNominationTokenAndTaskKey(String token, ElectionTaskKey taskKey) {
		return getByNominationTokenAndTaskKey(token, taskKey, false);
	}

	public CandidateElectionTaskProgress getByNominationTokenAndTaskKeyForUpdate(String token, ElectionTaskKey taskKey) {
		return getByNominationTokenAndTaskKey(token, taskKey, true);
	}

	private CandidateElectionTaskProgress getByNominationTokenAndTaskKey(String token, ElectionTaskKey taskKey, boolean forUpdate) {
		TypedQuery<CandidateElectionTaskProgress> query = em.createQuery("SELECT tp FROM Nomination n JOIN n.candidate c JOIN c.taskProgress tp JOIN tp.electionTask t WHERE n.acceptNominationToken = :token AND t.taskKey = :taskKey", CandidateElectionTaskProgress.class);
		query.setParameter("token", token);
		query.setParameter("taskKey", taskKey);
		if (forUpdate) {
			query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		}
		List<CandidateElectionTaskProgress> result = query.setMaxResults(1).getResultList();
		return result.isEmpty() ? null : result.get(0);
	}

	public CandidateElectionTaskProgress getByCandidateIdAndTaskKey(long candidateId, ElectionTaskKey taskKey) {
		return getByCandidateIdAndTaskKey(candidateId, taskKey, false);
	}

	public CandidateElectionTaskProgress getByCandidateIdAndTaskKeyForUpdate(long candidateId, ElectionTaskKey taskKey) {
		return getByCandidateIdAndTaskKey(candidateId, taskKey, true);
	}

	private CandidateElectionTaskProgress getByCandidateIdAndTaskKey(long candidateId, ElectionTaskKey taskKey, boolean forUpdate) {
		TypedQuery<CandidateElectionTaskProgress> query = em.createQuery("SELECT tp FROM CandidateElectionTaskProgress tp WHERE tp.candidate.candidateId = :candidateId AND tp.electionTask.taskKey = :taskKey", CandidateElectionTaskProgress.class);
		query.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		query.setParameter("taskKey", taskKey);
		if (forUpdate) {
			query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		}
		List<CandidateElectionTaskProgress> result = query.setMaxResults(1).getResultList();
		return result.isEmpty() ? null : result.get(0);
	}

	public List<CandidateElectionTaskProgress> getByElectionIdAndTaskKeys(long electionId, List<ElectionTaskKey> taskKeys) {
		if (taskKeys == null || taskKeys.isEmpty()) {
			return Collections.emptyList();
		}
		TypedQuery<CandidateElectionTaskProgress> query = em.createQuery("SELECT tp FROM CandidateElectionTaskProgress tp WHERE tp.electionTask.election.electionId = :electionId AND tp.electionTask.taskKey IN :taskKeys", CandidateElectionTaskProgress.class);
		query.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		query.setParameter("taskKeys", taskKeys);
		return query.getResultList();
	}

	public List<CandidateElectionTaskProgress> getByCandidateId(long candidateId) {
		TypedQuery<CandidateElectionTaskProgress> query = em.createQuery("SELECT tp FROM CandidateElectionTaskProgress tp WHERE tp.candidate.candidateId = :candidateId", CandidateElectionTaskProgress.class);
		query.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		return query.getResultList();
	}

	public List<Long> getCandidateIdsByElectionTask(long electionTaskId) {
		TypedQuery<Long> query = em.createQuery("SELECT tp.candidate.candidateId FROM CandidateElectionTaskProgress tp WHERE tp.electionTask.id = :electionTaskId", Long.class);
		query.setParameter("electionTaskId", electionTaskId);
		return query.getResultList();
	}

	public long countCompletedOrOmittedTasksByCandidate(long electionId, long candidateId) {
		TypedQuery<Long> query = em.createQuery("SELECT COUNT(tp) FROM CandidateElectionTaskProgress tp " + "WHERE tp.candidate.candidateId = :candidateId " + "AND tp.electionTask.election.electionId = :electionId " + "AND (tp.status = :completedStatus OR tp.status = :omittedStatus)", Long.class);
		query.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		query.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		query.setParameter("completedStatus", CandidateElectionTaskStatus.COMPLETED);
		query.setParameter("omittedStatus", CandidateElectionTaskStatus.OMITTED);
		Long value = query.getSingleResult();
		return value != null ? value.longValue() : 0L;
	}
}
