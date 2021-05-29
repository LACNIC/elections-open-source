package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.UserAdmin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

public class UserAdminTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( UserAdminTest.class );
    }
    
    public void testUserAdmin()
    {
    	AssertAnnotations.assertType(UserAdmin.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(UserAdmin.class, "userId", Id.class, Column.class);
       AssertAnnotations.assertField(UserAdmin.class, "password", Column.class);
       AssertAnnotations.assertField(UserAdmin.class, "email", Column.class);
       AssertAnnotations.assertField(UserAdmin.class, "idElectionAuthorized", Column.class);
       
       //metodos       
       AssertAnnotations.assertMethod(UserAdmin.class, "getUserAdminId");
       AssertAnnotations.assertMethod(UserAdmin.class, "getPassword");
       AssertAnnotations.assertMethod(UserAdmin.class, "getEmail");
       AssertAnnotations.assertMethod(UserAdmin.class, "getIdElectionAuthorized");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(UserAdmin.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;     
       c = ReflectTool.getFieldAnnotation(UserAdmin.class, "userId", Column.class);
       assertEquals("user_id", c.name());
       c = ReflectTool.getFieldAnnotation(UserAdmin.class, "password", Column.class);
       assertEquals("password", c.name());
       c = ReflectTool.getFieldAnnotation(UserAdmin.class, "email", Column.class);
       assertEquals("email", c.name());
       c = ReflectTool.getFieldAnnotation(UserAdmin.class, "idElectionAuthorized", Column.class);
       assertEquals("id_election_authorized", c.name());
       
    }

}
