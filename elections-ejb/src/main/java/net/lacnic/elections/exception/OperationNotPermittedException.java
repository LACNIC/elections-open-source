package net.lacnic.elections.exception;


public class OperationNotPermittedException extends Exception {

	private static final long serialVersionUID = 8041401889653642928L;

	public OperationNotPermittedException(String error) {
		super(error);
	}

}
