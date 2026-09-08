package net.lacnic.elections.dao;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;

public class SupportNominationDao {

	private EntityManager em;

	public SupportNominationDao(EntityManager em) {
		this.em = em;
	}

	public SupportNomination getSupportNominationByToken(String token) {
		TypedQuery<SupportNomination> q = em.createQuery("SELECT s FROM SupportNomination s WHERE s.token = :token", SupportNomination.class);
		q.setParameter("token", token);
		return q.getSingleResult();
	}

	public SupportNomination getSupportNomination(long supportNominationId) {
		TypedQuery<SupportNomination> q = em.createQuery(
				"SELECT s FROM SupportNomination s LEFT JOIN FETCH s.nomination LEFT JOIN FETCH s.nomination.election LEFT JOIN FETCH s.nomination.organization LEFT JOIN FETCH s.nomination.candidate LEFT JOIN FETCH s.supportingOrganization WHERE s.id = :supportNominationId",
				SupportNomination.class);
		q.setParameter("supportNominationId", supportNominationId);
		return q.getResultStream().findFirst().orElse(null);
	}

	public SupportNomination getSupportNominationForUpdate(long supportNominationId) {
		SupportNomination supportNomination = em.find(SupportNomination.class, supportNominationId, LockModeType.PESSIMISTIC_WRITE);
		if (supportNomination != null) {
			em.refresh(supportNomination, LockModeType.PESSIMISTIC_WRITE);
		}
		return supportNomination;
	}

	public List<SupportNomination> getCandidateSupportNominations(long candidateId) {
		TypedQuery<SupportNomination> q = em.createQuery(
				"SELECT s FROM SupportNomination s "
						+ "LEFT JOIN FETCH s.nomination "
						+ "LEFT JOIN FETCH s.nomination.election "
						+ "LEFT JOIN FETCH s.nomination.candidate "
						+ "LEFT JOIN FETCH s.supportingOrganization "
						+ "WHERE s.nomination.candidate.candidateId = :candidateId "
						+ "ORDER BY s.id",
				SupportNomination.class);
		q.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		return q.getResultList();
	}

	public List<SupportNomination> getElectionSupportNominations(long electionId) {
		TypedQuery<SupportNomination> q = em.createQuery(
				"SELECT s FROM SupportNomination s WHERE s.nomination.election.electionId = :electionId ORDER BY s.id",
				SupportNomination.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<SupportNomination> getElectionSupportNominationsForPublicElectionPage(long electionId) {
		TypedQuery<SupportNomination> q = em.createQuery(
				"SELECT s FROM SupportNomination s "
						+ "LEFT JOIN FETCH s.nomination "
						+ "LEFT JOIN FETCH s.supportingOrganization "
						+ "WHERE s.nomination.election.electionId = :electionId "
						+ "ORDER BY s.id",
				SupportNomination.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<SupportNomination> getElectionSupportNominationsBySupportingOrganizationId(long electionId, long supportingOrganizationId) {
		TypedQuery<SupportNomination> q = em.createQuery(
				"SELECT s FROM SupportNomination s "
						+ "WHERE s.nomination.election.electionId = :electionId "
						+ "AND s.supportingOrganization.id = :supportingOrganizationId "
						+ "ORDER BY s.id",
				SupportNomination.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.SUPPORTING_ORGANIZATION_ID, supportingOrganizationId);
		return q.getResultList();
	}

	public List<SupportNomination> getElectionSupportNominationsByNormalizedContactEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter) {
		if (electionId <= 0 || normalizedEmail == null || normalizedEmail.trim().isEmpty()) {
			return Collections.emptyList();
		}
		int safeMaxResults = maxResults > 0 ? maxResults : 10;
		String normalizedCountryCode = countryCodeFilter == null || countryCodeFilter.trim().isEmpty()
				? null
				: countryCodeFilter.trim().toUpperCase(java.util.Locale.ROOT);
		TypedQuery<SupportNomination> q = em.createQuery(
				"SELECT s FROM SupportNomination s LEFT JOIN s.supportingOrganization so "
						+ "WHERE s.nomination.election.electionId = :electionId "
						+ "AND LOWER(TRIM(s.supportingContactEmail)) = :email "
						+ "AND (s.supportingOrganization IS NULL OR so.deudor = false) "
						+ "AND (:countryCodeFilter IS NULL OR UPPER(TRIM(s.nomination.organization.country)) = :countryCodeFilter) "
						+ "ORDER BY s.id",
				SupportNomination.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("email", normalizedEmail.trim().toLowerCase(java.util.Locale.ROOT));
		q.setParameter("countryCodeFilter", normalizedCountryCode);
		q.setMaxResults(safeMaxResults);
		return q.getResultList();
	}

	public List<SupportNomination> getPendingSupportNominations() {
		TypedQuery<SupportNomination> q = em.createQuery(
				"SELECT s FROM SupportNomination s "
						+ "WHERE (s.supportStatus = :pendingStatus OR s.supportStatus IS NULL) "
						+ "ORDER BY s.id",
				SupportNomination.class);
		q.setParameter("pendingStatus", SupportStatus.PROPOSED);
		return q.getResultList();
	}

	public boolean existsOrganizationSupportRequestForNomination(long nominationId, long supportingOrganizationId) {
		TypedQuery<Long> q = em.createQuery(
				"SELECT COUNT(s) FROM SupportNomination s "
						+ "WHERE s.nomination.id = :nominationId "
						+ "AND s.supportingOrganization IS NOT NULL "
						+ "AND s.supportingOrganization.id = :supportingOrganizationId",
				Long.class);
		q.setParameter("nominationId", nominationId);
		q.setParameter(QueryParameterNames.SUPPORTING_ORGANIZATION_ID, supportingOrganizationId);
		Long result = q.getSingleResult();
		return result != null && result.longValue() > 0L;
	}

	public boolean existsOrganizationGrantedSupportInElection(long electionId, long supportingOrganizationId, Long excludedNominationId) {
		TypedQuery<Long> q = em.createQuery(
				"SELECT COUNT(s) FROM SupportNomination s "
						+ "WHERE s.nomination.election.electionId = :electionId "
						+ "AND s.supportingOrganization IS NOT NULL "
						+ "AND s.supportingOrganization.id = :supportingOrganizationId "
						+ "AND (s.supportStatus = :acceptedStatus OR s.supportStatus = :approvedStatus) "
						+ "AND (:excludedNominationId IS NULL OR s.nomination.id <> :excludedNominationId)",
				Long.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.SUPPORTING_ORGANIZATION_ID, supportingOrganizationId);
		q.setParameter("acceptedStatus", SupportStatus.ACCEPTED);
		q.setParameter("approvedStatus", SupportStatus.APPROVED);
		q.setParameter("excludedNominationId", excludedNominationId);
		Long result = q.getSingleResult();
		return result != null && result.longValue() > 0L;
	}

	public boolean existsUserSupportRequestForNominationByEmail(long nominationId, String normalizedEmail) {
		TypedQuery<Long> q = em.createQuery(
				"SELECT COUNT(s) FROM SupportNomination s "
						+ "WHERE s.nomination.id = :nominationId "
						+ "AND s.supportingOrganization IS NULL "
						+ "AND LOWER(s.supportingContactEmail) = :normalizedEmail",
				Long.class);
		q.setParameter("nominationId", nominationId);
		q.setParameter("normalizedEmail", normalizedEmail);
		Long result = q.getSingleResult();
		return result != null && result.longValue() > 0L;
	}

}
