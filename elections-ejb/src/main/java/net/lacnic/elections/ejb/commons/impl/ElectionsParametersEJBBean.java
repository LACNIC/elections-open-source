package net.lacnic.elections.ejb.commons.impl;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.dao.ElectionsDaoFactory;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.ejb.commons.ElectionsParametersEJB;
import net.lacnic.elections.utils.Constants;


/**
 * Session Bean implementation class ParametrosBean
 */
@Stateless
@Remote(ElectionsParametersEJB.class)
public class ElectionsParametersEJBBean implements ElectionsParametersEJB {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

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
	 * @param key
	 * 			Key of the parameter to look for.
	 * 
	 * @return returns a string with the value of the parameter, empty string if it does not exists.
	 */
	@Override
	public String getParameter(String key) {
		if (Constants.getParameters().containsKey(key)) {
			String value = Constants.getParameters().get(key);
			if (!value.isEmpty())
				return value;
		}
		Parameter parameter = em.find(Parameter.class, key);
		if (parameter != null) {
			Constants.getParameters().put(key, parameter.getValue());
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
	 * @param key
	 * 			Key of the new parameter
	 * @param  value
	 * 			Value of the new parameter
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
				Constants.cleanParametersCache();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			appLogger.error(e);
			return false;
		}
	}

	/**
	 * Updates the information of a parameter
	 * 
	 * @param parameter
	 * 				Entity with the information of the parameter to update.
	 */
	@Override
	public void editParameter(Parameter parameter) {
		try {
			em.merge(parameter);
			Constants.cleanParametersCache();
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	/**
	 * Deletes a parameter from the system
	 * 
	 * @param key
	 * 			Key of the parameter to delete.
	 */
	@Override
	public void deleteParameter(String key) {
		try {
			Parameter parameter = ElectionsDaoFactory.createParameterDao(em).getParameter(key);
			em.remove(parameter);
			Constants.cleanParametersCache();
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}
