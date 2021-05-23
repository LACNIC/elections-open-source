package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.TemplateEleccion;

public class TemplateEleccionDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private static final String ID_ELECCION = "idEleccion";

	private EntityManager em;

	public TemplateEleccionDao(EntityManager em) {
		this.em = em;
	}

	public String obtenerAsuntoTemplateEleccionTipoIdioma(Long idEleccion, String tipo, String idioma) {
		Query q;
		if (idioma.equalsIgnoreCase("en")) {
			q = em.createQuery("SELECT t.asuntoen FROM TemplateEleccion t WHERE t.eleccion.idEleccion=:idEleccion AND t.tipoTemplate=:tipo");
		} else if (idioma.equalsIgnoreCase("pt")) {
			q = em.createQuery("SELECT t.asuntopt FROM TemplateEleccion t WHERE t.eleccion.idEleccion=:idEleccion AND t.tipoTemplate=:tipo");
		} else {
			q = em.createQuery("SELECT COALESCE(t.asuntoes, t.asunto_es) asuntoes FROM TemplateEleccion t WHERE t.eleccion.idEleccion=:idEleccion AND t.tipoTemplate=:tipo");
		}			
		q.setParameter(ID_ELECCION, idEleccion);
		q.setParameter("tipoTemplate", tipo.toUpperCase());
		return (String) q.getSingleResult();
	}

	public String obtenerCuerpoTemplateEleccionTipoIdioma(Long idEleccion, String tipo, String idioma) {
		Query q; 
		if (idioma.equalsIgnoreCase("en")) {
			q = em.createQuery("SELECT t.cuerpoen  FROM TemplateEleccion t WHERE t.eleccion.idEleccion=:idEleccion AND t.tipoTemplate=:tipo");
		} else if (idioma.equalsIgnoreCase("pt")) {
			q =em.createQuery("SELECT t.cuerpopt FROM TemplateEleccion t WHERE t.eleccion.idEleccion=:idEleccion AND t.tipoTemplate=:tipo");
		} else {
			q = em.createQuery("SELECT t.cuerpoes FROM TemplateEleccion t WHERE t.eleccion.idEleccion=:idEleccion AND t.tipoTemplate=:tipo");
		}		

		q.setParameter(ID_ELECCION, idEleccion);
		q.setParameter("tipoTemplate", tipo.toUpperCase());

		return (String) q.getSingleResult();
	}

	public List<TemplateEleccion> obtenerTemplatesEleccion(Long idEleccion) {
		TypedQuery<TemplateEleccion> q = em.createQuery("SELECT t FROM TemplateEleccion t WHERE t.eleccion.idEleccion=:idEleccion", TemplateEleccion.class);
		q.setParameter(ID_ELECCION, idEleccion);
		return q.getResultList();
	}

	public List<TemplateEleccion> obtenerTemplatesBase() {
		TypedQuery<TemplateEleccion> q = em.createQuery("SELECT t FROM TemplateEleccion t WHERE t.eleccion=NULL", TemplateEleccion.class);
		return q.getResultList();
	}

	public TemplateEleccion obtenerTemplateBase(String tipo) {
		try {
			TypedQuery<TemplateEleccion> q = em.createQuery("SELECT t FROM TemplateEleccion t WHERE t.tipoTemplate=:tipo and t.eleccion=NULL", TemplateEleccion.class);
			q.setParameter("tipo", tipo.toUpperCase());
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public TemplateEleccion obtenerTemplate(String tipo, Long idEleccion) {
		TypedQuery<TemplateEleccion> q = em.createQuery("SELECT t FROM TemplateEleccion t WHERE t.tipoTemplate=:tipo and t.eleccion.idEleccion=:idEleccion", TemplateEleccion.class);
		q.setParameter("tipo", tipo.toUpperCase());
		q.setParameter(ID_ELECCION, idEleccion);
		List<TemplateEleccion> templates = q.getResultList();
		return templates.isEmpty() ? null : templates.get(0);
	}

}
