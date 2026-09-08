package net.lacnic.elections.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;

public class NominationDao {

	private EntityManager em;

	public NominationDao(EntityManager em) {
		this.em = em;
	}

	public Nomination getNominationByAcceptNominationToken(String token) {
		TypedQuery<Nomination> q = em.createQuery(
				"SELECT DISTINCT n FROM Nomination n LEFT JOIN FETCH n.candidate LEFT JOIN FETCH n.candidate.taskProgress LEFT JOIN FETCH n.candidate.taskProgress.electionTask LEFT JOIN FETCH n.candidate.taskProgress.electionTask.electionCalendar WHERE n.acceptNominationToken = :token",
				Nomination.class);
		q.setParameter("token", token);
		return q.getResultStream().findFirst().orElse(null);
	}

	public Nomination getNominationByAcceptNominationTokenForUpdate(String token) {
		TypedQuery<Nomination> q = em.createQuery(
				"SELECT n FROM Nomination n "
						+ "LEFT JOIN FETCH n.election "
						+ "LEFT JOIN FETCH n.organization "
						+ "LEFT JOIN FETCH n.candidate "
						+ "WHERE n.acceptNominationToken = :token",
				Nomination.class);
		q.setParameter("token", token);
		q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		return q.getResultStream().findFirst().orElse(null);
	}

	public Nomination getNomination(long nominationId) {
		TypedQuery<Nomination> q = em.createQuery(
				"SELECT DISTINCT n FROM Nomination n LEFT JOIN FETCH n.election LEFT JOIN FETCH n.organization LEFT JOIN FETCH n.candidate WHERE n.id = :nominationId",
				Nomination.class);
		q.setParameter("nominationId", nominationId);
		return q.getResultStream().findFirst().orElse(null);
	}

	public Nomination getNominationByCandidateId(long candidateId) {
		TypedQuery<Nomination> q = em.createQuery(
				"SELECT DISTINCT n FROM Nomination n LEFT JOIN FETCH n.election LEFT JOIN FETCH n.organization LEFT JOIN FETCH n.candidate WHERE n.candidate.candidateId = :candidateId",
				Nomination.class);
		q.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		return q.getResultStream().findFirst().orElse(null);
	}

	public List<Nomination> getPendingNominationsWithoutCandidate() {
		TypedQuery<Nomination> q = em.createQuery(
				"SELECT n FROM Nomination n "
						+ "WHERE (n.status = :pendingStatus OR n.status IS NULL) "
						+ "AND n.candidate IS NULL "
						+ "ORDER BY n.id",
				Nomination.class);
		q.setParameter("pendingStatus", NominationStatus.PROPOSED);
		return q.getResultList();
	}

	public long countNominationsByElectionAndOrganizationId(long electionId, long organizationId) {
		TypedQuery<Long> q = em.createQuery(
				"SELECT COUNT(n) FROM Nomination n WHERE n.election.electionId = :electionId AND n.organization.id = :organizationId",
				Long.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("organizationId", organizationId);
		Long result = q.getSingleResult();
		return result != null ? result.longValue() : 0L;
	}

	public Nomination getLatestNominationByElectionAndOrganizationId(long electionId, long organizationId) {
		TypedQuery<Nomination> q = em.createQuery(
				"SELECT DISTINCT n FROM Nomination n "
						+ "LEFT JOIN FETCH n.organization "
						+ "LEFT JOIN FETCH n.candidate "
						+ "WHERE n.election.electionId = :electionId "
						+ "AND n.organization.id = :organizationId "
						+ "ORDER BY n.id DESC",
				Nomination.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("organizationId", organizationId);
		q.setMaxResults(1);
		return q.getResultStream().findFirst().orElse(null);
	}

	public boolean existsElectionNominationByOrganizationIdAndStatuses(long electionId, long organizationId, Collection<NominationStatus> statuses) {
		if (electionId <= 0 || organizationId <= 0 || statuses == null || statuses.isEmpty()) {
			return false;
		}
		TypedQuery<Long> q = em.createQuery(
				"SELECT COUNT(n) FROM Nomination n "
						+ "WHERE n.election.electionId = :electionId "
						+ "AND n.organization.id = :organizationId "
						+ "AND n.status IN :statuses",
				Long.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("organizationId", organizationId);
		q.setParameter("statuses", statuses);
		Long result = q.getSingleResult();
		return result != null && result.longValue() > 0L;
	}

	public List<Nomination> getElectionNominationsByNormalizedNominationEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter) {
		if (electionId <= 0 || normalizedEmail == null || normalizedEmail.trim().isEmpty()) {
			return Collections.emptyList();
		}
		int safeMaxResults = maxResults > 0 ? maxResults : 10;
		String normalizedCountryCode = countryCodeFilter == null || countryCodeFilter.trim().isEmpty()
				? null
				: countryCodeFilter.trim().toUpperCase(java.util.Locale.ROOT);
		TypedQuery<Nomination> q = em.createQuery(
				"SELECT n FROM Nomination n WHERE n.election.electionId = :electionId "
						+ "AND LOWER(TRIM(n.nominationEmail)) = :email "
						+ "AND (:countryCodeFilter IS NULL OR UPPER(TRIM(n.organization.country)) = :countryCodeFilter) "
						+ "ORDER BY n.id",
				Nomination.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("email", normalizedEmail.trim().toLowerCase(java.util.Locale.ROOT));
		q.setParameter("countryCodeFilter", normalizedCountryCode);
		q.setMaxResults(safeMaxResults);
		return q.getResultList();
	}

	public boolean existsElectionNominationByNormalizedNominationEmailAndStatuses(long electionId, String normalizedEmail, Collection<NominationStatus> statuses) {
		if (electionId <= 0 || normalizedEmail == null || normalizedEmail.trim().isEmpty() || statuses == null || statuses.isEmpty()) {
			return false;
		}
		TypedQuery<Long> q = em.createQuery(
				"SELECT COUNT(n) FROM Nomination n "
						+ "WHERE n.election.electionId = :electionId "
						+ "AND LOWER(TRIM(n.nominationEmail)) = :email "
						+ "AND n.status IN :statuses",
				Long.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("email", normalizedEmail.trim().toLowerCase(java.util.Locale.ROOT));
		q.setParameter("statuses", statuses);
		Long result = q.getSingleResult();
		return result != null && result.longValue() > 0L;
	}

	public boolean existsElectionNominationByNormalizedMembershipContactEmailAndStatuses(long electionId, String normalizedEmail, Collection<NominationStatus> statuses) {
		if (electionId <= 0 || normalizedEmail == null || normalizedEmail.trim().isEmpty() || statuses == null || statuses.isEmpty()) {
			return false;
		}
		TypedQuery<Long> q = em.createQuery(
				"SELECT COUNT(n) FROM Nomination n "
						+ "JOIN n.organization o "
						+ "WHERE n.election.electionId = :electionId "
						+ "AND LOWER(TRIM(o.membershipContactEmail)) = :email "
						+ "AND n.status IN :statuses",
				Long.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("email", normalizedEmail.trim().toLowerCase(java.util.Locale.ROOT));
		q.setParameter("statuses", statuses);
		Long result = q.getSingleResult();
		return result != null && result.longValue() > 0L;
	}

	public List<Nomination> getElectionNominationsForPublicElectionPage(long electionId) {
		TypedQuery<Nomination> q = em.createQuery(
				"SELECT DISTINCT n FROM Nomination n "
						+ "LEFT JOIN FETCH n.organization "
						+ "LEFT JOIN FETCH n.candidate "
						+ "LEFT JOIN FETCH n.candidate.taskProgress "
						+ "LEFT JOIN FETCH n.candidate.taskProgress.electionTask "
						+ "WHERE n.election.electionId = :electionId "
						+ "ORDER BY n.id",
				Nomination.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

}
