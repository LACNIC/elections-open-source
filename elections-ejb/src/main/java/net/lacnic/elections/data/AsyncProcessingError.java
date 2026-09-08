package net.lacnic.elections.data;

import java.io.Serializable;
import java.util.Date;

public class AsyncProcessingError implements Serializable {

	private static final long serialVersionUID = 1600275496848132879L;

	private String messageKey;
	private Integer errorRow;
	private String errorInfo;
	private String technicalDetail;
	private Date createdAt;

	public AsyncProcessingError() {
		this.createdAt = new Date();
	}

	public AsyncProcessingError(String messageKey, Integer errorRow, String errorInfo, String technicalDetail) {
		this.messageKey = messageKey;
		this.errorRow = errorRow;
		this.errorInfo = errorInfo;
		this.technicalDetail = technicalDetail;
		this.createdAt = new Date();
	}

	public String getMessageKey() {
		return messageKey;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	public Integer getErrorRow() {
		return errorRow;
	}

	public void setErrorRow(Integer errorRow) {
		this.errorRow = errorRow;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}

	public String getTechnicalDetail() {
		return technicalDetail;
	}

	public void setTechnicalDetail(String technicalDetail) {
		this.technicalDetail = technicalDetail;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}
