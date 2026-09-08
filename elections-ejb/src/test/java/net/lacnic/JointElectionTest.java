package net.lacnic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.JointElection;

 class JointElectionTest extends TestCase {
	/**
	 * @return the suite of tests being tested
	 */
	 static Test suite() {
		return new TestSuite(JointElectionTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testJointElection() {
		AssertAnnotations.assertType(JointElection.class, Entity.class);

		// fields
		AssertAnnotations.assertField(JointElection.class, "jointElectionId", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
		AssertAnnotations.assertField(JointElection.class, "idElectionA", Column.class);
		AssertAnnotations.assertField(JointElection.class, "idElectionB", Column.class);

		AssertAnnotations.assertMethod(JointElection.class, "getJointElectionId");
		AssertAnnotations.assertMethod(JointElection.class, "getIdElectionA");
		AssertAnnotations.assertMethod(JointElection.class, "getIdElectionB");

		// class annotations
		Entity a = ReflectTool.getClassAnnotation(JointElection.class, Entity.class);
		assertEquals("", a.name());

		Column c;
		c = ReflectTool.getFieldAnnotation(JointElection.class, "jointElectionId", Column.class);
		assertEquals("jointelection_id", c.name());
		c = ReflectTool.getFieldAnnotation(JointElection.class, "idElectionA", Column.class);
		assertEquals("electiona_id", c.name());
		c = ReflectTool.getFieldAnnotation(JointElection.class, "idElectionB", Column.class);
		assertEquals("electionb_id", c.name());

	}

}
