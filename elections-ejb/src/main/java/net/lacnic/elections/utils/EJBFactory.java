package net.lacnic.elections.utils;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.ejb.ElectionsManagerEJB;
import net.lacnic.elections.ejb.ElectionsMonitorEJB;
import net.lacnic.elections.ejb.ElectionsVoterEJB;
import net.lacnic.elections.ejb.commons.ElectionsParametersEJB;
import net.lacnic.elections.ejb.commons.MailsSendingEJB;


public class EJBFactory {

	private static final String EBJ = "ejb:/";

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private static String JBOSSTEMPURI = System.getProperty("jboss.server.temp.dir");

	private ElectionsManagerEJB electionsManagerEJB;
	private ElectionsMonitorEJB electionsMonitorEJB;
	private ElectionsVoterEJB electionsVoterEJB;
	private ElectionsParametersEJB electionsParametersEJB;
	private MailsSendingEJB mailsSendingEJB;

	private static EJBFactory instance;


	private EJBFactory() {

		try {

			final Hashtable<String,String> jndiProperties = new Hashtable<>();
			jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			final Context context = new InitialContext(jndiProperties);

			final String moduleName = Constants.JAR_NAME;

			String managerEjb = EBJ + moduleName + "/ElectionsManagerEJBBean!net.lacnic.elections.ejb.ElectionsManagerEJB";
			String monitorEjb = EBJ + moduleName + "/ElectionsMonitorEJBBean!net.lacnic.elections.ejb.ElectionsMonitorEJB";
			String voterEjb = EBJ + moduleName + "/ElectionsVoterEJBBean!net.lacnic.elections.ejb.ElectionsVoterEJB";

			String parametersEjb = EBJ + moduleName + "/ElectionsParametersEJBBean!net.lacnic.elections.ejb.commons.ElectionsParametersEJB";
			String mailsSendingEjb = EBJ + moduleName + "/MailsSendingEJBBean!net.lacnic.elections.ejb.commons.MailsSendingEJB";

			setElectionsManagerEJB((ElectionsManagerEJB) context.lookup(managerEjb));
			setElectionsMonitorEJB((ElectionsMonitorEJB) context.lookup(monitorEjb));
			setElectionsVoterEJB((ElectionsVoterEJB) context.lookup(voterEjb));
			setElectionsParametersEJB((ElectionsParametersEJB) context.lookup(parametersEjb));
			setMailsSendingEJB((MailsSendingEJB) context.lookup(mailsSendingEjb));

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

	public static String getJbossTempUri() {
		return JBOSSTEMPURI.concat("/");
	}

	public ElectionsManagerEJB getElectionsManagerEJB() {
		return electionsManagerEJB;
	}

	public void setElectionsManagerEJB(ElectionsManagerEJB electionsManagerEJB) {
		this.electionsManagerEJB = electionsManagerEJB;
	}

	public ElectionsMonitorEJB getElectionsMonitorEJB() {
		return electionsMonitorEJB;
	}

	public void setElectionsMonitorEJB(ElectionsMonitorEJB electionsMonitorEJB) {
		this.electionsMonitorEJB = electionsMonitorEJB;
	}

	public ElectionsVoterEJB getElectionsVoterEJB() {
		return electionsVoterEJB;
	}

	public void setElectionsVoterEJB(ElectionsVoterEJB electionsVoterEJB) {
		this.electionsVoterEJB = electionsVoterEJB;
	}

	public ElectionsParametersEJB getElectionsParametersEJB() {
		return electionsParametersEJB;
	}

	public void setElectionsParametersEJB(ElectionsParametersEJB electionsParametersEJB) {
		this.electionsParametersEJB = electionsParametersEJB;
	}

	public MailsSendingEJB getMailsSendingEJB() {
		return this.mailsSendingEJB;
	}

	public void setMailsSendingEJB(MailsSendingEJB mailsSendingEJB) {
		this.mailsSendingEJB = mailsSendingEJB;
	}

}