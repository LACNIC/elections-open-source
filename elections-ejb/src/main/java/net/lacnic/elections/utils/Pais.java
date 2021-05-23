package net.lacnic.elections.utils;

import java.io.Serializable;

public class Pais implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9177700762659300540L;
	private String id;
	private String nombre;
	
	public Pais(String id, String nombre) {
		this.id = id;
		this.nombre = nombre;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getNombre() {
		return nombre;
	}

	@Override
	public String toString() {
		return this.nombre;
	}
}
