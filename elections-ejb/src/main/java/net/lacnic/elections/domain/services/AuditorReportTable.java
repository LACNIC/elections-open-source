package net.lacnic.elections.domain.services;

import java.io.Serializable;

import net.lacnic.elections.domain.Auditor;


public class AuditorReportTable implements Serializable {

	private static final long serialVersionUID = -6947143778751018563L;

	private long auditorId;
	private boolean commissioner;
	private boolean agreedConformity;
	private String mail;
	private String name;
	private Long electionId;
	private boolean revisionAvailable;
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


	public long getAuditorId() {
		return auditorId;
	}

	public void setAuditorId(long auditorId) {
		this.auditorId = auditorId;
	}

	public boolean isCommissioner() {
		return commissioner;
	}

	public void setCommissioner(boolean commissioner) {
		this.commissioner = commissioner;
	}

	public boolean isAgreedConformity() {
		return agreedConformity;
	}

	public void setAgreedConformity(boolean agreedConformity) {
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

	public Long getElection() {
		return electionId;
	}

	public void setElection(Long electionId) {
		this.electionId = electionId;
	}

	public boolean isRevisionAvailable() {
		return revisionAvailable;
	}

	public void setRevisionAvailable(boolean revisionAvailable) {
		this.revisionAvailable = revisionAvailable;
	}

	public Long getMigrationId() {
		return migrationId;
	}

	public void setMigrationId(Long migrationId) {
		this.migrationId = migrationId;
	}

}
