package net.lacnic.elections.data;

import java.io.Serializable;


public class TableReportDataStringId implements Serializable {

	private static final long serialVersionUID = 35111768959822129L;

	private String id;
	private String description;


	public TableReportDataStringId() { }

	public TableReportDataStringId(String id, String description) {
		this.id = id;
		this.description = description;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
