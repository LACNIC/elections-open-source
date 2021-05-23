package net.lacnic.elections.ejb.commons;

import java.util.List;

import javax.ejb.Remote;

import net.lacnic.elections.domain.Parametro;

@Remote
public interface ParametrosEleccionesEJB {

	List<Parametro> obtenerListadoParametro();
	
	String obtenerParametro(String clave);

	boolean isProd();

	boolean agregarParametro(String c, String v);

	void editarParametro(Parametro p);

	void borrarParametro(String c);

}
