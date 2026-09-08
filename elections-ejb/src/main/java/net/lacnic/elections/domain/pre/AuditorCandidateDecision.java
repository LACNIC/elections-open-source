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
import net.lacnic.elections.domain.Auditor;
import net.lacnic.elections.domain.Candidate;

@Entity
public class AuditorCandidateDecision implements Serializable {

	private static final long serialVersionUID = -7722025933746155201L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auditor_candidate_decision_seq")
	@SequenceGenerator(name = "auditor_candidate_decision_seq", sequenceName = "auditor_candidate_decision_seq", allocationSize = 1, initialValue = 90000)
	@Column(name = "id")
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "auditor_id", nullable = false)
	private Auditor auditor;

	@ManyToOne(optional = false)
	@JoinColumn(name = "candidate_id", nullable = false)
	private Candidate candidate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AuditorCandidateDecisionStatus decisionStatus;

	@Column(nullable = false)
	private Date decisionDate;

	@Column(name = "preapproved_date")
	private Date preapprovedDate;

	@Column(name = "approved_date")
	private Date approvedDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "pre_decision_status")
	private AuditorCandidateDecisionStatus preDecisionStatus;

	@Column(name = "pre_decision_date")
	private Date preDecisionDate;

	@Column(name = "pre_decision_comment", length = 4000)
	private String preDecisionComment;

	@Enumerated(EnumType.STRING)
	@Column(name = "final_decision_status")
	private AuditorCandidateDecisionStatus finalDecisionStatus;

	@Column(name = "final_decision_date")
	private Date finalDecisionDate;

	@Column(name = "final_decision_comment", length = 4000)
	private String finalDecisionComment;

	public AuditorCandidateDecision() {
		// Default constructor for JPA
	}

	public long getId() {
		return id;
	}

	public Auditor getAuditor() {
		return auditor;
	}

	public void setAuditor(Auditor auditor) {
		this.auditor = auditor;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public AuditorCandidateDecisionStatus getDecisionStatus() {
		return decisionStatus;
	}

	public void setDecisionStatus(AuditorCandidateDecisionStatus decisionStatus) {
		this.decisionStatus = decisionStatus;
	}

	public Date getDecisionDate() {
		return decisionDate;
	}

	public void setDecisionDate(Date decisionDate) {
		this.decisionDate = decisionDate;
	}

	public Date getPreapprovedDate() {
		return preapprovedDate;
	}

	public void setPreapprovedDate(Date preapprovedDate) {
		this.preapprovedDate = preapprovedDate;
	}

	public Date getApprovedDate() {
		return approvedDate;
	}

	public void setApprovedDate(Date approvedDate) {
		this.approvedDate = approvedDate;
	}

	public AuditorCandidateDecisionStatus getPreDecisionStatus() {
		return preDecisionStatus;
	}

	public void setPreDecisionStatus(AuditorCandidateDecisionStatus preDecisionStatus) {
		this.preDecisionStatus = preDecisionStatus;
	}

	public Date getPreDecisionDate() {
		return preDecisionDate;
	}

	public void setPreDecisionDate(Date preDecisionDate) {
		this.preDecisionDate = preDecisionDate;
	}

	public String getPreDecisionComment() {
		return preDecisionComment;
	}

	public void setPreDecisionComment(String preDecisionComment) {
		this.preDecisionComment = preDecisionComment;
	}

	public AuditorCandidateDecisionStatus getFinalDecisionStatus() {
		return finalDecisionStatus;
	}

	public void setFinalDecisionStatus(AuditorCandidateDecisionStatus finalDecisionStatus) {
		this.finalDecisionStatus = finalDecisionStatus;
	}

	public Date getFinalDecisionDate() {
		return finalDecisionDate;
	}

	public void setFinalDecisionDate(Date finalDecisionDate) {
		this.finalDecisionDate = finalDecisionDate;
	}

	public String getFinalDecisionComment() {
		return finalDecisionComment;
	}

	public void setFinalDecisionComment(String finalDecisionComment) {
		this.finalDecisionComment = finalDecisionComment;
	}
}
