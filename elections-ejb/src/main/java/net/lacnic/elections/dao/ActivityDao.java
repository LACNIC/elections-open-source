package net.lacnic.elections.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.lacnic.elections.data.TablesReportData;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.ActivityReportTable;
import net.lacnic.elections.domain.ElectionReportTable;


public class ActivityDao {

	private EntityManager em;

	public ActivityDao(EntityManager em) {
		this.em = em;
	}

	public List<Activity> getActivitiesAll() {
		TypedQuery<Activity> q = em.createQuery("SELECT a FROM Activity a ORDER BY a.activityId DESC", Activity.class);
		return q.getResultList();
	}

	public List<Activity> getElectionActivities(long electionId) {
		TypedQuery<Activity> q = em.createQuery("SELECT a FROM Activity a WHERE a.electionId = :electionId ORDER BY a.activityId DESC", Activity.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public List<Activity> getAdminActivities(String userName) {
		TypedQuery<Activity> q = em.createQuery("SELECT a FROM Activity a WHERE a.userName = :userName ORDER BY a.timestamp", Activity.class);
		q.setParameter("userName", userName);
		return q.getResultList();
	}
	
	public List<TablesReportData> getActivitiesData() {
		Query q = em.createQuery("SELECT a.activityId, a.description FROM Activity a ORDER BY a.activityId ");
		@SuppressWarnings("unchecked")
		List<Object[]> result = q.getResultList();

		List<TablesReportData> resultList = new ArrayList<>();
		for (int i = 0; i < result.size(); i++) {
			TablesReportData trd = new TablesReportData((long)result.get(i)[0], result.get(i)[1].toString());
			resultList.add(trd);
		}
		return resultList;	
	}
	
	public ActivityReportTable getActivityTableReport(Long id) {
		TypedQuery<ActivityReportTable> q = em.createQuery("SELECT a FROM ActivityReportTable a WHERE a.activityId = :activityId", ActivityReportTable.class);
		q.setParameter("activityId", id);
		return q.getSingleResult();
	
	}

}
