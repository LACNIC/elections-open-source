package net.lacnic.elections.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;

import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionLight;
import net.lacnic.elections.domain.JointElection;


public class ElectionDao {

	private EntityManager em;


	public ElectionDao(EntityManager em) {
		this.em = em;
	}
	
	public Election getElection(long electionId) {
		TypedQuery<Election> q = em.createQuery("SELECT e FROM Election e WHERE e.electionId = :electionId", Election.class);
		q.setParameter("electionId", electionId);
		return q.getSingleResult();
	}

	public List<Election> getElections() {
		TypedQuery<Election> q = em.createQuery("SELECT e FROM Election e", Election.class);
		return q.getResultList();
	}

	public Election getElectionByResultToken(String resultToken) {
		TypedQuery<Election> q = em.createQuery("SELECT e FROM Election e WHERE e.resultToken = :resultToken", Election.class);
		q.setParameter("resultToken", resultToken);
		return q.getSingleResult();
	}

	public List<Election> getElectionsAllOrderCreationDate() {
		TypedQuery<Election> q = em.createQuery("SELECT e FROM Election e ORDER BY e.creationDate", Election.class);
		return q.getResultList();
	}

	public List<Election> getElectionsAllOrderStartDateDesc() {
		TypedQuery<Election> q = em.createQuery("SELECT e FROM Election e ORDER BY e.startDate DESC", Election.class);
		return q.getResultList();
	}

	public List<ElectionLight> getElectionsLightAllOrderStartDateDesc() {
		TypedQuery<ElectionLight> q = em.createQuery("SELECT e FROM ElectionLight e ORDER BY e.startDate DESC", ElectionLight.class);
		return q.getResultList();
	}

	public List<Election> getElectionsLightThisYear() {
		TypedQuery<Election> q = em.createQuery("SELECT e FROM Election e WHERE e.creationDate > :startDate ORDER BY e.creationDate DESC", Election.class);
		Date startDate = new DateTime().minusYears(1).toDate();
		q.setParameter("startDate", startDate);
		return q.getResultList();
	}

	public boolean oneElectionExists() {
		Query q = em.createQuery("SELECT e.electionId FROM Election e");
		q.setMaxResults(1);
		return !q.getResultList().isEmpty();
	}

	public boolean electionIsSimple(long electionId) {
		Query q = em.createQuery("SELECT e FROM JointElection e WHERE e.idElectionA = :electionId OR e.idElectionB = :electionId");
		q.setParameter("electionId", electionId);

		return (q.getResultList() == null || q.getResultList().isEmpty());
	}

	public JointElection getJointElectionForElection(long electionId) {
		TypedQuery<JointElection> q = em.createQuery("SELECT e FROM JointElection e WHERE e.idElectionA = :electionId OR e.idElectionB = :electionId", JointElection.class);
		q.setParameter("electionId", electionId);
		return q.getSingleResult();
	}

	public List<JointElection> getJointElectionsAll() {
		TypedQuery<JointElection> q = em.createQuery("SELECT e FROM JointElection e", JointElection.class);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getElectionsAllIdAndTitle() {
		Query q = em.createQuery("SELECT e.electionId, e.titleSpanish FROM Election e ORDER BY e.electionId");
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getElectionsAllIdAndDescription(int pageSize, int offset) {
		Query q = em.createQuery("SELECT e.electionId, e.titleSpanish FROM Election e ORDER BY e.electionId");
		q.setMaxResults(pageSize);
		q.setFirstResult(offset * pageSize);
		return q.getResultList();
	}

}
