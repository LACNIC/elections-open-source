package net.lacnic.elections.ws.app;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.ejb.ElectionsMonitorEJB;
import net.lacnic.elections.utils.Constants;


public class AppContext {

	private static final Logger appLogger = LogManager.getLogger("servicesAppLogger");

	private static AppContext instance;

	private ElectionsMonitorEJB monitorBeanRemote;


	private AppContext() {
		try {
			final Hashtable<String, String> jndiProperties = new Hashtable<>();
			jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			final Context context = new InitialContext(jndiProperties);

			String monitorEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/ElectionsMonitorEJBBean!net.lacnic.elections.ejb.ElectionsMonitorEJB";

			setMonitorBeanRemote((ElectionsMonitorEJB) context.lookup(monitorEjb));
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

	public ElectionsMonitorEJB getMonitorBeanRemote() {
		return monitorBeanRemote;
	}

	public void setMonitorBeanRemote(ElectionsMonitorEJB monitorBeanRemote) {
		this.monitorBeanRemote = monitorBeanRemote;
	}

}
