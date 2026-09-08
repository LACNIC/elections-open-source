package net.lacnic.elections.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.lacnic.elections.domain.UserAdmin;

public class AdminLoginResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private UserAdmin userAdmin;
	private Set<String> roles = new LinkedHashSet<>();
	private String error;

	public AdminLoginResult() {
	}

	public AdminLoginResult(String error) {
		this.error = error;
	}

	public AdminLoginResult(UserAdmin userAdmin, Collection<String> roles) {
		this.userAdmin = userAdmin;
		setRoles(roles);
	}

	public UserAdmin getUserAdmin() {
		return userAdmin;
	}

	public void setUserAdmin(UserAdmin userAdmin) {
		this.userAdmin = userAdmin;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Collection<String> roles) {
		this.roles = roles == null ? new LinkedHashSet<>() : new LinkedHashSet<>(roles);
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
