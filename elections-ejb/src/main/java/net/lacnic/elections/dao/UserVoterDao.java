package net.lacnic.elections.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.lacnic.elections.domain.UserVoter;


public class UserVoterDao {

	private EntityManager em;


	public UserVoterDao(EntityManager em) {
		this.em = em;
	}

	public UserVoter getUserVoter(long userVoterId) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.userVoterId = :userVoterId", UserVoter.class);
		q.setParameter("userVoterId", userVoterId);
		return q.getSingleResult();
	}

	public List<UserVoter> getElectionUserVoters(long electionId) {		
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId = :electionId ORDER BY u.userVoterId", UserVoter.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public UserVoter getElectionUserVoterByMail(long electionId, String mail) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId = :electionId AND u.mail = :mail", UserVoter.class);
		q.setParameter("electionId", electionId);
		q.setParameter("mail", mail);
		List<UserVoter> users = q.getResultList();

		if(users != null && !users.isEmpty()) {
			return users.get(0);
		} else {
			return null;
		}
	}

	public List<UserVoter> getElectionCensusByCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.country = :country ORDER BY u.userVoterId", UserVoter.class);
		q.setParameter("electionId", electionId);
		q.setParameter("country", country.toUpperCase());
		return q.getResultList();
	}

	public List<UserVoter> getElectionsUserVotersNotVotedYet(long electionId) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = FALSE ORDER BY u.userVoterId DESC", UserVoter.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public List<UserVoter> getElectionsUserVotersNotVotedYetByCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = FALSE AND u.country = :country ORDER BY u.userVoterId DESC", UserVoter.class);
		q.setParameter("electionId", electionId);
		q.setParameter("country", country.toUpperCase());
		return q.getResultList();
	}

	public List<UserVoter> getElectionsUserVotersVoted(long electionId) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = TRUE ORDER BY u.voteDate ASC", UserVoter.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public List<UserVoter> getElectionsUserVotersVotedByCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = TRUE AND u.country = :country ORDER BY u.voteDate ASC", UserVoter.class);
		q.setParameter("electionId", electionId);
		q.setParameter("country", country.toUpperCase());
		return q.getResultList();
	}


	public List<UserVoter> getJointElectionUserVotersNotVotedYet(long electionId) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u, JointElection p"
				+ " WHERE (p.idElectionA = :electionId OR p.idElectionB = :electionId)"
				+ " AND (u.election.electionId = p.idElectionA OR u.election.electionId = p.idElectionB)"
				+ " AND u.voted = FALSE ORDER BY u.voteDate ASC", UserVoter.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public List<UserVoter> getJointElectionUserVotersNotVotedYetByCountry(long electionId, String country) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u, JointElection p"
				+ " WHERE (p.idElectionA = :electionId OR p.idElectionB = :electionId)"
				+ " AND (u.election.electionId = p.idElectionA OR u.election.electionId = p.idElectionB)"
				+ " AND u.voted = FALSE AND u.country = :country ORDER BY u.voteDate ASC", UserVoter.class);
		q.setParameter("electionId", electionId);
		q.setParameter("country", country.toUpperCase());

		return q.getResultList();
	}


	public Long getElectionCensusSize(long electionId) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId =: electionId");
		q.setParameter("electionId", electionId);
		return (Long) q.getSingleResult();
	}

	public UserVoter getUserVoterByToken(String voteToken) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.voteToken = :voteToken", UserVoter.class);
		q.setParameter("voteToken", voteToken);
		return q.getSingleResult();
	}

	public Long getElectionUserVotersVotedAmount(long electionId) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId =:electionId AND u.voted = TRUE");
		q.setParameter("electionId", electionId);
		return (Long) q.getSingleResult();
	}

	public List<Integer> getElectionUserVotersDistinctVoteAmounts(Long electionId) {
		TypedQuery<Integer> q = em.createQuery("SELECT DISTINCT(u.voteAmount) FROM UserVoter u WHERE u.election.electionId = :electionId ORDER BY u.voteAmount", Integer.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public Long getElectionUserVotersAmountByVoteAmount(long electionId, Integer voteAmount) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId = :electionId AND u.voteAmount = :voteAmount");
		q.setParameter("electionId", electionId);
		q.setParameter("voteAmount", voteAmount);
		return (Long) q.getSingleResult();
	}

	public Long getElectionUserVotersAmountByVoteAmountAndVoted(long electionId, Integer voteAmount) {
		Query q = em.createQuery("SELECT COUNT(u.userVoterId) FROM UserVoter u WHERE u.election.electionId = :electionId AND u.voteAmount = :voteAmount AND u.voted = TRUE");
		q.setParameter("electionId", electionId);
		q.setParameter("voteAmount", voteAmount);
		return (Long) q.getSingleResult();
	}

	public void deleteElectionCensus(long electionId) {
		Query q = em.createQuery("DELETE FROM UserVoter u WHERE u.election.electionId = :electionId");
		q.setParameter("electionId", electionId);
		q.executeUpdate();
	}

	public UserVoter getElectionUserVotersByOrganization(String orgID, Long electionId) {
		TypedQuery<UserVoter> q = em.createQuery("SELECT u FROM UserVoter u WHERE u.orgID = :orgID AND u.election.electionId = :electionId", UserVoter.class);
		q.setParameter("electionId", electionId);
		q.setParameter("orgID", orgID);
		List<UserVoter> users = q.getResultList();
		if (!users.isEmpty())
			return users.get(0);
		else
			return null;
	}

	public boolean electionsCensusEqual(long idElectionA, long idElectionB) {
		TypedQuery<String> q1 = em.createQuery("SELECT u.mail FROM UserVoter u WHERE u.election.electionId = :electionId", String.class);
		q1.setParameter("electionId", idElectionA);
		List<String> census1 = q1.getResultList();

		TypedQuery<String> q2 = em.createQuery("SELECT u.mail FROM UserVoter u WHERE u.election.electionId = :electionId", String.class);
		q2.setParameter("electionId", idElectionB);
		List<String> census2 = q2.getResultList();

		Collections.sort(census1);
		Collections.sort(census2);

		return census1.equals(census2);
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getUserVotersAllIdAndName() {
		Query q = em.createQuery("SELECT u.userVoterId, u.name FROM UserVoter u ORDER BY u.userVoterId");
		return q.getResultList();
	}

}