package net.lacnic.siselecciones.utils;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.siselecciones.ejb.ManagerEleccionesEJB;
import net.lacnic.siselecciones.ejb.MonitorEleccionesEJB;
import net.lacnic.siselecciones.ejb.VotanteEleccionesEJB;
import net.lacnic.siselecciones.ejb.commons.EnvioMailsEJB;
import net.lacnic.siselecciones.ejb.commons.ParametrosEleccionesEJB;

public class EJBFactory {

	private static final String EBJ = "ejb:/";
	
	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");
	
	private static String JBOSSTEMPURI = System.getProperty("jboss.server.temp.dir");

	private ManagerEleccionesEJB managerEleccionesEJB;
	private MonitorEleccionesEJB monitorEleccionesEJB;
	private VotanteEleccionesEJB votanteEleccionesEJB;
	private ParametrosEleccionesEJB parametrosEleccionesEJB;
	private EnvioMailsEJB envioMailsEJB;

	private static EJBFactory instance;

	private EJBFactory() {

		try {

			final Hashtable<String,String> jndiProperties = new Hashtable<>();
			jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			final Context context = new InitialContext(jndiProperties);

			final String moduleName = Constantes.NOMBRE_JAR;

			String managerEjb = EBJ + moduleName + "/ManagerEleccionesEJBBean!net.lacnic.siselecciones.ejb.ManagerEleccionesEJB";
			String monitorEjb = EBJ + moduleName + "/MonitorEleccionesEJBBean!net.lacnic.siselecciones.ejb.MonitorEleccionesEJB";
			String votanteEjb = EBJ + moduleName + "/VotanteEleccionesEJBBean!net.lacnic.siselecciones.ejb.VotanteEleccionesEJB";

			String parametrosEjb = EBJ + moduleName + "/ParametrosEleccionesEJBBean!net.lacnic.siselecciones.ejb.commons.ParametrosEleccionesEJB";
			String envioMailsEjb = EBJ + moduleName + "/EnvioMailsEJBBean!net.lacnic.siselecciones.ejb.commons.EnvioMailsEJB";

			setManagerEleccionesEJB((ManagerEleccionesEJB) context.lookup(managerEjb));
			setMonitorEleccionesEJB((MonitorEleccionesEJB) context.lookup(monitorEjb));
			setVotanteEleccionesEJB((VotanteEleccionesEJB) context.lookup(votanteEjb));
			setParametrosEleccionesEJB((ParametrosEleccionesEJB) context.lookup(parametrosEjb));
			setEnvioMailsEJB((EnvioMailsEJB) context.lookup(envioMailsEjb));

		} catch (NamingException e) {
			appLogger.error(e);
		}
	}

	public static EJBFactory getInstance() {
		if (instance == null) {
			instance = new EJBFactory();
		}
		return instance;
	}

	public ManagerEleccionesEJB getManagerEleccionesEJB() {
		return managerEleccionesEJB;
	}

	public void setManagerEleccionesEJB(ManagerEleccionesEJB managerEleccionesEJB) {
		this.managerEleccionesEJB = managerEleccionesEJB;
	}

	public MonitorEleccionesEJB getMonitorEleccionesEJB() {
		return monitorEleccionesEJB;
	}

	public void setMonitorEleccionesEJB(MonitorEleccionesEJB monitorEleccionesEJB) {
		this.monitorEleccionesEJB = monitorEleccionesEJB;
	}

	public VotanteEleccionesEJB getVotanteEleccionesEJB() {
		return votanteEleccionesEJB;
	}

	public void setVotanteEleccionesEJB(VotanteEleccionesEJB votanteEleccionesEJB) {
		this.votanteEleccionesEJB = votanteEleccionesEJB;
	}

	public ParametrosEleccionesEJB getParametrosEleccionesEJB() {
		return parametrosEleccionesEJB;
	}

	public void setParametrosEleccionesEJB(ParametrosEleccionesEJB parametrosEleccionesEJB) {
		this.parametrosEleccionesEJB = parametrosEleccionesEJB;
	}

	public static String getJbossTempUri() {
		return JBOSSTEMPURI.concat("/");
	}

	public EnvioMailsEJB getEnvioMailsEJB() {
		return this.envioMailsEJB;
	}

	public void setEnvioMailsEJB(EnvioMailsEJB envioMailsEJB) {
		this.envioMailsEJB = envioMailsEJB;
	}

}