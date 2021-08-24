package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.Customization;
import net.lacnic.elections.domain.JointElection;

public class JointElectionDao {
	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	private EntityManager em;


	public JointElectionDao(EntityManager em) {
		this.em = em;
	}

	public List<Long> getJointElections() {
		Query q = em.createQuery("SELECT j.jointElectionId FROM JointElection j order by j.jointElectionId");
		return q.getResultList();
	}
	
	public JointElection getJointElection(Long jointElectionId) {
		TypedQuery<JointElection> q = em.createQuery("SELECT j FROM JointElection j WHERE j.jointElectionId = :jointElectionId", JointElection.class);
		q.setParameter("jointElectionId", jointElectionId);
		return q.getSingleResult();
	}
}
