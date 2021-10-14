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

import net.lacnic.elections.utils.LinksUtils;
import net.lacnic.elections.utils.StringUtils;

@Entity
public class Auditor implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auditor_seq")
	@SequenceGenerator(name = "auditor_seq", sequenceName = "auditor_seq", allocationSize = 1)
	@Column(name = "auditor_id")
	private long auditorId;

	@Column(nullable = true, name = "migration_id")
	private Long migrationId;

	@Column
	private boolean commissioner;

	@Column
	private boolean agreedConformity;

	@Column
	private boolean revisionAvailable;

	@Column(nullable = true, length = 1000)
	private String resultToken;

	@Column(nullable = false)
	private String name;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id")
	private Election election;

	@Column(nullable = false)
	private String mail;

	public Auditor() {
		this.commissioner = false;
		this.agreedConformity = false;
		this.revisionAvailable = false;
		this.resultToken = StringUtils.createSecureToken();
		this.migrationId = 0L;
	}

	public Auditor(Election election, Commissioner commissioner) {
		this.commissioner = true;
		this.agreedConformity = false;
		this.resultToken = StringUtils.createSecureToken();
		this.revisionAvailable = false;
		this.name = commissioner.getName();
		this.election = election;
		this.mail = commissioner.getMail();
		this.migrationId = 0L;
	}

	public void clean() {
		this.name = null;
		this.mail = null;
		this.commissioner = false;
	}

	public String getResultLink() {
		return LinksUtils.buildAuditorResultsLink(resultToken);
	}


	public long getAuditorId() {
		return auditorId;
	}

	public void setAuditorId(long auditorId) {
		this.auditorId = auditorId;
	}

	public Long getMigrationId() {
		return migrationId;
	}

	public void setMigrationId(Long migrationId) {
		this.migrationId = migrationId;
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

	public boolean isRevisionAvailable() {
		return revisionAvailable;
	}

	public void setRevisionAvailable(boolean revisionAvailable) {
		this.revisionAvailable = revisionAvailable;
	}

	public String getResultToken() {
		return resultToken;
	}

	public void setResultToken(String resultToken) {
		this.resultToken = resultToken;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

}
