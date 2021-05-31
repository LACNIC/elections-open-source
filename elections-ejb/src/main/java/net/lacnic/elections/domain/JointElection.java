package net.lacnic.elections.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;


@Entity
public class JointElection implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jointelection_seq")
	@SequenceGenerator(name = "jointelection_seq", sequenceName = "jointelection_seq", allocationSize = 1)
	@Column(name = "jointelection_id")
	private long jointElectionId;

	@Column
	private long idElectionA;

	@Column
	private long idElectionB;


	public JointElection() { }


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
