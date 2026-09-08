package net.lacnic.elections.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import net.lacnic.elections.domain.pre.SyncRun;

public class SyncRunDao {
	private static final String QUERY_SYNC_RUN_BY_ELECTION_AND_TYPE = "SELECT r FROM SyncRun r WHERE r.election.electionId = :electionId AND r.syncType = :syncType ORDER BY r.syncAt DESC, r.id DESC";

	private final EntityManager em;

	public SyncRunDao(EntityManager em) {
		this.em = em;
	}

	public SyncRun getElectionLatestSyncRun(long electionId) {
		return em.createQuery(
				QUERY_SYNC_RUN_BY_ELECTION_AND_TYPE,
				SyncRun.class)
				.setParameter(QueryParameterNames.ELECTION_ID, electionId)
				.setParameter(QueryParameterNames.SYNC_TYPE, SyncRun.SYNC_TYPE_ORGANIZATIONS)
				.setMaxResults(1)
				.getResultStream()
				.findFirst()
				.orElse(null);
	}

	public List<SyncRun> getElectionSyncRuns(long electionId, int maxResults) {
		int safeMaxResults = maxResults <= 0 ? 100 : Math.min(maxResults, 500);
		return em.createQuery(
				QUERY_SYNC_RUN_BY_ELECTION_AND_TYPE,
				SyncRun.class)
				.setParameter(QueryParameterNames.ELECTION_ID, electionId)
				.setParameter(QueryParameterNames.SYNC_TYPE, SyncRun.SYNC_TYPE_ORGANIZATIONS)
				.setMaxResults(safeMaxResults)
				.getResultList();
	}

	public List<SyncRun> getElectionAutomaticCensusSyncRuns(long electionId, int maxResults) {
		int safeMaxResults = maxResults <= 0 ? 100 : Math.min(maxResults, 500);
		return em.createQuery(
				QUERY_SYNC_RUN_BY_ELECTION_AND_TYPE,
				SyncRun.class)
				.setParameter(QueryParameterNames.ELECTION_ID, electionId)
				.setParameter(QueryParameterNames.SYNC_TYPE, SyncRun.SYNC_TYPE_CENSUS)
				.setMaxResults(safeMaxResults)
				.getResultList();
	}

	public Map<Long, SyncRun> getLatestSyncRunsByElectionIds(List<Long> electionIds) {
		Map<Long, SyncRun> latestByElectionId = new HashMap<>();
		if (electionIds == null || electionIds.isEmpty()) {
			return latestByElectionId;
		}

		List<Long> sanitizedElectionIds = new ArrayList<>();
		for (Long electionId : electionIds) {
			if (electionId != null && electionId.longValue() > 0) {
				sanitizedElectionIds.add(electionId);
			}
		}
		if (sanitizedElectionIds.isEmpty()) {
			return latestByElectionId;
		}

		List<SyncRun> rows = em.createQuery(
				"SELECT r FROM SyncRun r JOIN FETCH r.election "
						+ "WHERE r.election.electionId IN :electionIds AND r.syncType = :syncType "
						+ "ORDER BY r.syncAt DESC, r.id DESC",
				SyncRun.class)
				.setParameter(QueryParameterNames.ELECTION_IDS, sanitizedElectionIds)
				.setParameter(QueryParameterNames.SYNC_TYPE, SyncRun.SYNC_TYPE_ORGANIZATIONS)
				.getResultList();
		for (SyncRun row : rows) {
			if (row == null || row.getElection() == null) {
				continue;
			}
			long electionId = row.getElection().getElectionId();
			if (!latestByElectionId.containsKey(electionId)) {
				latestByElectionId.put(electionId, row);
			}
		}
		return latestByElectionId;
	}

	public List<SyncRun> getSyncRuns(int maxResults) {
		int safeMaxResults = maxResults <= 0 ? 200 : Math.min(maxResults, 1000);
		return em.createQuery(
				"SELECT r FROM SyncRun r JOIN FETCH r.election WHERE r.syncType = :syncType ORDER BY r.syncAt DESC, r.id DESC",
				SyncRun.class)
				.setParameter(QueryParameterNames.SYNC_TYPE, SyncRun.SYNC_TYPE_ORGANIZATIONS)
				.setMaxResults(safeMaxResults)
				.getResultList();
	}
}
