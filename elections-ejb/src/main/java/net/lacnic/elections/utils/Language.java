package net.lacnic.elections.utils;

import java.io.Serializable;


public class Language implements Serializable {

	private static final long serialVersionUID = 9177700762659300540L;

	private String id;
	private String name;


	public Language(String id, String name) {
		this.id = id;
		this.name = name;
	}


	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
