package net.lacnic.elections.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.pre.Organization;

public class OrganizationDao {

	private EntityManager em;

	public OrganizationDao(EntityManager em) {
		this.em = em;
	}

	public Organization getOrganizationByDoNominationToken(String token) {
		TypedQuery<Organization> q = em.createQuery("SELECT o FROM Organization o WHERE o.doNominationToken = :token", Organization.class);
		q.setParameter("token", token);
		List<Organization> organizations = q.getResultList();
		if (organizations.isEmpty())
			return null;
		else
			return organizations.get(0);
	}

	public List<Organization> getAllByElectionId(long electionId) {
		return em.createQuery("SELECT o FROM Organization o WHERE o.election.electionId = :electionId ORDER BY o.orgId", Organization.class).setParameter(QueryParameterNames.ELECTION_ID, electionId).getResultList();
	}

	public Organization getByOrgId(String orgId) {
		List<Organization> organizations = em.createQuery("SELECT o FROM Organization o WHERE o.orgId = :orgId", Organization.class).setParameter("orgId", orgId).setMaxResults(1).getResultList();
		return organizations.isEmpty() ? null : organizations.get(0);
	}

	public Organization getOrganizationByElectionAndOrgId(long electionId, String orgId) {
		TypedQuery<Organization> q = em.createQuery("SELECT o FROM Organization o WHERE o.election.electionId = :electionId AND TRIM(UPPER(o.orgId)) = :orgId", Organization.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("orgId", orgId == null ? "" : orgId.trim().toUpperCase());
		List<Organization> organizations = q.getResultList();
		if (organizations.isEmpty()) {
			return null;
		}
		return organizations.get(0);
	}

	public List<Organization> getOrganizationByElection(long electionId) {
		TypedQuery<Organization> q = em.createQuery("SELECT o FROM Organization o WHERE o.election.electionId = :electionId ORDER by o.orgId DESC", Organization.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public Organization getOrganizationByElectionAndCnpj(long electionId, String cnpj) {
		TypedQuery<Organization> q = em.createQuery("SELECT o FROM Organization o WHERE o.election.electionId = :electionId AND UPPER(o.cnpj) = :cnpj", Organization.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("cnpj", cnpj == null ? "" : cnpj.trim().toUpperCase());
		List<Organization> organizations = q.getResultList();
		if (organizations.isEmpty()) {
			return null;
		}
		return organizations.get(0);
	}

	public Organization getOrganizationByElectionAndAsn(long electionId, String asn) {
		// Keep the previous exact-match behavior first.
		TypedQuery<Organization> exactQuery = em.createQuery(
				"SELECT o FROM Organization o WHERE o.election.electionId = :electionId AND UPPER(o.asn) = :asn",
				Organization.class);
		exactQuery.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		exactQuery.setParameter("asn", asn == null ? "" : asn.trim().toUpperCase(Locale.ROOT));
		List<Organization> exactMatches = exactQuery.getResultList();
		if (!exactMatches.isEmpty()) {
			return exactMatches.get(0);
		}

		// Fallback: allow searching a single ASN inside comma-separated ASN lists.
		String normalizedSearchAsn = normalizeAsnToken(asn);
		if (normalizedSearchAsn == null) {
			return null;
		}

		for (Organization organization : getAllByElectionId(electionId)) {
			if (containsAsnToken(organization != null ? organization.getAsn() : null, normalizedSearchAsn)) {
				return organization;
			}
		}
		return null;
	}

	private boolean containsAsnToken(String storedAsnValue, String normalizedSearchAsn) {
		if (normalizedSearchAsn == null) {
			return false;
		}
		for (String token : splitAsnTokens(storedAsnValue)) {
			if (normalizedSearchAsn.equals(normalizeAsnToken(token))) {
				return true;
			}
		}
		return false;
	}

	private List<String> splitAsnTokens(String storedAsnValue) {
		if (storedAsnValue == null || storedAsnValue.trim().isEmpty()) {
			return Collections.emptyList();
		}
		List<String> tokens = new ArrayList<>();
		for (String token : storedAsnValue.split(",")) {
			String normalized = token != null ? token.trim() : null;
			if (normalized != null && !normalized.isEmpty()) {
				tokens.add(normalized);
			}
		}
		return tokens;
	}

	private String normalizeAsnToken(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim().toUpperCase(Locale.ROOT);
		if (normalized.isEmpty()) {
			return null;
		}
		// Accept both "AS12345" and "12345" as the same ASN.
		if (normalized.startsWith("AS")) {
			String suffix = normalized.substring(2).trim();
			if (!suffix.isEmpty()) {
				return suffix;
			}
		}
		return normalized;
	}

	public List<Organization> getNonDebtorOrganizationsByElectionId(long electionId) {
		TypedQuery<Organization> q = em.createQuery(
				"SELECT o FROM Organization o WHERE o.election.electionId = :electionId AND o.deudor = false ORDER BY o.orgId",
				Organization.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<Organization> getNonDebtorOrganizationsByCountry(long electionId, String countryCode) {
		TypedQuery<Organization> q = em.createQuery(
				"SELECT o FROM Organization o WHERE o.election.electionId = :electionId AND o.deudor = false AND UPPER(o.country) = :countryCode ORDER BY o.orgId",
				Organization.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY_CODE, countryCode == null ? "" : countryCode.trim().toUpperCase());
		return q.getResultList();
	}

	public List<Organization> getNonDebtorOrganizationsWithoutCountry(long electionId, String countryCode) {
		TypedQuery<Organization> q = em.createQuery(
				"SELECT o FROM Organization o WHERE o.election.electionId = :electionId AND o.deudor = false AND (o.country IS NULL OR UPPER(o.country) <> :countryCode) ORDER BY o.orgId",
				Organization.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY_CODE, countryCode == null ? "" : countryCode.trim().toUpperCase());
		return q.getResultList();
	}

	public List<Organization> getElectionOrganizationsByNormalizedMembershipContactEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter) {
		if (electionId <= 0 || normalizedEmail == null || normalizedEmail.trim().isEmpty()) {
			return Collections.emptyList();
		}
		int safeMaxResults = maxResults > 0 ? maxResults : 10;
		String normalizedCountryCode = countryCodeFilter == null || countryCodeFilter.trim().isEmpty()
				? null
				: countryCodeFilter.trim().toUpperCase(java.util.Locale.ROOT);
		TypedQuery<Organization> q = em.createQuery(
				"SELECT o FROM Organization o "
						+ "WHERE o.election.electionId = :electionId "
						+ "AND o.deudor = false "
						+ "AND LOWER(TRIM(o.membershipContactEmail)) = :email "
						+ "AND o.doNominationToken IS NOT NULL "
						+ "AND TRIM(o.doNominationToken) <> '' "
						+ "AND (:countryCodeFilter IS NULL OR UPPER(TRIM(o.country)) = :countryCodeFilter) "
						+ "ORDER BY o.id",
				Organization.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("email", normalizedEmail.trim().toLowerCase(java.util.Locale.ROOT));
		q.setParameter("countryCodeFilter", normalizedCountryCode);
		q.setMaxResults(safeMaxResults);
		return q.getResultList();
	}

	public List<Organization> getNonDebtorOrganizationsNotNominatedYet(long electionId) {
		TypedQuery<Organization> q = em.createQuery(
				"SELECT o FROM Organization o WHERE o.election.electionId = :electionId AND o.deudor = false AND NOT EXISTS (SELECT n.id FROM Nomination n WHERE n.organization.id = o.id) ORDER BY o.orgId",
				Organization.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<Organization> getNonDebtorOrganizationsNotNominatedYetByCountry(long electionId, String countryCode) {
		TypedQuery<Organization> q = em.createQuery(
				"SELECT o FROM Organization o WHERE o.election.electionId = :electionId AND o.deudor = false AND UPPER(o.country) = :countryCode AND NOT EXISTS (SELECT n.id FROM Nomination n WHERE n.organization.id = o.id) ORDER BY o.orgId",
				Organization.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY_CODE, countryCode == null ? "" : countryCode.trim().toUpperCase());
		return q.getResultList();
	}

	public List<Organization> getNonDebtorOrganizationsNotNominatedYetWithoutCountry(long electionId, String countryCode) {
		TypedQuery<Organization> q = em.createQuery(
				"SELECT o FROM Organization o WHERE o.election.electionId = :electionId AND o.deudor = false AND (o.country IS NULL OR UPPER(o.country) <> :countryCode) AND NOT EXISTS (SELECT n.id FROM Nomination n WHERE n.organization.id = o.id) ORDER BY o.orgId",
				Organization.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY_CODE, countryCode == null ? "" : countryCode.trim().toUpperCase());
		return q.getResultList();
	}
}
