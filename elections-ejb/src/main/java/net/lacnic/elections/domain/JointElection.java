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
	@Column(name = "id")
	private long id;

	private long idElectionA;

	private long idElectionB;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}