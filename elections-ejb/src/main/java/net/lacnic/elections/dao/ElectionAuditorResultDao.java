package net.lacnic.elections.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.ElectionAuditorResult;

public class ElectionAuditorResultDao {

	private final EntityManager em;

	public ElectionAuditorResultDao(EntityManager em) {
		this.em = em;
	}

	public ElectionAuditorResult getElectionAuditorResult(long electionId) {
		TypedQuery<ElectionAuditorResult> query = em.createQuery(
				"SELECT r FROM ElectionAuditorResult r WHERE r.electionId = :electionId",
				ElectionAuditorResult.class);
		query.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		List<ElectionAuditorResult> results = query.setMaxResults(1).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}
}
