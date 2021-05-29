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

import net.lacnic.elections.utils.StringUtils;
import net.lacnic.elections.utils.UtilsLinks;

@Entity
public class Auditor implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auditor_seq")
	@SequenceGenerator(name = "auditor_seq", sequenceName = "auditor_seq", allocationSize = 1)
	@Column(name = "id_auditor")
	private long idAuditor;

	@Column(nullable = true)
	private Long idMigration;

	@Column
	private boolean commissioner;

	@Column
	private boolean agreement;

	@Column
	private boolean revisionAvailable;

	@Column(nullable = true, length = 1000)
	private String resulttoke;

	@Column(nullable = false)
	private String name;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_election")
	private Election election;

	@Column(nullable = false)
	private String mail;

	public Auditor() {
		this.commissioner = false;
		this.agreement = false;
		this.revisionAvailable = false;
		this.resulttoke = StringUtils.createSecureToken();
		this.idMigration = 0L;

	}

	public Auditor(Election election, Comissioner commissioner) {
		this.commissioner = true;
		this.agreement = false;
		this.resulttoke = StringUtils.createSecureToken();
		this.revisionAvailable = false;
		this.name = commissioner.getName();
		this.election = election;
		this.mail = commissioner.getMail();
		this.idMigration = 0L;
	}

	public long getIdAuditor() {
		return idAuditor;
	}

	public void setIdAuditor(long idAuditor) {
		this.idAuditor = idAuditor;
	}

	public String getResulttoke() {
		return resulttoke;
	}

	public void setResulttoke(String resultToken) {
		this.resulttoke = resultToken;
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

	public boolean isCommissioner() {
		return commissioner;
	}

	public void setCommissioner(boolean commissioner) {
		this.commissioner = commissioner;
	}

	public boolean isAgreement() {
		return agreement;
	}

	public void setAgreement(boolean agreement) {
		this.agreement = agreement;
	}

	public void clean() {
		this.name = null;
		this.mail = null;
		this.commissioner = false;

	}

	public String getResultLink() {
		return UtilsLinks.calcularLinkResultadoAuditor(resulttoke);
	}

	public boolean isRevisionAvailable() {
		return revisionAvailable;
	}

	public void setRevisionAvailable(boolean revisionAvailable) {
		this.revisionAvailable = revisionAvailable;
	}

	public long getIdMigration() {
		return idMigration;
	}

	public void setIdMigracion(long idMigration) {
		this.idMigration = idMigration;
	}
}
