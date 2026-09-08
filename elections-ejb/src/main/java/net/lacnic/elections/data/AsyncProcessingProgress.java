package net.lacnic.elections.data;

import java.io.Serializable;

public class AsyncProcessingProgress implements Serializable {

	private static final long serialVersionUID = 8815237409689445392L;
	public static final String PHASE_PREPARING = "PREPARING";
	public static final String PHASE_PLANNED = "PLANNED";
	public static final String OPERATION_CENSUS_REGENERATE_VOTE_LINK = "CENSUS_REGENERATE_VOTE_LINK";
	public static final String OPERATION_ORGANIZATIONS_REGENERATE_NOMINATION_LINK = "ORGANIZATIONS_REGENERATE_NOMINATION_LINK";
	public static final String OPERATION_ORGANIZATIONS_REGENERATE_SUPPORT_LINK = "ORGANIZATIONS_REGENERATE_SUPPORT_LINK";

	private int processedRows;
	private int totalRows;
	private int createdRows;
	private int updatedRows;
	private int deletedRows;
	private int totalCreatedRows;
	private int totalUpdatedRows;
	private int totalDeletedRows;
	private long updatedAt;
	private String phase;
	private String operationKey;

	public AsyncProcessingProgress() {
	}

	public AsyncProcessingProgress(int processedRows, int totalRows, int createdRows, int updatedRows, int deletedRows, int totalCreatedRows, int totalUpdatedRows, int totalDeletedRows, long updatedAt) {
		this(processedRows, totalRows, createdRows, updatedRows, deletedRows, totalCreatedRows, totalUpdatedRows, totalDeletedRows, updatedAt, PHASE_PLANNED, null);
	}

	public AsyncProcessingProgress(int processedRows, int totalRows, int createdRows, int updatedRows, int deletedRows, int totalCreatedRows, int totalUpdatedRows, int totalDeletedRows, long updatedAt,
			String phase) {
		this(processedRows, totalRows, createdRows, updatedRows, deletedRows, totalCreatedRows, totalUpdatedRows, totalDeletedRows, updatedAt, phase, null);
	}

	public AsyncProcessingProgress(int processedRows, int totalRows, int createdRows, int updatedRows, int deletedRows, int totalCreatedRows, int totalUpdatedRows, int totalDeletedRows, long updatedAt,
			String phase, String operationKey) {
		this.processedRows = processedRows;
		this.totalRows = totalRows;
		this.createdRows = createdRows;
		this.updatedRows = updatedRows;
		this.deletedRows = deletedRows;
		this.totalCreatedRows = totalCreatedRows;
		this.totalUpdatedRows = totalUpdatedRows;
		this.totalDeletedRows = totalDeletedRows;
		this.updatedAt = updatedAt;
		this.phase = phase;
		this.operationKey = operationKey;
	}

	public AsyncProcessingProgress(AsyncProcessingProgress source) {
		if (source == null) {
			return;
		}
		this.processedRows = source.processedRows;
		this.totalRows = source.totalRows;
		this.createdRows = source.createdRows;
		this.updatedRows = source.updatedRows;
		this.deletedRows = source.deletedRows;
		this.totalCreatedRows = source.totalCreatedRows;
		this.totalUpdatedRows = source.totalUpdatedRows;
		this.totalDeletedRows = source.totalDeletedRows;
		this.updatedAt = source.updatedAt;
		this.phase = source.phase;
		this.operationKey = source.operationKey;
	}

	public int getProcessedRows() {
		return processedRows;
	}

	public void setProcessedRows(int processedRows) {
		this.processedRows = processedRows;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
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

	public int getTotalCreatedRows() {
		return totalCreatedRows;
	}

	public void setTotalCreatedRows(int totalCreatedRows) {
		this.totalCreatedRows = totalCreatedRows;
	}

	public int getTotalUpdatedRows() {
		return totalUpdatedRows;
	}

	public void setTotalUpdatedRows(int totalUpdatedRows) {
		this.totalUpdatedRows = totalUpdatedRows;
	}

	public int getTotalDeletedRows() {
		return totalDeletedRows;
	}

	public void setTotalDeletedRows(int totalDeletedRows) {
		this.totalDeletedRows = totalDeletedRows;
	}

	public long getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(long updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String getOperationKey() {
		return operationKey;
	}

	public void setOperationKey(String operationKey) {
		this.operationKey = operationKey;
	}
}
