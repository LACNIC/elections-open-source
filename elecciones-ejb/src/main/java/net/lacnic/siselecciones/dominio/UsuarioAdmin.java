package net.lacnic.siselecciones.dominio;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UsuarioAdmin implements Serializable {

	private static final long serialVersionUID = 7237293453682145209L;

	@Id
	@Column(name = "user_id")
	private String userId;

	@Column(name = "password", columnDefinition = "text")
	private String password;

	@Column(name = "email", columnDefinition = "text")
	private String email;

	@Column(name = "id_eleccion_autorizado")
	private Long idEleccionAutorizado;


	public UsuarioAdmin() { }


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

	public Long getIdEleccionAutorizado() {
		return idEleccionAutorizado;
	}

	public void setIdEleccionAutorizado(Long idEleccionAutorizado) {
		this.idEleccionAutorizado = idEleccionAutorizado;
	}
}
