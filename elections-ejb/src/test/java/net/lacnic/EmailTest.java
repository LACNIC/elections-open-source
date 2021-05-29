package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Email;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

public class EmailTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( EmailTest.class );
    }
    
    public void testEmail()
    {
    	AssertAnnotations.assertType(Email.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Email.class, "id", Id.class, GeneratedValue.class, SequenceGenerator.class);
       AssertAnnotations.assertField(Email.class, "recipients", Column.class);
       AssertAnnotations.assertField(Email.class, "emailFrom", Column.class);
       AssertAnnotations.assertField(Email.class, "cc", Column.class);
       AssertAnnotations.assertField(Email.class, "bcc", Column.class);
       AssertAnnotations.assertField(Email.class, "subject", Column.class);
       AssertAnnotations.assertField(Email.class, "body", Column.class);
       AssertAnnotations.assertField(Email.class, "sent", Column.class);
       AssertAnnotations.assertField(Email.class, "createdDate", Column.class);
       AssertAnnotations.assertField(Email.class, "templateType", Column.class);
       AssertAnnotations.assertField(Email.class, "election", JoinColumn.class, ManyToOne.class);

       //metodos       
       AssertAnnotations.assertMethod(Email.class, "getId");
       AssertAnnotations.assertMethod(Email.class, "getRecipients");
       AssertAnnotations.assertMethod(Email.class, "getCc");
       AssertAnnotations.assertMethod(Email.class, "getBcc");
       AssertAnnotations.assertMethod(Email.class, "getSubject");
       AssertAnnotations.assertMethod(Email.class, "getBody");
       AssertAnnotations.assertMethod(Email.class, "getSent");
       AssertAnnotations.assertMethod(Email.class, "getEmailFrom");
       AssertAnnotations.assertMethod(Email.class, "getElection");
       AssertAnnotations.assertMethod(Email.class, "getTemplateType");
       AssertAnnotations.assertMethod(Email.class, "getCreatedDate");
 
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Email.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       SequenceGenerator sg;
       JoinColumn jc;
       sg = ReflectTool.getFieldAnnotation(Email.class, "id", SequenceGenerator.class);
       assertEquals("emaile_seq", sg.name());
       c = ReflectTool.getFieldAnnotation(Email.class, "recipients", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Email.class, "emailFrom", Column.class);
       assertEquals("email_from", c.name());
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
       assertEquals("id_election", jc.name());
       
    }
    

}
