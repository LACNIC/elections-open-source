package net.lacnic.elections.dao;

import java.util.Date;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.ActivityType;

public class ActivityDao {

	private EntityManager em;

	public ActivityDao(EntityManager em) {
		this.em = em;
	}

	public List<Activity> getActivitiesAll() {
		TypedQuery<Activity> q = em.createQuery("SELECT a FROM Activity a ORDER BY a.timestamp DESC", Activity.class);
		return q.getResultList();
	}

	public Activity getActivity(long activityId) {
		TypedQuery<Activity> q = em.createQuery("SELECT a FROM Activity a WHERE a.activityId = :activityId", Activity.class);
		q.setParameter("activityId", activityId);
		return q.getSingleResult();
	}

	public List<Activity> getElectionActivities(long electionId) {
		TypedQuery<Activity> q = em.createQuery("SELECT a FROM Activity a WHERE a.electionId = :electionId ORDER BY a.timestamp DESC", Activity.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getActivitiesAllIdAndDescription(int pageSize, int offset) {
		Query q = em.createQuery("SELECT a.activityId, a.description FROM Activity a ORDER BY a.activityId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

	public long countCandidateTextImprovementAttemptsSince(long candidateId, Date sinceDate) {
		TypedQuery<Long> query = em.createQuery(
				"SELECT COUNT(a) FROM Activity a "
						+ "WHERE a.activityType = :activityType "
						+ "AND a.timestamp >= :sinceDate "
						+ "AND a.description LIKE :descriptionMarker",
				Long.class);
		query.setParameter("activityType", ActivityType.EDIT_CANDIDATES);
		query.setParameter("sinceDate", sinceDate);
		query.setParameter("descriptionMarker", "%candidateId=" + candidateId + ", aiTextImprovementAttempt=true%");
		Long result = query.getSingleResult();
		return result == null ? 0L : result.longValue();
	}

}
