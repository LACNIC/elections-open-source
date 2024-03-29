package net.lacnic.elections.domain.services.dbtables;

import java.io.Serializable;
import java.util.Base64;

import net.lacnic.elections.domain.Candidate;


public class CandidateTableReport implements Serializable {

	private static final long serialVersionUID = 7063712150369421531L;

	private Long candidateId;
	private Long migrationId;
	private String name;
	private String mail;
	private Long electionId;
	private String pictureInfo;
	private String pictureName;
	private String bioSpanish;
	private String bioEnglish;
	private String bioPortuguese;
	private String pictureExtension;
	private Integer candidateOrder;
	private Boolean onlySp;
	private String linkSpanish;
	private String linkEnglish;
	private String linkPortuguese;


	public CandidateTableReport() { }

	public CandidateTableReport(Candidate candidate) {
		this.candidateId = candidate.getCandidateId();
		this.migrationId = candidate.getMigrationId();
		this.name = candidate.getName();
		this.mail = candidate.getMail();
		this.electionId = candidate.getElection().getElectionId();
		this.pictureInfo = Base64.getEncoder().encodeToString(candidate.getPictureInfo());
		this.pictureName = candidate.getPictureName();
		this.bioSpanish = candidate.getBioSpanish();
		this.bioEnglish = candidate.getBioEnglish();
		this.bioPortuguese = candidate.getBioPortuguese();
		this.pictureExtension = candidate.getPictureExtension();
		this.candidateOrder = candidate.getCandidateOrder();
		this.onlySp = candidate.isOnlySp();
		this.linkSpanish = candidate.getLinkSpanish();
		this.linkEnglish = candidate.getLinkEnglish();
		this.linkPortuguese = candidate.getLinkPortuguese();
	}


	public Long getCandidateId() {
		return candidateId;
	}

	public void setCandidateId(Long candidateId) {
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

	public Long getElectionId() {
		return electionId;
	}

	public void setElectionId(Long electionId) {
		this.electionId = electionId;
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

	public Integer getCandidateOrder() {
		return candidateOrder;
	}

	public void setCandidateOrder(Integer candidateOrder) {
		this.candidateOrder = candidateOrder;
	}

	public Boolean isOnlySp() {
		return onlySp;
	}

	public void setOnlySp(Boolean onlySp) {
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

	public String getPictureInfo() {
		return pictureInfo;
	}

	public void setPictureInfo(String pictureInfo) {
		this.pictureInfo = pictureInfo;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

}
