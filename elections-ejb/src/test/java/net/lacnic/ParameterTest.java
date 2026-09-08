package net.lacnic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Parameter;

 class ParameterTest extends TestCase {
	/**
	 * @return the suite of tests being tested
	 */
	 static Test suite() {
		return new TestSuite(ParameterTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testParameter() {
		AssertAnnotations.assertType(Parameter.class, Entity.class);

		// fields
		AssertAnnotations.assertField(Parameter.class, "key", Id.class);
		AssertAnnotations.assertField(Parameter.class, "value", Column.class);

		// metodos
		AssertAnnotations.assertMethod(Parameter.class, "getKey");
		AssertAnnotations.assertMethod(Parameter.class, "getValue");

		// class annotations
		Entity a = ReflectTool.getClassAnnotation(Parameter.class, Entity.class);
		assertEquals("", a.name());

		Column c;
		c = ReflectTool.getFieldAnnotation(Parameter.class, "value", Column.class);
		assertEquals("", c.name());
	}
}
