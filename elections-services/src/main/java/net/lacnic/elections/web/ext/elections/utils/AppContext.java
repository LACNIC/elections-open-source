package net.lacnic.elections.web.ext.elections.utils;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.ejb.MonitorEleccionesEJB;
import net.lacnic.elections.utils.Constants;

public class AppContext {

	private static final Logger appLogger = LogManager.getLogger("servicesAppLogger");

	private static AppContext instance;

	private MonitorEleccionesEJB monitorBeanRemote;

	private AppContext() {
		try {
			final Hashtable<String, String> jndiProperties = new Hashtable<>();
			jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			final Context context = new InitialContext(jndiProperties);

			String monitorEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/MonitorEleccionesEJBBean!net.lacnic.elections.ejb.MonitorEleccionesEJB";

			setMonitorBeanRemote((MonitorEleccionesEJB) context.lookup(monitorEjb));
		} catch (NamingException e) {
			appLogger.error(e);
		}
	}

	public static AppContext getInstance() {
		if (instance == null) {
			instance = new AppContext();
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
