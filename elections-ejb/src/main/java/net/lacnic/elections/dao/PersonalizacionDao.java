package net.lacnic.elections.dao;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.Personalizacion;

public class PersonalizacionDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;

	/**
	 * Constructor - asigna el entityManager
	 *  
	 */
	public PersonalizacionDao(EntityManager em) {
		this.em = em;
	}

	/**
	 * Obtiene la información de personalización
	 * 
	 * @return Retorna un objeto con la info de personalización
	 * 
	 */
	public Personalizacion getPersonalizacion() {
		try {
			TypedQuery<Personalizacion> q = em.createQuery("SELECT p FROM Personalizacion p WHERE p.idPersonalizacion = 1", Personalizacion.class);			
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

}
