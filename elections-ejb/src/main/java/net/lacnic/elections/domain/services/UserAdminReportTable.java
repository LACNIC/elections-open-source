package net.lacnic.elections.domain.services;

import java.io.Serializable;

import net.lacnic.elections.domain.UserAdmin;

public class UserAdminReportTable implements Serializable{

	private static final long serialVersionUID = 2884083950630100276L;
	
	private String userAdminId;
	private String email;
	private long authorizedElectionId;
	
	public UserAdminReportTable() { }
	
	public UserAdminReportTable(UserAdmin userAdmin) {
		this.userAdminId = userAdmin.getUserAdminId();
		this.email = userAdmin.getEmail();
		this.authorizedElectionId = userAdmin.getAuthorizedElectionId();
	}

	public String getUserAdminId() {
		return userAdminId;
	}

	public void setUserAdminId(String userAdminId) {
		this.userAdminId = userAdminId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getAuthorizedElectionId() {
		return authorizedElectionId;
	}

	public void setAuthorizedElectionId(long authorizedElectionId) {
		this.authorizedElectionId = authorizedElectionId;
	}
	
	

}
