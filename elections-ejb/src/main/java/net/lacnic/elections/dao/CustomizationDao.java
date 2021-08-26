package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.Customization;


public class CustomizationDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;


	public CustomizationDao(EntityManager em) {
		this.em = em;
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

	public Customization getCustomizationById(Long customizationId) {
		TypedQuery<Customization> q = em.createQuery("SELECT c FROM Customization c WHERE c.customizationId = :customizationId", Customization.class);
		q.setParameter("customizationId", customizationId);
		return q.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getCustomizationsAllIdAndDescription() {
		Query q = em.createQuery("SELECT c.customizationId, c.siteTitle FROM Customization c ORDER BY c.customizationId");
		return q.getResultList();
	}

}
