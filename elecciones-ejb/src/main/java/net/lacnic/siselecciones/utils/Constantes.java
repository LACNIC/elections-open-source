package net.lacnic.siselecciones.utils;

import java.util.HashMap;

/**
 * Clase utilizada para almacenar algunas constantes del sistema como por
 * ejemplo el path de los templates de emails, a la vez que las urls de
 * votacion, auditoria y resultados
 * 
 * @author Antonymous
 *
 */
public class Constantes {

	private static HashMap<String, String> parametros = new HashMap<>();

	// Parameter names for 'parametros' database table
	public static final String APP = "APP";
	public static final String URL = "URL";
	public static final String EMAIL_HOST = "EMAIL_HOST";
	public static final String EMAIL_USUARIO = "EMAIL_USUARIO";
	public static final String EMAIL_CLAVE = "EMAIL_CLAVE";
	public static final String REMITENTE_ESTANDAR = "REMITENTE_ESTANDAR";
	public static final String RECEPTOR_ESTANDAR = "RECEPTOR_ESTANDAR";
	public static final String WS_AUTH_TOKEN = "WS_AUTH_TOKEN";
	public static final String WS_IPS_HABILITADAS = "WS_IPS_HABILITADAS";
	public static final String WEBSITE_DEFAULT = "WEBSITE_DEFAULT";

	// Used for Candidates order
	public static final int ORDEN_MINIMO = 0;
	public static final int ORDEN_MAXIMO = 100000;

	// Used for EJB naming
	public static final String PREFIJO_EJB = "ejb:/";
	public static final String NOMBRE_JAR = "elecciones-ejb";

	public static final String SkGoogleApiReCaptcha = "SkGoogleApiReCaptcha";

	private static final String NOMBRE_WAR = "elecciones";
	private static final String VOTAR = "/votar";
	private static final String RESULT = "/result";
	private static final String AUDIT = "/audit";

	public static final String TipoTemplateNUEVO = "NUEVO";
	public static final String TipoTemplateAUDITOR = "AUDITOR";
	public static final String TipoTemplateAUDITOR_CONFORME = "AUDITOR_CONFORME";
	public static final String TipoTemplateCODIGOS_VOTACION = "CODIGOS_VOTACION";
	public static final String TipoTemplateAUDITOR_REVISION = "AUDITOR_REVISION";

	private Constantes() {
		throw new IllegalStateException("Utility class");
	}

	public static String getURLVotos(String ruta) {
		return ruta + NOMBRE_WAR + VOTAR;
	}

	public static String getURLResultados(String ruta) {
		return ruta + NOMBRE_WAR + RESULT;
	}

	public static String getURLResultadosAudit(String ruta) {
		return ruta + NOMBRE_WAR + AUDIT;
	}

	public static void clean() {
		cleanCacheParametros();
	}

	public static void cleanCacheParametros() {
		setParametros(new HashMap<String, String>());
	}

	public static HashMap<String, String> getParametros() {
		return parametros;
	}

	public static void setParametros(HashMap<String, String> parametros) {
		Constantes.parametros = parametros;
	}

}
