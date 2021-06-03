package net.lacnic.elections.ejb;

import java.io.File;
import java.util.List;

import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Commissioner;
import net.lacnic.elections.domain.Customization;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.exception.CensusValidationException;


public interface ElectionsManagerEJB {

	public UserAdmin userAdminLogin(String userAdminId, String password, String ip);

	public List<UserAdmin> getUserAdminsAll();

	public UserAdmin getUserAdmin(String userAdminId);

	public boolean isValidCaptchaResponse(String reCaptchaResponse);

	public boolean isProd();

	public Election getElection(long electionId);

	public List<Candidate> getElectionCandidatesOrdered(long electionId);

	public void removeElection(long electionId, String electionTitle, String userAdminId, String ip) throws Exception;

	public List<Auditor> getElectionAuditors(long electionId) throws Exception;

	public List<Election> getElectionsAllOrderCreationDate();

	public UserVoter getUserVoter(long userVoterId);

	public Election updateElection(Election election, String userAdminId, String ip) throws Exception;

	public String getResultsLink(Election election) throws Exception;

	public void setResultsLinkStatus(Long electionId, Boolean status, String userAdminId, String ip);

	public void setAuditLinkStatus(Long electionId, Boolean status, String userAdminId, String ip);

	public List<Commissioner> getCommissionersAll();

	public void removeUserVoter(UserVoter userVoter, String electionTitle, String userAdminId, String ip);

	public List<Activity> getActivitiesAll();

	public List<Activity> getElectionActivities(long electionId);

	public List<UserVoter> getElectionUserVoters(long electionId);

	public void updateUserVoterToken(long userVoterId, String name, String electionTitle, String userAdminId, String ip);

	public void removeUserAdmin(String userAdminToDeleteId, String userAdminId, String ip);

	public void updateElectionCensus(long electionId, byte[] content, String userAdminId, String ip) throws CensusValidationException, Exception;

	public List<Election> getElectionsLightThisYear();

	public boolean addUserVoter(long electionId, UserVoter userVoter, String userAdminId, String ip) throws CensusValidationException;

	public void editUserVoter(UserVoter userVoter, String userAdminId, String ip) throws CensusValidationException;

	public List<ElectionEmailTemplate> getElectionEmailTemplates(long electionId);

	public void modifyElectionEmailTemplate(ElectionEmailTemplate electionEmailTemplate);

	public void setVoteLinkStatus(Long electionId, Boolean status, String userAdminId, String ip);

	public List<ElectionEmailTemplate> getBaseEmailTemplates();

	public ElectionEmailTemplate getEmailTemplate(String templateType, long electionId);

	public void editUserAdmin(UserAdmin userAdmin, String email, Long authorizedElectionId, String userAdminId, String ip);

	public File exportCensus(long electionId);

	public void editAdminUserPassword(String userAdminToUpdateId, String password, String userAdminId, String ip);

	public boolean addUserAdmin(UserAdmin userAdmin, String userAdminId, String ip);

	public void addCandidate(long electionId, Candidate candidate, String userAdminId, String ip);

	public void removeCandidate(long candidateId, String userAdminId, String ip);

	public Candidate getCandidate(long candidateId);

	public void editCandidate(Candidate candidate, String userAdminId, String ip);

	public void addAuditor(long electionId, Auditor auditor, String electionTitle, String userAdminId, String ip);

	public void removeAuditor(long auditorId, String userAdminId, String ip);

	public Auditor getAuditor(long auditorId);

	public void editAuditor(Auditor auditor, String userAdminId, String ip);

	public void persistElectionAuditorsSet(long electionId, String electionTitle, String userAdminId, String ip);

	public List<Parameter> getParametersAll();

	public List<IpAccess> getAllDisabledIPs();

	public boolean addParameter(String key, String valor, String userAdminId, String ip);

	public void editParameter(Parameter parameter, String userAdminId, String ip);

	public void removeParameter(String key, String userAdminId, String ip);

	public List getRecipientsByRecipientType(ElectionEmailTemplate t) throws Exception;

	public Integer createMissingEmailTemplates();

	public void queueMassiveSending(List users, ElectionEmailTemplate electionEmailTemplate);

	public void fixCandidateToTop(long candidateId);

	public void fixCandidateToFirstNonFixed(long candidateId);

	public void fixCandidateToBottom(long candidateId);

	public void moveCandidateUp(long candidateId);

	public void moveCandidateDown(long candidateId);

	public void setSortCandidatesRandomly(Long electionId, Boolean value);

	public List<Email> getPendingSendEmails();

	public List<Email> getEmailsAll();

	public List<Email> getElectionEmails(Long electionId);

	public List<Email> getElectionPendingSendEmails(Long electionId);

	public Commissioner getCommissioner(long commissionerId);

	public boolean addCommissioner(String nombre, String mail, String userAdminId, String ip);

	public void removeCommissioner(long commissionerId, String nombre, String userAdminId, String ip);

	public void editCommissioner(Commissioner commissioner, String userAdminId, String ip);

	public File exportCensusExample(String filePath);

	public void createElectionEmailTemplates(Election eleccion);

	public void resendUserVoterElectionMail(UserVoter userVoter, Election election, String userAdminId, String ip);

	public long getUserAuthorizedElectionId(String userAdminId);

	public Parameter getParameter(String key);

	public void requestElectionRevision(Long electionId, Boolean status, String userAdminId, String ip);

	public List<Vote> getElectionVotes(Long electionId);

	public boolean isRevisionActive(long electionId, String userAdminId, String ip);

	public Candidate getNextAboveCandidate(Candidate candidate);

	public Candidate getNextBelowCandidate(Candidate candidate);

	public boolean commissionerExists(String name, String mail);

	public boolean auditorExists(long electionId, String name, String mail);

	public String getDefaultSender();

	public String getDefaultWebsite();

	public boolean createBaseEmailTemplate(ElectionEmailTemplate electionEmailTemplate, String userAdminId, String ip);

	public List<JointElection> getJointElectionsAll();

	public JointElection getJointElectionForElection(long electionId);

	public void updateJointElection(JointElection jointElection);

	public void removeJointElection(JointElection jointElection);

	public boolean isJointElection(long electionId);

	public boolean electionsCensusEqual(JointElection jointElection);

	public List<String> getElectionsAllIdAndTitle();

	public Customization getCustomization();

	public boolean updateCustomization(Customization customization);

	public void persistActivity(String userAdminId, ActivityType activityType, String description, String ip, Long electionId);

}
