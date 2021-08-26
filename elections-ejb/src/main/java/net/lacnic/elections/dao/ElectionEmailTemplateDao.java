package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.ElectionEmailTemplate;


public class ElectionEmailTemplateDao {

	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;


	public ElectionEmailTemplateDao(EntityManager em) {
		this.em = em;
	}

	public ElectionEmailTemplate getElectionEmailTemplate(Long electionEmailTemplateId) {
		TypedQuery<ElectionEmailTemplate> q = em.createQuery("SELECT e FROM ElectionEmailTemplate e WHERE e.electionEmailTemplateId = :electionEmailTemplateId", ElectionEmailTemplate.class);
		q.setParameter("electionEmailTemplateId", electionEmailTemplateId);
		return q.getSingleResult();
	}
	
	public String getSubjectByElectionTypeLanguageSP(Long electionId, String templateType) {
		TypedQuery<String> q;
		q = em.createQuery("SELECT t.subjectSP FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId AND t.templateType = :templateType", String.class);
		q.setParameter("electionId", electionId);
		q.setParameter("templateType", templateType.toUpperCase());
		return q.getSingleResult();
	}
	
	public String getSubjectByElectionTypeLanguageEN(Long electionId, String templateType) {
		TypedQuery<String> q;
		q = em.createQuery("SELECT t.subjectEN FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId AND t.templateType = :templateType", String.class);
		q.setParameter("electionId", electionId);
		q.setParameter("templateType", templateType.toUpperCase());
		return q.getSingleResult();
	}
	
	public String getSubjectByElectionTypeLanguagePT(Long electionId, String templateType) {
		TypedQuery<String> q;
		q = em.createQuery("SELECT t.subjectPT FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId AND t.templateType = :templateType", String.class);
		q.setParameter("electionId", electionId);
		q.setParameter("templateType", templateType.toUpperCase());
		return q.getSingleResult();
	}

	public String getBodyByElectionTypeLanguageSP(Long electionId, String templateType, String language) {
		TypedQuery<String> q;		
		q = em.createQuery("SELECT t.bodySP FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId AND t.templateType = :templateType", String.class);
		q.setParameter("electionId", electionId);
		q.setParameter("templateType", templateType.toUpperCase());
		return q.getSingleResult();
	}
	
	public String getBodyByElectionTypeLanguageEN(Long electionId, String templateType, String language) {
		TypedQuery<String> q;		
		q = em.createQuery("SELECT t.bodyEN FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId AND t.templateType = :templateType", String.class);
		q.setParameter("electionId", electionId);
		q.setParameter("templateType", templateType.toUpperCase());
		return q.getSingleResult();
	}
	
	public String getBodyByElectionTypeLanguagePT(Long electionId, String templateType, String language) {
		TypedQuery<String> q;		
		q = em.createQuery("SELECT t.bodyPT FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId AND t.templateType = :templateType", String.class);
		q.setParameter("electionId", electionId);
		q.setParameter("templateType", templateType.toUpperCase());
		return q.getSingleResult();
	}
		
	public List<ElectionEmailTemplate> getElectionTemplates(Long electionId) {
		TypedQuery<ElectionEmailTemplate> q = em.createQuery("SELECT t FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId", ElectionEmailTemplate.class);
		q.setParameter("electionId", electionId);
		return q.getResultList();
	}

	public List<ElectionEmailTemplate> getBaseTemplates() {
		TypedQuery<ElectionEmailTemplate> q = em.createQuery("SELECT t FROM ElectionEmailTemplate t WHERE t.election = NULL", ElectionEmailTemplate.class);
		return q.getResultList();
	}

	public ElectionEmailTemplate getBaseTemplate(String templateType) {
		try {
			TypedQuery<ElectionEmailTemplate> q = em.createQuery("SELECT t FROM ElectionEmailTemplate t WHERE t.templateType = :templateType and t.election = NULL", ElectionEmailTemplate.class);
			q.setParameter("templateType", templateType.toUpperCase());
			return q.getSingleResult();
		} catch (Exception e) {
			appLogger.error(e);
			return null;
		}
	}

	public ElectionEmailTemplate getElectionTemplateByType(String templateType, Long electionId) {
		TypedQuery<ElectionEmailTemplate> q = em.createQuery("SELECT t FROM ElectionEmailTemplate t WHERE t.templateType = :templateType and t.election.electionId = :electionId", ElectionEmailTemplate.class);
		q.setParameter("templateType", templateType.toUpperCase());
		q.setParameter("electionId", electionId);
		List<ElectionEmailTemplate> templates = q.getResultList();
		return templates.isEmpty() ? null : templates.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getElectionEmailTemplatesAllIdAndDescription() {
		Query q = em.createQuery("SELECT e.electionEmailTemplateId, e.templateType FROM ElectionEmailTemplate e ORDER BY e.electionEmailTemplateId");
		return q.getResultList();
	}

}
