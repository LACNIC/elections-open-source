package net.lacnic.elections.ejb.commons;

import java.util.List;
import java.util.Map;

import jakarta.ejb.Remote;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.utils.EmailTemplateType;

@Remote
public interface MailsSendingEJB {

	void queueMassiveSending(List users, ElectionEmailTemplate templateEleccion);

	List<Email> getEmailsToSend(int maxResults);

	void queueSingleSending(ElectionEmailTemplate tamplateElection, UserVoter userVoter, Auditor auditor, Election election, List<Vote> votes);

	void queueNominationSubmittedToNominee(long nominationId);

	void queueNominationAcceptedToRepresentative(long nominationId);

	void queueNominationRejectedToRepresentative(long nominationId);

	void queueOrganizationSupportRequestToMembershipContact(long supportNominationId);

	void queueUserSupportRequestToSupportingContact(long supportNominationId);

	void queueSupportStatusChangedToCandidate(long supportNominationId);

	void queueSupportReminderToSupportingContact(long supportNominationId);
	
	void queueSupportReminderToRequester(long supportNominationId);

	void queueNominationReminderToNominee(long nominationId);

	void queueCandidateReminderToCandidate(long candidateId);

	void queueAuditorReminderToAuditor(long auditorId);

	void queueAuditorRevisionReminderToAuditor(long auditorId);

	boolean queueLinkRecoveryEmail(long electionId, String recipientEmail, String linkTypeKey, String linkUrl);

	void queueCandidateConfirmedAndPublishedToCandidate(long candidateId);

	void queueCandidateTrainingAccessRequestedToElectionSender(long candidateId);

	void queueCandidateEvaluationAccessRequestedToElectionSender(long candidateId);

	void queueCandidateTaskCompletedToElectionSender(long candidateId, String taskKey);

	void queueCandidateAllTasksCompletedToElectionSender(long candidateId);

	void queueAuditorCandidateApprovedToElectionSender(long auditorId, long candidateId, String comment);

	void queueAuditorCandidateRejectedToElectionSender(long auditorId, long candidateId, String comment);

	void queueTemplateEmail(Election election, EmailTemplateType templateType, String recipient, String cc, LanguageCode language, Map<String, Object> variables);

	void markEmailAsSent(Email email);

	void moveEmailsToHistory();

	void purgeTables();

	void purgeCensusTablesAsync(long electionId);

}
