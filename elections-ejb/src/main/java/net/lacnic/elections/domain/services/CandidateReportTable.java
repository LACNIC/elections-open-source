package net.lacnic.elections.domain.services;

import java.io.Serializable;

import net.lacnic.elections.domain.Candidate;


public class CandidateReportTable implements Serializable {

	private static final long serialVersionUID = 7063712150369421531L;

	private long candidateId;
	private Long migrationId;
	private String name;
	private Long electionId;
	//	private byte[] pictureInfo;
	private String pictureName;
	private String bioSpanish;
	private String bioEnglish;
	private String bioPortuguese;
	private String pictureExtension;
	private int candidateOrder;
	private boolean onlySp;
	private String linkSpanish;
	private String linkEnglish;
	private String linkPortuguese;


	public CandidateReportTable() { }

	public CandidateReportTable(Candidate candidate) {
		this.candidateId = candidate.getCandidateId();
		this.migrationId = candidate.getMigrationId();
		this.name = candidate.getName();
		this.electionId = candidate.getElection().getElectionId();
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

}
