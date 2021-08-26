package net.lacnic.elections.domain.services;

import java.io.Serializable;

import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.EmailHistory;
import net.lacnic.elections.utils.DateTimeUtils;


public class EmailReportTable implements Serializable {

	private static final long serialVersionUID = -1874006208868867429L;

	private Long emailId;
	private String recipients;
	private String sender;
	private String cc;
	private String bcc;
	private String subject;
	private Boolean sent;
	private String createdDate;
	private String templateType;
	private Long electionId;


	public EmailReportTable() { }

	public EmailReportTable(Email email) {
		this.emailId = email.getEmailId();
		this.recipients = email.getRecipients();
		this.sender = email.getSender();
		this.cc = email.getCc();
		this.bcc = email.getBcc();
		this.subject = email.getSubject();
		this.sent = email.getSent();
		this.createdDate = DateTimeUtils.getTableServicesDateTimeString(email.getCreatedDate());
		this.templateType = email.getTemplateType();
		this.electionId = email.getElection().getElectionId();
	}

	public EmailReportTable(EmailHistory email) {
		this.emailId = email.getEmailHistoryId();
		this.recipients = email.getRecipients();
		this.sender = email.getSender();
		this.cc = email.getCc();
		this.bcc = email.getBcc();
		this.subject = email.getSubject();
		this.sent = email.getSent();
		this.createdDate = DateTimeUtils.getTableServicesDateTimeString(email.getCreatedDate());
		this.templateType = email.getTemplateType();
		this.electionId = email.getElectionId();
	}


	public Long getEmailId() {
		return emailId;
	}

	public void setEmailId(Long emailId) {
		this.emailId = emailId;
	}

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Boolean getSent() {
		return sent;
	}

	public void setSent(Boolean sent) {
		this.sent = sent;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

}
