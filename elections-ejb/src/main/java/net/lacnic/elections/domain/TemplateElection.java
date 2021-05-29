package net.lacnic.elections.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

@Entity
public class TemplateElection implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "templateelection_seq")
	@SequenceGenerator(name = "templateelection_seq", sequenceName = "templateelection_seq", allocationSize = 1, initialValue = 500)
	@Column(name = "id_template_election")
	private long idTemplate;

	@ManyToOne(optional = true)
	@JoinColumn(name = "id_election", nullable = true)
	private Election election;

	@Column(name = "type")
	private String templateType;

	@Column(columnDefinition = "TEXT")
	private String subjectSP;

	@Column(columnDefinition = "TEXT")
	private String subjectPT;

	@Column(columnDefinition = "TEXT")
	private String subjectEN;

	@Column(columnDefinition = "TEXT")
	private String bodySP;

	@Column(columnDefinition = "TEXT")
	private String bodyEN;

	@Column(columnDefinition = "TEXT")
	private String bodyPT;

	@Transient
	private RecipientType recipientType;

	public TemplateElection() {

	}

	public TemplateElection(Election e, TemplateElection t) {
		this.subjectEN = t.getSubjectEN();
		this.subjectSP = t.getSubjectSP();
		this.subjectPT = t.getSubjectPT();
		this.bodyEN = t.getBodyEN();
		this.bodySP = t.getBodySP();
		this.bodyPT = t.getBodyPT();
		this.templateType = t.getTemplateType();
		this.election = e;
	}

	public long getIdTemplate() {
		return idTemplate;
	}

	public void setIdTemplate(long idTemplate) {
		this.idTemplate = idTemplate;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType.toUpperCase();
	}

	public String getSubjectSP() {
		return subjectSP;
	}

	public void setSubjectSP(String subjectSP) {
		this.subjectSP = subjectSP;
	}

	public String getSubjectPT() {
		return subjectPT;
	}

	public void setSubjectPT(String subjectPT) {
		this.subjectPT = subjectPT;
	}

	public String getSubjectEN() {
		return subjectEN;
	}

	public void setSubjectEN(String subjectEN) {
		this.subjectEN = subjectEN;
	}

	public String getBodySP() {
		return bodySP;
	}

	public void setBodySP(String bodySP) {
		this.bodySP = bodySP;
	}

	public String getBodyEN() {
		return bodyEN;
	}

	public void setBodyEN(String bodyEN) {
		this.bodyEN = bodyEN;
	}

	public String getBodyPT() {
		return bodyPT;
	}

	public void setBodyPT(String bodyPT) {
		this.bodyPT = bodyPT;
	}

	public RecipientType getRecipientType() {
		return recipientType;
	}

	public void setRecipientType(RecipientType recipientType) {
		this.recipientType = recipientType;
	}

}