package net.lacnic.elections.admin.app;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.ejb.ManagerEleccionesEJB;
import net.lacnic.elections.ejb.MonitorEleccionesEJB;
import net.lacnic.elections.ejb.VotanteEleccionesEJB;
import net.lacnic.elections.utils.Constants;

public class AppContext {

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private static AppContext instance;

	private ManagerEleccionesEJB managerBeanRemote;

	private MonitorEleccionesEJB monitorBeanRemote;

	private VotanteEleccionesEJB voterBeanRemote;

	private AppContext() {

		try {

			final Hashtable<String, String> jndiProperties = new Hashtable<>();
			jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			final Context context = new InitialContext(jndiProperties);

			String managerEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/ManagerEleccionesEJBBean!net.lacnic.elections.ejb.ManagerEleccionesEJB";
			String monitorEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/MonitorEleccionesEJBBean!net.lacnic.elections.ejb.MonitorEleccionesEJB";
			String voterEjb = Constants.EJB_PREFIX + Constants.JAR_NAME + "/VotanteEleccionesEJBBean!net.lacnic.elections.ejb.VotanteEleccionesEJB";
			setVoterBeanRemote((VotanteEleccionesEJB) context.lookup(voterEjb));
			setMonitorBeanRemote((MonitorEleccionesEJB) context.lookup(monitorEjb));
			setManagerBeanRemote((ManagerEleccionesEJB) context.lookup(managerEjb));

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

	public ManagerEleccionesEJB getManagerBeanRemote() {
		return managerBeanRemote;
	}

	public void setManagerBeanRemote(ManagerEleccionesEJB managerBeanRemote) {
		this.managerBeanRemote = managerBeanRemote;
	}

	public MonitorEleccionesEJB getMonitorBeanRemote() {
		return monitorBeanRemote;
	}

	public void setMonitorBeanRemote(MonitorEleccionesEJB monitorBeanRemote) {
		this.monitorBeanRemote = monitorBeanRemote;
	}

	public VotanteEleccionesEJB getVoterBeanRemote() {
		return voterBeanRemote;
	}

	public void setVoterBeanRemote(VotanteEleccionesEJB voterBeanRemote) {
		this.voterBeanRemote = voterBeanRemote;
	}

}
