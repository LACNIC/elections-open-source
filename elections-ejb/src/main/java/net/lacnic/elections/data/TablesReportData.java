package net.lacnic.elections.data;

import java.io.Serializable;


public class TablesReportData implements Serializable {

	private static final long serialVersionUID = -6063578840115168590L;

	private long id;
	private String description;


	public TablesReportData() { }

	public TablesReportData(long id, String description) {
		super();
		this.id = id;
		this.description = description;
	}


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
