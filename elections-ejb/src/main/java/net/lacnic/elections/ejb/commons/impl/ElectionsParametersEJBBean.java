package net.lacnic.elections.ejb.commons.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.ejb.commons.ElectionsParametersEJB;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.ElectionsCaches;

/**
 * Session Bean implementation class ParametrosBean
 */
@Stateless
@Remote(ElectionsParametersEJB.class)
public class ElectionsParametersEJBBean implements ElectionsParametersEJB {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

	@PersistenceContext(unitName = "elections-pu")
	private EntityManager em;

	/**
	 * Gets a list with all the parameters on the system
	 * 
	 * @return returns a collection of parameter entity with the information.
	 */
	@Override
	public List<Parameter> getParametersAll() {
		return ElectionsDaoFactory.createParameterDao(em).getParametersAll();
	}

	/**
	 * Gets the value of a parameter
	 * 
	 * @param key Key of the parameter to look for.
	 * 
	 * @return returns a string with the value of the parameter, empty string if it does not exists.
	 */
	@Override
	public String getParameter(String key) {
		String cachedValue = ElectionsCaches.getParametersCache().get(key);
		if (cachedValue != null && !cachedValue.isEmpty()) {
			return cachedValue;
		}
		Parameter parameter = em.find(Parameter.class, key);
		if (parameter != null) {
			ElectionsCaches.putParameter(key, parameter.getValue());
			return parameter.getValue();
		}
		return "";
	}

	/**
	 * Validates if the application is production
	 * 
	 * @return returns true if the APP parameter has value "PROD", false otherwise.
	 */
	@Override
	public boolean isProd() {
		String app = getParameter(Constants.APP);
		if (app.isEmpty())
			return true;
		else
			return app.equalsIgnoreCase("PROD");
	}

	/**
	 * Creates a new parameter on the system.
	 * 
	 * @param key   Key of the new parameter
	 * @param value Value of the new parameter
	 * 
	 * @return returns true if the parameter is added correctly, false if it already exists or there is an exception thrown.
	 */
	@Override
	public boolean addParameter(String key, String value) {
		try {
			if (ElectionsDaoFactory.createParameterDao(em).getParameter(key) == null) {
				Parameter parameter = new Parameter();
				parameter.setKey(key);
				parameter.setValue(value);
				em.persist(parameter);
				ElectionsCaches.clearParametersCache();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Updates the information of a parameter
	 * 
	 * @param parameter Entity with the information of the parameter to update.
	 */
	@Override
	public void editParameter(Parameter parameter) {
		try {
			em.merge(parameter);
			ElectionsCaches.clearParametersCache();
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	/**
	 * Deletes a parameter from the system
	 * 
	 * @param key Key of the parameter to delete.
	 */
	@Override
	public void deleteParameter(String key) {
		try {
			Parameter parameter = ElectionsDaoFactory.createParameterDao(em).getParameter(key);
			em.remove(parameter);
			ElectionsCaches.clearParametersCache();
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	/**
	 * Returns the data site key
	 */
	@Override
	public String getDataSiteKey() {
		try {
			String dataSiteKey = getParameter(Constants.DataSiteKeyReCaptcha);
			if (dataSiteKey != null)
				return dataSiteKey;
		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
		return "";
	}

}
