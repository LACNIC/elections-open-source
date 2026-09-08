package net.lacnic.elections.domain.pre;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.LanguageCode;

@Entity
@Table(name = "candidate_question")
public class CandidateQuestion implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidate_question_seq")
	@SequenceGenerator(name = "candidate_question_seq", sequenceName = "candidate_question_seq", allocationSize = 1)
	@Column(name = "candidate_question_id")
	private long candidateQuestionId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id")
	private Election election;

	@ManyToOne(optional = false)
	@JoinColumn(name = "candidate_id")
	private Candidate candidate;

	@Column(name = "asked_by_name", nullable = true, length = 500)
	private String askedByName;

	@Column(name = "asked_by_email", nullable = true, length = 320)
	private String askedByEmail;

	@Enumerated(EnumType.STRING)
	@Column(name = "question_language", nullable = false, length = 16)
	private LanguageCode questionLanguage;

	@Column(name = "question_spanish", nullable = true, columnDefinition = "TEXT")
	private String questionSpanish;

	@Column(name = "question_english", nullable = true, columnDefinition = "TEXT")
	private String questionEnglish;

	@Column(name = "question_portuguese", nullable = true, columnDefinition = "TEXT")
	private String questionPortuguese;

	@Column(name = "answer_spanish", nullable = true, columnDefinition = "TEXT")
	private String answerSpanish;

	@Column(name = "answer_english", nullable = true, columnDefinition = "TEXT")
	private String answerEnglish;

	@Column(name = "answer_portuguese", nullable = true, columnDefinition = "TEXT")
	private String answerPortuguese;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 64)
	private CandidateQuestionStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "current_owner", nullable = false, length = 16)
	private CandidateQuestionOwner currentOwner;

	@Column(name = "creation_date", nullable = false)
	private Date creationDate;

	@Column(name = "update_date", nullable = false)
	private Date updateDate;

	@Column(name = "published_date", nullable = true)
	private Date publishedDate;

	public CandidateQuestion() {
		this.questionLanguage = LanguageCode.SP;
		this.status = CandidateQuestionStatus.QUESTION_RECEIVED_LACNIC;
		this.currentOwner = this.status.resolveOwner();
		this.creationDate = new Date();
		this.updateDate = new Date();
	}

	public void applyQuestionText(LanguageCode languageCode, String text) {
		LanguageCode resolvedLanguage = languageCode != null ? languageCode : LanguageCode.SP;
		if (resolvedLanguage == LanguageCode.EN) {
			setQuestionEnglish(text);
		} else if (resolvedLanguage == LanguageCode.PT) {
			setQuestionPortuguese(text);
		} else {
			setQuestionSpanish(text);
		}
		setQuestionLanguage(resolvedLanguage);
	}

	public String getQuestionPreview() {
		if (questionSpanish != null && !questionSpanish.trim().isEmpty()) {
			return questionSpanish;
		}
		if (questionEnglish != null && !questionEnglish.trim().isEmpty()) {
			return questionEnglish;
		}
		if (questionPortuguese != null && !questionPortuguese.trim().isEmpty()) {
			return questionPortuguese;
		}
		return "";
	}

	public long getCandidateQuestionId() {
		return candidateQuestionId;
	}

	public void setCandidateQuestionId(long candidateQuestionId) {
		this.candidateQuestionId = candidateQuestionId;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public String getAskedByName() {
		return askedByName;
	}

	public void setAskedByName(String askedByName) {
		this.askedByName = askedByName;
	}

	public String getAskedByEmail() {
		return askedByEmail;
	}

	public void setAskedByEmail(String askedByEmail) {
		this.askedByEmail = askedByEmail;
	}

	public LanguageCode getQuestionLanguage() {
		return questionLanguage;
	}

	public void setQuestionLanguage(LanguageCode questionLanguage) {
		this.questionLanguage = questionLanguage;
	}

	public String getQuestionSpanish() {
		return questionSpanish;
	}

	public void setQuestionSpanish(String questionSpanish) {
		this.questionSpanish = questionSpanish;
	}

	public String getQuestionEnglish() {
		return questionEnglish;
	}

	public void setQuestionEnglish(String questionEnglish) {
		this.questionEnglish = questionEnglish;
	}

	public String getQuestionPortuguese() {
		return questionPortuguese;
	}

	public void setQuestionPortuguese(String questionPortuguese) {
		this.questionPortuguese = questionPortuguese;
	}

	public String getAnswerSpanish() {
		return answerSpanish;
	}

	public void setAnswerSpanish(String answerSpanish) {
		this.answerSpanish = answerSpanish;
	}

	public String getAnswerEnglish() {
		return answerEnglish;
	}

	public void setAnswerEnglish(String answerEnglish) {
		this.answerEnglish = answerEnglish;
	}

	public String getAnswerPortuguese() {
		return answerPortuguese;
	}

	public void setAnswerPortuguese(String answerPortuguese) {
		this.answerPortuguese = answerPortuguese;
	}

	public CandidateQuestionStatus getStatus() {
		return status;
	}

	public void setStatus(CandidateQuestionStatus status) {
		this.status = status;
	}

	public CandidateQuestionOwner getCurrentOwner() {
		return currentOwner;
	}

	public void setCurrentOwner(CandidateQuestionOwner currentOwner) {
		this.currentOwner = currentOwner;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}
}
