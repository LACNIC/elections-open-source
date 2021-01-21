package net.lacnic.siselecciones.data;

import java.io.Serializable;
import java.util.List;

public class HealthCheck implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8887832519452826861L;
	private int intentosDeEnvio;
	private long ipsAccesosFallidos;
	private long sumaAccesosFallidos;
	private long correosTotales;
	private long correosPendientes;
	private long correosEnviados;
	private List<EleccionReporte> elecciones;

	public HealthCheck(int intentosDeEnvio, long ipsAccesosFallidos, long sumaAccesosFallidos, long correosTotales, long correosPendientes, long correosEnviados, List<EleccionReporte> elecciones) {
		this.intentosDeEnvio = intentosDeEnvio;
		this.ipsAccesosFallidos = ipsAccesosFallidos;
		this.sumaAccesosFallidos = sumaAccesosFallidos;
		this.correosTotales = correosTotales;
		this.correosPendientes = correosPendientes;
		this.correosEnviados = correosEnviados;
		this.elecciones = elecciones;
	}

	public int getIntentosDeEnvio() {
		return intentosDeEnvio;
	}

	public void setIntentosDeEnvio(int intentosDeEnvio) {
		this.intentosDeEnvio = intentosDeEnvio;
	}

	public long getIpsAccesosFallidos() {
		return ipsAccesosFallidos;
	}

	public void setIpsAccesosFallidos(long ipsAccesosFallidos) {
		this.ipsAccesosFallidos = ipsAccesosFallidos;
	}

	public long getSumaAccesosFallidos() {
		return sumaAccesosFallidos;
	}

	public void setSumaAccesosFallidos(long sumaAccesosFallidos) {
		this.sumaAccesosFallidos = sumaAccesosFallidos;
	}

	public long getCorreosTotales() {
		return correosTotales;
	}

	public void setCorreosTotales(long correosTotales) {
		this.correosTotales = correosTotales;
	}

	public long getCorreosPendientes() {
		return correosPendientes;
	}

	public void setCorreosPendientes(long correosPendientes) {
		this.correosPendientes = correosPendientes;
	}

	public long getCorreosEnviados() {
		return correosEnviados;
	}

	public void setCorreosEnviados(long correosEnviados) {
		this.correosEnviados = correosEnviados;
	}

	public List<EleccionReporte> getElecciones() {
		return elecciones;
	}

	public void setElecciones(List<EleccionReporte> elecciones) {
		this.elecciones = elecciones;
	}

}
