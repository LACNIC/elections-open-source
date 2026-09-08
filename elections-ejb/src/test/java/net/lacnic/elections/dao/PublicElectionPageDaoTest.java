package net.lacnic.elections.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateCountryLinkRow;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateWorkOrganizationRow;
import org.junit.jupiter.api.Test;

class PublicElectionPageDaoTest {

	@Test
	void shouldGetCountryLinkRowsForPublicElectionPage() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<PublicElectionCandidateCountryLinkRow> query = mock(TypedQuery.class);
		PublicElectionCandidateCountryLinkRow row = mock(PublicElectionCandidateCountryLinkRow.class);
		when(em.createQuery(anyString(), eq(PublicElectionCandidateCountryLinkRow.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(row));

		PublicElectionPageDao dao = new PublicElectionPageDao(em);
		List<PublicElectionCandidateCountryLinkRow> result = dao
				.getElectionCandidateCountryLinkRowsForPublicElectionPage(5L);

		assertEquals(1, result.size());
		assertSame(row, result.get(0));
		verify(query).setParameter("electionId", 5L);
	}

	@Test
	void shouldGetWorkOrganizationRowsForPublicElectionPage() {
		EntityManager em = mock(EntityManager.class);
		@SuppressWarnings("unchecked")
		TypedQuery<PublicElectionCandidateWorkOrganizationRow> query = mock(TypedQuery.class);
		PublicElectionCandidateWorkOrganizationRow row = mock(PublicElectionCandidateWorkOrganizationRow.class);
		when(em.createQuery(anyString(), eq(PublicElectionCandidateWorkOrganizationRow.class))).thenReturn(query);
		when(query.setParameter(anyString(), anyLong())).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of(row));

		PublicElectionPageDao dao = new PublicElectionPageDao(em);
		List<PublicElectionCandidateWorkOrganizationRow> result = dao
				.getElectionCandidateWorkOrganizationRowsForPublicElectionPage(5L);

		assertEquals(1, result.size());
		assertSame(row, result.get(0));
		verify(query).setParameter("electionId", 5L);
	}
}
