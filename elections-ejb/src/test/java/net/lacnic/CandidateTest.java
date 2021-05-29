package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Candidate;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

public class CandidateTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CandidateTest.class );
    }
    
    public void testCandidate()
    {
    	AssertAnnotations.assertType(Candidate.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Candidate.class, "idCandidate", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(Candidate.class, "idMigration", Column.class);
       AssertAnnotations.assertField(Candidate.class, "name", Column.class);
       AssertAnnotations.assertField(Candidate.class, "election", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(Candidate.class, "pictureInfo", Column.class);
       AssertAnnotations.assertField(Candidate.class, "pictureName", Column.class);
       AssertAnnotations.assertField(Candidate.class, "bioSpanish", Column.class);
       AssertAnnotations.assertField(Candidate.class, "bioEnglish", Column.class);
       AssertAnnotations.assertField(Candidate.class, "bioPortuguese", Column.class);
       AssertAnnotations.assertField(Candidate.class, "pictureExtension", Column.class);
       AssertAnnotations.assertField(Candidate.class, "candidateOrder", Column.class);
       AssertAnnotations.assertField(Candidate.class, "onlySp", Column.class);
       AssertAnnotations.assertField(Candidate.class, "linkSpanish", Column.class);
       AssertAnnotations.assertField(Candidate.class, "linkEnglish", Column.class);
       AssertAnnotations.assertField(Candidate.class, "linkPortuguese", Column.class);
       AssertAnnotations.assertField(Candidate.class, "votes", OneToMany.class);
       
       //metodos       
       AssertAnnotations.assertMethod(Candidate.class, "getIdCandidate");
       AssertAnnotations.assertMethod(Candidate.class, "getName");
       AssertAnnotations.assertMethod(Candidate.class, "getElection");
       AssertAnnotations.assertMethod(Candidate.class, "getPictureInfo");
       AssertAnnotations.assertMethod(Candidate.class, "getPictureName");
       AssertAnnotations.assertMethod(Candidate.class, "getPictureExtension");
       AssertAnnotations.assertMethod(Candidate.class, "getVotes");              
       AssertAnnotations.assertMethod(Candidate.class, "getBioSpanish");
       AssertAnnotations.assertMethod(Candidate.class, "getBioEnglish");
       AssertAnnotations.assertMethod(Candidate.class, "getBioPortuguese");
       AssertAnnotations.assertMethod(Candidate.class, "getCandidateOrder");
       AssertAnnotations.assertMethod(Candidate.class, "isOnlySp");
       AssertAnnotations.assertMethod(Candidate.class, "isFixed");
       AssertAnnotations.assertMethod(Candidate.class, "getLinkSpanish");
       AssertAnnotations.assertMethod(Candidate.class, "getLinkEnglish");
       AssertAnnotations.assertMethod(Candidate.class, "getLinkPortuguese");
       AssertAnnotations.assertMethod(Candidate.class, "getIdMigration");
    
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Candidate.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       JoinColumn jc;
       OneToMany oa;
       c = ReflectTool.getFieldAnnotation(Candidate.class, "idCandidate", Column.class);
       assertEquals("id_candidate", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "idMigration", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "name", Column.class);
       assertEquals("", c.name());
       jc = ReflectTool.getFieldAnnotation(Candidate.class, "election", JoinColumn.class);
       assertEquals("id_election", jc.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "pictureInfo", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "pictureName", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "bioSpanish", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "bioEnglish", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "bioPortuguese", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "pictureExtension", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "candidateOrder", Column.class);
       assertEquals("candidate_order", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "onlySp", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "linkSpanish", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "linkEnglish", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Candidate.class, "linkPortuguese", Column.class);
       assertEquals("", c.name());
       oa = ReflectTool.getFieldAnnotation(Candidate.class, "votes", OneToMany.class);
       assertEquals("candidate", oa.mappedBy());
       
       
    }

}
