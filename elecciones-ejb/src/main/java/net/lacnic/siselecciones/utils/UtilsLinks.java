package net.lacnic.siselecciones.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class UtilsLinks {
	
	private static final String TOKEN = "?token="; 
	
	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	
	private UtilsLinks() {
		throw new IllegalStateException("Utility class");
	}

	public static String calcularLinkVotar(String token) {
		try {
			String url = EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constantes.URL);
			return Constantes.getURLVotos(url) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1);
		}
		return "";

	}

	public static String calcularLinkResultado(String token) {
		try {
			String url = EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constantes.URL);
			return Constantes.getURLResultados(url) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1);
		}
		return "";

	}

	public static String calcularLinkResultadoAuditor(String token) {
		try {
			String url = EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constantes.URL);
			return Constantes.getURLResultadosAudit(url) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1);
		}
		return "";
	}

}
