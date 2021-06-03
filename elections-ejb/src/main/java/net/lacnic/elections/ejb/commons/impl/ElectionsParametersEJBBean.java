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


	@Override
	public List<Parameter> getParametersAll() {
		return ElectionsDaoFactory.createParameterDao(em).getParametersAll();
	}

	@Override
	public String getParameter(String key) {
		if (Constants.getParameters().containsKey(key)) {
			String value = Constants.getParameters().get(key);
			if (!value.isEmpty())
				return value;
		}
		Parameter oarameter = em.find(Parameter.class, key);
		if (oarameter != null) {
			Constants.getParameters().put(key, oarameter.getValue());
			return oarameter.getValue();
		}
		return "";
	}

	@Override
	public boolean isProd() {
		String app = getParameter(Constants.APP);
		if (app.isEmpty())
			return true;
		else 
			return app.equalsIgnoreCase("PROD");
	}

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

	@Override
	public void editParameter(Parameter oarameter) {
		try {
			em.merge(oarameter);
			Constants.cleanParametersCache();
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

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
