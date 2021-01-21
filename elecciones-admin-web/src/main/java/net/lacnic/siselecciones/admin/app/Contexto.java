package net.lacnic.siselecciones.admin.app;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.siselecciones.ejb.ManagerEleccionesEJB;
import net.lacnic.siselecciones.ejb.MonitorEleccionesEJB;
import net.lacnic.siselecciones.ejb.VotanteEleccionesEJB;
import net.lacnic.siselecciones.utils.Constantes;

public class Contexto {

	/*private static final String JBOSSCONFURI = "";
	private static final String JBOSSTEMPURI = "";*/

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private static Contexto instance;

	private ManagerEleccionesEJB managerBeanRemote;

	private MonitorEleccionesEJB monitorBeanRemote;

	private VotanteEleccionesEJB votanteBeanRemote;

	private Contexto() {

		try {

			final Hashtable<String, String> jndiProperties = new Hashtable<>();
			jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			final Context context = new InitialContext(jndiProperties);

			String managerEjb = Constantes.PREFIJO_EJB + Constantes.NOMBRE_JAR + "/ManagerEleccionesEJBBean!net.lacnic.siselecciones.ejb.ManagerEleccionesEJB";
			String monitorEjb = Constantes.PREFIJO_EJB + Constantes.NOMBRE_JAR + "/MonitorEleccionesEJBBean!net.lacnic.siselecciones.ejb.MonitorEleccionesEJB";
			String votanteEjb = Constantes.PREFIJO_EJB + Constantes.NOMBRE_JAR + "/VotanteEleccionesEJBBean!net.lacnic.siselecciones.ejb.VotanteEleccionesEJB";
			setVotanteBeanRemote((VotanteEleccionesEJB) context.lookup(votanteEjb));
			setMonitorBeanRemote((MonitorEleccionesEJB) context.lookup(monitorEjb));
			setManagerBeanRemote((ManagerEleccionesEJB) context.lookup(managerEjb));

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

	/*public static String getJbossConfUri() {
		return JBOSSCONFURI;
	}

	public static String getJbossTempUri() {
		return JBOSSTEMPURI.concat("/");
	}*/

	public ManagerEleccionesEJB getManagerBeanRemote() {
		return managerBeanRemote;
	}

	public MonitorEleccionesEJB getMonitorBeanRemote() {
		return monitorBeanRemote;
	}

	public VotanteEleccionesEJB getVotanteBeanRemote() {
		return votanteBeanRemote;
	}

	public void setVotanteBeanRemote(VotanteEleccionesEJB votanteBeanRemote) {
		this.votanteBeanRemote = votanteBeanRemote;
	}

	public void setManagerBeanRemote(ManagerEleccionesEJB managerBeanRemote) {
		this.managerBeanRemote = managerBeanRemote;
	}

	public void setMonitorBeanRemote(MonitorEleccionesEJB monitorBeanRemote) {
		this.monitorBeanRemote = monitorBeanRemote;
	}

}
