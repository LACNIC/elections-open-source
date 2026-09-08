package net.lacnic.elections.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;

@Entity
public class ElectionRestrictedCountry implements Serializable {

	private static final long serialVersionUID = 5582754956062424151L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "election_restricted_country_seq")
	@SequenceGenerator(name = "election_restricted_country_seq", sequenceName = "election_restricted_country_seq", allocationSize = 1, initialValue = 85000)
	@Column(name = "id")
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id", nullable = false)
	private Election election;

	@Column(nullable = false, length = 3)
	private String countryCode;

	public ElectionRestrictedCountry() {
		// Default constructor for JPA
	}

	public ElectionRestrictedCountry(Election election, String countryCode) {
		this.election = election;
		this.countryCode = countryCode;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
}
