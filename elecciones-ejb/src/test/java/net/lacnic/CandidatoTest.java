package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

import net.lacnic.siselecciones.dominio.Candidato;

public class CandidatoTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CandidatoTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(Candidato.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Candidato.class, "idCandidato", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(Candidato.class, "idMigracion", Column.class);
       AssertAnnotations.assertField(Candidato.class, "nombre", Column.class);
       AssertAnnotations.assertField(Candidato.class, "eleccion", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(Candidato.class, "contenidoFoto", Column.class);
       AssertAnnotations.assertField(Candidato.class, "nombreFoto", Column.class);
       AssertAnnotations.assertField(Candidato.class, "bioEspanol", Column.class);
       AssertAnnotations.assertField(Candidato.class, "bioIngles", Column.class);
       AssertAnnotations.assertField(Candidato.class, "bioPortugues", Column.class);
       AssertAnnotations.assertField(Candidato.class, "extensionFoto", Column.class);
       AssertAnnotations.assertField(Candidato.class, "orden", Column.class);
       AssertAnnotations.assertField(Candidato.class, "solosp", Column.class);
       AssertAnnotations.assertField(Candidato.class, "linkEspanol", Column.class);
       AssertAnnotations.assertField(Candidato.class, "linkIngles", Column.class);
       AssertAnnotations.assertField(Candidato.class, "linkPortugues", Column.class);
       AssertAnnotations.assertField(Candidato.class, "votos", OneToMany.class);
       
       //metodos       
       AssertAnnotations.assertMethod(Candidato.class, "getIdCandidato");
       AssertAnnotations.assertMethod(Candidato.class, "getNombre");
       AssertAnnotations.assertMethod(Candidato.class, "getEleccion");
       AssertAnnotations.assertMethod(Candidato.class, "getContenidoFoto");
       AssertAnnotations.assertMethod(Candidato.class, "getNombreFoto");
       AssertAnnotations.assertMethod(Candidato.class, "getExtensionFoto");
       AssertAnnotations.assertMethod(Candidato.class, "getVotos");              
       AssertAnnotations.assertMethod(Candidato.class, "getBioEspanol");
       AssertAnnotations.assertMethod(Candidato.class, "getBioPortugues");
       AssertAnnotations.assertMethod(Candidato.class, "getBioIngles");
       AssertAnnotations.assertMethod(Candidato.class, "getOrden");
       AssertAnnotations.assertMethod(Candidato.class, "isSolosp");
       AssertAnnotations.assertMethod(Candidato.class, "isFijo");
       AssertAnnotations.assertMethod(Candidato.class, "getLinkEspanol");
       AssertAnnotations.assertMethod(Candidato.class, "getLinkIngles");
       AssertAnnotations.assertMethod(Candidato.class, "getLinkPortugues");
       AssertAnnotations.assertMethod(Candidato.class, "getIdMigracion");
    
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Candidato.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       JoinColumn jc;
       OneToMany oa;
       c = ReflectTool.getFieldAnnotation(Candidato.class, "idCandidato", Column.class);
       assertEquals("id_candidato", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "idMigracion", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "nombre", Column.class);
       assertEquals("", c.name());
       jc = ReflectTool.getFieldAnnotation(Candidato.class, "eleccion", JoinColumn.class);
       assertEquals("id_eleccion", jc.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "contenidoFoto", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "nombreFoto", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "bioEspanol", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "bioIngles", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "bioPortugues", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "extensionFoto", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "orden", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "solosp", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "linkEspanol", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "linkIngles", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidato.class, "linkPortugues", Column.class);
       assertEquals("", c.name());
       oa = ReflectTool.getFieldAnnotation(Candidato.class, "votos", OneToMany.class);
       assertEquals("candidato", oa.mappedBy());
       
       
    }

}
