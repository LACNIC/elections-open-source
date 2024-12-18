package net.lacnic.elections.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserAdmin implements Serializable {

	private static final long serialVersionUID = 7237293453682145209L;

	@Id
	@Column(name = "useradmin_id")
	private String userAdminId;

	@Column(name = "password", columnDefinition = "text")
	private String password;

	@Column(name = "email", columnDefinition = "text")
	private String email;

	@Column(name = "authorizedelection_id")
	private Long authorizedElectionId;

	public UserAdmin() {
		// Default initialization
	}

	public String getUserAdminId() {
		return userAdminId;
	}

	public void setUserAdminId(String userAdminId) {
		this.userAdminId = userAdminId.toLowerCase();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password.toUpperCase();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email.toLowerCase();
	}

	public Long getAuthorizedElectionId() {
		return authorizedElectionId;
	}

	public void setAuthorizedElectionId(Long authorizedElectionId) {
		this.authorizedElectionId = authorizedElectionId;
	}

}
