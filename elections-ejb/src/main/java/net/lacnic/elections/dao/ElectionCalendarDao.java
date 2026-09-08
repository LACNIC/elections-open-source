package net.lacnic.elections.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;

public class ElectionCalendarDao {

	private final EntityManager em;

	public ElectionCalendarDao(EntityManager em) {
		this.em = em;
	}

	public List<ElectionCalendar> getElectionCalendars(long electionId) {
		TypedQuery<ElectionCalendar> q = em.createQuery("SELECT c FROM ElectionCalendar c WHERE c.election.electionId = :electionId ORDER BY c.id", ElectionCalendar.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public ElectionCalendar getElectionCalendar(long electionCalendarId) {
		return em.find(ElectionCalendar.class, electionCalendarId);
	}

	public ElectionCalendar getElectionCalendar(long electionId, long electionCalendarId) {
		TypedQuery<ElectionCalendar> q = em.createQuery("SELECT c FROM ElectionCalendar c WHERE c.election.electionId = :electionId AND c.id = :electionCalendarId", ElectionCalendar.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("electionCalendarId", electionCalendarId);
		List<ElectionCalendar> result = q.setMaxResults(1).getResultList();
		return result.isEmpty() ? null : result.get(0);
	}

	public ElectionCalendar getElectionCalendarByKey(long electionId, ElectionCalendarKey calendarKey) {
		TypedQuery<ElectionCalendar> q = em.createQuery(
				"SELECT c FROM ElectionCalendar c WHERE c.election.electionId = :electionId AND c.calendarKey = :calendarKey ORDER BY c.id",
				ElectionCalendar.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.CALENDAR_KEY, calendarKey);
		List<ElectionCalendar> result = q.setMaxResults(1).getResultList();
		return result.isEmpty() ? null : result.get(0);
	}

	public Date getCalendarStartDate(long electionId, ElectionCalendarKey calendarKey) {
		ElectionCalendar calendar = getElectionCalendarByKey(electionId, calendarKey);
		return calendar != null ? calendar.getStartDate() : null;
	}

	public Date getCalendarEndDate(long electionId, ElectionCalendarKey calendarKey) {
		ElectionCalendar calendar = getElectionCalendarByKey(electionId, calendarKey);
		if (calendar == null) {
			return null;
		}
		return calendar.getEndDate() != null ? calendar.getEndDate() : calendar.getStartDate();
	}

	public Map<Long, Date[]> getCalendarWindowByElectionIds(Set<Long> electionIds, ElectionCalendarKey calendarKey) {
		Map<Long, Date[]> windowsByElection = new HashMap<>();
		if (electionIds == null || electionIds.isEmpty() || calendarKey == null) {
			return windowsByElection;
		}

		TypedQuery<Object[]> q = em.createQuery(
				"SELECT c.election.electionId, c.startDate, c.endDate "
						+ "FROM ElectionCalendar c "
						+ "WHERE c.calendarKey = :calendarKey "
						+ "AND c.election.electionId IN :electionIds "
						+ "ORDER BY c.id",
				Object[].class);
		q.setParameter(QueryParameterNames.CALENDAR_KEY, calendarKey);
		q.setParameter(QueryParameterNames.ELECTION_IDS, electionIds);

		for (Object[] row : q.getResultList()) {
			Long electionId = (Long) row[0];
			if (electionId == null || windowsByElection.containsKey(electionId)) {
				continue;
			}
			Date startDate = (Date) row[1];
			Date endDate = (Date) row[2];
			windowsByElection.put(electionId, new Date[] { startDate, endDate != null ? endDate : startDate });
		}
		return windowsByElection;
	}

	public List<ElectionCalendar> getActiveCalendarsForOpenElectionsByKey(ElectionCalendarKey calendarKey, Date referenceDate) {
		TypedQuery<ElectionCalendar> q = em.createQuery(
				"SELECT c FROM ElectionCalendar c JOIN FETCH c.election "
						+ "WHERE c.calendarKey = :calendarKey "
						+ "AND c.startDate IS NOT NULL "
						+ "AND c.endDate IS NOT NULL "
						+ "AND c.startDate <= :referenceDate "
						+ "AND c.endDate > :referenceDate "
						+ "AND c.election.closed = false",
				ElectionCalendar.class);
		q.setParameter(QueryParameterNames.CALENDAR_KEY, calendarKey);
		q.setParameter("referenceDate", referenceDate);
		return q.getResultList();
	}

	public List<ElectionCalendar> getCalendarsForOpenElectionsByKey(ElectionCalendarKey calendarKey) {
		TypedQuery<ElectionCalendar> q = em.createQuery(
				"SELECT c FROM ElectionCalendar c JOIN FETCH c.election "
						+ "WHERE c.calendarKey = :calendarKey "
						+ "AND c.election.closed = false",
				ElectionCalendar.class);
		q.setParameter(QueryParameterNames.CALENDAR_KEY, calendarKey);
		return q.getResultList();
	}

	public long countActiveCalendarsByKeys(long electionId, Set<ElectionCalendarKey> calendarKeys, Date referenceDate) {
		TypedQuery<Long> q = em.createQuery(
				"SELECT COUNT(c) FROM ElectionCalendar c "
						+ "WHERE c.election.electionId = :electionId "
						+ "AND c.calendarKey IN :calendarKeys "
						+ "AND c.startDate IS NOT NULL "
						+ "AND c.endDate IS NOT NULL "
						+ "AND c.startDate <= :referenceDate "
						+ "AND c.endDate >= :referenceDate",
				Long.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("calendarKeys", calendarKeys);
		q.setParameter("referenceDate", referenceDate);
		Long value = q.getSingleResult();
		return value != null ? value.longValue() : 0L;
	}
}
