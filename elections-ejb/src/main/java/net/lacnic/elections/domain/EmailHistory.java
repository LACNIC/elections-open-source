package net.lacnic.elections.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class EmailHistory implements Serializable {

	private static final long serialVersionUID = -6954869970189933966L;
	private static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy HH:mm";

	@Id
	@Column(name = "emailhistory_id")
	private Long emailHistoryId;

	@Column
	private String recipients;

	@Column
	private String sender;

	@Column
	private String cc;

	@Column
	private String bcc;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String subject;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String body;

	@Column
	private Boolean sent = false;

	@Column
	private Date createdDate;

	@Column
	private String templateType;

	@Column(name = "election_id")
	private long electionId;


	public EmailHistory() { }

	public EmailHistory(Email email) {
		this.emailHistoryId = email.getEmailId();
		this.recipients = email.getRecipients();
		this.sender = email.getSender();
		this.cc = email.getCc();
		this.bcc = email.getBcc();
		this.subject = email.getSubject();
		this.body = email.getBody();
		this.sent = email.getSent();
		this.createdDate = email.getCreatedDate();
		this.templateType = email.getTemplateType();
		this.electionId = email.getElection().getElectionId();
	}


	public String getCreatedDateString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		return simpleDateFormat.format(getCreatedDate());
	}


	public Long getEmailHistoryId() {
		return emailHistoryId;
	}

	public void setEmailHistoryId(Long emailHistoryId) {
		this.emailHistoryId = emailHistoryId;
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

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Boolean getSent() {
		return sent;
	}

	public void setSent(Boolean sent) {
		this.sent = sent;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public long getElectionId() {
		return electionId;
	}

	public void setElectionId(long electionId) {
		this.electionId = electionId;
	}

}
