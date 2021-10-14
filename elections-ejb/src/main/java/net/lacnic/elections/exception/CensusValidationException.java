package net.lacnic.elections.exception;


public class CensusValidationException extends Exception {

	private static final long serialVersionUID = -917344981082570556L;

	private Integer errorRow;
	private String errorInfo;


	public CensusValidationException(String error, Integer errorRow, String errorInfo) {
		super(error);
		this.errorRow = errorRow;
		this.errorInfo = errorInfo;
	}


	public Integer getErrorRow() {
		return errorRow;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

}
