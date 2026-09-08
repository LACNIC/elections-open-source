package net.lacnic.elections.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
public class JointElection implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jointelection_seq")
	@SequenceGenerator(name = "jointelection_seq", sequenceName = "jointelection_seq", allocationSize = 1)
	@Column(name = "jointelection_id")
	private long jointElectionId;

	@Column(name = "electiona_id", nullable = false)
	private long idElectionA;

	@Column(name = "electionb_id", nullable = false)
	private long idElectionB;

	public JointElection() {
		// Intencionalmente vacio: JPA lo requiere para materializar la entidad.
	}

	public long getJointElectionId() {
		return jointElectionId;
	}

	public void setJointElectionId(long jointElectionId) {
		this.jointElectionId = jointElectionId;
	}

	public long getIdElectionA() {
		return idElectionA;
	}

	public void setIdElectionA(long idElectionA) {
		this.idElectionA = idElectionA;
	}

	public long getIdElectionB() {
		return idElectionB;
	}

	public void setIdElectionB(long idElectionB) {
		this.idElectionB = idElectionB;
	}

}
