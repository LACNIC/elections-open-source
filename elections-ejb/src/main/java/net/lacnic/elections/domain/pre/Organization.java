package net.lacnic.elections.domain.pre;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.utils.LinksUtils;
import net.lacnic.evra.registro.CategoriasEnum;

@Entity
public class Organization implements Serializable {

	private static final long serialVersionUID = -1076172074587070137L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_seq")
	@SequenceGenerator(name = "organization_seq", sequenceName = "organization_seq", allocationSize = 1, initialValue = 90000)
	@Column(name = "id")
	private long id;

	@Column(nullable = false)
	private String orgId;

	@Column(nullable = false, length = 1000)
	private String name;

	@Column(nullable = true)
	private Integer votes;

	@Convert(converter = OrganizationCategoryConverter.class)
	@Column(nullable = true)
	private CategoriasEnum category;

	@Column(nullable = true)
	private String country;

	@Column(nullable = true)
	private String cnpj;

	@Column(nullable = true, length = 1000)
	private String asn;

	@Column(nullable = true, length = 1000)
	private String membershipContactId;

	@Column(nullable = true, length = 1000)
	private String membershipContactName;

	@Column(nullable = true, length = 1000)
	private String membershipContactEmail;

	@Convert(converter = LanguageCodeConverter.class)
	@Column(nullable = true)
	private LanguageCode membershipContactLanguage;

	@Column(nullable = true)
	private String doNominationToken;

	@Column(nullable = false)
	private boolean deudor;

	@Column(nullable = true)
	private Boolean member;

	@Column(name = "created_at", nullable = false)
	private Date createdAt;

	@Column(name = "updated_at", nullable = false)
	private Date updatedAt;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id", nullable = false)
	private Election election;

	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Nomination> nominations;

	@OneToMany(mappedBy = "supportingOrganization", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SupportNomination> supportNominations;

	public Organization() {
		// Default constructor for JPA
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getVotes() {
		return votes;
	}

	public void setVotes(Integer votes) {
		this.votes = votes;
	}

	public String getCategory() {
		return category == null ? null : category.getNombreTabla();
	}

	public void setCategory(String category) {
		this.category = CategoriasEnum.fromValue(category);
	}

	public CategoriasEnum getCategoryEnum() {
		return category;
	}

	public void setCategoryEnum(CategoriasEnum category) {
		this.category = category;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public String getAsn() {
		return asn;
	}

	public void setAsn(String asn) {
		this.asn = asn;
	}

	public String getMembershipContactId() {
		return membershipContactId;
	}

	public void setMembershipContactId(String membershipContactId) {
		this.membershipContactId = membershipContactId;
	}

	public String getMembershipContactName() {
		return membershipContactName;
	}

	public void setMembershipContactName(String membershipContactName) {
		this.membershipContactName = membershipContactName;
	}

	public String getMembershipContactEmail() {
		return membershipContactEmail;
	}

	public void setMembershipContactEmail(String membershipContactEmail) {
		this.membershipContactEmail = membershipContactEmail;
	}

	public String getMembershipContactLanguage() {
		return membershipContactLanguage == null ? null : membershipContactLanguage.getCode();
	}

	public void setMembershipContactLanguage(String membershipContactLanguage) {
		this.membershipContactLanguage = LanguageCode.fromValue(membershipContactLanguage);
	}

	public LanguageCode getMembershipContactLanguageEnum() {
		return membershipContactLanguage;
	}

	public void setMembershipContactLanguageEnum(LanguageCode membershipContactLanguage) {
		this.membershipContactLanguage = membershipContactLanguage;
	}

	public String getDoNominationToken() {
		return doNominationToken;
	}

	public String getDoNominationLink() {
		return LinksUtils.buildDoNominationLink(doNominationToken);
	}

	public void setDoNominationToken(String doNominationToken) {
		this.doNominationToken = doNominationToken;
	}

	public boolean isDeudor() {
		return deudor;
	}

	public void setDeudor(boolean deudor) {
		this.deudor = deudor;
	}

	public boolean isMember() {
		return member == null || Boolean.TRUE.equals(member);
	}

	public void setMember(boolean member) {
		this.member = member;
	}

	public Boolean getMemberValue() {
		return member;
	}

	public void setMemberValue(Boolean member) {
		this.member = member;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public List<Nomination> getNominations() {
		return nominations;
	}

	public void setNominations(List<Nomination> nominations) {
		this.nominations = nominations;
	}

	public List<SupportNomination> getSupportNominations() {
		return supportNominations;
	}

	public void setSupportNominations(List<SupportNomination> supportNominations) {
		this.supportNominations = supportNominations;
	}

	@PrePersist
	private void onCreateTimestamps() {
		Date now = new Date();
		if (createdAt == null) {
			createdAt = now;
		}
		updatedAt = now;
	}

	@PreUpdate
	private void onUpdateTimestamp() {
		updatedAt = new Date();
	}
}
