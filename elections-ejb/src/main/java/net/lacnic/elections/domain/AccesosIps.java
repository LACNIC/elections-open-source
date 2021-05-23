package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class AccesosIps implements Serializable {

	private static final long serialVersionUID = -1584886907691554042L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aip_seq")
	@SequenceGenerator(name = "aip_seq", sequenceName = "aip_seq", allocationSize = 1)
	private long id;

	@Column(nullable = false)
	private int intentos;

	@Column(nullable = false)
	private String ip;

	@Column(nullable = false)
	private Date fechaUltimoIntento;

	@Column(nullable = false)
	private Date fechaPrimerIntento;

	public Date getFechaPrimerIntento() {
		return fechaPrimerIntento;
	}

	public void setFechaPrimerIntento(Date fechaPrimerIntento) {
		this.fechaPrimerIntento = fechaPrimerIntento;
	}

	public long getIdIpInhabilitada() {
		return id;
	}

	public void setIdIpInhabilitada(long id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getIntentos() {
		return intentos;
	}

	public void setIntentos(int intentos) {
		this.intentos = intentos;
	}

	public Date getFechaUltimoIntento() {
		return fechaUltimoIntento;
	}

	public void setFechaUltimoIntento(Date fechaUltimoIntento) {
		this.fechaUltimoIntento = fechaUltimoIntento;
	}

}
