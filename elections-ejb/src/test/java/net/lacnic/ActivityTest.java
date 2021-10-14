package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Activity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;


public class ActivityTest extends TestCase  {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ActivityTest.class );
    }
    
    public void testActivity()
    {
    	AssertAnnotations.assertType(Activity.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Activity.class, "activityId", Column.class, Id.class, GeneratedValue.class, SequenceGenerator.class);
       AssertAnnotations.assertField(Activity.class, "userName", Column.class);
       AssertAnnotations.assertField(Activity.class, "electionId", Column.class);
       AssertAnnotations.assertField(Activity.class, "ip", Column.class);
       AssertAnnotations.assertField(Activity.class, "timestamp", Column.class);
       AssertAnnotations.assertField(Activity.class, "activityType", Column.class, Enumerated.class);
       AssertAnnotations.assertField(Activity.class, "description", Column.class);
       
       //metodos       
       AssertAnnotations.assertMethod(Activity.class, "getActivityId");
       AssertAnnotations.assertMethod(Activity.class, "getUserName");
       AssertAnnotations.assertMethod(Activity.class, "getActivityType");
       AssertAnnotations.assertMethod(Activity.class, "getDescription");
       AssertAnnotations.assertMethod(Activity.class, "getIp");
       AssertAnnotations.assertMethod(Activity.class, "getTimestamp");
       AssertAnnotations.assertMethod(Activity.class, "getElectionId");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Activity.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       c = ReflectTool.getFieldAnnotation(Activity.class, "activityId", Column.class);
       assertEquals("activity_id", c.name());
       c = ReflectTool.getFieldAnnotation(Activity.class, "userName", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Activity.class, "electionId", Column.class);
       assertEquals("election_id", c.name());
       c = ReflectTool.getFieldAnnotation(Activity.class, "ip", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Activity.class, "timestamp", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Activity.class, "activityType", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Activity.class, "description", Column.class);
       assertEquals("", c.name());
      
       
       
    }
	
}
