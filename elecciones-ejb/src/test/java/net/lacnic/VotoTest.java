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

import net.lacnic.siselecciones.dominio.Voto;

public class VotoTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( VotoTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(Voto.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Voto.class, "idVoto", Id.class, GeneratedValue.class, SequenceGenerator.class);
       AssertAnnotations.assertField(Voto.class, "codigo", Column.class);
       AssertAnnotations.assertField(Voto.class, "ip", Column.class);
       AssertAnnotations.assertField(Voto.class, "fechaVoto", Column.class);
       AssertAnnotations.assertField(Voto.class, "candidato", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(Voto.class, "eleccion", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(Voto.class, "usuarioPadron", JoinColumn.class, ManyToOne.class);
       
       //metodos       
       AssertAnnotations.assertMethod(Voto.class, "getUsuarioPadron");
       AssertAnnotations.assertMethod(Voto.class, "getIdVoto");
       AssertAnnotations.assertMethod(Voto.class, "getEleccion");
       AssertAnnotations.assertMethod(Voto.class, "getCodigo");
       AssertAnnotations.assertMethod(Voto.class, "getCandidato");
       AssertAnnotations.assertMethod(Voto.class, "getIp");
       AssertAnnotations.assertMethod(Voto.class, "getFechaVoto");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Voto.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       JoinColumn jc;
       c = ReflectTool.getFieldAnnotation(Voto.class, "codigo", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Voto.class, "ip", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Voto.class, "fechaVoto", Column.class);
       assertEquals("", c.name());
       jc = ReflectTool.getFieldAnnotation(Voto.class, "candidato", JoinColumn.class);
       assertEquals("id_candidato", jc.name());
       jc = ReflectTool.getFieldAnnotation(Voto.class, "eleccion", JoinColumn.class);
       assertEquals("id_eleccion", jc.name());
       jc = ReflectTool.getFieldAnnotation(Voto.class, "usuarioPadron", JoinColumn.class);
       assertEquals("id_usuario_padron", jc.name());
       
       
    }

}
