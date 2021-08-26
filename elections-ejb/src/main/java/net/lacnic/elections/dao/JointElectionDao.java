package net.lacnic.elections.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import net.lacnic.elections.domain.JointElection;


public class JointElectionDao {

	private EntityManager em;


	public JointElectionDao(EntityManager em) {
		this.em = em;
	}

	public List<Long> getJointElectionsIds() {
		TypedQuery<Long> q = em.createQuery("SELECT j.jointElectionId FROM JointElection j ORDER BY j.jointElectionId", Long.class);
		return q.getResultList();
	}

	public JointElection getJointElection(Long jointElectionId) {
		TypedQuery<JointElection> q = em.createQuery("SELECT j FROM JointElection j WHERE j.jointElectionId = :jointElectionId", JointElection.class);
		q.setParameter("jointElectionId", jointElectionId);
		return q.getSingleResult();
	}

}
