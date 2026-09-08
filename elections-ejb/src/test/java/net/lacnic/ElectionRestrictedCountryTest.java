package net.lacnic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.ElectionRestrictedCountry;

 class ElectionRestrictedCountryTest extends TestCase {
	/**
	 * @return the suite of tests being tested
	 */
	 static Test suite() {
		return new TestSuite(ElectionRestrictedCountryTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testElectionRestrictedCountry() {
		AssertAnnotations.assertType(ElectionRestrictedCountry.class, Entity.class);

		// fields
		AssertAnnotations.assertField(ElectionRestrictedCountry.class, "id", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
		AssertAnnotations.assertField(ElectionRestrictedCountry.class, "election", ManyToOne.class, JoinColumn.class);
		AssertAnnotations.assertField(ElectionRestrictedCountry.class, "countryCode", Column.class);

		// methods
		AssertAnnotations.assertMethod(ElectionRestrictedCountry.class, "getId");
		AssertAnnotations.assertMethod(ElectionRestrictedCountry.class, "getElection");
		AssertAnnotations.assertMethod(ElectionRestrictedCountry.class, "getCountryCode");

		// class annotations
		Entity a = ReflectTool.getClassAnnotation(ElectionRestrictedCountry.class, Entity.class);
		assertEquals("", a.name());

		Column c;
		JoinColumn jc;
		ManyToOne mto;

		c = ReflectTool.getFieldAnnotation(ElectionRestrictedCountry.class, "id", Column.class);
		assertEquals("id", c.name());

		jc = ReflectTool.getFieldAnnotation(ElectionRestrictedCountry.class, "election", JoinColumn.class);
		assertEquals("election_id", jc.name());
		assertEquals(false, jc.nullable());

		mto = ReflectTool.getFieldAnnotation(ElectionRestrictedCountry.class, "election", ManyToOne.class);
		assertEquals(false, mto.optional());

		c = ReflectTool.getFieldAnnotation(ElectionRestrictedCountry.class, "countryCode", Column.class);
		assertEquals("", c.name());
		assertEquals(false, c.nullable());
		assertEquals(3, c.length());
	}
}
