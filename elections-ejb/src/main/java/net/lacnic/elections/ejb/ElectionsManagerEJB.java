package net.lacnic.elections.ejb;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.lacnic.elections.data.AsyncProcessingError;
import net.lacnic.elections.data.AsyncProcessingProgress;
import net.lacnic.elections.data.AsyncProcessingType;
import net.lacnic.elections.data.AdminLoginResult;
import net.lacnic.elections.data.CandidateBioMigrationBatchResult;
import net.lacnic.elections.data.CandidatePhotoBatchResult;
import net.lacnic.elections.data.OrganizationBulkImportResult;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import net.lacnic.elections.domain.Commissioner;
import net.lacnic.elections.domain.Customization;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionAuditorResult;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.UserAdmin;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.UserVoterLite;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStage;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateCountryLink;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.CandidateWorkOrganization;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SyncAudit;
import net.lacnic.elections.domain.pre.SyncRun;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.TaskDependencyLevel;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateCountryLinkRow;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCandidateWorkOrganizationRow;
import net.lacnic.elections.domain.services.publicelection.PublicElectionVoteCountRow;
import net.lacnic.elections.domain.services.detail.OrganizationDebtorImportResult;
import net.lacnic.elections.exception.CensusValidationException;

public interface ElectionsManagerEJB {

	public UserAdmin userAdminLogin(String userAdminId, String password, String ip);

	public List<UserAdmin> getUserAdminsAll();

	public UserAdmin getUserAdmin(String userAdminId);

	public boolean isValidCaptchaResponse(String reCaptchaResponse);

	public boolean isShowCaptcha();

	public boolean shouldShowLoginCaptcha(String userName, String ip);

	public boolean sendLinkRecoveryEmail(long electionId, String recipientEmail, String linkTypeKey, String linkUrl, String userAdminId, String ip);

	public Election getElection(long electionId);
	public ElectionAuditorResult getElectionAuditorResult(long electionId);

	public Election getElectionWithRestrictedCountries(long electionId);

	public Election getElectionByQuestionToken(String questionToken);

	public List<Candidate> getElectionCandidatesOrdered(long electionId);

	public List<Candidate> getElectionBallotCandidates(long electionId);

	public List<Candidate> getElectionPublishedCandidates(long electionId);

	public Candidate getElectionAbstentionCandidate(long electionId);

	public List<CandidateQuestion> getElectionCandidateQuestions(long electionId);
	public List<CandidateQuestion> getElectionCandidateQuestionsForPublicElectionPage(long electionId);

	public CandidateQuestion getCandidateQuestion(long candidateQuestionId);

	public CandidateQuestion saveCandidateQuestion(CandidateQuestion candidateQuestion, String userAdminId, String ip);
	public CandidateQuestion saveCandidateQuestion(CandidateQuestion candidateQuestion, boolean sendStatusNotificationEmail, String userAdminId, String ip);

	public boolean createCandidateQuestionFromPublicToken(String questionToken, long candidateId, String askedByName, String askedByEmail, LanguageCode questionLanguage, String questionText, String ip);

	public void removeElection(long electionId, String electionTitle, String userAdminId, String ip) throws Exception;

	public List<Auditor> getElectionAuditors(long electionId) throws Exception;

	public List<Election> getElectionsAllOrderCreationDate();

	public UserVoter getUserVoter(long userVoterId);

	public Election updateElection(Election election, String userAdminId, String ip) throws Exception;
	public ElectionAuditorResult saveElectionAuditorResult(ElectionAuditorResult electionAuditorResult, String userAdminId, String ip) throws Exception;

	public String getResultsLink(Election election) throws Exception;

	public void setResultsLinkStatus(Long electionId, Boolean status, String userAdminId, String ip);

	public void setAuditLinkStatus(Long electionId, Boolean status, String userAdminId, String ip);

	public void setDoNominationLinkStatus(Long electionId, Boolean status, String userAdminId, String ip);

	public void setNominationTasksLinkStatus(Long electionId, Boolean status, String userAdminId, String ip);

	public void setNominationSupportLinkStatus(Long electionId, Boolean status, String userAdminId, String ip);

	public void setPublicElectionLinkStatus(Long electionId, Boolean status, String userAdminId, String ip);

	public List<Commissioner> getCommissionersAll();

	public void removeUserVoter(UserVoter userVoter, String electionTitle, String userAdminId, String ip) throws CensusValidationException;

	public List<Activity> getActivitiesAll();

	public List<Activity> getElectionActivities(long electionId);

	public List<UserVoter> getElectionUserVoters(long electionId);

	public List<UserVoter> getElectionUserVotersByEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter);
	public List<Organization> getElectionOrganizationsByMembershipContactEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter);

	public List<UserVoterLite> getElectionUserVotersLite(long electionId);

	public void updateUserVoterToken(long userVoterId, String name, String electionTitle, String userAdminId, String ip);

	public void removeUserAdmin(String userAdminToDeleteId, String userAdminId, String ip);

	public void updateElectionCensus(String contentType, long electionId, byte[] content, boolean regenerateVoteLinks, String userAdminId, String ip) throws CensusValidationException, Exception;

	public boolean queueElectionCensusUpdate(String contentType, long electionId, byte[] content, boolean regenerateVoteLinks, String userAdminId, String ip);

	public void processElectionCensusUpdateAsync(String contentType, long electionId, byte[] content, boolean regenerateVoteLinks, String userAdminId, String ip);

	public OrganizationDebtorImportResult updateOrganizationsDebtors(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip) throws CensusValidationException;

	public boolean queueOrganizationsDebtorsUpdate(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip);

	public void processOrganizationsDebtorsUpdateAsync(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip);

	public List<Organization> getOrganizations(long electionId);

	public boolean addOrganization(long electionId, Organization organization, String userAdminId, String ip) throws CensusValidationException;

	public List<Election> getElectionsLightThisYear();

	public boolean addUserVoter(long electionId, UserVoter userVoter, String userAdminId, String ip) throws CensusValidationException;

	public void editUserVoter(UserVoter userVoter, String userAdminId, String ip) throws CensusValidationException;

	public List<ElectionEmailTemplate> getElectionEmailTemplates(long electionId);

	public void modifyElectionEmailTemplate(ElectionEmailTemplate electionEmailTemplate);

	public void setVoteLinkStatus(Long electionId, Boolean status, String userAdminId, String ip);

	public List<ElectionEmailTemplate> getBaseEmailTemplates();

	public ElectionEmailTemplate getEmailTemplate(String templateType, long electionId);

	public void editUserAdmin(UserAdmin userAdmin, String email, String userAdminId, String ip);

	public File exportCensus(long electionId);

	public File exportOrganizationsUpsert(long electionId);

	public void editAdminUserPassword(String userAdminToUpdateId, String password, String userAdminId, String ip);

	public boolean addUserAdmin(UserAdmin userAdmin, String userAdminId, String ip);

	public void addCandidate(long electionId, Candidate candidate, String userAdminId, String ip);

	public void addAbstentionCandidate(long electionId, String userAdminId, String ip);

	public void removeCandidate(long candidateId, String userAdminId, String ip);

	public Candidate getCandidate(long candidateId);
	public Map<ElectionTaskKey, CandidateElectionTaskStatus> getCandidateSupportTaskStatuses(long candidateId);

	public Nomination getNominationByCandidateId(long candidateId);
	public List<SupportNomination> getCandidateSupportNominations(long candidateId);
	public boolean rejectCandidateSupport(long candidateId, long supportNominationId, String reason, String userAdminId, String ip);
	boolean approveRejectedCandidateSupport(long candidateId, long supportNominationId, String comment, String userAdminId, String ip);
	boolean returnRejectedCandidateSupportToProposal(long candidateId, long supportNominationId, String comment, String userAdminId, String ip);

	public void editCandidate(Candidate candidate, String userAdminId, String ip);
	public boolean updateCandidateWinner(long candidateId, boolean winner, String userAdminId, String ip);

	public void editNomination(Nomination nomination, String userAdminId, String ip);

	public CandidateBioMigrationBatchResult migrateCandidateBiosHtmlToPlainText(String userAdminId, String ip);

	public CandidatePhotoBatchResult optimizeAllCandidatePhotos(String userAdminId, String ip);

	public boolean updateCandidateStatus(long candidateId, CandidateStatus newStatus, String comment, String userAdminId, String ip);
	public boolean updateCandidateStatus(long candidateId, CandidateStatus newStatus, String comment, boolean sendConfirmedAndPublishedEmail, String userAdminId, String ip);

	public void addAuditor(long electionId, Auditor auditor, String electionTitle, String userAdminId, String ip);

	public void removeAuditor(long auditorId, String userAdminId, String ip);

	public Auditor getAuditor(long auditorId);

	public void editAuditor(Auditor auditor, String userAdminId, String ip);

	public void persistElectionAuditorsSet(long electionId, String electionTitle, String userAdminId, String ip);

	public void persistElectionOrganizationsSet(long electionId, String electionTitle, String userAdminId, String ip);

	public void persistElectionCalendarSet(long electionId, String electionTitle, String userAdminId, String ip);

	public void saveElectionCalendars(long electionId, List<ElectionCalendar> calendars, String electionTitle, String userAdminId, String ip);

	public void persistElectionTasksSet(long electionId, String electionTitle, String userAdminId, String ip);

	public void persistElectionCallSet(long electionId, String electionTitle, String userAdminId, String ip);

	public void persistElectionElectorsSet(long electionId, String electionTitle, String userAdminId, String ip);

	public void persistElectionCandidatesSet(long electionId, String electionTitle, String userAdminId, String ip);

	public List<Parameter> getParametersAll();

	public List<IpAccess> getAllDisabledIPs();

	public boolean addParameter(String key, String value, String userAdminId, String ip);

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

	public File exportCensusExample();

	public File exportOrganizationsUpsertExample();

	public File exportOrganizationsDeleteExample();

	public File exportOrganizationsDebtorsExample();

	public void createElectionEmailTemplates(Election eleccion);

	public void resendUserVoterElectionMail(UserVoter userVoter, Election election, String userAdminId, String ip);

	public Parameter getParameter(String key);

	public void requestElectionRevision(Long electionId, Boolean status, String userAdminId, String ip);

	public List<Vote> getElectionVotes(Long electionId);
	public List<PublicElectionVoteCountRow> getElectionVoteCountRowsForPublicElectionPage(Long electionId);

	public List<Nomination> getElectionNominationsByEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter);

	public List<SupportNomination> getElectionSupportNominationsByEmail(long electionId, String normalizedEmail, int maxResults, String countryCodeFilter);

	public boolean isRevisionActive(long electionId, String userAdminId, String ip);

	public Candidate getNextAboveCandidate(Candidate candidate);

	public Candidate getNextBelowCandidate(Candidate candidate);

	public boolean commissionerExists(String name, String mail);

	public boolean auditorExists(long electionId, String name, String mail);

	public String getDefaultSender();

	public String getDefaultRecipient();

	public String getDefaultWebsite();

	public boolean createBaseEmailTemplate(ElectionEmailTemplate electionEmailTemplate, String userAdminId, String ip);

	public int forceBaseTemplateToOpenElections(String templateType, String userAdminId, String ip);

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

	public String getDataSiteKey();

	public boolean electionCanBeClosed(long electionId);

	public boolean closeElection(long electionId, String userAdminId, String ip);

	public AdminLoginResult login(String username, String password, String ip);

	public AdminLoginResult login(String username, String password, String ip, String totp);

	public String getAuditReportURL();

	public List<ElectionCalendar> getElectionCalendars(long electionId);

	public List<ElectionTask> getElectionTasks(long electionId);

	public boolean addElectionTask(long electionId, ElectionTaskKey taskKey, long electionCalendarId, TaskDependencyLevel dependencyLevel, Integer displayOrder, boolean publicable, String userAdminId,
			String ip);

	public boolean editElectionTask(long electionTaskId, long electionCalendarId, TaskDependencyLevel dependencyLevel, Integer displayOrder, boolean publicable, String userAdminId, String ip);

	public boolean moveElectionTaskUp(long electionTaskId, String userAdminId, String ip);

	public boolean moveElectionTaskDown(long electionTaskId, String userAdminId, String ip);

	public boolean removeElectionTask(long electionTaskId, String userAdminId, String ip);

	public List<Organization> getElectionOrganizations(long electionId);

	public SyncRun getElectionLatestSyncRun(long electionId);

	public List<SyncRun> getElectionSyncRuns(long electionId, int maxResults);
	public List<SyncRun> getElectionAutomaticCensusSyncRuns(long electionId, int maxResults);

	public Map<Long, SyncRun> getLatestSyncRunsByElectionIds(List<Long> electionIds);

	public List<SyncRun> getSyncRuns(int maxResults);

	public Map<Long, Date[]> getMilacnicSyncCalendarWindowByElectionIds(List<Long> electionIds);

	public List<SyncAudit> getElectionSyncAudits(long electionId, int maxResults);
	public List<SyncAudit> getElectionSyncAuditsByRunId(long electionId, String syncRunId, int maxResults);
	public List<SyncAudit> getElectionSyncAuditsByRunIds(long electionId, List<String> syncRunIds, int maxResults);

	public List<Nomination> getElectionNominations(long electionId);
	public List<Nomination> getElectionNominationsForPublicElectionPage(long electionId);

	public boolean sendCandidateReminder(long electionId, long candidateId, String userAdminId, String ip);

	public boolean sendAuditorReminder(long electionId, long auditorId, String userAdminId, String ip);

	public boolean sendAuditorRevisionReminder(long electionId, long auditorId, String userAdminId, String ip);

	public void processDailyReminderFrequency();
	
	public void processMilacnicOrganizationsSync();
	public void processAutomaticCensusSync();
	public void processAutomaticOrganizationsAndCensusSync();
	public void processAutomaticOrganizationsSyncForElectionTx(long electionId, String batchRunId, long executionTimestampMs);
	public void processAutomaticCensusSyncForElectionTx(long electionId, String batchRunId, long executionTimestampMs);

	public SyncRun forceElectionCensusSyncFromOrganizations(long electionId, boolean regenerateVoteLinks, String userAdminId, String ip);
	public boolean queueElectionCensusSyncFromOrganizations(long electionId, boolean regenerateVoteLinks, String userAdminId, String ip);
	public void processElectionCensusSyncFromOrganizationsAsync(long electionId, boolean regenerateVoteLinks, String userAdminId, String ip);
	public int regenerateElectionCensusVoteLinks(long electionId, String userAdminId, String ip) throws CensusValidationException;
	public boolean queueElectionCensusRegenerateVoteLinks(long electionId, String userAdminId, String ip);
	public void processElectionCensusRegenerateVoteLinksAsync(long electionId, String userAdminId, String ip);
	public int deleteElectionCensus(long electionId, String userAdminId, String ip) throws CensusValidationException;
	public boolean queueElectionCensusDeleteAll(long electionId, String userAdminId, String ip);
	public void processElectionCensusDeleteAllAsync(long electionId, String userAdminId, String ip);

	public void processMilacnicOrganizationsSyncForElectionInWindow(long electionId);
	public boolean queueMilacnicOrganizationsSyncForElectionInWindow(long electionId, String userAdminId, String ip);
	public void processMilacnicOrganizationsSyncForElectionInWindowAsync(long electionId, String userAdminId, String ip);
	public void processMilacnicOrganizationsSyncForElectionInWindow(long electionId, boolean regenerateNominationLinks);
	public boolean queueMilacnicOrganizationsSyncForElectionInWindow(long electionId, boolean regenerateNominationLinks, String userAdminId, String ip);
	public void processMilacnicOrganizationsSyncForElectionInWindowAsync(long electionId, boolean regenerateNominationLinks, String userAdminId, String ip);
	public boolean queueMilacnicOrganizationsDebtorMirrorSyncForElectionInWindow(long electionId, String userAdminId, String ip);
	public void processMilacnicOrganizationsDebtorMirrorSyncForElectionInWindowAsync(long electionId, String userAdminId, String ip);

	public void processMilacnicOrganizationsSyncForElection(long electionId);
	public boolean queueMilacnicOrganizationsSyncForElection(long electionId, String userAdminId, String ip);
	public void processMilacnicOrganizationsSyncForElectionAsync(long electionId, String userAdminId, String ip);
	public void processMilacnicOrganizationsSyncForElection(long electionId, boolean regenerateNominationLinks);
	public boolean queueMilacnicOrganizationsSyncForElection(long electionId, boolean regenerateNominationLinks, String userAdminId, String ip);
	public void processMilacnicOrganizationsSyncForElectionAsync(long electionId, boolean regenerateNominationLinks, String userAdminId, String ip);

	public void processMilacnicOrganizationsSyncForAllConfiguredElections();
	public int regenerateElectionOrganizationsNominationLinks(long electionId, String userAdminId, String ip) throws CensusValidationException;
	public boolean queueElectionOrganizationsRegenerateNominationLinks(long electionId, String userAdminId, String ip);
	public void processElectionOrganizationsRegenerateNominationLinksAsync(long electionId, String userAdminId, String ip);
	public int regenerateElectionOrganizationsSupportLinks(long electionId, String userAdminId, String ip) throws CensusValidationException;
	public boolean queueElectionOrganizationsRegenerateSupportLinks(long electionId, String userAdminId, String ip);
	public void processElectionOrganizationsRegenerateSupportLinksAsync(long electionId, String userAdminId, String ip);
	public int deleteElectionOrganizations(long electionId, String userAdminId, String ip) throws CensusValidationException;
	public boolean queueElectionOrganizationsDeleteAll(long electionId, String userAdminId, String ip);
	public void processElectionOrganizationsDeleteAllAsync(long electionId, String userAdminId, String ip);

	public List<SupportNomination> getElectionSupportNominations(long electionId);
	public List<SupportNomination> getElectionSupportNominationsForPublicElectionPage(long electionId);

	public List<AuditorCandidateDecision> getElectionAuditorCandidateDecisions(long electionId);

	public boolean updateAuditorCandidateDecisionStatus(long auditorId, long candidateId, AuditorCandidateDecisionStatus status, String actor, String ip, String comment);

	public boolean updateAuditorCandidateDecisionStageStatus(long auditorId, long candidateId, AuditorCandidateDecisionStage stage, AuditorCandidateDecisionStatus status, String actor, String ip, String comment);

	public List<CandidateCountryLink> getElectionCandidateCountryLinks(long electionId);
	public List<PublicElectionCandidateCountryLinkRow> getElectionCandidateCountryLinkRowsForPublicElectionPage(long electionId);

	public List<CandidateWorkOrganization> getElectionCandidateWorkOrganizations(long electionId);
	public List<PublicElectionCandidateWorkOrganizationRow> getElectionCandidateWorkOrganizationRowsForPublicElectionPage(long electionId);

	public OrganizationBulkImportResult removeOrganizationsFromExcel(String contentType, long electionId, byte[] content, String userAdminId, String ip) throws CensusValidationException;

	public boolean queueOrganizationsDeleteFromExcel(String contentType, long electionId, byte[] content, String userAdminId, String ip);

	public void processOrganizationsDeleteFromExcelAsync(String contentType, long electionId, byte[] content, String userAdminId, String ip);

	public OrganizationBulkImportResult upsertOrganizationsFromExcel(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip) throws CensusValidationException;

	public boolean queueOrganizationsUpsertFromExcel(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip);

	public void processOrganizationsUpsertFromExcelAsync(String contentType, long electionId, byte[] content, boolean overwriteAll, String userAdminId, String ip);

	public void validateOrganizationsDebtorsExcel(String contentType, byte[] content) throws CensusValidationException;

	public void validateOrganizationsDebtorsCanBeApplied(String contentType, long electionId, byte[] content) throws CensusValidationException;

	public void validateOrganizationsDeleteExcel(String contentType, byte[] content) throws CensusValidationException;

	public List<String> validateOrganizationsUpsertExcel(String contentType, byte[] content) throws CensusValidationException;

	public OrganizationBulkImportResult validateOrganizationsUpsertExcelDetailed(String contentType, byte[] content) throws CensusValidationException;

	public void validateOrganizationCanBeRemoved(long electionId, String orgId) throws CensusValidationException;

	public void validateOrganizationsDeleteCanBeApplied(String contentType, long electionId, byte[] content) throws CensusValidationException;

	public void validateOrganizationsUpsertOverwriteCanBeApplied(String contentType, long electionId, byte[] content) throws CensusValidationException;

	public void validateElectionCensusUpsertCanBeApplied(String contentType, long electionId, byte[] content, boolean regenerateVoteLinks) throws CensusValidationException;

	public void validateElectionCensusOverwriteCanBeApplied(String contentType, long electionId, byte[] content) throws CensusValidationException;

	public boolean isElectionCensusProcessing(long electionId);

	public boolean isElectionOrganizationsProcessing(long electionId);

	public AsyncProcessingType getElectionProcessingType(long electionId);

	public AsyncProcessingError getElectionCensusProcessingError(long electionId);

	public AsyncProcessingProgress getElectionCensusProcessingProgress(long electionId);

	public AsyncProcessingError getElectionOrganizationsProcessingError(long electionId);

	public AsyncProcessingProgress getElectionOrganizationsProcessingProgress(long electionId);

	public void validateUserVoterCanBeAdded(long electionId, UserVoter userVoter) throws CensusValidationException;

	public void validateUserVoterCanBeEdited(UserVoter userVoter) throws CensusValidationException;

	public void validateUserVoterCanBeRemoved(long electionId, long userVoterId) throws CensusValidationException;

	public boolean renewOrganizationNominationLink(long electionId, String orgId, String userAdminId, String ip) throws CensusValidationException;

	public boolean removeOrganization(long electionId, String orgId, String userAdminId, String ip) throws CensusValidationException;

	public String getCampusUrl();

	public String getCampusToken();

	public void verifyCampusCourseAccessForCandidates();
	public void verifyCampusEvaluationGradesForCandidates();
	public void verifyCampusCourseAccessForCandidatesRateLimited(String requesterKey, String candidateMail);
	public void verifyCampusEvaluationGradesForCandidatesRateLimited(String requesterKey, String candidateMail);

	public boolean sendNominationReminder(long electionId, long nominationId, String userAdminId, String ip);

	public boolean sendSupportReminder(long electionId, long supportNominationId, String userAdminId, String ip);

}
