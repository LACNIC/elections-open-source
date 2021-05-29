package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Comissioner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

public class ComissionerTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ComissionerTest.class );
    }
    
    public void testCommisioner()
    {
    	AssertAnnotations.assertType(Comissioner.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Comissioner.class, "idCommissioner", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(Comissioner.class, "name", Column.class);
       AssertAnnotations.assertField(Comissioner.class, "mail", Column.class);

       //metodos       
       AssertAnnotations.assertMethod(Comissioner.class, "getIdCommissioner");
       AssertAnnotations.assertMethod(Comissioner.class, "getName");
       AssertAnnotations.assertMethod(Comissioner.class, "getMail");
       
     //class annotations
       Entity a = ReflectTool.getClassAnnotation(Comissioner.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;       
       c = ReflectTool.getFieldAnnotation(Comissioner.class, "idCommissioner", Column.class);
       assertEquals("id_commissioner", c.name());
       c = ReflectTool.getFieldAnnotation(Comissioner.class, "name", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Comissioner.class, "mail", Column.class);
       assertEquals("", c.name());
       
    
    
    
    }

}
