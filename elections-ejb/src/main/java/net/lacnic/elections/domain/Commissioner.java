package net.lacnic.elections.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class Commissioner implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "commissioner_seq")
	@SequenceGenerator(name = "commissioner_seq", sequenceName = "commissioner_seq", allocationSize = 1)
	@Column(name = "commissioner_id")
	private long commissionerId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String mail;


	public Commissioner() { }


	public long getCommissionerId() {
		return commissionerId;
	}

	public void setCommissionerId(long commissionerId) {
		this.commissionerId = commissionerId;
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
