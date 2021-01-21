package net.lacnic.siselecciones.data;

import java.io.Serializable;
import java.util.Date;

public class Participacion implements Serializable {

	private static final long serialVersionUID = 1L;

	private String orgId;
	private String email;
	private String tituloEleccionSP;
	private String tituloEleccionEN;
	private String tituloEleccionPT;
	private String nombre;
	private String pais;
	private Date fechaInicioEleccion;
	private Date fechaFinEleccion;
	private String categoria;
	private String linkEleccionSP;
	private String linkEleccionEN;
	private String linkEleccionPT;
	private String linkVotar;

	boolean yaVoto;

	public Participacion() { }

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTituloEleccionSP() {
		return tituloEleccionSP;
	}

	public void setTituloEleccionSP(String tituloEleccionSP) {
		this.tituloEleccionSP = tituloEleccionSP;
	}

	public String getTituloEleccionEN() {
		return tituloEleccionEN;
	}

	public void setTituloEleccionEN(String tituloEleccionEN) {
		this.tituloEleccionEN = tituloEleccionEN;
	}

	public String getTituloEleccionPT() {
		return tituloEleccionPT;
	}

	public void setTituloEleccionPT(String tituloEleccionPT) {
		this.tituloEleccionPT = tituloEleccionPT;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Date getFechaInicioEleccion() {
		return fechaInicioEleccion;
	}

	public void setFechaInicioEleccion(Date fechaInicioEleccion) {
		this.fechaInicioEleccion = fechaInicioEleccion;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public boolean isYaVoto() {
		return yaVoto;
	}

	public void setYaVoto(boolean yaVoto) {
		this.yaVoto = yaVoto;
	}

	public Date getFechaFinEleccion() {
		return fechaFinEleccion;
	}

	public void setFechaFinEleccion(Date fechaFinEleccion) {
		this.fechaFinEleccion = fechaFinEleccion;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getLinkEleccionSP() {
		return linkEleccionSP;
	}

	public void setLinkEleccionSP(String linkEleccionSP) {
		this.linkEleccionSP = linkEleccionSP;
	}

	public String getLinkEleccionEN() {
		return linkEleccionEN;
	}

	public void setLinkEleccionEN(String linkEleccionEN) {
		this.linkEleccionEN = linkEleccionEN;
	}

	public String getLinkEleccionPT() {
		return linkEleccionPT;
	}

	public void setLinkEleccionPT(String linkEleccionPT) {
		this.linkEleccionPT = linkEleccionPT;
	}

	public String getLinkVotar() {
		return linkVotar;
	}

	public void setLinkVotar(String linkVotar) {
		this.linkVotar = linkVotar;
	}
}