package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import net.lacnic.siselecciones.dominio.Auditor;

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
       AssertAnnotations.assertField(Auditor.class, "idMigracion", Column.class);
       AssertAnnotations.assertField(Auditor.class, "comisionado", Column.class);
       AssertAnnotations.assertField(Auditor.class, "expresoConformidad", Column.class);
       AssertAnnotations.assertField(Auditor.class, "habilitaRevision", Column.class);
       AssertAnnotations.assertField(Auditor.class, "tokenResultado", Column.class);
       AssertAnnotations.assertField(Auditor.class, "nombre", Column.class);
       AssertAnnotations.assertField(Auditor.class, "eleccion", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(Auditor.class, "mail", Column.class);

       //metodos       
       AssertAnnotations.assertMethod(Auditor.class, "getIdAuditor");
       AssertAnnotations.assertMethod(Auditor.class, "getIdMigracion");
       AssertAnnotations.assertMethod(Auditor.class, "isComisionado");
       AssertAnnotations.assertMethod(Auditor.class, "isExpresoConformidad");
       AssertAnnotations.assertMethod(Auditor.class, "getTokenResultado");
       AssertAnnotations.assertMethod(Auditor.class, "isHabilitaRevision");
       AssertAnnotations.assertMethod(Auditor.class, "getNombre");
       AssertAnnotations.assertMethod(Auditor.class, "getEleccion");
       AssertAnnotations.assertMethod(Auditor.class, "getMail");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Auditor.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       JoinColumn jc;
       c = ReflectTool.getFieldAnnotation(Auditor.class, "idAuditor", Column.class);
       assertEquals("id_auditor", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "idMigracion", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "comisionado", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "expresoConformidad", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "habilitaRevision", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "tokenResultado", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "nombre", Column.class);
       assertEquals("", c.name());
       jc = ReflectTool.getFieldAnnotation(Auditor.class, "eleccion", JoinColumn.class);
       assertEquals("id_eleccion", jc.name());
       c = ReflectTool.getFieldAnnotation(Auditor.class, "mail", Column.class);
       assertEquals("", c.name());
       
       
    }

}
