package net.lacnic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Election;

 class ElectionTest extends TestCase {
	/**
	 * @return the suite of tests being tested
	 */
	 static Test suite() {
		return new TestSuite(ElectionTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testElection() {
		AssertAnnotations.assertType(Election.class, Entity.class);

		// fields
		AssertAnnotations.assertField(Election.class, "electionId", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
		AssertAnnotations.assertField(Election.class, "migrationId", Column.class);
		AssertAnnotations.assertField(Election.class, "category", Column.class, Enumerated.class);
		AssertAnnotations.assertField(Election.class, "migrated", Column.class);
		AssertAnnotations.assertField(Election.class, "creationDate", Column.class);
		AssertAnnotations.assertField(Election.class, "titleSpanish", Column.class);
		AssertAnnotations.assertField(Election.class, "titleEnglish", Column.class);
		AssertAnnotations.assertField(Election.class, "titlePortuguese", Column.class);
		AssertAnnotations.assertField(Election.class, "linkSpanish", Column.class);
		AssertAnnotations.assertField(Election.class, "linkEnglish", Column.class);
		AssertAnnotations.assertField(Election.class, "linkPortuguese", Column.class);
		AssertAnnotations.assertField(Election.class, "descriptionSpanish", Column.class);
		AssertAnnotations.assertField(Election.class, "descriptionEnglish", Column.class);
		AssertAnnotations.assertField(Election.class, "descriptionPortuguese", Column.class);
		AssertAnnotations.assertField(Election.class, "callSpanish", Column.class);
		AssertAnnotations.assertField(Election.class, "callEnglish", Column.class);
		AssertAnnotations.assertField(Election.class, "callPortuguese", Column.class);
		AssertAnnotations.assertField(Election.class, "maxCandidates", Column.class);
		AssertAnnotations.assertField(Election.class, "votingLinkAvailable", Column.class);
		AssertAnnotations.assertField(Election.class, "resultLinkAvailable", Column.class);
		AssertAnnotations.assertField(Election.class, "auditorLinkAvailable", Column.class);
		AssertAnnotations.assertField(Election.class, "revisionRequest", Column.class);
		AssertAnnotations.assertField(Election.class, "onlySp", Column.class);
		AssertAnnotations.assertField(Election.class, "resultToken", Column.class);
		AssertAnnotations.assertField(Election.class, "defaultSender", Column.class);
		AssertAnnotations.assertField(Election.class, "authorizedSupportEmails", Column.class);
		AssertAnnotations.assertField(Election.class, "authorizedNominateEmails", Column.class);
		AssertAnnotations.assertField(Election.class, "electorsSet", Column.class);
		AssertAnnotations.assertField(Election.class, "candidatesSet", Column.class);
		AssertAnnotations.assertField(Election.class, "auditorsSet", Column.class);
		AssertAnnotations.assertField(Election.class, "callSet", Column.class);
		AssertAnnotations.assertField(Election.class, "randomOrderCandidates", Column.class);
		AssertAnnotations.assertField(Election.class, "diffUTC", Column.class);
		AssertAnnotations.assertField(Election.class, "candidates", OneToMany.class);
		AssertAnnotations.assertField(Election.class, "userVoters", OneToMany.class);
		AssertAnnotations.assertField(Election.class, "auditors", OneToMany.class);
		AssertAnnotations.assertField(Election.class, "electionTemplates", OneToMany.class);
		AssertAnnotations.assertField(Election.class, "votes", OneToMany.class);
		AssertAnnotations.assertField(Election.class, "email", OneToMany.class);
		AssertAnnotations.assertField(Election.class, "restrictedCountries", OneToMany.class);
		AssertAnnotations.assertField(Election.class, "closed", Column.class);
		AssertAnnotations.assertField(Election.class, "closedDate", Column.class);

		// metodos
		AssertAnnotations.assertMethod(Election.class, "getElectionId");
		AssertAnnotations.assertMethod(Election.class, "getMaxCandidates");
		AssertAnnotations.assertMethod(Election.class, "isVotingLinkAvailable");
		AssertAnnotations.assertMethod(Election.class, "getCandidates");
		AssertAnnotations.assertMethod(Election.class, "getCreationDate");
		AssertAnnotations.assertMethod(Election.class, "getUserVoters");
		AssertAnnotations.assertMethod(Election.class, "getVotes");
		AssertAnnotations.assertMethod(Election.class, "isResultLinkAvailable");
		AssertAnnotations.assertMethod(Election.class, "getTitleSpanish");
		AssertAnnotations.assertMethod(Election.class, "getTitleEnglish");
		AssertAnnotations.assertMethod(Election.class, "getTitlePortuguese");
		AssertAnnotations.assertMethod(Election.class, "getDescriptionSpanish");
		AssertAnnotations.assertMethod(Election.class, "getDescriptionEnglish");
		AssertAnnotations.assertMethod(Election.class, "getDescriptionPortuguese");
		AssertAnnotations.assertMethod(Election.class, "getCallSpanish");
		AssertAnnotations.assertMethod(Election.class, "getCallEnglish");
		AssertAnnotations.assertMethod(Election.class, "getCallPortuguese");
		AssertAnnotations.assertMethod(Election.class, "getResultToken");
		AssertAnnotations.assertMethod(Election.class, "getAuditors");
		AssertAnnotations.assertMethod(Election.class, "getLinkSpanish");
		AssertAnnotations.assertMethod(Election.class, "getLinkEnglish");
		AssertAnnotations.assertMethod(Election.class, "getLinkPortuguese");
		AssertAnnotations.assertMethod(Election.class, "isOnlySp");
		AssertAnnotations.assertMethod(Election.class, "isAuditorLinkAvailable");
		AssertAnnotations.assertMethod(Election.class, "isElectorsSet");
		AssertAnnotations.assertMethod(Election.class, "isCandidatesSet");
		AssertAnnotations.assertMethod(Election.class, "isAuditorsSet");
		AssertAnnotations.assertMethod(Election.class, "isCallSet");
		AssertAnnotations.assertMethod(Election.class, "getElectionTemplates");
		AssertAnnotations.assertMethod(Election.class, "getDefaultSender");
		AssertAnnotations.assertMethod(Election.class, "getAuthorizedSupportEmails");
		AssertAnnotations.assertMethod(Election.class, "getAuthorizedNominateEmails");
		AssertAnnotations.assertMethod(Election.class, "getResultLink");
		AssertAnnotations.assertMethod(Election.class, "isRandomOrderCandidates");
		AssertAnnotations.assertMethod(Election.class, "getDiffUTC");
		AssertAnnotations.assertMethod(Election.class, "isRevisionRequest");
		AssertAnnotations.assertMethod(Election.class, "isFinished");
		AssertAnnotations.assertMethod(Election.class, "isStarted");
		AssertAnnotations.assertMethod(Election.class, "isEnabledToVote");
		AssertAnnotations.assertMethod(Election.class, "isMigrated");
		AssertAnnotations.assertMethod(Election.class, "getCategory");
		AssertAnnotations.assertMethod(Election.class, "isClosed");
		AssertAnnotations.assertMethod(Election.class, "getClosedDate");

		// class annotations
		Entity a = ReflectTool.getClassAnnotation(Election.class, Entity.class);
		assertEquals("", a.name());

		Column c;
		OneToMany oa;
		c = ReflectTool.getFieldAnnotation(Election.class, "electionId", Column.class);
		assertEquals("election_id", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "migrationId", Column.class);
		assertEquals("migration_id", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "category", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "migrated", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "creationDate", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "titleSpanish", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "titleEnglish", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "titlePortuguese", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "linkSpanish", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "linkEnglish", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "linkPortuguese", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "descriptionSpanish", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "descriptionEnglish", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "descriptionPortuguese", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "callSpanish", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "callEnglish", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "callPortuguese", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "maxCandidates", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "votingLinkAvailable", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "resultLinkAvailable", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "auditorLinkAvailable", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "revisionRequest", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "onlySp", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "resultToken", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "defaultSender", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "authorizedSupportEmails", Column.class);
		assertEquals("authorized_support_emails", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "authorizedNominateEmails", Column.class);
		assertEquals("authorized_nominate_emails", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "electorsSet", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "candidatesSet", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "auditorsSet", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "callSet", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "randomOrderCandidates", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "diffUTC", Column.class);
		assertEquals("", c.name());
		oa = ReflectTool.getFieldAnnotation(Election.class, "candidates", OneToMany.class);
		assertEquals("election", oa.mappedBy());
		oa = ReflectTool.getFieldAnnotation(Election.class, "auditors", OneToMany.class);
		assertEquals("election", oa.mappedBy());
		oa = ReflectTool.getFieldAnnotation(Election.class, "userVoters", OneToMany.class);
		assertEquals("election", oa.mappedBy());
		oa = ReflectTool.getFieldAnnotation(Election.class, "electionTemplates", OneToMany.class);
		assertEquals("election", oa.mappedBy());
		oa = ReflectTool.getFieldAnnotation(Election.class, "votes", OneToMany.class);
		assertEquals("election", oa.mappedBy());
		oa = ReflectTool.getFieldAnnotation(Election.class, "email", OneToMany.class);
		assertEquals("election", oa.mappedBy());
		oa = ReflectTool.getFieldAnnotation(Election.class, "restrictedCountries", OneToMany.class);
		assertEquals("election", oa.mappedBy());
		c = ReflectTool.getFieldAnnotation(Election.class, "closed", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Election.class, "closedDate", Column.class);
		assertEquals("", c.name());

	}

}
