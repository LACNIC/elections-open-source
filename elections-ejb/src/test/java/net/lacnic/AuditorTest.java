package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Auditor;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

public class AuditorTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AuditorTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(Auditor.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Auditor.class, "auditorId", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(Auditor.class, "migrationId", Column.class);
       AssertAnnotations.assertField(Auditor.class, "commissioner", Column.class);
       AssertAnnotations.assertField(Auditor.class, "agreedConformity", Column.class);
       AssertAnnotations.assertField(Auditor.class, "revisionAvailable", Column.class);
       AssertAnnotations.assertField(Auditor.class, "resultToken", Column.class);
       AssertAnnotations.assertField(Auditor.class, "name", Column.class);
       AssertAnnotations.assertField(Auditor.class, "election", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(Auditor.class, "mail", Column.class);

       //metodos       
       AssertAnnotations.assertMethod(Auditor.class, "getAuditorId");
       AssertAnnotations.assertMethod(Auditor.class, "getMigrationId");
       AssertAnnotations.assertMethod(Auditor.class, "isCommissioner");
       AssertAnnotations.assertMethod(Auditor.class, "isAgreedConformity");
       AssertAnnotations.assertMethod(Auditor.class, "getResultToken");
       AssertAnnotations.assertMethod(Auditor.class, "isRevisionAvailable");
       AssertAnnotations.assertMethod(Auditor.class, "getName");
       AssertAnnotations.assertMethod(Auditor.class, "getElection");
       AssertAnnotations.assertMethod(Auditor.class, "getMail");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Auditor.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       JoinColumn jc;
       c = ReflectTool.getFieldAnnotation(Auditor.class, "auditorId", Column.class);
       assertEquals("auditor_id", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "migrationId", Column.class);
       assertEquals("migration_id", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "commissioner", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "agreedConformity", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "revisionAvailable", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "resultToken", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "name", Column.class);
       assertEquals("", c.name());
       jc = ReflectTool.getFieldAnnotation(Auditor.class, "election", JoinColumn.class);
       assertEquals("election_id", jc.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "mail", Column.class);
       assertEquals("", c.name());
       
       
    }

}
