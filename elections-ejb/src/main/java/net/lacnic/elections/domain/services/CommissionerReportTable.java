package net.lacnic.elections.domain.services;

import java.io.Serializable;

import net.lacnic.elections.domain.Commissioner;


public class CommissionerReportTable implements Serializable {

	private static final long serialVersionUID = -4660664185660216489L;

	private long commissionerId;
	private String name;
	private String mail;


	public CommissionerReportTable() { }

	public CommissionerReportTable(Commissioner commissioner) {
		this.commissionerId = commissioner.getCommissionerId();
		this.name = commissioner.getName();
		this.mail = commissioner.getMail();
	}


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
