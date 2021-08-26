package net.lacnic.elections.domain.services;

import java.io.Serializable;

import net.lacnic.elections.domain.Auditor;


public class AuditorReportTable implements Serializable {

	private static final long serialVersionUID = -6947143778751018563L;

	private Long auditorId;
	private Boolean commissioner;
	private Boolean agreedConformity;
	private String mail;
	private String name;
	private Long electionId;
	private Boolean revisionAvailable;
	private Long migrationId;


	public AuditorReportTable() { }

	public AuditorReportTable(Auditor auditor) {
		this.auditorId = auditor.getAuditorId();
		this.commissioner = auditor.isCommissioner();
		this.agreedConformity = auditor.isAgreedConformity();
		this.mail = auditor.getMail();
		this.name = auditor.getName();
		this.electionId = auditor.getElection().getElectionId();
		this.revisionAvailable = auditor.isRevisionAvailable();
		this.migrationId = auditor.getMigrationId();
	}


	public Long getAuditorId() {
		return auditorId;
	}

	public void setAuditorId(Long auditorId) {
		this.auditorId = auditorId;
	}

	public Boolean getCommissioner() {
		return commissioner;
	}

	public void setCommissioner(Boolean commissioner) {
		this.commissioner = commissioner;
	}

	public Boolean getAgreedConformity() {
		return agreedConformity;
	}

	public void setAgreedConformity(Boolean agreedConformity) {
		this.agreedConformity = agreedConformity;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
	}

	public Boolean getRevisionAvailable() {
		return revisionAvailable;
	}

	public void setRevisionAvailable(Boolean revisionAvailable) {
		this.revisionAvailable = revisionAvailable;
	}

	public Long getMigrationId() {
		return migrationId;
	}

	public void setMigrationId(Long migrationId) {
		this.migrationId = migrationId;
	}

}
