package net.lacnic.elections.domain.services.dbtables;

import java.io.Serializable;

import net.lacnic.elections.domain.UserAdmin;


public class UserAdminTableReport implements Serializable {

	private static final long serialVersionUID = 2884083950630100276L;

	private String userAdminId;
	private String email;
	private String password;
	private Long authorizedElectionId;


	public UserAdminTableReport() { }

	public UserAdminTableReport(UserAdmin userAdmin) {
		this.userAdminId = userAdmin.getUserAdminId();
		this.email = userAdmin.getEmail();
		this.authorizedElectionId = userAdmin.getAuthorizedElectionId();
		this.password = "**********";
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

	public Long getAuthorizedElectionId() {
		return authorizedElectionId;
	}

	public void setAuthorizedElectionId(Long authorizedElectionId) {
		this.authorizedElectionId = authorizedElectionId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
