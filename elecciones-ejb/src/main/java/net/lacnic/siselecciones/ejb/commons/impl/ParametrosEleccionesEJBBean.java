package net.lacnic.siselecciones.ejb.commons.impl;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.siselecciones.dao.DaoFactoryElecciones;
import net.lacnic.siselecciones.dominio.Parametro;
import net.lacnic.siselecciones.ejb.commons.ParametrosEleccionesEJB;
import net.lacnic.siselecciones.utils.Constantes;

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
		if (Constantes.getParametros().containsKey(key)) {
			String valor = Constantes.getParametros().get(key);
			if (!valor.isEmpty())
				return valor;
		}
		Parametro p = em.find(Parametro.class, key);
		if (p != null) {
			Constantes.getParametros().put(key, p.getValor());
			return p.getValor();
		}
		return "";
	}

	@Override
	public boolean isProd() {
		String app = obtenerParametro(Constantes.APP);
		if (app.isEmpty())
			return true;
		else 
			return app.equalsIgnoreCase("PROD");
	}

	@Override
	public List<Parametro> obtenerListadoParametro() {
		return DaoFactoryElecciones.createParametroDao(em).obtenerParametros();
	}

	@Override
	public boolean agregarParametro(String c, String v) {
		try {
			if (DaoFactoryElecciones.createParametroDao(em).getParametro(c) == null) {
				Parametro p = new Parametro();
				p.setClave(c);
				p.setValor(v);
				em.persist(p);
				Constantes.cleanCacheParametros();
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
	public void editarParametro(Parametro p) {
		try {
			em.merge(p);
			Constantes.cleanCacheParametros();
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	@Override
	public void borrarParametro(String c) {
		try {
			Parametro p = DaoFactoryElecciones.createParametroDao(em).getParametro(c);
			em.remove(p);
			Constantes.cleanCacheParametros();
		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}
