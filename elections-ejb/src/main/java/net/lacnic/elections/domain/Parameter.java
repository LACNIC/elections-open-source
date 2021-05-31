package net.lacnic.elections.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class Parameter implements Serializable {

	private static final long serialVersionUID = 649026078308606975L;

	@Id
	private String key;

	@Column(columnDefinition="TEXT")
	private String value;


	public Parameter() { }


	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
