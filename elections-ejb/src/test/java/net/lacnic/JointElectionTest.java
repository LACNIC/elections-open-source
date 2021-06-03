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
       AssertAnnotations.assertField(JointElection.class, "jointElectionId", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(JointElection.class, "idElectionA",  Column.class);
       AssertAnnotations.assertField(JointElection.class, "idElectionB",  Column.class);
       
       AssertAnnotations.assertMethod(JointElection.class, "getJointElectionId");
       AssertAnnotations.assertMethod(JointElection.class, "getIdElectionA");
       AssertAnnotations.assertMethod(JointElection.class, "getIdElectionB");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(JointElection.class, Entity.class);
       assertEquals("", a.name());
              
       Column c;
       c = ReflectTool.getFieldAnnotation(JointElection.class, "jointElectionId", Column.class);
       assertEquals("jointelection_id", c.name());
       c = ReflectTool.getFieldAnnotation(JointElection.class, "idElectionA", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(JointElection.class, "idElectionB", Column.class);
       assertEquals("", c.name());
       
    }

}
