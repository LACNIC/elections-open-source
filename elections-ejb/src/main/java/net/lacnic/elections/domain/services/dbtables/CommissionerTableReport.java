package net.lacnic.elections.domain.services.dbtables;

import java.io.Serializable;

import net.lacnic.elections.domain.Commissioner;


public class CommissionerTableReport implements Serializable {

	private static final long serialVersionUID = -4660664185660216489L;

	private Long commissionerId;
	private String name;
	private String mail;


	public CommissionerTableReport() { }

	public CommissionerTableReport(Commissioner commissioner) {
		this.commissionerId = commissioner.getCommissionerId();
		this.name = commissioner.getName();
		this.mail = commissioner.getMail();
	}


	public Long getCommissionerId() {
		return commissionerId;
	}

	public void setCommissionerId(Long commissionerId) {
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
