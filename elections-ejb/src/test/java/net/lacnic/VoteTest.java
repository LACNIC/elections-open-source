package net.lacnic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.elections.domain.Vote;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

public class VoteTest extends TestCase {
	/**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( VoteTest.class );
    }
    
    public void testVote()
    {
    	AssertAnnotations.assertType(Vote.class, Entity.class);
    	
   	   // fields
       AssertAnnotations.assertField(Vote.class, "voteId", Id.class, GeneratedValue.class, SequenceGenerator.class, Column.class);
       AssertAnnotations.assertField(Vote.class, "code", Column.class);
       AssertAnnotations.assertField(Vote.class, "ip", Column.class);
       AssertAnnotations.assertField(Vote.class, "voteDate", Column.class);
       AssertAnnotations.assertField(Vote.class, "candidate", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(Vote.class, "election", JoinColumn.class, ManyToOne.class);
       AssertAnnotations.assertField(Vote.class, "userVoter", JoinColumn.class, ManyToOne.class);
       
       //metodos       
       AssertAnnotations.assertMethod(Vote.class, "getVoteId");
       AssertAnnotations.assertMethod(Vote.class, "getElection");
       AssertAnnotations.assertMethod(Vote.class, "getCode");
       AssertAnnotations.assertMethod(Vote.class, "getCandidate");
       AssertAnnotations.assertMethod(Vote.class, "getIp");
       AssertAnnotations.assertMethod(Vote.class, "getVoteDate");
       AssertAnnotations.assertMethod(Vote.class, "getUserVoter");
       
       //class annotations
       Entity a = ReflectTool.getClassAnnotation(Vote.class, Entity.class);
       assertEquals("", a.name());
       
       
       Column c;
       JoinColumn jc;
       c = ReflectTool.getFieldAnnotation(Vote.class, "voteId", Column.class);
       assertEquals("vote_id", c.name());
       c = ReflectTool.getFieldAnnotation(Vote.class, "code", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Vote.class, "ip", Column.class);
       assertEquals("", c.name());
       c = ReflectTool.getFieldAnnotation(Vote.class, "voteDate", Column.class);
       assertEquals("", c.name());
       jc = ReflectTool.getFieldAnnotation(Vote.class, "candidate", JoinColumn.class);
       assertEquals("candidate_id", jc.name());
       jc = ReflectTool.getFieldAnnotation(Vote.class, "election", JoinColumn.class);
       assertEquals("election_id", jc.name());
       jc = ReflectTool.getFieldAnnotation(Vote.class, "userVoter", JoinColumn.class);
       assertEquals("uservoter_id", jc.name());
       
       
    }

}
