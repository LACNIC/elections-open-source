package net.lacnic.siselecciones.dominio;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Parametro implements Serializable {

	private static final long serialVersionUID = 649026078308606975L;

	@Id
	private String clave;

	@Column(columnDefinition="TEXT")
	private String valor;


	public Parametro() { }


	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
}
