package net.lacnic.elections.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "auditor")
public class AuditorReportTable implements Serializable {

	private static final long serialVersionUID = -6947143778751018563L;
	
	@Id
	@Column(name = "auditor_id")
	private long auditorId;
	
	@Column
	private boolean commissioner;

	@Column
	private boolean agreedConformity;
	
	@Column(nullable = false)
	private String mail;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = true, name = "election_id")
	private Long electionId;
	
	@Column
	private boolean revisionAvailable;
	
	@Column(nullable = true, name = "migration_id")
	private Long migrationId;
	
	public AuditorReportTable() { }

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
