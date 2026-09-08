package net.lacnic.elections.adminweb.app;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.ejb.ElectionsManagerEJB;
import net.lacnic.elections.ejb.ElectionsMonitorEJB;
import net.lacnic.elections.ejb.ElectionsPreNominationEJB;
import net.lacnic.elections.ejb.ElectionsVoterEJB;
import net.lacnic.elections.utils.Constants;

public class AppContext {

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private static AppContext instance;

	private ElectionsManagerEJB managerBeanRemote;
	private ElectionsMonitorEJB monitorBeanRemote;
	private ElectionsVoterEJB voterBeanRemote;
	private ElectionsPreNominationEJB preNominationBeanRemote;

	private AppContext() {

		try {

			final Hashtable<String, String> jndiProperties = new Hashtable<>();
			jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			final Context context = new InitialContext(jndiProperties);

			String managerEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/ElectionsManagerEJBBean!net.lacnic.elections.ejb.ElectionsManagerEJB";
			String monitorEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/ElectionsMonitorEJBBean!net.lacnic.elections.ejb.ElectionsMonitorEJB";
			String voterEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/ElectionsVoterEJBBean!net.lacnic.elections.ejb.ElectionsVoterEJB";
			String preNominationEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/ElectionsPreNominationEJBBean!net.lacnic.elections.ejb.ElectionsPreNominationEJB";
			setVoterBeanRemote((ElectionsVoterEJB) context.lookup(voterEjb));
			setMonitorBeanRemote((ElectionsMonitorEJB) context.lookup(monitorEjb));
			setManagerBeanRemote((ElectionsManagerEJB) context.lookup(managerEjb));
			setPreNominationBeanRemote((ElectionsPreNominationEJB) context.lookup(preNominationEjb));

		} catch (NamingException e) {
			appLogger.error(e.getMessage(), e);
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

	public ElectionsPreNominationEJB getPreNominationBeanRemote() {
		return preNominationBeanRemote;
	}

	public void setPreNominationBeanRemote(ElectionsPreNominationEJB preNominationBeanRemote) {
		this.preNominationBeanRemote = preNominationBeanRemote;
	}

}
