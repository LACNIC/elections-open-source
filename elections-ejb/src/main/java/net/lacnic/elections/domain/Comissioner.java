package net.lacnic.elections.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class Comissioner implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidate_seq")
	@SequenceGenerator(name = "candidate_seq", sequenceName = "candidate_seq", allocationSize = 1)
	@Column(name = "id_commissioner")
	private long idCommissioner;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String mail;

	public long getIdCommissioner() {
		return idCommissioner;
	}

	public void setIdCommissioner(long idCommissioner) {
		this.idCommissioner = idCommissioner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}
}