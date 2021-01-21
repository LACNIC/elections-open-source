package net.lacnic.siselecciones.dominio;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class Comisionado implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidato_seq")
	@SequenceGenerator(name = "candidato_seq", sequenceName = "candidato_seq", allocationSize = 1)
	@Column(name = "id_comisionado")
	private long idComisionado;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false)
	private String mail;

	public long getIdComisionado() {
		return idComisionado;
	}

	public void setIdComisionado(long idComisionado) {
		this.idComisionado = idComisionado;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}
}