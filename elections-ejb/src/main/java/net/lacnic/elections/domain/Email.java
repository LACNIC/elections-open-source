package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
public class Email implements Serializable {

	private static final long serialVersionUID = -6954869970189933966L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emaile_seq")
	@SequenceGenerator(name = "emaile_seq", sequenceName = "emaile_seq", allocationSize = 1)
	private Long id;

	@Column(columnDefinition = "TEXT")
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

	@Column(nullable = false)
	private String templateType;

	@ManyToOne(optional = true)
	@JoinColumn(name = "id_election")
	private Election election;

	public Email() {
		this.createdDate = new Date();
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
		this.templateType = templateType;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

}
