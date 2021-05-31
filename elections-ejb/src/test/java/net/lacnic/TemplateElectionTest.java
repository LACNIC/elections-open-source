package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.ElectionEmailTemplate;

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
    	AssertAnnotations.assertType(ElectionEmailTemplate.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(ElectionEmailTemplate.class, "idTemplate", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(ElectionEmailTemplate.class, "election", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(ElectionEmailTemplate.class, "templateType", Column.class);
       AssertAnnotations.assertField(ElectionEmailTemplate.class, "subjectSP", Column.class);
       AssertAnnotations.assertField(ElectionEmailTemplate.class, "subjectPT", Column.class);
       AssertAnnotations.assertField(ElectionEmailTemplate.class, "subjectEN", Column.class);
       AssertAnnotations.assertField(ElectionEmailTemplate.class, "bodySP", Column.class);
       AssertAnnotations.assertField(ElectionEmailTemplate.class, "bodyEN", Column.class);
       AssertAnnotations.assertField(ElectionEmailTemplate.class, "bodyPT", Column.class);
       AssertAnnotations.assertField(ElectionEmailTemplate.class, "recipientType", Transient.class);
       
       //metodos       
       AssertAnnotations.assertMethod(ElectionEmailTemplate.class, "getIdTemplate");
       AssertAnnotations.assertMethod(ElectionEmailTemplate.class, "getElection");
       AssertAnnotations.assertMethod(ElectionEmailTemplate.class, "getTemplateType");
       AssertAnnotations.assertMethod(ElectionEmailTemplate.class, "getSubjectSP");
       AssertAnnotations.assertMethod(ElectionEmailTemplate.class, "getSubjectEN");
       AssertAnnotations.assertMethod(ElectionEmailTemplate.class, "getSubjectPT");
       AssertAnnotations.assertMethod(ElectionEmailTemplate.class, "getBodySP");
       AssertAnnotations.assertMethod(ElectionEmailTemplate.class, "getBodyEN");
       AssertAnnotations.assertMethod(ElectionEmailTemplate.class, "getBodyPT");
       AssertAnnotations.assertMethod(ElectionEmailTemplate.class, "getRecipientType");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(ElectionEmailTemplate.class, Entity.class);
       assertEquals("", a.name());
       
       Column c;
       JoinColumn jc;
       c = ReflectTool.getFieldAnnotation(ElectionEmailTemplate.class, "idTemplate", Column.class);
       assertEquals("id_template_election", c.name());
       jc = ReflectTool.getFieldAnnotation(ElectionEmailTemplate.class, "election", JoinColumn.class);
       assertEquals("id_election", jc.name());
       c = ReflectTool.getFieldAnnotation(ElectionEmailTemplate.class, "templateType", Column.class);
       assertEquals("type", c.name());
       c = ReflectTool.getFieldAnnotation(ElectionEmailTemplate.class, "subjectSP", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(ElectionEmailTemplate.class, "subjectEN", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(ElectionEmailTemplate.class, "subjectPT", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(ElectionEmailTemplate.class, "bodySP", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(ElectionEmailTemplate.class, "bodyEN", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(ElectionEmailTemplate.class, "bodyPT", Column.class);
       assertEquals("", c.name());
       
       
       
    }

}
