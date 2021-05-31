package net.lacnic.elections.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;

import net.lacnic.elections.data.Participation;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionLight;
import net.lacnic.elections.domain.JointElection;


public class EleccionDao {

	private EntityManager em;


	public EleccionDao(EntityManager em) {
		this.em = em;
	}

	public Election getElection(long electionId) {
		TypedQuery<Election> q = em.createQuery("SELECT e FROM Election e WHERE e.electionId = :electionId", Election.class);
		q.setParameter("electionId", electionId);
		return q.getSingleResult();
	}

	public Election getElectionByResultToken(String resultToken) {
		TypedQuery<Election> q = em.createQuery("SELECT e FROM Election e WHERE e.resultToken = :resultToken", Election.class);
		q.setParameter("resultToken", resultToken);
		return q.getSingleResult();
	}

	//	public Election obtenerEleccionConTokenAuditor(String token) {		
	//		return getElectionByResultToken(token);
	//	}

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

	public List<Participation> getParticipationsForOrganization(String orgID) {
		Query q = em.createQuery("SELECT u.name, u.mail, u.country, u.voted,"
				+ " e.startDate, e.endDate, e.titleSpanish, e.titleEnglish, e.titlePortuguese, e.category"
				+ " FROM UserVoter u, Election e"
				+ " WHERE u.orgID = :orgID AND u.election.electionId = e.electionId");
		q.setParameter("orgID", orgID);
		List<Object[]> result = q.getResultList();

		List<Participation> resultList = new ArrayList<>();
		for (int i = 0; i < result.size(); i++) {
			Participation p = new Participation();
			p.setNombre((String) result.get(i)[0]);
			p.setEmail((String) result.get(i)[1]);
			p.setPais((String) result.get(i)[2]);
			p.setYaVoto((Boolean) result.get(i)[3]);
			p.setFechaInicioEleccion((Date) result.get(i)[4]);
			p.setFechaFinEleccion((Date) result.get(i)[5]);
			p.setTituloEleccionSP((String) result.get(i)[6]);
			p.setTituloEleccionEN((String) result.get(i)[7]);
			p.setTituloEleccionPT((String) result.get(i)[8]);
			p.setCategoria((String) result.get(i)[9]);
			resultList.add(p);
		}
		return resultList;
	}

	//	public List<Participation> obtenerElecciones(String org) {
	//		return getParticipationsForOrganization(org);
	//	}

	public boolean isEleccionSimple(long electionId) {
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

	public List<String> getElectionsAllIdAndTitle() {
		Query q = em.createQuery("SELECT e.electionId, e.titleSpanish FROM Election e ORDER BY e.electionId");
		List<Object[]> result = q.getResultList();

		List<String> resultList = new ArrayList<>();

		for (int i = 0; i < result.size(); i++) {
			resultList.add(result.get(i)[0].toString() + "-" + result.get(i)[1].toString());
		}
		return resultList;
	}

}