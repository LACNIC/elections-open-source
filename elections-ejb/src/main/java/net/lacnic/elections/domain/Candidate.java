package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import net.lacnic.elections.domain.pre.AuditorCandidateDecision;
import net.lacnic.elections.domain.pre.CandidateCampusCourseStatus;
import net.lacnic.elections.domain.pre.CandidateCountryLink;
import net.lacnic.elections.domain.pre.CandidateElectionTaskProgress;
import net.lacnic.elections.domain.pre.CandidateEvaluationStatus;
import net.lacnic.elections.domain.pre.CandidatePepDeclaration;
import net.lacnic.elections.domain.pre.CandidateQuestion;
import net.lacnic.elections.domain.pre.CandidateStatus;
import net.lacnic.elections.domain.pre.CandidateWorkOrganization;
import net.lacnic.elections.utils.Constants;

@Entity
public class Candidate implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidate_seq")
	@SequenceGenerator(name = "candidate_seq", sequenceName = "candidate_seq", allocationSize = 1)
	@Column(name = "candidate_id")
	private long candidateId;

	@Column(nullable = true, name = "migration_id")
	private Long migrationId;

	@Column(nullable = false, length = 1000)
	private String name;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id")
	private Election election;

	@Column(nullable = false)
	private byte[] pictureInfo;

	@Column(nullable = false)
	private String pictureName;

	@Column(nullable = false)
	private String pictureExtension;

	@Column(columnDefinition = "TEXT")
	private String bioSpanish;

	@Column(columnDefinition = "TEXT")
	private String bioEnglish;

	@Column(columnDefinition = "TEXT")
	private String bioPortuguese;

	@Column
	private int candidateOrder;

	@Column(nullable = true)
	private boolean onlySp;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String linkSpanish;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String linkEnglish;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String linkPortuguese;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String linkedinUrl;

	@OneToMany(mappedBy = "candidate", cascade = CascadeType.REMOVE)
	private List<Vote> votes;

	@Column(nullable = true)
	private String mail;

	@Column(nullable = false)
	private boolean winner;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true, name = "reminder_frequency", length = 64)
	private ReminderFrequency reminderFrequency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private CandidateStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true, name = "candidate_type", length = 32)
	private CandidateType candidateType;

	// questions

	@Column(nullable = true)
	private Boolean qUnemployed;

	@Column(nullable = true)
	private Boolean qCanSpeakSpanish;

	@Column(nullable = true)
	private Boolean qLegalLimitationAnyCountry;

	@Column(nullable = true)
	private Boolean qHealthTravelLimitation;

	@Column(nullable = true)
	private Boolean qHealthMentalLimitation;

	@Column(nullable = true)
	private Boolean qAdultInCountry;

	@Column(nullable = true)
	private Boolean qCivilRightsLimitation;

	// OTHER_STATUTORY_QUESTIONS START
	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer1Spanish;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer1English;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer1Portuguese;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer2Spanish;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer2English;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer2Portuguese;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer3Spanish;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer3English;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer3Portuguese;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer4Spanish;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer4English;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherStatutoryAnswer4Portuguese;
	// OTHER_STATUTORY_QUESTIONS END

	// OTHER_NON_STATUTORY_QUESTIONS START
	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherNonStatutoryAnswer1Spanish;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherNonStatutoryAnswer1English;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String qOtherNonStatutoryAnswer1Portuguese;
	// OTHER_NON_STATUTORY_QUESTIONS END

	// DECLARATIONS START
	@Column(nullable = true, name = "q_declaration_incompatibilities")
	private Boolean qDeclarationIncompatibilities;

	@Column(nullable = true, name = "q_declaration_conflicts_interest")
	private Boolean qDeclarationConflictsOfInterest;

	@Column(nullable = true, name = "q_declaration_competencies")
	private Boolean qDeclarationCompetenciesAndSuitability;

	@Column(nullable = true, name = "q_declaration_disciplinary")
	private Boolean qDeclarationDisciplinaryRegulation;

	@Column(nullable = true, name = "q_declaration_iana_knowledge")
	private Boolean qDeclarationIanaKnowledge;

	@Column(nullable = true, name = "q_declaration_aso_knowledge")
	private Boolean qDeclarationAsoKnowledge;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true, name = "q_declaration_pep")
	private CandidatePepDeclaration qDeclarationPep;

	@Column(nullable = true, name = "q_declaration_dynamic")
	private Boolean qDeclarationDynamicCommitments;

	@Column(nullable = true, name = "q_declaration_data_usage")
	private Boolean qDeclarationDataUsageAndPublication;
	// DECLARATIONS END

	@Column(nullable = true)
	private byte[] proctorioResultFile;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private CandidateCampusCourseStatus campusCourseStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private CandidateEvaluationStatus evaluationStatus;

	@Column(nullable = true, length = 255)
	private String campusCourseCalification;

	@Column(nullable = true)
	private Long campusCourseSelected;

//	@OneToOne(optional = true)
//	@JoinColumn(name = "nomination_id", nullable = true, unique = true)
//	private Nomination nomination;

	@OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CandidateWorkOrganization> workOrganizations;

	@OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CandidateCountryLink> countryLinks;

	@OneToMany(mappedBy = "candidate", cascade = CascadeType.REMOVE)
	private List<AuditorCandidateDecision> auditorDecisions;

	@OneToMany(mappedBy = "candidate", cascade = CascadeType.REMOVE)
	private List<CandidateQuestion> candidateQuestions;

	@OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CandidateElectionTaskProgress> taskProgress;

	public Candidate() {
		this.reminderFrequency = ReminderFrequency.defaultValue();
		this.candidateType = CandidateType.NORMAL;
	}

	public String getBio(String displayName) {
		LanguageCode languageCode = LanguageCode.fromValueOrDefault(displayName, LanguageCode.SP);
		switch (languageCode) {
		case EN:
			return getBioEnglish();
		case PT:
			return getBioPortuguese();
		case SP:
		default:
			return getBioSpanish();
		}
	}

	public String getLink(String displayName) {
		LanguageCode languageCode = LanguageCode.fromValueOrDefault(displayName, LanguageCode.SP);
		switch (languageCode) {
		case EN:
			return getLinkEnglish();
		case PT:
			return getLinkPortuguese();
		case SP:
		default:
			return getLinkSpanish();
		}
	}

	public void copyBioToOtherLanguages() {
		setBioEnglish(getBioSpanish());
		setBioPortuguese(getBioSpanish());
		setLinkEnglish(getLinkSpanish());
		setLinkPortuguese(getLinkSpanish());
		setQOtherStatutoryAnswer1English(getQOtherStatutoryAnswer1Spanish());
		setQOtherStatutoryAnswer1Portuguese(getQOtherStatutoryAnswer1Spanish());
		setQOtherStatutoryAnswer2English(getQOtherStatutoryAnswer2Spanish());
		setQOtherStatutoryAnswer2Portuguese(getQOtherStatutoryAnswer2Spanish());
		setQOtherStatutoryAnswer3English(getQOtherStatutoryAnswer3Spanish());
		setQOtherStatutoryAnswer3Portuguese(getQOtherStatutoryAnswer3Spanish());
		setQOtherStatutoryAnswer4English(getQOtherStatutoryAnswer4Spanish());
		setQOtherStatutoryAnswer4Portuguese(getQOtherStatutoryAnswer4Spanish());
		setQOtherNonStatutoryAnswer1English(getQOtherNonStatutoryAnswer1Spanish());
		setQOtherNonStatutoryAnswer1Portuguese(getQOtherNonStatutoryAnswer1Spanish());
	}

	public void clean() {
		this.name = null;
		this.pictureInfo = null;
		this.pictureName = null;
		this.bioSpanish = null;
		this.bioEnglish = null;
		this.bioPortuguese = null;
		this.pictureExtension = null;
		this.mail = null;
	}

	public boolean isFixed() {
		return getCandidateOrder() == Constants.MIN_ORDER || getCandidateOrder() == Constants.MAX_ORDER;
	}

	public long getCandidateId() {
		return candidateId;
	}

	public void setCandidateId(long candidateId) {
		this.candidateId = candidateId;
	}

	public Long getMigrationId() {
		return migrationId;
	}

	public void setMigrationId(Long migrationId) {
		this.migrationId = migrationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public byte[] getPictureInfo() {
		return pictureInfo;
	}

	public void setPictureInfo(byte[] pictureInfo) {
		this.pictureInfo = pictureInfo;
	}

	public String getPictureName() {
		return pictureName;
	}

	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
	}

	public String getBioSpanish() {
		return bioSpanish;
	}

	public void setBioSpanish(String bioSpanish) {
		this.bioSpanish = bioSpanish;
	}

	public String getBioEnglish() {
		return bioEnglish;
	}

	public void setBioEnglish(String bioEnglish) {
		this.bioEnglish = bioEnglish;
	}

	public String getBioPortuguese() {
		return bioPortuguese;
	}

	public void setBioPortuguese(String bioPortuguese) {
		this.bioPortuguese = bioPortuguese;
	}

	public String getPictureExtension() {
		return pictureExtension;
	}

	public void setPictureExtension(String pictureExtension) {
		this.pictureExtension = pictureExtension;
	}

	public int getCandidateOrder() {
		return candidateOrder;
	}

	public void setCandidateOrder(int candidateOrder) {
		this.candidateOrder = candidateOrder;
	}

	public boolean isOnlySp() {
		return onlySp;
	}

	public void setOnlySp(boolean onlySp) {
		this.onlySp = onlySp;
	}

	public String getLinkSpanish() {
		return linkSpanish;
	}

	public void setLinkSpanish(String linkSpanish) {
		this.linkSpanish = linkSpanish;
	}

	public String getLinkEnglish() {
		return linkEnglish;
	}

	public void setLinkEnglish(String linkEnglish) {
		this.linkEnglish = linkEnglish;
	}

	public String getLinkPortuguese() {
		return linkPortuguese;
	}

	public void setLinkPortuguese(String linkPortuguese) {
		this.linkPortuguese = linkPortuguese;
	}

	public String getLinkedinUrl() {
		return linkedinUrl;
	}

	public void setLinkedinUrl(String linkedinUrl) {
		this.linkedinUrl = linkedinUrl;
	}

	public List<Vote> getVotes() {
		return votes;
	}

	public void setVotes(List<Vote> votes) {
		this.votes = votes;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public boolean isWinner() {
		return winner;
	}

	public void setWinner(boolean winner) {
		this.winner = winner;
	}

	public ReminderFrequency getReminderFrequency() {
		if (reminderFrequency == null) {
			reminderFrequency = ReminderFrequency.defaultValue();
		}
		return reminderFrequency;
	}

	public void setReminderFrequency(ReminderFrequency reminderFrequency) {
		this.reminderFrequency = reminderFrequency == null ? ReminderFrequency.defaultValue() : reminderFrequency;
	}

	public CandidateStatus getStatus() {
		return status;
	}

	public void setStatus(CandidateStatus status) {
		this.status = status;
	}

	public CandidateType getCandidateType() {
		return candidateType == null ? CandidateType.NORMAL : candidateType;
	}

	public void setCandidateType(CandidateType candidateType) {
		this.candidateType = candidateType == null ? CandidateType.NORMAL : candidateType;
	}

	public boolean isAbstention() {
		return CandidateType.ABSTENTION.equals(getCandidateType());
	}

	public Boolean isQUnemployed() {
		return qUnemployed;
	}

	public void setQUnemployed(Boolean qUnemployed) {
		this.qUnemployed = qUnemployed;
	}

	public Boolean getQCanSpeakSpanish() {
		return qCanSpeakSpanish;
	}

	public boolean isQCanSpeakSpanish() {
		return Boolean.TRUE.equals(qCanSpeakSpanish);
	}

	public void setQCanSpeakSpanish(Boolean qCanSpeakSpanish) {
		this.qCanSpeakSpanish = qCanSpeakSpanish;
	}

	public Boolean getQLegalLimitationAnyCountry() {
		return qLegalLimitationAnyCountry;
	}

	public boolean isQLegalLimitationAnyCountry() {
		return Boolean.TRUE.equals(qLegalLimitationAnyCountry);
	}

	public void setQLegalLimitationAnyCountry(Boolean qLegalLimitationAnyCountry) {
		this.qLegalLimitationAnyCountry = qLegalLimitationAnyCountry;
	}

	public Boolean getQHealthTravelLimitation() {
		return qHealthTravelLimitation;
	}

	public boolean isQHealthTravelLimitation() {
		return Boolean.TRUE.equals(qHealthTravelLimitation);
	}

	public void setQHealthTravelLimitation(Boolean qHealthTravelLimitation) {
		this.qHealthTravelLimitation = qHealthTravelLimitation;
	}

	public Boolean getQHealthMentalLimitation() {
		return qHealthMentalLimitation;
	}

	public boolean isQHealthMentalLimitation() {
		return Boolean.TRUE.equals(qHealthMentalLimitation);
	}

	public void setQHealthMentalLimitation(Boolean qHealthMentalLimitation) {
		this.qHealthMentalLimitation = qHealthMentalLimitation;
	}

	public Boolean getQAdultInCountry() {
		return qAdultInCountry;
	}

	public boolean isQAdultInCountry() {
		return Boolean.TRUE.equals(qAdultInCountry);
	}

	public void setQAdultInCountry(Boolean qAdultInCountry) {
		this.qAdultInCountry = qAdultInCountry;
	}

	public Boolean getQCivilRightsLimitation() {
		return qCivilRightsLimitation;
	}

	public boolean isQCivilRightsLimitation() {
		return Boolean.TRUE.equals(qCivilRightsLimitation);
	}

	public void setQCivilRightsLimitation(Boolean qCivilRightsLimitation) {
		this.qCivilRightsLimitation = qCivilRightsLimitation;
	}

	public String getQOtherStatutoryAnswer1Spanish() {
		return qOtherStatutoryAnswer1Spanish;
	}

	public void setQOtherStatutoryAnswer1Spanish(String qOtherStatutoryAnswer1Spanish) {
		this.qOtherStatutoryAnswer1Spanish = qOtherStatutoryAnswer1Spanish;
	}

	public String getQOtherStatutoryAnswer1() {
		return getQOtherStatutoryAnswer1Spanish();
	}

	public void setQOtherStatutoryAnswer1(String qOtherStatutoryAnswer1Spanish) {
		setQOtherStatutoryAnswer1Spanish(qOtherStatutoryAnswer1Spanish);
	}

	public String getQOtherStatutoryAnswer1English() {
		return qOtherStatutoryAnswer1English;
	}

	public void setQOtherStatutoryAnswer1English(String qOtherStatutoryAnswer1English) {
		this.qOtherStatutoryAnswer1English = qOtherStatutoryAnswer1English;
	}

	public String getQOtherStatutoryAnswer1Portuguese() {
		return qOtherStatutoryAnswer1Portuguese;
	}

	public void setQOtherStatutoryAnswer1Portuguese(String qOtherStatutoryAnswer1Portuguese) {
		this.qOtherStatutoryAnswer1Portuguese = qOtherStatutoryAnswer1Portuguese;
	}

	public String getQOtherStatutoryAnswer2Spanish() {
		return qOtherStatutoryAnswer2Spanish;
	}

	public void setQOtherStatutoryAnswer2Spanish(String qOtherStatutoryAnswer2Spanish) {
		this.qOtherStatutoryAnswer2Spanish = qOtherStatutoryAnswer2Spanish;
	}

	public String getQOtherStatutoryAnswer2() {
		return getQOtherStatutoryAnswer2Spanish();
	}

	public void setQOtherStatutoryAnswer2(String qOtherStatutoryAnswer2Spanish) {
		setQOtherStatutoryAnswer2Spanish(qOtherStatutoryAnswer2Spanish);
	}

	public String getQOtherStatutoryAnswer2English() {
		return qOtherStatutoryAnswer2English;
	}

	public void setQOtherStatutoryAnswer2English(String qOtherStatutoryAnswer2English) {
		this.qOtherStatutoryAnswer2English = qOtherStatutoryAnswer2English;
	}

	public String getQOtherStatutoryAnswer2Portuguese() {
		return qOtherStatutoryAnswer2Portuguese;
	}

	public void setQOtherStatutoryAnswer2Portuguese(String qOtherStatutoryAnswer2Portuguese) {
		this.qOtherStatutoryAnswer2Portuguese = qOtherStatutoryAnswer2Portuguese;
	}

	public String getQOtherStatutoryAnswer3Spanish() {
		return qOtherStatutoryAnswer3Spanish;
	}

	public void setQOtherStatutoryAnswer3Spanish(String qOtherStatutoryAnswer3Spanish) {
		this.qOtherStatutoryAnswer3Spanish = qOtherStatutoryAnswer3Spanish;
	}

	public String getQOtherStatutoryAnswer3() {
		return getQOtherStatutoryAnswer3Spanish();
	}

	public void setQOtherStatutoryAnswer3(String qOtherStatutoryAnswer3Spanish) {
		setQOtherStatutoryAnswer3Spanish(qOtherStatutoryAnswer3Spanish);
	}

	public String getQOtherStatutoryAnswer3English() {
		return qOtherStatutoryAnswer3English;
	}

	public void setQOtherStatutoryAnswer3English(String qOtherStatutoryAnswer3English) {
		this.qOtherStatutoryAnswer3English = qOtherStatutoryAnswer3English;
	}

	public String getQOtherStatutoryAnswer3Portuguese() {
		return qOtherStatutoryAnswer3Portuguese;
	}

	public void setQOtherStatutoryAnswer3Portuguese(String qOtherStatutoryAnswer3Portuguese) {
		this.qOtherStatutoryAnswer3Portuguese = qOtherStatutoryAnswer3Portuguese;
	}

	public String getQOtherStatutoryAnswer4Spanish() {
		return qOtherStatutoryAnswer4Spanish;
	}

	public void setQOtherStatutoryAnswer4Spanish(String qOtherStatutoryAnswer4Spanish) {
		this.qOtherStatutoryAnswer4Spanish = qOtherStatutoryAnswer4Spanish;
	}

	public String getQOtherStatutoryAnswer4() {
		return getQOtherStatutoryAnswer4Spanish();
	}

	public void setQOtherStatutoryAnswer4(String qOtherStatutoryAnswer4Spanish) {
		setQOtherStatutoryAnswer4Spanish(qOtherStatutoryAnswer4Spanish);
	}

	public String getQOtherStatutoryAnswer4English() {
		return qOtherStatutoryAnswer4English;
	}

	public void setQOtherStatutoryAnswer4English(String qOtherStatutoryAnswer4English) {
		this.qOtherStatutoryAnswer4English = qOtherStatutoryAnswer4English;
	}

	public String getQOtherStatutoryAnswer4Portuguese() {
		return qOtherStatutoryAnswer4Portuguese;
	}

	public void setQOtherStatutoryAnswer4Portuguese(String qOtherStatutoryAnswer4Portuguese) {
		this.qOtherStatutoryAnswer4Portuguese = qOtherStatutoryAnswer4Portuguese;
	}

	public String getQOtherNonStatutoryAnswer1Spanish() {
		return qOtherNonStatutoryAnswer1Spanish;
	}

	public void setQOtherNonStatutoryAnswer1Spanish(String qOtherNonStatutoryAnswer1Spanish) {
		this.qOtherNonStatutoryAnswer1Spanish = qOtherNonStatutoryAnswer1Spanish;
	}

	public String getQOtherNonStatutoryAnswer1() {
		return getQOtherNonStatutoryAnswer1Spanish();
	}

	public void setQOtherNonStatutoryAnswer1(String qOtherNonStatutoryAnswer1Spanish) {
		setQOtherNonStatutoryAnswer1Spanish(qOtherNonStatutoryAnswer1Spanish);
	}

	public String getQOtherNonStatutoryAnswer1English() {
		return qOtherNonStatutoryAnswer1English;
	}

	public void setQOtherNonStatutoryAnswer1English(String qOtherNonStatutoryAnswer1English) {
		this.qOtherNonStatutoryAnswer1English = qOtherNonStatutoryAnswer1English;
	}

	public String getQOtherNonStatutoryAnswer1Portuguese() {
		return qOtherNonStatutoryAnswer1Portuguese;
	}

	public void setQOtherNonStatutoryAnswer1Portuguese(String qOtherNonStatutoryAnswer1Portuguese) {
		this.qOtherNonStatutoryAnswer1Portuguese = qOtherNonStatutoryAnswer1Portuguese;
	}

	public Boolean getQDeclarationIncompatibilities() {
		return qDeclarationIncompatibilities;
	}

	public boolean isQDeclarationIncompatibilities() {
		return Boolean.TRUE.equals(qDeclarationIncompatibilities);
	}

	public void setQDeclarationIncompatibilities(Boolean qDeclarationIncompatibilities) {
		this.qDeclarationIncompatibilities = qDeclarationIncompatibilities;
	}

	public Boolean getQDeclarationConflictsOfInterest() {
		return qDeclarationConflictsOfInterest;
	}

	public boolean isQDeclarationConflictsOfInterest() {
		return Boolean.TRUE.equals(qDeclarationConflictsOfInterest);
	}

	public void setQDeclarationConflictsOfInterest(Boolean qDeclarationConflictsOfInterest) {
		this.qDeclarationConflictsOfInterest = qDeclarationConflictsOfInterest;
	}

	public Boolean getQDeclarationCompetenciesAndSuitability() {
		return qDeclarationCompetenciesAndSuitability;
	}

	public boolean isQDeclarationCompetenciesAndSuitability() {
		return Boolean.TRUE.equals(qDeclarationCompetenciesAndSuitability);
	}

	public void setQDeclarationCompetenciesAndSuitability(Boolean qDeclarationCompetenciesAndSuitability) {
		this.qDeclarationCompetenciesAndSuitability = qDeclarationCompetenciesAndSuitability;
	}

	public Boolean getQDeclarationDisciplinaryRegulation() {
		return qDeclarationDisciplinaryRegulation;
	}

	public boolean isQDeclarationDisciplinaryRegulation() {
		return Boolean.TRUE.equals(qDeclarationDisciplinaryRegulation);
	}

	public void setQDeclarationDisciplinaryRegulation(Boolean qDeclarationDisciplinaryRegulation) {
		this.qDeclarationDisciplinaryRegulation = qDeclarationDisciplinaryRegulation;
	}

	public Boolean getQDeclarationIanaKnowledge() {
		return qDeclarationIanaKnowledge;
	}

	public boolean isQDeclarationIanaKnowledge() {
		return Boolean.TRUE.equals(qDeclarationIanaKnowledge);
	}

	public void setQDeclarationIanaKnowledge(Boolean qDeclarationIanaKnowledge) {
		this.qDeclarationIanaKnowledge = qDeclarationIanaKnowledge;
	}

	public Boolean getQDeclarationAsoKnowledge() {
		return qDeclarationAsoKnowledge;
	}

	public boolean isQDeclarationAsoKnowledge() {
		return Boolean.TRUE.equals(qDeclarationAsoKnowledge);
	}

	public void setQDeclarationAsoKnowledge(Boolean qDeclarationAsoKnowledge) {
		this.qDeclarationAsoKnowledge = qDeclarationAsoKnowledge;
	}

	public CandidatePepDeclaration getQDeclarationPep() {
		return qDeclarationPep;
	}

	public void setQDeclarationPep(CandidatePepDeclaration qDeclarationPep) {
		this.qDeclarationPep = qDeclarationPep;
	}

	public Boolean getQDeclarationDynamicCommitments() {
		return qDeclarationDynamicCommitments;
	}

	public boolean isQDeclarationDynamicCommitments() {
		return Boolean.TRUE.equals(qDeclarationDynamicCommitments);
	}

	public void setQDeclarationDynamicCommitments(Boolean qDeclarationDynamicCommitments) {
		this.qDeclarationDynamicCommitments = qDeclarationDynamicCommitments;
	}

	public Boolean getQDeclarationDataUsageAndPublication() {
		return qDeclarationDataUsageAndPublication;
	}

	public boolean isQDeclarationDataUsageAndPublication() {
		return Boolean.TRUE.equals(qDeclarationDataUsageAndPublication);
	}

	public void setQDeclarationDataUsageAndPublication(Boolean qDeclarationDataUsageAndPublication) {
		this.qDeclarationDataUsageAndPublication = qDeclarationDataUsageAndPublication;
	}

	public List<CandidateElectionTaskProgress> getTaskProgress() {
		return taskProgress;
	}

	public void setTaskProgress(List<CandidateElectionTaskProgress> taskProgress) {
		this.taskProgress = taskProgress;
	}

	public List<CandidateWorkOrganization> getWorkOrganizations() {
		return workOrganizations;
	}

	public void setWorkOrganizations(List<CandidateWorkOrganization> workOrganizations) {
		this.workOrganizations = workOrganizations;
	}

	public List<CandidateCountryLink> getCountryLinks() {
		return countryLinks;
	}

	public void setCountryLinks(List<CandidateCountryLink> countryLinks) {
		this.countryLinks = countryLinks;
	}

	public CandidateCampusCourseStatus getCampusCourseStatus() {
		return campusCourseStatus;
	}

	public void setCampusCourseStatus(CandidateCampusCourseStatus campusCourseStatus) {
		this.campusCourseStatus = campusCourseStatus;
	}

	public CandidateEvaluationStatus getEvaluationStatus() {
		return evaluationStatus;
	}

	public void setEvaluationStatus(CandidateEvaluationStatus evaluationStatus) {
		this.evaluationStatus = evaluationStatus;
	}

	public String getCampusCourseCalification() {
		return campusCourseCalification;
	}

	public void setCampusCourseCalification(String campusCourseCalification) {
		this.campusCourseCalification = campusCourseCalification;
	}

	public Long getCampusCourseSelected() {
		return campusCourseSelected;
	}

	public void setCampusCourseSelected(Long campusCourseSelected) {
		this.campusCourseSelected = campusCourseSelected;
	}

	public byte[] getProctorioResultFile() {
		return proctorioResultFile;
	}

	public void setProctorioResultFile(byte[] proctorioResultFile) {
		this.proctorioResultFile = proctorioResultFile;
	}

	public List<AuditorCandidateDecision> getAuditorDecisions() {
		return auditorDecisions;
	}

	public void setAuditorDecisions(List<AuditorCandidateDecision> auditorDecisions) {
		this.auditorDecisions = auditorDecisions;
	}

	public List<CandidateQuestion> getCandidateQuestions() {
		return candidateQuestions;
	}

	public void setCandidateQuestions(List<CandidateQuestion> candidateQuestions) {
		this.candidateQuestions = candidateQuestions;
	}

}
