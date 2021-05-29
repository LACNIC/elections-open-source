package net.lacnic.elections.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserAdmin implements Serializable {

	private static final long serialVersionUID = 7237293453682145209L;

	@Id
	@Column(name = "user_id")
	private String userId;

	@Column(name = "password", columnDefinition = "text")
	private String password;

	@Column(name = "email", columnDefinition = "text")
	private String email;

	@Column(name = "id_election_authorized")
	private Long idElectionAuthorized;


	public UserAdmin() { }


	public String getUserAdminId() {
		return userId.toLowerCase();
	}

	public void setUserAdminId(String userId) {
		this.userId = userId;
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

	public Long getIdElectionAuthorized() {
		return idElectionAuthorized;
	}

	public void setIdElectionAuthorized(Long idElectionAuthorized) {
		this.idElectionAuthorized = idElectionAuthorized;
	}
}
