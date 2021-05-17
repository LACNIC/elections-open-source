package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import net.lacnic.siselecciones.dominio.EmailHistorico;

public class EmailHistoricoTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( EmailHistoricoTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(EmailHistorico.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(EmailHistorico.class, "id", Id.class);
       AssertAnnotations.assertField(EmailHistorico.class, "destinatarios", Column.class);
       AssertAnnotations.assertField(EmailHistorico.class, "desde", Column.class);
       AssertAnnotations.assertField(EmailHistorico.class, "cc", Column.class);
       AssertAnnotations.assertField(EmailHistorico.class, "bcc", Column.class);
       AssertAnnotations.assertField(EmailHistorico.class, "asunto", Column.class);
       AssertAnnotations.assertField(EmailHistorico.class, "cuerpo", Column.class);
       AssertAnnotations.assertField(EmailHistorico.class, "enviado", Column.class);
       AssertAnnotations.assertField(EmailHistorico.class, "fechaCreado", Column.class);
       AssertAnnotations.assertField(EmailHistorico.class, "tipoTemplate", Column.class);
       AssertAnnotations.assertField(EmailHistorico.class, "idEleccion", Column.class);
       
     //metodos       
       AssertAnnotations.assertMethod(EmailHistorico.class, "getId");
       AssertAnnotations.assertMethod(EmailHistorico.class, "getDestinatarios");
       AssertAnnotations.assertMethod(EmailHistorico.class, "getCc");
       AssertAnnotations.assertMethod(EmailHistorico.class, "getBcc");
       AssertAnnotations.assertMethod(EmailHistorico.class, "getAsunto");
       AssertAnnotations.assertMethod(EmailHistorico.class, "getCuerpo");
       AssertAnnotations.assertMethod(EmailHistorico.class, "getEnviado");
       AssertAnnotations.assertMethod(EmailHistorico.class, "getDesde");
       AssertAnnotations.assertMethod(EmailHistorico.class, "getIdEleccion");
       AssertAnnotations.assertMethod(EmailHistorico.class, "getTipoTemplate");
       AssertAnnotations.assertMethod(EmailHistorico.class, "getFechaCreado");
 
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(EmailHistorico.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;       
       c = ReflectTool.getFieldAnnotation(EmailHistorico.class, "destinatarios", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistorico.class, "desde", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistorico.class, "cc", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistorico.class, "bcc", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistorico.class, "asunto", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistorico.class, "cuerpo", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistorico.class, "enviado", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistorico.class, "fechaCreado", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistorico.class, "tipoTemplate", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(EmailHistorico.class, "idEleccion", Column.class);
       assertEquals("", c.name());
    }

}
