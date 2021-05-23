package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.UsuarioAdmin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

public class UsuarioAdminTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( UsuarioAdminTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(UsuarioAdmin.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(UsuarioAdmin.class, "userId", Id.class, Column.class);
       AssertAnnotations.assertField(UsuarioAdmin.class, "password", Column.class);
       AssertAnnotations.assertField(UsuarioAdmin.class, "email", Column.class);
       AssertAnnotations.assertField(UsuarioAdmin.class, "idEleccionAutorizado", Column.class);
       
       //metodos       
       AssertAnnotations.assertMethod(UsuarioAdmin.class, "getUserAdminId");
       AssertAnnotations.assertMethod(UsuarioAdmin.class, "getPassword");
       AssertAnnotations.assertMethod(UsuarioAdmin.class, "getEmail");
       AssertAnnotations.assertMethod(UsuarioAdmin.class, "getIdEleccionAutorizado");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(UsuarioAdmin.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;     
       c = ReflectTool.getFieldAnnotation(UsuarioAdmin.class, "userId", Column.class);
       assertEquals("user_id", c.name());
       c = ReflectTool.getFieldAnnotation(UsuarioAdmin.class, "password", Column.class);
       assertEquals("password", c.name());
       c = ReflectTool.getFieldAnnotation(UsuarioAdmin.class, "email", Column.class);
       assertEquals("email", c.name());
       c = ReflectTool.getFieldAnnotation(UsuarioAdmin.class, "idEleccionAutorizado", Column.class);
       assertEquals("id_eleccion_autorizado", c.name());
       
    }

}
