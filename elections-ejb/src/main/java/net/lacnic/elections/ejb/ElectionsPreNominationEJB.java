package net.lacnic.elections.ejb;

import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import net.lacnic.elections.domain.pre.CandidateTrainingCredentialsRequestResult;
import net.lacnic.elections.domain.pre.CandidateDeclarationsDefinition;
import net.lacnic.elections.domain.pre.CandidateTextImprovementInstruction;
import net.lacnic.elections.domain.pre.CandidateTextImprovementResponse;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.ReminderFrequency;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.PublicNominationSubmission;
import net.lacnic.elections.domain.pre.PublicNominationSubmissionResult;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;

public interface ElectionsPreNominationEJB {

	public Nomination verifyAcceptNominationAccess(String token);

	public Organization verifyDoNominationAccess(String token);

	public boolean doNomination(String token, Nomination nominationData, String clientIp);

	public PublicNominationSubmissionResult submitPublicNomination(String publicElectionToken, PublicNominationSubmission submission, String clientIp);

	public SupportNomination verifySupportNominationAccess(String token);

//	TODO: ger i26 ini
	public String getAcceptNominationConditions(String language);

	public boolean acceptNomination(String token, String clientIp);

	public boolean rejectNomination(String token, String clientIp);

	public boolean updateCandidateReminderFrequency(String token, ReminderFrequency reminderFrequency, String clientIp);

	public boolean updateCandidateTaskStatus(String token, ElectionTaskKey taskKey, CandidateElectionTaskStatus status, String clientIp);
	public boolean updateCandidateEvaluationStartedVariant(String token, boolean moveToSent, String clientIp);

	public java.util.List<CandidateQuestion> getCandidateQuestionsForNominationTasks(String token);

	public boolean submitCandidateQuestionAnswer(String token, long candidateQuestionId, String answerText, String actor, String ip);
//	TODO: ger i26 fin

	public Candidate saveCandidateProfile(String token, Candidate candidate, String actor, String ip);

	public Candidate saveCandidateCountries(String token, Candidate candidate, String actor, String ip);

	public Candidate saveCandidateOrganizations(String token, Candidate candidate, String actor, String ip);

	public Candidate saveCandidateIncompatibilities(String token, Candidate candidate, String actor, String ip);

	public Candidate saveCandidateDeclarations(String token, Candidate candidate, String actor, String ip);

	public CandidateDeclarationsDefinition getCandidateDeclarationsDefinition(String token, String language);

	public Candidate saveCandidateNonStatutoryDeclarations(String token, Candidate candidate, String actor, String ip);

	public CandidateDeclarationsDefinition getCandidateNonStatutoryDeclarationsDefinition(String token, String language);

	public Candidate saveCandidateOtherStatutoryQuestions(String token, Candidate candidate, String actor, String ip);

	public java.util.List<String> getOtherStatutoryQuestions(String language);

	public Candidate saveCandidateOtherNonStatutoryQuestions(String token, Candidate candidate, String actor, String ip);

	public java.util.List<String> getOtherNonStatutoryQuestions(String language);

	public String improveCandidateText(String token, String originalText, CandidateTextImprovementInstruction instruction);

	public String improveCandidateText(String token, String originalText, CandidateTextImprovementInstruction instruction, String questionText);

	public CandidateTextImprovementResponse improveCandidateTextWithStatus(String token, String originalText, CandidateTextImprovementInstruction instruction, String questionText);
	
	public CandidateTextImprovementResponse improveCandidateTextWithStatus(String token, String originalText, CandidateTextImprovementInstruction instruction, String questionText, String clientIp);

	public Candidate requestCandidateTrainingCredentials(String token, String actor, String ip);

	public CandidateTrainingCredentialsRequestResult requestCandidateTrainingCredentialsDetailed(String token, String actor, String ip);

	public CandidateTrainingCredentialsRequestResult requestCandidateTrainingCredentialsDetailed(String token, String actor, String ip, Long courseId);
		
	public Candidate requestCandidateEvaluationCredentials(String token, String actor, String ip);

	public Organization findOrganizationForSupportRequest(String token, String identifierType, String identifierValue);
	
	public String getOrganizationSupportSearchValidationMessageKey(String token, String identifierType, String identifierValue);

	public boolean requestCandidateOrganizationSupport(String token, Long supportingOrganizationId, String actor, String ip);

	public boolean requestCandidateUserSupport(String token, String supportingContactName, String supportingContactEmail, ElectionTaskKey taskKey, int requiredSupports, String actor, String ip);

	public boolean updateSupportNominationStatus(String token, SupportStatus status, String ip);
}
