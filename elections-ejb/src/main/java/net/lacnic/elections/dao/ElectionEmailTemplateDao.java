package net.lacnic.elections.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import net.lacnic.elections.domain.ElectionEmailTemplate;

public class ElectionEmailTemplateDao {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");

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
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.TEMPLATE_TYPE, templateType.toUpperCase());
		return q.getSingleResult();
	}

	public String getSubjectByElectionTypeLanguageEN(Long electionId, String templateType) {
		TypedQuery<String> q;
		q = em.createQuery("SELECT t.subjectEN FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId AND t.templateType = :templateType", String.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.TEMPLATE_TYPE, templateType.toUpperCase());
		return q.getSingleResult();
	}

	public String getSubjectByElectionTypeLanguagePT(Long electionId, String templateType) {
		TypedQuery<String> q;
		q = em.createQuery("SELECT t.subjectPT FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId AND t.templateType = :templateType", String.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.TEMPLATE_TYPE, templateType.toUpperCase());
		return q.getSingleResult();
	}

	public String getBodyByElectionTypeLanguageSP(Long electionId, String templateType, String language) {
		TypedQuery<String> q;
		q = em.createQuery("SELECT t.bodySP FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId AND t.templateType = :templateType", String.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.TEMPLATE_TYPE, templateType.toUpperCase());
		return q.getSingleResult();
	}

	public String getBodyByElectionTypeLanguageEN(Long electionId, String templateType, String language) {
		TypedQuery<String> q;
		q = em.createQuery("SELECT t.bodyEN FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId AND t.templateType = :templateType", String.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.TEMPLATE_TYPE, templateType.toUpperCase());
		return q.getSingleResult();
	}

	public String getBodyByElectionTypeLanguagePT(Long electionId, String templateType, String language) {
		TypedQuery<String> q;
		q = em.createQuery("SELECT t.bodyPT FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId AND t.templateType = :templateType", String.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		q.setParameter(QueryParameterNames.TEMPLATE_TYPE, templateType.toUpperCase());
		return q.getSingleResult();
	}

	public List<ElectionEmailTemplate> getElectionTemplates(Long electionId) {
		TypedQuery<ElectionEmailTemplate> q = em.createQuery("SELECT t FROM ElectionEmailTemplate t WHERE t.election.electionId = :electionId", ElectionEmailTemplate.class);
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		return q.getResultList();
	}

	public List<ElectionEmailTemplate> getBaseTemplates() {
		TypedQuery<ElectionEmailTemplate> q = em.createQuery("SELECT t FROM ElectionEmailTemplate t WHERE t.election IS NULL", ElectionEmailTemplate.class);
		return q.getResultList();
	}

	public ElectionEmailTemplate getBaseTemplate(String templateType) {
		try {
			if (templateType == null || templateType.trim().isEmpty()) {
				return null;
			}
			TypedQuery<ElectionEmailTemplate> q = em.createQuery("SELECT t FROM ElectionEmailTemplate t WHERE t.templateType = :templateType and t.election IS NULL", ElectionEmailTemplate.class);
			q.setParameter(QueryParameterNames.TEMPLATE_TYPE, templateType.toUpperCase());
			q.setMaxResults(1);
			List<ElectionEmailTemplate> templates = q.getResultList();
			return templates.isEmpty() ? null : templates.get(0);
		} catch (Exception e) {
			appLogger.error("Error retrieving base template. templateType={}", templateType, e);
			return null;
		}
	}

	public ElectionEmailTemplate getElectionTemplateByType(String templateType, Long electionId) {
		TypedQuery<ElectionEmailTemplate> q = em.createQuery("SELECT t FROM ElectionEmailTemplate t WHERE t.templateType = :templateType and t.election.electionId = :electionId", ElectionEmailTemplate.class);
		q.setParameter(QueryParameterNames.TEMPLATE_TYPE, templateType.toUpperCase());
		q.setParameter(QueryParameterNames.ELECTION_ID, electionId);
		List<ElectionEmailTemplate> templates = q.getResultList();
		return templates.isEmpty() ? null : templates.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getElectionEmailTemplatesAllIdAndDescription(int pageSize, int offset) {
		Query q = em.createQuery("SELECT e.electionEmailTemplateId, e.templateType FROM ElectionEmailTemplate e ORDER BY e.electionEmailTemplateId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getElectionTemplatePairs() {
		Query q = em.createQuery("SELECT t.election.electionId, t.templateType FROM ElectionEmailTemplate t WHERE t.election IS NOT NULL");
		return q.getResultList();
	}

}
