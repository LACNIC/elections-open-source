package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.IpAccess;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

public class IpAccessTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( IpAccessTest.class );
    }
    
    public void testIpAccess()
    {
    	AssertAnnotations.assertType(IpAccess.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(IpAccess.class, "ipAccessId", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(IpAccess.class, "attemptCount", Column.class);
       AssertAnnotations.assertField(IpAccess.class, "ip", Column.class);
       AssertAnnotations.assertField(IpAccess.class, "lastAttemptDate", Column.class);
       AssertAnnotations.assertField(IpAccess.class, "firstAttemptDate", Column.class);
       
       //metodos    
       AssertAnnotations.assertMethod(IpAccess.class, "getIpAccessId");
       AssertAnnotations.assertMethod(IpAccess.class, "getFirstAttemptDate");
       AssertAnnotations.assertMethod(IpAccess.class, "getIp");
       AssertAnnotations.assertMethod(IpAccess.class, "getAttemptCount");
       AssertAnnotations.assertMethod(IpAccess.class, "getLastAttemptDate");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(IpAccess.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       c = ReflectTool.getFieldAnnotation(IpAccess.class, "ipAccessId", Column.class);
       assertEquals("ipaccess_id", c.name());
       c = ReflectTool.getFieldAnnotation(IpAccess.class, "attemptCount", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(IpAccess.class, "ip", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(IpAccess.class, "lastAttemptDate", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(IpAccess.class, "firstAttemptDate", Column.class);
       assertEquals("", c.name());
       
    }

}
