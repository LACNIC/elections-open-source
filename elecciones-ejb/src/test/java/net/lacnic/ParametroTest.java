package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import net.lacnic.siselecciones.dominio.Parametro;

public class ParametroTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ParametroTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(Parametro.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Parametro.class, "clave", Id.class);
       AssertAnnotations.assertField(Parametro.class, "valor", Column.class);
       
       //metodos       
       AssertAnnotations.assertMethod(Parametro.class, "getClave");
       AssertAnnotations.assertMethod(Parametro.class, "getValor");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Parametro.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       c = ReflectTool.getFieldAnnotation(Parametro.class, "valor", Column.class);
       assertEquals("", c.name());
    }
}
