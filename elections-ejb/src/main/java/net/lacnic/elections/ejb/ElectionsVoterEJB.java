package net.lacnic.elections.ejb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.lacnic.elections.data.ElectionsResultsData;
import net.lacnic.elections.domain.*;
import net.lacnic.elections.exception.OperationNotPermittedException;

import javax.persistence.OptimisticLockException;

public interface ElectionsVoterEJB {

	public List<Object[]> getElectionVotesCandidateAndCode(long electionId);

	public void saveFailedAccessIp(String remoteAddress);

	public List<Candidate> getElectionCandidatesOrdered(long electionId) throws Exception;

	public List<Candidate> getElectionCandidates(long electionId) throws Exception;

	public long getCandidateVotesAmount(long candidateId) throws Exception;

	public long getElectionUserVotersVotedAmount(long electionId) throws Exception;

	public boolean userAlreadyVoted(long userVoterId) throws Exception;

	public long getElectionVotesAmount(long electionId) throws Exception;

	public byte[] getAquaItAudit();

	public byte[] getV1Requirements();

	public File getElectionRolesRevisionDocument(String filePath);

	public ElectionsResultsData getElectionsResultsData(long electionId) throws Exception;

	public UserVoter verifyUserVoterAccess(String voteToken);

	public Auditor verifyAuditorResultAccess(String resultToken);

	public Election verifyResultAccess(String resultToken);

	public void vote(List<Candidate> candidates, UserVoter userVoter, String ip) throws OperationNotPermittedException;

	public void confirmAuditorAgreedConformity(long auditorId);

	public List<Object[]> getElectionVotesCandidateForUserVoter(long userVoterId, long electionId);

	public List<Object> getElectionVoteEvolutionData(long electionId);

	public void enableAuditorElectionRevision(long auditorId, String ip);

	public Election getElection(long electionId);

	public boolean electionIsSimple(long electionId);

	public UserVoter[] verifyUserVoterAccessJointElection(String voteToken);

	public JointElection getJointElection(long electionId);

	ArrayList<Vote> doVotes(List<Candidate> candidates, UserVoter userVoter, String ip) throws OperationNotPermittedException;
}
