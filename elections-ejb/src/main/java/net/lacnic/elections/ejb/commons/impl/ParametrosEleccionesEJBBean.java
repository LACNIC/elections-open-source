package net.lacnic.elections.ejb.commons.impl;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.dao.DaoFactoryElecciones;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.ejb.commons.ParametrosEleccionesEJB;
import net.lacnic.elections.utils.Constants;

/**
 * Session Bean implementation class ParametrosBean
 */
@Stateless
@Remote(ParametrosEleccionesEJB.class)
public class ParametrosEleccionesEJBBean implements ParametrosEleccionesEJB {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	@PersistenceContext(unitName = "elecciones-pu")
	private EntityManager em;

	@Override
	public String obtenerParametro(String key) {
		if (Constants.getParameters().containsKey(key)) {
			String valor = Constants.getParameters().get(key);
			if (!valor.isEmpty())
				return valor;
		}
		Parameter p = em.find(Parameter.class, key);
		if (p != null) {
			Constants.getParameters().put(key, p.getValue());
			return p.getValue();
		}
		return "";
	}

	@Override
	public boolean isProd() {
		String app = obtenerParametro(Constants.APP);
		if (app.isEmpty())
			return true;
		else 
			return app.equalsIgnoreCase("PROD");
	}

	@Override
	public List<Parameter> obtenerListadoParametro() {
		return DaoFactoryElecciones.createParametroDao(em).obtenerParametros();
	}

	@Override
	public boolean agregarParametro(String c, String v) {
		try {
			if (DaoFactoryElecciones.createParametroDao(em).getParametro(c) == null) {
				Parameter p = new Parameter();
				p.setKey(c);
				p.setValue(v);
				em.persist(p);
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
	public void editarParametro(Parameter p) {
		try {
			em.merge(p);
			Constants.cleanParametersCache();
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void borrarParametro(String c) {
		try {
			Parameter p = DaoFactoryElecciones.createParametroDao(em).getParametro(c);
			em.remove(p);
			Constants.cleanParametersCache();
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}
