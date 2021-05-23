package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Actividad;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;


public class ActividadTest extends TestCase  {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ActividadTest.class );
    }
    
    public void testActividad()
    {
    	AssertAnnotations.assertType(Actividad.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Actividad.class, "idActividad", Column.class, Id.class, GeneratedValue.class, SequenceGenerator.class);
       AssertAnnotations.assertField(Actividad.class, "nomUser", Column.class);
       AssertAnnotations.assertField(Actividad.class, "idEleccion", Column.class);
       AssertAnnotations.assertField(Actividad.class, "ip", Column.class);
       AssertAnnotations.assertField(Actividad.class, "tiempo", Column.class);
       AssertAnnotations.assertField(Actividad.class, "tipoActividad", Column.class, Enumerated.class);
       AssertAnnotations.assertField(Actividad.class, "descripcion", Column.class);
       
       //metodos       
       AssertAnnotations.assertMethod(Actividad.class, "getIdActividad");
       AssertAnnotations.assertMethod(Actividad.class, "getNomUser");
       AssertAnnotations.assertMethod(Actividad.class, "getTipoActividad");
       AssertAnnotations.assertMethod(Actividad.class, "getDescripcion");
       AssertAnnotations.assertMethod(Actividad.class, "getIp");
       AssertAnnotations.assertMethod(Actividad.class, "getTiempo");
       AssertAnnotations.assertMethod(Actividad.class, "getIdEleccion");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Actividad.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       c = ReflectTool.getFieldAnnotation(Actividad.class, "idActividad", Column.class);
       assertEquals("id_actividad", c.name());
       c = ReflectTool.getFieldAnnotation(Actividad.class, "nomUser", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Actividad.class, "idEleccion", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Actividad.class, "ip", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Actividad.class, "tiempo", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Actividad.class, "tipoActividad", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Actividad.class, "descripcion", Column.class);
       assertEquals("", c.name());
      
       
       
    }
	
}
