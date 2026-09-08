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
import net.lacnic.elections.domain.Email;

 class EmailTest extends TestCase {
	/**
	 * @return the suite of tests being tested
	 */
	 static Test suite() {
		return new TestSuite(EmailTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testEmail() {
		AssertAnnotations.assertType(Email.class, Entity.class);

		// fields
		AssertAnnotations.assertField(Email.class, "emailId", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
		AssertAnnotations.assertField(Email.class, "recipients", Column.class);
		AssertAnnotations.assertField(Email.class, "sender", Column.class);
		AssertAnnotations.assertField(Email.class, "cc", Column.class);
		AssertAnnotations.assertField(Email.class, "bcc", Column.class);
		AssertAnnotations.assertField(Email.class, "subject", Column.class);
		AssertAnnotations.assertField(Email.class, "body", Column.class);
		AssertAnnotations.assertField(Email.class, "sent", Column.class);
		AssertAnnotations.assertField(Email.class, "createdDate", Column.class);
		AssertAnnotations.assertField(Email.class, "templateType", Column.class);
		AssertAnnotations.assertField(Email.class, "election", JoinColumn.class, ManyToOne.class);

		// metodos
		AssertAnnotations.assertMethod(Email.class, "getEmailId");
		AssertAnnotations.assertMethod(Email.class, "getRecipients");
		AssertAnnotations.assertMethod(Email.class, "getCc");
		AssertAnnotations.assertMethod(Email.class, "getBcc");
		AssertAnnotations.assertMethod(Email.class, "getSubject");
		AssertAnnotations.assertMethod(Email.class, "getBody");
		AssertAnnotations.assertMethod(Email.class, "getSent");
		AssertAnnotations.assertMethod(Email.class, "getSender");
		AssertAnnotations.assertMethod(Email.class, "getElection");
		AssertAnnotations.assertMethod(Email.class, "getTemplateType");
		AssertAnnotations.assertMethod(Email.class, "getCreatedDate");

		// class annotations
		Entity a = ReflectTool.getClassAnnotation(Email.class, Entity.class);
		assertEquals("", a.name());

		Column c;
		JoinColumn jc;
		c = ReflectTool.getFieldAnnotation(Email.class, "emailId", Column.class);
		assertEquals("email_id", c.name());
		c = ReflectTool.getFieldAnnotation(Email.class, "recipients", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Email.class, "sender", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Email.class, "cc", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Email.class, "bcc", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Email.class, "subject", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Email.class, "body", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Email.class, "sent", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Email.class, "createdDate", Column.class);
		assertEquals("", c.name());
		c = ReflectTool.getFieldAnnotation(Email.class, "templateType", Column.class);
		assertEquals("", c.name());
		jc = ReflectTool.getFieldAnnotation(Email.class, "election", JoinColumn.class);
		assertEquals("election_id", jc.name());

	}

}
