package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

public class ParameterTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ParameterTest.class );
    }
    
    public void testParameter()
    {
    	AssertAnnotations.assertType(Parameter.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Parameter.class, "key", Id.class);
       AssertAnnotations.assertField(Parameter.class, "value", Column.class);
       
       //metodos       
       AssertAnnotations.assertMethod(Parameter.class, "getKey");
       AssertAnnotations.assertMethod(Parameter.class, "getValue");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Parameter.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       c = ReflectTool.getFieldAnnotation(Parameter.class, "value", Column.class);
       assertEquals("", c.name());
    }
}
