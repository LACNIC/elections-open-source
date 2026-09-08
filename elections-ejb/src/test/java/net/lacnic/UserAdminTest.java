package net.lacnic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.UserAdmin;

 class UserAdminTest extends TestCase {
	/**
	 * @return the suite of tests being tested
	 */
	 static Test suite() {
		return new TestSuite(UserAdminTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testUserAdmin() {
		AssertAnnotations.assertType(UserAdmin.class, Entity.class);

		// fields
		AssertAnnotations.assertField(UserAdmin.class, "userAdminId", Id.class, Column.class);
		AssertAnnotations.assertField(UserAdmin.class, "password", Column.class);
		AssertAnnotations.assertField(UserAdmin.class, "email", Column.class);

		// metodos
		AssertAnnotations.assertMethod(UserAdmin.class, "getUserAdminId");
		AssertAnnotations.assertMethod(UserAdmin.class, "getPassword");
		AssertAnnotations.assertMethod(UserAdmin.class, "getEmail");

		// class annotations
		Entity a = ReflectTool.getClassAnnotation(UserAdmin.class, Entity.class);
		assertEquals("", a.name());

		Column c;
		c = ReflectTool.getFieldAnnotation(UserAdmin.class, "userAdminId", Column.class);
		assertEquals("useradmin_id", c.name());
		c = ReflectTool.getFieldAnnotation(UserAdmin.class, "password", Column.class);
		assertEquals("password", c.name());
		c = ReflectTool.getFieldAnnotation(UserAdmin.class, "email", Column.class);
		assertEquals("email", c.name());

	}

}
