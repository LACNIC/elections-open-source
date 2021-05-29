package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.TemplateElection;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

public class TemplateElectionTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TemplateElectionTest.class );
    }
    
    public void testTemplateElection()
    {
    	AssertAnnotations.assertType(TemplateElection.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(TemplateElection.class, "idTemplate", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(TemplateElection.class, "election", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(TemplateElection.class, "templateType", Column.class);
       AssertAnnotations.assertField(TemplateElection.class, "subjectSP", Column.class);
       AssertAnnotations.assertField(TemplateElection.class, "subjectPT", Column.class);
       AssertAnnotations.assertField(TemplateElection.class, "subjectEN", Column.class);
       AssertAnnotations.assertField(TemplateElection.class, "bodySP", Column.class);
       AssertAnnotations.assertField(TemplateElection.class, "bodyEN", Column.class);
       AssertAnnotations.assertField(TemplateElection.class, "bodyPT", Column.class);
       AssertAnnotations.assertField(TemplateElection.class, "recipientType", Transient.class);
       
       //metodos       
       AssertAnnotations.assertMethod(TemplateElection.class, "getIdTemplate");
       AssertAnnotations.assertMethod(TemplateElection.class, "getElection");
       AssertAnnotations.assertMethod(TemplateElection.class, "getTemplateType");
       AssertAnnotations.assertMethod(TemplateElection.class, "getSubjectSP");
       AssertAnnotations.assertMethod(TemplateElection.class, "getSubjectEN");
       AssertAnnotations.assertMethod(TemplateElection.class, "getSubjectPT");
       AssertAnnotations.assertMethod(TemplateElection.class, "getBodySP");
       AssertAnnotations.assertMethod(TemplateElection.class, "getBodyEN");
       AssertAnnotations.assertMethod(TemplateElection.class, "getBodyPT");
       AssertAnnotations.assertMethod(TemplateElection.class, "getRecipientType");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(TemplateElection.class, Entity.class);
       assertEquals("", a.name());
       
       Column c;
       JoinColumn jc;
       c = ReflectTool.getFieldAnnotation(TemplateElection.class, "idTemplate", Column.class);
       assertEquals("id_template_election", c.name());
       jc = ReflectTool.getFieldAnnotation(TemplateElection.class, "election", JoinColumn.class);
       assertEquals("id_election", jc.name());
       c = ReflectTool.getFieldAnnotation(TemplateElection.class, "templateType", Column.class);
       assertEquals("type", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateElection.class, "subjectSP", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateElection.class, "subjectEN", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateElection.class, "subjectPT", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateElection.class, "bodySP", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateElection.class, "bodyEN", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateElection.class, "bodyPT", Column.class);
       assertEquals("", c.name());
       
       
       
    }

}
