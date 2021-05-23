package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.TemplateEleccion;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

public class TemplateEleccionTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TemplateEleccionTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(TemplateEleccion.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(TemplateEleccion.class, "idTemplate", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(TemplateEleccion.class, "eleccion", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(TemplateEleccion.class, "tipoTemplate", Column.class);
       AssertAnnotations.assertField(TemplateEleccion.class, "asuntoES", Column.class);
       AssertAnnotations.assertField(TemplateEleccion.class, "asuntoPT", Column.class);
       AssertAnnotations.assertField(TemplateEleccion.class, "asuntoEN", Column.class);
       AssertAnnotations.assertField(TemplateEleccion.class, "cuerpoES", Column.class);
       AssertAnnotations.assertField(TemplateEleccion.class, "cuerpoPT", Column.class);
       AssertAnnotations.assertField(TemplateEleccion.class, "cuerpoEN", Column.class);
       AssertAnnotations.assertField(TemplateEleccion.class, "tipoDestinatario", Transient.class);
       
       //metodos       
       AssertAnnotations.assertMethod(TemplateEleccion.class, "getIdTemplate");
       AssertAnnotations.assertMethod(TemplateEleccion.class, "getEleccion");
       AssertAnnotations.assertMethod(TemplateEleccion.class, "getTipoTemplate");
       AssertAnnotations.assertMethod(TemplateEleccion.class, "getAsuntoES");
       AssertAnnotations.assertMethod(TemplateEleccion.class, "getAsuntoPT");
       AssertAnnotations.assertMethod(TemplateEleccion.class, "getAsuntoEN");
       AssertAnnotations.assertMethod(TemplateEleccion.class, "getCuerpoES");
       AssertAnnotations.assertMethod(TemplateEleccion.class, "getCuerpoPT");
       AssertAnnotations.assertMethod(TemplateEleccion.class, "getCuerpoEN");
       AssertAnnotations.assertMethod(TemplateEleccion.class, "getTipoDestinatario");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(TemplateEleccion.class, Entity.class);
       assertEquals("", a.name());
       
       Column c;
       JoinColumn jc;
       c = ReflectTool.getFieldAnnotation(TemplateEleccion.class, "idTemplate", Column.class);
       assertEquals("id_template_eleccion", c.name());
       jc = ReflectTool.getFieldAnnotation(TemplateEleccion.class, "eleccion", JoinColumn.class);
       assertEquals("id_eleccion", jc.name());
       c = ReflectTool.getFieldAnnotation(TemplateEleccion.class, "tipoTemplate", Column.class);
       assertEquals("tipo", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateEleccion.class, "asuntoES", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateEleccion.class, "asuntoPT", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateEleccion.class, "asuntoEN", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateEleccion.class, "cuerpoES", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateEleccion.class, "cuerpoPT", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(TemplateEleccion.class, "cuerpoEN", Column.class);
       assertEquals("", c.name());
       
       
       
    }

}
