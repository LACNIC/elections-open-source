package net.lacnic.siselecciones.data;

import java.io.Serializable;

public class EleccionReporte implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 972280016790017893L;
	private String nombreEleccion;
	private long usuariosVotaron;
	private long usuariosNoVotaron;
	private long usuariosTotales;
	private long correosPendientes;
	
	public EleccionReporte(String nombreEleccion, long usuariosVotaron, long usuariosNoVotaron, long usuariosTotales, long correosPendientes) {
		this.nombreEleccion = nombreEleccion;
		this.usuariosVotaron = usuariosVotaron;
		this.usuariosNoVotaron = usuariosNoVotaron;
		this.usuariosTotales = usuariosTotales;
		this.correosPendientes = correosPendientes;
	}
	
	public String getNombreEleccion() {
		return nombreEleccion;
	}

	public void setNombreEleccion(String nombreEleccion) {
		this.nombreEleccion = nombreEleccion;
	}

	public long getUsuariosVotaron() {
		return usuariosVotaron;
	}

	public void setUsuariosVotaron(long usuariosVotaron) {
		this.usuariosVotaron = usuariosVotaron;
	}

	public long getUsuariosNoVotaron() {
		return usuariosNoVotaron;
	}

	public void setUsuariosNoVotaron(long usuariosNoVotaron) {
		this.usuariosNoVotaron = usuariosNoVotaron;
	}

	public long getUsuariosTotales() {
		return usuariosTotales;
	}

	public void setUsuariosTotales(long usuariosTotales) {
		this.usuariosTotales = usuariosTotales;
	}

	public long getCorreosPendientes() {
		return correosPendientes;
	}

	public void setCorreosPendientes(long correosPendientes) {
		this.correosPendientes = correosPendientes;
	}

}
