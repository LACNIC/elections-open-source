package net.lacnic.elections.ejb.commons;

import java.util.List;

import javax.ejb.Remote;

import net.lacnic.elections.domain.Parameter;

@Remote
public interface ParametrosEleccionesEJB {

	List<Parameter> obtenerListadoParametro();
	
	String obtenerParametro(String clave);

	boolean isProd();

	boolean agregarParametro(String c, String v);

	void editarParametro(Parameter p);

	void borrarParametro(String c);

}
