package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import net.lacnic.siselecciones.dominio.Comisionado;

public class ComisionadoTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ComisionadoTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(Comisionado.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Comisionado.class, "idComisionado", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(Comisionado.class, "nombre", Column.class);
       AssertAnnotations.assertField(Comisionado.class, "mail", Column.class);

       //metodos       
       AssertAnnotations.assertMethod(Comisionado.class, "getIdComisionado");
       AssertAnnotations.assertMethod(Comisionado.class, "getNombre");
       AssertAnnotations.assertMethod(Comisionado.class, "getMail");
       
     //class annotations
       Entity a = ReflectTool.getClassAnnotation(Comisionado.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;       
       c = ReflectTool.getFieldAnnotation(Comisionado.class, "idComisionado", Column.class);
       assertEquals("id_comisionado", c.name());
       c = ReflectTool.getFieldAnnotation(Comisionado.class, "nombre", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Comisionado.class, "mail", Column.class);
       assertEquals("", c.name());
       
    
    
    
    }

}
