package net.lacnic.elections.domain.pre;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.utils.LinksUtils;

@Entity
public class Nomination implements Serializable {

	private static final long serialVersionUID = -7385996519688150824L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nomination_seq")
	@SequenceGenerator(name = "nomination_seq", sequenceName = "nomination_seq", allocationSize = 1, initialValue = 10000)
	@Column(name = "id")
	private long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id", nullable = false)
	private Election election;

	@ManyToOne(optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private Organization organization;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private NominationStatus status;

	@OneToMany(mappedBy = "nomination", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SupportNomination> supports;

	@OneToOne(optional = true)
	@JoinColumn(name = "candidate_id", nullable = true, unique = true)
	private Candidate candidate;

	@Column(nullable = true, length = 1000)
	private String acceptNominationToken;

	@Column
	private String nominationDate;

	@Column
	private String nominationName;

	@Column
	private String nominationEmail;

	@Column
	private String nominationPhoneNumber;

	@Column(length = 2000)
	private String nominationReasonSpanish;

	@Column(length = 2000)
	private String nominationReasonEnglish;

	@Column(length = 2000)
	private String nominationReasonPortuguese;

	// RESTO DE LOS DATOS DEL CANDIDATO PENDIENTE INCLUIDO RESPUESTAS A PREGUNTAS

	public Nomination() {
		// Default constructor for JPA
	}

	public Nomination(Election election, Organization organization, NominationStatus status, String acceptNominationToken, String nominationDate, String nominationName, String nominationEmail,
			String nominationPhoneNumber, String nominationReason) {
		this(election, organization, status, acceptNominationToken, nominationDate, nominationName, nominationEmail, nominationPhoneNumber, nominationReason, nominationReason, nominationReason);
	}

	public Nomination(Election election, Organization organization, NominationStatus status, String acceptNominationToken, String nominationDate, String nominationName, String nominationEmail,
			String nominationPhoneNumber, String nominationReasonSpanish, String nominationReasonEnglish, String nominationReasonPortuguese) {
		this.election = election;
		this.organization = organization;
		this.status = status;
		this.acceptNominationToken = acceptNominationToken;
		this.nominationDate = nominationDate;
		this.nominationName = nominationName;
		this.nominationEmail = nominationEmail;
		this.nominationPhoneNumber = nominationPhoneNumber;
		this.nominationReasonSpanish = nominationReasonSpanish;
		this.nominationReasonEnglish = nominationReasonEnglish;
		this.nominationReasonPortuguese = nominationReasonPortuguese;
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

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public NominationStatus getStatus() {
		return status;
	}

	public void setStatus(NominationStatus status) {
		this.status = status;
	}

	public List<SupportNomination> getSupports() {
		return supports;
	}

	public void setSupports(List<SupportNomination> supports) {
		this.supports = supports;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public String getAcceptNominationToken() {
		return acceptNominationToken;
	}

	public String getAcceptNominationLink() {
		return LinksUtils.buildAcceptNominationLink(acceptNominationToken);
	}

	public void setAcceptNominationToken(String acceptNominationToken) {
		this.acceptNominationToken = acceptNominationToken;
	}

	public String getNominationDate() {
		return nominationDate;
	}

	public void setNominationDate(String nominationDate) {
		this.nominationDate = nominationDate;
	}

	public String getNominationName() {
		return nominationName;
	}

	public void setNominationName(String nominationName) {
		this.nominationName = nominationName;
	}

	public String getNominationEmail() {
		return nominationEmail;
	}

	public void setNominationEmail(String nominationEmail) {
		this.nominationEmail = nominationEmail;
	}

	public String getNominationPhoneNumber() {
		return nominationPhoneNumber;
	}

	public void setNominationPhoneNumber(String nominationPhoneNumber) {
		this.nominationPhoneNumber = nominationPhoneNumber;
	}

	public String getNominationReason() {
		if (hasText(nominationReasonSpanish)) {
			return nominationReasonSpanish;
		}
		if (hasText(nominationReasonEnglish)) {
			return nominationReasonEnglish;
		}
		return nominationReasonPortuguese;
	}

	public String getNominationReason(String displayName) {
		LanguageCode languageCode = LanguageCode.fromValueOrDefault(displayName, LanguageCode.SP);
		switch (languageCode) {
		case EN:
			if (hasText(getNominationReasonEnglish())) {
				return getNominationReasonEnglish();
			}
			break;
		case PT:
			if (hasText(getNominationReasonPortuguese())) {
				return getNominationReasonPortuguese();
			}
			break;
		case SP:
		default:
			if (hasText(getNominationReasonSpanish())) {
				return getNominationReasonSpanish();
			}
			break;
		}
		return getNominationReason();
	}

	public void setNominationReason(String nominationReason) {
		this.nominationReasonSpanish = nominationReason;
		this.nominationReasonEnglish = nominationReason;
		this.nominationReasonPortuguese = nominationReason;
	}

	public String getNominationReasonSpanish() {
		return nominationReasonSpanish;
	}

	public void setNominationReasonSpanish(String nominationReasonSpanish) {
		this.nominationReasonSpanish = nominationReasonSpanish;
	}

	public String getNominationReasonEnglish() {
		return nominationReasonEnglish;
	}

	public void setNominationReasonEnglish(String nominationReasonEnglish) {
		this.nominationReasonEnglish = nominationReasonEnglish;
	}

	public String getNominationReasonPortuguese() {
		return nominationReasonPortuguese;
	}

	public void setNominationReasonPortuguese(String nominationReasonPortuguese) {
		this.nominationReasonPortuguese = nominationReasonPortuguese;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

}
