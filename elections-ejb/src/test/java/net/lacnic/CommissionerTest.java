package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Commissioner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

public class CommissionerTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CommissionerTest.class );
    }
    
    public void testCommissioner()
    {
    	AssertAnnotations.assertType(Commissioner.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Commissioner.class, "commissionerId", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(Commissioner.class, "name", Column.class);
       AssertAnnotations.assertField(Commissioner.class, "mail", Column.class);

       //metodos       
       AssertAnnotations.assertMethod(Commissioner.class, "getCommissionerId");
       AssertAnnotations.assertMethod(Commissioner.class, "getName");
       AssertAnnotations.assertMethod(Commissioner.class, "getMail");
       
     //class annotations
       Entity a = ReflectTool.getClassAnnotation(Commissioner.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;       
       c = ReflectTool.getFieldAnnotation(Commissioner.class, "commissionerId", Column.class);
       assertEquals("commissioner_id", c.name());
       c = ReflectTool.getFieldAnnotation(Commissioner.class, "name", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Commissioner.class, "mail", Column.class);
       assertEquals("", c.name());
       
    
    
    
    }

}
