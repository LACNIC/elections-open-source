package net.lacnic.elections.domain.services.dbtables;

import java.io.Serializable;

import net.lacnic.elections.domain.ElectionEmailTemplate;

public class ElectionEmailTemplateTableReport implements Serializable {
	
	private static final long serialVersionUID = -8753611728318452902L;
	
	private long electionEmailTemplateId;
	private String templateType;
	private String subjectSP;
	private String subjectPT;
	private String subjectEN;
	private String bodySP;
	private String bodyEN;
	private String bodyPT;
	private String recipientType;
	private Long electionId;
	
	public ElectionEmailTemplateTableReport() { }
	
	public ElectionEmailTemplateTableReport(ElectionEmailTemplate electionEmailTemplate) {
		this.electionEmailTemplateId = electionEmailTemplate.getElectionEmailTemplateId();		
		this.templateType = electionEmailTemplate.getTemplateType();
		this.subjectSP = electionEmailTemplate.getSubjectSP();
		this.subjectPT = electionEmailTemplate.getSubjectPT();
		this.subjectEN = electionEmailTemplate.getSubjectEN();
		this.bodySP = electionEmailTemplate.getBodySP();
		this.bodyPT = electionEmailTemplate.getBodyPT();
		this.bodyEN = electionEmailTemplate.getBodyEN();
		if (electionEmailTemplate.getRecipientType() != null) {
			this.recipientType = electionEmailTemplate.getRecipientType().getDescription();
		} else {
			this.recipientType = "";
		};
		if (electionEmailTemplate.getElection() != null) {
			this.electionId = electionEmailTemplate.getElection().getElectionId();
		} else {
			this.electionId = 0L;
		};		
	}
	
	public long getElectionEmailTemplateId() {
		return electionEmailTemplateId;
	}
	public void setElectionEmailTemplateId(long electionEmailTemplateId) {
		this.electionEmailTemplateId = electionEmailTemplateId;
	}
	public String getTemplateType() {
		return templateType;
	}
	public void setTemplateType(String templateType) {
		this.templateType = templateType;
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
	public String getRecipientType() {
		return recipientType;
	}
	public void setRecipientType(String recipientType) {
		this.recipientType = recipientType;
	}
	public Long getElectionId() {
		return electionId;
	}
	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}
	
	

}
