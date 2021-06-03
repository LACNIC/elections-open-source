package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Customization;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;


public class CustomizationTest extends TestCase {
	
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CustomizationTest.class );
    }
    
    public void testCustomization()
    {
    	AssertAnnotations.assertType(Customization.class, Entity.class);
    	
    	 // fields
        AssertAnnotations.assertField(Customization.class, "customizationId", Column.class, Id.class, GeneratedValue.class, SequenceGenerator.class);
        AssertAnnotations.assertField(Customization.class, "picSmallLogo", Column.class);
        AssertAnnotations.assertField(Customization.class, "picBigLogo", Column.class);
        AssertAnnotations.assertField(Customization.class, "picSymbol", Column.class);
        AssertAnnotations.assertField(Customization.class, "contPicSmallLogo", Column.class);
        AssertAnnotations.assertField(Customization.class, "contPicBigLogo", Column.class);
        AssertAnnotations.assertField(Customization.class, "contPicSymbol", Column.class);
        AssertAnnotations.assertField(Customization.class, "siteTitle", Column.class);
        AssertAnnotations.assertField(Customization.class, "loginTitle", Column.class);
        AssertAnnotations.assertField(Customization.class, "showHome", Column.class);
        AssertAnnotations.assertField(Customization.class, "homeHtml", Column.class);
        
        //metodos
     
        AssertAnnotations.assertMethod(Customization.class, "getCustomizationId");
        AssertAnnotations.assertMethod(Customization.class, "getPicSmallLogo");
        AssertAnnotations.assertMethod(Customization.class, "getPicBigLogo");
        AssertAnnotations.assertMethod(Customization.class, "getPicSymbol");
        AssertAnnotations.assertMethod(Customization.class, "getContPicSmallLogo");
        AssertAnnotations.assertMethod(Customization.class, "getContPicBigLogo");
        AssertAnnotations.assertMethod(Customization.class, "getContPicSymbol");
        AssertAnnotations.assertMethod(Customization.class, "getSiteTitle");
        AssertAnnotations.assertMethod(Customization.class, "getLoginTitle");
        AssertAnnotations.assertMethod(Customization.class, "isShowHome");
        
        //class annotations
        Entity a = ReflectTool.getClassAnnotation(Customization.class, Entity.class);
        assertEquals("", a.name());
        
        
        Column c;
        c = ReflectTool.getFieldAnnotation(Customization.class, "customizationId", Column.class);
        assertEquals("customization_id", c.name());
        c = ReflectTool.getFieldAnnotation(Customization.class, "picSmallLogo", Column.class);
        assertEquals("pic_small_logo", c.name());
        c = ReflectTool.getFieldAnnotation(Customization.class, "picBigLogo", Column.class);
        assertEquals("pic_big_logo", c.name());
        c = ReflectTool.getFieldAnnotation(Customization.class, "picSymbol", Column.class);
        assertEquals("pic_symbol", c.name());
        c = ReflectTool.getFieldAnnotation(Customization.class, "contPicSmallLogo", Column.class);
        assertEquals("cont_pic_small_logo", c.name());
        c = ReflectTool.getFieldAnnotation(Customization.class, "contPicBigLogo", Column.class);
        assertEquals("cont_pic_big_logo", c.name());
        c = ReflectTool.getFieldAnnotation(Customization.class, "contPicSymbol", Column.class);
        assertEquals("cont_pic_symbol", c.name());
        c = ReflectTool.getFieldAnnotation(Customization.class, "siteTitle", Column.class);
        assertEquals("site_title", c.name());
        c = ReflectTool.getFieldAnnotation(Customization.class, "loginTitle", Column.class);
        assertEquals("login_title", c.name());
        c = ReflectTool.getFieldAnnotation(Customization.class, "showHome", Column.class);
        assertEquals("show_home", c.name());
        c = ReflectTool.getFieldAnnotation(Customization.class, "homeHtml", Column.class);
        assertEquals("home_html", c.name());
       
    }
    
    

}
