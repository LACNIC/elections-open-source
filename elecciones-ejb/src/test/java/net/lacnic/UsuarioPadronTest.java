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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import net.lacnic.siselecciones.dominio.UsuarioPadron;

public class UsuarioPadronTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( UsuarioPadronTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(UsuarioPadron.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(UsuarioPadron.class, "idUsuarioPadron", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "idMigracion", Column.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "eleccion", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "yaVoto", Column.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "tokenVotacion", Column.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "cantVotos", Column.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "nombre", Column.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "mail", Column.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "pais", Column.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "idioma", Column.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "orgID", Column.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "fechaVoto", Column.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "votos", OneToMany.class);
       AssertAnnotations.assertField(UsuarioPadron.class, "resumenCodigos", Transient.class);

       //metodos       
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getIdUsuarioPadron");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getNombre");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getMail");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getPais");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getIdioma");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getOrgID");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "isYaVoto");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getTokenVotacion");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getEleccion");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getInformacionDelVotante");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getInformacionDelVotanteCompleta");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getResumenCodigos");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getLinkVotar");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getFechaVoto");
       AssertAnnotations.assertMethod(UsuarioPadron.class, "getIdMigracion");
       
     //class annotations
       Entity a = ReflectTool.getClassAnnotation(UsuarioPadron.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       JoinColumn jc;
       OneToMany oa;
       c = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "idUsuarioPadron", Column.class);
       assertEquals("id_usuario_padron", c.name());
       c = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "idMigracion", Column.class);
       assertEquals("", c.name());
       jc = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "eleccion", JoinColumn.class);
       assertEquals("id_eleccion", jc.name());
       c = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "yaVoto", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "tokenVotacion", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "cantVotos", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "nombre", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "mail", Column.class);
       assertEquals("", c.name());       
       c = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "pais", Column.class);
       assertEquals("", c.name());       
       c = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "idioma", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "orgID", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "fechaVoto", Column.class);
       assertEquals("", c.name());
       oa = ReflectTool.getFieldAnnotation(UsuarioPadron.class, "votos", OneToMany.class);
       assertEquals("usuarioPadron", oa.mappedBy());      
       
    
    }

}
