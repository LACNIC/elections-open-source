package net.lacnic.web.ext.elecciones.utils;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.siselecciones.ejb.MonitorEleccionesEJB;
import net.lacnic.siselecciones.utils.Constantes;

public class Contexto {

	private static final Logger appLogger = LogManager.getLogger("servicesAppLogger");

	private static Contexto instance;

	private MonitorEleccionesEJB monitorBeanRemote;

	private Contexto() {
		try {
			final Hashtable<String, String> jndiProperties = new Hashtable<>();
			jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			final Context context = new InitialContext(jndiProperties);

			String monitorEjb = Constantes.PREFIJO_EJB + Constantes.NOMBRE_JAR + "/MonitorEleccionesEJBBean!net.lacnic.siselecciones.ejb.MonitorEleccionesEJB";

			setMonitorBeanRemote((MonitorEleccionesEJB) context.lookup(monitorEjb));
		} catch (NamingException e) {
			appLogger.error(e);
		}
	}

	public static Contexto getInstance() {
		if (instance == null) {
			instance = new Contexto();
		}
		return instance;
	}

	public MonitorEleccionesEJB getMonitorBeanRemote() {
		return monitorBeanRemote;
	}

	public void setMonitorBeanRemote(MonitorEleccionesEJB monitorBeanRemote) {
		this.monitorBeanRemote = monitorBeanRemote;
	}

}
