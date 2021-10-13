package net.lacnic.elections.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

import net.lacnic.elections.utils.Constants;

@Entity
public class Candidate implements Serializable {

	private static final long serialVersionUID = 574501011615594210L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidate_seq")
	@SequenceGenerator(name = "candidate_seq", sequenceName = "candidate_seq", allocationSize = 1)
	@Column(name = "candidate_id")
	private long candidateId;

	@Column(nullable = true, name = "migration_id")
	private Long migrationId;

	@Column(nullable = false, length = 1000)
	private String name;

	@ManyToOne(optional = false)
	@JoinColumn(name = "election_id")
	private Election election;

	@Column(nullable = false)
	private byte[] pictureInfo;

	@Column(nullable = false)
	private String pictureName;

	@Column(columnDefinition = "TEXT")
	private String bioSpanish;

	@Column(columnDefinition = "TEXT")
	private String bioEnglish;

	@Column(columnDefinition = "TEXT")
	private String bioPortuguese;

	@Column(nullable = false)
	private String pictureExtension;

	@Column
	private int candidateOrder;

	@Column(nullable = true)
	private boolean onlySp;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String linkSpanish;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String linkEnglish;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String linkPortuguese;

	@OneToMany(mappedBy = "candidate", cascade = CascadeType.REMOVE)
	private List<Vote> votes;

	@Column(nullable = true)
	private String mail;


	public Candidate() { }

	public String getBio(String displayName) {
		if (displayName.toLowerCase().contains("en") || displayName.toLowerCase().contains("english"))
			return getBioEnglish();
		else if (displayName.toLowerCase().contains("pt") || displayName.toLowerCase().contains("portuguese"))
			return getBioPortuguese();
		return getBioSpanish();
	}

	public String getLink(String displayName) {
		if (displayName.toLowerCase().contains("en") || displayName.toLowerCase().contains("english"))
			return getLinkEnglish();
		else if (displayName.toLowerCase().contains("pt") || displayName.toLowerCase().contains("portuguese"))
			return getLinkPortuguese();
		return getLinkSpanish();
	}

	public void copyBioToOtherLanguages() {
		setBioEnglish(getBioSpanish());
		setBioPortuguese(getBioSpanish());
		setLinkEnglish(getLinkSpanish());
		setLinkPortuguese(getLinkSpanish());
	}

	public void clean() {
		this.name = null;
		this.pictureInfo = null;
		this.pictureName = null;
		this.bioSpanish = null;
		this.bioEnglish = null;
		this.bioPortuguese = null;
		this.pictureExtension = null;
		this.mail = null;
	}

	public boolean isFixed() {
		return getCandidateOrder() == Constants.MIN_ORDER || getCandidateOrder() == Constants.MAX_ORDER;
	}


	public long getCandidateId() {
		return candidateId;
	}

	public void setCandidateId(long candidateId) {
		this.candidateId = candidateId;
	}

	public Long getMigrationId() {
		return migrationId;
	}

	public void setMigrationId(Long migrationId) {
		this.migrationId = migrationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public byte[] getPictureInfo() {
		return pictureInfo;
	}

	public void setPictureInfo(byte[] pictureInfo) {
		this.pictureInfo = pictureInfo;
	}

	public String getPictureName() {
		return pictureName;
	}

	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
	}

	public String getBioSpanish() {
		return bioSpanish;
	}

	public void setBioSpanish(String bioSpanish) {
		this.bioSpanish = bioSpanish;
	}

	public String getBioEnglish() {
		return bioEnglish;
	}

	public void setBioEnglish(String bioEnglish) {
		this.bioEnglish = bioEnglish;
	}

	public String getBioPortuguese() {
		return bioPortuguese;
	}

	public void setBioPortuguese(String bioPortuguese) {
		this.bioPortuguese = bioPortuguese;
	}

	public String getPictureExtension() {
		return pictureExtension;
	}

	public void setPictureExtension(String pictureExtension) {
		this.pictureExtension = pictureExtension;
	}

	public int getCandidateOrder() {
		return candidateOrder;
	}

	public void setCandidateOrder(int candidateOrder) {
		this.candidateOrder = candidateOrder;
	}

	public boolean isOnlySp() {
		return onlySp;
	}

	public void setOnlySp(boolean onlySp) {
		this.onlySp = onlySp;
	}

	public String getLinkSpanish() {
		return linkSpanish;
	}

	public void setLinkSpanish(String linkSpanish) {
		this.linkSpanish = linkSpanish;
	}

	public String getLinkEnglish() {
		return linkEnglish;
	}

	public void setLinkEnglish(String linkEnglish) {
		this.linkEnglish = linkEnglish;
	}

	public String getLinkPortuguese() {
		return linkPortuguese;
	}

	public void setLinkPortuguese(String linkPortuguese) {
		this.linkPortuguese = linkPortuguese;
	}

	public List<Vote> getVotes() {
		return votes;
	}

	public void setVotes(List<Vote> votes) {
		this.votes = votes;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

}
