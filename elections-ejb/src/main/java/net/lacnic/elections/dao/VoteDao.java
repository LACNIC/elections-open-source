package net.lacnic.elections.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.domain.services.publicelection.PublicElectionVoteCountRow;

public class VoteDao {

	private EntityManager em;

	public VoteDao(EntityManager em) {
		this.em = em;
	}

	public List<Vote> getElectionVotes(long electionId) {
		TypedQuery<Vote> q = em.createQuery("SELECT v FROM Vote v WHERE v.election.electionId = :electionId ORDER BY v.voteId DESC", Vote.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<PublicElectionVoteCountRow> getElectionVoteCountRowsForPublicElectionPage(long electionId) {
		TypedQuery<PublicElectionVoteCountRow> q = em.createQuery(
				"SELECT NEW net.lacnic.elections.domain.services.publicelection.PublicElectionVoteCountRow(v.candidate.candidateId, COUNT(v.voteId)) "
						+ "FROM Vote v "
						+ "WHERE v.election.electionId = :electionId "
						+ "GROUP BY v.candidate.candidateId "
						+ "ORDER BY COUNT(v.voteId) DESC, v.candidate.candidateId ASC",
				PublicElectionVoteCountRow.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<Vote> getElectionUserVoterVotes(long userVoterId, long electionId) {
		TypedQuery<Vote> q = em.createQuery("SELECT v FROM Vote v WHERE v.userVoter.userVoterId = :userVoterId AND v.election.electionId = :electionId", Vote.class);
		q.setParameter("userVoterId", userVoterId);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<Vote> getCandidateVotes(long candidateId) {
		TypedQuery<Vote> q = em.createQuery("SELECT v FROM Vote v WHERE v.candidate.candidateId = :candidateId ORDER BY v.voteId DESC", Vote.class);
		q.setParameter(QueryParameterNames.CANDIDATE_ID, candidateId);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getElectionVotesCandidateAndCode(long electionId) {
		Query q = em.createQuery("SELECT v.candidate.name, v.code FROM Vote v WHERE v.election.electionId = :electionId ORDER BY v.code");
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public long getElectionVotesAmount(long electionId) {
		return getElectionVotes(electionId).size();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getElectionVotesCandidateForUserVoter(long userVoterId, long electionId) {
		Query q = em.createQuery("SELECT v.candidate.name, v.code, v.candidate.pictureInfo FROM Vote v WHERE v.userVoter.userVoterId = :userVoterId AND v.election.electionId = :electionId");
		q.setParameter("userVoterId", userVoterId);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public void deleteElectionVotes(long electionId) {
		Query q = em.createQuery("DELETE FROM Vote v WHERE v.election.electionId = :electionId");
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getVotesAllIdAndDate(int pageSize, int offset) {
		Query q = em.createQuery("SELECT v.voteId, v.candidate.candidateId FROM Vote v ORDER BY v.voteId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

	public Vote getVote(long voteId) {
		TypedQuery<Vote> q = em.createQuery("SELECT v FROM Vote v WHERE v.voteId = :voteId", Vote.class);
		q.setParameter("voteId", voteId);
		return q.getSingleResult();
	}

}
