package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class EmailHistory implements Serializable {

	private static final long serialVersionUID = -6954869970189933966L;

	@Id
	private Long id;

	@Column
	private String recipients;

	@Column(name = "email_from")
	private String emailFrom;

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

	@Column(name = "id_election")
	private long idElection;

	public EmailHistory() {
	}

	public EmailHistory(Email e) {
		this.id = e.getId();
		this.recipients = e.getRecipients();
		this.emailFrom = e.getEmailFrom();
		this.cc = e.getCc();
		this.bcc = e.getBcc();
		this.subject = e.getSubject();
		this.body = e.getBody();
		this.sent = e.getSent();
		this.createdDate = e.getCreatedDate();
		this.templateType = e.getTemplateType();
		this.idElection = e.getElection().getIdElection();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
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

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public long getIdElection() {
		return idElection;
	}

	public void setIdElection(long idElection) {
		this.idElection = idElection;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

}
