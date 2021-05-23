package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;


@Entity
public class Actividad implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actividad_seq")
	@SequenceGenerator(name = "actividad_seq", sequenceName = "actividad_seq", allocationSize = 1)
	@Column(name = "id_actividad")
	private long idActividad;

	@Column(nullable = false)
	private String nomUser;
	
	@Column(nullable = true)
	private Long idEleccion;

	@Column(nullable = false)
	private String ip;

	@Column(nullable = false)
	private Date tiempo;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private TipoActividad tipoActividad;

	@Column(columnDefinition="TEXT")
	private String descripcion;

	public Actividad() { }

	public long getIdActividad() {
		return idActividad;
	}

	public void setIdActividad(long idActividad) {
		this.idActividad = idActividad;
	}

	public String getNomUser() {
		return nomUser;
	}

	public void setNomUser(String nomUser) {
		this.nomUser = nomUser;
	}

	public TipoActividad getTipoActividad() {
		return tipoActividad;
	}

	public void setTipoActividad(TipoActividad tipoActividad) {
		this.tipoActividad = tipoActividad;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getTiempo() {
		return tiempo;
	}

	public void setTiempo(Date date) {
		this.tiempo = date;
	}

	public Long getIdEleccion() {
		return idEleccion;
	}

	public void setIdEleccion(Long idEleccion) {
		this.idEleccion = idEleccion;
	}

}
