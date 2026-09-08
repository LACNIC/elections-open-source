package net.lacnic.elections.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;

public class ElectionTaskDao {

	private EntityManager em;

	public ElectionTaskDao(EntityManager em) {
		this.em = em;
	}

	public List<ElectionTask> getElectionTasks(long electionId) {
		TypedQuery<ElectionTask> q = em.createQuery("SELECT t FROM ElectionTask t WHERE t.election.electionId = :electionId ORDER BY COALESCE(t.displayOrder, 2147483647), t.id", ElectionTask.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<ElectionTaskKey> getElectionTaskKeys(long electionId) {
		TypedQuery<ElectionTaskKey> q = em.createQuery("SELECT t.taskKey FROM ElectionTask t WHERE t.election.electionId = :electionId", ElectionTaskKey.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public ElectionTask getElectionTask(long electionTaskId) {
		return em.find(ElectionTask.class, electionTaskId);
	}

	public ElectionTask getElectionTaskByKey(long electionId, ElectionTaskKey taskKey) {
		TypedQuery<ElectionTask> q = em.createQuery("SELECT t FROM ElectionTask t WHERE t.election.electionId = :electionId AND t.taskKey = :taskKey", ElectionTask.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("taskKey", taskKey);
		List<ElectionTask> result = q.setMaxResults(1).getResultList();
		return result.isEmpty() ? null : result.get(0);
	}

	public long countTaskProgress(long electionTaskId) {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(tp.id) FROM CandidateElectionTaskProgress tp WHERE tp.electionTask.id = :electionTaskId", Long.class);
		q.setParameter("electionTaskId", electionTaskId);
		return q.getSingleResult();
	}

	public long countElectionTasks(long electionId) {
		TypedQuery<Long> q = em.createQuery("SELECT COUNT(t) FROM ElectionTask t WHERE t.election.electionId = :electionId", Long.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		Long value = q.getSingleResult();
		return value != null ? value.longValue() : 0L;
	}
}
