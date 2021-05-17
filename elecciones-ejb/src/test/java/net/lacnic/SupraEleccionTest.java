package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import net.lacnic.siselecciones.dominio.SupraEleccion;

public class SupraEleccionTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( SupraEleccionTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(SupraEleccion.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(SupraEleccion.class, "id", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       
       AssertAnnotations.assertMethod(SupraEleccion.class, "getId");
       AssertAnnotations.assertMethod(SupraEleccion.class, "getIdEleccionA");
       AssertAnnotations.assertMethod(SupraEleccion.class, "getIdEleccionB");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(SupraEleccion.class, Entity.class);
       assertEquals("", a.name());
              
       Column c;
       c = ReflectTool.getFieldAnnotation(SupraEleccion.class, "id", Column.class);
       assertEquals("id", c.name());
       
    }

}
