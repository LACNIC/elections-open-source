package net.lacnic.elections.domain.pre;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import net.lacnic.elections.domain.Candidate;

@Entity
public class CandidateWorkOrganization implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workorganization_seq")
	@SequenceGenerator(name = "workorganization_seq", sequenceName = "workorganization_seq", allocationSize = 1, initialValue = 60000)
	@Column(name = "id")
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "candidate_id", nullable = false)
	private Candidate candidate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private WorkOrganizationType workOrganizationType;

	@Column(nullable = false, length = 1000)
	private String organizationName;

	@Column(nullable = true, length = 4000)
	private String organizationGroup;

	public CandidateWorkOrganization() {
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

	public WorkOrganizationType getWorkOrganizationType() {
		return workOrganizationType;
	}

	public void setWorkOrganizationType(WorkOrganizationType workOrganizationType) {
		this.workOrganizationType = workOrganizationType;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getOrganizationGroup() {
		return organizationGroup;
	}

	public void setOrganizationGroup(String organizationGroup) {
		this.organizationGroup = organizationGroup;
	}
}
