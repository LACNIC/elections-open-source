package net.lacnic.elections.ejb.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.lacnic.elections.campus.CampusClient;
import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.dao.NominationDao;
import net.lacnic.elections.dao.SupportNominationDao;
import net.lacnic.elections.domain.Activity;
import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.ReminderFrequency;
import net.lacnic.elections.domain.pre.CandidateCampusCourseStatus;
import net.lacnic.elections.domain.pre.CandidateCountryLink;
import net.lacnic.elections.domain.pre.CandidateDeclarationCode;
import net.lacnic.elections.domain.pre.CandidateDeclarationDefinition;
import net.lacnic.elections.domain.pre.CandidateDeclarationInputType;
import net.lacnic.elections.domain.pre.CandidateDeclarationOptionDefinition;
import net.lacnic.elections.domain.pre.CandidateDeclarationsDefinition;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateElectionTaskStatus;
import net.lacnic.elections.domain.pre.CandidateEvaluationStatus;
import net.lacnic.elections.domain.pre.CandidatePepDeclaration;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import net.lacnic.elections.domain.pre.CandidateQuestionStatus;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.CandidateTextImprovementInstruction;
import net.lacnic.elections.domain.pre.CandidateTextImprovementResponse;
import net.lacnic.elections.domain.pre.CandidateTrainingCredentialsRequestResult;
import net.lacnic.elections.domain.pre.CandidateWorkOrganization;
import net.lacnic.elections.domain.pre.ElectionCalendar;
import net.lacnic.elections.domain.pre.ElectionCalendarKey;
import net.lacnic.elections.domain.pre.ElectionTask;
import net.lacnic.elections.domain.pre.ElectionTaskKey;
import net.lacnic.elections.domain.pre.Nomination;
import net.lacnic.elections.domain.pre.NominationStatus;
import net.lacnic.elections.domain.pre.Organization;
import net.lacnic.elections.domain.pre.PublicNominationSubmission;
import net.lacnic.elections.domain.pre.PublicNominationSubmissionResult;
import net.lacnic.elections.domain.pre.SupportNomination;
import net.lacnic.elections.domain.pre.SupportStatus;
import net.lacnic.elections.domain.pre.TaskDependencyLevel;
import net.lacnic.elections.ejb.ElectionsPreNominationEJB;
import net.lacnic.elections.utils.AuthorizedEmailListUtils;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.StringUtils;
import net.lacnic.elections.utils.CountryUtils;
import net.lacnic.elections.utils.CriticalOperationsLoggerUtils;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.ElectionsCaches;
import net.lacnic.elections.utils.FilesUtils;
import net.lacnic.elections.utils.HtmlSanitizerUtils;
import net.lacnic.elections.utils.OpenAiClient;
import net.lacnic.elections.utils.OpenAiPromptDefaults;
import net.lacnic.elections.utils.PublicNominationConfiguration;
import net.lacnic.evra.registro.CategoriasEnum;

@Stateless
@Remote(ElectionsPreNominationEJB.class)
public class ElectionsPreNominationEJBBean implements ElectionsPreNominationEJB {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");
	private static final String TEXT_ELECCION = " para la elección ";
	private static final String ACTIVITY_SOURCE_FRAGMENT = ". fuente=";
	private static final String ACTIVITY_CANDIDATE_ID_FRAGMENT = ", candidateId=";
	private static final String DEFAULT_CANDIDATE_NAME = "Candidato/a";
	private static final String ACTIVITY_SOURCE_ADMIN_PANEL = "ADMIN_PANEL";
	private static final String ACTIVITY_SOURCE_CANDIDATE_LINK = "CANDIDATE_LINK";
	private static final String CANDIDATE_ACTIVITY_PREFIX = "CANDIDATE_";
	private static final int MAX_ACTIVE_ORG_SUPPORTS = 2;
	private static final int MIN_ORGANIZATION_NAME_LENGTH = 2;
	private static final int MAX_ORGANIZATION_NAME_LENGTH = 1000;
	private static final int MIN_NOMINATOR_NAME_LENGTH = 2;
	private static final int MAX_NOMINATOR_NAME_LENGTH = 1000;
	private static final int MIN_NOMINATION_NAME_LENGTH = 3;
	private static final int MAX_NOMINATION_NAME_LENGTH = 255;
	private static final int MAX_EMAIL_LENGTH = 255;
	private static final int MAX_NOMINATION_PHONE_COUNTRY_CODE_LENGTH = 5;
	private static final int MIN_NOMINATION_PHONE_LOCAL_LENGTH = 5;
	private static final int MAX_NOMINATION_PHONE_LOCAL_LENGTH = 50;
	private static final int MAX_NOMINATION_REASON_LENGTH = 2000;
	private static final int DEFAULT_MAX_DAILY_TEXT_IMPROVEMENT_REQUESTS = 50;
	private static final String AI_SCOPE_CANDIDATE_PREFIX = "CANDIDATE:";
	private static final String AI_SCOPE_ORGANIZATION_PREFIX = "ORG:";
	private static final String AI_SCOPE_IP_PREFIX = "IP:";
	private static final String CAMPUS_TRAINING_ROLE_ID = "5";
	private static final String DEBUG_NA = "-";

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	@Override
	public Nomination verifyAcceptNominationAccess(String token) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination != null && nomination.getCandidate() != null) {
				// Force initialization before leaving EJB context to avoid
				// LazyInitializationException in web layer.
				if (nomination.getCandidate().getWorkOrganizations() != null) {
					nomination.getCandidate().getWorkOrganizations().size();
				}
				if (nomination.getCandidate().getCountryLinks() != null) {
					nomination.getCandidate().getCountryLinks().size();
				}
				if (nomination.getCandidate().getElection() != null && nomination.getCandidate().getElection().getRestrictedCountries() != null) {
					nomination.getCandidate().getElection().getRestrictedCountries().size();
				}
			}
			if (nomination != null && nomination.getSupports() != null) {
				nomination.getSupports().size();
				for (SupportNomination support : nomination.getSupports()) {
					if (support == null) {
						continue;
					}
					support.getSupportStatus();
					support.getSupportStatusDate();
					support.getSupportingContactName();
					support.getSupportingContactEmail();
					if (support.getSupportingOrganization() != null) {
						support.getSupportingOrganization().getId();
						support.getSupportingOrganization().getOrgId();
						support.getSupportingOrganization().getName();
						support.getSupportingOrganization().getCountry();
						support.getSupportingOrganization().getMembershipContactId();
						support.getSupportingOrganization().getMembershipContactName();
						support.getSupportingOrganization().getMembershipContactEmail();
					}
				}
			}
			return nomination;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public Organization verifyDoNominationAccess(String token) {
		try {
			Organization organization = ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByDoNominationToken(token);
			if (organization == null) {
				return null;
			}
			if (organization.isDeudor()) {
				return null;
			}

			if (organization.getElection() != null && organization.getElection().getRestrictedCountries() != null) {
				organization.getElection().getRestrictedCountries().size();
			}

			if (organization.getNominations() != null) {
				organization.getNominations().size();
				for (Nomination nomination : organization.getNominations()) {
					if (nomination == null) {
						continue;
					}
					nomination.getId();
					nomination.getStatus();
					nomination.getNominationDate();
					nomination.getNominationName();
					nomination.getNominationEmail();
					nomination.getNominationPhoneNumber();
					nomination.getNominationReason();
				}
			}

			return organization;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public boolean doNomination(String token, Nomination nominationData, String clientIp) {
		try {
			if (!hasText(token) || nominationData == null) {
				return false;
			}

			Organization organization = ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByDoNominationToken(token);
			if (organization == null || organization.getElection() == null) {
				return false;
			}
			if (organization.isDeudor()) {
				return false;
			}
			if (!isDoNominationWindowOpen(organization)) {
				return false;
			}

			String normalizedName = trimToNull(nominationData.getNominationName());
			String normalizedEmail = trimToNull(nominationData.getNominationEmail());
			String normalizedPhoneNumber = trimToNull(nominationData.getNominationPhoneNumber());
			String normalizedReason = sanitizePlainTextToNull(nominationData.getNominationReason());

			if (!hasText(normalizedName) || normalizedName.length() < 3 || normalizedName.length() > 255) {
				return false;
			}
			if (!hasText(normalizedEmail) || normalizedEmail.length() > 255 || !isValidEmail(normalizedEmail)) {
				return false;
			}
			if (!AuthorizedEmailListUtils.isEmailAuthorized(normalizedEmail, organization.getElection().getAuthorizedNominateEmails())) {
				return false;
			}
			if (!hasValidNominationPhoneNumber(normalizedPhoneNumber)) {
				return false;
			}
			if (!hasText(normalizedReason) || normalizedReason.length() < 20 || normalizedReason.length() > MAX_NOMINATION_REASON_LENGTH) {
				return false;
			}

			Nomination nomination = new Nomination(organization.getElection(), organization, NominationStatus.PROPOSED, StringUtils.createSecureToken(), new DateTime().toString("yyyy-MM-dd HH:mm:ss"), normalizedName, normalizedEmail, normalizedPhoneNumber, normalizedReason);
			em.persist(nomination);

			EJBFactory.getInstance().getMailsSendingEJB().queueNominationSubmittedToNominee(nomination.getId());
			return true;
		} catch (Exception e) {
			appLogger.error("Error persisting nomination from do-nomination flow", e);
			return false;
		}
	}

	@Override
	public PublicNominationSubmissionResult submitPublicNomination(String publicElectionToken, PublicNominationSubmission submission, String clientIp) {
		try {
			if (!hasText(publicElectionToken) || submission == null) {
				return PublicNominationSubmissionResult.failure("publicNominationFormSubmitUnavailable");
			}

			Election election = findElectionByPublicToken(publicElectionToken);
			if (!isPublicNominationEnabled(election)) {
				return PublicNominationSubmissionResult.failure("publicNominationFormSubmitUnavailable");
			}

			String organizationName = normalizeSingleLineText(submission.getOrganizationName());
			String organizationCountry = normalizeCountryCode(submission.getOrganizationCountry());
			String nominatorName = normalizeSingleLineText(submission.getNominatorName());
			String nominatorEmail = normalizeEmail(submission.getNominatorEmail());
			String nomineeName = normalizeSingleLineText(submission.getNomineeName());
			String nomineeEmail = normalizeEmail(submission.getNomineeEmail());
			String nomineePhoneNumber = normalizeSingleLineText(submission.getNomineePhoneNumber());
			String nominationReason = sanitizePlainTextToNull(submission.getNominationReason());
			LanguageCode membershipLanguage = LanguageCode.fromValueOrDefault(trimToNull(submission.getUiLanguage()), LanguageCode.SP);

			if (!isValidOrganizationName(organizationName)
					|| !isValidOrganizationCountry(organizationCountry)
					|| !isValidNominatorName(nominatorName)
					|| !isValidNormalizedEmail(nominatorEmail)
					|| !isValidNomineeName(nomineeName)
					|| !isValidNormalizedEmail(nomineeEmail)
					|| !hasValidNominationPhoneNumber(nomineePhoneNumber)
					|| !isValidNominationReason(nominationReason)) {
				return PublicNominationSubmissionResult.failure("publicNominationFormSubmitInvalid");
			}

			if (!AuthorizedEmailListUtils.isEmailAuthorized(nomineeEmail, election.getAuthorizedNominateEmails())) {
				return PublicNominationSubmissionResult.failure("publicNominationFormSubmitUnauthorizedNominee");
			}

			NominationDao nominationDao = ElectionsDaoFactory.createNominationDao(em);
			if (nominationDao.existsElectionNominationByNormalizedMembershipContactEmailAndStatuses(
					election.getElectionId(),
					nominatorEmail,
					NominationStatus.blockingForNewNomination())) {
				return PublicNominationSubmissionResult.failure("publicNominationFormSubmitDuplicateNominator");
			}

			if (nominationDao.existsElectionNominationByNormalizedNominationEmailAndStatuses(
					election.getElectionId(),
					nomineeEmail,
					NominationStatus.blockingForNewNomination())) {
				return PublicNominationSubmissionResult.failure("publicNominationFormSubmitDuplicateNominee");
			}

			Organization organization = buildSyntheticOrganization(
					election,
					organizationName,
					organizationCountry,
					nominatorName,
					nominatorEmail,
					membershipLanguage);
			em.persist(organization);

			Nomination nomination = new Nomination(
					election,
					organization,
					NominationStatus.PROPOSED,
					StringUtils.createSecureToken(),
					new DateTime().toString("yyyy-MM-dd HH:mm:ss"),
					nomineeName,
					nomineeEmail,
					nomineePhoneNumber,
					nominationReason);
			em.persist(nomination);

			EJBFactory.getInstance().getMailsSendingEJB().queueNominationSubmittedToNominee(nomination.getId());
			return PublicNominationSubmissionResult.success("publicNominationFormSubmitSuccess");
		} catch (Exception e) {
			appLogger.error("Error persisting nomination from public-election flow", e);
			return PublicNominationSubmissionResult.failure("publicNominationFormSubmitError");
		}
	}

	private Election findElectionByPublicToken(String publicElectionToken) {
		try {
			return ElectionsDaoFactory.createElectionDao(em).getElectionByQuestionToken(publicElectionToken);
		} catch (Exception e) {
			return null;
		}
	}

	private boolean isDoNominationWindowOpen(Organization organization) {
		if (organization == null || organization.getElection() == null || !organization.getElection().isDoNominationLinkAvailable()) {
			return false;
		}
		return isElectionN2WindowOpen(organization.getElection().getElectionId());
	}

	private boolean hasValidNominationPhoneNumber(String phoneNumber) {
		if (!hasText(phoneNumber)) {
			return false;
		}

		int separatorIndex = phoneNumber.indexOf(' ');
		if (separatorIndex <= 0 || separatorIndex == phoneNumber.length() - 1) {
			return false;
		}

		String countryCode = trimToNull(phoneNumber.substring(0, separatorIndex));
		String localPhone = trimToNull(phoneNumber.substring(separatorIndex + 1));

		return hasText(countryCode) && hasText(localPhone) && countryCode.length() <= MAX_NOMINATION_PHONE_COUNTRY_CODE_LENGTH && localPhone.length() >= MIN_NOMINATION_PHONE_LOCAL_LENGTH && localPhone.length() <= MAX_NOMINATION_PHONE_LOCAL_LENGTH;
	}

	private boolean isPublicNominationEnabled(Election election) {
		if (election == null) {
			return false;
		}
		String configuredValue = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.PUBLIC_NOMINATION_ENABLED);
		if (!PublicNominationConfiguration.isEnabled(configuredValue, election.getEffectiveElectionType())) {
			return false;
		}
		return isElectionN2WindowOpen(election.getElectionId());
	}

	private Organization buildSyntheticOrganization(Election election, String organizationName, String organizationCountry, String nominatorName, String nominatorEmail, LanguageCode membershipLanguage) {
		Organization organization = new Organization();
		organization.setElection(election);
		organization.setOrgId(generateSyntheticOrganizationId(election.getElectionId()));
		organization.setName(organizationName);
		organization.setVotes(0);
		organization.setCategoryEnum(CategoriasEnum.NONE);
		organization.setCountry(organizationCountry);
		organization.setCnpj(null);
		organization.setAsn(null);
		organization.setMembershipContactId(generateSyntheticMembershipContactId());
		organization.setMembershipContactName(nominatorName);
		organization.setMembershipContactEmail(nominatorEmail);
		organization.setMembershipContactLanguageEnum(membershipLanguage);
		organization.setDoNominationToken(StringUtils.createSecureToken());
		organization.setDeudor(false);
		organization.setMember(false);
		return organization;
	}

	private String generateSyntheticOrganizationId(long electionId) {
		for (int attempt = 0; attempt < 10; attempt++) {
			String candidateId = "PUBLIC-NOM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
			Organization existing = ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByElectionAndOrgId(electionId, candidateId);
			if (existing == null) {
				return candidateId;
			}
		}
		return "PUBLIC-NOM-" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
	}

	private String generateSyntheticMembershipContactId() {
		return "public-contact-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}

	private boolean isValidOrganizationName(String value) {
		return hasText(value) && value.length() >= MIN_ORGANIZATION_NAME_LENGTH && value.length() <= MAX_ORGANIZATION_NAME_LENGTH;
	}

	private boolean isValidOrganizationCountry(String countryCode) {
		return normalizeCountryCode(countryCode) != null;
	}

	private boolean isValidNominatorName(String value) {
		return hasText(value) && value.length() >= MIN_NOMINATOR_NAME_LENGTH && value.length() <= MAX_NOMINATOR_NAME_LENGTH;
	}

	private boolean isValidNomineeName(String value) {
		return hasText(value) && value.length() >= MIN_NOMINATION_NAME_LENGTH && value.length() <= MAX_NOMINATION_NAME_LENGTH;
	}

	private boolean isValidNormalizedEmail(String value) {
		return hasText(value) && value.length() <= MAX_EMAIL_LENGTH && isValidEmail(value);
	}

	private boolean isValidNominationReason(String value) {
		return hasText(value) && value.length() >= 20 && value.length() <= MAX_NOMINATION_REASON_LENGTH;
	}

	@Override
	public SupportNomination verifySupportNominationAccess(String token) {
		try {
			SupportNomination supportNomination = ElectionsDaoFactory.createSupportNominationDao(em).getSupportNominationByToken(token);
			if (isDebtorOrganizationSupport(supportNomination)) {
				return null;
			}
			return supportNomination;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			appLogger.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public Candidate saveCandidateProfile(String token, Candidate candidateData, String actor, String ip) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || candidateData == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return null;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return null;
			}

			Candidate candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate == null) {
				return null;
			}

			copyCandidateProfile(candidate, candidateData, nomination);
			if (!hasValidPictureData(candidate)) {
				return null;
			}

			candidate = em.merge(candidate);
			touchTaskLastUpdate(token, ElectionTaskKey.PROFILE);

			em.flush();
			persistCandidateEditActivity(actor, candidate, ip);
			return candidate;
		} catch (Exception e) {
			appLogger.error("Error saving candidate profile", e);
			return null;
		}
	}

	@Override
	public Candidate saveCandidateIncompatibilities(String token, Candidate candidateData, String actor, String ip) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || candidateData == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return null;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return null;
			}

			Candidate candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate == null) {
				return null;
			}

			copyCandidateIncompatibilities(candidate, candidateData);
			candidate = em.merge(candidate);
			touchTaskLastUpdate(token, ElectionTaskKey.INCOMPATIBILITIES);

			em.flush();
			persistCandidateIncompatibilitiesEditActivity(actor, candidate, ip);
			return candidate;
		} catch (Exception e) {
			appLogger.error("Error saving candidate incompatibilities", e);
			return null;
		}
	}

	@Override
	public Candidate saveCandidateCountries(String token, Candidate candidateData, String actor, String ip) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || candidateData == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return null;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return null;
			}

			Candidate candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate == null) {
				return null;
			}

			copyCandidateCountries(candidate, candidateData);
			candidate = em.merge(candidate);
			touchTaskLastUpdate(token, ElectionTaskKey.COUNTRIES);

			em.flush();
			persistCandidateCountriesEditActivity(actor, candidate, ip);
			return candidate;
		} catch (Exception e) {
			appLogger.error("Error saving candidate countries", e);
			return null;
		}
	}

	@Override
	public Candidate saveCandidateDeclarations(String token, Candidate candidateData, String actor, String ip) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || candidateData == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return null;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return null;
			}

			Candidate candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate == null) {
				return null;
			}

			ElectionType electionType = resolveElectionType(candidate.getElection());
			if (!hasValidDeclarations(candidateData, electionType)) {
				return null;
			}

			copyCandidateDeclarations(candidate, candidateData, electionType);
			candidate = em.merge(candidate);
			touchTaskLastUpdate(token, ElectionTaskKey.DECLARATIONS);

			em.flush();
			persistCandidateDeclarationsEditActivity(actor, candidate, ip);
			return candidate;
		} catch (Exception e) {
			appLogger.error("Error saving candidate declarations", e);
			return null;
		}
	}

	@Override
	public CandidateDeclarationsDefinition getCandidateDeclarationsDefinition(String token, String language) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			ElectionType electionType = resolveElectionType(nomination != null ? nomination.getElection() : null);

			String candidateName = null;
			if (nomination != null && nomination.getCandidate() != null && hasText(nomination.getCandidate().getName())) {
				candidateName = nomination.getCandidate().getName();
			} else if (nomination != null && hasText(nomination.getNominationName())) {
				candidateName = nomination.getNominationName();
			}

			return buildCandidateDeclarationsDefinition(electionType, candidateName, language);
		} catch (Exception e) {
			appLogger.error("Error loading candidate declarations definition", e);
			return buildCandidateDeclarationsDefinition(null, null, language);
		}
	}

	@Override
	public Candidate saveCandidateNonStatutoryDeclarations(String token, Candidate candidateData, String actor, String ip) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || candidateData == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return null;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return null;
			}

			Candidate candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate == null) {
				return null;
			}

			ElectionType electionType = resolveElectionType(candidate.getElection());
			if (!hasValidNonStatutoryDeclarations(candidateData, electionType)) {
				return null;
			}

			copyCandidateNonStatutoryDeclarations(candidate, candidateData, electionType);
			candidate = em.merge(candidate);
			touchTaskLastUpdate(token, ElectionTaskKey.DECLARATIONS_NON_STATUTORY);

			em.flush();
			persistCandidateNonStatutoryDeclarationsEditActivity(actor, candidate, ip);
			return candidate;
		} catch (Exception e) {
			appLogger.error("Error saving candidate non-statutory declarations", e);
			return null;
		}
	}

	@Override
	public CandidateDeclarationsDefinition getCandidateNonStatutoryDeclarationsDefinition(String token, String language) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			ElectionType electionType = resolveElectionType(nomination != null ? nomination.getElection() : null);

			String candidateName = null;
			if (nomination != null && nomination.getCandidate() != null && hasText(nomination.getCandidate().getName())) {
				candidateName = nomination.getCandidate().getName();
			} else if (nomination != null && hasText(nomination.getNominationName())) {
				candidateName = nomination.getNominationName();
			}

			return buildCandidateNonStatutoryDeclarationsDefinition(electionType, candidateName, language);
		} catch (Exception e) {
			appLogger.error("Error loading candidate non-statutory declarations definition", e);
			return buildCandidateNonStatutoryDeclarationsDefinition(null, null, language);
		}
	}

	@Override
	public Candidate saveCandidateOtherStatutoryQuestions(String token, Candidate candidateData, String actor, String ip) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || candidateData == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return null;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return null;
			}

			Candidate candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate == null) {
				return null;
			}

			copyCandidateOtherStatutoryQuestions(candidate, candidateData);
			candidate = em.merge(candidate);
			touchTaskLastUpdate(token, ElectionTaskKey.OTHER_STATUTORY_QUESTIONS);

			em.flush();
			persistCandidateOtherStatutoryQuestionsEditActivity(actor, candidate, ip);
			return candidate;
		} catch (Exception e) {
			appLogger.error("Error saving candidate other statutory questions", e);
			return null;
		}
	}

	@Override
	public List<String> getOtherStatutoryQuestions(String language) {
		String normalizedLanguage = normalizeLanguage(language);
		List<String> questions = new ArrayList<>();
		questions.add(getDeclarationParameterValue("OTHER_STATUTORY_Q1_LABEL", normalizedLanguage, getDefaultOtherStatutoryQuestionLabel(1, normalizedLanguage)));
		questions.add(getDeclarationParameterValue("OTHER_STATUTORY_Q2_LABEL", normalizedLanguage, getDefaultOtherStatutoryQuestionLabel(2, normalizedLanguage)));
		questions.add(getDeclarationParameterValue("OTHER_STATUTORY_Q3_LABEL", normalizedLanguage, getDefaultOtherStatutoryQuestionLabel(3, normalizedLanguage)));
		questions.add(getDeclarationParameterValue("OTHER_STATUTORY_Q4_LABEL", normalizedLanguage, getDefaultOtherStatutoryQuestionLabel(4, normalizedLanguage)));
		return questions;
	}

	@Override
	public Candidate saveCandidateOtherNonStatutoryQuestions(String token, Candidate candidateData, String actor, String ip) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || candidateData == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return null;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return null;
			}

			Candidate candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate == null) {
				return null;
			}

			copyCandidateOtherNonStatutoryQuestions(candidate, candidateData);
			candidate = em.merge(candidate);
			touchTaskLastUpdate(token, ElectionTaskKey.OTHER_NON_STATUTORY_QUESTIONS);

			em.flush();
			persistCandidateOtherNonStatutoryQuestionsEditActivity(actor, candidate, ip);
			return candidate;
		} catch (Exception e) {
			appLogger.error("Error saving candidate other non statutory questions", e);
			return null;
		}
	}

	@Override
	public List<String> getOtherNonStatutoryQuestions(String language) {
		String normalizedLanguage = normalizeLanguage(language);
		List<String> questions = new ArrayList<>();
		questions.add(getDeclarationParameterValue("OTHER_NON_STATUTORY_Q1_LABEL", normalizedLanguage, getDefaultOtherNonStatutoryQuestionLabel(normalizedLanguage)));
		return questions;
	}

	@Override
	public String improveCandidateText(String token, String originalText, CandidateTextImprovementInstruction instruction) {
		return improveCandidateText(token, originalText, instruction, null);
	}

	@Override
	public String improveCandidateText(String token, String originalText, CandidateTextImprovementInstruction instruction, String questionText) {
		CandidateTextImprovementResponse response = improveCandidateTextWithStatus(token, originalText, instruction, questionText);
		if (response == null || !response.isProcessed()) {
			return null;
		}
		return response.getImprovedText();
	}

	@Override
	public CandidateTextImprovementResponse improveCandidateTextWithStatus(String token, String originalText, CandidateTextImprovementInstruction instruction, String questionText) {
		return improveCandidateTextWithStatus(token, originalText, instruction, questionText, null);
	}

	@Override
	public CandidateTextImprovementResponse improveCandidateTextWithStatus(String token, String originalText, CandidateTextImprovementInstruction instruction, String questionText, String clientIp) {
		if (!OpenAiClient.isTextImprovementEnabled()) {
			return CandidateTextImprovementResponse.processingError();
		}
		Candidate candidate = null;
		boolean openAiRequestAttempted = false;
		boolean openAiRequestSuccess = false;
		try {
			String sanitizedOriginalText = sanitizePlainTextToNull(originalText);
			String sanitizedQuestionText = sanitizePlainTextToNull(questionText);
			if (!hasText(sanitizedOriginalText) || instruction == null) {
				return CandidateTextImprovementResponse.processingError();
			}

			TextImprovementAccessContext accessContext = resolveTextImprovementAccessContext(token, clientIp);
			if (accessContext == null || !hasText(accessContext.getRateLimitScopeKey())) {
				appLogger.warn("Text improvement rejected due to invalid token and missing clientIp. tokenPrefix={}", buildSafeTokenPrefix(token));
				return CandidateTextImprovementResponse.processingError();
			}

			candidate = accessContext.getCandidate();
			int maxDailyTextImprovementRequests = resolveTextImprovementMaxDailyRequests();
			if (isTextImprovementDailyLimitReached(accessContext.getRateLimitScopeKey(), maxDailyTextImprovementRequests)) {
				appLogger.warn("Text improvement daily rate limit reached. scope={}, maxPerDay={}", accessContext.getRateLimitScopeKey(), maxDailyTextImprovementRequests);
				return CandidateTextImprovementResponse.dailyLimitReached();
			}
			if (!accessContext.isOpenAiAllowed()) {
				appLogger.warn("Text improvement rejected due to invalid token. tokenPrefix={}, ip={}", buildSafeTokenPrefix(token), trimToNull(clientIp));
				return CandidateTextImprovementResponse.processingError();
			}

			String promptKeyPrefix = instruction.getParameterKeyPrefix();
			String prompt = getTextImprovementPrompt(instruction);
			if (!hasText(prompt)) {
				appLogger.warn("Missing text improvement prompt parameter. parameterKey={}", promptKeyPrefix);
				return CandidateTextImprovementResponse.processingError();
			}

			openAiRequestAttempted = true;
			OpenAiClient.TextImprovementResult improvementResult = OpenAiClient.improveCandidateText(prompt, sanitizedOriginalText, instruction, sanitizedQuestionText);
			if (improvementResult == null || !hasText(improvementResult.getText())) {
				return CandidateTextImprovementResponse.processingError();
			}
			String sanitizedImprovedText = sanitizePlainTextToNull(improvementResult.getText());
			if (!hasText(sanitizedImprovedText)) {
				appLogger.warn("Text improvement rejected because sanitized improved text is empty. tokenPrefix={}", buildSafeTokenPrefix(token));
				return CandidateTextImprovementResponse.processingError();
			}
			openAiRequestSuccess = true;
			return CandidateTextImprovementResponse.success(sanitizedImprovedText);
		} catch (Exception e) {
			appLogger.error("Error improving candidate text", e);
			return CandidateTextImprovementResponse.processingError();
		} finally {
			if (openAiRequestAttempted && candidate != null) {
				persistCandidateTextImprovementAttemptActivity(candidate, instruction, openAiRequestSuccess, clientIp);
			}
		}
	}

	private TextImprovementAccessContext resolveTextImprovementAccessContext(String token, String clientIp) {
		Nomination nomination = hasText(token) ? ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token) : null;
		if (nomination != null && nomination.getStatus() == NominationStatus.ACCEPTED_BY_CANDIDATE && nomination.getCandidate() != null) {
			Candidate candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate != null) {
				return TextImprovementAccessContext.allowOpenAi(candidate, buildCandidateTextImprovementScopeKey(candidate.getCandidateId()));
			}
		}

		Organization organization = hasText(token) ? ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByDoNominationToken(token) : null;
		if (organization != null && organization.getId() > 0L) {
			return TextImprovementAccessContext.allowOpenAi(null, buildOrganizationTextImprovementScopeKey(organization.getId()));
		}

		String ipScopeKey = buildIpTextImprovementScopeKey(clientIp);
		if (!hasText(ipScopeKey)) {
			return null;
		}
		return TextImprovementAccessContext.allowOpenAi(null, ipScopeKey);
	}

	private boolean isTextImprovementDailyLimitReached(String rateLimitScopeKey, int maxDailyTextImprovementRequests) {
		if (!hasText(rateLimitScopeKey) || maxDailyTextImprovementRequests <= 0) {
			return true;
		}
		int currentAttempts = ElectionsCaches.incrementAiTextImprovementAttempt(rateLimitScopeKey);
		return currentAttempts > maxDailyTextImprovementRequests;
	}

	private String buildCandidateTextImprovementScopeKey(long candidateId) {
		return candidateId > 0L ? AI_SCOPE_CANDIDATE_PREFIX + candidateId : null;
	}

	private String buildOrganizationTextImprovementScopeKey(long organizationId) {
		return organizationId > 0L ? AI_SCOPE_ORGANIZATION_PREFIX + organizationId : null;
	}

	private String buildIpTextImprovementScopeKey(String clientIp) {
		String normalizedIp = trimToNull(clientIp);
		if (!hasText(normalizedIp)) {
			return null;
		}
		return AI_SCOPE_IP_PREFIX + normalizedIp.toLowerCase(Locale.ROOT);
	}

	private String buildSafeTokenPrefix(String token) {
		String normalizedToken = trimToNull(token);
		if (!hasText(normalizedToken)) {
			return DEBUG_NA;
		}
		return normalizedToken.length() > 12 ? normalizedToken.substring(0, 12) + "..." : normalizedToken;
	}

	private int resolveTextImprovementMaxDailyRequests() {
		try {
			String value = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.AI_TEXT_IMPROVEMENT_MAX_DAILY_REQUESTS);
			if (!hasText(value)) {
				return DEFAULT_MAX_DAILY_TEXT_IMPROVEMENT_REQUESTS;
			}

			int parsedValue = Integer.parseInt(value.trim());
			return parsedValue > 0 ? parsedValue : DEFAULT_MAX_DAILY_TEXT_IMPROVEMENT_REQUESTS;
		} catch (Exception e) {
			appLogger.warn("Unable to resolve text improvement max daily requests from parameter {}, using default {}", Constants.AI_TEXT_IMPROVEMENT_MAX_DAILY_REQUESTS, DEFAULT_MAX_DAILY_TEXT_IMPROVEMENT_REQUESTS, e);
			return DEFAULT_MAX_DAILY_TEXT_IMPROVEMENT_REQUESTS;
		}
	}

	@Override
	public Candidate saveCandidateOrganizations(String token, Candidate candidateData, String actor, String ip) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || candidateData == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return null;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return null;
			}

			Candidate candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate == null) {
				return null;
			}

			copyCandidateOrganizations(candidate, candidateData);
			candidate = em.merge(candidate);
			touchTaskLastUpdate(token, ElectionTaskKey.ORGANIZATIONS);

			em.flush();
			persistCandidateOrganizationsEditActivity(actor, candidate, ip);
			return candidate;
		} catch (Exception e) {
			appLogger.error("Error saving candidate organizations", e);
			return null;
		}
	}

	@Override
	public Candidate requestCandidateTrainingCredentials(String token, String actor, String ip) {
		CandidateTrainingCredentialsRequestResult result = requestCandidateTrainingCredentialsDetailed(token, actor, ip);
		if (result == null || !result.isSuccess()) {
			return null;
		}
		return result.getCandidate();
	}

	@Override
	public CandidateTrainingCredentialsRequestResult requestCandidateTrainingCredentialsDetailed(String token, String actor, String ip) {
		return requestCandidateTrainingCredentialsDetailed(token, actor, ip, null);
	}

	@Override
	public CandidateTrainingCredentialsRequestResult requestCandidateTrainingCredentialsDetailed(String token, String actor, String ip, Long selectedCourseId) {
		Candidate candidate = null;
		Long courseId = null;
		long campusUserId = 0L;
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return buildTrainingCredentialsFailure("No fue posible validar la postulación para solicitar credenciales de capacitación.", "VALIDATE_NOMINATION", null, courseId, CAMPUS_TRAINING_ROLE_ID, null);
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return buildTrainingCredentialsFailure("La ventana para solicitar credenciales de capacitación no está disponible.", "VALIDATE_WINDOW", null, courseId, CAMPUS_TRAINING_ROLE_ID, null);
			}

			candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate == null) {
				return buildTrainingCredentialsFailure("No se encontró la información del candidato para solicitar credenciales de capacitación.", "LOAD_CANDIDATE", null, courseId, CAMPUS_TRAINING_ROLE_ID, null);
			}

			CandidateCampusCourseStatus previousStatus = candidate.getCampusCourseStatus();
			CandidateCampusCourseStatus normalizedPreviousStatus = previousStatus != null ? previousStatus : CandidateCampusCourseStatus.PENDING;
			if (normalizedPreviousStatus != CandidateCampusCourseStatus.PENDING) {
				return buildTrainingCredentialsFailure("El estado actual no permite solicitar credenciales de capacitación.", "VALIDATE_STATUS", candidate, courseId, CAMPUS_TRAINING_ROLE_ID, null);
			}

			courseId = resolveTrainingCourseId(candidate, selectedCourseId);
			if (candidate.getElection() == null || courseId == null || !hasText(candidate.getMail())) {
				return buildTrainingCredentialsFailure("Faltan datos obligatorios para solicitar credenciales en Campus.", "VALIDATE_REQUIRED_DATA", candidate, courseId, CAMPUS_TRAINING_ROLE_ID, null);
			}

			campusUserId = CampusClient.getOrCreateCampusUserId(candidate);
			if (campusUserId <= 0L) {
				return buildTrainingCredentialsFailure("No se pudo crear o recuperar el usuario en Campus.", "RESOLVE_OR_CREATE_USER", candidate, courseId, CAMPUS_TRAINING_ROLE_ID, null);
			}

			boolean enrolled = CampusClient.enrollUser(campusUserId, CAMPUS_TRAINING_ROLE_ID, String.valueOf(courseId));
			if (!enrolled) {
				return buildTrainingCredentialsFailure("No se pudo inscribir al candidato en el curso de Campus.", "ENROLL_USER", candidate, courseId, CAMPUS_TRAINING_ROLE_ID, campusUserId);
			}

			candidate.setCampusCourseStatus(CandidateCampusCourseStatus.SENT);
			candidate.setCampusCourseSelected(courseId);
			candidate = em.merge(candidate);
			touchTaskLastUpdate(token, ElectionTaskKey.COURSE);
			markTaskAsStartedIfPending(token, ElectionTaskKey.COURSE);

			em.flush();
			EJBFactory.getInstance().getMailsSendingEJB().queueCandidateTrainingAccessRequestedToElectionSender(candidate.getCandidateId());

			persistCandidateTrainingRequestActivity(actor, candidate, ip, normalizedPreviousStatus);
			return CandidateTrainingCredentialsRequestResult.success(candidate);
		} catch (Exception e) {
			appLogger.error("Error requesting candidate training credentials", e);
			return buildTrainingCredentialsFailure("Se produjo un error interno al procesar la solicitud de credenciales de capacitación.", "UNEXPECTED_EXCEPTION:" + e.getClass().getSimpleName(), candidate, courseId, CAMPUS_TRAINING_ROLE_ID, campusUserId > 0L ? campusUserId : null);
		}
	}

	private Long resolveTrainingCourseId(Candidate candidate, Long selectedCourseId) {
		if (candidate == null || candidate.getElection() == null) {
			return null;
		}
		if (selectedCourseId == null) {
			return candidate.getElection().getCampusCourse();
		}
		if (isConfiguredCampusCourse(candidate.getElection(), selectedCourseId)) {
			return selectedCourseId;
		}
		return null;
	}

	private boolean isConfiguredCampusCourse(Election election, Long courseId) {
		if (election == null || courseId == null) {
			return false;
		}
		return courseId.equals(election.getCampusCourse())
				|| courseId.equals(election.getCampusCourseEnglish())
				|| courseId.equals(election.getCampusCoursePortuguese());
	}

	@Override
	public Candidate requestCandidateEvaluationCredentials(String token, String actor, String ip) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return null;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return null;
			}

			Candidate candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate == null) {
				return null;
			}

			CandidateEvaluationStatus previousStatus = candidate.getEvaluationStatus();
			CandidateEvaluationStatus normalizedPreviousStatus = previousStatus != null ? previousStatus : CandidateEvaluationStatus.PENDING;
			if (normalizedPreviousStatus != CandidateEvaluationStatus.PENDING && normalizedPreviousStatus != CandidateEvaluationStatus.CREDENTIALS_REQUESTED && normalizedPreviousStatus != CandidateEvaluationStatus.CREDENTIALS_SENT) {
				return null;
			}

			candidate.setEvaluationStatus(CandidateEvaluationStatus.CREDENTIALS_REQUESTED);
			candidate = em.merge(candidate);
			touchTaskLastUpdate(token, ElectionTaskKey.EVALUATION);
			markTaskAsStartedIfPending(token, ElectionTaskKey.EVALUATION);

			em.flush();

			EJBFactory.getInstance().getMailsSendingEJB().queueCandidateEvaluationAccessRequestedToElectionSender(candidate.getCandidateId());
			persistCandidateEvaluationRequestActivity(actor, candidate, ip, normalizedPreviousStatus);
			return candidate;
		} catch (Exception e) {
			appLogger.error("Error requesting candidate evaluation access", e);
			return null;
		}
	}

	@Override
	public Organization findOrganizationForSupportRequest(String token, String identifierType, String identifierValue) {
		try {
			OrganizationSearchValidationResult validation = validateOrganizationForSupportRequestInternal(token, identifierType, identifierValue);
			if (!validation.isValid()) {
				return null;
			}
			return toSupportRequestOrganizationDto(validation.getOrganization());
		} catch (Exception e) {
			appLogger.error("Error searching supporting organization", e);
			return null;
		}
	}

	@Override
	public String getOrganizationSupportSearchValidationMessageKey(String token, String identifierType, String identifierValue) {
		try {
			return validateOrganizationForSupportRequestInternal(token, identifierType, identifierValue).getValidationMessageKey();
		} catch (Exception e) {
			appLogger.error("Error validating support organization search", e);
			return "acceptNominationOrgSupportsSearchNotFound";
		}
	}

	@Override
	public boolean requestCandidateOrganizationSupport(String token, Long supportingOrganizationId, String actor, String ip) {
		try {
			if (supportingOrganizationId == null) {
				return false;
			}

			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			nomination = lockCandidateSupportAggregate(nomination);
			if (nomination == null || nomination.getElection() == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE) {
				return false;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return false;
			}

			Organization supportingOrganization = em.find(Organization.class, supportingOrganizationId);
			if (supportingOrganization == null || supportingOrganization.getElection() == null || supportingOrganization.getElection().getElectionId() != nomination.getElection().getElectionId()) {
				return false;
			}
			if (supportingOrganization.isDeudor()) {
				return false;
			}

			if (!hasText(supportingOrganization.getMembershipContactEmail())) {
				return false;
			}
			if (hasSameEmail(nomination.getCandidate() != null ? nomination.getCandidate().getMail() : null, supportingOrganization.getMembershipContactEmail())) {
				return false;
			}
			if (isBlockedOrganizationForSupportRequest(nomination, supportingOrganization)) {
				return false;
			}

			List<SupportNomination> existingSupports = nomination.getSupports() == null ? new ArrayList<>() : nomination.getSupports();
			int activeSupportsCount = 0;
			for (SupportNomination existingSupport : existingSupports) {
				if (existingSupport == null || existingSupport.getSupportStatus() == null) {
					continue;
				}
				if (existingSupport.getSupportingOrganization() != null && isActiveOrganizationSupportStatus(existingSupport.getSupportStatus())) {
					activeSupportsCount++;
				}
			}

			if (activeSupportsCount >= MAX_ACTIVE_ORG_SUPPORTS) {
				return false;
			}

			SupportNomination supportNomination = new SupportNomination(nomination, supportingOrganization, trimToNull(supportingOrganization.getMembershipContactName()), trimToNull(supportingOrganization.getMembershipContactEmail()), StringUtils.createSecureToken(), SupportStatus.PROPOSED);
			em.persist(supportNomination);

			touchTaskLastUpdate(token, ElectionTaskKey.ORG_SUPPORTS);
			markTaskAsStartedIfPending(token, ElectionTaskKey.ORG_SUPPORTS);

			em.flush();
			EJBFactory.getInstance().getMailsSendingEJB().queueOrganizationSupportRequestToMembershipContact(supportNomination.getId());
			persistCandidateOrganizationSupportRequestActivity(actor, nomination, supportingOrganization, ip);
			return true;
		} catch (Exception e) {
			appLogger.error("Error requesting candidate organization support", e);
			return false;
		}
	}

	@Override
	public boolean requestCandidateUserSupport(String token, String supportingContactName, String supportingContactEmail, ElectionTaskKey taskKey, int requiredSupports, String actor, String ip) {
		try {
			if (taskKey != ElectionTaskKey.USER_SUPPORTS_2 && taskKey != ElectionTaskKey.USER_SUPPORTS_5) {
				return false;
			}
			if ((taskKey == ElectionTaskKey.USER_SUPPORTS_2 && requiredSupports != 2) || (taskKey == ElectionTaskKey.USER_SUPPORTS_5 && requiredSupports != 5)) {
				return false;
			}

			String normalizedContactName = trimToNull(supportingContactName);
			String normalizedContactEmail = trimToNull(supportingContactEmail);
			if (!hasText(normalizedContactName) || !hasText(normalizedContactEmail) || !isValidEmail(normalizedContactEmail)) {
				return false;
			}

			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			nomination = lockCandidateSupportAggregate(nomination);
			if (nomination == null || nomination.getElection() == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE) {
				return false;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return false;
			}
			if (hasSameEmail(nomination.getCandidate() != null ? nomination.getCandidate().getMail() : null, normalizedContactEmail)) {
				return false;
			}
			String normalizedContactEmailLower = normalizedContactEmail.toLowerCase(Locale.ROOT);
			SupportNominationDao supportNominationDao = ElectionsDaoFactory.createSupportNominationDao(em);
			if (supportNominationDao.existsUserSupportRequestForNominationByEmail(nomination.getId(), normalizedContactEmailLower)) {
				return false;
			}

			List<SupportNomination> existingSupports = nomination.getSupports() == null ? new ArrayList<>() : nomination.getSupports();
			int activeUserSupportsCount = 0;
			for (SupportNomination existingSupport : existingSupports) {
				if (existingSupport == null || existingSupport.getSupportStatus() == null) {
					continue;
				}
				if (existingSupport.getSupportingOrganization() != null) {
					continue;
				}
				if (isActiveUserSupportStatus(existingSupport.getSupportStatus())) {
					activeUserSupportsCount++;
				}
			}

			if (activeUserSupportsCount >= requiredSupports) {
				return false;
			}

			SupportNomination supportNomination = new SupportNomination(nomination, null, normalizedContactName, normalizedContactEmailLower, StringUtils.createSecureToken(), SupportStatus.PROPOSED);
			em.persist(supportNomination);

			touchTaskLastUpdate(token, taskKey);
			markTaskAsStartedIfPending(token, taskKey);

			em.flush();
			EJBFactory.getInstance().getMailsSendingEJB().queueUserSupportRequestToSupportingContact(supportNomination.getId());
			persistCandidateUserSupportRequestActivity(actor, nomination, normalizedContactName, normalizedContactEmailLower, taskKey, ip);
			return true;
		} catch (Exception e) {
			appLogger.error("Error requesting candidate user support", e);
			return false;
		}
	}

	private boolean isBlockedOrganizationForSupportRequest(Nomination nomination, Organization supportingOrganization) {
		return resolveBlockedOrganizationForSupportRequestMessageKey(nomination, supportingOrganization) != null;
	}

	private OrganizationSearchValidationResult validateOrganizationForSupportRequestInternal(String token, String identifierType, String identifierValue) {
		Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
		if (nomination == null || nomination.getElection() == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE) {
			return OrganizationSearchValidationResult.invalid("acceptNominationOrgSupportsRequestInvalidState");
		}
		if (isNominationTaskWindowClosed(nomination)) {
			return OrganizationSearchValidationResult.invalid("acceptNominationTaskRestrictionNominationWindowClosed");
		}

		String normalizedIdentifierValue = trimToNull(identifierValue);
		if (normalizedIdentifierValue == null) {
			return OrganizationSearchValidationResult.invalid("acceptNominationOrgSupportsIdentifierRequired");
		}

		String normalizedIdentifierType = trimToEmpty(identifierType).toUpperCase(Locale.ROOT);
		if ("IP".equals(normalizedIdentifierType) || (!"ORG_ID".equals(normalizedIdentifierType) && !"CNPJ".equals(normalizedIdentifierType) && !"ASN".equals(normalizedIdentifierType))) {
			return OrganizationSearchValidationResult.invalid("acceptNominationOrgSupportsIdentifierTypeRequired");
		}

		Organization organization = resolveOrganizationByIdentifier(nomination, normalizedIdentifierType, normalizedIdentifierValue);
		if (organization == null) {
			return OrganizationSearchValidationResult.invalid("acceptNominationOrgSupportsSearchNotFound");
		}
		if (organization.isDeudor()) {
			return OrganizationSearchValidationResult.invalid("acceptNominationOrgSupportsSearchNotFound");
		}

		String blockedReasonKey = resolveBlockedOrganizationForSupportRequestMessageKey(nomination, organization);
		if (blockedReasonKey != null) {
			return OrganizationSearchValidationResult.invalid(blockedReasonKey);
		}

		if (!hasText(organization.getMembershipContactEmail())) {
			return OrganizationSearchValidationResult.invalid("acceptNominationOrgSupportsSearchMissingMembershipContact");
		}
		if (hasSameEmail(nomination.getCandidate() != null ? nomination.getCandidate().getMail() : null, organization.getMembershipContactEmail())) {
			return OrganizationSearchValidationResult.invalid("acceptNominationOrgSupportsMembershipContactMatchesCandidate");
		}

		return OrganizationSearchValidationResult.valid(organization);
	}

	private Organization resolveOrganizationByIdentifier(Nomination nomination, String normalizedIdentifierType, String normalizedIdentifierValue) {
		if (nomination == null || nomination.getElection() == null) {
			return null;
		}
		switch (normalizedIdentifierType) {
		case "ORG_ID":
			return ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByElectionAndOrgId(nomination.getElection().getElectionId(), normalizedIdentifierValue);
		case "CNPJ":
			return ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByElectionAndCnpj(nomination.getElection().getElectionId(), normalizedIdentifierValue);
		case "ASN":
			return ElectionsDaoFactory.createOrganizationDao(em).getOrganizationByElectionAndAsn(nomination.getElection().getElectionId(), normalizedIdentifierValue);
		default:
			return null;
		}
	}

	private String resolveBlockedOrganizationForSupportRequestMessageKey(Nomination nomination, Organization supportingOrganization) {
		if (nomination == null || nomination.getElection() == null || supportingOrganization == null) {
			return "acceptNominationOrgSupportsRequestInvalidState";
		}
		if (nomination.getOrganization() != null && nomination.getOrganization().getId() == supportingOrganization.getId()) {
			return "acceptNominationOrgSupportsSearchSelfOrganization";
		}

		SupportNominationDao supportNominationDao = ElectionsDaoFactory.createSupportNominationDao(em);
		long nominationId = nomination.getId();
		long electionId = nomination.getElection().getElectionId();
		long supportingOrganizationId = supportingOrganization.getId();

		if (supportNominationDao.existsOrganizationSupportRequestForNomination(nominationId, supportingOrganizationId)) {
			return "acceptNominationOrgSupportsAlreadyRequested";
		}
		if (supportNominationDao.existsOrganizationGrantedSupportInElection(electionId, supportingOrganizationId, nominationId)) {
			return "acceptNominationOrgSupportsSearchAlreadyGranted";
		}
		return null;
	}

	private Organization toSupportRequestOrganizationDto(Organization organization) {
		if (organization == null) {
			return null;
		}
		Organization dto = new Organization();
		dto.setId(organization.getId());
		dto.setOrgId(trimToNull(organization.getOrgId()));
		dto.setName(trimToNull(organization.getName()));
		dto.setCountry(trimToNull(organization.getCountry()));
		dto.setCnpj(trimToNull(organization.getCnpj()));
		dto.setAsn(trimToNull(organization.getAsn()));
		dto.setMembershipContactId(trimToNull(organization.getMembershipContactId()));
		dto.setMembershipContactName(trimToNull(organization.getMembershipContactName()));
		dto.setMembershipContactEmail(trimToNull(organization.getMembershipContactEmail()));
		dto.setMembershipContactLanguage(trimToNull(organization.getMembershipContactLanguage()));
		return dto;
	}

	private static final class OrganizationSearchValidationResult {
		private final Organization organization;
		private final String validationMessageKey;

		private OrganizationSearchValidationResult(Organization organization, String validationMessageKey) {
			this.organization = organization;
			this.validationMessageKey = validationMessageKey;
		}

		static OrganizationSearchValidationResult valid(Organization organization) {
			return new OrganizationSearchValidationResult(organization, null);
		}

		static OrganizationSearchValidationResult invalid(String validationMessageKey) {
			return new OrganizationSearchValidationResult(null, validationMessageKey);
		}

		boolean isValid() {
			return validationMessageKey == null;
		}

		Organization getOrganization() {
			return organization;
		}

		String getValidationMessageKey() {
			return validationMessageKey;
		}
	}

	@Override
	public boolean updateSupportNominationStatus(String token, SupportStatus status, String ip) {
		try {
			if (token == null || token.trim().isEmpty() || status == null) {
				return false;
			}
			if (status == SupportStatus.PROPOSED) {
				return false;
			}

			SupportNominationDao supportNominationDao = ElectionsDaoFactory.createSupportNominationDao(em);
			SupportNomination supportSnapshot = supportNominationDao.getSupportNominationByToken(token);
			if (supportSnapshot == null || lockCandidateSupportAggregate(supportSnapshot.getNomination()) == null) {
				return false;
			}
			SupportNomination supportNomination = supportNominationDao.getSupportNominationForUpdate(supportSnapshot.getId());
			if (supportNomination == null) {
				return false;
			}
			if (isDebtorOrganizationSupport(supportNomination)) {
				return false;
			}
			SupportStatus currentStatus = supportNomination.getSupportStatus();
			if (!canTransitionSupportStatus(currentStatus, status)) {
				appLogger.warn("Invalid support status transition. supportNominationId={}, from={}, to={}", supportNomination.getId(), currentStatus != null ? currentStatus.name() : "NULL", status.name());
				return false;
			}

			supportNomination.setSupportStatus(status);
			if (status == SupportStatus.ACCEPTED || status == SupportStatus.REJECTED) {
				supportNomination.setSupportResponseInstant(new Date());
			}
			em.merge(supportNomination);
			autoCompleteOrganizationSupportsTaskIfThresholdReached(supportNomination, ip);
			autoCompleteUserSupportsTaskIfThresholdReached(supportNomination, ip);
			EJBFactory.getInstance().getMailsSendingEJB().queueSupportStatusChangedToCandidate(supportNomination.getId());
			return true;
		} catch (Exception e) {
			appLogger.error("Error updating support nomination status", e);
			return false;
		}
	}

	private boolean isDebtorOrganizationSupport(SupportNomination supportNomination) {
		Organization supportingOrganization = supportNomination != null ? supportNomination.getSupportingOrganization() : null;
		return supportingOrganization != null && supportingOrganization.isDeudor();
	}

	private Nomination lockCandidateSupportAggregate(Nomination nomination) {
		if (nomination == null || nomination.getCandidate() == null) {
			return null;
		}
		Candidate candidate = ElectionsDaoFactory.createCandidateDao(em).getCandidateForUpdate(nomination.getCandidate().getCandidateId());
		if (candidate == null) {
			return null;
		}
		return nomination;
	}

	private boolean canTransitionSupportStatus(SupportStatus currentStatus, SupportStatus targetStatus) {
		if (targetStatus == null || targetStatus == SupportStatus.PROPOSED) {
			return false;
		}
		if (currentStatus == null || currentStatus == SupportStatus.PROPOSED) {
			return targetStatus == SupportStatus.ACCEPTED || targetStatus == SupportStatus.REJECTED || targetStatus == SupportStatus.INVALID;
		}
		if (currentStatus == SupportStatus.ACCEPTED) {
			return targetStatus == SupportStatus.ACCEPTED || targetStatus == SupportStatus.APPROVED || targetStatus == SupportStatus.INVALID;
		}
		if (currentStatus == SupportStatus.REJECTED) {
			return targetStatus == SupportStatus.REJECTED || targetStatus == SupportStatus.INVALID;
		}
		return currentStatus == targetStatus;
	}

	private void copyCandidateProfile(Candidate target, Candidate source, Nomination nomination) {
		target.setName(trimToEmpty(source.getName()));
		target.setLinkedinUrl(trimToNull(source.getLinkedinUrl()));
		target.setBioSpanish(trimToNull(source.getBioSpanish()));
		target.setBioEnglish(trimToNull(source.getBioEnglish()));
		target.setBioPortuguese(trimToNull(source.getBioPortuguese()));
		target.setOnlySp(source.isOnlySp());

		if (source.getPictureInfo() != null && source.getPictureInfo().length > 0) {
			target.setPictureInfo(source.getPictureInfo());
			target.setPictureName(trimToNull(source.getPictureName()));
			target.setPictureExtension(trimToNull(source.getPictureExtension()));
		}

		if (hasText(source.getMail())) {
			target.setMail(trimToNull(source.getMail()));
		} else if (!hasText(target.getMail()) && nomination != null) {
			target.setMail(trimToNull(nomination.getNominationEmail()));
		}
	}

	private void copyCandidateIncompatibilities(Candidate target, Candidate source) {
		target.setQAdultInCountry(source.isQAdultInCountry());
		target.setQCanSpeakSpanish(source.isQCanSpeakSpanish());
		target.setQCivilRightsLimitation(source.isQCivilRightsLimitation());
		target.setQLegalLimitationAnyCountry(source.isQLegalLimitationAnyCountry());
		target.setQHealthTravelLimitation(source.isQHealthTravelLimitation());
		target.setQHealthMentalLimitation(source.isQHealthMentalLimitation());
	}

	private void copyCandidateOtherStatutoryQuestions(Candidate target, Candidate source) {
		target.setQOtherStatutoryAnswer1(normalizeOpenQuestionAnswer(source.getQOtherStatutoryAnswer1()));
		target.setQOtherStatutoryAnswer2(normalizeOpenQuestionAnswer(source.getQOtherStatutoryAnswer2()));
		target.setQOtherStatutoryAnswer3(normalizeOpenQuestionAnswer(source.getQOtherStatutoryAnswer3()));
		target.setQOtherStatutoryAnswer4(normalizeOpenQuestionAnswer(source.getQOtherStatutoryAnswer4()));
	}

	private void copyCandidateOtherNonStatutoryQuestions(Candidate target, Candidate source) {
		target.setQOtherNonStatutoryAnswer1(normalizeOtherNonStatutoryAnswer(source.getQOtherNonStatutoryAnswer1()));
	}

	private void copyCandidateDeclarations(Candidate target, Candidate source, ElectionType electionType) {
		target.setQDeclarationIncompatibilities(source.isQDeclarationIncompatibilities());
		target.setQDeclarationConflictsOfInterest(source.isQDeclarationConflictsOfInterest());
		target.setQDeclarationCompetenciesAndSuitability(source.isQDeclarationCompetenciesAndSuitability());
		target.setQDeclarationDisciplinaryRegulation(requiresBoardDisciplinaryDeclaration(electionType) ? source.isQDeclarationDisciplinaryRegulation() : null);
		target.setQDeclarationPep(source.getQDeclarationPep());
		target.setQDeclarationDynamicCommitments(source.isQDeclarationDynamicCommitments());
		target.setQDeclarationDataUsageAndPublication(source.isQDeclarationDataUsageAndPublication());
	}

	private void copyCandidateNonStatutoryDeclarations(Candidate target, Candidate source, ElectionType electionType) {
		target.setQDeclarationIanaKnowledge(requiresIanaKnowledgeDeclaration(electionType) ? source.isQDeclarationIanaKnowledge() : null);
		target.setQDeclarationAsoKnowledge(requiresAsoKnowledgeDeclaration(electionType) ? source.isQDeclarationAsoKnowledge() : null);
		target.setQDeclarationDynamicCommitments(source.isQDeclarationDynamicCommitments());
		target.setQDeclarationDataUsageAndPublication(source.isQDeclarationDataUsageAndPublication());
	}

	private void copyCandidateOrganizations(Candidate target, Candidate source) {
		boolean qUnemployed = source.isQUnemployed();
		target.setQUnemployed(qUnemployed);

		List<CandidateWorkOrganization> targetOrganizations = target.getWorkOrganizations();
		if (targetOrganizations == null) {
			targetOrganizations = new ArrayList<>();
			target.setWorkOrganizations(targetOrganizations);
		} else {
			targetOrganizations.clear();
		}

		if (qUnemployed || source.getWorkOrganizations() == null) {
			return;
		}

		for (CandidateWorkOrganization sourceOrganization : source.getWorkOrganizations()) {
			if (sourceOrganization == null || !hasText(sourceOrganization.getOrganizationName()) || sourceOrganization.getWorkOrganizationType() == null) {
				continue;
			}

			CandidateWorkOrganization targetOrganization = new CandidateWorkOrganization();
			targetOrganization.setCandidate(target);
			targetOrganization.setOrganizationName(trimToEmpty(sourceOrganization.getOrganizationName()));
			targetOrganization.setOrganizationGroup(sanitizePlainTextToNull(sourceOrganization.getOrganizationGroup()));
			targetOrganization.setWorkOrganizationType(sourceOrganization.getWorkOrganizationType());
			targetOrganizations.add(targetOrganization);
		}
	}

	private void copyCandidateCountries(Candidate target, Candidate source) {
		List<CandidateCountryLink> targetCountryLinks = target.getCountryLinks();
		if (targetCountryLinks == null) {
			targetCountryLinks = new ArrayList<>();
			target.setCountryLinks(targetCountryLinks);
		} else {
			targetCountryLinks.clear();
		}

		if (source.getCountryLinks() == null || source.getCountryLinks().isEmpty()) {
			return;
		}

		Set<String> usedCountryCodes = new HashSet<>();
		Set<String> validCountryCodes = new HashSet<>();
		for (String countryCode : new CountryUtils().getIdsListExcludingDefault()) {
			if (countryCode != null && !countryCode.trim().isEmpty()) {
				validCountryCodes.add(countryCode.trim().toUpperCase(Locale.ROOT));
			}
		}
		boolean primaryCountryAlreadySet = false;

		for (CandidateCountryLink sourceCountryLink : source.getCountryLinks()) {
			if (sourceCountryLink == null || !hasText(sourceCountryLink.getCountryCode())) {
				continue;
			}

			String normalizedCountryCode = sourceCountryLink.getCountryCode().trim().toUpperCase(Locale.ROOT);
			if (normalizedCountryCode.length() > 2 || usedCountryCodes.contains(normalizedCountryCode) || !validCountryCodes.contains(normalizedCountryCode)) {
				continue;
			}

			boolean isPrimaryCountry = sourceCountryLink.isPrimaryCountry();
			if (isPrimaryCountry && primaryCountryAlreadySet) {
				continue;
			}

			CandidateCountryLink targetCountryLink = new CandidateCountryLink();
			targetCountryLink.setCandidate(target);
			targetCountryLink.setPrimaryCountry(isPrimaryCountry);
			targetCountryLink.setCountryCode(normalizedCountryCode);
			targetCountryLink.setQCitizen(sourceCountryLink.isQCitizen());
			targetCountryLink.setQResidenceOver5y(sourceCountryLink.isQResidenceOver5y());
			targetCountryLink.setQLongEmploymentOrAdvisory5y(sourceCountryLink.isQLongEmploymentOrAdvisory5y());
			targetCountryLink.setQFamilyResidenceOver5y(sourceCountryLink.isQFamilyResidenceOver5y());
			targetCountryLink.setQInternetCommunityOrgParticipation(sourceCountryLink.isQInternetCommunityOrgParticipation());
			targetCountryLink.setQEligibleForCitizenship(sourceCountryLink.isQEligibleForCitizenship());
			targetCountryLinks.add(targetCountryLink);

			usedCountryCodes.add(normalizedCountryCode);
			if (isPrimaryCountry) {
				primaryCountryAlreadySet = true;
			}
		}
	}

	private boolean hasValidDeclarations(Candidate source, ElectionType electionType) {
		if (!source.isQDeclarationIncompatibilities()) {
			return false;
		}
		if (!source.isQDeclarationConflictsOfInterest()) {
			return false;
		}
		if (!source.isQDeclarationCompetenciesAndSuitability()) {
			return false;
		}
		if (requiresBoardDisciplinaryDeclaration(electionType) && !source.isQDeclarationDisciplinaryRegulation()) {
			return false;
		}
		if (source.getQDeclarationPep() == null) {
			return false;
		}
		if (!source.isQDeclarationDynamicCommitments()) {
			return false;
		}
		if (!source.isQDeclarationDataUsageAndPublication()) {
			return false;
		}
		return true;
	}

	private boolean hasValidNonStatutoryDeclarations(Candidate source, ElectionType electionType) {
		if (requiresIanaKnowledgeDeclaration(electionType) && !source.isQDeclarationIanaKnowledge()) {
			return false;
		}
		if (requiresAsoKnowledgeDeclaration(electionType) && !source.isQDeclarationAsoKnowledge()) {
			return false;
		}
		if (!source.isQDeclarationDynamicCommitments()) {
			return false;
		}
		if (!source.isQDeclarationDataUsageAndPublication()) {
			return false;
		}
		return true;
	}

	private boolean hasValidPictureData(Candidate candidate) {
		return candidate.getPictureInfo() != null && candidate.getPictureInfo().length > 0 && hasText(candidate.getPictureName()) && hasText(candidate.getPictureExtension());
	}

	private void persistCandidateEditActivity(String actor, Candidate candidate, String ip) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, candidate, source);
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " actualizó sus datos" : " actualizó los datos de un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", nombre=" + safeValue(candidate.getName()) + ", linkedin=" + safeValue(candidate.getLinkedinUrl()) + ", bioEsChars=" + safeLength(candidate.getBioSpanish()) + ", bioEnChars=" + safeLength(candidate.getBioEnglish()) + ", bioPtChars=" + safeLength(candidate.getBioPortuguese());
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
	}

	private void persistCandidateIncompatibilitiesEditActivity(String actor, Candidate candidate, String ip) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, candidate, source);
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " actualizó incompatibilidades" : " actualizó incompatibilidades de un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", qAdultInCountry=" + candidate.isQAdultInCountry() + ", qCanSpeakSpanish=" + candidate.isQCanSpeakSpanish() + ", qCivilRightsLimitation=" + candidate.isQCivilRightsLimitation() + ", qLegalLimitationAnyCountry=" + candidate.isQLegalLimitationAnyCountry() + ", qHealthTravelLimitation=" + candidate.isQHealthTravelLimitation() + ", qHealthMentalLimitation=" + candidate.isQHealthMentalLimitation();
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
	}

	private void persistCandidateDeclarationsEditActivity(String actor, Candidate candidate, String ip) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, candidate, source);
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " actualizó declaraciones" : " actualizó declaraciones de un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", qDeclarationIncompatibilities=" + candidate.isQDeclarationIncompatibilities() + ", qDeclarationConflictsOfInterest=" + candidate.isQDeclarationConflictsOfInterest() + ", qDeclarationCompetenciesAndSuitability=" + candidate.isQDeclarationCompetenciesAndSuitability() + ", qDeclarationDisciplinaryRegulation=" + candidate.isQDeclarationDisciplinaryRegulation() + ", qDeclarationPep=" + (candidate.getQDeclarationPep() != null ? candidate.getQDeclarationPep() : "-") + ", qDeclarationDynamicCommitments=" + candidate.isQDeclarationDynamicCommitments() + ", qDeclarationDataUsageAndPublication=" + candidate.isQDeclarationDataUsageAndPublication();
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
	}

	private void persistCandidateNonStatutoryDeclarationsEditActivity(String actor, Candidate candidate, String ip) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, candidate, source);
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " actualizó declaraciones (n.e)" : " actualizó declaraciones (n.e) de un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", qDeclarationIanaKnowledge=" + candidate.isQDeclarationIanaKnowledge() + ", qDeclarationAsoKnowledge=" + candidate.isQDeclarationAsoKnowledge() + ", qDeclarationDynamicCommitments=" + candidate.isQDeclarationDynamicCommitments() + ", qDeclarationDataUsageAndPublication=" + candidate.isQDeclarationDataUsageAndPublication();
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
	}

	private void persistCandidateOtherStatutoryQuestionsEditActivity(String actor, Candidate candidate, String ip) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, candidate, source);
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " actualizó otras preguntas" : " actualizó otras preguntas de un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", qOtherStatutoryAnswer1SpanishChars=" + safeLength(candidate.getQOtherStatutoryAnswer1Spanish()) + ", qOtherStatutoryAnswer2SpanishChars=" + safeLength(candidate.getQOtherStatutoryAnswer2Spanish()) + ", qOtherStatutoryAnswer3SpanishChars=" + safeLength(candidate.getQOtherStatutoryAnswer3Spanish()) + ", qOtherStatutoryAnswer4SpanishChars=" + safeLength(candidate.getQOtherStatutoryAnswer4Spanish());
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
	}

	private void persistCandidateOtherNonStatutoryQuestionsEditActivity(String actor, Candidate candidate, String ip) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, candidate, source);
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " actualizó otras preguntas" : " actualizó otras preguntas de un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", qOtherNonStatutoryAnswer1SpanishChars=" + safeLength(candidate.getQOtherNonStatutoryAnswer1Spanish());
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
	}

	private void persistCandidateOrganizationsEditActivity(String actor, Candidate candidate, String ip) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, candidate, source);
		int organizationsCount = candidate.getWorkOrganizations() == null ? 0 : candidate.getWorkOrganizations().size();
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " actualizó organizaciones vinculadas" : " actualizó organizaciones vinculadas de un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", qUnemployed=" + candidate.isQUnemployed() + ", organizationsCount=" + organizationsCount;
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
	}

	private void persistCandidateCountriesEditActivity(String actor, Candidate candidate, String ip) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, candidate, source);
		int countriesCount = candidate.getCountryLinks() == null ? 0 : candidate.getCountryLinks().size();
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " actualizó países vinculados" : " actualizó países vinculados de un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", countriesCount=" + countriesCount;
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
	}

	private void persistCandidateTrainingRequestActivity(String actor, Candidate candidate, String ip, CandidateCampusCourseStatus previousStatus) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, candidate, source);
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " solicitó credenciales de capacitación" : " solicitó credenciales de capacitación para un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", previousCampusCourseStatus=" + previousStatus + ", currentCampusCourseStatus=" + candidate.getCampusCourseStatus();
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
	}

	private void persistCandidateEvaluationRequestActivity(String actor, Candidate candidate, String ip, CandidateEvaluationStatus previousStatus) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, candidate, source);
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " solicitó acceso a la evaluación" : " solicitó acceso a la evaluación para un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", previousEvaluationStatus=" + previousStatus + ", currentEvaluationStatus=" + candidate.getEvaluationStatus();
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
	}

	private void persistCandidateTextImprovementAttemptActivity(Candidate candidate, CandidateTextImprovementInstruction instruction, boolean success, String ip) {
		if (candidate == null || candidate.getElection() == null || instruction == null) {
			return;
		}
		String source = ACTIVITY_SOURCE_CANDIDATE_LINK;
		String activityUserName = resolveEditCandidateActivityUserName(ACTIVITY_SOURCE_CANDIDATE_LINK, candidate, source);
		String description = activityUserName + " solicitó revisión de texto con IA" + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + source + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId() + ", aiTextImprovementAttempt=true, instruction=" + instruction.name() + ", success=" + success;
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, candidate.getElection().getElectionId());
	}

	private void persistCandidateOrganizationSupportRequestActivity(String actor, Nomination nomination, Organization supportingOrganization, String ip) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, nomination != null ? nomination.getCandidate() : null, source);
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " solicitó un apoyo de organización" : " solicitó un apoyo de organización para un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + (nomination != null && nomination.getElection() != null ? nomination.getElection().getTitleSpanish() : "-") + ACTIVITY_SOURCE_FRAGMENT + source + ", nominationId=" + (nomination != null ? nomination.getId() : "-") + ", supportingOrganizationId=" + (supportingOrganization != null ? supportingOrganization.getId() : "-") + ", supportingOrganizationOrgId=" + (supportingOrganization != null ? safeValue(supportingOrganization.getOrgId()) : "-") + ", supportingOrganizationName=" + (supportingOrganization != null ? safeValue(supportingOrganization.getName()) : "-");
		Long electionId = nomination != null && nomination.getElection() != null ? nomination.getElection().getElectionId() : null;
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, electionId);
	}

	private void persistCandidateUserSupportRequestActivity(String actor, Nomination nomination, String supportingContactName, String supportingContactEmail, ElectionTaskKey taskKey, String ip) {
		String source = resolveEditCandidateSource(actor);
		String activityUserName = resolveEditCandidateActivityUserName(actor, nomination != null ? nomination.getCandidate() : null, source);
		String actionDescription = ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source) ? " solicitó un apoyo de usuario" : " solicitó un apoyo de usuario para un candidato";
		String description = activityUserName + actionDescription + TEXT_ELECCION + (nomination != null && nomination.getElection() != null ? nomination.getElection().getTitleSpanish() : "-") + ACTIVITY_SOURCE_FRAGMENT + source + ", nominationId=" + (nomination != null ? nomination.getId() : "-") + ", taskKey=" + (taskKey != null ? taskKey : "-") + ", supportingContactName=" + safeValue(supportingContactName) + ", supportingContactEmail=" + safeValue(supportingContactEmail);
		Long electionId = nomination != null && nomination.getElection() != null ? nomination.getElection().getElectionId() : null;
		persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, ip, electionId);
	}

	private String resolveEditCandidateActivityUserName(String actor, Candidate candidate, String source) {
		if (ACTIVITY_SOURCE_CANDIDATE_LINK.equals(source)) {
			if (candidate != null && hasText(candidate.getMail())) {
				return candidate.getMail().trim();
			}
			if (candidate != null) {
				return CANDIDATE_ACTIVITY_PREFIX + candidate.getCandidateId();
			}
			return ACTIVITY_SOURCE_CANDIDATE_LINK;
		}

		if (hasText(actor)) {
			return actor.toUpperCase();
		}
		return ACTIVITY_SOURCE_ADMIN_PANEL;
	}

	private String resolveEditCandidateSource(String actor) {
		if (isCandidateLinkEdition(actor)) {
			return ACTIVITY_SOURCE_CANDIDATE_LINK;
		}
		return ACTIVITY_SOURCE_ADMIN_PANEL;
	}

	private boolean isCandidateLinkEdition(String actor) {
		return actor == null || actor.trim().isEmpty() || ACTIVITY_SOURCE_CANDIDATE_LINK.equalsIgnoreCase(actor.trim()) || actor.toUpperCase().startsWith(CANDIDATE_ACTIVITY_PREFIX);
	}

	private void persistActivity(String userName, ActivityType activityType, String description, String ip, Long electionId) {
		Activity activity = new Activity(userName, electionId, ip == null ? "-" : ip, activityType, description);
		em.persist(activity);
		CriticalOperationsLoggerUtils.logCriticalOperation(ip, userName, activityType == null ? null : activityType.toString(), electionId);
	}

	private String safeValue(String value) {
		return value == null ? "-" : value;
	}

	private int safeLength(String value) {
		return value == null ? 0 : value.length();
	}

	private CandidateTrainingCredentialsRequestResult buildTrainingCredentialsFailure(String errorMessage, String step, Candidate candidate, Long courseId, String roleId, Long campusUserId) {
		String debugDetails = buildTrainingCredentialsDebugDetails(step, candidate, courseId, roleId, campusUserId);
		appLogger.warn("Training credentials request failed. errorMessage={}, debugDetails={}", errorMessage, debugDetails);
		return CandidateTrainingCredentialsRequestResult.failure(errorMessage, debugDetails);
	}

	private String buildTrainingCredentialsDebugDetails(String step, Candidate candidate, Long courseId, String roleId, Long campusUserId) {
		String candidateId = candidate != null ? String.valueOf(candidate.getCandidateId()) : DEBUG_NA;
		String candidateMail = candidate != null ? safeValue(candidate.getMail()) : DEBUG_NA;
		String courseIdValue = courseId != null ? String.valueOf(courseId) : DEBUG_NA;
		String roleIdValue = hasText(roleId) ? roleId : DEBUG_NA;
		String campusUserIdValue = campusUserId != null ? String.valueOf(campusUserId) : DEBUG_NA;
		String stepValue = hasText(step) ? step : DEBUG_NA;
		return "paso=" + stepValue + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidateId + ", candidateMail=" + candidateMail + ", courseId=" + courseIdValue + ", roleId=" + roleIdValue + ", campusUserId=" + campusUserIdValue;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String resolveCandidateActivityUserName(Candidate candidate) {
		if (candidate == null) {
			return ACTIVITY_SOURCE_CANDIDATE_LINK;
		}
		if (hasText(candidate.getMail())) {
			return candidate.getMail().trim();
		}
		return CANDIDATE_ACTIVITY_PREFIX + candidate.getCandidateId();
	}

	private boolean isCandidateQuestionsWindowOpen(long electionId, Date referenceDate) {
		if (electionId <= 0 || referenceDate == null) {
			return false;
		}
		ElectionCalendar calendar = ElectionsDaoFactory.createElectionCalendarDao(em).getElectionCalendarByKey(electionId, ElectionCalendarKey.N_12_PERIODO_CANDIDATE_QUESTIONS);
		if (calendar == null) {
			return false;
		}
		Date startDate = calendar.getStartDate();
		Date endDate = calendar.getEndDate() != null ? calendar.getEndDate() : startDate;
		if (startDate == null || endDate == null) {
			return false;
		}
		return !referenceDate.before(startDate) && !referenceDate.after(endDate);
	}

	private void autoCompleteUserSupportsTaskIfThresholdReached(SupportNomination supportNomination, String ip) {
		if (supportNomination == null || supportNomination.getSupportingOrganization() != null) {
			return;
		}
		if (!isCompletedUserSupportStatus(supportNomination.getSupportStatus())) {
			return;
		}

		Nomination nomination = supportNomination.getNomination();
		if (nomination == null || !hasText(nomination.getAcceptNominationToken())) {
			return;
		}

		int completedUserSupports = countCompletedUserSupports(nomination);
		if (completedUserSupports >= 2) {
			completeCandidateTaskIfPending(nomination.getAcceptNominationToken(), ElectionTaskKey.USER_SUPPORTS_2, ip);
		}
		if (completedUserSupports >= 5) {
			completeCandidateTaskIfPending(nomination.getAcceptNominationToken(), ElectionTaskKey.USER_SUPPORTS_5, ip);
		}
	}

	private void autoCompleteOrganizationSupportsTaskIfThresholdReached(SupportNomination supportNomination, String ip) {
		if (supportNomination == null || supportNomination.getSupportingOrganization() == null) {
			return;
		}
		if (!isCompletedOrganizationSupportStatus(supportNomination.getSupportStatus())) {
			return;
		}

		Nomination nomination = supportNomination.getNomination();
		if (nomination == null || !hasText(nomination.getAcceptNominationToken())) {
			return;
		}

		int completedOrganizationSupports = countCompletedOrganizationSupports(nomination);
		if (completedOrganizationSupports >= MAX_ACTIVE_ORG_SUPPORTS) {
			completeCandidateTaskIfPending(nomination.getAcceptNominationToken(), ElectionTaskKey.ORG_SUPPORTS, ip);
		}
	}

	private int countCompletedUserSupports(Nomination nomination) {
		if (nomination == null || nomination.getSupports() == null || nomination.getSupports().isEmpty()) {
			return 0;
		}

		int completedSupports = 0;
		for (SupportNomination support : nomination.getSupports()) {
			if (support == null || support.getSupportingOrganization() != null) {
				continue;
			}
			if (isCompletedUserSupportStatus(support.getSupportStatus())) {
				completedSupports++;
			}
		}
		return completedSupports;
	}

	private int countCompletedOrganizationSupports(Nomination nomination) {
		if (nomination == null || nomination.getSupports() == null || nomination.getSupports().isEmpty()) {
			return 0;
		}

		int completedSupports = 0;
		for (SupportNomination support : nomination.getSupports()) {
			if (support == null || support.getSupportingOrganization() == null) {
				continue;
			}
			if (isCompletedOrganizationSupportStatus(support.getSupportStatus())) {
				completedSupports++;
			}
		}
		return completedSupports;
	}

	private boolean isCompletedUserSupportStatus(SupportStatus status) {
		return status == SupportStatus.ACCEPTED || status == SupportStatus.APPROVED;
	}

	private boolean isCompletedOrganizationSupportStatus(SupportStatus status) {
		return status == SupportStatus.ACCEPTED || status == SupportStatus.APPROVED;
	}

	private void completeCandidateTaskIfPending(String nominationToken, ElectionTaskKey taskKey, String ip) {
		CandidateElectionTaskProgress taskProgress = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).getByNominationTokenAndTaskKeyForUpdate(nominationToken, taskKey);
		if (taskProgress == null || taskProgress.getStatus() == CandidateElectionTaskStatus.COMPLETED || taskProgress.getStatus() == CandidateElectionTaskStatus.OMITTED) {
			return;
		}

		boolean updated = updateCandidateTaskStatus(nominationToken, taskKey, CandidateElectionTaskStatus.COMPLETED, ip);
		if (!updated) {
			appLogger.warn("Could not auto-complete task {} for nomination token {}", taskKey, nominationToken);
		}
	}

	private boolean isActiveOrganizationSupportStatus(SupportStatus status) {
		return status == SupportStatus.PROPOSED || status == SupportStatus.ACCEPTED || status == SupportStatus.APPROVED;
	}

	private boolean isActiveUserSupportStatus(SupportStatus status) {
		return status == SupportStatus.PROPOSED || status == SupportStatus.ACCEPTED || status == SupportStatus.APPROVED;
	}

	private boolean isValidEmail(String email) {
		String normalizedEmail = trimToNull(email);
		if (normalizedEmail == null) {
			return false;
		}
		int atIndex = normalizedEmail.indexOf('@');
		if (atIndex <= 0 || atIndex == normalizedEmail.length() - 1) {
			return false;
		}
		int dotIndex = normalizedEmail.lastIndexOf('.');
		return dotIndex > atIndex + 1 && dotIndex < normalizedEmail.length() - 1;
	}

	private boolean hasSameEmail(String firstEmail, String secondEmail) {
		String normalizedFirst = trimToNull(firstEmail);
		String normalizedSecond = trimToNull(secondEmail);
		if (normalizedFirst == null || normalizedSecond == null) {
			return false;
		}
		return normalizedFirst.toLowerCase(Locale.ROOT).equals(normalizedSecond.toLowerCase(Locale.ROOT));
	}

	private String normalizeSingleLineText(String value) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			return null;
		}
		return trimmed.replaceAll("\\s+", " ");
	}

	private String normalizeEmail(String value) {
		String normalized = trimToNull(value);
		if (normalized == null) {
			return null;
		}
		return normalized.toLowerCase(Locale.ROOT);
	}

	private String normalizeCountryCode(String value) {
		return new CountryUtils().normalizeCountryCode(value);
	}

	private String sanitizePlainTextToNull(String value) {
		return trimToNull(HtmlSanitizerUtils.sanitizeStrictToNull(value));
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String trimToEmpty(String value) {
		if (value == null) {
			return "";
		}
		return value.trim();
	}

	private String normalizeOpenQuestionAnswer(String value) {
		return trimToNull(value);
	}

	private String normalizeOtherNonStatutoryAnswer(String value) {
		return trimToNull(value);
	}

	private ElectionType resolveElectionType(net.lacnic.elections.domain.Election election) {
		if (election == null) {
			return null;
		}
		return election.getEffectiveElectionType();
	}

	private boolean requiresBoardDisciplinaryDeclaration(ElectionType electionType) {
		return ElectionType.BOARD == electionType;
	}

	private boolean requiresIanaKnowledgeDeclaration(ElectionType electionType) {
		return ElectionType.IANA == electionType;
	}

	private boolean requiresAsoKnowledgeDeclaration(ElectionType electionType) {
		return ElectionType.ASO == electionType;
	}

	private CandidateDeclarationsDefinition buildCandidateDeclarationsDefinition(ElectionType electionType, String candidateNameRaw, String language) {
		String normalizedLanguage = normalizeLanguage(language);
		String candidateName = hasText(candidateNameRaw) ? candidateNameRaw.trim() : DEFAULT_CANDIDATE_NAME;
		CandidateDeclarationsDefinition definition = new CandidateDeclarationsDefinition();
		definition.setElectionType(electionType);

		List<CandidateDeclarationDefinition> declarations = new ArrayList<>();
		declarations.add(buildCheckboxDeclaration(CandidateDeclarationCode.INCOMPATIBILITIES_AND_CAPACITIES, getDeclarationParameterValue("DECLARATIONS_D1_TITLE", normalizedLanguage, "Reglamento de Incompatibilidades y Capacidades"), resolveRegulationLinkLabel("DECLARATIONS_D1_LINK_LABEL", normalizedLanguage, "Ver reglamento de Incompatibilidades y Capacidades"), "/elections/static/reglamento-incompatibilidades-capacidades-v2-es.pdf", applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D1_DECLARATION", normalizedLanguage, "Yo, {{candidateName}}, declaro haber leído y aceptado el Reglamento de Incompatibilidades y Capacidades, y no estar comprendido en ninguna incompatibilidad o incapacidad excluyente que impida mi candidatura."), candidateName), getDeclarationParameterValue("DECLARATIONS_D1_DESCRIPTION_HTML", normalizedLanguage, "<p>He leído el Reglamento de Incompatibilidades y Capacidades y declaro no estar comprendida en ninguna incompatibilidad o capacidad excluyente que impida mi candidatura.</p>")));

		declarations.add(buildCheckboxDeclaration(CandidateDeclarationCode.CONFLICTS_OF_INTEREST, getDeclarationParameterValue("DECLARATIONS_D2_TITLE", normalizedLanguage, "Reglamento de Conflictos de Interés Electorales"), resolveRegulationLinkLabel("DECLARATIONS_D2_LINK_LABEL", normalizedLanguage, "Ver reglamento de Conflictos de Interés Electorales"), "/elections/static/reglamento-conflictos-interes-electorales-es.pdf", applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D2_DECLARATION", normalizedLanguage, "Yo, {{candidateName}}, declaro haber leído y aceptado el Reglamento de Conflictos de Interés Electorales y no estar comprendido en los impedimentos allí enumerados que me impidan presentar mi candidatura."), candidateName), getDeclarationParameterValue("DECLARATIONS_D2_DESCRIPTION_HTML", normalizedLanguage, "<p>He leído el Reglamento de Conflictos de Interés Electorales y declaro no tener impedimentos de los allí enumerados que me impidan presentar mi candidatura.</p>")));

		declarations.add(buildCheckboxDeclaration(CandidateDeclarationCode.COMPETENCIES_AND_SUITABILITY, getDeclarationParameterValue("DECLARATIONS_D3_TITLE", normalizedLanguage, "Reglamento de Competencias e Idoneidades"), resolveRegulationLinkLabel("DECLARATIONS_D3_LINK_LABEL", normalizedLanguage, "Ver reglamento de Competencias e Idoneidades"), "/elections/static/reglamento-competencias-idoneidades-directorio-2024-es.pdf", applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D3_DECLARATION", normalizedLanguage, "Yo, {{candidateName}}, acepto el Reglamento de Competencias e Idoneidades y autorizo la verificación de mi idoneidad conforme a dicho reglamento."), candidateName), getDeclarationParameterValue("DECLARATIONS_D3_DESCRIPTION_HTML", normalizedLanguage, "<p>He leído el Reglamento de Competencias e Idoneidades y autorizo a la organización a verificar los antecedentes necesarios para evaluar mi idoneidad conforme a dicho reglamento.</p>")));

		if (requiresBoardDisciplinaryDeclaration(electionType)) {
			declarations.add(buildCheckboxDeclaration(CandidateDeclarationCode.DISCIPLINARY_REGULATION, getDeclarationParameterValue("DECLARATIONS_D4_TITLE", normalizedLanguage, "Reglamento Disciplinario para Órganos Electivos"), resolveRegulationLinkLabel("DECLARATIONS_D4_LINK_LABEL", normalizedLanguage, "Ver reglamento Disciplinario para Órganos Electivos"), "/elections/static/reglamento-disciplinario-organos-electivos-lacnic-2025-es.pdf", applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D4_DECLARATION", normalizedLanguage, "Yo, {{candidateName}}, declaro haber leído y aceptado el Reglamento Disciplinario para Órganos Electivos y me comprometo a cumplir sus disposiciones durante el proceso electoral y, en su caso, en el ejercicio del cargo."), candidateName), getDeclarationParameterValue("DECLARATIONS_D4_DESCRIPTION_HTML", normalizedLanguage, "<p>He leído el Reglamento Disciplinario para Órganos Electivos y me comprometo a cumplir sus disposiciones durante el proceso electoral y, en su caso, en el ejercicio del cargo.</p>")));
		}

		CandidateDeclarationDefinition pepDeclaration = new CandidateDeclarationDefinition();
		pepDeclaration.setCode(CandidateDeclarationCode.PEP_DECLARATION);
		pepDeclaration.setInputType(CandidateDeclarationInputType.RADIO);
		pepDeclaration.setRequired(true);
		pepDeclaration.setTitle(getDeclarationParameterValue("DECLARATIONS_D5_TITLE", normalizedLanguage, "Declaración sobre Persona Políticamente Expuesta (PEP)"));
		pepDeclaration.setDescriptionHtml(getDeclarationParameterValue("DECLARATIONS_D5_DESCRIPTION_HTML", normalizedLanguage, "<p>¿Es una persona políticamente expuesta?, conforme a las definiciones y normativas vigentes aplicables en materia de prevención de lavado de activos, financiamiento del terrorismo u otras regulaciones relacionadas, en el pais del cual declara ser ciudadano y/o residente en el caso de que sean distintos.</p>"));
		pepDeclaration.getOptions().add(new CandidateDeclarationOptionDefinition(CandidatePepDeclaration.NOT_PEP.name(), applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D5_DECLARATION_NOT_PEP", normalizedLanguage, "Yo, {{candidateName}}, declaro no tener la condición de Persona Políticamente Expuesta (PEP)."), candidateName)));
		pepDeclaration.getOptions().add(new CandidateDeclarationOptionDefinition(CandidatePepDeclaration.PEP.name(), applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D5_DECLARATION_PEP", normalizedLanguage, "Yo, {{candidateName}}, declaro tener la condición de Persona Políticamente Expuesta (PEP), y acepto en caso de ser electo proporcionar la información requerida por la organización a efectos de realizar la debida diligencia intensificada por mi condición de PEP, conforme a las regulaciones vigentes aplicables en materia de prevención de lavado de activos, financiamiento del terrorismo."), candidateName)));
		declarations.add(pepDeclaration);

		declarations.add(buildCheckboxDeclaration(CandidateDeclarationCode.DYNAMIC_COMMITMENTS, getDeclarationParameterValue("DECLARATIONS_D6_TITLE", normalizedLanguage, "Declaraciones"), null, null, applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D6_DECLARATION", normalizedLanguage, "Yo, {{candidateName}}, me comprometo a actuar como individuo y a priorizar el interés de la organización y su comunidad en todo momento, así como al uso adecuado del proceso y de las comunicaciones conforme a lo indicado. Asimismo, declaro bajo juramento la veracidad de la información suministrada."), candidateName), getDeclarationParameterValue("DECLARATIONS_D6_DESCRIPTION_HTML", normalizedLanguage,
				"<p><strong>Actuación como individuo y prioridad del interés de la organización</strong><br>En caso de ser elegida como miembro de los órganos electivos de la organización, actuaré como individuo y no en representación de la organización a la cual pertenezco, anteponiendo por encima de cualquier otro interés el de la organización y su comunidad (Cap. V, art. 20 - estatuto de la organización).</p><p><strong>Veracidad de la información</strong><br>Declaro bajo juramento que toda la información proporcionada en este formulario, así como la documentación y respuestas asociadas, es completa, veraz, actualizada y correcta. Asimismo, me comprometo a informar oportunamente a la organización cualquier modificación relevante que pudiera producirse durante el proceso electoral.</p><p><strong>Uso adecuado del proceso y de las comunicaciones</strong><br>Me comprometo a no realizar prácticas de spam electoral, ni a utilizar los datos, canales de comunicación o instancias del proceso electoral de la organización para fines distintos a los expresamente previstos, respetando en todo momento los principios de buena fe, transparencia y equidad del proceso.</p>")));

		declarations.add(buildCheckboxDeclaration(CandidateDeclarationCode.DATA_USAGE_AND_PUBLICATION, getDeclarationParameterValue("DECLARATIONS_D7_TITLE", normalizedLanguage, "Tratamiento, uso y publicación de la información"), null, null, applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D7_DECLARATION", normalizedLanguage, "Yo, {{candidateName}}, autorizo el tratamiento, uso y publicación de la información y respuestas suministradas conforme a lo indicado."), candidateName),
				getDeclarationParameterValue("DECLARATIONS_D7_DESCRIPTION_HTML", normalizedLanguage, "<p>Autorizo expresamente a la organización a recopilar, almacenar, tratar, utilizar y compartir la información suministrada en el marco del proceso electoral, de conformidad con sus políticas institucionales, estatutos y normativas aplicables, y exclusivamente para los fines relacionados con dicho proceso.</p><p>Autorizo a la organización a publicar, total o parcialmente, la información y las respuestas provistas en este formulario, cuando así lo requiera el proceso electoral, a través de sus medios institucionales, plataformas digitales u otros canales de comunicación oficiales. Por la presente otorgo mi consentimiento expreso para que los datos que he proporcionado sean utilizados y publicados en el sitio web de la organización a los efectos de que sus integrantes conozcan las respuestas de los candidatos al cuestionario.</p>")));

		definition.setDeclarations(declarations);
		return definition;
	}

	private CandidateDeclarationsDefinition buildCandidateNonStatutoryDeclarationsDefinition(ElectionType electionType, String candidateNameRaw, String language) {
		String normalizedLanguage = normalizeLanguage(language);
		String candidateName = hasText(candidateNameRaw) ? candidateNameRaw.trim() : DEFAULT_CANDIDATE_NAME;
		CandidateDeclarationsDefinition definition = new CandidateDeclarationsDefinition();
		definition.setElectionType(electionType);

		List<CandidateDeclarationDefinition> declarations = new ArrayList<>();

		if (requiresIanaKnowledgeDeclaration(electionType)) {
			declarations.add(buildCheckboxDeclaration(CandidateDeclarationCode.IANA_KNOWLEDGE, getDeclarationParameterValue("DECLARATIONS_D8_TITLE", normalizedLanguage, "Conocimiento sobre IANA"), null, null, applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D8_DECLARATION", normalizedLanguage, "Yo, {{candidateName}}, declaro tener conocimiento del Acuerdo de Nivel de Servicio (SLA) para el Servicio de Números de la IANA y de la propuesta de la comunidad de números para la transición de IANA."), candidateName), getDeclarationParameterValue("DECLARATIONS_D8_DESCRIPTION_HTML", normalizedLanguage, "<p>Para ser admitida su candidatura, deberá declarar tener conocimiento del Acuerdo de Nivel de Servicio (SLA) para el Servicio de Números de la IANA, así como de la propuesta de la comunidad de números para la transición de IANA.</p>")));
		}

		if (requiresAsoKnowledgeDeclaration(electionType)) {
			declarations.add(buildCheckboxDeclaration(CandidateDeclarationCode.ASO_KNOWLEDGE, getDeclarationParameterValue("DECLARATIONS_D9_TITLE", normalizedLanguage, "Conocimiento sobre ASO AC y PDP"), null, null, applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D9_DECLARATION", normalizedLanguage, "Yo, {{candidateName}}, declaro tener conocimiento del proceso de desarrollo de políticas de LACNIC, del rol del ASO AC y de las formas de participación de la comunidad en estos procesos."), candidateName), getDeclarationParameterValue("DECLARATIONS_D9_DESCRIPTION_HTML", normalizedLanguage, "<p>Para ser admitida su candidatura, deberá declarar tener conocimiento del proceso de desarrollo de políticas de LACNIC, del rol del ASO AC y de las formas de participación de la comunidad en estos procesos.</p>")));
		}

		declarations.add(buildCheckboxDeclaration(CandidateDeclarationCode.DYNAMIC_COMMITMENTS, getDeclarationParameterValue("DECLARATIONS_D6_TITLE", normalizedLanguage, "Declaraciones"), null, null, applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D6_DECLARATION", normalizedLanguage, "Yo, {{candidateName}}, me comprometo a actuar como individuo y a priorizar el interés de la organización y su comunidad en todo momento, así como al uso adecuado del proceso y de las comunicaciones conforme a lo indicado. Asimismo, declaro bajo juramento la veracidad de la información suministrada."), candidateName), getDeclarationParameterValue("DECLARATIONS_D6_DESCRIPTION_HTML", normalizedLanguage,
				"<p><strong>Actuación como individuo y prioridad del interés de la organización</strong><br>En caso de ser elegida como miembro de los órganos electivos de la organización, actuaré como individuo y no en representación de la organización a la cual pertenezco, anteponiendo por encima de cualquier otro interés el de la organización y su comunidad (Cap. V, art. 20 - estatuto de la organización).</p><p><strong>Veracidad de la información</strong><br>Declaro bajo juramento que toda la información proporcionada en este formulario, así como la documentación y respuestas asociadas, es completa, veraz, actualizada y correcta. Asimismo, me comprometo a informar oportunamente a la organización cualquier modificación relevante que pudiera producirse durante el proceso electoral.</p><p><strong>Uso adecuado del proceso y de las comunicaciones</strong><br>Me comprometo a no realizar prácticas de spam electoral, ni a utilizar los datos, canales de comunicación o instancias del proceso electoral de la organización para fines distintos a los expresamente previstos, respetando en todo momento los principios de buena fe, transparencia y equidad del proceso.</p>")));

		declarations.add(buildCheckboxDeclaration(CandidateDeclarationCode.DATA_USAGE_AND_PUBLICATION, getDeclarationParameterValue("DECLARATIONS_D7_TITLE", normalizedLanguage, "Tratamiento, uso y publicación de la información"), null, null, applyCandidateName(getDeclarationParameterValue("DECLARATIONS_D7_DECLARATION", normalizedLanguage, "Yo, {{candidateName}}, autorizo el tratamiento, uso y publicación de la información y respuestas suministradas conforme a lo indicado."), candidateName),
				getDeclarationParameterValue("DECLARATIONS_D7_DESCRIPTION_HTML", normalizedLanguage, "<p>Autorizo expresamente a la organización a recopilar, almacenar, tratar, utilizar y compartir la información suministrada en el marco del proceso electoral, de conformidad con sus políticas institucionales, estatutos y normativas aplicables, y exclusivamente para los fines relacionados con dicho proceso.</p><p>Autorizo a la organización a publicar, total o parcialmente, la información y las respuestas provistas en este formulario, cuando así lo requiera el proceso electoral, a través de sus medios institucionales, plataformas digitales u otros canales de comunicación oficiales. Por la presente otorgo mi consentimiento expreso para que los datos que he proporcionado sean utilizados y publicados en el sitio web de la organización a los efectos de que sus integrantes conozcan las respuestas de los candidatos al cuestionario.</p>")));

		definition.setDeclarations(declarations);
		return definition;
	}

	private CandidateDeclarationDefinition buildCheckboxDeclaration(CandidateDeclarationCode code, String title, String regulationLinkLabel, String regulationLinkUrl, String declarationText, String descriptionHtml) {
		CandidateDeclarationDefinition declaration = new CandidateDeclarationDefinition();
		declaration.setCode(code);
		declaration.setInputType(CandidateDeclarationInputType.CHECKBOX);
		declaration.setRequired(true);
		declaration.setTitle(title);
		declaration.setRegulationLinkLabel(regulationLinkLabel);
		declaration.setRegulationLinkUrl(regulationLinkUrl);
		declaration.setDeclarationText(declarationText);
		declaration.setDescriptionHtml(descriptionHtml);
		return declaration;
	}

	private String normalizeLanguage(String language) {
		return normalizeLanguageCode(language).getLocaleCode();
	}

	private LanguageCode normalizeLanguageCode(String language) {
		return LanguageCode.fromValueOrDefault(language, LanguageCode.SP);
	}

	private String resolveRegulationLinkLabel(String keyPrefix, String language, String defaultSpanish) {
		return getDeclarationParameterValue(keyPrefix, language, defaultSpanish);
	}

	private String getDeclarationParameterValue(String keyPrefix, String language, String defaultSpanish) {
		String normalizedLanguage = normalizeLanguage(language);
		String parameterKey = keyPrefix + "_" + normalizedLanguage.toUpperCase(Locale.ROOT);
		String parameterValue = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(parameterKey);
		if (hasText(parameterValue)) {
			return parameterValue;
		}
		if (!"es".equals(normalizedLanguage)) {
			String spanishValue = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(keyPrefix + "_ES");
			if (hasText(spanishValue)) {
				return spanishValue;
			}
		}
		return defaultSpanish;
	}

	private String getTextImprovementPrompt(CandidateTextImprovementInstruction instruction) {
		if (instruction == null || !hasText(instruction.getParameterKeyPrefix())) {
			return null;
		}
		String value = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(instruction.getParameterKeyPrefix());
		if (hasText(value)) {
			if (instruction.isTranslation()) {
				String resolvedTargetLanguage = hasText(instruction.getTargetLanguageLabel()) ? instruction.getTargetLanguageLabel() : "español";
				return value.replace("{{targetLanguage}}", resolvedTargetLanguage);
			}
			return value;
		}
		appLogger.warn("Using default text improvement prompt. parameterKey={}", instruction.getParameterKeyPrefix());
		return OpenAiPromptDefaults.getDefaultTextImprovementPrompt(instruction);
	}

	private String applyCandidateName(String text, String candidateName) {
		if (text == null) {
			return null;
		}
		return text.replace("{{candidateName}}", hasText(candidateName) ? candidateName : DEFAULT_CANDIDATE_NAME);
	}

	private String getDefaultOtherStatutoryQuestionLabel(int index, String language) {
		String normalizedLanguage = normalizeLanguage(language);
		switch (normalizedLanguage) {
		case "en":
			switch (index) {
			case 1:
				return "Could you briefly describe your personal profile and interests?*";
			case 2:
				return "How did your interest in participating in the organization arise and what has your trajectory or link with it been?*";
			case 3:
				return "What is your vision of the organization, its mission, and its contribution to the community's development and growth?*";
			case 4:
			default:
				return "What do you consider your main contribution to the organization as a member of the body, if elected, and what motivates you to apply for that position?*";
			}
		case "pt":
			switch (index) {
			case 1:
				return "Poderia descrever brevemente seu perfil pessoal e seus interesses?*";
			case 2:
				return "Como surgiu seu interesse em participar da organização e qual tem sido sua trajetória ou vínculo com ela?*";
			case 3:
				return "Qual é sua visão sobre a organização, sua missão e sua contribuição para o desenvolvimento e crescimento da comunidade?*";
			case 4:
			default:
				return "Qual considera que seria sua principal contribuição para a organização como integrante do órgão, caso seja eleito, e o que o motiva a se candidatar a essa posição?*";
			}
		case "es":
		default:
			switch (index) {
			case 1:
				return "¿Podría describir brevemente su perfil personal y sus intereses?*";
			case 2:
				return "¿Cómo surgió su interés por participar en la organización y cuál ha sido su trayectoria o vinculación con ella?*";
			case 3:
				return "¿Cuál es su visión sobre la organización, su misión y su contribución al desarrollo y crecimiento de la comunidad?*";
			case 4:
			default:
				return "¿Cuál considera que sería su principal aporte a la organización como integrante del órgano, en caso de ser elegido, y qué le motiva a postularse a dicha posición?*";
			}
		}
	}

	private String getDefaultOtherNonStatutoryQuestionLabel(String language) {
		String normalizedLanguage = normalizeLanguage(language);
		switch (normalizedLanguage) {
		case "en":
			return "What are your main motivations for this nomination, and what value do you expect to contribute from this position?*";
		case "pt":
			return "Quais são suas principais motivações para esta indicação e que valor você espera contribuir a partir desta posição?*";
		case "es":
		default:
			return "¿Cuáles son sus principales motivaciones para esta nominación y qué valor espera aportar desde esta posición?*";
		}
	}

	@Override
	public String getAcceptNominationConditions(String language) {
		String DEFAULT_ACCEPT_NOMINATION_CONDITIONS = "pendiente parametro";
		try {
			LanguageCode normalizedLanguage = normalizeLanguageCode(language);
			String parameterKey;
			switch (normalizedLanguage) {
			case EN:
				parameterKey = Constants.ACCEPT_NOMINATION_CONDITIONS_EN;
				break;
			case PT:
				parameterKey = Constants.ACCEPT_NOMINATION_CONDITIONS_PT;
				break;
			case SP:
			default:
				parameterKey = Constants.ACCEPT_NOMINATION_CONDITIONS_ES;
				break;
			}

			String conditions = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(parameterKey);
			if (conditions == null || conditions.trim().isEmpty()) {
				return DEFAULT_ACCEPT_NOMINATION_CONDITIONS;
			}
			return conditions;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return DEFAULT_ACCEPT_NOMINATION_CONDITIONS;
		}
	}

	// TODO: ger i26 ini
	@Override
	public boolean acceptNomination(String token, String clientIp) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationTokenForUpdate(token);
			if (nomination == null) {
				return false;
			}

			if (nomination.getStatus() == NominationStatus.ACCEPTED_BY_CANDIDATE && nomination.getCandidate() != null) {
				return true;
			}
			if (nomination.getStatus() != NominationStatus.PROPOSED) {
				return false;
			}
			if (!isAcceptNominationWindowOpen(nomination)) {
				return false;
			}
			if (hasAnotherAcceptedNominationWithSameEmail(nomination)) {
				return false;
			}

			List<ElectionTask> electionTasks = ElectionsDaoFactory.createElectionTaskDao(em).getElectionTasks(nomination.getElection().getElectionId());
			if (electionTasks == null || electionTasks.isEmpty()) {
				appLogger.error("Cannot accept nomination {} for election {} because it has no election tasks configured", nomination.getId(),
						nomination.getElection() != null ? nomination.getElection().getElectionId() : null);
				return false;
			}

			Candidate candidate = createCandidateFromNomination(nomination);
			em.persist(candidate);
			nomination.setCandidate(candidate);

			for (ElectionTask electionTask : electionTasks) {
				CandidateElectionTaskProgress candidateTaskProgress = new CandidateElectionTaskProgress(candidate, electionTask, CandidateElectionTaskStatus.NOT_STARTED);
				em.persist(candidateTaskProgress);
			}

			nomination.setStatus(NominationStatus.ACCEPTED_BY_CANDIDATE);
			em.merge(nomination);

			EJBFactory.getInstance().getMailsSendingEJB().queueNominationAcceptedToRepresentative(nomination.getId());
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	private boolean isAcceptNominationWindowOpen(Nomination nomination) {
		if (nomination == null || nomination.getElection() == null || !nomination.getElection().isNominationTasksLinkAvailable()) {
			return false;
		}
		return isElectionN2WindowOpen(nomination.getElection().getElectionId());
	}

	private boolean isElectionN2WindowOpen(long electionId) {
		ElectionCalendar calendar = ElectionsDaoFactory.createElectionCalendarDao(em).getElectionCalendarByKey(
				electionId,
				ElectionCalendarKey.N_2_PERIODO_CALL_FOR_CANDIDATES);
		return isCalendarWindowOpen(calendar, new Date());
	}

	private boolean isCalendarWindowOpen(ElectionCalendar calendar, Date now) {
		if (calendar == null || calendar.getStartDate() == null || now == null) {
			return false;
		}
		if (now.before(calendar.getStartDate())) {
			return false;
		}
		return calendar.getEndDate() == null || !now.after(calendar.getEndDate());
	}

	@Override
	public boolean rejectNomination(String token, String clientIp) {
		try {
			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationTokenForUpdate(token);
			if (nomination == null) {
				return false;
			}

			if (nomination.getStatus() == NominationStatus.REJECTED_BY_CANDIDATE) {
				return true;
			}

			if (nomination.getStatus() != NominationStatus.PROPOSED) {
				return false;
			}
			if (!isAcceptNominationWindowOpen(nomination)) {
				return false;
			}

			nomination.setStatus(NominationStatus.REJECTED_BY_CANDIDATE);
			if (nomination.getSupports() != null) {
				for (SupportNomination supportNomination : nomination.getSupports()) {
					if (supportNomination.getSupportStatus() == SupportStatus.PROPOSED) {
						supportNomination.setSupportStatus(SupportStatus.REJECTED);
					}
				}
			}
			em.merge(nomination);

			EJBFactory.getInstance().getMailsSendingEJB().queueNominationRejectedToRepresentative(nomination.getId());
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean updateCandidateReminderFrequency(String token, ReminderFrequency reminderFrequency, String clientIp) {
		try {
			if (!hasText(token)) {
				return false;
			}

			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return false;
			}

			Candidate candidate = em.find(Candidate.class, nomination.getCandidate().getCandidateId());
			if (candidate == null || candidate.getElection() == null) {
				return false;
			}

			ReminderFrequency resolvedFrequency = reminderFrequency == null ? ReminderFrequency.defaultValue() : reminderFrequency;
			candidate.setReminderFrequency(resolvedFrequency);
			em.merge(candidate);

			String activityUserName = hasText(candidate.getMail()) ? candidate.getMail().trim() : CANDIDATE_ACTIVITY_PREFIX + candidate.getCandidateId();
			String description = activityUserName + " actualizó su frecuencia de recordatorios a " + resolvedFrequency.name() + TEXT_ELECCION + candidate.getElection().getTitleSpanish() + ACTIVITY_SOURCE_FRAGMENT + ACTIVITY_SOURCE_CANDIDATE_LINK + ACTIVITY_CANDIDATE_ID_FRAGMENT + candidate.getCandidateId();
			persistActivity(activityUserName, ActivityType.EDIT_CANDIDATES, description, clientIp, candidate.getElection().getElectionId());
			return true;
		} catch (Exception e) {
			appLogger.error("Error updating candidate reminder frequency", e);
			return false;
		}
	}

	@Override
	public List<CandidateQuestion> getCandidateQuestionsForNominationTasks(String token) {
		try {
			if (!hasText(token)) {
				return new ArrayList<>();
			}

			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null) {
				return new ArrayList<>();
			}

			return ElectionsDaoFactory.createCandidateQuestionDao(em).getCandidateQuestionsByCandidateId(nomination.getCandidate().getCandidateId());
		} catch (Exception e) {
			appLogger.error("Error loading candidate questions for nomination tasks", e);
			return new ArrayList<>();
		}
	}

	@Override
	public boolean submitCandidateQuestionAnswer(String token, long candidateQuestionId, String answerText, String actor, String ip) {
		try {
			if (!hasText(token) || candidateQuestionId <= 0 || !hasText(answerText)) {
				return false;
			}

			String normalizedAnswer = answerText.trim();
			if (normalizedAnswer.length() > 1000) {
				normalizedAnswer = normalizedAnswer.substring(0, 1000);
			}

			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE || nomination.getCandidate() == null || nomination.getElection() == null) {
				return false;
			}
			if (!isCandidateQuestionsWindowOpen(nomination.getElection().getElectionId(), new Date())) {
				return false;
			}

			CandidateQuestion currentQuestion = ElectionsDaoFactory.createCandidateQuestionDao(em).getCandidateQuestionByIdAndCandidateId(candidateQuestionId, nomination.getCandidate().getCandidateId());
			if (currentQuestion == null || currentQuestion.getStatus() != CandidateQuestionStatus.QUESTION_READY_FOR_CANDIDATE) {
				return false;
			}

			CandidateQuestion updatedQuestionData = new CandidateQuestion();
			updatedQuestionData.setCandidateQuestionId(currentQuestion.getCandidateQuestionId());
			updatedQuestionData.setElection(currentQuestion.getElection());
			updatedQuestionData.setCandidate(currentQuestion.getCandidate());
			updatedQuestionData.setAskedByName(currentQuestion.getAskedByName());
			updatedQuestionData.setAskedByEmail(currentQuestion.getAskedByEmail());
			updatedQuestionData.setQuestionLanguage(currentQuestion.getQuestionLanguage());
			updatedQuestionData.setQuestionSpanish(currentQuestion.getQuestionSpanish());
			updatedQuestionData.setQuestionEnglish(currentQuestion.getQuestionEnglish());
			updatedQuestionData.setQuestionPortuguese(currentQuestion.getQuestionPortuguese());
			updatedQuestionData.setAnswerSpanish(normalizedAnswer);
			updatedQuestionData.setAnswerEnglish(normalizedAnswer);
			updatedQuestionData.setAnswerPortuguese(normalizedAnswer);
			updatedQuestionData.setStatus(CandidateQuestionStatus.ANSWER_SUBMITTED_BY_CANDIDATE);

			String resolvedActor = hasText(actor) ? actor.trim() : resolveCandidateActivityUserName(currentQuestion.getCandidate());
			CandidateQuestion persisted = EJBFactory.getInstance().getElectionsManagerEJB().saveCandidateQuestion(updatedQuestionData, true, resolvedActor, ip);
			return persisted != null;
		} catch (Exception e) {
			appLogger.error("Error submitting candidate question answer", e);
			return false;
		}
	}

	@Override
	public boolean updateCandidateTaskStatus(String token, ElectionTaskKey taskKey, CandidateElectionTaskStatus status, String clientIp) {
		try {
			if (token == null || token.trim().isEmpty() || taskKey == null || status == null) {
				return false;
			}

			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || nomination.getCandidate() == null) {
				return false;
			}
			nomination = lockCandidateSupportAggregate(nomination);
			if (nomination == null) {
				return false;
			}
			if (nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE) {
				return false;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return false;
			}

			CandidateElectionTaskProgress taskProgress = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).getByNominationTokenAndTaskKeyForUpdate(token, taskKey);
			if (taskProgress == null) {
				return false;
			}

			long candidateId = nomination.getCandidate().getCandidateId();
			CandidateElectionTaskStatus previousStatus = taskProgress.getStatus();
			boolean allTasksCompletedBefore = areAllCandidateTasksCompleted(candidateId);
			if (status != CandidateElectionTaskStatus.NOT_STARTED && hasPendingTasksInPreviousDependencyLevel(candidateId, taskProgress)) {
				return false;
			}

			Date now = new Date();
			if (isTaskDeadlinePassed(taskProgress, now)) {
				return false;
			}
			switch (status) {
			case NOT_STARTED:
				taskProgress.setStatus(CandidateElectionTaskStatus.NOT_STARTED);
				taskProgress.setStartDate(null);
				taskProgress.setEndDate(now);
				if (taskKey == ElectionTaskKey.COURSE || taskKey == ElectionTaskKey.EVALUATION) {
					Candidate managedCandidate = em.find(Candidate.class, candidateId);
					if (managedCandidate != null) {
						if (taskKey == ElectionTaskKey.COURSE) {
							managedCandidate.setCampusCourseStatus(CandidateCampusCourseStatus.PENDING);
							managedCandidate.setCampusCourseSelected(null);
						} else {
							managedCandidate.setEvaluationStatus(CandidateEvaluationStatus.PENDING);
						}
						em.merge(managedCandidate);
					}
				}
				break;
			case STARTED:
				taskProgress.setStatus(CandidateElectionTaskStatus.STARTED);
				if (taskProgress.getStartDate() == null) {
					taskProgress.setStartDate(now);
				}
				taskProgress.setEndDate(now);
				if (taskKey == ElectionTaskKey.COURSE || taskKey == ElectionTaskKey.EVALUATION) {
					Candidate managedCandidate = em.find(Candidate.class, candidateId);
					if (managedCandidate != null) {
						if (taskKey == ElectionTaskKey.COURSE) {
							managedCandidate.setCampusCourseStatus(resolveDebugCourseStatusForStarted(managedCandidate.getCampusCourseStatus()));
						} else {
							managedCandidate.setEvaluationStatus(resolveDebugEvaluationStatusForStarted(managedCandidate.getEvaluationStatus()));
						}
						em.merge(managedCandidate);
					}
				}
				break;
			case COMPLETED:
				taskProgress.setStatus(CandidateElectionTaskStatus.COMPLETED);
				if (taskProgress.getStartDate() == null) {
					taskProgress.setStartDate(now);
				}
				taskProgress.setEndDate(now);
				if (taskKey == ElectionTaskKey.COURSE || taskKey == ElectionTaskKey.EVALUATION) {
					Candidate managedCandidate = em.find(Candidate.class, candidateId);
					if (managedCandidate != null) {
						if (taskKey == ElectionTaskKey.COURSE) {
							managedCandidate.setCampusCourseStatus(CandidateCampusCourseStatus.COMPLETED);
						} else {
							managedCandidate.setEvaluationStatus(CandidateEvaluationStatus.COMPLETED);
						}
						em.merge(managedCandidate);
					}
				}
				break;
			case OMITTED:
				taskProgress.setStatus(CandidateElectionTaskStatus.OMITTED);
				if (taskProgress.getStartDate() == null) {
					taskProgress.setStartDate(now);
				}
				taskProgress.setEndDate(now);
				break;
			default:
				return false;
			}

			em.merge(taskProgress);
			if (status == CandidateElectionTaskStatus.COMPLETED && previousStatus != CandidateElectionTaskStatus.COMPLETED) {
				EJBFactory.getInstance().getMailsSendingEJB().queueCandidateTaskCompletedToElectionSender(candidateId, taskKey.name());
			}
			boolean allTasksCompletedAfter = areAllCandidateTasksCompleted(candidateId);
			if (!allTasksCompletedBefore && allTasksCompletedAfter) {
				EJBFactory.getInstance().getMailsSendingEJB().queueCandidateAllTasksCompletedToElectionSender(candidateId);
			}
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean updateCandidateEvaluationStartedVariant(String token, boolean moveToSent, String clientIp) {
		try {
			if (!hasText(token)) {
				return false;
			}

			Nomination nomination = ElectionsDaoFactory.createNominationDao(em).getNominationByAcceptNominationToken(token);
			if (nomination == null || nomination.getCandidate() == null) {
				return false;
			}

			if (nomination.getStatus() != NominationStatus.ACCEPTED_BY_CANDIDATE) {
				return false;
			}
			if (isNominationTaskWindowClosed(nomination)) {
				return false;
			}

			CandidateElectionTaskProgress taskProgress = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).getByNominationTokenAndTaskKey(token, ElectionTaskKey.EVALUATION);
			if (taskProgress == null) {
				return false;
			}

			long candidateId = nomination.getCandidate().getCandidateId();
			if (hasPendingTasksInPreviousDependencyLevel(candidateId, taskProgress)) {
				return false;
			}

			Date now = new Date();
			if (isTaskDeadlinePassed(taskProgress, now)) {
				return false;
			}

			Candidate managedCandidate = em.find(Candidate.class, candidateId);
			if (managedCandidate == null) {
				return false;
			}

			taskProgress.setStatus(CandidateElectionTaskStatus.STARTED);
			taskProgress.setStartDate(now);
			taskProgress.setEndDate(now);

			managedCandidate.setEvaluationStatus(moveToSent ? CandidateEvaluationStatus.CREDENTIALS_SENT : CandidateEvaluationStatus.CREDENTIALS_REQUESTED);

			em.merge(managedCandidate);
			em.merge(taskProgress);
			return true;
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	private CandidateCampusCourseStatus resolveDebugCourseStatusForStarted(CandidateCampusCourseStatus currentStatus) {
		if (currentStatus == null || currentStatus == CandidateCampusCourseStatus.PENDING) {
			return CandidateCampusCourseStatus.SENT;
		}
		if (currentStatus == CandidateCampusCourseStatus.SENT) {
			return CandidateCampusCourseStatus.STARTED;
		}
		if (currentStatus == CandidateCampusCourseStatus.STARTED) {
			return CandidateCampusCourseStatus.STARTED;
		}
		if (currentStatus == CandidateCampusCourseStatus.NOT_APPLICABLE) {
			return CandidateCampusCourseStatus.NOT_APPLICABLE;
		}
		return CandidateCampusCourseStatus.STARTED;
	}

	private CandidateEvaluationStatus resolveDebugEvaluationStatusForStarted(CandidateEvaluationStatus currentStatus) {
		if (currentStatus == null || currentStatus == CandidateEvaluationStatus.PENDING) {
			return CandidateEvaluationStatus.CREDENTIALS_REQUESTED;
		}
		if (currentStatus == CandidateEvaluationStatus.CREDENTIALS_REQUESTED) {
			return CandidateEvaluationStatus.CREDENTIALS_SENT;
		}
		if (currentStatus == CandidateEvaluationStatus.CREDENTIALS_SENT) {
			return CandidateEvaluationStatus.CREDENTIALS_SENT;
		}
		if (currentStatus == CandidateEvaluationStatus.NOT_APPLICABLE) {
			return CandidateEvaluationStatus.NOT_APPLICABLE;
		}
		return CandidateEvaluationStatus.CREDENTIALS_SENT;
	}

	private boolean areAllCandidateTasksCompleted(long candidateId) {
		if (candidateId <= 0) {
			return false;
		}
		List<CandidateElectionTaskProgress> taskProgressRows = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).getByCandidateId(candidateId);
		if (taskProgressRows == null || taskProgressRows.isEmpty()) {
			return false;
		}
		for (CandidateElectionTaskProgress row : taskProgressRows) {
			if (row == null) {
				return false;
			}
			CandidateElectionTaskStatus rowStatus = row.getStatus();
			if (rowStatus != CandidateElectionTaskStatus.COMPLETED && rowStatus != CandidateElectionTaskStatus.OMITTED) {
				return false;
			}
		}
		return true;
	}

	private boolean hasPendingTasksInPreviousDependencyLevel(long candidateId, CandidateElectionTaskProgress targetTaskProgress) {
		if (targetTaskProgress == null || targetTaskProgress.getElectionTask() == null) {
			return false;
		}
		TaskDependencyLevel dependencyLevel = targetTaskProgress.getElectionTask().getDependencyLevel();
		if (dependencyLevel == null || dependencyLevel.isFirstLevel()) {
			return false;
		}
		int previousLevelIndex = dependencyLevel.getIndex() - 1;
		TaskDependencyLevel previousLevel = TaskDependencyLevel.fromIndex(previousLevelIndex);

		List<CandidateElectionTaskProgress> taskProgressRows = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).getByCandidateId(candidateId);
		for (CandidateElectionTaskProgress row : taskProgressRows) {
			if (row == null || row.getElectionTask() == null) {
				continue;
			}
			TaskDependencyLevel rowLevel = row.getElectionTask().getDependencyLevel();
			if (rowLevel == previousLevel && row.getStatus() != CandidateElectionTaskStatus.COMPLETED) {
				return true;
			}
		}
		return false;
	}

	private void touchTaskLastUpdate(String token, ElectionTaskKey taskKey) {
		if (token == null || token.trim().isEmpty() || taskKey == null) {
			return;
		}
		CandidateElectionTaskProgress taskProgress = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).getByNominationTokenAndTaskKeyForUpdate(token, taskKey);
		if (taskProgress == null) {
			return;
		}
		Date now = new Date();
		if (taskProgress.getStartDate() == null) {
			taskProgress.setStartDate(now);
		}
		taskProgress.setEndDate(now);
		em.merge(taskProgress);
	}

	private boolean isTaskDeadlinePassed(CandidateElectionTaskProgress taskProgress, Date referenceDate) {
		if (taskProgress == null || taskProgress.getElectionTask() == null || taskProgress.getElectionTask().getElectionCalendar() == null || referenceDate == null) {
			return false;
		}
		Date deadline = taskProgress.getElectionTask().getElectionCalendar().getEndDate();
		return deadline != null && deadline.before(referenceDate);
	}

	private boolean isNominationTaskWindowClosed(Nomination nomination) {
		if (nomination == null || nomination.getCandidate() == null) {
			return false;
		}

		List<CandidateElectionTaskProgress> taskProgressRows = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).getByCandidateId(nomination.getCandidate().getCandidateId());
		if (taskProgressRows == null || taskProgressRows.isEmpty()) {
			return false;
		}

		Date latestDeadline = null;
		for (CandidateElectionTaskProgress taskProgress : taskProgressRows) {
			if (taskProgress == null || taskProgress.getElectionTask() == null || taskProgress.getElectionTask().getElectionCalendar() == null) {
				continue;
			}
			Date deadline = taskProgress.getElectionTask().getElectionCalendar().getEndDate();
			if (deadline == null) {
				continue;
			}
			if (latestDeadline == null || deadline.after(latestDeadline)) {
				latestDeadline = deadline;
			}
		}

		return latestDeadline != null && latestDeadline.before(new Date());
	}

	private void markTaskAsStartedIfPending(String token, ElectionTaskKey taskKey) {
		if (token == null || token.trim().isEmpty() || taskKey == null) {
			return;
		}
		CandidateElectionTaskProgress taskProgress = ElectionsDaoFactory.createCandidateElectionTaskProgressDao(em).getByNominationTokenAndTaskKeyForUpdate(token, taskKey);
		if (taskProgress == null) {
			return;
		}
		if (taskProgress.getStatus() == CandidateElectionTaskStatus.COMPLETED || taskProgress.getStatus() == CandidateElectionTaskStatus.OMITTED) {
			return;
		}

		Date now = new Date();
		taskProgress.setStatus(CandidateElectionTaskStatus.STARTED);
		if (taskProgress.getStartDate() == null) {
			taskProgress.setStartDate(now);
		}
		taskProgress.setEndDate(now);
		em.merge(taskProgress);
	}

	private Candidate createCandidateFromNomination(Nomination nomination) {
		Candidate candidate = new Candidate();
		candidate.setElection(nomination.getElection());
		candidate.setName(nomination.getNominationName());
		candidate.setMail(nomination.getNominationEmail());
		candidate.setOnlySp(true);
		candidate.setStatus(CandidateStatus.INCOMPLETE);
		candidate.setCampusCourseStatus(CandidateCampusCourseStatus.PENDING);
		candidate.setEvaluationStatus(CandidateEvaluationStatus.PENDING);
		try {
			Object[] defaultPhoto = FilesUtils.getDefaultPhoto(null);
			candidate.setPictureInfo((byte[]) defaultPhoto[0]);
			candidate.setPictureName((String) defaultPhoto[1]);
			candidate.setPictureExtension((String) defaultPhoto[2]);
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			candidate.setPictureInfo(new byte[0]);
			candidate.setPictureName("default_candidate_photo.jpg");
			candidate.setPictureExtension("jpg");
		}
		candidate.setCandidateOrder(ElectionsDaoFactory.createCandidateDao(em).getLastNonFixedCandidateOrder(nomination.getElection().getElectionId()) + 1);
		return candidate;
	}

	private boolean hasAnotherAcceptedNominationWithSameEmail(Nomination nomination) {
		if (nomination == null || nomination.getElection() == null || nomination.getId() <= 0 || nomination.getNominationEmail() == null) {
			return false;
		}
		String normalizedEmail = nomination.getNominationEmail().trim().toLowerCase(Locale.ROOT);
		if (normalizedEmail.isEmpty()) {
			return false;
		}
		Long count = em.createQuery("SELECT COUNT(n) FROM Nomination n WHERE n.election.electionId = :electionId AND n.id <> :nominationId AND n.status = :status AND lower(trim(n.nominationEmail)) = :email", Long.class).setParameter("electionId", nomination.getElection().getElectionId()).setParameter("nominationId", nomination.getId()).setParameter("status", NominationStatus.ACCEPTED_BY_CANDIDATE).setParameter("email", normalizedEmail).getSingleResult();
		return count != null && count.longValue() > 0;
	}

	private static final class TextImprovementAccessContext {
		private final Candidate candidate;
		private final String rateLimitScopeKey;
		private final boolean openAiAllowed;

		private TextImprovementAccessContext(Candidate candidate, String rateLimitScopeKey, boolean openAiAllowed) {
			this.candidate = candidate;
			this.rateLimitScopeKey = rateLimitScopeKey;
			this.openAiAllowed = openAiAllowed;
		}

		private static TextImprovementAccessContext allowOpenAi(Candidate candidate, String rateLimitScopeKey) {
			return new TextImprovementAccessContext(candidate, rateLimitScopeKey, true);
		}

		private static TextImprovementAccessContext denyOpenAi(String rateLimitScopeKey) {
			return new TextImprovementAccessContext(null, rateLimitScopeKey, false);
		}

		private Candidate getCandidate() {
			return candidate;
		}

		private String getRateLimitScopeKey() {
			return rateLimitScopeKey;
		}

		private boolean isOpenAiAllowed() {
			return openAiAllowed;
		}
	}

	// TODO: ger i26 fin
}
