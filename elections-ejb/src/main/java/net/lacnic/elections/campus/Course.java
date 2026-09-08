package net.lacnic.elections.campus;

import java.io.Serializable;

public class Course implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String fullName;

	public Course() {
	}

	public Course(Long id, String fullName) {
		this.id = id;
		this.fullName = fullName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullDescription() {
		return "".concat(getId() + " - " + getFullName());
	}

}
