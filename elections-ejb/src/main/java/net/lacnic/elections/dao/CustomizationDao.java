package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.Customization;
import net.lacnic.elections.domain.Email;
import net.lacnic.elections.domain.IpAccess;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;


public class CustomizationDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;


	/**
	 * Constructor - Assigns the entityManager
	 *  
	 */
	public CustomizationDao(EntityManager em) {
		this.em = em;
	}
	
	public Customization getOneCustomization(Long customizationId) {
			TypedQuery<Customization> q = em.createQuery("SELECT c FROM Customization c WHERE c.customizationId = :customizationId", Customization.class);
			q.setParameter("customizationId", customizationId);
			return q.getSingleResult();
	}
	
	public List<Object[]> getAllCustomization() {
		Query q = em.createQuery("SELECT c.customizationId,c.siteTitle FROM Customization c order by c.customizationId");
		return q.getResultList();
	}
	
	
	/**
	 * Gets information about the customization
	 * 
	 * @return returns an entity with the  customization entity.
	 * 
	 */
	public Customization getCustomization() {
		try {
			TypedQuery<Customization> q = em.createQuery("SELECT c FROM Customization c WHERE c.customizationId = 1", Customization.class);
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

}
