package net.lacnic.elections.ejb.commons.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.Asynchronous;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.EmailHistory;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.Vote;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;
import net.lacnic.elections.ejb.commons.MailsSendingEJB;
import net.lacnic.elections.utils.AuditorCandidateDecisionUtils;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.EmailTemplateType;
import net.lacnic.elections.utils.ElectionsCaches;
import net.lacnic.elections.utils.PublicLinkRecoveryType;
import net.lacnic.elections.utils.VotingPeriodResolver;

@Stateless
@Remote(MailsSendingEJB.class)
public class MailsSendingEJBBean implements MailsSendingEJB {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");
	private static final String UNKNOWN_TEMPLATE_TYPE = "UNKNOWN_TEMPLATE";
	private static final String SIGNATURE_TOKEN = "$signature";
	private static final String SIGNATURE_TOKEN_BRACED = "${signature}";
	private static final String SIGNATURE_EMAIL_VARIABLE = "email";
	private static final String LINK_RECOVERY_URL_VARIABLE = "linkRecoveryUrl";
	private static final String LINK_RECOVERY_TYPE_SP_VARIABLE = "linkRecoveryTypeSpanish";
	private static final String LINK_RECOVERY_TYPE_EN_VARIABLE = "linkRecoveryTypeEnglish";
	private static final String LINK_RECOVERY_TYPE_PT_VARIABLE = "linkRecoveryTypePortuguese";
	private static final String VARIABLE_ELECTION = "election";
	private static final String VARIABLE_AUDITOR = "auditor";
	private static final String VARIABLE_ORGANIZATION = "organization";
	private static final String VARIABLE_CANDIDATE = "candidate";
	private static final String PREVIEW_TEMPLATE_FOUND = "templateFound";
	private static final String PREVIEW_SUBJECT = "subject";
	private static final String LINK_RECOVERY_LINK_PREFIX = "Link: $";

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	@Override
	public void queueMassiveSending(List users, ElectionEmailTemplate emailTemplate) {
		if (emailTemplate == null || emailTemplate.getElection() == null || users == null || users.isEmpty()) {
			return;
		}

		Election election = resolveElectionWithVotingWindow(emailTemplate.getElection());
		try {
			for (Object user : users) {
				if (user instanceof UserVoter) {
					queueMassiveSendingToVoter((UserVoter) user, election, emailTemplate);
				} else if (user instanceof Auditor) {
					queueMassiveSendingToAuditor((Auditor) user, election, emailTemplate);
				} else if (user instanceof Organization) {
					queueMassiveSendingToOrganization((Organization) user, election, emailTemplate);
				}
			}
		} catch (Exception e) {
			appLogger.error("Error queueing massive sending. electionId={}, templateType={}", election.getElectionId(), emailTemplate.getTemplateType(), e);
		}
	}

	@Override
	public void queueSingleSending(ElectionEmailTemplate emailTemplate, UserVoter userVoter, Auditor auditor, Election election, List<Vote> votes) {
		Election resolvedElection = resolveElectionWithVotingWindow(election);
		if (resolvedElection == null) {
			return;
		}

		try {
			boolean sendToAuditor = isAuditorTemplate(emailTemplate) || (emailTemplate == null && auditor != null && userVoter == null);
			if (sendToAuditor) {
				queueSingleSendingToAuditor(emailTemplate, auditor, resolvedElection);
			} else {
				queueSingleSendingToVoter(emailTemplate, userVoter, resolvedElection, votes);
			}
		} catch (Exception ex) {
			appLogger.error("Error queueing single sending. electionId={}, templateType={}", resolvedElection.getElectionId(), emailTemplate != null ? emailTemplate.getTemplateType() : UNKNOWN_TEMPLATE_TYPE, ex);
		}
	}

	private void queueMassiveSendingToVoter(UserVoter userVoter, Election election, ElectionEmailTemplate emailTemplate) {
		if (userVoter == null || !hasText(userVoter.getMail())) {
			return;
		}

		LanguageCode language = normalizeLanguage(userVoter.getLanguageEnum());
		if (templateContainsCodeSummary(getTemplateSubject(emailTemplate, language), getTemplateBody(emailTemplate, language))) {
			List<Vote> votes = ElectionsDaoFactory.createVoteDao(em).getElectionUserVoterVotes(userVoter.getUserVoterId(), election.getElectionId());
			userVoter.setCodesSummary(addVotes(votes));
		}

		Map<String, Object> variables = new HashMap<>();
		variables.put("user", userVoter);
		variables.put(VARIABLE_ELECTION, election);
		queueEmail(election, emailTemplate, userVoter.getMail(), language, variables);
	}

	private void queueMassiveSendingToAuditor(Auditor auditor, Election election, ElectionEmailTemplate emailTemplate) {
		if (auditor == null || !hasText(auditor.getMail())) {
			return;
		}

		Map<String, Object> variables = new HashMap<>();
		variables.put(VARIABLE_AUDITOR, auditor);
		variables.put(VARIABLE_ELECTION, election);
		queueEmail(election, emailTemplate, auditor.getMail(), Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	private void queueMassiveSendingToOrganization(Organization organization, Election election, ElectionEmailTemplate emailTemplate) {
		if (organization == null || !hasText(organization.getMembershipContactEmail())) {
			return;
		}

		Map<String, Object> variables = new HashMap<>();
		variables.put(VARIABLE_ORGANIZATION, organization);
		variables.put(VARIABLE_ELECTION, election);
		LanguageCode language = organization.getMembershipContactLanguageEnum() != null ? organization.getMembershipContactLanguageEnum() : Constants.DEFAULT_EMAIL_LANGUAGE;
		queueEmail(election, emailTemplate, organization.getMembershipContactEmail(), language, variables);
	}

	private void queueSingleSendingToAuditor(ElectionEmailTemplate emailTemplate, Auditor auditor, Election election) {
		if (auditor == null) {
			return;
		}

		String recipient = resolveElectionDefaultRecipient(election);
		if (!hasText(recipient)) {
			return;
		}

		Map<String, Object> variables = new HashMap<>();
		variables.put(VARIABLE_AUDITOR, auditor);
		variables.put(VARIABLE_ELECTION, election);
		queueEmailAndDispatchIfEnabled(election, emailTemplate, recipient, Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	private void queueSingleSendingToVoter(ElectionEmailTemplate emailTemplate, UserVoter userVoter, Election election, List<Vote> votes) {
		if (userVoter == null || !hasText(userVoter.getMail())) {
			return;
		}

		if (templateContainsCodeSummaryInAnyLanguage(emailTemplate)) {
			userVoter.setCodesSummary(addVotes(votes));
		}

		LanguageCode language = normalizeLanguage(userVoter.getLanguageEnum());
		Map<String, Object> variables = new HashMap<>();
		variables.put("user", userVoter);
		variables.put(VARIABLE_ELECTION, election);
		queueEmailAndDispatchIfEnabled(election, emailTemplate, userVoter.getMail(), language, variables);
	}

	private boolean isAuditorTemplate(ElectionEmailTemplate emailTemplate) {
		return emailTemplate != null && hasText(emailTemplate.getTemplateType()) && EmailTemplateType.AUDITOR.isContainedIn(emailTemplate.getTemplateType());
	}

	private boolean templateContainsCodeSummaryInAnyLanguage(ElectionEmailTemplate template) {
		return templateContainsCodeSummary(template != null ? template.getSubjectSP() : null, template != null ? template.getBodySP() : null) || templateContainsCodeSummary(template != null ? template.getSubjectEN() : null, template != null ? template.getBodyEN() : null) || templateContainsCodeSummary(template != null ? template.getSubjectPT() : null, template != null ? template.getBodyPT() : null);
	}

	private boolean templateContainsCodeSummary(String subject, String body) {
		return (subject != null && subject.contains(Constants.EMAIL_CODE_SUMMARY_TOKEN)) || (body != null && body.contains(Constants.EMAIL_CODE_SUMMARY_TOKEN));
	}

	@Override
	public void queueNominationSubmittedToNominee(long nominationId) {
		Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNomination(nominationId);
		if (nomination == null || nomination.getElection() == null) {
			return;
		}

		Map<String, Object> variables = buildTemplateVariables(nomination.getElection(), nomination, nomination.getCandidate(), null, null, null);
		queueEmailForTemplate(nomination.getElection(), EmailTemplateType.NOMINATION_SUBMITTED_NOMINEE, resolveNomineeRecipient(nomination), Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	@Override
	public void queueNominationAcceptedToRepresentative(long nominationId) {
		queueNominationToRepresentative(nominationId, EmailTemplateType.NOMINATION_ACCEPTED_REPRESENTATIVE);
	}

	@Override
	public void queueNominationRejectedToRepresentative(long nominationId) {
		queueNominationToRepresentative(nominationId, EmailTemplateType.NOMINATION_REJECTED_REPRESENTATIVE);
	}

	private void queueNominationToRepresentative(long nominationId, EmailTemplateType templateType) {
		Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNomination(nominationId);
		if (nomination == null || nomination.getElection() == null || nomination.getOrganization() == null) {
			return;
		}

		Organization organization = nomination.getOrganization();
		Map<String, Object> variables = buildTemplateVariables(nomination.getElection(), nomination, nomination.getCandidate(), null, null, null);
		queueEmailForTemplate(nomination.getElection(), templateType, organization.getMembershipContactEmail(), LanguageCode.fromValueOrDefault(organization.getMembershipContactLanguage(), Constants.DEFAULT_EMAIL_LANGUAGE), variables);
	}

	@Override
	public void queueOrganizationSupportRequestToMembershipContact(long supportNominationId) {
		SupportNomination supportNomination = ElectionsDaoFactory.createSupportNominationDao(em).getSupportNomination(supportNominationId);
		if (supportNomination == null || supportNomination.getNomination() == null || supportNomination.getNomination().getElection() == null) {
			return;
		}

		Organization supportingOrganization = supportNomination.getSupportingOrganization();
		String recipient = supportingOrganization != null ? supportingOrganization.getMembershipContactEmail() : supportNomination.getSupportingContactEmail();
		LanguageCode language = supportingOrganization != null ? LanguageCode.fromValueOrDefault(supportingOrganization.getMembershipContactLanguage(), Constants.DEFAULT_EMAIL_LANGUAGE) : Constants.DEFAULT_EMAIL_LANGUAGE;
		Map<String, Object> variables = buildTemplateVariables(supportNomination.getNomination().getElection(), supportNomination.getNomination(), supportNomination.getNomination().getCandidate(), null, supportNomination, null);
		queueEmailForTemplate(supportNomination.getNomination().getElection(), EmailTemplateType.NOMINATION_ORG_SUPPORT_REQUEST, recipient, language, variables);
	}

	@Override
	public void queueUserSupportRequestToSupportingContact(long supportNominationId) {
		SupportNomination supportNomination = ElectionsDaoFactory.createSupportNominationDao(em).getSupportNomination(supportNominationId);
		if (supportNomination == null || supportNomination.getNomination() == null || supportNomination.getNomination().getElection() == null) {
			return;
		}

		Map<String, Object> variables = buildTemplateVariables(supportNomination.getNomination().getElection(), supportNomination.getNomination(), supportNomination.getNomination().getCandidate(), null, supportNomination, null);
		queueEmailForTemplate(supportNomination.getNomination().getElection(), EmailTemplateType.NOMINATION_USER_SUPPORT_REQUEST, supportNomination.getSupportingContactEmail(), Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	@Override
	public void queueSupportStatusChangedToCandidate(long supportNominationId) {
		SupportNomination supportNomination = ElectionsDaoFactory.createSupportNominationDao(em).getSupportNomination(supportNominationId);
		if (supportNomination == null || supportNomination.getNomination() == null || supportNomination.getNomination().getElection() == null) {
			return;
		}

		Nomination nomination = supportNomination.getNomination();
		Candidate candidate = nomination.getCandidate();
		EmailTemplateType templateType = resolveSupportStatusCandidateTemplateType(supportNomination.getSupportStatus());
		if (templateType == null) {
			return;
		}
		String recipient = candidate != null && hasText(candidate.getMail()) ? candidate.getMail() : nomination.getNominationEmail();
		Map<String, Object> variables = buildTemplateVariables(nomination.getElection(), nomination, candidate, null, supportNomination, null);
		queueEmailForTemplate(nomination.getElection(), templateType, recipient, Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	@Override
	public void queueSupportReminderToSupportingContact(long supportNominationId) {
		SupportNomination supportNomination = ElectionsDaoFactory.createSupportNominationDao(em).getSupportNomination(supportNominationId);
		if (supportNomination == null || supportNomination.getNomination() == null || supportNomination.getNomination().getElection() == null) {
			return;
		}

		Nomination nomination = supportNomination.getNomination();
		Organization supportingOrganization = supportNomination.getSupportingOrganization();
		String recipient = supportingOrganization != null && hasText(supportingOrganization.getMembershipContactEmail()) ? supportingOrganization.getMembershipContactEmail() : supportNomination.getSupportingContactEmail();
		LanguageCode language = supportingOrganization != null ? LanguageCode.fromValueOrDefault(supportingOrganization.getMembershipContactLanguage(), Constants.DEFAULT_EMAIL_LANGUAGE) : Constants.DEFAULT_EMAIL_LANGUAGE;
		Map<String, Object> variables = buildTemplateVariables(nomination.getElection(), nomination, nomination.getCandidate(), null, supportNomination, null);
		queueEmailForTemplate(nomination.getElection(), EmailTemplateType.SUPPORT_REMINDER, recipient, language, variables);
	}
	
	@Override
	public void queueSupportReminderToRequester(long supportNominationId) {
		SupportNomination supportNomination = ElectionsDaoFactory.createSupportNominationDao(em).getSupportNomination(supportNominationId);
		if (supportNomination == null || supportNomination.getNomination() == null || supportNomination.getNomination().getElection() == null) {
			return;
		}
		
		Nomination nomination = supportNomination.getNomination();
		Candidate candidate = nomination.getCandidate();
		String recipient = candidate != null && hasText(candidate.getMail()) ? candidate.getMail() : nomination.getNominationEmail();
		Map<String, Object> variables = buildTemplateVariables(nomination.getElection(), nomination, candidate, null, supportNomination, null);
		queueEmailForTemplate(nomination.getElection(), EmailTemplateType.SUPPORT_REQUESTER_REMINDER, recipient, Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	private EmailTemplateType resolveSupportStatusCandidateTemplateType(SupportStatus supportStatus) {
		if (supportStatus == null) {
			return null;
		}

		if (supportStatus == SupportStatus.ACCEPTED || supportStatus == SupportStatus.APPROVED) {
			return EmailTemplateType.NOMINATION_SUPPORT_STATUS_APPROVED_CANDIDATE;
		}
		if (supportStatus == SupportStatus.REJECTED || supportStatus == SupportStatus.INVALID) {
			return EmailTemplateType.NOMINATION_SUPPORT_STATUS_REJECTED_CANDIDATE;
		}
		return null;
	}

	@Override
	public void queueNominationReminderToNominee(long nominationId) {
		Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNomination(nominationId);
		if (nomination == null || nomination.getElection() == null) {
			return;
		}

		Map<String, Object> variables = buildTemplateVariables(nomination.getElection(), nomination, nomination.getCandidate(), null, null, null);
		queueEmailForTemplate(nomination.getElection(), EmailTemplateType.NOMINATION_REMINDER, nomination.getNominationEmail(), Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	@Override
	public void queueCandidateReminderToCandidate(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		if (candidate == null || candidate.getElection() == null || candidate.isAbstention()) {
			return;
		}

		Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByCandidateId(candidateId);
		Map<String, Object> variables = buildTemplateVariables(candidate.getElection(), nomination, candidate, null, null, null);
		enrichCandidateReminderVariables(candidate, variables);
		queueEmailForTemplate(candidate.getElection(), EmailTemplateType.CANDIDATE_REMINDER, candidate.getMail(), Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	@Override
	public void queueAuditorReminderToAuditor(long auditorId) {
		Auditor auditor = ElectionsDaoFactory.createAuditorDao(em).getAuditor(auditorId);
		if (auditor == null || auditor.getElection() == null) {
			return;
		}

		Map<String, Object> variables = buildTemplateVariables(auditor.getElection(), null, null, auditor, null, null);
		enrichAuditorReminderVariables(auditor, variables);
		queueEmailForTemplate(auditor.getElection(), EmailTemplateType.AUDITOR_REMINDER, auditor.getMail(), Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	@Override
	public void queueAuditorRevisionReminderToAuditor(long auditorId) {
		Auditor auditor = ElectionsDaoFactory.createAuditorDao(em).getAuditor(auditorId);
		if (auditor == null || auditor.getElection() == null) {
			return;
		}

		Map<String, Object> variables = buildTemplateVariables(auditor.getElection(), null, null, auditor, null, null);
		queueEmailForTemplate(auditor.getElection(), EmailTemplateType.AUDITOR_REMINDER_REVISION, auditor.getMail(), Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	@Override
	public boolean queueLinkRecoveryEmail(long electionId, String recipientEmail, String linkTypeKey, String linkUrl) {
		if (electionId <= 0 || !hasText(recipientEmail) || !hasText(linkTypeKey) || !hasText(linkUrl)) {
			return false;
		}

		Election election = ElectionsDaoFactory.createElectionDao(em).getElection(electionId);
		if (election == null) {
			return false;
		}

		String normalizedRecipient = recipientEmail.trim();
		PublicLinkRecoveryType recoveryType = PublicLinkRecoveryType.fromKey(linkTypeKey);
		if (recoveryType == null) {
			return false;
		}
		Map<String, Object> variables = new HashMap<>();
		variables.put(VARIABLE_ELECTION, election);
		variables.put("linkRecoveryTypeKey", recoveryType.getKey());
		variables.put(LINK_RECOVERY_URL_VARIABLE, linkUrl.trim());
		variables.put(LINK_RECOVERY_TYPE_SP_VARIABLE, recoveryType.resolveLabel(LanguageCode.SP));
		variables.put(LINK_RECOVERY_TYPE_EN_VARIABLE, recoveryType.resolveLabel(LanguageCode.EN));
		variables.put(LINK_RECOVERY_TYPE_PT_VARIABLE, recoveryType.resolveLabel(LanguageCode.PT));

		ElectionEmailTemplate template = resolveTemplate(election, EmailTemplateType.LINK_RECOVERY);
		if (template == null) {
			template = buildLinkRecoveryFallbackTemplate();
		}
		return queueEmail(election, template, normalizedRecipient, Constants.DEFAULT_EMAIL_LANGUAGE, variables, EmailTemplateType.LINK_RECOVERY);
	}

	private void enrichCandidateReminderVariables(Candidate candidate, Map<String, Object> variables) {
		if (candidate == null || variables == null) {
			return;
		}
		List<String> pendingTasks = new ArrayList<>();
		List<String> completedTasks = new ArrayList<>();
		List<CandidateElectionTaskProgress> taskProgressRows = candidate.getTaskProgress();
		if (taskProgressRows != null) {
			for (CandidateElectionTaskProgress row : taskProgressRows) {
				if (row == null || row.getElectionTask() == null || row.getElectionTask().getTaskKey() == null) {
					continue;
				}
				String taskLabel = row.getElectionTask().getTaskKey().name();
				CandidateElectionTaskStatus status = row.getStatus();
				if (status == CandidateElectionTaskStatus.COMPLETED) {
					completedTasks.add(taskLabel);
				} else if (status == CandidateElectionTaskStatus.OMITTED) {
					completedTasks.add(taskLabel + " (OMITTED)");
				} else {
					pendingTasks.add(taskLabel);
				}
			}
		}

		variables.put("candidatePendingTasksCount", pendingTasks.size());
		variables.put("candidateCompletedTasksCount", completedTasks.size());
		variables.put("candidatePendingTasks", toBulletList(pendingTasks, "Ninguna"));
		variables.put("candidateCompletedTasks", toBulletList(completedTasks, "Ninguna"));
	}

	private void enrichAuditorReminderVariables(Auditor auditor, Map<String, Object> variables) {
		if (auditor == null || auditor.getElection() == null || variables == null) {
			return;
		}

		List<Candidate> electionCandidates = ElectionsDaoFactory.createCandidateDao(em).getElectionCandidates(auditor.getElection().getElectionId());
		Map<Long, AuditorCandidateDecision> decisionByCandidate = new HashMap<>();
		List<AuditorCandidateDecision> decisionRows = ElectionsDaoFactory.createAuditorCandidateDecisionDao(em).getElectionAuditorCandidateDecisions(auditor.getElection().getElectionId());
		for (AuditorCandidateDecision row : decisionRows) {
			if (row == null || row.getAuditor() == null || row.getCandidate() == null) {
				continue;
			}
			if (row.getAuditor().getAuditorId() != auditor.getAuditorId()) {
				continue;
			}
			decisionByCandidate.put(row.getCandidate().getCandidateId(), row);
		}

		List<String> pendingCandidates = new ArrayList<>();
		List<String> completedCandidates = new ArrayList<>();
		for (Candidate candidate : electionCandidates) {
			if (candidate == null || candidate.isAbstention()) {
				continue;
			}
			if (!AuditorCandidateDecisionUtils.isVisibleCandidateStatusForAuditor(candidate.getStatus())) {
				continue;
			}
			AuditorCandidateDecision decision = decisionByCandidate.get(candidate.getCandidateId());
			String candidateLabel = candidate.getName() + " (id=" + candidate.getCandidateId() + ")";
			if (auditor.isCommissioner() && AuditorCandidateDecisionUtils.isCandidateActionRequired(candidate.getStatus(), decision)) {
				pendingCandidates.add(candidateLabel);
			} else {
				completedCandidates.add(candidateLabel + " - " + resolveAuditorReminderCompletedStatus(decision, candidate.getStatus()));
			}
		}

		variables.put("auditorPendingCandidatesCount", pendingCandidates.size());
		variables.put("auditorCompletedCandidatesCount", completedCandidates.size());
		variables.put("auditorPendingCandidates", toBulletList(pendingCandidates, "Ninguno"));
		variables.put("auditorCompletedCandidates", toBulletList(completedCandidates, "Ninguno"));
	}

	private String resolveAuditorReminderCompletedStatus(AuditorCandidateDecision decision, CandidateStatus candidateStatus) {
		AuditorCandidateDecisionStatus status = AuditorCandidateDecisionUtils.resolveEffectiveAuditorDecisionStatus(decision, candidateStatus);
		if (status != null && status != AuditorCandidateDecisionStatus.ANALYZING) {
			return status.name();
		}
		return candidateStatus != null ? candidateStatus.name() : "-";
	}

	private String toBulletList(List<String> values, String emptyValue) {
		if (values == null || values.isEmpty()) {
			return "- " + emptyValue;
		}
		StringBuilder builder = new StringBuilder();
		for (String value : values) {
			if (!hasText(value)) {
				continue;
			}
			if (builder.length() > 0) {
				builder.append('\n');
			}
			builder.append("- ").append(value);
		}
		return builder.length() > 0 ? builder.toString() : "- " + emptyValue;
	}

	@Override
	public void queueCandidateConfirmedAndPublishedToCandidate(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		if (candidate == null || candidate.getElection() == null || candidate.isAbstention()) {
			return;
		}

		Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByCandidateId(candidateId);
		Map<String, Object> variables = buildTemplateVariables(candidate.getElection(), nomination, candidate, null, null, null);
		queueEmailForTemplate(candidate.getElection(), EmailTemplateType.CANDIDATE_CONFIRMED_AND_PUBLISHED, candidate.getMail(), Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	@Override
	public void queueCandidateTrainingAccessRequestedToElectionSender(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		if (candidate == null || candidate.getElection() == null) {
			return;
		}

		String candidateRecipient = hasText(candidate.getMail()) ? candidate.getMail().trim() : null;
		if (!hasText(candidateRecipient)) {
			return;
		}

		Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByCandidateId(candidateId);
		Map<String, Object> variables = buildTemplateVariables(candidate.getElection(), nomination, candidate, null, null, null);
		enrichCandidateReminderVariables(candidate, variables);
		variables.put("requestedTaskKey", ElectionTaskKey.COURSE.name());
		queueEmailForTemplate(
				candidate.getElection(),
				EmailTemplateType.CANDIDATE_TRAINING_ACCESS_REQUESTED_ADMIN,
				candidateRecipient,
				Constants.DEFAULT_EMAIL_LANGUAGE,
				variables);
	}

	@Override
	public void queueCandidateEvaluationAccessRequestedToElectionSender(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		if (candidate == null || candidate.getElection() == null) {
			return;
		}

		String senderRecipient = resolveElectionDefaultRecipient(candidate.getElection());
		if (!hasText(senderRecipient)) {
			return;
		}

		Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByCandidateId(candidateId);
		Map<String, Object> variables = buildTemplateVariables(candidate.getElection(), nomination, candidate, null, null, null);
		enrichCandidateReminderVariables(candidate, variables);
		variables.put("requestedTaskKey", ElectionTaskKey.EVALUATION.name());
		queueEmailForTemplate(
				candidate.getElection(),
				EmailTemplateType.CANDIDATE_EVALUATION_ACCESS_REQUESTED_ADMIN,
				senderRecipient,
				Constants.DEFAULT_EMAIL_LANGUAGE,
				variables);
	}

	@Override
	public void queueCandidateTaskCompletedToElectionSender(long candidateId, String taskKey) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		if (candidate == null || candidate.getElection() == null) {
			return;
		}

		String senderRecipient = resolveElectionDefaultRecipient(candidate.getElection());
		if (!hasText(senderRecipient)) {
			return;
		}

		Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByCandidateId(candidateId);
		Map<String, Object> variables = buildTemplateVariables(candidate.getElection(), nomination, candidate, null, null, null);
		String normalizedTaskKey = hasText(taskKey) ? taskKey : "";
		variables.put("completedTaskKey", normalizedTaskKey);
		variables.put("taskKey", normalizedTaskKey);
		queueEmailForTemplate(candidate.getElection(), EmailTemplateType.CANDIDATE_TASK_COMPLETED_ADMIN, senderRecipient, Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	@Override
	public void queueCandidateAllTasksCompletedToElectionSender(long candidateId) {
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		if (candidate == null || candidate.getElection() == null) {
			return;
		}

		String senderRecipient = resolveElectionDefaultRecipient(candidate.getElection());
		if (!hasText(senderRecipient)) {
			return;
		}

		Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByCandidateId(candidateId);
		Map<String, Object> variables = buildTemplateVariables(candidate.getElection(), nomination, candidate, null, null, null);
		queueEmailForTemplate(candidate.getElection(), EmailTemplateType.CANDIDATE_ALL_TASKS_COMPLETED_ADMIN, senderRecipient, Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	@Override
	public void queueAuditorCandidateApprovedToElectionSender(long auditorId, long candidateId, String comment) {
		queueAuditorCandidateDecisionToElectionSender(auditorId, candidateId, comment, EmailTemplateType.AUDITOR_CANDIDATE_APPROVED);
	}

	@Override
	public void queueAuditorCandidateRejectedToElectionSender(long auditorId, long candidateId, String comment) {
		queueAuditorCandidateDecisionToElectionSender(auditorId, candidateId, comment, EmailTemplateType.AUDITOR_CANDIDATE_REJECTED);
	}

	@Override
	public void queueTemplateEmail(Election election, EmailTemplateType templateType, String recipient, String cc, LanguageCode language, Map<String, Object> variables) {
		Election resolvedElection = resolveElectionWithVotingWindow(election);
		if (resolvedElection == null || templateType == null || !hasText(recipient)) {
			return;
		}

		ElectionEmailTemplate template = resolveTemplate(resolvedElection, templateType);
		queueEmailAndDispatchIfEnabled(resolvedElection, template, templateType, recipient, cc, language, variables);
	}

	private void queueAuditorCandidateDecisionToElectionSender(long auditorId, long candidateId, String comment, EmailTemplateType templateType) {
		Auditor auditor = ElectionsDaoFactory.createAuditorDao(em).getAuditor(auditorId);
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidate(candidateId);
		if (!areFromSameElection(auditor, candidate)) {
			return;
		}

		String senderRecipient = resolveElectionDefaultRecipient(candidate.getElection());
		if (!hasText(senderRecipient)) {
			return;
		}

		Map<String, Object> variables = new HashMap<>();
		variables.put(VARIABLE_ELECTION, candidate.getElection());
		String normalizedComment = hasText(comment) ? comment : "";
		variables.put("auditorDecisionComment", normalizedComment);
		variables.put("comment", normalizedComment);
		queueEmailForTemplate(candidate.getElection(), templateType, senderRecipient, Constants.DEFAULT_EMAIL_LANGUAGE, variables);
	}

	private boolean areFromSameElection(Auditor auditor, Candidate candidate) {
		return auditor != null && candidate != null && auditor.getElection() != null && candidate.getElection() != null && auditor.getElection().getElectionId() == candidate.getElection().getElectionId();
	}

	private void queueStandardDispatchNotice(Election election, EmailTemplateType targetTemplateType, String targetRecipient, LanguageCode targetLanguage, Map<String, Object> variables) {
		if (election == null || targetTemplateType == null) {
			return;
		}

		String standardRecipient = resolveElectionDefaultRecipient(election);
		if (!hasText(standardRecipient)) {
			return;
		}

		ElectionEmailTemplate standardTemplate = resolveTemplate(election, EmailTemplateType.STANDARD_DISPATCH_NOTICE);
		if (standardTemplate == null) {
			return;
		}

		Map<String, Object> standardVariables = buildStandardNoticeVariables(election, standardRecipient, standardTemplate, targetTemplateType, targetRecipient, targetLanguage, variables);
		queueEmail(election, standardTemplate, standardRecipient, Constants.DEFAULT_EMAIL_LANGUAGE, standardVariables);
	}

	private Map<String, Object> buildStandardNoticeVariables(Election election, String standardRecipient, ElectionEmailTemplate standardTemplate, EmailTemplateType targetTemplateType, String targetRecipient, LanguageCode targetLanguage, Map<String, Object> baseVariables) {
		Map<String, Object> variables = baseVariables == null ? new HashMap<>() : new HashMap<>(baseVariables);
		if (!variables.containsKey(VARIABLE_ELECTION)) {
			variables.put(VARIABLE_ELECTION, election);
		}

		LanguageCode normalizedTargetLanguage = normalizeLanguage(targetLanguage);
		Map<String, Object> targetPreview = renderTemplatePreview(election, targetTemplateType, normalizedTargetLanguage, variables);
		String eventType = targetTemplateType.resolveStandardDispatchEventType();
		String targetTemplateTypeKey = targetTemplateType.getKey();

		Map<String, Object> standardNotice = new HashMap<>();
		standardNotice.put(EmailTemplateType.KEY_EVENT_TYPE, eventType);
		standardNotice.put("createdAt", new Date());
		standardNotice.put("standardRecipient", standardRecipient);
		standardNotice.put("standardTemplateType", standardTemplate != null ? standardTemplate.getTemplateType() : null);
		standardNotice.put(EmailTemplateType.KEY_TARGET_TEMPLATE_TYPE, targetTemplateTypeKey);
		standardNotice.put("targetTemplateFound", targetPreview.get(PREVIEW_TEMPLATE_FOUND));
		standardNotice.put("targetLanguage", normalizedTargetLanguage != null ? normalizedTargetLanguage.name() : null);
		standardNotice.put("targetRecipient", targetRecipient);
		standardNotice.put("targetSender", resolveSender(election));
		standardNotice.put("targetSubject", targetPreview.get(PREVIEW_SUBJECT));
		standardNotice.put("targetBody", targetPreview.get("body"));

		Object nominationObject = variables.get("nomination");
		Object candidateObject = variables.get(VARIABLE_CANDIDATE);
		Object auditorObject = variables.get(VARIABLE_AUDITOR);
		Object supportNominationObject = variables.get("supportNomination");
		Nomination nomination = nominationObject instanceof Nomination ? (Nomination) nominationObject : null;
		Candidate candidate = candidateObject instanceof Candidate ? (Candidate) candidateObject : null;
		Auditor auditor = auditorObject instanceof Auditor ? (Auditor) auditorObject : null;
		SupportNomination supportNomination = supportNominationObject instanceof SupportNomination ? (SupportNomination) supportNominationObject : null;

		standardNotice.put("electionId", election != null ? election.getElectionId() : null);
		standardNotice.put("nominationId", nomination != null ? nomination.getId() : null);
		standardNotice.put("candidateId", candidate != null ? candidate.getCandidateId() : null);
		standardNotice.put("auditorId", auditor != null ? auditor.getAuditorId() : null);
		standardNotice.put("supportNominationId", supportNomination != null ? supportNomination.getId() : null);

		variables.put("standardNotice", standardNotice);
		variables.put(EmailTemplateType.KEY_EVENT_TYPE, eventType);
		variables.put(EmailTemplateType.KEY_TARGET_TEMPLATE_TYPE, targetTemplateTypeKey);
		variables.put("targetRecipient", targetRecipient);
		variables.put("targetLanguage", normalizedTargetLanguage != null ? normalizedTargetLanguage.name() : null);
		variables.put("targetSubject", targetPreview.get(PREVIEW_SUBJECT));
		variables.put("targetBody", targetPreview.get("body"));
		return variables;
	}

	private Map<String, Object> renderTemplatePreview(Election election, EmailTemplateType templateType, LanguageCode language, Map<String, Object> variables) {
		Map<String, Object> preview = new HashMap<>();
		preview.put(PREVIEW_TEMPLATE_FOUND, false);
		preview.put(PREVIEW_SUBJECT, "");
		preview.put("body", "");
		Election resolvedElection = resolveElectionWithVotingWindow(election);

		if (resolvedElection == null || templateType == null) {
			return preview;
		}

		ElectionEmailTemplate template = resolveTemplate(resolvedElection, templateType);
		if (template == null) {
			return preview;
		}
		preview.put(PREVIEW_TEMPLATE_FOUND, true);

		try {
			String templateSubject = getTemplateSubject(template, normalizeLanguage(language));
			String templateBody = getTemplateBody(template, normalizeLanguage(language));
			if (templateSubject == null) {
				templateSubject = "";
			}
			if (templateBody == null) {
				templateBody = "";
			}
			Map<String, Object> renderedVariables = enrichVariablesWithSignature(resolvedElection, normalizeLanguage(language), templateSubject, templateBody, variables);
			preview.put(PREVIEW_SUBJECT, processTemplate(templateSubject, renderedVariables));
			preview.put("body", processTemplate(templateBody, renderedVariables));
		} catch (Exception e) {
			appLogger.error("Error rendering target template preview. electionId={}, templateType={}", resolvedElection.getElectionId(), templateType.getKey(), e);
		}
		return preview;
	}

	private void queueEmailForTemplate(Election election, EmailTemplateType templateType, String recipient, LanguageCode language, Map<String, Object> variables) {
		Election resolvedElection = resolveElectionWithVotingWindow(election);
		if (resolvedElection == null || templateType == null || !hasText(recipient)) {
			return;
		}
		ElectionEmailTemplate template = resolveTemplate(resolvedElection, templateType);
		queueEmailAndDispatchIfEnabled(resolvedElection, template, templateType, recipient, language, variables);
	}

	private void queueEmailAndDispatchIfEnabled(Election election, ElectionEmailTemplate template, String recipient, LanguageCode language, Map<String, Object> variables) {
		EmailTemplateType templateType = template != null ? EmailTemplateType.fromKey(template.getTemplateType()) : null;
		queueEmailAndDispatchIfEnabled(election, template, templateType, recipient, language, variables);
	}

	private void queueEmailAndDispatchIfEnabled(Election election, ElectionEmailTemplate template, EmailTemplateType templateType, String recipient, LanguageCode language, Map<String, Object> variables) {
		queueEmailAndDispatchIfEnabled(election, template, templateType, recipient, null, language, variables);
	}

	private void queueEmailAndDispatchIfEnabled(Election election, ElectionEmailTemplate template, EmailTemplateType templateType, String recipient, String cc, LanguageCode language, Map<String, Object> variables) {
		if (template == null) {
			appLogger.error("Email not queued because the template was not found. electionId={}, templateType={}, recipient={}", election != null ? election.getElectionId() : null, templateType != null ? templateType.getKey() : null, recipient);
			return;
		}
		boolean emailQueued = queueEmail(election, template, recipient, cc, language, variables, templateType);
		if (!emailQueued) {
			return;
		}
		queueStandardDispatchNoticeIfEnabled(election, templateType, recipient, language, variables);
	}

	private void queueStandardDispatchNoticeIfEnabled(Election election, EmailTemplateType templateType, String targetRecipient, LanguageCode targetLanguage, Map<String, Object> variables) {
		if (templateType == null || !templateType.shouldSendStandardDispatch()) {
			return;
		}
		queueStandardDispatchNotice(election, templateType, targetRecipient, targetLanguage, variables);
	}

	private ElectionEmailTemplate resolveTemplate(Election election, EmailTemplateType templateType) {
		if (templateType == null) {
			return null;
		}
		return resolveTemplate(election, templateType.getKey());
	}

	private ElectionEmailTemplate resolveTemplate(Election election, String templateType) {
		try {
			ElectionEmailTemplate template = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getElectionTemplateByType(templateType, election.getElectionId());
			if (template == null) {
				template = ElectionsDaoFactory.createElectionEmailTemplateDao(em).getBaseTemplate(templateType);
			}
			if (template == null) {
				appLogger.warn("Email template not found. electionId={}, templateType={}", election.getElectionId(), templateType);
			}
			return template;
		} catch (Exception e) {
			appLogger.error("Error resolving email template. electionId={}, templateType={}", election != null ? election.getElectionId() : null, templateType, e);
			return null;
		}
	}

	private boolean queueEmail(Election election, ElectionEmailTemplate template, String recipient, LanguageCode language, Map<String, Object> variables) {
		return queueEmail(election, template, recipient, null, language, variables, null);
	}

	private boolean queueEmail(Election election, ElectionEmailTemplate template, String recipient, LanguageCode language, Map<String, Object> variables, EmailTemplateType fallbackTemplateType) {
		return queueEmail(election, template, recipient, null, language, variables, fallbackTemplateType);
	}

	private boolean queueEmail(Election election, ElectionEmailTemplate template, String recipient, String cc, LanguageCode language, Map<String, Object> variables, EmailTemplateType fallbackTemplateType) {
		Election resolvedElection = resolveElectionWithVotingWindow(election);
		if (resolvedElection == null || !hasText(recipient)) {
			return false;
		}
		try {
			LanguageCode normalizedLanguage = normalizeLanguage(language);
			String templateSubject = template != null ? getTemplateSubject(template, normalizedLanguage) : "";
			String templateBody = template != null ? getTemplateBody(template, normalizedLanguage) : "";
			if (templateSubject == null) {
				templateSubject = "";
			}
			if (templateBody == null) {
				templateBody = "";
			}
			String resolvedTemplateType = resolveTemplateTypeValue(template, fallbackTemplateType);
			boolean prioritized = resolvePrioritized(template, fallbackTemplateType);

			Map<String, Object> safeVariables = variables == null ? new HashMap<>() : new HashMap<>(variables);
			safeVariables.put(VARIABLE_ELECTION, resolvedElection);
			safeVariables = enrichVariablesWithSignature(resolvedElection, normalizedLanguage, templateSubject, templateBody, safeVariables);

			Email email = new Email(recipient, resolveSender(resolvedElection), processTemplate(templateSubject, safeVariables), processTemplate(templateBody, safeVariables), resolvedTemplateType, resolvedElection, prioritized);
			if (hasText(cc)) {
				email.setCc(cc.trim());
			}
			em.persist(email);
			return true;
		} catch (Exception e) {
			appLogger.error("Error queuing email. electionId={}, templateType={}, recipient={}", resolvedElection.getElectionId(), resolveTemplateTypeValue(template, fallbackTemplateType), recipient, e);
			return false;
		}
	}

	private String resolveTemplateTypeValue(ElectionEmailTemplate template, EmailTemplateType fallbackTemplateType) {
		if (template != null && hasText(template.getTemplateType())) {
			return template.getTemplateType();
		}
		if (fallbackTemplateType != null) {
			return fallbackTemplateType.getKey();
		}
		return UNKNOWN_TEMPLATE_TYPE;
	}

	private boolean resolvePrioritized(ElectionEmailTemplate template, EmailTemplateType fallbackTemplateType) {
		if (template != null && hasText(template.getTemplateType())) {
			EmailTemplateType resolvedType = EmailTemplateType.fromKey(template.getTemplateType());
			return resolvedType != null && resolvedType.isPrioritized();
		}
		return fallbackTemplateType != null && fallbackTemplateType.isPrioritized();
	}

	private Map<String, Object> buildTemplateVariables(Election election, Nomination nomination, Candidate candidate, Auditor auditor, SupportNomination supportNomination, String decisionStatus) {
		Map<String, Object> variables = new HashMap<>();
		Election resolvedElection = resolveElectionWithVotingWindow(election);
		if (resolvedElection != null) {
			variables.put(VARIABLE_ELECTION, resolvedElection);
		}
		if (nomination != null) {
			variables.put("nomination", nomination);
			if (nomination.getOrganization() != null) {
				variables.put(VARIABLE_ORGANIZATION, nomination.getOrganization());
			}
		}
		if (candidate != null) {
			variables.put(VARIABLE_CANDIDATE, candidate);
		}
		if (auditor != null) {
			variables.put(VARIABLE_AUDITOR, auditor);
		}
		if (supportNomination != null) {
			variables.put("supportNomination", supportNomination);
			if (supportNomination.getSupportingOrganization() != null) {
				variables.put("supportingOrganization", supportNomination.getSupportingOrganization());
			}
		}
		if (hasText(decisionStatus)) {
			variables.put("decisionStatus", decisionStatus);
			variables.put("status", decisionStatus);
		}
		return variables;
	}

	private Election resolveElectionWithVotingWindow(Election election) {
		if (election == null) {
			return null;
		}
		VotingPeriodResolver.applyVotingWindow(em, election);
		return election;
	}

	private LanguageCode normalizeLanguage(LanguageCode language) {
		return language != null ? language : Constants.DEFAULT_EMAIL_LANGUAGE;
	}

	private String getTemplateSubject(ElectionEmailTemplate template, LanguageCode language) {
		if (LanguageCode.EN == language) {
			return hasText(template.getSubjectEN()) ? template.getSubjectEN() : template.getSubjectSP();
		}
		if (LanguageCode.PT == language) {
			return hasText(template.getSubjectPT()) ? template.getSubjectPT() : template.getSubjectSP();
		}
		return template.getSubjectSP();
	}

	private String getTemplateBody(ElectionEmailTemplate template, LanguageCode language) {
		if (LanguageCode.EN == language) {
			return hasText(template.getBodyEN()) ? template.getBodyEN() : template.getBodySP();
		}
		if (LanguageCode.PT == language) {
			return hasText(template.getBodyPT()) ? template.getBodyPT() : template.getBodySP();
		}
		return template.getBodySP();
	}

	private String resolveSender(Election election) {
		if (election != null && hasText(election.getDefaultSender())) {
			return election.getDefaultSender().trim();
		}
		try {
			String systemDefaultSender = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.DEFAULT_SENDER);
			return hasText(systemDefaultSender) ? systemDefaultSender.trim() : null;
		} catch (Exception e) {
			appLogger.error("Error resolving default sender", e);
			return null;
		}
	}

	private String resolveElectionDefaultSender(Election election) {
		if (election == null) {
			return null;
		}
		return hasText(election.getDefaultSender()) ? election.getDefaultSender().trim() : null;
	}

	private String resolveElectionDefaultRecipient(Election election) {
		if (election != null && hasText(election.getDefaultRecipient())) {
			return election.getDefaultRecipient().trim();
		}
		try {
			String systemDefaultRecipient = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.DEFAULT_RECIPIENT);
			if (hasText(systemDefaultRecipient)) {
				return systemDefaultRecipient.trim();
			}
		} catch (Exception e) {
			appLogger.error("Error resolving default recipient", e);
		}
		String electionDefaultSender = resolveElectionDefaultSender(election);
		if (hasText(electionDefaultSender)) {
			return electionDefaultSender;
		}
		return resolveSender(election);
	}

	private String resolveNomineeRecipient(Nomination nomination) {
		if (nomination == null) {
			return null;
		}

		Candidate candidate = nomination.getCandidate();
		if (candidate != null && hasText(candidate.getMail())) {
			return candidate.getMail();
		}

		return nomination.getNominationEmail();
	}

	private ElectionEmailTemplate buildLinkRecoveryFallbackTemplate() {
		ElectionEmailTemplate template = new ElectionEmailTemplate();
		template.setTemplateType(EmailTemplateType.LINK_RECOVERY.getKey());
		String subject = "[Elections] Link recovery / Recuperacion de enlace / Recuperacao de link";
		template.setSubjectSP(subject);
		template.setSubjectEN(subject);
		template.setSubjectPT(subject);

		String body = "[English below]" + '\n'
				+ "[Português abaixo]" + '\n' + '\n'
				+ "Estimado/a," + '\n' + '\n'
				+ "Recibimos una solicitud para recuperar un enlace de acceso correspondiente a la elección:" + '\n' + '\n'
				+ "$election.titleSpanish" + '\n' + '\n'
				+ "Tipo de enlace: $" + LINK_RECOVERY_TYPE_SP_VARIABLE + '\n'
				+ LINK_RECOVERY_LINK_PREFIX + LINK_RECOVERY_URL_VARIABLE + '\n' + '\n'
				+ "Si usted no solicitó esta recuperación, puede ignorar este mensaje." + '\n' + '\n'
				+ "Si tiene dudas o necesita ayuda, puede responder a este correo." + '\n' + '\n'
				+ "--" + '\n' + '\n'
				+ "Dear recipient," + '\n' + '\n'
				+ "We received a request to recover an access link for the election:" + '\n' + '\n'
				+ "$election.titleEnglish" + '\n' + '\n'
				+ "Link type: $" + LINK_RECOVERY_TYPE_EN_VARIABLE + '\n'
				+ LINK_RECOVERY_LINK_PREFIX + LINK_RECOVERY_URL_VARIABLE + '\n' + '\n'
				+ "If you did not request this recovery, you may ignore this message." + '\n' + '\n'
				+ "If you have questions or need assistance, you may reply to this email." + '\n' + '\n'
				+ "--" + '\n' + '\n'
				+ "Prezado(a)," + '\n' + '\n'
				+ "Recebemos uma solicitação para recuperar um link de acesso correspondente à eleição:" + '\n' + '\n'
				+ "$election.titlePortuguese" + '\n' + '\n'
				+ "Tipo de link: $" + LINK_RECOVERY_TYPE_PT_VARIABLE + '\n'
				+ LINK_RECOVERY_LINK_PREFIX + LINK_RECOVERY_URL_VARIABLE + '\n' + '\n'
				+ "Se você não solicitou esta recuperação, pode ignorar esta mensagem." + '\n' + '\n'
				+ "Se tiver dúvidas ou precisar de ajuda, você pode responder a este e-mail." + '\n' + '\n'
				+ SIGNATURE_TOKEN;
		template.setBodySP(body);
		template.setBodyEN(body);
		template.setBodyPT(body);
		return template;
	}

	private Map<String, Object> enrichVariablesWithSignature(Election election, LanguageCode language, String templateSubject, String templateBody, Map<String, Object> variables) {
		Map<String, Object> safeVariables = variables == null ? new HashMap<>() : new HashMap<>(variables);
		if (!containsSignatureToken(templateSubject) && !containsSignatureToken(templateBody)) {
			return safeVariables;
		}

		String signatureBody = resolveSignatureBody(election, language, safeVariables);
		safeVariables.put("signature", signatureBody);
		return safeVariables;
	}

	private boolean containsSignatureToken(String template) {
		return template != null && (template.contains(SIGNATURE_TOKEN) || template.contains(SIGNATURE_TOKEN_BRACED));
	}

	private String resolveSignatureBody(Election election, LanguageCode language, Map<String, Object> variables) {
		String signatureTemplate = resolveSignatureTemplateBody(election, language);
		Map<String, Object> signatureVariables = variables == null ? new HashMap<>() : new HashMap<>(variables);
		signatureVariables.remove("signature");
		if (!signatureVariables.containsKey(VARIABLE_ELECTION) && election != null) {
			signatureVariables.put(VARIABLE_ELECTION, election);
		}

		String senderEmail = resolveElectionDefaultSender(election);
		if (!hasText(senderEmail)) {
			senderEmail = resolveSender(election);
		}
		signatureVariables.put(SIGNATURE_EMAIL_VARIABLE, hasText(senderEmail) ? senderEmail : "");

		try {
			return processTemplate(signatureTemplate, signatureVariables);
		} catch (Exception e) {
			appLogger.error("Error processing signature template. electionId={}", election != null ? election.getElectionId() : null, e);
			return signatureTemplate.replace("$email", hasText(senderEmail) ? senderEmail : "");
		}
	}

	private String resolveSignatureTemplateBody(Election election, LanguageCode language) {
		LanguageCode normalizedLanguage = normalizeLanguage(language);
		ElectionEmailTemplate signatureTemplate = resolveTemplate(election, EmailTemplateType.SIGNATURE);
		String body = signatureTemplate != null ? getTemplateBody(signatureTemplate, normalizedLanguage) : null;
		return hasText(body) ? body : "";
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String addVotes(List<Vote> votes) {
		if (votes == null || votes.isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (Vote vote : votes) {
			if (vote == null || vote.getCandidate() == null) {
				continue;
			}
			if (builder.length() > 0) {
				builder.append('\n');
			}
			builder.append(vote.getCode()).append(" / ").append(vote.getCandidate().getName());
		}
		return builder.toString();
	}

	private String processTemplate(String template, Map<String, Object> variables) throws IOException {
		if (template == null) {
			return "";
		}
		VelocityEngine velocityEngine = new VelocityEngine();
		velocityEngine.init();
		VelocityContext context = new VelocityContext();

		Map<String, Object> safeVariables = variables == null ? new HashMap<>() : variables;
		for (Map.Entry<String, Object> entry : safeVariables.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}

		StringWriter stringWriter = new StringWriter();
		Velocity.evaluate(context, stringWriter, "email-template", template);
		return stringWriter.toString();
	}

	@Override
	public List<Email> getEmailsToSend(int maxResults) {
		return ElectionsDaoFactory.createEmailDao(em).getPendingSendEmailsOrdered(maxResults);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void markEmailAsSent(Email email) {
		email.setSent(true);
		em.merge(email);
	}

	@Override
	public void moveEmailsToHistory() {
		List<Email> emails = ElectionsDaoFactory.createEmailDao(em).getEmailsOlderOneMonth();
		for (Email email : emails) {
			em.persist(new EmailHistory(email));
			em.remove(email);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void purgeTables() {

		Session session = em.unwrap(Session.class);

		session.doWork(connection -> {
			try {
				executeVacuum(connection, SIGNATURE_EMAIL_VARIABLE);
				executeVacuum(connection, "uservoter");
				executeVacuum(connection, VARIABLE_CANDIDATE);
				executeVacuum(connection, VARIABLE_ELECTION);
				executeVacuum(connection, "activity");
				executeVacuum(connection, VARIABLE_ORGANIZATION);

				execute(connection, "VACUUM");
				execute(connection, "VACUUM FULL");

			} catch (SQLException e) {
				appLogger.error("Error executing VACUUM", e);
			}
		});
	}

	@Override
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void purgeCensusTablesAsync(long electionId) {
		if (ElectionsCaches.isElectionProcessing(electionId)) {
			appLogger.info("Skipping census tables purge because election processing is still running. electionId={}", electionId);
			return;
		}
		Session session = em.unwrap(Session.class);
		session.doWork(connection -> {
			try {
				execute(connection, "VACUUM (ANALYZE) uservoter");
				execute(connection, "VACUUM (ANALYZE) vote");
				appLogger.info("Census tables purge completed. electionId={}", electionId);
			} catch (SQLException e) {
				appLogger.error("Error executing census tables purge. electionId={}", electionId, e);
			}
		});
	}

	private void executeVacuum(Connection connection, String table) throws SQLException {
		execute(connection, "VACUUM " + table);
		execute(connection, "VACUUM FULL " + table);
	}

	private void execute(Connection connection, String sql) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.execute();
			appLogger.info("Executed: {}", sql);
		}
	}

}
