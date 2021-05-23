package net.lacnic.elections.exception;


public class CensusValidationException extends Exception {

	private static final long serialVersionUID = -917344981082570556L;

	public CensusValidationException(String error) {
		super(error);
	}

}
