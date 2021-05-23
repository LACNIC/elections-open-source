package net.lacnic.elections.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class SupraEleccion implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "supraeleccion_seq")
	@SequenceGenerator(name = "supraeleccion_seq", sequenceName = "supraeleccion_seq", allocationSize = 1)
	@Column(name = "id")
	private long id;

	private long idEleccionA;

	private long idEleccionB;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getIdEleccionA() {
		return idEleccionA;
	}

	public void setIdEleccionA(long idEleccionA) {
		this.idEleccionA = idEleccionA;
	}

	public long getIdEleccionB() {
		return idEleccionB;
	}

	public void setIdEleccionB(long idEleccionB) {
		this.idEleccionB = idEleccionB;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}