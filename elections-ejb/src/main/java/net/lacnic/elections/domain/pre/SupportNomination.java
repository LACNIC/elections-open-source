package net.lacnic.elections.domain.pre;

import java.io.Serializable;
import java.util.Date;

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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import net.lacnic.elections.utils.LinksUtils;

@Entity
public class SupportNomination implements Serializable {

	private static final long serialVersionUID = 7342313110576198424L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "supportnomination_seq")
	@SequenceGenerator(name = "supportnomination_seq", sequenceName = "supportnomination_seq", allocationSize = 1, initialValue = 70000)
	@Column(name = "id")
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "nomination_id", nullable = false)
	private Nomination nomination;

	@ManyToOne(optional = true)
	@JoinColumn(name = "supporting_organization_id", nullable = true)
	private Organization supportingOrganization;

	@Column(nullable = true)
	private String supportingContactName;

	@Column(nullable = true)
	private String supportingContactEmail;

	@Column(nullable = false, length = 1000)
	private String token;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private SupportStatus supportStatus;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = true)
	private Date supportStatusDate;

	public SupportNomination() {
		// Default constructor for JPA
	}

	public SupportNomination(Nomination nomination, Organization supportingOrganization, String supportingContactName, String supportingContactEmail, String token, SupportStatus supportStatus) {
		this.nomination = nomination;
		this.supportingOrganization = supportingOrganization;
		this.supportingContactName = supportingContactName;
		this.supportingContactEmail = supportingContactEmail;
		this.token = token;
		this.supportStatus = supportStatus;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Nomination getNomination() {
		return nomination;
	}

	public void setNomination(Nomination nomination) {
		this.nomination = nomination;
	}

	public Organization getSupportingOrganization() {
		return supportingOrganization;
	}

	public void setSupportingOrganization(Organization supportingOrganization) {
		this.supportingOrganization = supportingOrganization;
	}

	public String getSupportingContactName() {
		return supportingContactName;
	}

	public void setSupportingContactName(String supportingContactName) {
		this.supportingContactName = supportingContactName;
	}

	public String getSupportingContactEmail() {
		return supportingContactEmail;
	}

	public void setSupportingContactEmail(String supportingContactEmail) {
		this.supportingContactEmail = supportingContactEmail;
	}

	public String getToken() {
		return token;
	}

	public String getSupportNominationLink() {
		return LinksUtils.buildSupportNominationLink(token);
	}

	public void setToken(String token) {
		this.token = token;
	}

	public SupportStatus getSupportStatus() {
		return supportStatus;
	}

	public void setSupportStatus(SupportStatus supportStatus) {
		this.supportStatus = supportStatus;
	}

	public Date getSupportStatusDate() {
		return supportStatusDate;
	}

	public void setSupportStatusDate(Date supportStatusDate) {
		this.supportStatusDate = supportStatusDate;
	}

	public Date getSupportResponseInstant() {
		return supportStatusDate;
	}

	public void setSupportResponseInstant(Date supportResponseInstant) {
		this.supportStatusDate = supportResponseInstant;
	}
}
