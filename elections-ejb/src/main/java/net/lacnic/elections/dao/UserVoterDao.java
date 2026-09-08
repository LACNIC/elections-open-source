package net.lacnic.elections.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.UserVoterLite;
import org.hibernate.Session;

public class UserVoterDao {

	private EntityManager em;

	public UserVoterDao(EntityManager em) {
		this.em = em;
	}

	public UserVoter getUserVoter(long userVoterId) {
		return em.find(UserVoter.class, userVoterId);
	}

	public List<UserVoter> getElectionUserVoters(long electionId) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId = :electionId ORDER BY u.userVoterId", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<UserVoterLite> getElectionUserVotersLite(long electionId) {
		TypedQuery<UserVoterLite> q = em.createQuery("SELECT u FROM UserVoterLite u WHERE u.electionId = :electionId ORDER BY u.userVoterId", UserVoterLite.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<UserVoter> getElectionUserVotersByNormalizedEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter) {
		if (electionId <= 0 || normalizedEmail == null || normalizedEmail.trim().isEmpty()) {
			return Collections.emptyList();
		}
		int safeMaxResults = maxResults > 0 ? maxResults : 10;
		String normalizedCountryCode = countryCodeFilter == null || countryCodeFilter.trim().isEmpty()
				? null
				: countryCodeFilter.trim().toUpperCase(java.util.Locale.ROOT);
		TypedQuery<UserVoter> q = em.createQuery(
				"SELECT u FROM UserVoter u WHERE u.election.electionId = :electionId "
						+ "AND LOWER(TRIM(u.mail)) = :email "
						+ "AND (:countryCodeFilter IS NULL OR UPPER(TRIM(u.country)) = :countryCodeFilter) "
						+ "ORDER BY u.userVoterId",
				UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("email", normalizedEmail.trim().toLowerCase(java.util.Locale.ROOT));
		q.setParameter("countryCodeFilter", normalizedCountryCode);
		q.setMaxResults(safeMaxResults);
		return q.getResultList();
	}

	public List<UserVoter> getUserVotersByEmail(String email, int pageSize, int offset) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.mail = :email ORDER BY u.userVoterId", UserVoter.class);
		q.setParameter("email", email);
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

	public List<UserVoter> getElectionCensusByCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.country = :country ORDER BY u.userVoterId", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY, country.toUpperCase());
		return q.getResultList();
	}

	public List<UserVoter> getElectionCensusWithoutCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND (u.country IS NULL OR UPPER(TRIM(u.country)) <> :country) ORDER BY u.userVoterId", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY, country == null ? "" : country.trim().toUpperCase());
		return q.getResultList();
	}

	public List<UserVoter> getElectionsUserVotersNotVotedYet(long electionId) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = FALSE ORDER BY u.userVoterId DESC", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<UserVoter> getElectionsUserVotersNotVotedYetByCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = FALSE AND u.country = :country ORDER BY u.userVoterId DESC", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY, country.toUpperCase());
		return q.getResultList();
	}

	public List<UserVoter> getElectionsUserVotersNotVotedYetWithoutCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = FALSE AND (u.country IS NULL OR UPPER(TRIM(u.country)) <> :country) ORDER BY u.userVoterId DESC", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY, country == null ? "" : country.trim().toUpperCase());
		return q.getResultList();
	}

	public List<UserVoter> getElectionsUserVotersVoted(long electionId) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = TRUE ORDER BY u.voteDate ASC", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<UserVoter> getElectionsUserVotersVotedByCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = TRUE AND u.country = :country ORDER BY u.voteDate ASC", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY, country.toUpperCase());
		return q.getResultList();
	}

	public List<UserVoter> getElectionsUserVotersVotedWithoutCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = TRUE AND (u.country IS NULL OR UPPER(TRIM(u.country)) <> :country) ORDER BY u.voteDate ASC", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY, country == null ? "" : country.trim().toUpperCase());
		return q.getResultList();
	}

	public List<UserVoter> getJointElectionUserVotersNotVotedYet(long electionId) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u, JointElection p" + " WHERE (p.idElectionA = :electionId OR p.idElectionB = :electionId)" + " AND (u.election.electionId = p.idElectionA OR u.election.electionId = p.idElectionB)" + " AND u.voted = FALSE ORDER BY u.voteDate ASC", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<UserVoter> getJointElectionUserVotersNotVotedYetByCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u, JointElection p" + " WHERE (p.idElectionA = :electionId OR p.idElectionB = :electionId)" + " AND (u.election.electionId = p.idElectionA OR u.election.electionId = p.idElectionB)" + " AND u.voted = FALSE AND u.country = :country ORDER BY u.voteDate ASC", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY, country.toUpperCase());

		return q.getResultList();
	}

	public List<UserVoter> getJointElectionUserVotersNotVotedYetWithoutCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u, JointElection p" + " WHERE (p.idElectionA = :electionId OR p.idElectionB = :electionId)" + " AND (u.election.electionId = p.idElectionA OR u.election.electionId = p.idElectionB)" + " AND u.voted = FALSE AND (u.country IS NULL OR UPPER(TRIM(u.country)) <> :country) ORDER BY u.voteDate ASC", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.COUNTRY, country == null ? "" : country.trim().toUpperCase());

		return q.getResultList();
	}

	public Long getElectionCensusSize(long electionId) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId =: electionId");
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return (Long) q.getSingleResult();
	}

	public UserVoter getUserVoterByToken(String voteToken) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.voteToken = :voteToken", UserVoter.class);
		q.setParameter("voteToken", voteToken);
		return q.getSingleResult();
	}

	public Long getElectionUserVotersVotedAmount(long electionId) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = TRUE");
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return (Long) q.getSingleResult();
	}

	public List<Integer> getElectionUserVotersDistinctVoteAmounts(Long electionId) {
		TypedQuery<Integer> q = em.createQuery("SELECT DISTINCT(u.voteAmount) FROM UserVoter u WHERE u.election.electionId = :electionId ORDER BY u.voteAmount", Integer.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public Long getElectionUserVotersAmountByVoteAmount(long electionId, Integer voteAmount) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId = :electionId AND u.voteAmount = :voteAmount");
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("voteAmount", voteAmount);
		return (Long) q.getSingleResult();
	}

	public Long getElectionUserVotersAmountByVoteAmountAndVoted(long electionId, Integer voteAmount) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId = :electionId AND u.voteAmount = :voteAmount AND u.voted = TRUE");
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("voteAmount", voteAmount);
		return (Long) q.getSingleResult();
	}

	public void deleteElectionCensus(long electionId) {
		Query q = em.createQuery("DELETE FROM UserVoter u WHERE u.election.electionId = :electionId");
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.executeUpdate();
	}

	public UserVoter getElectionUserVoterByOrganization(String orgID, Long electionId) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.orgID = :orgID AND u.election.electionId = :electionId", UserVoter.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter("orgID", orgID);
		List<UserVoter> users = q.getResultList();
		if (!users.isEmpty())
			return users.get(0);
		else
			return null;
	}

	public boolean electionsCensusEqual(long idElectionA, long idElectionB) {
		TypedQuery<String> q1 = em.createQuery("SELECT u.orgID FROM UserVoter u WHERE u.election.electionId = :electionId", String.class);
		q1.setParameter(QueryParameterNames.ELECTION_ID, idElectionA);
		List<String> census1 = q1.getResultList();

		TypedQuery<String> q2 = em.createQuery("SELECT u.orgID FROM UserVoter u WHERE u.election.electionId = :electionId", String.class);
		q2.setParameter(QueryParameterNames.ELECTION_ID, idElectionB);
		List<String> census2 = q2.getResultList();

		if (!hasOrgIds(census1) || !hasOrgIds(census2)) {
			return false;
		}

		Collections.sort(census1);
		Collections.sort(census2);

		return census1.equals(census2);
	}

	private boolean hasOrgIds(List<String> orgIds) {
		if (orgIds == null) {
			return false;
		}
		for (String orgId : orgIds) {
			if (orgId == null || orgId.trim().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getUserVotersAllIdAndName(int pageSize, int offset) {
		Query q = em.createQuery("SELECT u.userVoterId, u.name FROM UserVoter u ORDER BY u.userVoterId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

	public List<UserVoter> getUserVotersByOrganization(String orgID, int pageSize, int offset) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.orgID = :orgID ORDER BY u.orgID, u.userVoterId", UserVoter.class);
		q.setParameter("orgID", orgID);
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

	public int deleteUserVotersByIdsBatch(List<Long> userVoterIds) {
		if (userVoterIds == null || userVoterIds.isEmpty()) {
			return 0;
		}
		final int[] deletedRows = { 0 };
		em.unwrap(Session.class).doWork(connection -> {
			try (PreparedStatement statement = connection.prepareStatement("DELETE FROM uservoter WHERE uservoter_id = ?")) {
				for (Long userVoterId : userVoterIds) {
					if (userVoterId == null || userVoterId.longValue() <= 0) {
						continue;
					}
					statement.setLong(1, userVoterId.longValue());
					statement.addBatch();
				}
				int[] batchResult = statement.executeBatch();
				int affectedRows = 0;
				for (int rowCount : batchResult) {
					if (rowCount > 0) {
						affectedRows += rowCount;
					} else if (rowCount == PreparedStatement.SUCCESS_NO_INFO) {
						affectedRows++;
					}
				}
				deletedRows[0] = affectedRows;
			} catch (SQLException e) {
				throw new IllegalStateException("Unable to batch-delete user voters", e);
			}
		});
		return deletedRows[0];
	}

}
