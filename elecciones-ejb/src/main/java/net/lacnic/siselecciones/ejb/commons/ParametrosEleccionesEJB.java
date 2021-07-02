package net.lacnic.siselecciones.ejb.commons;

import java.util.List;

import javax.ejb.Remote;

import net.lacnic.siselecciones.dominio.Parametro;

@Remote
public interface ParametrosEleccionesEJB {

	List<Parametro> obtenerListadoParametro();
	
	String obtenerParametro(String clave);

	boolean isProd();

	boolean agregarParametro(String c, String v);

	void editarParametro(Parametro p);

	void borrarParametro(String c);

	String getDataSiteKey();

}
