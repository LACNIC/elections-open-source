package net.lacnic.elections.domain.pre;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import net.lacnic.elections.domain.Candidate;

@Entity
public class CandidateCountryLink implements Serializable {

	private static final long serialVersionUID = 248692319213321901L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidate_countrylink_seq")
	@SequenceGenerator(name = "candidate_countrylink_seq", sequenceName = "candidate_countrylink_seq", allocationSize = 1, initialValue = 60000)
	@Column(name = "id")
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "candidate_id", nullable = false)
	private Candidate candidate;

	@Column(nullable = false)
	private boolean primaryCountry;

	@Column(nullable = false, length = 3)
	private String countryCode;

	// checks
	@Column(nullable = true)
	private boolean qCitizen;

	@Column(nullable = true)
	private boolean qResidenceOver5y;

	@Column(nullable = true)
	private boolean qLongEmploymentOrAdvisory5y;

	@Column(nullable = true)
	private boolean qFamilyResidenceOver5y;

	@Column(nullable = true)
	private boolean qInternetCommunityOrgParticipation;

	@Column(nullable = true)
	private boolean qEligibleForCitizenship;

	public CandidateCountryLink() {
		// Default constructor for JPA
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public boolean isPrimaryCountry() {
		return primaryCountry;
	}

	public void setPrimaryCountry(boolean primaryCountry) {
		this.primaryCountry = primaryCountry;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public boolean isQCitizen() {
		return qCitizen;
	}

	public void setQCitizen(boolean qCitizen) {
		this.qCitizen = qCitizen;
	}

	public boolean isQResidenceOver5y() {
		return qResidenceOver5y;
	}

	public void setQResidenceOver5y(boolean qResidenceOver5y) {
		this.qResidenceOver5y = qResidenceOver5y;
	}

	public boolean isQLongEmploymentOrAdvisory5y() {
		return qLongEmploymentOrAdvisory5y;
	}

	public void setQLongEmploymentOrAdvisory5y(boolean qLongEmploymentOrAdvisory5y) {
		this.qLongEmploymentOrAdvisory5y = qLongEmploymentOrAdvisory5y;
	}

	public boolean isQFamilyResidenceOver5y() {
		return qFamilyResidenceOver5y;
	}

	public void setQFamilyResidenceOver5y(boolean qFamilyResidenceOver5y) {
		this.qFamilyResidenceOver5y = qFamilyResidenceOver5y;
	}

	public boolean isQInternetCommunityOrgParticipation() {
		return qInternetCommunityOrgParticipation;
	}

	public void setQInternetCommunityOrgParticipation(boolean qInternetCommunityOrgParticipation) {
		this.qInternetCommunityOrgParticipation = qInternetCommunityOrgParticipation;
	}

	public boolean isQEligibleForCitizenship() {
		return qEligibleForCitizenship;
	}

	public void setQEligibleForCitizenship(boolean qEligibleForCitizenship) {
		this.qEligibleForCitizenship = qEligibleForCitizenship;
	}
}
