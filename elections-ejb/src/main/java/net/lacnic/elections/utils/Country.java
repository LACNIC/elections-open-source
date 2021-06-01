package net.lacnic.elections.utils;

import java.io.Serializable;


public class Country implements Serializable {

	private static final long serialVersionUID = 9177700762659300540L;

	private String id;
	private String name;


	public Country(String id, String name) {
		this.id = id;
		this.name = name;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@Override
	public String toString() {
		return this.name;
	}

}
