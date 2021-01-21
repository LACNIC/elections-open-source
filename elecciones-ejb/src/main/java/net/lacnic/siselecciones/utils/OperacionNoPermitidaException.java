package net.lacnic.siselecciones.utils;

public class OperacionNoPermitidaException extends Exception {
	
	private static final long serialVersionUID = 8041401889653642928L;

	public OperacionNoPermitidaException(String error) {
		super(error);
	}

}
