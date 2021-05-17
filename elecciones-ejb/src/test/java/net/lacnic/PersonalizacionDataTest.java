package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import net.lacnic.siselecciones.dominio.Personalizacion;


public class PersonalizacionDataTest extends TestCase {
	
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PersonalizacionDataTest.class );
    }
    
    public void testPersonalizacion()
    {
    	AssertAnnotations.assertType(Personalizacion.class, Entity.class);
    	
    	 // fields
        AssertAnnotations.assertField(Personalizacion.class, "idPersonalizacion", Column.class, Id.class, GeneratedValue.class, SequenceGenerator.class);
        AssertAnnotations.assertField(Personalizacion.class, "picSmallLogo", Column.class);
        AssertAnnotations.assertField(Personalizacion.class, "picBigLogo", Column.class);
        AssertAnnotations.assertField(Personalizacion.class, "picSimbolo", Column.class);
        AssertAnnotations.assertField(Personalizacion.class, "contPicSmallLogo", Column.class);
        AssertAnnotations.assertField(Personalizacion.class, "contPicBigLogo", Column.class);
        AssertAnnotations.assertField(Personalizacion.class, "contPicSimbolo", Column.class);
        AssertAnnotations.assertField(Personalizacion.class, "tituloSitio", Column.class);
        AssertAnnotations.assertField(Personalizacion.class, "tituloLogin", Column.class);
        AssertAnnotations.assertField(Personalizacion.class, "showHome", Column.class);
        AssertAnnotations.assertField(Personalizacion.class, "homeHtml", Column.class);
        
        //metodos
     
        AssertAnnotations.assertMethod(Personalizacion.class, "getIdPersonalizacion");
        AssertAnnotations.assertMethod(Personalizacion.class, "getPicSmallLogo");
        AssertAnnotations.assertMethod(Personalizacion.class, "getPicBigLogo");
        AssertAnnotations.assertMethod(Personalizacion.class, "getPicSimbolo");
        AssertAnnotations.assertMethod(Personalizacion.class, "getContPicSmallLogo");
        AssertAnnotations.assertMethod(Personalizacion.class, "getContPicBigLogo");
        AssertAnnotations.assertMethod(Personalizacion.class, "getContPicSimbolo");
        AssertAnnotations.assertMethod(Personalizacion.class, "getTituloSitio");
        AssertAnnotations.assertMethod(Personalizacion.class, "getTituloLogin");
        AssertAnnotations.assertMethod(Personalizacion.class, "isShowHome");
        
        //class annotations
        Entity a = ReflectTool.getClassAnnotation(Personalizacion.class, Entity.class);
        assertEquals("", a.name());
        
        
        Column c;
        c = ReflectTool.getFieldAnnotation(Personalizacion.class, "idPersonalizacion", Column.class);
        assertEquals("id_personalizacion", c.name());
        c = ReflectTool.getFieldAnnotation(Personalizacion.class, "picSmallLogo", Column.class);
        assertEquals("pic_small_logo", c.name());
        c = ReflectTool.getFieldAnnotation(Personalizacion.class, "picBigLogo", Column.class);
        assertEquals("pic_big_logo", c.name());
        c = ReflectTool.getFieldAnnotation(Personalizacion.class, "picSimbolo", Column.class);
        assertEquals("pic_simbolo", c.name());
        c = ReflectTool.getFieldAnnotation(Personalizacion.class, "contPicSmallLogo", Column.class);
        assertEquals("cont_pic_small_logo", c.name());
        c = ReflectTool.getFieldAnnotation(Personalizacion.class, "contPicBigLogo", Column.class);
        assertEquals("cont_pic_big_logo", c.name());
        c = ReflectTool.getFieldAnnotation(Personalizacion.class, "contPicSimbolo", Column.class);
        assertEquals("cont_pic_simbolo", c.name());
        c = ReflectTool.getFieldAnnotation(Personalizacion.class, "tituloSitio", Column.class);
        assertEquals("titulo_sitio", c.name());
        c = ReflectTool.getFieldAnnotation(Personalizacion.class, "tituloLogin", Column.class);
        assertEquals("titulo_login", c.name());
        c = ReflectTool.getFieldAnnotation(Personalizacion.class, "showHome", Column.class);
        assertEquals("show_home", c.name());
        c = ReflectTool.getFieldAnnotation(Personalizacion.class, "homeHtml", Column.class);
        assertEquals("home_html", c.name());
       
    }
    
    

}
