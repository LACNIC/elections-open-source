package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;


import net.lacnic.siselecciones.dominio.Email;

public class EmailTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( EmailTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(Email.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Email.class, "id", Id.class, GeneratedValue.class, SequenceGenerator.class);
       AssertAnnotations.assertField(Email.class, "destinatarios", Column.class);
       AssertAnnotations.assertField(Email.class, "desde", Column.class);
       AssertAnnotations.assertField(Email.class, "cc", Column.class);
       AssertAnnotations.assertField(Email.class, "bcc", Column.class);
       AssertAnnotations.assertField(Email.class, "asunto", Column.class);
       AssertAnnotations.assertField(Email.class, "cuerpo", Column.class);
       AssertAnnotations.assertField(Email.class, "enviado", Column.class);
       AssertAnnotations.assertField(Email.class, "fechaCreado", Column.class);
       AssertAnnotations.assertField(Email.class, "tipoTemplate", Column.class);
       AssertAnnotations.assertField(Email.class, "eleccion", JoinColumn.class, ManyToOne.class);

       //metodos       
       AssertAnnotations.assertMethod(Email.class, "getId");
       AssertAnnotations.assertMethod(Email.class, "getDestinatarios");
       AssertAnnotations.assertMethod(Email.class, "getCc");
       AssertAnnotations.assertMethod(Email.class, "getBcc");
       AssertAnnotations.assertMethod(Email.class, "getAsunto");
       AssertAnnotations.assertMethod(Email.class, "getCuerpo");
       AssertAnnotations.assertMethod(Email.class, "getEnviado");
       AssertAnnotations.assertMethod(Email.class, "getDesde");
       AssertAnnotations.assertMethod(Email.class, "getEleccion");
       AssertAnnotations.assertMethod(Email.class, "getTipoMail");
       AssertAnnotations.assertMethod(Email.class, "getFechaCreado");
 
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Email.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       SequenceGenerator sg;
       JoinColumn jc;
       sg = ReflectTool.getFieldAnnotation(Email.class, "id", SequenceGenerator.class);
       assertEquals("emaile_seq", sg.name());
       c = ReflectTool.getFieldAnnotation(Email.class, "destinatarios", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Email.class, "desde", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Email.class, "cc", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Email.class, "bcc", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Email.class, "asunto", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Email.class, "cuerpo", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Email.class, "enviado", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Email.class, "fechaCreado", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Email.class, "tipoTemplate", Column.class);
       assertEquals("", c.name());
       jc = ReflectTool.getFieldAnnotation(Email.class, "eleccion", JoinColumn.class);
       assertEquals("id_eleccion", jc.name());
       
    }
    

}
