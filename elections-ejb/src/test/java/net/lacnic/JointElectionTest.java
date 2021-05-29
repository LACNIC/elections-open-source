package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.JointElection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

public class JointElectionTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( JointElectionTest.class );
    }
    
    public void testJointElection()
    {
    	AssertAnnotations.assertType(JointElection.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(JointElection.class, "id", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       
       AssertAnnotations.assertMethod(JointElection.class, "getId");
       AssertAnnotations.assertMethod(JointElection.class, "getIdElectionA");
       AssertAnnotations.assertMethod(JointElection.class, "getIdElectionB");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(JointElection.class, Entity.class);
       assertEquals("", a.name());
              
       Column c;
       c = ReflectTool.getFieldAnnotation(JointElection.class, "id", Column.class);
       assertEquals("id", c.name());
       
    }

}
