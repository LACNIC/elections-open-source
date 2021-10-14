package net.lacnic.elections.adminweb.app;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.ejb.ElectionsManagerEJB;
import net.lacnic.elections.ejb.ElectionsMonitorEJB;
import net.lacnic.elections.ejb.ElectionsVoterEJB;
import net.lacnic.elections.utils.Constants;


public class AppContext {

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private static AppContext instance;

	private ElectionsManagerEJB managerBeanRemote;
	private ElectionsMonitorEJB monitorBeanRemote;
	private ElectionsVoterEJB voterBeanRemote;


	private AppContext() {

		try {

			final Hashtable<String, String> jndiProperties = new Hashtable<>();
			jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			final Context context = new InitialContext(jndiProperties);

			String managerEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/ElectionsManagerEJBBean!net.lacnic.elections.ejb.ElectionsManagerEJB";
			String monitorEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/ElectionsMonitorEJBBean!net.lacnic.elections.ejb.ElectionsMonitorEJB";
			String voterEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/ElectionsVoterEJBBean!net.lacnic.elections.ejb.ElectionsVoterEJB";
			setVoterBeanRemote((ElectionsVoterEJB) context.lookup(voterEjb));
			setMonitorBeanRemote((ElectionsMonitorEJB) context.lookup(monitorEjb));
			setManagerBeanRemote((ElectionsManagerEJB) context.lookup(managerEjb));

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

	public ElectionsManagerEJB getManagerBeanRemote() {
		return managerBeanRemote;
	}

	public void setManagerBeanRemote(ElectionsManagerEJB managerBeanRemote) {
		this.managerBeanRemote = managerBeanRemote;
	}

	public ElectionsMonitorEJB getMonitorBeanRemote() {
		return monitorBeanRemote;
	}

	public void setMonitorBeanRemote(ElectionsMonitorEJB monitorBeanRemote) {
		this.monitorBeanRemote = monitorBeanRemote;
	}

	public ElectionsVoterEJB getVoterBeanRemote() {
		return voterBeanRemote;
	}

	public void setVoterBeanRemote(ElectionsVoterEJB voterBeanRemote) {
		this.voterBeanRemote = voterBeanRemote;
	}

}
