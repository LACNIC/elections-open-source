package net.lacnic.elections.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrganizationBulkImportResult implements Serializable {

	private static final long serialVersionUID = 3917235370066624688L;

	private int processedRows;
	private int createdRows;
	private int updatedRows;
	private int deletedRows;
	private List<String> errors = new ArrayList<>();
	private byte[] errorReport;

	public int getProcessedRows() {
		return processedRows;
	}

	public void setProcessedRows(int processedRows) {
		this.processedRows = processedRows;
	}

	public int getCreatedRows() {
		return createdRows;
	}

	public void setCreatedRows(int createdRows) {
		this.createdRows = createdRows;
	}

	public int getUpdatedRows() {
		return updatedRows;
	}

	public void setUpdatedRows(int updatedRows) {
		this.updatedRows = updatedRows;
	}

	public int getDeletedRows() {
		return deletedRows;
	}

	public void setDeletedRows(int deletedRows) {
		this.deletedRows = deletedRows;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

	public byte[] getErrorReport() {
		return errorReport;
	}

	public void setErrorReport(byte[] errorReport) {
		this.errorReport = errorReport;
	}

	public boolean hasErrors() {
		return errors != null && !errors.isEmpty();
	}
}
