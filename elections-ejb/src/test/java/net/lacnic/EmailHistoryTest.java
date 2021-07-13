package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.EmailHistory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

public class EmailHistoryTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( EmailHistoryTest.class );
    }
    
    public void testEmailHistory()
    {
    	AssertAnnotations.assertType(EmailHistory.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(EmailHistory.class, "emailHistoryId", Id.class, Column.class);
       AssertAnnotations.assertField(EmailHistory.class, "recipients", Column.class);
       AssertAnnotations.assertField(EmailHistory.class, "sender", Column.class);
       AssertAnnotations.assertField(EmailHistory.class, "cc", Column.class);
       AssertAnnotations.assertField(EmailHistory.class, "bcc", Column.class);
       AssertAnnotations.assertField(EmailHistory.class, "subject", Column.class);
       AssertAnnotations.assertField(EmailHistory.class, "body", Column.class);
       AssertAnnotations.assertField(EmailHistory.class, "sent", Column.class);
       AssertAnnotations.assertField(EmailHistory.class, "createdDate", Column.class);
       AssertAnnotations.assertField(EmailHistory.class, "templateType", Column.class);
       AssertAnnotations.assertField(EmailHistory.class, "electionId", Column.class);
       
     //metodos       
       AssertAnnotations.assertMethod(EmailHistory.class, "getEmailHistoryId");
       AssertAnnotations.assertMethod(EmailHistory.class, "getRecipients");
       AssertAnnotations.assertMethod(EmailHistory.class, "getCc");
       AssertAnnotations.assertMethod(EmailHistory.class, "getBcc");
       AssertAnnotations.assertMethod(EmailHistory.class, "getSubject");
       AssertAnnotations.assertMethod(EmailHistory.class, "getBody");
       AssertAnnotations.assertMethod(EmailHistory.class, "getSent");
       AssertAnnotations.assertMethod(EmailHistory.class, "getSender");
       AssertAnnotations.assertMethod(EmailHistory.class, "getElectionId");
       AssertAnnotations.assertMethod(EmailHistory.class, "getTemplateType");
       AssertAnnotations.assertMethod(EmailHistory.class, "getCreatedDate");
 
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(EmailHistory.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;       
       c = ReflectTool.getFieldAnnotation(EmailHistory.class, "emailHistoryId", Column.class);
       assertEquals("emailhistory_id", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistory.class, "recipients", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistory.class, "sender", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistory.class, "cc", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistory.class, "bcc", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistory.class, "subject", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistory.class, "body", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistory.class, "sent", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistory.class, "createdDate", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistory.class, "templateType", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistory.class, "electionId", Column.class);
       assertEquals("election_id", c.name());
    }

}
