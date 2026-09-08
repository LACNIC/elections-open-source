package net.lacnic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Transient;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.UserVoter;

 class UserVoterTest extends TestCase {
	/**
	 * @return the suite of tests being tested
	 */
	 static Test suite() {
		return new TestSuite(UserVoterTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testUserVoter() {
		AssertAnnotations.assertType(UserVoter.class, Entity.class);

		// fields
		AssertAnnotations.assertField(UserVoter.class, "userVoterId", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
		AssertAnnotations.assertField(UserVoter.class, "migrationId", Column.class);
		AssertAnnotations.assertField(UserVoter.class, "election", JoinColumn.class, ManyToOne.class);
		AssertAnnotations.assertField(UserVoter.class, "voted", Column.class);
		AssertAnnotations.assertField(UserVoter.class, "voteToken", Column.class);
		AssertAnnotations.assertField(UserVoter.class, "voteAmount", Column.class);
		AssertAnnotations.assertField(UserVoter.class, "name", Column.class);
		AssertAnnotations.assertField(UserVoter.class, "mail", Column.class);
		AssertAnnotations.assertField(UserVoter.class, "country", Column.class);
		AssertAnnotations.assertField(UserVoter.class, "language", Column.class);
		AssertAnnotations.assertField(UserVoter.class, "orgID", Column.class);
		AssertAnnotations.assertField(UserVoter.class, "voteDate", Column.class);
		AssertAnnotations.assertField(UserVoter.class, "votes", OneToMany.class);
		AssertAnnotations.assertField(UserVoter.class, "codesSummary", Transient.class);

		// metodos
		AssertAnnotations.assertMethod(UserVoter.class, "getUserVoterId");
		AssertAnnotations.assertMethod(UserVoter.class, "getName");
		AssertAnnotations.assertMethod(UserVoter.class, "getMail");
		AssertAnnotations.assertMethod(UserVoter.class, "getCountry");
		AssertAnnotations.assertMethod(UserVoter.class, "getLanguage");
		AssertAnnotations.assertMethod(UserVoter.class, "getOrgID");
		AssertAnnotations.assertMethod(UserVoter.class, "isVoted");
		AssertAnnotations.assertMethod(UserVoter.class, "getVoteToken");
		AssertAnnotations.assertMethod(UserVoter.class, "getVoteAmount");
		AssertAnnotations.assertMethod(UserVoter.class, "getElection");
		AssertAnnotations.assertMethod(UserVoter.class, "getVoterInformation");
		AssertAnnotations.assertMethod(UserVoter.class, "getCompleteVoterInformation");
		AssertAnnotations.assertMethod(UserVoter.class, "getCodesSummary");
		AssertAnnotations.assertMethod(UserVoter.class, "getVoteLink");
		AssertAnnotations.assertMethod(UserVoter.class, "getVoteDate");
		AssertAnnotations.assertMethod(UserVoter.class, "getMigrationId");

		// class annotations
		Entity a = ReflectTool.getClassAnnotation(UserVoter.class, Entity.class);
		assertEquals("", a.name());

		Column c;
		JoinColumn jc;
		OneToMany oa;
		c = ReflectTool.getFieldAnnotation(UserVoter.class, "userVoterId", Column.class);
		assertEquals("uservoter_id", c.name());
		c = ReflectTool.getFieldAnnotation(UserVoter.class, "migrationId", Column.class);
		assertEquals("migration_id", c.name());
		jc = ReflectTool.getFieldAnnotation(UserVoter.class, "election", JoinColumn.class);
		assertEquals("election_id", jc.name());
		c = ReflectTool.getFieldAnnotation(UserVoter.class, "voted", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(UserVoter.class, "voteToken", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(UserVoter.class, "voteAmount", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(UserVoter.class, "name", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(UserVoter.class, "mail", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(UserVoter.class, "country", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(UserVoter.class, "language", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(UserVoter.class, "orgID", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(UserVoter.class, "voteDate", Column.class);
		assertEquals("", c.name());
		oa = ReflectTool.getFieldAnnotation(UserVoter.class, "votes", OneToMany.class);
		assertEquals("userVoter", oa.mappedBy());

	}

}
