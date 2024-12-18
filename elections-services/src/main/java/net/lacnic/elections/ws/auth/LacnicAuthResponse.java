package net.lacnic.elections.ws.auth;

import java.io.Serializable;
import java.util.List;

public class LacnicAuthResponse implements Serializable {

	private static final long serialVersionUID = 2480608316427827824L;

	private Boolean authenticated;
	private String token;
	private List<String> roles;
	private String error;
	private String ipAllowed;

	public LacnicAuthResponse() {
		// Default initialization
	}

	public Boolean getAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(Boolean authenticated) {
		this.authenticated = authenticated;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getIpAllowed() {
		return ipAllowed;
	}

	public void setIpAllowed(String ipAllowed) {
		this.ipAllowed = ipAllowed;
	}

}
