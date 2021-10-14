package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.lacnic.elections.domain.Activity;


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
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getActivitiesAllIdAndDescription(int pageSize, int offset) {
		Query q = em.createQuery("SELECT a.activityId, a.description FROM Activity a ORDER BY a.activityId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

}
