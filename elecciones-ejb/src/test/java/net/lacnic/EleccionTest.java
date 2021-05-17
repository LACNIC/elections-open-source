package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import net.lacnic.siselecciones.dominio.Eleccion;

public class EleccionTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( EleccionTest.class );
    }
    
    public void testAuditor()
    {
    	AssertAnnotations.assertType(Eleccion.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Eleccion.class, "idEleccion", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(Eleccion.class, "idMigracion", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "categoria", Column.class, Enumerated.class);
       AssertAnnotations.assertField(Eleccion.class, "migrada", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "fechaInicio", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "fechaFin", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "fechaCreacion", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "tituloEspanol", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "tituloIngles", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "tituloPortugues", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "linkEspanol", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "linkIngles", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "linkPortugues", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "descripcionEspanol", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "descripcionIngles", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "descripcionPortugues", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "maxCandidatos", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "habilitadoLinkVotacion", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "habilitadoLinkResultado", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "habilitadoLinkAuditor", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "solicitarRevision", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "solosp", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "tokenResultado", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "remitentePorDefecto", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "padronSeteado", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "candidatosSeteado", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "auditoresSeteado", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "candidatosAleatorios", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "diffUTC", Column.class);
       AssertAnnotations.assertField(Eleccion.class, "candidatos", OneToMany.class);
       AssertAnnotations.assertField(Eleccion.class, "usuariosPadron", OneToMany.class);
       AssertAnnotations.assertField(Eleccion.class, "auditores", OneToMany.class);
       AssertAnnotations.assertField(Eleccion.class, "templatesEleccion", OneToMany.class);
       AssertAnnotations.assertField(Eleccion.class, "votos", OneToMany.class);
       AssertAnnotations.assertField(Eleccion.class, "auxFechaInicio", Transient.class);
       AssertAnnotations.assertField(Eleccion.class, "auxHoraInicio", Transient.class);
       AssertAnnotations.assertField(Eleccion.class, "auxFechaFin", Transient.class);
       AssertAnnotations.assertField(Eleccion.class, "auxHoraFin", Transient.class);
       
       //metodos       
       AssertAnnotations.assertMethod(Eleccion.class, "getIdEleccion");
       AssertAnnotations.assertMethod(Eleccion.class, "getFechaInicio");
       AssertAnnotations.assertMethod(Eleccion.class, "getFechaFin");
       AssertAnnotations.assertMethod(Eleccion.class, "getMaxCandidatos");
       AssertAnnotations.assertMethod(Eleccion.class, "isHabilitadoLinkVotacion");
       AssertAnnotations.assertMethod(Eleccion.class, "getCandidatos");
       AssertAnnotations.assertMethod(Eleccion.class, "getFechaCreacion");
       AssertAnnotations.assertMethod(Eleccion.class, "getUsuariosPadron");
       AssertAnnotations.assertMethod(Eleccion.class, "getVotos");
       AssertAnnotations.assertMethod(Eleccion.class, "isHabilitadoLinkResultado");
       AssertAnnotations.assertMethod(Eleccion.class, "getTituloEspanol");
       AssertAnnotations.assertMethod(Eleccion.class, "getTituloIngles");
       AssertAnnotations.assertMethod(Eleccion.class, "getTituloPortugues");
       AssertAnnotations.assertMethod(Eleccion.class, "getDescripcionEspanol");
       AssertAnnotations.assertMethod(Eleccion.class, "getDescripcionIngles");
       AssertAnnotations.assertMethod(Eleccion.class, "getDescripcionPortugues");
       AssertAnnotations.assertMethod(Eleccion.class, "getTokenResultado");
       AssertAnnotations.assertMethod(Eleccion.class, "getAuditores");
       AssertAnnotations.assertMethod(Eleccion.class, "getLinkEspanol");
       AssertAnnotations.assertMethod(Eleccion.class, "getLinkIngles");
       AssertAnnotations.assertMethod(Eleccion.class, "getLinkPortugues");
       AssertAnnotations.assertMethod(Eleccion.class, "getAuxFechaInicio");
       AssertAnnotations.assertMethod(Eleccion.class, "getAuxHoraInicio");
       AssertAnnotations.assertMethod(Eleccion.class, "getAuxHoraFin");
       AssertAnnotations.assertMethod(Eleccion.class, "getAuxFechaFin");
       AssertAnnotations.assertMethod(Eleccion.class, "isSolosp");
       AssertAnnotations.assertMethod(Eleccion.class, "isHabilitadoLinkAuditor");
       AssertAnnotations.assertMethod(Eleccion.class, "isPadronSeteado");
       AssertAnnotations.assertMethod(Eleccion.class, "isCandidatosSeteado");
       AssertAnnotations.assertMethod(Eleccion.class, "isAuditoresSeteado");
       AssertAnnotations.assertMethod(Eleccion.class, "getTemplatesEleccion");
       AssertAnnotations.assertMethod(Eleccion.class, "getRemitentePorDefecto");
       AssertAnnotations.assertMethod(Eleccion.class, "getLinkResultado");
       AssertAnnotations.assertMethod(Eleccion.class, "isCandidatosAleatorios");
       AssertAnnotations.assertMethod(Eleccion.class, "getFechaInicioString");
       AssertAnnotations.assertMethod(Eleccion.class, "getDiffUTC");
       AssertAnnotations.assertMethod(Eleccion.class, "isSolicitarRevision");
       AssertAnnotations.assertMethod(Eleccion.class, "isTermino");
       AssertAnnotations.assertMethod(Eleccion.class, "isComenzo");
       AssertAnnotations.assertMethod(Eleccion.class, "isHabilitadaParaVotar");
       AssertAnnotations.assertMethod(Eleccion.class, "isMigrada");
       AssertAnnotations.assertMethod(Eleccion.class, "getIdMigracion");
       AssertAnnotations.assertMethod(Eleccion.class, "getCategoria");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Eleccion.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       OneToMany oa;
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "idEleccion", Column.class);
       assertEquals("id_eleccion", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "idMigracion", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "categoria", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "migrada", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "fechaInicio", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "fechaFin", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "fechaCreacion", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "tituloEspanol", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "tituloIngles", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "tituloPortugues", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "linkEspanol", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "linkIngles", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "linkPortugues", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "descripcionEspanol", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "descripcionIngles", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "descripcionPortugues", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "maxCandidatos", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "habilitadoLinkVotacion", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "habilitadoLinkResultado", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "habilitadoLinkAuditor", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "solicitarRevision", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "solosp", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "tokenResultado", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "remitentePorDefecto", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "padronSeteado", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "candidatosSeteado", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "auditoresSeteado", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "candidatosAleatorios", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Eleccion.class, "diffUTC", Column.class);
       assertEquals("", c.name());
       oa = ReflectTool.getFieldAnnotation(Eleccion.class, "candidatos", OneToMany.class);
       assertEquals("eleccion", oa.mappedBy());
       oa = ReflectTool.getFieldAnnotation(Eleccion.class, "auditores", OneToMany.class);
       assertEquals("eleccion", oa.mappedBy());
       oa = ReflectTool.getFieldAnnotation(Eleccion.class, "usuariosPadron", OneToMany.class);
       assertEquals("eleccion", oa.mappedBy());
       oa = ReflectTool.getFieldAnnotation(Eleccion.class, "templatesEleccion", OneToMany.class);
       assertEquals("eleccion", oa.mappedBy());
       oa = ReflectTool.getFieldAnnotation(Eleccion.class, "votos", OneToMany.class);
       assertEquals("eleccion", oa.mappedBy());
       oa = ReflectTool.getFieldAnnotation(Eleccion.class, "email", OneToMany.class);
       assertEquals("eleccion", oa.mappedBy());
       
    
    
    }

}
