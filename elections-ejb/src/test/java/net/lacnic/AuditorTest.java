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
       AssertAnnotations.assertField(Auditor.class, "idAuditor", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(Auditor.class, "idMigration", Column.class);
       AssertAnnotations.assertField(Auditor.class, "commissioner", Column.class);
       AssertAnnotations.assertField(Auditor.class, "agreement", Column.class);
       AssertAnnotations.assertField(Auditor.class, "revisionAvailable", Column.class);
       AssertAnnotations.assertField(Auditor.class, "resulttoke", Column.class);
       AssertAnnotations.assertField(Auditor.class, "name", Column.class);
       AssertAnnotations.assertField(Auditor.class, "election", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(Auditor.class, "mail", Column.class);

       //metodos       
       AssertAnnotations.assertMethod(Auditor.class, "getIdAuditor");
       AssertAnnotations.assertMethod(Auditor.class, "getIdMigration");
       AssertAnnotations.assertMethod(Auditor.class, "isCommissioner");
       AssertAnnotations.assertMethod(Auditor.class, "isAgreement");
       AssertAnnotations.assertMethod(Auditor.class, "getResulttoke");
       AssertAnnotations.assertMethod(Auditor.class, "isRevisionAvailable");
       AssertAnnotations.assertMethod(Auditor.class, "getName");
       AssertAnnotations.assertMethod(Auditor.class, "getElection");
       AssertAnnotations.assertMethod(Auditor.class, "getMail");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Auditor.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       JoinColumn jc;
       c = ReflectTool.getFieldAnnotation(Auditor.class, "idAuditor", Column.class);
       assertEquals("id_auditor", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "idMigration", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "commissioner", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "agreement", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "revisionAvailable", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "resulttoke", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "name", Column.class);
       assertEquals("", c.name());
       jc = ReflectTool.getFieldAnnotation(Auditor.class, "election", JoinColumn.class);
       assertEquals("id_election", jc.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "mail", Column.class);
       assertEquals("", c.name());
       
       
    }

}
