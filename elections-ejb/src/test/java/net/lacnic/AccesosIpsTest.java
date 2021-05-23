package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.AccesosIps;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

public class AccesosIpsTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AccesosIpsTest.class );
    }
    
    public void testAcceosIps()
    {
    	AssertAnnotations.assertType(AccesosIps.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(AccesosIps.class, "id", Id.class, GeneratedValue.class, SequenceGenerator.class);
       AssertAnnotations.assertField(AccesosIps.class, "intentos", Column.class);
       AssertAnnotations.assertField(AccesosIps.class, "ip", Column.class);
       AssertAnnotations.assertField(AccesosIps.class, "fechaUltimoIntento", Column.class);
       AssertAnnotations.assertField(AccesosIps.class, "fechaPrimerIntento", Column.class);
       
       //metodos       
       AssertAnnotations.assertMethod(AccesosIps.class, "getFechaPrimerIntento");
       AssertAnnotations.assertMethod(AccesosIps.class, "getIdIpInhabilitada");
       AssertAnnotations.assertMethod(AccesosIps.class, "getIp");
       AssertAnnotations.assertMethod(AccesosIps.class, "getIntentos");
       AssertAnnotations.assertMethod(AccesosIps.class, "getFechaUltimoIntento");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(AccesosIps.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       c = ReflectTool.getFieldAnnotation(AccesosIps.class, "intentos", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(AccesosIps.class, "ip", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(AccesosIps.class, "fechaUltimoIntento", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(AccesosIps.class, "fechaPrimerIntento", Column.class);
       assertEquals("", c.name());
       
    }

}
