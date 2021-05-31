package net.lacnic.elections.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class UtilsLinks {

	private static final String TOKEN = "?token="; 
	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");


	private UtilsLinks() {
		throw new IllegalStateException("Utility class");
	}


	public static String buildVoteLink(String token) {
		try {
			String url = EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constants.URL);
			return Constants.getVotesURL(url) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1);
		}
		return "";
	}

	public static String buildResultsLink(String token) {
		try {
			String url = EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constants.URL);
			return Constants.getResultsURL(url) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1);
		}
		return "";
	}

	public static String buildAuditorResultsLink(String token) {
		try {
			String url = EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constants.URL);
			return Constants.getResultsAuditURL(url) + TOKEN + token;
		} catch (Exception e1) {
			appLogger.error(e1);
		}
		return "";
	}

}
