package net.lacnic.elections.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateCountryLinkRow;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateWorkOrganizationRow;

public class PublicElectionPageDao {

	private final EntityManager em;

	public PublicElectionPageDao(EntityManager em) {
		this.em = em;
	}

	public List<PublicElectionCandidateCountryLinkRow> getElectionCandidateCountryLinkRowsForPublicElectionPage(long electionId) {
		TypedQuery<PublicElectionCandidateCountryLinkRow> q = em.createQuery(
				"SELECT NEW net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateCountryLinkRow(l.candidate.candidateId, l.primaryCountry, l.countryCode) "
						+ "FROM CandidateCountryLink l "
						+ "WHERE l.candidate.election.electionId = :electionId "
						+ "ORDER BY l.id",
				PublicElectionCandidateCountryLinkRow.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<PublicElectionCandidateWorkOrganizationRow> getElectionCandidateWorkOrganizationRowsForPublicElectionPage(long electionId) {
		TypedQuery<PublicElectionCandidateWorkOrganizationRow> q = em.createQuery(
				"SELECT NEW net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateWorkOrganizationRow(w.candidate.candidateId, w.organizationName) "
						+ "FROM CandidateWorkOrganization w "
						+ "WHERE w.candidate.election.electionId = :electionId "
						+ "ORDER BY w.id",
				PublicElectionCandidateWorkOrganizationRow.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}
}
